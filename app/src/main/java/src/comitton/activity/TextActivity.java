package src.comitton.activity;

import java.util.ArrayList;
import java.util.Date;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetConfigActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.config.SetImageText;
import src.comitton.config.SetImageTextColorActivity;
import src.comitton.config.SetImageTextDetailActivity;
import src.comitton.config.SetNoiseActivity;
import src.comitton.config.SetTextActivity;
import src.comitton.data.FileData;
import src.comitton.data.RecordItem;
import src.comitton.data.TextDrawData;
import src.comitton.dialog.BookmarkDialog;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.Information;
import src.comitton.dialog.InputDialog;
import src.comitton.dialog.InputDialog.SearchListener;
import src.comitton.dialog.ListDialog;
import src.comitton.dialog.MenuDialog;
import src.comitton.dialog.PageSelectDialog;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;
import src.comitton.dialog.CloseDialog.CloseListenerInterface;
import src.comitton.dialog.ListDialog.ListSelectListener;
import src.comitton.dialog.MenuDialog.MenuSelectListener;
import src.comitton.dialog.TextConfigDialog;
import src.comitton.dialog.TextConfigDialog.TextConfigListenerInterface;
import src.comitton.filelist.RecordList;
import src.comitton.listener.PageSelectListener;
import src.comitton.noise.NoiseSwitch;
import src.comitton.data.PictureData;
import src.comitton.stream.ImageManager;
import src.comitton.stream.TextManager;
import src.comitton.stream.TextManager.MidashiData;
import src.comitton.view.GuideView;
import src.comitton.view.MyTextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 画像のスクロールを試すための画面を表します。
 */
@SuppressLint("NewApi")
public class TextActivity extends Activity implements OnTouchListener, Handler.Callback, MenuSelectListener, PageSelectListener,  BookmarkListenerInterface {
	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;
	//
	private static final int TIME_VIB_TERM = 20;
	private static final int TIME_VIB_RANGE = 30;

	private static final int CTL_COUNT[] = { 1, 1, 2, 99999 }; // 対象のページ数
	private static final int CTL_RANGE[] = { 2, 4, 3, 1 }; // 1ページ選択に必要な移動幅(単位)

	private static final int NOISE_NEXTPAGE = 1;
	private static final int NOISE_PREVPAGE = 2;
	private static final int NOISE_NEXTSCRL = 3;
	private static final int NOISE_PREVSCRL = 4;

	private final int VOLKEY_NONE = 0;
	private final int VOLKEY_DOWNTONEXT = 1;
//	private final int VOLKEY_UPTONEXT   = 2;

	public static final int MSG_LOAD_END = 1;
	public static final int MSG_READ_END = 2;
	public static final int MSG_ERROR = 4;
	public static final int MSG_CACHE = 5;
	public static final int MSG_LOADING = 6;
	public static final int MSG_NOISE = 7;
	public static final int MSG_NOISESTATE = 8;

	private final int EVENT_TOUCH_TOP = 206;
	private final int EVENT_TOUCH_BOTTOM = 207;

	// 上下の操作領域タッチ後何msでボタンを表示するか
	private static final int LONGTAP_TIMER_UI = 400;

	private final int EVENT_READTIMER = 200;
//	private final int EVENT_NOISE  = 203;

	private static final int PAGE_SLIDE = 0;
	private static final int PAGE_INPUT = 1;

	private int RANGE_FLICK;

	private final int TOUCH_NONE      = 0;
	private final int TOUCH_COMMAND   = 1;
	private final int TOUCH_OPERATION = 2;

	private final int COMMAND_RES[] =
	{
		R.string.rotateMenu,		// 画面方向
		R.string.tguide02,			// 見開き設定
		R.string.tguide03,			// 画像サイズ
		R.string.selChapterMenu,	// 見出し選択
	};
	private String mCommandStr[];

	private int mPaperSel;
	private int mTextWidth;
	private int mTextHeight;

	private int mHeadSizeOrg;
	private int mBodySizeOrg;
	private int mRubiSizeOrg;
	private int mInfoSizeOrg;
	private int mMarginWOrg;
	private int mMarginHOrg;

	private int mHeadSize;
	private int mBodySize;
	private int mRubiSize;
	private int mInfoSize;
	private int mPicSize;
	private int mSpaceW;
	private int mSpaceH;
	private int mMarginW;
	private int mMarginH;

	private int mAscMode;	// 半角の表示方法

	private int mTextColor;
	private int mBackColor;
	private int mGradColor;
	private int mGradation;
	private int mSrchColor;

	// 設定値の保持
	private int mClickArea = 16;
	private int mPageRange = 16;
	private int mScroll = 5;
	private int mMoveRange = 12;
	private int mCenter = 0;
	private int mShadow = 0;
	private int mViewPoint;
	private int mMargin;
	private int mMgnColor;
	private int mCenColor;
	private int mTopColor1;
	private int mTopColor2;
	private int mVolKeyMode = VOLKEY_DOWNTONEXT;
	private int mViewRota;
	private int mRotateBtn;
	private int mVolScrl;
	private int mScrlRngW;
	private int mScrlRngH;
	private int mLastMsg;
	private int mPageSelect;
	private int mEffectTime;
	private int mMomentMode;
	private int mBkLight;
	private boolean mOldMenu;

	private boolean mIsConfSave;

	private int mNoiseScrl;
	private int mNoiseUnder;
	private int mNoiseOver;
	private int mNoiseDec;
	private boolean mNoiseLevel;

	private boolean mNotice;
	private boolean mNoSleep;
	private boolean mChgPage;
	private boolean mChgPageKey;
	private boolean mChgFlick;
	private boolean mConfirmBack;
	private boolean mCMargin;
	private boolean mCShadow;
	private boolean mPrevRev;
	private boolean mVibFlag;
	private boolean mPseLand;
	private boolean mEffect;
	private boolean mTapScrl;
	private boolean mFlickPage;
	private boolean mFlickEdge;
	private boolean mImmEnable;
	private boolean mBottomFile;
	private boolean mPinchEnable;

	private String mFontFile;
	private String mCharset;

	// ファイル情報
	private int mServer;
	private String mPath;
	private String mLocalPath;
	private String mUser;
	private String mPass;
	private String mFileName;
	private String mTextName;
	private String mFilePath;
	private int mPage;

	private ImageManager mImageMgr;
	private TextManager mTextMgr;

	// ページ表示のステータス情報
	private int mRestorePage;
	private int mCurrentPage;
	private int mSelectPage = 0;
//	private boolean mPageBack = false;
	private int mInitFlg = 0; // 初期表示の制御用フラグ

	// 画像の表示制御情報
	private int mScaleMode;
	private int mDispMode;

	// 画面を構成する View の保持
	private MyTextView mTextView = null;
	private GuideView mGuideView = null;

	// 画面タッチの制御
	private float mTouchBeginX; // 開始x座標
	private float mTouchBeginY; // 開始y座標
	private int mTouchDrawLeft;
	private int mOperation; 	// 操作種別
	private boolean mTouchFirst = false; // タッチ開始後リミットを超えて移動していない
	private boolean mPageMode = false; // ページ選択モード
	private boolean mPageModeIn = false; // ページ選択中の操作エリア外フラグ
	private boolean mPinchOn = false;
	private boolean mPinchDown = false;
	private int mPinchScale = 100;
	private int mPinchScaleSel;
	private int mPinchCount;
	private long mPinchTime;
	private int mPinchRange;

	// private boolean mLongTouchMode = false;
	private Message mLongTouchMsg = null;

	private long mPrevVibTime = 0;
//	private long mPrevScrollTime = 0;

	// ビットマップ読み込みスレッドの制御用
	private Handler mHandler;

	private Activity mActivity;
	SharedPreferences mSharedPreferences;
	private float mDensity;
	private int mImmCancelRange;
	private boolean mImmCancel;

	private TextLoad mTextLoad;
	private Thread mTextThread;

	private Vibrator mVibrator;

	private boolean mTerminate;
	private boolean mReadRunning;
	private boolean mReadBreak;
	private boolean mHistorySaved;

	private ProgressDialog mReadDialog;
	private String mParsingMsg;
	private String mFormattingMsg;
	private Message mReadTimerMsg;

	private NoiseSwitch mNoiseSwitch = null;
	private int mNoiseScroll = 0;

	private int mTapPattern;
	private int mTapRate;

	private final int MAX_TOUCHPOINT = 6;
	private final int TERM_MOMENT = 200;
	private int mTouchPointNum;
	private PointF mTouchPoint[];
	private long mTouchPointTime[];
	private boolean mTouchThrough;

	private TextConfigDialog mTextConfigDialog;
	private CloseDialog mCloseDialog;
	private ListDialog mListDialog;
	private InputDialog mInputDialog;
	private MenuDialog mMenuDialog;

	private String mSearchText;

	private boolean mTimeDisp;
	private int mTimeFormat;
	private int mTimePos;
	private int mTimeSize;
	private int mTimeColor;

