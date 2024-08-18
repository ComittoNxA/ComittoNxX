#include <android/log.h>
#include <avif/avif.h>
#include <cstring>

#include "Image.h"

extern char	*gLoadBuffer;
extern long	gLoadFileSize;
extern int gMaxThreadNum;

// avifの読み込み
int LoadImageAvif(IMAGEDATA *pData, int page, int scale)
{

    int returnCode = 0;         // この関数のリターンコード
    uint32_t width;             // 画像の幅
    uint32_t height;            // 画像の高さ

    avifDecoder *decoder;       // avifデコーダーへのポインタ
    avifRGBImage rgb;           // avifデコードで作成したいRGBの形式を指定する変数
    avifResult result;          // avif関数のリターンコード

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
    if (rgb.format != AVIF_RGB_FORMAT_RGB) {
        LOGE("LoadImageAvif : [error] avif color_format error");
        returnCode = -8;
        goto cleanup;
    }

    SetBuff(page, width, height, rgb.pixels, COLOR_FORMAT_RGB);

    pData->UseFlag = 1;
    pData->OrgWidth = width;
    pData->OrgHeight = height;

cleanup:
    avifRGBImageFreePixels(&rgb); //  avifRGBImageAllocatePixels() で確保したメモリを開放する
    avifDecoderDestroy(decoder);
    return returnCode;
}
