#include <cinttypes>
#include <android/log.h>
#include <cstdio>
#include <cstdlib>
#include <jpeglib.h>
#include "Image.h"

extern char			*gLoadBuffer;
extern long			gLoadFileSize;
extern int          gCancel;

int LoadImageJpeg(IMAGEDATA *pData, int page, int scale)
{

    uint32_t width;             // 画像の幅
    uint32_t height;            // 画像の高さ
    int components;             // 1ピクセルのバイト数
    colorFormat colorFormat;    // SetBuffに渡すカラーフォーマット

    jpeg_decompress_struct in_info{};
    jpeg_error_mgr jpeg_error{};
    int row_bytes;
    JSAMPROW buffer = NULL;
    JSAMPROW row;
    int yy;
    int ret = 0;

    if (gLoadBuffer == NULL) {
		LOGD("LoadImageJpeg : gLoadBuffer is null");
		return -1;
	}

	in_info.err = jpeg_std_error(&jpeg_error); //エラーハンドラ設定
	jpeg_error.error_exit = [](j_common_ptr in_info) {
		char pszMessage[JMSG_LENGTH_MAX];
		(*in_info->err->format_message)(in_info, pszMessage);
		if (!gCancel) {
			LOGE("LoadImageJpeg :JpegError[%s], abort()", pszMessage);
			abort();
		}
	};

	jpeg_create_decompress(&in_info);
	jpeg_mem_src(&in_info, (uint8_t*)gLoadBuffer, gLoadFileSize); //読込ファイル設定
	jpeg_read_header(&in_info,true); //ヘッダー読込
	in_info.out_color_space = JCS_RGB;
	in_info.dither_mode = JDITHER_NONE;

	// 倍率
	in_info.scale_num = 1;
	in_info.scale_denom = 1;
	if (scale % 8 == 0) {
		in_info.scale_denom = 8;
		scale /= 8;
	}
	else if (scale % 4 == 0) {
		in_info.scale_denom = 4;
		scale /= 4;
	}
	else if (scale % 2 == 0) {
		in_info.scale_denom = 2;
		scale /= 2;
	}

	jpeg_start_decompress(&in_info); //デコードスタート

	width  = ROUNDUP_DIV(in_info.output_width, scale);
	height = ROUNDUP_DIV(in_info.output_height, scale);
    components = in_info.output_components;
    if (in_info.out_color_space == JCS_RGB) {
        colorFormat = COLOR_FORMAT_RGB;
    }
    else if (in_info.out_color_space == JCS_GRAYSCALE) {
        colorFormat = COLOR_FORMAT_GRAYSCALE;
    }

#ifdef DEBUG
	LOGD("LoadImageJpeg : scl=%d, sn=%d, sd=%d, w=%d, h=%d, sw=%d, sh=%d, comp=%d", scale, in_info.scale_num, in_info.scale_denom, in_info.output_width, in_info.output_height, width, height, components);
#endif

    //出力 1ラインのバイト数
    row_bytes = sizeof(JSAMPLE) * width * components;
    buffer = (JSAMPLE*)malloc(row_bytes * height);
	if (buffer == NULL) {
		LOGD("LoadImageJpeg : MAlloc Error. size=%d", (int)(row_bytes * height));
		return -7;
	}
    row = buffer;

    for(yy = 0; yy < height; yy++) {
        //Jpegを1ライン読み込む
        jpeg_read_scanlines(&in_info, &row, 1);
        row += row_bytes;
    }

    ret = SetBuff(page, width, height, (uint8_t *)buffer, colorFormat);

	if (ret == 0) {
		jpeg_finish_decompress(&in_info);		// 読み込み終了処理
        // イメージサイズを設定
        pData->UseFlag = 1;
        pData->OrgWidth = width;
        pData->OrgHeight = height;
	}
	jpeg_destroy_decompress(&in_info);
	free(buffer);
	return ret;
}
