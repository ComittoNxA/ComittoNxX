package src.comitton.fileview;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import jp.dip.muracoro.comittonx.BuildConfig;
import jp.dip.muracoro.comittonx.R;
import src.comitton.expandview.ExpandActivity;
import src.comitton.fileview.data.ServerData;
import src.comitton.helpview.HelpActivity;
import src.comitton.textview.TextActivity;
import src.comitton.common.DEF;
import src.comitton.cropimageview.CropImageActivity;
import src.comitton.fileaccess.FileAccess;
import src.comitton.common.ImageAccess;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetConfigActivity;
import src.comitton.config.SetEpubActivity;
import src.comitton.config.SetFileColorActivity;
import src.comitton.config.SetFileListActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.config.SetImageText;
import src.comitton.config.SetRecorderActivity;
import src.comitton.fileview.data.FileData;
import src.comitton.fileview.data.RecordItem;
import src.comitton.dialog.BookmarkDialog;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.DownloadDialog;
import src.comitton.dialog.EditServerDialog;
import src.comitton.dialog.Information;
import src.comitton.dialog.ListDialog;
import src.comitton.dialog.MarkerInputDialog;
import src.comitton.dialog.RemoveDialog;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;
import src.comitton.dialog.ListDialog.ListSelectListener;
import src.comitton.dialog.RemoveDialog.RemoveListener;
import src.comitton.dialog.TextInputDialog;
import src.comitton.fileaccess.FileAccessException;
import src.comitton.fileview.filelist.FileSelectList;
import src.comitton.fileview.filelist.RecordList;
import src.comitton.fileview.filelist.ServerSelect;
import src.comitton.imageview.ImageActivity;
import src.comitton.common.ThumbnailLoader;
import src.comitton.fileview.view.list.FileListArea;
import src.comitton.fileview.view.list.ListNoticeListener;
import src.comitton.fileview.view.list.ListScreenView;
import src.comitton.fileview.view.list.RecordListArea;
import src.comitton.fileview.view.list.TitleArea;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.storage.StorageManager;
import androidx.preference.PreferenceManager;

import android.provider.DocumentsContract;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

@SuppressLint("DefaultLocale")
public class FileSelectActivity extends AppCompatActivity implements OnTouchListener, ListNoticeListener, BookmarkListenerInterface, Handler.Callback {
	private static final String TAG = "FileSelectActivity";

	private static final int OPERATE_NONREAD = 0;
	private static final int OPERATE_READ = 1;
	private static final int OPERATE_OPEN = 2;
	private static final int OPERATE_DEL = 3;
	private static final int OPERATE_DOWN = 4;
	private static final int OPERATE_EXPAND = 5;
	private static final int OPERATE_RENAME = 6;
	private static final int OPERATE_DELCACHE = 7;
	private static final int OPERATE_FIRST = 8;
	private static final int OPERATE_EPUB = 9;
	private static final int OPERATE_SETTHUMBASDIR = 100;
	private static final int OPERATE_SETTHUMBCROPPED = 101;

	private static final int BACKMODE_EXIT = 0;
	private static final int BACKMODE_PARENT = 1;
	private static final int BACKMODE_HISTORY = 2;

	private ListScreenView mListScreenView;

	private float mDensity;

	private FileSelectList mFileList = null;

	private boolean mIsLoading = false;
	private String mLoadListCursor;
	private int mLoadListTopIndex;
	private int mLoadListNextOpen;
	private String mLoadListNextPath;
	private String mLoadListNextFile;
	private String mLoadListNextInFile;
	private float mLoadListNextPageRate;
	private int mLoadListNextPage;

	// ダイアログ情報
	private Information mInformation;
	private ListDialog mListDialog;
	private MarkerInputDialog mMarkerInputDialog;
	private TextInputDialog mTextInputDialog;
	EditServerDialog mEditServerDialog;

	// ダイアログ表示中に選択項目を記憶しておくのに使用
	private FileData mFileData = null;
	private RecordItem mSelectRecord = null;
	private int mSelectPos;
	private int mOperate[] = {-1, -1, -1, -1, -1, -1, -1, -1,-1,-1,-1};

	private String mURI = "";
	private String mPath = "";
	private ServerSelect mServer;
	private int mSortMode;
	private int mFontTitle;
	private int mFontMain;
	private int mFontSub;
	private int mFontTile;
	private int mItemMargin;
	private int mBackMode;

	private int mDirColor;
	private int mBefColor;
	private int mNowColor;
	private int mAftColor;
	private int mImgColor;
	private int mTxtColor;
	private int mInfColor;
	private int mBakColor;
	private int mMrkColor;
	private int mCurColor;
	private int mTitColor;
	private int mTibColor;
	private int mTldColor;
	private int mTlbColor;

	private boolean mTapExpand; // タップで展開

	private int mListRota;
	private int mRotateBtn;
	private boolean mListRotaChg;

	private boolean mHidden;
	private boolean mThumbSort;
	private boolean mParentMove;
	private boolean mClearTop;
	private boolean mShowExt;
	private boolean mSplitFilename;
	private int mMaxLines;
	private int mShowDelMenu;
	private int mShowRenMenu;
	private boolean mEpubViewer;
	private boolean mEpubThumb;

	private boolean mResumeOpen;
	private boolean mThumbnail;
	private boolean mUseThumbnailTap;
	private int mDuration = 800; // 長押し時間
	// private ThumbnailLoad mThumbnailLoad;
	private FileThumbnailLoader mThumbnailLoader;
	private Handler mHandler;
	private int mThumbSizeW;
	private int mThumbSizeH;
	private int mThumbNum;
	private int mThumbCrop;
	private int mThumbMargin;
	private short mListMode;
	private int mListThumbSizeH;

	private boolean mToolbarShow = true;
	private boolean mToolbarLabel;
	private boolean mSelectorShow = true;

	private int mToolbarSize;
	private short mListType[];
	private String mMarker;
	private boolean mFilter;
	private boolean mApplyDir;

	private short mSortType;
	private int mHistCount;
	private boolean mLocalSave;
	private boolean mSambaSave;

	private SharedPreferences mSharedPreferences;
	private FileSelectActivity mActivity;

	private PathHistory mPathHistory;

	private long mExitBackTimer;

	private long mThumbID;
	private int mFileFirstIndex;
	private int mFileLastIndex;

	private View mEditDlg = null;
	private int mInitialize = 0;

	private StorageManager mStorageManager;

	private static final int REQUEST_CODE = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		boolean debug = false;
		super.onCreate(savedInstanceState);

		// 設定の読込
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = mSharedPreferences.edit();
		try {
			mInitialize = mSharedPreferences.getInt("Initialize", 0);
			if (mInitialize >= 3) {
				// 3回連続で起動処理中が最後まで実行されなかった
				// ローカルはストレージルートにリセット
				String path = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
				ed.putString("path", path);
				// 表示モードはリスト表示(サムネイルOFF)にセット
				ed.putInt("ListMode", FileListArea.LISTMODE_LIST);
				ed.putBoolean("Thumbnail", false);
				ed.putInt("Initialize", 1);
			} else {
				// 起動処理の実行回数を保存
				// あとで起動処理が正常に終了したら回数をリセットする
				ed.putInt("Initialize", mInitialize + 1);
			}
			ed.apply();
		}
		catch (Exception e){
			Log.d("FileSelectAvtivity", e.getLocalizedMessage());
		}

		mActivity = this;
		mDensity = getResources().getDisplayMetrics().scaledDensity;
		mInformation = new Information(this);

		// カメラボタンで縦横切替した場合
		if (savedInstanceState != null) {
			mListRotaChg = savedInstanceState.getBoolean("Rotate");
		}

		// 設定の読込
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SetCommonActivity.loadSettings(mSharedPreferences);
		readConfig();

		mHandler = new Handler(this);
		if (mPathHistory == null) {
			// パス遷移の記録
			mPathHistory = new PathHistory();
		}

		FrameLayout layout = new FrameLayout(this);
		mListScreenView = new ListScreenView(this, mDuration);
		layout.addView(mListScreenView);
		setContentView(layout);

		// リストモードの設定
		mListMode = (short) mSharedPreferences.getInt("ListMode", FileListArea.LISTMODE_LIST);
		mThumbnail = mSharedPreferences.getBoolean("Thumbnail", false);
		saveListMode(false);

		mListScreenView.setOnTouchListener(this);

		mListScreenView.mTitleArea.setTextSize(mFontTitle, mTitColor, mTibColor);

		mListScreenView.mToolbarArea.setDisplay(mToolbarShow, mToolbarSize, mToolbarLabel, mTldColor, mTlbColor);

		mListScreenView.setDrawColor(mDirColor, mImgColor, mBefColor, mNowColor, mAftColor, mBakColor, mCurColor, mMrkColor, mTlbColor, mTxtColor, mInfColor);
		mListScreenView.setDrawInfo(mFontTile, mFontMain, mFontSub, mItemMargin, mShowExt, mSplitFilename, mMaxLines);
		mListScreenView.setListType(mListType);
		mListScreenView.setListSortType(RecordList.TYPE_FILELIST, mSortMode); // ソート状態を設定
		mListScreenView.mFileListArea.setThumbnail(mThumbnail, mThumbSizeW, mThumbSizeH, mListThumbSizeH);
		mListScreenView.mFileListArea.setListMode(mListMode); // タイル/リストの設定
		// mListScreenView.mFileListArea.update(true);
		mListScreenView.setListNoticeListener(this);

		mListScreenView.mSelectorArea.setConfig(mSelectorShow, mToolbarSize, mToolbarLabel, mListType, mTldColor, mTlbColor);
		mListScreenView.mSelectorArea.setSelect(0);

		// 回転時など保存しておいた情報
		mServer = new ServerSelect(mSharedPreferences, this);
		mServer.select(DEF.INDEX_LOCAL);
		RecordList.setContext(this);

		// Intentに保存されたデータを取り出す
		Intent intent = getIntent();

