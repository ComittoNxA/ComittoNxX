package src.comitton.stream;

import android.graphics.Bitmap;

public class CallImgLibrary {
	static {
		// JNIライブラリのロード
		System.loadLibrary("comitton");
	}
	public static final int RESULT_OK = 0;
	public static final int RESULT_ALLOC_ERR = 1;
	
	public static native int ImageInitialize(long loadsize, int buffnum, int totalpage, int maxthreadnum);
	public static native void ImageTerminate();
	public static native int ImageGetFreeSize();
//	public static native int ImageSave(int page, Bitmap bitmap); // 保存してハンドルを返す
//	public static native int ImageRestore(int page, Bitmap bitmap, int width, int height); // ハンドルのデータをセット
	public static native int ImageFree(int page);
	public static native int ImageScaleFree(int page, int half);

	public static native int ImageSetPage(int page, long size);
	public static native int ImageSetData(byte data[], int size);
	public static native int ImageSetFileSize(long size);
	public static native int ImageGetSize(int type, int imagesize[]);
	public static native int ImageSetBitmap(Bitmap bitmap);
	public static native int ImageConvert(int type, int scale);
	public static native int ImageGetBitmap(int type, int scale, Bitmap bitmap);

	public static native int GetMarginSize(int Page, int Half, int Index, int SclWidth, int SclHeight, int Margin, int MarginColor, int size[]);
	public static native int ImageScale(int page, int half, int width, int height, int left, int right, int top, int bottom, int algorithm, int rotate, int margin, int margincolor, int bright, int gamma, int param, int size[]);
	public static native int ImageDraw(int page, int half, int x, int y, Bitmap bitmap);
	public static native int ImageScaleDraw(int page, int rotate, int sx, int sy, int scx, int scy, int dx, int dy, int dcx, int dcy, int psel, Bitmap bm, int cutLeft, int cutRight, int cutTop, int cutBottom);
	public static native int ImageCancel(int flag);

	public static native int ThumbnailInitialize(long id, int pagesize, int pagenum, int imageNum);
	public static native int ThumbnailCheck(long id, int index);
	public static native int ThumbnailSetNone(long id, int index);
	public static native int ThumbnailCheckAll(long id);
	public static native int ThumbnailSizeCheck(long id, int width, int height);
	public static native int ThumbnailImageAlloc(long id, int blocks, int index);
	public static native int ThumbnailSave(long id, Bitmap bitmap, int index);
	public static native int ThumbnailDraw(long id, Bitmap bitmap, int index);
	public static native int ThumbnailFree(long id);

	// スレッド数設定
	public static native int SetParameter(int threadnum);

	public static int ImageScaleParam(int sharpen, int invert, int gray, int coloring, int moire, int pseland) {
		int val = (sharpen != 0 ? 1 : 0)
				+ (invert != 0 ? 2 : 0)
				+ (gray != 0 ? 4 : 0)
				+ (coloring != 0 ? 8 : 0)
				+ (moire != 0 ? 16 : 0)
				+ (pseland != 0 ? 32 : 0);
		return val;
	}
}
