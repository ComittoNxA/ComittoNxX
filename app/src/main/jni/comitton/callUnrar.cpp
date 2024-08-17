#include <jni.h>
#include <time.h>
#include <malloc.h>
#include <string.h>
#include <android/log.h>
#include <stdio.h>
#include <unrar/rar.hpp>

byte *ToBuff = NULL;
byte *FromBuff = NULL;
byte *Window = NULL;
int		FromPos = 0;
int		ToPos = 0;

int		MaxCompLen = 0;
int		MaxOrigLen = 0;
int		CompLen = 0;
int		OrigLen = 0;
int		RarVersion = 0;
bool	NoCompress = false;

ComprDataIO	DataIO;
Unpack		*Unp = NULL;

extern "C" {
/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarAlloc
 * Signature: (I[II)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallJniLibrary_rarAlloc(JNIEnv *env, jclass obj, jint cmplen, jint orglen)
{
	if (FromBuff != NULL) {
		free(FromBuff);
	}
	if (ToBuff != NULL) {
		free(ToBuff);
	}
#if 0	// COMITTONxT_MOD
	if (Window != NULL) {
		free(Window);
	}
#endif
	FromBuff = (byte*)malloc(cmplen);
	ToBuff = (byte*)malloc(orglen);
#if 0	// COMITTONxT_MOD
	Window = (byte*)malloc(MAXWINSIZE);
#endif

	MaxCompLen = cmplen;
	MaxOrigLen = orglen;

#if 0	// COMITTONxT_MOD
	if (FromBuff == NULL || ToBuff == NULL || Window == NULL) {
		if (FromBuff != NULL) {
			free(FromBuff);
			FromBuff = NULL;
		}
		if (ToBuff != NULL) {
			free(ToBuff);
			ToBuff = NULL;
		}
		if (Window != NULL) {
			free(Window);
			Window = NULL;
		}
		return -1;
	}
#else
	if (FromBuff == NULL || ToBuff == NULL) {
		if (FromBuff != NULL) {
			free(FromBuff);
			FromBuff = NULL;
		}
		if (ToBuff != NULL) {
			free(ToBuff);
			ToBuff = NULL;
		}
		return -1;
	}
#endif
	if (Unp == NULL) {
		Unp = new Unpack(&DataIO);
	}
#if 0	// COMITTONxT_MOD
	Unp->Init(Window);
#else
    // 外部確保したバッファを利用出来なくなった
	Unp->Init(UNPACK_MAX_WRITE,false);
#endif
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarInit
 * Signature: (I[II)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallJniLibrary_rarInit(JNIEnv *env, jclass obj, jint cmplen, jint orglen, jint rarver, jboolean nocomp)
{
#ifdef DEBUG
	LOGD("rarInit : cl=%d, ol=%d, rv=%d, nc=%d", cmplen, orglen, rarver, (int)nocomp);
#endif
	if (FromBuff == NULL || ToBuff == NULL) {
		return -1;
	}
	if (MaxCompLen < cmplen || MaxOrigLen < orglen) {
		return -2;
	}

	CompLen = cmplen;
	OrigLen = orglen;
	RarVersion = rarver;
	NoCompress = nocomp;

	FromPos = 0;
	ToPos = 0;

//	memset(FromBuff, 0, cmplen);
//	memset(ToBuff, 0, orglen);
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarWrite
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallJniLibrary_rarWrite(JNIEnv *env, jclass obj, jbyteArray cmpArray, jint offset, jint size)
{
	if (FromBuff == NULL) {
		return -1;
	}

	jbyte *data = env->GetByteArrayElements(cmpArray, NULL);

	if (CompLen - FromPos < size) {
		// バッファサイズまでしか書き込まない
		size = CompLen - FromPos;
	}

	memcpy(&FromBuff[FromPos], &data[offset], size);
	FromPos += size;

	env->ReleaseByteArrayElements(cmpArray, data, 0);
	return size;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarDecomp
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallJniLibrary_rarDecomp(JNIEnv *env, jclass obj)
{
#ifdef DEBUG
	LOGD("rarDecomp : NoCompress=%d", (int)NoCompress);
#endif
	if (NoCompress) {
		// 無圧縮の場合はそのままコピー
		memcpy(ToBuff, FromBuff, OrigLen);
	}
	else {
	    DataIO.CurUnpRead=0;
	    DataIO.CurUnpWrite=0;
#if 0	// COMITTONxT_MOD
	    DataIO.UnpFileCRC=0xffffffff;
	    DataIO.PackedCRC=0xffffffff;
#else
        // Hashに変わっている
        // 何もしなくてもOK？
#endif
	    DataIO.UnpVolume = 0;
	    DataIO.NextVolumeMissing=false;
	//    DataIO.TotalArcSize = cmplen;
	//    DataIO.UnpArcSize = cmplen;
		DataIO.SetPackedSizeToRead(CompLen);
	
		DataIO.SetUnpackFromMemory(FromBuff, CompLen);
		DataIO.SetUnpackToMemory(ToBuff, OrigLen);

#if 0	// COMITTONxT_MOD
		memset(Window, 0, MAXWINSIZE);
#else
        // 内部確保領域なので対処不要？
#endif
		Unp->SetDestSize(OrigLen);
		Unp->DoUnpack(RarVersion, false);
	}
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarRead
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallJniLibrary_rarRead(JNIEnv *env, jclass obj, jbyteArray orgArray, jint offset, jint size)
{
	if (ToBuff == NULL) {
		return -1;
	}

	jbyte *data = env->GetByteArrayElements(orgArray, NULL);

	if (OrigLen - ToPos < size) {
		// バッファサイズまでしか読み込まない
		size = OrigLen - ToPos;
	}

	memcpy(&data[offset], &ToBuff[ToPos], size);
	ToPos += size;

	env->ReleaseByteArrayElements(orgArray, data, 0);
	return size;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarInitSeek
 * Signature: (I[II)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_stream_CallJniLibrary_rarInitSeek(JNIEnv *env, jclass obj)
{
	if (ToBuff == NULL) {
		return -1;
	}

	ToPos = 0;
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarClose
 * Signature: ([BI)I
 */
JNIEXPORT void JNICALL Java_src_comitton_stream_CallJniLibrary_rarClose(JNIEnv *env, jclass obj)
{
	if (ToBuff != NULL) {
		free(ToBuff);
		ToBuff = NULL;
//		LOGD("Close : ToBuff");
	}
	if (FromBuff != NULL) {
		free(FromBuff);
		FromBuff = NULL;
//		LOGD("Close : FromBuff");
	}
#if 0	// COMITTONxT_MOD
	if (Window != NULL) {
		free(Window);
		Window = NULL;
//		LOGD("Close : Window");
	}
#endif
//	Unp = NULL;
//	if (Unp != NULL) {
//		delete Unp;
//		LOGD("Close : Delete Unp");
//	}

	CompLen = 0;
	OrigLen = 0;
	return;
}

}

