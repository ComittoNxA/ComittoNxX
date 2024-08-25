#include <malloc.h>
#include <string.h>
#include <math.h>
#include<vector>
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

// 上下左右の端のラインの色の最頻値を調べる
int GetModeColor(int Page, int Half, int Index, int SclWidth, int SclHeight, WORD *ColorL, WORD *ColorR, WORD *ColorT, WORD *ColorB) {

    bool debug = true;

    IMAGEDATA *pData = &gImageData[Page];
    const int OrgWidth  = pData->OrgWidth;
    const int OrgHeight = pData->OrgHeight;

    int ret = 0;

    // 元データ配列化
    ret = SetLinesPtr(Page, Half, Index, OrgWidth, OrgHeight);
    if (ret < 0) {
        return ret;
    }
    if (debug) LOGD("getModeColor Page=%d, Half=%d, 配列化成功", Page, Half);

    WORD *buffptr = NULL;
    WORD *orgbuff1;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    std::vector<WORD> ColorVectorL(OrgHeight);
    std::vector<WORD> ColorVectorR(OrgHeight);
    std::vector<WORD> ColorVectorT(OrgWidth);
    std::vector<WORD> ColorVectorB(OrgWidth);

    // 上下左右の端のラインの色の最頻値を調べる
    // 配列に左右端のラインの色を代入
    for (yy = 0 ; yy < OrgHeight ; yy++) {
        orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2];
        ColorVectorL[yy] = orgbuff1[0 + HOKAN_DOTS / 2];
        ColorVectorR[yy] = orgbuff1[OrgWidth - 1 + HOKAN_DOTS / 2];
    }
    // 配列に上下端のラインの色を代入
    for (xx = 0 ; xx < OrgWidth ; xx++) {
        ColorVectorT[xx] = gLinesPtr[0 + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2];
        ColorVectorB[xx] = gLinesPtr[OrgHeight - 1 + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2];
    }
    // 昇順ソート
    sort(ColorVectorL.begin(), ColorVectorL.end());
    sort(ColorVectorR.begin(), ColorVectorR.end());
    sort(ColorVectorT.begin(), ColorVectorT.end());
    sort(ColorVectorB.begin(), ColorVectorB.end());

    // 最頻値
    int pre_modeL, pre_modeR, pre_modeT, pre_modeB;
    int numL = 1, numR = 1, numT = 1, numB = 1; // テンポラリの出現回数
    int max_numL = 1, max_numR = 1, max_numT = 1, max_numB = 1; // 最頻値の出現回数
    int modeL, modeR, modeT, modeB;

    // 初期値を代入
    modeL = ColorVectorL[0];	// 色の最頻値の初期値
    modeR = ColorVectorR[0];	// 色の最頻値の初期値
    modeT = ColorVectorT[0];	// 色の最頻値の初期値
    modeB = ColorVectorB[0];	// 色の最頻値の初期値

    pre_modeL = ColorVectorL[0];	// 出現する回数を数える値
    pre_modeR = ColorVectorR[0];	// 出現する回数を数える値
    pre_modeT = ColorVectorL[0];	// 出現する回数を数える値
    pre_modeB = ColorVectorR[0];	// 出現する回数を数える値

    // 左右端のラインの色が出現する最頻値を求める
    for (yy = 0 ; yy < OrgHeight ; yy++) {
        // 左のライン
        if (pre_modeL == ColorVectorL[yy]) {
            // 同じ値の場合
            // 出現回数に1を足す
            ++ numL;
        }
        else {
            // 違う値の場合
            // 出現回数が最頻値の出現回数より多ければ最頻値を更新する
            if (numL > max_numL) {
                modeL = pre_modeL;
                max_numL = numL;
            }

            // 出現する回数を数える値を変更
            pre_modeL = ColorVectorL[yy];
            numL = 1;
        }

        // 右のライン
        if (pre_modeR == ColorVectorR[yy]) {
            // 同じ値の場合
            // 出現回数に1を足す
            ++ numR;
        }
        else {
            // 違う値の場合
            // 出現回数が最頻値の出現回数より多ければ最頻値を更新する
            if (numR > max_numR) {
                modeR = pre_modeR;
                max_numR = numR;
            }

            // 出現する回数を数える値を変更
            pre_modeR = ColorVectorR[yy];
            numR = 1;
        }
    }
    // 後処理
    if (numL > max_numL) {
        modeL = pre_modeL;
        max_numL = numL;
    }
    if (numR > max_numR) {
        modeR = pre_modeR;
        max_numR = numR;
    }
    *ColorL = modeL;
    *ColorR = modeR;

    // 上下端のラインの色が出現する最頻値を求める
    for (xx = 0 ; xx < OrgWidth ; xx++) {
        // 上のライン
        if (pre_modeT == ColorVectorT[xx]) {
            // 同じ値の場合
            // 出現回数に1を足す
            ++ numT;
        }
        else {
            // 違う値の場合
            // 出現回数が最頻値の出現回数より多ければ最頻値を更新する
            if (numT > max_numT) {
                modeT = pre_modeT;
                max_numT = numT;
            }

            // 出現する回数を数える値を変更
            pre_modeT = ColorVectorT[xx];
            numT = 1;
        }
        // 下のライン
        if (pre_modeB == ColorVectorB[xx]) {
            // 同じ値の場合
            // 出現回数に1を足す
            ++ numB;
        }
        else {
            // 違う値の場合
            // 出現回数が最頻値の出現回数より多ければ最頻値を更新する
            if (numB > max_numB) {
                modeB = pre_modeB;
                max_numB = numB;
            }

            // 出現する回数を数える値を変更
            pre_modeB = ColorVectorB[xx];
            numB = 1;
        }
    }
    // 後処理
    if (numT > max_numT) {
        modeT = pre_modeT;
        max_numT = numT;
    }
    if (numB > max_numB) {
        modeB = pre_modeB;
        max_numB = numB;
    }
    *ColorT = modeT;
    *ColorB = modeB;

    if (debug) LOGD("getModeColor: End. ColorL=%d, ColorR=%d, ColorT=%d, ColorB=%d", *ColorL, *ColorR, *ColorT, *ColorB);
    return ret;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// *pLeft, *pRight, *pTop, *pBottom  : 余白カット量を返す
