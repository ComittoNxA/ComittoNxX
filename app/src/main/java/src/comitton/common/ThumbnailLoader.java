package src.comitton.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import src.comitton.config.SetImageTextDetailActivity;
import src.comitton.config.SetTextActivity;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.jni.CallImgLibrary;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class ThumbnailLoader {
	private static final String TAG = "ThumbnailLoader";

	protected AppCompatActivity mActivity;
	protected Handler mHandler;
	protected String mURI;
	protected String mPath;
	protected String mUriPath;
	protected ArrayList<FileData> mFiles;
	protected String mCachePath;
	protected boolean mThreadBreak;
	protected final Object mFileListLock;

	protected long mID;
	protected int mThumbSizeW;
	protected int mThumbSizeH;
	protected int mFirstIndex;
	protected int mLastIndex;

	protected int mThumbCacheNum;
	protected int mThumbCrop;
	protected int mThumbMargin;

	protected boolean mOut_of_memory = false;

	// 以下、テキストマネージャー用の変数
	protected static float mDensity;
	protected static int mHeadSizeOrg;
	protected static int mBodySizeOrg;
	protected static int mRubiSizeOrg;
	protected static int mInfoSizeOrg;
	protected static int mMarginWOrg;
	protected static int mMarginHOrg;

	protected static int mPaperSel;
	protected static boolean mNotice;
	protected static boolean mImmEnable;
	protected static int mViewRota;
	protected static int mTextWidth;
	protected static int mTextHeight;
	protected static int mHeadSize;
	protected static int mBodySize;
	protected static int mRubiSize;
	protected static int mInfoSize;
	protected static int mPicSize;
	protected static int mSpaceW;
	protected static int mSpaceH;
	protected static int mMarginW;
	protected static int mMarginH;

	protected static int mAscMode;	// 半角の表示方法
	protected static String mFontFile;
	// ここまで、テキストマネージャー用の変数

	public ThumbnailLoader(AppCompatActivity activity, String uri, String path, Handler handler, long id, ArrayList<FileData> files, int sizeW, int sizeH, int cachenum, int crop, int margin) {
		super();
		int logLevel = Logcat.LOG_LEVEL_WARN;

		mActivity = activity;
		mHandler = handler;
		mThreadBreak = false;
		mThumbSizeW = sizeW;
		mThumbSizeH = sizeH;
		mURI = uri;
		mPath = path;
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		mID = id;
		mFirstIndex = 0;
		if (files != null) {
			mLastIndex = files.size() - 1;
		}
		else {
			mLastIndex = 0;
		}
		mThumbCacheNum = cachenum;
		mThumbCrop = crop;
		mThumbMargin = margin;

		mFileListLock = new Object();
		mFiles = new ArrayList<>(files);

		// キャッシュフォルダ
		mCachePath = DEF.getBaseDirectory() + "thumb/";
		try {
			new File(mCachePath).mkdirs();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
			mCachePath = null;
		}

	}

	// 解放
	protected void releaseThumbnail() {
		// サムネイル画像データ解放
		CallImgLibrary.ThumbnailFree(mID);
		interruptThread();
	}

	// スレッド停止
	protected void breakThread() {
		mThreadBreak = true;
		interruptThread();
	}

	//path1のサムネイルをpath2のキャッシュとして割り当て
	public void setThumbnailCache(String path1, String path2) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// 他のパスのキャッシュとして保存
		String pathcode = DEF.makeCode(path1, mThumbSizeW, mThumbSizeH);
		Bitmap bm = loadThumbnailCache(pathcode);
		if (bm != null) {
			try {
				deleteThumbnailCache(path2, mThumbSizeW, mThumbSizeH);//キャッシュが有れば削除して
				String pathcode2 = DEF.makeCode(path2, mThumbSizeW, mThumbSizeH);
				String cacheFile = getThumbnailCacheName(pathcode2);
				FileOutputStream cacheSave = new FileOutputStream(cacheFile);
				bm.compress(CompressFormat.JPEG, 80, cacheSave);
				cacheSave.flush();
				cacheSave.close();
			} catch (Exception e) {
				Logcat.e(logLevel, "", e);
			}
		}
	}

	//特定のパスにサムネイルキャッシュを割り当てる
	public void setThumbnailCache(String path, Bitmap bm) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String pathcode = DEF.makeCode(path, mThumbSizeW, mThumbSizeH);
		String cacheFile = getThumbnailCacheName(pathcode);
		if (bm != null) {
			try {
				FileOutputStream cacheSave = new FileOutputStream(cacheFile);
				bm.compress(CompressFormat.JPEG, 80, cacheSave);
				cacheSave.flush();
				cacheSave.close();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "",  e);
			}
		}
	}

	public void saveThumbnailCache(String pathcode, Bitmap bm) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// キャッシュとして保存
		String cacheFile = getThumbnailCacheName(pathcode);
		File file = new File(cacheFile);

		if (file.exists()) {
			// 更新日付を現在時刻に
			file.setLastModified(System.currentTimeMillis());
		}
		else {
			try {
				FileOutputStream cacheSave = new FileOutputStream(cacheFile);
				bm.compress(CompressFormat.JPEG, 80, cacheSave);
				cacheSave.flush();
				cacheSave.close();
			}
			catch (Exception e) {
				Logcat.e(logLevel, "", e);
			}
		}
	}

	// キャッシュファイル名
	protected String getThumbnailCacheName(String pathcode) {
		return mCachePath + pathcode + ".cache";
	}

	// キャッシュファイルから読み込み
	protected Bitmap loadThumbnailCache(String pathcode) {
		String cacheName = getThumbnailCacheName(pathcode);
        return BitmapFactory.decodeFile(cacheName);
	}

	// キャッシュファイル存在チェック
	protected boolean checkThumbnailCache(String pathcode) {
		String cacheName = getThumbnailCacheName(pathcode);
		File cacheFile = new File(cacheName);
		return cacheFile.exists();
	}

	/**
	 * サムネイルのキャッシュサイズが保存上限数を超えていたら削除する
	 * @param leave 保存上限数（-1を指定したときは削除しない）
	 */
	public static void deleteThumbnailCache(int leave) {
		if (leave < 0) {
			// -1を指定の場合は削除しない
			return;
		}

		// キャッシュ保存先
		String path = DEF.getBaseDirectory() + "thumb/";

		File[] files = new File(path).listFiles();
		if (files == null) {
			// ファイルなし
			return;
		}
		ArrayList<File> fileArray = new ArrayList<File>();
		for (File file : files) {
			if (!file.isDirectory()) {
				String name = file.getName();
				if (name.equals("comittona.cache")) {
					// 削除対象外
					continue;
				}
				if (!name.toLowerCase().endsWith(".cache")) {
					// 削除対象外
					continue;
				}
				fileArray.add(file);
			}
		}

		long nowtime = System.currentTimeMillis();
		if (fileArray.size() > leave) {
			// 新しい順に並べる
			Collections.sort(fileArray, new ThumbnailLoader.TimeSortComparator());
			for (int i = fileArray.size() - 1; i >= leave; i--) {
				File work = fileArray.get(i);
				long filetime = work.lastModified();
				if (leave > 0 && filetime >= nowtime - DEF.MILLIS_DELETECHE) {
					// 削除猶予期間
					break;
				}
				work.delete();
			}
		}
	}

	public static void deleteThumbnailCache(String filepath, int thum_cx, int thum_cy) {
		// キャッシュ保存先
		String path = DEF.getBaseDirectory() + "thumb/";
		String pathcode = DEF.makeCode(filepath, thum_cx, thum_cy);

		// キャッシュファイルを削除
		File file = new File(path + pathcode + ".cache");
		if (file.exists()) {
			file.delete();
		}
	}

	public static void renameThumbnailCache(String fromfilepath, String tofilepath, int thum_cx, int thum_cy) {
		// キャッシュ保存先
		String path = DEF.getBaseDirectory() + "thumb/";
		String frompathcode = DEF.makeCode(fromfilepath, thum_cx, thum_cy);
		String topathcode = DEF.makeCode(tofilepath, thum_cx, thum_cy);

		// キャッシュファイルの名前を変更
		File fromfile = new File(path + frompathcode + ".cache");
		File tofile = new File(path + topathcode + ".cache");
		fromfile.renameTo(tofile);
	}

	public static class TimeSortComparator implements Comparator<File> {
		public int compare(File file1, File file2) {
			// 日時比較
			long t1 = file1.lastModified();
			long t2 = file2.lastModified();

			// IMAGEとZIPのソート優先度は同じにする
			if (t1 == t2) {
				return 0;
			}
			else if (t1 > t2) {
				return -1;
			}
			return 1;
		}
	}

	// スクロール位置でソート
	public static class FileRangeComparator implements Comparator<FileData> {

		private static int mFirstIndex = -1;
		private static int mLastIndex = -1;
		private static int mCenter = -1;

		public int compare(FileData file1, FileData file2) {

			if (file2 == null) {
				return -1;
			}
			if (file1 == null) {
				return 1;
			}

			// 表示の中心からの距離が近い順
			int distance1 = Math.abs(file1.getIndex() - mCenter);
			int distance2 = Math.abs(file2.getIndex() - mCenter);

			return distance1 - distance2;
		}

		// 表示中の範囲を設定
		public void setDispRange(int firstindex, int lastindex) {
			mFirstIndex = firstindex;
			mLastIndex = lastindex;
			mCenter = (mLastIndex + mFirstIndex) / 2 ;
		}
	}

	// ファイル種別とスクロール位置でソート
	public static class FileTypeSortComparator implements Comparator<FileData> {

		private static int mFirstIndex = 0;
		private static int mLastIndex = 1;
		private static int mRange = 1;

		public int compare(FileData file1, FileData file2) {

			if (file2 == null) {
				return -1;
			}
			if (file1 == null) {
				return 1;
			}

			// 表示の中心からの距離が近い順
			int distance1 = 0;
			int distance2 = 0;

			if (file1.getIndex() < mFirstIndex) {
				distance1 = (int) Math.ceil(((double) mFirstIndex - file1.getIndex()) / mRange);
			}
			else if (mLastIndex < file1.getIndex()){
				distance1 = (int) Math.ceil(((double) file1.getIndex() - mLastIndex) / mRange);
			}

			if (file2.getIndex() < mFirstIndex) {
				distance2 = (int) Math.ceil(((double) mFirstIndex - file2.getIndex()) / mRange);
			}
			else if (mLastIndex < file2.getIndex()){
				distance2 = (int) Math.ceil(((double) file2.getIndex() - mLastIndex) / mRange);
			}

			if (distance1 != distance2) {
				return distance1 - distance2;
			}

			// ファイルタイプの単純な順
			int type1 = getType(file1);
			int type2 = getType(file2);

			if (type1 != type2) {
				return type1 - type2;
			}

			// ファイルサイズの小さい順
			return (int)(file1.getSize() - file2.getSize());
		}

		// 表示中の範囲を設定
		public void setDispRange(int firstindex, int lastindex) {
			mFirstIndex = firstindex;
			mLastIndex = lastindex;
			mRange = mLastIndex - mFirstIndex;
		}

		private int getType(FileData file) {
			switch (file.getType()) {
				case FileData.FILETYPE_IMG:
					return 1;
				case FileData.FILETYPE_PDF:
					return 2;
				case FileData.FILETYPE_ARC:
					switch (file.getExtType()) {
						case FileData.EXTTYPE_ZIP:
							return 3;
						case FileData.EXTTYPE_RAR:
						default:
							return 6;
					}
				case FileData.FILETYPE_EPUB:
					return 4;
				case FileData.FILETYPE_DIR:
					return 5;
				default:
					return 100;
			}
		}

	}

	public void readTextConfig() {
		SharedPreferences mSp = PreferenceManager.getDefaultSharedPreferences(mActivity);

		mSpaceW = SetTextActivity.getSpaceW(mSp);
		mSpaceH = SetTextActivity.getSpaceH(mSp);
		mHeadSizeOrg = SetTextActivity.getFontTop(mSp);	// 見出し
		mBodySizeOrg = SetTextActivity.getFontBody(mSp);	// 本文
		mRubiSizeOrg = SetTextActivity.getFontRubi(mSp);	// ルビ
		mInfoSizeOrg = SetTextActivity.getFontInfo(mSp);	// ページ情報など
		mMarginWOrg = SetTextActivity.getMarginW(mSp);	// 左右余白(設定値)
		mMarginHOrg = SetTextActivity.getMarginH(mSp);	// 上下余白(設定値)
		mDensity = mActivity.getResources().getDisplayMetrics().scaledDensity;
		mHeadSize = DEF.calcFontPix(mHeadSizeOrg, mDensity);	// 見出し
		mBodySize = DEF.calcFontPix(mBodySizeOrg, mDensity);	// 本文
		mRubiSize = DEF.calcFontPix(mRubiSizeOrg, mDensity);	// ルビ
		mInfoSize = DEF.calcFontPix(mInfoSizeOrg, mDensity);	// ページ情報など
		mPicSize = SetTextActivity.getPicSize(mSp);	// 挿絵サイズ

		mMarginW = DEF.calcDispMargin(mMarginWOrg);				// 左右余白
		mMarginH = mInfoSize + DEF.calcDispMargin(mMarginHOrg);	// 上下余白
		mAscMode = SetTextActivity.getAscMode(mSp);

		String fontname = SetTextActivity.getFontName(mSp);
		if (!fontname.isEmpty()) {
			String path = DEF.getFontDirectory();
			mFontFile = path + fontname;
		}
		else {
			mFontFile = null;
		}

		mPaperSel = SetTextActivity.getPaper(mSp);	// 用紙サイズ
		mNotice = SetTextActivity.getNotice(mSp);	// ステータスバーを隠す
		mImmEnable = SetImageTextDetailActivity.getImmEnable(mSp);	// ナビゲーションバーを隠す
		mViewRota = SetTextActivity.getViewRota(mSp);

		int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		int statusBarHeight = mActivity.getResources().getDimensionPixelSize(resourceId);
		resourceId = mActivity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		int navigationBarHeight = mActivity.getResources().getDimensionPixelSize(resourceId);

		if (mPaperSel == DEF.PAPERSEL_SCREEN) {
			Point point = new Point();
			mActivity.getWindowManager().getDefaultDisplay().getRealSize(point);
			int cx = point.x;
			int cy = point.y;

			if (cx < cy) {
				mTextWidth = cx;
				mTextHeight = cy;
			}
			else {
				mTextWidth = cy;
				mTextHeight = cx;
			}

			if (!mImmEnable) {
				mTextHeight -= navigationBarHeight;
			}
			if (!mNotice) {
				if (mViewRota == 0 || mViewRota == 1 || mViewRota == 3) {
					// 『回転あり』または『縦固定』または『縦固定(90°回転)』
					mTextHeight -= statusBarHeight;
				}
				else {
					// 『横固定』
					mTextWidth -= statusBarHeight;
				}

			}
		}
		else {
			mTextWidth = DEF.PAPERSIZE[mPaperSel][0];
			mTextHeight = DEF.PAPERSIZE[mPaperSel][1];
		}
	}

	// 表示中の範囲を設定
	public void setDispRange(int firstindex, int lastindex) {
		mFirstIndex = firstindex;
		mLastIndex = lastindex;
		interruptThread();
	}

	// スレッド中断
	protected void interruptThread() {
		;
	}

	protected void removeFile(FileData file) {
		synchronized (mFileListLock) {
			if (mFiles != null && !mFiles.isEmpty()) {
				mFiles.remove(file);
			}
		}
	}

	public class CacheException extends IOException {
		private static final long serialVersionUID = 1L;

		public CacheException(String str) {
			super(str);
		}

		public CacheException(Exception e) {
			super(e);
		}
	}


	/**
	 * ビットマップデータをメモリキャッシュに格納する
	 * @param index
	 * @param thum_cx
	 * @param thum_cy
	 * @param bm
	 * @param priority 強制挿入する時はtrue
	 * @return メモリキャッシュに変動があればtrue、それ以外はfalse
	 * @exception CacheException メモリキャッシュが空けられなかった場合
	 */
	public boolean loadMemory(int index, int thum_cx, int thum_cy, Bitmap bm, boolean priority) throws CacheException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "index=" + index + " 開始します. priority=" + priority);

		int result;
		boolean save = false;

		if (bm == null) {
			Logcat.d(logLevel, "index=" + index + " NULLです");
			return false;
		} else {
			Logcat.d(logLevel, "index=" + index + " NULLじゃないです.");
		}

		// ビットマップをサムネイルサイズぴったりにリサイズする
		Logcat.d(logLevel, "index=" + index + " リサイズします. thum_cx=" + thum_cx + ", thum_cy=" + thum_cy + ", crop=" + mThumbCrop + ", margin=" + mThumbMargin);
		bm = ImageAccess.resizeTumbnailBitmap(bm, thum_cx, thum_cy, mThumbCrop, mThumbMargin);

		Logcat.d(logLevel, "index=" + index + " 切り出します.");
		int w = bm.getWidth();
		int h = bm.getHeight();
		boolean chg = false;
		if (w > mThumbSizeW) {
			w = mThumbSizeW;
			chg = true;
		}
		if (h > mThumbSizeH) {
			h = mThumbSizeH;
			chg = true;
		}

		// ビットマップを切り出す
		if (chg || bm.getConfig() != Bitmap.Config.RGB_565) {
			Bitmap bm2 = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			bm = Bitmap.createBitmap(bm, 0, 0, w, h);
			Paint drawBmp = new Paint();
			Canvas offScreen = new Canvas(bm2);
			drawBmp.setColor(0xFFFFFFFF);
			drawBmp.setStyle(Paint.Style.FILL);
			offScreen.drawRect(0, 0, w, h, drawBmp);
			offScreen.drawBitmap(bm, 0, 0, null);
			bm = bm2;
		}

		if (logLevel <= Logcat.LOG_LEVEL_DEBUG) {
			bm = ImageAccess.setText(bm, String.valueOf(index), Color.BLUE, DEF.ALIGN_CENTER);
		}

		// 空きメモリがあるかをチェック
		result = CallImgLibrary.ThumbnailMemorySizeCheck(mID, bm.getWidth(), bm.getHeight());
		if (result == 0) {
			// メモリあり
			Logcat.d(logLevel, "index=" + index + " メモリキャッシュの空きがありました");
		} else if (result > 0 && priority) {
			// メモリなしでもpriorityがtrueなら
			// 表示の中心から外れたものを解放してメモリを空ける
			result = CallImgLibrary.ThumbnailImageAlloc(mID, result, (mFirstIndex + mLastIndex) / 2);
			if (result == 0) {
				// メモリ獲得成功
				Logcat.d(logLevel, "index=" + index + " メモリキャッシュの空きを作りました.");
			} else {
				// メモリなし
				Logcat.d(logLevel, "index=" + index + " メモリキャッシュの空きが作れませんでした.");
				mOut_of_memory = true;
				throw new CacheException("メモリキャッシュの空きが作れませんでした.");
			}
		}
		else {
			Logcat.d(logLevel, "index=" + index + " メモリキャッシュの空きがありませんでした");
			return false;
		}

		result = CallImgLibrary.ThumbnailSave(mID, bm, index);
		if (result != CallImgLibrary.RESULT_OK) {
			// メモリ保持失敗
			Logcat.d(logLevel, "index=" + index + " メモリキャッシュに挿入できませんでした. result=" + result);
			mOut_of_memory = true;
			return false;
		}
		else {
			Logcat.d(logLevel, "index=" + index + " メモリキャッシュに挿入しました.");
			return true;
		}

	}

	protected void saveCache(Bitmap bm, String pathcode) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		boolean debug = false;

		if (bm != null) {
			Logcat.d(logLevel, "キャッシュにセーブします pathcode=" + pathcode);
			saveThumbnailCache(pathcode, bm);
		}
	}

}
