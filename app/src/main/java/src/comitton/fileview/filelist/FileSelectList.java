package src.comitton.fileview.filelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.dialog.LoadingDialog;
import src.comitton.imageview.ImageManager;
import src.comitton.textview.TextManager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("DefaultLocale")
public class FileSelectList implements Runnable, Callback, DialogInterface.OnDismissListener {
	private static final String TAG = "FileSelectList";

	//標準のストレージパスを保存
	private static final String mStaticRootDir = Environment.getExternalStorageDirectory().getAbsolutePath() +"/";

	private ArrayList<FileData> mFileList = null;
	private ArrayList<FileData> mFileList2 = null;

	private String mURI;
	private String mPath;
	private String mUriPath;
	private String mUser;
	private String mPass;
	private int mSortMode = 0;
	private boolean mParentMove;
	private boolean mHidden;
	private boolean mEpubViewer;

	private boolean mImagePageDual;
	private boolean mImageTopSingle;
	private boolean mTextPageDual;

	public LoadingDialog mDialog;
	private Handler mHandler;
	private Handler mActivityHandler;
	private static AppCompatActivity mActivity;
	private static SharedPreferences mSp;
	private ImageManager mImageMgr = null;
	private TextManager mTextMgr;
	private Thread mThread;

	private static float mDensity;
	private static int mHeadSizeOrg;
	private static int mBodySizeOrg;
	private static int mRubiSizeOrg;
	private static int mInfoSizeOrg;
	private static int mMarginWOrg;
	private static int mMarginHOrg;

	private static int mPaperSel;
	private static boolean mNotice;
	private static boolean mImmEnable;
	private static int mViewRota;
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
	private static boolean mCacheFile = false;

	public FileSelectList(Handler handler, AppCompatActivity activity, SharedPreferences sp) {
		mActivityHandler = handler;
		mHandler = new Handler(this);
		mActivity = activity;
		mSp = sp;
	}

	// パス
	public void setPath(String uri, String path, String user, String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", path=" + path);
		//if (debug) {DEF.StackTrace(TAG, "setPath: ");}

		mURI = uri;
		mPath = path;
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
	public void setParams(boolean hidden, boolean parentmove, boolean epubViewer, int imageDispMode, int textDispMode, int imageViewRota, boolean imageTopSingle) {
		mHidden = hidden;
		mParentMove = parentmove;
		mEpubViewer = epubViewer;

		if (imageDispMode == DEF.DISPMODE_IM_DUAL) {
			mImagePageDual = true;
		}
		else if (imageDispMode == DEF.DISPMODE_IM_EXCHANGE && (imageViewRota == DEF.ROTATE_AUTO || imageViewRota == DEF.ROTATE_PSELAND)) {
			mImagePageDual = true;
		}
		else {
			mImagePageDual = false;
		}
		mImageTopSingle = imageTopSingle;

		if (textDispMode == DEF.DISPMODE_TX_DUAL) {
			mTextPageDual = true;
		}
		else {
			mTextPageDual = false;
		}
	}

