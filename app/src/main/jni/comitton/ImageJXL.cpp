#include <android/log.h>
#include <cstring>
#include <vector>

#include <jxl/decode.h>
#include <jxl/decode_cxx.h>
#include <jxl/resizable_parallel_runner.h>
#include <jxl/resizable_parallel_runner_cxx.h>

#include "Image.h"

//#define DEBUG

extern char	*gLoadBuffer[];
extern long	gLoadFileSize[];
extern int gMaxThreadNum;

// 画像をBitmapに変換してバッファに入れる
int LoadImageJxl(int index, int loadCommand, IMAGEDATA *pData, int page, int scale, WORD *canvas)
{
    int returnCode = 0;         // この関数のリターンコード
    uint32_t width;             // 画像の幅
    uint32_t height;            // 画像の高さ
    uint8_t *buffer = nullptr;

    JxlDecoderStatus status;
    JxlBasicInfo jxl_info;
    JxlPixelFormat format = {3, JXL_TYPE_UINT8, JXL_LITTLE_ENDIAN, 0};

#ifdef DEBUG
    LOGD("LoadImageJxl: Start.");
#endif

    auto runner = JxlResizableParallelRunnerMake(nullptr);      // マルチスレッド実行用
    auto decoder = JxlDecoderMake(nullptr);

    if (gLoadBuffer[index] == nullptr) {
        LOGE("LoadImageJxl: [error] gLoadBuffer is null");
        returnCode = ERROR_CODE_CACHE_NOT_INITIALIZED;
        goto cleanup;
    }

    if (JXL_DEC_SUCCESS !=
        JxlDecoderSubscribeEvents(decoder.get(), JXL_DEC_BASIC_INFO |
                                                 JXL_DEC_COLOR_ENCODING |
                                                 JXL_DEC_FULL_IMAGE)) {
        LOGE("LoadImageJxl: JxlDecoderSubscribeEvents failed");
        returnCode = -2;
        goto cleanup;
    }

    if (JXL_DEC_SUCCESS != JxlDecoderSetParallelRunner(decoder.get(),
                                                       JxlResizableParallelRunner,
                                                       runner.get())) {
        LOGE("LoadImageJxl: JxlDecoderSetParallelRunner failed");
        returnCode = -3;
        goto cleanup;
    }

    JxlDecoderSetInput(decoder.get(), (uint8_t *)gLoadBuffer[index], gLoadFileSize[index]);
    JxlDecoderCloseInput(decoder.get());
    for (;;) {

        status = JxlDecoderProcessInput(decoder.get());

        if (status == JXL_DEC_ERROR) {
            LOGE("LoadImageJxl: Decoder error");
            returnCode = -4;
            goto cleanup;
        } else if (status == JXL_DEC_NEED_MORE_INPUT) {
            LOGE("LoadImageJxl: Error, already provided all input");
            returnCode = -5;
            goto cleanup;
        } else if (status == JXL_DEC_BASIC_INFO) {
            if (JXL_DEC_SUCCESS != JxlDecoderGetBasicInfo(decoder.get(), &jxl_info)) {
                LOGE("LoadImageJxl: JxlDecoderGetBasicInfo failed");
                returnCode = -6;
                goto cleanup;
            }
            width  = jxl_info.xsize;
            height = jxl_info.ysize;
            JxlResizableParallelRunnerSetThreads(
                    runner.get(), JxlResizableParallelRunnerSuggestThreads(
                            jxl_info.xsize, jxl_info.ysize));
        } else if (status == JXL_DEC_NEED_IMAGE_OUT_BUFFER) {
            size_t buffer_size;
            if (JXL_DEC_SUCCESS !=
                JxlDecoderImageOutBufferSize(decoder.get(), &format, &buffer_size)) {
                LOGE("LoadImageJxl: JxlDecoderImageOutBufferSize failed");
                returnCode = -7;
                goto cleanup;
            }
#ifdef DEBUG
            LOGD("LoadImageJxl: buffer_size=%d", buffer_size);
#endif
            buffer = (uint8_t *)malloc(buffer_size);
            if (buffer == (unsigned char *) nullptr)
            {
                returnCode = ERROR_CODE_MALLOC_FAILURE;
                goto cleanup;
            }

            if (JXL_DEC_SUCCESS != JxlDecoderSetImageOutBuffer(decoder.get(), &format,
                                                               buffer,
                                                               buffer_size)) {
                LOGE("LoadImageJxl: JxlDecoderSetImageOutBuffer failed");
                returnCode = -9;
                goto cleanup;
            }
#ifdef DEBUG
            LOGD("LoadImageJxl: JxlDecoderSetImageOutBuffer succeed");
#endif
        } else if (status == JXL_DEC_FULL_IMAGE) {
#ifdef DEBUG
            LOGD("LoadImageJxl: status == JXL_DEC_FULL_IMAGE");
#endif
            if (buffer == (unsigned char *) nullptr)
            {
                LOGE("LoadImageJxl: UnableToReadImageData");
                returnCode = -10;
                goto cleanup;
            }

            if (loadCommand == SET_BUFFER) {
#ifdef DEBUG
                LOGD("LoadImageJxl: SetBuff() Start. page=%d, width=%d, height=%d", page, width, height);
#endif
                returnCode = SetBuff(index, page, width, height, buffer, COLOR_FORMAT_RGB);
                if (returnCode < 0) {
                    LOGE("LoadImageJxl: SetBuff() failed. return=%d", returnCode);
                    returnCode = -10;
                    goto cleanup;
                }
                pData->UseFlag = 1;
                pData->OrgWidth = width;
                pData->OrgHeight = height;
            }
            else if (loadCommand == SET_BITMAP) {
#ifdef DEBUG
                LOGD("LoadImageJxl: SetBitmap() Start. page=%d, width=%d, height=%d", page, width, height);
#endif
                returnCode = SetBitmap(index, page, width, height, buffer, COLOR_FORMAT_RGB, canvas);
                if (returnCode < 0) {
                    LOGE("LoadImageJxl: SetBitmap() failed. return=%d", returnCode);
                    goto cleanup;
                }
            }
            break;
        }
    }

cleanup:
    // cleanup
    JxlDecoderReleaseInput(decoder.get());
    if (buffer == (unsigned char *) nullptr)
    {
        free(buffer);
    }

#ifdef DEBUG
    LOGD("LoadImageJxl: End. return=%d, width=%d, height=%d", returnCode, width, height);
#endif
    return returnCode;
}

