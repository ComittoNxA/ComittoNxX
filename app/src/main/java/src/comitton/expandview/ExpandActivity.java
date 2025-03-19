package src.comitton.expandview;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jp.dip.muracoro.comittonx.R;

import src.comitton.common.Logcat;
import src.comitton.config.SetTextActivity;
import src.comitton.fileview.FileSelectActivity;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetFileColorActivity;
import src.comitton.config.SetFileListActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.fileview.data.FileData;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.ListDialog;
import src.comitton.imageview.ImageActivity;
import src.comitton.fileview.data.FileListItem;
import src.comitton.fileview.filelist.FileSelectList;
import src.comitton.imageview.ImageManager;
import src.comitton.textview.TextActivity;
import src.comitton.textview.TextManager;
import src.comitton.fileview.view.TitleView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ExpandActivity extends AppCompatActivity implements Handler.Callback, OnScrollListener {
	private static final String TAG = "ExpandActivity";

	private static final int OPERATE_NONREAD = 0;
	private static final int OPERATE_READ = 1;
	private static final int OPERATE_READHERE = 2;
	private static final int OPERATE_SETTHUMBNAIL = 9;

	private TitleView mTitleView;
	private ListView mListView;

	private ImageManager mImageMgr = null;
	private ExpandThumbnailLoader mThumbnailLoader;
	private ExpandFileStatusLoader mFileStatusLoader;

	private float mDensity;

	private int mFontTitle;
	private int mFontMain;
	private int mFontSub;
	private int mItemMargin;

	private boolean mHidden;
	private int mFileSort;
	private boolean mShowExt;

	private int mBefColor;
	private int mAftColor;
	private int mNowColor;
	private int mInfColor;
	private int mBakColor;
	private int mCurColor;
	private int mTitColor;
	private int mTibColor;
	private int mTlbColor;
	private int mListRota;
	private boolean mTextPageDual;

	private int mServer;
	private String mURI;
	private String mPath;
	private String mUser;
	private String mPass;
	private String mFileName;
	private String mText;
	private int mCurrentPage;

	private ProgressDialog mReadDialog;
	private ListDialog mListDialog;
	private String[] mReadingMsg;

	private ZipLoad mZipLoad;
	private Thread mZipThread;

	private boolean mTerminate;

	private ArrayList<FileData> mFileList;
	private int mSelectIndex;
	private int mSelectPos;

	private Handler mHandler;
	private SharedPreferences mSharedPreferences = null;
	private FileListAdapter mFileListAdapter = null;

	private long mThumbID = 0;
	private boolean mThumbnail;
	private int mThumbSizeW;
	private int mThumbSizeH;
	private int mThumbNum;
	private int mThumbCrop;
	private int mThumbMargin;
	private int mFirstIndex = -1;
	private int mLastIndex = 0;

	private int mOpenOperation;
	private String mOpenLastFile;

	private AppCompatActivity mActivity = null;

	protected ListView getListView() { return findViewById( android.R.id.list ); }

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		mActivity = this;
		mDensity = getResources().getDisplayMetrics().scaledDensity;

		mHandler = new Handler(this);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SetCommonActivity.loadSettings(mSharedPreferences);

		mBefColor = SetFileColorActivity.getBefColor(mSharedPreferences);
		mNowColor = SetFileColorActivity.getNowColor(mSharedPreferences);
		mAftColor = SetFileColorActivity.getAftColor(mSharedPreferences);
		mNowColor = SetFileColorActivity.getNowColor(mSharedPreferences);
		mInfColor = SetFileColorActivity.getInfColor(mSharedPreferences);
		mBakColor = SetFileColorActivity.getBakColor(mSharedPreferences);
		mCurColor = SetFileColorActivity.getCurColor(mSharedPreferences);

		mTitColor = SetFileColorActivity.getTitColor(mSharedPreferences);
		mTibColor = SetFileColorActivity.getTibColor(mSharedPreferences);
		mTlbColor = SetFileColorActivity.getTlbColor(mSharedPreferences);

		mThumbnail = SetFileListActivity.getThumbnail(mSharedPreferences);
		mThumbSizeW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));
		mThumbSizeH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		mThumbNum = SetFileListActivity.getThumbCacheNum(mSharedPreferences);
		mThumbCrop = SetFileListActivity.getThumbCrop(mSharedPreferences);
		mThumbMargin = SetFileListActivity.getThumbMargin(mSharedPreferences);

		mFileSort = SetImageActivity.getFileSort(mSharedPreferences);
		mHidden = SetCommonActivity.getHiddenFile(mSharedPreferences);
		mShowExt = SetFileListActivity.getExtension(mSharedPreferences);

		mFontTitle = DEF.calcFontPix(SetFileListActivity.getFontTitle(mSharedPreferences), mDensity);
		mFontMain = DEF.calcFontPix(SetFileListActivity.getFontMain(mSharedPreferences), mDensity);
		mFontSub = DEF.calcFontPix(SetFileListActivity.getFontSub(mSharedPreferences), mDensity);
		mItemMargin = DEF.calcSpToPix(SetFileListActivity.getItemMargin(mSharedPreferences), mDensity);
		mListRota = SetFileListActivity.getListRota(mSharedPreferences);

		int textDispMode = SetTextActivity.getInitView(mSharedPreferences); // 表示モード(DUAL/HALF/SERIAL)
		if (textDispMode == DEF.DISPMODE_TX_DUAL) {
			mTextPageDual = true;
		}
		else {
			mTextPageDual = false;
		}

		DEF.setRotation(this, mListRota);

		// Intentを取得する
		Intent intent = getIntent();

		if (intent != null) {
			// Intentに保存されたデータを取り出す
			mServer = intent.getIntExtra("Server", -1);
			mURI = intent.getStringExtra("Uri");
			mPath = intent.getStringExtra("Path");
			mUser = intent.getStringExtra("User");
			mPass = intent.getStringExtra("Pass");
			mFileName = intent.getStringExtra("File"); 		// ZIP指定時
			mText = intent.getStringExtra("Text"); 			// Textファイル
			Logcat.d(logLevel, "mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
					+ ", mUser=" + mUser + ", mPass=" + mPass
					+ ", mFileName=" + mFileName + ", mText=" + mText);
		}

		setContentView(R.layout.serverview);

		mTitleView = (TitleView) this.findViewById(R.id.title);
		Resources res = getResources();
		mTitleView.setTextSize(mFontTitle, mTitColor, mTibColor);
		mTitleView.setTitle("[" + res.getString(R.string.compTitle) + "]", mFileName);

		mListView = this.getListView();
		mListView.setBackgroundColor(mBakColor);
		mListView.setFastScrollEnabled(true);
		// イベント組み込み
		mListView.setOnItemLongClickListener(new MyClickAdapter());
		// スクロール位置通知
		mListView.setOnScrollListener(this);
		try {
			Method setLayerTypeMethod = mListView.getClass().getMethod("setLayerType", new Class[] {int.class, Paint.class});
			setLayerTypeMethod.invoke(mListView, new Object[] {View.LAYER_TYPE_SOFTWARE, null});
		} catch (Exception e) {
			;
		}

		LinearLayout linear = (LinearLayout) this.findViewById(R.id.listlayout);
		linear.setBackgroundColor(mBakColor);
		mTerminate = false;

		mOpenOperation = CloseDialog.CLICK_CLOSE;
		mOpenLastFile = null;

		loadListView();

		mListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
				int logLevel = Logcat.LOG_LEVEL_WARN;
				ArrayList<FileData> files = mFileList;

				if (position < files.size()) {
					FileData file = (FileData) files.get(position);
					String name = file.getName();
					int type = file.getType();

					// 現在のパスを設定
					mSelectIndex = position;
					mSelectPos = 0;

					if (type == FileData.FILETYPE_TXT || type == FileData.FILETYPE_EPUB_SUB) {
						// TXTファイル表示
						Logcat.d(logLevel, "openTextFile()");
						openTextFile(name);
					}
					else {
						// イメージファイル表示
						openImageFile(name);
					}

					freeListView();
				}
			}
		} );
		//mListView.setAdapter( adapter );
	}

	public class ZipLoad implements Runnable {
		private final Handler handler;
		private final AppCompatActivity mActivity;

		public ZipLoad(Handler handler, AppCompatActivity activity) {
			super();
			this.handler = handler;
			this.mActivity = activity;
		}

		public void run() {
			// ファイルリストの読み込み
			mImageMgr = new ImageManager(this.mActivity, DEF.relativePath(mActivity, mURI, mPath), mFileName, mUser, mPass, mFileSort, handler, mHidden, ImageManager.OPENMODE_LIST, 1);
			mImageMgr.LoadImageList(0, 0, 0);

			// 終了通知
			Message message = new Message();
			message.what = DEF.HMSG_READ_END;
			handler.sendMessage(message);
		}
	}

	private float mScreenWay = 0;

	/**
	 * 画面の設定が変更された時に発生します。
	 *
	 * @param newConfig
	 *            新しい設定。
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		View mRootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
		float w = mRootView.getWidth();
		float h = mRootView.getHeight();
		if (w == 0.0f || h == 0.0f) {
			return;
		}
		float way = w / h;
		if (mScreenWay == 0 || (mScreenWay < 1.0f && way > 1.0f) || (mScreenWay > 1.0f && way < 1.0f)) {
			mScreenWay = way;
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keycode = event.getKeyCode();
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keycode) {
				case KeyEvent.KEYCODE_BACK:
					finishActivity();
					break;
				default:
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	// Activityの破棄
	protected void onDestroy() {
		super.onDestroy();

		// サムネイルスレッド終了
		if (mThumbnailLoader != null) {
			mThumbnailLoader.breakThread();
			mThumbnailLoader = null;
		}

		// 既読情報スレッド終了
		if (mFileStatusLoader != null) {
			mFileStatusLoader.breakThread();
			mFileStatusLoader = null;
		}

		mImageMgr = null;
		return;
	}

	// 画面遷移が戻ってきた時の通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. requestCode=" + requestCode + ", resultCode=" + resultCode);
		if (requestCode == DEF.REQUEST_IMAGE) {
			Logcat.d(logLevel, "requestCode == DEF.REQUEST_IMAGE");
			if (resultCode == RESULT_OK && data != null) {
				Logcat.d(logLevel, "onActivityResult: resultCode == RESULT_OK");
				int nextopen = data.getExtras().getInt("NextOpen", -1);
				String lastfile = data.getExtras().getString("LastFile");

				if (nextopen != CloseDialog.CLICK_CLOSE) {
					Logcat.d(logLevel, "nextopen != CloseDialog.CLICK_CLOSE. ビュワーから復帰しました.");
					// ビュワーからの復帰
					Intent intent = new Intent();
					intent.putExtra("NextOpen", nextopen);
					intent.putExtra("LastFile", lastfile);
					setResult(RESULT_OK, intent);
					finishActivity();
					return;
				}
				else {
					Logcat.d(logLevel, "nextopen == CloseDialog.CLICK_CLOSE");
				}
			}
		}
		else if (requestCode == DEF.REQUEST_TEXT) {
			Logcat.d(logLevel, "requestCode == DEF.REQUEST_TEXT");
			if (resultCode == RESULT_OK && data != null) {
				Logcat.d(logLevel, "resultCode == RESULT_OK. ビュワーから復帰しました.");
				mOpenOperation = data.getExtras().getInt("NextOpen", -1);
				mOpenLastFile = data.getExtras().getString("LastFile");
			}
			else {
				Logcat.d(logLevel, "resultCode != RESULT_OK");
				mOpenOperation = CloseDialog.CLICK_CLOSE;
				mOpenLastFile = null;
			}
		}

		// 他画面から戻ったときは設定＆リスト更新
		loadListView();
		Logcat.d(logLevel, "終了します");
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// スクロールイベント通知

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. firstVisibleItem=" + firstVisibleItem + ", visibleItemCount=" + visibleItemCount  + ", totalItemCount=" + totalItemCount);

		// 位置変更
		if (mFirstIndex != firstVisibleItem || mLastIndex != firstVisibleItem + visibleItemCount) {
			// リストボックスの位置が変わったときに通知
			Logcat.d(logLevel, "変更あり.");

			mFirstIndex = firstVisibleItem;
			mLastIndex = mFirstIndex + visibleItemCount;

			if (mThumbnailLoader != null) {
				mThumbnailLoader.setDispRange(mFirstIndex, mLastIndex);
			}
			if (mFileStatusLoader != null) {
				mFileStatusLoader.setDispRange(mFirstIndex, mLastIndex);
			}
		}
	}

	/*
	@Override
	// 選択イベント
	protected void onListItemClick(ListView listView, View v, int position, long id) {
		super.onListItemClick(listView, v, position, id);
		ArrayList<FileData> files = mFileList;

		if (position < files.size()) {
			FileData file = (FileData) files.get(position);
			String name = file.getName();
			int type = file.getType();

			freeListView();

			// 現在のパスを設定
			mSelectIndex = position;
			mSelectPos = 0;
//			mSelectIndex = mListView.getFirstVisiblePosition();
//			mSelectPos = 0;
//			View lvi = mListView.getChildAt(0);
//			if (lvi != null) {
//				mSelectPos = v.getTop();
//			}
			if (type == FileData.FILETYPE_EPUB_SUB || type == FileData.FILETYPE_TXT) {
				// TXTファイル表示
				openTextFile(name);
			}
			else {
				// イメージファイル表示
				openImageFile(name);
			}
		}
	}
	*/

	private void openImageFile(String image) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		Toast.makeText(this, image, Toast.LENGTH_SHORT).show();

		if (mFileList == null) {
			Logcat.d(logLevel, "mFileList=" + mFileList);
		}
		else {
			Logcat.d(logLevel, "mFileList.size()=" + mFileList.size());
		}

		ArrayList<FileData> sortfiles = new ArrayList<FileData>(mFileList.size());
		for (FileData fd : mFileList) {
			if (fd.getType() == FileData.FILETYPE_IMG) {
				sortfiles.add(fd);
			}
		}
		Collections.sort(sortfiles, new FileSelectActivity.FilenameComparator());

		// ソート後に現在ファイルを探す
		FileData searchfd = new FileData(mActivity, image);
		int index = sortfiles.indexOf(searchfd);
		Logcat.d(logLevel, "index=" + index);

		Editor ed = mSharedPreferences.edit();
		ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass), index);
		ed.apply();

		Intent intent = new Intent(ExpandActivity.this, ImageActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Server", mServer);
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mUser);
		intent.putExtra("Pass", mPass);
		intent.putExtra("File", mFileName);
		intent.putExtra("Image", image);
		startActivityForResult(intent, DEF.REQUEST_IMAGE);
	}

	private void openTextFile(String text) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "text=" + text);

		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		Intent intent;
		intent = new Intent(ExpandActivity.this, TextActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Server", mServer);
		intent.putExtra("Uri", mURI);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mUser);
		intent.putExtra("Pass", mPass);
		intent.putExtra("File", mFileName);
		intent.putExtra("Text", text);
		startActivityForResult(intent, DEF.REQUEST_TEXT);
	}

	/*
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		Resources res = getResources();

		// メニューをクリア
		menu.clear();

		// 更新
		menu.add(0, DEF.MENU_REFRESH, Menu.NONE, res.getString(R.string.refresh)).setIcon(R.drawable.ic_menu_refresh);
		// サムネイル切替
		menu.add(0, DEF.MENU_THUMBSWT, Menu.NONE, res.getString(R.string.thumbSwt)).setIcon(android.R.drawable.ic_menu_gallery);
		// ヘルプ
		menu.add(0, DEF.MENU_ONLINE, Menu.NONE, res.getString(R.string.onlineMenu)).setIcon(android.R.drawable.ic_menu_set_as);
		return ret;
	}
	*/

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == DEF.MENU_REFRESH) {
			// 表示更新
			updateListView();
		}
		else if (id == DEF.MENU_THUMBSWT) {
			// サムネイル表示切替
			mThumbnail = !mThumbnail;
			updateListView();

			// サムネイル読み込み
			loadThumbnail();
			loadFileState();
		}
		else if (id == DEF.MENU_ONLINE) {
			// 操作方法画面に遷移
			Resources res = getResources();
			String url = res.getString(R.string.url_complist);	// 設定画面
			Intent intent;
			intent = new Intent(ExpandActivity.this, HelpActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("Url", url);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	// 長押しイベント処理用クラス
	class MyClickAdapter implements OnItemLongClickListener {
		private final int[] mOperate = { -1, -1, -1, -1 };

		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Resources res = getResources();
			ArrayList<FileData> files = mFileList;
			final FileData filedata = files.get(position);
			final int datapos = position;

			String[] items;

			String ope0 = res.getString(R.string.ope00);	// 未読設定
			String ope1 = res.getString(R.string.ope01);	// 既読設定
			String ope6 = res.getString(R.string.ope06);	// ここまで読んだ
			String ope7 = res.getString(R.string.ope100);

			int state = filedata.getState();
			int itemnum;
			if (filedata.getType() == FileData.FILETYPE_TXT || filedata.getType() == FileData.FILETYPE_EPUB_SUB) {
				// テキストファイル長押し
				switch (state) {
					case -1:
					case -2:
						itemnum = 1;
						break;
					default:
						itemnum = 2;
						break;
				}
			}
			else {
				itemnum = 2;
			}

			items = new String[itemnum];

			int i = 0;
			if (filedata.getType() == FileData.FILETYPE_TXT || filedata.getType() == FileData.FILETYPE_EPUB_SUB) {
				// テキストファイル長押し
				if (state != DEF.PAGENUMBER_UNREAD) {
					// 未読にする
					items[i] = ope0;
					mOperate[i] = OPERATE_NONREAD;
					i++;
				}
				if (state != DEF.PAGENUMBER_READ) {
					// 既読にする
					items[i] = ope1;
					mOperate[i] = OPERATE_READ;
					i++;
				}
			}
			else {
				// イメージファイル長押し
				items[i] = ope6;
				mOperate[i] = OPERATE_READHERE;
				i++;
				items[i] = ope7;
				mOperate[i] = OPERATE_SETTHUMBNAIL;
				i++;
			}

			mListDialog = new ListDialog(mActivity, R.style.MyDialog, res.getString(R.string.opeTitle), items, -1, new ListDialog.ListSelectListener() {
				public void onSelectItem(int pos) {
					int logLevel = Logcat.LOG_LEVEL_WARN;
					Logcat.d(logLevel, "開始します.");

					if (pos < 0 && 2 < pos) {
						// 選択インデックスが範囲外
						return;
					}

					Editor ed;

					switch (mOperate[pos]) {
						case OPERATE_NONREAD: {
							// 未読にする
							ed = mSharedPreferences.edit();
							ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + filedata.getName() + "#maxpage");
							ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + filedata.getName());
							//ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + filedata.getName() + "#date");
							ed.apply();
							updateListView();
							break;
						}
						case OPERATE_READ: {
							// 既読にする
							ed = mSharedPreferences.edit();
							ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + filedata.getName() + "maxpage", DEF.PAGENUMBER_READ);
							ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + filedata.getName(), DEF.PAGENUMBER_READ);
							//ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + filedata.getName() + "#date", (int)((filedata.getDate() / 1000)));
							ed.apply();
							updateListView();
							break;
						}
						case OPERATE_READHERE: {
							// ここまで読んだ
							int state = DEF.PAGENUMBER_READING;
							for (int i = 0 ; i <= datapos ; i ++) {
								if (mFileList.get(i).getType() != FileData.FILETYPE_TXT && mFileList.get(i).getType() != FileData.FILETYPE_EPUB_SUB) {
									state ++;
								}
							}

							ed = mSharedPreferences.edit();
							//int maxpage = mSharedPreferences.getInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
							ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass), state);
							ed.apply();
							updateListView();
							break;
						}
						case OPERATE_SETTHUMBNAIL: {
							FileData data = mFileList.get(datapos);
							String path2 = DEF.relativePath(mActivity, mURI, mPath, mFileName);
							String path1 = path2 + ":" + data.getName();
							mThumbnailLoader.setThumbnailCache(path1, path2);
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
			return true;

/*
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExpandActivity.this, R.style.MyDialog);

			if (filedata == null) {
				// データがない
				return true;
			}

			CharSequence[] items;

			dialogBuilder.setTitle(res.getString(R.string.opeTitle));
			String ope0 = res.getString(R.string.ope00);	// 未読設定
			String ope1 = res.getString(R.string.ope01);	// 既読設定
			String ope6 = res.getString(R.string.ope06);	// ここまで読んだ
			String ope7 = res.getString(R.string.ope100);

			int state = filedata.getState();
			int itemnum;
			if (filedata.getType() == FileData.FILETYPE_TXT || filedata.getType() == FileData.FILETYPE_EPUB_SUB) {
				// テキストファイル長押し
				switch (state) {
					case -1:
					case -2:
						itemnum = 1;
						break;
					default:
						itemnum = 2;
						break;
				}
			}
			else {
				itemnum = 2;
			}

			items = new CharSequence[itemnum];

			int i = 0;
			if (filedata.getType() == FileData.FILETYPE_TXT || filedata.getType() == FileData.FILETYPE_EPUB_SUB) {
				// テキストファイル長押し
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
			}
			else {
				// イメージファイル長押し
				items[i] = ope6;
				mOperate[i] = OPERATE_READHERE;
				i++;
				items[i] = ope7;
				mOperate[i] = OPERATE_SETTHUMBNAIL;
				i++;
			}

			dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item < 0 && 2 < item) {
						// 選択インデックスが範囲外
						return;
					}

					Editor ed;

					switch (mOperate[item]) {
						case OPERATE_NONREAD: { // 未読にする
							ed = mSharedPreferences.edit();
							ed.remove(DEF.createUrl(mUri + mPath + mFileName + filedata.getName(), mUser, mPass));
							ed.apply();
							updateListView();
							break;
						}
						case OPERATE_READ: { // 既読にする
							ed = mSharedPreferences.edit();
							ed.putInt(DEF.createUrl(mUri + mPath + mFileName + filedata.getName(), mUser, mPass), -2);
							ed.apply();
							updateListView();
							break;
						}
						case OPERATE_READHERE: { // ここまで読んだ
							int state = 0;
							for (int i = 0 ; i <= datapos ; i ++) {
								if (mFileList.get(i).getType() != FileData.FILETYPE_TXT && mFileList.get(i).getType() != FileData.FILETYPE_EPUB_SUB) {
									state ++;
								}
							}

							ed = mSharedPreferences.edit();
							ed.putInt(DEF.createUrl(mUri + mPath + mFileName, mUser, mPass), state);
							ed.apply();
							updateListView();
							break;
						}
						case OPERATE_SETTHUMBNAIL: {
							FileData data = mFileList.get(datapos);
							String filepath = mUri + mPath + mFileName + ":" + data.getName();
							String filepath2 = mUri + mPath + mFileName;
							mThumbnailLoader.setThumbnailCache(filepath, filepath2);
							break;
						}
					}
				}
			});
			AlertDialog alert = dialogBuilder.create();
			alert.show();
			return true;
 */
		}
	}

	public class FileListAdapter extends ArrayAdapter<FileData> {
		private final ArrayList<FileData> items;
		private final LayoutInflater inflater;

		public FileListAdapter(Context context, int textViewResourceId, ArrayList<FileData> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

        @NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			// ビューを受け取る
			View view = convertView;
			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.listitem, null);
				// 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);
			}

			// 表示すべきデータの取得
			FileData item = (FileData) items.get(position);
			if (item != null) {
				ListItemView itemView = (ListItemView) view.findViewById(R.id.listitem);
				int size = mFontSub;
				boolean thumbflag = false;

				if (mThumbnail) {
					if (item.getType() != FileData.FILETYPE_TXT && item.getType() != FileData.FILETYPE_EPUB_SUB) {
						thumbflag = true;
					}
				}

				int color;
				switch (item.getState()) {
					case DEF.PAGENUMBER_UNREAD:
						color = mBefColor;
						break;
					case DEF.PAGENUMBER_READ:
						color = mAftColor;
						break;
					default:
						color = mNowColor;
						break;
				}

				String readInfo = "";
				int readColor = 0x000000;
				if (item.getType() == FileData.FILETYPE_IMG) {
					if (item.getState() == DEF.PAGENUMBER_READ) {
						readInfo = "Read.";
						readColor = mAftColor;
					} else if (item.getState() == DEF.PAGENUMBER_UNREAD) {
						readInfo = "Unread.";
						readColor = mBefColor;
					}
				}
				else {
					if ((item.getState() >= 0) && (item.getMaxpage() > 0)) {
						// 読書率
						float rate = (float) (item.getState() + 1) / (float) item.getMaxpage();
						readInfo = (int) (rate * 100) + "% Read.";
						readColor = mNowColor;
					} else if (item.getState() == DEF.PAGENUMBER_READ) {
						readInfo = "100% Read.";
						readColor = mAftColor;
					} else if (item.getState() == DEF.PAGENUMBER_UNREAD) {
						readInfo = "Unread.";
						readColor = mBefColor;
					} else if (item.getState() != DEF.PAGENUMBER_UNREAD && item.getMaxpage() == DEF.PAGENUMBER_NONE) {
						readInfo = "??% Read.";
						readColor = mNowColor;
					}
				}

				itemView.setDrawInfo(color, mTlbColor, mInfColor, readColor, mFontMain, size, thumbflag, mThumbSizeW, mThumbSizeH, mItemMargin);
				itemView.setFileInfo(mThumbID, position, thumbflag, item.getName(), item.getFileInfo(), readInfo, mShowExt);
				itemView.setMarker(mBakColor, mCurColor);
			}
			return view;
		}
	}

	String mMessage = "";
	String mMessage2 = "";
	String mWorkMessage = "";
	// スレッドからの通知取得
	public boolean handleMessage(Message msg) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		switch (msg.what) {
			case DEF.HMSG_PROGRESS:
				// 読込中の表示
				synchronized (this) {
					if (mReadDialog != null) {
						// ページ読み込み中
						String readmsg;
						if (msg.arg2 < mReadingMsg.length && mReadingMsg[msg.arg2] != null) {
							readmsg = mReadingMsg[msg.arg2];
						} else {
							readmsg = "Loading...";
						}
						mMessage = MessageFormat.format("{0} ({1})", new Object[]{readmsg, msg.arg1});
						mReadDialog.setMessage(DEF.ProgressMessage(mMessage, mMessage2, mWorkMessage));
					}
				}
				return true;

			case DEF.HMSG_ERROR: {
				// 読込中の表示
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				return true;
			}
			case DEF.HMSG_WORKSTREAM:
				// ファイルアクセスの表示
				return true;
			case DEF.HMSG_READ_END: {
				Logcat.v(logLevel, "DEF.HMSG_READ_END. ImageManager の読み込みが終了しました.");
				// 読込中の表示
				if (mReadDialog != null) {
					mReadDialog.dismiss();
					mReadDialog = null;
				}
//				mReadRunning = false;
				if (mTerminate) {
					finishActivity();
					return true;
				}

				loadListViewAfter();

				// レジュームオープン
				if (mText != null && !mText.isEmpty()) {
					// TXTファイル表示
					openTextFile(mText);
					mText = null;
				}
				else if (mOpenOperation != CloseDialog.CLICK_CLOSE) {
					Logcat.v(logLevel, "mOpenOperation != CloseDialog.CLICK_CLOSE");
					// 次のファイル検索
					FileData nextfile = searchNextFile(mFileList, mOpenLastFile, mOpenOperation);
					if (nextfile != null && !nextfile.getName().isEmpty()) {
						Logcat.d(logLevel, "nextfile=" + nextfile.getName());
						Editor ed = mSharedPreferences.edit();
						switch (mOpenOperation) {
							case CloseDialog.CLICK_PREVTOP:
							case CloseDialog.CLICK_NEXTTOP:
								ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName() + "#maxpage");
								ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName());
								//ed.remove(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName() + "#date");
								updateListView();
								ed.apply();
								break;

							case CloseDialog.CLICK_PREVLAST:
							case CloseDialog.CLICK_NEXTLAST:
								// DEF.REQUEST_TEXT時のみ呼び出される
								int	maxpage;
								maxpage = mSharedPreferences.getInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName(), DEF.PAGENUMBER_NONE);
								// 未読の場合と未読で無かった場合で場合分けする
								if (maxpage > 0) {
									//	未読で無かった場合は最大ページ数を最終ページにする
									ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName() + "#maxpage", maxpage);
									ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName(), maxpage);
									//ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName() + "#date", (int)((nextfile.getDate() / 1000)));
									ed.apply();
								} else {
									//	未読の場合はバックグラウンドで計算する
									// ファイルリストの読み込み
									ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName(), DEF.PAGENUMBER_READ);
									//ed.putInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass) + nextfile.getName() + "#date", (int)((nextfile.getDate() / 1000)));
									ed.apply();
									mFileStatusLoader.update(nextfile);
								}
								updateListView();
								break;
						}
                        // 次のファイルがあれば開く
                        openTextFile(nextfile.getName());
                    }
				}
				else {
					Logcat.v(logLevel, "mOpenOperation == CloseDialog.CLICK_CLOSE");
					// 初回表示またはビュワー終了
					updateListView();

					// サムネイル読み込み
					loadThumbnail();
					// 既読情報読み込み
					loadFileState();
				}
				return true;
			}
			case DEF.HMSG_THUMBNAIL, DEF.HMSG_FILE_STATUS:
				Logcat.v(logLevel, "HMSG_THUMBNAIL or HMSG_FILE_STATUS");
				// Bitmapの通知
				String name = (String) msg.obj;
				int bmIndex = msg.arg1;
				if (name != null && mFileList != null) {
					ArrayList<FileData> files = mFileList;

					for (int i = 0; i < files.size(); i++) {
						if (name.equals(files.get(i).getName())) {
							if (msg.what == DEF.HMSG_FILE_STATUS) {
								// 既読情報の更新
								readState(files.get(i));
								updateListView();
							}
							// リストの更新
							mFileListAdapter.notifyDataSetChanged();
							break;
						}
					}
				}
				return true;

		}
		return true;
	}

	private void loadListView() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		Resources res = getResources();
		mReadingMsg = new String[1];
		mReadingMsg[0] = res.getString(R.string.reading);

		// プログレスダイアログ準備
		mReadDialog = new ProgressDialog(this, R.style.MyDialog);
		mReadDialog.setMessage(mReadingMsg[0] + " (0)");
		mReadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mReadDialog.setCancelable(true);
		mReadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// Thread を停止
				if (mImageMgr != null) {
					mImageMgr.setBreakTrigger();
				}
				mTerminate = true;
			}
		});
		mReadDialog.show();

