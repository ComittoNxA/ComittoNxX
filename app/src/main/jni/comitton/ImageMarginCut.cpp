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
#include "Image.h"
#include <unistd.h>

//#define DEBUG

extern IMAGEDATA	*gImageData[];

extern WORD			**gLinesPtr[];
extern WORD			**gSclLinesPtr[];
extern int			gCancel[];

extern int			gMaxThreadNum;

void *ImageMarginCut_ThreadFunc(void *param)
{
    int *range = (int*)param;
    int index     = range[0];
    int stindex   = range[1];
    int edindex   = range[2];
    int ReturnWidth  = range[3];
    int CutT      = range[4];
    int CutL      = range[5];

    WORD *orgbuff1;
    WORD *buffptr;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageMarginCut : loop yy=%d", yy);
        if (gCancel[index]) {
//			LOGD("ImageRotate : cancel.");
//			ReleaseBuff(Page, 1, Half);
            return (void*)ERROR_CODE_USER_CANCELED;
        }

        // バッファ位置
        buffptr = gSclLinesPtr[index][yy];
//		LOGD("ImageRotate : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

        orgbuff1 = gLinesPtr[index][yy + CutT + HOKAN_DOTS / 2];
        memcpy(buffptr, &orgbuff1[CutL + HOKAN_DOTS / 2], ReturnWidth * sizeof(WORD));

        // 補完用の余裕
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[ReturnWidth + 0] = buffptr[ReturnWidth - 1];
        buffptr[ReturnWidth + 1] = buffptr[ReturnWidth - 1];
    }
    return nullptr;
}