// 画像の幅と高さを返す
int ImageGetSizeJxl(int index, int type, jint *width, jint *height)
{
    int returnCode = 0;         // この関数のリターンコード

    JxlDecoderStatus status;
    JxlBasicInfo jxl_info;

#ifdef DEBUG
    LOGD("ImageGetSizeJxl: Start.");
#endif

    auto decoder = JxlDecoderMake(nullptr);

    if (gLoadBuffer[index] == nullptr) {
        LOGE("ImageGetSizeJxl: [error] gLoadBuffer is null");
        returnCode = ERROR_CODE_CACHE_NOT_INITIALIZED;
        goto cleanup;
    }
    if (width == nullptr) {
        LOGE("ImageGetSizeJxl: [error] width is null");
        returnCode = -1;
        goto cleanup;
    }
    if (height == nullptr) {
        LOGE("ImageGetSizeJxl: [error] height is null");
        returnCode = -1;
        goto cleanup;
    }

    if (JXL_DEC_SUCCESS !=
        JxlDecoderSubscribeEvents(decoder.get(), JXL_DEC_BASIC_INFO |
                                             JXL_DEC_COLOR_ENCODING |
                                             JXL_DEC_FULL_IMAGE)) {
        LOGE("ImageGetSizeJxl: JxlDecoderSubscribeEvents failed");
        returnCode = -2;
        goto cleanup;
    }

    JxlDecoderSetInput(decoder.get(), (uint8_t *)gLoadBuffer[index], gLoadFileSize[index]);
    JxlDecoderCloseInput(decoder.get());
    status = JxlDecoderProcessInput(decoder.get());

    if (status == JXL_DEC_ERROR) {
        LOGE("ImageGetSizeJxl: Decoder error");
        returnCode = -4;
        goto cleanup;
    } else if (status == JXL_DEC_NEED_MORE_INPUT) {
        LOGE("ImageGetSizeJxl: Error, already provided all input");
        returnCode = -5;
        goto cleanup;
    } else if (status == JXL_DEC_BASIC_INFO) {
        if (JXL_DEC_SUCCESS != JxlDecoderGetBasicInfo(decoder.get(), &jxl_info)) {
            LOGE("ImageGetSizeJxl: JxlDecoderGetBasicInfo failed");
            returnCode = -5;
            goto cleanup;
        }
        *width  = jxl_info.xsize;
        *height = jxl_info.ysize;
    }

cleanup:
    // cleanup
#ifdef DEBUG
    LOGD("ImageGetSizeJxl: End. return=%d, width=%d, height=%d", returnCode, *width, *height);
#endif
    return returnCode;
}
