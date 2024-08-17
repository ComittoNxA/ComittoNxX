#include <malloc.h>
#include <string.h>
#include <math.h>
#include <pthread.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "Image.h"

extern WORD			**gLinesPtr;
extern WORD			**gSclLinesPtr;
extern int			gCancel;

extern int			gMaxThreadNum;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

void *ImageSharpen_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int OrgWidth  = range[2];
	int OrgHeight = range[3];

	WORD *buffptr = NULL;

	// 使用するバッファを保持
	WORD *orgbuff1;
	WORD *orgbuff2;
	WORD *orgbuff3;

	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	int		rr, gg, bb;
	int		yd3, yd2;

	for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageSharpen : loop yy=%d", yy);
		if (gCancel) {
			LOGD("ImageSharpen : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)-1;
		}

		// バッファ位置
		buffptr = gSclLinesPtr[yy];
//		LOGD("ImageRotate : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2 - 1];
		orgbuff2 = gLinesPtr[yy + HOKAN_DOTS / 2 + 0];
		orgbuff3 = gLinesPtr[yy + HOKAN_DOTS / 2 + 1];

		yd3 = gDitherY_3bit[yy & 0x07];
		yd2 = gDitherY_2bit[yy & 0x03];
		for (xx =  0 ; xx < OrgWidth ; xx++) {
			// 
			rr =  RGB565_RED_256(orgbuff2[xx]) * 5;
			rr -= RGB565_RED_256(orgbuff1[xx]);
			rr -= RGB565_RED_256(orgbuff2[xx - 1]);
			rr -= RGB565_RED_256(orgbuff2[xx + 1]);
			rr -= RGB565_RED_256(orgbuff3[xx]);

			gg =  RGB565_GREEN_256(orgbuff2[xx]) * 5;
			gg -= RGB565_GREEN_256(orgbuff1[xx]);
			gg -= RGB565_GREEN_256(orgbuff2[xx - 1]);
			gg -= RGB565_GREEN_256(orgbuff2[xx + 1]);
			gg -= RGB565_GREEN_256(orgbuff3[xx]);

			bb =  RGB565_BLUE_256(orgbuff2[xx]) * 5;
			bb -= RGB565_BLUE_256(orgbuff1[xx]);
			bb -= RGB565_BLUE_256(orgbuff2[xx - 1]);
			bb -= RGB565_BLUE_256(orgbuff2[xx + 1]);
			bb -= RGB565_BLUE_256(orgbuff3[xx]);

//			rr = RED_RANGE(rr);
//			gg = GREEN_RANGE(gg);
//			bb = BLUE_RANGE(bb);
			// 0～255に収める
			rr = LIMIT_RGB(rr);
			gg = LIMIT_RGB(gg);
			bb = LIMIT_RGB(bb);

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

//			buffptr[xx] = REMAKE565(rr, gg, bb);
			buffptr[xx] = MAKE565(rr, gg, bb);
//			buffptr[xx] = orgbuff2[xx];
		}

		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[OrgWidth + 0] = buffptr[OrgWidth - 1];
		buffptr[OrgWidth + 1] = buffptr[OrgWidth - 1];
	}
	return 0;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// pOrgWidth  : 余白カット後の幅を返す
// pOrgHeight : 余白カット後の高さを返す
int ImageSharpen(int Page, int Half, int Index, int OrgWidth, int OrgHeight)
{
//	LOGD("ImageSharpen : p=%d, h=%d, i=%d, ow=%d, oh=%d", Page, Half, Index, OrgWidth, OrgHeight);

	int ret = 0;

	int linesize;

	// ラインサイズ
	linesize  = OrgWidth + HOKAN_DOTS;

	//  サイズ変更画像待避用領域確保
	if (ScaleMemAlloc(linesize, OrgHeight) < 0) {
		return -6;
	}

	// データの格納先ポインタリストを更新
	if (RefreshSclLinesPtr(Page, Half, Index, OrgHeight, linesize) < 0) {
		return -7;
	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][4];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
		
		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], NULL, ImageSharpen_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageSharpen_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != 0) {
//			LOGD("CreateScaleCubic : cancel");
			ret = -10;
		}
	}
//	LOGD("ImageSharpen : complete");
	return ret;
}