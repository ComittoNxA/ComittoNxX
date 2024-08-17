#include <time.h>
#include <malloc.h>
#include <string.h>
#include <pthread.h>
#include <android/bitmap.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "Image.h"

//#define DEBUG
#define	THUMB_BLOCKSIZE	(8 * 1024)	// ブロックサイズ

typedef struct thumb_info {
	int			BlockIndex;
	short		Width;
	short		Height;
} THUMBINFO;

typedef short THUMBBLOCK;

// サムネイル管理
long long	gThumbnailId = 0;

THUMBBLOCK	*gThBlockMng = NULL;

BYTE		**gThPageBuff = NULL;	// 領域のアドレス

int			gThPageNum = 0;	// ページ数
int			gThPageBlockNum = 0;	// ページあたりのブロック数
int			gThBlockNum = 0;	// 全ブロック数
int			gThUseBlockNum = 0;	// 使用中ブロック数

int			gThPageSize = 0;	// ページのメモリサイズ
int			gThImageNum = 0;	// 表示するイメージの数

THUMBINFO	*gThImageMng = NULL;	// イメージ管理(最大イメージ数分)

bool gThMutexInit = false;
pthread_mutex_t gThMutex;  // Mutex

void ThumbnailImageFree(int index);
//void CountLog(const char*);

// サムネイル用メモリ解放
int ThumbnailAlloc(long long id, int pagesize, int pagenum, int imagenum)
{
	int ret = 0;

	if (!gThMutexInit) {
//		LOGD("ThumbnailInitialize : Mutex Init");
		// mutex 作成
		pthread_mutexattr_t attr;
		pthread_mutexattr_init(&attr);
		pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE_NP);
		pthread_mutex_init(&gThMutex, &attr);
		gThMutexInit = true;
	}

//	LOGD("ThumbnailAlloc : Mutex Lock/Pre");
	pthread_mutex_lock(&gThMutex);
//	LOGD("ThumbnailAlloc : Mutex Lock/Aft");

	// IDの一致チェック
	if (gThumbnailId > id) {
//		LOGD("ThumbnailInitialize : Illegal thumbnail id=%lld", id);
		ret = -1;
		goto ERROREND;
	}
	else if (gThumbnailId == id) {
//		LOGD("ThumbnailInitialize : already initialized=%lld", id);
		ret = 0;
		goto ERROREND;
	}

	gThumbnailId = id;			// 新しいIDを保持

	if (gThPageNum != pagenum || gThPageSize != pagesize) {
		LOGD("ThumbnailInitialize : Alloc pagenum=%d, pagesize=%d", pagenum, pagesize);
		// メモリサイズの設定が変わった場合は再確保
		ThumbnailFree(id);

		gThPageNum  = pagenum;	// メモリを何ページ獲得するか
		gThPageSize = pagesize;	// 1ページのバイト数
		gThPageBlockNum = pagesize / THUMB_BLOCKSIZE;	// 1ページのブロック数
		gThBlockNum = gThPageBlockNum * pagenum;	// 全ブロック数

		// 保存領域確保(1ページ4MBでページ数分取得)
		gThPageBuff = (BYTE**)malloc(sizeof(BYTE**) * gThPageNum);
		if (gThPageBuff == NULL) {
			LOGE("ThumbnailInitialize : pages array alloc = null");
			ret = -2;
			goto ERROREND;
		}
		// ポインタ配列初期化
		memset(gThPageBuff, 0, sizeof(BYTE**) * gThPageNum);
	
		// ページごとの領域獲得
		for (int i = 0 ; i < gThPageNum ; i ++) {
			gThPageBuff[i] = (BYTE*)malloc(gThPageSize);
			if (gThPageBuff[i] == NULL) {
				LOGE("ThumbnailInitialize : page alloc = null");
				ret = -3;
				goto ERROREND;
			}
		}	

		gThBlockMng = (THUMBBLOCK*)malloc(sizeof(THUMBBLOCK) * gThBlockNum);
		if (gThBlockMng == NULL) {
			LOGE("ThumbnailInitialize : blocks alloc = null");
			ret = -4;
			goto ERROREND;
		}
	}

	// ポインタ配列初期化
	memset(gThBlockMng, 0xFF, sizeof(THUMBBLOCK) * gThBlockNum);
	LOGD("ThumbnailInitialize : pagenum=%d, pagesize=%d, blocknum=%d", pagenum, pagesize, gThBlockNum);
	gThUseBlockNum = 0;	// 使用ブロック数

	// イメージ管理領域
	if (gThImageNum != imagenum) {
		// 新しい個数
		gThImageNum = imagenum;
		// 管理領域獲得
		if (gThImageMng != NULL) {
			// 既存領域は開放
			free(gThImageMng);
		}
		gThImageMng = (THUMBINFO*)malloc(sizeof(THUMBINFO) * gThImageNum);
		if (gThImageMng == NULL) {
			LOGE("ThumbnailInitialize : manage alloc = null");
			ret = -5;
			goto ERROREND;
		}
	}

	memset(gThImageMng, 0xFF, sizeof(THUMBINFO) * gThImageNum);
	LOGD("ThumbnailInitialize : imagenum=%d", imagenum);

