package src.comitton.stream;

import android.graphics.Bitmap;

public class CallTxtLibrary {
	static {
		// JNIライブラリのロード
		System.loadLibrary("comitton");
	}

	public static final int MAX_CACHE_PAGES = 5;

	public static native int SetTextImage(Bitmap bitmap, int page, int currnetPage);
	public static native int GetTextImage(Bitmap bitmap, int page);
//	public static native int DrawTextImage(Bitmap bitmap, int page, int x, int y);
	public static native int CheckTextImage(int page);
	public static native int FreeTextImage();
}
