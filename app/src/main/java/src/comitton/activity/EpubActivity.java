package src.comitton.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import src.comitton.config.SetEpubActivity;
import src.comitton.data.FileData;
import src.comitton.data.RecordItem;
import src.comitton.dialog.BookmarkDialog;
import src.comitton.dialog.ChapterPageSelectDialog;
import src.comitton.dialog.CloseDialog;
import src.comitton.dialog.EpubConfigDialog;
import src.comitton.dialog.Information;
import src.comitton.dialog.InputDialog;
import src.comitton.dialog.ListDialog;
import src.comitton.dialog.MenuDialog;
import src.comitton.dialog.PageSelectDialog;
import src.comitton.filelist.RecordList;
import src.comitton.listener.ChapterPageSelectListener;
import src.comitton.listener.EpubWebViewListener;
import src.comitton.listener.PageSelectListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.epub.EpubReader;
import src.comitton.noise.NoiseSwitch;
import src.comitton.stream.WorkStream;
import src.comitton.view.GuideView;
import src.comitton.view.image.EpubView;
import src.comitton.view.image.EpubWebView;

@SuppressLint("NewApi")
public class EpubActivity extends Activity implements View.OnTouchListener, Handler.Callback, MenuDialog.MenuSelectListener, ChapterPageSelectListener, EpubWebViewListener, BookmarkDialog.BookmarkListenerInterface {
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

    private static final int PAGE_SLIDE = 0;
    private static final int PAGE_INPUT = 1;

    private int RANGE_FLICK;

    private final int TOUCH_NONE      = 0;
    private final int TOUCH_COMMAND   = 1;
    private final int TOUCH_OPERATION = 2;

    private final int COMMAND_RES[] =
            {
                    R.string.rotateMenu,		// 画面方向
                    R.string.selChapterMenu,	// 見出し選択
            };
    private String mCommandStr[];

    private int mPaperSel;
    private int mEpubWidth;
    private int mEpubHeight;

    private int mBodySizeOrg;
    private int mInfoSizeOrg;
    private int mMarginWOrg;
    private int mMarginHOrg;

    private int mBodySize;
    private int mInfoSize;
    private int mMarginW;
    private int mMarginH;

    private int mTextColor;
    private int mBackColor;
    private int mGradColor;
    private int mGradation;

    // 設定値の保持
    private int mClickArea = 16;
    private int mPageRange = 16;
    private int mScroll = 5;
    private int mMoveRange = 12;
    private int mViewPoint;
    private int mMargin;
    private int mTopColor1;
    private int mTopColor2;
    private int mVolKeyMode = VOLKEY_DOWNTONEXT;
    private int mViewRota;
    private int mRotateBtn;
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
    private boolean mChgFlick;
    private boolean mConfirmBack;
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
    private EpubReader mEpubReader = null;
    private Book mEpubBook;

    // ファイル情報
    private int mServer;
    private String mPath;
    private String mLocalPath;
    private String mUser;
    private String mPass;
    private String mFileName;
    private String mEpubName;
    private String mFilePath;
    private int mPage;
    private float mPageRate;
    private int mChapter;

    // ページ表示のステータス情報
    private int mRestorePage;
    private int mRestoreChapter;
    private float mRestorePageRate;
    private int mCurrentPage;
    private int mCurrentChapter;
    private float mCurrentPageRate;
    private int mSelectPage = 0;
    private int mInitFlg = 0; // 初期表示の制御用フラグ

    // 画面を構成する View の保持
    private RelativeLayout mLayout = null;
    private EpubView mEpubView = null;
    private RelativeLayout mInnerLayout = null;
    private EpubWebView mWebView = null;
    private GuideView mGuideView = null;
    private TextView mHeader = null;
    private TextView mFooter = null;

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

    private Message mLongTouchMsg = null;

    private long mPrevVibTime = 0;

    // ビットマップ読み込みスレッドの制御用
    private Handler mHandler;

    private Activity mActivity;
    SharedPreferences mSharedPreferences;
    private float mDensity;
    private int mImmCancelRange;
    private boolean mImmCancel;

    private EpubActivity.EpubLoad mEpubLoad;
    private Thread mEpubThread;

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

    private EpubConfigDialog mEpubConfigDialog;
    private CloseDialog mCloseDialog;
    private ListDialog mListDialog;
    private InputDialog mInputDialog;
    private MenuDialog mMenuDialog;
    private ChapterPageSelectDialog mChapterPageSelectDialog;
    private String mSearchText;

    private boolean mTimeDisp;
    private int mTimeFormat;
    private int mTimePos;
    private int mTimeSize;
    private int mTimeColor;

    private WorkStream mWorkerStream = null;

    List<String> mPagesRef = new ArrayList<>();
    List<String> mPages = new ArrayList<>();
    ArrayList<RecordItem> mBookMarks = new ArrayList<>();
    int mPageNumber = 0;

    /**
     * 画面が作成された時に発生します。
     *
     * @param savedInstanceState
     *            保存されたインスタンスの状態。
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("EpubActivity", "onCreate: 開始します.");

        // 回転
        mInitFlg = 0;
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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

        // EpubView を初期化
        initializeEpubView();
        mGuideView = new GuideView(this);

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
        //mGuideView = new GuideView(this);
        mHeader = (TextView) findViewById(R.id.header);
        mFooter = (TextView) findViewById(R.id.footer);

        mGuideView.setGuideMode(false, mBottomFile, true, mPageSelect, false);
        mEpubView.setView(mHandler, mInnerLayout, mWebView, mGuideView, mHeader, mFooter);
        mEpubView.setMarker(null);
        setConfig();

        // 色とサイズを指定
        mGuideView.setColor(mTopColor1, mTopColor2, 0xffffffff);
        mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);

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
                        mEpubName = path.substring(prev + 1);
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
            mEpubName = intent.getStringExtra("Epub");
            mPage = intent.getIntExtra("Page", -1);
            mChapter = intent.getIntExtra("Chapter", -1);
            mPageRate = intent.getFloatExtra("PageRate", -1f);
            Log.d("EpubActivity", "onCreate: mServer=" + mServer+ ", uri=" + uri + ", mPath=" + mPath + ", mUser=" + mUser + ", mPass=" + mPass + ", mFileName=" + mFileName + ", mEpubName=" + mEpubName + ", mPage=" + mPage);
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

        Log.d("EpubActivity", "onCreate: mRestoreChapter を取得します.");
        //mRestoreChapter = -1;
        mRestoreChapter = mSharedPreferences.getInt(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#chapter", -1);
        Log.d("EpubActivity", "onCreate: mCurrentChapter を設定します.");
        mCurrentChapter = (mChapter != -1) ? mChapter : (mRestoreChapter != -1 ? mRestoreChapter : 0);
        Log.d("EpubActivity", "onCreate: mCurrentChapter=" + mCurrentChapter);
        //mEpubView.setChapter(chapter);
        Log.d("EpubActivity", "onCreate: mRestorePageRate を取得します.");
        mRestorePageRate = mSharedPreferences.getFloat(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#pageRate", -1f);
        Log.d("EpubActivity", "onCreate: pageRate を設定します.");
        mCurrentPageRate = (mPageRate != -1f) ? mPageRate : (mRestorePageRate != -1f ? mRestorePageRate : 0f);
        Log.d("EpubActivity", "onCreate: mCurrentPageRate=" + mCurrentPageRate);
        //mWebView.setPageRate(pageRate);
        mRestorePage = mSharedPreferences.getInt(DEF.createUrl(mFilePath + mEpubName, mUser, mPass), -1);
        mCurrentPage = (mPage != -1) ? mPage : (mRestorePage != -1 ? mRestorePage : 0);
        mEpubView.setOnTouchListener(this);

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
                mTerminate = true;
                mReadBreak = true;

            }
        });

    }

    /**
     * @Override アクティビティ一時停止時に呼び出される
     */
    protected void onPause() {
        Log.d("EpubActivity", "onPause: 開始します.");
        super.onPause();
        if (mNoiseSwitch != null) {
            mNoiseSwitch.recordPause(true);
        }
    }

