package src.comitton.fileview.filelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import src.comitton.common.DEF;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.dialog.LoadingDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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
	private Activity mActivity;
	private SharedPreferences mSp;
	private Thread mThread;

	public FileSelectList(Handler handler, Activity activity, SharedPreferences sp) {
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

	@Override
	public void run() {
		boolean debug = false;
		String name;
		int state;
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
				uri = DEF.relativePath(mActivity, currentPath, fileList.get(i).getName());

				if (fileList.get(i).getType() == FileData.FILETYPE_ARC
						|| fileList.get(i).getType() == FileData.FILETYPE_PDF
						|| fileList.get(i).getType() == FileData.FILETYPE_TXT
						|| fileList.get(i).getType() == FileData.FILETYPE_DIR) {
					state = (int)mSp.getFloat(DEF.createUrl(uri, mUser, mPass) + "#pageRate", (float)DEF.PAGENUMBER_UNREAD);
					if (state == DEF.PAGENUMBER_UNREAD) {
						state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
					}
					fileList.get(i).setState(state);
				}
				if (fileList.get(i).getType() == FileData.FILETYPE_EPUB) {
					if (DEF.TEXT_VIEWER == mEpubViewer) {
                        state = (int)mSp.getFloat(DEF.createUrl(uri + "META-INF/container.xml", mUser, mPass) + "#pageRate", (float)DEF.PAGENUMBER_UNREAD);
                        if (state == DEF.PAGENUMBER_UNREAD) {
                            state = mSp.getInt(DEF.createUrl(uri + "META-INF/container.xml", mUser, mPass), DEF.PAGENUMBER_UNREAD);
                        }
					}
					else {
                        state = mSp.getInt(DEF.createUrl(uri, mUser, mPass), DEF.PAGENUMBER_UNREAD);
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
		if (msg.obj != null) {
			Toast.makeText(mActivity, (String)msg.obj, Toast.LENGTH_LONG).show();
		}
		return false;
	}
}
