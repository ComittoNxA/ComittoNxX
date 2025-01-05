package src.comitton.view.list;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.listener.DrawNoticeListener;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

public class TitleArea implements Handler.Callback {
	public static final int SELECT_NONE = 0;
	public static final int SELECT_SORT = 1;
	public static final int SELECT_MENU = 2;

	private final int EVENT_SCROLL = 1;
	private final int SCROLL_TERM_FIRST = 1000;
	private final int SCROLL_TERM_NEXT = 25;

	private int mTextColor1;
	private int mTextColor2;
	private int mBackColor[] = { 0, 0, 0 };
	private int mTextSize;
	private String mTitle1;
	private String mTitle2;
	private boolean mIsRunning;

	// 描画情報
	private float mTextWidth;
	private float mTextPos;
	private int mTextAscent;
	private int mTextDescent;
	private String mSortName;
	private boolean mSortType;
	private int mMarginW;
	private int mMarginH;
	private int mSelectArea;

	// ビューサイズ
	private int mViewWidth;

	private float mScrollStep;
	private float mScrollMargin;
	private Message mScrollMsg;

	// 描画オブジェクト
	private Handler mHandler;
	private Paint mTextPaint;
	private Paint mArrowPaint;
	private Path mArrowPath;

	private Context mContext;
	private DrawNoticeListener mDrawNoticeListener = null;

	private int mAreaWidth;
	private int mAreaHeight;

	public TitleArea(Context context, DrawNoticeListener listener) {
		mHandler = new Handler(this);
		mIsRunning = true;
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mArrowPath = new Path();

		mContext = context;
		float density = mContext.getResources().getDisplayMetrics().scaledDensity;
		mScrollStep = density;
		if (mScrollStep < 1) {
			mScrollStep = 1;
		}
		mScrollMargin = density * 40;
		mDrawNoticeListener = listener;
	}

	public void drawArea(Canvas canvas) {
		boolean debug = false;
		int fullcx = mAreaWidth;
		int cy = mAreaHeight;

		// グラデーション幅算出
		GradientDrawable grad;
		int colors1[] = { mBackColor[2], mBackColor[1], mBackColor[0] };
		grad = new GradientDrawable(Orientation.TOP_BOTTOM, colors1);
		grad.setBounds(new Rect(0, 0, fullcx, cy));
		grad.draw(canvas);


		// テキスト描画
		mTextPaint.setColor(mTextColor1);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.LEFT);

