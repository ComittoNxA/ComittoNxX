#include <malloc.h>
#include <android/log.h>
#include <src/webp/decode.h>

#include "Image.h"

extern char* gLoadBuffer;
extern long gLoadFileSize;

//webpの読み込み
//参考：https://daeudaeu.com/libwebp/
int LoadImageWebp(int loadCommand, IMAGEDATA* pData, int page, int scale, WORD *canvas)
{
    int ret = 0;
    uint8_t* decodedData; //入力デコードデータ
    int width = 0; //デコードデータの幅
    int height = 0; //デコードデータの高さ
    int ch = 3; //RGB
    colorFormat colorFormat;    // SetBuffに渡すカラーフォーマット

    if (gLoadBuffer == NULL) {
        LOGE("LoadImageWebp : [error] gLoadBuffer is null\n");
        return -1;
    }

    //alphaを確認するかどうか
    int checkAlpha = false; //alphaはチェックしない
    if(checkAlpha){
        VP8StatusCode ret_; //webp関数の戻り値格納
        WebPBitstreamFeatures features; //入力ファイルの情報
        //データの情報取得
        ret_ = WebPGetFeatures((uint8_t*)gLoadBuffer, gLoadFileSize, &features);
        if (ret_ != VP8_STATUS_OK) {
            LOGE("LoadImageWebp : [error] WebPGetFeatures error\n");
            return -1;
        }
        width = features.width;
        height = features.height;
        if(features.has_alpha != 0){
            ch = 4; //RGBA
        }
    }

    //decode
    if (ch == 3) {
        //ch=3：RGB
        //width,heightは自動格納される
        decodedData = WebPDecodeRGB((uint8_t*)gLoadBuffer, gLoadFileSize, &width, &height);
        colorFormat = COLOR_FORMAT_RGB;
    }
    else {
        //ch=4：RGBA
        //width,heightは自動格納される
        decodedData = WebPDecodeRGBA((uint8_t*)gLoadBuffer, gLoadFileSize, &width, &height);
        colorFormat = COLOR_FORMAT_RGBA;

    }

    ret = SetBuff(page, width, height, decodedData, colorFormat);

    pData->UseFlag = 1;
    pData->OrgWidth = width;
    pData->OrgHeight = height;

    //メモリ解放
    WebPFree(decodedData);
    return ret;
}
