package src.comitton.fileview.view;

import src.comitton.common.DEF;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

public class TitleView extends View implements Handler.Callback {
	private final int EVENT_SCROLL = 1;
	private final int SCROLL_TERM_FIRST = 1000;
	private final int SCROLL_TERM_NEXT = 30;

	private int mTextColor;
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
	private int mMarginW;
	private int mMarginH;

	// ビューサイズ
	private int mViewWidth;

	private float mScrollStep;
	private float mScrollMargin;
	private Message mScrollMsg;

	// 描画オブジェクト
	private Handler mHandler;
	private Paint mTextPaint;

	private Context mContext;

	public TitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHandler = new Handler(this);
		mIsRunning = true;
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mContext = context;
		float density = mContext.getResources().getDisplayMetrics().scaledDensity;
		mScrollStep = density;
		if (mScrollStep < 1) {
			mScrollStep = 1;
		}
		mScrollMargin = density * 40;

	}

	@Override
	protected void onDraw(Canvas canvas) {
		int fullcx = getWidth();
		int cy = getHeight();

		// グラデーション幅算出
		GradientDrawable grad;
		int colors1[] = { mBackColor[2], mBackColor[1], mBackColor[0] };
		grad = new GradientDrawable(Orientation.TOP_BOTTOM, colors1);
		grad.setBounds(new Rect(0, 0, fullcx, cy));
		grad.draw(canvas);

		// テキスト描画
		mTextPaint.setColor(mTextColor);
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
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int cx = MeasureSpec.getSize(widthMeasureSpec);

		// 項目高さを求める
		int cy = (mTextSize + mTextDescent) * 2 + mMarginH * 3;
		setMeasuredDimension(cx, cy);

		mViewWidth = cx - mMarginW * 2;
		invalidate();
	}

	// 終了時に解放
	protected void onDetachedFromWindow() {
		// スクロールの停止
		mIsRunning = false;
		super.onDetachedFromWindow();
	}

	// 描画設定
	public void setTextSize(int size, int textColor, int backColor) {
		mTextColor = textColor;
		int color2 = DEF.margeColor(textColor, backColor, 1, 2);
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

		// 文字列幅が入りきらない場合のスクロール開始
		mTextPos = 0;
		startScrollTimer();

		// 位置初期化
		invalidate();
	}

	// タイマー開始
	public void startScrollTimer() {
		mScrollMsg = mHandler.obtainMessage(EVENT_SCROLL);
		long NextTime = SystemClock.uptimeMillis() + SCROLL_TERM_FIRST;

		mHandler.sendMessageAtTime(mScrollMsg, NextTime);
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
			invalidate();
			// 稼働中のみ次のイベント登録
			if (mIsRunning) {
				mScrollMsg = mHandler.obtainMessage(EVENT_SCROLL);
				mHandler.sendMessageAtTime(mScrollMsg, NextTime);
			}
		}
		return false;
	}
}
