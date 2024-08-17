package src.comitton.view;

import src.comitton.common.TextFormatter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;

public class MenuItemView extends View {
	public final static int TYPE_SECTION = 0;
	public final static int TYPE_ITEM = 1;
	public final static int TYPE_SEPARATE = 2;

	public final static int SUBTYPE_STRING = 0;
	public final static int SUBTYPE_CHECK  = 1;
	public final static int SUBTYPE_RADIO  = 2;


	private final int MARGIN_SIZE = 16;

	private int mTxtColor;
	private int mBakColor;
	private int mCurColor;

	private int mType;
	private int mSubType;
	private String mText;
	private String mTextS;
	private String mSubText1;
	private String mSubText2;
	private int mMenuId;
	private int mSelect;

	// 描画情報
	private int mWidth;
	private int mMargin;
	private int mTextSize;
	private int mTextAscent;
	private int mTextDecent;

	private Paint mTextPaint;
	private Paint mFillPaint;
	private Paint mFramePaint;
	private Path mChkPath;
	private Paint mTextSPaint;
	private Paint mFrameSPaint;
	private GradientDrawable mGradient;
	private Rect mChkRect1;
//	private Rect mChkRect2;
	private int mRadioTX1;
	private int mRadioTX2;
	private int mRadioCX1;
	private int mRadioCX2;
	private int mRadioY;
	private int mSubText1X;

	private boolean mPress;

	public MenuItemView(Context context, int width, int txtcolor, int bakcolor) {
		super(context);
		initMenuItemView(TYPE_SEPARATE, 0, null, null, null, 0, 0, 0, width, txtcolor, bakcolor, 0);
	}

	public MenuItemView(Context context, int type, int subtype, String text, String subtext1, String subtext2, int select, int id, int size, int width, int txtcolor, int bakcolor, int curcolor) {
		super(context);
		initMenuItemView(type, subtype, text, subtext1, subtext2, select, id, size, width, txtcolor, bakcolor, curcolor);
	}

