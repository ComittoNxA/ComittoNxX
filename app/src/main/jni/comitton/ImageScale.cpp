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

extern IMAGEDATA	*gImageData;
extern WORD			**gLinesPtr;
extern WORD			**gSclLinesPtr;

extern BUFFMNG		*gBuffMng;
extern long			gBuffNum;

extern BUFFMNG		*gSclBuffMng;
extern long			gSclBuffNum;

extern int			gCancel;

// RetSize 返却用ポインタ
// RetSize[0] 完成サイズ(幅)
// RetSize[1] 完成サイズ(高さ)
int CreateScale(int Page, int Half, int SclWidth, int SclHeight, int left, int right, int top, int bottom, int algorithm, int Rotate, int Margin, int Bright, int Gamma, int Param, jint *RetSize)
{
	int Sharpen  = (Param & PARAM_SHARPEN) != 0 ? 1 : 0;
	int Invert   = (Param & PARAM_INVERT) != 0 ? 1 : 0;;
	int Gray     = (Param & PARAM_GRAY) != 0 ? 1 : 0;
	int Moire    = (Param & PARAM_MOIRE) != 0 ? 1 : 0;
	int Pseland  = (Param & PARAM_PSELAND) != 0 ? 1 : 0;

	IMAGEDATA *pData = &gImageData[Page];
	
	pData->SclFlag[Half] = 0;

	int ret = 0;

	int Index     = 0;
	int OrgWidth  = pData->OrgWidth;
	int OrgHeight = pData->OrgHeight;
	int scl_w = SclWidth;
	int scl_h = SclHeight;

	// 拡大縮小用メモリ初期化
	ScaleMemInit();

	if (Margin > 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		// 余白カット
		ret = ImageMarginCut(Page, Half, Index, SclWidth, SclHeight, left, right, top, bottom, Margin, &OrgWidth, &OrgHeight);
		if (ret < 0) {
			return ret;
		}
		else if(ret > 0) {
			//LOGD("CreateScale: Page=%d, Half=%d, 強制カット Index=%d, SclWidth=%d, SclHeight=%d, RetWidth=%d, RetHeight=%d", Page, Half, Index, scl_w, scl_h, OrgWidth, OrgHeight);
			// 余白カットあり
			Index ++;
		}
	}

	if (Rotate != 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		// 回転
		ret = ImageRotate(Page, Half, Index, OrgWidth, OrgHeight, Rotate);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(Index);
		Index ++;

		if (Rotate == 1 || Rotate == 3) {
			// 元画像の幅と高さを入れ替え
			int workWidth = OrgWidth;
			OrgWidth  = OrgHeight;
			OrgHeight = workWidth;
		}
	}

	if (Half != 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		ret = ImageHalf(Page, Half, Index, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(Index);
		Index ++;

		// 元画像の幅を半分に
		OrgWidth  = (OrgWidth + 1) / 2;
	}

	int NowWidth;
	int NowHeight;

	// 縮小時のモアレ軽減モード
	if (Moire) {
		// 50%以下にする場合は半分に落とす
		while (scl_w <= OrgWidth / 2 && scl_h <= OrgHeight / 2) {
			// 50%以下の縮小
			NowWidth  = OrgWidth / 2;
			NowHeight = OrgHeight / 2;
	
			// 元データ配列化
			ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
			if (ret < 0) {
				return ret;
			}
	
			// 50%の圧縮
			ret = CreateScaleHalf(Page, Half, Index, OrgWidth, OrgHeight);
			if (ret < 0) {
				return ret;
			}

			// 古いワークデータは削除
			EraseSclBuffMng(Index);
			Index ++;
	
			OrgWidth = NowWidth;
			OrgHeight = NowHeight;
		}
	
		// 50%以上の縮小がある場合
		int zw = scl_w * 100 /  OrgWidth;
		int zh = scl_h * 100 /  OrgHeight;
		if (zw < 100 && zh < 100) {
			// 元データ配列化
			ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
			if (ret < 0) {
				return ret;
			}
	
			// ぼかし化
			ret = ImageBlur(Page, Half, Index, OrgWidth, OrgHeight, zw > zh ? zw : zh);
			if (ret < 0) {
				return ret;
			}

//			// 古いワークデータは削除
//			EraseSclBuffMng(Index);
//			Index ++;
		}
	}

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

	for (int i = 0 ; i < loopMax ; i ++) {
		if (i == 1) {
			// 2ループ目
			OrgWidth  = NowWidth;	// 前回の縮小サイズ
			OrgHeight = NowHeight;
			NowWidth  = scl_w;	// 最終的なサイズ
			NowHeight = scl_h;
		}

#ifdef DEBUG
		LOGD("ImageScale : Scale - page=%d, half=%d / ow=%d, oh=%d, nw=%d, nh=%d, alg=%d"
						, Page, Half, OrgWidth, OrgHeight, NowWidth, NowHeight, algorithm);
#endif

		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
		if (ret < 0) {
			return ret;
		}

		switch (algorithm) {
			case 1:
			case 2:
				ret = CreateScaleLinear(Page, Half, Index, NowWidth, NowHeight, OrgWidth, OrgHeight);
				break;
			case 3:
			case 4:
				ret = CreateScaleCubic(Page, Half, Index, NowWidth, NowHeight, OrgWidth, OrgHeight);
				break;
			default:
				ret = CreateScaleNear(Page, Half, Index, NowWidth, NowHeight, OrgWidth, OrgHeight);
				break;
		}
		if (ret < 0) {
			// エラー終了
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(Index);
		Index ++;
	}

	if (Pseland != 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// 90°回転
		ret = ImageRotate(Page, Half, Index, scl_w, scl_h, 1);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(Index);
		Index ++;

		// 元画像の幅と高さを入れ替え
		int workWidth = scl_w;
		scl_w  = scl_h;
		scl_h = workWidth;
	}

	if (Sharpen > 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// シャープ化
		ret = ImageSharpen(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}
		// 古いワークデータは削除
		EraseSclBuffMng(Index);
		Index ++;
	}

	if (Gray > 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// グレースケール化
		ret = ImageGray(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}
		// 色の変化だけなのでワークデータは作成されない
	}

	if (Invert > 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// 色の反転
		ret = ImageInvert(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}
		// 反転だけなのでワークデータは作成されない
	}

	if (Bright != 0 || Gamma != 0) {
		// 元データ配列化
		ret = SetLinesPtr(Page, Half, Index, scl_w, scl_h);
		if (ret < 0) {
			return ret;
		}

		// 明るさの調整
		ret = ImageBright(Page, Half, Index, scl_w, scl_h, Bright, Gamma);
		if (ret < 0) {
			return ret;
		}
		// 色の調整だけなのでワークデータは作成されない
	}

	CopySclBuffMngToBuffMng();
	pData->SclFlag[Half] = 1;
	pData->SclWidth[Half] = scl_w;
	pData->SclHeight[Half] = scl_h;
	// 完成サイズを入れて返す
    RetSize[0] =  scl_w;
    RetSize[1] =  scl_h;
	return 0;
}

int SetLinesPtr(int Page, int Half, int Index, int OrgWidth, int OrgHeight)
{
	IMAGEDATA *pData = &gImageData[Page];

	BUFFMNG		*pMngptr;
	int			nMngnum;

	int			linesize;
	int			linenum;
	int			Type;

	linesize = OrgWidth + HOKAN_DOTS;
	linenum  = OrgHeight;

	if (Index == 0) {
		pMngptr = gBuffMng;
		nMngnum = gBuffNum;
		Type = 0;
		Half = 0;
	}
	else {
		pMngptr = gSclBuffMng;
		nMngnum = gSclBuffNum;
		Type = 1;
	}

	// 領域確保
	if (ScaleMemLine(linenum) < 0) {
		return -1;
	}

	// 領域確保
	int buffindex = -1;
	int buffpos = 0;
	int lineindex;

	for (lineindex = 0 ; lineindex < linenum ; lineindex ++) {
		if (gCancel) {
//			LOGD("CreateScale : cancel.");
			return -10;
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
//		LOGD("SetLinesPtr: lineindex=%d, buffindex=%d, buffpos=%d", lineindex + HOKAN_DOTS/2, buffindex, buffpos);
//		gLinesPtr[lineindex + HOKAN_DOTS/2] = pMngptr[buffindex].Buff + buffpos + HOKAN_DOTS/2;
		gLinesPtr[lineindex + HOKAN_DOTS/2] = pMngptr[buffindex].Buff + buffpos;
		buffpos += linesize;
	}

//	LOGD("Set gLinesPtr : %d, %d", lineindex, HOKAN_DOTS/2);
	gLinesPtr[0] = gLinesPtr[HOKAN_DOTS/2];
	gLinesPtr[1] = gLinesPtr[HOKAN_DOTS/2];
	gLinesPtr[lineindex + HOKAN_DOTS/2 + 0] = gLinesPtr[lineindex + HOKAN_DOTS/2 - 1];
	gLinesPtr[lineindex + HOKAN_DOTS/2 + 1] = gLinesPtr[lineindex + HOKAN_DOTS/2 - 1];
	return 0;
}

int NextSclBuff(int Page, int Half, int Index, int *pBuffIndex, int *pBuffPos, int LineSize)
{
	int buffindex = *pBuffIndex;

	if (buffindex < 0 || BLOCKSIZE - *pBuffPos < LineSize) {
		for (buffindex ++ ; buffindex < gSclBuffNum ; buffindex ++) {
			if (gSclBuffMng[buffindex].Page == -1) {
				break;
			}
		}
		if (buffindex >= gSclBuffNum) {
			// 領域不足
			LOGE("NextSclBuff : Data Error page=%d, buffindex=%d/%d (Scale)", Page, buffindex, (int)gSclBuffNum);
			return -6;
		}
		gSclBuffMng[buffindex].Page = Page;
		gSclBuffMng[buffindex].Size = 0;
		gSclBuffMng[buffindex].Type = 1;
		gSclBuffMng[buffindex].Half = Half;
		gSclBuffMng[buffindex].Index = Index;
		*pBuffPos = 0;
		*pBuffIndex = buffindex;
	}
	return 0;
}

int EraseSclBuffMng(int index)
{
	for (int i = 0 ; i < gSclBuffNum ; i ++) {
		if (gSclBuffMng[i].Page != -1 && gSclBuffMng[i].Index != index) {
//			LOGD("EraseSclBuffMng : %d/%d, %d, %d, %d, %d, %d, %d, %d, %d", i, gSclBuffNum
//				, (int)gSclBuffMng[i].Buff[0]
//				, (int)gSclBuffMng[i].Buff[1]
//				, (int)gSclBuffMng[i].Buff[2]
//				, (int)gSclBuffMng[i].Buff[3]
//				, (int)gSclBuffMng[i].Buff[4]
//				, (int)gSclBuffMng[i].Buff[5]
//				, (int)gSclBuffMng[i].Buff[6]
//				, (int)gSclBuffMng[i].Buff[7] );
			// 使用中でindex一致の場合は消す
			gSclBuffMng[i].Page = -1;
			gSclBuffMng[i].Type = 0;
			gSclBuffMng[i].Half = 0;
			gSclBuffMng[i].Size = 0;
			gSclBuffMng[i].Index = 0;
		}
	}
	return 0;
}

int CopySclBuffMngToBuffMng()
{
	int buffindex = -1;

	for (int i = 0 ; i < gSclBuffNum ; i ++) {
		if (gSclBuffMng[i].Page != -1) {
			// コピー先を探す
			for (buffindex ++ ; buffindex < gBuffNum ; buffindex ++) {
				if (gBuffMng[buffindex].Page == -1) {
					// 見つけた
					break;
				}
			}
			if (buffindex >= gBuffNum) {
				// 領域不足
				LOGE("CopySclBuffMngToBuffMng : Data Error buffindex=%d/%d", buffindex, (int)gBuffNum);
				return -1;
			}
			// メモリコピー
//			LOGD("CopySclBuffMngToBuffMng St : %d/%d -> %d/%d", i, gSclBuffNum, buffindex, gBuffNum);

			gBuffMng[buffindex].Page = gSclBuffMng[i].Page;
			gBuffMng[buffindex].Type = gSclBuffMng[i].Type;
			gBuffMng[buffindex].Half = gSclBuffMng[i].Half;
			gBuffMng[buffindex].Size = gSclBuffMng[i].Size;
			gBuffMng[buffindex].Index = 0;
			memcpy(gBuffMng[buffindex].Buff, gSclBuffMng[i].Buff, BLOCKSIZE * sizeof(WORD));
//			LOGD("CopySclBuffMngToBuffMng Ed : %d/%d -> %d/%d", i, gSclBuffNum, buffindex, gBuffNum);
		}
	}
	return 0;
}

// 出力先ラインポインタ配列を設定
int RefreshSclLinesPtr(int Page, int Half, int Index, int Height, int LineSize)
{
	int buffpos = 0;
	int buffindex = -1;
	int ret;

//	LOGD("RefreshSclLinePtr : start page=%d, half=%d, idx=%d, h=%d, l=%d", Page, Half, Index, Height, LineSize);
	for (int yy = 0 ; yy < Height ; yy ++) {
		ret = NextSclBuff(Page, Half, Index, &buffindex, &buffpos, LineSize);
//		LOGD("RefreshSclLinePtr : buffindex=%d, buffpos=%d, LineSize=%d", buffindex, buffpos, LineSize);
		if (ret < 0) {
			LOGD("RefreshSclLinePtr : NextSclBuff error=%d", ret);
			return ret;
		}

		gSclLinesPtr[yy] = gSclBuffMng[buffindex].Buff + buffpos + HOKAN_DOTS / 2;
		gSclBuffMng[buffindex].Size += LineSize;
		buffpos += LineSize;
	}
//	LOGD("RefreshSclLinePtr : end page=%d, half=%d, idx=%d, h=%d, l=%d", Page, Half, Index, Height, LineSize);
	return 0;
}