		try {
			String action = intent.getAction();
			String type = intent.getType();
			Bundle extras = intent.getExtras();
			ComponentName component = intent.getComponent();
			Uri contentUri = null;
			if (debug) {Log.d(TAG, "onCreate: Intent解析中. action=" + action + ", type=" + type + ", extras=" + extras + ", component=" + component);}

			if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) {
				// 他のアプリから呼び出された場合

				if (Intent.ACTION_SEND.equals(action)) {
					if (debug) {Log.d(TAG, "onCreate: Intent.ACTION_SEND");}
					contentUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
					if (contentUri != null) {
						if (debug) {Log.d(TAG, "onCreate: contentUri=" + contentUri.getPath());}
					}
				} else if (Intent.ACTION_VIEW.equals(action)) {
					if (debug) {Log.d(TAG, "onCreate: Intent.ACTION_VIEW");}
					contentUri = intent.getData();
					if (contentUri != null) {
						if (debug) {Log.d(TAG, "onCreate: contentUri=" + contentUri.getPath());}
					}
				}

				// 起動処理終了を保存
				if (mInitialize != 0) {
					ed.putInt("Initialize", 0);
					ed.apply();
					mInitialize = 0;
				}

				if (contentUri != null) {
					String path = contentUri.getPath();
					intent.putExtra("Uri", mURI);
					intent.putExtra("Path", mPath);
					mServer = new ServerSelect(mSharedPreferences, this);
					mServer.select(DEF.INDEX_LOCAL);
					mURI = mServer.getURI();
					mPath = path.substring(0, path.lastIndexOf('/') + 1);
					FileData fileData = new FileData(mActivity, path.substring(path.lastIndexOf('/') + 1));
					if (debug) {
						Log.d(TAG, "onCreate: Intent解析中. mPath=" + mPath + ", name=" + fileData.getName());
					}

					openFile(fileData, "", (float) DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD, mEpubViewer);
				}
			}

		} catch (Exception e) {
			Log.e(TAG, "onCreate: Intent解析中にエラーが発生しました. " + e.getLocalizedMessage());
		}

		String path = "";
		String server = "";
		int serverSelect;
		// ローカルパス取得
		if (savedInstanceState != null) {
			// レジュームから復帰
			path = savedInstanceState.getString("Path");
			server = savedInstanceState.getString("Server");
			serverSelect = savedInstanceState.getInt("ServerSelect", -2);
			if (debug) {Log.d(TAG, "onCreate: レジューム復帰. path=" + path + ", server=" + server + ", serverSelect=" + serverSelect);}
		}
		else {
			// ショートカットから起動
			path = intent.getStringExtra("Path");
			server = intent.getStringExtra("Server");
			serverSelect = intent.getIntExtra("ServerSelect", -2);
			if (debug) {Log.d(TAG, "onCreate: ショートカットから起動. path=" + path + ", server=" + server + ", serverSelect=" + serverSelect);}

			// 起動処理終了を保存
			if (mInitialize != 0) {
				ed.putInt("Initialize", 0);
				ed.apply();
				mInitialize = 0;
			}
		}

		// レジューム起動チェック
		if (path == null) {
			// アイコンから起動(ショートカットや回転ではない)とき
			if (mResumeOpen && savedInstanceState == null && intent.getStringExtra("Refresh") == null) {
				// 初回起動のみ(回転時などは行わない)
				int lastView = mSharedPreferences.getInt("LastOpen", -1);
				if (lastView != DEF.LASTOPEN_NONE) {
					showDialog(DEF.MESSAGE_RESUME);
				}
			}
		}

		if (path != null && !path.isEmpty()) {
			mPath = path;
		}

		// サーバパス
		if (serverSelect != -2) {
			if (mServer.select(serverSelect)) {
				mURI = mServer.getURI();
			}
		} else if (server != null && !server.isEmpty()) {
			if (mServer.select(server)) {
				mURI = mServer.getURI();
			} else {
				mPath = "";
				Resources res = getResources();
				Toast.makeText(this, res.getString(R.string.svNotFound), Toast.LENGTH_LONG).show();
			}
		}

		// 画面リフレッシュ実施時
		int topindex = 0;
		if (savedInstanceState != null) {
			mMarker = savedInstanceState.getString("Marker");
			topindex = savedInstanceState.getInt("TopIndex", 0);
			savedInstanceState.clear();
		}
		else {
			mMarker = intent.getStringExtra("Marker");
		}
		if (mMarker == null) {
			mMarker = "";
		}

		String cursor = intent.getStringExtra("Cursor");

		if (mPath == null || mPath.isEmpty()) {
			mServer.select(DEF.INDEX_LOCAL);
			mURI = mServer.getURI();
			mPath = mServer.getPath();
		}

		if (mServer.getSelect() == DEF.INDEX_LOCAL && "/".equals(mPath)) {
			// ローカルのルートフォルダ
			File current_dir = new File(mPath);
			if (!current_dir.canRead()) {
				// 読み取り権限がない
				mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
			}
		}

		// ファイルリスト
		mFileList = new FileSelectList(mHandler, this, mSharedPreferences);
		mFileList.setMode(mSortMode);

		// 回転前にリストがなければ再読込
		if (cursor != null) {
			loadListView(cursor);
		}
		else {
			loadListView(topindex);
		}

		PackageManager packageManager = this.getPackageManager();
		String verName = null;
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			verName = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			// 取得不可エラー
		}

		ArrayList<String> permissions = new ArrayList<String>(0);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (debug) {Log.d(TAG, "onCreate: Android11(R)以降のバージョンです.");}
			//==== パーミッション承認状態判定(外部ストレージ) ====//
			if (!Environment.isExternalStorageManager()){
				if (debug) {Log.d(TAG, "onCreate: 外部ストレージアクセス権限がありません.");}
				//==== ユーザに自分で権限リストを設定してもらう ====//
				intent = new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
				startActivityForResult(intent, DEF.APP_STORAGE_ACCESS_REQUEST_CODE);
			}else{
				if (debug) {Log.d(TAG, "onCreate: 外部ストレージアクセス権限があります.");}
			}
		} else {
			if (debug) {Log.d(TAG, "onCreate: Android10(Q)以前のバージョンです.");}

			//==== パーミッション承認状態判定(読み込み) ====//
			if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			{
				if (debug) {Log.d(TAG, "onCreate: READ_EXTERNAL_STORAGE 権限がありません.");}
				//==== 承認要求する権限リストに追加 ====//
				permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			else {
				if (debug) {Log.d(TAG, "onCreate: READ_EXTERNAL_STORAGE 権限があります.");}
			}

			//==== パーミッション承認状態判定(書き込み) ====//
			if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				if (debug) {Log.d(TAG, "onCreate: WRITE_EXTERNAL_STORAGE 権限がありません.");}
				//==== 承認要求する権限リストに追加 ====//
				permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			} else {
				if (debug) {Log.d(TAG, "onCreate: WRITE_EXTERNAL_STORAGE 権限があります.");}
			}
		}

		//==== パーミッション承認状態判定(マイク使用) ====//
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
		{
			if (debug) {Log.d(TAG, "onCreate: RECORD_AUDIO 権限がありません.");}
			//==== 承認要求する権限リストに追加 ====//
			permissions.add(Manifest.permission.RECORD_AUDIO);
		} else {
			if (debug) {Log.d(TAG, "onCreate: RECORD_AUDIO 権限があります.");}
		}

		if (permissions.size() > 0) {
			//==== まとめて承認要求を行う ====//
			ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_CODE);
		}

		// 前回起動時のバージョン取得
		String prevVerName = mSharedPreferences.getString(DEF.KEY_LAST_VERSION, null);
		if (prevVerName == null || !prevVerName.equals(verName)) {

			// バージョンが変わったときはお知らせ表示
			ed = mSharedPreferences.edit();
			ed.putString(DEF.KEY_LAST_VERSION, verName);
			ed.apply();

			// お知らせ表示
			mInformation.showNotice();
		}

		// GitHubに新しいバージョンがリリースされているか確認する
		mInformation.checkRecentRelease(mHandler, false);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("Path", mPath);
		savedInstanceState.putString("Server", mServer.getCode());
		savedInstanceState.putInt("ServerSelect", mServer.getSelect());

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "onNewIntent: 開始します");}
		super.onNewIntent(intent);
		String path = intent.getStringExtra("Path");
		String server = intent.getStringExtra("Server");
		int serverSelect = intent.getIntExtra("ServerSelect", -2);
		if (debug) {Log.d(TAG, "onNewIntent: path=" + path + ", server=" + server + ", serverSelect=" + serverSelect);}

		if (path == null || !path.isEmpty()) {
			mPath = path;
		}

		// サーバパス
		if (serverSelect != -2) {
			if (mServer.select(serverSelect)) {
				mURI = mServer.getURI();
			}
		}
		else if (server != null && !server.isEmpty()) {
			if (mServer.select(server)) {
				mURI = mServer.getURI();
			}
			else {
				mPath = null;
				Resources res = getResources();
				Toast.makeText(this, res.getString(R.string.svNotFound), Toast.LENGTH_LONG).show();
			}
		}

		if (mPath == null || mPath.isEmpty()) {
			mServer.select(DEF.INDEX_LOCAL);
			mPath = mServer.getPath();
			mURI = mServer.getURI();
		}
		loadListView();
	}

	/**
	 * 画面の設定が変更された時に発生します。
	 *
	 * @param newConfig
	 *            - 新しい設定。
	 */
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if(result.getResultCode() == RESULT_OK && result.getData() != null){
						Intent data = result.getData();
					}
				}
			});

	// 画面遷移が戻ってきた時の通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		boolean debug = false;
		if (debug) {Log.d(TAG, "onActivityResult: 開始します. requestCode=" + requestCode + ", resultCode=" + resultCode);}

		if (requestCode == DEF.REQUEST_CODE_ACTION_OPEN_DOCUMENT) {
			// PICKERのURI取得
			if (resultCode == RESULT_OK) {
				Uri uri = null;
				if (data != null) {
					uri = data.getData();
					if (debug) {Log.d(TAG, "onActivityResult: DEF.DOCUMENT_PROVIDER_REQUEST_CODE uri=" + uri);}
					// 権限の永続化
					Intent intent = getIntent();
					final int takeFlags =
							intent.getFlags()
									& (Intent.FLAG_GRANT_READ_URI_PERMISSION
									| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					// Check for the freshest data.
					getContentResolver().takePersistableUriPermission(uri, takeFlags);
					if (mEditServerDialog != null) {
						if (debug) {Log.d(TAG, "onActivityResult: mEditServerDialog != null");}
						mEditServerDialog.setProvider(uri.toString());
					}

					mURI = uri.toString();
					mPath = "";
					FileData fileData = new FileData(mActivity, mURI);

					if (debug) {Log.d(TAG, "onActivityResult: ファイルをオープンします. mURI=" + mURI + ", name=" + fileData.getName());}
					openFile(fileData, "", (float)DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD, mEpubViewer);

				}
			}
			return;
		}
		if (requestCode == DEF.REQUEST_CODE_ACTION_OPEN_DOCUMENT_TREE) {
			// SAFのURI取得
			if (resultCode == RESULT_OK) {
				Uri uri = null;
				if (data != null) {
					uri = data.getData();
					if (debug) {Log.d(TAG, "onActivityResult: DEF.DOCUMENT_PROVIDER_REQUEST_CODE uri=" + uri);}
					// 権限の永続化
					Intent intent = getIntent();
					final int takeFlags =
							intent.getFlags()
							& (Intent.FLAG_GRANT_READ_URI_PERMISSION
							| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					// Check for the freshest data.
					getContentResolver().takePersistableUriPermission(uri, takeFlags);
					if (mEditServerDialog != null) {
						if (debug) {Log.d(TAG, "onActivityResult: mEditServerDialog != null");}
						mEditServerDialog.setProvider(uri.toString());
					}
				}
			}
			return;
		}
		else if(requestCode == DEF.APP_STORAGE_ACCESS_REQUEST_CODE) {
			// MANAGE_EXTERNAL_STORAGE権限取得 Android11以上
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				if (Environment.isExternalStorageManager()) {
					// Permission granted. Now resume your workflow.

				}
			}
			return;
		}
		else if (requestCode == DEF.REQUEST_CROP) {
			// クロップ完了
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				setThumb(uri);
			}
			loadThumbnail();
		}
		else {
			// 履歴の内容を更新する
			mListScreenView.updateRecordList();

			// 新しいバージョンがリリースされているか確認する
			mInformation.checkRecentRelease(mHandler, false);

			if (requestCode == DEF.REQUEST_IMAGE || requestCode == DEF.REQUEST_TEXT || requestCode == DEF.REQUEST_EPUB || requestCode == DEF.REQUEST_EXPAND) {
				if (debug) {Log.d(TAG, "onActivityResult: REQUEST_IMAGE || REQUEST_TEXT || REQUEST_EPUB || REQUEST_EXPAND");}
				if (resultCode == RESULT_OK && data != null) {
					if (debug) {Log.d(TAG, "onActivityResult: RESULT_OK. ビュワーから復帰しました.");}
					// ビュワーからの復帰
					int nextopen = data.getExtras().getInt("NextOpen", -1);
					String file = data.getExtras().getString("LastFile");
					String path = data.getExtras().getString("LastPath");
					if (debug) {Log.d("FileSelectActivity", "onActivityResult: nextopen=" + nextopen + ", file=" + file + ", path=" + path);}
					if (nextopen != CloseDialog.CLICK_CLOSE) {
						// 次のファイルを開く場合
						if (debug) {Log.d(TAG, "onActivityResult: nextopen != CloseDialog.CLICK_CLOSE");}
						if (mIsLoading || mFileList.getFileList() == null) {
							if (debug) {Log.d(TAG, "onActivityResult: mIsLoading == true");}
							// リストデータがない場合は読み込むまで待つ
							mLoadListNextOpen = nextopen;
							mLoadListNextFile = file;
							mLoadListNextPath = path;
							mLoadListNextInFile = "";
							mLoadListNextPageRate = (float)DEF.PAGENUMBER_READ;
							mLoadListNextPage = DEF.PAGENUMBER_READ;
							return;
						}
						else if (path != null && !path.equals(mPath)) {
							if (debug) {Log.d(TAG, "onActivityResult: mIsLoading == false, path.equals(mPath) == false");}
							// パス移動後読み込み終了まで待つ
							moveFileSelect(mURI, path, file, true);
							mLoadListNextOpen = nextopen;
							mLoadListNextFile = file;
							mLoadListNextPath = path;
							mLoadListNextInFile = "";
							mLoadListNextPageRate = (float)DEF.PAGENUMBER_READ;
							mLoadListNextPage = DEF.PAGENUMBER_READ;
							return;
						}
						else {
							if (debug) {Log.d(TAG, "onActivityResult: nextFileOpen");}
							if (nextFileOpen(nextopen, path, file, "", (float) DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD)) {
								// オープンできた
								return;
							}
						}
					}
					else {
						// 次のファイルを開かない場合
						if (debug) {Log.d(TAG, "onActivityResult: nextopen == CloseDialog.CLICK_CLOSE");}
						moveFileSelect(mURI, path, file, true);
					}
				}
			}

			if (!mIsLoading && mFileList.getFileList() != null) {
				if (debug) {Log.d(TAG, "onActivityResult: mIsLoading == false");}
				// 他画面から戻ったときは設定＆リスト更新
				// サムネイル解放
				releaseThumbnail();

				// 画面遷移によって設定反映
				if (checkConfigChange()) {
					if (debug) {Log.d(TAG, "onActivityResult: checkConfigChange() == true");}
					// 変更されている
					// 設定の読込
					// スクロール位置は最初に戻る
					readConfig();
					refreshFileSelect();
				}
				else {
					if (debug) {Log.d(TAG, "onActivityResult: checkConfigChange() == false");}
					// 設定は変更されていない
					updateListView();
					loadThumbnail();
				}
			}
		}
		if(debug) {Log.d(TAG, "onActivityResult: 終了します");}
	}

	public void setThumb(Uri uri) {
		Bitmap bm = null;
		long thumbID = System.currentTimeMillis();
		int thumH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		int thumW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));

		try {
			ContentResolver cr = getContentResolver();
			InputStream in = cr.openInputStream(uri);
			// サイズのみ取得
			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, option);
			in.close();
			if (option.outHeight != -1 && option.outWidth != -1) {
				// 縮小してファイル読込
				option.inJustDecodeBounds = false;
				option.inPreferredConfig = Bitmap.Config.RGB_565;
				option.inSampleSize = DEF.calcThumbnailScale(option.outWidth, option.outHeight, thumW, thumH);
				in = cr.openInputStream(uri);
				bm = BitmapFactory.decodeStream(in, null, option);
				in.close();
			}
			if(bm == null)
				return;
		}catch(Exception e){
			Log.e("ImageActivity", "setThumb error");
		}

		bm = ImageAccess.resizeTumbnailBitmap(bm, thumW, thumH, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
		if (bm != null) {
			ThumbnailLoader loader = new ThumbnailLoader(mActivity, "", "", null, thumbID, new ArrayList<FileData>(), thumW, thumH, 0, mThumbCrop, mThumbMargin);
			loader.deleteThumbnailCache(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), thumW, thumH);
			loader.setThumbnailCache(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), bm);
			Toast.makeText(this, R.string.ThumbConfigured, Toast.LENGTH_SHORT).show();
		}
	}

	// 次のファイルを開く
	private boolean nextFileOpen(int nextopen, String path, String file, String image, float pagerate, int page) {
		boolean debug = false;
		if(debug) {Log.d(TAG, "nextFileOpen: 開始します. nextopen=" + nextopen + ", path=" + path + ", file=" + file + ", image=" + image + ", pagerate=" + pagerate + ", page=" + page);}
		// 次のファイル検索
		FileData nextfile = searchNextFile(mFileList.getFileList(), file, nextopen);

		String user = mServer.getUser();
		String pass = mServer.getPass();

		if (nextfile != null && !nextfile.getName().isEmpty()) {
			if(debug) {Log.d(TAG, "nextFileOpen: nextfile != null");}

			switch (nextopen) {
				case CloseDialog.CLICK_PREVTOP:
				case CloseDialog.CLICK_NEXTTOP: {
					Editor ed = mSharedPreferences.edit();
					ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, nextfile.getName()), user, pass) + "#pageRate");
					ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, nextfile.getName()), user, pass));
					ed.apply();
					break;
				}
				case CloseDialog.CLICK_PREVLAST:
				case CloseDialog.CLICK_NEXTLAST: {
					Editor ed = mSharedPreferences.edit();
					ed.putFloat(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, nextfile.getName()), user, pass) + "#pageRate", (float)DEF.PAGENUMBER_READ);
					ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, nextfile.getName()), user, pass), DEF.PAGENUMBER_READ);
					ed.apply();
					updateListView();
					break;
				}
			}

			if (nextopen != CloseDialog.CLICK_CANCEL && nextopen != CloseDialog.CLICK_CLOSE) {
				if(debug) {Log.d(TAG, "nextFileOpen: nextopen != CloseDialog.CLICK_CANCEL && nextopen != CloseDialog.CLICK_CLOSE");}

				if (openFile(nextfile, image, pagerate, page, mEpubViewer)) {
					// サムネイル解放
					releaseThumbnail();
					return true;
				}
			}
		}
		else {
			if(debug) {Log.d(TAG, "nextFileOpen: nextfile == null");}
			if (file !=null && !file.isEmpty() && FileAccess.isDirectory(mActivity, DEF.relativePath(mActivity, path, file), user, pass)) {
				if(debug) {Log.d(TAG, "nextFileOpen: IS DIRECTORY: " + file);}
				if (nextopen != CloseDialog.CLICK_CANCEL && nextopen != CloseDialog.CLICK_CLOSE) {
					if (debug) {Log.d(TAG, "nextFileOpen: nextopen != CloseDialog.CLICK_CANCEL && nextopen != CloseDialog.CLICK_CLOSE");}

					if (openFile(nextfile, image, pagerate, page, mEpubViewer)) {
						// サムネイル解放
						releaseThumbnail();
						return true;
					}
				}
			}
		}
		return false;
	}

	// 設定の読み込み
	private void readConfig() {
		boolean debug = false;
		// 色の設定
		mDirColor = SetFileColorActivity.getDirColor(mSharedPreferences);
		mBefColor = SetFileColorActivity.getBefColor(mSharedPreferences);
		mNowColor = SetFileColorActivity.getNowColor(mSharedPreferences);
		mAftColor = SetFileColorActivity.getAftColor(mSharedPreferences);
		mImgColor = SetFileColorActivity.getImgColor(mSharedPreferences);
		mTxtColor = SetFileColorActivity.getTxtColor(mSharedPreferences);
		mInfColor = SetFileColorActivity.getInfColor(mSharedPreferences);
		mBakColor = SetFileColorActivity.getBakColor(mSharedPreferences);

		mTitColor = SetFileColorActivity.getTitColor(mSharedPreferences);
		mTibColor = SetFileColorActivity.getTibColor(mSharedPreferences);
		mTldColor = SetFileColorActivity.getTldColor(mSharedPreferences);
		mTlbColor = SetFileColorActivity.getTlbColor(mSharedPreferences);
		mMrkColor = SetFileColorActivity.getMrkColor(mSharedPreferences);
		mCurColor = SetFileColorActivity.getCurColor(mSharedPreferences);

		mSortMode = SetFileListActivity.getListSort(mSharedPreferences);
		mFontTitle = DEF.calcFontPix(SetFileListActivity.getFontTitle(mSharedPreferences), mDensity);
		mFontMain = DEF.calcFontPix(SetFileListActivity.getFontMain(mSharedPreferences), mDensity);
		mFontSub = DEF.calcFontPix(SetFileListActivity.getFontSub(mSharedPreferences), mDensity);
		mFontTile = DEF.calcFontPix(SetFileListActivity.getFontTile(mSharedPreferences), mDensity);
		mItemMargin = DEF.calcSpToPix(SetFileListActivity.getItemMargin(mSharedPreferences), mDensity);

		mTapExpand = SetFileListActivity.getTapExpand(mSharedPreferences);

		mListRota = SetFileListActivity.getListRota(mSharedPreferences);

		mBackMode = SetFileListActivity.getBackMode(mSharedPreferences);
		// mThumbnail = SetFileListActivity.getThumbnail(mSharedPreferences);
		mThumbSizeW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));
		mThumbSizeH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		mListThumbSizeH = DEF.calcThumbnailSize(SetFileListActivity.getListThumbSizeH(mSharedPreferences));
		mThumbNum = SetFileListActivity.getThumbCacheNum(mSharedPreferences);
		mThumbCrop = SetFileListActivity.getThumbCrop(mSharedPreferences);
		mThumbMargin = SetFileListActivity.getThumbMargin(mSharedPreferences);
		mRotateBtn = DEF.RotateBtnList[SetCommonActivity.getRotateBtn(mSharedPreferences)];
		mClearTop = SetFileListActivity.getCrearTop(mSharedPreferences);
		mShowExt = SetFileListActivity.getExtension(mSharedPreferences);
		mSplitFilename = SetFileListActivity.getSplitFilename(mSharedPreferences);
		mMaxLines = SetFileListActivity.getMaxLines(mSharedPreferences);
		mThumbSort = SetFileListActivity.getThumbnailSort(mSharedPreferences);
		mParentMove = SetFileListActivity.getParentMove(mSharedPreferences);
		mShowDelMenu = SetFileListActivity.getFileDelMenu(mSharedPreferences);
		mShowRenMenu = SetFileListActivity.getFileRenMenu(mSharedPreferences);

		mHidden = SetCommonActivity.getHiddenFile(mSharedPreferences);

		mResumeOpen = SetImageText.getResumeOpen(mSharedPreferences); // 戻るキーで確認メッセージ

		// mToolbarShow =
		// SetFileListActivity.getShowToolbar(mSharedPreferences); // ツールバー表示
		mToolbarSize = DEF.calcToolbarPix(SetFileListActivity.getToolbarSize(mSharedPreferences), mDensity); // ツールバーサイズ(px)
		mToolbarLabel = SetFileListActivity.getToolbarName(mSharedPreferences);

		// mSelectorShow =
		// SetRecorderActivity.getShowSelector(mSharedPreferences); // セレクタ表示
		mListType = SetRecorderActivity.getListTypes(mSharedPreferences);
		if(debug) {Log.d(TAG, "mListType.length=" + mListType.length);}

		mHistCount = DEF.calcSaveNum(SetRecorderActivity.getHistNum(mSharedPreferences));
		mLocalSave = SetRecorderActivity.getRecLocal(mSharedPreferences);
		mSambaSave = SetRecorderActivity.getRecServer(mSharedPreferences);

		mUseThumbnailTap = SetFileListActivity.getThumbnailTap(mSharedPreferences);	// サムネイルタップで長押しメニューの有効化フラグ
		mDuration = DEF.calcMSec100(SetFileListActivity.getMenuLongTap(mSharedPreferences));

		mEpubViewer = SetFileListActivity.getEpubViewer(mSharedPreferences);
		mEpubThumb = SetEpubActivity.getEpubThumb(mSharedPreferences);

		if (!mListRotaChg) {
			// 手動で切り替えていない
			DEF.setRotation(this, mListRota);
		}
	}

	// 設定の変更チェック
	private boolean checkConfigChange() {
		// 色の設定
		if (mDirColor != SetFileColorActivity.getDirColor(mSharedPreferences)) {
			return true;
		}
		if (mBefColor != SetFileColorActivity.getBefColor(mSharedPreferences)) {
			return true;
		}
		if (mNowColor != SetFileColorActivity.getNowColor(mSharedPreferences)) {
			return true;
		}
		if (mAftColor != SetFileColorActivity.getAftColor(mSharedPreferences)) {
			return true;
		}
		if (mImgColor != SetFileColorActivity.getImgColor(mSharedPreferences)) {
			return true;
		}
		if (mTxtColor != SetFileColorActivity.getTxtColor(mSharedPreferences)) {
			return true;
		}
		if (mInfColor != SetFileColorActivity.getInfColor(mSharedPreferences)) {
			return true;
		}
		if (mBakColor != SetFileColorActivity.getBakColor(mSharedPreferences)) {
			return true;
		}
		if (mMrkColor != SetFileColorActivity.getMrkColor(mSharedPreferences)) {
			return true;
		}
		if (mCurColor != SetFileColorActivity.getCurColor(mSharedPreferences)) {
			return true;
		}
		if (mTitColor != SetFileColorActivity.getTitColor(mSharedPreferences)) {
			return true;
		}
		if (mTibColor != SetFileColorActivity.getTibColor(mSharedPreferences)) {
			return true;
		}
		if (mTldColor != SetFileColorActivity.getTldColor(mSharedPreferences)) {
			return true;
		}
		if (mTlbColor != SetFileColorActivity.getTlbColor(mSharedPreferences)) {
			return true;
		}
		if (mSortMode != SetFileListActivity.getListSort(mSharedPreferences)) {
			return true;
		}
		if (mFontTitle != DEF.calcFontPix(SetFileListActivity.getFontTitle(mSharedPreferences), mDensity)) {
			return true;
		}
		if (mFontMain != DEF.calcFontPix(SetFileListActivity.getFontMain(mSharedPreferences), mDensity)) {
			return true;
		}
		if (mFontSub != DEF.calcFontPix(SetFileListActivity.getFontSub(mSharedPreferences), mDensity)) {
			return true;
		}
		if (mFontTile != DEF.calcFontPix(SetFileListActivity.getFontTile(mSharedPreferences), mDensity)) {
			return true;
		}
		if (mListRota != SetFileListActivity.getListRota(mSharedPreferences)) {
			return true;
		}
		if (mToolbarShow != SetFileListActivity.getShowToolbar(mSharedPreferences)) { // ツールバー表示
			return true;
		}
		if (mToolbarSize != DEF.calcToolbarPix(SetFileListActivity.getToolbarSize(mSharedPreferences), mDensity)) { // ツールバーサイズ(px)
			return true;
		}
		if (mToolbarLabel != SetFileListActivity.getToolbarName(mSharedPreferences)) {
			return true;
		}
		if (mListThumbSizeH != DEF.calcThumbnailSize(SetFileListActivity.getListThumbSizeH(mSharedPreferences))) {
			return true;
		}
		if (mThumbSizeW != DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences))) {
			return true;
		}
		if (mThumbSizeH != DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences))) {
			return true;
		}
		if (mThumbNum != SetFileListActivity.getThumbCacheNum(mSharedPreferences)) {
			return true;
		}
		if (mThumbCrop != SetFileListActivity.getThumbCrop(mSharedPreferences)) {
			return true;
		}
		if (mThumbMargin != SetFileListActivity.getThumbMargin(mSharedPreferences)) {
			return true;
		}
		if (mBackMode != SetFileListActivity.getBackMode(mSharedPreferences)) {
			return true;
		}
		if (mRotateBtn != DEF.RotateBtnList[SetCommonActivity.getRotateBtn(mSharedPreferences)]) {
			return true;
		}
		if (mToolbarShow != SetFileListActivity.getShowToolbar(mSharedPreferences)) {
			return true;
		}
		if (mHidden != SetCommonActivity.getHiddenFile(mSharedPreferences)) {
			return true;
		}
		if (mItemMargin != DEF.calcSpToPix(SetFileListActivity.getItemMargin(mSharedPreferences), mDensity)) {
			return true;
		}
		if (mTapExpand != SetFileListActivity.getTapExpand(mSharedPreferences)) {
			return true;
		}
		if (mShowExt != SetFileListActivity.getExtension(mSharedPreferences)) {
			return true;
		}
		if (mSplitFilename != SetFileListActivity.getSplitFilename(mSharedPreferences)) {
			return true;
		}
		if (mMaxLines != SetFileListActivity.getMaxLines(mSharedPreferences)) {
			return true;
		}
		if (mThumbSort != SetFileListActivity.getThumbnailSort(mSharedPreferences)) {
			return true;
		}
		if (mParentMove != SetFileListActivity.getParentMove(mSharedPreferences)) {
			return true;
		}
		if (mShowDelMenu != SetFileListActivity.getFileDelMenu(mSharedPreferences)) {
			return true;
		}
		if (mShowRenMenu != SetFileListActivity.getFileRenMenu(mSharedPreferences)) {
			return true;
		}
		if (mDuration != DEF.calcMSec100(SetFileListActivity.getMenuLongTap(mSharedPreferences))) {
			return true;
		}
		if (mEpubViewer != SetFileListActivity.getEpubViewer(mSharedPreferences)) {
			return true;
		}
		if (mEpubThumb != SetEpubActivity.getEpubThumb(mSharedPreferences)) {
			return true;
		}
		short[] listtype = SetRecorderActivity.getListTypes(mSharedPreferences);
		if (mListType.length != listtype.length) {
			return true;
		}
		else {
			for (int i = 0; i < mListType.length; i++) {
				if (mListType[i] != listtype[i]) {
					return true;
				}
			}
		}
		if (mUseThumbnailTap != SetFileListActivity.getThumbnailTap(mSharedPreferences)) {
			return true;
		}
		return false;
	}

	private boolean mEnterDown = false;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keycode = event.getKeyCode();
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keycode) {
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					if (!mEnterDown) {
						mListScreenView.moveCursor(keycode, true);
						mEnterDown = true;
					}
					break;
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_MOVE_END:
				case KeyEvent.KEYCODE_MOVE_HOME:
					mListScreenView.moveCursor(keycode, true);
					break;
				case KeyEvent.KEYCODE_DEL:
					// 親ディレクトリに移動
					if (!mPath.equals("/")) {
						moveParentDir();
					}
					break;
				case KeyEvent.KEYCODE_SPACE:
					// リストを選択
					mListScreenView.switchListType(false);
					break;
				case KeyEvent.KEYCODE_ESCAPE:
				case KeyEvent.KEYCODE_BACK:
					int listtype = mListScreenView.getListType();
					if(listtype != RecordList.TYPE_FILELIST){
						switchFileList(); // ファイルリストをアクティブ化
						return true;
					}

					// ジェスチャーナビゲーションのBack操作中に長押し判定が発生していた場合にキャンセルする
					if (mTouchArea == ListScreenView.AREATYPE_FILELIST) {
						mListScreenView.mFileListArea.cancelOperation();
					} else if (mTouchArea == ListScreenView.AREATYPE_DIRLIST) {
						mListScreenView.mDirListArea.cancelOperation();
					} else if (mTouchArea == ListScreenView.AREATYPE_SERVERLIST) {
						mListScreenView.mServerListArea.cancelOperation();
					} else if (mTouchArea == ListScreenView.AREATYPE_FAVOLIST) {
						mListScreenView.mFavoListArea.cancelOperation();
					} else if (mTouchArea == ListScreenView.AREATYPE_HISTLIST) {
						mListScreenView.mHistListArea.cancelOperation();
					}

					if (mBackMode == BACKMODE_EXIT) {
						checkExitTimer();
						return true;
					}
					else if (mBackMode == BACKMODE_PARENT) {
						// 親ディレクトリに移動
						if (mPath.equals("/")) {
							// アプリ終了
							checkExitTimer();
							return true;
						}
						moveParentDir();
						mExitBackTimer = 0;

						switchFileList(); // ファイルリストをアクティブ化
						return true;
					}
					else if (mBackMode == BACKMODE_HISTORY) {
						HistoryData data = mPathHistory.pop();
						String uri;
						if (data == null) {
							// 最初のディレクトリ
							checkExitTimer();
							return true;
						}
						else if (data.mCode == null || data.mCode.length() <= 0) {
							mServer.select(DEF.INDEX_LOCAL);
							uri = "";
						}
						else {
							if (mServer.select(data.mCode) != true) {
								mExitBackTimer = 0;
								return true;
							}
							uri = mServer.getURI();
						}
						moveFileSelect(uri, data.mPath, data.mTopIndex, false);
						mExitBackTimer = 0;

						switchFileList(); // ファイルリストをアクティブ化
						return true;
					}
					break;
				case KeyEvent.KEYCODE_CAMERA:
				case KeyEvent.KEYCODE_FOCUS:
					if (mRotateBtn == 0) {
						break;
					}
					else if (keycode != mRotateBtn) {
						return true;
					}
					if (mListRota == DEF.ROTATE_PORTRAIT || mListRota == DEF.ROTATE_LANDSCAPE) {
						int rotate;
						if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							// 横にする
							rotate = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
						}
						else {
							// 縦にする
							rotate = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
						}
						setRequestedOrientation(rotate);
						mListRotaChg = true;
					}
					return true;
				case KeyEvent.KEYCODE_VOLUME_UP:
				case KeyEvent.KEYCODE_PAGE_UP:
					mListScreenView.moveListUp(mMarker.length() > 0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
				case KeyEvent.KEYCODE_PAGE_DOWN:
					mListScreenView.moveListDown(mMarker.length() > 0);
					return true;
				default:
					break;
			}
		}
		else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (keycode) {
				case KeyEvent.KEYCODE_BACK:
					if (mBackMode == BACKMODE_PARENT || mBackMode == BACKMODE_HISTORY) {
						// 上へ又は履歴移動のときのUPは何もしない
						return true;
					}
					break;
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					if (mEnterDown == true) {
						mListScreenView.moveCursor(keycode, false);
					}
					mEnterDown = false;
					return true;
				default:
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	private void checkExitTimer() {
		// 一度目はトーストのみ
		long now = System.currentTimeMillis();
		if (mExitBackTimer == 0 || mExitBackTimer > now || mExitBackTimer + DEF.MILLIS_EXITTIME < now) {
			// 初回 || タイマーカンスト || 3秒以上経過
			Resources res = getResources();
			Toast.makeText(this, res.getString(R.string.backEnd), Toast.LENGTH_SHORT).show();
			mExitBackTimer = System.currentTimeMillis();
		}
		else {
			finishApplication();
		}
		return;
	}

	// Activityの破棄
	protected void onDestroy() {
		super.onDestroy();

		// サムネイルスレッド終了
		if (mThumbnailLoader != null) {
			mThumbnailLoader.breakThread();
			mThumbnailLoader = null;
		}
		return;
	}

	/**
	 * ダイアログのレイアウトを指定する
	 */
	// ActivityクラスのonCreateDialogをオーバーライド
	@Override
	protected Dialog onCreateDialog(int id) {
		boolean debug = false;
		Dialog dialog = null;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MyDialog);
		boolean isDirectory;
		File file;
		switch (id) {
			case DEF.MESSAGE_FILE_DELETE:
				dialogBuilder.setTitle("ファイル削除");
				dialogBuilder.setMessage(R.string.delMsg);
				dialogBuilder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					// ファイル削除
					if (mFileData != null) {
						String user = mServer.getUser();
						String pass = mServer.getPass();
						if (mFileData.getType() != FileData.FILETYPE_DIR) {
							// ファイル単体の場合はそのまま消す
							if (debug) {Log.d(TAG, "onCreateDialog: ファイルを削除します: " + DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()));}
							try {
								boolean isDeleted = FileAccess.delete(mActivity, DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass);
								if (isDeleted) {
									if (debug) {Log.d(TAG, "onCreateDialog: ファイルを削除できました。");}
									// 削除できていたら画面から消す
									mListScreenView.removeFileList(mFileData);
								}
								else {
									Log.e(TAG, "onCreateDialog: " + getResources().getString(R.string.delErrorMsg));
									Toast.makeText(mActivity, R.string.delErrorMsg + "\n" + DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), Toast.LENGTH_LONG).show();
								}
							} catch (FileAccessException e) {
								Toast.makeText(mActivity, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
							}
							mFileData = null;
						}
						else {
							// ディレクトリの場合は中身を順番に消す
							if (debug) {Log.d(TAG, "onCreateDialog: ディレクトリを削除します: " + DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()));}
							RemoveDialog dlg = new RemoveDialog(mActivity, R.style.MyDialog, mURI, mPath, user, pass, mFileData.getName(), new RemoveListener() {
								@Override
								public void onClose() {
								// 終了でリスト更新
								try {
									String user = mServer.getUser();
									String pass = mServer.getPass();
									boolean isExist = FileAccess.exists(mActivity, DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass);
									if (!isExist) {
										// 削除されていたら消す
										mListScreenView.removeFileList(mFileData);
									}
								} catch (FileAccessException e) {
									Toast.makeText(mActivity, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
								}
								mFileData = null;
								}
							});
							dlg.show();
						}
					}
					dialog.dismiss();
					loadThumbnail();
					}
				});
				dialogBuilder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mFileData = null;
					}
				});
				dialog = dialogBuilder.create();
				break;
			case DEF.MESSAGE_RECORD_DELETE:
				dialogBuilder.setTitle("dummy");
				dialogBuilder.setMessage(R.string.delBmMsg);
				dialogBuilder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// ブックマーク削除
						int listtype = mListScreenView.getListType();
						ArrayList<RecordItem> recordList = mListScreenView.getList(listtype);
						if (recordList != null && 0 <= mSelectPos && mSelectPos < recordList.size()) {
							recordList.remove(mSelectPos);
							RecordList.update(recordList, listtype);
							mListScreenView.notifyUpdate(listtype);
						}
						dialog.dismiss();
					}
				});
				dialogBuilder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// dialog.cancel();
					}
				});
				dialog = dialogBuilder.create();
				break;
			case DEF.MESSAGE_DOWNLOAD:
				dialogBuilder.setTitle("Download");
				dialogBuilder.setMessage("");
				dialogBuilder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mFileData != null) {
							// ダウンロード開始
							String user = mServer.getUser();
							String pass = mServer.getPass();
							DownloadDialog dlg = new DownloadDialog(mActivity, R.style.MyDialog, mURI, mPath, user, pass, mFileData.getName(), mServer.getPath(DEF.INDEX_LOCAL));
							dlg.show();
							mFileData = null;
						}
						dialog.dismiss();
					}
				});
				dialogBuilder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mFileData = null;
						dialog.dismiss();
					}
				});
				dialog = dialogBuilder.create();
				break;
			case DEF.MESSAGE_SHORTCUT: {
				Resources res = getResources();
				String title = res.getString(R.string.scTitle);
				String message = res.getString(R.string.scMsg);

				mTextInputDialog = new TextInputDialog(mActivity, R.style.MyDialog, title, null, message, null, new TextInputDialog.SearchListener() {
					@Override
					public void onSearch(String title) {
						// ショートカットに持たせるインテントの内容
						// ここでは MainActというクラスをACTION_VIEWで呼び出すという内容
						Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
						shortcutIntent.setClassName(mActivity, FileSelectActivity.class.getName());
						shortcutIntent.putExtra("Path", mPath);
						// shortcutIntent.putExtra("Server", mServer.getCode());
						shortcutIntent.putExtra("ServerSelect", mServer.getSelect());
						shortcutIntent.putExtra("File", "");
						if (mClearTop) {
							shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						}

						// ショートカットをHOMEに作成する
						if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							// Android 8 O API26 以降
							Icon icon = Icon.createWithResource(mActivity, R.drawable.comittonxx);
							ShortcutInfo shortcut = new ShortcutInfo.Builder(mActivity, title)
									.setShortLabel(title)
									.setLongLabel(title)
									.setIcon(icon)
									.setIntent(shortcutIntent)
									.build();
							ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
							shortcutManager.requestPinShortcut(shortcut, null); // フツーのショートカット
							// shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut)); // ダイナミックショートカット
						}
						else {
							// Android 7以前
							Intent intent = new Intent();

							intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
							intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
							Parcelable iconResource = Intent.ShortcutIconResource.fromContext(mActivity, R.drawable.icon);
							intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
							intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
							sendBroadcast(intent);
						}
					}

					@Override
					public void onCancel() {
						// キャンセル処理
					}

					@Override
					public void onClose() {
						// 終了
						mTextInputDialog = null;
					}
				});
				mTextInputDialog.show();
				break;
			}
			case DEF.MESSAGE_MARKER: {

				mMarkerInputDialog = new MarkerInputDialog(mActivity, R.style.MyDialog, mMarker, mFilter, mApplyDir, new MarkerInputDialog.SearchListener() {
					@Override
					public void onSearch(String text, boolean filter, boolean applyDir) {
						if (text != null && !text.isEmpty()) {
							// 検索文字列セット
							mMarker = text;
							mFilter = filter;
							mApplyDir = applyDir;
							loadListView();
						}
						else {
							// 検索文字列クリア
							mMarker = "";
							Toast.makeText(mActivity, R.string.searchJumpNoText, Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onCancel() {
						// 検索文字列クリア
						mMarker = "";
						loadListView();
					}

					@Override
					public void onClose() {
						// 終了
						mMarkerInputDialog = null;
					}
				});
				mMarkerInputDialog.show();
				break;
			}
			case DEF.MESSAGE_FILE_RENAME: {
				Resources res = getResources();
				String title = res.getText(R.string.renTitle).toString();
				String notice = res.getText(R.string.renMsg).toString();
				String fromfile = mFileData.getName();
				int filetype = mFileData.getType();
				int index;
				if (filetype == FileData.FILETYPE_PARENT) {
					break;
				}
				else if (filetype == FileData.FILETYPE_DIR) {
					// ディレクトリは最後の"/"を除去
					if (!fromfile.endsWith("/")) {
						break;
					}
					index = fromfile.length() - 1;
				}
				else {
					// ファイルは拡張子を除去
					index = fromfile.lastIndexOf('.');
					if (index < 0) {
						break;
					}
				}
				fromfile = fromfile.substring(0, index);

				mTextInputDialog = new TextInputDialog(mActivity, R.style.MyDialog, title, fromfile, notice, fromfile, new TextInputDialog.SearchListener() {
					@Override
					public void onSearch(String filename) {
						// ファイル削除 (ローカルのみ)
						if (mFileData != null) {
							String user = mServer.getUser();
							String pass = mServer.getPass();
							String fromfile = mFileData.getName();
							int index;
							int filetype = mFileData.getType();
							if (filetype != FileData.FILETYPE_DIR) {
								// ファイルは拡張子を除去
								index = fromfile.lastIndexOf('.');
								if (index < 0) {
									return;
								}
								// 拡張子をつける
								filename = filename + fromfile.substring(index);
							}
							try {
								if (debug) {Log.d(TAG, "onCreateDialog: ファイル名を変更します: " + DEF.relativePath(mActivity, mURI, mPath, fromfile));}
								boolean ret = FileAccess.renameTo(mActivity, DEF.relativePath(mActivity, mURI, mPath), fromfile, filename, user, pass);
								if (filetype == FileData.FILETYPE_DIR) {
									filename = filename + "/";
								}
								if (ret) {
									mFileData.setName(mActivity, filename);
								}
								else {
									Log.e(TAG, "onCreateDialog: " + getResources().getString(R.string.renameErrorMsg) + ": " + DEF.relativePath(mActivity, mURI, mPath, fromfile));
									Toast.makeText(mActivity, R.string.renameErrorMsg + "\n" + DEF.relativePath(mActivity, mURI, mPath, fromfile), Toast.LENGTH_LONG).show();
								}
							} catch (FileAccessException e) {
								Toast.makeText(mActivity, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
							}
							mFileData = null;
						}
						updateListView();
					}

					@Override
					public void onCancel() {
						// 検索文字列クリア
						mFileData = null;
					}

					@Override
					public void onClose() {
						// 終了
						mTextInputDialog = null;
					}
				});
				mTextInputDialog.show();
				break;
			}
			case DEF.MESSAGE_RESUME: {
				int svrindex = DEF.INDEX_LOCAL;
				try {
					svrindex = mSharedPreferences.getInt("LastServer", DEF.INDEX_LOCAL);
				} catch (Exception ex) {
					break;
				}
				int lastView = mSharedPreferences.getInt("LastOpen", -1);
				String path = mSharedPreferences.getString("LastPath", "");
				String lastFile = mSharedPreferences.getString("LastFile", "");
				String lastText = mSharedPreferences.getString("LastText", "");
				String lastImage = mSharedPreferences.getString("LastImage", "");
				String uri = "";
				if (svrindex != DEF.INDEX_LOCAL) {
					ServerSelect server = new ServerSelect(mSharedPreferences, this);
					server.select(svrindex);
					uri = server.getURI();
				}
				if (debug) {Log.d(TAG, "onCreateDialog: DEF.MESSAGE_RESUME: uri=" + uri + ", lastView=" + lastView + ", path=" + path + ", lastFile=" + lastFile + ", lastText=" + lastText + ", lastImage=" + lastImage);}

				Resources res = getResources();
				String msg = res.getString(R.string.rsMsg) + "\n\n" + uri + path + lastFile;
				if (lastView == DEF.LASTOPEN_TEXT && !lastText.isEmpty()) {
					if (!lastText.equals("META-INF/container.xml")) {
						msg += "\n" + lastText;
					}
				}
				if (lastView == DEF.LASTOPEN_IMAGE && !lastImage.isEmpty()) {
					msg += "\n" + lastImage;
				}
				dialogBuilder.setTitle(R.string.rsTitle);
				dialogBuilder.setMessage(msg);

				dialogBuilder.setPositiveButton(R.string.rsBtnYes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int lastView = mSharedPreferences.getInt("LastOpen", -1);
						int server = mSharedPreferences.getInt("LastServer", DEF.INDEX_LOCAL);
						String path = mSharedPreferences.getString("LastPath", "");
						String lastFile = mSharedPreferences.getString("LastFile", "");
						String lastText = mSharedPreferences.getString("LastText", "");
						String lastImage = mSharedPreferences.getString("LastImage", "");

						// 起動処理失敗回数をリセット
						if (mInitialize != 0) {
							SharedPreferences.Editor ed = mSharedPreferences.edit();
							ed.putInt("Initialize", 0);
							ed.apply();
							mInitialize = 0;
						}

						// ダイアログ終了
						dialog.dismiss();

						// データを利用
						if (lastView == DEF.LASTOPEN_TEXT) {
							if (!lastFile.isEmpty()) {
								// 最後に開いていた圧縮ファイル展開
								// 圧縮ファイルを設定
								mLoadListNextFile = lastFile;
								mLoadListNextPath = path;
								mLoadListNextInFile = lastText;
							} else {
								// 最後に開いたテキストオープン
								mLoadListNextFile = lastText;
								mLoadListNextPath = path;
							}
							mLoadListNextPage = DEF.PAGENUMBER_UNREAD;
						} else if (lastView == DEF.LASTOPEN_IMAGE) {
							if (!lastImage.isEmpty()) {
								mLoadListNextFile = lastFile;
								mLoadListNextPath = path;
								mLoadListNextInFile = lastImage;
								mLoadListNextPage = -2;
							}
							else {
								mLoadListNextFile = lastFile;
								mLoadListNextPath = path;
								mLoadListNextPage = -2;
							}
						}

						moveFileSelectFromServer(server, mLoadListNextPath);
						mLoadListNextOpen = CloseDialog.CLICK_THIS;
					}

				});
				dialogBuilder.setNegativeButton(R.string.rsBtnNo, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mFileData = null;
					}
				});
				dialog = dialogBuilder.create();
				break;
			}
			case DEF.MESSAGE_RESETLOCAL:{
				if(debug) {Log.d(TAG, "onCreateDialog: ローカルのパスリセットのダイアログを表示します.");}
				int serverindex = mSelectRecord.getServer(); // サーバのキーインデックス
				ServerSelect server = new ServerSelect(mSharedPreferences, this);

				dialogBuilder.setMessage(R.string.resetLocal);
				dialogBuilder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// ローカルの場合はストレージルートにリセット
						String path = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
						//path = DEF.getBaseDirectory();
						server.select(serverindex);
						server.setPath(path);
						mSelectRecord.setPath(path);
						mListScreenView.notifyUpdate(RecordList.TYPE_SERVER);

						ArrayList<RecordItem> recordList = mListScreenView.getList(RecordList.TYPE_SERVER);
						RecordList.update(recordList, RecordList.TYPE_SERVER);
					}
				});
				dialogBuilder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* キャンセル処理 */
					}
				});
				dialog = dialogBuilder.create();
				break;
			}
			case DEF.MESSAGE_EDITSERVER:{
				if(debug) {Log.d(TAG, "onCreateDialog: サーバ情報の編集ダイアログを表示します.");}
				int serverindex = mSelectRecord.getServer(); // サーバのキーインデックス
				mEditServerDialog = new EditServerDialog(mActivity, R.style.MyDialog, serverindex, new EditServerDialog.SearchListener() {
					@Override
					public void onSearch(int accessType, String name, String host, String user, String pass, String path, String provider, String dispName) {
						int listtype = RecordList.TYPE_SERVER;
						// リストのデータを更新
						mSelectRecord.setAccessType(accessType);
						mSelectRecord.setServerName(name);
						mSelectRecord.setHost(host);
						mSelectRecord.setUser(user);
						mSelectRecord.setPass(pass);
						mSelectRecord.setPath(path);
						mSelectRecord.setProvider(provider);
						mSelectRecord.setDispName(dispName);

						ArrayList<RecordItem> recordList = mListScreenView.getList(listtype);
						RecordList.update(recordList, listtype);
						mListScreenView.notifyUpdate(listtype);
					}

					@Override
					public void onCancel() {
						// キャンセル処理
					}

					@Override
					public void onClose() {
						// 終了
						mEditServerDialog = null;
					}
				});
				mEditServerDialog.show();
				break;
			}
		}
		return dialog;
	}

	/**
	 * ダイアログに初期値を設定する
	 */
	// ActivityクラスのonCreateDialogをオーバーライド
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog) {
		// 一度ダイアログを表示すると二回目に表示するときは中身だけ書き換える
		switch (id) {
			case DEF.MESSAGE_FILE_DELETE: // ファイル削除
			case DEF.MESSAGE_DOWNLOAD: // ファイルダウンロード
			case DEF.MESSAGE_FILE_RENAME: // リネーム
				ArrayList<FileData> files = mFileList.getFileList();
				if (files != null) {
					String title = new String(mFileData.getName());
					dialog.setTitle(title);
				}
				break;
			case DEF.MESSAGE_RECORD_DELETE: // ファイル削除
				if (mSelectRecord != null) {
					String title;
					int listtype = mListScreenView.getListType();
					if (listtype == RecordList.TYPE_BOOKMARK) {
						title = new String(mSelectRecord.getDispName());
					}
					else {
						title = new String(mSelectRecord.getPath());
					}
					dialog.setTitle(title);
				}
				break;
			case DEF.MESSAGE_RESETLOCAL: {
				break;
			}
			case DEF.MESSAGE_EDITSERVER:
				if (mEditDlg == null) {
					break;
				}
				// 一度ダイアログを表示すると二回目に表示するときは中身だけ書き換える
				EditText name = (EditText) mEditDlg.findViewById(R.id.edit_name);
				EditText host = (EditText) mEditDlg.findViewById(R.id.edit_host);
				EditText user = (EditText) mEditDlg.findViewById(R.id.edit_user);
				EditText pass = (EditText) mEditDlg.findViewById(R.id.edit_pass);
				// 属性
				name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
				host.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
				user.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
				pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				// 文字列
				name.setText(mSelectRecord.getServerName());
				host.setText(mSelectRecord.getHost());
				user.setText(mSelectRecord.getUser());
				pass.setText(mSelectRecord.getPass());
				break;
		}
		if (id == DEF.MESSAGE_DOWNLOAD) {
			String localPath = mServer.getPath(DEF.INDEX_LOCAL);
			Resources res = getResources();
			String str1 = res.getString(R.string.downMsg1);
			String str2 = res.getString(R.string.downMsg2);
			AlertDialog ad = (AlertDialog) dialog;
			if (!str1.equals("")) {
				ad.setMessage(str1 + " " + localPath);
			}
			else {
				ad.setMessage(localPath + " " + str2);
			}
		}
		return;
	}

	/**
	 * オプションメニューを表示
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		Resources res = getResources();

		/*
		 * 操作確認 menu.add(0, DEF.MENU_HELP, Menu.NONE,
		 * res.getString(R.string.helpMenu
		 * )).setIcon(android.R.drawable.ic_menu_help);
		 */

		// // サーバを選択
		// menu.add(0, DEF.MENU_SERVER, Menu.NONE,
		// res.getString(R.string.selectSv)).setIcon(R.drawable.ic_menu_archive);
		// // 更新
		// menu.add(0, DEF.MENU_REFRESH, Menu.NONE,
		// res.getString(R.string.refresh)).setIcon(R.drawable.ic_menu_refresh);
		// ショートカットを作成
		menu.add(0, DEF.MENU_SHORTCUT, Menu.NONE, res.getString(R.string.shortCut)).setIcon(android.R.drawable.ic_menu_myplaces);
		// // 検索
		// menu.add(0, DEF.MENU_MARKER, Menu.NONE,
		// res.getString(R.string.marker)).setIcon(android.R.drawable.ic_menu_search);
		// ディレクトリ登録
		//menu.add(0, DEF.MENU_ADDDIR, Menu.NONE, res.getString(R.string.addDirMenu)).setIcon(android.R.drawable.ic_menu_add);
		// // 表示モード
		// menu.add(0, DEF.MENU_LISTMODE, Menu.NONE,
		// res.getString(R.string.listModeMenu)).setIcon(android.R.drawable.ic_menu_add);
		// しおり削除
		menu.add(0, DEF.MENU_SIORI, Menu.NONE, res.getString(R.string.delMenu)).setIcon(android.R.drawable.ic_menu_delete);
		// サムネイルキャッシュ削除
		menu.add(0, DEF.MENU_THUMBDEL, Menu.NONE, res.getString(R.string.thumbMenu)).setIcon(android.R.drawable.ic_menu_delete);
		// 設定
		menu.add(0, DEF.MENU_SETTING, Menu.NONE, res.getString(R.string.setMenu)).setIcon(android.R.drawable.ic_menu_preferences);
		// 画面回転
		if (mListRota == DEF.ROTATE_PORTRAIT || mListRota == DEF.ROTATE_LANDSCAPE) {
			menu.add(0, DEF.MENU_ROTATE, Menu.NONE, res.getString(R.string.rotateMenu)).setIcon(android.R.drawable.ic_menu_rotate);
		}
		// ヘルプ
		menu.add(0, DEF.MENU_ONLINE, Menu.NONE, res.getString(R.string.onlineMenu)).setIcon(android.R.drawable.ic_menu_set_as);
		// 動作・使用上の注意
		menu.add(0, DEF.MENU_NOTICE, Menu.NONE, R.string.noticeMenu).setIcon(android.R.drawable.ic_menu_info_details);
		// バージョン情報
		menu.add(0, DEF.MENU_ABOUT, Menu.NONE, res.getString(R.string.aboutMenu)).setIcon(android.R.drawable.ic_menu_info_details);
		// ライセンス情報
		//menu.add(0, DEF.MENU_LICENSE, Menu.NONE, res.getString(R.string.licenseMenu)).setIcon(android.R.drawable.ic_menu_info_details);
		// 終了
		//menu.add(0, DEF.MENU_QUIT, Menu.NONE, res.getString(R.string.exitMenu)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return ret;
	}

	/**
	 * オプションメニューの項目が選択された
	 */
	@SuppressLint("DefaultLocale")
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		onOptionsItemSelected(id);
		return super.onOptionsItemSelected(item);
	}

	public void onOptionsItemSelected(int id) {
		if (id == DEF.MENU_SETTING) {
			// 設定
			// Intentをつかって画面遷移する
			Intent intent = new Intent(FileSelectActivity.this, SetConfigActivity.class);
			startActivityForResult(intent, DEF.REQUEST_SETTING);
		}
		else if (id == DEF.MENU_SHORTCUT) {
			// ショートカット名入力ダイアログ表示
			showDialog(DEF.MESSAGE_SHORTCUT);
			switchFileList();
		}
		else if (id == DEF.MENU_REFRESH) {
			// リストを更新
			loadListView();
		}
		else if (id == DEF.MENU_MARKER) {
			// マーカー設定ダイアログ表示
			showDialog(DEF.MESSAGE_MARKER);
		}
		else if (id == DEF.MENU_ROTATE) {
			int rotate;
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				// 横にする
				rotate = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}
			else {
				// 縦にする
				rotate = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
			setRequestedOrientation(rotate);
			mListRotaChg = true;
		}
		else if (id == DEF.MENU_SIORI) {
			// しおり削除
			showShioriDialog();
			switchFileList();
		}
		else if (id == DEF.MENU_THUMBDEL) {
			// サムネイルキャッシュ削除
			ThumbnailLoader.deleteThumbnailCache(0); // 0個を残す
			switchFileList();
		}
		else if (id == DEF.MENU_ONLINE) {
			// 操作方法画面に遷移
			Resources res = getResources();
			String url = res.getString(R.string.url_filer);	// 設定画面
			Intent intent;
			intent = new Intent(FileSelectActivity.this, HelpActivity.class);
			intent.putExtra("Url", url);
			startActivity(intent);
		}
		else if (id == DEF.MENU_QUIT) {
			// アプリ終了
			finishApplication();
		}
		else if (id == DEF.MENU_NOTICE) {
			// お知らせ表示
			mInformation.showNotice();
			// NoticeDialog dlg = new NoticeDialog(this);
			// dlg.show();
		}
		else if (id == DEF.MENU_ADDDIR) {
			// ディレクトリ登録
			RecordList.add(RecordList.TYPE_DIRECTORY, RecordItem.TYPE_FOLDER, mServer.getSelect(), mPath, null
					, new Date().getTime(), null, -1, null);
		}
		else if (id == DEF.MENU_LISTMODE) {
			// リストモードの選択
			showListModeDialog();
		}
		else if (id == DEF.MENU_ABOUT) {
			// バージョン情報
			mInformation.showAbout();
			// GitHubに新しいバージョンがリリースされているか確認する
			mInformation.checkRecentRelease(mHandler, true);
			// AboutDialog dlg = new AboutDialog(this);
			// dlg.show();
		}
		else if (id == DEF.MENU_LICENSE) {
			// ライセンス情報
		}
	}
	/**
	 * ファイルリストの再読み込み
	 */
	private void loadListView(String cursor) {
		loadListView();

		mLoadListCursor = cursor;
		mLoadListTopIndex = 0;
		mLoadListNextOpen = -1;
	}

	/**
	 * ファイルリストの再読み込み
	 */
	private void loadListView(int topindex) {
		loadListView();

		mLoadListCursor = null;
		mLoadListTopIndex = topindex;
		mLoadListNextOpen = -1;
	}

	/**
	 * ファイルリストの再読み込み
	 */
	private void loadListView() {
		if (mFileList.mDialog != null) {
			// 読み込み中であれば二重実行しない
			return;
		}
		mLoadListCursor = null;
		mLoadListTopIndex = 0;
		mLoadListNextOpen = -1;

		// パス情報の保存
		mServer.setPath(mPath);

		// パスの設定
		String user = mServer.getUser();
		String pass = mServer.getPass();

		// ファイルリスト取得条件セット
		mFileList.setPath(mURI, mPath, user, pass);
		mFileList.setParams(mHidden, mMarker, mFilter, mApplyDir, mParentMove, mEpubViewer);

		mListScreenView.mFileListArea.setThumbnailId(0);

		// ファイルリスト取得(非同期)
		mFileList.loadFileList();

		if (mListScreenView != null) {
			mListScreenView.clearFileList();
		}

		// タイトル設定
		mListScreenView.setListTitle(RecordList.TYPE_FILELIST, "[" + mServer.getName() + "]", mPath);

		mIsLoading = true;
	}

	/**
	 * ファイルリスト読み込み後のリスト設定
	 */
	private void loadListViewAfter() {
		// 読み込み中フラグOFF
		mIsLoading = false;

		// リストの内容設定
		if (mFileList.getFileList() != null) {
			mListScreenView.setFileList(mFileList.getFileList(), false);
		}

		// カーソル位置設定
		if (mLoadListCursor != null && !mLoadListCursor.isEmpty()) {
			int i;
			ArrayList<FileData> list = mFileList.getFileList();

			if (list != null) {
				FileData fd = new FileData(mActivity, mLoadListCursor);
				i = list.indexOf(fd);
				if (0 <= i && i < list.size()) {
					// タイル表示
					mListScreenView.mFileListArea.setTopIndex(i, (int) (20 * mDensity));
				}
			}
		}
		else if (mLoadListTopIndex != -1) {
			// 指定のインデックスを選択
			mListScreenView.mFileListArea.setTopIndex(mLoadListTopIndex, (int) (20 * mDensity));
		}
		else {
			// 先頭を選択
			mListScreenView.mFileListArea.setScrollPos(0, false);
		}
		mListScreenView.mFileListArea.update(false);

		boolean thumbload = true;
		if (mLoadListNextOpen != -1) {
			if (nextFileOpen(mLoadListNextOpen, mLoadListNextPath, mLoadListNextFile, mLoadListNextInFile, mLoadListNextPageRate, mLoadListNextPage) == true) {
				// オープンできた
				thumbload = false;
			}
		}
		if (thumbload) {
			// サムネイルの読込
			loadThumbnail();
		}
	}

	/**
	 * ファイルリストの更新
	 */
	private void updateListView() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String path = DEF.relativePath(mActivity, mURI, mPath);
		String user = mServer.getUser();
		String pass = mServer.getPass();
		ArrayList<FileData> files = mFileList.getFileList();

		if (files != null) {
			// nullのときは読み込み中なので処理しない
			String marker = mMarker.toUpperCase();
			if (marker.isEmpty()) {
				marker = null;
			}
			for (int i = 0; i < files.size(); i++) {
				FileData data = files.get(i);
				String name = data.getName();
				int state = DEF.PAGENUMBER_UNREAD;
				int type = data.getType();
				if (type == FileData.FILETYPE_ARC
						|| type == FileData.FILETYPE_PDF
						|| type == FileData.FILETYPE_TXT
						|| type == FileData.FILETYPE_DIR) {
					state = (int)mSharedPreferences.getFloat(DEF.createUrl(path + name, user, pass) + "#pageRate", (float)DEF.PAGENUMBER_UNREAD);
					if (state == DEF.PAGENUMBER_UNREAD) {
						state = sharedPreferences.getInt(DEF.createUrl(path + name, user, pass), DEF.PAGENUMBER_UNREAD);
					}
					data.setState(state);
				}
				if (type == FileData.FILETYPE_EPUB) {
					if (DEF.TEXT_VIEWER == mEpubViewer) {
						state = (int) mSharedPreferences.getFloat(DEF.createUrl(path + name + "META-INF/container.xml", user, pass) + "#pageRate", (float) DEF.PAGENUMBER_UNREAD);
						if (state == DEF.PAGENUMBER_UNREAD) {
							state = sharedPreferences.getInt(DEF.createUrl(path + name + "META-INF/container.xml", user, pass), DEF.PAGENUMBER_UNREAD);
						}
					}
					else {
						state = sharedPreferences.getInt(DEF.createUrl(path + name, user, pass), DEF.PAGENUMBER_UNREAD);
					}
					data.setState(state);
				}
				boolean hit = false;
				if (marker != null) {
					if (name.toUpperCase().contains(marker)) {
						// 検索文字列が含まれる
						hit = true;
					}
				}
				data.setMarker(hit);
			}
			mListScreenView.setFileList(files, true);
		}
		mListScreenView.update(ListScreenView.AREATYPE_FILELIST);
	}

	/**
	 * ファイルリストを選択
	 */
	private void switchFileList() {
		// ファイルリストを選択
		int listindex = mListScreenView.getListIndex(RecordList.TYPE_FILELIST);
		mListScreenView.setListIndex(listindex, 0, false);
	}

	private boolean mTouchState;
	private short mTouchArea;

	/**
	 * タッチイベント
	 */
	public boolean onTouch(View v, MotionEvent event) {
		v.performClick();
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();

		// 起動処理失敗回数をリセット
		if (mInitialize != 0) {
			SharedPreferences.Editor ed = mSharedPreferences.edit();
			ed.putInt("Initialize", 0);
			ed.apply();
			mInitialize = 0;
		}

		// 押した時はどのエリアか求める
		if (action == MotionEvent.ACTION_DOWN) {
			mTouchArea = mListScreenView.findAreaType((int) x, (int) y);
		}
		if (mTouchArea == ListScreenView.AREATYPE_NONE) {
			// 無ければ終了
			return true;
		}
		x = mListScreenView.areaPosX(mTouchArea, x);
		y = mListScreenView.areaPosY(mTouchArea, y);

		if (mTouchArea == ListScreenView.AREATYPE_TITLE) {
			int select = mListScreenView.mTitleArea.sendTouchEvent(action, x, y);
			if (select == TitleArea.SELECT_MENU) {
				// メニューを表示
				openOptionsMenu();
			}
			else if (select == TitleArea.SELECT_SORT) {
				int listtype = mListScreenView.getListType();
				if (listtype != RecordList.TYPE_SERVER && listtype != RecordList.TYPE_MENU) {
					// ダイアログ表示
					showSortDialog();
				}
			}
			return true;
		}
		else if (mTouchArea == ListScreenView.AREATYPE_TOOLBAR) {
			if (mToolbarShow) {
				// ボタン押下
				int result = mListScreenView.mToolbarArea.sendTouchEvent(action, (int) x, (int) y);
				switch (result) {
					case DEF.TOOLBAR_ADDDIR:
						// ディレクトリ登録
						RecordList.add(RecordList.TYPE_DIRECTORY, RecordItem.TYPE_FOLDER, mServer.getSelect(), mPath, null
								, new Date().getTime(), null, -1, null);

						mListScreenView.notifyUpdate(RecordList.TYPE_DIRECTORY);
						// ディレクトリリストに切り替える
						mListScreenView.updateRecordList(RecordList.TYPE_DIRECTORY);
						int listindex = mListScreenView.getListIndex(RecordList.TYPE_DIRECTORY);
						mListScreenView.setListIndex(listindex, 0, false);
						mListScreenView.onUpdateArea(ListScreenView.AREATYPE_ALL, false);
						break;
					case DEF.TOOLBAR_PARENT:
						// 親ディレクトリに移動
						if (!mPath.equals("/")) {
							moveParentDir();
						}
						break;
					case DEF.TOOLBAR_MARKER:
						// マーカー設定ダイアログ表示
						showDialog(DEF.MESSAGE_MARKER);
						break;
					case DEF.TOOLBAR_THUMBNAIL:
						// リスト表示切り替え
						showListModeDialog();
						break;
					case DEF.TOOLBAR_REFRESH:
						// リストを更新
						loadListView();
						break;
					case DEF.TOOLBAR_EXIT:
						// アプリ終了
						finishApplication();
						break;
				}
			}
		}
		else if (mTouchArea == ListScreenView.AREATYPE_SELECTOR) {
			// 下部の表示セレクタボタン
			if (mSelectorShow) {
				// ボタン押下
				int listindex = mListScreenView.mSelectorArea.sendTouchEvent(action, (int) x, (int) y);
				if (listindex != -1) {
					// リスト切り替え
					int listtype = mListScreenView.getListType(listindex);
					mListScreenView.updateRecordList(listtype);
					mListScreenView.setListIndex(listindex, 0, false);
					mListScreenView.onUpdateArea(ListScreenView.AREATYPE_ALL, false);
				}
			}
		}
		else if (mTouchArea == ListScreenView.AREATYPE_FILELIST) {
			if (mListScreenView.sendTouchEvent(action, x, y)) {
				mListScreenView.mFileListArea.sendTouchEvent(action, x, y);
			}
			else {
				mListScreenView.mFileListArea.cancelOperation();
			}
			return true;
		}
		else if (mTouchArea == ListScreenView.AREATYPE_DIRLIST) {
			if (mListScreenView.sendTouchEvent(action, x, y)) {
				mListScreenView.mDirListArea.sendTouchEvent(action, x, y);
			}
			else {
				mListScreenView.mDirListArea.cancelOperation();
			}
			return true;
		}
		else if (mTouchArea == ListScreenView.AREATYPE_SERVERLIST) {
			if (mListScreenView.sendTouchEvent(action, x, y)) {
				mListScreenView.mServerListArea.sendTouchEvent(action, x, y);
			}
			else {
				mListScreenView.mServerListArea.cancelOperation();
			}
			return true;
		}
		else if (mTouchArea == ListScreenView.AREATYPE_FAVOLIST) {
			if (mListScreenView.sendTouchEvent(action, x, y)) {
				mListScreenView.mFavoListArea.sendTouchEvent(action, x, y);
			}
			else {
				mListScreenView.mFavoListArea.cancelOperation();
			}
			return true;
		}
		else if (mTouchArea == ListScreenView.AREATYPE_HISTLIST) {
			if (mListScreenView.sendTouchEvent(action, x, y)) {
				mListScreenView.mHistListArea.sendTouchEvent(action, x, y);
			}
			else {
				mListScreenView.mHistListArea.cancelOperation();
			}
			return true;
		}
		else if (mTouchArea == ListScreenView.AREATYPE_MENULIST) {
			if (mListScreenView.sendTouchEvent(action, x, y)) {
				mListScreenView.mMenuListArea.sendTouchEvent(action, x, y);
			}
			else {
				mListScreenView.mMenuListArea.cancelOperation();
			}
			return true;
		}
		return true;
	}

	/**
	 * ソートメニュー表示
	 */
	private void showSortDialog() {
		String[] items;
		Resources res = getResources();

		String title = res.getString(R.string.sortTitle);
		int listtype = mListScreenView.getListType();
		if (listtype == RecordList.TYPE_FILELIST) {
			if (mListDialog != null) {
				return;
			}
			items = new String[6];
			items[0] = res.getString(R.string.lsort01);
			items[1] = res.getString(R.string.lsort02);
			items[2] = res.getString(R.string.lsort03);
			items[3] = res.getString(R.string.lsort04);
			items[4] = res.getString(R.string.lsort05);
			items[5] = res.getString(R.string.lsort06);
			mListDialog = new ListDialog(this, R.style.MyDialog, title, items, mSortMode - 1, true, new ListSelectListener() {
				@Override
				public void onSelectItem(int item) {
					if (item >= 0 && item < 6) {
						// ソートに反映
						mSortMode = item + 1;
						sortList(RecordList.TYPE_FILELIST);
					}
				}

				@Override
				public void onClose() {
					// 終了
					mListDialog = null;
				}
			});
			mListDialog.show();
		}
		else {
			if (mListDialog != null) {
				return;
			}
			if (listtype == RecordList.TYPE_BOOKMARK) {
				items = new String[6];
				items[0] = res.getString(R.string.rsort00);
				items[1] = res.getString(R.string.rsort01);
				items[2] = res.getString(R.string.rsort02);
				items[3] = res.getString(R.string.rsort03);
				items[4] = res.getString(R.string.rsort04);
				items[5] = res.getString(R.string.rsort05);
			}
			else {
				items = new String[4];
				items[0] = res.getString(R.string.rsort00);
				items[1] = res.getString(R.string.rsort01);
				items[2] = res.getString(R.string.rsort04);
				items[3] = res.getString(R.string.rsort05);
			}
			int select;
			if (listtype == RecordList.TYPE_BOOKMARK) {
				select = mSortType;
			}
			else {
				select = mSortType - (mSortType >= 2 ? 2 : 0);
			}
			mListDialog = new ListDialog(this, R.style.MyDialog, title, items, select, true, new ListSelectListener() {
				@Override
				public void onSelectItem(int item) {
					int listtype = mListScreenView.getListType();
					if (listtype == RecordList.TYPE_BOOKMARK) {
						mSortType = (short) item;
					}
					else {
						mSortType = (short) (item + (item >= 2 ? 2 : 0));
					}
					sortList(listtype);
				}

				@Override
				public void onClose() {
					// 終了
					mListDialog = null;
				}
			});
			mListDialog.show();
		}
	}

	/**
	 * ファイルリストの長押し選択表示
	 */
	private void showFileLongClickDialog() {
		boolean debug = false;
		if (debug) {Log.d(TAG, "showFileLongClickDialog: 開始します.");}

		if (mListDialog != null) {
			return;
		}
		String[] items;
		Resources res = getResources();

		String title = res.getString(R.string.opeTitle);
		String ope0 = res.getString(R.string.ope00);
		String ope1 = res.getString(R.string.ope01);
		String ope2 = res.getString(R.string.ope02);
		String ope3 = res.getString(R.string.ope03); // 削除
		String ope4 = res.getString(R.string.ope04);
		String ope5 = res.getString(R.string.ope05); // zip/rar/pdfの内容表示
		String ope7 = res.getString(R.string.ope07); // zip/rar/pdfを開く
		String ope8 = res.getString(R.string.ope08); // ファイル名変更
		String ope9 = res.getString(R.string.ope09); // サムネイルキャッシュ削除
		String ope11 = res.getString(R.string.ope11); // Epubビュワーで開く
		String ope100 = res.getString(R.string.ope100);;// 親ディレクトリのサムネイルに設定
		String ope101 = res.getString(R.string.ope101);;// 1枚目を切り抜いてサムネイルに設定
		String ope10 = res.getString(R.string.ope10); // 先頭から読む

		boolean delmenu = false;
		boolean renmenu = false;
		if (mShowDelMenu == DEF.SHOWMENU_ALWAYS
				|| (mServer.getSelect() == DEF.INDEX_LOCAL && mShowDelMenu == DEF.SHOWMENU_LOCAL)
						|| (mServer.getSelect() != DEF.INDEX_LOCAL && mShowDelMenu == DEF.SHOWMENU_SERVER)) {
			delmenu = true;
		}
		if (mShowRenMenu == DEF.SHOWMENU_ALWAYS
				|| (mServer.getSelect() == DEF.INDEX_LOCAL && mShowRenMenu == DEF.SHOWMENU_LOCAL)
						|| (mServer.getSelect() != DEF.INDEX_LOCAL && mShowRenMenu == DEF.SHOWMENU_SERVER)) {
			renmenu = true;
		}

		int i = 0;
		if (mFileData.getType() == FileData.FILETYPE_IMG) {
			if (mServer.getSelect() == DEF.INDEX_LOCAL) {
				items = new String[2 + (delmenu ? 1 : 0) + (renmenu ? 1 : 0)];
			}
			else {
				items = new String[3 + (delmenu ? 1 : 0) + (renmenu ? 1 : 0)];
				// SMBのイメージファイルの時はダウンロード
				items[i] = ope4;
				mOperate[i] = OPERATE_DOWN;
				i++;
			}
			if (delmenu) {
    			// ファイル削除
    			items[i] = ope3;
    			mOperate[i] = OPERATE_DEL;
    			i++;
			}
			if (renmenu) {
    			// リネーム
    			items[i] = ope8;
    			mOperate[i] = OPERATE_RENAME;
    			i++;
			}
			// キャッシュ削除
			items[i] = ope9;
			mOperate[i] = OPERATE_DELCACHE;
			i++;
			// 親ディレクトリのサムネイルとして設定
			items[i] = ope100;
			mOperate[i] = OPERATE_SETTHUMBASDIR;
			i++;
		}
		else {
			int state = mFileData.getState();
			int itemnum = 0;
			if (mFileData.getType() != FileData.FILETYPE_DIR && mFileData.getType() != FileData.FILETYPE_TXT && mFileData.getType() != FileData.FILETYPE_EPUB) {
				// zip/rar/pdfファイルを展開
				itemnum++;
			}
			if (mFileData.getType() == FileData.FILETYPE_EPUB) {
				// zip/rar/pdfファイルを展開
				itemnum++;
				// Epubビュワーで開く
				itemnum++;
			}
			if (state != -1) {
				// 未読にする
				itemnum++;
				// 先頭から読む
				itemnum++;
			}
			if (state != -2) {
				// 既読にする
				itemnum++;
			}
			if (mFileData.getType() == FileData.FILETYPE_DIR) {
				// フォルダを開く
				itemnum++;
				if (mServer.getSelect() != DEF.INDEX_LOCAL) {
					// SMBならダウンロード
					itemnum++;
				}
			}
			else {
				if (mServer.getSelect() != DEF.INDEX_LOCAL) {
					// SMBならダウンロード
					itemnum++;
				}
			}
			if (renmenu) {
				// ファイル名変更
				itemnum++;
			}
			if (delmenu) {
				 // ディレクトリ削除あり
				itemnum++;
			}
			if (mFileData.getType() != FileData.FILETYPE_TXT) {
				// サムネイルキャッシュ削除
				itemnum++;
				// 親ディレクトリのサムネイルとして設定
				itemnum++;
			}
			if (mFileData.getType() != FileData.FILETYPE_DIR && mFileData.getType() != FileData.FILETYPE_TXT) {
				// 先頭ページを範囲選択してサムネイルに設定
				itemnum++;
			}

			// ここから設定
			items = new String[itemnum];

			if (mFileData.getType() != FileData.FILETYPE_DIR && mFileData.getType() != FileData.FILETYPE_TXT && mFileData.getType() != FileData.FILETYPE_EPUB) {
				if (mTapExpand) {
					// zip/rar/pdfファイルを開く
					items[i] = ope7;
				}
				else {
					// zip/rar/pdfファイルを展開
					items[i] = ope5;
				}
				mOperate[i] = OPERATE_EXPAND;
				i++;
			}

			if (mFileData.getType() == FileData.FILETYPE_EPUB) {
				// zip/rar/pdfファイルを展開
				items[i] = ope5;
				mOperate[i] = OPERATE_EXPAND;
				i++;
				if (DEF.TEXT_VIEWER == mEpubViewer) {
					// zip/rar/pdfファイルを開く
					items[i] = ope7;
				} else {
					// Epubビュワーで開く
					items[i] = ope11;
				}
				mOperate[i] = OPERATE_EPUB;
				i++;
			}

			if (state != -1) {
				// 未読にする
				items[i] = ope0;
				mOperate[i] = OPERATE_NONREAD;
				i++;
			}

			if (state != -2) {
				// 既読にする
				items[i] = ope1;
				mOperate[i] = OPERATE_READ;
				i++;
			}

			if (state != -1) {
				// 先頭から読む
				items[i] = ope10;
				mOperate[i] = OPERATE_FIRST;
				i++;
			}

			if (mFileData.getType() == FileData.FILETYPE_DIR) {
				// フォルダを開く
				items[i] = ope2;
				mOperate[i] = OPERATE_OPEN;
				i++;
				if (mServer.getSelect() != DEF.INDEX_LOCAL) {
					// SMBならダウンロード
					items[i] = ope4;
					mOperate[i] = OPERATE_DOWN;
					i++;
				}
			}
			else {
				if (mServer.getSelect() != DEF.INDEX_LOCAL) {
					// SMBならダウンロード
					items[i] = ope4;
					mOperate[i] = OPERATE_DOWN;
					i++;
				}
			}
			if (renmenu) {
    			// ファイル名変更
    			items[i] = ope8;
    			mOperate[i] = OPERATE_RENAME;
    			i++;
			}
			if (delmenu) {
    			// ディレクトリ削除あり
    			items[i] = ope3;
    			mOperate[i] = OPERATE_DEL;
    			i++;
			}
			if (mFileData.getType() != FileData.FILETYPE_DIR && mFileData.getType() != FileData.FILETYPE_TXT) {
				// 先頭ページを範囲選択してサムネイルに設定
				items[i] = ope101;
				mOperate[i] = OPERATE_SETTHUMBCROPPED;
				i++;
			}
			if (mFileData.getType() != FileData.FILETYPE_TXT) {
				// 親ディレクトリのサムネイルに設定
				items[i] = ope100;
				mOperate[i] = OPERATE_SETTHUMBASDIR;
				i++;
			}
			if (mFileData.getType() != FileData.FILETYPE_TXT) {
				// サムネイルキャッシュ削除
				items[i] = ope9;
				mOperate[i] = OPERATE_DELCACHE;
				i++;
			}
		}
		mListDialog = new ListDialog(this, R.style.MyDialog, title, items, -1, true, new ListSelectListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onSelectItem(int item) {
				if (mFileData == null) {
					// ファイル情報がない
					return;
				}
				if (item < 0 || mOperate.length <= item) {
					// 選択インデックスが範囲外
					return;
				}

				Editor ed;

				switch (mOperate[item]) {
					case OPERATE_EXPAND: { // ファイルの展開
						// サムネイル解放
						releaseThumbnail();

						if (mTapExpand && mFileData.getType() != FileData.FILETYPE_EPUB) {
							// zip/rar/pdfファイルを開く
							openCompFile(mFileData.getName(), "");
						}
						else {
							// zip/rar/pdfファイルを展開
							expandCompFile(mFileData.getName());
						}
						break;
					}
					case OPERATE_EPUB: // ビュワーで開く
						if (DEF.TEXT_VIEWER == mEpubViewer) {
							// zip/rar/pdfファイルを開く
							openCompFile(mFileData.getName(), "");
						} else {
							// Epubビュワーで開く
							openEpubFile(mFileData.getName(), (float)DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD);
						}
						break;
					case OPERATE_NONREAD: // 未読にする
					case OPERATE_FIRST: { // 先頭から読む
						ed = mSharedPreferences.edit();
						String user = mServer.getUser();
						String pass = mServer.getPass();
						if (mFileData.getType() == FileData.FILETYPE_EPUB && DEF.TEXT_VIEWER == mEpubViewer) {
							ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()) + "META-INF/container.xml", user, pass));
							ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()) + "META-INF/container.xml", user, pass) + "#pageRate");
						}
						else {
							ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass));
							ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass) + "#pageRate");
						}
						ed.apply();
						updateListView();
						if (mOperate[item] == OPERATE_FIRST) {
							// ファイルオープン
							String name = mFileData.getName();
							int type = mFileData.getType();
							// サムネイル解放
							releaseThumbnail();

							if (type == FileData.FILETYPE_TXT) {
								openTextFile(name, (float)DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD);
							}
							else if (type == FileData.FILETYPE_EPUB) {
								openEpubFile(name, (float)DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD);
							}
							else if (type == FileData.FILETYPE_ARC || type == FileData.FILETYPE_PDF) {
								// zip/rar/pdfファイルを開く
								openCompFile(name, "");
							}
							else if (type == FileData.FILETYPE_DIR) {
								// ディレクトリを開く
								openImageDir(mFileData.getName(), DEF.PAGENUMBER_READ);

							}
						}
						break;
					}
					case OPERATE_READ: { // 既読にする
						int type = mFileData.getType();
						ed = mSharedPreferences.edit();
						String user = mServer.getUser();
						String pass = mServer.getPass();
						if (mFileData.getType() == FileData.FILETYPE_EPUB && DEF.TEXT_VIEWER == mEpubViewer) {
							ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()) + "META-INF/container.xml", user, pass), DEF.PAGENUMBER_READ);
							ed.putFloat(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()) + "META-INF/container.xml", user, pass) + "#pageRate", (float)DEF.PAGENUMBER_READ);
						}
						else {
							if (debug) {Log.d(TAG, "showFileLongClickDialog: onSelectItem: OPERATE_READ: Url=" + DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass));}
							ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass), DEF.PAGENUMBER_READ);
							if (type == FileData.FILETYPE_TXT) {
								ed.putFloat(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), user, pass) + "#pageRate", (float) DEF.PAGENUMBER_READ);
							}
						}
						ed.apply();
						updateListView();
						break;
					}
					case OPERATE_OPEN: { // フォルダを開く
						// サムネイル解放
						releaseThumbnail();

						openImageDir(mFileData.getName(), DEF.PAGENUMBER_READ);
						break;
					}
					case OPERATE_DEL: // ファイル削除
						showDialog(DEF.MESSAGE_FILE_DELETE);
						break;
					case OPERATE_DOWN: // ファイル削除
						showDialog(DEF.MESSAGE_DOWNLOAD);
						break;
					case OPERATE_RENAME: // ファイル名変更
						removeDialog(DEF.MESSAGE_FILE_RENAME);
						showDialog(DEF.MESSAGE_FILE_RENAME);
						break;
					case OPERATE_DELCACHE: // サムネイルキャッシュ削除
						ThumbnailLoader.deleteThumbnailCache(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), mThumbSizeW, mThumbSizeH);
						break;
					case OPERATE_SETTHUMBASDIR: { // 親ディレクトリのサムネイルとして設定
						mThumbnailLoader.setThumbnailCache(DEF.relativePath(mActivity, mURI, mPath, mFileData.getName()), DEF.relativePath(mActivity, mURI, mPath));
						break;
					}
					case OPERATE_SETTHUMBCROPPED: { // クロップしてサムネイルに設定
						Intent intent = new Intent(mActivity, CropImageActivity.class);
						intent.putExtra("Path", DEF.relativePath(mActivity, mURI, mPath));
						intent.putExtra("File", mFileData.getName());
						intent.putExtra("User", mServer.getUser());
						intent.putExtra("Pass", mServer.getPass());
						intent.putExtra("aspectRatio", (float) mThumbSizeW / (float) mThumbSizeH);
						// サムネイル解放
						releaseThumbnail();
						startActivityForResult(intent, DEF.REQUEST_CROP);
						break;
					}
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
				mListScreenView.update(ListScreenView.AREATYPE_FILELIST);
			}
		});
		mListDialog.show();

		if (debug) {Log.d(TAG, "showFileLongClickDialog: 終了します.");}
	}

	/**
	 * 履歴系リストの長押し選択表示
	 */
	private void showRecordLongClickDialog() {
		boolean debug = false;
		if (mListDialog != null) {
			return;
		}

		Resources res = getResources();

		String title = res.getString(R.string.opeTitle);

		// RecordList.TYPE_xxx が入る
		int listtype = mListScreenView.getListType();

		if (listtype == RecordList.TYPE_DIRECTORY) {
			String[] items = new String[1];
			items[0] = res.getString(R.string.bm02);
			mListDialog = new ListDialog(this, R.style.MyDialog, title, items, -1, true, new ListSelectListener() {
				@Override
				public void onSelectItem(int item) {
					switch (item) {
						case 0: { // ブックマーク削除
							showDialog(DEF.MESSAGE_RECORD_DELETE);
							break;
						}
					}
				}

				@Override
				public void onClose() {
					// 終了
					mListDialog = null;
				}
			});
			mListDialog.show();
		}
		else if (listtype == RecordList.TYPE_SERVER) {
			if(debug) {Log.d(TAG, "showRecordLongClickDialog: サーバリストのアイテムが長押しされました.");}
			// サーバリストのアイテムが長押しされた
			int serverindex = mSelectRecord.getServer(); // サーバのキーインデックス
			if (serverindex == DEF.INDEX_LOCAL) {
				if(debug) {Log.d(TAG, "showRecordLongClickDialog: ローカルを選択しました.");}
				showDialog(DEF.MESSAGE_RESETLOCAL);
			}
			else {
				if(debug) {Log.d(TAG, "showRecordLongClickDialog: ローカル以外のサーバを選択しました.");}
				showDialog(DEF.MESSAGE_EDITSERVER);
			}
		}
		else if (listtype == RecordList.TYPE_MENU) {

		}
		else if (listtype == RecordList.TYPE_BOOKMARK || listtype == RecordList.TYPE_HISTORY) {
			String[] items = new String[3];
			items[0] = res.getString(R.string.bm00);
			items[1] = res.getString(R.string.bm01);
			items[2] = res.getString(R.string.bm02);
			mListDialog = new ListDialog(this, R.style.MyDialog, title, items, -1, true, new ListSelectListener() {
				@Override
				public void onSelectItem(int item) {
					switch (item) {
						case 0: { // フォルダを開く
							int server = mSelectRecord.getServer();
							String path = mSelectRecord.getPath();
							if (mSelectRecord.getType() == RecordItem.TYPE_COMPTEXT) {
								if (path != null && path.length() > 0) {
									int idx = path.lastIndexOf('/'); // 最後に/がついているので飛ばす
									if (idx > 2) {
										path = path.substring(0, idx + 1);
									}
								}
							}
							moveFileSelectFromServer(server, path);
							switchFileList(); // ファイルリストをアクティブ化
							break;
						}
						case 1: { // 編集
							// showDialog(DEF.MESSAGE_RENAME);
							BookmarkDialog renameDlg = new BookmarkDialog(mActivity, R.style.MyDialog);
							renameDlg.setBookmarkListear(mActivity);
							renameDlg.setName(mSelectRecord.getDispName());
							renameDlg.show();
							break;
						}
						case 2: { // ブックマーク削除
							showDialog(DEF.MESSAGE_RECORD_DELETE);
							break;
						}
					}
				}

				@Override
				public void onClose() {
					// 終了
					mListDialog = null;
				}
			});
			mListDialog.show();
		}
	}

	/**
	 * しおり削除のメニューを表示
	 */
	private void showShioriDialog() {
		Resources res = getResources();
		String siori0 = res.getString(R.string.siori00); // すべてのしおり
		String siori1 = res.getString(R.string.siori01); // ファイルがないしおり
		String siori2 = res.getString(R.string.siori02); // このディレクトリのしおり
		String siori3;
		if (mServer.getSelect() == DEF.INDEX_LOCAL) {
			siori3 = res.getString(R.string.siori03_1); // ローカル上のしおり
		}
		else {
			siori3 = res.getString(R.string.siori03_2); // このサーバ上のしおり
		}
		final String[] items = { siori0, siori1, siori2, siori3 };

		String title = res.getString(R.string.sioriTitle);
		mListDialog = new ListDialog(this, R.style.MyDialog, title, items, -1, true, new ListSelectListener() {
			@Override
			public void onSelectItem(int item) {
				Editor ed = mSharedPreferences.edit();
				switch (item) {
					case 0:
					case 1:
					case 3:
						Map<String, ?> keys = mSharedPreferences.getAll();
						if (keys != null) {
							for (String key : keys.keySet()) {
							    //Log.i("Key Check",key);
								if (item == 3) {
									String user = mServer.getUser();
									String pass = mServer.getPass();
									String uri = DEF.createUrl(mURI, user, pass).toLowerCase() + "/";
									int urilen = uri.length();
									if (key.length() < urilen) {
										continue;
									}
									else if (!key.substring(0, urilen + 1).toLowerCase().equals(uri)) {
										// URI部分が一致する場合のみ削除する
										continue;
									}
								}
								else {
									int len = key.length();
									if ((len >= 1 && key.substring(0, 1).equals("/"))) {
										// ローカルのしおり削除
										if (item == 1 && new File(key).exists()) {
											// 存在するので削除しない
											continue;
										}
									}
									else if (mServer.getSelect() != DEF.INDEX_LOCAL) {
										// サーバのしおり削除
										if (item == 1) {
											try {
												if (FileAccess.exists(mActivity, key, "", "")) {
													// 存在するので削除しない
													continue;
												}
											}
											catch (Exception e) {
												// 参照不可の場合は削除しない
												continue;
											}
										}
									}
									else {
										continue;
									}
								}
								ed.remove(key);
							}
						}
						break;
					case 2:
						String user = mServer.getUser();
						String pass = mServer.getPass();
						ArrayList<FileData> files = mFileList.getFileList();
						for (int i = 0; i < files.size(); i++) {
							FileData data = files.get(i);
							if (data.getType() == FileData.FILETYPE_ARC || data.getType() == FileData.FILETYPE_TXT || data.getType() == FileData.FILETYPE_DIR) {
								// .zip又はディレクトリのしおり削除
								String uri = DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, data.getName()), user, pass);
								ed.remove(uri);
							}
						}
						break;
				}
				ed.apply();
				updateListView();
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
	}

	/**
	 * タイル・リスト切替ダイアログ
	 */
	private void showListModeDialog() {
		if (mListDialog != null) {
			return;
		}
		String[] items = new String[4];
		Resources res = getResources();

		String title = res.getString(R.string.lmTitle);
		items[0] = res.getString(R.string.listmode00);
		items[1] = res.getString(R.string.listmode01);
		items[2] = res.getString(R.string.listmode02);
		items[3] = res.getString(R.string.listmode03);
		int select = 0;
		select = (mListMode == FileListArea.LISTMODE_LIST ? 0 : 2) + (mThumbnail ? 0 : 1);
		mListDialog = new ListDialog(this, R.style.MyDialog, title, items, select, true, new ListSelectListener() {
			public void onSelectItem(int pos) {
				boolean isChange = false;
				switch (pos) {
					case 0: {
						// サムネイルあり
						if (!mThumbnail) {
							mThumbnail = true;
							isChange = true;
						}
						mListMode = FileListArea.LISTMODE_LIST;
						break;
					}
					case 1: {
						// サムネイルなし
						if (mThumbnail) {
							;
							mThumbnail = false;
							isChange = true;
						}
						mListMode = FileListArea.LISTMODE_LIST;
						break;
					}
					case 2: {
						// サムネイルあり
						if (!mThumbnail) {
							mThumbnail = true;
							isChange = true;
						}
						mListMode = FileListArea.LISTMODE_TILE;
						break;
					}
					default: {
						// サムネイルなし
						if (mThumbnail) {
							;
							mThumbnail = false;
							isChange = true;
						}
						mListMode = FileListArea.LISTMODE_TILE;
						break;
					}
				}
				// 表示を反映
				mListScreenView.setListMode(mListMode);
				if (isChange) {
					mListScreenView.setThumbnail(mThumbnail, mThumbSizeW, mThumbSizeH, mListThumbSizeH);
				}
				mListScreenView.notifyUpdate(RecordList.TYPE_FILELIST);
				saveListMode(true);
				if (isChange) {
					if (mThumbnail) {
						loadThumbnail();
					}
					else {
						releaseThumbnail();
					}
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
	}

	/**
	 * リストをソート
	 */
	private void sortList(int listtype) {
		if (listtype == RecordList.TYPE_FILELIST) {
			if (mFileList == null || mFileList.getFileList() == null) {
				return;
			}

			// リストにソートを反映
			mFileList.setMode(mSortMode);
			mListScreenView.setListSortType(listtype, mSortMode); // タイトル更新
			mListScreenView.notifyUpdate(listtype);

			Editor ed = mSharedPreferences.edit();
			ed.putString(DEF.KEY_LISTSORT, Integer.toString(mSortMode));
			ed.apply();

			// サムネイル読み直し
			loadThumbnail();
		}
		else if (listtype == RecordList.TYPE_SERVER || listtype == RecordList.TYPE_MENU) {
			mListScreenView.notifyUpdate(listtype);
			return;
		}
		else {
			// ソート切り替え
			ArrayList<RecordItem> recordList = mListScreenView.getList(listtype);
			if (recordList != null) {
				Collections.sort(recordList, new BookmarkComparator());
			}

			mListScreenView.setListSortType(listtype, mSortType);
			mListScreenView.notifyUpdate(listtype);

			Editor ed = mSharedPreferences.edit();
			if (listtype == RecordList.TYPE_BOOKMARK) {
				ed.putInt("RBSort", mSortType);
			}
			else if (listtype == RecordList.TYPE_DIRECTORY) {
				ed.putInt("RDSort", mSortType);
			}
			else {
				ed.putInt("RHSort", mSortType);
			}
			ed.apply();
		}
	}

	/**
	 * 親フォルダに移動
	 */
	private void moveParentDir() {
		int pos = 0;
		String cursor = FileAccess.filename(mActivity, mPath);
		String path = FileAccess.parent(mActivity, mPath);
		moveFileSelect(mURI, path, cursor, true);
	}

	/**
	 * パスの移動
	 */
	private void moveFileSelect(String uri, String path, int topindex, boolean history) {
		// 移動
		moveFileSelectProc(uri, path, history);

		// リスト再ロード
		loadListView(topindex);
	}

	/**
	 * パスの移動
	 */
	private void moveFileSelect(String uri, String path, String cursor, boolean history) {
		// 移動
		moveFileSelectProc(uri, path, history);

		// リスト再ロード
		loadListView(cursor);
	}

	/**
	 * パスの移動
	 */
	private void moveFileSelect(String uri, String path, boolean history) {
		// 移動
		moveFileSelectProc(uri, path, history);

		// リスト再ロード
		loadListView();
	}

	/**
	 * パスの移動
	 */
	private void moveFileSelect(int serverindex, boolean history) {
		ServerSelect server = new ServerSelect(mSharedPreferences, this);

		String uri = server.getURI(serverindex);
		String path = server.getPath(serverindex);

		// 移動
		moveFileSelectProc(uri, path, history);

		mServer = server;
		mServer.select(serverindex);

		// リスト再ロード
		loadListView();
	}

	/**
	 * パスの移動
	 */
	private boolean moveFileSelectFromServer(int svrindex, String path) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "moveFileSelectFromServer: svrindex=" + svrindex  + ", path=" + path);}
		ServerSelect server = new ServerSelect(mSharedPreferences, this);
		if (svrindex != DEF.INDEX_LOCAL) {
			server.select(svrindex);
		}
		String uri = server.getURI();

		// 移動
		moveFileSelectProc(uri, path, true);

		mServer = server;

		// リスト再ロード
		loadListView();
		return true;
	}

	/**
	 * パスの移動
	 *
	 * @param history
	 *            true:履歴に保存する
	 */
	private void moveFileSelectProc(String uri, String path, boolean history) {
		if (history) {
			if (!uri.equals(mURI) || !path.equals(mPath)) {
				// タイルの場合
				int topindex = mListScreenView.mFileListArea.getTopIndex();
				mPathHistory.push(mServer.getCode(), mPath, topindex);
			}
		}

		// 新しいパスを設定
		mURI = uri;
		mPath = path;
	}

	/**
	 * ファイルリスト画面を作り直す
	 */
	private void refreshFileSelect() {
		Intent intent = new Intent(FileSelectActivity.this, FileSelectActivity.class);
		intent.putExtra("Path", mPath);
		// intent.putExtra("Server", mServer.getCode());
		intent.putExtra("ServerSelect", mServer.getSelect());
		intent.putExtra("Marker", mMarker);
		intent.putExtra("Refresh", "1");

		// // 画面の向き
		// if (mListRotaChg) {
		// intent.putExtra("Rotate", "1");
		// }
		//
		// 次の画面スタート
		startActivity(intent);
		finish();
		return;
	}

	/**
	 * アプリの終了
	 */
	private void finishApplication() {
		this.moveTaskToBack(true);
		finish();
		return;
	}

	/**
	 * ファイルオープン
	 */
	private boolean openFile(FileData fd, String infile, float pagerate, int page, boolean epubviewer) {
		boolean debug = false;
		if (fd == null) {
			if (debug) {Log.d(TAG, "openFile: 開始します. mPath=" + mPath + ", fd=null");}
		}
		else {
			if (debug) {Log.d(TAG, "openFile: 開始します. mPath=" + mPath + ", name=" + fd.getName());}
		}

		// ファイルを表示
		if (fd == null) {
			//if (page != DEF.PAGENUMBER_READ) {
			//	return false;
			//}
			if (infile == null || infile.isEmpty()) {
				if (debug) {Log.d(TAG, "openFile: openImageDir: mPath=" + mPath + ", infile=" + infile);}
				// 前回の続きでディレクトリオープン
				openImageDir("", page);
			}
			else {
				if (debug) {Log.d(TAG, "openFile: openImageDir: mPath=" + mPath + ", infile=" + infile);}
				openImageFile(infile);
			}
		}
		else {
			switch (fd.getType()) {
				case FileData.FILETYPE_DIR:
					if (debug) {Log.d(TAG, "openFile: FILETYPE_DIR: mPath=" + mPath + ", name=" + fd.getName());}
					openImageDir(fd.getName(), DEF.PAGENUMBER_READ);
					break;
				case FileData.FILETYPE_IMG:
					if (debug) {Log.d(TAG, "openFile: FILETYPE_IMG: mPath=" + mPath + ", name=" + fd.getName());}
					openImageFile(fd.getName());
					break;
				case FileData.FILETYPE_ARC:
					if (debug) {Log.d(TAG, "openFile: FILETYPE_ARC: mPath=" + mPath + ", name=" + fd.getName());}
					String ext = DEF.getFileExt(infile);
					if (FileData.isText(ext)) {
						// zip内テキストファイルオープン
						expandCompFile(fd.getName(), infile, page);
					}
					else {
						// zipのイメージ表示
						openCompFile(fd.getName(), infile);
					}
					break;
				case FileData.FILETYPE_TXT:
					if (debug) {Log.d(TAG, "openFile: FILETYPE_TXT: mPath=" + mPath + ", name=" + fd.getName());}
					openTextFile(fd.getName(), pagerate, page);
					break;
				case FileData.FILETYPE_PDF:
					if (debug) {Log.d(TAG, "openFile: FILETYPE_PDF: mPath=" + mPath + ", name=" + fd.getName());}
					openCompFile(fd.getName(), infile);
					break;
				case FileData.FILETYPE_EPUB:
					if (debug) {Log.d(TAG, "openFile: FILETYPE_EPUB: mPath=" + mPath + ", name=" + fd.getName());}
					if (DEF.TEXT_VIEWER == epubviewer) {
						Log.d(TAG, "openFile: DEF.EPUB_VIEWER");
						// Epubビューワーで開く
						openEpubFile(fd.getName(), pagerate, page);
					}
					else {
						Log.d(TAG, "openFile: DEF.IMAGE_VIEWER");
						// zipのイメージ表示
						openCompFile(fd.getName(), infile);
					}
					break;
			}
		}
		return true;
	}

	/**
	 * Epubファイルオープン
	 */
	private void openEpubFile(String name, float pageRate, int page) {
		// saveLastOpenComp(mServer.getCode(), mPath, null, name,
		// DEF.LASTOPEN_TEXT);
		Toast.makeText(this, FileAccess.filename(mActivity, name), Toast.LENGTH_SHORT).show();

		// 描画停止

		setDrawEnable();

		Intent intent;
		intent = new Intent(FileSelectActivity.this, TextActivity.class);
		intent.putExtra("Server", mServer.getSelect());
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mServer.getUser());
		intent.putExtra("Pass", mServer.getPass());
		intent.putExtra("File", name);
		intent.putExtra("Text", "META-INF/container.xml");
		//intent.putExtra("Epub", name);
		//intent.putExtra("Chapter", chapter);
		intent.putExtra("PageRate", pageRate);
		intent.putExtra("Page", page);
		startActivityForResult(intent, DEF.REQUEST_EPUB);

		return;
	}

	/**
	 * テキストファイルオープン
	 */
	private void openTextFile(String name, float pageRate, int page) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "openTextFile: 開始します. mPath=" + mPath + ", name=" + name);}
		//if (debug) {DEF.StackTrace(TAG, "openTextFile:");}

		// saveLastOpenComp(mServer.getCode(), mPath, null, name,
		// DEF.LASTOPEN_TEXT);
		Toast.makeText(this, FileAccess.filename(mActivity, name), Toast.LENGTH_SHORT).show();

		// 描画停止
		setDrawEnable();

		Intent intent;
		intent = new Intent(FileSelectActivity.this, TextActivity.class);
		intent.putExtra("Server", mServer.getSelect());
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mServer.getUser());
		intent.putExtra("Pass", mServer.getPass());
		intent.putExtra("File", "");
		intent.putExtra("Text", name);
		intent.putExtra("PageRate", pageRate);
		intent.putExtra("Page", page);
		startActivityForResult(intent, DEF.REQUEST_TEXT);
		return;
	}

	/**
	 * 画像ファイルオープン
	 */
	private void openImageFile(String name) {
		// saveLastOpenComp(mServer.getCode(), mPath, null, name,
		// DEF.LASTOPEN_IMAGE);
		Toast.makeText(this, mPath + FileAccess.filename(mActivity, name), Toast.LENGTH_SHORT).show();

		// 描画停止
		setDrawEnable();

		Intent intent = new Intent(FileSelectActivity.this, ImageActivity.class);
		intent.putExtra("Server", mServer.getSelect());
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mServer.getUser());
		intent.putExtra("Pass", mServer.getPass());
		intent.putExtra("File", "");
		intent.putExtra("Image", name);
		startActivityForResult(intent, DEF.REQUEST_IMAGE);
		return;
	}

	/**
	 * ディレクトリ内のイメージ表示
	 */
	private void openImageDir(String name, int page) {
		// saveLastOpenComp(mServer.getCode(), mPath + name, null, null,
		// DEF.LASTOPEN_IMAGE);
		Toast.makeText(this, mPath + FileAccess.filename(mActivity, name), Toast.LENGTH_SHORT).show();

		// Intentをつかって画面遷移する
		Intent intent = new Intent(FileSelectActivity.this, ImageActivity.class);
		intent.putExtra("Server", mServer.getSelect());
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", DEF.relativePath(mActivity, mPath, name));
		intent.putExtra("User", mServer.getUser());
		intent.putExtra("Pass", mServer.getPass());
		intent.putExtra("File", "");
		intent.putExtra("Image", "");
		intent.putExtra("Page", page);
		startActivityForResult(intent, DEF.REQUEST_IMAGE);
		return;
	}

	/**
	 * 圧縮ファイルオープン
	 */
	private void openCompFile(String name, String page) {
		// saveLastOpenComp(mServer.getCode(), mPath, name, null,
		// DEF.LASTOPEN_IMAGE);
		Toast.makeText(this, FileAccess.filename(mActivity, name), Toast.LENGTH_SHORT).show();

		// 描画停止
		setDrawEnable();

		Intent intent;
		intent = new Intent(FileSelectActivity.this, ImageActivity.class);
		intent.putExtra("Server", mServer.getSelect());	// サーバ選択番号
		intent.putExtra("Uri", mURI);						// ベースディレクトリのuri
		intent.putExtra("Path", mPath);					// ベースURIからの相対パス名
		intent.putExtra("User", mServer.getUser());		// SMB認証用
		intent.putExtra("Pass", mServer.getPass());		// SMB認証用
		intent.putExtra("File", name);					// ZIPファイル名
		intent.putExtra("Image", page); 					// 中身の画像ファイル名
		startActivityForResult(intent, DEF.REQUEST_IMAGE);

		return;
	}

	/**
	 * 圧縮ファイル展開
	 */
	private void expandCompFile(String comp) {
		expandCompFile(comp, null);
		return;
	}

	/**
	 * 圧縮ファイル展開
	 */
	private void expandCompFile(String comp, String text) {
		expandCompFile(comp, text, -1);
		return;
	}

	/**
	 * 圧縮ファイル展開
	 */
	private void expandCompFile(String comp, String text, int page) {
		// Intentをつかって画面遷移する
		Intent intent = new Intent(FileSelectActivity.this, ExpandActivity.class);
		intent.putExtra("Server", mServer.getSelect());
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mServer.getUser());
		intent.putExtra("Pass", mServer.getPass());
		intent.putExtra("File", comp); // 圧縮ファイル
		intent.putExtra("Text", text); // 圧縮ファイル内のファイル
		intent.putExtra("Page", page); // 圧縮ファイル内のファイルのページ指定
		startActivityForResult(intent, DEF.REQUEST_EXPAND);
		return;
	}

	private void setDrawEnable() {
		mListScreenView.setDrawEnable(false);
		// Message msg = mHandler.obtainMessage(DEF.HMSG_DRAWENABLE);
		// mHandler.sendMessage(msg);
	}

	// Bitmap読込のスレッドからの通知取得
	public boolean handleMessage(Message msg) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "handleMessage: what=" + msg.what);}

		if (DEF.ToastMessage(mActivity, msg)) {
			// HMSG_TOASTならトーストを表示して終了
			return true;
		}

		switch (msg.what) {
			//case DEF.HMSG_DRAWENABLE: {
			//	// 描画再開
			//	mListScreenView.setDrawEnable(true);
			//	break;
			//}
			case DEF.HMSG_RECENT_RELEASE: {
				// GitHubに新しいバージョンがリリースされていればダイアログを表示する
				mInformation.showRecentRelease();
				break;
			}
			case DEF.HMSG_ERROR: {
				// 読込中の表示
				// Toast.makeText(this, (String) msg.obj,
				// Toast.LENGTH_SHORT).show();
				break;
			}
			case DEF.HMSG_LOADFILELIST: {
				loadListViewAfter();
				break;
			}
			case DEF.HMSG_THUMBNAIL:
				// Bitmapの通知
				String name = (String) msg.obj;
				int bmIndex = msg.arg1;
				if (name != null) {
					ArrayList<FileData> files = mFileList.getFileList();
					if (files != null) {
						for (int i = 0; i < files.size(); i++) {
							if (name.equals(files.get(i).getName())) {
								// リストの更新
								mListScreenView.mFileListArea.update(false);
								break;
							}
						}
					}
				}
				break;
		}
		return false;
	}

	public void loadThumbnail() {
		if (!mThumbnail) {
			return;
		}
		// releaseThumbnail();
		if (mThumbnailLoader != null) {
			// 既存のスレッドを停止
			mThumbnailLoader.breakThread();
			mThumbnailLoader = null;
		}

		mThumbID = System.currentTimeMillis();
		String user = mServer.getUser();
		String pass = mServer.getPass();
		int filesort = SetImageActivity.getFileSort(mSharedPreferences);
		mThumbnailLoader = new FileThumbnailLoader(this, mURI, mPath, user, pass, mHandler, mThumbID, mFileList.getFileList(), mThumbSizeW, mThumbSizeH, mThumbNum, filesort, mHidden, mThumbSort, mThumbCrop, mThumbMargin, mEpubThumb);
		mThumbnailLoader.setDispRange(mFileFirstIndex, mFileLastIndex);

		// 現在時をIDに設定
		mListScreenView.mFileListArea.setThumbnailId(mThumbID);
		return;
	}

	// サムネイル画像をメモリから解放
	private void releaseThumbnail() {
		// サムネイル読み込みスレッドを停止
		if (mThumbnailLoader != null) {
			mThumbnailLoader.breakThread();
			mThumbnailLoader.releaseThumbnail();
			mThumbnailLoader = null;
		}
		return;
	}

	public FileData searchNextFile(ArrayList<FileData> files, String file, int nextopen) {
		boolean debug = false;
		if(debug) {Log.d(TAG, "searchNextFile: 開始します. file=" + file + ", nextopen=" + nextopen);}
		if (files == null || file == null || file.isEmpty()) {
			return null;
		}
		// 検索対象
		FileData searchfd = new FileData(mActivity, file);

		FileData nextfile = null;
		if (nextopen == CloseDialog.CLICK_THIS) {
			int index = files.indexOf(searchfd);
			if (index >= 0) {
				nextfile = files.get(index);
			}
			return nextfile;
		}

		ArrayList<FileData> sortfiles = new ArrayList<FileData>(files.size());
		for (FileData fd : files) {
			int type = fd.getType();
			switch (type) {
				case FileData.FILETYPE_DIR: // ディレクトリ
				case FileData.FILETYPE_TXT: // テキスト
				case FileData.FILETYPE_ARC: // ZIP
				case FileData.FILETYPE_EPUB: // Epub
					sortfiles.add(fd);
					break;
				case FileData.FILETYPE_IMG: // イメージ
					// イメージは親フォルダで管理
					break;
			}
		}
		Collections.sort(sortfiles, new FilenameComparator());

		// ソート後に現在ファイルを探す
		int index = sortfiles.indexOf(searchfd);
		if (index >= 0) {
			// 見つかった場合
			switch (nextopen) {
				case CloseDialog.CLICK_NEXT:
				case CloseDialog.CLICK_NEXTTOP:
				case CloseDialog.CLICK_NEXTLAST:
					// 次のファイル
					index++;
					break;
				case CloseDialog.CLICK_PREV:
				case CloseDialog.CLICK_PREVTOP:
				case CloseDialog.CLICK_PREVLAST:
					// 前のファイル
					index--;
					break;
			}
		}
		if (0 <= index && index < sortfiles.size()) {
			nextfile = sortfiles.get(index);
		}
		return nextfile;
	}

	// ファイル名でソート
	public static class FilenameComparator implements Comparator<FileData> {
		public int compare(FileData file1, FileData file2) {

			// ディレクトリ/ファイルタイプ
			int type1 = file1.getType();
			int type2 = file2.getType();

			// IMAGEとZIPのソート優先度は同じにする
			if (type1 == FileData.FILETYPE_IMG || type1 == FileData.FILETYPE_TXT) {
				type1 = FileData.FILETYPE_ARC;
			}
			if (type2 == FileData.FILETYPE_IMG || type2 == FileData.FILETYPE_TXT) {
				type2 = FileData.FILETYPE_ARC;
			}

			if (type1 != type2) {
				return type1 - type2;
			}
			return DEF.compareFileName(file1.getName(), file2.getName());
		}
	}

	private void saveListMode(boolean isSave) {
		if (isSave) {
			Editor ed = mSharedPreferences.edit();
			ed.putInt("ListMode", mListMode);
			ed.putBoolean("Thumbnail", mThumbnail);
			ed.apply();
		}
	}

	private static class HistoryData {
		public String mCode;
		public String mPath;
		public int mTopIndex;

		public HistoryData(String code, String path, int topindex) {
			mCode = code;
			mPath = path;
			mTopIndex = topindex;
			return;
		}
	}

	private static class PathHistory {
		private final ArrayList<HistoryData> mHistory;

		public PathHistory() {
			mHistory = new ArrayList<HistoryData>();
		}

		public void push(String code, String path, int topindex) {
			HistoryData data = peek();

			if (data != null) {
				if (code.equals(data.mCode) && path.equals(data.mPath)) {
					// 同じパスなら登録しない
					return;
				}
			}

			// データを設定
			mHistory.add(new HistoryData(code, path, topindex));
			return;
		}

		public HistoryData pop() {
			// データを取得
			int size = mHistory.size();

			if (size <= 0) {
				return null;
			}
			HistoryData data = mHistory.get(size - 1);
			mHistory.remove(size - 1);
			return data;
		}

		public HistoryData peek() {
			// データを取得
			int size = mHistory.size();

			if (size <= 0) {
				return null;
			}
            return mHistory.get(size - 1);
		}
	}

	@Override
	public void onScrollChanged(int listtype, int firstindex, int lastindex) {
		// スクロール位置変更
		if (listtype == RecordList.TYPE_FILELIST) {
    		// ファイルリスト
			if (mFileFirstIndex != firstindex || mFileLastIndex != lastindex) {
				mFileFirstIndex = firstindex;
				mFileLastIndex = lastindex;
				if (mThumbnailLoader != null) {
    				// リストボックスの位置が変わったときに通知
    				mThumbnailLoader.setDispRange(mFileFirstIndex, mFileLastIndex);
    			}
    		}
		}
	}

	@Override
	public void onItemLongClick(int listtype, int position) {
		if (listtype == RecordList.TYPE_FILELIST) {
			if (mTouchState) {
				// フリックされてたら反応しない
				return;
			}

			ArrayList<FileData> files = mFileList.getFileList();
			mFileData = files.get(position);

			if (mFileData != null && mFileData.getType() != FileData.FILETYPE_PARENT) {
				// ファイル長押し
				showFileLongClickDialog();
			}
		}
		else {
			mSelectRecord = mListScreenView.getRecordItem(listtype, position);
			mSelectPos = position;
			if (mSelectRecord == null) {
				return;
			}
			// 履歴長押し
			showRecordLongClickDialog();
		}
		return;
	}

	@Override
	public void onItemClick(int listtype, int listpos, Point point) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "onItemClick: 開始します. listtype=" + listtype);}
		//if (debug) {DEF.StackTrace(TAG, "onItemClick: ");}

		if (listtype == RecordList.TYPE_FILELIST) {
			ArrayList<FileData> files = mFileList.getFileList();
			// サムネイル有りでタイル表示の時はファイル情報部分
			// リスト表示の時はサムネ部分タップで長押しメニュー表示
			// サムネイルタップで長押しメニューの有効化フラグ対応
			if(mThumbnail && mUseThumbnailTap &&
					((mListMode == FileListArea.LISTMODE_TILE &&  point != null && point.y > mThumbSizeH + mItemMargin)
					|| (mListMode == FileListArea.LISTMODE_LIST &&  point != null && point.x < mThumbSizeW * mListThumbSizeH / mThumbSizeH + mItemMargin)) )
			{
				mFileData = files.get(listpos);
				if (mFileData != null)  {
					if (mFileData.getType() == FileData.FILETYPE_PARENT) {
						// 親ディレクトリに移動
						moveParentDir();
					} else {
						// ファイル長押しメニュー
						showFileLongClickDialog();
					}
				}
			}else {
				// リストのクリック
				if (files != null && 0 <= listpos && listpos < files.size()) {
					FileData file = (FileData) files.get(listpos);
					String name = file.getName();
					int type = file.getType();
					if (type == FileData.FILETYPE_PARENT) {
						// 親ディレクトリに移動
						moveParentDir();
					} else if (type == FileData.FILETYPE_DIR) {
						// ディレクトリ移動
						moveFileSelect(mURI, DEF.relativePath(mActivity, mPath, name), true);
					} else if (type == FileData.FILETYPE_IMG) {
						// サムネイル解放
						releaseThumbnail();
						// イメージファイル表示
						openImageFile(name);
					} else if (type == FileData.FILETYPE_TXT) {
						// サムネイル解放
						releaseThumbnail();
						openTextFile(name, (float)DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD);
					} else if (type == FileData.FILETYPE_PDF) {
						// サムネイル解放
						releaseThumbnail();
						// zip/rar/pdfファイルを開く
						openCompFile(name, "");
					} else if (type == FileData.FILETYPE_EPUB) {
						// サムネイル解放
						releaseThumbnail();
						if (DEF.TEXT_VIEWER == mEpubViewer) {
							Log.d(TAG, "onItemClick: DEF.EPUB_VIEWER");
							// EpubViewerで開く
							openEpubFile(name, (float)DEF.PAGENUMBER_UNREAD, DEF.PAGENUMBER_UNREAD);
						}
						else {
							Log.d(TAG, "onItemClick: DEF.IMAGE_VIEWER");
							// zip/rar/pdfファイルを開く
							openCompFile(name, "");
						}
					} else if (type == FileData.FILETYPE_ARC) {
						if (mTapExpand) {
							// zip/rar/pdfファイルを展開
							expandCompFile(name);
						} else {
							// サムネイル解放
							releaseThumbnail();
							// zip/rar/pdfファイルを開く
							openCompFile(name, "");
						}
					}
				}
			}
		}
		else if (listtype == RecordList.TYPE_MENU) {
			RecordItem rd = mListScreenView.getRecordItem(listtype, listpos);
			int item = rd.getItem();
//			switchFileList(); // ファイルリストをアクティブ化
			onOptionsItemSelected(item);
		}
		else {
			// ディレクトリ一覧 or サーバ一覧 or ブックマーク一覧 or 履歴
			if (debug) {Log.d(TAG, "onItemClick: listtype=" + listtype);}
			RecordItem rd = mListScreenView.getRecordItem(listtype, listpos);
			if (rd != null) {
				// データを利用
				int server = rd.getServer();
				String file = rd.getFile();
				String path = rd.getPath();
				String image = rd.getImage();
				float pagerate = rd.getPageRate();
				int page = rd.getPage();
				int type = rd.getType();
				if (debug) {Log.d(TAG, "onItemClick: server=" +server + ", file=" + file + ", path=" + path + ", image=" + image + ", pagerate=" + pagerate + ", page=" + page + ", type=" + type);}

				if (type == RecordItem.TYPE_COMPTEXT) {
					if (debug) {Log.d(TAG, "onItemClick: 圧縮ファイル内のテキストファイル閲覧.");}
					if (FileAccess.isDirectory(mActivity, path, mServer.getUser(server), mServer.getPass(server))) {
						// path がディレクトリの場合(異常時)
						mLoadListNextFile = "";
						mLoadListNextPath = path;
					}
					else {
						// path がディレクトリ以外の場合、親ディレクトリとファイル名に分割
						mLoadListNextFile = FileAccess.filename(mActivity, path);
						mLoadListNextPath = FileAccess.parent(mActivity, path);
					}
					mLoadListNextInFile = file;
				}
				else if (type == RecordItem.TYPE_IMAGE) {
					if (debug) {Log.d(TAG, "onItemClick: ディレクトリ内のイメージファイル閲覧.");}
					// ディレクトリか圧縮ファイルの画像オープン
					mLoadListNextFile = file;
					mLoadListNextPath = path;
					mLoadListNextInFile = "";
				}
				else if (type == RecordItem.TYPE_TEXT || type == RecordItem.TYPE_IMAGEDIRECT) {
					if (debug) {Log.d(TAG, "onItemClick: テキストまたはイメージ直接指定.");}
					// 画像直接かテキストファイルファイルオープン
					mLoadListNextFile = file;
					mLoadListNextPath = path;
					mLoadListNextInFile = image;
				}
				else {
					mLoadListNextPath = path;
				}

				// サーバ選択とパス選択をファイル一覧に反映
				moveFileSelectFromServer(server, mLoadListNextPath);


				if (type != RecordItem.TYPE_NONE && type != RecordItem.TYPE_FOLDER) {
					mLoadListNextOpen = CloseDialog.CLICK_THIS;
					mLoadListNextPageRate = pagerate;
					mLoadListNextPage = page;
				}

				if (listtype == RecordList.TYPE_DIRECTORY){
					if (debug) {Log.d(TAG, "onItemClick: ディレクトリ一覧.");}
					switchFileList(); // ファイルリストをアクティブ化
				}
				else if (listtype == RecordList.TYPE_SERVER) {
					if (debug) {Log.d(TAG, "onItemClick: サーバ一覧.");}
					if (!rd.getServerName().isEmpty()) {
						if (rd.getAccessType() == DEF.ACCESS_TYPE_PICKER) {
							if (debug) {Log.d(TAG, "onItemClick: ファイルピッカー.");}
							mLoadListNextOpen = -1;
							mSelectRecord = rd;
							showDialog(DEF.MESSAGE_EDITSERVER);
						}
						else {
							if (debug) {Log.d(TAG, "onItemClick: ファイルピッカー以外.");}
							mLoadListNextOpen = CloseDialog.CLICK_CLOSE;
							switchFileList(); // ファイルリストをアクティブ化
						}
					}
				}
			}
		}
	}

	/**
	 * 履歴系リストをファイル名でソート
	 */
	public class BookmarkComparator implements Comparator<RecordItem> {
		public int compare(RecordItem data1, RecordItem data2) {
			int result = 0;
			switch (mSortType) {
				case 0: // パス
				case 1: //
					result = data1.getServer() - data2.getServer();
					if (result == 0) {
						String str1 = data1.getPath() + data1.getFile();
						String str2 = data2.getPath() + data2.getFile();
						result = DEF.compareFileName(str1, str2);
					}
					if (result == 0) {
						String str1 = data1.getDispName() + data1.getDispName();
						String str2 = data2.getDispName() + data2.getDispName();
						result = DEF.compareFileName(str1, str2);
					}
					break;
				case 2: // 名前(昇順)
				case 3: // 名前(降順)
					String str1 = data1.getDispName();
					String str2 = data2.getDispName();
					result = DEF.compareFileName(str1, str2);
					break;
				case 4: // 登録日時(昇順)
				case 5: // 登録日時(降順)
					if (data1.getDate() == data2.getDate()) {
						result = 0;
					}
					else {
						result = data1.getDate() < data2.getDate() ? -1 : 1;
					}
					break;
			}
			// ディレクトリ/ファイルタイプ
			return result * (mSortType % 2 == 0 ? 1 : -1);
		}
	}

	/**
	 * 履歴系リストを最新状態にする
	 */
	@Override
	public void onRequestUpdate(RecordListArea list, int listtype) {
		// 履歴系リストを最新状態にする
		long modified = list.getLastModefied();
		list.setLastModefied(new Date().getTime());

		// 現在保持しているリスト
		ArrayList<RecordItem> recordList = list.getList();

		if (RecordList.checkModified(listtype, modified)) {
			// リストの読み込み
			recordList = RecordList.load(recordList, listtype);
			for (int i = 0; i < recordList.size(); i++) {
				RecordItem data = recordList.get(i);
				if (listtype != RecordList.TYPE_SERVER) {
					data.setServerName(mServer.getName(data.getServer()));
				}
			}

			int listsize = recordList.size();
			if (listtype == RecordList.TYPE_HISTORY) {
				if (mHistCount >= 0 && listsize > mHistCount) {
					// 古いものを削除
					mSortType = 4;
					Collections.sort(recordList, new BookmarkComparator());
                    // 日付昇順にして先頭を削除
                    if (listsize - mHistCount > 0) {
                        recordList.subList(0, listsize - mHistCount).clear();
                    }
				}
				if (!mLocalSave || !mSambaSave) {
					// 履歴の場合
					for (int i = recordList.size() - 1; i >= 0; i--) {
						RecordItem data = recordList.get(i);
						if ((!mLocalSave && data.getServer() == -1) || (!mSambaSave && data.getServer() != -1)) {
							recordList.remove(i);
						}
					}
				}
			}
			if (listsize > recordList.size()) {
				// 削除があった場合は更新する
				RecordList.update(recordList, listtype);
			}

			// ソート情報の読み込み
			if (listtype == RecordList.TYPE_BOOKMARK) {
				mSortType = (short) mSharedPreferences.getInt("RBSort", 0);
			} else if (listtype == RecordList.TYPE_DIRECTORY) {
				mSortType = (short) mSharedPreferences.getInt("RDSort", 0);
			} else {
				mSortType = (short) mSharedPreferences.getInt("RHSort", 5);
			}
			if (listtype != RecordList.TYPE_SERVER && listtype != RecordList.TYPE_MENU) {
				Collections.sort(recordList, new BookmarkComparator());
			}
			mListScreenView.setRecordList(list, recordList);
			mListScreenView.setListSortType(listtype, mSortType);
			// list.update(false);
		}
	}

	/**
	 * 履歴系リストに項目を追加？
	 */
	@Override
	public void onAddBookmark(String name) {
		// 名前の更新
		if (mSelectRecord != null) {
			int listtype = mListScreenView.getListType();
			mSelectRecord.setDispName(name);
			ArrayList<RecordItem> recordList = mListScreenView.getList(listtype);
			if (recordList != null) {
				RecordList.update(recordList, listtype);
			}
			mListScreenView.notifyUpdate(listtype);
		}
	}
}
