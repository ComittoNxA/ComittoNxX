//#define DEBUG

#include <time.h>
#include <malloc.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>
#include <gif_lib.h>

#include "Image.h"

extern char			*gLoadBuffer;
extern long			gLoadFileSize;
extern int			gLoadError;
static int			gLoadOffset;

extern BUFFMNG		*gBuffMng;
extern long			gBuffNum;

extern int			gCancel;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

const short InterlacedOffset[] = { 0, 4, 2, 1 }; /* The way Interlaced image should. */
const short InterlacedJumps[] = { 8, 8, 4, 2 };    /* be read - offsets and jumps... */

// データ読み込み用
int gif_memread_func(GifFileType* GifFile, GifByteType* buf, int count) {
    int copysize = count;

    if (gLoadOffset + count > gLoadFileSize) {
        copysize = gLoadFileSize - gLoadOffset;
    }

//	LOGI("gif_memread_func : total=%d, offset=%d, count=%d, copysize=%d", gLoadFileSize, gLoadOffset, count, copysize);

    if (copysize <= 0) {
        return 0;
    }

    memcpy(buf, gLoadBuffer + gLoadOffset, copysize);
    gLoadOffset += copysize;
    return copysize;
}

int DGifGetLineByte(GifFileType *GifFile, GifPixelType *pLineBuf, GifPixelType *pBackColor, int width)
{
    memcpy(pLineBuf, pBackColor, width);
    int result = DGifGetLine(GifFile, pLineBuf, width);
    return result;
}

