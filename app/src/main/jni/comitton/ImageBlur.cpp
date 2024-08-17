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

void *ImageBlur_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex = range[0];
	int edindex = range[1];
	int Width   = range[2];
	int Height  = range[3];
	int Zoom    = range[4];

//	LOGD("ImageBlur_ThreadFund : st=%d, ed=%d, w=%d, h=%d, z=%d", stindex, edindex, Width, Height, Zoom);

	int		rr, gg, bb;
	int		rr1, gg1, bb1;
	int		rr2, gg2, bb2;
	int		yd3, yd2;
	int		dotcnt;

	// 使用するバッファを保持
	WORD *orgbuff1;
	WORD *orgbuff2;

//	WORD *buffptr = NULL;

	int raito = Zoom * Zoom / 100;
//	LOGD("ImageBlur_ThreadFund : zoom=%d, raito=%d", Zoom, raito);
	if (raito < 25) {
		raito = 25;
	}

	for (int yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageBlur : loop yy=%d", yy);
		if (gCancel) {
//			LOGD("ImageBlur_ThreadFund : cancel.(gCancel=%d)", gCancel);
			return (void*)-1;
		}

		// バッファ位置
//		buffptr = gSclLinesPtr[yy];

		orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2 + 0];
		orgbuff2 = gLinesPtr[yy + HOKAN_DOTS / 2 + 1];

		yd3 = gDitherY_3bit[yy & 0x07];
		yd2 = gDitherY_2bit[yy & 0x03];

		for (int xx =  0 ; xx < Width ; xx++) {
			rr1 =  RGB565_RED_256(orgbuff1[xx]);
			gg1 =  RGB565_GREEN_256(orgbuff1[xx]);
			bb1 =  RGB565_BLUE_256(orgbuff1[xx]);

			rr2 =  RGB565_RED_256(orgbuff1[xx + 1]);
			gg2 =  RGB565_GREEN_256(orgbuff1[xx + 1]);
			bb2 =  RGB565_BLUE_256(orgbuff1[xx + 1]);

			rr2 += RGB565_RED_256(orgbuff2[xx]);
			gg2 += RGB565_GREEN_256(orgbuff2[xx]);
			bb2 += RGB565_BLUE_256(orgbuff2[xx]);

			rr2 += RGB565_RED_256(orgbuff2[xx + 1]);
			gg2 += RGB565_GREEN_256(orgbuff2[xx + 1]);
			bb2 += RGB565_BLUE_256(orgbuff2[xx + 1]);

			// 0～255に収める
			rr = LIMIT_RGB((rr1 * raito + rr2 * (100 - raito) / 3) / 100);
			gg = LIMIT_RGB((gg1 * raito + gg2 * (100 - raito) / 3) / 100);
			bb = LIMIT_RGB((bb1 * raito + bb2 * (100 - raito) / 3) / 100);

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

			orgbuff1[xx] = MAKE565(rr, gg, bb);
		}

		// 補完用の余裕
		orgbuff1[-2] = orgbuff1[0];
		orgbuff1[-1] = orgbuff1[0];
		orgbuff1[Width + 0] = orgbuff1[Width - 1];
		orgbuff1[Width + 1] = orgbuff1[Width - 1];
	}
//	LOGD("ImageBlur_ThreadFund : end");
	return 0;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// pOrgWidth  : 幅を指定
// pOrgHeight : 高さを指定
// Zoom       : 倍率（0%～100%→0～100で表す）
int ImageBlur(int Page, int Half, int Index, int OrgWidth, int OrgHeight, int Zoom)
{
//	LOGD("ImageBlur : p=%d, h=%d, i=%d, ow=%d, oh=%d, zm=%d", Page, Half, Index, OrgWidth, OrgHeight, Zoom);

//	int linesize;
	int ret = 0;

	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	// 50%まで
	if (Zoom < 50) {
		Zoom = 50;
	}

	// ラインサイズ
//	linesize  = OrgWidth + HOKAN_DOTS;

//	//  サイズ変更画像待避用領域確保
//	if (ScaleMemAlloc(linesize, OrgHeight) < 0) {
//		return -6;
//	}

//	// データの格納先ポインタリストを更新
//	if (RefreshSclLinesPtr(Page, Half, Index, OrgHeight, linesize) < 0) {
//		return -7;
//	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][5];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
		param[i][4] = Zoom;
		
		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], NULL, ImageBlur_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageBlur_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != 0) {
//			LOGD("ImageBlur : cancel");
			ret = -10;
		}
	}

//	LOGD("ImageBlur : complete(%d)", ret);
	return ret;
}
