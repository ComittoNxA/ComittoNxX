package src.comitton.fileview.filelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import src.comitton.common.DEF;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.dialog.LoadingDialog;
import src.comitton.imageview.ImageManager;
import src.comitton.textview.TextManager;
import src.comitton.config.SetTextActivity;
import src.comitton.imageview.MyImageView;

import android.graphics.Point;
import android.view.WindowMetrics;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("DefaultLocale")
public class FileSelectList implements Runnable, Callback, DialogInterface.OnDismissListener {
	private static final String TAG = "FileSelectList";

	//標準のストレージパスを保存
	private static final String mStaticRootDir = Environment.getExternalStorageDirectory().getAbsolutePath() +"/";

	private ArrayList<FileData> mFileList = null;

	private String mURI;
	private String mPath;
	private String mUriPath;
	private String mUser;
	private String mPass;
	private int mSortMode = 0;
	private boolean mParentMove;
	private boolean mHidden;
	private boolean mFilter;
	private boolean mApplyDir;
	private String mMarker;
	private boolean mEpubViewer;

	public LoadingDialog mDialog;
	private Handler mHandler;
	private Handler mActivityHandler;
	private static AppCompatActivity mActivity;
	private static SharedPreferences mSp;
	private ImageManager mImageMgr = null;
	private TextManager mTextMgr;
	private Thread mThread;
	private MyImageView mImageView = null;

	private static float mDensity;
	private static int mHeadSizeOrg;
	private static int mBodySizeOrg;
	private static int mRubiSizeOrg;
	private static int mInfoSizeOrg;
	private static int mMarginWOrg;
	private static int mMarginHOrg;

	private static int mPaperSel;
	private static int mTextWidth;
	private static int mTextHeight;
	private static int mHeadSize;
	private static int mBodySize;
	private static int mRubiSize;
	private static int mInfoSize;
	private static int mPicSize;
	private static int mSpaceW;
	private static int mSpaceH;
	private static int mMarginW;
	private static int mMarginH;

	private static int mAscMode;	// 半角の表示方法
	private static String mFontFile;
	private static boolean mChangeTextSize = false;

	public FileSelectList(Handler handler, AppCompatActivity activity, SharedPreferences sp) {
		mActivityHandler = handler;
		mHandler = new Handler(this);
		mActivity = activity;
		mSp = sp;
	}