ERROREND:
	// 処理途中でエラー発生の場合は解放する
	if (ret != 0){
		ThumbnailFree(id);
	}

//	LOGD("ThumbnailAlloc : Mutex Unlock/Pre");
	pthread_mutex_unlock(&gThMutex);
//	LOGD("ThumbnailAlloc : Mutex Unlock/Aft");

//	CountLog("init");
	return ret;
}

int ThumbnailCheck(long long id, int index)
{
	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailCheck : Illegal thumbnail ID(id=%lld)", id);
		return -1;
	}
	if (gThImageMng == NULL) {
		// 獲得されていない
//		LOGD("ThumbnailCheck : Memory Not Alloced");
		return -2;
	}

//	LOGD("ThumbnailCheck : index=%d, blockindex=%d", index, gThImageMng[index].BlockIndex);
	if (gThImageMng[index].BlockIndex == -1) {
		return 0;
	}
	else if (gThImageMng[index].BlockIndex == -2) {
		return 2;
	}
	return 1;
}

int ThumbnailSetNone(long long id, int index)
{
	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailSetNone : Illegal thumbnail ID(id=%lld)", id);
		return -1;
	}
	if (gThImageMng == NULL) {
		// 獲得されていない
//		LOGD("ThumbnailSetNone : Memory Not Alloced");
		return -2;
	}

//	LOGD("ThumbnailSetNone : index=%d, blockindex=%d->-2", index, gThImageMng[index].BlockIndex);
	gThImageMng[index].BlockIndex = -2;
	return 0;
}

int ThumbnailCheckAll(long long id)
{
	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailCheckAll : Illegal thumbnail ID(id=%lld)", id);
		return -1;
	}
	if (gThImageMng == NULL) {
		// 獲得されていない
//		LOGD("ThumbnailCheckAll : Memory Not Alloced");
		return -2;
	}

	for (int i = 0 ; i < gThImageNum ; i ++) {
		if (gThImageMng[i].BlockIndex == -1) {
//			LOGD("ThumbnailCheckAll : imageindex=%d, blockindex=%d", i, gThImageMng[i].BlockIndex);
			return 1;
		}
	}
	return 0;
}

int ThumbnailSizeCheck(long long id, int bmp_width, int bmp_height)
{
#ifdef DEBUG
	LOGD("ThumbnailSizeCheck : id=%lld, width=%d, height=%d", id, bmp_width, bmp_height);
#endif
	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailSizeCheck : Illegal thumbnail ID(id=%lld)", id);
		return -1;
	}
	if (gThPageBuff == NULL) {
		// 獲得されていない
//		LOGD("ThumbnailSizeCheck : Memory Not Alloced");
		return -2;
	}

	// 何ブロック必要か
	int lines = THUMB_BLOCKSIZE / (bmp_width * sizeof(WORD));
	int blocks = (bmp_height + (lines - 1)) / lines;

//	LOGD("ThumbnailSizeCheck : blocknum=%d, usenum=%d, chknum=%d, lines=%d", gThBlockNum, gThUseBlockNum, blocks, lines);
	if (gThBlockNum - gThUseBlockNum < blocks) {
		return blocks;
	}
	return 0;
}

