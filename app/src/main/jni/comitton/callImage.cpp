#include <jni.h>
#include <malloc.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <csetjmp>
#include <cstring>
#include "Image.h"

//#define DEBUG

// イメージ管理
/** キャッシュの初期化有無 */
bool        gIsInit[MAX_BUFFER_INDEX] = {false};
/** 画像のページ番号やサイズ、バッファーのアドレスなど */
IMAGEDATA	*gImageData[MAX_BUFFER_INDEX] = {nullptr};
/** 画像の総数 */
long		gTotalPages[MAX_BUFFER_INDEX] = {0};
/** 画像ファイルの最大ファイルサイズ */
long		gLoadBuffSize[MAX_BUFFER_INDEX] = {0};
/** 画像ファイルを受け取るためのバッファー */
char		*gLoadBuffer[MAX_BUFFER_INDEX] = {nullptr};
/** ピクセルデータの保存先アドレス */
LONG		**gLinesPtr[MAX_BUFFER_INDEX];
LONG		**gDsLinesPtr[MAX_BUFFER_INDEX];
/** サイズ変更時ピクセルデータの一時保存先アドレス */
LONG		**gSclLinesPtr[MAX_BUFFER_INDEX];

long		gLoadFileSize[MAX_BUFFER_INDEX];
long		gLoadFilePos[MAX_BUFFER_INDEX];
long		gLoadPage[MAX_BUFFER_INDEX];
int			gLoadError[MAX_BUFFER_INDEX];
/** ユーザによるキャンセルコード */
int			gCancel[MAX_BUFFER_INDEX];
int			gMaxThreadNum = 1;

/** キャッシュ領域の割り当て状況 */
BUFFMNG		*gBuffMng[MAX_BUFFER_INDEX] = {nullptr};
long		gBuffNum[MAX_BUFFER_INDEX] = {0};

/** サイズ変更画像待避用領域の割り当て状況 */
BUFFMNG		*gSclBuffMng[MAX_BUFFER_INDEX] = {nullptr};
long		gSclBuffNum[MAX_BUFFER_INDEX] = {0};

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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailInitialize (JNIEnv *env, jclass obj, jlong id, jint pagesize, jint pagenum, jint imagenum)
{
//#define DEBUG_ThumbnailInitialize

#ifdef DEBUG_ThumbnailInitialize
	LOGD("callImage: ThumbnailInitialize : id=%lld, pagesize=%d, pagenum=%d, imagenum=%d", id, pagesize, pagenum, imagenum);
#endif

	int ret = ThumbnailAlloc(id, pagesize, pagenum, imagenum);
	return ret;
}

// サムネイルのNoImage設定
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailSetNone (JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailSetNone

#ifdef DEBUG_ThumbnailSetNone
	LOGD("callImage: ThumbnailSetNone : id=%lld, index=%d", id, index);
#endif

	int ret = ThumbnailSetNone(id, index);
	return ret;
}

// サムネイルの残り領域確認
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailCheck (JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailCheck

#ifdef DEBUG_ThumbnailCheck
	LOGD("callImage: ThumbnailCheck : id=%lld, index=%d", id, index);
#endif

	int ret = ThumbnailCheck(id, index);
	return ret;
}

// サムネイルの残り領域確認
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailMemorySizeCheck (JNIEnv *env, jclass obj, jlong id, jint width, jint height)
{
//#define DEBUG_ThumbnailMemorySizeCheck

#ifdef DEBUG_ThumbnailMemorySizeCheck
	LOGD("callImage: ThumbnailMemorySizeCheck : id=%lld, width=%d, height=%d", id, width, height);
#endif

	int ret = ThumbnailMemorySizeCheck(id, width, height);
	return ret;
}