	// パス
	public void setPath(String uri, String path, String user, String pass) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "setPath: 開始します. uri=" + uri + ", path=" + path);}
		//if (debug) {DEF.StackTrace(TAG, "setPath: ");}

		mURI = uri;
		mPath = path;
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		mUser = user;
		mPass = pass;
	}

	// ソートモード
	public void setMode(int mode) {
		mSortMode = mode;
		if (mFileList != null) {
			// ソートあり設定の場合
			Collections.sort(mFileList, new MyComparator());
		}
	}

	// リストモード
	public void setParams(boolean hidden, String marker, boolean filter, boolean applydir, boolean parentmove, boolean epubViewer) {
		mHidden = hidden;
		mMarker = marker;
		mFilter = filter;
		mApplyDir = applydir;
		mParentMove = parentmove;
		mEpubViewer = epubViewer;
	}

	public ArrayList<FileData> getFileList() {
		return mFileList;
	}

	public void setFileList(ArrayList<FileData> filelist) {
		mFileList = filelist; 
	}

	public void loadFileList() {
		mDialog = new LoadingDialog(mActivity);
		mDialog.setOnDismissListener(this);
		mDialog.show();

		// サムネイルスレッド開始
		if (mThread != null) {
			// 起動中のスレッドあり
			return;
		}

		mThread = new Thread(this);
		mThread.start();
		return;
	}

	public static void SetReadConfig(SharedPreferences msp, TextManager manager)	{
		mSpaceW = SetTextActivity.getSpaceW(msp);
		mSpaceH = SetTextActivity.getSpaceH(msp);
		mHeadSizeOrg = SetTextActivity.getFontTop(msp);	// 見出し
		mBodySizeOrg = SetTextActivity.getFontBody(msp);	// 本文
		mRubiSizeOrg = SetTextActivity.getFontRubi(msp);	// ルビ
		mInfoSizeOrg = SetTextActivity.getFontInfo(msp);	// ページ情報など
		mMarginWOrg = SetTextActivity.getMarginW(msp);	// 左右余白(設定値)
		mMarginHOrg = SetTextActivity.getMarginH(msp);	// 上下余白(設定値)
		mDensity = mActivity.getResources().getDisplayMetrics().scaledDensity;
		mHeadSize = DEF.calcFontPix(mHeadSizeOrg, mDensity);	// 見出し
		mBodySize = DEF.calcFontPix(mBodySizeOrg, mDensity);	// 本文
		mRubiSize = DEF.calcFontPix(mRubiSizeOrg, mDensity);	// ルビ
		mInfoSize = DEF.calcFontPix(mInfoSizeOrg, mDensity);	// ページ情報など
		mPicSize = SetTextActivity.getPicSize(msp);	// 挿絵サイズ

		mMarginW = DEF.calcDispMargin(mMarginWOrg);				// 左右余白
		mMarginH = mInfoSize + DEF.calcDispMargin(mMarginHOrg);	// 上下余白
		mAscMode = SetTextActivity.getAscMode(msp);
		String fontname = SetTextActivity.getFontName(msp);
		if (fontname != null && fontname.length() > 0) {
			String path = DEF.getFontDirectory();
			mFontFile = path + fontname;
		}
		else {
			mFontFile = null;
		}
		mPaperSel = SetTextActivity.getPaper(msp); // 用紙サイズ
		if (mPaperSel == DEF.PAPERSEL_SCREEN) {
			int cx;
			int cy;
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
				Point point = new Point();
				mActivity.getWindowManager().getDefaultDisplay().getRealSize(point);
				cx = point.x;
				cy = point.y;
			}else{
				WindowMetrics wm = mActivity.getWindowManager().getCurrentWindowMetrics();
				cx = wm.getBounds().width();
				cy = wm.getBounds().height();
			}

			if (cx < cy) {
				mTextWidth = cx;
				mTextHeight = cy;
			}
			else {
				mTextWidth = cy;
				mTextHeight = cx;
			}
		}
		else {
			mTextWidth = DEF.PAPERSIZE[mPaperSel][0];
			mTextHeight = DEF.PAPERSIZE[mPaperSel][1];
		}
		manager.formatTextFile(mTextWidth, mTextHeight, mHeadSize, mBodySize, mRubiSize, mSpaceW, mSpaceH, mMarginW, mMarginH, mPicSize, mFontFile, mAscMode);
	}

	public static void ChangeTextSize()
	{
		mChangeTextSize = true;
	}

	@Override
	public void run() {
		boolean debug = false;
		String name;
		int maxpage;
		int state;
		long size;
		long date;
		long nowdate;
		boolean hit;

		Thread thread = mThread;
		boolean hidden = mHidden;
		String marker = mMarker.toUpperCase();
		if (marker.isEmpty()) {
			// 空文字列ならnullにする
			marker = null;
		}
		
		ArrayList<FileData> fileList;
		mFileList = null;
		String currentPath = DEF.relativePath(mActivity, mURI, mPath);

		try {
			fileList = FileAccess.listFiles(mActivity, currentPath, mUser, mPass, mHandler);

			if (thread.isInterrupted()) {
				// 処理中断
				return;
			}

			if (fileList.isEmpty()) {
				// ファイルがない場合
				Log.d(TAG, "run ファイルがありません.");
				fileList = new ArrayList<FileData>();
				String uri = FileAccess.parent(mActivity, mPath);
				FileData fileData;

				if (!uri.isEmpty() && mParentMove) {
					// 親フォルダを表示
					fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
					fileList.add(fileData);
				}

				// ローカルの初期フォルダより上のフォルダの場合
				if(debug) {Log.d(TAG, "run: mStaticRootDir=" + mStaticRootDir + ", mURI=" + mURI + ", mPath=" + mPath + ", currentPath=" + currentPath);}
				if (mStaticRootDir.startsWith(currentPath) && !mStaticRootDir.equals(currentPath)) {
					int pos = mStaticRootDir.indexOf("/", mPath.length());
					String dir = mStaticRootDir.substring(mPath.length(), pos + 1);

					//途中のフォルダを表示対象に追加
					fileData = new FileData(mActivity, dir, DEF.PAGENUMBER_UNREAD);
					fileList.add(fileData);
				}

				// 処理中断
				sendResult(true, thread);
				mFileList = fileList;
				return;
			}

			String uri = FileAccess.parent(mActivity, mPath);
			if (!uri.isEmpty() && mParentMove) {
				FileData fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
				fileList.add(0, fileData);
			}

			for (int i = fileList.size() - 1; i >= 0; i--) {

				name = fileList.get(i).getName();
				uri = DEF.relativePath(mActivity, currentPath, name);

				if (fileList.get(i).getType() == FileData.FILETYPE_TXT) {
					maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
					state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					if (state > 0) {
						nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
						date = fileList.get(i).getDate();
						if ((nowdate != ((date / 1000))) || (mChangeTextSize)) {
							int openmode = 0;
							// ファイルリストの読み込み
							openmode = ImageManager.OPENMODE_TEXTVIEW;
							mImageMgr = new ImageManager(this.mActivity, currentPath, "", mUser, mPass, 0, mHandler, mHidden, openmode, 1);
							mImageMgr.LoadImageList(0, 0, 0);
							mTextMgr = new TextManager(mImageMgr, name, mUser, mPass, mHandler, mActivity, FileData.FILETYPE_TXT);
							SetReadConfig(mSp, mTextMgr);
							maxpage = mTextMgr.length();
							SharedPreferences.Editor ed = mSp.edit();
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
							ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
							ed.apply();
							releaseManager();
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state + 1 >= maxpage - 1) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						else {
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state + 1 >= maxpage - 1) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						fileList.get(i).setSize(size);
					}
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_ARC
						|| fileList.get(i).getType() == FileData.FILETYPE_PDF) {
					maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
					state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					fileList.get(i).setState(state);
					if	(state > 0)	{
						nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
						date = fileList.get(i).getDate();
						if (nowdate != ((date / 1000)))	{
							int openmode = 0;
							// ファイルリストの読み込み
							openmode = ImageManager.OPENMODE_VIEW;
							// 設定の読み込み
							mImageMgr = new ImageManager(this.mActivity, currentPath, name, mUser, mPass, 0, mHandler, mHidden, openmode, 1);
							mImageMgr.LoadImageList(0, 0, 0);
							maxpage = mImageMgr.length();
							SharedPreferences.Editor ed = mSp.edit();
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
							ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
							ed.apply();
							releaseManager();
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state + 1 >= maxpage - 1) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						else {
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state + 1 >= maxpage - 1) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						fileList.get(i).setSize(size);
					}
					fileList.get(i).setState(state);
				}
				if (fileList.get(i).getType() == FileData.FILETYPE_EPUB) {
					if (DEF.TEXT_VIEWER == mEpubViewer) {
						maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml" + "#maxpage", DEF.PAGENUMBER_NONE);
						state = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml", DEF.PAGENUMBER_UNREAD);
						if	(state > 0)	{
							nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
							date = fileList.get(i).getDate();
							if ((nowdate != ((date / 1000))) || (mChangeTextSize))	{
								int openmode = 0;
								// ファイルリストの読み込み
								openmode = ImageManager.OPENMODE_TEXTVIEW;
								if (debug) {Log.d(TAG,"run: mUri + mPath=" + mURI + mPath + ", name=" + name);}
								mImageMgr = new ImageManager(this.mActivity, mURI + mPath, name, mUser, mPass, 0, mHandler, mHidden, openmode, 1);
								mImageMgr.LoadImageList(0, 0, 0);
								mTextMgr = new TextManager(mImageMgr, "META-INF/container.xml", mUser, mPass, mHandler, mActivity, FileData.FILETYPE_EPUB);
								SetReadConfig(mSp, mTextMgr);
								maxpage = mTextMgr.length();
								SharedPreferences.Editor ed = mSp.edit();
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml" + "#maxpage", maxpage);
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "META-INF/container.xml", state);
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
								ed.apply();
								releaseManager();
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state + 1 >= maxpage - 1) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							else	{
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state + 1 >= maxpage - 1) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							fileList.get(i).setSize(size);
						}
					}
					else {
						maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
						state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
						if	(state > 0)	{
							nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
							date = fileList.get(i).getDate();
							if ((nowdate != ((date / 1000))) || (mChangeTextSize))	{
								int openmode = 0;
								// ファイルリストの読み込み
								openmode = ImageManager.OPENMODE_VIEW;
								mImageMgr = new ImageManager(this.mActivity, currentPath, "", mUser, mPass, 0, mHandler, mHidden, openmode, 1);
								mImageMgr.LoadImageList(0, 0, 0);
								maxpage = mImageMgr.length();
								SharedPreferences.Editor ed = mSp.edit();
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
								ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
								ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
								ed.apply();
								releaseManager();
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state + 1 >= maxpage - 1) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							else {
								if (maxpage == DEF.PAGENUMBER_NONE) {
									state = DEF.PAGENUMBER_UNREAD;
									size = DEF.PAGENUMBER_NONE;
								} else if (state + 1 >= maxpage - 1) {
									// 0から始まるので+1、見開きの分で-1
									state = DEF.PAGENUMBER_READ;
									size = maxpage;
								} else {
									size = maxpage;
								}
							}
							fileList.get(i).setSize(size);
						}
					}
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_DIR) {
					maxpage = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
					state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					fileList.get(i).setState(state);
					if	(state > 0)	{
						nowdate = mSp.getInt(DEF.createUrl(uri, mUser, mPass) + "#date", DEF.PAGENUMBER_UNREAD);
						date = fileList.get(i).getDate();
						if (nowdate != ((date / 1000)))	{
							int openmode = 0;
							// ファイルリストの読み込み
							openmode = ImageManager.OPENMODE_VIEW;
							// 設定の読み込み
							mImageMgr = new ImageManager(this.mActivity, currentPath, name, mUser, mPass, 0, mHandler, mHidden, openmode, 1);
							mImageMgr.LoadImageList(0, 0, 0);
							maxpage = mImageMgr.length();
							SharedPreferences.Editor ed = mSp.edit();
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#maxpage", maxpage);
							ed.putInt(DEF.createUrl(uri, mUser, mPass), state);
							ed.putInt(DEF.createUrl(uri, mUser, mPass) + "#date", (int)((date / 1000)));
							ed.apply();
							releaseManager();
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state + 1 >= maxpage - 1) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						else	{
							if (maxpage == DEF.PAGENUMBER_NONE) {
								state = DEF.PAGENUMBER_UNREAD;
								size = DEF.PAGENUMBER_NONE;
							} else if (state + 1 >= maxpage - 1) {
								// 0から始まるので+1、見開きの分で-1
								state = DEF.PAGENUMBER_READ;
								size = maxpage;
							} else {
								size = maxpage;
							}
						}
						fileList.get(i).setSize(size);
					}
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_IMG){
					state = DEF.PAGENUMBER_NONE;
					fileList.get(i).setState(state);
				}

				if (fileList.get(i).getType() == FileData.FILETYPE_NONE){
					fileList.remove(i);
					continue;
				}
				if (fileList.get(i).getType() == FileData.FILETYPE_EPUB_SUB){
					fileList.remove(i);
					continue;
				}
				if (fileList.get(i).getType() != FileData.FILETYPE_DIR && fileList.get(i).getType() != FileData.FILETYPE_PARENT) {
					// 通常のファイル
					if (hidden && DEF.checkHiddenFile(name)) {
						fileList.remove(i);
						continue;
					}
				}

				hit = false;
				if (marker != null) {
					if (name.toUpperCase().contains(marker)) {
						// 検索文字列が含まれる
						hit = true;
					}
					//フィルタ設定
					if(mFilter){
						if(!hit){
							fileList.remove(i);
							continue;
						}
						//ディレクトリに適用する場合にリスト削除
						if(!mApplyDir){
							if(fileList.get(i).getType() == FileData.FILETYPE_DIR){
								fileList.remove(i);
								continue;
							}
						}
					}
				}
				fileList.get(i).setMarker(hit);

				if (thread.isInterrupted()) {
					// 処理中断
					return;
				}
			}
			mChangeTextSize = false;
		}
		catch (Exception e) {
			String s = null;
            s = e.getLocalizedMessage();
            if (s != null) {
                Log.e(TAG, s);
            }
            else {
                s = "error.";
            }
            e.printStackTrace();
			sendResult(false, s, thread);
			return;
		}

		if (thread.isInterrupted()) {
			// 処理中断
			return;
		}

		// sort
		if (mSortMode != 0) {
			// ソートあり設定の場合
			Collections.sort(fileList, new MyComparator());
		}

		if (thread.isInterrupted()) {
			// 処理中断
			return;
		}
		mFileList = fileList;
		sendResult(true, thread);
	}

	public class MyComparator implements Comparator<FileData> {
		public int compare(FileData file1, FileData file2) {

			int result;
			// ディレクトリ/ファイルタイプ
			int type1 = file1.getType();
			int type2 = file2.getType();
			if (type1 == FileData.FILETYPE_PARENT || type2 == FileData.FILETYPE_PARENT) {
				return type1 - type2;
			}
			else if (mSortMode == DEF.ZIPSORT_FILESEP || mSortMode == DEF.ZIPSORT_NEWSEP || mSortMode == DEF.ZIPSORT_OLDSEP) {
				// IMAGEとZIPのソート優先度は同じにする
				if (type1 == FileData.FILETYPE_IMG || type1 == FileData.FILETYPE_TXT) {
					type1 = FileData.FILETYPE_ARC;
				}
				if (type2 == FileData.FILETYPE_IMG || type2 == FileData.FILETYPE_TXT) {
					type2 = FileData.FILETYPE_ARC;
				}

				result = type1 - type2;
				if (result != 0) {
					return result;
				}
			}
			switch (mSortMode) {
				case DEF.ZIPSORT_FILEMGR:
				case DEF.ZIPSORT_FILESEP:
//					return file1.getName().toUpperCase().compareTo(file2.getName().toUpperCase());
					return DEF.compareFileName(file1.getName(), file2.getName());
				case DEF.ZIPSORT_NEWMGR:
				case DEF.ZIPSORT_NEWSEP:
				{
					long val = file2.getDate() - file1.getDate();
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
				case DEF.ZIPSORT_OLDMGR:
				case DEF.ZIPSORT_OLDSEP:
				{
					long val = file1.getDate() - file2.getDate();
					return val == 0 ? 0 : (val > 0 ? 1 : -1);
				}
			}
			return 0;
		}
	}
	
	private void sendResult(boolean result, Thread thread) {
		sendResult(result, result ? null : "User Cancelled.", thread);
	}

	private void sendResult(boolean result, String str, Thread thread) {
		if (mThread != null) {
			if (mThread == thread) {
				if (!result) {
					mFileList = new ArrayList<FileData>();
					if (mParentMove) {
						String uri = FileAccess.parent(mActivity, mPath);
    					FileData fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
    					mFileList.add(fileData);
					}
				}

				Message message;
				message = new Message();
				message.what = DEF.HMSG_LOADFILELIST;
				message.arg1 = result ? 1 : 0;
				mActivityHandler.sendMessage(message);

				message = new Message();
				message.what = DEF.HMSG_LOADFILELIST;
				message.arg1 = result ? 1 : 0;
				message.obj = str;
				mHandler.sendMessage(message);
			}
			mThread = null;
		}
	}

	public void closeDialog() {
		if (mDialog != null) {
			try {
				mDialog.dismiss();
			}
			catch (IllegalArgumentException e) {
				;
			}
			mDialog = null;
		}
	}

	@Override
	public void onDismiss(DialogInterface di) {
		// 閉じる
		if (mDialog != null) {
			mDialog = null;
			// 割り込み
			if (mThread != null) {
				mThread.interrupt();

				// キャンセル時のみ
				sendResult(false, mThread);
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// 終了
		closeDialog();
		if (msg.what == DEF.HMSG_WORKSTREAM) {
			// ファイルアクセスの表示
			return true;
		}
		if (msg.obj != null) {
			Toast.makeText(mActivity, (String)msg.obj, Toast.LENGTH_LONG).show();
		}
		return false;
	}

	// ImageManager と TextManager を解放する
	private void releaseManager() {
		// 読み込み終了
		if (mImageMgr != null) {
			try {
				mImageMgr.close();
			} catch (IOException e) {
				;
			}
			mImageMgr = null;
		}
		if (mTextMgr != null) {
			mTextMgr.release();
			mTextMgr = null;
		}
	}
}
