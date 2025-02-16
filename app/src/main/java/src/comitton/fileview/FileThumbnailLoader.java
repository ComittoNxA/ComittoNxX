package src.comitton.fileview;

import java.io.IOException;
import java.util.ArrayList;

import src.comitton.common.DEF;
import src.comitton.fileaccess.FileAccessException;
import src.comitton.fileaccess.FileAccess;
import src.comitton.common.ImageAccess;
import src.comitton.common.WaitFor;
import src.comitton.fileview.data.FileData;
import src.comitton.imageview.ImageManager;
import src.comitton.jni.CallImgLibrary;
import src.comitton.common.ThumbnailLoader;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class FileThumbnailLoader extends ThumbnailLoader implements Runnable {
	private static final String TAG = "FileThumbnailLoader";

	private String mUser;
	private String mPass;
	private int mFileSort;
	private boolean mHidden;
	private boolean mThumbSort;
	private boolean mEpubThumb;

	private ImageManager mImageMgr;
	private Object mImageMgrLock;

	private WaitFor mWaitFor;

	private boolean mOut_of_memory = false;

	public FileThumbnailLoader(AppCompatActivity activity, String uri, String path, String user, String pass, Handler handler, long id, ArrayList<FileData> files, int sizeW, int sizeH, int cachenum, int filesort, boolean hidden, boolean thumbsort, int crop, int margin, boolean epubThumb) {
		super(activity, uri, path, handler, id, files, sizeW, sizeH, cachenum, crop, margin);
		boolean debug = false;
		if (debug) {Log.d(TAG, "FileThumbnailLoader: 開始します. epubThumb=" + epubThumb);}

		mUser = user;
		mPass = pass;
		mFileSort = filesort;
		mHidden = hidden;
		mThumbSort = thumbsort;
		mEpubThumb = epubThumb;

		mImageMgrLock = new Object();

		mWaitFor = new WaitFor(60000);

		// スレッド起動
		Thread thread = new Thread(this);
		thread.start();
		return;
	}

	// 解放
	public void releaseThumbnail() {
		super.releaseThumbnail();
		return;
	}

	// スレッド停止
	public void breakThread() {
		super.breakThread();
		releaseManager();
	}

	// スレッド停止
	public void releaseManager() {
		// 読み込み終了
		synchronized (mImageMgrLock) {
			if (mImageMgr != null) {
				//mImageMgr.setBreakTrigger();
				try {
					mImageMgr.close();
				} catch (IOException e) {
					;
				}
				mImageMgr = null;
			}
		}
	}

	protected void interruptThread() {
		if (mWaitFor != null) {
			mWaitFor.interrupt();
		}
	}

	// スレッド開始
	public void run() {
		boolean debug = false;
		if (debug) {Log.d(TAG,"run: mPath=" + mPath);}

		if (mThreadBreak || mCachePath == null || mFiles == null) {
			return;
		}
		int fileNum = mFiles.size();
		if (fileNum <= 0) {
			return;
		}

		// サムネイル保持領域初期化
		int ret = CallImgLibrary.ThumbnailInitialize(mID, DEF.THUMBNAIL_PAGESIZE, DEF.THUMBNAIL_MAXPAGE, fileNum);
		if (ret < 0) {
			return;
		}

		int thum_cx = mThumbSizeW;
		int thum_cy = mThumbSizeH;
		int firstindex = -1;
		int lastindex;

		while (!mThreadBreak) {
			// 初回又は変化があるかのチェック
			if (firstindex != mFirstIndex) {
				// 表示範囲
				firstindex = mFirstIndex;
				lastindex = mLastIndex;

				// 最初は表示範囲を優先
				for (int loop = 0; loop < 2; loop++) {
					// 1週目キャッシュからのみ、2週目は実体も
					for (int i = firstindex; i <= lastindex && firstindex == mFirstIndex; i++) {
						if (i < 0 || i >= fileNum) {
							// 範囲内だけ処理する
							continue;
						}

						if (debug) {Log.d(TAG,"run: index=" + i + " " + (loop+1) + "周目 run");}
						// 1周目は新たに読み込みしない
						loadBitmap(i, thum_cx, thum_cy, loop == 0, true);
						if (mThreadBreak) {
							if (debug) {Log.d(TAG, "run: index=" + i + " " + (loop+1) + "周目 run 中断されました.");}
							return;
						}
					}
				}
				if (firstindex != mFirstIndex) {
					// 選択範囲が変わったら再チェック
					continue;
				}

				// 前後をキャッシュから読み込み
				int range = (lastindex - firstindex) * 2;
				boolean isBreak = false;
				boolean prevflag = false;
				boolean nextflag = false;
				for (int count = 1; count <= range && !isBreak; count++) {
					if ((prevflag && nextflag) || firstindex != mFirstIndex) {
						// 範囲オーバー、選択範囲変更
						break;
					}
					for (int way = 0; way < 2 && firstindex == mFirstIndex; way++) {
						if (mThreadBreak) {
							return;
						}
						// キャッシュからのみ
						int index;
						if (way == 0) {
							// 前側
							index = firstindex - count;
							if (index < 0) {
								// 範囲内だけ処理する
								prevflag = true;
								continue;
							}
						} else {
							// 後側
							index = lastindex + count;
							if (index >= fileNum) {
								// 範囲内だけ処理する
								nextflag = true;
								continue;
							}
						}
						// キャッシュからのみ読み込み
						if (!loadBitmap(index, thum_cx, thum_cy, true, false)) {
							// メモリ不足で中断
							isBreak = true;
							break;
						}
					}
				}
				if (firstindex != mFirstIndex) {
					continue;
				}

				// 前後をキャッシュと実体から読み込み
				range = (lastindex - firstindex);
				isBreak = false;
				prevflag = false;
				nextflag = false;
				for (int count = 1; count <= range && firstindex == mFirstIndex; count++) {
					if ((prevflag && nextflag) || firstindex != mFirstIndex) {
						// 範囲オーバー、選択範囲変更
						break;
					}
					for (int way = 0; way < 2 && firstindex == mFirstIndex; way++) {
						if (mThreadBreak) {
							return;
						}
						// 実体も読み込み
						int index;
						if (way == 0) {
							// 前側
							index = firstindex - count;
							if (index < 0) {
								// 範囲内だけ処理する
								prevflag = true;
								continue;
							}
						} else {
							// 後側
							index = lastindex + count;
							if (index >= fileNum) {
								// 範囲内だけ処理する
								nextflag = true;
								continue;
							}
						}
						// キャッシュと実体から読み込み
						if (!loadBitmap(index, thum_cx, thum_cy, false, true)) {
							break;
						}
					}
				}
				if (firstindex != mFirstIndex) {
					continue;
				}

			}

			if (CallImgLibrary.ThumbnailCheckAll(mID) == 0) {
				// 全部読み込めた
				break;
			} else {
				// ページ選択待ちに入る
				mWaitFor.sleep();
			}
		}

		if (!mThreadBreak) {
			// サムネイルキャッシュ削除
			if (mCachePath != null) {
				deleteThumbnailCache(mThumbCacheNum);
			}
		}
	}

	private boolean loadBitmap(int index, int thum_cx, int thum_cy, boolean firstloop, boolean priority) {
		boolean debug = false;

		int result = CallImgLibrary.ThumbnailCheck(mID, index);
		boolean ret = false;
		if (result > 0) {
			// 既に読み込み済み
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap 読み込み済みです.");}
			return true;
		}
		// ファイル情報取得
		if (index >= mFiles.size()) {
			Log.e(TAG, "index=" + index + " loadBitmap3 ファイルのindexが範囲外です. index＝" + index + ", size=" + mFiles.size());
			return false;
		}
		FileData file = mFiles.get(index);
		String filename = file.getName();
		String uri = DEF.relativePath(mActivity,mURI, mPath, filename);
		int fileType = FileData.getType(mActivity, filename);
		String pathcode = DEF.makeCode(uri, thum_cx, thum_cy);
		if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap filename=" + filename);}
		int filetype = file.getType();

		if (fileType == FileData.FILETYPE_PARENT) {
			// 対象外のファイル
			CallImgLibrary.ThumbnailSetNone(mID, index);
			ret = true;
		}
		else if (fileType == FileData.FILETYPE_TXT) {
			// 対象外のファイル
			CallImgLibrary.ThumbnailSetNone(mID, index);
			ret = true;
		}
		else {
			ret = loadBitmap2(filename, uri, index, thum_cx, thum_cy, firstloop, priority, pathcode);
		}

		if (!firstloop && !ret) {
			// 2周目で画像セーブに失敗していたら
			CallImgLibrary.ThumbnailSetNone(mID, index);
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap 空で登録しました.");}
		}

		if (!firstloop || ret) {
			// 1周目で画像セーブに成功しているか、2周目が終わったら
			// 通知
			Message message = new Message();
			message.what = DEF.HMSG_THUMBNAIL;
			message.arg1 = ret ? index : DEF.THUMBSTATE_ERROR;
			message.obj = getFilename(index);
			mHandler.sendMessage(message);
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap 通知しました arg1=" + message.arg1);}
		}
		return !mOut_of_memory;
	}

	private boolean loadBitmap2(String filename, String uri, int index, int thum_cx, int thum_cy, boolean firstloop, boolean priority, String pathcode) {
		boolean debug = false;

		if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2  Filename=" + filename);}
		boolean ret = false;
		Bitmap bm;

		if (filename.equals("..")) {
			// 対象外のファイル
			return false;
		}
		String ext = DEF.getExtension(filename);
		if (FileData.isText(ext)) {
			// 対象外のファイル
			return false;
		}

		// キャッシュから読込
		if (checkThumbnailCache(pathcode)) {
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 キャッシュに登録済みです.");}
			bm = loadThumbnailCache(pathcode);
			if (bm == null) {
				if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 キャッシュが空でした.スキップします.");}
				return true;
			}
			else {
				if (debug) {Log.d(TAG, "index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 キャッシュがありました.");}
				loadMemory(index, thum_cx, thum_cy, bm, priority);
				//saveBitmap(bm, pathcode);
				return true;
			}
		}
		else {
			if (debug) {Log.d(TAG, "index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 キャッシュに登録済されていません.");}
		}

		if (mThreadBreak) {
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 中断されました.");}
			return false;
		}

		if (firstloop) {
			// 初回ループはキャッシュからのみ読み込み
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 スキップします.");}
			return false;
		}

		ArrayList<FileData> infile = new ArrayList<FileData>();

		// ディレクトリ(簡易判定)の場合は中のファイルを参照
		if (filename.endsWith("/")) {
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 ディレクトリの中を検索します.");}
			try {
				infile = FileAccess.listFiles(mActivity, uri, mUser, mPass, null);
			} catch (FileAccessException e) {
				throw new RuntimeException(e);
			}

			if (infile.isEmpty()) {
				if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 ディレクトリの中は空でした.");}
				return false;
			}
			if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 ディレクトリに " + infile.size() + " 個のファイルがあります.");}
			for (int i = 0; i < infile.size(); i++) {
				if (mThreadBreak) {
					return false;
				}
				FileData file = infile.get(i);
				String inFilename = file.getName();
				String inUri = DEF.relativePath(mActivity, uri, inFilename);
				int inFileType = FileData.getType(mActivity, inFilename);
				if (inFileType == FileData.FILETYPE_DIR) {
					if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 ディレクトリの中にディレクトリがあります. infilename=" + infile.get(i).getName());}
					if (loadBitmap2(inFilename, inUri, index, thum_cx, thum_cy, firstloop, priority, pathcode)) {
							return true;
					}
				} else {
					if (debug) {Log.d(TAG,"index=" + index + " " + (firstloop ? 1 : 2) + "周目 loadBitmap2 ディレクトリの中にファイルがあります. infilename=" + infile.get(i).getName());}
					if (loadBitmap3(inFilename, inUri, index, thum_cx, thum_cy, priority, pathcode)) {
						return true;
					}
				}
			}
		}
		else {
			if (loadBitmap3(filename, uri, index, thum_cx, thum_cy, priority, pathcode)) {
				return true;
			}
		}
		return ret;
	}

	private boolean loadBitmap3(String filename, String uri, int index, int thum_cx, int thum_cy, boolean priority, String pathcode) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 filename=" + filename);}
		// ビットマップ読み込み
		Bitmap bm = null;
		BitmapFactory.Options option = new BitmapFactory.Options();

		int type = FileData.getType(mActivity, filename);
		if (type != FileData.FILETYPE_IMG && type != FileData.FILETYPE_PDF && type != FileData.FILETYPE_ARC && type != FileData.FILETYPE_EPUB) {
			if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 対象外のファイルタイプです. filename=" + filename);}
			return false;
		}

		try {
			int openmode = 0;
			// ファイルリストの読み込み
			if (mThumbSort) {
				openmode = ImageManager.OPENMODE_THUMBSORT;
			} else {
				openmode = ImageManager.OPENMODE_THUMBNAIL;
			}
			if (mThreadBreak) {
				// 読み込み中断
				if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 中断されました. filename=" + filename);}
				return true;
			}
			ImageManager imageManager = new ImageManager(mActivity, mUriPath, uri, mUser, mPass, mFileSort, mHandler, mHidden, openmode, 1);
			synchronized (mImageMgrLock) {
				mImageMgr  = imageManager;
			}
			if (mThreadBreak) {
				// 読み込み中断
				if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 中断されました. filename=" + filename);}
				releaseManager();
				return true;
			}
			if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 サムネイル取得します. filename=" + filename);}
			try {
				if (debug) {Log.d(TAG, "index=" + index + "loadBitmap3: mEpubThumb=" + mEpubThumb + ", filename=" + filename);}
				if (mEpubThumb && type == FileData.FILETYPE_EPUB) {
					if (debug) {Log.d(TAG, "index=" + index + "loadBitmap3: LoadEpubThumbnail を実行します. width=" + mThumbSizeW + ", height=" + mThumbSizeH + " filename=" + filename);}
					bm = mImageMgr.LoadEpubThumbnail(mThumbSizeW, mThumbSizeH);
				}
				else {
					if (debug) {Log.d(TAG, "index=" + index + "loadBitmap3: LoadThumbnail を実行します. page=0, width=" + mThumbSizeW + ", height=" + mThumbSizeH + " filename=" + filename);}
					bm = mImageMgr.LoadThumbnail(0, mThumbSizeW, mThumbSizeH);
				}
				if (bm != null) {
					if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3: mImageMgr.LoadThumbnail() の実行に成功しました. filename=" + filename);}
				}
				else {
					Log.e(TAG, "index=" + index + " loadBitmap3: mImageMgr.LoadThumbnail の実行に失敗しました. filename=" + filename);
				}
			} catch (Exception e) {
				Log.e(TAG, "index=" + index + " loadBitmap3 サムネイル取得でエラーになりました. filename=" + filename);
				if (e.getLocalizedMessage() != null) {
					Log.e(TAG, "index=" + index + " loadBitmap3 エラーメッセージ. " + e.getLocalizedMessage());
					releaseManager();
					return false;
				}
			} finally {
				if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 圧縮ファイルを開きました. filename=" + filename);}
			}

			if (bm == null) {
				// NoImageであればステータス設定
				if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 取得できませんでした. filename=" + filename);}
				releaseManager();
				return false;
			}
		} catch (Exception e) {
			Log.e(TAG, "index=" + index + " loadBitmap3 エラーが発生しました. filename=" + filename);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "index=" + index + " loadBitmap3 エラーメッセージ. " + e.getLocalizedMessage());
			}
			return false;
		} finally {
			releaseManager();
		}

		if (mThreadBreak) {
			// 読み込み中断
			if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 中断されました. filename=" + filename);}
			return true;
		}

		if (debug) {Log.d(TAG, "index=" + index + " loadBitmap3 キャッシュに登録します. filename=" + filename);}
		loadMemory(index, thum_cx, thum_cy, bm, priority);
		saveCache(bm, pathcode);
		bm.recycle();

		return true;
	}

	private void loadMemory(int index, int thum_cx, int thum_cy, Bitmap bm, boolean priority) {
		boolean debug = false;

		int result;
		boolean save = false;

		if (bm != null) {
			if (debug) {Log.d(TAG, "index=" + index + " loadMemory NULLじゃないです");}
		} else {
			if (debug) {Log.d(TAG, "index=" + index + " loadMemory NULLです");}
		}
		// ビットマップをサムネイルサイズぴったりにリサイズする
		if (bm != null) {
			if (debug) {Log.d(TAG, "index=" + index + " loadMemory リサイズします. thum_cx=" + thum_cx + ", thum_cy=" + thum_cy + ", crop=" + mThumbCrop + ", margin=" + mThumbMargin);}
			bm = ImageAccess.resizeTumbnailBitmap(bm, thum_cx, thum_cy, mThumbCrop, mThumbMargin);
		}
		if (bm != null) {
			if (debug) {Log.d(TAG, "index=" + index + " loadMemory 切り出します");}
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
			if (chg || bm.getConfig() != Config.RGB_565) {
				Bitmap bm2 = Bitmap.createBitmap(w, h, Config.RGB_565);
				bm = Bitmap.createBitmap(bm, 0, 0, w, h);
				Paint drawBmp = new Paint();
				Canvas offScreen = new Canvas(bm2);
				drawBmp.setColor(0xFFFFFFFF);
				drawBmp.setStyle(Style.FILL);
				offScreen.drawRect(0, 0, w, h, drawBmp);
				offScreen.drawBitmap(bm, 0, 0, null);
				bm = bm2;
			}
		}

		if (bm != null) {
			// 空きメモリがあるかをチェック
			result = CallImgLibrary.ThumbnailMemorySizeCheck(mID, bm.getWidth(), bm.getHeight());
			if (result == 0) {
				// メモリあり
				if (debug) {Log.d(TAG, "index=" + index + " loadMemory 空きメモリがありました");}
				save = true;
			} else if (result > 0 && priority) {
				// 表示の中心から外れたものを解放してメモリを空ける
				result = CallImgLibrary.ThumbnailImageAlloc(mID, result, (mFirstIndex + mLastIndex) / 2);
				if (result == 0) {
					// メモリ獲得成功
					if (debug) {Log.d(TAG, "index=" + index + " loadMemory メモリを解放しました");}
					save = true;
				} else {
					// メモリなし
					if (debug) {Log.d(TAG, "index=" + index + " loadMemory 空きメモリがありません");}
					mOut_of_memory = true;
					save = false;
				}
			}

			if (save) {
				result = CallImgLibrary.ThumbnailSave(mID, bm, index);
				if (result != CallImgLibrary.RESULT_OK) {
					// メモリ保持失敗
					if (debug) {Log.d(TAG, "index=" + index + " loadMemory メモリに保持できません");}
					mOut_of_memory = true;
					save = false;
				}
			}
		}
	}

	private void saveCache(Bitmap bm, String pathcode) {
		boolean debug = false;

		int result;
		if (bm != null) {
			if (debug) {Log.d(TAG, "saveCache  キャッシュにセーブします pathcode=" + pathcode);}
			saveThumbnailCache(pathcode, bm);
			return;
		}
		return;
	}

	private String getFilename(int index) {
		String filename = "";
		boolean ret = false;
		// ファイル情報取得
		if (index >= mFiles.size()) {
			return filename;
		}
		FileData file = mFiles.get(index);
		filename = file.getName();
		return filename;
	}

}