int ThumbnailImageAlloc(long long id, int blocks, int index)
{
#ifdef DEBUG
	LOGD("ThumbnailImageAlloc : id = %lld, blocks=%d, index=%d", id, blocks, index);
#endif
	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailImageAlloc : Illegal thumbnail ID(id=%lld)", id);
		return -1;
	}
	if (gThPageBuff == NULL) {
		// 獲得されていない
//		LOGD("ThumbnailImageAlloc : Pages Not Alloced");
		return -2;
	}
	if (index < 0 || index >= gThImageNum) {
//		LOGD("ThumbnailImageAlloc : Out of range.");
		return -3;
	}

	// 必要なロック数
	int stidx = 0;
	int edidx = gThImageNum - 1;
	int cridx;

	while (stidx <= index && edidx >= index) {
		if (index - stidx > edidx - index) {
			cridx = stidx;
			stidx ++;
		}
		else {
			cridx = edidx;
			edidx --;
		}
		if (gThImageMng[cridx].BlockIndex >= 0) {
			// 画像データを解放
			ThumbnailImageFree(cridx);
		}

		if (gThBlockNum - gThUseBlockNum >= blocks) {
			// 必要なブロック以上に空いていたら終了
//			LOGD("ThumbnailImageAlloc : Alloc success.");
			return 0;
		}
	}
#ifdef DEBUG
	LOGD("ThumbnailImageAlloc : Alloc failure.(%d/%d)", gThBlockNum, gThUseBlockNum);
#endif
	return 1;
}

// 画像データの使用を解除
void ThumbnailImageFree(int index) {
#ifdef DEBUG
	LOGD("ThumbnailImageFree : gThImageMng[%d].BlockIndex=%d", index, gThImageMng[index].BlockIndex);
#endif
	// 画像管理からの参照を解除

//	LOGD("ThumbnailImageFree : Mutex Lock/Pre");
	pthread_mutex_lock(&gThMutex);
//	LOGD("ThumbnailImageFree : Mutex Lock/Aft");
	
	int blockindex = gThImageMng[index].BlockIndex;
	gThImageMng[index].BlockIndex = -1;
	while (blockindex >= 0) {
//		LOGD("ThumbnailImageFree : gThBlockMng[%d]=%d", blockindex, gThBlockMng[blockindex]);

		// 参照先のブロックをクリア
		int nextindex = gThBlockMng[blockindex];
		gThBlockMng[blockindex] = -1;
		blockindex = nextindex;

		// 使用数を減らす
		gThUseBlockNum --;
	}
//	LOGD("ThumbnailImageFree : Mutex Unlock/Pre");
	pthread_mutex_unlock(&gThMutex);
//	LOGD("ThumbnailImageFree : Mutex Unlock/Aft");
//	CountLog("free");
	return;
}

int ThumbnailSave(long long id, int index, int bmp_width, int bmp_height, int bmp_stride, BYTE* canvas)
{
	int ret = 0;

	int linenum;
	int blocknum;
	int count;
	int prev;
	int firstindex;

	// メモリにコピー
	BYTE	*pos;
	BYTE	*buff;
	int		nextindex;
	int		linecount;
	int		linesize;

//	LOGD("ThumbnailSave : Mutex Lock/Pre");
	pthread_mutex_lock(&gThMutex);
//	LOGD("ThumbnailSave : Mutex Lock/Aft");

	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailSave : Illegal thumbnail ID(id=%lld)", id);
		ret = -1;
		goto ERROREND;
	}
	if (gThPageBuff == NULL || gThImageMng == NULL) {
		// 獲得されていない
//		LOGD("ThumbnailSave : Pages Not Alloced");
		ret = -2;
		goto ERROREND;
	}
	if (index < 0 || index >= gThImageNum) {
//		LOGD("ThumbnailSave : Out of range(index=%d)", index);
		ret = -3;
		goto ERROREND;
	}

	// 領域獲得
	gThImageMng[index].Width = bmp_width;
	gThImageMng[index].Height = bmp_height;

	// ブロックに何行入るか
	linenum = THUMB_BLOCKSIZE / (bmp_width * sizeof(WORD));
	// 何ブロック必要か
	blocknum = (bmp_height + (linenum - 1)) / linenum;
	count = 0;
	prev = -1;
	firstindex = -1;