// 上下左右の端のラインの色の最頻値を調べる
int GetModeColor(int index, int Page, int Half, int Index, int StartH, int StartW, WORD *ColorL, WORD *ColorR, WORD *ColorT, WORD *ColorB) {

    IMAGEDATA *pData = &gImageData[index][Page];
    const int OrgWidth  = pData->OrgWidth;
    const int OrgHeight = pData->OrgHeight;

    int ret = 0;

    // 元データ配列化
    ret = SetLinesPtr(index, Page, Half, Index, OrgWidth, OrgHeight);
    if (ret < 0) {
        LOGE("GetModeColor 元データ配列化に失敗しました. return=%d", ret);
        return ret;
    }
#ifdef DEBUG
    LOGD("getModeColor Page=%d, Half=%d, 配列化成功", Page, Half);
#endif
    WORD *buffptr = nullptr;
    WORD *orgbuff1;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    std::vector<WORD> ColorVectorL(OrgHeight);
    std::vector<WORD> ColorVectorR(OrgHeight);
    std::vector<WORD> ColorVectorT(OrgWidth);
    std::vector<WORD> ColorVectorB(OrgWidth);

#ifdef DEBUG
    LOGD("getModeColor Page=%d, Half=%d, index=%d, gLinesPtr[%d]=%ld, sizeof(gLinesPtr[%d])=%ld", Page, Half, index, index, (long)(gLinesPtr[index]), index, sizeof(gLinesPtr[index]));
#endif
    // 上下左右の端のラインの色の最頻値を調べる
    // 配列に左右端のラインの色を代入
    for (yy = 0 ; yy < OrgHeight ; yy++) {
        orgbuff1 = gLinesPtr[index][yy + HOKAN_DOTS / 2];
        ColorVectorL[yy] = orgbuff1[0 + StartW + HOKAN_DOTS / 2];
        ColorVectorR[yy] = orgbuff1[OrgWidth - StartW - 1 + HOKAN_DOTS / 2];
    }
    // 配列に上下端のラインの色を代入
    for (xx = 0 ; xx < OrgWidth ; xx++) {
        ColorVectorT[xx] = gLinesPtr[index][0 + StartH + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2];
        ColorVectorB[xx] = gLinesPtr[index][OrgHeight - StartH - 1 + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2];
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
#ifdef DEBUG
    LOGD("getModeColor: End. ColorL=%d, ColorR=%d, ColorT=%d, ColorB=%d", *ColorL, *ColorR, *ColorT, *ColorB);
#endif
    return ret;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// *pLeft, *pRight, *pTop, *pBottom  : 余白カット量を返す
int GetMarginSize(int index, int Page, int Half, int Index, int Margin, int MarginColor, int *pLeft, int *pRight, int *pTop, int *pBottom)
{

    IMAGEDATA *pData = &gImageData[index][Page];
    int OrgWidth  = pData->OrgWidth;
    int OrgHeight = pData->OrgHeight;
#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, 元サイズ Index=%d, OrgWidth=%d, OrgHeight=%d, Margin=%d, MarginColor=%d", Page, Half, Index, OrgWidth, OrgHeight, Margin, MarginColor);
#endif
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

    /** 違う色の出現回数の許容する割合(単位0.1%)\n
     *  違う色の回数がこの数値を超えると余白でないと判定する
     */
    int limit;
    /** 余白判定された幅のうち画像から削除する割合(単位1%)\n
     *  100%からこの値を引いた割合に応じて画像の縁に余白を残す
     */
    int space;
    /** 余白判定を行う範囲(単位1%)\n
     *  画像のほとんどが余白のときカットする量を制限する
     */
    int range;
    /** 余白判定を開始するまでの無視区間(単位0.1%)\n
     *  画面端にノイズがあっても余白削除を失敗させない
     */
    int start;
    /** 色判定時のビットマスク深度\n
     *  RGBすべてが上位ビットからこのビット数一致すれば同じ色と判定する
     */
    int bitmask;

    switch (Margin) {
        case BMPMARGIN_NONE:		// なし
            return 0;
        case BMPMARGIN_WEAK:		// 弱
            limit = 5;
            space = 60;
            range = 25;
            start = 1;
            bitmask = 4;
            break;
        case BMPMARGIN_MEDIUM:		// 中
            limit = 6;
            space = 80;
            range = 30;
            start = 2;
            bitmask = 4;
            break;
        case BMPMARGIN_STRONG:		// 強
            limit = 8;
            space = 90;
            range = 45;
            start = 3;
            bitmask = 3;
            break;
        case BMPMARGIN_SPECIAL:		// 特上
            limit = 20;
            space = 95;
            range = 50;
            start = 5;
            bitmask = 3;
            break;
        default:	// 最強
            limit = 50;
            space = 100;
            range = 100;
            start = 10;
            bitmask = 2;
            break;
    }

    int startH = ceil((float)OrgHeight * start / 1000);
    int startW = ceil((float)OrgWidth * start / 1000);

    WORD mask;

    switch (bitmask) {
        case 0:
            mask = 0x0000;  // 上位0ビット
            break;
        case 1:
            mask = 0x8410;  // 上位1ビット
            break;
        case 2:
            mask = 0xC618;  // 上位2ビット
            break;
        case 3:
            mask = 0xE71B;  // 上位3ビット
            break;
        case 4:
            mask = 0xF79D;  // 上位4ビット
            break;
        default:
            mask = 0xF79D;  // 上位4ビット
    }

    // 上下左右の端のラインの色の最頻値を調べる
    if (MarginColor == MARGIN_COLOR_ALL_COLORS) {
#ifdef DEBUG
        LOGD("GetMarginSize 色の最頻値を余白とします.");
        LOGD("GetMarginSize 色の最頻値を取得します.");
#endif
        ret = GetModeColor(index, Page, Half, Index, startH, startW, &ColorL, &ColorR, &ColorT, &ColorB);
        if (ret < 0) {
            LOGE("GetMarginSize 色の最頻値取得に失敗しました. return=%d", ret);
            return ret;
        }
        else {
#ifdef DEBUG
            LOGD("GetMarginSize 色の最頻値取得に成功しました.");
#endif
        }
    }
    else {
#ifdef DEBUG
        LOGD("GetMarginSize 白か黒を余白とします.");
#endif
    }

#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, 配列化準備", Page, Half);
#endif
    if (Margin > 0) {
        // 元データ配列化
        ret = SetLinesPtr(index, Page, Half, Index, OrgWidth, OrgHeight);
        if (ret < 0) {
            return ret;
        }
    }
#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, 配列化成功", Page, Half);
#endif
    WORD *buffptr = nullptr;
    WORD *orgbuff1;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    int CheckCX = OrgWidth * range / 100;
    int CheckCY = OrgHeight * range / 100;
    /** 行または列内で白の出現回数がlimitに達していないときtrue */
    bool MODE_WHITE;
    /** 行または列内で黒の出現回数がlimitに達していないときtrue */
    bool MODE_BLACK;
    /** 行または列内の白以外の出現回数 */
    int whitecnt;
    /** 行または列内の黒以外の出現回数 */
    int blackcnt;
    /** 行または列内の最頻色以外の出現回数 */
    int colorcnt;

#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, 余白調査範囲 CheckCX=%d, CheckCY=%d", Page, Half, CheckCX, CheckCY);
#endif

    bool breakflag = false;
    for (yy = startH + 0; yy < CheckCY ; yy ++) {
        orgbuff1 = gLinesPtr[index][yy + HOKAN_DOTS / 2];
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        top = yy;
        for (xx = 0 ; xx < OrgWidth ; xx ++) {
            if (MarginColor == MARGIN_COLOR_WHITE_AND_BLACK) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], mask)) {
                        whitecnt++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (whitecnt >= OrgWidth * limit / 1000) {
                            MODE_WHITE = false;
                        }
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], mask)) {
                        blackcnt++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (blackcnt >= OrgWidth * limit / 1000) {
                            MODE_BLACK = false;
                        }
                    }
                }
                if (!MODE_WHITE && !MODE_BLACK) {
                    breakflag = true;
                    break;
                }
            }
            else {
                // 最頻色チェック
                if (!COLOR_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], ColorT, mask)) {
                    colorcnt++;
                    // (limit/10)%をオーバーしたら余白ではないとする
                    if (colorcnt >= OrgWidth * limit / 1000) {
                        breakflag = true;
                        break;
                    }
                }
            }
        }
        if (breakflag) {
            break;
        }
    }

    breakflag = false;
    for (int yy = OrgHeight - startH - 1 ; yy >= OrgHeight - CheckCY ; yy --) {
        orgbuff1 = gLinesPtr[index][yy + HOKAN_DOTS / 2];
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        bottom = OrgHeight - 1 - yy;
        for (xx = 0 ; xx < OrgWidth ; xx ++) {
            if (MarginColor == MARGIN_COLOR_WHITE_AND_BLACK) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], mask)) {
                        whitecnt ++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (whitecnt >= OrgWidth * limit / 1000) {
                            MODE_WHITE = false;
                        }
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], mask)) {
                        blackcnt ++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (blackcnt >= OrgWidth * limit / 1000) {
                            MODE_BLACK = false;
                        }
                    }
                }
                if (!MODE_WHITE && !MODE_BLACK) {
                    breakflag = true;
                    break;
                }
            }
            else {
                // 最頻色チェック
                if (!COLOR_CHECK(orgbuff1[xx + HOKAN_DOTS / 2], ColorB, mask)) {
                    colorcnt++;
                    // (limit/10)%をオーバーしたら余白ではないとする
                    if (colorcnt >= OrgWidth * limit / 1000) {
                        breakflag = true;
                        break;
                    }
                }
            }
        }
        if (breakflag) {
            break;
        }

    }

