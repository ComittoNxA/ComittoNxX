package src.comitton.fileview.view.list;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.fileview.DrawNoticeListener;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class ToolbarArea {
	private final int BUTTON_NUM = 6;

	private final int ICON_ID[] =
	{
			R.drawable.toolbar_back, R.drawable.toolbar_refresh, R.drawable.toolbar_mode, R.drawable.toolbar_search, R.drawable.add_directory, R.drawable.toolbar_exit
	};
	private final int TEXT_ID[] = { R.string.toolbar00, R.string.toolbar01, R.string.toolbar02, R.string.toolbar03, R.string.toolbar04, R.string.toolbar05 };

	private Bitmap mBitmap[];
	private int mTouchIndex;
	private String mNameText[];

	int mColorShadow;

	private boolean mShowToolbar;	/* ツールバー表示ON/OFF */
	private boolean mShowLabel;		/* ラベル表示ON/OFF */
	private int mBakColor;
	private int mToolbarSize;
	private int mSizeFont;
	private int mSizeBitmap;
	private int mTextMargin;
	private int mTextAscent;

	private Paint mBitmapPaint;
	private Paint mFillPaint;
	private Paint mLinePaint;
	private Paint mTextPaint;

	private DrawNoticeListener mDrawNoticeListener = null;

	private int mAreaWidth;
	private int mAreaHeight;

	private Context mContext;

	// ビューサイズ
	public ToolbarArea(Context context, DrawNoticeListener listener) {
		mContext = context;
		Resources res = mContext.getResources();

		// ボタン名称をリソースから読み込み
		mNameText = new String[BUTTON_NUM];
		for (int i = 0; i < BUTTON_NUM; i++) {
			mNameText[i] = res.getString(TEXT_ID[i]);
		}

		mTouchIndex = -1;

		mBitmapPaint = new Paint();
		mFillPaint = new Paint();
		mFillPaint.setStyle(Style.FILL);
		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mDrawNoticeListener = listener;
	}

	protected void drawArea(Canvas canvas, int baseX, int baseY) {
		if (!mShowToolbar) {
			return;
		}

		int cx = mAreaWidth;
		int cy = mAreaHeight;

		// グラデーション幅算出
		int bmp_x[] = new int[BUTTON_NUM];
		int bmp_y[] = new int[BUTTON_NUM];
		int line1, line2;

		for (int i = 0; i < BUTTON_NUM; i++) {
			boolean btnDown = false;
			if (mTouchIndex == i) {
				btnDown = true;
			}

			if (btnDown) {
				mFillPaint.setColor(mColorShadow);
			}
			else {
				mFillPaint.setColor(mBakColor);
			}
			if (cx > cy) {
				line1 = baseX + cx * i / BUTTON_NUM;
				line2 = baseX + cx * (i + 1) / BUTTON_NUM;

				bmp_x[i] = baseX + cx * (i * 2 + 1) / (BUTTON_NUM * 2) - mSizeBitmap / 2;
				bmp_y[i] = baseY + (cy - (mSizeBitmap + (mShowLabel ? mSizeFont + mTextMargin : 0))) / 2;

				// 押されていないか押されてても違うボタン
				canvas.drawRect(line1, baseY + 0, line2, baseY + cy, mFillPaint);
			}
			else {
				line1 = baseY + cy * i / BUTTON_NUM;
				line2 = baseY + cy * (i + 1) / BUTTON_NUM;

				bmp_x[i] = baseX + (cx - mSizeBitmap) / 2;
				bmp_y[i] = baseY + cy * (i * 2 + 1) / (BUTTON_NUM * 2) - (mSizeBitmap + (mShowLabel ? mSizeFont + mTextMargin : 0)) / 2;

				// 押されていないか押されてても違うボタン
				canvas.drawRect(baseX + 0, line1, baseX + cx, line2, mFillPaint);
			}
		}

		// ビットマップ描画
		for (int i = 0; i < BUTTON_NUM; i++) {
			Bitmap bm = mBitmap[i];
			canvas.drawBitmap(bm,  bmp_x[i], bmp_y[i], mBitmapPaint);
		}

		if (mShowLabel) {
			for (int i = 0; i < BUTTON_NUM; i++) {
				int tx = bmp_x[i] + mSizeBitmap / 2;
				int ty = bmp_y[i] + mSizeBitmap + mTextMargin + mTextAscent;
				canvas.drawText(mNameText[i], tx, ty, mTextPaint);
			}
		}
		return;
	}

	public Rect setDrawArea(int x1, int y1, int x2, int y2, int orientation) {
		int cx = 0;
		int cy = 0;

		if (mShowToolbar) {
			cx = x2 - x1;
			cy = y2 - y1;

			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				cx = mToolbarSize;
			}
			else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				cy = mToolbarSize;
			}
		}
		mAreaWidth = cx;
		mAreaHeight = cy;
		return new Rect(x1, y1, x1 + cx, y1 + cy);
	}

	// 終了時に解放
	protected void onDetachedFromWindow() {
		// スクロールの停止
	}

	public int sendTouchEvent(int action, int x, int y) {
		int cx = mAreaWidth;
		int cy = mAreaHeight;
		int index;
		boolean inArea;
		int ret = -1;

		// エリア内かを求める
		if (0 <= x && x < cx && 0 <= y && y <= cy) {
			inArea = true;
		}
		else {
			inArea = false;
		}

		// タッチしたボタンのインデックスを求める
		if (inArea == false) {
			index = -1;
		}
		else if (cx > cy) {
			index = x * BUTTON_NUM / cx;
		}
		else {
			index = y * BUTTON_NUM / cy;
		}

		// イベント処理
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (inArea) {
					// 押された
					mTouchIndex = index;
					update();
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (index != mTouchIndex) {
					mTouchIndex = index;
					update();
				}
				break;

			case MotionEvent.ACTION_UP:
				if (index != -1) {
					ret = index;
				}
				mTouchIndex = -1;
				update();
				break;
		}
		return ret;
	}

//	public void setThumbnail(boolean thumb) {
//		mThumbnail = thumb;
//		invalidate();
//		return;
//	}

	public void setDisplay(boolean show, int size, boolean label, int drawcolor, int bakcolor) {
		mShowToolbar = show;
		if (show) {
			mShowLabel = label;
			if (label) {
				mSizeBitmap = (int)(size * 0.60);
				mSizeFont = (int)(size * 0.20);
				mTextMargin = (int)(size * 0.05);
				mTextPaint.setTextSize(mSizeFont);
				FontMetrics fm = mTextPaint.getFontMetrics();
				mTextAscent = (int) (-fm.ascent);
			}
			else {
				mSizeBitmap = (int)(size * 0.6);
				mSizeFont = 0;
				mTextMargin = 0;
			}
		}
		mToolbarSize = size;

		mBakColor = bakcolor;
		mColorShadow = DEF.calcColor(mBakColor, -16);

		// ビットマップリソースを読み込み
		Resources res = mContext.getResources();
		mBitmap = new Bitmap[ICON_ID.length];
		if (mShowToolbar) {
			for (int i = 0; i < BUTTON_NUM; i++) {
				mBitmap[i] = ImageAccess.createIcon(res, ICON_ID[i], mSizeBitmap, drawcolor);
			}
			mTextPaint.setColor(drawcolor);
		}
		return;
	}

	private void update() {
		mDrawNoticeListener.onUpdateArea(ListScreenView.AREATYPE_TOOLBAR, false);
	}
}