//	LOGD("ThumbnailSave : index=%d, width=%d, height=%d, stride=%d, blocknum=%d", index, bmp_width, bmp_height, bmp_stride, blocknum);

	for (int i = 0 ; i < gThBlockNum ; i ++) {
		if (gThBlockMng[i] == -1) {
			if (count == 0) {
				// 最初に見つけたものはイメージ管理へ設定
				firstindex = i;
//				LOGD("ThumbnailSave : Found:gThImageMng[%d].Block<-%d, blocknum=%d, count=%d", index, i, blocknum, count);
			}
			else {
				// 2つめ以降はブロック管理へ設定
				gThBlockMng[prev] = i;
//				LOGD("ThumbnailSave : Found:gThBlockMng[%d]<-%d, blocknum=%d, count=%d", prev, i, blocknum, count);
			}
			count ++;
			gThUseBlockNum ++;
			if (count >= blocknum) {
				// 必要な数を見つけた
				gThBlockMng[i] = -2;	// 終端設定
				break;
			}
			// 次を設定するため保持しておく
			prev = i;
		}
	}

	// メモリにコピー
	pos = canvas;
	buff = NULL;
	nextindex = firstindex;
	linecount = linenum;
	linesize = bmp_width * sizeof(WORD);

	for (int i = 0 ; i < bmp_height ; i ++) {
		if (linecount >= linenum) {
//			LOGD("ThumbnailSave : next buffptr nextindex=%d, pageblocknum=%d, linecount=%d, linenum=%d", nextindex, gThPageBlockNum, linecount, linenum);
			if (nextindex < 0) {
//				LOGD("ThumbnailSave : Illegal Index(nextindex=%d)", nextindex);
			}
			int pageindex = nextindex / gThPageBlockNum;
			int blockptr = (nextindex % gThPageBlockNum) * THUMB_BLOCKSIZE;
			buff = gThPageBuff[pageindex] + blockptr;
			nextindex = gThBlockMng[nextindex];
			linecount = 0;
		}
		memcpy (buff, pos, linesize);
		pos += bmp_stride;
		buff += linesize;
		linecount ++;
	}

	// 管理領域に設定
	gThImageMng[index].BlockIndex = firstindex;
//	CountLog("alloc");

ERROREND:
//	LOGD("ThumbnailSave : Mutex Unlock/Pre");
	pthread_mutex_unlock( &gThMutex );
//	LOGD("ThumbnailSave : Mutex Unlock/Aft");
	return ret;
}

int ThumbnailDraw(long long id, int index, int bmp_width, int bmp_height, int bmp_stride, BYTE* canvas)
{
	int ret = 0;

	int		width;
	int		height;
	int		linenum;
	BYTE	*pos;
	BYTE	*buff;
	int		nextindex;
	int		linecount;
	int		linesize;

//	LOGD("ThumbnailDraw : Mutex Lock/Pre");
	pthread_mutex_lock(&gThMutex);
//	LOGD("ThumbnailDraw : Mutex Lock/Aft");

	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
		ret = -1;
		goto ERROREND;
	}
	if (gThPageBuff == NULL || gThImageMng == NULL) {
		// 獲得されていない
		ret = -2;
		goto ERROREND;
	}
	if (index < 0 || index >= gThImageNum) {
		ret = -3;
		goto ERROREND;
	}
	// 保存されていなければ終了
	else if (gThImageMng[index].BlockIndex < 0) {
		ret = -4;
		goto ERROREND;
	}

	// 保存されている画像のサイズ
	width = gThImageMng[index].Width;
	height = gThImageMng[index].Height;

	// ブロックに何行入るか
	linenum = THUMB_BLOCKSIZE / (width * sizeof(WORD));

