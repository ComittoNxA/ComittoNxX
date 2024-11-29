#include <jni.h>
#include <malloc.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <csetjmp>
#include <cstring>
#include "Image.h"

//#define DEBUG

// イメージ管理
IMAGEDATA	*gImageData = NULL;
long		gTotalPages = 0;
long		gLoadBuffSize = 0;
char		*gLoadBuffer = NULL;
WORD		**gLinesPtr;
WORD		**gDsLinesPtr;
WORD		**gSclLinesPtr;	// 出力先ラインポインタ配列

long		gBitmapBuffPos;

long		gLoadFileSize;
long		gLoadFilePos;
long		gLoadPage;
int			gLoadError;
int			gCancel;
jmp_buf		gJmpBuff;
int			gMaxThreadNum = 1;

BUFFMNG		*gBuffMng = NULL;
long		gBuffNum = 0;

BUFFMNG		*gSclBuffMng = NULL;
long		gSclBuffNum = 0;

char gDitherX_3bit[8][8] = {{0, 0, 0, 0, 0, 0, 0, 0},
							{8, 0, 0, 0, 0, 0, 0, 0},
							{8, 0, 0, 0, 8, 0, 0, 0},
							{8, 0, 0, 8, 0, 0, 8, 0},
							{8, 0, 8, 0, 8, 0, 8, 0},
							{8, 8, 8, 0, 8, 0, 8, 0},
							{8, 8, 8, 0, 8, 8, 8, 0},
							{8, 8, 8, 8, 8, 8, 8, 0}};
char gDitherX_2bit[4][4] = {{0, 0, 0, 0},
							{4, 0, 0, 0},
							{4, 0, 4, 0},
							{4, 4, 4, 0}};
char gDitherY_3bit[8] = {0, 2, 4, 6, 0, 2, 4, 6};
char gDitherY_2bit[4] = {0, 2, 0, 2};