// サムネイルを整理して容量確保
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailImageAlloc (JNIEnv *env, jclass obj, jlong id, jint blocks, jint index)
{
//#define DEBUG_ThumbnailImageAlloc

#ifdef DEBUG_ThumbnailImageAlloc
	LOGD("callImage: ThumbnailImageAlloc : id=%lld, blocks=%d, index=%d", id, blocks, index);
#endif

	int ret = ThumbnailImageAlloc(id, blocks, index);
	return ret;
}

// サムネイルが全て設定されているかをチェック
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailCheckAll (JNIEnv *env, jclass obj, jlong id)
{
//#define DEBUG_ThumbnailCheckAll

#ifdef DEBUG_ThumbnailCheckAll
	LOGD("callImage: ThumbnailCheckAll : id=%lld", id);
#endif

	int ret = ThumbnailCheckAll(id);
	return ret;
}

// サムネイルが全て設定されているかをチェック
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailSave (JNIEnv *env, jclass obj, jlong id, jobject bitmap, jint index)
{
//#define DEBUG_ThumbnailSave

#ifdef DEBUG_ThumbnailSave
	LOGD("callImage: ThumbnailSave : id=%lld, index=%d", id, index);
#endif
	if (bitmap == nullptr) {
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

// サムネイルが全て設定されているかをチェック
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailRemove (JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailRemove

#ifdef DEBUG_ThumbnailRemove
    LOGD("callImage: ThumbnailRemove : id=%lld, index=%d", id, index);
#endif

    return ThumbnailRemove(id, index);

}

// サムネイル描画
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailImageSize(JNIEnv *env, jclass obj, jlong id, jint index)
{
//#define DEBUG_ThumbnailImageSize

#ifdef DEBUG_ThumbnailImageSize
    LOGD("callImage: ThumbnailImageSize: 開始します. id=%ld, index=%d", id, index);
#endif
    return ThumbnailImageSize(id, index);
}

// サムネイル描画
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailDraw(JNIEnv *env, jclass obj, jlong id, jobject bitmap, jint index)
{
//#define DEBUG_ThumbnailDraw

#ifdef DEBUG_ThumbnailDraw
	LOGD("callImage: ThumbnailDraw : id=%lld, index=%d", id, index);
#endif

	if (bitmap == nullptr) {
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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ThumbnailFree(JNIEnv *env, jclass obj, jlong id)
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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageInitialize (JNIEnv *env, jclass obj, jlong loadsize, jint buffsize, jint totalpage, jint threadnum)
{
#ifdef DEBUG
	LOGD("callImage: ImageInitialize : buffsize=%d * 4, page=%d", buffsize, totalpage);
#endif

    // 空いているキャッシュindexを取得する
    int index;
    for (index = 0; index < MAX_BUFFER_INDEX; ++index) {
        if (gIsInit[index] == false) {
            break;
        }
    }
    if (index == MAX_BUFFER_INDEX) {
        // キャッシュインデックスに空きがなければエラー終了
        return ERROR_CODE_CACHE_COUNT_LIMIT_EXCEEDED;
    }

    // 読み込み用領域確保
	//MemFree(index);

	gLoadBuffSize[index] = loadsize;
	gTotalPages[index]  = totalpage;
	gCancel[index] = 0;

	jint ret = index;

	gLoadPage[index] = -1;
	ret = MemAlloc(index, buffsize);

	if (threadnum > 0) {
		gMaxThreadNum = threadnum;
	}
	else {
		LOGE("callImage: ImageInitialize : Illegal Param.(%d)", threadnum);
	}

#ifdef DEBUG
    LOGD("callImage: Initialize: gLoadBuffer[%d]=%ld", index,  (long)(gLoadBuffer[index]));
#endif
	return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageSetPage (JNIEnv *env, jclass obj, jint index, jint page, jlong size) {
#ifdef DEBUG
    LOGD("callImage: ImageSetSize : page=%d, size=%d", page, size);
#endif

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (gLoadBuffSize[index] < size) {
        // ロード領域不足
        LOGE("callImage: ImageSetSize : gLoadBuffSize[index]=%ld < size=%ld", gLoadBuffSize[index], size);
        return -2;
    }

    if (page < 0 || gTotalPages[index] <= page) {
        // ページ番号不正
        return -3;
    }

    gLoadPage[index] = page;
    gLoadFileSize[index] = size;
    gLoadFilePos[index] = 0;
    gImageData[index][page].SclFlag[0] = 0;
    gImageData[index][page].SclFlag[1] = 0;
    gImageData[index][page].SclFlag[2] = 0;
	return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetData
 * Signature: ([BI)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageSetData (JNIEnv *env, jclass obj, jint index, jbyteArray dataArray, jint size)
{
	jbyte *data = env->GetByteArrayElements(dataArray, nullptr);


    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

	if (gLoadFileSize[index] - gLoadFilePos[index] < size) {
        // セットしたサイズを超えないように
		size = gLoadFileSize[index] - gLoadFilePos[index];
	}
	memcpy(&gLoadBuffer[index][gLoadFilePos[index]], data, size);
	gLoadFilePos[index] += size;

	env->ReleaseByteArrayElements(dataArray, data, 0);
//	LOGD("setdata end");
	return size;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageSetFileSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageSetFileSize (JNIEnv *env, jclass obj, jint index, jlong size)
{
#ifdef DEBUG
    LOGD("callImage: ImageSetSize : page=%d, size=%d", size);
#endif

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (gLoadBuffSize[index] < size) {
        // ロード領域不足
        return -2;
    }

    gLoadPage[index] = -1;
    gLoadFileSize[index] = size;
    gLoadFilePos[index] = 0;
    return 0;
}

/*
 * Class:     src_comitton_stream_ImageGetSize
 * Method:    ImageGetSize
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageGetSize (JNIEnv *env, jclass obj, jint index, jint type, jintArray imagesize)
{
    int ret = 0;

#ifdef DEBUG
    LOGD("callImage: ImageGetSize : page=%d, filesize=%d, type=%d", (int)gLoadPage[index], (int)gLoadFilePos[index], type);
#endif

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    jint *iSize = nullptr;
    if (imagesize != nullptr) {
        iSize = (jint*)env->GetIntArrayElements(imagesize, nullptr);
    }

    CheckImageType(index, &type);

    if (type == IMAGETYPE_JPEG) {
        // なにもしない
        if (gLoadError[index] != 0) {
            ret = -4;
        }
    }
    else if (type == IMAGETYPE_PNG){
        // なにもしない
        if (gLoadError[index] != 0) {
            ret = -4;
        }
    }
    else if (type == IMAGETYPE_GIF){
        // なにもしない
        if (gLoadError[index] != 0) {
            ret = -4;
        }
    }
    else if (type == IMAGETYPE_WEBP){
        // なにもしない
        if (gLoadError[index] != 0) {
            ret = -4;
        }
    }
    else if (type == IMAGETYPE_AVIF){
#ifdef DEBUG
        LOGD("callImage: ImageGetSize : Call ImageGetSizeAvif.");
#endif
        ret = ImageGetSizeAvif(index, type, &iSize[0], &iSize[1]);
        if (ret == 0 && gLoadError[index]) {
            ret = -4;
        }
    }
    else if (type == IMAGETYPE_HEIF){
        // なにもしない
        if (gLoadError[index] != 0) {
            ret = -4;
        }
    }
    else if (type == IMAGETYPE_JXL){
#ifdef DEBUG
        LOGD("callImage: ImageGetSize : Call ImageGetSizeJxl.");
#endif
        ret = ImageGetSizeJxl(index, type, &iSize[0], &iSize[1]);
        if (ret == 0 && gLoadError[index]) {
            ret = -4;
        }
    }

    if (imagesize != nullptr) {
        env->ReleaseIntArrayElements(imagesize, iSize, 0);
    }

    gLoadPage[index] = -1;
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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageSetBitmap (JNIEnv *env, jclass obj, jint index, jobject bitmap)
{
    AndroidBitmapInfo	info;
    void				*canvas;
    colorFormat colorFormat;

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (gLoadPage[index] < 0 || gTotalPages[index] <= gLoadPage[index]) {
        LOGE("callImage: ImageSetBitmap: Illegal Page.(%d)", (int)gLoadPage[index]);
        return - 3;
    }
#ifdef DEBUG
    LOGD("callImage: ImageSetBitmap : page=%d, filesize=%d", (int)gLoadPage[index], (int)gLoadFilePos[index]);
#endif

    int ret = 0;
    gLoadError[index] = 0;

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

    if ((ret = SetBuff(index, gLoadPage[index], info.width, info.height, (uint8_t *)canvas, colorFormat)) < 0) {
        LOGE("callImage: ImageSetBitmap: SetBuff() failed ! error=%d", ret);
        return ret;
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    gImageData[index][gLoadPage[index]].UseFlag = 1;
    gImageData[index][gLoadPage[index]].OrgWidth = info.width;
    gImageData[index][gLoadPage[index]].OrgHeight = info.height;

    gLoadPage[index] = -1;
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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageConvert (JNIEnv *env, jclass obj, jint index, jint type, jint scale)
{

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (gLoadPage[index] < 0 || gTotalPages[index] <= gLoadPage[index]) {
		LOGE("callImage: ImageConvert: Illegal Page.(%d)", (int)gLoadPage[index]);
		return - 2;
	}
#ifdef DEBUG
	LOGD("callImage: ImageConvert: page=%d, filesize=%d, type=%d, scale=%d", (int)gLoadPage[index], (int)gLoadFilePos[index], type, scale);
#endif

	int ret = 0;
	gLoadError[index] = 0;

    CheckImageType(index, &type);

    if (type == IMAGETYPE_JPEG) {
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_PNG){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_GIF){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_WEBP){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_AVIF){
        ret = LoadImageAvif(index, SET_BUFFER, &gImageData[index][gLoadPage[index]], gLoadPage[index], scale, nullptr);
        if (ret < 0) {
            LOGE("callImage: ImageConvert: LoadImageAvif() failed. return=%d", ret);
        }
        if (gLoadError[index] != 0) {
            ret = -200 - IMAGETYPE_AVIF;
        }
    }
    else if (type == IMAGETYPE_HEIF){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_JXL){
        ret = LoadImageJxl(index, SET_BUFFER, &gImageData[index][gLoadPage[index]], gLoadPage[index], scale, NULL);
        if (ret < 0) {
            LOGE("ImageConvert: LoadImageJxl() failed. return=%d", ret);
        }
        if (gLoadError[index] != 0) {
            ret = -200 - IMAGETYPE_JXL;
        }
    }

	gLoadPage[index] = -1;
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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageGetBitmap (JNIEnv *env, jclass obj, jint index, jint type, jint scale, jobject bitmap)
{

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (gLoadPage[index] != -1) {
		LOGE("callImage: ImageGetBitmap: Illegal Page.(%d)", (int)gLoadPage[index]);
		return - 2;
	}
#ifdef DEBUG
	LOGD("callImage: ImageGetBitmap : page=%d, filesize=%d, type=%d, scale=%d", (int)gLoadPage[index], (int)gLoadFilePos[index], type, scale);
#endif
    int ret = 0;
    gLoadError[index] = 0;
    AndroidBitmapInfo	info;
    void				*canvas;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("callImage: ImageGetBitmap: AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -3;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
        LOGE("callImage: ImageGetBitmap: AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -5;
    }

    CheckImageType(index, &type);

    if (type == IMAGETYPE_JPEG) {
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_PNG){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_GIF){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_WEBP){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_AVIF){
        ret = LoadImageAvif(index, SET_BITMAP, &gImageData[index][gLoadPage[index]], gLoadPage[index], scale, (WORD *)canvas);
        if (ret < 0) {
            LOGE("callImage: ImageGetBitmap: [error] LoadImageAvif() failed. return=%d", ret);
        }
        if (ret == 0 && gLoadError[index]) {
            ret = -200 - IMAGETYPE_AVIF;
        }
    }
    else if (type == IMAGETYPE_HEIF){
        ret = ERROR_CODE_IMAGE_TYPE_NOT_SUPPORT;
    }
    else if (type == IMAGETYPE_JXL){
        ret = LoadImageJxl(index, SET_BITMAP, &gImageData[index][gLoadPage[index]], gLoadPage[index], scale, (WORD *)canvas);
        if (ret < 0) {
            LOGE("ImageGetBitmap: [error] LoadImageJxl() failed. return=%d", ret);
        }
        if (ret == 0 && gLoadError[index]) {
            ret = -200 - IMAGETYPE_JXL;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    gLoadPage[index] = -1;
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
JNIEXPORT int JNICALL Java_src_comitton_jni_CallImgLibrary_ImageTerminate (JNIEnv *env, jclass obj, jint index)
{
//	LOGD("Terminate");
    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    MemFree(index);
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageGetFreeSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageGetFreeSize (JNIEnv *env, jclass obj, jint index)
{
//	LOGD("ImageGetFreeSize : Start");

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    int  count = 0, i;
	for (i = 0 ; i < gBuffNum[index] ; i ++) {
		if (gBuffMng[index][i].Page == -1) {
			count ++;
		}
	}
#ifdef DEBUG
	LOGD("callImage: ImageGetFreeSize : %d / %d", count, (int)gBuffNum[index]);
#endif
	return (count);
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    GetMarginSize
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_GetMarginSize (JNIEnv *env, jclass obj, jint index, jint page, jint half, jint Index, jint margin, jint margincolor, jintArray size)
{
//#define DEBUG_GetMarginSize

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (page < 0 || gTotalPages[index] <= page) {
		LOGE("callImage: GetMarginSize : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG_GetMarginSize
	LOGD("callImage: GetMarginSize : page=%d, half=%d, width=%d, height=%d", page, half, width, height);
#endif

	jint *retsize = env->GetIntArrayElements(size, nullptr);
	int ret = GetMarginSize(index, page, half, Index, margin, margincolor, &retsize[0], &retsize[1], &retsize[2], &retsize[3]);
	env->ReleaseIntArrayElements(size, retsize, 0);
	return ret;
//	return
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageScale
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageScale (JNIEnv *env, jclass obj, jint index, jint page, jint half, jint width, jint height, jint left, jint right, jint top, jint bottom, jint algorithm, jint rotate, jint margin, jint margincolor, jint sharpen, jint bright, jint gamma, jint param, jintArray size)
{

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (page < 0 || gTotalPages[index] <= page) {
		LOGE("callImage: ImageScale : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG
	LOGD("callImage: ImageScale : page=%d, half=%d, width=%d, height=%d", page, half, width, height);
#endif

    jint *retsize = env->GetIntArrayElements(size, nullptr);
	int ret = CreateScale(index, page, half, width, height, left, right, top, bottom, algorithm, rotate, margin, margincolor, sharpen, bright, gamma, param, retsize);
    env->ReleaseIntArrayElements(size, retsize, 0);
	return ret;
//	return 
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageFree
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageFree (JNIEnv *env, jclass obj, jint index, jint page)
{

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (page < 0 || gTotalPages[index] <= page) {
		LOGE("callImage: ImageFree : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG
	LOGD("callImage: ImageFree : page=%d", page);
#endif
	gImageData[index][page].UseFlag = 0;
    // 画像の領域全部を解放
	ReleaseBuff(index, page, -1, -1);
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageScaleFree
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageScaleFree (JNIEnv *env, jclass obj, jint index, jint page, jint half)
{

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (page != -1 && (page < 0 || gTotalPages[index] <= page)) {
		LOGE("callImage: ImageScaleFree : Illegal Page.(%d)", page);
		return -1;
	}
#ifdef DEBUG
	LOGD("callImage: ImageScaleFree : page=%d, half=%d", page, half);
#endif

    // 画像の縮尺を解放
	ReleaseBuff(index, page, 1, half);
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageDraw
 * Signature: (IIILandroid/graphics/Bitmap;)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageDraw (JNIEnv *env, jclass obj, jint index, jint page, jint half, jint x, jint y, jobject bitmap)
{
//#define DEBUG_ImageDraw

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (page < 0 || gTotalPages[index] <= page) {
		LOGE("callImage: ImageDraw : Illegal Page.(%d)", page);
		return -1;
	}

#ifdef DEBUG_ImageDraw
	LOGD("callImage: ImageDraw : page=%d, half=%d, x=%d, y=%d / sflg=%d, ow=%d, oh=%d, sw=%d, sh=%d"
			, page, half, x, y
			, (int)gImageData[index][page].SclFlag[half]
			, (int)gImageData[index][page].OrgWidth, (int)gImageData[index][page].OrgHeight
			, (int)gImageData[index][page].SclWidth[half], (int)gImageData[index][page].SclHeight[half]);
#endif

	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("callImage: ImageDraw : AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -2;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("callImage: ImageDraw : AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -4;
	}

//	memset(canvas, 0, info.width * info.height * sizeof(uint16_t));
	ret = DrawBitmap(index, page, half, x, y, canvas, info.width, info.height, info.stride, &gImageData[index][page]);

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
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageScaleDraw (JNIEnv *env, jclass obj, jint index, jint page, jint rotate, jint s_x, jint s_y, jint s_cx, jint s_cy, jint d_x, jint d_y, jint d_cx, jint d_cy, jint psel, jobject bitmap, jint cut_left, jint cut_right, jint cut_top, jint cut_bottom)
{

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    if (!gIsInit[index]) {
        return ERROR_CODE_CACHE_NOT_INITIALIZED;
    }

    if (page < 0 || gTotalPages[index] <= page) {
		LOGE("callImage: ImageScaleDraw : Illegal Page.(%d)", page);
		return -1;
	}
	if (gImageData[index][page].UseFlag != 1) {
		LOGE("callImage: ImageScaleDraw : no Scale Image.(%d)", page);
		return -2;
	}

//	LOGD("callImage: ImageScaleDraw : page=%d, x=%d, y=%d / sflg=%d, ow=%d, oh=%d, sw=%d, sh=%d"
//			, page, x, y
//			, gImageData[index][page].SclFlag
//			, gImageData[index][page].OrgWidth, gImageData[index][page].OrgHeight
//			, gImageData[index][page].SclWidth, gImageData[index][page].SclHeight);

	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("callImage: ImageScaleDraw : AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -3;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("callImage: ImageScaleDraw : AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -5;
	}

//	memset(canvas, 0, info.width * info.height * sizeof(uint16_t));
	ret = DrawScaleBitmap(index, page, rotate, s_x, s_y, s_cx, s_cy, d_x, d_y, d_cx, d_cy, canvas, info.width, info.height, info.stride, psel, &gImageData[index][page], cut_left, cut_right, cut_top, cut_bottom);

	AndroidBitmap_unlockPixels(env, bitmap);
	return ret;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    ImageCancel
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_ImageCancel (JNIEnv *env, jclass obj, jint index, jint flag)
{
//	LOGD("ImageCancel : flag=%d", flag);

    if (index < 0 || MAX_BUFFER_INDEX <= index) {
        return ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE;
    }

    gCancel[index] = flag;
    return 0;
}

/*
 * Class:     src_comitton_stream_CallImgLibrary
 * Method:    SetParameter
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallImgLibrary_SetParameter (JNIEnv *env, jclass obj, jint threadnum) {

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
