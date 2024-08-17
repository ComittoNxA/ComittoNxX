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

extern WORD			**gLinesPtr;
extern WORD			**gSclLinesPtr;
extern int			gCancel;

extern int			gMaxThreadNum;

void *CreateScaleNear_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int SclWidth  = range[2];
	int SclHeight = range[3];
	int OrgWidth  = range[4];
	int OrgHeight = range[5];

//	LOGD("CreateScaleNear_ThreadFund : st=%d, ed=%d, sw=%d, sh=%d, ow=%d, oh=%d", stindex, edindex, SclWidth, SclHeight, OrgWidth, OrgHeight);

	WORD *buffptr = NULL;
	WORD *orgbuff1;

	int		sx;	// 元画像の参照x座標
	int		sy;	// 元画像の参照x座標
	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	for (yy = stindex ; yy < edindex ; yy ++) {
		// 元座標
		sy = yy * OrgHeight / SclHeight;

		orgbuff1 = gLinesPtr[sy + HOKAN_DOTS / 2] + HOKAN_DOTS / 2;

		if (gCancel) {
//			LOGD("CreateScaleBQ : cancel.");
//			ReleaseBuff(Page, 1, half);
			return (void*)-1;
		}

		// バッファ位置
		buffptr = gSclLinesPtr[yy];
//		LOGD("CreateScale : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		for (xx = 0 ; xx < SclWidth ; xx ++) {
			// 元座標
			sx = xx * OrgWidth / SclWidth;

			buffptr[xx] = orgbuff1[sx];
		}
		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[SclWidth + 0] = buffptr[SclWidth - 1];
		buffptr[SclWidth + 1] = buffptr[SclWidth - 1];
	}
//	LOGD("CreateScaleNear_ThreadFund : end");
	return 0;
}

int CreateScaleNear(int Page, int Half, int Index, int SclWidth, int SclHeight, int OrgWidth, int OrgHeight)
{
	int ret = 0;

	int linesize;

	linesize  = SclWidth + HOKAN_DOTS;

	//  サイズ変更画像待避用領域確保
	if (ScaleMemAlloc(linesize, SclHeight) < 0) {
		return -6;
	}

	// データの格納先ポインタリストを更新
	if (RefreshSclLinesPtr(Page, Half, Index, SclHeight, linesize) < 0) {
		return -7;
	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][6];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = SclHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = SclWidth;
		param[i][3] = SclHeight;
		param[i][4] = OrgWidth;
		param[i][5] = OrgHeight;
		
		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], NULL, CreateScaleNear_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = CreateScaleNear_ThreadFunc((void*)param[i]);
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

//	LOGD("CreateScaleNear : End");
	return ret;
}