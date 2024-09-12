package src.comitton.view.image;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubReader;
import src.comitton.activity.EpubActivity;
import src.comitton.common.DEF;
import src.comitton.data.MarkerDrawData;
import src.comitton.stream.CallTxtLibrary;
import src.comitton.stream.ImageManager;
import src.comitton.view.GuideView;
import src.comitton.view.MyTextView;

public class EpubView extends SurfaceView implements SurfaceHolder.Callback, GuideView.UpdateListener, Runnable {

    private final int PAGEBASE_LEFT = 0;
    private final int PAGEBASE_RIGHT = 1;
    private final int PAGEBASE_CENTER = 2;

    private final int CHINFO_ROTATE = 1;
    private final int CHINFO_REVERSE = 2;
    private final int CHINFO_TURN = 4;
    private final int CHINFO_HALF = 8;

    private int mMgnColor = 0;
    private int mCenColor = 0;
    private int mGuiColor = 0;
    private int mMargin = 0;
    private int mViewPoint = DEF.VIEWPT_RIGHTTOP;
    private boolean mPrevRev = false;
    private boolean mPseLand = false;

    private int mDispWidth = 0;
    private int mDispHeight = 0;


    // 画像が画面からはみ出るサイズ(0～)
    private int mMgnLeft; // 左
    private int mMgnRight; // 右
    private int mMgnTop; // 上
    private int mMgnBottom; // 下

    private float mDrawLeft;
    private float mDrawTop;
    private int mDrawWidth;
    private int mDrawHeight;
    private int mDrawWidthSum = 0;
    private Bitmap mDrawBitmap;
    private boolean mEffectDraw = false; // エフェクト描画中(ビットマップからコピー)

    private float mMomentiumX;
    private float mMomentiumY;
    private long mMomentiumTime;
    private int mMomentiumNum;
    private Message mMomentiumMsg;
    private int mMomentDrain;

    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mCirclePaint;
    private Paint mDrawPaint;
    private Paint mPaperPaint;
    private Paint mBackPaint;
    private Paint mCenterPaint;
    private Paint mMarkerPaint;
    private float mDrawScale;
    private Rect mDstRect;
    private Rect mSrcRect;
    private Path mLinePath;

    // バックグラウンド処理
    private int mDrawPage;
    private EpubView.DrawThread mDrawThread;
    private boolean mMessageBreak;
    private Thread mUpdateThread;
    private boolean mIsRunning;

    private Point mScrollPos[];
    private Point mScrollPoint;
    private boolean mScrolling;

    private String mDrawTitle;

    private int mTextColor;
    private int mBackColor;
    private int mGradColor;
    private int mSrchColor;

    private int mGradation; // グラデーション有効

    private int mTextWidth;
    private int mTextHeight;
    private int mTextInfo;
    private int mTextMarginW;
    private int mTextMarginH;
    private int mFontSize;
    private int mInfoSize;

    private SparseArray<ArrayList<MarkerDrawData>> mMarker;

    private boolean mEffect;
    private float mEffectRate;
    private long mEffectStart;
    private int mEffectTime;

    private long mMoveStart;
    private int mMoveFromLeft;
    private int mMoveFromTop;
    private int mMoveToLeft;

    // イメージ更新処理
    private boolean mIsPageBack = false;
    private boolean mInitialize;

    private Object mLock = new Object();
    private boolean mDrawBreak;

    Message mEventPageMsg;

    // 挿絵の保持
    private String mUser;
    private String mPass;
    private SparseArray<BitmapDrawable> mPicMap1;
    private SparseArray<BitmapDrawable> mPicMap2;
    private int mPicMapPage;
    private ImageManager mImageMgr;

    private float mShiftX[] = { 0.0f, 0.65f, 0.2f, 0.05f, 0.0f, 0.3f, 0.0f, 0.0f, 0.0f, -0.7f, -0.15f};
    private float mShiftY[] = { 0.0f, 0.5f, 0.1f, 0.10f, 0.1f, 0.5f, -0.03f, 0.03f, 0.0f, 0.0f, -0.30f};

    int current_x = 0;

    private MyTextView mTextView = null;
    private RelativeLayout mInnerLayout = null;
    private EpubWebView mWebView = null;
    private GuideView mGuideView = null;
    private TextView mHeader = null;
    private TextView mFooter = null;
    private SurfaceView mSurfaceView = null;

    private boolean mInitialized = false;
    private Handler mHandler;

    private boolean mVisible = false;
    private String mFontFile;
    private Context mContext;
    private int mwidth = -1;
    private int mHeight = -1;