void DGifSetData(WORD *buffptr, GifPixelType *pLineBuf, int yy, int width, ColorMapObject *ColorMap)
{
    int yd3 = gDitherY_3bit[yy & 0x07];
    int yd2 = gDitherY_2bit[yy & 0x03];

    for (int xx = 0 ; xx < width ; xx ++) {
        GifColorType* color = &ColorMap->Colors[pLineBuf[xx]];

        // 切り捨ての値を分散
        int rr = color->Red;
        int gg = color->Green;
        int bb = color->Blue;

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
    return;
}

// GIF読み込み
int LoadImageGif(int loadCommand, IMAGEDATA *pData, int page, int scale, WORD *canvas)
{
    int ret = 0;
    int error = 0;

    int buffindex;
    int buffpos;
    int linesize;
    WORD **pBuffPtrs = NULL;
    WORD *buffptr;

    int row, col, swidth, sheight, width, height, ExtCode;

    gLoadOffset = 0;

    GifRecordType RecordType;
    GifByteType *Extension;
    ColorMapObject *ColorMap;

    GifPixelType *pLineBuff = NULL;
    GifPixelType *pBackColor = NULL;

    //画像ファイル読み込み
    GifFileType *GifFile = DGifOpen(NULL, gif_memread_func, &error);
    if (GifFile == NULL) {
        ret = -10;
        goto ERROREND;
    }

    swidth = GifFile->SWidth;
    sheight = GifFile->SHeight;

    /* Scan the content of the GIF file and load the image(s) in: */
    do {
//		LOGD("DEBUG1:GetRecordType", RecordType);
        if (DGifGetRecordType(GifFile, &RecordType) == GIF_ERROR) {
            ret = -11;
            goto ERROREND;
        }
//		LOGD("DEBUG2:RecordType=%d (%d,%d,%d)", RecordType, IMAGE_DESC_RECORD_TYPE, EXTENSION_RECORD_TYPE, TERMINATE_RECORD_TYPE);

        switch (RecordType) {
            case IMAGE_DESC_RECORD_TYPE:
            {
//				LOGD("DEBUG4:Interface=%d", GifFile->Image.Interlace);
                if (DGifGetImageDesc(GifFile) == GIF_ERROR) {
                    ret = -12;
                    goto ERROREND;
                }

                /* Image Position relative to Screen. */
                row = GifFile->Image.Top;
                col = GifFile->Image.Left;
                width = GifFile->Image.Width;
                height = GifFile->Image.Height;

                buffindex = -1;
                buffpos   = 0;
                linesize  = (width + HOKAN_DOTS);
                buffptr   = NULL;

//				LOGI("LoadImageGif : sw=%d, sh=%d w=%d h=%d, col=%d, row=%d", swidth, sheight, width, height, col, row);

                pLineBuff = (GifPixelType*)malloc(sizeof(GifPixelType) * width);
                pBackColor = (GifPixelType*)malloc(sizeof(GifPixelType) * width);
                pBuffPtrs = (WORD**)malloc(sizeof(WORD*) * height);

                // カラーマップ
                ColorMap = (GifFile->Image.ColorMap ? GifFile->Image.ColorMap : GifFile->SColorMap);

                if (GifFile->Image.Left + GifFile->Image.Width > GifFile->SWidth ||
                    GifFile->Image.Top + GifFile->Image.Height > GifFile->SHeight) {
                    ret = -13;
                    goto ERROREND;
                }

                for (int yy = 0 ; yy < height ; yy ++) {
//					LOGD("DEBUG5:buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

                    // ライン毎のバッファの位置を保存
                    if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
                        for (buffindex ++ ; buffindex < gBuffNum ; buffindex ++) {
                            if (gBuffMng[buffindex].Page == -1) {
                                break;
                            }
                        }
                        if (buffindex >= gBuffNum) {
                            // 領域不足
                            ret = -15;
                            break;
                        }
                        buffpos = 0;
                        gBuffMng[buffindex].Page = page;
                        gBuffMng[buffindex].Type = 0;
                        gBuffMng[buffindex].Half = 0;
                        gBuffMng[buffindex].Size = 0;
                        gBuffMng[buffindex].Index = 0;
                    }

                    pBuffPtrs[yy] = gBuffMng[buffindex].Buff + buffpos;

                    // go to next line
                    buffpos += linesize;
                    gBuffMng[buffindex].Size += linesize;
                }

                /* 背景色を設定する */
                for (int i = 0; i < width; i++) {
                    pBackColor[i] = GifFile->SBackGroundColor;
                }

                int interlace = 0;
                int line = InterlacedOffset[0];
                for(int yy = 0; yy < height; yy++) {
                    if (gCancel) {
                        LOGD("LoadImageGif : cancel.");
                        ReleaseBuff(page, -1, -1);
                        ret = -14;
                        break;
                    }

//					LOGD("DEBUG3:yy=%d, il=%d, line=%d, interlace=%d", yy, GifFile->Image.Interlace, line, interlace);
                    if (GifFile->Image.Interlace) {
                        /* Need to perform 4 passes on the images: */
                        if (DGifGetLineByte(GifFile, pLineBuff, pBackColor, width) == GIF_ERROR) {
                            ret = -16;
                            goto ERROREND;
                        }

                        buffptr = pBuffPtrs[line];
                        // 次のラインへ
                        line += InterlacedJumps[interlace];
                        if (line >= height) {
                            interlace ++;
                            line = InterlacedOffset[interlace];
                        }
                    }
                    else {
                        if (DGifGetLineByte(GifFile, pLineBuff, pBackColor, width) == GIF_ERROR) {
                            ret = -17;
                            goto ERROREND;
                        }
                        buffptr = pBuffPtrs[yy];
                    }

                    DGifSetData(buffptr + 2, pLineBuff, yy, width, ColorMap);

                    // 補完用の余裕
                    buffptr[0] = buffptr[2];
                    buffptr[1] = buffptr[2];
                    buffptr[width + 2] = buffptr[width + 1];
                    buffptr[width + 3] = buffptr[width + 1];
                }
                break;
            }
            case EXTENSION_RECORD_TYPE:
                /* EXTENSION BLOCKS */
                if (DGifGetExtension(GifFile, &ExtCode, &Extension) == GIF_ERROR) {
                    ret = -18;
                    goto ERROREND;
                }
                while (Extension != NULL) {
                    if (DGifGetExtensionNext(GifFile, &Extension) == GIF_ERROR) {
                        ret = -19;
                        goto ERROREND;
                    }
                }
                break;
            case TERMINATE_RECORD_TYPE:
                break;
            default:		    /* Should be traps by DGifGetRecordType. */
                break;
        }
    } while (RecordType != TERMINATE_RECORD_TYPE);

    pData->UseFlag = 1;
    pData->OrgWidth = width;
    pData->OrgHeight = height;

    ERROREND:
    if (GifFile != NULL) {
        EGifCloseFile(GifFile, &error);
    }
    if (pLineBuff != NULL) {
        free(pLineBuff);
    }
    if (pBackColor != NULL) {
        free(pBackColor);
    }
    if (pBuffPtrs != NULL) {
        free(pBuffPtrs);
    }
    return ret;
}
