#include <malloc.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>
#include <pthread.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "Image.h"

extern WORD			**gLinesPtr;
extern int			gCancel;

extern int			gMaxThreadNum;

static int colorConvert_5bit[32];
static int colorConvert_6bit[64];

void *ImageBright_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int OrgWidth  = range[2];
	int OrgHeight = range[3];

	// 使用するバッファを保持
	WORD *orgbuff;

	int		xx;	// x座標
	int		yy;	// y座標

	int cc, rr, gg, bb;

	// ラインサイズ
	for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageColoring : loop yy=%d", yy);
		if (gCancel) {
			LOGD("ImageColoring : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)-1;
		}

		orgbuff = gLinesPtr[yy + HOKAN_DOTS / 2];

		for (xx =  0 ; xx < OrgWidth + HOKAN_DOTS ; xx++) {
			// 色の変換
			rr = colorConvert_5bit[RGB565_RED(orgbuff[xx])];
			gg = colorConvert_6bit[RGB565_GREEN(orgbuff[xx])];
			bb = colorConvert_5bit[RGB565_BLUE(orgbuff[xx])];
			orgbuff[xx] = MAKE565(rr, gg, bb);
		}

		// 補完用の余裕
		orgbuff[-2] = orgbuff[0];
		orgbuff[-1] = orgbuff[0];
		orgbuff[OrgWidth + 0] = orgbuff[OrgWidth - 1];
		orgbuff[OrgWidth + 1] = orgbuff[OrgWidth - 1];
	}
	return 0;
}

// 自動着色
int ImageBright(int Page, int Half, int Index, int OrgWidth, int OrgHeight, int Bright, int Gamma)
{
//	LOGD("ImageColoring : p=%d, h=%d, i=%d, ow=%d, oh=%d", Page, Half, Index, OrgWidth, OrgHeight);
	int ret = 0;
	double f = 1.0f;;
	double base = 1.0f;
	double scale = 0;;

	f = 1.0f / (1.0f + ((double)Gamma) * 0.1);

	if (Bright < 0) {
		scale = 1.0f + ((double)Bright) * 0.1;
	}
	else {
		scale = 1.0f - ((double)Bright) * 0.1;
		base = 255 * (1.0f - scale);
	}

//	switch (Gamma) {
//		case 0:
//			// -2のとき
//			f = 1.0f / 0.5f;
//			break;
//		case 1:
//			// -1のとき
//			f = 1.0f / 0.75f;
//			break;
//		case 2:
//			// 0のとき
//			f = 1.0f;
//			break;
//		case 3:
//			// 1のとき
//			f = 1.0f / 1.25f;
//			break;
//		case 4:
//			// 2のとき
//			f = 1.0f / 1.5f;
//			break;
//		default:
//			// 0、1、2のとき
//			return -1;
//	}

//	switch (Bright) {
//		case 0:
//			// -2のとき
//			scale = 0.5f;
//			base = 0.0f;
//			break;
//		case 1:
//			// -1のとき
//			scale = 0.75f;
//			base = 0.0f;
//			break;
//		case 2:
//			// 0のとき
//			scale = 1.0f;
//			base = 0;
//			break;
//		case 3:
//			// 1のとき
//			scale = 0.75f;
//			base = 255 * (1.0f - scale);
//			break;
//		case 4:
//			// 2のとき
//			scale = 0.5f;
//			base = 255 * scale;
//			break;
//		default:
//			// 0、1、2のとき
//			return -1;
//	}

	for (int i = 0; i < 32; i ++) {
		colorConvert_5bit[i] = (int)(pow(((float)(i << 3) / 255.0f), f) * 255.0f) * scale + base;
	}
	for (int i = 0; i < 64; i ++) {
		colorConvert_6bit[i] = (int)(pow(((float)(i << 2) / 255.0f), f) * 255.0f) * scale + base;
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
			if (pthread_create(&thread[i], NULL, ImageBright_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageBright_ThreadFunc((void*)param[i]);
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
//	LOGD("ImageColoring : complete");
	return ret;
}
