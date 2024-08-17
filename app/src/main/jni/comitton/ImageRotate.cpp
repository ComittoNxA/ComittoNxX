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

void *ImageRotate_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex    = range[0];
	int edindex    = range[1];
	int RotWidth   = range[2];
	int RotHeight  = range[3];
	int OrgWidth   = range[4];
	int OrgHeight  = range[5];
	int RotateMode = range[6];

	WORD *buffptr = NULL;
	WORD *orgbuff1;

	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	for (yy = stindex ; yy < edindex ; yy ++) {
		if (gCancel) {
//			LOGD("ImageRotate : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)-10;
		}

		// バッファ位置
		buffptr = gSclLinesPtr[yy];
//		LOGD("ImageRotate : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		if (RotateMode == 1) {
			for (xx = 0 ; xx < RotWidth ; xx ++) {
				// 元座標
				orgbuff1 = gLinesPtr[(RotWidth - (xx + 1)) + HOKAN_DOTS / 2];
				buffptr[xx] = orgbuff1[yy + HOKAN_DOTS / 2];
			}
		}
		else if (RotateMode == 2) {
			orgbuff1 = gLinesPtr[(RotHeight - (yy + 1)) + HOKAN_DOTS / 2];
			for (xx = 0 ; xx < RotWidth ; xx ++) {
				// 元座標
				buffptr[xx] = orgbuff1[(RotWidth - (xx + 1)) + HOKAN_DOTS / 2];
			}
		}
		else if (RotateMode == 3) {
			for (xx = 0 ; xx < RotWidth ; xx ++) {
				// 元座標
				orgbuff1 = gLinesPtr[xx + HOKAN_DOTS / 2];
				buffptr[xx] = orgbuff1[(RotHeight - (yy + 1)) + HOKAN_DOTS / 2];
			}
		}

		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[RotWidth + 0] = buffptr[RotWidth - 1];
		buffptr[RotWidth + 1] = buffptr[RotWidth - 1];
	}
	return 0;
}

int ImageRotate(int Page, int Half, int Index, int OrgWidth, int OrgHeight, int RotateMode)
{
	int ret = 0;

	int linesize;

	// 使用するバッファを保持
	int RotWidth;
	int RotHeight;

	if (RotateMode == 0) {
		// 回転なし
		return 0;
	}
	else if (RotateMode == 2) {
		// 縦横は一緒
		RotWidth  = OrgWidth;
		RotHeight = OrgHeight;
	}
	else {
		// 縦横を入れ替え
		RotWidth  = OrgHeight;
		RotHeight = OrgWidth;
	}

	// 縮小画像から取得
	linesize  = RotWidth + HOKAN_DOTS;

	//  サイズ変更画像待避用領域確保
	if (ScaleMemAlloc(linesize, RotHeight) < 0) {
		return -6;
	}

	// データの格納先ポインタリストを更新
	if (RefreshSclLinesPtr(Page, Half, Index, RotHeight, linesize) < 0) {
		return -7;
	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][7];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = RotHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = RotWidth;
		param[i][3] = RotHeight;
		param[i][4] = OrgWidth;
		param[i][5] = OrgHeight;
		param[i][6] = RotateMode;
		
		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], NULL, ImageRotate_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageRotate_ThreadFunc((void*)param[i]);
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
	return ret;
}