package src.comitton.view;

import src.comitton.common.DEF;
import src.comitton.stream.CallImgLibrary;
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
import android.util.AttributeSet;
import android.view.View;

public class ListItemView extends View {
//	private final int MARGIN_CY = 4;
//	private final int MARGIN_CX = 4;
	private final int MAX_LINES = 3;
	private final int DATETIME_LENGTH = 21;

	private int mTextColor;
	private int mBakColor;
	private int mCurColor;
	private int mSummaryColor;

	private long mThumbId;
	private int mThumbIndex;
	private boolean mThumbView;
	private int mThumbSizeW;
	private int mThumbSizeH;
	private String mTitle;
//	private String mTitleDot;
	private String mTitleSep[];
	private String mSummary;
//	private String mSummaryDot;
	private String mSummarySep[];
	private int mTitleSize;
	private int mSummarySize;
	private int mItemMargin;
	private int mCounter;
	
	// 描画情報
	private int mTitleAscent;
	private int mTitleDecent;
	private int mSummaryAscent;
	private int mSummaryDecent;
//	private int mTextPos;
	private boolean mRightFlag;
	private boolean mThumbnail;
	private Bitmap mThumbBitmap;
	
	private Rect mSrcRect;
	private Rect mDstRect;
	private Paint mLinePaint;
	private Paint mFillPaint;
	private Paint mTextPaint;

	public ListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mSrcRect = new Rect();
		mDstRect = new Rect();
		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(1);
		mFillPaint = new Paint();
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG); 
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//		super.onDraw(canvas);
		int cx = getWidth();
		int cy = getHeight();
		int y = mItemMargin;
		int x = mItemMargin;

		mFillPaint.setStyle(Style.FILL);
//		if (mMarker == true) {
//			fill.setColor(mBakColor);
//			canvas.drawRect(0, 0, getWidth(), getHeight(), fill);
//		}

		if (this.isPressed() == true || this.isSelected() == true) {
			int mh = mItemMargin / 2;
			if (this.isPressed() == true) {
				mFillPaint.setColor(DEF.margeColor(mCurColor, mBakColor, mCounter + 64, 96));
    			if (mCounter < 32) {
    				mCounter ++;
    			}
			}
			else {
				mFillPaint.setColor(DEF.margeColor(mCurColor, mBakColor, 64, 96));
			}
			canvas.drawRect(0, 0, cx, cy, mFillPaint);

			mFillPaint.setColor(mCurColor);
			canvas.drawRect(0, 0, cx, mh, mFillPaint);
			canvas.drawRect(0, cy - mh, cx, cy, mFillPaint);
		}
		else {
			mFillPaint.setColor(mBakColor);
			canvas.drawRect(0, 0, cx, cy, mFillPaint);
			mCounter = 0;
		}

		if (mThumbView) {
			// サムネイル表示モード
			int draw_cx = mThumbSizeW;
			int draw_cy = mThumbSizeH;
			x += draw_cx + mItemMargin;

			// 枠の描画


			mDstRect.set(mItemMargin - 1, mItemMargin - 1, draw_cx + mItemMargin, draw_cy + mItemMargin);
			canvas.drawRect(mDstRect, mLinePaint);

			int retBitmap = CallImgLibrary.ThumbnailCheck(mThumbId, mThumbIndex);
			if (mThumbnail && retBitmap == 1) {
				// bitmap.setAntiAlias(true);
				if (mThumbBitmap != null) {
    				int retValue = CallImgLibrary.ThumbnailDraw(mThumbId, mThumbBitmap, mThumbIndex);
    				int width = retValue >> 16;
    				int height = retValue & 0xFFFF;
    				int dstWidth = width * draw_cy / height;
    				int dstHeight = draw_cy;
    				int dstX = 0;
    				if (dstWidth < draw_cx) {
    					dstX = (draw_cx - dstWidth) / 2;
    				}
    				mSrcRect.set(0, 0, width, height);
    				mDstRect.set(dstX + mItemMargin, mItemMargin, dstX + dstWidth + mItemMargin, dstHeight + mItemMargin);
    				canvas.drawBitmap(mThumbBitmap, mSrcRect, mDstRect, null);
				}
			}
			else {
				// サムネイルあり
				int draw_font = Math.min(mThumbSizeW, mThumbSizeH) / 6;

				// 操作説明
				String str = retBitmap == 0 ? "Loading..." : "No Image";
				int text_x = mItemMargin + draw_cx / 2;
				int text_y = mItemMargin + (draw_cy + draw_font) / 2 - draw_font / 4;

				// 中央
				mTextPaint.setTextAlign(Paint.Align.CENTER);
				mTextPaint.setTypeface(Typeface.DEFAULT);
				mTextPaint.setTextSize(draw_font);

				mTextPaint.setStrokeWidth(1.0f);
				mTextPaint.setStyle(Paint.Style.FILL);
				mTextPaint.setColor(Color.DKGRAY);
				canvas.drawText(str, text_x + 1, text_y + 1, mTextPaint);

				mTextPaint.setColor(Color.WHITE);
				canvas.drawText(str, text_x, text_y, mTextPaint);
			}
		}

		// タイトル描画
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(mTitleSize);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.LEFT);

		for (int i = 0; i < mTitleSep.length; i++) {
			canvas.drawText(mTitleSep[i], x, y + mTitleAscent, mTextPaint);
			y += (mTitleSize + mTitleDecent);
		}