	/**
	 * 画面が作成された時に発生します。
	 *
	 * @param savedInstanceState
	 *            保存されたインスタンスの状態。
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// 回転
		mInitFlg = 0;
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mScaleMode = DEF.SCALE_ORIGINAL;
		mTerminate = false;
		mReadRunning = false;
		mHandler = new Handler(this);
		mActivity = this;
		mDensity = getResources().getDisplayMetrics().scaledDensity;
		mImmCancelRange = (int)(getResources().getDisplayMetrics().density * 6);
		mIsConfSave = true;

		// 慣性スクロール用領域初期化
		mTouchPointNum = 0;
		mTouchPoint = new PointF[MAX_TOUCHPOINT];
		mTouchPointTime = new long[MAX_TOUCHPOINT];
		for (int i = 0 ; i < MAX_TOUCHPOINT ; i ++) {
			mTouchPoint[i] = new PointF();
		}

		RANGE_FLICK = (int)(50 * mDensity);

		super.onCreate(savedInstanceState);

		// JCIFSのログを出力しない
//		jcifs.Config.setProperty("jcifs.util.loglevel", "0");

		// タイトル非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 設定の読み込み
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		ReadSetting(mSharedPreferences);
		if (mNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (mImmEnable && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}

		if (mNoSleep) {
	        // スリープしない
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

//        this.setContentView(R.layout.text);
		mTextView = new MyTextView(this);
		mGuideView = new GuideView(this);
		FrameLayout layout = new FrameLayout(this);
		layout.addView(mTextView);
//		layout.addView(mGuideView);
		setContentView(layout);

		if (mGuideView != null) {
			mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
		}

		Resources res = getResources();
		mParsingMsg = res.getString(R.string.parsing);
		mFormattingMsg = res.getString(R.string.formatting);

		mCommandStr = new String[COMMAND_RES.length];
		for (int i = 0 ; i < mCommandStr.length ; i ++) {
			mCommandStr[i] = res.getString(COMMAND_RES[i]);
		}

		// Viewの取得
//		this.mTextView = (MyTextView) this.findViewById(R.id.text_view);
//		this.mGuideView = (GuideView) this.findViewById(R.id.guide_view);

		mTextView.setDispMode(mDispMode);
		mGuideView.setGuideMode(mDispMode == DEF.DISPMODE_TX_DUAL, mBottomFile, true, mPageSelect, false);
		setConfig();
		mTextView.setColor(mTextColor, mBackColor, mGradColor, mGradation, mSrchColor);
		mTextView.setTextScale(mScaleMode, mPinchScale);
		mTextView.setGuideView(mGuideView);
		mTextView.setMarker(null);

		// 色とサイズを指定
		mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
		mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
//		mGuideView.setRotateMode(mPseLand);

		// 上部メニューの文字列情報をガイドに設定
		mGuideView.setTopCommandStr(mCommandStr);

		// Intentを取得する
		Intent intent = getIntent();
		mServer = -1;	// デフォルトはローカル
		try {
			String path = null;
			if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
				Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
				path = uri.getPath();
			}
			else if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
				path = Uri.decode(intent.getDataString());
			}
			// 先頭"file://"を削除
			if (path != null) {
				if (path.length() > 7 && path.substring(0, 7).equals("file://")) {
					path = path.substring(7);
				}
			}

			// ファイルが指定されている
			if (path != null && path.length() >= 5) {
				// ファイル名の切り出し
				int pos = 0, prev = 0;
				while (true) {
					// 次のディレクトリの区切り
					pos = path.indexOf("/", prev + 1);
					if (pos == -1) {
						// 最後まで移動
						break;
					}
					prev = pos;
				}
				// パスの構成チェック
				if (path.length() > prev + 1) {
					mPath = path.substring(0, prev + 1);
					String ext = path.substring(path.length() - 4).toLowerCase();
					if (FileData.isText(ext)) {
						// 圧縮ファイル
						mFileName = "";
						mTextName = path.substring(prev + 1);
					}
					else {
						mPath = null;
					}
				}
				else {
					mPath = null;
				}
			}
		}
		catch (Exception e) {
			;
		}

		String uri = "";
		if (mPath == null) {
			// Intentに保存されたデータを取り出す
			mServer = intent.getIntExtra("Server", -1);
			uri = intent.getStringExtra("Uri");
			mPath = intent.getStringExtra("Path");
			mUser = intent.getStringExtra("User");
			mPass = intent.getStringExtra("Pass");
			mFileName = intent.getStringExtra("File");
			mTextName = intent.getStringExtra("Text");
			mPage = intent.getIntExtra("Page", -1);
		}
		else {
			mPage = -1;
		}
		if (mPath == null) {
			// パスの設定がなければ終了
			return;
		}

		// 最後に開いたファイル情報を保存
		mLocalPath = mPath;

		mPath = uri + mPath;
		if (mPath != null && mFileName != null) {
			mFilePath = mPath + mFileName;
		}

		// 続きから開く設定を記録
		saveLastFile();

		mRestorePage = mSharedPreferences.getInt(DEF.createUrl(mFilePath + mTextName, mUser, mPass), -1);
		mCurrentPage = (mPage != -1) ? mPage : (mRestorePage != -1 ? mRestorePage : 0);
		mTextView.setOnTouchListener(this);

		// プログレスダイアログ準備
		mReadBreak = false;
		mReadDialog = new ProgressDialog(this);
		mReadDialog.setMessage(mParsingMsg + " (0)");
		mReadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mReadDialog.setCancelable(true);
		mReadDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				if (mImmEnable && mSdkVersion >= 19) {
					int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
					uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
					uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
					getWindow().getDecorView().setSystemUiVisibility(uiOptions);
				}
			}
		});
		mReadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// Thread を停止
				if (mTextMgr != null) {
					mTextMgr.setBreakTrigger();
				}
				mTerminate = true;
				mReadBreak = true;

			}
		});
		return;
	}

	/**
	 * @Override アクティビティ一時停止時に呼び出される
	 */
	protected void onPause() {
		super.onPause();
		if (mTextView != null) {
			mTextView.breakMessage(true);
		}
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
	}

