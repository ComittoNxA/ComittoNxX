package src.comitton.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.View;

@SuppressLint("ViewConstructor")
public class SelectIconView extends View {
	private final int MARGIN1 = 4;
	private final int MARGIN2 = 12;

	public Context mContext;

	// アイコンのビットマップ
	private Bitmap mIcon;
	
	private Paint mBakPaint;
	private Paint mCurPaint;
	private Paint mBmpPaint;

	private Rect mIconSrcRect;
	private Rect mIconDstRect;
	private Rect mFrameRect;

	private boolean mSelect;

	public SelectIconView(Context context, int iconId, int bakcolor, int curcolor) {
		super(context);
		mContext = context;

		// 描画用設定
		mBakPaint = new Paint();
		mBakPaint.setStyle(Style.FILL);
		mBakPaint.setColor(bakcolor);

		mCurPaint = new Paint();
		mCurPaint.setStyle(Style.STROKE);
		mCurPaint.setStrokeWidth(4);
		mCurPaint.setColor(curcolor);

		mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBmpPaint.setStyle(Style.FILL);

		Resources res = getResources();
		mIcon = BitmapFactory.decodeResource(res, iconId);

		if (mIcon != null) {
			mIconSrcRect = new Rect(0, 0, mIcon.getWidth(), mIcon.getHeight());
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// 選択中
		canvas.drawRect(mFrameRect, mSelect == true ? mCurPaint : mBakPaint);
		if (mIcon != null) {
			canvas.drawBitmap(mIcon, mIconSrcRect, mIconDstRect, mBmpPaint);
		}
		return;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		mFrameRect = new Rect(MARGIN1, MARGIN1, width - MARGIN1, height - MARGIN1);
		mIconDstRect = new Rect(MARGIN2, MARGIN2, width - MARGIN2, height - MARGIN2);

		setMeasuredDimension(width, height);
	}

	public void setSelect(boolean select) {
		if (mSelect != select) {
			mSelect = select;
			invalidate();
		}
	}
}
