#include <time.h>
#include <malloc.h>
#include <string.h>
#include <arpa/inet.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#include <mutex>
#endif

#include "Image.h"

#define synchronized(monitor) \
  if (auto __lock = std::make_unique<std::lock_guard<std::mutex>>(monitor))
std::mutex monitor_;

extern bool         gIsInit[];
extern IMAGEDATA	*gImageData[];
extern char			*gLoadBuffer[];
extern LONG			**gLinesPtr[];
/** 行のポインタ保持 */
extern LONG			**gDsLinesPtr[];
extern LONG			**gSclLinesPtr[];

extern long			gTotalPages[];
extern long			gLoadBuffSize[];

extern BUFFMNG		*gBuffMng[];
extern long			gBuffNum[];

extern BUFFMNG		*gSclBuffMng[];
extern long			gSclBuffNum[];

extern int			gCancel[];

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

/** サイズ変更時の画像補間計算に利用する領域 */
long long	*gSclLLongParam[MAX_BUFFER_INDEX] = {nullptr};
/** サイズ変更時の画像補間計算に利用する領域 */
int			*gSclIntParam1[MAX_BUFFER_INDEX] = {nullptr};
/** サイズ変更時の画像補間計算に利用する領域 */
int			*gSclIntParam2[MAX_BUFFER_INDEX] = {nullptr};
/** サイズ変更時の画像補間計算に利用する領域 */
int			*gSclIntParam3[MAX_BUFFER_INDEX] = {nullptr};
int			gMaxColumn[MAX_BUFFER_INDEX] = {0};
int			gMaxLine[MAX_BUFFER_INDEX] = {0};

int DrawBitmap(int index, int page, int half, int x, int y, void *canvas, int width, int height, int stride, IMAGEDATA *pData)
{
	WORD	*pixels = (WORD*)canvas;
	int		image_width;
	int		image_height;

	if (pData->SclFlag[half] == 0) {
		//LOGE("DrawBitmap/0: SclFlag[%d] == 0", half);
		return -5;
	}

//	if (pData->SclFlag == 0) {
//		image_width  = pData->OrgWidth;
//		image_height = pData->OrgHeight;
//		images =  pData->OrgBuff;
//	}
//	else {
		image_width  = pData->SclWidth[half];
		image_height = pData->SclHeight[half];
//	}
	
	int imx = image_width;
	int imy = image_height;
	int xpos = 0, ypos = 0;

	if (y > 0) {
		// 描画開始位置をずらす
		pixels = (WORD*)(((char*)pixels) + stride * y);
		height -= y;
	}
	else if (y < 0) {
		ypos = -y;
		imy += y;
	}

	if (x > 0) {
        // 横方向は偶数でずらす
        pixels = pixels + x * 2;
        width -= x;
	}
	else if (x < 0) {
		xpos = -x;
		imx += x;
	}

	int lines = (height < imy) ? height : imy;
	int dots  = (width < imx) ? width : imx;

	if (lines < 0 || dots < 0) {
		return 1;
	}

//	LOGD("DrawBitmap : x=%d, y=%d, w=%d, h=%d, s=%d, l=%d, d=%d, xp=%d, yp=%d", x, y, width, height, stride, lines, dots, xpos, ypos);

	int		yy;
    int	loopx;
    int rgb;
    LONG	*pixeldata;

	int buffindex = -1;
	int buffpos = 0;
	int linesize = image_width + HOKAN_DOTS;
	LONG *buffptr = nullptr;

	// バッファのスキップ
	for (yy = 0 ; yy < ypos ; yy ++) {
		if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
			for (buffindex ++ ; buffindex < gBuffNum[index] ; buffindex ++) {
				if (gBuffMng[index][buffindex].Page == page && gBuffMng[index][buffindex].Type == 1 && gBuffMng[index][buffindex].Half == half) {
					break;
				}
			}
			if (buffindex >= gBuffNum[index]) {
				// 領域不足
				LOGE("DrawBitmap/1: Data Error page=%d, buffindex=%d", page, buffindex);
				return ERROR_CODE_CACHE_IS_FULL;
			}
			buffpos = 0;
		}
		buffpos += linesize;
//		LOGD("DEBUG1:yy=%d/%d, idx=%d, pos=%d", yy, ypos, buffindex, buffpos);
	}

	for (yy = 0 ; yy < lines ; yy++) {
		if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
			for (buffindex ++ ; buffindex < gBuffNum[index] ; buffindex ++) {
				if (gBuffMng[index][buffindex].Page == page && gBuffMng[index][buffindex].Type == 1 && gBuffMng[index][buffindex].Half == half) {
					break;
				}
			}
			if (buffindex >= gBuffNum[index]) {
				// 領域不足
				LOGE("DrawBitmap/2: Data Error page=%d, line=%d, buffindex=%d", page, yy, buffindex);
				return ERROR_CODE_CACHE_IS_FULL;
			}
			buffpos = 0;
		}