	public ArrayList<FileData> getFileList(String marker, boolean filter, boolean applyDir) {

		if (mFileList == null) {
			return null;
		}

		ArrayList<FileData> fileList = new ArrayList<>(mFileList);

		boolean hit;
		marker = marker.toUpperCase();
		if (marker.isEmpty()) {
			// 空文字列ならnullにする
			marker = null;
		}

		for (int i = fileList.size() - 1; i >= 0; i--) {

			String name = fileList.get(i).getName();

			hit = false;
			if (marker != null) {
				if (name.toUpperCase().contains(marker)) {
					// 検索文字列が含まれる
					hit = true;
				}
				//フィルタ設定
				if(filter){
					if(!hit && !name.equals("..")){
						fileList.remove(i);
						continue;
					}
					//ディレクトリに適用する場合にリスト削除
					if(!applyDir){
						if(fileList.get(i).getType() == FileData.FILETYPE_DIR){
							fileList.remove(i);
							continue;
						}
					}

				}
			}
			fileList.get(i).setMarker(hit);
		}

		for (int i = 0; i < fileList.size(); ++i) {
			fileList.get(i).setIndex(i);
		}
		return fileList;
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

	public static void FlushFileList()
	{
		mCacheFile = false;
	}

	@SuppressLint("SuspiciousIndentation")
    @Override
	public void run() {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		Thread thread = mThread;
		boolean hidden = mHidden;

		ArrayList<FileData> fileList;
		mFileList = null;

		String currentPath = DEF.relativePath(mActivity, mURI, mPath);

		try {
			if (!mCacheFile) {
				fileList = FileAccess.listFiles(mActivity, currentPath, mUser, mPass, mHandler);

				if (thread.isInterrupted()) {
					// 処理中断
					return;
				}

				if (fileList.isEmpty()) {
					// ファイルがない場合
					Logcat.d(logLevel, "ファイルがありません.");
					fileList = new ArrayList<FileData>();
					String uri = FileAccess.parent(mActivity, mPath);
					FileData fileData;

					if (!uri.isEmpty() && mParentMove) {
						// 親フォルダを表示
						fileData = new FileData(mActivity, "..", DEF.PAGENUMBER_NONE);
						fileList.add(fileData);
					}

					// SDカードフォルダより上のフォルダの場合
					String[] SDCardPath = FileAccess.getExtSdCardPaths(mActivity);
					for (int i = 0; i < SDCardPath.length; ++i) {
						Logcat.i(logLevel, "SD Card Path=" + SDCardPath[i] + ", mURI=" + mURI + ", mPath=" + mPath + ", currentPath=" + currentPath);
						if (SDCardPath[i].startsWith(currentPath) && !SDCardPath[i].equals(currentPath)) {
							int pos = SDCardPath[i].indexOf("/", mPath.length());
							String dir = SDCardPath[i].substring(mPath.length(), pos + 1);

							//途中のフォルダを表示対象に追加
							fileData = new FileData(mActivity, dir, DEF.PAGENUMBER_UNREAD);
							if (!fileList.contains(fileData)) {
								Logcat.i(logLevel, "追加 dir=" + dir);
								fileList.add(fileData);
							}
						}
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
				mFileList2 = fileList;
				mCacheFile = true;

				mFileList = fileList;
				updateListView(thread);

			} else {
				fileList = mFileList2;
			}

			String uri = FileAccess.parent(mActivity, mPath);
			mFileList = fileList;

			Logcat.v(logLevel, "updateListView 開始します. ");
			Logcat.v(logLevel, "ArrayList 開始します. ");

			for (int i = mFileList.size() - 1; i >= 0; i--) {

				String name = mFileList.get(i).getName();
				uri = FileAccess.parent(mActivity, mPath);

				if (mFileList.get(i).getType() == FileData.FILETYPE_NONE) {
					mFileList.remove(i);
					continue;
				}
				if (mFileList.get(i).getType() == FileData.FILETYPE_EPUB_SUB) {
					mFileList.remove(i);
					continue;
				}
				if (mFileList.get(i).getType() != FileData.FILETYPE_DIR && mFileList.get(i).getType() != FileData.FILETYPE_PARENT) {
					// 通常のファイル
					if (hidden && DEF.checkHiddenFile(name)) {
						mFileList.remove(i);
						continue;
					}
				}
			}

		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
			sendResult(false, e.toString(), thread);
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
		sendResult(true, thread);
	}

	/**
	 * ファイルリストの更新
	 */
	public void updateListView(Thread thread) {
		for (int i = mFileList.size() - 1; i >= 0; i--) {
			readState(mFileList.get(i));
			if (thread != null && thread.isInterrupted()) {
				// 処理中断
				return;
			}
		}
	}

	public void readState(FileData fileData) {

		if (fileData.getType() == FileData.FILETYPE_IMG){
			fileData.setState(DEF.PAGENUMBER_NONE);
			return;
		}

		int maxpage;
		int state;

		String currentPath = DEF.relativePath(mActivity, mURI, mPath);
		String name = fileData.getName();
		String uri = DEF.createUrl(DEF.relativePath(mActivity, currentPath, name), mUser, mPass);

		String maxpageKey;
		String stateKey;
		int openmode;

		switch (fileData.getType()) {
			case FileData.FILETYPE_TXT:
				maxpageKey = uri + "#maxpage";
				stateKey = uri;
				openmode = ImageManager.OPENMODE_TEXTVIEW;
				break;
			case FileData.FILETYPE_ARC, FileData.FILETYPE_PDF, FileData.FILETYPE_DIR:
				maxpageKey = uri + "#maxpage";
				stateKey = uri;
				openmode = ImageManager.OPENMODE_VIEW;
				break;
			case FileData.FILETYPE_EPUB:
				if (mEpubViewer == DEF.TEXT_VIEWER) {
					maxpageKey = uri + "META-INF/container.xml" + "#maxpage";
					stateKey = uri + "META-INF/container.xml";
					openmode = ImageManager.OPENMODE_TEXTVIEW;
					break;
				}
				else {
					maxpageKey = uri + "#maxpage";
					stateKey = uri;
					openmode = ImageManager.OPENMODE_VIEW;
					break;
				}
            default:
				// なにもしない
				return;
		}

		maxpage = mSp.getInt(maxpageKey, DEF.PAGENUMBER_NONE);
		state = mSp.getInt(stateKey, DEF.PAGENUMBER_UNREAD);
		if (state >= 0) {
			if (maxpage == DEF.PAGENUMBER_NONE) {
				state = DEF.PAGENUMBER_NONE;
			}
			else if (state + 1 >= maxpage) {
				// ページ+1が最終ページなら既読
				state = DEF.PAGENUMBER_READ;
			}
			else if (state + 1 >= maxpage - 1) {
				// ページ+1が最終ページ-1なら 見開き表示なら既読
				if (openmode == ImageManager.OPENMODE_TEXTVIEW && mTextPageDual) {
					state = DEF.PAGENUMBER_READ;
				} else if (openmode != ImageManager.OPENMODE_TEXTVIEW && mImagePageDual && !(state == 0 && mImageTopSingle)) {
					state = DEF.PAGENUMBER_READ;
				}
			}
		}

		fileData.setMaxpage(maxpage);
		fileData.setState(state);
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

		if (msg.what == DEF.HMSG_WORKSTREAM) {
			// ファイルアクセスの表示
			return true;
		}

		// ファイルアクセス以外なら終了
		closeDialog();

		if (msg.obj != null) {
			Toast.makeText(mActivity, (String)msg.obj, Toast.LENGTH_LONG).show();
		}
		return true;
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
