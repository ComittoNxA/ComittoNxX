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
    uint8_t *buffer = NULL;
    uint8_t *row;
    int yy;

    typedef struct {
        uint8_t* data_ptr;
        uint32_t len;
        uint32_t offset;
    } png_buffer;

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

    png_buffer pBuffer = {(uint8_t*)gLoadBuffer, 0, (uint32_t)gLoadFileSize};
    auto read_fn = [](png_struct* p, png_byte* data, png_size_t length) {
        auto* r = (png_buffer*) png_get_io_ptr(p);
        uint32_t next = std::min(r->offset, (uint32_t) length);
        if (next > 0) {
            memcpy(data, r->data_ptr + r->len, next);
            r->len += next;
            r->offset -= next;
        }
    };

    png_set_read_fn(pPng, &pBuffer, read_fn);
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

    buffer = (uint8_t *)malloc(image_bytes);
    if (buffer == NULL) {
        LOGD("LoadImageJpeg : MAlloc Error. size=%d", (int)(image_bytes));
        return -7;
    }
    row = buffer;

    for(yy = 0; yy < height; yy++) {
        //１ラインづつ読み込んでいく。
        png_read_row(pPng, row, NULL);
        row += row_bytes;
    }

    //読み込み終了処理。
    png_read_end(pPng,NULL); //イメージデータの後ろにあるチャンクをスキップ。

    ret = SetBuff(page, width, height, (uint8_t *)buffer, COLOR_FORMAT_RGB);

    pData->UseFlag = 1;
    pData->OrgWidth = width;
    pData->OrgHeight = height;

    png_destroy_read_struct(&pPng, &pInfo, NULL);

    return ret;
}
