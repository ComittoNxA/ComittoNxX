#include <malloc.h>
#include <string.h>
#include <math.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "Image.h"

extern IMAGEDATA	*gImageData[];
extern LONG			**gLinesPtr[];

extern BUFFMNG		*gBuffMng[];
extern long			gBuffNum[];

extern BUFFMNG		*gSclBuffMng[];
extern long			gSclBuffNum[];

extern int			gCancel[];

int ImageHalf(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight)
{
	int ret = 0;

	int buffindex;
	int buffpos = 0;
	int linesize;
	LONG *buffptr = nullptr;

	// 使用するバッファを保持
	int StartX;
	int HalfWidth;

	LONG *orgbuff1;

	int		xx;	// サイズ変更後のx座標
	int		yy;	// サイズ変更後のy座標

	if (Half == 0) {
		return -1;
	}

	// 左右分割する
	StartX = 0;
	HalfWidth  = (OrgWidth + 1) / 2;
	if (Half == 2) {
		// 右側
		StartX = OrgWidth / 2;
	}

	linesize  = HalfWidth + HOKAN_DOTS;

	//  サイズ変更画像待避用領域確保
    ret = ScaleMemAlloc(index, linesize, OrgHeight);
	if (ret < 0) {
		return ret;
	}

	buffindex = -1;
	buffpos   = 0;

//	LOGD("ImageHalf : half=%d, sx=%d, hw=%d, ow=%d, oh=%d", Half, StartX, HalfWidth, OrgWidth, OrgHeight);

	for (yy = 0; yy < OrgHeight ; yy ++) {
		if (gCancel[index]) {
//			LOGD("ImageRotate : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return ERROR_CODE_USER_CANCELED;
		}

		orgbuff1 = gLinesPtr[index][yy + HOKAN_DOTS / 2];

		ret = NextSclBuff(index, Page, Half, Count, &buffindex, &buffpos, linesize);
		if (ret < 0) {
			return ret;
		}

		// バッファ位置
		buffptr = gSclBuffMng[index][buffindex].Buff + buffpos + HOKAN_DOTS / 2;
//		LOGD("ImageHalf : buffindex=%d, buffpos=%d, linesize=%d", buffindex, buffpos, linesize);

		for (xx = 0 ; xx < HalfWidth ; xx ++) {
			// 元座標
			buffptr[xx] = orgbuff1[xx + StartX + HOKAN_DOTS / 2];
		}

		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[HalfWidth + 0] = buffptr[HalfWidth - 1];
		buffptr[HalfWidth + 1] = buffptr[HalfWidth - 1];

		buffpos += linesize;
		gSclBuffMng[index][buffindex].Size += linesize;
	}
	return 0;
}