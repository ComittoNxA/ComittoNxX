#include <cinttypes>
#include <vector>
#include <android/log.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <png.h>
#include "Image.h"

extern char	*gLoadBuffer;
extern long	gLoadFileSize;
extern int gLoadError;

extern BUFFMNG *gBuffMng;
extern long gBuffNum;

extern int gCancel;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

int LoadImagePng(IMAGEDATA *pData, int page, int scale)
{
    int ret = 0;
    uint32_t width;
    uint32_t height;
    int color_type;
    uint32_t image_bytes;
    uint32_t row_bytes;

    png_structp pPng = NULL;
    png_infop pInfo = NULL;
    uint8_t** ppRowImage;

    typedef struct {
        uint8_t* data_ptr;
        uint32_t len;
        uint32_t offset;
    } png_buffer;

    int buffindex;
    int buffpos;
    int linesize;
    WORD *buffptr;

    if(png_sig_cmp((png_bytep)gLoadBuffer, 0, 8)){
        LOGE("LoadImagePng : png_sig_cmp error");
        return -1;
    }

    auto error_fn = [](png_struct*, png_const_charp msg) {
        LOGE("LoadImagePng : errorFn[%s]", msg);
        abort();
    };
    auto warn_fn = [](png_struct*, png_const_charp msg) {
        LOGE("LoadImagePng : warnFn[%s]", msg);
    };

    pPng = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, error_fn, warn_fn);
    if (!pPng) {
        LOGE("LoadImagePng : png_create_read_struct == NULL.");
        return -6;
    }

    pInfo = png_create_info_struct(pPng);
    if (!pInfo) {
        LOGE("LoadImagePng : png_create_info_struct == NULL.");
        png_destroy_read_struct(&pPng, NULL, NULL);
        return -7;
    }

    png_buffer buffer = {(uint8_t*)gLoadBuffer, 0, (uint32_t)gLoadFileSize};
    auto read_fn = [](png_struct* p, png_byte* data, png_size_t length) {
        auto* r = (png_buffer*) png_get_io_ptr(p);
        uint32_t next = std::min(r->offset, (uint32_t) length);
        if (next > 0) {
            memcpy(data, r->data_ptr + r->len, next);
            r->len += next;
            r->offset -= next;
        }
    };

    png_set_read_fn(pPng, &buffer, read_fn);
    png_set_expand(pPng);

	png_read_info(pPng, pInfo);
	color_type = png_get_color_type(pPng, pInfo);
    if (color_type == PNG_COLOR_TYPE_GRAY || color_type == PNG_COLOR_TYPE_GRAY_ALPHA) {
        png_set_gray_to_rgb(pPng);
    }
    if (png_get_bit_depth(pPng, pInfo) > 8) {
        png_set_strip_16(pPng);
    }
    if (color_type & PNG_COLOR_MASK_ALPHA) {
        png_set_strip_alpha(pPng);
    }
    if (color_type & PNG_COLOR_MASK_PALETTE) {
        png_set_palette_to_rgb(pPng);
    }

    png_read_update_info(pPng, pInfo);

    if (png_get_color_type(pPng, pInfo) != PNG_COLOR_TYPE_RGB) {
        LOGE("LoadImagePng : png color_type error");
        png_destroy_read_struct(&pPng, &pInfo, NULL);
        return -1;
    }
    width = png_get_image_width(pPng, pInfo);
    height = png_get_image_height(pPng, pInfo);
    row_bytes = png_get_rowbytes(pPng, pInfo);
    image_bytes = height * row_bytes;

    buffindex = -1;
    buffpos   = 0;
    linesize  = (width + HOKAN_DOTS);
    buffptr   = NULL;

    //画像データ用バッファーの確保
    ppRowImage = (uint8_t**)malloc(sizeof(uint8_t*) * height + image_bytes);
    if (!ppRowImage) {
        png_destroy_read_struct(&pPng, &pInfo, NULL);
        return -8;
    }
    //画像データ用バッファーの初期化
    {
        uint8_t* pRowImage;
        pRowImage = (uint8_t*)&(ppRowImage[height]);
        for(int yy = 0; yy < height; yy++)
        {
            ppRowImage[yy] = pRowImage;
            pRowImage += row_bytes;
        }
    }

    //画像ファイル読み込み
    png_read_image(pPng, ppRowImage);

    for(int yy = 0; yy < height; yy++)
    {
        if (gCancel) {
            LOGD("LoadImagePng : cancel.");
            ReleaseBuff(page, -1, -1);
            ret = -9;
            break;
        }

        // ライン毎のバッファの位置を保存
        if (buffindex < 0 || BLOCKSIZE - buffpos < linesize) {
            for (buffindex ++ ; buffindex < gBuffNum ; buffindex ++) {
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
//      LOGD("DEBUG2:yy=%d, idx=%d, pos=%d", yy, buffindex, buffpos);

        uint8_t* pImagePixel;
        pImagePixel = (uint8_t*)(ppRowImage[yy]);		//画像ファイルの行データ

        int yd3 = gDitherY_3bit[yy & 0x07];
        int yd2 = gDitherY_2bit[yy & 0x03];

        for (int xx = 0 ; xx < width ; xx ++) {
            int rr = pImagePixel[xx * 3];
            int gg = pImagePixel[xx * 3 + 1];
            int bb = pImagePixel[xx * 3 + 2];

//          LOGD("RGB : rr=%02x, gg=%02x, bb=%02x", (int)pImagePixel[xx * 4], (int)pImagePixel[xx * 4 + 1], (int)pImagePixel[xx * 4 + 2]);

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
//          buffptr[xx] = MAKE565(pImagePixel[xx * 3], pImagePixel[xx * 3 + 1], pImagePixel[xx * 3 + 2]);
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

    png_destroy_read_struct(&pPng, &pInfo, NULL);

    return ret;
}
