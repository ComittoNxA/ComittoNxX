package src.comitton.view.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.FileOutputStream;

import static android.graphics.Matrix.MTRANS_X;
import static android.graphics.Matrix.MTRANS_Y;
import static java.lang.Math.abs;

public class CropImageView extends ImageView{
    private Paint mPaintGuide;
    private Paint mPaintMask;
    private int mImageWidth;
    private int mImageHeight;
    private RectF mScaledRect;
    private Rect mFrameRect;
    private int mTouchX;
    private int mTouchY;
    private int mMoveX;
    private int mMoveY;
    private boolean mSetParamsDone;
    private Bitmap mBitmap;
    private float mAspectRatio; // width/height
    private int mHandleMargin;
    private int mMinWidth = 100;
    private int mMinHeight = 100;
    private float mScale;
    private int mPaddingLeft;
    private int mPaddingTop;
    Matrix mMatrix;

    private int mTouchState;
    private final int TS_LEFT = 1;
    private final int TS_RIGHT = 1 << 1;
    private final int TS_TOP = 1 << 2;
    private final int TS_BOTTOM = 1 << 3;
    private final int TS_INSIDE = 1 << 4;

    // 選択範囲変更時のアス比通知用callback
    private CropCallback mCropCallback;
    public interface CropCallback{
        void cropCallback(float aspectRatio);
    }
    public void setCallback(CropCallback callback){
        mCropCallback = callback;
    }

