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

extern LONG			**gLinesPtr[];
extern LONG			**gSclLinesPtr[];
extern int			gCancel[];

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
    int index    = range[5];

//	LOGD("ImageBlur_ThreadFund : st=%d, ed=%d, w=%d, h=%d, z=%d", stindex, edindex, Width, Height, Zoom);

	int		rr, gg, bb;
	int		rr1, gg1, bb1;
	int		rr2, gg2, bb2;
	int		yd3, yd2;
	int		dotcnt;

	// 使用するバッファを保持
    LONG *orgbuff1;
    LONG *orgbuff2;

	LONG *buffptr = NULL;

	int raito = Zoom * Zoom / 100;
//	LOGD("ImageBlur_ThreadFund : zoom=%d, raito=%d", Zoom, raito);
	if (raito < 25) {
		raito = 25;
	}

	for (int yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageBlur : loop yy=%d", yy);
		if (gCancel[index]) {
//			LOGD("ImageBlur_ThreadFund : cancel.(gCancel[index]=%d)", gCancel[index]);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

        // バッファ位置
        buffptr = gSclLinesPtr[index][yy];

        orgbuff1 = gLinesPtr[index][yy + HOKAN_DOTS / 2 + 0];
		orgbuff2 = gLinesPtr[index][yy + HOKAN_DOTS / 2 + 1];

		yd3 = gDitherY_3bit[yy & 0x07];
		yd2 = gDitherY_2bit[yy & 0x03];

		for (int xx =  0 ; xx < Width + HOKAN_DOTS; xx++) {
			rr1 =  RGB888_RED(orgbuff1[xx]);
			gg1 =  RGB888_GREEN(orgbuff1[xx]);
			bb1 =  RGB888_BLUE(orgbuff1[xx]);

			rr2 =  RGB888_RED(orgbuff1[xx + 1]);
			gg2 =  RGB888_GREEN(orgbuff1[xx + 1]);
			bb2 =  RGB888_BLUE(orgbuff1[xx + 1]);

			rr2 += RGB888_RED(orgbuff2[xx]);
			gg2 += RGB888_GREEN(orgbuff2[xx]);
			bb2 += RGB888_BLUE(orgbuff2[xx]);

			rr2 += RGB888_RED(orgbuff2[xx + 1]);
			gg2 += RGB888_GREEN(orgbuff2[xx + 1]);
			bb2 += RGB888_BLUE(orgbuff2[xx + 1]);

			// 0～255に収める
			rr = LIMIT_RGB((rr1 * raito + rr2 * (100 - raito) / 3) / 100);
			gg = LIMIT_RGB((gg1 * raito + gg2 * (100 - raito) / 3) / 100);
			bb = LIMIT_RGB((bb1 * raito + bb2 * (100 - raito) / 3) / 100);

            buffptr[xx - HOKAN_DOTS / 2] = MAKE8888(rr, gg, bb);
		}

		// 補完用の余裕
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[Width + 0] = buffptr[Width - 1];
        buffptr[Width + 1] = buffptr[Width - 1];
	}
//	LOGD("ImageBlur_ThreadFund : end");
	return 0;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// pOrgWidth  : 幅を指定
// pOrgHeight : 高さを指定
// Zoom       : 倍率（0%～100%→0～100で表す）
int ImageBlur(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight, int Zoom)
{
//	LOGD("ImageBlur : p=%d, h=%d, c=%d, ow=%d, oh=%d, zm=%d", Page, Half, Count, OrgWidth, OrgHeight, Zoom);

    int ret = 0;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    // 50%まで
    if (Zoom < 50) {
        Zoom = 50;
    }

    int linesize;

    // ラインサイズ
    linesize  = OrgWidth + HOKAN_DOTS;

    //  サイズ変更画像待避用領域確保
    if (ScaleMemAlloc(index, linesize, OrgHeight) < 0) {
        return -6;
    }

    // データの格納先ポインタリストを更新
    if (RefreshSclLinesPtr(index, Page, Half, Count, OrgHeight, linesize) < 0) {
        return -7;
    }

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][6];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
		param[i][4] = Zoom;
        param[i][5] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, ImageBlur_ThreadFunc, (void*)param[i]) != 0) {
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
		if (status[i] != nullptr) {
//			LOGD("ImageBlur : cancel");
			ret = (long)status[i];
		}
	}

//	LOGD("ImageBlur : complete(%d)", ret);
	return ret;
}
