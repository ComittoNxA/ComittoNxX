package src.comitton.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import src.comitton.common.DEF;
import src.comitton.data.FileData;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class ThumbnailLoader {
	public static final int SIZE_LOCALHEADER = 30;
	public static final int SIZE_BITFLAG = 12;

	public static final int OFFSET_LCL_SIGNA_LEN = 0;
	public static final int OFFSET_LCL_BFLAG_LEN = 6;
	public static final int OFFSET_LCL_CRC32_LEN = 14;
	public static final int OFFSET_LCL_CDATA_LEN = 18;
	public static final int OFFSET_LCL_FNAME_LEN = 26;
	public static final int OFFSET_LCL_EXTRA_LEN = 28;

	protected Handler mHandler;
	protected String mUri;
	protected String mPath;
	protected ArrayList<FileData> mFiles;
	protected String mCachePath;
	protected boolean mThreadBreak;

	protected long mID;
	protected int mThumbSizeW;
	protected int mThumbSizeH;
	protected int mFirstIndex;
	protected int mLastIndex;

	protected int mThumbCacheNum;
	protected int mThumbCrop;
	protected int mThumbMargin;

	public ThumbnailLoader(String uri, String path, Handler handler, long id, ArrayList<FileData> files, int sizeW, int sizeH, int cachenum,int crop, int margin) {
		super();
		mHandler = handler;
		mThreadBreak = false;
		mThumbSizeW = sizeW;
		mThumbSizeH = sizeH;
		mUri = uri;
		mPath = path;
		mID = id;
		mFirstIndex = 0;
		mLastIndex = files.size() - 1;
		mThumbCacheNum = cachenum;
		mThumbCrop = crop;
		mThumbMargin = margin;

		// キャッシュフォルダ
		mCachePath = DEF.getBaseDirectory() + "thumb/";
		try {
			new File(mCachePath).mkdirs();
		}
		catch (Exception e) {
			Log.e("Thumbnail/mkdirs", e.getMessage());
			mCachePath = null;
		}

		mFiles = files;
		return;
	}

	// 解放
	protected void releaseThumbnail() {
		// サムネイル画像データ解放
		CallImgLibrary.ThumbnailFree(mID);
		interruptThread();
		return;
	}

	// スレッド停止
	protected void breakThread() {
		mThreadBreak = true;
		interruptThread();
		return;
	}

	//path1のサムネイルをpath2のキャッシュとして割り当て
	public void setThumbnailCache(String path1, String path2) {
		// 他のパスのキャッシュとして保存
		String pathcode = DEF.makeCode(path1, mThumbSizeW, mThumbSizeH);
//		File file = new File(cacheFile);
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
				Log.e("Thumbnail/cacheSave", e.getMessage());
			}
		}
	}

	//特定のパスにサムネイルキャッシュを割り当てる
	public void setThumbnailCache(String path, Bitmap bm) {
		String pathcode = DEF.makeCode(path, mThumbSizeW, mThumbSizeH);
//		File file = new File(cacheFile);
		String cacheFile = getThumbnailCacheName(pathcode);
		if (bm != null) {
			try {
				FileOutputStream cacheSave = new FileOutputStream(cacheFile);
				bm.compress(CompressFormat.JPEG, 80, cacheSave);
				cacheSave.flush();
				cacheSave.close();
			}
			catch (Exception e) {
				Log.e("Thumbnail/cacheSave", e.getMessage());
			}
		}
	}

	public void saveThumbnailCache(String pathcode, Bitmap bm) {
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
				Log.e("Thumbnail/cacheSave", e.getMessage());
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
		Bitmap bm = BitmapFactory.decodeFile(cacheName);
		return bm;
	}

	// キャッシュファイル存在チェック
	protected boolean checkThumbnailCache(String pathcode) {
		String cacheName = getThumbnailCacheName(pathcode);
		File cacheFile = new File(cacheName);
		return cacheFile.exists();
	}

	public static void deleteThumbnailCache(int leave) {
		if (leave < 0) {
			// -1を指定の場合は削除しない
			return;
		}

		// キャッシュ保存先
		String path = DEF.getBaseDirectory() + "thumb/";

		File files[] = new File(path).listFiles();
		if (files == null) {
			// ファイルなし
			return;
		}
		ArrayList<File> fileArray = new ArrayList<File>();
		for (File file : files) {
			if (!file.isDirectory()) {
				String name = file.getName();
				if (name == null || name.length() <= 6 || name.equals("comittona.cache")) {
					// 削除対象外
					continue;
				}
				String ext = name.substring(name.length() - 6);
				if (!ext.equalsIgnoreCase(".cache")) {
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
			// 更新日付を現在時刻に
			file.delete();
		}
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

	// 表示中の範囲を設定
	public void setDispRange(int firstindex, int lastindex) {
		mFirstIndex = firstindex;
		mLastIndex = lastindex;
		interruptThread();
	}

	// スレッド中断
	protected void interruptThread() {
		return;
	}
}