//		LOGD("DEBUG2:yy=%d, idx=%d, pos=%d, off=%d", yy, buffindex, buffpos, buffpos + xpos + HOKAN_DOTS / 2);
        pixeldata = &gBuffMng[index][buffindex].Buff[buffpos + xpos + HOKAN_DOTS / 2];
        for	(loopx = 0 ; loopx < dots ; loopx++)	{
            rgb = *(pixeldata + loopx);
            *(pixels + loopx * 2 + 0) = rgb & 0xffff;
            *(pixels + loopx * 2 + 1) = (rgb >> 16) & 0xffff;
        }

		pixels = (WORD*)(((char*)pixels) + stride);
		buffpos += linesize;
	}
	return 0;
}

int DrawScaleBitmap(int index, int page, int rotate, int s_x, int s_y, int s_cx, int s_cy, int d_x, int d_y, int d_cx, int d_cy, void *canvas, int width, int height, int stride, int psel, IMAGEDATA *pData, int cut_left, int cut_right, int cut_top, int cut_bottom)
{
//#define DEBUG_DRAW_SCALE_BITMAP
#ifdef DEBUG_DRAW_SCALE_BITMAP
	LOGD("DrawScaleBitmap: start: page=%d, rote=%d, s(x=%d, y=%d, cx=%d, cy=%d)-d(x=%d, y=%d, cx=%d, cy=%d) / (w=%d, h=%d, s=%d) / p=%d cut(l=%d, r=%d, t=%d, b=%d)"
			, page, rotate
			, s_x, s_y, s_cx, s_cy
			, d_x, d_y, d_cx, d_cy
			, width, height, stride, psel
			, cut_left, cut_right, cut_top, cut_bottom);
#endif
    int ret = 0;

	WORD	*pixels = (WORD*)canvas;
	int		image_width  = pData->OrgWidth;
	int		image_height = pData->OrgHeight;

	if (pData->UseFlag == 0) {
		return -5;
	}

	int buffindex;
	int buffpos = 0;
	int linesize = (pData->OrgWidth + HOKAN_DOTS);
	int lineindex = 0;

	// 領域確保
    ret = ScaleMemLine(index, pData->OrgHeight - cut_top - cut_bottom);
	if (ret < 0) {
		return ret;
	}

	buffindex = -1;
	for (lineindex = 0 ; lineindex < pData->OrgHeight - cut_bottom ; lineindex ++) {
		if (gCancel[index]) {
//			LOGD("DrawScaleBitmap : cancel.");
			return ERROR_CODE_USER_CANCELED;
		}
		if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
			for (buffindex ++ ; buffindex < gBuffNum[index] ; buffindex ++) {
				if (gBuffMng[index][buffindex].Page == page && gBuffMng[index][buffindex].Type == 0) {
					break;
				}
			}
			if (buffindex >= gBuffNum[index]) {
				// 領域不足
				LOGE("DrawScaleBitmap: Data Error page=%d, lineindex=%d/%d", page, lineindex, (int)pData->OrgHeight);
				return ERROR_CODE_CACHE_IS_FULL;
			}
			buffpos = 0;
		}
//		LOGD("DrawScaleBitmap : lineindex=%d, buffindex=%d, buffpos=%d", lineindex, buffindex, buffpos);
        if (lineindex - cut_top >= 0) {
    		gDsLinesPtr[index][lineindex - cut_top] = gBuffMng[index][buffindex].Buff + buffpos + cut_left + HOKAN_DOTS / 2;
		}
		buffpos += linesize;
	}

	int		yy, xx, yi, xi;

	int OrgWidth;
	int OrgHeight;

	if (rotate == 0 || rotate == 2) {
		OrgWidth  = pData->OrgWidth - cut_left - cut_right;
		OrgHeight = pData->OrgHeight - cut_top - cut_bottom;
	}
	else {
		OrgWidth  = pData->OrgHeight - cut_top - cut_bottom;
		OrgHeight = pData->OrgWidth - cut_left - cut_right;
	}

	if (psel == 0) {
		int		ypos;
		int		*xpos;

		xpos = (int*)malloc(sizeof(int) * d_cx);
        if (xpos == nullptr) {
            LOGE("DrawScaleBitMap : malloc error. (xpos)");
            return ERROR_CODE_MALLOC_FAILURE;
        }
		for (xx = 0 ; xx < d_cx ; xx ++) {
			// ソースの座標計算
			xpos[xx] = (s_x + (xx * s_cx / d_cx)) * (pData->OrgWidth - cut_left - cut_right) / pData->OrgWidth;
		}

//	LOGD("DrawScaleBitmap : page=%d, OrgWidth=%d, pData->OrgWidth=%d, OrgHeight=%d, pData->OrgHeight=%d", page, OrgWidth, pData->OrgWidth, OrgHeight, pData->OrgHeight);

		// 横固定90回転なし
		for (yy = 0 ; yy < d_cy ; yy++) {
			// ソースの座標計算
			ypos = (s_y + (yy * s_cy / d_cy)) * (pData->OrgHeight - cut_top - cut_bottom) / pData->OrgHeight;
			if (0 <= ypos && ypos < OrgHeight) {
				for (xx = 0 ; xx < d_cx ; xx++) {
					if (0 <= xpos[xx] && xpos[xx] < OrgWidth) {
						if (rotate == 0) {
							xi = xpos[xx];
							yi = ypos;
						}
						else if (rotate == 1) {
							xi = ypos;
							yi = OrgWidth - (xpos[xx] + 1);
						}
						else if (rotate == 2) {
							xi = OrgWidth - (xpos[xx] + 1);
							yi = OrgHeight - (ypos + 1);
						}
						else {
							xi = OrgHeight - (ypos + 1);
							yi = xpos[xx];
						}
                        pixels[xx * 2 + 0] = gDsLinesPtr[index][yi][xi] & 0xffff;
                        pixels[xx * 2 + 1] = ((gDsLinesPtr[index][yi][xi] >> 16) & 0xffff);
					}
				}
			}
			// 描画先バッファの1行目から順に描く
			pixels = (WORD*)(((char*)pixels) + stride);
		}
		free (xpos);
	}
	else {
		// 横固定90回転あり
		// 描画先のサイズは横長が指定されているが、実体は縦長
		int		*ypos;
		int		xpos;

		ypos = (int*)malloc(sizeof(int) * d_cy);
        if (ypos == nullptr) {
            LOGE("DrawScaleBitMap : malloc error. (ypos)");
            return ERROR_CODE_MALLOC_FAILURE;
        }
		for (yy = 0 ; yy < d_cy ; yy ++) {
			// ソースの座標計算
			ypos[yy] = (s_y + ((d_cy - yy - 1) * s_cy / d_cy)) * (pData->OrgHeight - cut_top - cut_bottom) / pData->OrgHeight;
		}

		for (xx = 0 ; xx < d_cx ; xx++) {
			// ソースの座標計算
			xpos = (s_x + (xx * s_cx / d_cx)) * (pData->OrgWidth - cut_left - cut_right) / pData->OrgWidth;
			if (0 <= xpos && xpos < OrgWidth) {
				for (yy = 0 ; yy < d_cy ; yy++) {
					// yy=0のとき画面では一番下！
					if (0 <= ypos[yy] && ypos[yy] < OrgHeight) {
                        if (rotate == 0) {
                            xi = xpos;
                            yi = ypos[yy];
                        }
                        else if (rotate == 1) {
                            xi = ypos[yy];
                            yi = OrgWidth - (xpos + 1);
                        }
                        else if (rotate == 2) {
                            xi = OrgWidth - (xpos + 1);
                            yi = OrgHeight - (ypos[yy] + 1);
                        }
                        else {
                            xi = OrgHeight - (ypos[yy] + 1);
                            yi = xpos;
                        }
                        pixels[xx * 2 + 0] = gDsLinesPtr[index][yi][xi] & 0xffff;
                        pixels[xx * 2 + 1] = ((gDsLinesPtr[index][yi][xi] >> 16) & 0xffff);
                    }
				}
			}
			pixels = (WORD*)(((char*)pixels) + stride);
		}
		free (ypos);
	}
