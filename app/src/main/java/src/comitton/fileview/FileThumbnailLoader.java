package src.comitton.fileview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileaccess.FileAccessException;
import src.comitton.fileaccess.FileAccess;
import src.comitton.common.WaitFor;
import src.comitton.fileview.data.FileData;
import src.comitton.imageview.ImageManager;
import src.comitton.jni.CallImgLibrary;
import src.comitton.common.ThumbnailLoader;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class FileThumbnailLoader extends ThumbnailLoader implements Runnable {
	private static final String TAG = "FileThumbnailLoader";

	private String mUser;
	private String mPass;
	private int mFileSort;
	private boolean mHidden;
	private boolean mThumbSort;
	private boolean mEpubThumb;
	private boolean mEpubViewer;

	private ThumbnailCacheLoader mThumbnailCacheLoader;
	private ImageManager mImageMgr;
	private FileTypeSortComparator mComparator;
	private FileData mCurrentFile;

	private WaitFor mWaitFor;
	private boolean mSkip;
	private boolean mSorting;

	public FileThumbnailLoader(AppCompatActivity activity, String uri, String path, String user, String pass, Handler handler, long id, ArrayList<FileData> files, int sizeW, int sizeH, int cachenum, int filesort, boolean hidden, boolean thumbsort, int crop, int margin, boolean epubThumb, boolean epubViewer) {
		super(activity, uri, path, handler, id, files, sizeW, sizeH, cachenum, crop, margin);
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. epubThumb=" + epubThumb);

		mSkip = false;
		mSorting = false;

		mUser = user;
		mPass = pass;
		mFileSort = filesort;
		mHidden = hidden;
		mThumbSort = thumbsort;
		mEpubThumb = epubThumb;
		mEpubViewer = epubViewer;

		mComparator = new FileTypeSortComparator();
		mComparator.setDispRange(mFirstIndex, mLastIndex);

		// サムネイル保持領域初期化
		int ret = CallImgLibrary.ThumbnailInitialize(mID, DEF.THUMBNAIL_PAGESIZE, DEF.THUMBNAIL_MAXPAGE, mFiles.size());
		if (ret < 0) {
			return;
		}

		for (int i = mFiles.size() - 1; i >= 0; i--) {
			if (mFiles.get(i).getType() == FileData.FILETYPE_PARENT || mFiles.get(i).getType() == FileData.FILETYPE_TXT) {
				CallImgLibrary.ThumbnailSetNone(mID, mFiles.get(i).getIndex());
				removeFile(mFiles.get(i));
			}
		}

		mThumbnailCacheLoader = new ThumbnailCacheLoader(mActivity, this, mURI, mPath, mUser, mPass, mHandler, mID, mFiles, mThumbSizeW, mThumbSizeH, mThumbCacheNum, mHidden, mThumbCrop, mThumbMargin, mEpubViewer);

		mWaitFor = new WaitFor(60000);

		// スレッド起動
		Thread thread = new Thread(this);
		thread.start();
	}

	// 解放
	public void releaseThumbnail() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		super.releaseThumbnail();
	}

	// スレッド停止
	public void breakThread() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		mThumbnailCacheLoader.breakThread();
		super.breakThread();
	}

	// ImageManager を解放する
	public void releaseManager() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		// 意図的にExceptionを発生させるためスレッドセーフにしない
		if (mImageMgr != null) {
			//mImageMgr.setBreakTrigger();
			try {
				Logcat.v(logLevel, "mImageMgr.close() cacheIndex=" + mImageMgr.getCacheIndex());
				mImageMgr.close();
			} catch (IOException e) {
				Logcat.w(logLevel, "mImageMgr.close() cacheIndex=" + mImageMgr.getCacheIndex(), e);
			}
			mImageMgr = null;
		}
	}

	public void remove(FileData file) {
		CallImgLibrary.ThumbnailRemove(mID, file.getIndex());
	}

	public void update(FileData file) {

		remove(file);

		if (!mFiles.contains(file)) {
			mFiles.add(file);
		}

		// 中でループさせたいので非同期処理にする
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(new Runnable() {
			@Override
			public void run() {
				synchronized (mFileListLock) {
					if (mFiles != null && !mFiles.isEmpty()) {
						while (true) {
							// ソートが成功するまでループする
							try {
								Collections.sort(mFiles, mComparator);
								break;
							} catch (ConcurrentModificationException e) {
								continue;
							}
						}
						if (mComparator.compare(mFiles.get(0), mCurrentFile) < 0) {
							interruptThread();
						}
					}
				}
			}
		});
	}

	// 表示中の範囲を設定
	public void setDispRange(int firstindex, int lastindex) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"開始します. firstindex=" + firstindex + ", lastindex=" + lastindex);

		if (mFirstIndex != firstindex || mLastIndex != lastindex) {
			Logcat.v(logLevel,"表示範囲に変化があります.");
			// スクロール位置に変化があったら実行する
			mFirstIndex = firstindex;
			mLastIndex = lastindex;
			mComparator.setDispRange(mFirstIndex, mLastIndex);

			if (mThumbnailCacheLoader != null) {
				mThumbnailCacheLoader.setDispRange(firstindex, lastindex);
			}

			if (!mSorting) {
				// 中でループさせたいので非同期処理にする
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.submit(new Runnable() {
					@Override
					public void run() {
						synchronized (mFileListLock) {
							mSorting = true;
							if (mFiles != null && !mFiles.isEmpty()) {
								while (true) {
									// ソートが成功するまでループする
									try {
										Collections.sort(mFiles, mComparator);
										if (logLevel <= Logcat.LOG_LEVEL_VERBOSE) {
											for (int i = 0; i < mFiles.size(); ++i) {
												Logcat.v(logLevel, "mFiles[" + i + "]=" + mFiles.get(i).getIndex() + " : " + mFiles.get(i).getName());
											}
										}
										break;
									} catch (ConcurrentModificationException e) {
										continue;
									}
								}
								if (mComparator.compare(mFiles.get(0), mCurrentFile) < 0) {
									interruptThread();
								}
							}
							mSorting = false;
						}
					}
				});
			}
		}

		Logcat.d(logLevel,"終了します.");
	}

	protected void interruptThread() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"開始します.");
		mSkip = true;
		releaseManager();
		if (mWaitFor != null) {
			mWaitFor.interrupt();
		}
	}

	// スレッド開始
	public void run() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. mPath=" + mPath);

		if (mFiles.isEmpty()) {
			return;
		}

		while(!mThreadBreak) {
			mSkip = false;
			mCurrentFile = null;
			synchronized (mFileListLock) {
				if (mFiles != null && !mFiles.isEmpty()) {
					mCurrentFile = mFiles.get(0);
				}
			}

			if (mCurrentFile == null) {
				// ファイルがなくなったら新たにファイルが挿入されるまで待機する
				mWaitFor.sleep();
				continue;
			}

			String uri = DEF.relativePath(mActivity,mURI, mPath, mCurrentFile.getName());
			String pathcode = DEF.makeCode(uri, mThumbSizeW, mThumbSizeH);
			if (checkThumbnailCache(pathcode)) {
				// ファイルキャッシュが存在すれば次のファイルを処理する
				removeFile(mCurrentFile);
				continue;
			}

            try {
				loadFileCache(mCurrentFile);
            } catch (CacheException e) {
				continue;
            }
            removeFile(mCurrentFile);

		}

		// サムネイルのファイルキャッシュ削除
		deleteThumbnailCache(mThumbCacheNum);
		Logcat.d(logLevel, "終了します.");
	}

	@SuppressLint("SuspiciousIndentation")
    private void loadFileCache(FileData file) throws CacheException {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		int index = file.getIndex();
		String filename = file.getName();
		String uri = DEF.relativePath(mActivity,mURI, mPath, filename);
		String pathcode = DEF.makeCode(uri, mThumbSizeW, mThumbSizeH);

		Logcat.d(logLevel,"index=" + index + " loadFileCache: 開始します. filename=" + filename);

        boolean retCode = searchFileCache(file, uri, pathcode);

        if (!retCode) {
			// ファイルキャッシュの保存に失敗していたらNo Imageを登録する
			CallImgLibrary.ThumbnailSetNone(mID, index);
			Logcat.d(logLevel,"index=" + index + " loadFileCache: 空で登録しました.");
		}

		if (retCode) {
			// ファイルキャッシュの保存に成功していたらメモリキャッシュを更新する
			mThumbnailCacheLoader.interruptThread();
			Logcat.d(logLevel,"index=" + index + " loadFileCache: 通知しました.");
		}

	}

	private boolean searchFileCache(FileData file, String uri, String pathcode) throws CacheException {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		int index = file.getIndex();
		String filename = file.getName();

		Logcat.d(logLevel,"index=" + index + " 開始します. Filename=" + filename);

		if (mThreadBreak) {
			Logcat.d(logLevel, "index=" + index + " 中断されました.");
			throw new CacheException(TAG + ": index=" + index + " searchFileCache: 中断されました.");
		}

		ArrayList<FileData> inFiles;

		// ディレクトリ(簡易判定)の場合は中のファイルを参照
		if (FileAccess.isDirectory(mActivity, uri, mUser, mPass)) {
			Logcat.d(logLevel,"index=" + index + " ディレクトリの中を検索します.");
			try {
				inFiles = FileAccess.listFiles(mActivity, uri, mUser, mPass, null);
			} catch (FileAccessException e) {
				Logcat.e(logLevel, "saveBitmap: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
				throw new CacheException(TAG + ": index=" + index + " searchFileCache: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
			}

			if (inFiles.isEmpty()) {
				Logcat.d(logLevel,"index=" + index + " ディレクトリの中は空でした.");
				return false;
			}
			Logcat.d(logLevel,"index=" + index + " ディレクトリに " + inFiles.size() + " 個のファイルがあります.");
			for (int i = 0; i < inFiles.size(); i++) {

				if (inFiles.get(i).getType() == FileData.FILETYPE_NONE || inFiles.get(i).getType() == FileData.FILETYPE_TXT || inFiles.get(i).getType() == FileData.FILETYPE_EPUB_SUB) {
					continue;
				}

				if (mThreadBreak) {
					Logcat.d(logLevel, "index=" + index + " 中断されました.");
					throw new CacheException(TAG + ": index=" + index + " searchFileCache: 中断されました.");
				}

				FileData inFile = inFiles.get(i);
				String inFilename = inFile.getName();
				String inUri = DEF.relativePath(mActivity, uri, inFile.getName());

				if (inFile.getType() == FileData.FILETYPE_DIR) {
					Logcat.d(logLevel,"index=" + index + " ディレクトリの中にディレクトリがあります. inFilename=" + inFilename);
					if (searchFileCache(inFile, inUri, pathcode)) {
							return true;
					}
				} else {
					Logcat.d(logLevel,"index=" + index + " ディレクトリの中にファイルがあります. inFilename=" + inFilename);
					if (saveFileCache(inFile, inUri, pathcode)) {
						return true;
					}
				}
			}
		}
		else {
			if (saveFileCache(file, uri, pathcode)) {
				return true;
			}
		}
		return false;
	}

	private boolean saveFileCache(FileData file, String uri, String pathcode) throws CacheException {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		int index = file.getIndex();
		String filename = file.getName();

		Logcat.d(logLevel, "index=" + index + ", filename=" + filename);
		// ビットマップ読み込み

		Bitmap bm;

		if (mThreadBreak) {
			Logcat.d(logLevel, "index=" + index + ", 中断されました. filename=" + filename);
			throw new CacheException(TAG + ": saveFileCache: 中断されました.");
		}

		int openmode = 0;
		// ファイルリストの読み込み
		if (mThumbSort) {
			openmode = ImageManager.OPENMODE_THUMBSORT;
		} else {
			openmode = ImageManager.OPENMODE_THUMBNAIL;
		}

		try {
			mImageMgr = new ImageManager(mActivity, mUriPath, uri, mUser, mPass, mFileSort, mHandler, mHidden, openmode, 1);

			if (mThreadBreak) {
				Logcat.d(logLevel, "index=" + index + ", 中断されました. filename=" + filename);
				throw new CacheException(TAG + ": saveFileCache: 中断されました.");
			}

			if (mSkip) {
				throw new CacheException("saveFileCache: スキップします.");
			}

			Logcat.d(logLevel, "index=" + index + ", サムネイル取得します. filename=" + filename);
			if (mEpubThumb && file.getType() == FileData.FILETYPE_EPUB) {
				Logcat.d(logLevel, "index=" + index + ", LoadEpubThumbnail を実行します. width=" + mThumbSizeW + ", height=" + mThumbSizeH + " filename=" + filename);
				bm = mImageMgr.LoadEpubThumbnail(mThumbSizeW, mThumbSizeH);
			} else {
				Logcat.d(logLevel, "index=" + index + ", LoadThumbnail を実行します. page=0, width=" + mThumbSizeW + ", height=" + mThumbSizeH + " filename=" + filename);
				bm = mImageMgr.LoadThumbnail(0, mThumbSizeW, mThumbSizeH);
			}
			Logcat.v(logLevel, "new ImageManager() cacheIndex=" + mImageMgr.getCacheIndex());
			releaseManager();
		}
		catch (CacheException e) {
			releaseManager();
			throw e;
		}
		catch (Exception e) {
			releaseManager();
			Logcat.w(logLevel, "index=" + index + ", エラーが発生しました. filename=" + filename, e);

			if (mThreadBreak) {
				Logcat.d(logLevel, "index=" + index + ", 中断されました. filename=" + filename, e);
				throw new CacheException(TAG + ": saveFileCache: 中断されました: " + e);
			}

			if (mSkip) {
				throw new CacheException(TAG + "saveFileCache: スキップします: " + e);
			}

			return false;
		}

		if (mSkip) {
			throw new CacheException("saveFileCache: スキップします.");
		}

		if (bm != null) {
			Logcat.d(logLevel, "index=" + index + ", 画像データを取得しました. filename=" + filename);
		}
		else {
			Logcat.w(logLevel, "index=" + index + ", 画像データを取得できませんでした. filename=" + filename);
			return false;
		}

		Logcat.d(logLevel, "index=" + index + ", ファイルキャッシュに登録します. filename=" + filename);
		saveCache(bm, pathcode);

		return true;
	}

}

