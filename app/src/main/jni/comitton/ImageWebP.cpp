#include <malloc.h>
#include <android/log.h>
#include <src/webp/decode.h>

#include "Image.h"

extern char* gLoadBuffer;
extern long gLoadFileSize;
extern int gLoadError;

extern BUFFMNG* gBuffMng;
extern long gBuffNum;

extern int gCancel;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

extern "C"
{
//#define XMD_H
}

//webpの読み込み
//参考：https://daeudaeu.com/libwebp/
int LoadImageWebp(IMAGEDATA* pData, int page, int scale)
{
    uint8_t* decodedData; //入力デコードデータ
    int width = 0; //デコードデータの幅
    int height = 0; //デコードデータの高さ
    int ch = 3; //RGB

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
    }
    else {
        //ch=4：RGBA
        //width,heightは自動格納される
        decodedData = WebPDecodeRGBA((uint8_t*)gLoadBuffer, gLoadFileSize, &width, &height);
    }

    int buffindex = -1;
    int buffpos = 0;
    int linesize = (width + HOKAN_DOTS);
    int ret = 0;
    WORD* buffptr = NULL;

    uint8_t *ddata;
    ddata = &decodedData[0];
    //LOGI("LoadImageWebp : Height -> %d", height);
    for (int yy = 0; yy < height; yy++)
    {
        if (gCancel) {
            LOGD("LoadImageWebp : cancel.");
            ReleaseBuff(page, -1, -1);
            ret = -9;
            break;
        }

        // ライン毎のバッファの位置を保存
        if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
            for (buffindex++; buffindex < gBuffNum; buffindex++) {
                if (gBuffMng[buffindex].Page == -1) {
                    break;
                }
            }
            if (buffindex >= gBuffNum) {
                // 領域不足
                ret = -10;
                break;
            }
            buffpos = 0;
            gBuffMng[buffindex].Page = page;
            gBuffMng[buffindex].Type = 0;
            gBuffMng[buffindex].Half = 0;
            gBuffMng[buffindex].Size = 0;
            gBuffMng[buffindex].Index = 0;
        }

        buffptr = gBuffMng[buffindex].Buff + buffpos + HOKAN_DOTS / 2;
        //LOGD("DEBUG2:yy=%d, idx=%d, pos=%d", yy, buffindex, buffpos);

        // データセット
        int yd3 = gDitherY_3bit[yy & 0x07];
        int yd2 = gDitherY_2bit[yy & 0x03];
        int rr, gg, bb;

        for (int xx = 0; xx < width; xx++) {
            //alpha はスキップ
            rr = *ddata; ddata++;
            gg = *ddata; ddata++;
            bb = *ddata; ddata++;
            if(ch == 4) ddata++;

            //LOGI("RGB : rr=%d, gg=%d, bb=%d \n", rr, gg, bb);
            //LOGI("RGB : rr=%02x, gg=%02x, bb=%02x \n", (int)decodedData[(yy * width * ch) + (ch * xx)], decodedData[(yy * width * ch) + (ch * xx)], (int)decodedData[(yy * width * ch) + (ch * xx)]);

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

    //メモリ解放
    WebPFree(decodedData);
    return ret;
}
