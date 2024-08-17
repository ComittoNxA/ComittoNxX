#include <android/log.h>
#include <avif/avif.h>
#include <cstring>

#include "Image.h"

extern char	*gLoadBuffer;
extern long	gLoadFileSize;
extern int gLoadError;
extern int gMaxThreadNum;

extern BUFFMNG *gBuffMng;
extern long	gBuffNum;

extern int gCancel;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

// avifの読み込み
int LoadImageAvif(IMAGEDATA *pData, int page, int scale)
{

    int returnCode = 0;         // この関数のリターンコード

    avifDecoder *decoder;       // avifデコーダーへのポインタ
    avifRGBImage rgb;           // avifデコードで作成したいRGBの形式を指定する変数
    avifResult result;          // avif関数のリターンコード

    uint32_t width;             // 画像の幅
    uint32_t height;            // 画像の高さ
    avifRGBFormat rgb_format;   // RGB画像のフォーマットタイプ
    uint32_t pixel_bytes;       // 1ピクセルのバイトサイズ

    uint8_t *decodedData;       // デコードしたデータへのポインタ

    // データ格納時に使う変数
    int yy, xx, yd3, yd2;
    int rr, gg, bb;
    int buffindex;
    int buffpos;
    int linesize;
    WORD *buffptr;

    if (gLoadBuffer == NULL) {
        LOGD("LoadImageAvif : [error] gLoadBuffer is null");
        returnCode = -1;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // avifのデコーダ
    decoder = avifDecoderCreate();
    if (decoder == NULL) {
        LOGD("LoadImageAvif : [error] Memory allocation failure");
        returnCode = -2;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // バッファを取り込む
    result = avifDecoderSetIOMemory(decoder, (uint8_t *)gLoadBuffer, gLoadFileSize);
    if (result != AVIF_RESULT_OK) {
        LOGD("LoadImageAvif : [error] Cannot set IO on avifDecoder");
        returnCode = -3;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // 画像情報を取得する
    result = avifDecoderParse(decoder);
    if (result != AVIF_RESULT_OK) {
        LOGD("LoadImageAvif : [error] Failed to decode image: %s", avifResultToString(result));
        returnCode = -4;
        goto cleanup;
    }

#ifdef DEBUG
    LOGD("Parsed AVIF: %ux%u (%ubpc)\n", decoder->image->width, decoder->image->height, decoder->image->depth);
#endif

    width  = decoder->image->width;
    height = decoder->image->height;

    ///////////////////////////////////////////////
    // avifは複数の画像を持つことができるので、最初の画像を取得する
    result = avifDecoderNextImage(decoder);
    if (result != AVIF_RESULT_OK) {
        LOGD("LoadImageAvif : [error] Failed to decode image: %s", avifResultToString(result));
        returnCode = -5;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // image の width、height、depth を rgb にコピーする
    // その他の変数の値は適当に初期化される
    memset(&rgb, 0, sizeof(rgb));
    avifRGBImageSetDefaults(&rgb, decoder->image);

    ///////////////////////////////////////////////
    // デコードで取得したい画像形式を指定する
    rgb.format = AVIF_RGB_FORMAT_RGB;
    rgb.depth = 8;
    rgb.maxThreads = gMaxThreadNum;

    ///////////////////////////////////////////////
    // 自動でメモリを確保してくれる
    // 以下のコードを実行するのと同じ。
    // rgb.rowBytes = rgb.width * avifRGBImagePixelSize(&rgb);
    // rgb.pixels = (uint8_t *)malloc(rgb.rowBytes * rgb.height);
    // メモリ開放は avifRGBImageFreePixels(&rgb) すること
    result = avifRGBImageAllocatePixels(&rgb);
    if (result != AVIF_RESULT_OK) {
        LOGD("LoadImageAvif : [error] Pixels Memory allocation failure: %s", avifResultToString(result));
        returnCode = -6;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // YUVから指定したRGB形式に変換して結果をrgb.pixelsに入れる
    result = avifImageYUVToRGB(decoder->image, &rgb);
    if (result != AVIF_RESULT_OK) {
        LOGD("LoadImageAvif : [error] Conversion from YUV failed: %s", avifResultToString(result));
        returnCode = -7;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // 自分で指定したのでエラーは出ない
    rgb_format = rgb.format;
    if (rgb_format != AVIF_RGB_FORMAT_RGB) {
        LOGE("LoadImageAvif : [error] avif color_format error");
        returnCode = -8;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    //画像ファイルのピクセルデータ
    pixel_bytes = avifRGBImagePixelSize(&rgb);
    decodedData = &rgb.pixels[0];

    buffindex = -1;
    buffpos = 0;
    linesize = (int)(width + HOKAN_DOTS);
    buffptr = NULL;

    // 画像バッファにデータを格納する処理
    for(yy = 0; yy < height; yy++)
    {
        // キャンセルされたら終了する
        if (gCancel) {
            LOGD("LoadImageAvif : cancel.");
            ReleaseBuff(page, -1, -1);
            returnCode = -9;
            goto cleanup;
        }

        // ライン毎のバッファの位置を保存
        if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
            for (buffindex++; buffindex < gBuffNum ; buffindex++) {
                if (gBuffMng[buffindex].Page == -1) {
                    break;
                }
            }
            if (buffindex >= gBuffNum) {
                // 領域不足
                returnCode = -10;
                goto cleanup;
            }
            buffpos = 0;
            gBuffMng[buffindex].Page = page;
            gBuffMng[buffindex].Type = 0;
            gBuffMng[buffindex].Half = 0;
            gBuffMng[buffindex].Size = 0;
            gBuffMng[buffindex].Index = 0;
        }

        buffptr = gBuffMng[buffindex].Buff + buffpos + HOKAN_DOTS / 2;

        // データセット
        yd3 = gDitherY_3bit[yy & 0x07];
        yd2 = gDitherY_2bit[yy & 0x03];

        for (xx = 0 ; xx < width ; xx ++) {
            rr = *decodedData; decodedData++;
            gg = *decodedData; decodedData++;
            bb = *decodedData; decodedData++;
            if (pixel_bytes == 4) {     // 3固定なので実行しない
                //alpha はスキップ
                decodedData++;
            }

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
            buffptr[xx] = MAKE565(rr, gg, bb);
        }

        // 補完用の余裕
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[width + 0] = buffptr[width - 1];
        buffptr[width + 1] = buffptr[width - 1];

        // go to next line
        buffpos += linesize;
        gBuffMng[buffindex].Size += linesize;
    }

    pData->UseFlag = 1;
    pData->OrgWidth = width;
    pData->OrgHeight = height;

cleanup:
    avifRGBImageFreePixels(&rgb); //  avifRGBImageAllocatePixels() で確保したメモリを開放する
    avifDecoderDestroy(decoder);
    return returnCode;
}