//	LOGD("ThumbnailDraw : idx=%d, w=%d, h=%d, iw=%d, ih=%d, st=%d", index, width, height, bmp_width, bmp_height, bmp_stride);

	// メモリからBitmapにコピー
	pos = canvas;
	buff = NULL;
	nextindex = gThImageMng[index].BlockIndex;
	linecount = linenum;
	linesize = width * sizeof(WORD);

	for (int i = 0 ; i < bmp_height && i < height ; i ++) {
		if (linecount >= linenum) {
//			LOGD("ThumbnailDraw : next buffptr nextindex=%d, blocknum=%d, linecount=%d, linenum=%d", nextindex, gThPageBlockNum, linecount, linenum);
			if (nextindex < 0) {
				LOGD("ThumbnailDraw : Illegal Index(nextindex=%d)", nextindex);
			}
			int pageindex = nextindex / gThPageBlockNum;
			int blockptr = (nextindex % gThPageBlockNum) * THUMB_BLOCKSIZE;
			buff = gThPageBuff[pageindex] + blockptr;
			nextindex = gThBlockMng[nextindex];
			linecount = 0;
		}
		memcpy (pos, buff, linesize);
		pos += bmp_stride;
		buff += linesize;
		linecount ++;
	}

	ret = (width << 16 | height);

ERROREND:
//	LOGD("ThumbnailDraw : Mutex Unlock/Pre");
	pthread_mutex_unlock( &gThMutex );
//	LOGD("ThumbnailDraw : Mutex Unlock/Aft");
	return ret;
}

void ThumbnailFree(long long id)
{
//	LOGD("ThumbnailFree : Mutex Lock/Pre");
	pthread_mutex_lock(&gThMutex);
//	LOGD("ThumbnailFree : Mutex Lock/Aft");

	// IDの一致チェック
	if (gThumbnailId != id) {
		// 初期化したIDと異なる
//		LOGD("ThumbnailFree : Illegal thumbnail ID(%lld, id=%lld)", gThumbnailId, id);
		goto ERROREND;
	}

	// ページバッファの解放
	if (gThPageBuff != NULL) {
		LOGD("ThumbnailFree : gThPageNum=%d", gThPageNum);
		for (int i = 0 ; i < gThPageNum ; i ++) {
			if (gThPageBuff[i] != NULL ) {
				LOGD("ThumbnailFree : gThPageBuff[%d] free", i);
				free (gThPageBuff[i]);
			}
		}	

		// アドレス保存配列解放
		LOGD("ThumbnailFree : gThumbnailPages free");
		free (gThPageBuff);
		gThPageBuff = NULL;
	}

	// ブロック管理の解放
	if (gThBlockMng != NULL) {
		LOGD("ThumbnailFree : gThBlockMng free");
		free (gThBlockMng);
		gThBlockMng = NULL;
	}

	gThPageSize = 0;
	gThPageNum = 0;
	gThPageBlockNum = 0;
	gThBlockNum = 0;
	gThUseBlockNum = 0;

	// サムネイル解放
	if (gThImageMng != NULL) {
		LOGD("ThumbnailFree : gThumbnailMng != NULL");
		// 解放
		free (gThImageMng);
		gThImageMng = NULL;
	}
	gThImageNum = 0;

//	if (gThMutexInit) {
		// mutex 開放
//		pthread_mutex_destroy(&gThMutex);
//		gThMutexInit = false;
//	}

#ifdef DEBUG
	LOGD("ThumbnailFree : End");
#endif

ERROREND:
//	LOGD("ThumbnailFree : Mutex Unlock/Pre");
	pthread_mutex_unlock( &gThMutex );
//	LOGD("ThumbnailFree : Mutex Unlock/Aft");
	return;
}

//// 空き領域の数を数えてログ出力
//void CountLog(const char *label)
//{
//	// カウンタ
//	int imfree = 0;
//	int imnone = 0;
//	int imuse = 0;
//
//	int use = 0;
//	int free = 0;
//	int term = 0;
//
//	for (int i = 0 ; i < gThImageNum ; i ++) {
//		if (gThImageMng[i].BlockIndex == -1) {
//			imfree ++;
//		}
//		else if (gThImageMng[i].BlockIndex == -2) {
//			imnone ++;
//		}
//		else {
//			imuse ++;
//		}
//	}
//
//	for (int i = 0 ; i < gThBlockNum ; i ++) {
//		if (gThBlockMng[i] == -1) {
//			free ++;
//		}
//		else {
//			use ++;
//			if (gThBlockMng[i] == -2) {
//				term ++;
//			}
//		}
//	}
//	LOGD("ThumbnailCount:%s-image(u=%d,n=%d,f=%d)/block(u=%d,f=%d,t=%d/u=%d,a=%d)", label, imuse, imnone, imfree, use, free, term, gThUseBlockNum, gThBlockNum);
//	return;
//}