#ifdef DEBUG_DRAW_SCALE_BITMAP
    LOGD("DrawScaleBitmap: end:");
#endif
	return 0;
}

// メモリ確保
int MemAlloc(int index, int buffsize)
{
    int buffnum = buffsize * 2;
    int ret = index;

    gIsInit[index] = true;

    synchronized (monitor_) {
        gLoadBuffer[index] = (char *) malloc(gLoadBuffSize[index]);
        if (gLoadBuffer[index] == nullptr) {
            LOGE("MemAlloc: malloc error.(LoadBuff)");
            ret = ERROR_CODE_MALLOC_FAILURE;
            goto ERROREND;
        }

        gImageData[index] = (IMAGEDATA *) malloc(sizeof(IMAGEDATA) * gTotalPages[index]);
        if (gImageData[index] == nullptr) {
            LOGE("MemAlloc: malloc error.(ImageData)");
            ret = ERROR_CODE_MALLOC_FAILURE;
            goto ERROREND;
        }
        memset(gImageData[index], 0, sizeof(IMAGEDATA) * gTotalPages[index]);

        gBuffMng[index] = (BUFFMNG *) malloc(sizeof(BUFFMNG) * buffnum);
        if (gBuffMng[index] == nullptr) {
            LOGE("MemAlloc: malloc error.(BuffMng)");
            ret = ERROR_CODE_MALLOC_FAILURE;
            goto ERROREND;
        }

        int i;
        for (i = 0; i < buffnum; i++) {
            gBuffMng[index][i].Page = -1;    // 未使用状態に初期化
            gBuffMng[index][i].Buff = (LONG *) malloc(BLOCKSIZE * sizeof(LONG));
            if (gBuffMng[index][i].Buff == nullptr) {
                LOGE("MemAlloc: malloc error.(Buff / index=%d)", i);
                ret = ERROR_CODE_MALLOC_FAILURE;
                goto ERROREND;
            }
            gBuffMng[index][i].Page = -1;
            gBuffNum[index] = i;
        }

        // 拡大縮小画像領域確保
        gSclBuffMng[index] = (BUFFMNG *) malloc(sizeof(BUFFMNG) * SCLBUFFNUM);
        if (gSclBuffMng[index] == nullptr) {
            LOGE("MemAlloc: malloc error.(SclBuffMng)");
            ret = ERROR_CODE_MALLOC_FAILURE;
            goto ERROREND;
        }
        gSclBuffNum[index] = 0;

        // 保存先ラインポインタ確保
        gSclLinesPtr[index] = (LONG **) malloc(sizeof(LONG *) * MAX_LINES);
        if (gSclLinesPtr[index] == nullptr) {
            LOGE("MemAlloc: malloc error.(SclLineBuffPtr)");
            ret = ERROR_CODE_MALLOC_FAILURE;
            goto ERROREND;
        }

        return ret;
    }

ERROREND:
    MemFree(index);
    return ret;
}

