package src.comitton.imageview;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.dip.muracoro.comittonx.R;
import src.comitton.cropimageview.CropImageActivity;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import src.comitton.config.SetCacheActivity;
import src.comitton.config.SetCommonActivity;
import src.comitton.config.SetConfigActivity;
import src.comitton.config.SetEpubActivity;
import src.comitton.config.SetImageActivity;
import src.comitton.config.SetImageDetailActivity;
import src.comitton.config.SetImageText;
import src.comitton.config.SetImageTextColorActivity;
import src.comitton.config.SetImageTextDetailActivity;
import src.comitton.config.SetNoiseActivity;
import src.comitton.fileview.data.RecordItem;
import src.comitton.dialog.BookmarkDialog;
import src.comitton.dialog.CheckDialog;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.DirTreeDialog;
import src.comitton.dialog.ImageConfigDialog;
import src.comitton.dialog.Information;
import src.comitton.dialog.ListDialog;
import src.comitton.dialog.ToolbarEditDialog;
import src.comitton.dialog.PageSelectDialog;
import src.comitton.dialog.PageThumbnail;
import src.comitton.dialog.BookmarkDialog.BookmarkListenerInterface;
import src.comitton.dialog.CheckDialog.CheckListener;
import src.comitton.dialog.CloseDialog.CloseListenerInterface;
import src.comitton.dialog.ListDialog.ListSelectListener;
import src.comitton.dialog.MenuDialog.MenuSelectListener;
import src.comitton.dialog.ImageConfigDialog.ImageConfigListenerInterface;
import src.comitton.dialog.TabDialogFragment;
import src.comitton.fileview.filelist.RecordList;
import src.comitton.noise.NoiseSwitch;
import src.comitton.common.GuideView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import src.comitton.common.ImageAccess;
import src.comitton.config.SetFileListActivity;
import src.comitton.fileview.data.FileData;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import src.comitton.common.ThumbnailLoader;

/**
 * 画像のスクロールを試すための画面を表します。
 */
@SuppressLint("NewApi")
public class ImageActivity extends AppCompatActivity implements OnTouchListener, Handler.Callback, MenuSelectListener, PageSelectListener, BookmarkListenerInterface {
	private static final String TAG = "ImageActivity";

	private static final int DISPMODE_NORMAL = 0;
	private static final int DISPMODE_DUAL = 1;
	private static final int DISPMODE_HALF = 2;
	private static final int DISPMODE_EXCHANGE = 3;

	public static final int FILESORT_NONE = 0;
	public static final int FILESORT_NAME_UP = 1;
	public static final int FILESORT_NAME_DOWN = 2;

	private static final int HALFPOS_1ST = 1;
	private static final int HALFPOS_2ND = 2;

	private static final int TIME_VIB_TERM = 20;
	private static final int TIME_VIB_RANGE = 30;

	private static final int[] CTL_COUNT = { 1, 1, 2, 99999 }; // 対象のページ数
	private static final int[] CTL_RANGE = { 2, 4, 3, 1 }; // 1ページ選択に必要な移動幅(単位)

	private static final int NOISE_NEXTPAGE = 1;
	private static final int NOISE_PREVPAGE = 2;
	private static final int NOISE_NEXTSCRL = 3;
	private static final int NOISE_PREVSCRL = 4;

	private static final int PAGE_SLIDE = 0;
	private static final int PAGE_INPUT = 1;
	private static final int PAGE_THUMB = 2;

	// 上下の操作領域タッチ後何msでボタンを表示するか
	private static final int LONGTAP_TIMER_UI = 400;
	private static final int LONGTAP_TIMER_BTM = 400;

	private final int mSdkVersion = android.os.Build.VERSION.SDK_INT;

	// 古い設定ファイルとの互換性維持のための番号
	// ここに追加や削除する場合は番号を変更しないこと
	private final int[] COMMAND_INDEX =
	{
			0,	// 画面方向
			1,	// 余白削除
			21,	// 余白削除の色
			22,	// 画像表示設定
			2,	// 見開き設定
			3,	// 画像サイズ
			4,	// 音操作
			5,	// オートプレイ開始
			6,	// ブックマーク追加
			7,	// ブックマーク選択
			8,	// シャープ化
			23,	// 明るさ補正
			24,	// ガンマ補正
			25,	// バックライト
			9,	// 白黒反転
			10,	// グレースケール
			11,	// 画像回転
			12,	// 画像補間方式
			13, // ページ逆順
			14,	// 操作入れ替え
			15,	// 表紙方向
			16,	// スクロール方向入れ替え
			17,	// 上部メニュー設定
			18,	// 設定
			19,	// 中央余白表示
			20	// 中央影表示
	};

	private final int[] COMMAND_ID =
	{
		DEF.MENU_ROTATE,	// 画面方向
		DEF.MENU_MGNCUT,	// 余白削除
		DEF.MENU_MGNCUTCOLOR,	// 余白削除の色
		DEF.MENU_IMGCONF,	// 画像表示設定
		DEF.MENU_IMGVIEW,	// 見開き設定
		DEF.MENU_IMGSIZE,	// 画像サイズ
		DEF.MENU_NOISE,		// 音操作
		DEF.MENU_AUTOPLAY,	// オートプレイ開始
		DEF.MENU_ADDBOOKMARK,// ブックマーク追加
		DEF.MENU_SELBOOKMARK,// ブックマーク選択
		DEF.MENU_SHARPEN,	// シャープ化
		DEF.MENU_BRIGHT,	// 明るさ補正
		DEF.MENU_GAMMA,		// ガンマ補正
		DEF.MENU_BKLIGHT,	// バックライト
		DEF.MENU_INVERT,	// 白黒反転
		DEF.MENU_GRAY,		// グレースケール
		DEF.MENU_IMGROTA,	// 画像回転
		DEF.MENU_IMGALGO,	// 画像補間方式
		DEF.MENU_REVERSE, 	// ページ逆順
		DEF.MENU_CHG_OPE,	// 操作入れ替え
		DEF.MENU_PAGEWAY,	// 表紙方向
		DEF.MENU_SCRLWAY,	// スクロール方向入れ替え
		DEF.MENU_TOP_SETTING,// 上部メニュー設定
		DEF.MENU_SETTING,	// 設定
		DEF.MENU_CMARGIN,	// 中央余白表示
		DEF.MENU_CSHADOW	// 中央影表示
	};
	private final int[] COMMAND_RES =
	{
		R.string.rotateMenu,	// 画面方向
		R.string.mgnCutMenu,	// 余白削除
		R.string.mgnCutColorMenu,	// 余白削除の色
		R.string.imgConfMenu,	// 画像表示設定
		R.string.tguide02,		// 見開き設定
		R.string.tguide03,		// 画像サイズ
		R.string.noiseMenu,		// 音操作
		R.string.playMenu,		// オートプレイ開始
		R.string.addBookmarkMenu,// ブックマーク追加
		R.string.selBookmarkMenu,// ブックマーク選択
		R.string.sharpenMenu,	// シャープ化
		R.string.brightMenu,	// 明るさ補正
		R.string.gammaMenu,		// ガンマ補正
		R.string.bklightMenu,	// バックライト
		R.string.invertMenu,	// 白黒反転
		R.string.grayMenu,		// グレースケール
		R.string.imgRotaMenu,	// 画像回転
		R.string.algoriMenu,	// 画像補間方式
		R.string.reverseMenu,	// ページ逆順
		R.string.chgOpeMenu,	// 操作入れ替え
		R.string.pageWayMenu,	// 表紙方向
		R.string.scrlWay2Menu,	// スクロール方向入れ替え
		R.string.setTopMenu,	// 上部メニュー設定
		R.string.setMenu,		// 設定
		R.string.cMargin,		// 中央余白表示
		R.string.cShadow		// 中央影表示
	};
	private int[] mCommandId;
	private String[] mCommandStr;

	private int RANGE_FLICK;

	// public static final int FLICK_NONE = 0;
	// public static final int FLICK_RIGHTNEXT = 1;
	// public static final int FLICK_LEFTNEXT = 2;

	private final int EVENT_READTIMER = 200;
	private final int EVENT_EFFECT = 201;
	private final int EVENT_SCROLL = 202;
	private final int EVENT_LOADING = 203;
	private final int EVENT_AUTOPLAY = 204;
	private final int EVENT_TOUCH_ZOOM = 205;
	private final int EVENT_TOUCH_TOP = 206;
	private final int EVENT_TOUCH_BOTTOM = 207;

	private final int SELLIST_ALGORITHM = 0;
	private final int SELLIST_IMG_ROTATE = 1;
	private final int SELLIST_VIEW_MODE = 2;
	private final int SELLIST_SCALE_MODE = 3;
	private final int SELLIST_MARGIN_CUT = 4;
	private final int SELLIST_MARGIN_CUTCOLOR = 5;
	private final int SELLIST_SCR_ROTATE = 6;

	private final int TOUCH_NONE      = 0;
	private final int TOUCH_COMMAND   = 1;
	private final int TOUCH_OPERATION = 2;

	private final int SCALENAME_ORDER[] = { 0, 1, 6, 2, 3, 7, 4, 5 };

	// 設定値の保持
	private int mClickArea = 16;
	private int mPageRange = 16;
	private int mScroll = 5;
	private int mMoveRange = 12;
	private int mLongTapZoom = 800; // 長押し時間
	private int mCenter = 0;
	private int mShadow = 0;
	private int mFileSort = FILESORT_NAME_UP;
	// private int mOrgWidth = DEF.DEFAULT_ORGWIDTH;
	// private int mOrgHeight = DEF.DEFAULT_ORGHEIGHT;
	private int mViewPoint;
	private int mMargin;
	private int mMgnColor;
	private int mCenColor;
	private int mTopColor1;
	private int mTopColor2;
	private int mZoomType = 0; // 拡大方法
	private int mPageWay = DEF.PAGEWAY_RIGHT;
	private int mMemSize;
	private int mMemNext;
	private int mMemPrev;
	private int mVolKeyMode;
	private int mViewRota;
	private int mRotateBtn;
	private int mVolScrl;
	private int mScrlWay;
	private int mScrlRngW;
	private int mScrlRngH;
	private int mMgnCut;
	private int mMgnCutColor;
	private int mEffect;
	private int mLastMsg;
	private int mPageSelect;
	private int mMomentMode;
	private int mSharpen;
	private int mBright;
	private int mGamma;
	private int mBkLight;
	private int mMaxThread;
	private boolean mOldMenu;
	private int mLoupeSize;

	private boolean mHidden;
	private boolean mDelShare;
	private boolean mFlickPage;
	private boolean mReverseOrder;

	private int mNoiseScrl;
	private int mNoiseUnder;
	private int mNoiseOver;
	private int mNoiseDec;
	private boolean mNoiseLevel;

	private int mViewWidth;
	private int mViewHeight;

	private boolean mNotice = false;
	private boolean mNoSleep = false;
	private boolean mChgPage = false;
	private boolean mChgFlick = false;
	// private boolean mTwice = false;
	// private boolean mResumeOpen;
	private boolean mConfirmBack;
	private boolean mFitDual = true;
	private boolean mCMargin = false;
	private boolean mCShadow = false;
	private boolean mPrevRev = false;
	private boolean mNoExpand = true;
	private boolean mVibFlag = false;
	private boolean mPseLand = false;
	// private boolean mHalfHeight = false;
	// private boolean mScaleBmp = false;
	// private boolean mBackMode = false;
	private boolean mAccessLamp = true;
	private boolean mTapScrl = false;
	private boolean mInvert;
	private boolean mGray;
	private boolean mMoire;
	private boolean mTopSingle;
	private boolean mSavePage;
	private boolean mFlickEdge;
	private boolean mIsConfSave;
	private boolean mScrlNext = false; // スクロールで前後のページへ移動
	private boolean mViewNext = false; // 次のページを表示
	private boolean mNextFilter = true;
	private boolean mChgPageKey = false;
	private boolean mEpubOrder = false;
	private boolean mEpubThumb = false;

	// ファイル情報
	/** 選択したサーバのインデックス */
	private int mServer;
	/** URI(サーバのTOPディレクトリ) */
	private String mURI = "";
	private String mUser = "";
	private String mPass = "";
	/** ベースURIからの相対パス名 */
	private String mPath = "";
	/** ZIP指定時 */
	private String mFileName = "";
	/** 画像直接指定時はファイル/ExpandActivityから開いた時はZIP内部のファイル */
	private String mImageName = "";
	/** URIとパス */
	private String mUriPath = "";
	/** URIとパスと圧縮ファイル */
	private String mFilePath = "";

	private ImageManager mImageMgr = null;

	// ページ表示のステータス情報
	private int mRestorePage;
	private int mCurrentPage;
	private int mNextPage;
	private boolean mPageSelecting;
	private int mSelectPage = 0;
	private boolean mCurrentPageHalf = false;
	private boolean mCurrentPageDual = false;
	private int mHalfPos = HALFPOS_1ST;
	private boolean mPageBack = false;
	private int mRotate = 0; // 回転角度(0～359)
	private int mInitFlg = 0; // 初期表示の制御用フラグ

	// 画像の表示制御情報
	private int mScaleMode;
	private int mDispMode;
	private int mAlgoMode;

	// 表示文言
	private String mLoadErrStr;

	// 画面を構成する View の保持
	private MyImageView mImageView = null;
	private GuideView mGuideView = null;
	private boolean mKeepGuide = false;
	// フリック判定用
	// private long mInTime1;
	// private long mInTime2;
	// private long mTouchTime;
	// private Point mInPoint1 = new Point();
	// private Point mInPoint2 = new Point();

	// 画面タッチの制御
	private float mTouchBeginX; // 開始x座標
	private float mTouchBeginY; // 開始y座標
	// private float mTouchFirstX; // 開始x座標
	private int mTouchDrawLeft;
	private int mOperation; 	// 操作種別
	private boolean mTouchFirst = false; // タッチ開始後リミットを超えて移動していない
	private boolean mPageMode = false; // ページ選択中の操作エリア外フラグ
	private boolean mPageModeIn = false; // ページ選択中の操作エリア外フラグ
	private boolean mTopMode = false; // トップ操作モード
	private boolean mPinchOn = false;
	private boolean mPinchDown = false;
	private int mPinchScale = 100;
	private int mPinchScaleSel;
	private int mPinchCount;
	private long mPinchTime;
	private int mPinchRange;
	private int mTapPattern;
	private int mTapRate;
	private boolean mVerticalSwipe = false;

	private final int MAX_TOUCHPOINT = 4;
	private final int TERM_MOMENT = 200;
	private int mTouchPointNum;
	private PointF mTouchPoint[];
	private long mTouchPointTime[];

	private boolean mPnumDisp;
	private int mPnumFormat;
	private int mPnumPos;
	private int mPnumSize;
	private int mPnumColor;

	private boolean mTimeDisp;
	private int mTimeFormat;
	private int mTimePos;
	private int mTimeSize;
	private int mTimeColor;

	// サムネイルページ選択用
	private long mThumID;

	// ビットマップの保持
	private final ImageData[] mSourceImage = { null, null };
	// private boolean mIsLandscape = false;

	private long mPrevVibTime = 0;
	// private long mPrevScrollTime = 0;

	// ビットマップ読み込みスレッドの制御用
	private Handler mHandler;
	private BmpLoad mBmpLoad;
	private Thread mBmpThread;
	private boolean mBitmapLoading = false;
	private boolean mLoadingNext = false;

	private ZipLoad mZipLoad;
	private Thread mZipThread;

	// long touch timer
	private boolean mLongTouchMode = false;
	private int mLongTouchCount = 0;

	private int mWAdjust = 100;
	private int mWidthScale = 100;
	private int mImgScale = 100;

	private Vibrator mVibrator;

	private boolean mTerminate = false;
	private boolean mImageLoading = false; //
	private boolean mListLoading = false; //
	private boolean mReadBreak;
	private boolean mFinishActivity;

	private Information mInformation;
	private ProgressDialog mReadDialog;
	private String[] mReadingMsg;
	private Message mReadTimerMsg;

	private NoiseSwitch mNoiseSwitch = null;
	private int mNoiseScroll = 0;
	private boolean mScrolling = false;

	private boolean mAutoPlay;
	private int mAutoPlayTerm = 1000;

	private float mEffectRate = 0;
	private long mEffectStart = 0;
	private int mEffectTime;
	private boolean mImmEnable;
	private boolean mBottomFile;
	private boolean mPinchEnable;

	private final int EFFECT_TERM = 1;
	private final int SCROLL_TERM = 4;
	private final int LOADING_TERM_START = 500;
	private final int LOADING_TERM = 150;

	private ImageActivity mActivity;
	private SharedPreferences mSharedPreferences;
	private float mSDensity;
	private int mImmCancelRange;
	private boolean mImmCancel;

	private ImageConfigDialog mImageConfigDialog;
	private CloseDialog mCloseDialog;
	private ListDialog mListDialog;
	private CheckDialog mCheckDialog;
	private TabDialogFragment mMenuDialog;

	private PageSelectDialog mPageDlg = null;
	private PageThumbnail mThumbDlg = null;

