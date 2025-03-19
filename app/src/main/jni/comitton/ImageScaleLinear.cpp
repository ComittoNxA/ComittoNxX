#include <time.h>
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

extern long long	*gSclLLongParam[];
extern int			*gSclIntParam1[];
extern int			*gSclIntParam2[];
extern int			*gSclIntParam3[];

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

void *CreateScaleLinear_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int SclWidth  = range[2];
	int SclHeight = range[3];
	int OrgWidth  = range[4];
	int OrgHeight = range[5];
    int index = range[6];

//	LOGD("CreateScaleLinear_ThreadFund : st=%d, ed=%d, sw=%d, sh=%d, ow=%d, oh=%d", stindex, edindex, SclWidth, SclHeight, OrgWidth, OrgHeight);

	int *orgx = gSclIntParam1[index];
	int *d1   = gSclIntParam2[index];
	int *d2   = gSclIntParam3[index];

    LONG *buffptr = nullptr;

    LONG *orgbuff1;
    LONG *orgbuff2;

	long long syy;
	int orgy;

	int d3, d4;

	int r1, r2, r3, r4, rr;
	int g1, g2, g3, g4, gg;
	int b1, b2, b3, b4, bb;

	int yy, xx;
	int wkxx;

	for (yy = stindex ; yy < edindex ; yy ++) {
		if (gCancel[index]) {
//			LOGD("CreateScale : cancel.");
//			ReleaseBuff(page, 1, half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

		syy = (int)(((long long)yy * 256 * OrgHeight) / SclHeight);
		orgy = (int)(syy / 256);
		d4 = (int)(syy % 256) + 1;
		d3 = 256 - d4;

		orgbuff1 = gLinesPtr[index][orgy + HOKAN_DOTS / 2];
		orgbuff2 = gLinesPtr[index][orgy + HOKAN_DOTS / 2 + 1];

		// バッファ位置
		buffptr = gSclLinesPtr[index][yy];
//		LOGD("CreateScale : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		for (xx = 0 ; xx < SclWidth ; xx++) {
			// 
			wkxx = orgx[xx] + HOKAN_DOTS / 2;

			r1 = RGB888_RED(orgbuff1[wkxx + 0]);
			r2 = RGB888_RED(orgbuff1[wkxx + 1]);
			r3 = RGB888_RED(orgbuff2[wkxx + 0]);
			r4 = RGB888_RED(orgbuff2[wkxx + 1]);

			g1 = RGB888_GREEN(orgbuff1[wkxx + 0]);
			g2 = RGB888_GREEN(orgbuff1[wkxx + 1]);
			g3 = RGB888_GREEN(orgbuff2[wkxx + 0]);
			g4 = RGB888_GREEN(orgbuff2[wkxx + 1]);

			b1 = RGB888_BLUE(orgbuff1[wkxx + 0]);
			b2 = RGB888_BLUE(orgbuff1[wkxx + 1]);
			b3 = RGB888_BLUE(orgbuff2[wkxx + 0]);
			b4 = RGB888_BLUE(orgbuff2[wkxx + 1]);

			rr = (int)((LONG)(r1 * d1[xx] + r2 * d2[xx]) * d3 + (r3 * d1[xx] + r4 * d2[xx]) * d4) / 256 / 256;
			gg = (int)((LONG)(g1 * d1[xx] + g2 * d2[xx]) * d3 + (g3 * d1[xx] + g4 * d2[xx]) * d4) / 256 / 256;
			bb = (int)((LONG)(b1 * d1[xx] + b2 * d2[xx]) * d3 + (b3 * d1[xx] + b4 * d2[xx]) * d4) / 256 / 256;

// LOGD("CreateScaleLinear : x=%d, y=%d, rr=%d(%d,%d,%d,%d), gg=%d(%d,%d,%d,%d)", xx, yy, rr, r1, r2, r3, r4, gg, g1, g2, g3, g4);


			buffptr[xx] = MAKE8888(rr, gg, bb);
		}
		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[SclWidth + 0] = buffptr[SclWidth - 1];
		buffptr[SclWidth + 1] = buffptr[SclWidth - 1];
	}
	return nullptr;
}

int CreateScaleLinear(int index, int Page, int Half, int Count, int SclWidth, int SclHeight, int OrgWidth, int OrgHeight)
{
#ifdef DEBUG
	LOGD("CreateScaleLinear : p=%d, h=%d, i=%d, sw=%d, sh=%d, ow=%d, oh=%d", Page, Half, Index, SclWidth, SclHeight, OrgWidth, OrgHeight);
#endif
	int ret = 0;

	int linesize;

	// サイズ設定
	linesize  = SclWidth + HOKAN_DOTS;

	//  サイズ変更演算領域用領域確保
    ret = ScaleMemColumn(index, SclWidth);
	if (ret < 0) {
		return ret;
	}
	//  サイズ変更画像待避用領域確保
    ret = ScaleMemAlloc(index, linesize, SclHeight);
	if (ret < 0) {
		return ret;
	}

	// データの格納先ポインタリストを更新
    ret = RefreshSclLinesPtr(index, Page, Half, Count, SclHeight, linesize);
	if (ret < 0) {
		return ret;
	}

	long long *sxx  = gSclLLongParam[index];
	int *orgx = gSclIntParam1[index];
	int *d1   = gSclIntParam2[index];
	int *d2   = gSclIntParam3[index];

	for (int xx = 0 ; xx < SclWidth ; xx++) {
		sxx[xx] = ((long long)xx) * 256 * ((long long)OrgWidth) / ((long long)SclWidth);
		orgx[xx] = (int)(sxx[xx] / 256);
		d2[xx] = sxx[xx] % 256;
		d1[xx] = 256 - d2[xx];
//		LOGD("CreateScaleLinear : xx / %d, %d, %d, %d, %d",xx, (int)sxx[xx], orgx[xx], d1[xx], d2[xx]);
	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][7];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = SclHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = SclWidth;
		param[i][3] = SclHeight;
		param[i][4] = OrgWidth;
		param[i][5] = OrgHeight;
        param[i][6] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, CreateScaleLinear_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = CreateScaleLinear_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != nullptr) {
//			LOGD("CreateScaleCubic : cancel");
			ret = (long)status[i];
		}
	}

//	LOGD("CreateScaleLinear : End");
	return ret;
}
