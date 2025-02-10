//#define DEBUG
#include <jni.h>
#include "common.h"

#define  LOG_TAG    "comitton_img"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define MAX_BUFFER_INDEX 20

#define IMAGETYPE_NONE 0
#define IMAGETYPE_PDF 3;
#define IMAGETYPE_JPEG 4
#define IMAGETYPE_PNG 5
#define IMAGETYPE_GIF 6
#define IMAGETYPE_TXT 7
#define IMAGETYPE_WEBP 51
#define IMAGETYPE_AVIF 52
#define IMAGETYPE_HEIF 53
#define IMAGETYPE_JXL 54

#define MAKE565(red, green, blue) (((red<<8) & 0xf800) | ((green<<3) & 0x07e0) | ((blue >> 3) & 0x001f))
#define MAKE555(red, green, blue) (((red<<8) & 0xf800) | ((green<<3) & 0x07c0) | ((blue >> 3) & 0x001f))
#define RGB565_RED_256(rgb) ((rgb>>8) & 0x00F8)
#define RGB565_GREEN_256(rgb) ((rgb>>3) & 0x00FC)
#define RGB565_BLUE_256(rgb) ((rgb<<3) & 0x00F8)

#define RGB555_GREEN_256(rgb) ((rgb>>3) & 0x00F8)

#define REMAKE565(red, green, blue) (((red<<11) & 0xf800) | ((green<<5) & 0x07e0) | (blue & 0x001f))
#define RGB565_RED(rgb) ((rgb>>11) & 0x001F)
#define RGB565_GREEN(rgb) ((rgb>>5) & 0x003F)
#define RGB565_BLUE(rgb) (rgb & 0x001F)

#define WHITE_CHECK(rgb, mask) ((rgb & mask) == mask)
#define BLACK_CHECK(rgb, mask) ((rgb & mask) == 0x0000)

#define COLOR_CHECK(rgb1, rgb2, mask) \
( \
    ( \
        REMAKE565( \
            std::abs(RGB565_RED(rgb1) - RGB565_RED(rgb2)) , \
            std::abs(RGB565_GREEN(rgb1) - RGB565_GREEN(rgb2)) , \
            std::abs(RGB565_BLUE(rgb1) - RGB565_BLUE(rgb2))) \
        & mask \
    ) \
    == 0x0000 \
)

//#define RED_RANGE(rr) (rr < 0 ? 0 : (rr > 0x001F ? 0x001F : rr))
//#define GREEN_RANGE(gg) (gg < 0 ? 0 : (gg > 0x003F ? 0x003F : gg))
//#define BLUE_RANGE(bb) (bb < 0 ? 0 : (bb > 0x001F ? 0x001F : bb))
#define LIMIT_RGB(color) ((color)<0x00?0x00:((color)>0xff?0xff:(color)))

#define ROUNDUP_DIV(v,d)	(v / d + (v % d != 0 ? 1 : 0))

#define HOKAN_DOTS	4
#define SCLBUFFNUM	500

#define	MAX_LINES	6400
#define	MAX_COLUMNS	6400
#define	BLOCKSIZE	(128 * 1024)

#define SCALE_BORDER1	0.5
#define SCALE_BORDER2	0.8

typedef	unsigned short	WORD;
typedef	unsigned char	BYTE;


typedef enum {
    COLOR_FORMAT_RGB = 0,
    COLOR_FORMAT_RGBA,
    COLOR_FORMAT_ARGB,
    COLOR_FORMAT_BGR,
    COLOR_FORMAT_BGRA,
    COLOR_FORMAT_ABGR,
    COLOR_FORMAT_RGB565,
    COLOR_FORMAT_GRAYSCALE
} colorFormat;

typedef enum {
    SET_BUFFER = 0,
    SET_BITMAP
} loadCommand;

typedef struct imagedata {
	short		UseFlag;
	int		OrgWidth;
	int		OrgHeight;
	short		SclFlag[3];
	int		SclWidth[3];
	int		SclHeight[3];
} IMAGEDATA;