int GetMarginSize(int Page, int Half, int Index, int SclWidth, int SclHeight, int Margin, int MarginColor, int *pLeft, int *pRight, int *pTop, int *pBottom)
{
    bool debug = true;

    IMAGEDATA *pData = &gImageData[Page];
    int OrgWidth  = pData->OrgWidth;
    int OrgHeight = pData->OrgHeight;
    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 元サイズ Index=%d, OrgWidth=%d, OrgHeight=%d, SclWidth=%d, SclHeight=%d, Margin=%d, MarginColor=%d", Page, Half, Index, OrgWidth, OrgHeight, SclWidth, SclHeight, Margin, MarginColor);

    int ret = 0;

    WORD ColorT;
    WORD ColorB;
    WORD ColorL;
    WORD ColorR;

    // 使用するバッファを保持
    int left = 0;
    int right = 0;
    int top = 0;
    int bottom = 0;

    int limit;
    int space;
    int range;

    // パラメタ設定 limit=白黒以外の色の許容×0.1％ 、space=余白のカット％ 、range=余白を探す範囲％
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
            limit = 8;
            space = 90;
            range = 45;
            break;
        case 4:		// 特上
            limit = 20;
            space = 95;
            range = 50;
            break;
        default:	// 最強
            limit = 50;
            space = 100;
            range = 100;
            break;
    }

    // 上下左右の端のラインの色の最頻値を調べる
    if (MarginColor == 1) {
        if (debug) LOGD("GetMarginSize 色の最頻値を余白とします.");
        if (debug) LOGD("GetMarginSize 色の最頻値を取得します.");
        ret = GetModeColor(Page, Half, Index, SclWidth, SclHeight, &ColorL, &ColorR, &ColorT, &ColorB);
        if (ret < 0) {
            if (debug) LOGD("GetMarginSize 色の最頻値取得に失敗しました. return=%d", ret);
            return ret;
        }
        else {
            if (debug) LOGD("GetMarginSize 色の最頻値取得に成功しました.");
        }
    }
    else {
        if (debug) LOGD("GetMarginSize 白か黒を余白とします.");
    }

    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 配列化準備", Page, Half);
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
    int whitecnt, blackcnt, colorcnt;

    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 余白調査範囲 CheckCX=%d, CheckCY=%d", Page, Half, CheckCX, CheckCY);
    for (yy = 0 ; yy < CheckCY ; yy ++) {
        orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2];
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        top = yy;
        for (xx = 0 ; xx < OrgWidth ; xx ++) {
            if (MarginColor == 0) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(orgbuff1[xx + HOKAN_DOTS / 2])) {
                        whitecnt++;
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(orgbuff1[xx + HOKAN_DOTS / 2])) {
                        blackcnt++;
                    }
                }
            }
            else {
                // 最頻色チェック
                if (!COLOR_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], ColorT)) {
                    colorcnt++;
                }
            }
        }

        if (MarginColor == 0) {
            // 白チェック
            if (MODE_WHITE) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (whitecnt >= OrgWidth * limit / 1000) {
                    MODE_WHITE = false;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (blackcnt >= OrgWidth * limit / 1000) {
                    MODE_BLACK = false;
                }
            }
            if (!MODE_WHITE && !MODE_BLACK) {
                break;
            }
        }
        else {
            // 最頻色チェック
            // (limit/10)%以上がオーバーしたら余白ではないとする
            if (colorcnt >= OrgWidth * limit / 1000) {
                break;
            }
        }
    }
    
    for (int yy = OrgHeight - 1 ; yy >= OrgHeight - CheckCY ; yy --) {
        orgbuff1 = gLinesPtr[yy + HOKAN_DOTS / 2];
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        bottom = OrgHeight - 1 - yy;
        for (xx = 0 ; xx < OrgWidth ; xx ++) {
            if (MarginColor == 0) {
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
            else {
                // 最頻色チェック
                if (!COLOR_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], ColorB)) {
                    colorcnt++;
                }
            }
        }

        if (MarginColor == 0) {
            // 白チェック
            if (MODE_WHITE) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (whitecnt >= OrgWidth * limit / 1000) {
                    MODE_WHITE = false;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (blackcnt >= OrgWidth * limit / 1000) {
                    MODE_BLACK = false;
                }
            }
            if (!MODE_WHITE && !MODE_BLACK) {
                break;
            }
        }
        else {
            // 最頻色チェック
            // (limit/10)%以上がオーバーしたら余白ではないとする
            if (colorcnt >= OrgWidth * limit / 1000) {
                break;
            }
        }
    }
    if (debug) LOGD("GetMarginSize Page=%d, Half=%d, 縦カット値 上=%d, 下=%d", Page, Half, top, bottom);

    for (xx = 0 ; xx < CheckCX ; xx ++) {
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        left = xx;
        for (yy = top + 1 ; yy < OrgHeight - bottom ; yy ++) {
            if (MarginColor == 0) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                        whitecnt++;
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                        blackcnt++;
                    }
                }
            }
            else {
                // 最頻色チェック
                if (!COLOR_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], ColorL)) {
                    colorcnt++;
                }
            }
        }

        if (MarginColor == 0) {
            // 白チェック
            if (MODE_WHITE) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (whitecnt >= (OrgHeight - top - bottom) * limit / 1000) {
                    MODE_WHITE = false;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (blackcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                    MODE_BLACK = false;
                }
            }
            if (!MODE_WHITE && !MODE_BLACK) {
                break;
            }
        }
        else {
            // 最頻色チェック
            // (limit/10)%以上がオーバーしたら余白ではないとする
            if (colorcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                break;
            }
        }
    }

    for (int xx = OrgWidth - 1 ; xx >= OrgWidth - CheckCX ; xx --) {
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        right = OrgWidth - 1 - xx;
        for (yy = top + 1 ; yy < OrgHeight - bottom ; yy ++) {
            if (MarginColor == 0) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                        whitecnt++;
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2])) {
                        blackcnt++;
                    }
                }
            } else {
                // 最頻色チェック
                if (!COLOR_CHECK(gLinesPtr[yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], ColorR)) {
                    colorcnt++;
                }
            }
        }

        if (MarginColor == 0) {
            // 白チェック
            if (MODE_WHITE) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (whitecnt >= (OrgHeight - top - bottom) * limit / 1000) {
                    MODE_WHITE = false;
                }
            }
            // 黒チェック
            if (MODE_BLACK) {
                // (limit/10)%以上がオーバーしたら余白ではないとする
                if (blackcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                    MODE_BLACK = false;
                }
            }
            if (!MODE_WHITE && !MODE_BLACK) {
                break;
            }
        }
        else {
            // 最頻色チェック
            // (limit/10)%以上がオーバーしたら余白ではないとする
            if (colorcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                break;
            }
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
int ImageMarginCut(int Page, int Half, int Index, int SclWidth, int SclHeight, int left, int right, int top, int bottom, int Margin, int MarginColor, int *pReturnWidth, int *pReturnHeight)
{
    bool debug = false;

    IMAGEDATA *pData = &gImageData[Page];
    int OrgWidth  = pData->OrgWidth;
    int OrgHeight = pData->OrgHeight;
    if (debug) LOGD("ImageMarginCut Page=%d, Half=%d, 元サイズ Index=%d, OrgWidth=%d, OrgHeight=%d, left=%d, right=%d, top=%d, bottom=%d, SclWidth=%d, SclHeight=%d, Margin=%d, MarginColor=%d", Page, Half, Index, OrgWidth, OrgHeight, SclWidth, SclHeight, left, right, top, bottom, Margin, MarginColor);

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
