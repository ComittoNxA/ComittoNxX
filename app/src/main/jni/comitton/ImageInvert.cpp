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

void *ImageInvert_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int OrgWidth  = range[2];
	int OrgHeight = range[3];
    int index = range[4];

    LONG *buffptr = nullptr;

    // 使用するバッファを保持
	LONG *orgbuff;

	int		xx;	// x座標
	int		yy;	// y座標

	// ラインサイズ
	for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageInvert : loop yy=%d", yy);
		if (gCancel[index]) {
//			LOGD("ImageInvert : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}
        // バッファ位置
        buffptr = gSclLinesPtr[index][yy];

		orgbuff = gLinesPtr[index][yy + HOKAN_DOTS / 2];

		for (xx =  0 ; xx < OrgWidth + HOKAN_DOTS ; xx++) {
			// 反転
            buffptr[xx - HOKAN_DOTS / 2] = orgbuff[xx] ^ 0xFFFFFF;
		}

		// 補完用の余裕
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[OrgWidth + 0] = buffptr[OrgWidth - 1];
        buffptr[OrgWidth + 1] = buffptr[OrgWidth - 1];
	}
	return nullptr;
}

// 色を反転するのみ
int ImageInvert(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight)
{
//	LOGD("ImageInvert : p=%d, h=%d, c=%d, ow=%d, oh=%d", Page, Half, Count, OrgWidth, OrgHeight);
	int ret = 0;

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
	int param[gMaxThreadNum][5];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
        param[i][4] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, ImageInvert_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageInvert_ThreadFunc((void*)param[i]);
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
//	LOGD("ImageInvert : complete");
	return ret;
}