typedef struct buff_manage {
	short		Page;
	char		Type;
	char		Half;
	long		Size;
    /** Scale変更をした回数 */
	long		Count;
	WORD		*Buff;
} BUFFMNG;

#define TYPE_ORIGINAL	1
#define TYPE_SCALING	2

#define QUALITY_LOW		0
#define QUALITY_HIGH	1

#define BMPMARGIN_NONE      0
#define BMPMARGIN_WEAK      1
#define BMPMARGIN_MEDIUM    2
#define BMPMARGIN_STRONG    3
#define BMPMARGIN_SPECIAL   4
#define BMPMARGIN_OVERKILL  5
#define BMPMARGIN_IGNORE_ASPECT_RATIO  6

#define MARGIN_COLOR_WHITE_AND_BLACK	0
#define MARGIN_COLOR_ALL_COLORS     	1

#define PARAM_SHARPEN	0x0001
#define PARAM_INVERT	0x0002
#define PARAM_GRAY		0x0004
#define PARAM_COLORING	0x0008
#define PARAM_MOIRE		0x0010
#define PARAM_PSELAND	0x0020

int ThumbnailAlloc(long long, int, int, int);
int ThumbnailSetNone(long long, int);
int ThumbnailCheck(long long, int);
int ThumbnailCheckAll(long long);
int ThumbnailMemorySizeCheck(long long, int, int);
int ThumbnailImageAlloc(long long, int, int);
int ThumbnailSave(long long, int, int, int, int, BYTE*);
int ThumbnailImageSize(long long, int);
int ThumbnailDraw(long long, int, int, int, int, BYTE*);
void ThumbnailFree(long long);

void CheckImageType(int, int *);
int SetBuff(int, int, uint32_t, uint32_t, uint8_t*, colorFormat);
int SetBitmap(int, int, uint32_t, uint32_t, uint8_t*, colorFormat, WORD *);
int ReleaseBuff(int, int, int, int);
int MemAlloc(int, int);
void MemFree(int);
int ScaleMemLine(int, int);
int ScaleMemColumn(int, int);
void ScaleMemLineFree(int);
void ScaleMemColumnFree(int);

int ScaleMemInit(int);
int ScaleMemAlloc(int, int, int);

int DrawScaleBitmap(int, int, int, int, int, int, int, int, int, int, int, void *, int, int, int, int, IMAGEDATA *, int, int, int, int);
int DrawBitmap(int, int, int half, int x, int y, void *, int, int, int, IMAGEDATA *);

int CreateScale(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, jint*);

int SetLinesPtr(int, int, int, int, int, int);
int NextSclBuff(int, int, int, int, int*, int*, int);
int EraseSclBuffMng(int, int);
int CopySclBuffMngToBuffMng(int);
int RefreshSclLinesPtr(int, int, int, int, int, int);

int ImageRotate(int, int, int, int, int, int, int);
int GetMarginSize(int, int, int, int, int, int, int*, int*, int*, int*);
int ImageMarginCut(int, int, int, int, int, int, int, int, int, int, int*, int*);
int ImageHalf(int, int, int, int, int, int);
int ImageSharpen(int, int, int, int, int, int, int);
int ImageBlur(int, int, int, int, int, int, int);
int ImageInvert(int, int, int, int, int, int);
int ImageGray(int, int, int, int, int, int);
int ImageBright(int, int, int, int, int, int, int, int);

int CreateScaleNear(int, int, int, int, int, int, int, int);
int CreateScaleLinear(int, int, int, int, int, int, int, int);
int CreateScaleCubic(int, int, int, int, int, int, int, int);
int CreateScaleHalf(int, int, int, int, int, int);

int ImageGetSizeAvif(int, int, int *, int *);
int LoadImageAvif(int, int, IMAGEDATA *, int, int, WORD *);

int ImageGetSizeJxl(int, int, int *, int *);
int LoadImageJxl(int, int, IMAGEDATA *, int, int, WORD *);