    //コンストラクタ
    public CropImageView(Context context) {
        this(context, null, 0);
    }
    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mPaintGuide = new Paint();
        mPaintGuide.setColor(Color.WHITE);
        mPaintGuide.setStyle(Paint.Style.STROKE);
        mPaintGuide.setStrokeWidth((int)(3 * metrics.density));
        mPaintMask = new Paint();
        mPaintMask.setColor(0xA0000000);
        mPaintMask.setStyle(Paint.Style.FILL);
        mPaintMask.setAntiAlias(true);
        mPaintMask.setFilterBitmap(true);
        mTouchX = mTouchY = mMoveX = mMoveY = 0;
        mScaledRect = new RectF(0, 0, 0, 0);
        mBitmap = null;
        mFrameRect = new Rect(0, 0, 0, 0);
        mAspectRatio = 3.0f / 4.0f;
    }

    public void Crop(String path) {
        try {
            Bitmap bmDest = Bitmap.createBitmap(mBitmap, mFrameRect.left, mFrameRect.top, mFrameRect.width(), mFrameRect.height());
            FileOutputStream os = new FileOutputStream(path);
            bmDest.compress(Bitmap.CompressFormat.JPEG, 80, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("Thumbnail/cacheSave", e.getMessage());
        }
    }

    // width / height
    public void setAspectRatio(float ratio) {
        if(ratio == 0)ratio = 1;
        if(abs(ratio - mAspectRatio) < 0.005f)// 差が小さければ無視
            return;
        mAspectRatio = ratio;
        resize();
        invalidate();
    }

    // 正なら右、負なら左に移動
    public void move(float ratio){
        if(ratio == 0) return;
        int moveX = (int)(mImageWidth * ratio);
        if(mFrameRect.right + moveX > mImageWidth)
            moveX = mImageWidth - mFrameRect.right;
        else if(mFrameRect.left + moveX < 0)
            moveX = 0 - mFrameRect.left;

        mFrameRect.left += moveX;
        mFrameRect.right += moveX;
        invalidate();
    }

    public void resize() {
        int height = mImageHeight;
        int width = mImageWidth;
        if(width != 0) {
            if (width > height * mAspectRatio)
                width = (int)(height * mAspectRatio);
            else
                height = (int)(width / mAspectRatio);
            if(mFrameRect.width() == 0){
                mFrameRect.set(0,0,mImageWidth,mImageHeight);
            }
            int posX = mFrameRect.left;
            int posY = mFrameRect.top;
            if(posX + width > mImageWidth)
                posX = mImageWidth - width;
            if(posY + height > mImageHeight)
                posY = mImageHeight - height;
            mFrameRect.left = posX;
            mFrameRect.right = width + posX;
            mFrameRect.top = posY;
            mFrameRect.bottom = height + posY;
        }
        invalidate();
    }

    private void setParams() {
        mMatrix = getImageMatrix();
        float[] values = new float[9];
        mMatrix.getValues(values);
        Rect rect = getDrawable().getBounds();
        mScale = values[Matrix.MSCALE_X];
        mScaledRect.left = values[MTRANS_X];
        mScaledRect.top = values[MTRANS_Y];
        mScaledRect.right = mScale * rect.right + values[MTRANS_X];
        mScaledRect.bottom = mScale * rect.bottom + values[MTRANS_Y];
        mImageWidth = rect.right;
        mImageHeight = rect.bottom;
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mHandleMargin = (int)(30 / mScale);
        mSetParamsDone = true;
        resize();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mBitmap = bm;
        super.setImageBitmap(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); // 画像は任せて枠だけ自前で描画
        if(mBitmap != null) {
            if (!mSetParamsDone) {
                setParams();
            }
            canvas.save();
            if (getPaddingTop() != 0 && getPaddingLeft() != 0) {
                canvas.translate(getPaddingLeft(), getPaddingTop());
            }
            canvas.concat(mMatrix);

            canvas.clipRect(mFrameRect, Region.Op.DIFFERENCE);
            canvas.drawRect(0,0,mImageWidth, mImageHeight, mPaintMask);
            canvas.clipRect(mFrameRect, Region.Op.INTERSECT);
            canvas.drawRect(mFrameRect, mPaintGuide);
            canvas.restore();
        }
    }

    private int hitTest(int x, int y) {
        mTouchState = 0;
        if (abs(mFrameRect.left - x) < mHandleMargin)
            mTouchState |= TS_LEFT;
        else if (abs(mFrameRect.right - x) < mHandleMargin)
            mTouchState |= TS_RIGHT;
        if (abs(mFrameRect.bottom - y) < mHandleMargin) {
            mTouchState |= TS_BOTTOM;
        } else if (abs(mFrameRect.top - y) < mHandleMargin) {
            mTouchState |= TS_TOP;
        }
        if (mTouchState == 0 && mFrameRect.contains(x, y))
            mTouchState = TS_INSIDE;
        return mTouchState;
    }

    private int convPosX(float x){
        x = (x - (mPaddingLeft + mScaledRect.left)) / mScale;
        return (int)x;
    }
    private int convPosY(float y){
        y = (y - (mPaddingTop + mScaledRect.top)) / mScale;
        return (int)y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = convPosX(event.getX());
                mTouchY = convPosY(event.getY());
                mTouchState = hitTest(mTouchX, mTouchY);
                return true;
            //ドラッグ時
            case MotionEvent.ACTION_MOVE:
                mMoveX = convPosX(event.getX()) - mTouchX;
                mMoveY = convPosY(event.getY()) - mTouchY;
                // ガイドの中なら枠移動
                if (mTouchState == TS_INSIDE) {
                    // はみ出したら押し戻す
                    if (mMoveY + mFrameRect.top < 0)
                        mMoveY = 0 - mFrameRect.top;
                    else if (mMoveY + mFrameRect.bottom > mImageHeight)
                        mMoveY = mImageHeight - mFrameRect.bottom;
                    if (mMoveX + mFrameRect.left < 0)
                        mMoveX = 0 - mFrameRect.left;
                    else if (mMoveX + mFrameRect.right > mImageWidth)
                        mMoveX = mImageWidth - mFrameRect.right;
                    mFrameRect.top += mMoveY;
                    mFrameRect.bottom += mMoveY;
                    mTouchY += mMoveY;
                    mFrameRect.left += mMoveX;
                    mFrameRect.right += mMoveX;
                    mTouchX += mMoveX;
                    mMoveX = mMoveY = 0;
                    // ボーダーを摘まんだらサイズ変更
                } else if (mTouchState != 0) {
                    if ((mTouchState & TS_LEFT) != 0) {
                        // はみだし修正
                        if (mMoveX + mFrameRect.left < 0)
                            mMoveX = 0 - mFrameRect.left;
                        // 最小サイズ維持
                        if (mFrameRect.width() - mMoveX  < mMinWidth)
                            mMoveX = mFrameRect.width() - mMinWidth;
                        mFrameRect.left += mMoveX;
                        mTouchX += mMoveX;
                    } else if ((mTouchState & TS_RIGHT) != 0) {
                        if (mMoveX + mFrameRect.right > mImageWidth)
                            mMoveX = mImageWidth - mFrameRect.right;
                        if (mFrameRect.width() + mMoveX < mMinWidth)
                            mMoveX = mFrameRect.left + mMinWidth - mFrameRect.right;
                        mFrameRect.right += mMoveX;
                        mTouchX += mMoveX;
                    }
                    if ((mTouchState & TS_TOP) != 0) {
                        if (mMoveY + mFrameRect.top < 0)
                            mMoveY = 0 - mFrameRect.top;
                        if (mFrameRect.height() - mMoveY < mMinHeight)
                            mMoveY = mFrameRect.width() - mMinHeight;
                        mFrameRect.top += mMoveY;
                        mTouchY += mMoveY;
                    } else if ((mTouchState & TS_BOTTOM) != 0) {
                        if (mMoveY + mFrameRect.bottom > mImageHeight)
                            mMoveY = mImageHeight - mFrameRect.bottom;
                        if (mFrameRect.height() + mMoveY < mMinHeight)
                            mMoveY = mFrameRect.top + mMinHeight - mFrameRect.bottom;
                        mFrameRect.bottom += mMoveY;
                        mTouchY += mMoveY;
                    }
                }
                mMoveX = mMoveY = 0;
                mAspectRatio = (float)mFrameRect.width() / (float)mFrameRect.height();
                mCropCallback.cropCallback(mAspectRatio);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                mTouchX = mTouchY = mMoveX = mMoveY = 0;
                mAspectRatio = (float)mFrameRect.width() / (float)mFrameRect.height();
                mCropCallback.cropCallback(mAspectRatio);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mSetParamsDone = false;
    }
}