	/**
	 * @Override アクティビティ再開時に呼び出される
	 */
	public void onResume() {
		super.onResume();
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(false);
		}
//		if (mTextView != null) {
//			mTextView.breakMessage(false);
//			mTextView.update(true);
//		}
	}

	/**
	 * @Override アクティビティ停止時に呼び出される
	 */
	protected void onStop() {
		super.onStop();

		// 履歴保存
		if (mHistorySaved == false) {
			saveHistory(true);
		}

		// マイク停止
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
	}

	/**
	 * @Override アクティビティ再開時に呼び出される
	 */
	public void onRestart(){
		super.onRestart();
		// IMM
		if (mImmEnable && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}
	}

	public class TextLoad implements Runnable {
		private Handler handler;
		private Activity mActivity;

		public TextLoad(Handler handler, TextActivity activity) {
			super();
			this.handler = handler;
			this.mActivity = activity;

		}

		public void run() {
			// ファイルリストの読み込み
			mImageMgr = new ImageManager(this.mActivity, mPath, mFileName, mUser, mPass, ImageManager.FILESORT_NAME_UP, handler, mCharset, true, ImageManager.OPENMODE_TEXTVIEW, 1);
			mImageMgr.LoadImageList(0, 0, 0);
			mTextMgr = new TextManager(mImageMgr, mTextName, mUser, mPass, handler, mActivity);
			mTextMgr.LoadTextFile();
			mTextMgr.formatTextFile(mTextWidth, mTextHeight, mHeadSize, mBodySize, mRubiSize, mSpaceW, mSpaceH, mMarginW, mMarginH, mPicSize, mFontFile, mAscMode);

			String imagePath;
			if (mImageMgr == null) {
				// ディスクから読み込み
				imagePath = mPath;
			}
			else {
				// 圧縮ファイルから読み込み
				imagePath = DEF.getDir(mTextName);
			}
			mTextView.setPicturePath(imagePath, mImageMgr, mUser, mPass);

			// 終了通知
			Message message = new Message();
			message.what = MSG_READ_END;
			handler.sendMessage(message);
		}
	}

	// 終了処理
	protected void onDestroy() {
		super.onDestroy();

		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordStop();
			mNoiseSwitch = null;
		}

		if (mTextView != null) {
			mTextView.setTextBuffer(null, null, null);
			if (mTextMgr != null) {
				mTextMgr.release();
			}
			mTextView.setPictures(null);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		/*
		 * if (hasFocus) { if (mInitFlg == 0) { // 起動直後のみ呼び出し mInitFlg = 1;
		 *
		 * // ビットマップの設定 mPageBack = false; setTextPageData(); } }
		 */

		// サイズ取得
		if (mPaperSel == DEF.PAPERSEL_SCREEN) {
			int cx, cy;
//			if (mSdkVersion >= 19 && mImmEnable) {
//				// ウィンドウマネージャのインスタンス取得
//				WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
//				// ディスプレイのインスタンス生成
//				Display disp = wm.getDefaultDisplay();
//				// ナビゲーション以外
//				Point dispSize = new Point();
//				disp.getSize(dispSize);
//				// ハードウェアサイズ
//				Point hardSize = new Point();
//				disp.getRealSize(hardSize);
//				//
//				cx = mTextView.getWidth();
//				cy = hardSize.y - dispSize.y + mTextView.getHeight();
//				Log.d("screen size", "hard=(" + hardSize.x + "," + hardSize.y + ") disp(" + dispSize.x + "," + dispSize.y + ") view(" + mTextView.getWidth() + "," + mTextView.getHeight() + ")");
//			}
//			else {
				cx = mTextView.getWidth();
				cy = mTextView.getHeight();
//			}
			if (cx < cy) {
				mTextWidth = cx;
				mTextHeight = cy;
			}
			else {
				mTextWidth = cy;
				mTextHeight = cx;
			}
		}

		// プログレスダイアログの設定
		if (mInitFlg == 0) {
			mInitFlg = 1;
			startDialogTimer(100);

			mReadRunning = true;
			mTextLoad = new TextLoad(mHandler, this);
			mTextThread = new Thread(mTextLoad);
			mTextThread.start();
		}

		if (hasFocus) {
			if (mImmEnable && mSdkVersion >= 19) {
				int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				getWindow().getDecorView().setSystemUiVisibility(uiOptions);
			}
		}
	}

	/**
	 * 画面の設定が変更された時に発生します。
	 *
	 * @param newConfig
	 *            新しい設定。
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			int code = event.getKeyCode();
			switch (code) {
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				{
					if (mChgPageKey) {
						// 前ページへ
						prevPage();
					} else {
						// 次ページへ
						nextPage();
					}
					break;
				}
				case KeyEvent.KEYCODE_DPAD_LEFT:
				{
					if (mChgPageKey) {
						// 次ページへ
						nextPage();
					} else {
						// 前ページへ
						prevPage();
					}
				}
				case KeyEvent.KEYCODE_MENU:
					// 独自メニュー表示
					openMenu();
					return true;
				case KeyEvent.KEYCODE_DEL:
				case KeyEvent.KEYCODE_BACK:
					operationBack();
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
				case KeyEvent.KEYCODE_VOLUME_UP:
				{
					// ボリュームモード
					if (mVolKeyMode == VOLKEY_NONE) {
						// Volキーを使用しない
						break;
					}

					int move = mVolKeyMode == VOLKEY_DOWNTONEXT ? 1 : -1;
					if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
						move *= -1;
					}
					// 読込中の表示
					startScroll(move);
					return true;
				}
				case KeyEvent.KEYCODE_SPACE:
				{
					int meta = event.getMetaState();
					int move = (meta & KeyEvent.META_SHIFT_ON) == 0 ? 1 : -1;
					// 読込中の表示
					startScroll(move);
					return true;
				}
				case KeyEvent.KEYCODE_CAMERA:
				case KeyEvent.KEYCODE_FOCUS:
					if (mRotateBtn == 0) {
						break;
					}
					else if (event.getKeyCode() != mRotateBtn) {
						return true;
					}
					if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota == DEF.ROTATE_LANDSCAPE) {
						int rotate;
						if (getRequestedOrientation() == DEF.ROTATE_PORTRAIT) {
							// 横にする
							rotate = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
						}
						else {
							// 縦にする
							rotate = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
						}
						setRequestedOrientation(rotate);
					}
					break;
				default:
					break;
			}
		}
		else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_BACK:
					break;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
				case KeyEvent.KEYCODE_VOLUME_UP:
					// ボリュームモード
					if (mVolKeyMode == VOLKEY_NONE) {
						// Volキーを使用しない
						break;
					}
					return true;
				default:
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	// 長押しタイマー開始
	public boolean startLongTouchTimer(int longtouch_event) {
		if (mImmEnable == false) {
			return false;
		}

		mLongTouchMsg = mHandler.obtainMessage(longtouch_event);
		long NextTime = SystemClock.uptimeMillis() + LONGTAP_TIMER_UI;

		mHandler.sendMessageAtTime(mLongTouchMsg, NextTime);
		return (true);
	}

	// 起動時のプログレスダイアログ表示
	public boolean startDialogTimer(int time) {
		mReadTimerMsg = mHandler.obtainMessage(EVENT_READTIMER);
		long NextTime = SystemClock.uptimeMillis() + time;

		mHandler.sendMessageAtTime(mReadTimerMsg, NextTime);
		return (true);
	}

	// Bitmap読込のスレッドからの通知取得
	public boolean handleMessage(Message msg) {
		if (mReadTimerMsg == msg) {
			// プログレスダイアログを表示
			synchronized (this) {
				if (mReadDialog != null) {
					if (mImmEnable && mSdkVersion >= 19) {
						mReadDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
						mReadDialog.show();
						mReadDialog.getWindow().getDecorView().setSystemUiVisibility(this.getWindow().getDecorView().getSystemUiVisibility());
						mReadDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
					}
					else {
						mReadDialog.show();
					}
				}
			}
			return true;
		}

		switch (msg.what) {
			case EVENT_TOUCH_TOP:
			case EVENT_TOUCH_BOTTOM:
				if (mLongTouchMsg == msg) {
					// 最新のタイマーの時だけ処理
					if (mTouchFirst) {
						// 上部の操作エリア
						mGuideView.eventTouchTimer();
					}
				}
				return true;
			case DEF.HMSG_TX_PARSE:
			case DEF.HMSG_TX_LAYOUT:
				// 読込中の表示
				synchronized (this) {
    				if (mReadDialog != null) {
    					// ページ読み込み中
    					String str;
    					if (msg.what == DEF.HMSG_TX_LAYOUT) {
    						str = mFormattingMsg;
    					}
    					else {
    						str = mParsingMsg;
    					}
    					mReadDialog.setMessage(str + " (" + msg.arg1 + "%)");
    				}
				}
				return true;
			case MSG_ERROR:
				// 読込中の表示
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				return true;

			case MSG_NOISESTATE:
				// 状態表示
				if (mNoiseSwitch != null) {
					mGuideView.setNoiseState(msg.arg1, mNoiseLevel ? msg.arg2 : -1);
				}
				return true;

			case MSG_NOISE:
				// 読込中の表示
				if (msg.arg1 == NOISE_NEXTPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
//					else
					if (!mTextView.setViewPosScroll(1)) {
						// スクロールする余地がなければ次ページ
						nextPage();
					}
					else {
						// スクロール開始
						mTextView.startScroll();
					}
				}
				else if (msg.arg1 == NOISE_PREVPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
//					else
					if (!mTextView.setViewPosScroll(-1)) {
						// スクロールする余地がなければ前ページ
						prevPage();
					}
					else {
						// スクロール開始
						mTextView.startScroll();
					}
				}
				else if (msg.arg1 == NOISE_NEXTSCRL || msg.arg1 == NOISE_PREVSCRL) {
					int way = 1;
					if (msg.arg1 == NOISE_PREVSCRL) {
						way = -1;
					}

					// 読込中の表示
					if (mTextView.checkScrollPoint() && (mNoiseScroll != 0 && way == mNoiseScroll)) {
//						long nowTime = System.currentTimeMillis();
//						if (mPrevScrollTime + 50 < nowTime || mPrevScrollTime > nowTime || mPrevScrollTime == 0) {
						mTextView.moveToNextPoint(mNoiseScrl);
//							mPrevScrollTime = nowTime;
//						}
					}
					else {
						mNoiseScroll = way;
						// 次のポイントへスクロール開始
						if (mTextView.setViewPosScroll(mNoiseScroll)) {
							// スクロール開始
//							startViewTimer(EVENT_NOISE);
						}
					}
//					mTextView.scrollStart(100, 100);
//					mTextView.scrollMove(140, 60, mScroll);
//					mTextView.scrollStart(100, 100);
//					mTextView.scrollMove(60, 140, mScroll);
				}
				return true;

			case MSG_READ_END:
				// 読込中の表示
				synchronized (this) {
    				if (mReadDialog != null) {
    					try {
    						mReadDialog.dismiss();
    					}
    					catch (Exception e){
    						;
    					}
    					mReadDialog = null;
    				}
				}
				mReadRunning = false;
				if (mTerminate) {
					finish();
				}

				// 既読の場合は最終ページ
				if (mTextMgr.length() == 0) {
					mCurrentPage = 0;
				}
				else if (mCurrentPage < 0) {
					mCurrentPage = mTextMgr.length() - 1;
				}
				else if (mCurrentPage >= mTextMgr.length()) {
					mCurrentPage = mTextMgr.length() - 1;
				}

				// テキストの設定
				char textbuff[] = mTextMgr.getTextBuffer();
				String title = mTextMgr.getTitle();
				// テキストデータ
				TextDrawData page[][] = mTextMgr.getTextData();

				mTextView.setTextBuffer(textbuff, title, page);

				// 挿絵配列取得
				PictureData pictures[] = mTextMgr.getPictures();
				mTextView.setPictures(pictures);

				// 画面サイズが必要なのでここでセット
				mTextView.lockDraw();
				mTextView.setTextConfig(mTextWidth, mTextHeight, mInfoSize, mMarginH);
				setTextPageData();
				mTextView.setPage(mCurrentPage, false, true);
				mTextView.update(true);
				break;
		}
		return false;
	}

	public void setTextPageData() {
		if (mTextMgr == null || mTextMgr.length() - 1 < mCurrentPage) {
			return;
		}

		mPinchScale = mTextView.TextScaling();
		this.updateOverSize();
		return;
	}

	/**
	 * View がクリックされた時に発生します。
	 */
	public void ChangeScale(int mode) {
		mScaleMode = mode;
		mTextView.setTextScale(mScaleMode, mPinchScale);
		// イメージ拡大縮小
		mPinchScale = mTextView.TextScaling();

		this.updateOverSize();

		// 第3引数は、表示期間（LENGTH_SHORT、または、LENGTH_LONG）
		// Toast.makeText(this, strScaleMode, Toast.LENGTH_SHORT).show();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); // ←このthisは普通Activityとかね
		Editor ed = sp.edit();
		ed.putInt("scalemode", mScaleMode);
		ed.commit();
	}

	/**
	 * View がタッチされた時に発生します。
	 *
	 * @param v
	 *            タッチされた View。
	 * @param event
	 *            イベント データ。
	 *
	 * @return タッチ操作を他の View へ伝搬しないなら true。する場合は false。
	 */
	public boolean onTouch(View v, MotionEvent event) {
		if (mReadRunning) {
			// ファイル一覧の読み込み中はページ操作しない
			return true;
		}
		if (mTextMgr == null || mTextMgr.length() == 0) {
			setResult(RESULT_OK);
			finish();
			return true;
		}

		float x;
		float y;
		int cx;
		int cy;
		if (mPseLand == false) {
			x = event.getX();
			y = event.getY();
			cx = mTextView.getWidth();
			cy = mTextView.getHeight();
		}
		else {
			// 疑似横モード
			cx = mTextView.getHeight();
			cy = mTextView.getWidth();
			y = cy - event.getX();
			x = event.getY();
		}

		int action = event.getAction();

		// ピンチイン・アウト対応
		if (mPinchEnable) {
    		int action2 = action & MotionEvent.ACTION_MASK;
    		// ズーム中ではない && ページ表示ではない && ガイド表示ではない
    		if (action2 == MotionEvent.ACTION_POINTER_1_DOWN) {
    			mPinchDown = true;
    			if (mPinchOn) {
    				// 記録
    				if (mPinchCount == 0) {
    					mPinchTime = SystemClock.uptimeMillis();
    				}
    				mPinchCount ++;
    			}
    		}
    		else if (action2 == MotionEvent.ACTION_POINTER_1_UP) {
    			mPinchDown = false;
    			if (mPinchOn) {
    				// 押されてからの時間を判定
    				long nowtime = SystemClock.uptimeMillis();
    				if (nowtime - mPinchTime <= 1000) {
    					// 1000ミリ秒以内
    					if (mPinchCount == 2) {
    						// 100%にする
    						// 任意スケーリング変更中
    						mPinchOn = true;
    						mPinchScaleSel = 100;
    					}
    				}
    				else {
    					mPinchCount = 0;
    				}

    				if (mPinchCount == 2) {
    					mPinchCount = 0;
    				}
    			}
    		}
    		if (!mPinchOn && !mPageMode && mOperation != TOUCH_COMMAND && mPinchDown) {
    			if (action2 == MotionEvent.ACTION_MOVE) {
    				int count = event.getPointerCount();
    				if (count >= 2) {
    					float x1 = (int)event.getX(0);
    					float y1 = (int)event.getY(0);
    					float x2 = (int)event.getX(1);
    					float y2 = (int)event.getY(1);
    					if (Math.abs(x1 - x2) > mDensity * 20 || Math.abs(y1 - y2) > mDensity * 20) {
    						// 2点間が10sp以上であれば拡大縮小開始
    						mPinchOn = true;
    						mPinchScaleSel = mPinchScale;
    						mTouchFirst = false;
    					}
    				}
    			}
    		}
    		if (mPinchOn) {
    			// サイズ変更中
    			if (action2 == MotionEvent.ACTION_POINTER_1_DOWN || action2 == MotionEvent.ACTION_MOVE) {
    				// サイズ変更
    				int count = event.getPointerCount();
    				float x1 = 0;
    				float y1 = 0;
    				for (int i = 0; i < count; i++) {
    					x = (int) event.getX(i);
    					y = (int) event.getY(i);

    					if (i == 0) {
    						x1 = x;
    						y1 = y;

    					}
    					else if (i == 1) { // if (mPinchId == (int) event.getPointerId(i)) {
    						// 距離を求める
    						int range;
    						range = (int) Math.sqrt(Math.pow(Math.abs(x1 - x), 2) + Math.pow(Math.abs(y1 - y), 2));
    						if (mPinchDown) {
    							mPinchRange = range;
    							mPinchDown = false;
    						}
    						else {
    							// 初回は記録のみ
    							int range2 = (int)((range - mPinchRange) / (8 * mDensity));
    							int zoom = range2;
    							if (Math.abs(zoom) >= 6) {
    								zoom *= 8;
    							}
    							else if (Math.abs(zoom) >= 4) {
    								zoom *= 4;
    							}
    							else if (Math.abs(zoom) >= 2) {
    								zoom *= 2;
    							}
    							mPinchScaleSel += zoom;
    							if (mPinchScaleSel < 10) {
    								mPinchScaleSel = 10;
    							}
    							else if (mPinchScaleSel > 250) {
    								mPinchScaleSel = 250;
    							}
    							mPinchRange += range2 * (8 * mDensity);
    						}
    						mGuideView.setGuideText(mPinchScaleSel + "%");
    					}
    				}
    			}
    			else if (action2 == MotionEvent.ACTION_UP) {
    				// サイズ変更終了
    				mGuideView.setGuideText(null);
    				mPinchOn = false;
    				if (mPinchScale != mPinchScaleSel) {
    					mPinchScale = mPinchScaleSel;
    					// テキストモード
						mTextView.lockDraw();
    					mTextView.setTextScale(DEF.SCALE_PINCH, mPinchScale);
    					mPinchScale = mTextView.TextScaling();
    					this.updateOverSize();
    					mTextView.update(true);
    				}
    			}
    			return true;
    		}
		}

		if (mImmEnable) {
			if (action == MotionEvent.ACTION_DOWN) {
//				Log.d("touchDown", "x=" + x + ", y=" + y);
				if (y <= mImmCancelRange || y >= cy - mImmCancelRange) {
					// IMMERSIVEモードの発動時にタッチ処理を無視する
					mImmCancel = true;
				}
			}
			if (mImmCancel == true) {
				// ImmerModeの場合は上下端のタッチを無視する
				if (action == MotionEvent.ACTION_UP) {
					// UPイベントで解除
					mImmCancel = false;
				}
				return true;
			}
		}

		switch (action) {
			case MotionEvent.ACTION_DOWN:
			{
				// 押下状態を設定
				mGuideView.eventTouchDown((int)x, (int)y, cx, cy, mImmEnable ? false : true);

				mPageMode = false;
				mTouchPointNum = 0;

				// 慣性スクロールの停止
				mTouchThrough = mTextView.scrollStop();

				if (y > cy - mClickArea) {
					if (mPageSelect == PAGE_SLIDE) {
						if (mClickArea <= x && x <= cx - mClickArea) {
							// ページ選択開始
							mCurrentPage = mTextView.getPage(); // 現在ページ取得

							int sel = GuideView.GUIDE_BCENTER;
							mSelectPage = mCurrentPage;
							mGuideView.setGuideIndex(sel);

							mPageMode = true;
//							mPageView.setText(mFileList.createPageStr(mSelectPage));
//							mPageView.setBackgroundColor(mTopColor1);
							mGuideView.setPageColor(mTopColor1);
							if (mPageSelect == 0) {
								mGuideView.setPageText(mTextMgr.createPageStr(mSelectPage, mFilePath));
							}
							mPageModeIn = true;
						}
					}
					// 下部押下
					startLongTouchTimer(EVENT_TOUCH_BOTTOM); // ロングタッチのタイマー開始
					mOperation = TOUCH_COMMAND;
				}
				else if (y < mClickArea) {
					// 上部押下
					startLongTouchTimer(EVENT_TOUCH_TOP); // ロングタッチのタイマー開始
					mOperation = TOUCH_COMMAND;
				}
				else {
					// 操作モード
					mOperation = TOUCH_OPERATION;

					mTouchPoint[0].x = x;
					mTouchPoint[0].y = y;
					mTouchPointTime[0] = SystemClock.uptimeMillis();
					mTouchPointNum = 1;
//					mTouchTime = SystemClock.uptimeMillis();	// フリックの判定に
					mTouchDrawLeft = (int)mTextView.getDrawLeft();
//					mTouchFirstX = x;
				}

				this.mTouchFirst = true;
				this.mTouchBeginX = x;
				this.mTouchBeginY = y;
				break;
			}
			case MotionEvent.ACTION_MOVE:
			{
				// 移動位置設定
				mGuideView.eventTouchMove((int)x, (int)y);

				if (mOperation == TOUCH_COMMAND) {
					if (this.mPageMode && mPageSelect == PAGE_SLIDE) {
						// スライドページ選択中
						int sel = GuideView.GUIDE_NOSEL;
						if (y >= cy - mClickArea) {
							// 操作エリアから出て戻ったらそこを基準にする
							if (mPageModeIn == false) {
								// 指定のページを基準とした位置を設定
								mTouchBeginX = x - calcPageSelectRange(mSelectPage);
							}

							// タッチの位置でページを選択
							if (x < mClickArea) {
								if (mSelectPage != mTextMgr.length() - 1) {
									mSelectPage = mTextMgr.length() - 1;
									startVibrate();
								}
								sel = GuideView.GUIDE_BLEFT;
							}
							else if (x > cx - mClickArea) {
								if (mSelectPage != 0) {
									mSelectPage = 0;
									startVibrate();
								}
								sel = GuideView.GUIDE_BRIGHT;
							}
							else {
								// mSelectPage = mCurrentPage
								// + (int) (x - this.mTouchBeginX) / mPageRange;
								mSelectPage = calcSelectPage(x);

								if (mSelectPage < 0) {
									// 最小値は先頭ページ
									mSelectPage = 0;
									// タッチ位置を先頭ページとしたときのCurrentPageの位置を求める
									mTouchBeginX = x - calcPageSelectRange(mSelectPage);
								}
								else if (mSelectPage > mTextMgr.length() - 1) {
									// 最大値は最終ページ
									mSelectPage = mTextMgr.length() - 1;
									// タッチ位置を最終ページとしたときのCurrentPageの位置を求める
									mTouchBeginX = x - calcPageSelectRange(mSelectPage);
								}
								sel = GuideView.GUIDE_BCENTER;
							}

							String strPage = mTextMgr.createPageStr(mSelectPage, mFilePath);
							// String strOld = mPageView.getText().toString();
							String strOld = mGuideView.getPageText();
							if (!strPage.equals(strOld)) {
								if (mCurrentPage - 1 <= mSelectPage && mSelectPage <= mCurrentPage + 1) {
									// ページ変更時に振動
									startVibrate();
								}
								mGuideView.setPageText(strPage);
							}
							mGuideView.setPageColor(mTopColor1);
							mPageModeIn = true;
						}
						else {
							mPageModeIn = false;
						}
						// 選択に反映
						mGuideView.setGuideIndex(sel);
					}
				}
				else if (mOperation == TOUCH_OPERATION) {
					// ページ戻or進、スクロール処理
					if (this.mTouchFirst && ((Math.abs(this.mTouchBeginX - x) > mMoveRange || Math.abs(this.mTouchBeginY - y) > mMoveRange))) {
						// タッチ後に範囲を超えて移動した場合はスクロールモードへ
						this.mTouchFirst = false;
						mTextView.scrollStart(mTouchBeginX, mTouchBeginY, RANGE_FLICK, mScroll);
					}

					if (this.mTouchFirst == false) {
							// スクロールモード
							long now = SystemClock.uptimeMillis();
							mTextView.scrollMoveAmount(x - mTouchPoint[0].x, y - mTouchPoint[0].y, mScroll, true);

							for (int i = MAX_TOUCHPOINT - 1 ; i >= 1 ; i --) {
								mTouchPoint[i].x = mTouchPoint[i - 1].x;
								mTouchPoint[i].y = mTouchPoint[i - 1].y;
								mTouchPointTime[i] = mTouchPointTime[i - 1];
							}
							mTouchPoint[0].x = x;
							mTouchPoint[0].y = y;
							mTouchPointTime[0] = now;
							if (mTouchPointNum < MAX_TOUCHPOINT) {
								mTouchPointNum ++;
							}
					}
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			{
				// 押してる間のフラグクリア
				mTouchFirst = false;
				mOperation = TOUCH_NONE;
				mPinchOn = false;
				mPinchDown = false;

				// 上部/下部選択中の状態解除
				mGuideView.eventTouchCancel();
				// ページ選択中解除
				mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
				break;
			}
			case MotionEvent.ACTION_UP:
			{
				// 選択されたコマンド
				int result = mGuideView.eventTouchUp((int)x, (int)y);

				if (mPageMode) {
					// ページ選択モード終了
					mGuideView.setPageText(null);
					mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
					mGuideView.setGuideIndex(GuideView.GUIDE_NONE);

					if (mPageSelect == PAGE_SLIDE) {
						if (y > cy - mClickArea) {
							if (mPageSelect == 0 || x < mClickArea || x > cx - mClickArea) {
								// ページ選択確定
								if (mSelectPage != mCurrentPage) {
									// ページ変更時に振動
									startVibrate();
									mCurrentPage = mSelectPage;
									setPage(false);
								}
							}
						}
					}
				}
				if (result != -1) {
					int index = (result & 0x7FFF);
					if ((result & 0x8000) != 0) {
						// 上部選択の場合は選択リストを表示
						showSelectList(index);
					}
					else if (result == 0x4000) {
						// 戻るボタン
						operationBack();
					}
					else if (result == 0x4001) {
						// メニューボタン
						// 独自メニュー表示
						openMenu();
					}
					else if (result == 0x4002 || result == 0x4003) {
						// 末尾ボタン
						if (result == 0x4003) {
							if (mSelectPage != mTextMgr.length() - 1) {
								mSelectPage = mTextMgr.length() - 1;
							}
						}
						else {
							// 右側ボタン
							if (mSelectPage != 0) {
								mSelectPage = 0;
							}
						}
						// ページ選択確定
						if (mSelectPage != mCurrentPage) {
							// ページ変更時に振動
							startVibrate();
							mCurrentPage = mSelectPage;
							setPage(false);
						}
					}
					else {
						switch (index) {
							case 0:
								// 1ページ次へずらす
								if (mDispMode == DEF.DISPMODE_TX_DUAL && mCurrentPage > 0) {
									startVibrate();
									mCurrentPage--;
									setPage(true);
								}
								break;
							case 1:
								// 1ページ前へずらす
								if (mDispMode == DEF.DISPMODE_TX_DUAL && mCurrentPage < mTextMgr.length() - 1) {
									startVibrate();
									mCurrentPage++;
									setPage(false);
								}
								break;
							case 2:
								// 次巻(しおり位置)
								// 次のファイルを開き、続きから記録せず、現在頁保存
								finishActivity(CloseDialog.CLICK_NEXT, false, true);
								break;
							case 3:
								// 次巻(先頭ページ)
								// 次のファイルを開き、続きから記録せず、現在頁保存
								finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
								break;
							case 4:
								// 次巻(最終ページ)
								// 次のファイルを開き、続きから記録せず、現在頁保存
								finishActivity(CloseDialog.CLICK_NEXTLAST, false, true);
								break;
							case 5:
								// 前巻(しおり位置)
								// 前のファイルを開き、続きから記録せず、現在頁保存
								finishActivity(CloseDialog.CLICK_PREV, false, true);
								break;
							case 6:
								// 前巻(先頭ページ)
								// 前のファイルを開き、続きから記録せず、現在頁保存
								finishActivity(CloseDialog.CLICK_PREVTOP, false, true);
								break;
							case 7:
								// 前巻(最終ページ)
								// 前のファイルを開き、続きから記録せず、現在頁保存
								finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
								break;
							case 8:
								if (mPageSelect == PAGE_INPUT) {
									// 下部選択の場合は対応する操作を実行
									mCurrentPage = mTextView.getPage(); // 現在ページ取得

									PageSelectDialog pageDlg = new PageSelectDialog(this, mImmEnable);
									pageDlg.setParams(mCurrentPage, mTextMgr.length(), true);
									pageDlg.setPageSelectListear(this);
									pageDlg.show();
								}
								break;
						}
					}
				}
				else if (mOperation == TOUCH_OPERATION) {
					if (this.mTouchFirst) {
						// スクロール停止の時は呼ばない
						if (mTouchThrough == false) {
							this.mTouchFirst = false;

							boolean next = checkTapDirectionNext(x, y, cx, cy);
							if (mTapScrl) {
								// タップでスクロール
								int move = next ? 1 : -1;
								// 読込中の表示
								if (!mTextView.setViewPosScroll(move)) {
									// スクロールする余地がなければ次ページ
									changePage(move);
								}
								else {
									// スクロール開始
									mTextView.startScroll();
								}
							}
							else {
								// タップでスクロールしない
								if (next) {
									// 次ページへ
									nextPage();
								}
								else {
									// 前ページへ
									prevPage();
								}
							}
							// スクロールを停止した場合は処理しない
							break;
						}
					}
					else {
						// スワイプ
						int flickPage = mTextView.checkFlick();

						if (mFlickPage && mDispMode != DEF.DISPMODE_TX_SERIAL && flickPage != 0) {
							// 連続表示ではなし
							// フリックでページ遷移
							if (mFlickEdge && mTouchDrawLeft != (int)mTextView.getDrawLeft()) {
								// 端からフリックしないときはページめくりしない
								;
							}
							else if (flickPage > 0 ? !mChgFlick : mChgFlick) {
								// 次ページへ
								nextPage();
							}
							else {
								// 前ページへ
								prevPage();
							}
						}
						else if (mMomentMode < DEF.MAX_MOMENTMODE){
							int i;
							long now = SystemClock.uptimeMillis();
							for (i = 1 ; i < mTouchPointNum && i < MAX_TOUCHPOINT ; i ++) {
								if (now - mTouchPointTime[i]> TERM_MOMENT) {
									// 過去0.2秒の範囲
									break;
								}
							}
							if (i >= 3) {
								float sx = mTouchPoint[2].x - mTouchPoint[i - 1].x;
								float sy = mTouchPoint[2].y - mTouchPoint[i - 1].y;
								long term = mTouchPointTime[2] - mTouchPointTime[i - 1];
//								Log.d("moment_up", "i=" + i + ", sx=" + sx + ", sy=" + sy + ", term=" + term);
								mTextView.momentiumStart(x, y, mScroll, sx, sy, (int)term, mMomentMode);
							}
						}
					}
				}
				// 押してる間のフラグクリア
				mTouchFirst = false;
				mOperation = TOUCH_NONE;
				break;
			}
		}
		return true;
	}

	// タップが前/次どちらか判定
	private boolean checkTapDirectionNext(float x, float y, int cx, int cy){
		boolean next = false;

		float rate = mTapRate + 1;
		float rcx = cx / 10.0f;
		float rcy = (cy - mClickArea * 2) / 10.0f;
		switch (mTapPattern) {
			case 0:
				next = (x >= cx - rcx * rate) ? !mChgPage : mChgPage;
				break;
			case 1:
				next = (x <= rcx * rate / 2 || x >= cx - rcx * rate / 2) ? !mChgPage : mChgPage;
				break;
			case 2:
				next = (y > cy - mClickArea - rcy * rate) ? !mChgPage : mChgPage;
				break;
			case 3:
				next = (y > cy - mClickArea - rcy * rate / 2 || y < mClickArea + rcy * rate / 2) ? !mChgPage : mChgPage;
				break;
		}
		return next;
	}


	private int mSelectMode;

	private void showSelectList(int index) {
		if (mListDialog != null) {
			return;
		}
		if (index < 0 || index > mCommandStr.length) {
			// インデックスが範囲外
			return;
		}
		if (index == 3) {
			// 見出し選択
			openChapterMenu();
			return;

		}
		// 再読み込みになるのでページ戻は解除
		Resources res = getResources();

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;
		int nItem;

		String title;
		int selIndex;
		switch (index) {
			case 0:
				// 画面方向
				title = res.getString(R.string.rotateMenu);
				selIndex = mViewRota - 1;
				nItem = SetTextActivity.RotateName.length - 1;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.RotateName[i + 1]);
				}
				break;
			case 1:
				// 見開きモードの選択肢設定
				title = res.getString(R.string.tguide02);
				selIndex = mDispMode;
				nItem = SetTextActivity.ViewName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetTextActivity.ViewName[i]);
				}
				break;
			case 2:
				// サイズ設定の選択肢設定
				title = res.getString(R.string.tguide03);
				selIndex = mScaleMode;
				nItem = SetTextActivity.ScaleName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetTextActivity.ScaleName[i]);
				}
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(this, title, items, selIndex, true, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case 0:
						// 画面方向
						if (mViewRota != index + 1) {
							int prevRota = mViewRota;
							mViewRota = index + 1;
							DEF.setRotation(mActivity, mViewRota);
							if (mViewRota == DEF.ROTATE_PSELAND) {
								// 疑似横画面
								mPseLand = true;
							}
							else {
								mPseLand = false;
							}
							// 90度回転反映
							setConfig();

							boolean isPrePort = true;
							boolean isAftPort = true;

							if (prevRota == DEF.ROTATE_LANDSCAPE) {
								isPrePort = false;
							}
							else if (prevRota == DEF.ROTATE_AUTO) {
								int width = mTextView.getWidth();
								int height = mTextView.getHeight();
								if (DEF.checkPortrait(width, height) == false) {
									isPrePort = false;
								}
							}

							if (mViewRota == DEF.ROTATE_LANDSCAPE) {
								isAftPort = false;
							}

							if (isPrePort == isAftPort) {
								// 変化がないときは強制的に発生させる
								mTextView.lockDraw();
								mTextView.updateScreenSize();
								mTextView.TextScaling();
								mTextView.updateOverSize();
								mTextView.update(true);
							}
						}
						break;
					case 1:
						// 見開き設定変更
						if (mDispMode != index) {
							mDispMode = index;
							mTextView.lockDraw();
							mTextView.setDispMode(mDispMode);
							mGuideView.setGuideMode(mDispMode == DEF.DISPMODE_TX_DUAL, mBottomFile, true, mPageSelect, false);
							setTextPageData();
							mTextView.update(true);
						}
						break;
					case 2:
						// 画像拡大率の変更
						mTextView.lockDraw();
						ChangeScale(index);
						mTextView.update(true);
						break;
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
		return;
	}

	// 座標から選択するページを求める
	private int calcSelectPage(float x) {
		int page = mCurrentPage;
		int pagecnt = 0;
		int range = (int) Math.abs((x - mTouchBeginX)); // 絶対値
		int sign = x < mTouchBeginX ? 1 : -1; // ページ方向

		for (int i = 0; i < CTL_COUNT.length; i++) {
			if (range <= mPageRange * (CTL_COUNT[i] * CTL_RANGE[i])) {
				// 左右3単位分までページ変化なし
				page = mCurrentPage + (pagecnt + range / (mPageRange * CTL_RANGE[i])) * sign;
				break;
			}
			// 移動範囲から減らす
			range -= mPageRange * CTL_COUNT[i] * CTL_RANGE[i];
			// その分のページ数を加算
			pagecnt += CTL_COUNT[i];
		}
		return page;
	}

	// ページ選択時に表示する文字列を作成
	private float calcPageSelectRange(int page) {
		int pagecnt = Math.abs(mCurrentPage - page); // ページの差の絶対値
		int range = 0;

		for (int i = 0; i < CTL_COUNT.length; i++) {
			if (pagecnt <= CTL_COUNT[i]) {
				// 半端分を計算
				range += pagecnt * (mPageRange * CTL_RANGE[i]);
				break;
			}
			// 移動範囲から減らす
			range += CTL_COUNT[i] * (mPageRange * CTL_RANGE[i]);

			// その分のページ数を加算
			pagecnt -= CTL_COUNT[i];
		}
		// 方向を設定
		return range * (mCurrentPage <= page ? -1 : 1);
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	private void updateOverSize() {
		// 用紙位置を調整
		mTextView.updateOverSize(false);
	}

	// オプションメニューが表示される度に呼び出されます
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		boolean ret = super.onPrepareOptionsMenu(menu);
//		return ret;
//	}

	// 戻る操作
	private void operationBack() {
		if (mGuideView.getOperationMode()) {
			mGuideView.setOperationMode(false);
			return;
		}
		else if (mReadRunning) {
			if (mTextMgr != null) {
				mTextMgr.setBreakTrigger();
			}
			mTerminate = true;
			return;
		}
		else {
			if (mConfirmBack) {
				// 終了
				showCloseDialog(CloseDialog.LAYOUT_BACK);
			}
			else {
				finishActivity(true);
			}
		}
		return;
	}

	// メニューを開く
	private void openMenu() {
		if (mMenuDialog != null || mTextMgr == null || mTextView == null) {
			return;
		}

		Resources res = getResources();
//		setOptionMenu(menu);
		mMenuDialog = new MenuDialog(this, mTextView.getWidth(), mTextView.getHeight(), true, this);

		mMenuDialog.addSection(res.getString(R.string.operateSec));
		if (mTextMgr.getMidashiSize() > 0) {
    		// 見出し選択
			mMenuDialog.addItem(DEF.MENU_SELCHAPTER, res.getString(R.string.selChapterMenu));
		}
		// ブックマーク選択
		mMenuDialog.addItem(DEF.MENU_SELBOOKMARK, res.getString(R.string.selBookmarkMenu));
		// ブックマーク追加
		mMenuDialog.addItem(DEF.MENU_ADDBOOKMARK, res.getString(R.string.addBookmarkMenu));
		// 検索
		mMenuDialog.addItem(DEF.MENU_SEARCHTEXT, res.getString(R.string.searchTextMenu));
		if (mTextMgr.getMarker() != null) {
    		// 検索
			mMenuDialog.addItem(DEF.MENU_SEARCHJUMP, res.getString(R.string.searchJumpMenu));
		}
//		// ページ選択
//		menuDlg.addItem(DEF.MENU_PAGESEL, res.getString(R.string.pageselMenu));
		// 音操作
		mMenuDialog.addItem(DEF.MENU_NOISE, res.getString(R.string.noiseMenu), mNoiseSwitch != null);
		// 画面回転
		if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota == DEF.ROTATE_LANDSCAPE) {
			mMenuDialog.addItem(DEF.MENU_ROTATE, res.getString(R.string.rotateMenu));
		}

		mMenuDialog.addSection(res.getString(R.string.settingSec));
		// テキスト表示設定
		mMenuDialog.addItem(DEF.MENU_TXTCONF, res.getString(R.string.txtConfMenu));
		// 見開き設定
		mMenuDialog.addItem(DEF.MENU_IMGVIEW, res.getString(R.string.tguide02));
		// 画像サイズ
		mMenuDialog.addItem(DEF.MENU_IMGSIZE, res.getString(R.string.tguide03));
		// ページめくりタップの入れ替え
		mMenuDialog.addItem(DEF.MENU_CHG_OPE, res.getString(R.string.chgOpeMenu), mChgPage);

		mMenuDialog.addSection(res.getString(R.string.otherSec));
		// ヘルプ
		mMenuDialog.addItem(DEF.MENU_ONLINE, res.getString(R.string.onlineMenu));
		// 操作確認
		mMenuDialog.addItem(DEF.MENU_HELP, res.getString(R.string.helpMenu), mGuideView.getOperationMode());
		// 設定
		mMenuDialog.addItem(DEF.MENU_SETTING, res.getString(R.string.setMenu));
		// バージョン情報
		mMenuDialog.addItem(DEF.MENU_ABOUT, res.getString(R.string.aboutMenu));
		mMenuDialog.show();
	}

	// メニューを開く
	private void openBookmarkMenu() {
		if (mTextMgr == null || mTextView == null || mMenuDialog != null) {
			return;
		}

		mMenuDialog = new MenuDialog(this, mTextView.getWidth(), mTextView.getHeight(), false, this);

		ArrayList<RecordItem>list = RecordList.load(null, RecordList.TYPE_BOOKMARK, mServer, mLocalPath + mFileName, mTextName);

		for (int i = 0 ; i < list.size(); i ++) {
			// ブックマーク追加
			RecordItem data = list.get(i);
			int page = data.getPage();
			mMenuDialog.addItem(DEF.MENU_BOOKMARK + page, data.getDispName(), "P." + (page + 1));
		}
		if (list != null && list.size() > 0) {
			mMenuDialog.show();
		}
		else {
			mMenuDialog = null;
		}
	}

	// メニューを開く
	private void openChapterMenu() {
		if (mTextMgr == null || mTextView == null || mMenuDialog != null) {
			return;
		}

		mMenuDialog = new MenuDialog(this, mTextView.getWidth(), mTextView.getHeight(), false, this);

		int size = mTextMgr.getMidashiSize();

		for (int i = 0 ; i < size; i ++) {
			// ブックマーク追加
			MidashiData md = mTextMgr.getMidashi(i);
			if (md != null) {
				int page = md.getPage();
				if (page >= 0) {
					mMenuDialog.addItem(DEF.MENU_CHAPTER + page, md.getText(), "P." + (page + 1));
				}
			}
		}
		if (size > 0) {
			mMenuDialog.show();
		}
		else {
			mMenuDialog = null;
		}
	}

	// メニューを開く
	private void openSearchMenu() {
		if (mTextMgr == null || mTextView == null || mMenuDialog != null) {
			return;
		}

		MidashiData[] mdlist = mTextMgr.getSearchList();
		if (mdlist == null || mdlist.length == 0) {
			// 該当なし
			return;
		}

		mMenuDialog = new MenuDialog(this, mTextView.getWidth(), mTextView.getHeight(), false, true, this);

		for (int i = 0; i < mdlist.length; i ++) {
			// 検索結果表示
			int page = mdlist[i].getPage();
			if (page >= 0) {
				mMenuDialog.addItem(DEF.MENU_CHAPTER + page, "P." + (page + 1));
			}
		}
		mMenuDialog.show();
	}

