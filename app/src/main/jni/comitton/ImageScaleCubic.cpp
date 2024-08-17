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

extern WORD	**gLinesPtr;
extern WORD	**gSclLinesPtr;
extern int	gCancel;

extern int			gMaxThreadNum;

extern int	*gSclIntParam1;
extern int	*gSclIntParam2;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

//#define LIMIT_RB(color) ((color)<0x00?0x00:((color)>0x1f?0x1f:(color)))
//#define LIMIT_G(color) ((color)<0x00?0x00:((color)>0x3f?0x3f:(color)))

typedef struct _cubic_spline {
	int		h1;
	int		h2;
	int		h3;
	int		h4;
} CUBIC_SPLINE;

#define MAX_CUBIC_SPLINE	256

CUBIC_SPLINE	*gCubicSplines = NULL;

float bicubic_spline(float t)
{
  float tmp = fabs(t);
  if ( tmp <= 1.0 ) return ( tmp * tmp * tmp - 2.0f * tmp * tmp             + 1.0f);
  if ( tmp <= 2.0 ) return (-tmp * tmp * tmp + 5.0f * tmp * tmp -8.0f * tmp + 4.0f);
  return 0.0;
}

void *CreateScaleCubic_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int SclWidth  = range[2];
	int SclHeight = range[3];
	int OrgWidth  = range[4];
	int OrgHeight = range[5];

//	LOGD("CreateScaleCubic_ThreadFund : st=%d, ed=%d, sw=%d, sh=%d, ow=%d, oh=%d", stindex, edindex, SclWidth, SclHeight, OrgWidth, OrgHeight);

	float	sy;	// 元画像の参照x座標
	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	int		bx;
	int		by;
	int		hx1, hx2, hx3, hx4;
	int		hy1, hy2, hy3, hy4;

	int		rr, gg, bb;
	int		yd3, yd2;

	WORD *buffptr = NULL;

	WORD *orgbuff1;
	WORD *orgbuff2;
	WORD *orgbuff3;
	WORD *orgbuff4;

	int *x_index = gSclIntParam1;
	int *x_pos   = gSclIntParam2;

	int		yi;
	int		xi;

	for (yy = stindex ; yy < edindex ; yy ++) {
		// 元座標
		sy = (float)yy * (float)OrgHeight / (float)SclHeight;
		by = (int)floor(sy);
		yi = (int)((sy - by) * 256.0f);

		// 重み付け
		hy1 = gCubicSplines[yi].h1;
		hy2 = gCubicSplines[yi].h2;
		hy3 = gCubicSplines[yi].h3;
		hy4 = gCubicSplines[yi].h4;

		orgbuff1 = gLinesPtr[by - 1 + HOKAN_DOTS / 2];
		orgbuff2 = gLinesPtr[by + 0 + HOKAN_DOTS / 2];
		orgbuff3 = gLinesPtr[by + 1 + HOKAN_DOTS / 2];
		orgbuff4 = gLinesPtr[by + 2 + HOKAN_DOTS / 2];

		if (gCancel) {
//			LOGD("CreateScaleBQ : cancel.");
//			ReleaseBuff(page, 1, half);
			return (void*)-1;
		}

		// バッファ位置
		buffptr = gSclLinesPtr[yy];

		yd3 = gDitherY_3bit[yy & 0x07];
		yd2 = gDitherY_2bit[yy & 0x03];
		for (xx = 0 ; xx < SclWidth ; xx++) {
			xi = x_index[xx];
			bx = x_pos[xx] + HOKAN_DOTS / 2;

			// 重み付け
 			hx1 = gCubicSplines[xi].h1;
			hx2 = gCubicSplines[xi].h2;
			hx3 = gCubicSplines[xi].h3;
			hx4 = gCubicSplines[xi].h4;

			rr = (
				hy1 * (hx1 * RGB565_RED_256(orgbuff1[bx-1]) + hx2 * RGB565_RED_256(orgbuff1[bx+0]) + hx3 * RGB565_RED_256(orgbuff1[bx+1]) + hx4 * RGB565_RED_256(orgbuff1[bx+2])) +
				hy2 * (hx1 * RGB565_RED_256(orgbuff2[bx-1]) + hx2 * RGB565_RED_256(orgbuff2[bx+0]) + hx3 * RGB565_RED_256(orgbuff2[bx+1]) + hx4 * RGB565_RED_256(orgbuff2[bx+2])) +
				hy3 * (hx1 * RGB565_RED_256(orgbuff3[bx-1]) + hx2 * RGB565_RED_256(orgbuff3[bx+0]) + hx3 * RGB565_RED_256(orgbuff3[bx+1]) + hx4 * RGB565_RED_256(orgbuff3[bx+2])) +
				hy4 * (hx1 * RGB565_RED_256(orgbuff4[bx-1]) + hx2 * RGB565_RED_256(orgbuff4[bx+0]) + hx3 * RGB565_RED_256(orgbuff4[bx+1]) + hx4 * RGB565_RED_256(orgbuff4[bx+2]))
				) / 256 / 256;
			gg = (
				hy1 * (hx1 * RGB565_GREEN_256(orgbuff1[bx-1]) + hx2 * RGB565_GREEN_256(orgbuff1[bx+0]) + hx3 * RGB565_GREEN_256(orgbuff1[bx+1]) + hx4 * RGB565_GREEN_256(orgbuff1[bx+2])) +
				hy2 * (hx1 * RGB565_GREEN_256(orgbuff2[bx-1]) + hx2 * RGB565_GREEN_256(orgbuff2[bx+0]) + hx3 * RGB565_GREEN_256(orgbuff2[bx+1]) + hx4 * RGB565_GREEN_256(orgbuff2[bx+2])) +
				hy3 * (hx1 * RGB565_GREEN_256(orgbuff3[bx-1]) + hx2 * RGB565_GREEN_256(orgbuff3[bx+0]) + hx3 * RGB565_GREEN_256(orgbuff3[bx+1]) + hx4 * RGB565_GREEN_256(orgbuff3[bx+2])) +
				hy4 * (hx1 * RGB565_GREEN_256(orgbuff4[bx-1]) + hx2 * RGB565_GREEN_256(orgbuff4[bx+0]) + hx3 * RGB565_GREEN_256(orgbuff4[bx+1]) + hx4 * RGB565_GREEN_256(orgbuff4[bx+2]))
				) / 256 / 256;
			bb = (
				hy1 * (hx1 * RGB565_BLUE_256(orgbuff1[bx-1]) + hx2 * RGB565_BLUE_256(orgbuff1[bx+0]) + hx3 * RGB565_BLUE_256(orgbuff1[bx+1]) + hx4 * RGB565_BLUE_256(orgbuff1[bx+2])) +
				hy2 * (hx1 * RGB565_BLUE_256(orgbuff2[bx-1]) + hx2 * RGB565_BLUE_256(orgbuff2[bx+0]) + hx3 * RGB565_BLUE_256(orgbuff2[bx+1]) + hx4 * RGB565_BLUE_256(orgbuff2[bx+2])) +
				hy3 * (hx1 * RGB565_BLUE_256(orgbuff3[bx-1]) + hx2 * RGB565_BLUE_256(orgbuff3[bx+0]) + hx3 * RGB565_BLUE_256(orgbuff3[bx+1]) + hx4 * RGB565_BLUE_256(orgbuff3[bx+2])) +
				hy4 * (hx1 * RGB565_BLUE_256(orgbuff4[bx-1]) + hx2 * RGB565_BLUE_256(orgbuff4[bx+0]) + hx3 * RGB565_BLUE_256(orgbuff4[bx+1]) + hx4 * RGB565_BLUE_256(orgbuff4[bx+2]))
				) / 256 / 256;

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

			// buffptr[xx] = REMAKE565(rr, gg, bb);
			buffptr[xx] = MAKE565(rr, gg, bb);
		}
		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[SclWidth + 0] = buffptr[SclWidth - 1];
		buffptr[SclWidth + 1] = buffptr[SclWidth - 1];
	}
