#include <malloc.h>
#include <string.h>
#include <math.h>
#include <pthread.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif
//#include <unistd.h>
#include "Image.h"
#include <unistd.h>

extern IMAGEDATA	*gImageData;

extern WORD			**gLinesPtr;
extern WORD			**gSclLinesPtr;
extern int			gCancel;

extern int			gMaxThreadNum;

void *ImageMarginCut_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int SclWidth  = range[2];
	int SclHeight = range[3];
	int OrgWidth  = range[4];
	int OrgHeight = range[5];
	int CutT      = range[6];
	int CutL      = range[7];

	WORD *orgbuff1;
	WORD *buffptr;

	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageMarginCut : loop yy=%d", yy);
		if (gCancel) {
//			LOGD("ImageRotate : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)-1;
		}

		// バッファ位置
		buffptr = gSclLinesPtr[yy];
//		LOGD("ImageRotate : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		orgbuff1 = gLinesPtr[yy + CutT + HOKAN_DOTS / 2];
		memcpy(buffptr, &orgbuff1[CutL + HOKAN_DOTS / 2], SclWidth * sizeof(WORD));

		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[SclWidth + 0] = buffptr[SclWidth - 1];
		buffptr[SclWidth + 1] = buffptr[SclWidth - 1];
	}
	return 0;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// *pLeft, *pRight, *pTop, *pBottom  : 余白カット量を返す
int GetMarginSize(int Page, int Half, int Index, int SclWidth, int SclHeight, int Margin, int *pLeft, int *pRight, int *pTop, int *pBottom)
{
    bool debug = false;

    IMAGEDATA *pData = &gImageData[Page];
    int OrgWidth  = pData->OrgWidth;
    int OrgHeight = pData->OrgHeight;
    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 元サイズ Index=%d, OrgWidth=%d, OrgHeight=%d, SclWidth=%d, SclHeight=%d, Margin=%d", Page, Half, Index, OrgWidth, OrgHeight, SclWidth, SclHeight, Margin);

    int ret = 0;

    // 使用するバッファを保持
    int left = 0;
    int right = 0;
    int top = 0;
    int bottom = 0;

    int limit;
    int space;
    int range;

    // パラメタ設定 limit=白黒以外の色の許容％ 、space=余白のカット％ 、range=余白を探す範囲％
    switch (Margin) {
        case 0:		// なし
            return 0;
        case 1:		// 弱
            limit = 5;
            space = 60;
            range = 25;
            break;
        case 2:		// 中
            limit = 6;
            space = 80;
            range = 30;
            break;
        case 3:		// 強
            limit = 7;
            space = 90;
            range = 45;
            break;
        default:	// 最強
            limit = 8;
            space = 100;
            range = 100;
            break;
    }

    if (Margin > 0) {
        // 元データ配列化
        ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
        if (ret < 0) {
            return ret;
        }
    }
    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 配列化成功", Page, Half);

    WORD *buffptr = NULL;
    WORD *orgbuff1;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    int CheckCX = OrgWidth * range / 100;
    int CheckCY = OrgHeight * range / 100;
    bool MODE_WHITE, MODE_BLACK;
    int whitecnt, blackcnt;

    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 余白調査範囲 CheckCX=%d, CheckCY=%d", Page, Half, CheckCX, CheckCY);
    for (yy = 0 ; yy < CheckCY ; yy ++) {
        orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2];
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        top = yy;
        for (xx = 0 ; xx < OrgWidth ; xx ++) {
            // 白チェック
            if (MODE_WHITE) {
                if (!WHITE_CHECK(orgbuff1[xx + HOKAN_DOTS / 2])) {
                    whitecnt ++;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                if (!BLACK_CHECK(orgbuff1[xx + HOKAN_DOTS / 2])) {
                    blackcnt ++;
                }
            }
        }

        if (MODE_WHITE) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (whitecnt >= OrgWidth * limit / 1000) {
                // 5%以上
                MODE_WHITE = false;
            }
        }

        if (MODE_BLACK) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (blackcnt >= OrgWidth * limit / 1000) {
                // 5%以上
                MODE_BLACK = false;
            }
        }
        
        if (!MODE_WHITE && !MODE_BLACK) {
            break;
        }
    }
    
    for (int yy = OrgHeight - 1 ; yy >= OrgHeight - CheckCY ; yy --) {
        orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2];
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        bottom = OrgHeight - 1 - yy;
        for (xx = 0 ; xx < OrgWidth ; xx ++) {
            // 白チェック
            if (MODE_WHITE) {
                if (!WHITE_CHECK(orgbuff1[xx + HOKAN_DOTS / 2])) {
                    whitecnt ++;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                if (!BLACK_CHECK(orgbuff1[xx + HOKAN_DOTS / 2])) {
                    blackcnt ++;
                }
            }
        }

        if (MODE_WHITE) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (whitecnt >= OrgWidth * limit / 1000) {
                // 5%以上
                MODE_WHITE = false;
            }
        }

        if (MODE_BLACK) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (blackcnt >= OrgWidth * limit / 1000) {
                // 5%以上
                MODE_BLACK = false;
            }
        }
        
        if (!MODE_WHITE && !MODE_BLACK) {
            break;
        }
    }
    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 縦カット値 上=%d, 下=%d", Page, Half, top, bottom);

    for (xx = 0 ; xx < CheckCX ; xx ++) {
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        left = xx;
        for (yy = top + 1 ; yy < OrgHeight - bottom ; yy ++) {
            // 白チェック
            if (MODE_WHITE) {
                if (!WHITE_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                    whitecnt ++;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                if (!BLACK_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                    blackcnt ++;
                }
            }
        }

        if (MODE_WHITE) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (whitecnt >= (OrgHeight - top - bottom) * limit / 1000) {
                // 5%以上
                MODE_WHITE = false;
            }
        }

        if (MODE_BLACK) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (blackcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                // 5%以上
                MODE_BLACK = false;
            }
        }
        
        if (!MODE_WHITE && !MODE_BLACK) {
            break;
        }
    }

    for (int xx = OrgWidth - 1 ; xx >= OrgWidth - CheckCX ; xx --) {
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        right = OrgWidth - 1 - xx;
        for (yy = top + 1 ; yy < OrgHeight - bottom ; yy ++) {
            // 白チェック
            if (MODE_WHITE) {
                if (!WHITE_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                    whitecnt ++;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                if (!BLACK_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                    blackcnt ++;
                }
            }
        }

        if (MODE_WHITE) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (whitecnt >= (OrgHeight - top - bottom) * limit / 1000) {
                // 5%以上
                MODE_WHITE = false;
            }
        }

        if (MODE_BLACK) {
            // 0.5%以上がオーバーしたら余白ではないとする
            if (blackcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                // 5%以上
                MODE_BLACK = false;
            }
        }
        
        if (!MODE_WHITE && !MODE_BLACK) {
            break;
        }
    }

    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 横カット値 左=%d, 右=%d", Page, Half, left, right);

    left = left * space / 100;
    right = right * space / 100;
    top = top * space / 100;
    bottom = bottom * space / 100;
    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, カット率反映 CutLeft=%d, CutRight=%d, CutTop=%d, CutBottom=%d", Page, Half, left, right, top, bottom);

    if (left + right <= 0 && top + bottom <= 0) {
        // 余白無し
        return 0;
    }

    *pLeft = left;
    *pRight = right;
    *pTop = top;
    *pBottom = bottom;
    return 1;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// pReturnWidth  : 余白カット後の幅を返す