extern "C" {
// サムネイルの初期化
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailInitialize (JNIEnv *env, jclass obj, jlong id, jint pagesize, jint pagenum, jint imagenum)
{
//#define DEBUG_ThumbnailInitialize

#ifdef DEBUG_ThumbnailInitialize
	LOGD("callImage: ThumbnailInitialize : id=%lld, pagesize=%d, pagenum=%d, imagenum=%d", id, pagesize, pagenum, imagenum);
#endif

	int ret = ThumbnailAlloc(id, pagesize, pagenum, imagenum);
	return ret;
}

// サムネイルのNoImage設定
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailSetNone (JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailSetNone

#ifdef DEBUG_ThumbnailSetNone
	LOGD("callImage: ThumbnailSetNone : id=%lld, index=%d", id, index);
#endif

	int ret = ThumbnailSetNone(id, index);
	return ret;
}

// サムネイルの残り領域確認
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailCheck (JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailCheck

#ifdef DEBUG_ThumbnailCheck
	LOGD("callImage: ThumbnailCheck : id=%lld, index=%d", id, index);
#endif

	int ret = ThumbnailCheck(id, index);
	return ret;
}

// サムネイルの残り領域確認
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailMemorySizeCheck (JNIEnv *env, jclass obj, jlong id, jint width, jint height)
{
//#define DEBUG_ThumbnailMemorySizeCheck

#ifdef DEBUG_ThumbnailMemorySizeCheck
	LOGD("callImage: ThumbnailMemorySizeCheck : id=%lld, width=%d, height=%d", id, width, height);
#endif

	int ret = ThumbnailMemorySizeCheck(id, width, height);
	return ret;
}

// サムネイルを整理して容量確保
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailImageAlloc (JNIEnv *env, jclass obj, jlong id, jint blocks, jint index)
{
//#define DEBUG_ThumbnailImageAlloc

#ifdef DEBUG_ThumbnailImageAlloc
	LOGD("callImage: ThumbnailImageAlloc : id=%lld, blocks=%d, index=%d", id, blocks, index);
#endif

	int ret = ThumbnailImageAlloc(id, blocks, index);
	return ret;
}

// サムネイルが全て設定されているかをチェック
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailCheckAll (JNIEnv *env, jclass obj, jlong id)
{
//#define DEBUG_ThumbnailCheckAll

#ifdef DEBUG_ThumbnailCheckAll
	LOGD("callImage: ThumbnailCheckAll : id=%lld", id);
#endif

	int ret = ThumbnailCheckAll(id);
	return ret;
}

// サムネイルが全て設定されているかをチェック
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailSave (JNIEnv *env, jclass obj, jlong id, jobject bitmap, jint index)
{
//#define DEBUG_ThumbnailSave

#ifdef DEBUG_ThumbnailSave
	LOGD("callImage: ThumbnailSave : id=%lld, index=%d", id, index);
#endif
	if (bitmap == NULL) {
		return 0;
	}

    // ビットマップ情報取得
	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("callImage: ThumbnailSave : AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -2;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
		LOGE("callImage: ThumbnailSave: Bitmap format is not RGB_565 !");
		return -3;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("callImage: ThumbnailSave : AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -4;
	}

	ret = ThumbnailSave(id, index, info.width, info.height, info.stride, (BYTE*)canvas);

	AndroidBitmap_unlockPixels(env, bitmap);
	return ret;
}

// サムネイル描画
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailImageSize(JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailImageSize

#ifdef DEBUG_ThumbnailImageSize
    LOGD("callImage: ThumbnailImageSize: 開始します. id=%ld, index=%d", id, index);
#endif
    return ThumbnailImageSize(id, index);
}

// サムネイル描画
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailDraw(JNIEnv *env, jclass obj, jlong id, jobject bitmap, jint index)
{
//#define DEBUG_ThumbnailDraw

#ifdef DEBUG_ThumbnailDraw
	LOGD("callImage: ThumbnailDraw : id=%lld, index=%d", id, index);
#endif

	if (bitmap == NULL) {
		return 0;
	}

    // メモリ獲得
	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("callImage: ThumbnailDraw : AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -2;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
		LOGE("callImage: ThumbnailDraw : Bitmap format is not RGB_565 !");
		return -3;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("callImage: ThumbnailDraw : AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -4;
	}

	ret = ThumbnailDraw(id, index, info.width, info.height, info.stride, (BYTE*)canvas);

	AndroidBitmap_unlockPixels(env, bitmap);
	return ret;
}

// サムネイル解放
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ThumbnailFree(JNIEnv *env, jclass obj, jlong id)
{
//#define DEBUG_ThumbnailFree


#ifdef DEBUG_ThumbnailFree
	LOGD("callImage: ThumbnailFree : id=%lld", id);
#endif
//	if (gThumbnailId != id) {
//		// 初期化したIDと異なる
//		return -1;
//	}
	ThumbnailFree(id);
	return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageInitialize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageInitialize (JNIEnv *env, jclass obj, jlong loadsize, jint buffsize, jint totalpage, jint threadnum)
{
#ifdef DEBUG
	LOGD("callImage: ImageInitialize : buffsize=%d * 4, page=%d", buffsize, totalpage);
#endif

    // 読み込み用領域確保
	MemFree();

	gLoadBuffSize  = loadsize;
	gTotalPages  = totalpage;
	gCancel = 0;

	jint ret = 0;

	gLoadPage = -1;
	ret = MemAlloc(buffsize);

	if (threadnum > 0) {
		gMaxThreadNum = threadnum;
	}
	else {
		LOGE("callImage: ImageInitialize : Illegal Param.(%d)", threadnum);
	}
	return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageSetPage (JNIEnv *env, jclass obj, jint page, jlong size) {
#ifdef DEBUG
    LOGD("callImage: ImageSetSize : page=%d, size=%d", page, size);
#endif

    if (gLoadBuffSize < size) {
        // ロード領域不足
        return -2;
    }

    if (page < 0 || gTotalPages <= page) {
        // ページ番号不正
        return -3;
    }

    gLoadPage = page;
    gLoadFileSize = size;
    gLoadFilePos = 0;
    gImageData[page].SclFlag[0] = 0;
    gImageData[page].SclFlag[1] = 0;
    gImageData[page].SclFlag[2] = 0;
	return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetData
 * Signature: ([BI)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageSetData (JNIEnv *env, jclass obj, jbyteArray dataArray, jint size)
{
	jbyte *data = env->GetByteArrayElements(dataArray, NULL);

	if (gLoadFileSize - gLoadFilePos < size) {
        // セットしたサイズを超えないように
		size = gLoadFileSize - gLoadFilePos;
	}
	memcpy(&gLoadBuffer[gLoadFilePos], data, size);
	gLoadFilePos += size;

	env->ReleaseByteArrayElements(dataArray, data, 0);
//	LOGD("setdata end");
	return size;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetFileSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageSetFileSize (JNIEnv *env, jclass obj, jlong size)
{
#ifdef DEBUG
    LOGD("callImage: ImageSetSize : page=%d, size=%d", size);
#endif

    if (gLoadBuffSize < size) {
        // ロード領域不足
        return -2;
    }

    gLoadPage = -1;
    gLoadFileSize = size;
    gLoadFilePos = 0;
    return 0;
}

/*
 * Class:     src_comitton_stream_ImageGetSize
 * Method:    ImageGetSize
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageGetSize (JNIEnv *env, jclass obj, jint type, jintArray imagesize)
{
    int ret = 0;

#ifdef DEBUG
    LOGD("callImage: ImageGetSize : page=%d, filesize=%d, type=%d", (int)gLoadPage, (int)gLoadFilePos, type);
#endif
    jint *iSize = NULL;
    if (imagesize != NULL) {
        iSize = (jint*)env->GetIntArrayElements(imagesize, NULL);
    }

    if (setjmp(gJmpBuff) == 0) {
        CheckImageType(&type);

        if (type == IMAGETYPE_JPEG) {
#ifdef HAVE_LIBJPEG
            // なにもしない
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
        else if (type == IMAGETYPE_PNG){
#ifdef HAVE_LIBPNG
            // なにもしない
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
        else if (type == IMAGETYPE_GIF){
#ifdef HAVE_LIBGIF
            // なにもしない
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
        else if (type == IMAGETYPE_WEBP){
#ifdef HAVE_LIBWEBP
            // なにもしない
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
        else if (type == IMAGETYPE_AVIF){
#ifdef DEBUG
            LOGD("callImage: ImageGetSize : Call ImageGetSizeAvif.");
#endif
#ifdef HAVE_LIBAVIF
            ret = ImageGetSizeAvif(type, &iSize[0], &iSize[1]);
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
        else if (type == IMAGETYPE_HEIF){
#ifdef HAVE_LIBHEIF
            // なにもしない
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
        else if (type == IMAGETYPE_JXL){
#ifdef DEBUG
            LOGD("callImage: ImageGetSize : Call ImageGetSizeJxl.");
#endif
#ifdef HAVE_LIBJXL
            ret = ImageGetSizeJxl(type, &iSize[0], &iSize[1]);
#endif
            if (ret == 0 && gLoadError) {
                ret = -4;
            }
        }
    }
    else {
        ret = -1;
    }

    if (imagesize != NULL) {
        env->ReleaseIntArrayElements(imagesize, iSize, 0);
    }

    gLoadPage = -1;
#ifdef DEBUG
    LOGD("callImage: ImageGetSize: end - result=%d, width=%d, height=%d", ret, iSize[0], iSize[1]);
#endif
    return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetBitmap
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageSetBitmap (JNIEnv *env, jclass obj, jobject bitmap)
{
    AndroidBitmapInfo	info;
    void				*canvas;
    colorFormat colorFormat;

    if (gLoadPage < 0 || gTotalPages <= gLoadPage) {
        LOGE("callImage: ImageSetBitmap: Illegal Page.(%d)", (int)gLoadPage);
        return - 3;
    }
#ifdef DEBUG
    LOGD("callImage: ImageSetBitmap : page=%d, filesize=%d", (int)gLoadPage, (int)gLoadFilePos);
#endif

    int ret = 0;
    gLoadError = 0;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("callImage: ImageSetBitmap: AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -4;
    }

    if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        colorFormat = COLOR_FORMAT_RGB565;
    }
    else if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        colorFormat = COLOR_FORMAT_RGBA;
    }
    else {
        LOGE("callImage: ImageSetBitmap: Bitmap format is not RGBA_8888 or RGB_565 !");
        return -5;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
        LOGE("callImage: ImageSetBitmap: AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -4;
    }

    if ((ret = SetBuff(gLoadPage, info.width, info.height, (uint8_t *)canvas, colorFormat)) < 0) {
        LOGE("callImage: ImageSetBitmap: SetBuff() failed ! error=%d", ret);
        return -5;
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    gImageData[gLoadPage].UseFlag = 1;
    gImageData[gLoadPage].OrgWidth = info.width;
    gImageData[gLoadPage].OrgHeight = info.height;

    gLoadPage = -1;
#ifdef DEBUG
    LOGD("callImage: ImageConvert : end - result=%d", ret);
#endif
    return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageConvert
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageConvert (JNIEnv *env, jclass obj, jint type, jint scale)
{
	if (gLoadPage < 0 || gTotalPages <= gLoadPage) {
		LOGE("callImage: ImageConvert: Illegal Page.(%d)", (int)gLoadPage);
		return - 2;
	}
#ifdef DEBUG
	LOGD("callImage: ImageConvert: page=%d, filesize=%d, type=%d, scale=%d", (int)gLoadPage, (int)gLoadFilePos, type, scale);
#endif

	int ret = 0;
	gLoadError = 0;

    if (setjmp(gJmpBuff) == 0) {
        CheckImageType(&type);

        if (type == IMAGETYPE_JPEG) {
#ifdef HAVE_LIBJPEG
            //ret = LoadImageJpeg(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
#endif
            ret = -200 - IMAGETYPE_JPEG;
        }
        else if (type == IMAGETYPE_PNG){
#ifdef HAVE_LIBPNG
            //ret = LoadImagePng(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
#endif
            ret = -200 - IMAGETYPE_PNG;
        }
        else if (type == IMAGETYPE_GIF){
#ifdef HAVE_LIBGIF
            //ret = LoadImageGif(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
#endif
            ret = -200 - IMAGETYPE_GIF;
        }
        else if (type == IMAGETYPE_WEBP){
#ifdef HAVE_LIBWEBP
            //ret = LoadImageWebp(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
#endif
            ret = -200 - IMAGETYPE_WEBP;
        }
        else if (type == IMAGETYPE_AVIF){
#ifdef HAVE_LIBAVIF
            ret = LoadImageAvif(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
            if (ret < 0) {
                LOGE("callImage: ImageConvert: LoadImageAvif() failed. return=%d", ret);
            }
#endif
            if (ret == 0 && gLoadError) {
                ret = -200 - IMAGETYPE_AVIF;
            }
        }
        else if (type == IMAGETYPE_HEIF){
#ifdef HAVE_LIBHEIF
            //ret = LoadImageHeif(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
#endif
            ret = -200 - IMAGETYPE_HEIF;
        }
        else if (type == IMAGETYPE_JXL){
#ifdef HAVE_LIBJXL
            ret = LoadImageJxl(SET_BUFFER, &gImageData[gLoadPage], gLoadPage, scale, NULL);
            if (ret < 0) {
                LOGE("ImageConvert: LoadImageJxl() failed. return=%d", ret);
            }
#endif
            if (ret == 0 && gLoadError) {
                ret = -200 - IMAGETYPE_JXL;
            }
        }
	}
    else {
		ret = -1;
	}

	gLoadPage = -1;
#ifdef DEBUG
	LOGD("callImage: ImageConvert: end - result=%d", ret);
#endif
	return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageGetBitmap
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageGetBitmap (JNIEnv *env, jclass obj, jint type, jint scale, jobject bitmap)
{
	if (gLoadPage != -1) {
		LOGE("callImage: ImageGetBitmap: Illegal Page.(%d)", (int)gLoadPage);
		return - 2;
	}
#ifdef DEBUG
	LOGD("callImage: ImageGetBitmap : page=%d, filesize=%d, type=%d, scale=%d", (int)gLoadPage, (int)gLoadFilePos, type, scale);
#endif
    int ret = 0;
    gLoadError = 0;
    AndroidBitmapInfo	info;
    void				*canvas;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("callImage: ImageGetBitmap: AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -3;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("callImage: ImageGetBitmap: Bitmap format is not RGB_565 !");
        return -4;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
        LOGE("callImage: ImageGetBitmap: AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -5;
    }

    if (setjmp(gJmpBuff) == 0) {
        CheckImageType(&type);

        if (type == IMAGETYPE_JPEG) {
#ifdef HAVE_LIBJPEG
            //ret = LoadImageJpeg(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
#endif
            ret = -200 - IMAGETYPE_JPEG;
        }
        else if (type == IMAGETYPE_PNG){
#ifdef HAVE_LIBPNG
            //ret = LoadImagePng(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
#endif
            ret = -200 - IMAGETYPE_PNG;
        }
        else if (type == IMAGETYPE_GIF){
#ifdef HAVE_LIBGIF
            //ret = LoadImageGif(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
#endif
            ret = -200 - IMAGETYPE_GIF;
        }
        else if (type == IMAGETYPE_WEBP){
#ifdef HAVE_LIBWEBP
            //ret = LoadImageWebp(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
#endif
            ret = -200 - IMAGETYPE_WEBP;
        }
        else if (type == IMAGETYPE_AVIF){
#ifdef HAVE_LIBAVIF
            ret = LoadImageAvif(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
            if (ret < 0) {
                LOGE("callImage: ImageGetBitmap: [error] LoadImageAvif() failed. return=%d", ret);
            }
#endif
            if (ret == 0 && gLoadError) {
                ret = -200 - IMAGETYPE_AVIF;
            }
        }
        else if (type == IMAGETYPE_HEIF){
#ifdef HAVE_LIBHEIF
            //ret = LoadImageHeif(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
#endif
            ret = -200 - IMAGETYPE_HEIF;
        }
        else if (type == IMAGETYPE_JXL){
#ifdef HAVE_LIBJXL
            ret = LoadImageJxl(SET_BITMAP, &gImageData[gLoadPage], gLoadPage, scale, (WORD *)canvas);
            if (ret < 0) {
                LOGE("ImageGetBitmap: [error] LoadImageJxl() failed. return=%d", ret);
            }
#endif
            if (ret == 0 && gLoadError) {
                ret = -200 - IMAGETYPE_JXL;
            }
        }
    }
    else {
        ret = -1;
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    gLoadPage = -1;
#ifdef DEBUG
    LOGD("callImage: ImageGetBitmap: end - result=%d", ret);
#endif
    return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageTerminate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_src_comitton_stream_CallImgLibrary_ImageTerminate (JNIEnv *env, jclass obj)
{
//	LOGD("Terminate");
	MemFree();
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageGetFreeSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageGetFreeSize (JNIEnv *env, jclass obj)
{
//	LOGD("ImageGetFreeSize : Start");
	int  count = 0, i;
	for (i = 0 ; i < gBuffNum ; i ++) {
		if (gBuffMng[i].Page == -1) {
			count ++;
		}
	}
#ifdef DEBUG
	LOGD("callImage: ImageGetFreeSize : %d / %d", count, (int)gBuffNum);
#endif
	return (count);
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    GetMarginSize
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_GetMarginSize (JNIEnv *env, jclass obj, jint page, jint half, jint index, jint width, jint height, jint margin, jint margincolor, jintArray size)
{
//#define DEBUG_GetMarginSize
	if (page < 0 || gTotalPages <= page) {
		LOGE("callImage: GetMarginSize : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG_GetMarginSize
	LOGD("callImage: GetMarginSize : page=%d, half=%d, width=%d, height=%d", page, half, width, height);
#endif

	jint *retsize = env->GetIntArrayElements(size, NULL);
	int ret = GetMarginSize(page, half, index, width, height, margin, margincolor, &retsize[0], &retsize[1], &retsize[2], &retsize[3]);
	env->ReleaseIntArrayElements(size, retsize, 0);
	return ret;
//	return
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageScale
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageScale (JNIEnv *env, jclass obj, jint page, jint half, jint width, jint height, jint left, jint right, jint top, jint bottom, jint algorithm, jint rotate, jint margin, jint margincolor, jint sharpen, jint bright, jint gamma, jint param, jintArray size)
{
	if (page < 0 || gTotalPages <= page) {
		LOGE("callImage: ImageScale : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG
	LOGD("callImage: ImageScale : page=%d, half=%d, width=%d, height=%d", page, half, width, height);
#endif

    jint *retsize = env->GetIntArrayElements(size, NULL);
	int ret = CreateScale(page, half, width, height, left, right, top, bottom, algorithm, rotate, margin, margincolor, sharpen, bright, gamma, param, retsize);
    env->ReleaseIntArrayElements(size, retsize, 0);
	return ret;
//	return 
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageFree
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageFree (JNIEnv *env, jclass obj, jint page)
{
	if (page < 0 || gTotalPages <= page) {
		LOGE("callImage: ImnageFree : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG
	LOGD("callImage: ImageFree : page=%d", page);
#endif
	gImageData[page].UseFlag = 0;
    // 画像の領域全部を解放
	ReleaseBuff(page, -1, -1);
//	gImageData[page].LoadSize = 0;
//	gImageData[page].LoadPos = 0;
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageScaleFree
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageScaleFree (JNIEnv *env, jclass obj, jint page, jint half)
{
	if (page != -1 && (page < 0 || gTotalPages <= page)) {
		LOGE("callImage: ImageScaleFree : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG
	LOGD("callImage: ImageScaleFree : page=%d, half=%d", page, half);
#endif

    // 画像の縮尺を解放
	ReleaseBuff(page, 1, half);
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageDraw
 * Signature: (IIILandroid/graphics/Bitmap;)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageDraw (JNIEnv *env, jclass obj, jint page, jint half, jint x, jint y, jobject bitmap)
{
//#define DEBUG_ImageDraw
	if (page < 0 || gTotalPages <= page) {
		LOGE("callImage: ImageDraw : Illegal Page.(%d)", page);
		return -1;
	}
//	if (gImageData[page].SclFlag != 1) {
//		LOGE("ImageDraw : no Scale Image.(%d)", page);
//		return -2;
//	}

#ifdef DEBUG_ImageDraw
	LOGD("callImage: ImageDraw : page=%d, half=%d, x=%d, y=%d / sflg=%d, ow=%d, oh=%d, sw=%d, sh=%d"
			, page, half, x, y
			, (int)gImageData[page].SclFlag[half]
			, (int)gImageData[page].OrgWidth, (int)gImageData[page].OrgHeight
			, (int)gImageData[page].SclWidth[half], (int)gImageData[page].SclHeight[half]);
#endif

	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("callImage: ImageDraw : AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -2;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
		LOGE("callImage: ImageDraw : Bitmap format is not RGB_565 !");
		return -3;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("callImage: ImageDraw : AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -4;
	}

//	memset(canvas, 0, info.width * info.height * sizeof(uint16_t));
	ret = DrawBitmap(page, half, x, y, canvas, info.width, info.height, info.stride, &gImageData[page]);

	AndroidBitmap_unlockPixels(env, bitmap);
#ifdef DEBUG_ImageDraw
	LOGD("callImage: ImageDraw : end");
#endif
	return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageScaleDraw
 * Signature: (IIILandroid/graphics/Bitmap;)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageScaleDraw (JNIEnv *env, jclass obj, jint page, jint rotate, jint s_x, jint s_y, jint s_cx, jint s_cy, jint d_x, jint d_y, jint d_cx, jint d_cy, jint psel, jobject bitmap, jint cut_left, jint cut_right, jint cut_top, jint cut_bottom)
{
	if (page < 0 || gTotalPages <= page) {
		LOGE("callImage: ImageScaleDraw : Illegal Page.(%d)", page);
		return -1;
	}
	if (gImageData[page].UseFlag != 1) {
		LOGE("callImage: ImageScaleDraw : no Scale Image.(%d)", page);
		return -2;
	}

//	LOGD("callImage: ImageScaleDraw : page=%d, x=%d, y=%d / sflg=%d, ow=%d, oh=%d, sw=%d, sh=%d"
//			, page, x, y
//			, gImageData[page].SclFlag
//			, gImageData[page].OrgWidth, gImageData[page].OrgHeight
//			, gImageData[page].SclWidth, gImageData[page].SclHeight);

	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("callImage: ImageScaleDraw : AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -3;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
		LOGE("callImage: ImageScaleDraw : Bitmap format is not RGB_565 !");
		return -4;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("callImage: ImageScaleDraw : AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -5;
	}

//	memset(canvas, 0, info.width * info.height * sizeof(uint16_t));
	ret = DrawScaleBitmap(page, rotate, s_x, s_y, s_cx, s_cy, d_x, d_y, d_cx, d_cy, canvas, info.width, info.height, info.stride, psel, &gImageData[page], cut_left, cut_right, cut_top, cut_bottom);

	AndroidBitmap_unlockPixels(env, bitmap);
	return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageCancel
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_ImageCancel (JNIEnv *env, jclass obj, jint flag)
{
//	LOGD("ImageCancel : flag=%d", flag);
	gCancel = flag;
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    SetParameter
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallImgLibrary_SetParameter (JNIEnv *env, jclass obj, jint threadnum) {

    jint ret = 0;
    uint32_t width;             // 画像の幅
    uint32_t height;            // 画像の高さ

    if (threadnum > 0) {
        gMaxThreadNum = threadnum;
    }
    else {
        LOGE("callImage: SetParameter : Illegal Param.(%d)", threadnum);
        ret = -1;
    }
    return ret;
}


} // extern "C"