// メモリ解放
void MemFree(int index)
{

    synchronized (monitor_) {
        // 読み込みバッファの解放
        if (gLoadBuffer[index] != nullptr) {
            free(gLoadBuffer[index]);
            gLoadBuffer[index] = nullptr;
        }

        // イメージ管理バッファの解放
        if (gBuffMng[index] != nullptr) {
            for (int i = 0; i < gBuffNum[index]; i++) {
                if (gBuffMng[index][i].Buff != nullptr) {
                    free(gBuffMng[index][i].Buff);
                    gBuffMng[index][i].Buff = nullptr;
                }
            }
            free(gBuffMng[index]);
            gBuffMng[index] = nullptr;
        }
        gBuffNum[index] = 0;

        // 拡大縮小画像領域解放
        if (gSclBuffMng[index] != nullptr) {
            for (int i = 0; i < gSclBuffNum[index]; i++) {
                if (gSclBuffMng[index][i].Buff != nullptr) {
                    free(gSclBuffMng[index][i].Buff);
                    gSclBuffMng[index][i].Buff = nullptr;
                }
            }
            free(gSclBuffMng[index]);
            gSclBuffMng[index] = nullptr;
        }
        gSclBuffNum[index] = 0;

        // 保存先ラインポインタ
        if (gSclLinesPtr[index] != nullptr) {
            free(gSclLinesPtr[index]);
            gSclLinesPtr[index] = nullptr;
        }

        ScaleMemLineFree(index);
        ScaleMemColumnFree(index);
    }

    gIsInit[index] = false;

#ifdef __ANDROID_UNAVAILABLE_SYMBOLS_ARE_WEAK__
    if (__builtin_available(android 28, *)) {
#else
    if (__ANDROID_API__ >= 28) {
#endif
        //LOGD("MemFree: 解放します.");
        mallopt(M_PURGE, 0);
    }
}

// 拡大縮小用メモリ初期化
int ScaleMemInit(int index)
{
	// 拡大縮小画像領域チェック
	if (gSclBuffMng[index] == nullptr) {
		return -1;
	}

	int		i;
	for (i = 0 ; i < gSclBuffNum[index] ; i ++) {
		gSclBuffMng[index][i].Page = -1;	// 未使用状態に初期化
		gSclBuffMng[index][i].Count = -1;
	}
	return 0;
}

//*
/**
 * サイズ変更画像待避用領域確保
 * @param index     作業領域のチャンネルを指定する番号
 * @param linesize  1行の幅(+HOKAN_DOTS)
 * @param linenum   行数(=高さ)
 * @return          エラー発生時はマイナスの値
 */
int ScaleMemAlloc(int index, int linesize, int linenum)
{
    /** 1ブロックに何行分確保できるか */
	int NumOfLines = (BLOCKSIZE / linesize);
    /** 全部で何ブロック必要か */
	int buffnum  = (linenum + NumOfLines - 1) / NumOfLines;
	int ret = 0;

#ifdef DEBUG
	LOGD("ScaleMemAlloc : 開始します. linesize=%d, linenum=%d / nol=%d, bn=%d", linesize, linenum, NumOfLines, buffnum);
#endif

	// 拡大縮小画像領域チェック
	if (gSclBuffMng[index] == nullptr) {
		return -1;
	}

	int	i;
    /** 未試用領域の数 */
	int	count = 0;
	for (i = 0 ; i < gSclBuffNum[index] ; i ++) {
		if (gSclBuffMng[index][i].Count == -1) {	// 未使用領域をカウント
			count ++;
		}
	}

#ifdef DEBUG
	LOGD("ScaleMemAlloc : sbn=%d, count=%d" , (int)gSclBuffNum[index], count);
#endif

    if (SCLBUFFNUM < gSclBuffNum[index] + (buffnum - count)) {
        // 確保できなかった
        return ERROR_CODE_CACHE_IS_FULL;
    }

	// 不足分を確保
	for (i = gSclBuffNum[index] ; i < SCLBUFFNUM && i < gSclBuffNum[index] + (buffnum - count) ; i ++) {
		gSclBuffMng[index][i].Buff = (LONG*)malloc(BLOCKSIZE * sizeof(LONG));
		if (gSclBuffMng[index][i].Buff == nullptr) {
			LOGE("Initialize: malloc error.(SclBuffMng / index=%d)", i);
			gSclBuffNum[index] = i;
			return ERROR_CODE_MALLOC_FAILURE;
		}
		gSclBuffMng[index][i].Page = -1;	// 未使用状態に初期化
		gSclBuffMng[index][i].Count = -1;
	}
	gSclBuffNum[index] = i;
	if (i < (buffnum - count)) {
		// 確保できなかった
		return ERROR_CODE_CACHE_IS_FULL;
	}

#ifdef DEBUG
	LOGD("ScaleMemAlloc : end");
#endif
	return ret;
}

/**
 * サイズ変更時の画像補間計算に利用する領域を確保する
 * @param index
 * @param SclWidth
 * @return
 */
int ScaleMemColumn(int index, int SclWidth)
{
	if (gMaxColumn[index] < SclWidth) {
		// 幅が今まで使っていた物よりも大きければ再確保
		ScaleMemColumnFree(index);

		gSclLLongParam[index] = (long long*)malloc(sizeof(long long) * SclWidth);
		gSclIntParam1[index]  = (int*)malloc(sizeof(int) * SclWidth);
		gSclIntParam2[index]  = (int*)malloc(sizeof(int) * SclWidth);
		gSclIntParam3[index]  = (int*)malloc(sizeof(int) * SclWidth);

		if (gSclLLongParam[index] == nullptr || gSclIntParam1[index] == nullptr || gSclIntParam2[index] == nullptr || gSclIntParam3[index] == nullptr) {
			LOGE("ScaleMemColumn: MAlloc Error.");
			gMaxColumn[index] = 0;
			ScaleMemColumnFree(index);
			return ERROR_CODE_MALLOC_FAILURE;
		}
		gMaxColumn[index] = SclWidth;
	}
	return 0;
}

int ScaleMemLine(int index, int SclHeight)
{
//	LOGD("ScaleMemLine : SclHeight=%d", SclHeight);
	if (gMaxLine[index] < SclHeight) {
		// 高さが今まで使っていた物よりも大きければ再確保
		ScaleMemLineFree(index);

		gLinesPtr[index] = (LONG**)malloc(sizeof(LONG*) * (SclHeight + HOKAN_DOTS));
		gDsLinesPtr[index] = (LONG**)malloc(sizeof(LONG*) * SclHeight);

		if (gLinesPtr[index] == nullptr || gDsLinesPtr[index] == nullptr) {
			LOGE("ScaleMemLine: MAlloc Error.");
			gMaxLine[index] = 0;
			ScaleMemLineFree(index);
			return ERROR_CODE_MALLOC_FAILURE;
		}
		gMaxLine[index] = SclHeight;
	}
	return 0;
}

void ScaleMemColumnFree(int index)
{
	gMaxColumn[index] = 0;
	if (gSclLLongParam[index] != nullptr) {
		free(gSclLLongParam[index]);
		gSclLLongParam[index] = nullptr;
	}
	if (gSclIntParam1[index] != nullptr) {
		free(gSclIntParam1[index]);
		gSclIntParam1[index] = nullptr;
	}
	if (gSclIntParam2[index] != nullptr) {
		free(gSclIntParam2[index]);
		gSclIntParam2[index] = nullptr;
	}
	if (gSclIntParam3[index] != nullptr) {
		free(gSclIntParam3[index]);
		gSclIntParam3[index] = nullptr;
	}
}

void ScaleMemLineFree(int index)
{
	gMaxLine[index] = 0;
	if (gLinesPtr[index] != nullptr) {
		free (gLinesPtr[index]);
		gLinesPtr[index] = nullptr;
	}

	if (gDsLinesPtr[index] != nullptr) {
		free (gDsLinesPtr[index]);
		gDsLinesPtr[index] = nullptr;
	}
}

void CheckImageType(int index, int *type)
{
    if (strncmp(gLoadBuffer[index]+6, "JFIF", 4)==0) {
        *type = IMAGETYPE_JPEG;
    }
    else if (strncmp(gLoadBuffer[index]+1,"PNG",3)==0) {
        *type = IMAGETYPE_PNG;
    }
    else if (strncmp(gLoadBuffer[index],"GIF87a",6)==0 || strncmp(gLoadBuffer[index],"GIF89a",6)==0) {
        *type = IMAGETYPE_GIF;
    }
    else if (strncmp(gLoadBuffer[index],"RIFF",4)==0 && strncmp(gLoadBuffer[index]+8,"WEBP",4)==0) {
        *type = IMAGETYPE_WEBP;
    }
    else if (strncmp(gLoadBuffer[index]+4,"ftypavif",8)==0) {
        *type = IMAGETYPE_AVIF;
    }
    else if (
        strncmp(gLoadBuffer[index]+4,"ftypheic",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftypheix",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftyphevc",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftypheim",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftypheis",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftyphevm",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftypmif1",8)==0 ||
        strncmp(gLoadBuffer[index]+4,"ftypmsf1",8)==0
    ) {
        *type = IMAGETYPE_HEIF;
    }
    else if (
        ((uint8_t)*gLoadBuffer[index] == 0xFF && (uint8_t)*gLoadBuffer[index] == 0x0A) ||
        strncmp(gLoadBuffer[index]+4,"JXL",3)==0
    ) {
        *type = IMAGETYPE_JXL;
    }
    else {
//		LOGD("ImageConvert : Judge - ELSE(%d)", type);
    }
}


int SetBuff(int index, int page, uint32_t width, uint32_t height, uint8_t *data, colorFormat colorFormat)
{
    int returnCode = 0;

    int buffindex = -1;
    int buffpos = 0;
    int linesize = (width + HOKAN_DOTS);
    int ret = 0;
    LONG *buffptr = nullptr;

    int yy, xx, yd3, yd2;
    int rr, gg, bb;

    // 画像バッファにデータを格納する処理
    for(yy = 0; yy < height; yy++)
    {
        // キャンセルされたら終了する
        if (gCancel[index]) {
            LOGD("SetBuff: cancel.");
            ReleaseBuff(index, page, -1, -1);
            returnCode = ERROR_CODE_USER_CANCELED;
            break;
        }

        // ライン毎のバッファの位置を保存
        if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
            for (buffindex++; buffindex < gBuffNum[index] ; buffindex++) {
                if (gBuffMng[index][buffindex].Page == -1) {
                    break;
                }
            }
            if (buffindex >= gBuffNum[index]) {
                LOGE("SetBuff: Out of memory.");
                // 領域不足
                returnCode = ERROR_CODE_CACHE_IS_FULL;
                break;
            }
            buffpos = 0;
            gBuffMng[index][buffindex].Page = page;
            gBuffMng[index][buffindex].Type = 0;
            gBuffMng[index][buffindex].Half = 0;
            gBuffMng[index][buffindex].Size = 0;
            gBuffMng[index][buffindex].Count = 0;
        }

        buffptr = gBuffMng[index][buffindex].Buff + buffpos + HOKAN_DOTS / 2;

        // データセット
        yd3 = gDitherY_3bit[yy & 0x07];
        yd2 = gDitherY_2bit[yy & 0x03];

        for (xx = 0 ; xx < width ; xx++) {
            if (colorFormat == COLOR_FORMAT_RGB565) {
                WORD rgb = *(WORD *)data; data += sizeof(WORD);
                rr = RGB565_RED_256(rgb);
                gg = RGB565_GREEN_256(rgb);
                bb = RGB565_BLUE_256(rgb);
            }
            else {
                if (colorFormat == COLOR_FORMAT_ARGB || colorFormat == COLOR_FORMAT_ABGR) {
                    data++;
                }
                if (colorFormat == COLOR_FORMAT_RGB || colorFormat == COLOR_FORMAT_RGBA ||
                    colorFormat == COLOR_FORMAT_ARGB) {
                    rr = *data;
                    data++;
                    gg = *data;
                    data++;
                    bb = *data;
                    data++;
                } else if (colorFormat == COLOR_FORMAT_BGR || colorFormat == COLOR_FORMAT_BGRA ||
                           colorFormat == COLOR_FORMAT_ABGR) {
                    bb = *data;
                    data++;
                    gg = *data;
                    data++;
                    rr = *data;
                    data++;
                }
                if (colorFormat == COLOR_FORMAT_RGBA || colorFormat == COLOR_FORMAT_BGRA) {
                    data++;
                }
                if (colorFormat == COLOR_FORMAT_GRAYSCALE) {
                    rr = gg = bb = *data;
                    data++;
                }
            }
            buffptr[xx] = MAKE8888(rr, gg, bb);
        }

        // 補完用の余裕
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[width + 0] = buffptr[width - 1];
        buffptr[width + 1] = buffptr[width - 1];

        // go to next line
        buffpos += linesize;
        gBuffMng[index][buffindex].Size += linesize;
    }
    return returnCode;
}