//	void setOptionMenu(Menu menu) {
//		Resources res = getResources();
//
//		// メニューをクリア
//		menu.clear();
//		return;
//	}

//	public boolean onOptionsItemSelected(MenuItem item) {
//		int id = item.getItemId();
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public void onSelectMenuDialog(int id) {
		// メニュークローズ
		mMenuDialog = null;

		switch (id) {
			case DEF.MENU_TXTCONF: {
				// テキスト設定
				showTextConfigDialog();
				break;
			}
			case DEF.MENU_IMGVIEW: {
				// 見開き設定
				showSelectList(1);
				break;
			}
			case DEF.MENU_IMGSIZE: {
				// 画像サイズ
				showSelectList(2);
				break;
			}
			case DEF.MENU_HELP: {
				// 操作方法画面に遷移
				// Intent intent = new Intent(TextActivity.this, HelpActivity.class);
				// startActivityForResult(intent, DEF.REQUEST_HELP);
				boolean flag = !mGuideView.getOperationMode();
				mGuideView.setOperationMode(flag);
				break;
			}
			case DEF.MENU_ONLINE: {
				// 操作方法画面に遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_operatetext);	// 設定画面
				Intent intent;
				intent = new Intent(TextActivity.this, HelpActivity.class);
				intent.putExtra("Url", url);
				startActivity(intent);
				break;
			}
			case DEF.MENU_ROTATE: {
				// 画面の縦横切替
				int rotate;
				if (getRequestedOrientation() == DEF.ROTATE_PORTRAIT) {
					// 横にする
					rotate = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				}
				else {
					// 縦にする
					rotate = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				}
				setRequestedOrientation(rotate);
				break;
			}
			case DEF.MENU_SETTING: {
				// 設定画面に遷移
				Intent intent = new Intent(TextActivity.this, SetConfigActivity.class);
				startActivityForResult(intent, DEF.REQUEST_SETTING);
				break;
			}
			case DEF.MENU_NOISE: {
				// マイク開始
				if (mNoiseSwitch == null) {
					mNoiseSwitch = new NoiseSwitch(mHandler);
					mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);
					mNoiseSwitch.recordStart();
					// 画面をスリープ無効
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				else {
					mNoiseSwitch.recordStop();
					mNoiseSwitch = null;
					mGuideView.setNoiseState(0, 0);
					// 画面をスリープ有効
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				break;
			}
			case DEF.MENU_CHG_OPE: {
				// 操作方向の入れ替え
				mChgPage = !mChgPage;
				mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
//				mGuideView.invalidate();
				break;
			}
			case DEF.MENU_ADDBOOKMARK: {
				// ブックマーク追加ダイアログ表示
				mCurrentPage = mTextView.getPage(); // 現在ページ取得

				BookmarkDialog bookmarkDlg = new BookmarkDialog(this);
				bookmarkDlg.setBookmarkListear(this);
				bookmarkDlg.setName((mCurrentPage + 1) + " / " + mTextMgr.length());
				bookmarkDlg.show();
				break;
			}
			case DEF.MENU_SELBOOKMARK: {
				// ブックマーク選択ダイアログ表示
				openBookmarkMenu();
				break;
			}
			case DEF.MENU_SELCHAPTER: {
				// 見出し選択ダイアログ表示
				openChapterMenu();
				break;
			}
			case DEF.MENU_SEARCHTEXT: {
				// レイアウトの呼び出し
				if (mInputDialog != null) {
					return;
				}
				Resources res = getResources();
				String title = res.getString(R.string.searchTextMenu);
				mInputDialog = new InputDialog(this, title, mSearchText, new SearchListener() {
        			@Override
        			public void onSearch(String text) {
        				if (text != null && text.length() > 0) {
            				// 検索文字列セット
            				mSearchText = text;
            				mTextMgr.searchText(text);
            				mTextView.setMarker(mTextMgr.getMarker());

            				// メニュー表示
            				openSearchMenu();
        				}
        				else {
            				// 検索文字列クリア
            				mSearchText = "";
            				// 検索該当箇所クリア
            				mTextMgr.searchClear();
            				mTextView.setMarker(null);
        				}
        			}

        			@Override
        			public void onCancel() {
        				// 検索文字列クリア
        				mSearchText = "";
        				// 検索該当箇所クリア
        				mTextMgr.searchClear();
        				mTextView.setMarker(null);
        			}

        			@Override
        			public void onClose() {
        				// 終了
        				mInputDialog = null;
        			}
        		});
        		mInputDialog.show();
        		break;
			}
			case DEF.MENU_SEARCHJUMP: {
				// メニュー表示
				openSearchMenu();
        		break;
			}
			default: {
				if (id >= DEF.MENU_CHAPTER) {
					onSelectPage(id - DEF.MENU_CHAPTER);
				}
				else if (id >= DEF.MENU_BOOKMARK) {
					onSelectPage(id - DEF.MENU_BOOKMARK);
				}
				else {
					// バージョン情報
			        Information dlg = new Information(this);
					dlg.showAbout();
				}
				break;
			}
		}
	}

	// テキスト設定用ダイアログ表示
	private void showTextConfigDialog() {
		if (mTextConfigDialog != null) {
			return;
		}
		mTextConfigDialog = new TextConfigDialog(this);

		mTextConfigDialog.setConfig(mPicSize, mBkLight, mHeadSizeOrg, mBodySizeOrg, mRubiSizeOrg, mInfoSizeOrg, mSpaceW, mSpaceH, mMarginWOrg, mMarginHOrg, mAscMode, mIsConfSave);
		mTextConfigDialog.setTextConfigListner(new TextConfigListenerInterface() {
			@Override
			public void onButtonSelect(int select, int picsize, int bklight, int top, int body, int rubi, int info, int spacew, int spaceh, int marginw, int marginh, int ascmode, boolean issave) {
				// 選択状態を通知
				boolean ischange = false;
				// 変更があるかを確認(適用後のキャンセルの場合も含む)
				if (mPicSize != picsize || mHeadSizeOrg != top || mBodySizeOrg != body || mRubiSizeOrg != rubi || mInfoSizeOrg != info || mSpaceW != spacew || mSpaceH != spaceh || mMarginWOrg != marginw || mMarginHOrg != marginh || mAscMode != ascmode) {
					ischange = true;
				}
				mPicSize = picsize;
				mHeadSizeOrg = top;
				mBodySizeOrg = body;
				mRubiSizeOrg = rubi;
				mInfoSizeOrg = info;
				mSpaceW = spacew;
				mSpaceH = spaceh;
				mMarginWOrg = marginw;
				mMarginHOrg = marginh;
				mAscMode = ascmode;
				mIsConfSave = issave;

				mHeadSize = DEF.calcFontPix(top, mDensity);
				mBodySize = DEF.calcFontPix(body, mDensity);
				mRubiSize = DEF.calcFontPix(rubi, mDensity);
				mInfoSize = DEF.calcFontPix(info, mDensity);
				mMarginW = DEF.calcDispMargin(marginw);
				mMarginH = mInfoSize + DEF.calcDispMargin(marginh);

				if (mBkLight != bklight) {
					// バックライト変更
					mBkLight = bklight;

					float l = -1;
					if (mBkLight <= 10) {
						l = (float)mBkLight / 10;
					}
					WindowManager.LayoutParams lp = getWindow().getAttributes();
					lp.screenBrightness = l;
					getWindow().setAttributes(lp);
				}

				if (ischange) {
					// 表示を更新
					reloadText();
				}

				if (issave) {
					// 設定を指定
					Editor ed = mSharedPreferences.edit();
					ed.putString(DEF.KEY_TX_BKLIGHT, Integer.toString(mBkLight));
					ed.putString(DEF.KEY_TX_PICSIZE, Integer.toString(mPicSize));
					ed.putInt(DEF.KEY_TX_FONTTOP, mHeadSizeOrg);
					ed.putInt(DEF.KEY_TX_FONTBODY, mBodySizeOrg);
					ed.putInt(DEF.KEY_TX_FONTRUBI, mRubiSizeOrg);
					ed.putInt(DEF.KEY_TX_FONTINFO, mInfoSizeOrg);
					ed.putInt(DEF.KEY_TX_SPACEW, mSpaceW);
					ed.putInt(DEF.KEY_TX_SPACEW, mSpaceH);
					ed.putInt(DEF.KEY_TX_MARGINW, mMarginWOrg);
					ed.putInt(DEF.KEY_TX_MARGINH, mMarginHOrg);
					ed.putInt(DEF.KEY_TX_ASCMODE, mAscMode);
					ed.commit();
				}
			}

			@Override
			public void onClose() {
				// 終了
				mTextConfigDialog = null;
			}
		});
		mTextConfigDialog.show();
	}

	@Override
	public void onCloseMenuDialog() {
		// メニュー終了
		mMenuDialog = null;
	}

	@Override
	public void onSelectPage(int page) {
		// 現在ページ
		mCurrentPage = mTextView.getPage();
		// ページ選択確定
		if (mCurrentPage != page) {
			// ページ変更時に振動
			startVibrate();
			mCurrentPage = page;
			setPage(false);
		}
	}

	// 他アクティビティからの復帰通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DEF.REQUEST_SETTING || requestCode == DEF.REQUEST_HELP) {
			// 設定の読込
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

			ReadSetting(sharedPreferences);

			reloadText();
		}
		else if (requestCode == DEF.REQUEST_FILE) {
			setTextPageData();
		}
	}

	// テキストを再読み込み
	private void reloadText() {
		if (mImmEnable && mSdkVersion >= 19) {
			int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		}

		// 現在ページ
		mCurrentPage = mTextView.getPage();

		mTextView.setTextBuffer(null, null, null);
		mTextView.setDispMode(mDispMode);
		mGuideView.setGuideMode(mDispMode == DEF.DISPMODE_TX_DUAL, mBottomFile, true, mPageSelect, false);
		setConfig();
		mTextView.setColor(mTextColor, mBackColor, mGradColor, mGradation, mSrchColor);
		mTextView.updateScreenSize();
		mTextView.setMarker(null);

		// 色とサイズを指定
		mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
		mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);

		// プログレスダイアログ準備
		mReadDialog = new ProgressDialog(this);
		mReadDialog.setMessage(mParsingMsg + " (0)");
		mReadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mReadDialog.setCancelable(true);
		mReadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// Thread を停止
				if (mTextMgr != null) {
					mTextMgr.setBreakTrigger();
				}
				mTerminate = true;
			}
		});
		if (mImmEnable && mSdkVersion >= 19) {
			mReadDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
			mReadDialog.show();
			mReadDialog.getWindow().getDecorView().setSystemUiVisibility(this.getWindow().getDecorView().getSystemUiVisibility());
			mReadDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		}
		else {
			mReadDialog.show();
		}

		mReadRunning = true;
		mTextLoad = new TextLoad(mHandler, this);
		mTextThread = new Thread(mTextLoad);
		mTextThread.start();
	}

	// 設定の読み込み
	private void ReadSetting(SharedPreferences sharedPreferences) {
		// 設定値取得
		mViewPoint = SetImageText.getViewPt(sharedPreferences);

		mScroll = DEF.calcScroll(SetImageTextDetailActivity.getScroll(sharedPreferences));
		mClickArea = DEF.calcClickAreaPix(SetImageTextDetailActivity.getClickArea(sharedPreferences), mDensity);
		mPageRange = DEF.calcPageRangePix(SetImageTextDetailActivity.getPageRange(sharedPreferences), mDensity);
		mMoveRange = DEF.calcTapRangePix(SetImageTextDetailActivity.getTapRange(sharedPreferences), mDensity);

		mEffectTime = DEF.calcEffectTime(SetImageTextDetailActivity.getEffectTime(sharedPreferences));
		mPageSelect = SetImageText.getTxPageSelect(sharedPreferences);

		mCenter = SetImageTextDetailActivity.getCenter(sharedPreferences);
		mShadow = SetImageTextDetailActivity.getGradation(sharedPreferences);
		mMargin = SetImageTextDetailActivity.getMargin(sharedPreferences);
		if (mSdkVersion >= 19) {
			// KitKat以降のみ設定読み込み
			mImmEnable = SetImageTextDetailActivity.getImmEnable(sharedPreferences);
		}
		else {
			mImmEnable = false;
		}
		mOldMenu = SetImageTextDetailActivity.getOldMenu(sharedPreferences);
		mBottomFile = SetImageTextDetailActivity.getBottomFile(sharedPreferences);
		mPinchEnable = SetImageTextDetailActivity.getPinchEnable(sharedPreferences);

		mVolScrl = DEF.calcScrlSpeedPix(SetImageTextDetailActivity.getVolScrl(sharedPreferences), mDensity);
		mTapScrl = SetImageText.getTapScrl(sharedPreferences);
		mScrlRngW = DEF.calcScrlRange(SetTextActivity.getScrlRngW(sharedPreferences));
		mScrlRngH = DEF.calcScrlRange(SetTextActivity.getScrlRngH(sharedPreferences));

		mNoiseScrl = DEF.calcScrlSpeedPix(SetNoiseActivity.getNoiseScrl(sharedPreferences), mDensity);
		mNoiseUnder = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseUnder(sharedPreferences));
		mNoiseOver = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseOver(sharedPreferences));
		mNoiseLevel = SetNoiseActivity.getNoiseLevel(sharedPreferences);
		mNoiseDec = SetNoiseActivity.getNoiseDec(sharedPreferences);
		if (mNoiseSwitch != null) {
			mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);
		}

		mMgnColor = SetImageTextColorActivity.getTxtMgnColor(sharedPreferences);
		mCenColor = SetImageTextColorActivity.getTxtCntColor(sharedPreferences);
		// mTopColor = 0x60000000 |
		// ((mMgnColor & 0x00010000) != 0 ? 0x00700000 : 0) |
		// ((mMgnColor & 0x00000100) != 0 ? 0x00007000 : 0) |
		// ((mMgnColor & 0x00000001) != 0 ? 0x00000070 : 0);
		mTopColor1 = SetImageTextColorActivity.getTxtGuiColor(sharedPreferences);
		mTopColor2 = 0x40000000 | (mTopColor1 & 0x00FFFFFF);

		mChgPage = SetImageText.getChgPage(sharedPreferences);
		mChgPageKey = SetImageText.getChgPageKey(sharedPreferences);
		mChgFlick = SetImageText.getChgFlick(sharedPreferences);
		mLastMsg = SetImageText.getLastPage(sharedPreferences);
		// mSavePage = false;// SetImageText.getSavePage(sharedPreferences);
		mVibFlag = SetImageText.getVibFlag(sharedPreferences);
		mFlickPage = SetImageText.getFlickPage(sharedPreferences);
		mFlickEdge = SetImageText.getFlickEdge(sharedPreferences);
		mMomentMode = SetImageTextDetailActivity.getMomentMode(sharedPreferences);

		mPrevRev = SetImageText.getPrevRev(sharedPreferences); // ページ戻り時の左右位置反転
		mTapPattern = SetImageText.getTapPattern(sharedPreferences);	// タップパターン
		mTapRate = SetImageText.getTapRate(sharedPreferences); 			// タップの比率

		mVolKeyMode = SetImageText.getVolKey(sharedPreferences); // 音量キー操作
		mRotateBtn = DEF.RotateBtnList[SetCommonActivity.getRotateBtn(sharedPreferences)];
		mCharset = DEF.CharsetList[SetCommonActivity.getCharset(sharedPreferences)];

		mConfirmBack = SetImageText.getConfirmBack(sharedPreferences);	// 戻るキーで確認メッセージ

		mPaperSel = SetTextActivity.getPaper(sharedPreferences); // 用紙サイズ
		if (mPaperSel == DEF.PAPERSEL_SCREEN) {
			if (mTextView != null) {
				int cx;
				int cy;
				if (mSdkVersion >= 19 && mImmEnable) {
					// ウィンドウマネージャのインスタンス取得
					WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
					// ディスプレイのインスタンス生成
					Display disp = wm.getDefaultDisplay();
					// ナビゲーション以外
					Point dispSize = new Point();
					disp.getSize(dispSize);
					// ハードウェアサイズ
					Point hardSize = new Point();
					disp.getRealSize(hardSize);
					//
					cx = mTextView.getWidth();
					cy = hardSize.y - dispSize.y + mTextView.getHeight();
				}
				else {
					cx = mTextView.getWidth();
					cy = mTextView.getHeight();
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
		}
		else {
			mTextWidth = DEF.PAPERSIZE[mPaperSel][0];
			mTextHeight = DEF.PAPERSIZE[mPaperSel][1];
		}
		mHeadSizeOrg = SetTextActivity.getFontTop(sharedPreferences);	// 見出し
		mBodySizeOrg = SetTextActivity.getFontBody(sharedPreferences);	// 本文
		mRubiSizeOrg = SetTextActivity.getFontRubi(sharedPreferences);	// ルビ
		mInfoSizeOrg = SetTextActivity.getFontInfo(sharedPreferences);	// ページ情報など

		mHeadSize = DEF.calcFontPix(mHeadSizeOrg, mDensity);	// 見出し
		mBodySize = DEF.calcFontPix(mBodySizeOrg, mDensity);	// 本文
		mRubiSize = DEF.calcFontPix(mRubiSizeOrg, mDensity);	// ルビ
		mInfoSize = DEF.calcFontPix(mInfoSizeOrg, mDensity);	// ページ情報など

		mPicSize = SetTextActivity.getPicSize(sharedPreferences);	// 挿絵サイズ

		mSpaceW = SetTextActivity.getSpaceW(sharedPreferences);	// 行間
		mSpaceH = SetTextActivity.getSpaceH(sharedPreferences);	// 字間

		mMarginWOrg = SetTextActivity.getMarginW(sharedPreferences);	// 左右余白(設定値)
		mMarginHOrg = SetTextActivity.getMarginH(sharedPreferences);	// 上下余白(設定値)

		mMarginW = DEF.calcDispMargin(mMarginWOrg);				// 左右余白
		mMarginH = mInfoSize + DEF.calcDispMargin(mMarginHOrg);	// 上下余白

		mDispMode = SetTextActivity.getInitView(sharedPreferences); // 表示モード(DUAL/HALF/SERIAL)
		mViewRota = SetTextActivity.getViewRota(sharedPreferences);
		mScaleMode = SetTextActivity.getIniScale(sharedPreferences);
		mAscMode = SetTextActivity.getAscMode(sharedPreferences);

		mNotice = SetTextActivity.getNotice(sharedPreferences);
		mNoSleep = SetTextActivity.getNoSleep(sharedPreferences);
		mCMargin = SetTextActivity.getCenterMargin(sharedPreferences);
		mCShadow = SetTextActivity.getCenterShadow(sharedPreferences);
		mEffect = SetTextActivity.getEffect(sharedPreferences);

		mTextColor = SetImageTextColorActivity.getTvtColor(sharedPreferences);
		mBackColor = SetImageTextColorActivity.getTvbColor(sharedPreferences);
		mGradColor = SetImageTextColorActivity.getTvgColor(sharedPreferences);
		mGradation = SetImageTextColorActivity.getGradation(sharedPreferences);

		mSrchColor = SetImageTextColorActivity.getHitColor(sharedPreferences);

		mBkLight = SetTextActivity.getBkLight(sharedPreferences);

		mTimeDisp = SetImageActivity.getTimeDisp(sharedPreferences); // 時刻と充電表示有無
		mTimeFormat = SetImageActivity.getTimeFormat(sharedPreferences); // 時刻と充電表示書式
		mTimePos = SetImageActivity.getTimePos(sharedPreferences); // 時刻と充電表示位置
		mTimeSize = DEF.calcPnumSizePix(SetImageActivity.getTimeSize(sharedPreferences), mDensity); // 時刻と充電表示サイズ
		mTimeColor = SetImageActivity.getTimeColor(sharedPreferences); // 時刻と充電表示色

		if (mGuideView != null) {
			mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
		}

		DEF.setRotation(this, mViewRota);
		if (mViewRota == DEF.ROTATE_PSELAND) {
			// 疑似横画面
			mPseLand = true;
		}
		else {
			mPseLand = false;
		}

		String fontname = SetTextActivity.getFontName(sharedPreferences);
		if (fontname != null && fontname.length() > 0) {
			String path = DEF.getFontDirectory();
			mFontFile = path + fontname;
		}
		else {
			mFontFile = null;
		}

		// バックライト設定
		if (mBkLight <= 10) {
			// バックライト変更
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.screenBrightness = (float)mBkLight / 10;
			getWindow().setAttributes(lp);
		}
		return;
	}

	private void setConfig() {
		if (mTextView != null) {
			boolean result;
			result = mTextView.setConfig(mMgnColor, mCenColor, mTopColor1, mViewPoint, mMargin, mCenter, mShadow, mScrlRngW, mScrlRngH, mVolScrl, mPrevRev, mCMargin, mCShadow, mPseLand, mEffect, mEffectTime, mFontFile, mAscMode != TextManager.ASC_NORMAL);
			if (result == false) {
				Toast.makeText(this, "open font error:\"" + mFontFile + "\"", Toast.LENGTH_LONG).show();

			}
		}
	}

	private void startVibrate() {
		long nowTime = System.currentTimeMillis();

		if (mVibFlag) {
			if (nowTime > mPrevVibTime + TIME_VIB_TERM) {
				// 前回と間が空いているときだけ振動
				mVibrator.vibrate(TIME_VIB_RANGE);
				mPrevVibTime = nowTime;
			}
		}
	}

	private void changePage(int move) {
		if (move >= 0) {
			// 次ページ
			nextPage();
		}
		else {
			// 前ページ
			prevPage();
		}
		return;
	}

	private void setPage(boolean isBack) {
		mTextView.setPage(mCurrentPage, isBack, false);
	}

	private void nextPage() {
		if (mTextView.nextPage()) {
			startVibrate();
		}
		else {
			// 最終ページ
			if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_LAST);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
			}
			else {
				finishActivity(false);
			}
		}
	}

	private void prevPage() {
		// 前ページへ
		if (mTextView.prevPage()) {
			startVibrate();
		}
		else {
			// 先頭ページ
			if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_TOP);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
			}
		}
	}

	private void startScroll(int move) {
		if (!mReadRunning && !mTextView.getScrolling()) {
			if (!mTextView.setViewPosScroll(move)) {
				// スクロールする余地がなければ次ページ
				changePage(move);
			}
			else {
				// スクロール開始
				mTextView.startScroll();
			}
		}
	}

	private void finishActivity(boolean resume) {
		finishActivity(CloseDialog.CLICK_CLOSE, resume, true);
	}

	private void finishActivity(int select, boolean resume, boolean mark) {
		// 続きから読み込みの設定
		if (resume == false) {
			removeLastFile();
		}

		if (mark == true) {
			// しおりを保存する
			saveCurrentPage();
		}
		else {
			// しおりを起動時の状態に戻す
			restoreCurrentPage();
		}

		// 履歴保存
		saveHistory(false);
		mHistorySaved = true;

		// ZIPをオープンしていたら閉じる
		mImageMgr.closeFiles();

		// 解放
		mTextMgr.release();
		Intent intent = new Intent();
		intent.putExtra("nextopen", select);
		intent.putExtra("lastfile", mTextName);
		intent.putExtra("lastpath", mLocalPath);
		setResult(RESULT_OK, intent);
		finish();
	}


	private void saveCurrentPage() {
		mCurrentPage = mTextView.getPage();

		// 現在ページ情報を保存
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = sp.edit();
		int savePage = mCurrentPage;
		if (mTextMgr.length() <= mCurrentPage + 1) {
			// 既読
			savePage = -2;
		}
		else if (mDispMode == DEF.DISPMODE_TX_DUAL && mTextMgr.length() <= mCurrentPage + 2) {
			// 見開きの場合は1ページ前でも既読
			savePage = -2;
		}
		else if (savePage < 0) {
			// 範囲外は読み込みしない
			savePage = 0;
		}
		ed.putInt(DEF.createUrl(mFilePath + mTextName, mUser, mPass), savePage);
		ed.commit();
	}

	// 起動時のページ情報に戻す
	private void restoreCurrentPage() {
		if (mImageMgr != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = sp.edit();
			if (mRestorePage == -1) {
				ed.remove(DEF.createUrl(mFilePath + mTextName, mUser, mPass));
			}
			else {
				ed.putInt(DEF.createUrl(mFilePath + mTextName, mUser, mPass), mRestorePage);
			}
			ed.commit();
		}
	}

	// 起動時のページ情報に戻す
	private void saveHistory(boolean isSavePage) {
		if (mReadBreak == false && mTextMgr != null && mTextView != null) {
			int type = (mFileName == null || mFileName.length() == 0) ? RecordItem.TYPE_TEXT : RecordItem.TYPE_COMPTEXT;
			mCurrentPage = mTextView.getPage();
			RecordList.add(RecordList.TYPE_HISTORY, type, mServer, mLocalPath + mFileName
						, mTextName, new Date().getTime(), null, mCurrentPage, null);

			// タスク切り替え時しおりを保存
			if (isSavePage) {
				saveCurrentPage();
			}
		}
	}

	private void saveLastFile() {
		Editor ed = mSharedPreferences.edit();
		ed.putInt("LastServer", mServer);
		ed.putString("LastPath", mLocalPath);
		ed.putString("LastUser", mUser);
		ed.putString("LastPass", mPass);
		ed.putString("LastFile", mFileName);
		ed.putString("LastText", mTextName);
		ed.putInt("LastOpen", DEF.LASTOPEN_TEXT);
		ed.commit();
	}

	private void removeLastFile() {
		Editor ed = mSharedPreferences.edit();
		ed.putInt("LastOpen", DEF.LASTOPEN_NONE);
		ed.commit();
	}

	@Override
	public void onAddBookmark(String name) {
		// ブックマーク追加
		int type = (mFileName == null || mFileName.length() == 0) ? RecordItem.TYPE_TEXT : RecordItem.TYPE_COMPTEXT;
		RecordList.add(RecordList.TYPE_BOOKMARK, type, mServer, mLocalPath + mFileName
				, mTextName, new Date().getTime(), null, mCurrentPage, name);
	}

	private void showCloseDialog(int layout) {
		if (mCloseDialog != null) {
			return;
		}
		mCloseDialog = new CloseDialog(this);
		mCloseDialog.setTitleText(layout);
		mCloseDialog.setCloseListear(new CloseListenerInterface() {
			@Override
			public void onCloseSelect(int select, boolean resume, boolean mark) {
				if (select != CloseDialog.CLICK_CANCEL) {
					finishActivity(select, resume, mark);
				}
			}

			@Override
			public void onClose() {
				// 終了
				mCloseDialog = null;
			}
		});
		mCloseDialog.show();
	}

//	// 長押しタイマーイベント検知処理
//	private final Handler mWaitHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			if (mReadTimerMsg == msg) {
//				// プログレスダイアログを表示
//				if (mReadDialog != null) {
//					mReadDialog.show();
//				}
//			}
//		}
//	};
}
