package src.comitton.view.image;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.common.WaitFor;
import src.comitton.dialog.PageThumbnail;
import src.comitton.stream.CallImgLibrary;
import src.comitton.stream.ImageManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class ThumbnailView extends View implements Runnable, Callback {
	private final int MESSAGE_INIT = 3001;
	private final int MESSAGE_LOADED = 3002;
	private final int MARGIN_WIDTH = 10;
	private final int MARGIN_HEIGHT = 10;

	private final int DRAW_CENTER = 0;
	private final int DRAW_LEFT = 1;
	private final int DRAW_RIGHT = 2;

	private int mMaxPage;
	private int mFirstPage;
	private int mDispPage;
	private int mThumW;
	private int mThumH;
	private long mThumID;
	private int mThumQuality;
	private int mViewWidth;
	private int mTextSize;
	private int mTextTop;
	private int mPageWidth;
	private int mPageHeight;
	private int mSelectWidth;
	private int mDrawTop;
	private int mDrawLast;
	private boolean mReverse;

	private WaitFor mWaitFor;
	private Object mLock;
	private boolean mBreakThread;

	private ImageManager mImageMgr;
	private Handler mHandler;

	private PageThumbnail mParentDlg;
	private HorizontalScrollView mScrollView;
	Paint mRectPaint;
	Paint mDrawPaint;
	Paint mTextPaint;
	Bitmap mDrawBitmap;

	public class PageArea {
		int page;
		int right;
		int left;
	}

	private PageArea mPageArea[];
	private int mPageAreaNum;

	public ThumbnailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRectPaint = new Paint();
		mRectPaint.setColor(Color.LTGRAY);

		mDrawPaint = new Paint();
		mHandler = new Handler(this);
		mDrawTop = mDrawLast = -1;

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTypeface(Typeface.MONOSPACE);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mBreakThread = false;
		mWaitFor = new WaitFor(60000);

		// setDrawingCacheEnabled(false);
	}

	public void initialize(int page, int max, boolean reverse, ImageManager im, PageThumbnail dlg, HorizontalScrollView sv, long thumid, int thumquality) {
		// 最初のページ
		mFirstPage = mDispPage = page;
		mMaxPage = max;
		mReverse = reverse;
		mImageMgr = im;
		mParentDlg = dlg;
		mScrollView = sv;
		mLock = im.getLockObject();
		mThumID = thumid;
		mThumQuality = thumquality;
	}

	public void setPosition(int page) {
		page = calcReversePage(page);
		mScrollView.smoothScrollTo(mSelectWidth * page, 0);
	}

	public int getCurrentPage(int x) {
		int page = -1;
		for (int i = 0; i < mPageAreaNum; i++) {

			if (mPageArea[i].left <= x && x <= mPageArea[i].right) {
				page = mPageArea[i].page;
			}
		}
		return page;
	}

	private int calcReversePage(int page) {
		if (mReverse) {
			// ページ反転
			page = mMaxPage - page - 1;
		}
		return page;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		if (mViewWidth == 0 || mDrawBitmap == null) {
			return;
		}
		int xpos = mScrollView.getScrollX();

		canvas.drawColor(0x40000000);

		mPageAreaNum = 0;

		int width = mSelectWidth;
		int halfWidth = width / 2;

		int page = (xpos + halfWidth) / width;
		int newpage = calcReversePage(page);
		if (newpage != mDispPage) {
			mDispPage = newpage;
			// スレッドにページ変更通知
			mWaitFor.interrupt();
		}
		mDrawTop = mDrawLast = page;

		float rate = ((float) (((xpos + halfWidth) % width) - halfWidth)) / (float) halfWidth;

		Rect rc = drawBitmap(canvas, page, xpos + mViewWidth / 2, MARGIN_HEIGHT, rate, DRAW_CENTER);

		int px1 = rc.left - MARGIN_WIDTH;
		int nx1 = rc.right + MARGIN_WIDTH;
		int xright = xpos + mViewWidth;

		// 前
		for (int i = page - 1; i >= 0 && px1 >= xpos; i--) {
			rc = drawBitmap(canvas, i, px1, MARGIN_HEIGHT, 0, DRAW_RIGHT);
			px1 = rc.left - MARGIN_WIDTH;
			mDrawTop = i;
		}

		// 次
		for (int i = page + 1; i < mMaxPage && nx1 <= xright; i++) {
			rc = drawBitmap(canvas, i, nx1, MARGIN_HEIGHT, 0, DRAW_LEFT);
			nx1 = rc.right + MARGIN_WIDTH;
			mDrawLast = i;
		}
//		Log.d("thview_onDraw", "page=" + page + ", top=" + mDrawTop + ", last=" + mDrawLast);
		return;
	}

	private Rect drawBitmap(Canvas canvas, int page, int x, int y, float rate, int flag) {
//		Log.d("thview_drawBmp", "page=" + page);
		page = calcReversePage(page);

		// int retValue = (200<<16) | 200;
		int retValue = CallImgLibrary.ThumbnailDraw(mThumID, mDrawBitmap, page);
		Rect rcSrc;
		Rect rcDst;

		if (retValue > 0) {
			int w = retValue >> 16;
			int h = retValue & 0xFFFF;
			int w2 = w * mThumQuality;
			int h2 = h * mThumQuality;

			int dstWidth = w2 * mThumH / h2;
			int dstHeight = mThumH;
			int posx = (int) (w2 / 2 * rate);
			rcSrc = new Rect(0, 0, w, h);
			rcDst = new Rect(x - posx, MARGIN_HEIGHT, x - posx + dstWidth, dstHeight + MARGIN_HEIGHT);
			if (flag == DRAW_CENTER) {
				rcDst.offset(w2 / 2 * -1, 0);
			}
			else if (flag == DRAW_RIGHT) {
				rcDst.offset(w2 * -1, 0);
			}
			if (w2 < mPageWidth) {
				if (flag == DRAW_LEFT) {
					rcDst.offset((mPageWidth - w2) / 2, 0);
				}
				else if (flag == DRAW_RIGHT) {
					rcDst.offset((mPageWidth - w2) / 2 * -1, 0);
				}
			}
			canvas.drawBitmap(mDrawBitmap, rcSrc, rcDst, null);

			if (w2 < mPageWidth) {
				rcDst.left -= (mPageWidth - w2) / 2;
				rcDst.right += (mPageWidth - w2) / 2;
			}
		}
		else {
			// 選択ページ
			int posx = (int) (mPageWidth / 2 * rate);
			if (flag == DRAW_CENTER) {
				x -= mPageWidth / 2;
			}
			else if (flag == DRAW_RIGHT) {
				x -= mPageWidth;
			}
			rcDst = new Rect(x - posx, y, x - posx + mPageWidth, y + mPageHeight);
			canvas.drawRect(rcDst, mRectPaint);
		}
		// ページ番号表示
		int tx = (rcDst.left + rcDst.right) / 2;
		String pageStr = "" + (page + 1);
		mTextPaint.setStrokeWidth(2.5f);
		mTextPaint.setStyle(Paint.Style.STROKE);
		mTextPaint.setColor(mFirstPage == page ? 0xFFFF0000 : 0xFF000000);
		canvas.drawText(pageStr, tx, mTextTop, mTextPaint);

		mTextPaint.setStrokeWidth(0.0f);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(Color.WHITE);
		canvas.drawText(pageStr, tx, mTextTop, mTextPaint);

		if (mPageArea != null && mPageAreaNum < mPageArea.length) {
			// 位置の記憶
			mPageArea[mPageAreaNum].page = page;
			mPageArea[mPageAreaNum].left = rcDst.left;
			mPageArea[mPageAreaNum].right = rcDst.right;
			mPageAreaNum++;
		}
		return rcDst;
	}

	public void setLayoutChange(int pw, int ph) {
		// 220spの1/11で20sp
		mTextSize = ph / 11;
		mViewWidth = pw;

		mPageHeight = ph - MARGIN_HEIGHT * 2 - mTextSize;
		mPageWidth = mPageHeight * 1000 / 1414;

		mThumW = mPageWidth * 2;
		mThumH = mPageHeight;
		
		int bcx = mThumW / mThumQuality;
		int bcy = mThumH / mThumQuality;

		// サイズ0のエラー回避
		if (bcx < 1) {
			bcx = 1;
		}
		if (bcy < 1){
			bcy = 1;
		}
		try {
			mDrawBitmap = Bitmap.createBitmap(bcx, bcy, Config.RGB_565);
		}
		catch (Exception e) {
			;
		}

		int num = mViewWidth / mPageWidth + 2;
		mPageArea = new PageArea[num];
		for (int i = 0; i < num; i++) {
			mPageArea[i] = new PageArea();
		}

		mTextPaint.setTextSize(mTextSize);
		FontMetrics fm = mTextPaint.getFontMetrics();
		mTextTop = MARGIN_HEIGHT + mPageHeight - (int) fm.ascent;

		mSelectWidth = ph / 3;
		setMeasuredDimension(mSelectWidth * (mMaxPage - 1) + pw, ph);

		Message message = new Message();
		message.what = MESSAGE_INIT;
		mHandler.sendMessage(message);
		// setPosition(mCurPage);
		// startThumbnail();
	}

	/**
	 * スクロール位置変更通知
	 * 
	 * @param x
	 *            新たな幅
	 * @param y
	 *            新たな高さ
	 * @param oldx
	 *            以前の幅
	 * @param oldy
	 *            以前の高さ
	 */
	public void onScrollChanged(int x, int y, int oldx, int oldy) {
		// リスナー作るのも面倒なので
		int page = calcReversePage((x + mSelectWidth / 2) / mSelectWidth);
		mParentDlg.onScrollChanged(page);
		// 2013.07.11
		invalidate();
	}

	/**
	 * 画面サイズ変更時の通知
	 * 
	 * @param w
	 *            新たな幅
	 * @param h
	 *            新たな高さ
	 * @param oldw
	 *            以前の幅
	 * @param oldh
	 *            以前の高さ
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// int width = mScrollView.getWidth();
		if (w == 0) {
			mPageArea = null;
			mPageAreaNum = 0;
		}
		// else if (mViewWidth != width) {
		// スクロールビューのサイズ変更
		// mViewWidth = width;
		// requestLayout();
		// }
		else {
			// 通知
			// Message message = new Message();
			// message.what = MESSAGE_INIT;
			// mHandler.sendMessage(message);
			// mPageHeight = h - MARGIN_HEIGHT * 2;
			// mPageWidth = mPageHeight * 1000 / 1414;
			// mThumW = mPageWidth * 2;
			// mThumH = mPageHeight;
			// mDrawBitmap = Bitmap.createBitmap(mThumW, mThumH,
			// Config.RGB_565);
			// setPosition(mCurPage);
			//
			// int num = mViewWidth / mPageWidth + 2;
			// mPageArea = new PageArea[num];
			// for (int i = 0 ; i < num ; i ++) {
			// mPageArea[i] = new PageArea();
			// }
			// startThumbnail();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int width = mScrollView.getWidth();
		// int width = MeasureSpec.getSize(widthMeasureSpec);
		if (width > 0) {
			mSelectWidth = height / 3;
			setMeasuredDimension(mSelectWidth * (mMaxPage - 1) + width, height);
		}
		else {
			width = MeasureSpec.getSize(widthMeasureSpec);
			setMeasuredDimension(width, height);
		}
	}

	// サムネイル読み込み
	private void startThumbnail() {
		// サムネイルスレッド開始
		Thread thread = new Thread(this);
		thread.start();
	}

	// 解放
	private void releaseThumbnail() {
		// サムネイル読み込みスレッドを停止
		mBreakThread = true;
		mWaitFor.interrupt();
	}

	// スレッド開始
	public void run() {
		// Log.d("thumb", "Thread - start page:" + mCurPage + ", disp:" +
		// mDispPage);

		// 初期化(2回目以降は無視される)
		CallImgLibrary.ThumbnailInitialize(mThumID, DEF.THUMBNAIL_PAGESIZE, DEF.THUMBNAIL_MAXPAGE, mImageMgr.length());

		int thumDataW = mThumW / mThumQuality;
		int thumDataH = mThumH / mThumQuality;
		int range = DEF.THUMBNAIL_PAGESIZE / DEF.THUMBNAIL_BLOCK / (mThumW * mThumH / DEF.THUMBNAIL_BLOCK + 1); 
		
		int firstindex = -1;

		while (mBreakThread == false) {
			firstindex = mDispPage;
		
			int count;
			boolean prevflag = false;
			boolean nextflag = false;
			for (count = 0 ; count < range ; count ++) {
				if ((prevflag && nextflag) || firstindex != mDispPage) {
					// 範囲オーバー
					break;
				}
				for (int way = 0 ; way < 2 && firstindex == mDispPage ; way ++) {
					if (mBreakThread == true) {
						// スレッド終了
						return;
					}
					int page;
        			if (way == 0) {
        				// 前側
        				page = firstindex - count;
        				if (page < 0) {
       						// 範囲内だけ処理する
        					prevflag = true;
        					continue;
        				}
        			}
        			else {
        				// 後側
        				page = firstindex + count + 1;
        				if (page >= mMaxPage) {
       						// 範囲内だけ処理する
        					nextflag = true;
        					continue;
        				}
        			}    					
		
            		if (page >= 0 && page < mMaxPage && CallImgLibrary.ThumbnailCheck(mThumID, page) == 0) {
            			// ビットマップ読み込み
            			Bitmap bm = null;
            			// Log.d("thumb", "Loop - index:" + index + ", page:" + page);
            
            			try {
            				synchronized (mLock) {
            					// 読み込み処理とは排他する
            					bm = mImageMgr.loadThumbnailFromStream(page, thumDataW, thumDataH);
            				}
            			} catch (Exception ex) {
            				;
            			}
            
            			// ビットマップをサムネイルサイズぴったりにリサイズする
            			if (bm != null) {
            				bm = ImageAccess.resizeTumbnailBitmap(bm, thumDataW, thumDataH, ImageAccess.BMPALIGN_CENTER);
            			}
            			if (bm != null) {
            				int w = bm.getWidth();
            				int h = bm.getHeight();
            				boolean chg = false;
            				if (w > thumDataW) {
            					w = thumDataW;
            					chg = true;
            				}
            				if (h > thumDataH) {
            					h = thumDataH;
            					chg = true;
            				}
            
            				// ビットマップを切り出す
            				if (chg || bm.getConfig() != Config.RGB_565) {
            					Bitmap bm2 = Bitmap.createBitmap(w, h, Config.RGB_565);
            					bm = Bitmap.createBitmap(bm, 0, 0, w, h);
            					//Paint drawBmp = new Paint();
            					Canvas offScreen = new Canvas(bm2);
            					offScreen.drawColor(0xFFFFFFFF);
            					//drawBmp.setStyle(Style.FILL);
            					//offScreen.drawRect(0, 0, w, h, drawBmp);
            					offScreen.drawBitmap(bm, 0, 0, null);
            					bm = bm2;
            					// Log.d("thumb", "w=" + bm2.getWidth() + ", h=" +
            					// bm2.getHeight());
            				}
            			}
            
            			boolean save = false;
            			if (bm != null) {
            				// 空きメモリがあるかをチェック
            				int result = CallImgLibrary.ThumbnailSizeCheck(mThumID, bm.getWidth(), bm.getHeight());
//            				Log.d("thview_run", "sizecheck:page=" + page + ", result=" + result);
            				if (result == 0) {
            					// メモリあり
            					save = true;
            				}
            				else if (result > 0 && Math.abs(page - mDispPage) < 20) {
            					// 表示の中心から外れたものを解放してメモリを空ける
            					result = CallImgLibrary.ThumbnailImageAlloc(mThumID, result, mDispPage);
//                				Log.d("thview_run", "imagealloc:page=" + page + ", disppage=" + mDispPage + ", result=" + result);
            					if (result == 0) {
            						// メモリ獲得成功
            						save = true;	
            					}
            				}
            			}
            			if (bm != null && save) {
//            				Log.d("thview_run", "save:page=" + page);
            				if (CallImgLibrary.ThumbnailSave(mThumID, bm, page) < 0) {
            					bm = null;
            				}
            			}
        
            			// 通知
        				Message message = new Message();
        				message.what = MESSAGE_LOADED;
        				message.arg1 = bm == null ? 0 : 1;
        				message.arg2 = page;
        				mHandler.sendMessage(message);
        			}
        		}
				
            }
			
			if (CallImgLibrary.ThumbnailCheckAll(mThumID) == 0) {
				// 全部読み込めた
				break;
			}
			else if (count >= range || (prevflag && nextflag) || firstindex == mDispPage) {
				// ページ選択待ちに入る
//				Log.d("thview_run", "sleep");
				mWaitFor.sleep();
			}
		}
	}

	public void close() {
		// サムネイル解放
		releaseThumbnail();
	}

	// スクロールタイマーイベント検知処理
	@Override
	public boolean handleMessage(Message msg) {
		// ページ読込通知
		if (msg.what == MESSAGE_INIT) {
			if (this.getWidth() > 0) {
				setPosition(mFirstPage);
				startThumbnail();
			}
			else {
				Message message = new Message();
				message.what = MESSAGE_INIT;
				mHandler.sendMessage(message);
			}
		}
		else if (msg.what == MESSAGE_LOADED) {
			int range1 = calcReversePage(mDrawTop);
			int range2 = calcReversePage(mDrawLast); 
			if (range1 > range2) {
				int wk = range1;
				range1 = range2;
				range2 = wk;
			}
			if (range1 <= msg.arg2 && msg.arg2 <= range2) {
//				Log.d("thview_Loaded", "msg.arg2=" + msg.arg2 + ", top=" + range1 + ", last=" + range2);
				invalidate();
			}
		}
		return false;
	}
}