	private void initMenuItemView(int type, int subtype, String text, String subtext1, String subtext2, int select, int id, int size, int width, int txtcolor, int bakcolor, int curcolor) {
		// 描画情報の設定
		mType = type;
		mSubType = subtype;
		mText = text;
		mTextS = text;
		mSubText1 = subtext1;
		mSubText2 = subtext2;
		mSelect = select;
		mMenuId = id;
		mTextSize = size;
		if (type == TYPE_ITEM) {
			mMargin = mTextSize * 3 / 4;
		}
		else {
			mMargin = mTextSize / 3;
		}
		mWidth = width;
		mBakColor = bakcolor;
		mCurColor = curcolor;
		mTxtColor = txtcolor;

		int colors[] = { mBakColor, mTxtColor, mTxtColor, mBakColor };
		mGradient = new GradientDrawable(Orientation.LEFT_RIGHT, colors);

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(size);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(txtcolor);

		mTextSPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextSPaint.setTextSize(size);
		mTextSPaint.setTypeface(Typeface.DEFAULT);
		mTextSPaint.setTextAlign(Paint.Align.LEFT);
		mTextSPaint.setStrokeWidth(3.0f);
		mTextSPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mTextSPaint.setColor(0xC0000000);

		mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFillPaint.setStyle(Style.FILL);
		mFillPaint.setColor(0xFF40C0FF);

		mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFramePaint.setStyle(Style.STROKE);
		mFramePaint.setStrokeWidth(4.0f);
		mFramePaint.setColor(txtcolor);

		mFrameSPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFrameSPaint.setStyle(Style.STROKE);
		mFrameSPaint.setStrokeWidth(7.0f);
		mFrameSPaint.setColor(0xC0000000);

		if (mSubType == SUBTYPE_CHECK) {
			mChkRect1 = new Rect();
//			mChkRect2 = new Rect();
			mChkPath = new Path();
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		int cx = getWidth();
		int cy = getHeight();

		if (mType == TYPE_SEPARATE) {
			mGradient.setBounds(new Rect(0, 0, cx, cy));
			mGradient.draw(canvas);
			return;
		}

//		int margin_y = mTextSize * 2 / 3; //(mType == TYPE_SECTION ? MARGIN_SIZE / 2 : MARGIN_SIZE);
		int y = mMargin;
		int x = MARGIN_SIZE;

		// タイトル以外は選択時に色塗り
		if (mPress == true && mType == TYPE_ITEM) {
			//mFillPaint.setColor(mCurColor);
			canvas.drawColor(mCurColor);
		}
		else {
			//mFillPaint.setColor(mBakColor);
			canvas.drawColor(mBakColor);
		}
		//canvas.drawRect(0, 0, cx, cy, mFillPaint);

		if (mType != TYPE_SEPARATE) {
			// テキスト描画
			canvas.drawText(mTextS, x, y + mTextAscent, mTextSPaint);
			canvas.drawText(mTextS, x, y + mTextAscent, mTextPaint);
			if (mSubType == SUBTYPE_STRING) {
				if (mSubText1 != null) {
					canvas.drawText(mSubText1, mSubText1X, y + mTextAscent, mTextSPaint);
					canvas.drawText(mSubText1, mSubText1X, y + mTextAscent, mTextPaint);
				}
			}
			else if (mSubType == SUBTYPE_CHECK) {
				canvas.drawRect(mChkRect1, mFrameSPaint);
				canvas.drawRect(mChkRect1, mFramePaint);
				if (mSelect != 0) {
//					canvas.drawRect(mChkRect2, mFillPaint);
					canvas.drawPath(mChkPath, mTextSPaint);
					canvas.drawPath(mChkPath, mFillPaint);
				}
			}
			else if (mSubType == SUBTYPE_RADIO) {
				canvas.drawText(mSubText1, mRadioTX1, y + mTextAscent, mTextSPaint);
				canvas.drawText(mSubText1, mRadioTX1, y + mTextAscent, mTextPaint);
				canvas.drawText(mSubText2, mRadioTX2, y + mTextAscent, mTextSPaint);
				canvas.drawText(mSubText2, mRadioTX2, y + mTextAscent, mTextPaint);

				canvas.drawCircle(mRadioCX1, mRadioY, mTextSize * 4 / 10, mFrameSPaint);
				canvas.drawCircle(mRadioCX1, mRadioY, mTextSize * 4 / 10, mFramePaint);
				canvas.drawCircle(mRadioCX2, mRadioY, mTextSize * 4 / 10, mFrameSPaint);
				canvas.drawCircle(mRadioCX2, mRadioY, mTextSize * 4 / 10, mFramePaint);
				if (mSelect == 0) {
					canvas.drawCircle(mRadioCX1, mRadioY, mTextSize / 4, mFrameSPaint);
					canvas.drawCircle(mRadioCX1, mRadioY, mTextSize / 4, mFillPaint);
				}
				else {
					canvas.drawCircle(mRadioCX2, mRadioY, mTextSize / 4, mFrameSPaint);
					canvas.drawCircle(mRadioCX2, mRadioY, mTextSize / 4, mFillPaint);
				}
			}
		}
		return;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int width = MeasureSpec.getSize(widthMeasureSpec);
		int cy = 0;
		if (mType == TYPE_SEPARATE) {
			cy = 1;
		}
		else {
			FontMetrics fm;

			// テキスト描画属性設定
			fm = mTextPaint.getFontMetrics();
			mTextAscent = (int) (-fm.ascent);
			mTextDecent = (int) (fm.descent);

			// 項目高さを求める
			cy = mTextAscent + mTextDecent; // + MARGIN_CY;
			cy += mMargin * 2;

			if (mType != TYPE_SEPARATE) {
				// テキスト描画
				if (mSubType == SUBTYPE_STRING) {
					if (mSubText1 != null) {
						mSubText1X = mWidth - ((int)mTextPaint.measureText(mSubText1) + mTextSize / 4);
						if (mTextS == mText) {
							mTextS = TextFormatter.getShorteningSingle(mText, mSubText1X - mTextSize / 4, mTextPaint, true);
						}
					}
				}
				else if (mSubType == SUBTYPE_CHECK) {
					mChkRect1.left = mWidth - mMargin - mTextSize;
					mChkRect1.top = (cy - mTextSize) / 2;
					mChkRect1.right = mChkRect1.left + mTextSize;
					mChkRect1.bottom = cy - (cy - mTextSize) / 2;
					// mChkRect2.left = mChkRect1.left + mTextSize * 2 / 10;
					// mChkRect2.top = mChkRect1.top + mTextSize * 2 / 10;
					// mChkRect2.right = mChkRect1.right - mTextSize * 2 / 10;
					// mChkRect2.bottom = mChkRect1.bottom - mTextSize * 2 / 10;
					// 楔を描く
					mChkPath.reset();
					mChkPath.moveTo(mChkRect1.left + 1, mChkRect1.top + mTextSize / 2);
					mChkPath.lineTo(mChkRect1.left + mTextSize / 3, mChkRect1.top + mTextSize * 2 / 3);
					mChkPath.lineTo(mChkRect1.right + mTextSize / 10, mChkRect1.top - 1);
					mChkPath.lineTo(mChkRect1.left + mTextSize * 2 / 5, mChkRect1.bottom - mTextSize / 10);

				}
				else if (mSubType == SUBTYPE_RADIO) {
					int textWidth = mTextSize * 3;
					mRadioTX1 = mWidth - textWidth * 2 - mTextSize - mTextSize / 3 - mMargin;
					mRadioTX2 = mWidth - textWidth - mMargin;
					mRadioCX1 = mRadioTX1 - mTextSize / 2; 
					mRadioCX2 = mRadioTX2 - mTextSize / 2;
					mRadioY = cy / 2;
				}
			}
		}
		setMeasuredDimension(mWidth, cy);
	}

	public void setSelect(boolean press) {
		mPress = press;
		invalidate();
	}

	public int getType() {
		return mType;
	}

	public int getMenuId() {
		return mMenuId;
	}
}
