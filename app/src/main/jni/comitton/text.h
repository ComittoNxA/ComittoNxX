#define  LOG_TAG    "comitton_txt"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define MAX_TEXTPAGE	5
typedef	unsigned short	WORD;
typedef	unsigned char	BYTE;

int TextImagesAlloc(int);
int TextImageGetFree(int);
int TextImageFindPage(int);
void TextImagesFree(void);