		if (mTitle1 != null) {
			int y;
			if (mTitle2 != null) {
				y = mMarginH;
			} else {
				y = (cy - (mTextSize + mTextDescent)) / 2;
			}
			canvas.drawText(mTitle1, mMarginW, y + mTextAscent, mTextPaint);
			if (mTitle2 != null) {
				y += mTextSize + mTextDescent + mMarginH;
				canvas.drawText(mTitle2, mMarginW - mTextPos, y + mTextAscent, mTextPaint);
				if (mTextWidth > mViewWidth && mTextWidth + mScrollMargin - mTextPos < mViewWidth) {
					canvas.drawText(mTitle2, mMarginW + mTextWidth + mScrollMargin - mTextPos, y + mTextAscent, mTextPaint);
				}
			}
		}
		// ソート三角描画
		if (mSortName != null) {
			mTextPaint.setColor(mSelectArea != SELECT_SORT ? mTextColor1 : mTextColor2);
			mArrowPaint.setColor(mSelectArea != SELECT_SORT ? mTextColor1 : mTextColor2);
			int s = mTextSize + mTextDescent;
			int sh = s * 2 / 5;
			int x1 = fullcx - s;
			int x2 = fullcx;
			int y1 = mMarginH;
			int y2 = y1 + s;

			mArrowPath.reset();
			if (!mSortName.equals("")) {
				if (mSortType) {
					// 昇順
					mArrowPath.moveTo(x1 + s / 2, y1 + sh);
					mArrowPath.lineTo(x2 - s / 3, y2 - sh);
					mArrowPath.lineTo(x1 + s / 3, y2 - sh);
				} else {
					// 降順
					mArrowPath.moveTo(x1 + s / 2, y2 - sh);
					mArrowPath.lineTo(x2 - s / 3, y1 + sh);
					mArrowPath.lineTo(x1 + s / 3, y1 + sh);
				}
			}
			canvas.drawPath(mArrowPath, mArrowPaint);
			mTextPaint.setTextAlign(Paint.Align.RIGHT);
			canvas.drawText(mSortName, fullcx - mMarginW - mTextSize, mMarginH + mTextAscent, mTextPaint);
		}
	}

	public Rect setDrawArea(int x1, int y1, int x2, int y2, int orientation) {
		int cx = x2 - x1;

		// 項目高さを求める
		mAreaHeight = (mTextSize + mTextDescent) * 2 + mMarginH * 3;
		mAreaWidth = cx;
		// 描画領域
		mViewWidth = cx - mMarginW * 2;
		return new Rect(x1, y1, mAreaWidth, mAreaHeight);
	}

	// 終了時に解放
	protected void onDetachedFromWindow() {
		// スクロールの停止
		mIsRunning = false;
	}

	// 描画設定
	public void setTextSize(int size, int textColor, int backColor) {
		mTextColor1 = textColor;
		mTextColor2 = DEF.margeColor(textColor, backColor, 1, 2);
		mBackColor[0] = backColor;
		mBackColor[1] = DEF.calcColor(backColor, 16);
		mBackColor[2] = DEF.calcColor(backColor, 32);
		mTextSize = size;
		mTextPaint.setTextSize(size);

		mMarginH = size / 32;
		mMarginW = size / 8;
	}

	// 文字列の設定
	public void setTitle(String title1, String title2) {
		mTitle1 = title1;
		mTitle2 = title2;

		FontMetrics fm = mTextPaint.getFontMetrics();
		mTextAscent = (int) (-fm.ascent);
		mTextDescent = (int) (fm.descent);
		if (mTitle2 != null) {
			mTextWidth = (int) mTextPaint.measureText(mTitle2);
		}
		else {
			mTextWidth = 0;
		}

		// 文字列幅が入りきらない場合のスクロール開始
		mTextPos = 0;
		startScrollTimer();

		// 位置初期化
		update(false);
	}

	// ソート状態の設定
	public void setSortMode(String name, boolean type) {
		mSortName = name;
		mSortType = type;
		update(false);
	}

	// タイマー開始
	public void startScrollTimer() {
		if (mTextWidth > mViewWidth) {
    		mScrollMsg = mHandler.obtainMessage(EVENT_SCROLL);
    		long NextTime = SystemClock.uptimeMillis() + SCROLL_TERM_FIRST;

    		mHandler.sendMessageAtTime(mScrollMsg, NextTime);
		}
		else {
			mScrollMsg = null;
		}
		return;
	}

	// スクロールタイマーイベント検知処理
	@Override
	public boolean handleMessage(Message msg) {
		if (msg != mScrollMsg) {
			return false;
		}
		long NextTime = SystemClock.uptimeMillis();

		if (mTextWidth > mViewWidth) {
			mTextPos += mScrollStep;
			if (mTextWidth + mScrollMargin <= mTextPos) {
				// 端までスクロールした
				mTextPos = 0;
				NextTime += SCROLL_TERM_FIRST;
			}
			else {
				NextTime += SCROLL_TERM_NEXT;
			}

			// 表示更新
			update(false);

			// 稼働中のみ次のイベント登録
			if (mIsRunning) {
				mScrollMsg = mHandler.obtainMessage(EVENT_SCROLL);
				mHandler.sendMessageAtTime(mScrollMsg, NextTime);
			}
		}
		return false;
	}

	private void update(boolean isRealtime) {
		mDrawNoticeListener.onUpdateArea(ListScreenView.AREATYPE_TITLE, false);
	}

	public int sendTouchEvent(int action, float x, float y) {
		// タイトルのタッチ処理
		int result = SELECT_NONE;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				if (0 <= x && x < mAreaWidth && 0 <= y && y < mAreaHeight) {
					mSelectArea = SELECT_SORT;
				}
				else {
					mSelectArea = SELECT_NONE;
				}
				if (action == MotionEvent.ACTION_UP) {
					result = mSelectArea;
					// 離した時点でクリア
					mSelectArea = SELECT_NONE;
				}
				break;
		}
		update(false);
		return result;
	}
}
