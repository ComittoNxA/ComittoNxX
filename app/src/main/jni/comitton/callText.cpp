#include <jni.h>
#include <time.h>
#include <malloc.h>
#include <string.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <stdio.h>
#include <setjmp.h>

#include "text.h"

//#define DEBUG

// サムネイル管理
BYTE		*gTextImages[MAX_TEXTPAGE] = {NULL, NULL, NULL, NULL, NULL};
int			gTextImagePages[MAX_TEXTPAGE] = {-1, -1, -1, -1, -1};
int			gTextImageSize = 0;

extern "C" {
// イメージ保存
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallTxtLibrary_SetTextImage (JNIEnv *env, jclass obj, jobject bitmap, jint page, jint current_page)
{
//	LOGD("SetTextImage : pg=%d, cp=%d", page, current_page);

    // IDの一致チェック
	if (page < 0 || page < current_page - 2 || page > current_page + 2) {
        // ページ不正
//		LOGD("SetTextImage : Illegal page(pg=%d, cp=%d)", page, current_page);
		return -1;
	}

    // メモリ獲得
	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -2;
	}
//	LOGD("BitmaInfo : w=%d, h=%d, s=%d, fm=%d, fl=%d, pg=%d, cpg=%d", info.width, info.height, info.stride, info.format, info.flags, page, current_page);

//	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
//		LOGE("Bitmap format is not RGB_565 !");
//		return -3;
//	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -4;
	}

    // イメージサイズ
//	int size = sizeof(WORD) * info.width * info.height;
	int size = info.stride * info.height;
	if (gTextImageSize != size) {
        // メモリ再獲得
		if (TextImagesAlloc(size) != 0) {
			return -5;
		}
	}

	int index = TextImageGetFree(current_page);
	if (index == -1) {
		return -6;
	}

    // メモリに保存
	memcpy(gTextImages[index], (BYTE*)canvas, size);
	gTextImagePages[index] = page;

	AndroidBitmap_unlockPixels(env, bitmap);
	return ret;
}

// イメージ取得
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallTxtLibrary_GetTextImage(JNIEnv *env, jclass obj, jobject bitmap, jint page)
{
//	LOGD("GetTextImage : pg=%d", page);
    // メモリ獲得
	AndroidBitmapInfo	info;
	int					ret;
	void				*canvas;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return -2;
	}

//	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
//		LOGE("Bitmap format is not RGB_565 !");
//		return -3;
//	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &canvas)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return -4;
	}

	int size = info.stride * info.height;
	int index = TextImageFindPage(page);
	
//	LOGD("getTextImage : page=%d, size=%d/%d, index=%d", page, gTextImageSize, size, index);
	if (gTextImageSize == size && index != -1) {
        // ビットマップに返す
		memcpy(canvas, gTextImages[index], gTextImageSize);
		ret = 0;
	}
	else {
		ret = -1;
	}
	AndroidBitmap_unlockPixels(env, bitmap);
	return ret;
}

// イメージ存在チェック
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallTxtLibrary_CheckTextImage(JNIEnv *env, jclass obj, jint page)
{
//	LOGD("CheckTextImage : pg=%d", page);
    // メモリ獲得
	int		ret = 0;

	int index = TextImageFindPage(page);
//	LOGD("checkTextImage : page=%d, index=%d", page, index);
	
	if (index != -1) {
        // ビットマップに返す
		ret = 1;
	}
	return ret;
}

// イメージ解放
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallTxtLibrary_FreeTextImage(JNIEnv *env, jclass obj)
{
//	LOGD("FreeTextImage");
	TextImagesFree();
	return 0;
}
}
