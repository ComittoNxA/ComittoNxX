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

extern WORD			**gLinesPtr[];
extern WORD			**gSclLinesPtr[];
extern int			gCancel[];

extern int			gMaxThreadNum;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

int mAmount = 16;

void *ImageSharpen_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int OrgWidth  = range[2];
    int index = range[3];

	WORD *buffptr = nullptr;

	// 使用するバッファを保持
	WORD *orgbuff1;
	WORD *orgbuff2;
	WORD *orgbuff3;

	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	int	rr = 0, gg = 0, bb = 0;
	int		yd3, yd2;

	for (yy = stindex ; yy < edindex ; yy ++) {
		if (gCancel[index]) {
			LOGD("ImageSharpen : cancel.");
			return (void*)ERROR_CODE_USER_CANCELED;
		}

		// バッファ位置
		orgbuff1 = gLinesPtr[index][yy + HOKAN_DOTS / 2 - 1];
		orgbuff2 = gLinesPtr[index][yy + HOKAN_DOTS / 2 + 0];
		orgbuff3 = gLinesPtr[index][yy + HOKAN_DOTS / 2 + 1];

		yd3 = gDitherY_3bit[yy & 0x07];
		yd2 = gDitherY_2bit[yy & 0x03];
		for (xx =  0 ; xx < OrgWidth ; xx++) {
			// 
            rr -= RGB565_RED_256(orgbuff1[xx - 1]) * mAmount;
            rr -= RGB565_RED_256(orgbuff1[xx + 0]) * mAmount * 2;
            rr -= RGB565_RED_256(orgbuff1[xx + 1]) * mAmount;
            rr -= RGB565_RED_256(orgbuff2[xx - 1]) * mAmount * 2;
            rr += RGB565_RED_256(orgbuff2[xx + 0]) * (16 + (mAmount * 12));
            rr -= RGB565_RED_256(orgbuff2[xx + 1]) * mAmount * 2;
            rr -= RGB565_RED_256(orgbuff3[xx - 1]) * mAmount;
            rr -= RGB565_RED_256(orgbuff3[xx + 0]) * mAmount * 2;
            rr -= RGB565_RED_256(orgbuff3[xx + 1]) * mAmount;
            rr /= 16;

            gg -= RGB565_GREEN_256(orgbuff1[xx - 1]) * mAmount;
            gg -= RGB565_GREEN_256(orgbuff1[xx + 0]) * mAmount * 2;
            gg -= RGB565_GREEN_256(orgbuff1[xx + 1]) * mAmount;
            gg -= RGB565_GREEN_256(orgbuff2[xx - 1]) * mAmount * 2;
            gg += RGB565_GREEN_256(orgbuff2[xx + 0]) * (16 + (mAmount * 12));
            gg -= RGB565_GREEN_256(orgbuff2[xx + 1]) * mAmount * 2;
            gg -= RGB565_GREEN_256(orgbuff3[xx - 1]) * mAmount;
            gg -= RGB565_GREEN_256(orgbuff3[xx + 0]) * mAmount * 2;
            gg -= RGB565_GREEN_256(orgbuff3[xx + 1]) * mAmount;
            gg /= 16;

            bb -= RGB565_BLUE_256(orgbuff1[xx - 1]) * mAmount;
            bb -= RGB565_BLUE_256(orgbuff1[xx + 0]) * mAmount * 2;
            bb -= RGB565_BLUE_256(orgbuff1[xx + 1]) * mAmount;
            bb -= RGB565_BLUE_256(orgbuff2[xx - 1]) * mAmount * 2;
            bb += RGB565_BLUE_256(orgbuff2[xx + 0]) * (16 + (mAmount * 12));
            bb -= RGB565_BLUE_256(orgbuff2[xx + 1]) * mAmount * 2;
            bb -= RGB565_BLUE_256(orgbuff3[xx - 1]) * mAmount;
            bb -= RGB565_BLUE_256(orgbuff3[xx + 0]) * mAmount * 2;
            bb -= RGB565_BLUE_256(orgbuff3[xx + 1]) * mAmount;
            bb /= 16;

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

            orgbuff2[xx] = MAKE565(rr, gg, bb);
		}

		// 補完用の余裕
        orgbuff2[-2] = orgbuff2[0];
        orgbuff2[-1] = orgbuff2[0];
        orgbuff2[OrgWidth + 0] = orgbuff2[OrgWidth - 1];
        orgbuff2[OrgWidth + 1] = orgbuff2[OrgWidth - 1];
	}
	return nullptr;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// pOrgWidth  : 余白カット後の幅を返す
// pOrgHeight : 余白カット後の高さを返す
int ImageSharpen(int index, int Page, int Sharpen, int Half, int Count, int OrgWidth, int OrgHeight)
{
//#ifdef DEBUG
	LOGD("ImageSharpen : index=%d, Page=%d, Sharpen=%d, Half=%d, Count=%d, OrgWidth=%d, OrgHeight=%d", index, Page, Sharpen, Half, Count, OrgWidth, OrgHeight);
//#endif

	int ret = 0;

    mAmount = Sharpen;

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][4];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
        param[i][3] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, ImageSharpen_ThreadFunc, (void*)param[i]) != 0) {
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
		if (status[i] != nullptr) {
			ret = (long)status[i];
		}
	}
//	LOGD("ImageSharpen : complete");
	return ret;
}