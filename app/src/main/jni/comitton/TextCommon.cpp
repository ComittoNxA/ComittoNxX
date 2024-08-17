#include <malloc.h>
#include <string.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "text.h"

extern BYTE		*gTextImages[MAX_TEXTPAGE];
extern int		gTextImagePages[MAX_TEXTPAGE];
extern int		gTextImageSize;

// テキスト用メモリ獲得
int TextImagesAlloc(int size) {
	TextImagesFree();

	// イメージバッファ獲得
	for (int i = 0 ; i < MAX_TEXTPAGE ; i ++) {
		gTextImages[i] = (BYTE*)malloc(size);
		if (gTextImages[i] == NULL) {
			TextImagesFree();
			return -1;
		}
	}
	gTextImageSize = size;
	return 0;
}

// テキスト探す
int TextImageFindPage(int page) {
	for (int i = 0 ; i < MAX_TEXTPAGE ; i ++) {
		if (gTextImagePages[i] == page) {
			return i;
		}
	}
	return -1;
}

// 空きスペースを探す
int TextImageGetFree(int currnet_page) {
	for (int i = 0 ; i < MAX_TEXTPAGE ; i ++) {
		if (gTextImagePages[i] == -1 || gTextImagePages[i] < currnet_page - 2 || gTextImagePages[i] > currnet_page + 2) {
			return i;
		}
	}
	return -1;
}

// テキスト用メモリ解放
void TextImagesFree()
{
	// イメージバッファ解放
	for (int i = 0 ; i < MAX_TEXTPAGE ; i ++) {
		if (gTextImages[i] != NULL) {
			// 解放
			free(gTextImages[i]);
			gTextImages[i] = NULL;
		}
		gTextImagePages[i] = -1;
	}
	gTextImageSize = 0;
	return;
}