    private EpubReader mEpubReader = null;
    private Book mEpubBook;
    private String mTitle;
    private String mChapterTitle;
    private List<Resource> mChapterResources;
    private Resource mChapterResource;

    private int mChapterCount = -1;
    private int mCurrentChapter = -1;
    private float mPageRate = -1;
    private int mPageCount = -1;
    private int mCurrentPage = -1;

    private EpubView.ChapterManager mChapter;
    private EpubView.ChapterData[] mSearchList;

    public EpubView(Context context) {
        this(context, null);
    }

    public EpubView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    public boolean nextPage() {
        Log.d("EpubView", "nextPage: 開始します.");
        if (!mWebView.nextPage()) {
            // 最終ページ
            return false;
        }
        return true;
    }

    public boolean prevPage() {
        Log.d("EpubView", "prevPage: 開始します.");
        if (!mWebView.prevPage()) {
            // 最終ページ
            return false;
        }
        return true;
    }

    public void loadPage() {
        Log.d("EpubView", "loadPage: 開始します.");
        mWebView.loadPage();
    }

    private void loadAnimation(int scrollX) {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollX", current_x, scrollX);
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    public int getChapterCount() {
        return mWebView.getChapterCount();
    }

    public int getChapter() {
        return mWebView.getChapter();
    }

    public int getPageCount() {
        return mWebView.getPageCount();
    }

    public void setPageRate(float pageRate) {
        mWebView.setPageRate(pageRate);
    }

    public float getPageRate() {
        return mWebView.getPageRate();
    }

    public void setPage(int page) {
        mWebView.setPage(page);
    }

    public int getPage() {
        return mWebView.getPage();
    }

    public int getContentWidth() {
        return mWebView.getContentWidth();
    }

    public int getContentHeight() {
        return mWebView.getContentHeight();
    }

    public void initialize() {
        Log.d("EpubView", "initialize: 開始します.");
        mInitialized = true;
        //updateNotify();
    }

    // 余白色を設定
    public boolean setConfig(int guiColor, int textColor, int viewPoint, int fontSize, int infoSize, int marginW, int marginH, int margin, boolean prevRev, boolean pseLand, boolean effect, int effecttime, String fontFile) {
        Log.d("EpubView", "setConfig: 開始します.");
        boolean result = true;

        mGuiColor = guiColor;
        mTextColor = textColor;
        mViewPoint = viewPoint;
        mFontSize = fontSize;
        mInfoSize = infoSize;
        mMargin = margin;

        mPrevRev = prevRev;
        mPseLand = pseLand;
        mEffect = effect;
        mEffectTime = effecttime;

        // タイトルとページ番号の色とサイズ
        mHeader.setTextColor(mTextColor);
        mHeader.setTextSize(mInfoSize);
        mFooter.setTextColor(mTextColor);
        mFooter.setTextSize(mInfoSize);

        // フォントファイルの場所　/sdcard/ipam.ttf
        // テキストビューにフォントファイルを指定
        mFontFile = fontFile;
        Typeface face = null;
        if (mFontFile != null && mFontFile.length() > 0) {
            try {
                face = Typeface.createFromFile(mFontFile);
            }
            catch (RuntimeException e) {
                result = false;
            }
            if (face != null) {
                mHeader.setTypeface(face);
                mFooter.setTypeface(face);
            }
        }

        if(mTextMarginW != marginW || mTextMarginH != marginH) {
            mTextMarginW = marginW;
            mTextMarginH = marginH;
            // マージンの設定
            int FP = RelativeLayout.LayoutParams.FILL_PARENT;
            int WC = RelativeLayout.LayoutParams.WRAP_CONTENT;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(FP, FP);
            params.setMargins(mTextMarginW, mTextMarginH, mTextMarginW, mTextMarginH);
            mInnerLayout.updateViewLayout(mWebView, params);

            // マージンサイズが変わったらチャプターを取り込みなおす
            mWebView.setChapter(mWebView.getChapter());
        }

        result = mWebView.setConfig(mTextColor, mFontSize, mFontFile, mEffect);

        updateGuide();
        return result;
    }

    public String getFont(){
        return mFontFile;
    }

    // ページ選択時に表示する文字列を作成
    public String createPageStr(int page, String filename) {
        Log.d("EpubView", "createPageStr: 開始します.");
        if (page < 0 || mWebView.getPageCount() <= page) {
            return "";
        }

        String strPath = filename;
        if (strPath.indexOf("smb://") == 0) {
            int idx = strPath.indexOf("@");
            if (idx >= 0) {
                strPath = "smb://" + strPath.substring(idx + 1);
            }
        }

        String pageStr;
        pageStr = (page + 1) + " / " + mWebView.getPageCount() + "\n" + strPath;
        return pageStr;
    }

    public float getDrawLeft() {
        //Log.d("EpubView", "getDrawLeft: 開始します.");
        return mDrawLeft;
    }

    public int checkFlick() {
        Log.d("EpubView", "checkFlick: 開始します.");
        //int overX = mOverScrollX;
        //mOverScrollX = 0;
        //if (Math.abs(overX) * 90 / mOverScrollMax >= 90) {
        //    // 80% 以上引っ張っているとき
        //    // mLastAttenuate = null;
        //    return overX;
        //}
        return 0;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d("EpubView", "surfaceCreated: 開始します.");
    }

    // Surface の属性が変更された際にコールされる
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d("EpubView", "surfaceChanged: 開始します.");
        //((EpubActivity)mContext).reloadText();
        setChapter(getChapter());
    }