#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, 縦カット値 上=%d, 下=%d", Page, Half, top, bottom);
#endif

    breakflag = false;
    for (xx = startW + 0; xx < CheckCX ; xx ++) {
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;	// 白でないカウンタ
        blackcnt = 0;	// 黒でないカウンタ
        colorcnt = 0;	// 最頻色でないカウンタ
        left = xx;
        for (yy = top + 1 ; yy < OrgHeight - bottom ; yy ++) {
            if (MarginColor == MARGIN_COLOR_WHITE_AND_BLACK) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(gLinesPtr[index][yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], mask)) {
                        whitecnt++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (whitecnt >= (OrgHeight - top - bottom) * limit / 1000) {
                            MODE_WHITE = false;
                        }
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(gLinesPtr[index][yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], mask)) {
                        blackcnt++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (blackcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                            MODE_BLACK = false;
                        }
                    }
                }
                if (!MODE_WHITE && !MODE_BLACK) {
                    breakflag = true;
                    break;
                }
            }
            else {
                // 最頻色チェック
                if (!COLOR_CHECK(gLinesPtr[index][yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], ColorL, mask)) {
                    colorcnt++;
                    // (limit/10)%をオーバーしたら余白ではないとする
                    if (colorcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                        breakflag = true;
                        break;
                    }
                }
            }
        }
        if (breakflag) {
            break;
        }
    }

    breakflag = false;
    for (int xx = OrgWidth - startW - 1 ; xx >= OrgWidth - CheckCX ; xx --) {
        MODE_WHITE = true;
        MODE_BLACK = true;
        whitecnt = 0;    // 白でないカウンタ
        blackcnt = 0;    // 黒でないカウンタ
        colorcnt = 0;    // 最頻色でないカウンタ
        right = OrgWidth - 1 - xx;
        for (yy = top + 1; yy < OrgHeight - bottom; yy++) {
            if (MarginColor == MARGIN_COLOR_WHITE_AND_BLACK) {
                // 白チェック
                if (MODE_WHITE) {
                    if (!WHITE_CHECK(gLinesPtr[index][yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], mask)) {
                        whitecnt++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (whitecnt >= (OrgHeight - top - bottom) * limit / 1000) {
                            MODE_WHITE = false;
                        }
                    }
                }
                // 黒チェック
                if (MODE_BLACK) {
                    if (!BLACK_CHECK(gLinesPtr[index][yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], mask)) {
                        blackcnt++;
                        // (limit/10)%をオーバーしたら余白ではないとする
                        if (blackcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                            MODE_BLACK = false;
                        }
                    }
                }
                if (!MODE_WHITE && !MODE_BLACK) {
                    breakflag = true;
                    break;
                }
            } else {
                // 最頻色チェック
                if (!COLOR_CHECK(gLinesPtr[index][yy + HOKAN_DOTS / 2][xx + HOKAN_DOTS / 2], ColorR, mask)) {
                    colorcnt++;
                    // (limit/10)%をオーバーしたら余白ではないとする
                    if (colorcnt >= (OrgHeight - top - bottom) * limit / 1000) {
                        breakflag = true;
                        break;
                    }
                }
            }
        }
        if (breakflag) {
            break;
        }
    }