	private int mResult = 0;
	/**
	 * 画面が作成された時に発生します。
	 *
	 * @param savedInstanceState
	 *            保存されたインスタンスの状態。
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "onCreate: 開始します.");}

		// 回転
		mInitFlg = 0;
		mDispMode = DISPMODE_NORMAL;
		mBitmapLoading = false;
		mLoadingNext = false;
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mScaleMode = DEF.SCALE_ORIGINAL;
		mReverseOrder = false;
		mTerminate = false;
		mImageLoading = false;
		mListLoading = false;
		mHandler = new Handler(this);
		mActivity = this;
		mNextPage = -1;
		mIsConfSave = true;

		// ダイアログは初期化
		PageThumbnail.mIsOpened = false;
		PageSelectDialog.mIsOpened = false;

		// 慣性スクロール用領域初期化
		mTouchPointNum = 0;
		mTouchPoint = new PointF[MAX_TOUCHPOINT];
		mTouchPointTime = new long[MAX_TOUCHPOINT];
		for (int i = 0; i < MAX_TOUCHPOINT; i++) {
			mTouchPoint[i] = new PointF();
		}
		mImmCancelRange = (int)(getResources().getDisplayMetrics().density * 6);
		mSDensity = getResources().getDisplayMetrics().scaledDensity;
		RANGE_FLICK = (int) (50 * mSDensity);

		super.onCreate(savedInstanceState);

		// タイトル非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 設定の読み込み
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SetCommonActivity.loadSettings(mSharedPreferences);
		ReadSetting(mSharedPreferences);
		if (mNotice) {
			// 通知領域非表示
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if (mNoSleep) {
			// スリープしない
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		Resources res = getResources();
		mLoadErrStr = res.getString(R.string.loaderr);
		mReadingMsg = new String[3];
		mReadingMsg[0] = res.getString(R.string.reading);
		mReadingMsg[1] = res.getString(R.string.epubParsing);
		mReadingMsg[2] = res.getString(R.string.htmlParsing);

		mImageView = new MyImageView(this);
		mGuideView = new GuideView(this);
		FrameLayout layout = new FrameLayout(this);
		layout.addView(mImageView);
		setContentView(layout);

		mImageView.setFocusable(true);
		mImageView.setGuideView(mGuideView);

		// 色とサイズを指定
		mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
		mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);

		// 上部メニューの設定を読み込み
		loadTopMenuState();
		if (mGuideView != null) {
			// 上部メニューの文字列情報をガイドに設定
			mGuideView.setTopCommandStr(mCommandStr);
			// 時刻＆バッテリー表示の情報をガイドに設定
			mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
		}

		setViewConfig();

		// Intentを取得する
		Intent intent = getIntent();
		// Intentに保存されたデータを取り出す
		mServer = intent.getIntExtra("Server", -1);	// サーバ選択番号
		//mHost = intent.getStringExtra("Uri");
		mURI = intent.getStringExtra("Uri");					// ベースディレクトリのuri
		mPath = intent.getStringExtra("Path");				// ベースURIからの相対パス名
		mUser = intent.getStringExtra("User");				// SMB認証用
		mPass = intent.getStringExtra("Pass");				// SMB認証用
		mFileName = intent.getStringExtra("File");			// ZIP指定時
		mImageName = intent.getStringExtra("Image"); 			// 画像直接指定時はファイル/ExpandActivityから開いた時はZIP内部のファイル
		if (debug) {Log.d(TAG, "onCreate: mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
				+ ", mUser=" + mUser + ", mPass=" + mPass
				+ ", mFileName=" + mFileName + ", mImageName=" + mImageName);}

		if (mPath == null) {
			return;
		}

		// 最後に保存したファイル用
		mUriPath = DEF.relativePath(mActivity, mURI, mPath);
		if (mFileName != null) {
			mFilePath = DEF.relativePath(mActivity, mUriPath, mFileName);
		}
		if (debug) {Log.d(TAG, "onCreate: mUriPath=" + mUriPath + ", mFilePath=" + mFilePath);}

		saveLastFile();

		mRestorePage = intent.getIntExtra("Page", DEF.PAGENUMBER_READ);
		if (mRestorePage == DEF.PAGENUMBER_READ) {
			mRestorePage = mSharedPreferences.getInt(DEF.createUrl(mFilePath, mUser, mPass), DEF.PAGENUMBER_UNREAD);
		}
		mCurrentPage = mRestorePage != DEF.PAGENUMBER_UNREAD ? mRestorePage : 0;
		mImageView.setOnTouchListener(this);

		// プログレスダイアログ準備
		mReadBreak = false;

		mReadDialog = new ProgressDialog(this, R.style.MyDialog);
		mReadDialog.setMessage(mReadingMsg[0] + " (0)");
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
				if (mImageMgr != null) {
					mImageMgr.setBreakTrigger();
				}
				mTerminate = true;
				mReadBreak = true;
			}
		});

		mListLoading = true;
		mZipLoad = new ZipLoad(mHandler, this);
		mZipThread = new Thread(mZipLoad);
		mZipThread.start();

		// WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		// WindowManager.LayoutParams params = new WindowManager.LayoutParams(
		// 120, 120,
		// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
		// WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
		// WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
		// WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
		// WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
		// PixelFormat.TRANSLUCENT);
		// params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		//
		// View iv = new View(this);
		// iv.setBackgroundColor(0xC0000000);
		// wm.addView(iv, params);
		return;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String path = intent.getStringExtra("Path");
		String server = intent.getStringExtra("Server");
		Log.d(TAG, "onNewIntent: path:" + path + ", server:" + server);
		intent.putExtra("NewIntent", true);
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * @Override アクティビティ一時停止時に呼び出される
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (!mFinishActivity && mSavePage && !mReadBreak) {
			saveCurrentPage();
		}
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
	}

	/**
	 * @Override アクティビティ停止時に呼び出される
	 */
	@Override
	protected void onStop() {
		super.onStop();

		if (!mFinishActivity) {
			saveHistory();
		}

		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(true);
		}
	}

	/**
	 * @Override アクティビティ再開時に呼び出される
	 */
	@Override
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

	public class ZipLoad implements Runnable {
		private Handler handler;
		private AppCompatActivity mActivity;

		public ZipLoad(Handler handler, ImageActivity activity) {
			super();
			this.handler = handler;
			this.mActivity = activity;
		}

		public void run() {
			boolean debug = false;
			// ファイルリストの読み込み
			mImageMgr = new ImageManager(this.mActivity, mUriPath, mFileName, mUser, mPass, mFileSort, handler, mHidden, ImageManager.OPENMODE_VIEW, mMaxThread);
			if(debug) {Log.d(TAG, "run メモリ利用状況.\n" + getMemoryString());}
			setMgrConfig(true);
			mImageMgr.LoadImageList(mMemSize, mMemNext, mMemPrev);
			if(debug) {Log.d(TAG, "run メモリ利用状況.(2回目)\n" + getMemoryString());}
			// mImageMgr.setConfig(mScaleMode, mCenter, mFitDual, mDispMode,
			// mNoExpand, mAlgoMode, mRotate, mWAdjust, mImgScale, mPageWay,
			// mMgnCut);
			mImageMgr.setViewSize(mViewWidth, mViewHeight);
			mImageView.setImageManager(mImageMgr);

			// 終了通知
			Message message = new Message();
			message.what = DEF.HMSG_READ_END;
			handler.sendMessage(message);
		}
	}

	/**
	 * @Override アクティビティ終了時に呼び出される
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordStop();
			mNoiseSwitch = null;
		}

		if (mSourceImage[0] != null) {
			mSourceImage[0] = null;
		}
		if (mSourceImage[1] != null) {
			mSourceImage[1] = null;
		}
		if (mImageView != null) {
			mImageView.setImageBitmap(mSourceImage);
		}
		System.gc();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		/*
		 * if (hasFocus) { if (mInitFlg == 0) { // 起動直後のみ呼び出し mInitFlg = 1;
		 *
		 * // ビットマップの設定 mPageBack = false; setBitmapImage(); } }
		 */
		// プログレスダイアログの設定
		if (mInitFlg == 0) {
			mInitFlg = 1;
			startDialogTimer(100);
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
		if (mWAdjust != 100) {
			mImageView.setImageBitmap(null);
		}
		super.onConfigurationChanged(newConfig);

		// 画面方向を保持
		// Configuration config = getResources().getConfiguration();
		// mIsLandscape = (config.orientation ==
		// Configuration.ORIENTATION_LANDSCAPE);

		if (mBitmapLoading) {
			return;
		}
		if (mInitFlg == 0) {
			// // 起動直後のみ呼び出し
			// mInitFlg = false;
			//
			// // ビットマップの設定
			// setBitmapImage();
		}
		else {
			// // 縦横で単ページと見開き切替える場合
			// if (mDispMode == DISPMODE_EXCHANGE) {
			// mImageView.updateScreenSize();

			// // イメージ拡大縮小
			// ImageScaling();
			// updateOverSize();
			// setBitmapImage();
			// return;
			// }

			// 幅調整なら回転時に再読み込み
			if (mWAdjust != 100) {
				setBitmapImage();
			}

			// 2011/11/26 Viewのサイズ変更で処理する
			// this.updateOverSize();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean debug = false;
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mAutoPlay) {
				// オートプレイ中は解除
				setAutoPlay(false);
			}

			int code = event.getKeyCode();
			switch (code) {
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_DPAD_LEFT: {
					// カーソル左右でページ遷移
					if ((code == (!mChgPageKey ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT) && mPageWay == DEF.PAGEWAY_RIGHT) ||
						(code == (!mChgPageKey ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT) && mPageWay != DEF.PAGEWAY_RIGHT)) {
						// 次ページへ
						nextPage();
					}
					else {
						// 前ページへ
						prevPage();
					}
					break;
				}
				case KeyEvent.KEYCODE_DPAD_UP: {
					// 前ページへずらす
					shiftPage(-1);
					break;
				}
				case KeyEvent.KEYCODE_DPAD_DOWN: {
					// 次ページへずらす
					shiftPage(1);
					break;
				}
				case KeyEvent.KEYCODE_MENU: {
					// 独自メニュー表示
					openMenu();
					return true;
				}
				case KeyEvent.KEYCODE_DEL:
				case KeyEvent.KEYCODE_BACK: {
					operationBack();
					return true;
				}
				case KeyEvent.KEYCODE_VOLUME_DOWN:
				case KeyEvent.KEYCODE_VOLUME_UP: {
					// ボリュームモード
					if (mVolKeyMode == DEF.VOLKEY_NONE) {
						// Volキーを使用しない
						break;
					}

					int move = mVolKeyMode == DEF.VOLKEY_DOWNTONEXT ? 1 : -1;
					if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
						move *= -1;
					}
					// 読込中の表示
					startScroll(move);
					return true;
				}
				case KeyEvent.KEYCODE_SPACE: {
					int meta = event.getMetaState();
					int move = (meta & KeyEvent.META_SHIFT_ON) == 0 ? 1 : -1;
					// 読込中の表示
					startScroll(move);
					return true;
				}
				case KeyEvent.KEYCODE_CAMERA:
				case KeyEvent.KEYCODE_FOCUS: {
					if (mRotateBtn == 0) {
						break;
					}
					else if (event.getKeyCode() != mRotateBtn) {
						return true;
					}
					if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota == DEF.ROTATE_LANDSCAPE) {
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
					}
					break;
				}
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
					if (mVolKeyMode == DEF.VOLKEY_NONE) {
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

	String mMessage = "";
	String mMessage2 = "";
	String mWorkMessage = "";
	// スレッドからの通知取得
	public boolean handleMessage(Message msg) {
		boolean debug = false;

		if (DEF.ToastMessage(mActivity, msg)) {
			// HMSG_TOASTならトーストを表示して終了
			return true;
		}

		if (mReadTimerMsg == msg) {
			// プログレスダイアログを表示
			synchronized (this) {
				if (mReadDialog != null) {
					if (mImmEnable && mSdkVersion >= 19) {
						mReadDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
						mReadDialog.show();
						mReadDialog.getWindow().getDecorView().setSystemUiVisibility(this.getWindow().getDecorView().getSystemUiVisibility());
						mReadDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
					} else {
						mReadDialog.show();
					}
				}
			}
			return true;
		}

		// スクロールタイマーイベント検知処理
		// 次のイベント時間
		long NextTime = SystemClock.uptimeMillis();
		boolean nextEvent = false;

		switch (msg.what) {
			case DEF.HMSG_RECENT_RELEASE:
				// 最新バージョンを表示
				mInformation.showRecentRelease();
				return true;

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

			case DEF.HMSG_EPUB_PARSE:
			case DEF.HMSG_HTML_PARSE:
				// 読込中の表示
				synchronized (this) {
					if (mReadDialog != null) {
						// ページ読み込み中
						String str = "";
						if (msg.what == DEF.HMSG_EPUB_PARSE) {
							str = mReadingMsg[1];
							mMessage = str;
							mReadDialog.setMessage(str);
						}
						else if (msg.what == DEF.HMSG_HTML_PARSE) {
							str = mReadingMsg[2];
							mMessage = MessageFormat.format("{0} [{1}/{2}]", new Object[]{str, msg.arg1, msg.arg2});
						}
						mReadDialog.setMessage(DEF.ProgressMessage(mMessage, mMessage2, mWorkMessage));
					}
				}
				return true;

			case DEF.HMSG_SUB_MESSAGE:
				// 読込中の表示
				synchronized (this) {
					if (mReadDialog != null) {
						mMessage2 = MessageFormat.format("{0} ({1})", new Object[]{msg.obj.toString(), msg.arg1});
						mReadDialog.setMessage(DEF.ProgressMessage(mMessage, mMessage2, mWorkMessage));
					}
				}
				return true;

			case DEF.HMSG_WORKSTREAM:
				// 読込中の表示
				synchronized (this) {
					if (mReadDialog != null) {
						mWorkMessage = msg.obj.toString();
						mReadDialog.setMessage(DEF.ProgressMessage(mMessage, mMessage2, mWorkMessage));
					}
				}
				return true;

			case EVENT_TOUCH_ZOOM:
//				Log.d("long touch", "handle : msg=" + msg.what + ", arg1=" + msg.arg1 + ", count" + mLongTouchCount);

				if (mLongTouchCount == msg.arg1) {
					// 最新のタイマーの時だけ処理
					if (mTouchFirst) {
						if (mVibFlag) {
							// 振動
							mVibrator.vibrate(TIME_VIB_RANGE);
						}

						// タッチ位置が範囲内の時だけ処理
						mLongTouchMode = true;
						mImageView.setZoomMode(true);
					}
				}
				return true;
			case EVENT_TOUCH_TOP:
			case EVENT_TOUCH_BOTTOM:
//				Log.d("long touch", "handle : msg=" + msg.what + ", arg1=" + msg.arg1 + ", count" + mLongTouchCount);

				if (mLongTouchCount == msg.arg1) {
					// 最新のタイマーの時だけ処理
					if (mTouchFirst) {
						// 上部の操作エリア
						mGuideView.eventTouchTimer();
					}
				}
				return true;

			case EVENT_EFFECT: {
				if (debug) {Log.d(TAG, "handleMessage: EVENT_EFFECT:");}
				// 稼働中のみ次のイベント登録
				int t = (int) (NextTime - mEffectStart);
				if (t >= mEffectTime) {
					// mEffectTimeミリ秒を超えている
					mEffectRate = 0.0f;
				}
				else {
					// mEffectTimeミリ秒未満
					mEffectRate = ((float) (mEffectTime - t)) / (float) mEffectTime * (mEffectRate > 0 ? 1.0f : -1.0f);
				}
				if (mEffectRate == 0.0f) {
					// エフェクトの終わりには以前のページ表示をしない
					mImageView.createBackground(false);
				}
				// エフェクト位置
				mImageView.setEffectRate(mEffectRate);

				if (debug) {Log.d(TAG, "handleMessage: EVENT_EFFECT: mEffectRate=" + mEffectRate);}
				if (mEffectRate != 0.0f) {
					// エフェクト中は次のイベントを登録
					nextEvent = true;
					NextTime += EFFECT_TERM;
				}
				else {
					// エフェクト無しのときはそのまま修了
					mLoadingNext = true;
					if (debug) {Log.d(TAG, "handleMessage: EVENT_EFFECT: SET mBitmapLoading = false");}
					mBitmapLoading = false;
					mImageLoading = false;
					if (mAutoPlay) {
						startViewTimer(EVENT_AUTOPLAY);
					}
				}
				break;
			}
			case EVENT_SCROLL: {
				// スクロールで移動
				if (mImageView.moveToNextPoint(mVolScrl)) {
					// エフェクト中は次のイベントを登録
					nextEvent = true;
					NextTime += SCROLL_TERM;
				}
				else {
					mScrolling = false;
					if (mAutoPlay) {
						startViewTimer(EVENT_AUTOPLAY);
					}
				}
				break;
			}
			case EVENT_LOADING: {
				// イメージをローディング中
				if (mImageLoading && mEffectRate == 0.0f) {
					mGuideView.countLoading(true);
					nextEvent = true;
					NextTime += LOADING_TERM;
				}
				break;
			}
			case EVENT_AUTOPLAY: {
				// スクロールで移動
				if (mAutoPlay) {
					startScroll(1);
				}
				break;
			}
			// case EVENT_NOISE:
			// // スクロールで移動
			// if (!mImageView.moveToNextPoint(mNoiseScrl)) {
			// if (!mImageView.setViewPosScroll(mNoiseScroll)) {
			// // スクロール開始
			// mNoiseScroll = 0;
			// }
			// }
			// // エフェクト中は次のイベントを登録
			// if (mNoiseScroll != 0) {
			// nextEvent = true;
			// NextTime += SCROLL_TERM;
			// }
			// break;
			case DEF.HMSG_LOAD_END: // 画像読み込み終了
				if (debug) {Log.d(TAG, "handleMessage: HMSG_LOAD_END:");}
				if (mBitmapLoading) {
					if (debug) {Log.d(TAG, "handleMessage: HMSG_LOAD_END: IF mBitmapLoading == true");}
					// Loading中を消去
					if (mSourceImage[0] != null) {
						mGuideView.setLodingState();
					}
					else {
						mGuideView.setLodingState(mLoadErrStr);
					}

					// ビットマップを設定
					synchronized (mImageView) {
						mImageView.setImageBitmap(mSourceImage);

						// 表示中の画像が1枚か2枚かを判定
						int shareType;
						if (mSourceImage[0] != null && mSourceImage[1] != null) {
							shareType = DEF.SHARE_LR;
						}
						else {
							shareType = DEF.SHARE_SINGLE;
						}

						// ページ番号入力が開いていたら共有タイプを設定
						if (PageSelectDialog.mIsOpened) {
							mPageDlg.setShareType(shareType);
						}
						// サムネイルページ選択が開いていたら共有タイプを設定
						if (PageThumbnail.mIsOpened) {
							mThumbDlg.setShareType(shareType);
						}

						// 2011/11/18 ルーペ機能
						this.updateOverSize(false);
					}
					if (mTerminate) {
						finish();
					}

					if (mEffect != 0 && !mPageSelecting) {
						// エフェクト開始
						// 次のページ遷移が予約されている場合はエフェクトしない
						if (debug) {Log.d(TAG, "handleMessage: HMSG_LOAD_END: mEffect=" + mEffect +", mPageSelecting=" + mPageSelecting);}
						if (debug) {Log.d(TAG, "handleMessage: HMSG_LOAD_END: startViewTimer(EVENT_EFFECT)");}
						startViewTimer(EVENT_EFFECT);
					}
					else {
						// 以前のページ表示を終了
						mImageView.createBackground(false);

						// エフェクト無しのときはそのまま終了
						mLoadingNext = true;
						if (debug) {Log.d(TAG, "handleMessage: HMSG_LOAD_END: SET mBitmapLoading = false");}
						mBitmapLoading = false;
						mImageLoading = false;
						mPageSelecting = false;
						if (mAutoPlay) {
							startViewTimer(EVENT_AUTOPLAY);
						}
					}
					mGuideView.countLoading(false);

					String pagenum = null;
					if (mPnumDisp) {
						if (mPnumFormat == 0 || mSourceImage[1] == null) {
							pagenum = (mCurrentPage + 1) + " / " + mImageMgr.length();
						}
						else {
							pagenum = (mCurrentPage + 1) + " - " + (mCurrentPage + 2) + " / " + mImageMgr.length();
						}
					}
					mGuideView.setPageNumberFormat(mPnumDisp, mPnumFormat, mPnumPos, mPnumSize, mPnumColor);
					mGuideView.setPageNumberString(pagenum);

					// 現在ページを保存
					if (mSavePage) {
						saveCurrentPage();
					}
					if (mNextPage != -1 && !mBitmapLoading) {
						// ページ選択
						if (mCurrentPage != mNextPage) {
							Log.d("PageSelect", "current:" + mCurrentPage + ", next:" + mNextPage + " (LoadEnd)");
							mCurrentPage = mNextPage;

							// ページ変更時に振動
							startVibrate();
							mPageBack = false;
							setBitmapImage();
							mPageSelecting = true;
						}
						mNextPage = -1;
					}
				}
				return true;

			case DEF.HMSG_ERROR:
				// 読込中の表示
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				return true;

			case DEF.HMSG_CACHE:
				// アクセス状態表示フラグ
				if (mAccessLamp) {
					// 読込中の表示
					int mark;
					if (msg.arg1 < 0) {
						mark = 0;
					}
					else {
						mark = msg.arg2;
					}
					mGuideView.setCacheMark(mark);
				}
				return true;

			case DEF.HMSG_NOISESTATE:
				// 状態表示
				if (mNoiseSwitch != null) {
					mGuideView.setNoiseState(msg.arg1, mNoiseLevel ? msg.arg2 : -1);
				}
				return true;

			case DEF.HMSG_NOISE:
				// 読込中の表示
				if (msg.arg1 == NOISE_NEXTPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
					// else
					startScroll(1);
				}
				else if (msg.arg1 == NOISE_PREVPAGE) {
					if (mNoiseScroll != 0) {
						// スクロール停止
						mNoiseScroll = 0;
					}
					// else
					startScroll(-1);
				}
				else if (msg.arg1 == NOISE_NEXTSCRL || msg.arg1 == NOISE_PREVSCRL) {
					int way = 1;
					if (msg.arg1 == NOISE_PREVSCRL) {
						way = -1;
					}

					// 読込中の表示
					if (mImageView.checkScrollPoint() && (mNoiseScroll != 0 && way == mNoiseScroll)) {
						mImageView.moveToNextPoint(mNoiseScrl);
					}
					else {
						mNoiseScroll = way;
						// 次のポイントへスクロール開始
						if (mImageView.setViewPosScroll(mNoiseScroll)) {
						}
					}
				}
				return true;

			case DEF.HMSG_LOADING:
				// アクセス状態表示フラグ
				if (mAccessLamp) {
					// 読み込み済みデータサイズの表示
					mGuideView.setLodingState((msg.arg1 >> 8) & 0xFF, msg.arg1 & 0xFF, msg.arg1 >> 24, msg.arg2);
				}
				return true;

			case DEF.HMSG_READ_END:
				// 読込中の表示
				if (mReadDialog != null) {
					mReadDialog.dismiss();
					mReadDialog = null;
				}
				mListLoading = false;
				if (mTerminate) {
					finish();
				}

				if (mImageName != null && !mImageName.isEmpty()) {
					int page = mImageMgr.search(mImageName);
					if (page != -1) {
						mCurrentPage = page;
					}
				}

				// 既読の場合は最終ページ
				if (mImageMgr.length() == 0) {
					mCurrentPage = 0;
				}
				else if (mCurrentPage < 0) {
					mCurrentPage = mImageMgr.length() - 1;
				}
				else if (mCurrentPage >= mImageMgr.length()) {
					mCurrentPage = mImageMgr.length() - 1;
				}

				mThumID = System.currentTimeMillis();
				mPageBack = false;
				setBitmapImage();
				break;
		}
		if (nextEvent) {
			// 次のイベントあり
			if (debug) {Log.d(TAG, "handleMessage: 次のイベントを実行: msg.what=" + msg.what);}
			msg = mHandler.obtainMessage(msg.what);
			mHandler.sendMessageAtTime(msg, NextTime);
		}
		return false;
	}

	/**
	 * @Override アクティビティ初回起動時や再開時に画面が表示される時に呼び出される
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (mNoiseSwitch != null) {
			mNoiseSwitch.recordPause(false);
		}
		 if (mImageView != null) {
			 mImageView.update(true);
		 }
	}

	public void setBitmapImage() {
		boolean debug = false;
		if (mImageMgr == null || mImageMgr.length() - 1 < mCurrentPage) {
			return;
		}

		// イメージの場合
		if (mBitmapLoading) {
			return;
		}
		if (debug) {Log.d(TAG, "setBitmapImage: SET mBitmapLoading = true");}
		mBitmapLoading = true;

		mImageView.lockDraw();
		mImageView.createBackground(true);
		mImageView.update(true);

		// mImageView.lockDraw();
		// 旧ビットマップを解放
		mSourceImage[0] = null;
		mSourceImage[1] = null;
		// 解放
		mImageView.setImageBitmap(mSourceImage);

		mCurrentPageHalf = false;
		mCurrentPageDual = false;
		if (!isDualView() && mCurrentPage < 0) {
			// 範囲外は読み込みしない
			mCurrentPage = 0;
		}

		// // Loadingのダイアログを表示
		if (mTerminate) {
			finish();
		}
		mImageLoading = true;
		mBmpLoad = new BmpLoad(mHandler, DEF.HMSG_LOAD_END);
		mBmpThread = new Thread(mBmpLoad);
		mBmpThread.start();

		startViewTimer(EVENT_LOADING);
		return;
	}

	public class BmpLoad implements Runnable {
		private Handler handler;
		private int mLoadEndMsg;

		public BmpLoad(Handler handler, int msg) {
			super();
			this.handler = handler;
			mLoadEndMsg = msg;
		}

		public void run() {
			// 0: 現在のページ, 1: 2ページ目, 2: 前のページ, 3: 前の2ページ目, 4: 次のページ, 5: 次の2ページ目
			//ImageData bm[] = { null, null, null, null, null, null };

			ImageData bm[] = { null, null };

			// 仮に現在ページを設定
			mImageMgr.setCurrentPage(mCurrentPage, false);

			// 並べて表示以外は1回ループ
			for (int i = 0; i < 2; i++) {
				// 並べて表示以外は1回のみ読込
				if (!isDualView() && i == 1) {
					break;
				}

				int idx;

				// 並べて表示かつページ戻りのときは2ページ目から読込み
				if (isDualView() && mPageBack) {
					idx = (i == 0) ? 1 : 0;
				}
				else {
					idx = i;
				}

				int page = mCurrentPage + idx;

				if (page >= mImageMgr.length() || page < 0) {
					// 範囲外は読み込みしない
					continue;
				}

				// ビットマップのロード
				bm[idx] = loadBitmap(page, true);
				if (bm[idx] != null && isDualView()) {
					if (i == 0) {
						if (!DEF.checkPortrait(bm[idx].Width, bm[idx].Height, mRotate)) {
							// 横長だったので次は読み込まない
							break;
						}
					}
					else if (i == 1) {
						// 2ループ目
						// 2枚目の横長チェック
						// 2つ目のBitmapが横長の場合は読み込まない
						if (!DEF.checkPortrait(bm[idx].Width, bm[idx].Height, mRotate)) {
							// 横長だったので使用しない
							bm[idx] = null;
						}
					}
				}
			}

			if (bm[1] != null && bm[0] == null) {
				// 片側しか読み込んでいない場合はbm[0]に移す
				bm[0] = bm[1];
				bm[1] = null;
			}

			boolean isSingle = false; // 現在ページが単ページか
			if (isHalfView() && bm[0] != null) {
				if (!DEF.checkPortrait(bm[0].Width, bm[0].Height, mRotate)) {
					// 横長画像であれば分割
					mSourceImage[0] = bm[0];
					if ((mHalfPos == HALFPOS_2ND && mPageWay == DEF.PAGEWAY_RIGHT) || (mHalfPos != HALFPOS_2ND && mPageWay == DEF.PAGEWAY_LEFT)) {
						// 左側用にする
						mSourceImage[0].HalfMode = ImageData.HALF_LEFT;
					}
					else {
						// 右側用にする
						mSourceImage[0].HalfMode = ImageData.HALF_RIGHT;
					}

					mCurrentPageHalf = true;
				}
				else {
					mHalfPos = HALFPOS_1ST;
					mSourceImage[0] = bm[0];
				}
			}
			else if (isDualView()) {
				if (bm[0] != null && bm[1] != null) {
					if (mCurrentPage == 0 && mTopSingle) {
						// 1ページのみ出力
						mHalfPos = HALFPOS_1ST;
						if (mPageBack) {
							// 戻りの場合は2ページ目にする
							mSourceImage[0] = (mPageWay != DEF.PAGEWAY_LEFT) ? bm[1] : bm[0];
							mCurrentPage = 1;
						}
						else {
							// 戻りじゃない場合は1ページ目にする
							mSourceImage[0] = (mPageWay != DEF.PAGEWAY_LEFT) ? bm[0] : bm[1];
							mCurrentPage = 0;
						}
						isSingle = true;
					}
					else {
						// 並べて表示
						// ページ方向が逆なら左右の並びを入れ替える
						if (mPageWay != DEF.PAGEWAY_LEFT) {
							mSourceImage[0] = bm[0]; // 右描画用
							mSourceImage[1] = bm[1]; // 左描画用
						}
						else {
							mSourceImage[0] = bm[1]; // 右描画用
							mSourceImage[1] = bm[0]; // 左描画用
						}

						mCurrentPageDual = true;
						mHalfPos = HALFPOS_1ST;
					}
				}
				else {
					// 1ページをそのまま出力
					mHalfPos = HALFPOS_1ST;
					mSourceImage[0] = bm[0];
					if (mPageBack) {
						// 戻りの場合は2ページ目にする
						mCurrentPage++;
						// if (mCurrentPage == 0) {
						// }
					}
					isSingle = true;
				}
			}
			else if (bm[0] != null) {
				// 1ページをそのまま出力
				mHalfPos = HALFPOS_1ST;
				mSourceImage[0] = bm[0];
			}
			bm[0] = null;
			bm[1] = null;

			// 正しい現在頁
			mImageMgr.setCurrentPage(mCurrentPage, isSingle);

			// 拡大/縮小
			ImageScaling();

			// 任意拡大率の初期値設定
			// int maxScale = 0;
			// for (int i = 0; i < 2; i++) {
			// if (mSourceImage[i] != null && mSourceImage[i].Height != 0) {
			// int scale = mSourceImage[i].SclHeight * 100 /
			// mSourceImage[i].Height;
			// if (maxScale < scale) {
			// maxScale = scale;
			// }
			// }
			// }
			// mPinchScale = maxScale;
			// if (mPinchScale < 10) {
			// mPinchScale = 10;
			// }
			// else if (mPinchScale > 500) {
			// mPinchScale = 500;
			// }

			// 終了通知
			Message message = new Message();
			message.what = mLoadEndMsg;
			handler.sendMessage(message);
		}
	}

	// Bitmapを読み込む
	private ImageData loadBitmap(int page, boolean notice) {
		ImageData bm = null;

		try {
			bm = mImageMgr.loadBitmap(page, notice);
		} catch (Exception ex) {
			Message message = new Message();
			message.what = DEF.HMSG_ERROR;
			message.obj = ex.getMessage();
			mHandler.sendMessage(message);
			return null;
		}
		return bm;
	}

	/**
	 * View がクリックされた時に発生します。
	 */
	public void ChangeScale(int mode) {
		mScaleMode = mode;

		setMgrConfig(true);
		// イメージ拡大縮小
		ImageScaling();

		this.updateOverSize(true);

		// 第3引数は、表示期間（LENGTH_SHORT、または、LENGTH_LONG）
		// Toast.makeText(this, strScaleMode, Toast.LENGTH_SHORT).show();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = sp.edit();
		ed.putInt("scalemode", mScaleMode);
		ed.commit();
	}

	/**
	 * ページ並びを逆順にする
	 */
	public void reverseOrder() {
		mReverseOrder = !mReverseOrder;
		mImageMgr.reverseOrder();
		setMgrConfig(false);

		// イメージ拡大縮小
		ImageScaling();
		this.updateOverSize(false);
		setBitmapImage();
	}

	//指定したページをクロップして書庫／フォルダのサムネイルに設定
	public void setThumbCropped(int page) {
		String path = mImageMgr.decompFile(page, null);
		if (path != null && path.length() >= 5) {
			int thumbH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
			int thumbW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));
			Intent intent = new Intent(mActivity, CropImageActivity.class);
			intent.putExtra("uri", path);
			intent.putExtra("aspectRatio", (float)thumbW / (float)thumbH);
			startActivityForResult(intent, DEF.REQUEST_CROP);
		}else{
			Toast.makeText(mActivity, "Failed to open file", Toast.LENGTH_SHORT).show();
		}
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
			option.inPreferredConfig = Bitmap.Config.RGB_565;
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
		}catch(IOException e){
			Log.e(TAG, "setThumb error");
			Toast.makeText(mActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			return;
		}

		bm = ImageAccess.resizeTumbnailBitmap(bm, thumW, thumH, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
		if (bm != null) {
			ThumbnailLoader loader = new ThumbnailLoader(mActivity, "", "", null, thumbID, new ArrayList<FileData>(), thumW, thumH, 0, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
			loader.deleteThumbnailCache(mFilePath, thumW, thumH);
			loader.setThumbnailCache(mFilePath, bm);
			Toast.makeText(this, R.string.ThumbConfigured, Toast.LENGTH_SHORT).show();
		}
	}

	//指定したページを書庫／フォルダのサムネイルに設定
	public void setThumb(int page) {
		long thumbID = System.currentTimeMillis();
		int thumH = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeH(mSharedPreferences));
		int thumW = DEF.calcThumbnailSize(SetFileListActivity.getThumbSizeW(mSharedPreferences));
		Bitmap bm = null;
		try {
			Object lock = mImageMgr.getLockObject();
			synchronized (lock) {
				Log.d(TAG, "setThumb: Call LoadThumbnail(" + page + ", " + thumW + ", " + thumH + ") start.");
				// 読み込み処理とは排他する
				bm = mImageMgr.LoadThumbnail(page, thumW, thumH);
			}
		} catch (Exception e) {
			Log.e(TAG, "setThumb error");
		}
		if (bm != null) {
			bm = ImageAccess.resizeTumbnailBitmap(bm, thumW, thumH, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
		}
		if (bm != null) {
			ThumbnailLoader loader = new ThumbnailLoader(mActivity, "", "", null, thumbID, new ArrayList<FileData>(), thumW, thumH, 0, ImageAccess.BMPCROP_NONE, ImageAccess.BMPMARGIN_NONE);
			loader.deleteThumbnailCache(mFilePath, thumW, thumH);
			loader.setThumbnailCache(mFilePath, bm);
			Toast.makeText(this, R.string.ThumbConfigured, Toast.LENGTH_SHORT).show();
		}
	}
	public void toggleCenterMargin() {
		mCMargin = !mCMargin;
		setViewConfig();
		setBitmapImage();
	}

	public void toggleCenterShadow() {
		mCShadow = !mCShadow;
		setViewConfig();
		setBitmapImage();
	}

	private void setMgrConfig(boolean scaleinit) {
		boolean debug = false;
		if(debug) {Log.d(TAG, "setMgrConfig: 開始します. scaleinit=" + scaleinit);}
		if (mImageMgr != null) {
			mImageMgr.setConfig(mScaleMode, mCenter, mFitDual, mDispMode, mNoExpand, mAlgoMode, mRotate, mWAdjust
					, mWidthScale, mImgScale, mPageWay, mMgnCut, mMgnCutColor, 0, mBright, mGamma, mSharpen, mInvert, mGray, mPseLand, mMoire, mTopSingle, scaleinit, mEpubOrder);
		}
		// モードが変わればスケールは初期化
		if (scaleinit) {
			mPinchScale = mImgScale;
		}
		if(debug) {Log.d(TAG, "setMgrConfig: 終了します.");}
	}

	private void setViewConfig() {
		if (mImageView != null) {
			mImageView.setConfig(this, mMgnColor, mCenColor, mTopColor1, mViewPoint, mMargin, mCenter, mShadow, mZoomType, mPageWay, mScrlWay, mScrlRngW, mScrlRngH, mPrevRev, mNoExpand, mFitDual,
					mCMargin, mCShadow, mPseLand, mEffect, mScrlNext, mViewNext, mNextFilter);
			mImageView.setLoupeConfig( mLoupeSize );	// ルーペサイズの設定
		}
		if (mGuideView != null) {
			// 操作ガイドの設定
			mGuideView.setGuideMode(isDualView() == true, mBottomFile, mPageWay == DEF.PAGEWAY_RIGHT, mPageSelect, mImmEnable);
		}
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
		boolean debug = false;
		if (debug) {Log.d(TAG, "onTouch: 開始します.");}

		if (mAutoPlay) {
			if (debug) {Log.d(TAG, "onTouch: mAutoPlay");}
			// オートプレイ中は解除
			setAutoPlay(false);
		}

		if (mListLoading) {
			if (debug) {Log.d(TAG, "onTouch: mListLoading");}
			// ファイル一覧の読み込み中はページ操作しない
			return true;
		}
		if (mImageMgr == null || mImageMgr.length() == 0) {
			if (debug) {Log.d(TAG, "onTouch: mImageMgr == null || mImageMgr.length() == 0");}
			if (mImageMgr != null) {
				// 読み込み停止
				mImageMgr.setBreakTrigger();
				// キャッシュ読込スレッド停止
				mImageMgr.closeFiles();
			}
			setResult(RESULT_OK);
			finish();
			return true;
		}
		if (mBitmapLoading) {
			// ビットマップ読込中は操作不可
			if (debug) {Log.d(TAG, "onTouch: IF mBitmapLoading == true");}
			// mHandler.sendMessageAtTime がキャンセルされるので、再度セットする
			startViewTimer(EVENT_EFFECT);
			return true;
		}

		float x;
		float y;
		int cx;
		int cy;
		if (!mPseLand) {
			x = event.getX();
			y = event.getY();
			cx = mImageView.getWidth();
			cy = mImageView.getHeight();
		}
		else {
			// 疑似横モード
			cx = mImageView.getHeight();
			cy = mImageView.getWidth();
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
    						mPinchScaleSel = mImgScale;
    						mImageView.setPinchChanging(mPinchScaleSel);
    						mGuideView.setGuideText(mPinchScaleSel + "%");
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
    					if (Math.abs(x1 - x2) > mSDensity * 20 || Math.abs(y1 - y2) > mSDensity * 20) {
    						// 2点間が10sp以上であれば拡大縮小開始
    						mPinchOn = true;
    						mPinchScaleSel = mPinchScale;
    						mTouchFirst = false;
    						mImageView.setZoomMode(false);
    						mLongTouchMode = false;
    					}
    				}
    			}
    		}
    		if (mPinchOn) {
    			// サイズ変更中
    			if (action == MotionEvent.ACTION_CANCEL) {
    				// サイズ変更キャンセル(画面回転など)
    				mImageView.setPinchChanging(0);
    				mGuideView.setGuideText(null);
    				mPinchOn = false;
    			}
    			else if (action2 == MotionEvent.ACTION_POINTER_1_DOWN || action2 == MotionEvent.ACTION_MOVE) {
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
    					else if (i == 1) { // if (mPinchId == (int)
    									   // event.getPointerId(i)) {
    						// 距離を求める
    						int range;
    						range = (int) Math.sqrt(Math.pow(Math.abs(x1 - x), 2) + Math.pow(Math.abs(y1 - y), 2));
    						if (mPinchDown) {
    							mPinchRange = range;
    							mPinchDown = false;
    						}
    						else {
    							// 初回は記録のみ
    							int range2 = (int)((range - mPinchRange) / (8 * mSDensity));
    							if (range2 != 0) {
    								mPinchCount = 0;
    							}
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
    							mPinchRange += range2 * (8 * mSDensity);
    						}
    						// 任意スケーリング変更中
    						mImageView.setPinchChanging(mPinchScaleSel);
    						mGuideView.setGuideText(mPinchScaleSel + "%");
    					}
    				}
    			}
    			else if (action2 == MotionEvent.ACTION_UP) {
    				// サイズ変更終了
    				if (mPinchScale != mPinchScaleSel) {
    					mImageView.lockDraw();
    				}
    				mImageView.setPinchChanging(0);
    				mGuideView.setGuideText(null);
    				mPinchOn = false;
    				mPinchDown = false;
    				if (mPinchScale != mPinchScaleSel) {
    					synchronized (mImageView) {
    						mPinchScale = mPinchScaleSel;
    						mImageMgr.setImageScale(mPinchScale);
    						ImageScaling();
    					}
    					this.updateOverSize(true);
    					mImageView.update(true);
    				}
    			}
    			return true;
    		}
		}

		if (mLoadingNext) {
			// ローディング中のタッチは次の移動イベントでONにする
			if (action == MotionEvent.ACTION_MOVE) {
				if (!mScrlNext) {
					action = MotionEvent.ACTION_DOWN;
				}
			}
			mLoadingNext = false;
		}

		if (mImmEnable) {
			if (action == MotionEvent.ACTION_DOWN) {
//				Log.d("touchDown", "x=" + x + ", y=" + y);
				if (y <= mImmCancelRange || y >= cy - mImmCancelRange) {
					// IMMERSIVEモードの発動時にタッチ処理を無視する
					mImmCancel = true;
				}
			}
			if (mImmCancel) {
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
				if (debug) {Log.d(TAG, "onTouch: ACTION_DOWN");}
				// 押下状態を設定
				mGuideView.eventTouchDown((int)x, (int)y, cx, cy, mImmEnable ? false : true);

				mPageMode = false;
				mTouchPointNum = 0;

				// 慣性スクロールの停止
				mImageView.scrollStop();

				if (y >= cy - mClickArea) {
					if (mPageSelect == PAGE_SLIDE) {
						if (mClickArea <= x && x <= cx - mClickArea) {
							// ページ選択開始
							int sel = GuideView.GUIDE_BCENTER;
							mSelectPage = mCurrentPage;
							mGuideView.setGuideIndex(sel);

							mPageMode = true;
							mPageModeIn = true;
						}
					}
					else {
						mSelectPage = mCurrentPage;
					}
					// 下部押下
					startLongTouchTimer(EVENT_TOUCH_BOTTOM); // ロングタッチのタイマー開始
					mOperation = TOUCH_COMMAND;
					// 長押し対応のため、再設定する(IMMERSIVEがOFFでも長押し対応するため)
					mGuideView.eventTouchDown((int)x, (int)y, cx, cy, false);
					// 文書情報を表示
					mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
					mGuideView.setPageColor(mTopColor1);
				}
				else if (y <= mClickArea) {
					// 上部押下
					startLongTouchTimer(EVENT_TOUCH_TOP); // ロングタッチのタイマー開始
					mOperation = TOUCH_COMMAND;
				}
				else {
					// 操作モード
					mOperation = TOUCH_OPERATION;
					// 現在のイメージ表示位置をフリックの判定のため記憶
					mTouchDrawLeft = (int) mImageView.getDrawLeft();
					callZoomAreaDraw(x, y);
					startLongTouchTimer(EVENT_TOUCH_ZOOM); // ロングタッチのタイマー開始

					mTouchPoint[0].x = x;
					mTouchPoint[0].y = y;
					mTouchPointTime[0] = SystemClock.uptimeMillis();
					mTouchPointNum = 1;
				}

				this.mTouchFirst = true;	// 押してから移動してないフラグ
				this.mTouchBeginX = x;	// 最初の位置
				this.mTouchBeginY = y;
				break;

			case MotionEvent.ACTION_MOVE:
				if (debug) {Log.d(TAG, "onTouch: ACTION_MOVE");}
				// Log.d("action_move", "x=" + x + ", y=" + y);
				// 移動位置設定
				mGuideView.eventTouchMove((int)x, (int)y);

				if (mOperation == TOUCH_COMMAND) {
					if (mPageMode && mPageSelect == PAGE_SLIDE) {
						// スライドページ選択中
						int sel = GuideView.GUIDE_NOSEL;
						if (y >= cy - mClickArea) {
							// 操作エリアから出て戻ったらそこを基準にする
							if (!mPageModeIn) {
								// 指定のページを基準とした位置を設定
								mTouchBeginX = x - calcPageSelectRange(mSelectPage);
							}

							// タッチの位置でページを選択
							if (x < mClickArea) {
								int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? mImageMgr.length() - 1 : 0;
								if (mSelectPage != leftpage) {
									mSelectPage = leftpage;
									startVibrate();
								}
								sel = GuideView.GUIDE_BLEFT;
							}
							else if (x > cx - mClickArea) {
								int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : mImageMgr.length() - 1;
								if (mSelectPage != rightpage) {
									mSelectPage = rightpage;
									startVibrate();
								}
								sel = GuideView.GUIDE_BRIGHT;
							}
							else {
								// 選択中のページ
								mSelectPage = calcSelectPage(x);

								if (mSelectPage < 0) {
									// 最小値は先頭ページ
									mSelectPage = 0;
									// タッチ位置を先頭ページとしたときのCurrentPageの位置を求める
									mTouchBeginX = x - calcPageSelectRange(mSelectPage);
								}
								else if (mSelectPage > mImageMgr.length() - 1) {
									// 最大値は最終ページ
									mSelectPage = mImageMgr.length() - 1;
									// タッチ位置を最終ページとしたときのCurrentPageの位置を求める
									mTouchBeginX = x - calcPageSelectRange(mSelectPage);
								}
								sel = GuideView.GUIDE_BCENTER;
							}
							mPageModeIn = true;
						}
						else {
							mPageModeIn = false;
						}

						// ファイル名＋ページ表示
						String strPage = mImageMgr.createPageStr(mSelectPage);
						String strOld = mGuideView.getPageText();
						if (!strPage.equals(strOld)) {
							if (mCurrentPage - 1 <= mSelectPage && mSelectPage <= mCurrentPage + 1) {
								// ページ変更時に振動
								startVibrate();
							}
							mGuideView.setPageText(strPage);
						}

						mGuideView.setPageColor(mTopColor1);

						// 選択に反映
						mGuideView.setGuideIndex(sel);
					}
				}
				else if (mOperation == TOUCH_OPERATION) {
					if (mLongTouchMode) {
						// ズーム表示
						callZoomAreaDraw(x, y);
					}
					else {
//						// 縦フリックメニュー表示処理
//						if (this.mTouchFirst && (Math.abs(this.mTouchBeginY - y) > mMoveRange && Math.abs(this.mTouchBeginY - y) > Math.abs(this.mTouchBeginX - x) * 2)) {
//						// タッチ後に範囲を超えたときに、縦の移動が横の移動の2倍を超えている場合は縦フリックモードへ
//							mVerticalSwipe = true;					
//						}
						// ページ戻or進、スクロール処理
						if (this.mTouchFirst && ((Math.abs(this.mTouchBeginX - x) > mMoveRange || Math.abs(this.mTouchBeginY - y) > mMoveRange))) {
							// タッチ後に範囲を超えて移動した場合はスクロールモードへ
							this.mTouchFirst = false;
							mLongTouchCount ++;
							// mGuideView.setGuideIndex(mGuideView.GUIDE_NONE,
							// mGuideView.GUIDE_NONE);
							mImageView.scrollStart(mTouchBeginX, mTouchBeginY, RANGE_FLICK, mScroll);
						}

						if (!this.mTouchFirst && !mVerticalSwipe) {
//						if (this.mTouchFirst == false) {
							// ■■■ スクロール中なら
							long now = SystemClock.uptimeMillis();
							mImageView.scrollMoveAmount(x - mTouchPoint[0].x, y - mTouchPoint[0].y, mScroll, true);

							for (int i = MAX_TOUCHPOINT - 1; i >= 1; i--) {
								mTouchPoint[i].x = mTouchPoint[i - 1].x;
								mTouchPoint[i].y = mTouchPoint[i - 1].y;
								mTouchPointTime[i] = mTouchPointTime[i - 1];
							}
							mTouchPoint[0].x = x;
							mTouchPoint[0].y = y;
							mTouchPointTime[0] = now;
							if (mTouchPointNum < MAX_TOUCHPOINT) {
								mTouchPointNum++;
							}
						}
					}
				}
				break;

			case MotionEvent.ACTION_CANCEL:
				if (debug) {Log.d(TAG, "onTouch: ACTION_CANCEL");}
				if (mLongTouchMode) {
					// ズーム表示解除
					mImageView.setZoomMode(false);
					mLongTouchMode = false;
				}
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
			case MotionEvent.ACTION_UP:
				if (debug) {Log.d(TAG, "onTouch: ACTION_UP");}
//				if (mVerticalSwipe) {
//					// 縦フリックの指を離した
//					//Toast.makeText(this, "メニューを表示します", Toast.LENGTH_SHORT).show();
//					mVerticalSwipe = false;
//					if (mPageSelect == PAGE_INPUT) {
//						// ページ番号入力
//						if (PageSelectDialog.mIsOpened == false) {
//							PageSelectDialog pageDlg = new PageSelectDialog(this, mImmEnable);
//							pageDlg.setParams(mCurrentPage, mImageMgr.length(), mPageWay == DEF.PAGEWAY_RIGHT);
//							pageDlg.setPageSelectListear(this);
//							pageDlg.show();
//							mPageDlg = pageDlg;
//						}
//					}
//					else if (mPageSelect == PAGE_THUMB) {
//						// サムネイルページ選択
//						if (PageThumbnail.mIsOpened == false) {
//							PageThumbnail thumbDlg = new PageThumbnail(this);
//							thumbDlg.setParams(mCurrentPage, mPageWay == DEF.PAGEWAY_RIGHT, mImageMgr, mThumID);
//							thumbDlg.setPageSelectListear(this);
//							thumbDlg.show();
//							mThumbDlg = thumbDlg;
//						}
//					}
//					openMiniMenu();
//				}
				// 選択されたコマンド
				int result = mGuideView.eventTouchUp((int)x, (int)y);
				mResult = result;
				// 情報表示クリア
				mGuideView.setPageText(null);
				mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
				mGuideView.setGuideIndex(GuideView.GUIDE_NONE);

				if (mPageMode) {
					// ページ選択モード終了
					if (mPageSelect == PAGE_SLIDE) {
						if (y > cy - mClickArea) {
							if (mPageSelect == PAGE_SLIDE || (x < mClickArea || x > cx - mClickArea)) {
								// ページ選択確定
								if (mSelectPage != mCurrentPage) {
									// ページ変更時に振動
									startVibrate();
									mCurrentPage = mSelectPage;
									mPageBack = false;
									if (mScrlNext) {
										mImageView.scrollReset();
									}
									setBitmapImage();
								}
							}
						}
					}
				}
				if (result != -1) {
					int index = (result & 0x7FFF);
					if ((result & 0x8000) != 0) {
						// 上部選択の場合は選択リストを表示
						execCommand(mCommandId[index]);
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
						// 先頭/末尾ボタン
						mResult = result;

						if (mPageSelect == PAGE_SLIDE) {
							// ページ選択方法が画面下をスワイプのとき

							if (mResult == 0x4003) {
								// 左側ボタン
								int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? mImageMgr.length() - 1 : 0;
								if (mSelectPage != leftpage) {
									mSelectPage = leftpage;
								}
							}
							else {
								// 右側ボタン
								int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : mImageMgr.length() - 1;
								if (mSelectPage != rightpage) {
									mSelectPage = rightpage;
								}
							}
							// ページ選択確定
							if (mSelectPage != mCurrentPage) {
								// ページ変更時に振動
								mCurrentPage = mSelectPage;
								mPageBack = false;
								if (mScrlNext) {
									mImageView.scrollReset();
								}
								setBitmapImage();
							}
						}
						else {
							// ページ選択方法がスライダー表示かサムネイルのとき

							AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MyDialog);
							if ((mResult == 0x4002 && mPageWay == DEF.PAGEWAY_RIGHT) || (mResult == 0x4003 && mPageWay != DEF.PAGEWAY_RIGHT)) {
								dialogBuilder.setTitle(R.string.pageTop);
							}
							else {
								dialogBuilder.setTitle(R.string.pageLast);
							}
							dialogBuilder.setMessage(null);
							dialogBuilder.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int whichButton) {

									if (mResult == 0x4003) {
										// 左側ボタン
										int leftpage = mPageWay == DEF.PAGEWAY_RIGHT ? mImageMgr.length() - 1 : 0;
										if (mSelectPage != leftpage) {
											mSelectPage = leftpage;
										}
									}
									else {
										// 右側ボタン
										int rightpage = mPageWay == DEF.PAGEWAY_RIGHT ? 0 : mImageMgr.length() - 1;
										if (mSelectPage != rightpage) {
											mSelectPage = rightpage;
										}
									}
									// ページ選択確定
									if (mSelectPage != mCurrentPage) {
										// ページ変更時に振動
										mCurrentPage = mSelectPage;
										mPageBack = false;
										if (mScrlNext) {
											mImageView.scrollReset();
										}
										setBitmapImage();
									}

									dialog.dismiss();
								}
							});
							dialogBuilder.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									// dialog.cancel();
								}
							});
							Dialog dialog = dialogBuilder.create();
							dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
							dialog.getWindow().getDecorView().setSystemUiVisibility(
								mActivity.getWindow().getDecorView().getSystemUiVisibility());
							dialog.show();
							dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
						}

					}
					else {
						// 下部選択の場合は対応する操作を実行
						switch (index) {
							case 0:
								if (isDualView()) {
									// 1ページ次へずらす
									shiftPage(1);
								}
								break;
							case 1:
								if (isDualView()) {
									// 1ページ前へずらす
									shiftPage(-1);
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
								// 次巻(最終位置)
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
								// 表示中の画像が1枚か2枚かを判定
								ImageData[] bm = mImageView.getImageBitmap();
								int shareType;
								if (bm[0] != null && bm[1] != null) {
									shareType = DEF.SHARE_LR;
								}
								else {
									shareType = DEF.SHARE_SINGLE;
								}

								if (mPageSelect == PAGE_INPUT) {

									// 文書情報を表示
									mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
									mGuideView.setPageColor(0x80000000);

									// ページ番号入力
									if (!PageSelectDialog.mIsOpened) {
										PageSelectDialog pageDlg = new PageSelectDialog(this, R.style.MyDialog);
										pageDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mImageMgr.length(), mPageWay == DEF.PAGEWAY_RIGHT, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
										pageDlg.setPageSelectListear(this);
										pageDlg.show();
										pageDlg.setShareType(shareType);
										mPageDlg = pageDlg;
									}
								}
								else if (mPageSelect == PAGE_THUMB) {

									// 文書情報を表示
									mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
									mGuideView.setPageColor(0x80000000);

									// サムネイルページ選択
									if (!PageThumbnail.mIsOpened) {
										PageThumbnail thumbDlg = new PageThumbnail(this, R.style.MyDialog);
										thumbDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mPageWay == DEF.PAGEWAY_RIGHT, mImageMgr, mThumID, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
										thumbDlg.setPageSelectListear(this);
										thumbDlg.show();
										thumbDlg.setShareType(shareType);
										mThumbDlg = thumbDlg;
									}
								}
								break;
							case 9:
								// 閉じる
								finishActivity( true );
								break;
							case 10:
								// 設定画面に遷移
								if (mImmEnable && mSdkVersion >= 19) {
									int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
									uiOptions &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
									getWindow().getDecorView().setSystemUiVisibility(uiOptions);
								}

								// バックグラウンドでのキャッシュ読み込み停止
								mImageMgr.setCacheSleep(true);

								Intent intent = new Intent(ImageActivity.this, SetConfigActivity.class);
								startActivityForResult(intent, DEF.REQUEST_SETTING);
								break;
						}
					}
				}
				else if (mLongTouchMode) {
					// ズーム表示解除
					// startVibrate();
					mImageView.setZoomMode(false);
					mLongTouchMode = false;
				}
				else if (mOperation == TOUCH_OPERATION) {
					if (debug) {Log.d(TAG, "onTouch: 通常タップ");}
					// ■■■ タップ終了なら
					if (this.mTouchFirst) {
						this.mTouchFirst = false;
						mPageBack = false;
						boolean next = checkTapDirectionNext(x, y, cx, cy);
						if (mTapScrl) {
							// タップでスクロール
							int move = next ? 1 : -1;
							// 読込中の表示
							startScroll(move);
						} else {
							// タップでスクロールしない
							// 普通のタッチでページ遷移
							if (next) {
								// 次ページへ
//								if (mScrlNext) {
//									mImageView.scrollReset();
//								}
								nextPage();
							} else {
								// 前ページへ
//								if (mScrlNext) {
//									mImageView.scrollReset();
//								}
								prevPage();
							}
						}
					}
					else {
						// ■■■ スクロール終了なら
						int flickPage = mImageView.checkFlick();
						if (mFlickPage && flickPage != 0) {
							// 「スクロールで前後のページへ移動する」が無効なら
							// フリックでページ遷移
							if (mFlickEdge && mTouchDrawLeft != (int) mImageView.getDrawLeft()) {
								// 端からフリックしないときはページめくりしない
								;
							} else if ((flickPage > 0 && mPageWay == DEF.PAGEWAY_RIGHT) || (flickPage < 0 && mPageWay != DEF.PAGEWAY_RIGHT) ? !mChgFlick : mChgFlick) {
								// 次ページへ
								if (mScrlNext) {
									mImageView.scrollReset();
								}
								nextPage();
							} else {
								// 前ページへ
								if (mScrlNext) {
									mImageView.scrollReset();
								}
								prevPage();
							}
						} else if (mMomentMode < DEF.MAX_MOMENTMODE) {
							long now = SystemClock.uptimeMillis();

							int i;
							for (i = 1; i < mTouchPointNum && i < MAX_TOUCHPOINT; i++) {
								if (now - mTouchPointTime[i] > TERM_MOMENT) {
									// 過去0.2秒の範囲
									break;
								}
							}
							if (i >= 3) {
								float sx = mTouchPoint[2].x - mTouchPoint[i - 1].x;
								float sy = mTouchPoint[2].y - mTouchPoint[i - 1].y;
								long term = mTouchPointTime[2] - mTouchPointTime[i - 1];
								// Log.d("moment_up", "i=" + i + ", sx=" + sx +
								// ", sy=" + sy + ", term=" + term);
								mImageView.momentiumStart(x, y, mScroll, sx, sy, (int) term, mMomentMode);
							}
						}
					}
				}
				// 押してる間のフラグクリア
				mTouchFirst = false;
				mOperation = TOUCH_NONE;
				break;
		}
		return true;
	}

	// タップが前/次どちらか判定
	private boolean checkTapDirectionNext(float x, float y, int cx, int cy) {
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
		// if (index < 0 || index > DEF.GUIDE_TOP_NUM) {
		// // インデックスが範囲外
		// return;
		// }
		// 再読み込みになるのでページ戻は解除
		mPageBack = false;

		Resources res = getResources();

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;
		int nItem;

		String title;
		int selIndex;
		switch (index) {
			case SELLIST_ALGORITHM:
				// 画像補間法の選択肢設定
				title = res.getString(R.string.algoriMenu);
				selIndex = mAlgoMode;
				nItem = SetImageActivity.AlgoModeName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.AlgoModeName[i]);
				}
				break;
			case SELLIST_IMG_ROTATE:
				// 回転の選択肢設定
				title = res.getString(R.string.imgRotaMenu);
				selIndex = mRotate;
				nItem = SetImageActivity.ImgRotaName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.ImgRotaName[i]);
				}
				break;
			case SELLIST_VIEW_MODE:
				// 見開きモードの選択肢設定
				title = res.getString(R.string.tguide02);
				selIndex = mDispMode;
				nItem = SetImageActivity.ViewName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.ViewName[i]);
				}
				break;
			case SELLIST_SCALE_MODE:
				// サイズ設定の選択肢設定
				title = res.getString(R.string.tguide03);
				selIndex = 0;
				for (int i = 0; i < SCALENAME_ORDER.length; i++) {
					if (SCALENAME_ORDER[i] == mScaleMode) {
						selIndex = i;
						break;
					}
				}
				nItem = SetImageActivity.ScaleName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.ScaleName[SCALENAME_ORDER[i]]);
				}
				break;
			case SELLIST_MARGIN_CUT:
				// 余白削除
				title = res.getString(R.string.mgnCutMenu);
				selIndex = mMgnCut;
				nItem = SetImageActivity.MgnCutName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.MgnCutName[i]);
				}
				break;
			case SELLIST_MARGIN_CUTCOLOR:
				// 余白削除
				title = res.getString(R.string.mgnCutColorMenu);
				selIndex = mMgnCutColor;
				nItem = SetImageActivity.MgnCutColorName.length;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.MgnCutColorName[i]);
				}
				break;
			case SELLIST_SCR_ROTATE:
				// 画面方向
				title = res.getString(R.string.rotateMenu);
				selIndex = mViewRota - 1;
				nItem = SetImageActivity.RotateName.length - 1;
				items = new String[nItem];
				for (int i = 0; i < nItem; i++) {
					items[i] = res.getString(SetImageActivity.RotateName[i + 1]);
				}
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(this, R.style.MyDialog, title, items, selIndex, true, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_ALGORITHM:
						// 画像補間法
						if (mAlgoMode != index) {
							mAlgoMode = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_IMG_ROTATE:
						// 回転
						if (mRotate != index && index >= 0 && index < 4) {
							// 角度を変更した
							mRotate = index;
							mImageView.setRotate(mRotate);
							setMgrConfig(false);
							setBitmapImage();
						}
						break;
					case SELLIST_VIEW_MODE:
						// 見開き設定変更
						if (mDispMode != index) {
							mDispMode = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_SCALE_MODE: {
						// 画像拡大率の変更
						mImageView.lockDraw();
						ChangeScale(SCALENAME_ORDER[index]);
						mImageView.update(true);
						break;
					}
					case SELLIST_MARGIN_CUT:
						// 余白削除
						if (mMgnCut != index) {
							mMgnCut = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_MARGIN_CUTCOLOR:
						// 余白削除の色
						if (mMgnCutColor != index) {
							mMgnCutColor = index;
							setImageConfig();
							setBitmapImage();
						}
						break;
					case SELLIST_SCR_ROTATE:
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

							setMgrConfig(true);
							setViewConfig();

							boolean isPrePort = true;
							boolean isAftPort = true;

							if (prevRota == DEF.ROTATE_LANDSCAPE) {
								isPrePort = false;
							}
							else if (prevRota == DEF.ROTATE_AUTO) {
								if (DEF.checkPortrait(mViewWidth, mViewHeight) == false) {
									isPrePort = false;
								}
							}

							if (mViewRota == DEF.ROTATE_LANDSCAPE) {
								isAftPort = false;
							}

							if (isPrePort == isAftPort) {
								// 変化がないとき
								mImageView.updateScreenSize();
							}
						}
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

	private void showImageConfigDialog(int command_id) {
		boolean debug = false;

		if (mImageConfigDialog != null) {
			return;
		}
		mImageConfigDialog = new ImageConfigDialog(this, R.style.MyDialog, command_id, false, this);

		// 画像サイズの選択項目を求める
		int selIndex = 0;
		for (int i = 0; i < SCALENAME_ORDER.length; i++) {
			if (SCALENAME_ORDER[i] == mScaleMode) {
				selIndex = i;
				break;
			}
		}
		mImageConfigDialog.setConfig(mGray, mInvert, mMoire, mTopSingle, mSharpen, mBright, mGamma, mBkLight, mAlgoMode, mDispMode, selIndex, mMgnCut, mMgnCutColor, mIsConfSave);
		mImageConfigDialog.setImageConfigListner(new ImageConfigListenerInterface() {
			@Override
			public void onButtonSelect(int select, boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave) {
				// 選択状態を通知
				boolean ischange = false;
				// 変更があるかを確認(適用後のキャンセルの場合も含む)
				if (mGray != gray || mInvert != invert || mMoire != moire || mTopSingle != topsingle || mSharpen != sharpen || mBright != bright || mGamma != gamma || mAlgoMode != algomode || mDispMode != dispmode || mMgnCut != mgncut || mMgnCutColor != mgncutcolor) {
					ischange = true;
				}
				mGray = gray;
				mInvert = invert;
				mMoire = moire;
				mTopSingle = topsingle;
				mSharpen = sharpen;
				mBright = bright;
				mGamma = gamma;
				mAlgoMode = algomode;
				mMgnCut = mgncut;
				mMgnCutColor = mgncutcolor;
				mIsConfSave = issave;

				if (mScaleMode != SCALENAME_ORDER[scalemode]) {
					// 画像拡大率の変更
					mImageView.lockDraw();
					ChangeScale(SCALENAME_ORDER[scalemode]);
					ischange = true;
				}
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
				if (mDispMode != dispmode) {
					mDispMode = dispmode;
					// 表示を更新
					setImageConfig();
					setBitmapImage();

					// 操作ガイドの設定
					mGuideView.setGuideMode(isDualView() == true, mBottomFile, mPageWay == DEF.PAGEWAY_RIGHT, mPageSelect, mImmEnable);
				}
				else if (ischange) {
					// 表示を更新
					mImageView.lockDraw();
					setImageConfig();
					mImageView.update(true);
				}

				if (issave) {
					// 設定を指定
					Editor ed = mSharedPreferences.edit();
					ed.putBoolean(DEF.KEY_GRAY, mGray);
					ed.putBoolean(DEF.KEY_INVERT, mInvert);
					ed.putBoolean(DEF.KEY_MOIRE, mMoire);
					ed.putBoolean(DEF.KEY_TOPSINGLE, mTopSingle);
					ed.putString(DEF.KEY_SHARPEN, Integer.toString(mSharpen));
					ed.putString(DEF.KEY_BRIGHT, Integer.toString(mBright));
					ed.putString(DEF.KEY_GAMMA, Integer.toString(mGamma));
					ed.putString(DEF.KEY_BKLIGHT, Integer.toString(mBkLight));
					ed.putString(DEF.KEY_ALGOMODE, Integer.toString(mAlgoMode));
					ed.putString(DEF.KEY_INITVIEW, Integer.toString(mDispMode));
					ed.putString(DEF.KEY_MARGINCUT, Integer.toString(mMgnCut));
					ed.putString(DEF.KEY_MARGINCUTCOLOR, Integer.toString(mMgnCutColor));
					ed.putString(DEF.KEY_INISCALE, Integer.toString(mScaleMode));
					ed.commit();
				}
			}

			@Override
			public void onClose() {
				// 終了
				mImageConfigDialog = null;
			}
		});
		mImageConfigDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	private void showCheckList() {
		if (mCheckDialog != null) {
			return;
		}

		Resources res = getResources();

		// 選択肢を設定
		String[] items = null;
		int nItem;

		String title;

		// 画像補間法の選択肢設定
		title = res.getString(R.string.setTopMenu);
		nItem = COMMAND_ID.length;
		items = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			items[i] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
		}

		boolean[] states = loadTopMenuState();

		mCheckDialog = new CheckDialog(this, R.style.MyDialog, title, states, items, new CheckListener() {
			@Override
			public void onSelected(boolean[] states) {
				// 選択完了
				saveTopMenuState(states);
				// 読み込みなおし
				loadTopMenuState();
				if (mGuideView != null) {
					// 上部メニューの文字列情報をガイドに設定
					mGuideView.setTopCommandStr(mCommandStr);
				}
			}

			@Override
			public void onClose() {
				// 終了
				mCheckDialog = null;
			}
		});
		mCheckDialog.show();
		return;
	}

	// 上部メニューの設定を読み込み
	private boolean[] loadTopMenuState() {
		boolean debug = false;
		if (debug) {Log.d(TAG, "loadTopMenuState: 開始します.");}

		boolean states[] = null;
		try {
			Resources res = getResources();

			if (debug) {Log.d(TAG, "loadTopMenuState: 保存された設定を取得します.");}
			states = new boolean[COMMAND_INDEX.length];
			int count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					states[i] = mSharedPreferences.getBoolean(DEF.KEY_TOPMENU + COMMAND_INDEX[i], COMMAND_INDEX[i] < 4 ? true : false);
					if (states[i] == true) {
						// 表示する個数
						count++;
					}
				}
				catch (Exception e) {
					Log.e(TAG, "loadTopMenuState: ループ1でエラーが発生しました.");
					if (e.getLocalizedMessage() != null) {
						Log.e(TAG, "loadTopMenuState: エラーメッセージ. " + e.getLocalizedMessage());
					}
					Log.e(TAG, "loadTopMenuState: i=" + i);
					Log.e(TAG, "loadTopMenuState: COMMAND_INDEX[i]=" + COMMAND_INDEX[i]);
				}
			}

			if (debug) {Log.d(TAG, "loadTopMenuState: 表示するコマンドを設定します.");}
			mCommandId = new int[count];
			mCommandStr = new String[count];
			count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					if (states[i]) {
						// 表示するコマンドを設定
						mCommandId[count] = COMMAND_ID[i];
						mCommandStr[count] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
						count++;
					}
				}
				catch (Exception e) {
					Log.e(TAG, "loadTopMenuState: ループ2でエラーが発生しました.");
					if (e.getLocalizedMessage() != null) {
						Log.e(TAG, "loadTopMenuState: エラーメッセージ. " + e.getLocalizedMessage());
					}
					Log.e(TAG, "loadTopMenuState: i=" + i);
					Log.e(TAG, "loadTopMenuState: COMMAND_INDEX[i]=" + COMMAND_INDEX[i]);
				}
			}
			if (debug) {
				Log.d(TAG, "loadTopMenuState: 終了します.");
			}
		}
		catch (Exception e) {
			Log.e(TAG, "loadTopMenuState: エラーが発生しました.");
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "loadTopMenuState: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}
		if (debug) {Log.d(TAG, "loadTopMenuState: 終了します.");}
		return states;
	}

	// 上部メニューの設定を保存
	private void saveTopMenuState(boolean states[]) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "saveTopMenuState: 開始します. states.length=" + states.length);}

		Editor ed = mSharedPreferences.edit();
		for (int i = 0 ; i < states.length ; i ++) {
			try {
				ed.putBoolean(DEF.KEY_TOPMENU + COMMAND_INDEX[i], states[i]);
			}
			catch (Exception e) {
				Log.e(TAG, "saveTopMenuState: エラーが発生しました.");
				if (e.getLocalizedMessage() != null) {
					Log.e(TAG, "saveTopMenuState: エラーメッセージ. " + e.getLocalizedMessage());
				}
			}
		}
		ed.commit();
		if (debug) {Log.d(TAG, "saveTopMenuState: 終了します.");}
	}

	// 座標から選択するページを求める
	private int calcSelectPage(float x) {
		int page = mCurrentPage;
		int pagecnt = 0;
		int range = (int) Math.abs((x - mTouchBeginX)); // 絶対値
		int sign = x < mTouchBeginX ? -1 : 1; // ページ方向
		// 右表紙なら逆転させる
		sign *= mPageWay == DEF.PAGEWAY_RIGHT ? -1 : 1;

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
		return range * (mCurrentPage <= page ? 1 : -1) * (mPageWay == DEF.PAGEWAY_RIGHT ? -1 : 1);
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	public void setImageConfig() {
		if (mImageMgr != null) {
			mImageMgr.setViewSize(mViewWidth, mViewHeight);
			setMgrConfig(false);
			ImageScaling();
			// setBitmapImage();
		}
		return;
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	public void setImageViewSize(int width, int height) {
		// if (mViewWidth != width || mViewHeight != height) {
		mViewWidth = width;
		mViewHeight = height;
		if (mImageMgr != null) {
			mImageMgr.setViewSize(mViewWidth, mViewHeight);
			ImageScaling();

			// 初回のリスト読み込み中は表示不要
			if (mListLoading == false) {
				// 縦横で単ページと見開き切替える場合
				if (mDispMode == DISPMODE_EXCHANGE) {
					// イメージ拡大縮小
					updateOverSize(false);
					setBitmapImage();
				}
			}
		}
		// }
		return;
	}

	private void ImageScaling() {
		int page1 = -1;
		int page2 = -1;
		int half1 = ImageData.HALF_NONE;
		int half2 = ImageData.HALF_NONE;
		if (mSourceImage[0] != null) {
			page1 = mSourceImage[0].Page;
			half1 = mSourceImage[0].HalfMode;
			if (mSourceImage[1] != null) {
				page2 = mSourceImage[1].Page;
				half2 = mSourceImage[1].HalfMode;
			}
			mImageMgr.ImageScalingSync(page1, page2, half1, half2, mSourceImage[0], mSourceImage[1]);
		}
	}

	/**
	 * 画像と表示領域を比較し、はみ出る量を算出します。
	 */
	private void updateOverSize(boolean isResize) {
		// ルーペの大きさを再設定
		mImageView.updateZoomView();

		// ビットマップを調整
		mImageView.updateOverSize(mPageBack, isResize);
	}

	// // オプションメニューが表示される度に呼び出されます
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu2) {
	// boolean ret = super.onPrepareOptionsMenu(menu2);
	// setOptionMenu(menu);
	// }

	// 戻る操作
	private void operationBack() {
		if (mGuideView.getOperationMode()) {
			mGuideView.setOperationMode(false);
			return;
		}
		else if (mListLoading || (mImageLoading && mConfirmBack == false)) {
			if (mImageMgr != null) {
				mImageMgr.setBreakTrigger();
			}
			mTerminate = true;
			return;
		}
		else if (mConfirmBack) {
			// 戻るで確認表示
			showCloseDialog(CloseDialog.LAYOUT_BACK);
		}
		else {
			finishActivity(true);
		}
		return;
	}

	// メニューを開く
	private void openMenu() {
		if (mImageMgr == null || mImageView == null || mMenuDialog != null) {
			return;
		}

		if (mAutoPlay) {
			// オートプレイ中は解除
			setAutoPlay(false);
		}

		Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, true, this);

		// 操作カテゴリ
		mMenuDialog.addSection(res.getString(R.string.operateSec));
		if (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR) {
			// ディレクトリ選択
			mMenuDialog.addItem(DEF.MENU_SEL_DIR_TREE, res.getString(R.string.selDirTreeMenu));
		}
		// ブックマーク選択
		mMenuDialog.addItem(DEF.MENU_SELBOOKMARK, res.getString(R.string.selBookmarkMenu));
		// ブックマーク追加
		mMenuDialog.addItem(DEF.MENU_ADDBOOKMARK, res.getString(R.string.addBookmarkMenu));
		// // ブックマーク選択
		// mMenuDialog.addItem(DEF.MENU_SELBOOKMARK,
		// res.getString(R.string.selBookmarkMenu));
		// // ページ選択
		// mMenuDialog.addItem(DEF.MENU_PAGESEL,
		// res.getString(R.string.pageselMenu));
		// 音操作
		mMenuDialog.addItem(DEF.MENU_NOISE, res.getString(R.string.noiseMenu), mNoiseSwitch != null);
		// 自動再生
		mMenuDialog.addItem(DEF.MENU_AUTOPLAY, res.getString(R.string.playMenu));
		// 画面回転
		// if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota ==
		// DEF.ROTATE_LANDSCAPE) {
		mMenuDialog.addItem(DEF.MENU_ROTATE, res.getString(R.string.rotateMenu));
		// }
		ImageData bm[] = mImageView.getImageBitmap();
		if (bm[0] != null && bm[1] != null) {
			// 共有 (右画像)
			mMenuDialog.addItem(DEF.MENU_SHARER, res.getString(R.string.shareRMenu));
			// 共有 (左画像)
			mMenuDialog.addItem(DEF.MENU_SHAREL, res.getString(R.string.shareLMenu));
		}
		else {
			// 共有
			mMenuDialog.addItem(DEF.MENU_SHARE, res.getString(R.string.shareMenu));
		}
		// 共有一時ファイル削除
		mMenuDialog.addItem(DEF.MENU_DELSHARE, res.getString(R.string.delshareMenu));
		mMenuDialog.addItem(DEF.MENU_SETTHUMB, res.getString(R.string.setThumb));
		mMenuDialog.addItem(DEF.MENU_SETTHUMBCROPPED, res.getString(R.string.setThumbCropped));

		// 一時設定
		mMenuDialog.addSection(res.getString(R.string.settingSec));
		// イメージ表示設定
		mMenuDialog.addItem(DEF.MENU_IMGCONF, res.getString(R.string.imgConfMenu));
//		// 画像補間方式
//		mMenuDialog.addItem(DEF.MENU_IMGALGO, res.getString(R.string.algoriMenu));
		// 画像回転
		mMenuDialog.addItem(DEF.MENU_IMGROTA, res.getString(R.string.imgRotaMenu));
//		// 見開き設定
//		mMenuDialog.addItem(DEF.MENU_IMGVIEW, res.getString(R.string.tguide02));
//		// 画像サイズ
//		mMenuDialog.addItem(DEF.MENU_IMGSIZE, res.getString(R.string.tguide03));
//		// 余白削除
//		mMenuDialog.addItem(DEF.MENU_MGNCUT, res.getString(R.string.mgnCutMenu), mMgnCut > 0);
//		// シャープ化
//		mMenuDialog.addItem(DEF.MENU_SHARPEN, res.getString(R.string.sharpenMenu), mSharpen);
//		// 色反転
//		mMenuDialog.addItem(DEF.MENU_INVERT, res.getString(R.string.invertMenu), mInvert);
//		// グレースケール
//		mMenuDialog.addItem(DEF.MENU_GRAY, res.getString(R.string.grayMenu), mGray);
		// ページ逆順
		mMenuDialog.addItem(DEF.MENU_REVERSE, res.getString(R.string.reverseMenu), mReverseOrder);
		// 開き方向入れ替え
		mMenuDialog.addItem(DEF.MENU_CHG_OPE, res.getString(R.string.chgOpeMenu), mChgPage);
		// 表紙方向
		mMenuDialog.addItem(DEF.MENU_PAGEWAY, res.getString(R.string.pageWayMenu), res.getString(R.string.pageWayMenuSub1), res.getString(R.string.pageWayMenuSub2), mPageWay == DEF.PAGEWAY_RIGHT ? 0 : 1);
		// スクロール方向入れ替え
		mMenuDialog.addItem(DEF.MENU_SCRLWAY, res.getString(R.string.scrlWayMenu), res.getString(R.string.scrlWayMenuSub1), res.getString(R.string.scrlWayMenuSub2), mScrlWay == DEF.SCRLWAY_H ? 0 : 1);

		// 一時設定
		mMenuDialog.addSection(res.getString(R.string.otherSec));
		// ヘルプ
		mMenuDialog.addItem(DEF.MENU_ONLINE, res.getString(R.string.onlineMenu));
		// 操作確認
		mMenuDialog.addItem(DEF.MENU_HELP, res.getString(R.string.helpMenu), mGuideView.getOperationMode());
		// 上部選択メニュー設定
		mMenuDialog.addItem(DEF.MENU_TOP_SETTING, res.getString(R.string.setTopMenu));
		// ツールバー設定
		mMenuDialog.addItem(DEF.MENU_EDIT_TOOLBAR, res.getString(R.string.ToolbarEditToolbar));
		// 設定
		mMenuDialog.addItem(DEF.MENU_SETTING, res.getString(R.string.setMenu));
		// バージョン情報
		mMenuDialog.addItem(DEF.MENU_ABOUT, res.getString(R.string.aboutMenu));

		mMenuDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
	}

	// メニューを開く
	private void openDirTreeMenu() {
		if (mImageMgr == null || mImageView == null || mMenuDialog != null) {
			return;
		}

		// 文書情報を表示
		mGuideView.setPageText(mImageMgr.createPageStr(mSelectPage));
		mGuideView.setPageColor(0x80000000);

		Resources res = getResources();
		DirTreeDialog mMenuDialog = new DirTreeDialog(this, R.style.MyDialog, true, false, false, true, this);

		// タイトル
		mMenuDialog.addSection(res.getString(R.string.selDirTreeMenu));
		// ファイルリスト
		mMenuDialog.addFileList(mImageMgr.getList());

		mMenuDialog.show();

	}

	// メニューを開く
	private void openBookmarkMenu() {
		if (mImageMgr == null || mImageView == null || mMenuDialog != null) {
			return;
		}

		Resources res = getResources();
		TabDialogFragment mMenuDialog = new TabDialogFragment(this, R.style.MyDialog, true, this);

		// ブックマーク選択
		mMenuDialog.addSection(res.getString(R.string.selBookmarkMenu));

		ArrayList<RecordItem> list = RecordList.load(null, RecordList.TYPE_BOOKMARK, mServer, mPath, mFileName);

		boolean isAdd = false;
		for (int i = 0; i < list.size(); i++) {
			// ブックマーク追加
			RecordItem data = list.get(i);
			String image = data.getImage();
			for (int j = 0; j < mImageMgr.mFileList.length; j++) {
				if (mImageMgr.mFileList[j].name.equals(image)) {
					mMenuDialog.addItem(DEF.MENU_BOOKMARK + j, data.getDispName(), "P." + (j + 1));
					isAdd = true;
					break;
				}
			}
		}
		if (isAdd) {
			mMenuDialog.show(getSupportFragmentManager(), TabDialogFragment.class.getSimpleName());
		}
		else {
			Toast.makeText(this, R.string.bmNotFound, Toast.LENGTH_SHORT).show();
			mMenuDialog = null;
		}
	}

	@Override
	public void onSelectMenuDialog(int id) {
		mMenuDialog = null;

		execCommand(id);
	}

	@Override
	public void onCloseMenuDialog() {
		// メニュー終了
		mMenuDialog = null;
		// 情報表示クリア
		mGuideView.setPageText(null);
		mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
		mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
	}

	private void execCommand(int id) {
		// メニュー選択
		// ページ戻りにはしない

		// ページ番号入力が開いていたら閉じる
		if (PageSelectDialog.mIsOpened == true) {
			mPageDlg.dismiss();
		}
		// サムネイルページ選択が開いていたら閉じる
		if (PageThumbnail.mIsOpened == true) {
			mThumbDlg.dismiss();
		}

		mPageBack = false;
		switch (id) {
			case DEF.MENU_IMGCONF: {
				// 画像表示設定
				showImageConfigDialog(DEF.MENU_IMGCONF);
				break;
			}
			case DEF.MENU_IMGALGO: {
				// 画像補間方式
				showSelectList(SELLIST_ALGORITHM);
				break;
			}
			case DEF.MENU_IMGROTA: {
				// 画像回転
				showSelectList(SELLIST_IMG_ROTATE);
				break;
			}
			case DEF.MENU_IMGVIEW: {
				// 見開き設定
				showSelectList(SELLIST_VIEW_MODE);
				break;
			}
			case DEF.MENU_IMGSIZE: {
				// 画像サイズ
				showSelectList(SELLIST_SCALE_MODE);
				break;
			}
			case DEF.MENU_MGNCUT: {
				// 余白削除
				showSelectList(SELLIST_MARGIN_CUT);
				break;
			}
			case DEF.MENU_MGNCUTCOLOR: {
				// 余白削除
				showSelectList(SELLIST_MARGIN_CUTCOLOR);
				break;
			}
			case DEF.MENU_ROTATE: {
				// 画面方向
				showSelectList(SELLIST_SCR_ROTATE);
				break;
			}
			case DEF.MENU_SHARPEN: {
				// シャープ化
				showImageConfigDialog(DEF.MENU_SHARPEN);
				break;
			}
			case DEF.MENU_BRIGHT: {
				// 明るさ補正
				showImageConfigDialog(DEF.MENU_BRIGHT);
				break;
			}
			case DEF.MENU_GAMMA: {
				// ガンマ補正
				showImageConfigDialog(DEF.MENU_GAMMA);
				break;
			}
			case DEF.MENU_BKLIGHT: {
				// バックライト
				showImageConfigDialog(DEF.MENU_BKLIGHT);
				break;
			}
			case DEF.MENU_INVERT: {
				// 白黒反転
				mInvert = mInvert ? false : true;
				setImageConfig();
				setBitmapImage();
				break;
			}
			case DEF.MENU_GRAY: {
				// グレースケール
				mGray = mGray ? false : true;
				setImageConfig();
				setBitmapImage();
				break;
			}
			case DEF.MENU_HELP: {
				boolean flag = !mGuideView.getOperationMode();
				mGuideView.setOperationMode(flag);
				break;
			}
			case DEF.MENU_ONLINE: {
				// 操作方法画面に遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_operate);	// 設定画面
				Intent intent;
				intent = new Intent(ImageActivity.this, HelpActivity.class);
				intent.putExtra("Url", url);
				startActivity(intent);
				break;
			}
			case DEF.MENU_SETTING: {
				// 設定画面に遷移
				if (mImmEnable && mSdkVersion >= 19) {
					int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
					uiOptions &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
					getWindow().getDecorView().setSystemUiVisibility(uiOptions);
				}

				// バックグラウンドでのキャッシュ読み込み停止
				mImageMgr.setCacheSleep(true);

				Intent intent = new Intent(ImageActivity.this, SetConfigActivity.class);
				startActivityForResult(intent, DEF.REQUEST_SETTING);
				break;
			}
			case DEF.MENU_TOP_SETTING: {
				// 上部の設定
				showCheckList();
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
			case DEF.MENU_AUTOPLAY: {
				// オートプレイ中の設定
				setAutoPlay(true);
				startViewTimer(EVENT_AUTOPLAY);
				break;
			}
			case DEF.MENU_SHARE:
			case DEF.MENU_SHARER:
			case DEF.MENU_SHAREL: {
				// 共有
				int page = mCurrentPage;
				if ((id == DEF.MENU_SHAREL && mPageWay == DEF.PAGEWAY_RIGHT) || (id == DEF.MENU_SHARER && mPageWay == DEF.PAGEWAY_LEFT)) {
					page++;
				}
				String path = mImageMgr.decompFile(page);
				if (path != null && path.length() >= 5) {
					String ext = DEF.getFileExt(path);
					String mime = FileData.getMimeType(mActivity, path);

					// インテント起動
					if (id == DEF.MENU_SHARE || id == DEF.MENU_SHARER || id == DEF.MENU_SHAREL) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType(mime);
						// 保存した画像のURIを第二引数に。
						//intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
						Uri uri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName() + ".provider", new File(path));
						intent.putExtra(Intent.EXTRA_STREAM, uri);
						startActivity(intent);
					}
				}
				break;
			}
			case DEF.MENU_DELSHARE: {
				// 共有用一時ファイルを削除する
				mImageMgr.deleteShareCache();
				break;
			}
			case DEF.MENU_REVERSE: {
				// ページを逆順にする
				reverseOrder();
				break;
			}

			case DEF.MENU_SETTHUMB: {
				// サムネイルに設定
				setThumb(mCurrentPage);
				break;
			}
			case DEF.MENU_SETTHUMBCROPPED: {
				//切り出してサムネイルに設定
				setThumbCropped(mCurrentPage);
				break;
			}
			case DEF.MENU_CMARGIN: {
				// 中央に余白を表示
				toggleCenterMargin();
				break;
			}
			case DEF.MENU_CSHADOW: {
				// 中央に影を表示
				toggleCenterShadow();
				break;
			}

			case DEF.MENU_CHG_OPE: {
				// 操作方向の入れ替え
				mChgPage = !mChgPage;
				mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
				// mGuideView.invalidate();
				break;
			}
			case DEF.MENU_PAGEWAY: {
				// ページめくり方向の入れ替え
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					mPageWay = DEF.PAGEWAY_LEFT;
				}
				else {
					mPageWay = DEF.PAGEWAY_RIGHT;
				}

				// ページ基準点
				if (mViewPoint == DEF.VIEWPT_LEFTTOP) {
					mViewPoint = DEF.VIEWPT_RIGHTTOP;
				}
				else if (mViewPoint == DEF.VIEWPT_RIGHTTOP) {
					mViewPoint = DEF.VIEWPT_LEFTTOP;
				}
				else if (mViewPoint == DEF.VIEWPT_LEFTBTM) {
					mViewPoint = DEF.VIEWPT_RIGHTBTM;
				}
				else if (mViewPoint == DEF.VIEWPT_RIGHTBTM) {
					mViewPoint = DEF.VIEWPT_LEFTBTM;
				}
				setMgrConfig(false);
				setViewConfig();

				// イメージ拡大縮小
				ImageScaling();
				this.updateOverSize(false);
				setBitmapImage();
				break;
			}
			case DEF.MENU_SCRLWAY: {
				// スクロール方向の入れ替え
				if (mScrlWay == DEF.SCRLWAY_H) {
					mScrlWay = DEF.SCRLWAY_V;
				}
				else {
					mScrlWay = DEF.SCRLWAY_H;
				}
				setViewConfig();

				// スクロール位置の反映
				this.updateOverSize(false);
				break;
			}
			case DEF.MENU_SEL_DIR_TREE: {
				// ディレクトリ選択ダイアログ表示
				openDirTreeMenu();
				break;
			}
			case DEF.MENU_ADDBOOKMARK: {
				// ブックマーク追加ダイアログ表示
				BookmarkDialog bookmarkDlg = new BookmarkDialog(this, R.style.MyDialog);
				bookmarkDlg.setBookmarkListear(this);
				bookmarkDlg.setName((mCurrentPage + 1) + " / " + mImageMgr.mFileList.length);
				bookmarkDlg.show();
				break;
			}
			case DEF.MENU_SELBOOKMARK: {
				// ブックマーク選択ダイアログ表示
				openBookmarkMenu();
				break;
			}
			case DEF.MENU_EDIT_TOOLBAR: {
				// ツールバー編集ダイアログ表示
				ToolbarEditDialog dialog = new ToolbarEditDialog(this, R.style.MyDialog, mImageView.getWidth(), mImageView.getHeight());
				dialog.show();
				break;
			}
			default: {
				if (id >= DEF.MENU_DIR_TREE) {
					onSelectPage(id - DEF.MENU_DIR_TREE);
				}
				else if (id >= DEF.MENU_BOOKMARK) {
					onSelectPage(id - DEF.MENU_BOOKMARK);
				}
				else {
					// バージョン情報
					mInformation = new Information(this);
					mInformation.showAbout();
					mInformation.checkRecentRelease(mHandler, true);
				}
				break;
			}
		}
	}

	@Override
	public void onSelectPage(int page) {
		boolean debug = false;
		if (!mListLoading && !mImageLoading && !mScrolling) {
			if (mCurrentPage != page) {
				// ページ選択
				mCurrentPage = page;

				// ページ変更時に振動
				startVibrate();
				mPageBack = false;
				if (mScrlNext) {
					mImageView.scrollReset();
				}
				setBitmapImage();
				mPageSelecting = true;
			}
		}
		else {
			mNextPage = page;
		}

		// 文書情報を更新
		mGuideView.setPageText(mImageMgr.createPageStr(mCurrentPage));

		if(debug) {Log.d(TAG, "onSelectPage: currentPage=" + mCurrentPage + ", nextPage=" + mNextPage);}
	}

	@Override
	public void onSelectPageSelectDialog(int menuId) {

		switch (menuId) {
			case DEF.TOOLBAR_DISMISS: {
				if (!mKeepGuide) {
					// 情報表示クリア
					mGuideView.setPageText(null);
					mGuideView.setPageColor(Color.argb(0, 0, 0, 0));
					mGuideView.setGuideIndex(GuideView.GUIDE_NONE);
				}
				mKeepGuide = false;
				break;
			}
			case DEF.TOOLBAR_BOOK_LEFT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 次巻(先頭ページ)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
				}
				else {
					// 前巻(最終ページ)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_BOOK_RIGHT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 前巻(最終ページ)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
				}
				else {
					// 次巻(先頭ページ)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_BOOKMARK_LEFT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 次巻(しおり位置)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXT, false, true);
				}
				else {
					// 前巻(しおり位置)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREV, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_BOOKMARK_RIGHT: {
				if (mPageWay == DEF.PAGEWAY_RIGHT) {
					// 前巻(しおり位置)
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREV, false, true);
				}
				else {
					// 次巻(しおり位置)
					// 次のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXT, false, true);
				}
				break;
			}
			case DEF.TOOLBAR_THUMB_SLIDER: {
				// イメージビュワー専用

				// ダイアログを閉じるときにガイド表示を消さない
				mKeepGuide = true;

				// 表示中の画像が1枚か2枚かを判定
				ImageData bm[] = mImageView.getImageBitmap();
				int shareType;
				if (bm[0] != null && bm[1] != null) {
					shareType = DEF.SHARE_LR;
				}
				else {
					shareType = DEF.SHARE_SINGLE;
				}

				if (mPageSelect == PAGE_INPUT) {

					mPageSelect = PAGE_THUMB;
					Editor ed = mSharedPreferences.edit();
					ed.putString(DEF.KEY_PAGESELECT, String.valueOf(mPageSelect));
					ed.commit();

					// サムネイルページ選択
					if (PageThumbnail.mIsOpened = true) {
						PageThumbnail thumbDlg = new PageThumbnail(this, R.style.MyDialog);
						thumbDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mPageWay == DEF.PAGEWAY_RIGHT, mImageMgr, mThumID, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
						thumbDlg.setPageSelectListear(this);
						thumbDlg.show();
						thumbDlg.setShareType(shareType);
						mThumbDlg = thumbDlg;
					}
				}
				else if (mPageSelect == PAGE_THUMB) {

					mPageSelect = PAGE_INPUT;
					Editor ed = mSharedPreferences.edit();
					ed.putString(DEF.KEY_PAGESELECT, String.valueOf(mPageSelect));
					ed.commit();

					// ページ番号入力
					if (PageSelectDialog.mIsOpened = true) {
						PageSelectDialog pageDlg = new PageSelectDialog(this, R.style.MyDialog);
						pageDlg.setParams(DEF.IMAGE_VIEWER, mCurrentPage, mImageMgr.length(), mPageWay == DEF.PAGEWAY_RIGHT, (mImageMgr.getFileType() == mImageMgr.FILETYPE_ZIP || mImageMgr.getFileType() == mImageMgr.FILETYPE_RAR));
						pageDlg.setPageSelectListear(this);
						pageDlg.show();
						pageDlg.setShareType(shareType);
						mPageDlg = pageDlg;
					}
				}

				break;
			}
			case DEF.TOOLBAR_DIR_TREE: {
				// イメージビュワー専用

				// ダイアログを閉じるときにガイド表示を消さない
				mKeepGuide = true;

				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened == true) {
					mPageDlg.dismiss();
				}
				// サムネイルページ選択が開いていたら閉じる
				if (PageThumbnail.mIsOpened == true) {
					mThumbDlg.dismiss();
				}

				openDirTreeMenu();
				break;
			}
			case DEF.TOOLBAR_TOC: {
				// テキストビュワー専用
				break;
			}
			case DEF.TOOLBAR_FAVORITE: {
				execCommand(DEF.MENU_SELBOOKMARK);
				break;
			}
			case DEF.TOOLBAR_ADD_FAVORITE: {
				execCommand(DEF.MENU_ADDBOOKMARK);
				break;
			}
			case DEF.TOOLBAR_SEARCH: {
				// テキストビュワー専用
				break;
			}
			case DEF.TOOLBAR_SHARE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SHARE);
				break;
			}
			case DEF.TOOLBAR_SHARE_LEFT_PAGE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SHAREL);
				break;
			}
			case DEF.TOOLBAR_SHARE_RIGHT_PAGE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SHARER);
				break;
			}
			case DEF.TOOLBAR_ROTATE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_ROTATE);
				break;
			}
			case DEF.TOOLBAR_ROTATE_IMAGE: {
				// イメージビュワー専用
				execCommand(DEF.MENU_IMGROTA);
				break;
			}
			case DEF.TOOLBAR_SELECT_THUMB: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SETTHUMB);
				break;
			}
			case DEF.TOOLBAR_TRIM_THUMB: {
				// イメージビュワー専用
				execCommand(DEF.MENU_SETTHUMBCROPPED);
				break;
			}
			case DEF.TOOLBAR_CONTROL: {
				execCommand(DEF.MENU_IMGCONF);
				break;
			}
			case DEF.TOOLBAR_MENU: {
				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened == true) {
					mPageDlg.dismiss();
				}
				// サムネイルページ選択が開いていたら閉じる
				if (PageThumbnail.mIsOpened == true) {
					mThumbDlg.dismiss();
				}

				openMenu();
				break;
			}
			case DEF.TOOLBAR_CONFIG: {
				// ページ番号入力が開いていたら閉じる
				if (PageSelectDialog.mIsOpened == true) {
					mPageDlg.dismiss();
				}
				// サムネイルページ選択が開いていたら閉じる
				if (PageThumbnail.mIsOpened == true) {
					mThumbDlg.dismiss();
				}

				execCommand(DEF.MENU_SETTING);
				break;
			}
			case DEF.TOOLBAR_EDIT_TOOLBAR: {
				execCommand(DEF.MENU_EDIT_TOOLBAR);
				break;
			}
		}
	}

	// 他アクティビティからの復帰通知
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mPageBack = false;

		// バックグラウンドでのキャッシュ読み込み再開
		mImageMgr.setCacheSleep(false);

		if (requestCode == DEF.REQUEST_SETTING) {
			// 設定の読込
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

			ReadSetting(sharedPreferences);

			if (mImmEnable && mSdkVersion >= 19) {
				int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
				getWindow().getDecorView().setSystemUiVisibility(uiOptions);
			}

			mImageView.setImageBitmap(null);
			setViewConfig();
			mImageView.updateScreenSize();
			// mImageView.setScaleMode(mScaleMode);

			// 色とサイズを指定
			mGuideView.setColor(mTopColor1, mTopColor2, mMgnColor);
			mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);
			// mGuideView.setRotateMode(mPseLand);
			mGuideView.setPageNumberFormat(mPnumDisp, mPnumFormat, mPnumPos, mPnumSize, mPnumColor);
			mGuideView.setPageNumberString(null);

			setMgrConfig(true);

			// ファイルリストを再作成
			// if (mFileList != null) {
			// mFileList.close();
			// }
			// mFileList = new ImageFileList(mUriPath, mFileName, mFileSort);
			setBitmapImage();
		}
		else if (requestCode == DEF.REQUEST_FILE) {
			setBitmapImage();
		}
		else if (requestCode == DEF.REQUEST_CROP) {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				setThumb(uri);
			}
		}
	}

	// 設定の読み込み
	private void ReadSetting(SharedPreferences sharedPreferences) {
		boolean debug = false;
		// 設定値取得
		try {
			mFileSort = SetImageActivity.getFileSort(sharedPreferences);
			mZoomType = SetImageActivity.getZoomType(sharedPreferences);
			mViewPoint = SetImageText.getViewPt(sharedPreferences);
			mScaleMode = SetImageActivity.getIniScale(sharedPreferences);
			mEffect = SetImageActivity.getEffect(sharedPreferences);

			mScroll = DEF.calcScroll(SetImageTextDetailActivity.getScroll(sharedPreferences));
			mClickArea = DEF.calcClickAreaPix(SetImageTextDetailActivity.getClickArea(sharedPreferences), mSDensity);
			mPageRange = DEF.calcPageRangePix(SetImageTextDetailActivity.getPageRange(sharedPreferences), mSDensity);
			mMoveRange = DEF.calcTapRangePix(SetImageTextDetailActivity.getTapRange(sharedPreferences), mSDensity);
			mLongTapZoom = DEF.calcMSec200(SetImageDetailActivity.getLongTap(sharedPreferences));
			mWAdjust = DEF.calcWAdjust(SetImageDetailActivity.getWAdjust(sharedPreferences));
			mWidthScale = DEF.calcWScaling(SetImageDetailActivity.getWScaling(sharedPreferences));
			mImgScale = DEF.calcScaling(SetImageDetailActivity.getScaling(sharedPreferences));
			if (debug) {Log.d(TAG, "ReadSetting mWidthScale=" + mWidthScale + ", mImgScale=" + mImgScale);}
			
			mEffectTime = DEF.calcEffectTime(SetImageTextDetailActivity.getEffectTime(sharedPreferences));
			mAutoPlayTerm = DEF.calcAutoPlay(SetImageDetailActivity.getAutoPlay(sharedPreferences));
			mPageSelect = SetImageText.getPageSelect(sharedPreferences);

			if (SetImageActivity.getCenterMargin(sharedPreferences)) {
				mCenter = SetImageTextDetailActivity.getCenter(sharedPreferences);
			} 
			else {
				mCenter = 0;
			}
			if (SetImageActivity.getCenterShadow(sharedPreferences)) {
				mShadow = SetImageTextDetailActivity.getGradation(sharedPreferences);
			}
			else {
				mShadow = 0;
			}
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

			mVolScrl = DEF.calcScrlSpeedPix(SetImageTextDetailActivity.getVolScrl(sharedPreferences), mSDensity);
			mScrlWay = SetImageActivity.getScrlWay(sharedPreferences);
			mTapScrl = SetImageText.getTapScrl(sharedPreferences);
			mFlickPage = SetImageText.getFlickPage(sharedPreferences);

			mMgnCut = SetImageActivity.getMgnCut(sharedPreferences);
			mMgnCutColor = SetImageActivity.getMgnCutColor(sharedPreferences);
			mBright = SetImageActivity.getBright(sharedPreferences);
			mGamma = SetImageActivity.getGamma(sharedPreferences);
			mBkLight = SetImageActivity.getBkLight(sharedPreferences);
			mSharpen = SetImageActivity.getSharpen(sharedPreferences);
			mInvert = SetImageActivity.getInvert(sharedPreferences);
			mGray = SetImageActivity.getGray(sharedPreferences);
			mMoire = SetImageActivity.getMoire(sharedPreferences);
			mTopSingle = SetImageActivity.getTopSingle(sharedPreferences);

			mScrlRngW = DEF.calcScrlRange(SetImageDetailActivity.getScrlRngW(sharedPreferences));
			mScrlRngH = DEF.calcScrlRange(SetImageDetailActivity.getScrlRngH(sharedPreferences));

			// バックライト設定
			if (mBkLight <= 10) {
				// バックライト変更
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.screenBrightness = (float)mBkLight / 10;
				getWindow().setAttributes(lp);
			}

			// 処理スレッド数
			mMaxThread = SetImageDetailActivity.getMaxThread(sharedPreferences);
			if (mMaxThread == 0) {
				mMaxThread = Runtime.getRuntime().availableProcessors();
				if (mMaxThread > 7) {
					// ひとつ落とす
					mMaxThread = 7;
				}
				else if (mMaxThread <= 0) {
					mMaxThread = 1;
				}
			}

			mLoupeSize = SetImageDetailActivity.getLoupeSize(sharedPreferences);

			mNoiseScrl = DEF.calcScrlSpeedPix(SetNoiseActivity.getNoiseScrl(sharedPreferences), mSDensity);
			mNoiseUnder = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseUnder(sharedPreferences));
			mNoiseOver = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseOver(sharedPreferences));
			mNoiseLevel = SetNoiseActivity.getNoiseLevel(sharedPreferences);
			mNoiseDec = SetNoiseActivity.getNoiseDec(sharedPreferences);
			if (mNoiseSwitch != null) {
				mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);
			}

			mMgnColor = SetImageTextColorActivity.getMgnColor(sharedPreferences);
			mCenColor = SetImageTextColorActivity.getCntColor(sharedPreferences);
			// mTopColor = 0x60000000 |
			// ((mMgnColor & 0x00010000) != 0 ? 0x00700000 : 0) |
			// ((mMgnColor & 0x00000100) != 0 ? 0x00007000 : 0) |
			// ((mMgnColor & 0x00000001) != 0 ? 0x00000070 : 0);
			mTopColor1 = SetImageTextColorActivity.getGuiColor(sharedPreferences);
			mTopColor2 = 0x40000000 | (mTopColor1 & 0x00FFFFFF);

			mNotice = SetImageActivity.getNotice(sharedPreferences);
			mNoSleep = SetImageActivity.getNoSleep(sharedPreferences);
			mChgPage = SetImageText.getChgPage(sharedPreferences);
			mChgFlick = SetImageText.getChgFlick(sharedPreferences);
			mLastMsg = SetImageText.getLastPage(sharedPreferences);
			mSavePage = SetImageText.getSavePage(sharedPreferences);
			mFitDual = SetImageActivity.getFitDual(sharedPreferences);
			mCMargin = SetImageActivity.getCenterMargin(sharedPreferences);
			mCShadow = SetImageActivity.getCenterShadow(sharedPreferences);
			mNoExpand = SetImageActivity.getNoExpand(sharedPreferences);
			mVibFlag = SetImageText.getVibFlag(sharedPreferences);
			mMomentMode = SetImageTextDetailActivity.getMomentMode(sharedPreferences);
			mFlickEdge = SetImageText.getFlickEdge(sharedPreferences);

			mPrevRev = SetImageText.getPrevRev(sharedPreferences); // ページ戻り時の左右位置反転
			mPageWay = SetImageActivity.getPageWay(sharedPreferences); // ページ方向(右表紙/左表紙)
			mDispMode = SetImageActivity.getInitView(sharedPreferences); // 表示モード(NORMAL/DUAL/HALF/縦横で切替)
			mAlgoMode = SetImageActivity.getAlgoMode(sharedPreferences); // 補間モード
			mDelShare = SetImageActivity.getDelShare(sharedPreferences); // 共有ファイルの削除
			mViewRota = SetImageActivity.getViewRota(sharedPreferences);
			DEF.setRotation(this, mViewRota);
			if (mViewRota == DEF.ROTATE_PSELAND) {
				// 疑似横画面
				mPseLand = true;
			}
			else {
				mPseLand = false;
			}

			mVolKeyMode = SetImageText.getVolKey(sharedPreferences); // 音量キー操作
			mTapPattern = SetImageText.getTapPattern(sharedPreferences); // タップパターン
			mTapRate = SetImageText.getTapRate(sharedPreferences); // タップの比率
			mChgPageKey = SetImageText.getChgPageKey(sharedPreferences); // 左右キー操作入れ替え

			mPnumDisp = SetImageActivity.getPnumDisp(sharedPreferences); // ページ表示有無
			mPnumFormat = SetImageActivity.getPnumFormat(sharedPreferences); // ページ表示書式
			mPnumPos = SetImageActivity.getPnumPos(sharedPreferences); // ページ表示位置
			mPnumSize = DEF.calcPnumSizePix(SetImageActivity.getPnumSize(sharedPreferences), mSDensity); // ページ表示サイズ
			mPnumColor = SetImageActivity.getPnumColor(sharedPreferences); // ページ表示色

			mTimeDisp = SetImageActivity.getTimeDisp(sharedPreferences); // 時刻と充電表示有無
			mTimeFormat = SetImageActivity.getTimeFormat(sharedPreferences); // 時刻と充電表示書式
			mTimePos = SetImageActivity.getTimePos(sharedPreferences); // 時刻と充電表示位置
			mTimeSize = DEF.calcPnumSizePix(SetImageActivity.getTimeSize(sharedPreferences), mSDensity); // 時刻と充電表示サイズ
			mTimeColor = SetImageActivity.getTimeColor(sharedPreferences); // 時刻と充電表示色

			if (mGuideView != null) {
				// 時刻＆バッテリー表示の情報をガイドに設定
				mGuideView.setTimeFormat(mTimeDisp, mTimeFormat, mTimePos, mTimeSize, mTimeColor);
			}
			mConfirmBack = SetImageText.getConfirmBack(sharedPreferences); // 戻るキーで確認メッセージ
			// mResumeOpen = false;

			mHidden = SetCommonActivity.getHiddenFile(sharedPreferences);

			mMemSize = DEF.calcMemSize(SetCacheActivity.getMemSize(sharedPreferences));
			mMemNext = DEF.calcMemPage(SetCacheActivity.getMemNext(sharedPreferences));
			mMemPrev = DEF.calcMemPage(SetCacheActivity.getMemPrev(sharedPreferences));

			mScrlNext = SetImageActivity.getScrlNext(sharedPreferences); // スクロールで次のページへ移動
			mViewNext = SetImageActivity.getViewNext(sharedPreferences); // 次のページを表示
			mNextFilter = SetImageActivity.getNextFilter(sharedPreferences); // 次のページのエフェクト表示

			mRotateBtn = DEF.RotateBtnList[SetCommonActivity.getRotateBtn(sharedPreferences)];

			// アクセス状態表示
			mAccessLamp = SetImageDetailActivity.getAccessLamp(sharedPreferences);

			// 上部メニューの設定を読み込み
			loadTopMenuState();
			if (mGuideView != null) {
				// 上部メニューの文字列情報をガイドに設定
				mGuideView.setTopCommandStr(mCommandStr);
			}

			mEpubOrder = SetEpubActivity.getEpubOrder(sharedPreferences);
		}
		catch (Exception e) {
			Log.e(TAG, "ReadSetting error.");
		}
		return;
	}

	// 拡大位置の座標補正 & 設定
	private void callZoomAreaDraw(float x, float y) {
		// 2011/11/18 ルーペ機能
		mImageView.setZoomPos((int) x, (int) y);
		return;
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

	public void nextPage() {
		// 次ページへ
		if ((mCurrentPage >= mImageMgr.length() - 1 && (!mCurrentPageHalf || (mCurrentPageHalf && mHalfPos == HALFPOS_2ND))) || (mCurrentPage >= mImageMgr.length() - 2 && mCurrentPageDual)) {
			// 分割表示中は最終ページの2ページ目なら次ページに遷移しない
			// 並べて表示中以外は最終ページなら次ページに遷移しない
			// 並べて表示中は最終ページ-1なら次ページに遷移しない

			// 最終ページ
			if (mAutoPlay == true) {
				// 自動再生中は停止
				setAutoPlay(false);
			}
			else if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_LAST);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				if (mUriPath.substring(mUriPath.length() - 1).equals("/")) {
					if (mImageName == null || mImageName.isEmpty()) {
						// ディレクトリオープンのとき
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
					} else {
						// イメージファイル直接オープンのとき
						// 閉じる
						finishActivity(false);
					}
				}
				else {
					// 圧縮ファイルオープンのとき
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_NEXTTOP, false, true);
				}
			}
			else {
				// 閉じる
				finishActivity(false);
			}
			return;
		}

		if (mCurrentPageHalf && mHalfPos != HALFPOS_2ND) {
			// 分割表示の2ページ目表示中
			mHalfPos = HALFPOS_2ND;
		}
		else {
			if (mCurrentPageDual) {
				// 2ページ表示のときは2ページ進む
				mCurrentPage += 2;
			}
			else {
				// 1ページ進む
				mCurrentPage++;
			}
			mHalfPos = HALFPOS_1ST;
		}
		mPageBack = false;
		startVibrate();
		if (mImageView.getPageLock()) {
			mPageSelecting = true;
		}
		setBitmapImage();

	}

	public void prevPage() {
		// 前ページへ
		if (mCurrentPage <= 0 && (!mCurrentPageHalf || (mCurrentPageHalf && mHalfPos != HALFPOS_2ND))) {
			// 先頭ページかつ分割表示中かつ2ページ目でないなら前ページはない
			// 先頭ページかつ分割表示でないなら前ページはない

			// 先頭ページ
			if (mLastMsg == DEF.LASTMSG_DIALOG) {
				showCloseDialog(CloseDialog.LAYOUT_TOP);
			}
			else if (mLastMsg == DEF.LASTMSG_NEXT) {
				if (mUriPath.substring(mUriPath.length() - 1).equals("/")) {
					if (mImageName == null || mImageName.length() == 0) {
						// ディレクトリオープンのとき
						// 前のファイルを開き、続きから記録せず、現在頁保存
						finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
					} else {
						// イメージファイル直接オープンのとき
						// 閉じる
						finishActivity(false);
					}
				}
				else {
					// 圧縮ファイルオープンのとき
					// 前のファイルを開き、続きから記録せず、現在頁保存
					finishActivity(CloseDialog.CLICK_PREVLAST, false, true);
				}
			}
			else {
				// 閉じる
				finishActivity(false);
			}
			return;
		}
		if (mCurrentPageHalf && mHalfPos == HALFPOS_2ND) {
			// 分割表示の2ページ目表示中
			mHalfPos = HALFPOS_1ST;
		}
		else {
			// 1ページ戻る
			if (isDualView() == true) {
				mCurrentPage -= 2;
			}
			else {
				mCurrentPage--;
			}
			mHalfPos = HALFPOS_2ND;
		}
		mPageBack = true;
		startVibrate();
		if (mImageView.getPageLock()) {
			mPageSelecting = true;
		}
		setBitmapImage();
	}

	// 1ページずらし
	private void shiftPage(int way) {
		if (isDualView()) {
			// 見開きモードの時だけ
			if (way < 0) {
				// 1ページ前へ
				if (mCurrentPage > 0) {
					startVibrate();
					mCurrentPage--;
					mPageBack = true;
					setBitmapImage();
				}
			}
			else if (way > 0) {
				// 1ページ次へ
				if (mCurrentPage < mImageMgr.length() - 1) {
					startVibrate();
					mCurrentPage++;
					mPageBack = false;
					setBitmapImage();
				}
			}
		}
	}

	// 現在見開き表示中かを返す
	private boolean isDualView() {
		if (mDispMode == DISPMODE_DUAL) {
			return true;
		}
		else if (mDispMode == DISPMODE_EXCHANGE) {
			if (mViewRota == DEF.ROTATE_PSELAND) {
				return true;
			}
			else {
				if (DEF.checkPortrait(mViewWidth, mViewHeight) == false) {
					// /* getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT */
					return true;
				}
			}
		}
		return false;
	}

	// 現在単ページ表示中かを返す
	private boolean isHalfView() {
		if (mDispMode == DISPMODE_HALF) {
			return true;
		}
		else if (mDispMode == DISPMODE_EXCHANGE) {
			if (mViewRota != DEF.ROTATE_PSELAND) {
				if (DEF.checkPortrait(mViewWidth, mViewHeight) == true) {
					/* getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT */
					return true;
				}
			}
		}
		return false;
	}

	private void startScroll(int move) {
		if (!mListLoading && !mImageLoading && !mScrolling) {
			if (!mImageView.setViewPosScroll(move)) {
				// スクロールする余地がなければ次ページ
//				if (mScrlNext) {
//					mImageView.scrollReset();
//				}
				changePage(move);
			}
			else {
				// スクロール開始
				startViewTimer(EVENT_SCROLL);
			}
		}
	}

	// 長押しタイマー開始
	public boolean startLongTouchTimer(int longtouch_event) {
		int longtaptime;
		if (longtouch_event == EVENT_TOUCH_ZOOM) {
			longtaptime = mLongTapZoom;
		}
		else {
			// 下部押下時のみIMMERSIVEがOFFでも長押しにする(先頭・末尾の誤爆対策)
			if (longtouch_event == EVENT_TOUCH_BOTTOM) {
				longtaptime = LONGTAP_TIMER_BTM;
			}
			else {
				if (mImmEnable == false) {
					return false;
				}
				longtaptime = LONGTAP_TIMER_UI;
			}
		}

		Message msg = mHandler.obtainMessage(longtouch_event);
		msg.arg1 = ++ mLongTouchCount;
		long NextTime = SystemClock.uptimeMillis() + longtaptime;

		mHandler.sendMessageAtTime(msg, NextTime);
//		Log.d("long touch", "send : msg=" + longtouch_event + ", count=" + mLongTouchCount);
		return true;
	}

	// 起動時のプログレスダイアログ表示
	public boolean startDialogTimer(int time) {
		mReadTimerMsg = mHandler.obtainMessage(EVENT_READTIMER);
		long NextTime = SystemClock.uptimeMillis() + time;

		mHandler.sendMessageAtTime(mReadTimerMsg, NextTime);
		return (true);
	}

	// タイマー開始
	public void startViewTimer(int event) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "startViewTimer: 開始します. event=" + event);}

		Message msg = mHandler.obtainMessage(event);
		long NextTime = SystemClock.uptimeMillis();

		if (event == EVENT_EFFECT) {
			// エフェクト開始
			if (mEffectTime > 0) {
				mEffectRate = 0.99f * (mPageBack ? -1 : 1) * (mPageWay != DEF.PAGEWAY_LEFT ? 1 : -1);
				mEffectStart = NextTime;
				// mImageView.setEffectRate(mEffectRate);
				NextTime += EFFECT_TERM;
			}
			else {
				mImageView.setEffectRate(0.0f);
			}
		}
		else if (event == EVENT_SCROLL) {
			// スクロール開始
			if (!mImageView.moveToNextPoint(mVolScrl)) {
				return;
			}
			NextTime += SCROLL_TERM;
			mScrolling = true;
		}
		else if (event == EVENT_AUTOPLAY) {
			// 自動再生
			if (!mAutoPlay) {
				return;
			}
			NextTime += mAutoPlayTerm;
		}
		else {
			// ローディング表示
			NextTime += LOADING_TERM_START;
		}

		mHandler.sendMessageAtTime(msg, NextTime);
	}

	private void setAutoPlay(boolean mode) {
		Window window = getWindow();
		if (mode) {
			// 画面をスリープしないように設定
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		else {
			// 画面をスリープするように戻す
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		mAutoPlay = mode;
	}

	private void showCloseDialog(int layout) {
		if (mCloseDialog != null) {
			return;
		}
		mCloseDialog = new CloseDialog(this, R.style.MyDialog);
		mCloseDialog.setTitleText(layout);
		mCloseDialog.setCloseListear(new CloseListenerInterface() {
			@Override
			public void onCloseSelect(int select, boolean resume, boolean mark) {
				//
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

	@Override
	public void onAddBookmark(String name) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "onAddBookmark: mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
				+ ", mFileName=" + mFileName + ", mImageName=" + mImageName + ", mCurrentPage=" + mCurrentPage);}
		// ブックマーク追加
		if ((mFileName == null || mFileName.isEmpty()) && (mImageName != null && !mImageName.isEmpty())) {
			if (debug) {Log.d(TAG, "onAddBookmark: 画像ファイル指定.");}
				// 画像ファイル直接指定
			RecordList.add(RecordList.TYPE_BOOKMARK, RecordItem.TYPE_IMAGEDIRECT, mServer, mPath, mFileName
					, new Date().getTime(), mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, name);
		}
		else {
			if (debug) {Log.d(TAG, "onAddBookmark: ディレクトリまたは圧縮ファイル.");}
			// ディレクトリまたは圧縮ファイル
			RecordList.add(RecordList.TYPE_BOOKMARK, RecordItem.TYPE_IMAGE, mServer, mPath, mFileName
					, new Date().getTime(), mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, name);
		}
	}

	private void finishActivity(boolean resume) {
		finishActivity(CloseDialog.CLICK_CLOSE, resume, true);
	}

	public void finishActivity(int select, boolean resume, boolean mark) {
		// 続きから読み込みの設定
		if (!resume) {
			removeLastFile();
		}

		if (mark && !mSavePage) {
			// しおりを保存する
			saveCurrentPage();
		} else if (!mark && mSavePage) {
			// しおりを起動時の状態に戻す
			restoreCurrentPage();
		}

		// 履歴保存
		saveHistory();
		mFinishActivity = true;

		// 開いているファイルorディレクトリ
		String lastfile = null;
		String lastpath = null;
		if (mFileName == null || mFileName.isEmpty()) {
			// 圧縮ファイルオープン以外の時
			if (select == CloseDialog.CLICK_CLOSE && mImageName != null && !mImageName.isEmpty()) {
				// クローズかつイメージファイル直接オープンのとき
				lastfile = mImageName;
				lastpath = mPath;
			}
			else {
				// ディレクトリオープンのとき
				if (mPath != null) {
					int plen = mPath.length();
					if (plen > 2) {
						int index = mPath.lastIndexOf('/', plen - 2);
						if (index >= 0) {
							// 先頭以外で/があれば切り出し
							lastfile = mPath.substring(index + 1);
							lastpath = mPath.substring(0, index + 1);
						}
					}
				}
			}
		}
		else {
			lastfile = mFileName;
			lastpath = mPath;
		}

		// 呼び出し元に通知
		Intent intent = new Intent();
		intent.putExtra("NextOpen", select);
		intent.putExtra("LastFile", lastfile);
		intent.putExtra("LastPath", lastpath);
		intent.putExtra("LastUri", lastpath);
		setResult(RESULT_OK, intent);

		mSourceImage[0] = null;
		mSourceImage[1] = null;
		mImageView.setImageBitmap(mSourceImage);
		mImageView.breakThread();

		// ZIPをオープンしていたら閉じる
		if (mImageMgr != null) {
			// 読み込み停止
			mImageMgr.setBreakTrigger();

			// キャッシュ読込スレッド停止
			mImageMgr.closeFiles();
			if (mDelShare) {
				// 共有一時ファイルの削除
				mImageMgr.deleteShareCache();
			}
		}

		if (mImageLoading) {
			mTerminate = true;
			return;
		}
		else {
			// 即終了
			System.gc();
			finish();
		}
	}

	// 現在ページ情報を保存
	private void saveCurrentPage() {
		boolean debug = false;
		if (mImageMgr != null && mImageMgr.length() > 0) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = sp.edit();
			int savePage = mCurrentPage;

			if (mImageMgr.length() <= mCurrentPage + 1) {
				// 既読
				savePage = -2;
			}
			else if (mCurrentPageDual && mImageMgr.length() <= mCurrentPage + 2) {
				// 見開きの場合は1ページ前でも既読
				savePage = -2;
			}
			else if (savePage < 0) {
				// 範囲外は読み込みしない
				savePage = 0;
			}

			if (mImageName != null && !mImageName.isEmpty()) {
				ed.putString("LastImage", mImageMgr.mFileList[mCurrentPage].name);
			}
			if (debug) {Log.d(TAG, "saveCurrentPage: Url=" + DEF.createUrl(mFilePath, mUser, mPass));}
			ed.putInt(DEF.createUrl(mFilePath, mUser, mPass), savePage);
			ed.apply();
		}
	}

	// 起動時のページ情報に戻す
	private void restoreCurrentPage() {
		if (mImageMgr != null && mImageMgr.length() > 0) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = sp.edit();
			if (mRestorePage == -1) {
				ed.remove(DEF.createUrl(mFilePath, mUser, mPass));
			}
			else {
				ed.putInt(DEF.createUrl(mFilePath, mUser, mPass), mRestorePage);
			}

			if (mImageName != null && !mImageName.isEmpty()) {
				ed.putString("LastImage", mImageName);
			}
			ed.apply();
		}
	}

	private void saveLastFile() {
		boolean debug = false;
		Editor ed = mSharedPreferences.edit();
		ed.putInt("LastServer", mServer);
		ed.putString("LastUri", mURI);
		ed.putString("LastPath", mPath);
		ed.putString("LastUser", mUser);
		ed.putString("LastPass", mPass);
		ed.putString("LastFile", mFileName);
		ed.putString("LastImage", mImageName);
		ed.putInt("LastOpen", DEF.LASTOPEN_IMAGE);
		ed.apply();
	}

	private void removeLastFile() {
		Editor ed = mSharedPreferences.edit();
		ed.putInt("LastOpen", DEF.LASTOPEN_NONE);
		ed.apply();
	}

	private void saveHistory() {
		boolean debug = false;
		if (debug) {Log.d(TAG, "saveHistory: mServer=" + mServer + ", mURI=" + mURI + ", mPath=" + mPath
				+ ", mFileName=" + mFileName + ", mImageName=" + mImageName + ", mCurrentPage=" + mCurrentPage);}
		// 履歴追加
		if (!mReadBreak && mImageMgr != null && mImageMgr.length() > 0) {
			if ((mFileName == null || mFileName.isEmpty()) && (mImageName != null && !mImageName.isEmpty())) {
				if (debug) {Log.d(TAG, "saveHistory: 画像ファイル指定.");}
				// 画像ファイル直接指定
				RecordList.add(RecordList.TYPE_HISTORY, RecordItem.TYPE_IMAGEDIRECT
						, mServer, mPath, mFileName, new Date().getTime()
						, mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, null);
			}
			else {
				if (debug) {Log.d(TAG, "saveHistory: ディレクトリまたは圧縮ファイル.");}
				// ディレクトリまたは圧縮ファイル
				RecordList.add(RecordList.TYPE_HISTORY, RecordItem.TYPE_IMAGE
						, mServer, mPath, mFileName, new Date().getTime()
						, mImageMgr.mFileList[mCurrentPage].name, mCurrentPage, null);
			}
		}
	}

	private String getMemoryString() {
		int memoryClass = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
		int largeMemoryClass = 0;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			largeMemoryClass = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getLargeMemoryClass();
		}

		// メモリ情報を取得
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);

		int avaliMem = (int) (memoryInfo.availMem / 1024 / 1024);
		int threshold = (int) (memoryInfo.threshold / 1024 / 1024);
		boolean lowMemory = memoryInfo.lowMemory;

		int nativeAllocate = (int) (Debug.getNativeHeapAllocatedSize() / 1024 / 1024);
		int dalvikTotal = (int) (Runtime.getRuntime().totalMemory() / 1024 / 1024);
		int dalvikFree = (int) (Runtime.getRuntime().freeMemory() / 1024 / 1024);

		int javaAllocate = dalvikTotal - dalvikFree;
		int totalAllocate = nativeAllocate + javaAllocate;

		int ratio = (int)((double) totalAllocate / memoryClass * 100);
		int largeRatio = (int)((double) totalAllocate / largeMemoryClass * 100);

		return Build.BRAND + " " + Build.MODEL + " Android " + Build.VERSION.RELEASE + "\n"
				+ "使用可能メモリ = " + String.valueOf(memoryClass) + " MB\n"
				+ "使用可能メモリ(large) = " + largeMemoryClass + " MB\n"
				+ "native割当済み = " + nativeAllocate + " MB\n"
				+ "java割当済み = " + javaAllocate + " MB\n"
				+ "total割当済み = " + totalAllocate + " MB\n"
				+ "使用率 = " + ratio + "%\n"
				+ "使用率(large) = " + largeRatio + "%\n"
				+ "(dalvik最大メモリ = " + dalvikTotal + " MB)\n"
				+ "(dalvik空きメモリ = " + dalvikFree + " MB)\n"
				+ "availMem = " + avaliMem + " MB\n"
				+ "threshold = " + threshold + " MB\n"
				+ "lowMemory = " + lowMemory;
	}

}