    // Surface が破棄された際にコールされる
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d("EpubView", "surfaceDestroyed: 開始します.");
    }

    @Override
    public void run() {
        Log.d("EpubView", "run: 開始します.");
    }

    public void setChapter(int chapter) {
        Log.d("EpubView", "setChapter: 開始します. chapter=" + chapter);
        mWebView.setChapter(chapter);
    }

    public void searchClear() {
        Log.d("EpubView", "searchClear: 開始します.");
        mMarker = null;
    }

    public void searchText(String searchtext) {
        Log.d("EpubView", "searchText: 開始します.");
        /*
        // 検索文字列を小文字にする
        searchtext = searchtext.toLowerCase(Locale.JAPANESE);

        ArrayList<EpubView.ChapterData> mdlist = new ArrayList<EpubView.ChapterData>();
        // マーカー表示
        if (mMarker == null) {
            mMarker = new SparseArray<ArrayList<MarkerDrawData>>();
        }
        else {
            mMarker.clear();
        }
        int searchlen = searchtext.length();
        int lastpage = -1;
        StringBuffer sb = new StringBuffer(searchtext.length() + 100);

        for (int i = mWebView.getPageCount() - 1; i >= 0; i--) {
            TextDrawData[] tdlist = mTextPages[i];
            for (int j = tdlist.length - 1; j >= 0; j--) {
                TextDrawData td = tdlist[j];
                if (td.mIsText == false) {
                    // 画像又はスタイル又はルビ
                    continue;
                }
                sb.insert(0, getTextString(mTextBuff, td.mTextPos, td.mTextLen, td.mExData).toLowerCase(Locale.JAPANESE));

                int pos = sb.length() - searchtext.length();
                for (int k = pos; k >= 0; k--) {
                    if (sb.substring(k).equals(searchtext)) {
                        // ヒット
                        if (lastpage != i) {
                            EpubView.ChapterData md = new EpubView.ChapterData();
                            md.text = "";
                            md.page = i;
                            md.line = 0;
                            mdlist.add(0, md);
                            lastpage = i;
                        }

                        // ハイライト設定
                        setHighlight(i, j, k, searchlen);
                    }
                    sb.setLength(k + searchlen - 1);
                }
            }
        }
        mSearchList = mdlist.toArray(new EpubView.ChapterData[0]);
        */
        return;
    }
    public class ChapterData {
        public String text; // 見出しテキスト
        public int line; // 行位置
        public int page; // ページ位置

        public void setText(String t) {
            Log.d("EpubView", "ChapterData: setText: 開始します.");
            if (text == null) {
                text = t;
            }
            else {
                text = text + t;
            }
        }

        public String getText() {
            Log.d("EpubView", "ChapterData: getText: 開始します.");
            return text;
        }

        public int getPage() {
            Log.d("EpubView", "ChapterData: getPage: 開始します.");
            return page;
        }

        public boolean equals(Object obj) {
            Log.d("EpubView", "ChapterData: equals: 開始します.");

            EpubView.ChapterData md = (EpubView.ChapterData) obj;
            if (this.text.equals(md.text) && this.page == md.page && this.line == md.line) {
                return true;
            }

            return false;
        }
    }

    public int getChapterSize() {
        Log.d("EpubView", "getChapterSize: 開始します.");

        if (mChapter != null && mChapter.chapterlist != null) {
            return mChapter.chapterlist.size();
        }

        return 0;
    }

    public EpubView.ChapterData getChapter(int index) {
        Log.d("EpubView", "getChapter: 開始します.");

        if (mChapter != null && mChapter.chapterlist != null && index < mChapter.chapterlist.size()) {
            return mChapter.chapterlist.get(index);
        }

        return null;
    }

    public EpubView.ChapterData[] getSearchList() {
        Log.d("EpubView", "getSearchList: 開始します.");
        return mSearchList;
    }

    public SparseArray<ArrayList<MarkerDrawData>> getMarker() {
        Log.d("EpubView", "getMarker: 開始します.");
        return mMarker;
    }

    /**
     * TextBuff+ExtDataを文字列化する
     * @param textbuff
     * @param index
     * @param length
     * @param extdata
     * @return
     */

    public String getTextString(char[] textbuff, int index, int length, char[][] extdata) {
        Log.d("EpubView", "getTextString: 開始します.");
        return null;
        /*
        StringBuffer sb = new StringBuffer(length);
        if (index >= 0 && length > 0) {
            int extidx = 0;
            for (int i = 0 ; i < length ; i ++) {
                char code = mTextBuff[index + i];
                if (0x2a00 <= code && code <= 0x2aff) {
                    // 特殊文字対応
                    if (extdata != null && extidx < extdata.length) {
                        sb.append(extdata[extidx++]);
                    }
                }
                else {
                    sb.append(code);
                }
            }
        }
        return sb.toString();
        */
    }

    public String getChapterTitle(int chapter) {
        return mWebView.getChapterTitle(chapter);
    }

    public String getChapterText(int chapter) {
        Log.d("EpubView", "getChapterText: 開始します.");
        return mWebView.getChapterText(chapter);
    }

    public class ChapterManager {
        private ArrayList<EpubView.ChapterData> chapterlist;
        private int current;

        public ChapterManager() {
            // 初期化
            chapterlist = new ArrayList<EpubView.ChapterData>();
            current = 0;
        }

        public EpubView.ChapterData addChapterData(String t, int l) {
            EpubView.ChapterData md = new EpubView.ChapterData();
            md.text = t;
            md.line = l;
            md.page = -1;
            if (chapterlist.indexOf(md) < 0) {
                // 同じものがない場合のみ登録
                chapterlist.add(md);
            }
            return md;
        }

        public void initCurrent() {
            current = 0;
        }

        // レイアウトの行に変換
        public void setLine(int linecnt, int newline) {
            while (current < chapterlist.size()) {
                EpubView.ChapterData md = chapterlist.get(current);
                if (md.line > linecnt) {
                    break;
                }
                if (md.line == linecnt) {
                    md.line = newline;
                }
                current++;
            }
        }

        // ページ確定時にページ番号を設定
        public void setPage(int linecnt, int page) {
            while (current < chapterlist.size()) {
                EpubView.ChapterData md = chapterlist.get(current);
                if (md.line > linecnt) {
                    break;
                }
                if (md.line <= linecnt) {
                    md.page = page;
                }
                current++;
            }
        }
    }

    public void setMarker(SparseArray<ArrayList<MarkerDrawData>> marker) {
        Log.d("EpubView", "setMarker: 開始します.");
        mMarker = marker;
    }

    public class DrawThread implements Runnable {

        private int mPage;
        private int mMaxPage;
        private boolean mThreadBreak;

        // 排他用オブジェクト
        private Object mLock;

        public DrawThread(int page, int maxpage, Object lock) {
            super();
            Log.d("EpubView", "DrawThread: 開始します.");
            mThreadBreak = false;
            mPage = page;
            mMaxPage = maxpage;
            mLock = lock;

            // スレッド起動
            Thread thread = new Thread(this);
            thread.start();
        }

        // スレッド停止
        public void breakThread() {
            Log.d("EpubView", "DrawThread: breakThread: 開始します.");
            mThreadBreak = true;
        }

        // 処理対象ページ
        public int getPage() {
            Log.d("EpubView", "DrawThread: getPage: 開始します.");
            return mPage;
        }

        // スレッド開始
        public void run() {
            Log.d("EpubView", "DrawThread: run: 開始します.");

            int p;

            if (mThreadBreak == true) {
                return;
            }

            int i = 0;
            while (i < CallTxtLibrary.MAX_CACHE_PAGES && mDrawBitmap != null) {
                while (mEffectDraw) {
                    if (mThreadBreak == true) {
                        return;
                    }
                    sleepThread(200);
                }
                sleepThread(50);

                p = mPage + ((i + 1) / 2) * (i % 2 == 1 ? 1 : -1);
                if (p < 0 || p >= mMaxPage) {
                    // 範囲外
                    continue;
                }

                synchronized (mLock) {
                    if (mThreadBreak == true) {
                        return;
                    }
                    if (CallTxtLibrary.CheckTextImage(p) == 0) {
                        //setDrawBitmap(p);
                        if (mDrawBreak) {
                            continue;
                        }
                    }
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // 何もしない
                    ;
                }
                i++;
            }
        }

        // 中断可能なスリープ
        void sleepThread(int t) {
            //Log.d("EpubView", "DrawThread: sleepThread: 開始します.");
            while (t > 0) {
                int tt = t;
                if (tt > 50) {
                    tt = 50;
                }
                t -= tt;
                try {
                    Thread.sleep(tt);
                }
                catch (InterruptedException e) {
                    // 何もしない
                    return;
                }
                if (mThreadBreak == true) {
                    return;
                }
            }
        }
    }

    public void setView(Handler handler, RelativeLayout innerLayout, EpubWebView webView, GuideView guideView, TextView header, TextView footer) {
        Log.d("EpubView", "setView: 開始します.");

        mHandler = handler;
        mInnerLayout = innerLayout;
        mWebView = webView;
        mWebView.setView(mHandler, this);

        mGuideView = guideView;
        guideView.setParentView(this);
        guideView.setUpdateListear(this);

        mHeader = header;
        mFooter = footer;
        setSurfaceView(this);
    }

    // ガイド表示用クラス
    public void setSurfaceView(SurfaceView view) {
        mSurfaceView = view;
        // 半透明を設定
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // コールバック登録
        mSurfaceView.getHolder().addCallback(this);
        // フォーカス可
        setFocusable(true);
        // このViewをトップにする
        mSurfaceView.setZOrderOnTop(true);
    }

    public void drawGuide(Canvas canvas) {
        int cx;
        int cy;

        cx = getWidth();
        cy = getHeight();

        if (mWebView.getChapterCount() != -1 && mWebView.getPageCount() != -1) {
            // canvas の描画内容をクリア
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            Log.d("EpubView", "drawGuide: mTextMarginH=" + mTextMarginH + ", mInfoSize=" + mInfoSize + ", mTitle=" + mWebView.getTitle());
            // タイトル描画
            if (mHeader != null && mWebView.getTitle() != null && mWebView.getTitle().length() != 0) {
                Log.d("EpubView", "drawGuide: mHeader.setText=" + mWebView.getTitle());
                mHeader.setText(mWebView.getTitle());
                mHeader.invalidate();
            }

            Log.d("EpubView", "drawGuide: chapter=" + (mWebView.getChapter() + 1) + ", chapterCount=" + mWebView.getChapterCount() + ", page=" + (mWebView.getPage() + 1) + ", pageCount=" + mWebView.getPageCount());
            // ページ番号描画
            if (mFooter != null) {
                String pagestr = "[" + (mWebView.getChapter() + 1) + " / " + mWebView.getChapterCount() + "] " + (mWebView.getPage() + 1) + " / " + mWebView.getPageCount();
                Log.d("EpubView", "drawGuide: mFooter.setText=" + pagestr);
                mFooter.setText(pagestr);
                mFooter.invalidate();
            }
            // ガイド表示
            if (mGuideView != null) {
                mGuideView.draw(canvas, cx, cy);
            }
        }
    }

    public boolean updateGuide() {
        Log.d("EpubView", "updateGuide: 開始します.");
        Canvas canvas = null;
        SurfaceHolder surfaceHolder = this.getHolder();
        try {
            canvas = surfaceHolder.lockCanvas(); // ロックして、書き込み用のcanvasを受け取る
            if (canvas == null)
                return false; // canvasが受け取れてなかったら抜ける

            drawGuide(canvas);
        }
        catch (Exception e) {
            Log.e("EpubView", "updateGuide: エラーが発生しました.");
            if (e != null && e.getMessage() != null) {
                Log.e("EpubView", "updateGuide: エラーメッセージ. " + e.getMessage());
            }
        } finally {
            if (canvas != null)
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas); // 例外が出て、canvas受け取ってたらロックはずす
                } catch (IllegalStateException e) {
                    // Surface has already been released.
                }
        }
        return true;
    }

    @Override
    public void onUpdate() {
        Log.d("EpubView", "onUpdate: 開始します.");
        // ガイドの更新
        updateGuide();
    }

}