//	LOGD("CreateScaleCubic_ThreadFund : end");
	return 0;
}

int CreateScaleCubic(int Page, int Half, int Index, int SclWidth, int SclHeight, int OrgWidth, int OrgHeight)
{
//	LOGD("CreateScaleCubic : p=%d, h=%d, i=%d, sw=%d, sh=%d, ow=%d, oh=%d", Page, Half, Index, SclWidth, SclHeight, OrgWidth, OrgHeight);
	int linesize;
	int ret = 0;

	float	sx;	// 元画像の参照x座標
	float	sy;	// 元画像の参照x座標
	int		bx;
	int		by;

	if (gCubicSplines == NULL) {
		gCubicSplines = (CUBIC_SPLINE*)malloc(MAX_CUBIC_SPLINE * sizeof(CUBIC_SPLINE));

		float	n1, n2, n3, n4;

		for (int i = 0 ; i < MAX_CUBIC_SPLINE ; i ++) {
			sy = 1.0f + (1.0f * (float)i / 256.0f);
			by = (int)floor(sy);

			n1 = 1.0f + sy - by;
			n2 =        sy - by;
			n3 = by + 1.0f - sy;
			n4 = by + 2.0f - sy;

			gCubicSplines[i].h1 = (int)(bicubic_spline(n1) * 256);
			gCubicSplines[i].h2 = (int)(bicubic_spline(n2) * 256);
			gCubicSplines[i].h3 = (int)(bicubic_spline(n3) * 256);
			gCubicSplines[i].h4 = 256 - gCubicSplines[i].h1 - gCubicSplines[i].h2 - gCubicSplines[i].h3;
		}
	}

	linesize  = SclWidth + HOKAN_DOTS;

	//  サイズ変更演算領域用領域確保
	if (ScaleMemColumn(SclWidth) < 0) {
		return -5;
	}

	//  サイズ変更画像待避用領域確保
	if (ScaleMemAlloc(linesize, SclHeight) < 0) {
		return -6;
	}

	// データの格納先ポインタリストを更新
	if (RefreshSclLinesPtr(Page, Half, Index, SclHeight, linesize) < 0) {
		return -7;
	}

	int *x_index = gSclIntParam1;
	int *x_pos   = gSclIntParam2;

	for (int xx = 0 ; xx < SclWidth ; xx++) {
		// 元座標
		sx = (float)xx * (float)OrgWidth / (float)SclWidth;
		// 切り捨てして座標を求める
		bx = (int)floor(sx);

		x_pos[xx] = bx;
		x_index[xx] = (int)((sx - bx) * 256.0f);
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
			if (pthread_create(&thread[i], NULL, CreateScaleCubic_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = CreateScaleCubic_ThreadFunc((void*)param[i]);
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

//	LOGD("CreateScaleCubic : End");
	return ret;
}
