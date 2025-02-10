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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.view.MotionEvent;

public class SelectorArea {
	private final int BUTTON_MAXNUM = 6;

	private final int ICON_ID[] =
	{
			R.drawable.directory, R.drawable.toolbar_server, R.drawable.list_favorite, R.drawable.list_history, R.drawable.menu, R.drawable.list_file,
	};
	private final int TEXT_ID[] =
	{
		R.string.listname01, R.string.listname02, R.string.listname03, R.string.listname04, R.string.listname05, R.string.listname00
	};

	private Bitmap mBitmap[];
	private int mTouchIndex;
	private int mSelectIndex;
	private short mListType[];
	private String mNameText[];

	int mColorBright;
	int mColorShadow;

	private int mBakColor;

	private Paint mBitmapPaint;
	private Paint mFillPaint;
	private Paint mLinePaint;
	private Paint mTextPaint;

	private int mButtonSize;
	private int mButtonStart;
	private int mButtonNum;

	private boolean mShowSelector;
	private boolean mShowLabel;
	private int mSelectorSize;
	private int mSizeBitmap;
	private int mSizeFont;
	private int mTextMargin;
	private int mTextAscent;
	private int mTextDescent;

	private DrawNoticeListener mDrawNoticeListener = null;

	private int mAreaWidth;
	private int mAreaHeight;

	private Context mContext;

	// ビューサイズ
	public SelectorArea(Context context, DrawNoticeListener listener) {
		mContext = context;
		mBitmapPaint = new Paint();

		mFillPaint = new Paint();
		mFillPaint.setStyle(Style.FILL);

		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mTouchIndex = -1;
		mSelectIndex = -1;
		mDrawNoticeListener = listener;


	}

	public void drawArea(Canvas canvas, int baseX, int baseY) {
		if (!mShowSelector) {
			return;
		}

		int cx = mAreaWidth;
		int cy = mAreaHeight;
		int size_bitmap;

		size_bitmap = mSizeBitmap;

		// グラデーション幅算出
		int bmp_x[] = new int[mButtonNum];
		int bmp_y[] = new int[mButtonNum];
		int x1, x2;
		int y1, y2;

		if (mButtonNum < BUTTON_MAXNUM) {
			mFillPaint.setColor(mBakColor);
			// 背景塗り
			canvas.drawRect(baseX, baseY, baseX + mAreaWidth, baseY + mAreaHeight, mFillPaint);
		}

		for (int i = 0; i < mButtonNum; i++) {
			int color = mBakColor;
			if (mTouchIndex == i) {
				// タッチ中は暗く
				color = mColorShadow;
			}
			else if (mSelectIndex == i) {
				// 選択中は明るく
				color = mColorBright;
			}
			mFillPaint.setColor(color);

			if (cx > cy) {
				// 横長ツールバー
//				x1 = baseX + mButtonStart + cx * i / BUTTON_MAXNUM;
//				x2 = baseX + mButtonStart + cx * (i + 1) / BUTTON_MAXNUM;
				x1 = baseX + cx * i / mButtonNum;
				x2 = baseX + cx * (i + 1) / mButtonNum;
				y1 = baseY + 0;
				y2 = baseY + cy;

//				bmp_x[i] = baseX + mButtonStart + cx * (i * 2 + 1) / (BUTTON_MAXNUM * 2) - size_bitmap / 2;
				bmp_x[i] = baseX + cx * (i * 2 + 1) / (mButtonNum * 2) - size_bitmap / 2;
				bmp_y[i] = baseY + (cy - (size_bitmap + (mShowLabel ? mSizeFont + mTextMargin : 0))) / 2;
			}
			else {
				// 縦長ツールバー
				x1 = baseX + 0;
				x2 = baseX + cx;
//				y1 = baseY + mButtonStart + cy * i / BUTTON_MAXNUM;
//				y2 = baseY + mButtonStart + cy * (i + 1) / BUTTON_MAXNUM;
				y1 = baseY + cy * i / mButtonNum;
				y2 = baseY + cy * (i + 1) / mButtonNum;

				bmp_x[i] = baseX + (cx - size_bitmap) / 2;
//				bmp_y[i] = baseY + mButtonStart + cy * (i * 2 + 1) / (BUTTON_MAXNUM * 2) - (size_bitmap + (mShowLabel ? mSizeFont + mTextMargin : 0)) / 2;
				bmp_y[i] = baseY + cy * (i * 2 + 1) / (mButtonNum * 2) - (size_bitmap + (mShowLabel ? mSizeFont + mTextMargin : 0)) / 2;
			}

			// 背景塗り
			canvas.drawRect(x1, y1, x2, y2, mFillPaint);
		}

		// ビットマップ描画
		for (int i = 0; i < mButtonNum; i++) {
			canvas.drawBitmap(mBitmap[i],  bmp_x[i], bmp_y[i], mBitmapPaint);
		}


		if (mShowLabel) {
			for (int i = 0; i < mButtonNum; i++) {
				int tx = bmp_x[i] + mSizeBitmap / 2;
				int ty = bmp_y[i] + mSizeBitmap + mTextMargin + mTextAscent;
				if (cx > cy) {
    				canvas.drawText(mNameText[i], tx, ty, mTextPaint);
				}
				else {
					int strlen = mNameText[i].length();
					if (strlen > 4) {
						int halflen = (strlen + 1) / 2;
						String str1 = mNameText[i].substring(0, halflen);
						String str2 = mNameText[i].substring(halflen);
	    				canvas.drawText(str1, tx - mSizeFont /3, ty, mTextPaint);
	    				ty += mSizeFont + mTextDescent;
	    				canvas.drawText(str2, tx + mSizeFont /3, ty, mTextPaint);
					}
					else {
        				canvas.drawText(mNameText[i], tx, ty, mTextPaint);
					}
				}
			}
		}
		return;
	}

