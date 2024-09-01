#include <android/log.h>
#include <avif/avif.h>
#include <cstring>

#include "Image.h"

//#define DEBUG

extern char	*gLoadBuffer;
extern long	gLoadFileSize;
extern int gMaxThreadNum;

// 画像をBitmapに変換してバッファに入れる
int LoadImageAvif(int loadCommand, IMAGEDATA *pData, int page, int scale, WORD *canvas)
{

    int returnCode = 0;         // この関数のリターンコード
    uint32_t width;             // 画像の幅
    uint32_t height;            // 画像の高さ

    avifDecoder *decoder;       // avifデコーダーへのポインタ
    avifRGBImage rgb;           // avifデコードで作成したいRGBの形式を指定する変数
    avifResult result;          // avif関数のリターンコード
#ifdef DEBUG
    LOGD("LoadImageAvif: Start. loadCommand=%d, page=%d, scale=%d", loadCommand, page, scale);
#endif
    if (gLoadBuffer == NULL) {
        LOGE("LoadImageAvif: gLoadBuffer is null");
        returnCode = -1;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // avifのデコーダ
    decoder = avifDecoderCreate();
    if (decoder == NULL) {
        LOGE("LoadImageAvif: avifDecoderCreate() failed.");
        returnCode = -2;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // バッファを取り込む
    result = avifDecoderSetIOMemory(decoder, (uint8_t *)gLoadBuffer, gLoadFileSize);
    if (result != AVIF_RESULT_OK) {
        LOGE("LoadImageAvif: avifDecoderSetIOMemory() failed. %s", avifResultToString(result));
        returnCode = -3;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // 画像情報を取得する
    result = avifDecoderParse(decoder);
    if (result != AVIF_RESULT_OK) {
        LOGE("LoadImageAvif: avifDecoderParse() failed. %s", avifResultToString(result));
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
        LOGE("LoadImageAvif: avifDecoderNextImage() failed. %s", avifResultToString(result));
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
        LOGE("LoadImageAvif: avifRGBImageAllocatePixels() failed. %s", avifResultToString(result));
        returnCode = -6;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // YUVから指定したRGB形式に変換して結果をrgb.pixelsに入れる
    result = avifImageYUVToRGB(decoder->image, &rgb);
    if (result != AVIF_RESULT_OK) {
        LOGE("LoadImageAvif: avifImageYUVToRGB() failed. %s", avifResultToString(result));
        returnCode = -7;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // 自分で指定したのでエラーは出ない
    if (rgb.format != AVIF_RGB_FORMAT_RGB) {
        LOGE("LoadImageAvif: avif color_format error");
        returnCode = -8;
        goto cleanup;
    }

    if (loadCommand == SET_BUFFER) {
        LOGD("LoadImageAvif: SetBuff() Start. page=%d, width=%d, height=%d", page, width, height);
        returnCode = SetBuff(page, width, height, rgb.pixels, COLOR_FORMAT_RGB);
        if (returnCode < 0) {
            LOGE("LoadImageAvif: SetBuff() failed. return=%d", returnCode);
            goto cleanup;
        }
        pData->UseFlag = 1;
        pData->OrgWidth = width;
        pData->OrgHeight = height;
    }
    else if (loadCommand == SET_BITMAP) {
#ifdef DEBUG
        LOGD("LoadImageAvif: SetBitmap() Start. page=%d, width=%d, height=%d", page, width, height);
#endif
        returnCode = SetBitmap(page, width, height, rgb.pixels, COLOR_FORMAT_RGB, canvas);
        if (returnCode < 0) {
            LOGE("LoadImageAvif: SetBitmap() failed. return=%d", returnCode);
            goto cleanup;
        }
    }

cleanup:
    avifRGBImageFreePixels(&rgb); //  avifRGBImageAllocatePixels() で確保したメモリを開放する
    avifDecoderDestroy(decoder);
#ifdef DEBUG
    LOGD("LoadImageAvif: End. return=%d", returnCode);
#endif
    return returnCode;
}

// 画像の幅と高さを返す
int ImageGetSizeAvif(int type, jint *width, jint *height)
{

    int returnCode = 0;         // この関数のリターンコード

    avifDecoder *decoder;       // avifデコーダーへのポインタ
    avifResult result;          // avif関数のリターンコード
#ifdef DEBUG
    LOGD("ImageGetSizeAvif: Start.");
#endif
    if (gLoadBuffer == NULL) {
        LOGE("ImageGetSizeAvif: [error] gLoadBuffer is null");
        returnCode = -1;
        goto cleanup;
    }
    if (width == NULL) {
        LOGE("ImageGetSizeAvif: [error] width is null");
        returnCode = -1;
        goto cleanup;
    }
    if (height == NULL) {
        LOGE("ImageGetSizeAvif: [error] height is null");
        returnCode = -1;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // avifのデコーダ
    decoder = avifDecoderCreate();
    if (decoder == NULL) {
        LOGE("ImageGetSizeAvif: [error] Memory allocation failure");
        returnCode = -2;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // バッファを取り込む
    result = avifDecoderSetIOMemory(decoder, (uint8_t *)gLoadBuffer, gLoadFileSize);
    if (result != AVIF_RESULT_OK) {
        LOGE("ImageGetSizeAvif: [error] Cannot set IO on avifDecoder");
        returnCode = -3;
        goto cleanup;
    }

    ///////////////////////////////////////////////
    // 画像情報を取得する
    result = avifDecoderParse(decoder);
    if (result != AVIF_RESULT_OK) {
        LOGE("ImageGetSizeAvif: [error] Failed to decode image: %s", avifResultToString(result));
        returnCode = -4;
        goto cleanup;
    }

    *width  = decoder->image->width;
    *height = decoder->image->height;

cleanup:
    avifDecoderDestroy(decoder);
#ifdef DEBUG
    LOGD("LoadImageAvif: End. return=%d, width=%d, height=%d", returnCode, *width, *height);
#endif
    return returnCode;
}