    /**
     * @Override アクティビティ再開時に呼び出される
     */
    public void onResume() {
        Log.d("EpubActivity", "onResume: 開始します.");
        super.onResume();
        if (mNoiseSwitch != null) {
            mNoiseSwitch.recordPause(false);
        }
    }

    /**
     * @Override アクティビティ停止時に呼び出される
     */
    protected void onStop() {
        Log.d("EpubActivity", "onStop: 開始します.");
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

    // 終了処理
    protected void onDestroy() {
        Log.d("EpubActivity", "onDestroy: 開始します.");
        super.onDestroy();

        if (mNoiseSwitch != null) {
            mNoiseSwitch.recordStop();
            mNoiseSwitch = null;
        }

        if (mWebView != null) {
            mWebView.destroy();
        }
    }

    /**
     * @Override アクティビティ再開時に呼び出される
     */
    public void onRestart(){
        Log.d("EpubActivity", "onRestart: 開始します.");
        super.onRestart();
        // IMM
        if (mImmEnable && mSdkVersion >= 19) {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d("EpubActivity", "onWindowFocusChanged: 開始します.");
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
            cx = mEpubView.getWidth();
            cy = mEpubView.getHeight();
//			}
            if (cx < cy) {
                mEpubWidth = cx;
                mEpubHeight = cy;
            }
            else {
                mEpubWidth = cy;
                mEpubHeight = cx;
            }
        }

        // プログレスダイアログの設定
        if (mInitFlg == 0) {
            mInitFlg = 1;
            startDialogTimer(100);

            mReadRunning = true;
            mEpubLoad = new EpubActivity.EpubLoad(mHandler, this);
            mEpubThread = new Thread(mEpubLoad);
            mEpubThread.start();
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
        Log.d("EpubActivity", "onConfigurationChanged: 開始します.");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("EpubActivity", "dispatchKeyEvent: 開始します.");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int code = event.getKeyCode();
            switch (code) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                {
                    // 次ページへ
                    nextPage();
                    break;
                }
                case KeyEvent.KEYCODE_DPAD_LEFT:
                {
                    // 前ページへ
                    prevPage();
                    break;
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
                    changePage(move);
                    return true;
                }
                case KeyEvent.KEYCODE_SPACE:
                {
                    int meta = event.getMetaState();
                    int move = (meta & KeyEvent.META_SHIFT_ON) == 0 ? 1 : -1;
                    // 読込中の表示
                    changePage(move);
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
        Log.d("EpubActivity", "startLongTouchTimer: 開始します.");
        if (mImmEnable == false) {
            Log.d("EpubActivity", "startLongTouchTimer: mImmEnable == false");
            return false;
        }
        Log.d("EpubActivity", "startLongTouchTimer: mImmEnable == true");

        mLongTouchMsg = mHandler.obtainMessage(longtouch_event);
        long NextTime = SystemClock.uptimeMillis() + LONGTAP_TIMER_UI;

        mHandler.sendMessageAtTime(mLongTouchMsg, NextTime);
        return (true);
    }

    private void initializeEpubView() {
        Log.d("EpubActivity", "initializeEpubView: 開始します.");

        try {

            setContentView(R.layout.epub_activity);

            mLayout = (RelativeLayout) findViewById(R.id.epubLayout);
            if (mLayout == null) {
                Log.e("EpubActivity", "initializeEpubView: レイアウトがnullです.");
            }
            mLayout.setBackgroundColor(Color.WHITE);

            mEpubView = (EpubView) findViewById(R.id.epubView);
            if (mEpubView == null) {
                Log.e("EpubActivity", "initializeEpubView: mEpubViewがnullです.");
            }

            mInnerLayout = (RelativeLayout) findViewById(R.id.innerLayout);
            mWebView = (EpubWebView) findViewById(R.id.webView);
            mWebView.setEpubWebViewListenar(this);

        } catch (Exception e) {
            Log.e("EpubActivity", "initializeEpubView: エラーが発生しました.");
            if (e != null && e.getMessage() != null) {
                Log.e("EpubActivity", "initializeEpubView: エラーメッセージ. " + e.getMessage());
            }
        }
    }

    // 設定の読み込み
    private void ReadSetting(SharedPreferences sharedPreferences) {
        Log.d("EpubActivity", "ReadSetting: 開始します.");
        // 設定値取得
        mViewPoint = SetImageText.getViewPt(sharedPreferences);

        mScroll = DEF.calcScroll(SetImageTextDetailActivity.getScroll(sharedPreferences));
        mClickArea = DEF.calcClickAreaPix(SetImageTextDetailActivity.getClickArea(sharedPreferences), mDensity);
        mPageRange = DEF.calcPageRangePix(SetImageTextDetailActivity.getPageRange(sharedPreferences), mDensity);
        mMoveRange = DEF.calcTapRangePix(SetImageTextDetailActivity.getTapRange(sharedPreferences), mDensity);

        mEffectTime = DEF.calcEffectTime(SetImageTextDetailActivity.getEffectTime(sharedPreferences));
        mPageSelect = PAGE_INPUT;

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

        mTapScrl = SetImageText.getTapScrl(sharedPreferences);

        mNoiseScrl = DEF.calcScrlSpeedPix(SetNoiseActivity.getNoiseScrl(sharedPreferences), mDensity);
        mNoiseUnder = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseUnder(sharedPreferences));
        mNoiseOver = DEF.calcNoiseLevel(SetNoiseActivity.getNoiseOver(sharedPreferences));
        mNoiseLevel = SetNoiseActivity.getNoiseLevel(sharedPreferences);
        mNoiseDec = SetNoiseActivity.getNoiseDec(sharedPreferences);
        if (mNoiseSwitch != null) {
            mNoiseSwitch.setConfig(mNoiseUnder, mNoiseOver, mNoiseDec);
        }

        mTopColor1 = SetImageTextColorActivity.getTxtGuiColor(sharedPreferences);
        mTopColor2 = 0x40000000 | (mTopColor1 & 0x00FFFFFF);

        mChgPage = SetImageText.getChgPage(sharedPreferences);
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
            if (mEpubView != null) {
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
                    cx = mEpubView.getWidth();
                    cy = hardSize.y - dispSize.y + mEpubView.getHeight();
                }
                else {
                    cx = mEpubView.getWidth();
                    cy = mEpubView.getHeight();
                }
                if (cx < cy) {
                    mEpubWidth = cx;
                    mEpubHeight = cy;
                }
                else {
                    mEpubWidth = cy;
                    mEpubHeight = cx;
                }
            }
        }
        else {
            mEpubWidth = DEF.PAPERSIZE[mPaperSel][0];
            mEpubHeight = DEF.PAPERSIZE[mPaperSel][1];
        }
        mBodySizeOrg = SetEpubActivity.getFontBody(sharedPreferences);	// 本文
        mInfoSizeOrg = SetEpubActivity.getFontInfo(sharedPreferences);	// ページ情報など

        mBodySize = DEF.calcFontPix(mBodySizeOrg, mDensity);	// 本文
        mInfoSize = DEF.calcFontPix(mInfoSizeOrg, mDensity);	// ページ情報など

        mMarginWOrg = SetEpubActivity.getMarginW(sharedPreferences);	// 左右余白(設定値)
        mMarginHOrg = SetEpubActivity.getMarginH(sharedPreferences);	// 上下余白(設定値)

        mMarginW = DEF.calcDispMargin(mMarginWOrg);				// 左右余白
        mMarginH = mInfoSize + DEF.calcDispMargin(mMarginHOrg);	// 上下余白

        mViewRota = SetEpubActivity.getViewRota(sharedPreferences);

        mNotice = SetEpubActivity.getNotice(sharedPreferences);
        mNoSleep = SetEpubActivity.getNoSleep(sharedPreferences);
        mEffect = SetEpubActivity.getEffect(sharedPreferences);

        mTextColor = SetImageTextColorActivity.getTvtColor(sharedPreferences);
        mBackColor = SetImageTextColorActivity.getTvbColor(sharedPreferences);
        mGradColor = SetImageTextColorActivity.getTvgColor(sharedPreferences);
        mGradation = SetImageTextColorActivity.getGradation(sharedPreferences);

        mBkLight = SetEpubActivity.getBkLight(sharedPreferences);

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
        Log.d("EpubActivity", "setConfig: 開始します.");
        if (mEpubView != null) {
            boolean result = false;
            mLayout.setBackgroundColor(mBackColor);

            // 背景色の設定
            int[] colors = new int[]{mBackColor, mGradColor};
            GradientDrawable.Orientation orientation = null;
            switch (mGradation) {
                case 0: // lt->rb
                    orientation = null;
                    colors = new int[]{mBackColor, mBackColor};
                    break;
                case 1: // lt->rb
                    orientation = GradientDrawable.Orientation.TL_BR;
                    break;
                case 2: // ct->cb
                    orientation = GradientDrawable.Orientation.TOP_BOTTOM;
                    break;
                case 3: // rt->lb
                    orientation = GradientDrawable.Orientation.TR_BL;
                    break;
                case 4: // rc->lc
                    orientation = GradientDrawable.Orientation.RIGHT_LEFT;
                    break;
                case 5: // rb->lt
                    orientation = GradientDrawable.Orientation.BR_TL;
                    break;
                case 6: // cb->ct
                    orientation = GradientDrawable.Orientation.BOTTOM_TOP;
                    break;
                case 7: // lb->rt
                    orientation = GradientDrawable.Orientation.BL_TR;
                    break;
                case 8: // lc->rc
                    orientation = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
            }
            Drawable drawable = new GradientDrawable(orientation, colors);
            mLayout.setBackground(drawable);

            result = mEpubView.setConfig(mTopColor1, mTextColor, mViewPoint, mBodySizeOrg, mInfoSizeOrg, mMarginW, mMarginH, mMargin, mPrevRev, mPseLand, mEffect, mEffectTime, mFontFile);
            if (result == false) {
                Toast.makeText(this, "open font error:\"" + mFontFile + "\"", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void saveLastFile() {
        Log.d("EpubActivity", "saveLastFile: 開始します.");
        SharedPreferences.Editor ed = mSharedPreferences.edit();
        ed.putInt("LastServer", mServer);
        ed.putString("LastPath", mLocalPath);
        ed.putString("LastUser", mUser);
        ed.putString("LastPass", mPass);
        ed.putString("LastFile", mFileName);
        ed.putString("LastEpub", mEpubName);
        ed.putInt("LastOpen", DEF.LASTOPEN_EPUB);
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
        Log.d("EpubActivity", "onTouch: 開始します.");

        // ************************************
        // TODO: 不要な処理が大量に含まれているため削除が必要
        // ************************************

        if (mEpubView == null || mEpubView.getPageCount() == 0) {
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
            cx = mEpubView.getWidth();
            cy = mEpubView.getHeight();
        }
        else {
            // 疑似横モード
            cx = mEpubView.getHeight();
            cy = mEpubView.getWidth();
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

                if (y > cy - mClickArea) {
                    if (mPageSelect == PAGE_SLIDE) {
                        if (mClickArea <= x && x <= cx - mClickArea) {
                            // ページ選択開始
                            mCurrentPage = mEpubView.getPage(); // 現在ページ取得

                            int sel = GuideView.GUIDE_BCENTER;
                            mSelectPage = mCurrentPage;
                            mGuideView.setGuideIndex(sel);

                            mPageMode = true;
                            mGuideView.setPageColor(mTopColor1);
                            if (mPageSelect == 0) {
                                mGuideView.setPageText(mEpubView.createPageStr(mSelectPage, mFilePath));
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
                    mTouchDrawLeft = (int) mEpubView.getDrawLeft();
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
                                if (mSelectPage != mEpubView.getPageCount() - 1) {
                                    mSelectPage = mEpubView.getPageCount() - 1;
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
                                else if (mSelectPage > mEpubView.getPageCount() - 1) {
                                    // 最大値は最終ページ
                                    mSelectPage = mEpubView.getPageCount() - 1;
                                    // タッチ位置を最終ページとしたときのCurrentPageの位置を求める
                                    mTouchBeginX = x - calcPageSelectRange(mSelectPage);
                                }
                                sel = GuideView.GUIDE_BCENTER;
                            }

                            String strPage = mEpubView.createPageStr(mSelectPage, mFilePath);
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
                        //mEpubView.scrollStart(mTouchBeginX, mTouchBeginY, RANGE_FLICK, mScroll);
                    }

                    if (this.mTouchFirst == false) {
                        // スクロールモード
                        long now = SystemClock.uptimeMillis();
                        //mEpubView.scrollMoveAmount(x - mTouchPoint[0].x, y - mTouchPoint[0].y, mScroll, true);

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
                                    setPage();
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
                            if (mSelectPage != mEpubView.getPageCount() - 1) {
                                mSelectPage = mEpubView.getPageCount() - 1;
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
                            setPage();
                        }
                    }
                    else {
                        switch (index) {
                            case 0:
                                // 1ページ次へずらす
                                // 選択肢にない
                                break;
                            case 1:
                                // 1ページ前へずらす
                                // 選択肢にない
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
                                    mCurrentPage = mEpubView.getPage(); // 現在ページ取得

                                    //  ページ選択ダイアログを表示
                                    mChapterPageSelectDialog = new ChapterPageSelectDialog(this, mImmEnable);
                                    mChapterPageSelectDialog.setParams(mEpubView.getChapter(), mEpubView.getChapterCount(), mCurrentPage, mEpubView.getPageCount(), true);
                                    mChapterPageSelectDialog.setChapterPageSelectListear(this);
                                    mChapterPageSelectDialog.show();

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
                            //if (mTapScrl) {
                                // タップでスクロール
                                int move = next ? 1 : -1;
                                // 読込中の表示
                                //if (!mEpubView.setViewPosScroll(move)) {
                                //    // スクロールする余地がなければ次ページ
                                //    changePage(move);
                                //}
                                //else {
                                //    // スクロール開始
                                //    mEpubView.startScroll();
                                //}
                            //}
                            //else {
                                // タップでスクロールしない
                                if (next) {
                                    // 次ページへ
                                    nextPage();
                                }
                                else {
                                    // 前ページへ
                                    prevPage();
                                }
                            //}
                            // スクロールを停止した場合は処理しない
                            break;
                        }
                    }
                    else {
                        Log.d("EpubActivity", "onTouch: フリック判定開始します.");

                        // スワイプ
                        if (mFlickPage && Math.abs(this.mTouchBeginX - x) > mMoveRange) {

                            if (mFlickEdge && mTouchDrawLeft != (int) mEpubView.getDrawLeft()) {
                                // 端からフリックしないときはページめくりしない
                                ;
                            }
                            else if (this.mTouchBeginX - x > 0) {
                                if (!mChgFlick) {
                                    prevPage();
                                }
                                else {
                                    nextPage();
                                }
                            }
                            else {
                                if (!mChgFlick) {
                                    nextPage();
                                }
                                else {
                                    prevPage();
                                }

                            }

                        }

                        /*
                        int flickPage = mEpubView.checkFlick();

                        if (mFlickPage && mDispMode != DEF.DISPMODE_TX_SERIAL && flickPage != 0) {
                            // 連続表示ではなし
                            // フリックでページ遷移
                            if (mFlickEdge && mTouchDrawLeft != (int) mEpubView.getDrawLeft()) {
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

                         */
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
                                //mEpubView.momentiumStart(x, y, mScroll, sx, sy, (int)term, mMomentMode);
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

    // ページ選択時に表示する文字列を作成
    private float calcPageSelectRange(int page) {
        Log.d("EpubActivity", "calcPageSelectRange: 開始します.");
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

    private void startVibrate() {
        Log.d("EpubActivity", "startVibrate: 開始します.");
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
        Log.d("EpubActivity", "changePage: 開始します.");
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

    private void nextPage() {
        Log.d("EpubActivity", "nextPage: 開始します.");
        mCurrentPage++;
        if (mEpubView.nextPage()) {
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
        Log.d("EpubActivity", "prevPage: 開始します.");
        // 前ページへ
        mCurrentPage--;
        if (mEpubView.prevPage()) {
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

    private void showCloseDialog(int layout) {
        Log.d("EpubActivity", "showCloseDialog: 開始します.");
        if (mCloseDialog != null) {
            return;
        }
        mCloseDialog = new CloseDialog(this);
        mCloseDialog.setTitleText(layout);
        mCloseDialog.setCloseListear(new CloseDialog.CloseListenerInterface() {
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

    private void finishActivity(boolean resume) {
        Log.d("EpubActivity", "finishActivity: 開始します.");
        finishActivity(CloseDialog.CLICK_CLOSE, resume, true);
    }

    private void finishActivity(int select, boolean resume, boolean mark) {
        Log.d("EpubActivity", "finishActivity: 開始します.");
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

        // 解放
        Intent intent = new Intent();
        intent.putExtra("nextopen", select);
        intent.putExtra("lastfile", mEpubName);
        intent.putExtra("lastpath", mLocalPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void removeLastFile() {
        Log.d("EpubActivity", "removeLastFile: 開始します.");
        SharedPreferences.Editor ed = mSharedPreferences.edit();
        ed.putInt("LastOpen", DEF.LASTOPEN_NONE);
        ed.commit();
    }

    private void saveCurrentPage() {
        Log.d("EpubActivity", "saveCurrentPage: 開始します.");

        // 現在ページ情報を保存
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = sp.edit();
        int saveChapter = mEpubView.getChapter();
        int savePage = mEpubView.getPage();
        float savePageRate = mEpubView.getPageRate();

        if (mEpubView.getChapterCount() <= mEpubView.getChapter() + 1) {
            // 既読
            saveChapter = -2;
        }
        else if (saveChapter < 0) {
            // 範囲外は読み込みしない
            saveChapter = 0;
        }
        ed.putInt(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#chapter", saveChapter);

        if (mEpubView.getPageRate() > 1) {
            savePageRate = -2;
        }
        else if (mEpubView.getPageRate() < 0) {
            // 範囲外は読み込みしない
            savePageRate = 0;
        }
        ed.putFloat(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#pageRate", savePageRate);

        if ((mEpubView.getChapterCount() <= mEpubView.getChapter() + 1) && (mEpubView.getPageCount() <= mEpubView.getPage() + 1)){
            // 既読
            savePage = -2;
        }
        //else if (mDispMode == DEF.DISPMODE_TX_DUAL && mEpubView.getPageCount() <= mCurrentPage + 2) {
        //    // 見開きの場合は1ページ前でも既読
        //    savePage = -2;
        //}
        else if (savePage < 0) {
            // 範囲外は読み込みしない
            savePage = 0;
        }
        ed.putInt(DEF.createUrl(mFilePath + mEpubName, mUser, mPass), savePage);
        ed.commit();
    }

    // 起動時のページ情報に戻す
    private void restoreCurrentPage() {
        Log.d("EpubActivity", "restoreCurrentPage: 開始します.");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = sp.edit();
        mRestoreChapter = mSharedPreferences.getInt(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#chapter", -1);

        if (mRestoreChapter == -1) {
            ed.remove(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#chapter");
        }
        else {
            ed.putFloat(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#chapter", mRestoreChapter);
        }
        if (mRestorePageRate == -1) {
            ed.remove(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#pageRate");
        }
        else {
            ed.putFloat(DEF.createUrl(mFilePath + mEpubName, mUser, mPass) + "#pageRate", mRestorePageRate);
        }
        if (mRestorePage == -1) {
            ed.remove(DEF.createUrl(mFilePath + mEpubName, mUser, mPass));
        }
        else {
            ed.putInt(DEF.createUrl(mFilePath + mEpubName, mUser, mPass), mRestorePage);
        }
        ed.commit();
    }

    // 起動時のページ情報に戻す
    private void saveHistory(boolean isSavePage) {
        Log.d("EpubActivity", "saveHistory: 開始します.");
        if (mReadBreak == false && mEpubView != null) {
            int type = RecordItem.TYPE_EPUB;
            mCurrentPage = mEpubView.getPage();
            RecordList.add(RecordList.TYPE_HISTORY, type, mServer, mLocalPath + mFileName
                    , mEpubName, new Date().getTime(), null, mEpubView.getChapter(), mEpubView.getPageRate(), mEpubView.getPage(), null);

            // タスク切り替え時しおりを保存
            if (isSavePage) {
                saveCurrentPage();
            }
        }
    }

    // 座標から選択するページを求める
    private int calcSelectPage(float x) {
        Log.d("EpubActivity", "calcSelectPage: 開始します.");
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

    private int mSelectMode;

    private void showSelectList(int index) {
        Log.d("EpubActivity", "showSelectList: 開始します.");
        if (mListDialog != null) {
            return;
        }
        if (index < 0 || index > mCommandStr.length) {
            // インデックスが範囲外
            return;
        }
        if (index == 1) {
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

        String title = "";
        int selIndex = 0;
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
            default:
                return;
        }
        mListDialog = new ListDialog(this, title, items, selIndex, true, new ListDialog.ListSelectListener() {
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
                                int width = mEpubView.getWidth();
                                int height = mEpubView.getHeight();
                                if (DEF.checkPortrait(width, height) == false) {
                                    isPrePort = false;
                                }
                            }

                            if (mViewRota == DEF.ROTATE_LANDSCAPE) {
                                isAftPort = false;
                            }

                            if (isPrePort == isAftPort) {
                                // 変化がないときは強制的に発生させる
                                // 特になにもしない
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

    public void setChapter(int chapter) {
        Log.d("EpubActivity", "onSelectChapter: 開始します. chapter=" + chapter);
        // 現在ページ
        mCurrentPage = mEpubView.getChapter();
        // ページ選択確定
        if (mCurrentPage != chapter) {
            // ページ変更時に振動
            startVibrate();
            mCurrentChapter = chapter;
            mEpubView.setChapter(chapter);
            if (mChapterPageSelectDialog != null) {
                mChapterPageSelectDialog.setChapter(chapter);
            }
        }
    }

    private void setPageRate(float page) {
        Log.d("EpubActivity", "setPageRate: 開始します. page=" + page);
        mEpubView.setPageRate(page);
    }

    public void setPage(int page) {
        Log.d("EpubActivity", "setPageRate: 開始します. page=" + page);
        mCurrentPage = page;
        setPage();
    }

    private void setPage() {
        Log.d("EpubActivity", "setPageRate: 開始します.");
        mEpubView.setPage(mCurrentPage);
    }

    // 戻る操作
    private void operationBack() {
        Log.d("EpubActivity", "operationBack: 開始します.");
        if (mGuideView.getOperationMode()) {
            mGuideView.setOperationMode(false);
            return;
        }
        else if (mReadRunning) {
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
        Log.d("EpubActivity", "openMenu: 開始します.");
        if (mMenuDialog != null || mEpubView == null) {
            return;
        }

        Resources res = getResources();
//		setOptionMenu(menu);
        mMenuDialog = new MenuDialog(this, mEpubView.getWidth(), mEpubView.getHeight(), true, this);

        mMenuDialog.addSection(res.getString(R.string.operateSec));
        if (mEpubView.getChapterCount() > 0) {
            // 見出し選択
            mMenuDialog.addItem(DEF.MENU_SELCHAPTER, res.getString(R.string.selChapterMenu));
        }
        // ブックマーク選択
        mMenuDialog.addItem(DEF.MENU_SELBOOKMARK, res.getString(R.string.selBookmarkMenu));
        // ブックマーク追加
        mMenuDialog.addItem(DEF.MENU_ADDBOOKMARK, res.getString(R.string.addBookmarkMenu));
        // 検索
        //mMenuDialog.addItem(DEF.MENU_SEARCHTEXT, res.getString(R.string.searchTextMenu));
        //if (mEpubView.getMarker() != null) {
        //    // 検索
        //    mMenuDialog.addItem(DEF.MENU_SEARCHJUMP, res.getString(R.string.searchJumpMenu));
        //}
        // 音操作
        mMenuDialog.addItem(DEF.MENU_NOISE, res.getString(R.string.noiseMenu), mNoiseSwitch != null);
        // 画面回転
        if (mViewRota == DEF.ROTATE_PORTRAIT || mViewRota == DEF.ROTATE_LANDSCAPE) {
            mMenuDialog.addItem(DEF.MENU_ROTATE, res.getString(R.string.rotateMenu));
        }

        mMenuDialog.addSection(res.getString(R.string.settingSec));
        // テキスト表示設定
        mMenuDialog.addItem(DEF.MENU_TXTCONF, res.getString(R.string.settingSec));
        // 見開き設定
        //mMenuDialog.addItem(DEF.MENU_IMGVIEW, res.getString(R.string.tguide02));
        // 画像サイズ
        //mMenuDialog.addItem(DEF.MENU_IMGSIZE, res.getString(R.string.tguide03));
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

    // タップが前/次どちらか判定
    private boolean checkTapDirectionNext(float x, float y, int cx, int cy){
        Log.d("EpubActivity", "checkTapDirectionNext: 開始します.");
        boolean next = false;

        float rate = mTapRate + 1;
        float rcx = cx / 10.0f;
        float rcy = (cy - mClickArea * 2) / 10.0f;
        switch (mTapPattern) {
            case 0:
                Log.d("EpubActivity", "checkTapDirectionNext: mTapPattern=0");
                next = (x >= cx - rcx * rate) ? !mChgPage : mChgPage;
                break;
            case 1:
                Log.d("EpubActivity", "checkTapDirectionNext: mTapPattern=1");
                next = (x <= rcx * rate / 2 || x >= cx - rcx * rate / 2) ? !mChgPage : mChgPage;
                break;
            case 2:
                Log.d("EpubActivity", "checkTapDirectionNext: mTapPattern=2");
                next = (y > cy - mClickArea - rcy * rate) ? !mChgPage : mChgPage;
                break;
            case 3:
                Log.d("EpubActivity", "checkTapDirectionNext: mTapPattern=3");
                next = (y > cy - mClickArea - rcy * rate / 2 || y < mClickArea + rcy * rate / 2) ? !mChgPage : mChgPage;
                break;
        }
        Log.d("EpubActivity", "checkTapDirectionNext: next=" + next);
        return next;
    }

    // メニューを開く
    private void openChapterMenu() {
        Log.d("EpubActivity", "openChapterMenu: 開始します.");

        if (mEpubView == null || mMenuDialog != null) {
            return;
        }

        mMenuDialog = new MenuDialog(this, mEpubView.getWidth(), mEpubView.getHeight(), false, this);

        int size = mEpubView.getChapterCount();

        for (int i = 0 ; i < size; i ++) {
            // 章追加
            String title = mEpubView.getChapterTitle(i);
            String text = mEpubView.getChapterText(i);
            mMenuDialog.addItem(DEF.MENU_CHAPTER + i, title, text.substring(0, Math.min(5, text.length())));
        }
        if (size > 0) {
            mMenuDialog.show();
        }
        else {
            mMenuDialog = null;
        }

    }

    // Bitmap読込のスレッドからの通知取得
    public boolean handleMessage(Message msg) {
        Log.d("EpubActivity", "handleMessage: 開始します.");
        if (mReadTimerMsg == msg) {
            Log.d("EpubActivity", "handleMessage: mReadTimerMsg == msg");
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
                Log.d("EpubActivity", "handleMessage: case EVENT_TOUCH_TOP");
            case EVENT_TOUCH_BOTTOM:
                Log.d("EpubActivity", "handleMessage: case EVENT_TOUCH_BOTTOM");
                if (mLongTouchMsg == msg) {
                    Log.d("EpubActivity", "handleMessage: mLongTouchMsg == msg");
                    // 最新のタイマーの時だけ処理
                    if (mTouchFirst) {
                        Log.d("EpubActivity", "handleMessage: mTouchFirst == true");
                        // 上部の操作エリア
                        mGuideView.eventTouchTimer();
                    }
                }
                return true;
            case DEF.HMSG_TX_PARSE:
                Log.d("EpubActivity", "handleMessage: case HMSG_TX_PARSE");
            case DEF.HMSG_TX_LAYOUT:
                Log.d("EpubActivity", "handleMessage: case HMSG_TX_LAYOUT");
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
                Log.d("EpubActivity", "handleMessage: case MSG_ERROR");
                // 読込中の表示
                Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                return true;

            case MSG_NOISESTATE:
                Log.d("EpubActivity", "handleMessage: case MSG_NOISESTATE");
                // 状態表示
                if (mNoiseSwitch != null) {
                    mGuideView.setNoiseState(msg.arg1, mNoiseLevel ? msg.arg2 : -1);
                }
                return true;

            case MSG_NOISE:
                Log.d("EpubActivity", "handleMessage: case MSG_NOISE");
                // 読込中の表示
                if (msg.arg1 == NOISE_NEXTPAGE) {
                    if (mNoiseScroll != 0) {
                        // スクロール停止
                        mNoiseScroll = 0;
                    }
//					else
                    //if (!mEpubView.setViewPosScroll(1)) {
                    //    // スクロールする余地がなければ次ページ
                    //    nextPage();
                    //}
                    //else {
                    //    // スクロール開始
                    //    mEpubView.startScroll();
                    //}
                }
                else if (msg.arg1 == NOISE_PREVPAGE) {
                    if (mNoiseScroll != 0) {
                        // スクロール停止
                        mNoiseScroll = 0;
                    }
//					else
                    //if (!mEpubView.setViewPosScroll(-1)) {
                    //    // スクロールする余地がなければ前ページ
                    //    prevPage();
                    //}
                    //else {
                    //    // スクロール開始
                    //    mEpubView.startScroll();
                    //}
                }
                else if (msg.arg1 == NOISE_NEXTSCRL || msg.arg1 == NOISE_PREVSCRL) {
                    int way = 1;
                    if (msg.arg1 == NOISE_PREVSCRL) {
                        way = -1;
                    }

                    // 読込中の表示
                    //if (mEpubView.checkScrollPoint() && (mNoiseScroll != 0 && way == mNoiseScroll)) {
//						long nowTime = System.currentTimeMillis();
//						if (mPrevScrollTime + 50 < nowTime || mPrevScrollTime > nowTime || mPrevScrollTime == 0) {
                    //    mEpubView.moveToNextPoint(mNoiseScrl);
//							mPrevScrollTime = nowTime;
//						}
                    //}
                    //else {
                    //    mNoiseScroll = way;
                    //    // 次のポイントへスクロール開始
                    //    if (mEpubView.setViewPosScroll(mNoiseScroll)) {
                    //    }
                    //}
                }
                return true;

            case MSG_LOAD_END:
                Log.d("EpubActivity", "handleMessage: case MSG_LOAD_END");
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
                break;


            case MSG_READ_END:
                Log.d("EpubActivity", "handleMessage: case MSG_READ_END");
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

                mEpubView.setChapter(mCurrentChapter);
                mEpubView.setPageRate(mCurrentPageRate);
                break;
        }
        return false;
    }

    @Override
    public void onCloseMenuDialog() {
        Log.d("EpubActivity", "onCloseMenuDialog: 開始します.");
        // メニュー終了
        mMenuDialog = null;
    }

    @Override
    public void onChapterSearch() {
        Log.d("EpubActivity", "onSelectPage: 開始します.");
        openChapterMenu();
    }

    // EpubWebViewから呼び出す
    public void onChangeMaxpage(int maxpage, int page) {
        if (mChapterPageSelectDialog != null) {
            mChapterPageSelectDialog.setMaxPage(maxpage, page);
        }
    }

    // ChapterPageSelectDialogから呼び出す
    @Override
    public void onSelectChapter(int chapter) {
        Log.d("EpubActivity", "onSelectChapter: 開始します. chapter=" + chapter);
        // 現在ページ
        mCurrentPage = mEpubView.getChapter();
        // ページ選択確定
        if (mCurrentPage != chapter) {
            // ページ変更時に振動
            startVibrate();
            mCurrentChapter = chapter;
            mEpubView.setChapter(chapter);
        }
    }

    // ChapterPageSelectDialogから呼び出す
    @Override
    public void onSelectPage(int page) {
        Log.d("EpubActivity", "onSelectPage: 開始します. page=" + page);
        // 現在ページ
        mCurrentPage = mEpubView.getPage();
        // ページ選択確定
        if (mCurrentPage != page) {
            // ページ変更時に振動
            startVibrate();
            mCurrentPage = page;
            setPage();
        }
    }

    // ChapterPageSelectDialogから呼び出す
    @Override
    public void onSelectPageRate(float pageRate) {
        Log.d("EpubActivity", "onSelectPageRate: 開始します. pageRate=" + pageRate);
        // 現在ページ
        mCurrentPageRate = mEpubView.getPageRate();
        Log.d("EpubActivity", "onSelectPageRate: 開始します. mCurrentPageRate=" + mCurrentPageRate);
        // ページ選択確定
        if (mCurrentPageRate != pageRate) {
            // ページ変更時に振動
            startVibrate();
            mCurrentPageRate = pageRate;
            setPageRate(mCurrentPageRate);
        }
    }

    @Override
    public void onAddBookmark(String name) {
        Log.d("EpubActivity", "onAddBookmark: 開始します. name=" + name);
        // ブックマーク追加
        int type = RecordItem.TYPE_EPUB;
        RecordList.add(RecordList.TYPE_BOOKMARK, type, mServer, mLocalPath + mFileName
                , mEpubName, new Date().getTime(), null, mEpubView.getChapter(), mEpubView.getPageRate(), mEpubView.getPage(), name);
    }

    @Override
    public void onSelectMenuDialog(int id) {
        Log.d("EpubActivity", "onSelectMenuDialog: 開始します.");
        // メニュークローズ
        mMenuDialog = null;

        switch (id) {
            case DEF.MENU_TXTCONF: {
                // テキスト設定
                showEpubConfigDialog();
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
                boolean flag = !mGuideView.getOperationMode();
                mGuideView.setOperationMode(flag);
                break;
            }
            case DEF.MENU_ONLINE: {
                // 操作方法画面に遷移
                Resources res = getResources();
                String url = res.getString(R.string.url_operateepub);	// 設定画面
                Intent intent;
                intent = new Intent(EpubActivity.this, HelpActivity.class);
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
                Intent intent = new Intent(EpubActivity.this, SetConfigActivity.class);
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
                BookmarkDialog bookmarkDlg = new BookmarkDialog(this);
                bookmarkDlg.setBookmarkListear(this);
                bookmarkDlg.setName("[" + (mEpubView.getChapter() + 1) + " / " + mEpubView.getChapterCount() + "] " + (mEpubView.getPage() + 1) + " / " + mEpubView.getPageCount());
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
                mInputDialog = new InputDialog(this, title, mSearchText, new InputDialog.SearchListener() {
                    @Override
                    public void onSearch(String text) {
                        if (text != null && text.length() > 0) {
                            // 検索文字列セット
                            mSearchText = text;
                            mEpubView.searchText(text);
                            mEpubView.setMarker(mEpubView.getMarker());

                            // メニュー表示
                            openSearchMenu();
                        }
                        else {
                            // 検索文字列クリア
                            mSearchText = "";
                            // 検索該当箇所クリア
                            mEpubView.searchClear();
                            mEpubView.setMarker(null);
                        }
                    }

                    @Override
                    public void onCancel() {
                        // 検索文字列クリア
                        mSearchText = "";
                        // 検索該当箇所クリア
                        mEpubView.searchClear();
                        mEpubView.setMarker(null);
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
                    setChapter(id - DEF.MENU_CHAPTER);
                    setPageRate(0f);
                }
                else if (id >= DEF.MENU_BOOKMARK) {
                    int index = id - DEF.MENU_BOOKMARK;
                    setChapter(mBookMarks.get(index).getChapter());
                    setPageRate(mBookMarks.get(index).getPageRate());
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
    private void showEpubConfigDialog() {
        Log.d("EpubActivity", "showTextConfigDialog: 開始します.");
        if (mEpubConfigDialog != null) {
            return;
        }
        mEpubConfigDialog = new EpubConfigDialog(this);

        mEpubConfigDialog.setConfig(mBkLight, mBodySizeOrg, mInfoSizeOrg, mMarginWOrg, mMarginHOrg, mIsConfSave);
        mEpubConfigDialog.setEpubConfigListner(new EpubConfigDialog.EpubConfigListenerInterface() {
            @Override
            public void onButtonSelect(int select, int bklight, int body, int info, int marginw, int marginh, boolean issave) {
                // 選択状態を通知
                boolean ischange = false;
                // 変更があるかを確認(適用後のキャンセルの場合も含む)
                if (mBodySizeOrg != body || mInfoSizeOrg != info || mMarginWOrg != marginw || mMarginHOrg != marginh) {
                    ischange = true;
                }
                mBodySizeOrg = body;
                mInfoSizeOrg = info;
                mMarginWOrg = marginw;
                mMarginHOrg = marginh;
                mIsConfSave = issave;

                mBodySize = DEF.calcFontPix(body, mDensity);
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
                    setConfig();
                }

                if (issave) {
                    // 設定を指定
                    SharedPreferences.Editor ed = mSharedPreferences.edit();
                    ed.putString(DEF.KEY_TX_BKLIGHT, Integer.toString(mBkLight));
                    ed.putInt(DEF.KEY_TX_FONTBODY, mBodySizeOrg);
                    ed.putInt(DEF.KEY_TX_FONTINFO, mInfoSizeOrg);
                    ed.putInt(DEF.KEY_TX_MARGINW, mMarginWOrg);
                    ed.putInt(DEF.KEY_TX_MARGINH, mMarginHOrg);
                    ed.commit();
                }
            }

            @Override
            public void onClose() {
                // 終了
                mEpubConfigDialog = null;
            }
        });
        mEpubConfigDialog.show();
    }

    // メニューを開く
    private void openBookmarkMenu() {
        Log.d("EpubActivity", "openBookmarkMenu: 開始します.");
        if (mEpubView == null || mMenuDialog != null) {
            return;
        }

        mMenuDialog = new MenuDialog(this, mEpubView.getWidth(), mEpubView.getHeight(), false, this);

        mBookMarks = RecordList.load(null, RecordList.TYPE_BOOKMARK, mServer, mLocalPath + mFileName, mEpubName);

        for (int i = 0 ; i < mBookMarks.size(); i ++) {
            // ブックマーク追加
            RecordItem data = mBookMarks.get(i);
            int chapter = data.getChapter();
            int page = data.getPage();
            mMenuDialog.addItem(DEF.MENU_BOOKMARK + i, data.getDispName(), "[" + (chapter + 1) + "] P." + (page + 1));
        }
        if (mBookMarks != null && mBookMarks.size() > 0) {
            mMenuDialog.show();
        }
        else {
            mMenuDialog = null;
        }
    }


    // メニューを開く
    private void openSearchMenu() {
        Log.d("EpubActivity", "openSearchMenu: 開始します.");
        if (mEpubView == null || mMenuDialog != null) {
            return;
        }

        EpubView.ChapterData[] mdlist = mEpubView.getSearchList();
        if (mdlist == null || mdlist.length == 0) {
            // 該当なし
            return;
        }

        mMenuDialog = new MenuDialog(this, mEpubView.getWidth(), mEpubView.getHeight(), false, true, this);

        for (int i = 0; i < mdlist.length; i ++) {
            // 検索結果表示
            int page = mdlist[i].getPage();
            if (page >= 0) {
                mMenuDialog.addItem(DEF.MENU_CHAPTER + page, "P." + (page + 1));
            }
        }
        mMenuDialog.show();
    }

    // 他アクティビティからの復帰通知
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEF.REQUEST_SETTING || requestCode == DEF.REQUEST_HELP) {
            // 設定の読込
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            ReadSetting(sharedPreferences);

            setConfig();
        }
    }

    // テキストを再読み込み
    public void reloadText() {
        Log.d("EpubActivity", "reloadText: 開始します.");
        if (mImmEnable && mSdkVersion >= 19) {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }

        // 現在ページ
        mCurrentPage = mEpubView.getPage();

        //mEpubView.setDispMode(mDispMode);
        mGuideView.setGuideMode(false, mBottomFile, true, mPageSelect, false);
        setConfig();
        //mEpubView.setColor(mTextColor, mBackColor, mGradColor, mGradation, mSrchColor);
        //mEpubView.updateScreenSize();
        mEpubView.setMarker(null);

        // 色とサイズを指定
        mGuideView.setColor(mTopColor1, mTopColor2, 0xffffffff);
        mGuideView.setGuideSize(mClickArea, mTapPattern, mTapRate, mChgPage, mOldMenu);

        // プログレスダイアログ準備
        mReadDialog = new ProgressDialog(this);
        mReadDialog.setMessage(mParsingMsg + " (0)");
        mReadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mReadDialog.setCancelable(true);
        mReadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
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

        mEpubView.loadPage();
    }

    // 起動時のプログレスダイアログ表示
    public boolean startDialogTimer(int time) {
        mReadTimerMsg = mHandler.obtainMessage(EVENT_READTIMER);
        long NextTime = SystemClock.uptimeMillis() + time;

        mHandler.sendMessageAtTime(mReadTimerMsg, NextTime);
        return (true);
    }

    public class EpubLoad implements Runnable {
        private Handler handler;
        private Activity mActivity;

        public EpubLoad(Handler handler, EpubActivity activity) {
            super();
            this.handler = handler;
            this.mActivity = activity;

        }

        public void run() {
            try {
                if (mFilePath.length() >= 1 && mFilePath.substring(0, 1).equals("/")) {
                    // ローカルパス
                    mWorkerStream =  new WorkStream("", mFilePath + mEpubName, mUser, mPass, false);
                }
                else if (mFilePath.length() >= 6 && mFilePath.substring(0, 6).equals("smb://")) {
                    // サーバパス
                    mWorkerStream = new WorkStream(mFilePath + mEpubName, "", mUser, mPass, false);
                }

                mWebView.setStream(mWorkerStream);
            } catch (Exception e) {
                Log.e("EpubActivity", "run: エラーが発生しました.");
                if (e != null && e.getMessage() != null) {
                    Log.e("EpubActivity", "run: エラーメッセージ. " + e.getMessage());
                }
            }
            // ファイルリストの読み込み
            // 終了通知
            Message message = new Message();
            message.what = MSG_READ_END;
            handler.sendMessage(message);
        }
    }

    private RectF getGradationRect(float l, float t, float r, float b, int gradation) {
        if (gradation <= 0 || gradation > 8) {
            return null;
        }

        RectF rc = new RectF();
        // left
        switch (gradation) {
            case 3: // rt->lb
            case 4: // rc->lc
            case 5: // rb->lt
                rc.left = r;
                rc.right = l;
                break;
            case 7: // lb->rt
            case 8: // lc->rc
            case 1: // lt->rb
                rc.left = l;
                rc.right = r;
                break;
            case 2: // ct->cb
            case 6: // cb->ct
                rc.left = (r - l) / 2;
                rc.right = (r - l) / 2;
                break;
        }
        switch (gradation) {
            case 2: // ct->cb
            case 3: // rt->lb
            case 1: // lt->rb
                rc.top = t;
                rc.bottom = b;
                break;
            case 5: // rb->lt
            case 6: // cb->ct
            case 7: // lb->rt
                rc.top = b;
                rc.bottom = t;
                break;
            case 4: // rc->lc
            case 8: // lc->rc
                rc.top = (b - t) / 2;
                rc.bottom = (b - t) / 2;
                break;
        }
        return rc;
    }

}