	public Rect setDrawArea(int x1, int y1, int x2, int y2, int orientation) {
		int cx = x2 - x1;
		int cy = y2 - y1;

		if (mShowSelector) {
			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				cx = mSelectorSize;
				mButtonSize = cy / mButtonNum;
//				mButtonSize = cy / BUTTON_MAXNUM;
//				mButtonStart = cy * (BUTTON_MAXNUM - mButtonNum) / BUTTON_MAXNUM / 2;
			}
			else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				cy = mSelectorSize;
				mButtonSize = cx / mButtonNum;
//				mButtonSize = cx / BUTTON_MAXNUM;
//				mButtonStart = cx * (BUTTON_MAXNUM - mButtonNum) / BUTTON_MAXNUM / 2;
			}
		}
		mAreaWidth = cx;
		mAreaHeight = cy;
//		return new Rect(x2 - cx, y2 - cy, x2, y2);
		mButtonStart = 0;
		return new Rect(x2 - cx, y2 - cy, x2, y2);
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

		if (!mShowSelector) {
			return -1;
		}

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
//			index = (x - mButtonStart) / mButtonSize;
			index = x / mButtonSize;
		}
		else {
//			index = (x - mButtonStart) / mButtonSize;
			index = y / mButtonSize;
		}
		if (index >= mListType.length) {
			// 範囲外
			index = -1;
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
					mSelectIndex = index;
					mTouchIndex = -1;

					// 選択ボタンを通知
					ret = index;
				}
				update();
				break;
		}
		return ret;
	}

	public void setConfig(boolean show, int size, boolean label, short[] listtype, int drawcolor, int bakcolor) {
		mListType = listtype;
		mButtonNum = listtype.length;
		if (mListType.length == 0) {
			// リスト表示がなければセレクターも表示しない
			show = false;
		}

		mShowSelector = show;
		if (show) {
			mShowLabel = label;
			if (label) {
				mSizeBitmap = (int)(size * 0.60);
				mSizeFont = (int)(size * 0.20);
				mTextMargin = (int)(size * 0.05);
				mTextPaint.setTextSize(mSizeFont);
				FontMetrics fm = mTextPaint.getFontMetrics();
				mTextAscent = (int) (-fm.ascent);
				mTextDescent = (int) (fm.descent);
			}
			else {
				mSizeBitmap = (int)(size * 0.6);
				mSizeFont = 0;
				mTextMargin = 0;
			}
		}
		mSelectorSize = size;

		mBakColor = bakcolor;

		mColorBright = DEF.calcColor(mBakColor, 24);
		mColorShadow = DEF.calcColor(mBakColor, -16);

		// ビットマップリソースを読み込み
		Resources res = mContext.getResources();
		mBitmap = new Bitmap[mButtonNum];
		mNameText = new String[mButtonNum];
		if (mShowSelector) {
			for (int i = 0; i < mButtonNum; i++) {
				int idBitmap = ICON_ID[mListType[i]];
				int idText = TEXT_ID[mListType[i]];
				// mBitmap[i] = BitmapFactory.decodeResource(res, id);
				mBitmap[i] = ImageAccess.createIcon(res, idBitmap, mSizeBitmap, drawcolor);
				// ボタン名称をリソースから読み込み
				mNameText[i] = res.getString(idText);
				mTextPaint.setColor(drawcolor);
			}
		}
		update();
		return;
	}

	public void setSelect(int index) {
		if (mSelectIndex != index) {
			mSelectIndex = index;
		}
		update();
	}

	private void update() {
		mDrawNoticeListener.onUpdateArea(ListScreenView.AREATYPE_SELECTOR, false);
	}
}