#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, 横カット値 左=%d, 右=%d", Page, Half, left, right);
#endif

    if(left <= startW){left = 0;}
    if(right <= startW){right = 0;}
    if(top <= startH){top = 0;}
    if(bottom <= startH){bottom = 0;}

    left = left * space / 100;
    right = right * space / 100;
    top = top * space / 100;
    bottom = bottom * space / 100;
#ifdef DEBUG
    LOGD("GetMarginSize Page=%d, Half=%d, カット率反映 CutLeft=%d, CutRight=%d, CutTop=%d, CutBottom=%d", Page, Half, left, right, top, bottom);
#endif
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
int ImageMarginCut(int index, int Page, int Half, int Count, int left, int right, int top, int bottom, int Margin, int MarginColor, int *pReturnWidth, int *pReturnHeight)
{

    int OrgWidth  = *pReturnWidth;
    int OrgHeight = *pReturnHeight;

#ifdef DEBUG
    LOGD("ImageMarginCut : index=%d, Page=%d, Half=%d, 開始します. Count=%d, OrgWidth=%d, OrgHeight=%d, left=%d, right=%d, top=%d, bottom=%d, Margin=%d, MarginColor=%d", index, Page, Half, Count, OrgWidth, OrgHeight, left, right, top, bottom, Margin, MarginColor);
#endif
    int ret = 0;

    // 使用するバッファを保持
    int ReturnWidth = OrgWidth - left - right;
    int ReturnHeight = OrgHeight - top - bottom;

#ifdef DEBUG
    LOGD("ImageMarginCut : index=%d, Page=%d, Half=%d, 出力サイズ ReturnWidth=%d, ReturnHeight=%d", index, Page, Half, ReturnWidth, ReturnHeight);
#endif
		// 縮小画像から取得
	int linesize  = ReturnWidth + HOKAN_DOTS;

	//  サイズ変更画像待避用領域確保
    ret = ScaleMemAlloc(index, linesize, ReturnHeight);
	if (ret < 0) {
		return ret;
	}

	// データの格納先ポインタリストを更新
    ret = RefreshSclLinesPtr(index, Page, Half, Count, ReturnHeight, linesize);
	if (ret < 0) {
		return ret;
	}

    ret = 1;

    pthread_t thread[gMaxThreadNum];
    int start = 0;
    int param[gMaxThreadNum][6];
    void *status[gMaxThreadNum];

    for (int i = 0 ; i < gMaxThreadNum ; i ++) {
        param[i][0] = index;
        param[i][1] = start;
        param[i][2] = start = ReturnHeight * (i + 1)  / gMaxThreadNum;
        param[i][3] = ReturnWidth;
        param[i][4] = top;
        param[i][5] = left;

        if (i < gMaxThreadNum - 1) {
            /* スレッド起動 */
            if (pthread_create(&thread[i], nullptr, ImageMarginCut_ThreadFunc, (void*)param[i]) != 0) {
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
        if (status[i] != nullptr) {
            ret = (long)status[i];
        }
    }

    *pReturnWidth = ReturnWidth;
    *pReturnHeight = ReturnHeight;

#ifdef DEBUG
    LOGD("ImageMarginCut : index=%d, Page=%d, Half=%d, 終了します. *pReturnWidth=%d, *pReturnHeight=%d", index, Page, Half, *pReturnWidth, *pReturnHeight);
#endif
	return ret;
}