//		y +=  MARGIN_CY;

		// サマリ描画
		if (mSummarySize > 0) {
			mTextPaint.setColor(mSummaryColor);
			mTextPaint.setTextSize(mSummarySize);
			mTextPaint.setTypeface(Typeface.MONOSPACE);

			for (int i = 0; i < mSummarySep.length; i++) {
				if (mRightFlag) {
					mTextPaint.setTextAlign(Paint.Align.RIGHT);
					canvas.drawText(mSummarySep[i], cx, y + mSummaryAscent, mTextPaint);
				}
				else {
					mTextPaint.setTextAlign(Paint.Align.LEFT);
					canvas.drawText(mSummarySep[i], x, y + mSummaryAscent, mTextPaint);
				}
				y += mSummarySize + mSummaryDecent;
			}
		}
		return;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int cx = width - mItemMargin * 2;
		FontMetrics fm;

		// テキスト描画属性設定
		mTextPaint.setTextSize(mTitleSize);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		fm = mTextPaint.getFontMetrics();
		mTitleAscent = (int) (-fm.ascent);
		mTitleDecent = (int) (fm.descent);

		if (mThumbView) {
			cx -= mThumbSizeW + mItemMargin;
		}
		mTitleSep = getMultiLine(mTitle, cx, mTextPaint);

		if (mSummarySize > 0) {
			mTextPaint.setTextSize(mSummarySize);
			mTextPaint.setTypeface(Typeface.MONOSPACE);
			mTextPaint.setTextAlign(Paint.Align.RIGHT);
			fm = mTextPaint.getFontMetrics();
			mSummaryAscent = (int) (-fm.ascent);
			mSummaryDecent = (int) (fm.descent);
//			mSummaryDot = getDotText(mSummary, cx, text);
			float result[] = new float[mSummary.length()];
			float len[] = { 0, 0 };
			mTextPaint.getTextWidths(mSummary, result);
			for (int i = 0; i < result.length; i++) {
				len[0] += result[i];
				if (i < DATETIME_LENGTH) {
					// 日付部分の長さ
					len[1] += result[i];
				}
			}

			if (cx >= (int) Math.ceil(len[0])) {
				mSummarySep = new String[1];
				mSummarySep[0] = mSummary;
			}
			else {
				// 更新日時とサイズを分割
				mSummarySep = new String[2];
				mSummarySep[0] = mSummary.substring(0, DATETIME_LENGTH);
				mSummarySep[1] = mSummary.substring(DATETIME_LENGTH).trim();
				if (len[1] > cx) {
					// 日付がはいりりきらない
					mSummarySep[0] = "[" + mSummarySep[0].substring(3, DATETIME_LENGTH - 4) + "]";
				}
			}
		}
		else {
			mSummaryAscent = 0;
		}

		// 項目高さを求める
		int cy = (mTitleSize + mTitleDecent) * mTitleSep.length; // + MARGIN_CY;
		if (mSummarySize > 0) {
			cy += (mSummarySize + mSummaryDecent) * mSummarySep.length;
		}
		// ビットマップ表示のサイズ確保
		if (mThumbnail && cy < mThumbSizeH) {
			cy = mThumbSizeH;
		}

		setMeasuredDimension(width, cy + mItemMargin * 2);
//		invalidate();
	}

	// 描画設定
	public void setRight() {
		mRightFlag = true;
	}

	// 描画設定
	public void setDrawInfo(int textColor, int rectColor, int summColor, int size1, int size2, boolean thumb, int tsizew, int tsizeh, int margin) {
		mTextColor = textColor;
		mSummaryColor = summColor;
		mTitleSize = size1;
		mSummarySize = size2;
		mThumbnail = thumb;
		mThumbSizeW = tsizew;
		mThumbSizeH = tsizeh;
		mItemMargin = margin;
		mCounter = 0;
		if (thumb == true) {
			// サムネイル有りの場合
			mThumbBitmap = Bitmap.createBitmap(tsizew, tsizeh, Config.RGB_565);
		}
		mLinePaint.setColor(rectColor);
	}

	// パスの設定
	public void setFileInfo(long thumbId, int bmIndex, boolean bmView, String title, String summary, boolean showext) {
		// サムネイルなし
		mThumbId = thumbId;
		mThumbIndex = bmIndex;
		mThumbView = bmView;
		mTitle = title;
		if (showext == false) {
			int dot = title.lastIndexOf('.');
			if (dot >= 1 && dot < title.length() - 1){
				mTitle = title.substring(0, dot);
			}
		}
		mTitleSep = null;
		mSummary = summary;
		mSummarySep = null;
		requestLayout();
	}

	// 背景色の設定
	public void setMarker(int bakcolor, int curcolor) {
		mBakColor =  bakcolor;
		mCurColor =  curcolor;
	}
		
	private String[] getMultiLine(String str, int cx, Paint text) {
		float result[] = new float[str.length()];
		text.getTextWidths(str, result);

		int i;
		int lastpos = 0;
		int line = 0;
		float sum = 0.0f;
		int pos[] = new int[MAX_LINES];

		float dotwidth = text.measureText("...");

		// 1行に入る文字長をしらべる
		for (i = 0; i < result.length; i++) {
			if (line == MAX_LINES - 1 && sum + result[i] > cx - dotwidth) {
				if (lastpos == 0) {
					lastpos = i;
				}
			}
			if (sum + result[i] > cx) {
				sum = result[i];
				if (line == MAX_LINES - 1) {
					pos[line] = lastpos;
					break;
				}
				else {
					pos[line++] = i;
				}
			}
			else {
				sum += result[i];
			}
		}

		String strSep[] = new String[line + 1];
		int st = 0;
		// 文字列の切り出し
		for (i = 0; i <= line; i++) {
			if (pos[i] == 0) {
				strSep[i] = mTitle.substring(st);

			}
			else {
				strSep[i] = mTitle.substring(st, pos[i]);
				if (i == MAX_LINES - 1) {
					strSep[i] += "...";
				}
			}
			st = pos[i];
		}
		return strSep;
	}
}