//		mReadRunning = true;
		mZipLoad = new ZipLoad(mHandler, this);
		mZipThread = new Thread(mZipLoad);
		mZipThread.start();
		Logcat.d(logLevel, "終了します.");
	}

	private void loadListViewAfter() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		// しおり情報取得
		int	maxpage = mSharedPreferences.getInt(DEF.createUrl(DEF.relativePath(mActivity,mURI, mPath, mFileName), mUser, mPass) + "#maxpage", DEF.PAGENUMBER_NONE);
		mCurrentPage = mSharedPreferences.getInt(DEF.createUrl(DEF.relativePath(mActivity,mURI, mPath, mFileName), mUser, mPass), DEF.PAGENUMBER_UNREAD);
		Logcat.d(logLevel, "mCurrentPage=" + mCurrentPage);

		// ファイルリスト
		FileListItem[] files = mImageMgr.getList();
		int filenum = 0;
		if (files != null) {
			filenum = files.length;
		}
		mFileList = new ArrayList<FileData>(filenum);

		int imageCnt = 0;
		for (int i = 0; i < filenum; i++) {
			int state = DEF.PAGENUMBER_UNREAD;
			FileData data = new FileData(mActivity, files[i].name, files[i].orglen, files[i].dtime);

			if (files[i].type == FileData.FILETYPE_IMG) {
				if (mCurrentPage == DEF.PAGENUMBER_READ) {
					if	(maxpage == DEF.PAGENUMBER_NONE)	{
					}
					else if	(mCurrentPage >= (maxpage - 1))	{
						state = DEF.PAGENUMBER_READ;
					}
				}
				else if (mCurrentPage >= 0) {
					if (imageCnt < mCurrentPage) {
						state = DEF.PAGENUMBER_READ;
					}
				}
				data.setState(state);
				imageCnt ++;
				mFileList.add(data);
				Logcat.d(logLevel, "name=" + files[i].name + ", state=" + state);
			}
			else if(files[i].type == FileData.FILETYPE_TXT || files[i].name.equals("META-INF/container.xml")) {
				Logcat.d(logLevel, "FILETYPE_TXT url=" + DEF.relativePath(mActivity,mURI, mPath, mFileName) + files[i].name);
				readState(data);
				mFileList.add(data);
				Logcat.d(logLevel, "name=" + files[i].name + ", state=" + state);
			}
		}

		Collections.sort(mFileList, new FilenameComparator());

		mFileListAdapter = new FileListAdapter(this, R.layout.listitem, mFileList);
		//setListAdapter(mFileListAdapter);
		mListView.setAdapter(mFileListAdapter);

		if (mSelectIndex >= mFileList.size()) {
			mSelectIndex = mFileList.size() - 1;
			mSelectPos = 0;
		}
		else if (mSelectIndex < 0) {
			mSelectIndex = 0;
			mSelectPos = 0;
		}

		mListView.setSelectionFromTop(mSelectIndex, mSelectPos);
	}

	private void updateListView() {
		// エラーチェック
		if (mFileList == null){
			return;
		}

		// しおり情報取得
		mCurrentPage = mSharedPreferences.getInt(DEF.createUrl(DEF.relativePath(mActivity, mURI, mPath, mFileName), mUser, mPass), 0);

		// ファイルリスト
		int imageCnt = 0;
		ArrayList<FileData> files = mFileList;
		for (int i = 0; i < files.size() ; i++) {
			FileData data = files.get(i);

			int state = DEF.PAGENUMBER_UNREAD;
			if (data.getType() == FileData.FILETYPE_IMG) {
				if (mCurrentPage == DEF.PAGENUMBER_READ) {
					state = DEF.PAGENUMBER_READ;
				}
				else if (mCurrentPage >= 0) {
					if (imageCnt < mCurrentPage) {
						state = DEF.PAGENUMBER_READ;
					}
				}
				data.setState(state);
				imageCnt ++;
			}
			else {
				readState(data);
			}

		}
		mFileListAdapter.notifyDataSetChanged();
	}

	private void freeListView() {
		// サムネイル解放
		releaseThumbnail();

		mFileListAdapter.clear();
		mFileList = null;
	}

	// 既読情報読み込み
	private void loadFileState() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mFileStatusLoader != null) {
			// 今動いてるのは止める
			mFileStatusLoader.breakThread();
		}
		mFileStatusLoader = new ExpandFileStatusLoader(mActivity, mImageMgr, mURI, mPath, mFileName, mUser, mPass, mHandler, mFileList, mHidden);
		mFileStatusLoader.setDispRange(mFirstIndex, mLastIndex);
	}

	// サムネイル読み込み
	private void loadThumbnail() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		if (!mThumbnail) {
			return;
		}

		if (mThumbnailLoader != null) {
			// 今動いてるのは止める
			mThumbnailLoader.breakThread();
		}
		mImageMgr.unsetBreakTrigger();
		mThumbID = System.currentTimeMillis();

		mThumbnailLoader = new ExpandThumbnailLoader(mActivity, mURI, DEF.relativePath(mActivity, mPath, mFileName), mHandler, mThumbID, mImageMgr, mFileList, mThumbSizeW, mThumbSizeH, mThumbNum, mThumbCrop, mThumbMargin);
		mThumbnailLoader.setDispRange(mFirstIndex, mLastIndex);
	}

	public void readState(FileData fileData) {

		if (fileData.getType() == FileData.FILETYPE_IMG){
			return;
		}

		int maxpage;
		int state;

		String currentPath = DEF.relativePath(mActivity, mURI, mPath);
		String name = fileData.getName();
		String uri = DEF.createUrl(DEF.relativePath(mActivity, currentPath, mFileName), mUser, mPass);

		String maxpageKey;
		String stateKey;

		switch (fileData.getType()) {
			case FileData.FILETYPE_TXT:
				maxpageKey = uri + name + "#maxpage";
				stateKey = uri + name;
				break;
			case FileData.FILETYPE_EPUB_SUB:
				maxpageKey = uri + "META-INF/container.xml" + "#maxpage";
				stateKey = uri + "META-INF/container.xml";
				break;
			default:
				// なにもしない
				return;
		}

		maxpage = mSharedPreferences.getInt(maxpageKey, DEF.PAGENUMBER_NONE);
		state = mSharedPreferences.getInt(stateKey, DEF.PAGENUMBER_UNREAD);
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
				if (mTextPageDual) {
					state = DEF.PAGENUMBER_READ;
				}
			}
		}

		fileData.setMaxpage(maxpage);
		fileData.setState(state);
	}

	// 解放
	private void releaseThumbnail() {
		// サムネイル読み込みスレッドを停止
		if (mThumbnailLoader != null) {
			mThumbnailLoader.breakThread();
			mThumbnailLoader.releaseThumbnail();
			mThumbnailLoader = null;
		}

		// 既読情報読み込みスレッドを停止
		if (mFileStatusLoader != null) {
			mFileStatusLoader.breakThread();
			mFileStatusLoader.releaseThumbnail();
			mFileStatusLoader = null;
		}

		// イメージ管理解放
		if (mImageMgr != null) {
			mImageMgr.closeFiles();
			mImageMgr = null;
		}
	}

	public FileData searchNextFile(ArrayList<FileData> files, String file, int nextopen) {
		FileData nextfile = null;
		ArrayList<FileData> sortfiles = new ArrayList<FileData>(files.size());

		for (FileData fd : files) {
			int type = fd.getType();
			switch (type) {
				case FileData.FILETYPE_TXT: // テキスト
					sortfiles.add(fd);
					break;
			}
		}
		Collections.sort(sortfiles, new FilenameComparator());

		// ソート後に現在ファイルを探す
		FileData fd = new FileData(mActivity, file);
		int index = sortfiles.indexOf(fd);
		if (index >= 0) {
			// 見つかった場合
			switch (nextopen) {
				case CloseDialog.CLICK_NEXT:
				case CloseDialog.CLICK_NEXTTOP:
				case CloseDialog.CLICK_NEXTLAST:
					// 次のファイル
					index ++;
					break;
				case CloseDialog.CLICK_PREV:
				case CloseDialog.CLICK_PREVTOP:
				case CloseDialog.CLICK_PREVLAST:
					// 前のファイル
					index --;
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
			if (file1.getName().equals("META-INF/container.xml")) {
				return -1;
			}
			else if (file2.getName().equals("META-INF/container.xml")) {
				return 1;
			}

			return DEF.compareFileName(file1.getName(), file2.getName());
		}
	}

	private void finishActivity() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		// サムネイルスレッド終了
		releaseThumbnail();

		if (mImageMgr != null) {
			try {
				mImageMgr.close();
			}
			catch (IOException e) {
				Logcat.e(logLevel, "", e);
			}
			mImageMgr = null;
		}
		finish();
	}
}