// pReturnHeight : 余白カット後の高さを返す
int ImageMarginCut(int Page, int Half, int Index, int SclWidth, int SclHeight, int left, int right, int top, int bottom, int Margin, int *pReturnWidth, int *pReturnHeight)
{
    bool debug = false;

    IMAGEDATA *pData = &gImageData[Page];
    int OrgWidth  = pData->OrgWidth;
    int OrgHeight = pData->OrgHeight;
    if (debug) LOGD("ImageMarginCut Page=%d, Half=%d, 元サイズ Index=%d, OrgWidth=%d, OrgHeight=%d, left=%d, right=%d, top=%d, bottom=%d, SclWidth=%d, SclHeight=%d, Margin=%d", Page, Half, Index, OrgWidth, OrgHeight, SclWidth, SclHeight, left, right, top, bottom, Margin);

    int ret = 0;

    // 使用するバッファを保持
    int ReturnWidth = 0;
    int ReturnHeight = 0;

    ReturnWidth  = OrgWidth - left - right;
    ReturnHeight = OrgHeight - top - bottom;
    if (debug) LOGD("ImageMarginCut Page=%d, Half=%d, 出力サイズ ReturnWidth=%d, ReturnHeight=%d", Page, Half, ReturnWidth, ReturnHeight);
		// 縮小画像から取得
	int linesize  = ReturnWidth + HOKAN_DOTS;

	//  サイズ変更画像待避用領域確保
	if (ScaleMemAlloc(linesize, ReturnHeight) < 0) {
		return -6;
	}

	// データの格納先ポインタリストを更新
	if (RefreshSclLinesPtr(Page, Half, Index, ReturnHeight, linesize) < 0) {
		return -7;
	}

	ret = 1;

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][8];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = ReturnHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = ReturnWidth;
		param[i][3] = ReturnHeight;
		param[i][4] = OrgWidth;
		param[i][5] = OrgHeight;
		param[i][6] = top;
		param[i][7] = left;
		
		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], NULL, ImageMarginCut_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageMarginCut_ThreadFunc((void*)param[i]);
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
	*pReturnWidth = ReturnWidth;
	*pReturnHeight = ReturnHeight;

	//LOGD("ImageMarginCut : complete");
	return ret;
}