int SetBitmap(int index, int page, uint32_t width, uint32_t height, uint8_t *data, colorFormat colorFormat, WORD *canvas)
{
    int returnCode = 0;

    int buffindex = -1;
    int buffpos = 0;
    int linesize = width;
    int ret = 0;
    WORD *buffptr = nullptr;

    int yy, xx, yd3, yd2;
    int rr, gg, bb;

#ifdef DEBUG
    LOGD("SetBitmap: Start. page=%d, width=%d, height=%d, colorFormat=%d", page, width, height, colorFormat);
#endif
    if (canvas == nullptr) {
        LOGE("SetBitmap: canvas is null.");
        return -100;
    }
        // 画像バッファにデータを格納する処理
    for(yy = 0; yy < height; yy++)
    {
        // キャンセルされたら終了する
        if (gCancel[index]) {
            LOGD("SetBitmap: cancel.");
            ReleaseBuff(index, page, -1, -1);
            returnCode = ERROR_CODE_USER_CANCELED;
            break;
        }

        buffptr = canvas + buffpos;

        // データセット
        yd3 = gDitherY_3bit[yy & 0x07];
        yd2 = gDitherY_2bit[yy & 0x03];

        for (xx = 0 ; xx < width ; xx++) {
            if (colorFormat == COLOR_FORMAT_ARGB || colorFormat == COLOR_FORMAT_ABGR) {
                data++;
            }
            if (colorFormat == COLOR_FORMAT_RGB || colorFormat == COLOR_FORMAT_RGBA || colorFormat == COLOR_FORMAT_ARGB) {
                rr = *data; data++;
                gg = *data; data++;
                bb = *data; data++;
            }
            else if (colorFormat == COLOR_FORMAT_BGR || colorFormat == COLOR_FORMAT_BGRA || colorFormat == COLOR_FORMAT_ABGR) {
                bb = *data; data++;
                gg = *data; data++;
                rr = *data; data++;
            }
            if (colorFormat == COLOR_FORMAT_RGBA || colorFormat == COLOR_FORMAT_BGRA) {
                data++;
            }
            if (colorFormat == COLOR_FORMAT_GRAYSCALE) {
                rr = gg = bb = *data; data++;
            }

            // 切り捨ての値を分散
            if (rr < 0xF8) {
                rr = rr + gDitherX_3bit[rr & 0x07][(xx + yd3) & 0x07];
            }
            if (gg < 0xFC) {
                gg = gg + gDitherX_2bit[gg & 0x03][(xx + yd2) & 0x03];
            }
            if (bb < 0xF8) {
                bb = bb + gDitherX_3bit[bb & 0x07][(xx + yd3) & 0x07];
            }
            buffptr[xx] = MAKE565(rr, gg, bb);
        }

        // go to next line
        buffpos += linesize;
    }

#ifdef DEBUG
    LOGD("SetBitmap: End. return=%d", returnCode);
#endif
    return returnCode;
}

int ReleaseBuff(int index, int page, int type, int half)
{
	// ページデータ解放
	int	i;
	for (i = 0 ; i < gBuffNum[index] ; i ++) {
		if (gBuffMng[index][i].Page == page || page == -1) {
			if (gBuffMng[index][i].Type == type || type == -1) {
				if (gBuffMng[index][i].Half == half || half == -1) {
					// 使用状況設定
//					LOGD("ReleaseBuff : index=%d, page=%d, type=%d, half=%d, size=%d",
//							i, gBuffMng[index][i].Page, gBuffMng[index][i].Type, gBuffMng[index][i].Half, gBuffMng[index][i].Size );
					gBuffMng[index][i].Page = -1;
					gBuffMng[index][i].Type = 0;
					gBuffMng[index][i].Half = 0;
					gBuffMng[index][i].Size = 0;
				}
			}
		}
	}
	return 0;
}
