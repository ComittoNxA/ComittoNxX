package src.comitton.activity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jp.dip.muracoro.comittonx.R;

import src.comitton.common.DEF;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetFileColorActivity;
import src.comitton.config.SetFileListActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.data.FileData;
import src.comitton.dialog.CloseDialog;
import src.comitton.stream.ExpandThumbnailLoader;
import src.comitton.stream.FileListItem;
import src.comitton.stream.ImageManager;
import src.comitton.view.ListItemView;
import src.comitton.view.TitleView;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ExpandActivity extends ListActivity implements Handler.Callback, OnScrollListener {
	private static final int OPERATE_NONREAD = 0;
	private static final int OPERATE_READ = 1;
	private static final int OPERATE_READHERE = 2;
	private static final int OPERATE_SETTHUMBNAIL = 9;

	private TitleView mTitleView;
	private ListView mListView;

	private ImageManager mImageMgr = null;
	private ExpandThumbnailLoader mThumbnailLoader;

	private float mDensity;

	private int mFontTitle;
	private int mFontMain;
	private int mFontSub;
	private int mItemMargin;

	private boolean mHidden;
	private String mCharset;
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

	private int mServer;
	private String mUri;
	private String mPath;
	private String mUser;
	private String mPass;
	private String mFileName;
	private String mText;
	private int mPage;
	private int mCurrentPage;

	private ProgressDialog mReadDialog;
	private String mReadingMsg[];

	private ZipLoad mZipLoad;
	private Thread mZipThread;

	private boolean mTerminate;
//	private boolean mReadRunning; // 読み込み中フラグ

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

	private ExpandActivity mActivity = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = this;
		mDensity = getResources().getDisplayMetrics().scaledDensity;

		mHandler = new Handler(this);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
		mCharset = DEF.CharsetList[SetCommonActivity.getCharset(mSharedPreferences)];
		mHidden = SetCommonActivity.getHiddenFile(mSharedPreferences);
		mShowExt = SetFileListActivity.getExtension(mSharedPreferences);

		mFontTitle = DEF.calcFontPix(SetFileListActivity.getFontTitle(mSharedPreferences), mDensity);
		mFontMain = DEF.calcFontPix(SetFileListActivity.getFontMain(mSharedPreferences), mDensity);
		mFontSub = DEF.calcFontPix(SetFileListActivity.getFontSub(mSharedPreferences), mDensity);
		mItemMargin = DEF.calcSpToPix(SetFileListActivity.getItemMargin(mSharedPreferences), mDensity);
		mListRota = SetFileListActivity.getListRota(mSharedPreferences);
		DEF.setRotation(this, mListRota);

		// Intentを取得する
		Intent intent = getIntent();

		if (intent != null) {
			// Intentに保存されたデータを取り出す
			mServer = intent.getIntExtra("Server", -1);
			mUri = intent.getStringExtra("Uri");
			mPath = intent.getStringExtra("Path");
			mUser = intent.getStringExtra("User");
			mPass = intent.getStringExtra("Pass");
			mFileName = intent.getStringExtra("File"); // ZIP指定時
			mText = intent.getStringExtra("Text"); // Textファイル
			mPage = intent.getIntExtra("Page", -1); // Textファイルのページ
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
	}

	public class ZipLoad implements Runnable {
		private Handler handler;
		private Activity mActivity;

		public ZipLoad(Handler handler, Activity activity) {
			super();
			this.handler = handler;
			this.mActivity = activity;
		}

		public void run() {
			// ファイルリストの読み込み
			mImageMgr = new ImageManager(this.mActivity, mUri + mPath, mFileName, mUser, mPass, mFileSort, handler, mCharset, mHidden, ImageManager.OPENMODE_LIST, 1);
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
		WindowManager windowmanager = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display disp = windowmanager.getDefaultDisplay();
		float w = disp.getWidth();;
		float h = disp.getHeight();
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

		mImageMgr = null;
		return;
	}

	// 画面遷移が戻ってきた時の通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("ExpandActivity", "onActivityResult: 開始します. requestCode=" + requestCode + ", resultCode=" + resultCode);
//		if (mImageMgr == null) {
//			// 何らかのエラーでActivityが再作成されてしまった
//			// 暫定的に終了
//			Log.e("onActivityResult", "mImageMgr == null");
//			return;
//		}
		if (requestCode == DEF.REQUEST_IMAGE) {
			Log.d("ExpandActivity", "onActivityResult: requestCode == DEF.REQUEST_IMAGE");
			if (resultCode == RESULT_OK && data != null) {
				Log.d("ExpandActivity", "onActivityResult: resultCode == RESULT_OK");
				int nextopen = data.getExtras().getInt("nextopen", -1);
				String lastfile = data.getExtras().getString("lastfile");

				if (nextopen != CloseDialog.CLICK_CLOSE) {
					Log.d("ExpandActivity", "onActivityResult: nextopen != CloseDialog.CLICK_CLOSE. ビュアーから復帰しました.");
					// ビュアーからの復帰
					Intent intent = new Intent();
					intent.putExtra("nextopen", nextopen);
					intent.putExtra("lastfile", lastfile);
					setResult(RESULT_OK, intent);
					finishActivity();
					return;
				}
				else {
					Log.d("ExpandActivity", "onActivityResult: nextopen == CloseDialog.CLICK_CLOSE");
				}
			}
		}
		else if (requestCode == DEF.REQUEST_TEXT) {
			Log.d("ExpandActivity", "onActivityResult: requestCode == DEF.REQUEST_TEXT");
			if (resultCode == RESULT_OK && data != null) {
				Log.d("ExpandActivity", "onActivityResult: resultCode == RESULT_OK. ビュアーから復帰しました.");
				mOpenOperation = data.getExtras().getInt("nextopen", -1);
				mOpenLastFile = data.getExtras().getString("lastfile");
			}
			else {
				Log.d("ExpandActivity", "onActivityResult: resultCode != RESULT_OK");
				mOpenOperation = CloseDialog.CLICK_CLOSE;
				mOpenLastFile = null;
			}
		}

		// 他画面から戻ったときは設定＆リスト更新
		loadListView();
		Log.d("ExpandActivity", "onActivityResult: 終了します");
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// スクロールイベント通知

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// 位置変更
		if (mThumbnailLoader == null) {
			return;
		}

		if (mFirstIndex != firstVisibleItem) {
			// リストボックスの位置が変わったときに通知
			mFirstIndex = firstVisibleItem;
			mLastIndex = mFirstIndex + visibleItemCount;
			mThumbnailLoader.setDispRange(mFirstIndex, mLastIndex);
		}
	}

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
			if (type != FileData.FILETYPE_TXT) {
				// イメージファイル表示
				openImageFile(name);
			}
			else {
				// TXTファイル表示
				openTextFile(name);
			}
		}
	}

	private void openImageFile(String image) {
		Intent intent = new Intent(ExpandActivity.this, ImageActivity.class);
		intent.putExtra("Server", mServer);
		intent.putExtra("Uri", mUri);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mUser);
		intent.putExtra("Pass", mPass);
		intent.putExtra("File", mFileName);
		intent.putExtra("Image", image);
		startActivityForResult(intent, DEF.REQUEST_IMAGE);
	}

	private void openTextFile(String text) {
		openTextFile(text, -1);
	}

	private void openTextFile(String text, int page) {
		Intent intent;
		intent = new Intent(ExpandActivity.this, TextActivity.class);
		intent.putExtra("Server", mServer);
		intent.putExtra("Uri", mUri);
		intent.putExtra("Path", mPath);
		intent.putExtra("User", mUser);
		intent.putExtra("Pass", mPass);
		intent.putExtra("File", mFileName);
		intent.putExtra("Text", text);
		intent.putExtra("Page", page);
		startActivityForResult(intent, DEF.REQUEST_TEXT);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		Resources res = getResources();

		// メニューをクリア
		menu.clear();

		// 更新
		menu.add(0, DEF.MENU_REFRESH, Menu.NONE, res.getString(R.string.refresh)).setIcon(R.drawable.ic_menu_refresh);
		// サムネイル切替
		menu.add(0, DEF.MENU_THUMBSWT, Menu.NONE, res.getString(R.string.thumbSwt)).setIcon(android.R.drawable.ic_menu_gallery);
		// オンラインヘルプ
		menu.add(0, DEF.MENU_ONLINE, Menu.NONE, res.getString(R.string.onlineMenu)).setIcon(android.R.drawable.ic_menu_set_as);
		return ret;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == DEF.MENU_REFRESH) {
			// 表示更新
			updateListView();
		}
		else if (id == DEF.MENU_THUMBSWT) {
			// サムネイル表示切替
			mThumbnail = mThumbnail ? false : true;
			updateListView();

			// サムネイル読み込み
			loadThumbnail();
		}
		else if (id == DEF.MENU_ONLINE) {
			// 操作方法画面に遷移
			Resources res = getResources();
			String url = res.getString(R.string.url_complist); // 操作説明
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	// 長押しイベント処理用クラス
	class MyClickAdapter implements OnItemLongClickListener {
		private int mOperate[] = { -1, -1, -1, -1 };

		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Resources res = getResources();
			ArrayList<FileData> files = mFileList;
			final FileData filedata = files.get(position);
			final int datapos = position;

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

			if (filedata == null) {
				// データがない
				return true;
			}

			CharSequence[] items;

			builder.setTitle(res.getString(R.string.opeTitle));
			String ope0 = res.getString(R.string.ope00);	// 未読設定
			String ope1 = res.getString(R.string.ope01);	// 既読設定
			String ope6 = res.getString(R.string.ope06);	// ここまで読んだ
			String ope7 = res.getString(R.string.ope100);

			int state = filedata.getState();
			int itemnum;
			if (filedata.getType() == FileData.FILETYPE_TXT) {
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
			if (filedata.getType() == FileData.FILETYPE_TXT) {
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

			builder.setItems(items, new DialogInterface.OnClickListener() {
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
							ed.commit();
							updateListView();
							break;
						}
						case OPERATE_READ: { // 既読にする
							ed = mSharedPreferences.edit();
							ed.putInt(DEF.createUrl(mUri + mPath + mFileName + filedata.getName(), mUser, mPass), -2);
							ed.commit();
							updateListView();
							break;
						}
						case OPERATE_READHERE: { // ここまで読んだ
							int state = 0;
							for (int i = 0 ; i <= datapos ; i ++) {
								if (mFileList.get(i).getType() != FileData.FILETYPE_TXT) {
									state ++;
								}
							}

							ed = mSharedPreferences.edit();
							ed.putInt(DEF.createUrl(mUri + mPath + mFileName, mUser, mPass), state);
							ed.commit();
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
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
	}

	public class FileListAdapter extends ArrayAdapter<FileData> {
		private ArrayList<FileData> items;
		private LayoutInflater inflater;

		public FileListAdapter(Context context, int textViewResourceId, ArrayList<FileData> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ビューを受け取る
			View view = convertView;
			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.listitem, null);
				// // 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);
			}

			// 表示すべきデータの取得
			FileData item = (FileData) items.get(position);
			if (item != null) {
				ListItemView itemView = (ListItemView) view.findViewById(R.id.listitem);
				int size = mFontSub;
				boolean thumbflag = false;

				if (mThumbnail) {
					if (item.getType() != FileData.FILETYPE_TXT) {
						thumbflag = true;
					}
				}

				int color;
				switch (item.getState()) {
					case -1:
						color = mBefColor;
						break;
					case -2:
						color = mAftColor;
						break;
					default:
						color = mNowColor;
						break;
				}
				itemView.setDrawInfo(color, mTlbColor, mInfColor, mFontMain, size, thumbflag, mThumbSizeW, mThumbSizeH, mItemMargin);
				itemView.setFileInfo(mThumbID, position, thumbflag, item.getName(), item.getFileInfo(), mShowExt);
				itemView.setMarker(mBakColor, mCurColor);
			}
			return view;
		}
	}

	// Bitmap読込のスレッドからの通知取得
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case DEF.HMSG_PROGRESS: {
				// 読込中の表示
				if (mReadDialog != null) {
					// ページ読み込み中
					String readmsg;
					if (msg.arg2 < mReadingMsg.length && mReadingMsg[msg.arg2] != null) {
						readmsg = mReadingMsg[msg.arg2];
					}
					else {
						readmsg = "Loading...";
					}
					if(msg.arg2 == 3) {
						mReadDialog.setMessage(msg.obj.toString());
					}
					else {
						mReadDialog.setMessage(readmsg + " (" + msg.arg1 + ")");
					}
				}
				return true;
			}
			case DEF.HMSG_ERROR: {
				// 読込中の表示
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				return true;
			}
			case DEF.HMSG_READ_END: {
				Log.d("ExpandActivity", "handleMessage: DEF.HMSG_READ_END. ImageManager の読み込みが終了しました.");
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
				if (mText != null && mText.length() > 0) {
					// TXTファイル表示
					openTextFile(mText, mPage);
					mText = null;
				}
				else if (mOpenOperation != CloseDialog.CLICK_CLOSE) {
					Log.d("ExpandActivity", "handleMessage: mOpenOperation != CloseDialog.CLICK_CLOSE");
					// 次のファイル検索
					FileData nextfile = searchNextFile(mFileList, mOpenLastFile, mOpenOperation);
					if (nextfile != null && nextfile.getName().length() > 0) {
						switch (mOpenOperation) {
							case CloseDialog.CLICK_NEXTTOP:
							{
								Editor ed = mSharedPreferences.edit();
								ed.remove(DEF.createUrl(mUri + mPath + mFileName + nextfile.getName(), mUser, mPass));
								ed.commit();
								break;
							}
							case CloseDialog.CLICK_PREVLAST:
							{
								Editor ed = mSharedPreferences.edit();
								ed.putInt(DEF.createUrl(mUri + mPath + mFileName + nextfile.getName(), mUser, mPass), -2);
								ed.commit();
								updateListView();
								break;
							}
						}
						if (nextfile != null) {
							// 次のファイルがあれば開く
							Toast.makeText(this, nextfile.getName(), Toast.LENGTH_SHORT).show();
							openTextFile(nextfile.getName());
						}
					}
				}
				else {
					Log.d("ExpandActivity", "handleMessage: mOpenOperation == CloseDialog.CLICK_CLOSE");
					// サムネイル読み込み
					loadThumbnail();
					mThumbnailLoader.setDispRange(mFirstIndex, mLastIndex);
				}
				break;
			}
			case DEF.HMSG_THUMBNAIL: {
				// Bitmapの通知
				String name = (String) msg.obj;
				int bmIndex = msg.arg1;
				if (name != null && mFileList != null) {
					ArrayList<FileData> files = mFileList;

					for (int i = 0; i < files.size(); i++) {
						if (name.equals(files.get(i).getName())) {
							// リストの更新
							mFileListAdapter.notifyDataSetChanged();
							break;
						}
					}
				}
				break;
			}
		}
		return false;
	}

	private void loadListView() {
		Log.d("ExpandActivity", "loadListView 開始します.");
		Resources res = getResources();
		mReadingMsg = new String[4];
		mReadingMsg[0] = res.getString(R.string.reading);
		mReadingMsg[1] = res.getString(R.string.readxref);
		mReadingMsg[2] = res.getString(R.string.readpage);
		mReadingMsg[3] = res.getString(R.string.download);

		// プログレスダイアログ準備
		mReadDialog = new ProgressDialog(this);
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
		Log.d("ExpandActivity", "loadListView 終了します.");
	}

	private void loadListViewAfter() {
		// しおり情報取得
		mCurrentPage = mSharedPreferences.getInt(DEF.createUrl(mUri + mPath + mFileName, mUser, mPass), 0);

		// ファイルリスト
		FileListItem files[] = mImageMgr.getList();
		int filenum = 0;
		if (files != null) {
			filenum = files.length;
		}
		mFileList = new ArrayList<FileData>(filenum);

		int imageCnt = 0;
		for (int i = 0; i < filenum; i++) {
			FileData data = new FileData();
			int state = -1;
			if (files[i].type == FileData.FILETYPE_TXT) {
				state = mSharedPreferences.getInt(DEF.createUrl(mUri + mPath + mFileName + files[i].name, mUser, mPass), -1);
			}
			else {
				if (mCurrentPage == -2) {
					state = -2;
				}
				else if (mCurrentPage >= 0) {
					if (imageCnt < mCurrentPage) {
						state = -2;
					}
				}
			}

			data.setName(files[i].name);
			data.setSize(files[i].orglen);
			data.setType(files[i].type);
			data.setDate(files[i].dtime);
			data.setState(state);
			mFileList.add(data);

			if (files[i].type != FileData.FILETYPE_TXT) {
				imageCnt ++;
			}
		}
		mFileListAdapter = new FileListAdapter(this, R.layout.listitem, mFileList);
		setListAdapter(mFileListAdapter);
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
		mCurrentPage = mSharedPreferences.getInt(DEF.createUrl(mUri + mPath + mFileName, mUser, mPass), 0);

		// ファイルリスト
		int imageCnt = 0;
		ArrayList<FileData> files = mFileList;
		for (int i = 0; i < files.size() ; i++) {
			FileData data = files.get(i);

			int state = -1;
			if (data.getType() == FileData.FILETYPE_TXT) {
				state = mSharedPreferences.getInt(DEF.createUrl(mUri + mPath + mFileName + data.getName(), mUser, mPass), -1);
			}
			else {
				if (mCurrentPage == -2) {
					state = -2;
				}
				else if (mCurrentPage >= 0) {
					if (imageCnt < mCurrentPage) {
						state = -2;
					}
				}
			}

			data.setState(state);

			if (data.getType() != FileData.FILETYPE_TXT) {
				imageCnt ++;
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

	// サムネイル読み込み
	private void loadThumbnail() {
		Log.d("ExpandActivity", "loadThumbnail: 開始します.");
		if (mThumbnail == false) {
			return;
		}

		if (mThumbnailLoader != null) {
			// 今動いてるのは止める
			mThumbnailLoader.breakThread();
		}
		mImageMgr.unsetBreakTrigger();
		mThumbID = System.currentTimeMillis();

		mThumbnailLoader = new ExpandThumbnailLoader(mUri, mPath + mFileName, mHandler, mThumbID, mImageMgr, mFileList, mThumbSizeW, mThumbSizeH, mThumbNum, mThumbCrop, mThumbMargin);
	}

	// 解放
	private void releaseThumbnail() {
		// サムネイル読み込みスレッドを停止
		if (mThumbnailLoader != null) {
			mThumbnailLoader.breakThread();
			mThumbnailLoader.releaseThumbnail();
			mThumbnailLoader = null;
		}

		// イメージ管理解放
		if (mImageMgr != null) {
			mImageMgr.closeFiles();
			mImageMgr = null;
		}
	}

	public static FileData searchNextFile(ArrayList<FileData> files, String file, int nextopen) {
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
		FileData fd = new FileData();
		fd.setName(file);
		int index = sortfiles.indexOf(fd);
		if (index >= 0) {
			// 見つかった場合
			switch (nextopen) {
				case CloseDialog.CLICK_NEXT:
				case CloseDialog.CLICK_NEXTTOP:
					// 次のファイル
					index ++;
					break;
				case CloseDialog.CLICK_PREV:
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
			return DEF.compareFileName(file1.getName().toLowerCase(), file2.getName().toLowerCase());
		}
	}

	private void finishActivity() {
		// サムネイルスレッド終了
		releaseThumbnail();

		if (mImageMgr != null) {
			try {
				mImageMgr.close();
			}
			catch (IOException e) {
				//
				String s = "";
				if (e != null && e.getMessage() != null) {
					s = e.getMessage();
				}
				Log.e("releaseThumbnail", s);
			}
			mImageMgr = null;
		}
		finish();
	}
}
