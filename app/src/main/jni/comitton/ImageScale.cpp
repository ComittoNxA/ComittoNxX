#include <malloc.h>
#include <string.h>
#include <math.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#include <jni.h>

#endif

#include "Image.h"

//#define DEBUG

extern IMAGEDATA	*gImageData[];
extern LONG			**gLinesPtr[];
extern LONG			**gSclLinesPtr[];

extern BUFFMNG		*gBuffMng[];
extern long			gBuffNum[];

extern BUFFMNG		*gSclBuffMng[];
extern long			gSclBuffNum[];

extern int			gCancel[];

// RetSize 返却用ポインタ
// RetSize[0] 完成サイズ(幅)
// RetSize[1] 完成サイズ(高さ)
int CreateScale(int index, int Page, int Half, int SclWidth, int SclHeight, int left, int right, int top, int bottom, int algorithm, int Rotate, int Margin, int MarginColor, int Sharpen, int Bright, int Gamma, int Param, jint *RetSize)
{
//#define DEBUG_CREATESCALE
    int Invert   = (Param & PARAM_INVERT) != 0 ? 1 : 0;;
	int Gray     = (Param & PARAM_GRAY) != 0 ? 1 : 0;
	int Moire    = (Param & PARAM_MOIRE) != 0 ? 1 : 0;
	int Pseland  = (Param & PARAM_PSELAND) != 0 ? 1 : 0;
#ifdef DEBUG_CREATESCALE
    LOGD("CreateScale: index=%d, Page=%d, Half=%d, SclWidth=%d, SclHeight=%d, left=%d, right=%d, top=%d, bottom=%d, algorithm=%d, Rotate=%d, Margin=%d, MarginColor=%d, Sharpen=%d, Bright=%d, Gamma=%d", index, Page, Half, SclWidth, SclHeight, left, right, top, bottom, algorithm, Rotate, Margin, MarginColor, Sharpen, Bright, Gamma);
    LOGD("CreateScale: Param[Invert=%d, Gray=%d, Moire=%d, Pseland=%d]", Invert, Gray, Moire, Pseland);
#endif
    IMAGEDATA *pData = &gImageData[index][Page];
	
	pData->SclFlag[Half] = 0;

	int ret = 0;

	int Count     = 0;
	int OrgWidth  = pData->OrgWidth;
	int OrgHeight = pData->OrgHeight;
	int scl_w = SclWidth;
	int scl_h = SclHeight;

	// 拡大縮小用メモリ初期化
	ScaleMemInit(index);

    if (Margin > 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Margin START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

        // 余白カット
		ret = ImageMarginCut(index, Page, Half, Count, left, right, top, bottom, Margin, MarginColor, &OrgWidth, &OrgHeight);
		if (ret < 0) {
			return ret;
		}
        // 古いワークデータは削除
        EraseSclBuffMng(index, Count);
        Count ++;
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Margin   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	if (Rotate != 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Rotate START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		// 回転
		ret = ImageRotate(index, Page, Half, Count, OrgWidth, OrgHeight, Rotate);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(index, Count);
        Count ++;

		if (Rotate == 1 || Rotate == 3) {
			// 元画像の幅と高さを入れ替え
			int workWidth = OrgWidth;
			OrgWidth  = OrgHeight;
			OrgHeight = workWidth;
		}
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Rotate   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	if (Half != 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Half START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		ret = ImageHalf(index, Page, Half, Count, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(index, Count);
        Count ++;

		// 元画像の幅を半分に
		OrgWidth  = (OrgWidth + 1) / 2;
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Half   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	int NowWidth = 0;
	int NowHeight = 0;

	// 縮小時のモアレ軽減モード
	if (Moire > 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Moire START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 50%以下にする場合は半分に落とす
		while (scl_w <= OrgWidth / 2 && scl_h <= OrgHeight / 2) {
			// 50%以下の縮小
			NowWidth  = OrgWidth / 2;
			NowHeight = OrgHeight / 2;
	
			// 元データ配列化
			ret = SetLinesPtr(index, Page, Half, Count, OrgWidth, OrgHeight);
			if (ret < 0) {
				return ret;
			}
	
			// 50%の圧縮
			ret = CreateScaleHalf(index, Page, Half, Count, OrgWidth, OrgHeight);
			if (ret < 0) {
				return ret;
			}

			// 古いワークデータは削除
			EraseSclBuffMng(index, Count);
            Count ++;
	
			OrgWidth = NowWidth;
			OrgHeight = NowHeight;
		}
	
		// 50%以上の縮小がある場合
		int zw = scl_w * 100 /  OrgWidth;
		int zh = scl_h * 100 /  OrgHeight;
		if (zw < 100 && zh < 100) {
			// 元データ配列化
			ret = SetLinesPtr(index, Page, Half, Count, OrgWidth, OrgHeight);
			if (ret < 0) {
				return ret;
			}
	
			// ぼかし化
			ret = ImageBlur(index, Page, Half, Count, OrgWidth, OrgHeight, zw > zh ? zw : zh);
			if (ret < 0) {
				return ret;
			}

			// 古いワークデータは削除
			EraseSclBuffMng(index, Count);
			Count ++;
		}
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Moire   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}


#ifdef DEBUG_CREATESCALE
    LOGD("ImageScale : Scale - BEFORE: page=%d, half=%d / ow=%d, oh=%d, nw=%d, nh=%d, alg=%d", Page, Half, OrgWidth, OrgHeight, NowWidth, NowHeight, algorithm);
#endif

    // 拡大縮小
	int loopMax;

	double scale_x = exp(log((double)scl_w / (double)OrgWidth) / 2.0);
	double scale_y = exp(log((double)scl_h / (double)OrgHeight) / 2.0);

	if ((algorithm == 2 || algorithm == 4) && (scale_x <= SCALE_BORDER2 && scale_y <= SCALE_BORDER2)) {
		// 二段階縮小かつ大きく縮小
//		LOGD("ImageScale : loop=2");
		NowWidth  = (int)((double)OrgWidth * scale_x);
		NowHeight = (int)((double)OrgHeight * scale_y);
		loopMax = 2;
	}
	else {
//		LOGD("ImageScale : loop=1");
		NowWidth  = scl_w;
		NowHeight = scl_h;
		loopMax = 1;
	}

#ifdef DEBUG_CREATESCALE
    LOGD("ImageScale : Scale -  AFTER: page=%d, half=%d / ow=%d, oh=%d, nw=%d, nh=%d, alg=%d", Page, Half, OrgWidth, OrgHeight, NowWidth, NowHeight, algorithm);
#endif

	for (int i = 0 ; i < loopMax ; i ++) {
		if (i == 1) {
			// 2ループ目
			OrgWidth  = NowWidth;	// 前回の縮小サイズ
			OrgHeight = NowHeight;
			NowWidth  = scl_w;	// 最終的なサイズ
			NowHeight = scl_h;
		}

		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		switch (algorithm) {
			case 1:
			case 2:
#ifdef DEBUG_CREATESCALE
                LOGD("CreateScale: Linear Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
				ret = CreateScaleLinear(index, Page, Half, Count, NowWidth, NowHeight, OrgWidth, OrgHeight);
				break;
			case 3:
			case 4:
#ifdef DEBUG_CREATESCALE
                LOGD("CreateScale: Cubic Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
				ret = CreateScaleCubic(index, Page, Half, Count, NowWidth, NowHeight, OrgWidth, OrgHeight);
				break;
			default:
#ifdef DEBUG_CREATESCALE
                LOGD("CreateScale: Near Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
				ret = CreateScaleNear(index, Page, Half, Count, NowWidth, NowHeight, OrgWidth, OrgHeight);
				break;
		}
		if (ret < 0) {
			// エラー終了
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(index, Count);
        Count ++;
	}

	if (Pseland != 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Pseland START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// 90°回転
		ret = ImageRotate(index, Page, Half, Count, scl_w, scl_h, 1);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(index, Count);
        Count ++;

		// 元画像の幅と高さを入れ替え
		int workWidth = scl_w;
		scl_w  = scl_h;
		scl_h = workWidth;
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Pseland   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	if (Sharpen > 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Sharpen START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// シャープ化
		ret = ImageSharpen(index, Page, Sharpen, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(index, Count);
        Count ++;
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Sharpen   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	if (Gray > 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Gray START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// グレースケール化
		ret = ImageGray(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}
        // 古いワークデータは削除
        EraseSclBuffMng(index, Count);
        Count ++;
	}

	if (Invert > 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Invert START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// 色の反転
		ret = ImageInvert(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}
        // 古いワークデータは削除
        EraseSclBuffMng(index, Count);
        Count ++;
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Invert   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	if (Bright != 0 || Gamma != 0) {
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Bright || Gamma START: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
		// 元データ配列化
		ret = SetLinesPtr(index, Page, Half, Count, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// 明るさの調整
		ret = ImageBright(index, Page, Half, Count, scl_w, scl_h, Bright, Gamma);
		if (ret < 0) {
			return ret;
		}
        // 古いワークデータは削除
        EraseSclBuffMng(index, Count);
        Count ++;
#ifdef DEBUG_CREATESCALE
        LOGD("CreateScale: Bright || Gamma   END: Page=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", Page, Half, Count, OrgWidth, OrgHeight);
#endif
	}

	CopySclBuffMngToBuffMng(index);
	pData->SclFlag[Half] = 1;
	pData->SclWidth[Half] = scl_w;
	pData->SclHeight[Half] = scl_h;
	// 完成サイズを入れて返す
    RetSize[0] =  scl_w;
    RetSize[1] =  scl_h;
	return 0;
}

int SetLinesPtr(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight)
{
	IMAGEDATA *pData = &gImageData[index][Page];

    int ret = 0;

	BUFFMNG		*pMngptr;
	int			nMngnum;

	int			linesize;
	int			linenum;
	int			Type;

	linesize = OrgWidth + HOKAN_DOTS;
	linenum  = OrgHeight;

	if (Count == 0) {
		pMngptr = gBuffMng[index];
		nMngnum = gBuffNum[index];
		Type = 0;
		Half = 0;
	}
	else {
		pMngptr = gSclBuffMng[index];
		nMngnum = gSclBuffNum[index];
		Type = 1;
	}

	// 領域確保
    ret = ScaleMemLine(index, linenum);
	if (ret < 0) {
		return ret;
	}

	// 領域確保
	int buffindex = -1;
	int buffpos = 0;
	int lineindex;

	for (lineindex = 0 ; lineindex < linenum ; lineindex ++) {
		if (gCancel[index]) {
//			LOGD("CreateScale : cancel.");
			return ERROR_CODE_USER_CANCELED;
		}
		if (buffindex < 0 || BLOCKSIZE - buffpos < linesize ) {
			for (buffindex ++ ; buffindex < nMngnum ; buffindex ++) {
				if (pMngptr[buffindex].Page == Page && pMngptr[buffindex].Type == Type && pMngptr[buffindex].Half == Half) {
					break;
				}
			}
			if (buffindex >= nMngnum) {
				// 領域不足
				LOGE("CreateScale : Data Error page=%d, lineindex=%d/%d", Page, lineindex, linenum);
				return -2;
			}
			buffpos = 0;
		}
//		LOGD("SetLinesPtr: lineindex=%d, buffindex=%d, buffpos=%d", lineindex + HOKAN_DOTS / 2, buffindex, buffpos);
//		gLinesPtr[index][lineindex + HOKAN_DOTS / 2] = pMngptr[buffindex].Buff + buffpos + HOKAN_DOTS / 2;
		gLinesPtr[index][lineindex + HOKAN_DOTS / 2] = pMngptr[buffindex].Buff + buffpos;
		buffpos += linesize;
	}

//	LOGD("Set gLinesPtr : %d, %d", lineindex, HOKAN_DOTS / 2);
	gLinesPtr[index][0] = gLinesPtr[index][HOKAN_DOTS / 2];
	gLinesPtr[index][1] = gLinesPtr[index][HOKAN_DOTS / 2];
	gLinesPtr[index][lineindex + HOKAN_DOTS/2 + 0] = gLinesPtr[index][lineindex + HOKAN_DOTS / 2 - 1];
	gLinesPtr[index][lineindex + HOKAN_DOTS/2 + 1] = gLinesPtr[index][lineindex + HOKAN_DOTS / 2 - 1];
	return 0;
}

/**
 * サイズ変更時ピクセルデータの一時保存先バッファーを1ライン分設定する\n
 * 使用するバッファの位置を<code>gSclBuffMng</code>と<code>pBuffPos</code>に入れて返す
 * @param [in] index        バッファ番号 ImageManagerのインスタンス毎に払い出される
 * @param Page              ページ番号 画像ファイルを特定する番号
 * @param Half              画像を左右で分割した場合の左右のどちらかを指定
 * @param Count
 * @param [out] pBuffIndex  バッファの番号 gSclBuffMng[index][<code>pBuffIndex</code>]
 * @param pBuffPos          バッファー内の位置 gSclBuffMng[index][<code>pBuffIndex</code>].Buff + <code>pBuffPos</code>
 * @param [in] LineSize     1ラインのサイズ
 * @return                  エラーコード
 */
int NextSclBuff(int index, int Page, int Half, int Count, int *pBuffIndex, int *pBuffPos, int LineSize)
{
	int buffindex = *pBuffIndex;

#ifdef DEBUG
    LOGD("NextSclBuff : 開始します. index=%d, Page=%d, Half=%d, Count=%d, LineSize=%d", index, Page, Half, Count, LineSize);
#endif
	if (buffindex < 0 || BLOCKSIZE - *pBuffPos < LineSize) {
		for (buffindex ++ ; buffindex < gSclBuffNum[index] ; buffindex ++) {
			if (gSclBuffMng[index][buffindex].Page == -1) {
				break;
			}
		}
		if (buffindex >= gSclBuffNum[index]) {
			// 領域不足
			LOGE("NextSclBuff : Data Error page=%d, buffindex=%d/%d (Scale)", Page, buffindex, (int)gSclBuffNum[index]);
			return ERROR_CODE_CACHE_IS_FULL;
		}
		gSclBuffMng[index][buffindex].Page = Page;
		gSclBuffMng[index][buffindex].Size = 0;
		gSclBuffMng[index][buffindex].Type = 1;
		gSclBuffMng[index][buffindex].Half = Half;
		gSclBuffMng[index][buffindex].Count = Count;
		*pBuffPos = 0;
		*pBuffIndex = buffindex;
	}

	return 0;
}

/**
 * gSclBuffMng[index].CountがCountと不一致のものを未使用にする
 * @param index
 * @param Count
 * @return
 */
int EraseSclBuffMng(int index, int Count)
{
	for (int i = 0 ; i < gSclBuffNum[index] ; i ++) {
		if (gSclBuffMng[index][i].Page != -1 && gSclBuffMng[index][i].Count != Count) {
			// 使用中でindex不一致の場合は消す
			gSclBuffMng[index][i].Page = -1;
			gSclBuffMng[index][i].Type = 0;
			gSclBuffMng[index][i].Half = 0;
			gSclBuffMng[index][i].Size = 0;
			gSclBuffMng[index][i].Count = 0;
		}
	}
	return 0;
}

int CopySclBuffMngToBuffMng(int index)
{
	int buffindex = -1;

	for (int i = 0 ; i < gSclBuffNum[index] ; i ++) {
		if (gSclBuffMng[index][i].Page != -1) {
			// コピー先を探す
			for (buffindex ++ ; buffindex < gBuffNum[index] ; buffindex ++) {
				if (gBuffMng[index][buffindex].Page == -1) {
					// 見つけた
					break;
				}
			}
			if (buffindex >= gBuffNum[index]) {
				// 領域不足
				LOGE("CopySclBuffMngToBuffMng : Data Error buffindex=%d/%d", buffindex, (int)gBuffNum[index]);
				return -1;
			}
			// メモリコピー
//			LOGD("CopySclBuffMngToBuffMng St : %d/%d -> %d/%d", i, gSclBuffNum[index], buffindex, gBuffNum[index]);

			gBuffMng[index][buffindex].Page = gSclBuffMng[index][i].Page;
			gBuffMng[index][buffindex].Type = gSclBuffMng[index][i].Type;
			gBuffMng[index][buffindex].Half = gSclBuffMng[index][i].Half;
			gBuffMng[index][buffindex].Size = gSclBuffMng[index][i].Size;
			gBuffMng[index][buffindex].Count = 0;
			memcpy(gBuffMng[index][buffindex].Buff, gSclBuffMng[index][i].Buff, BLOCKSIZE * sizeof(LONG));
//			LOGD("CopySclBuffMngToBuffMng Ed : %d/%d -> %d/%d", i, gSclBuffNum[index], buffindex, gBuffNum[index]);
		}
	}
	return 0;
}

// 出力先ラインポインタ配列を設定
int RefreshSclLinesPtr(int index, int Page, int Half, int Count, int Height, int LineSize)
{
	int buffpos = 0;
	int buffindex = -1;
	int ret;

#ifdef DEBUG
	LOGD("RefreshSclLinePtr : 開始します. index=%d, page=%d, half=%d, Count=%d, h=%d, l=%d", index, Page, Half, Count, Height, LineSize);
#endif
	for (int yy = 0 ; yy < Height ; yy ++) {
		ret = NextSclBuff(index, Page, Half, Count, &buffindex, &buffpos, LineSize);
//		LOGD("RefreshSclLinePtr : buffindex=%d, buffpos=%d, LineSize=%d", buffindex, buffpos, LineSize);
		if (ret < 0) {
			LOGE("RefreshSclLinePtr : NextSclBuff error=%d", ret);
			return ret;
		}

		gSclLinesPtr[index][yy] = gSclBuffMng[index][buffindex].Buff + buffpos + HOKAN_DOTS / 2;
		gSclBuffMng[index][buffindex].Size += LineSize;
		buffpos += LineSize;
	}
//	LOGD("RefreshSclLinePtr : end page=%d, half=%d, Count=%d, h=%d, l=%d", Page, Half, Count, Height, LineSize);
	return 0;
}
