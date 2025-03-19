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

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

void *CreateScaleHalf_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int SclWidth  = range[2];
	int SclHeight = range[3];
	int OrgWidth  = range[4];
	int OrgHeight = range[5];
    int index = range[6];

//	LOGD("CreateScaleHalf_ThreadFund : st=%d, ed=%d, sw=%d, sh=%d, ow=%d, oh=%d", stindex, edindex, SclWidth, SclHeight, OrgWidth, OrgHeight);

    LONG *buffptr = nullptr;
    LONG *orgbuff1;
    LONG *orgbuff2;

	int		sx;	// 元画像の参照x座標
	int		sy;	// 元画像の参照x座標
	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	int		rr, gg, bb;

	for (yy = stindex; yy < edindex ; yy ++) {
		if (gCancel[index]) {
//			LOGD("CreateScaleBQ : cancel.");
//			ReleaseBuff(Page, 1, half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

		// 元座標
		sy = yy * 2;

		orgbuff1 = gLinesPtr[index][sy + 0 + HOKAN_DOTS / 2] + HOKAN_DOTS / 2;
		orgbuff2 = gLinesPtr[index][sy + 1 + HOKAN_DOTS / 2] + HOKAN_DOTS / 2;

		// バッファ位置
		buffptr = gSclLinesPtr[index][yy];
//		LOGD("CreateScale : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		for (xx = 0 ; xx < SclWidth ; xx ++) {
			// 元座標
			sx = xx * 2;
	
			rr = RGB888_RED(orgbuff1[sx])
			   + RGB888_RED(orgbuff1[sx + 1])
			   + RGB888_RED(orgbuff2[sx])
			   + RGB888_RED(orgbuff2[sx + 1]);

			gg = RGB888_GREEN(orgbuff1[sx])
			   + RGB888_GREEN(orgbuff1[sx + 1])
			   + RGB888_GREEN(orgbuff2[sx])
			   + RGB888_GREEN(orgbuff2[sx + 1]);

			bb = RGB888_BLUE(orgbuff1[sx])
			   + RGB888_BLUE(orgbuff1[sx + 1])
			   + RGB888_BLUE(orgbuff2[sx])
			   + RGB888_BLUE(orgbuff2[sx + 1]);

			// 0～255に収める
			rr = LIMIT_RGB(rr / 4);
			gg = LIMIT_RGB(gg / 4);
			bb = LIMIT_RGB(bb / 4);

			buffptr[xx] = MAKE8888(rr, gg, bb);
		}
		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[SclWidth + 0] = buffptr[SclWidth - 1];
		buffptr[SclWidth + 1] = buffptr[SclWidth - 1];
	}
//	LOGD("CreateScaleHalf_ThreadFund : end");
	return nullptr;
}

int CreateScaleHalf(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight)
{
	int ret = 0;

	int SclWidth  = OrgWidth / 2;
	int SclHeight = OrgHeight / 2;

	int linesize = SclWidth + HOKAN_DOTS;

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
			if (pthread_create(&thread[i], nullptr, CreateScaleHalf_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = CreateScaleHalf_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != 0) {
			ret = -10;
		}
	}

//	LOGD("CreateScaleHalf : End");
	return ret;
}
