package src.comitton.view;

import java.io.IOException;
import java.util.ArrayList;

import src.comitton.common.DEF;
import src.comitton.data.MarkerDrawData;
import src.comitton.data.TextDrawData;
import src.comitton.data.PictureData;
import src.comitton.stream.CallTxtLibrary;
import src.comitton.stream.ImageManager;
import src.comitton.stream.TextInputStream;
import src.comitton.stream.TextManager;
import src.comitton.view.GuideView.UpdateListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.BitmapFactory;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MyTextView extends SurfaceView implements Handler.Callback, SurfaceHolder.Callback, UpdateListener, Runnable {
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
	private int mCenter = 0; // 中央のすき間幅
	private int mShadow = 0;
	private int mViewPoint = DEF.VIEWPT_RIGHTTOP;
	private int mScrlRangeW; // 音量によるスクロール最大幅
	private int mScrlRangeH; // 音量によるスクロール最大高さ
	private int mPinchScl;
	private int mSclMode;
	private int mDispMode; // 表示モード(0:単ページ, 1:見開き, 2:連続)
	private int mVolScrl; // ボリュームキー等でのスクｰロール量
	private boolean mPrevRev = false;
	private boolean mIsMargin = false; // 中央のすき間あり
	private boolean mIsShadow = false;
	private boolean mPseLand = false;
	private boolean mAscRotate;

	private int mDispWidth = 0;
	private int mDispHeight = 0;

	private float mScrollBaseX = 0;
	private float mScrollBaseY = 0;
	private int mOverScrollX;
	private int mOverScrollMax;

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
	private Bitmap mBackBitmap;
	private Bitmap mDrawBitmap;
	private boolean mEffectDraw = false; // エフェクト描画中(ビットマップからコピー)
//	BitmapDrawable mDrawable;

	private float mMomentiumX;
	private float mMomentiumY;
	private long mMomentiumTime;
	private int mMomentiumNum;
	private Message mMomentiumMsg;
	private int mMomentDrain;

	private int mCurrentPage;
	private TextDrawData mTextData[][] = new TextDrawData[2][];
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
//	private GradientDrawable mGradient;
//	private Shader mShader;

	// バックグラウンド処理
	private int mDrawPage;
	private DrawThread mDrawThread;
	private boolean mMessageBreak;
	private Thread mUpdateThread;
	private boolean mIsRunning;

	private Point mScrollPos[];
	private Point mScrollPoint;
	private boolean mScrolling;

	private String mTitle;
	private String mDrawTitle;
//	private String mAuther;

	private int mTextColor;
	private int mBackColor;
	private int mGradColor;
	private int mSrchColor;

	private int mGradation; // グラデーション有効

	private int mTextWidth;
	private int mTextHeight;
	private int mTextInfo;
//	private int mTextMarginW;
	private int mTextMarginH;

	private SparseArray<ArrayList<MarkerDrawData>> mMarker;
	private char mTextBuff[];
	private PictureData mPictures[];

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

//	private ViewTimerHanlder mViewTimerHanlder;
	private Handler mHandler;
	private Object mLock = new Object();
	private boolean mDrawBreak;

	Message mEventPageMsg;

	// 挿絵の保持
	private String mUser;
	private String mPass;
	private TextInputStream mTextStream;
	private SparseArray<BitmapDrawable> mPicMap1;
	private SparseArray<BitmapDrawable> mPicMap2;
	private int mPicMapPage;
	private ImageManager mImageMgr;
	private GuideView mGuideView;

	private float mShiftX[] = { 0.0f, 0.65f, 0.2f, 0.05f, 0.0f, 0.3f, 0.0f, 0.0f, 0.0f, -0.7f, -0.15f};
	private float mShiftY[] = { 0.0f, 0.5f, 0.1f, 0.10f, 0.1f, 0.5f, -0.03f, 0.03f, 0.0f, 0.0f, -0.30f};
// TODO 半角対応
//	private float mShiftX[] = { 0.0f, 0.65f, 0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7f, -0.15f};
//	private float mShiftY[] = { 0.0f, 0.5f, 0.1f, 0.0f, 0.1f, 0.0f, -0.03f, 0.03f, 0.0f, 0.0f, -0.30f};

	public MyTextView(Context context) {
		super(context);
		getHolder().addCallback(this);
		// SurfaceHolder.Callbackを継承したSurfaceCallback()です。

		// この3つを書いてフォーカスを当てないとSurfaceViewが動かないみたい
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTypeface(Typeface.DEFAULT);
//		mTextPaint.setTypeface(Typeface.MONOSPACE);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStyle(Paint.Style.STROKE);

		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setStyle(Paint.Style.FILL);

		mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mDrawPaint.setColor(0xFF000000);
//		mSmallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mSmallPaint.setColor(0xFF000000);
//		mSmallPaint.setTypeface(Typeface.MONOSPACE);
//		mSmallPaint.setTextAlign(Paint.Align.CENTER);

//		mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackPaint = new Paint();
		mCenterPaint = new Paint();
		mPaperPaint = new Paint();
		mMarkerPaint = new Paint();
		mMarkerPaint.setStyle(Style.FILL);

		mDstRect = new Rect();
		mSrcRect = new Rect();
//		mGradient = new GradientDrawable();
//		mShader = new Shader();

		mLinePath = new Path();

		mEffectRate = 0.0f;
		mEffectDraw = false;
		mInitialize = true;

//		mViewTimerHanlder = new ViewTimerHanlder();
		mHandler = new Handler(this);

		mTextStream = new TextInputStream();
		mPicMap1 = new SparseArray<BitmapDrawable>();
		mPicMap2 = new SparseArray<BitmapDrawable>();

		mDrawPage = -1;
	}

	/*
	 * private String mResizingStr; private TextView mTextView;
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mIsRunning = true;

		// 描画スレッド起動
		mUpdateThread = new Thread(this);
		mUpdateThread.setPriority(Thread.MAX_PRIORITY);
		mUpdateThread.start();
		update(false);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// Surface の属性が変更された際にコールされる
		updateNotify();
//		update();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface が破棄された際にコールされる
		// mMode = MODE_STOP;
		// 解放
		breakThread();
	}

	public void breakThread() {
		mIsRunning = false;
		mUpdateThread.interrupt();
	}

	public void updateNotify() {
		if (mUpdateThread != null) {
			mUpdateThread.interrupt();
		}
		else {
			update(false);
		}
	}

	@Override
	public void onUpdate() {
		// ガイドの更新
		updateNotify();
	}

	private boolean mDrawLock;
	public void lockDraw() { mDrawLock = true; }

	public void unlockDraw() { mDrawLock = false; }

	public boolean update(boolean unlock) {
		if (mDrawLock == true && unlock == false) {
			return false;
		}
		if (unlock == true) {
			// 描画ロック解除
			mDrawLock = false;
		}

		Canvas canvas = null;
		SurfaceHolder surfaceHolder = getHolder();
		try {
			canvas = surfaceHolder.lockCanvas(); // ロックして、書き込み用のcanvasを受け取る
			if (canvas == null)
				return false; // canvasが受け取れてなかったら抜ける

//			synchronized (surfaceHolder) {
				drawScreen(canvas);
//			}
		} finally {
			if (canvas != null)
				try {
					surfaceHolder.unlockCanvasAndPost(canvas); // 例外が出て、canvas受け取ってたらロックはずす
				}
				catch (IllegalStateException e) {
					// Surface has already been released.
				}
		}
		return true;
	}

	public void drawScreen(Canvas canvas) {
		// 背景色設定
		canvas.drawColor(mMgnColor);
		if (mTextData == null) {
			return;
		}

		int cx;
		int cy;
		if (mEffectDraw == true && mBackBitmap != null) {
			// エフェクト中
			// 前のページ描画
			canvas.drawBitmap(mBackBitmap, 0, 0, null);
		}

		if (mPseLand == false) {
			cx = getWidth();
			cy = getHeight();
		}
		else {
			cx = getHeight();
			cy = getWidth();
			int pos;
			if (cx < cy) {
				pos = cx / 2;
			}
			else {
				pos = cy / 2;
			}
			canvas.rotate(90.0f, pos, pos);
		}

		if (mEffectDraw != true) {
			// エフェクト中でなければ背景を描画
			// canvas.drawRect(0, 0, cx, cy, mBackPaint);
		}
		else {
			// エフェクト中は描画位置をずらす
			canvas.translate(cx * mEffectRate * (mIsPageBack ? 1 : -1), 0);
//			Log.d("draw", "alpha" + mEffectRate);
		}

		if (mInitialize == true) {
			return;
		}

		// 描画
		int st;
		int ed;
		if (mDispMode == DEF.DISPMODE_TX_HALF) {
			st = mCurrentPage;
			ed = mCurrentPage;
		}
		else if (mDispMode == DEF.DISPMODE_TX_DUAL) {
			st = mCurrentPage;
			ed = mCurrentPage + 1;
		}
		else {
			st = mCurrentPage - 2;
			ed = mCurrentPage + 2;
		}

		int draw_index = 0;

		mDrawBreak = true;
		synchronized (mLock) {
			mDrawBreak = false;
			for (int p = st; p <= ed; p++) {
				if (p < 0 || p >= mTextData.length || mTextData[p] == null) {
					continue;
				}

				if (mDispMode == DEF.DISPMODE_TX_DUAL) {
					if (0 <= mCurrentPage && mCurrentPage < mTextData.length - 1) {
						// 2ページ表示時
						draw_index = 1 - (p - st);
					}
				}
				else if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
					draw_index = (mTextData.length - p - 1);
				}

				Rect rcDraw = mDstRect;
				rcDraw.left = (int)(mDrawLeft + (mDrawWidth + mCenter) * draw_index);
				rcDraw.top = (int)mDrawTop;
				rcDraw.right = rcDraw.left + mDrawWidth;
				rcDraw.bottom = (int)(mDrawTop + mDrawHeight);
				if (rcDraw.right < 0 || rcDraw.left > mDispWidth) {
					continue;
				}

				if (mDrawBitmap == null) {
					// 表示位置のみを描画
					if (mGradation != 0) {
						// グラデーションをセット
						RectF rcGrad = getGradationRect(rcDraw.left, rcDraw.top, rcDraw.right, rcDraw.bottom, mGradation);
						Shader s = new LinearGradient(rcGrad.left, rcGrad.top, rcGrad.right, rcGrad.bottom, mBackColor, mGradColor, Shader.TileMode.CLAMP);
						mPaperPaint.setShader(s);
					}
					canvas.drawRect(rcDraw, mPaperPaint);
					if (mCurrentPage - 2 <= p && p <= mCurrentPage + 2) {
						TextDraw(canvas, p, (int) rcDraw.left, (int) rcDraw.top);
					}
				}
				else {
					int result = CallTxtLibrary.GetTextImage(mDrawBitmap, p);
					if (result != 0) {
						setDrawBitmap(p);
//						Log.d("draw", "page=" + p);
					}
					if (rcDraw.left < 0) {
						mSrcRect.left = rcDraw.left * -1;
						rcDraw.left = 0;
					}
					else {
						mSrcRect.left = 0;
					}
//					canvas.drawBitmap(mDrawBitmap, rcDraw.left, rcDraw.top, mDrawPaint);
					canvas.drawBitmap(mDrawBitmap, mSrcRect, rcDraw, mDrawPaint);
//					Log.d("drawBitmap", "l=" + rcDraw.left + ", t=" + rcDraw.top + ", r=" + rcDraw.right + ", b=" + rcDraw.bottom);
				    //canvasに描画
//				    canvas.save();
//				    canvas.translate(rcDraw.left, rcDraw.top);
//				    mDrawable.draw(canvas);
//				    canvas.restore();
//
//					Log.d("drawBitmap", "l=" + rcDraw.left + ", t=" + rcDraw.top + ", r=" + rcDraw.right + ", b=" + rcDraw.bottom);
				}
			}
		}

		if (mDispMode == DEF.DISPMODE_TX_DUAL && (0 <= mCurrentPage && mCurrentPage < mTextData.length - 1)) {
			int center = 0;
			// 中央のすき間
			if (mIsMargin && mCenter > 0) {
				canvas.drawRect(mDrawLeft + mDrawWidth, mDrawTop, mDrawLeft + mDrawWidth + mCenter, mDrawTop + mDrawHeight, mCenterPaint);
				center = mCenter;
			}

			// 中央影
			if (mIsShadow && mShadow > 0) {
				// グラデーション幅算出
				int grad_cx = 0;
				int cen_x1, cen_x2;
				GradientDrawable grad;
				int colors[] = { 0x00000000, 0x06000000, 0x10000000, 0x30000000, 0x80000000 };

				if (mShadow > 0) {
					grad_cx = mDrawWidth * mShadow / 100;
				}

				cen_x1 = (int) mDrawLeft + mDrawWidth + center;
				cen_x2 = cen_x1 + grad_cx;
				grad = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
				grad.setBounds(new Rect(cen_x1, (int) mDrawTop, cen_x2, (int) mDrawTop + mDrawHeight));
				grad.draw(canvas);

				cen_x1 = (int) mDrawLeft + mDrawWidth - grad_cx;
				cen_x2 = cen_x1 + grad_cx;
				grad = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
				grad.setBounds(new Rect(cen_x1, (int) mDrawTop, cen_x2, (int) mDrawTop + mDrawHeight));
				grad.draw(canvas);
			}
		}

		// オーバースクロール
		if (mOverScrollX != 0) {
			// グラデーション幅算出
			int grad_cx = Math.min(cx, cy) / 20;
			int cen_x1, cen_x2;
			GradientDrawable grad;
			int colors[] = {0, 
					0x06000000 | (mGuiColor & 0x00FFFFFF),
					0x10000000 | (mGuiColor & 0x00FFFFFF),
					0x30000000 | (mGuiColor & 0x00FFFFFF),
					0x80000000 | (mGuiColor & 0x00FFFFFF)};
			if (mOverScrollX < 0) {
				cen_x1 = cx + grad_cx * mOverScrollX / mOverScrollMax;
				cen_x2 = cx;
				grad = new GradientDrawable(Orientation.LEFT_RIGHT, colors);  
				grad.setBounds(cen_x1, 0, cen_x2, cy);
				grad.draw(canvas);  
			}
			else if (mOverScrollX > 0) {
				cen_x1 = 0;
				cen_x2 = grad_cx * mOverScrollX / mOverScrollMax;
				grad = new GradientDrawable(Orientation.RIGHT_LEFT, colors);  
				grad.setBounds(cen_x1, 0, cen_x2, cy);
				grad.draw(canvas);
			}
		}

//		if (mPseLand == true) {
//			canvas.restore();
//		}
		// ガイド表示
		if (mGuideView != null) {
			mGuideView.draw(canvas, cx, cy);
		}
	}

	// ガイド表示用クラス
	public void setGuideView(GuideView view) {
		mGuideView = view;
		view.setParentView(this);
		view.setUpdateListear(this);
	}

	private void setDrawBitmap(int page) {
		if (mDrawBitmap == null) {
			return;
		}
		Canvas canvasBmp = new Canvas(mDrawBitmap);
		canvasBmp.drawColor(mBackColor);

		// 表示位置のみを描画
		int bmpCx = mDrawBitmap.getWidth();
		int bmpCy = mDrawBitmap.getHeight();
		if (mGradation != 0) {
			// グラデーションをセット
			RectF rcGrad = getGradationRect(0, 0, bmpCx, bmpCy, mGradation);
			Shader s = new LinearGradient(rcGrad.left, rcGrad.top, rcGrad.right, rcGrad.bottom, mBackColor, mGradColor, Shader.TileMode.CLAMP);
			mPaperPaint.setShader(s);
		}
		canvasBmp.drawRect(0, 0, bmpCx, bmpCy, mPaperPaint);
		if (mCurrentPage - 2 <= page && page <= mCurrentPage + 2) {
			TextDraw(canvasBmp, page, (int) 0, (int) 0);
		}
		if (mDrawBreak == false) {
			CallTxtLibrary.SetTextImage(mDrawBitmap, page, mCurrentPage);
		}
//		Log.d("setDrawBitmap", "index=" + page);
	}

	private RectF getGradationRect(float l, float t, float r, float b, int gradation) {
		if (gradation <= 0 || gradation > 8) {
			return null;
		}

		RectF rc = new RectF();
		// left
		switch (gradation) {
			case 3: // rt->lb
			case 4: // rc->lc
			case 5: // rb->lt
				rc.left = r;
				rc.right = l;
				break;
			case 7: // lb->rt
			case 8: // lc->rc
			case 1: // lt->rb
				rc.left = l;
				rc.right = r;
				break;
			case 2: // ct->cb
			case 6: // cb->ct
				rc.left = (r - l) / 2;
				rc.right = (r - l) / 2;
				break;
		}
		switch (gradation) {
			case 2: // ct->cb
			case 3: // rt->lb
			case 1: // lt->rb
				rc.top = t;
				rc.bottom = b;
				break;
			case 5: // rb->lt
			case 6: // cb->ct
			case 7: // lb->rt
				rc.top = b;
				rc.bottom = t;
				break;
			case 4: // rc->lc
			case 8: // lc->rc
				rc.top = (b - t) / 2;
				rc.bottom = (b - t) / 2;
				break;
		}
		return rc;
	}

	private void TextDraw(Canvas canvas, int index, float x, float y) {
		if (mTextData == null) {
			return;
		}
		TextDrawData tdds[] = mTextData[index];

		float shiftX[] = mShiftX;
		float shiftY[] = mShiftY;

		// 作成済みの描画情報を使用
		Paint text = mTextPaint;
		Paint line = mLinePaint;
		Paint circle = mCirclePaint;
		Paint marker = mMarkerPaint;

		if (mMarker != null) {
			// マーカー表示
			ArrayList<MarkerDrawData> mdlist = mMarker.get(index);
			if (mdlist != null) {
    			for (MarkerDrawData md : mdlist) {
    				float x1 = x + mDrawWidth - md.mX1 * mDrawScale;
    				float y1 = y + md.mY1 * mDrawScale;
    				float x2 = x + mDrawWidth - md.mX2 * mDrawScale;
    				float y2 = y + md.mY2 * mDrawScale;
    				canvas.drawRect(x1, y1, x2, y2, marker);
    			}
			}
		}
		
		// 画面上部にタイトル描画
		float infosize = mTextInfo * mDrawScale;
		float margin = (mTextMarginH - mTextInfo) * mDrawScale / 3;
		text.setTextSize(infosize);
		FontMetrics fm = text.getFontMetrics();

		// タイトル描画
		if (mDrawTitle != null) {
			canvas.drawText(mDrawTitle, x + mDrawWidth / 2, y + -fm.top + margin, text);
		}

		// ページ番号描画
		String pagestr = (index + 1) + " / " + mTextData.length;
		canvas.drawText(pagestr, x + mDrawWidth / 2, y + mDrawHeight - fm.bottom - margin, text);

		char[] code = new char[2];

		for (int i = 0 ; i < tdds.length && mDrawBreak == false ; i++) {
			TextDrawData tdd = tdds[i]; 
			// ビットマップ描画
			if (tdd.mTextPos == -1) {
				int picIndex = tdd.mTextLen;
				if (picIndex >= 0 && picIndex < mPictures.length) {
					PictureData picdata = mPictures[picIndex];
					if (picdata != null && picdata.mIsError == false) {
						BitmapDrawable bm = loadPicture(picIndex, picdata.mFileName);
						if (bm != null) {
							// bitmapを拡大縮小描画
						    Matrix matrix = new Matrix();
							int drawX = (int)(x + mDrawWidth - ((float) tdd.mTextX + picdata.mWidth) * mDrawScale);
							int drawY = (int)(y + (float)tdd.mTextY * mDrawScale);
							float srcWidth = bm.getBitmap().getWidth();
							float srcHeight = bm.getBitmap().getHeight();
							float dstWidth = (float)picdata.mWidth * mDrawScale;
							float dstHeight = (float)picdata.mHeight * mDrawScale;

						    matrix.postScale(dstWidth / srcWidth, dstHeight / srcHeight);

						    canvas.save();
						    canvas.translate(drawX, drawY);
							canvas.concat(matrix);
						    //canvasに描画
						    bm.draw(canvas);
						    canvas.restore();
						}
						else {
							// 読み込めなかった場合は次からは読まない
							picdata.mIsError = true;
						}
					}
				}
				continue;
			}
			else if ((tdd.mTextPos & TextManager.STYLE_LINE_BASE) != 0) {
				// 傍線
				float ty1 = tdd.mTextY * mDrawScale;
				float ty2 = (tdd.mTextY + tdd.mGap) * mDrawScale;
				float tx = mDrawWidth - tdd.mTextX * mDrawScale;
				float textscl = tdd.mTextSize * mDrawScale;
				line.setStrokeWidth(textscl / 24.0f);
				
				if (tdd.mTextPos != TextManager.STYLE_LINE5) {
					// 波線以外
    				float w = tdd.mTextSize / 8 * mDrawScale;
    				tx += w;
    				if (tdd.mTextPos == TextManager.STYLE_LINE3) {
    					 line.setPathEffect(new DashPathEffect(new float[]{ w, w }, 0));
    				}
    				else if (tdd.mTextPos == TextManager.STYLE_LINE4) {
    					 line.setPathEffect(new DashPathEffect(new float[]{ w * 3, w }, 0));
    				}
    				canvas.drawLine(x + tx, y + ty1, x + tx, y + ty2, line);
    				line.setPathEffect(null);
    				if (tdd.mTextPos == TextManager.STYLE_LINE2) {
    					// 二重傍線
    					tx += tdd.mTextSize / 8 * mDrawScale; 
    					canvas.drawLine(x + tx, y + ty1, x + tx, y + ty2, line);
    				}
				}
				else {
					// 波線
					Path linepath = mLinePath;
					linepath.reset();
    				float w = tdd.mTextSize / 8 * mDrawScale;
    				int cnt = 1;
    				tx += w;
    				linepath.moveTo(x + tx, y + ty1);
    				
    				while (ty1 + cnt * w < ty2) {
    					if (cnt % 2 == 1) {
    						linepath.lineTo(x + tx + w, y + ty1 + cnt * w);
    					}
    					else {
    						linepath.lineTo(x + tx, y + ty1 + cnt * w);
    					}
    					cnt ++;
    				}
    				canvas.drawPath(linepath, line);
				}
				continue;
			}
 
			// テキストの描画情報
			float textscl = tdd.mTextSize * mDrawScale;
			float textscl_h = tdd.mGap * mDrawScale;
			text.setTextSize(textscl);
			// small.setTextSize(textscl);
			fm = text.getFontMetrics();
			float textpos = -fm.top + (textscl - (-fm.top + fm.bottom)) / 2;// -fm.top; // ascent; // + fm.descent; 全各文字はdescentがないようだ

			line.setStrokeWidth(textscl / 20.0f);

			float tx = mDrawWidth - (float) tdd.mTextX * mDrawScale - textscl / 2;
			float ypos = 0;
			int idx = tdd.mTextPos;
			int len = tdd.mTextLen;
			char point = ' '; 
			int extidx = 0;

			if ((tdd.mTextPos & TextManager.STYLE_POINT_BASE) != 0) {
				// 傍点
				point = TextManager.getStylePointChar(tdd.mTextPos);
			}
			for (int j = 0; j < len; j++) {
				if ((tdd.mTextPos & TextManager.STYLE_POINT_BASE) == 0) {
					code[0] = mTextBuff[idx + j];
				}
				else {
					code[0] = point;
				}
				int charInfo = getCharInfo(code[0]);
				int shift = isShift(code[0]);
				float skip = 1.0f;
				if (code[0] >= 0x1a00 && code[0] <= 0x1cff) {
					// 文字画像
					int picIndex = code[0] - 0x1a00;
					if (picIndex >= 0 && picIndex < mPictures.length) {
						PictureData picdata = mPictures[picIndex];
						if (picdata != null && picdata.mIsError == false) {
							BitmapDrawable bm = loadPicture(picIndex, picdata.mFileName);
							if (bm != null) {
							    Matrix matrix = new Matrix();
								int drawX = (int)(x + mDrawWidth - tdd.mTextX * mDrawScale - textscl);
								int drawY = (int)(y + tdd.mTextY * mDrawScale + ypos);
								float srcWidth = bm.getBitmap().getWidth();
								float srcHeight = bm.getBitmap().getHeight();
								float dstWidth = textscl;
								float dstHeight = textscl;

							    matrix.postScale(dstWidth / srcWidth, dstHeight / srcHeight);

							    canvas.save();
							    canvas.translate(drawX, drawY);
								canvas.concat(matrix);
							    //canvasに描画
							    bm.draw(canvas);
							    canvas.restore();
							}
							else {
								// 読み込めなかった場合は次からは読まない
								picdata.mIsError = true;
							}
						}
					}
				}
				else if (code[0] >= 0x2a00 && code[0] <= 0x2cff) {
					// そのまま描画
					float ty = (float) tdd.mTextY * mDrawScale + ypos + textpos;
					float sx = textscl * shiftX[shift];
					float sy = -textscl * shiftY[shift];
					float sx2 = 0.0f;
					float sy2 = 0.0f;
					code[1] = 0;
					switch (code[0]) {
						case TextManager.CHARDRAW_HANDAKU:
							// 半濁点付き
							code[1] = '゜';
							sx2 = textscl * 0.9f;
							sy2 = textscl * 0.05f;
							break;
						case TextManager.CHARDRAW_DAKUTEN:
							// 濁点付き
							code[1] = '゛';
							sx2 = textscl * 0.9f;
							sy2 = textscl * 0.05f;
							break;
						case TextManager.CHARDRAW_YOKO:
							// そのまま横に並べるだけ
							break;
						case TextManager.CHARDRAW_KOGAKI:
							// そのまま横に並べるだけ
							break;
						case TextManager.CHARDRAW_KOGAKIDAKUTEN:
							// そのまま横に並べるだけ
							break;
						case TextManager.CHARDRAW_KOGAKIHANDAKU:
							// そのまま横に並べるだけ
							break;
					}
					
					if (tdd.mExData != null && extidx < tdd.mExData.length) {
						// 文字描画
						int extlen = tdd.mExData[extidx].length;
						canvas.drawText(tdd.mExData[extidx], 0, extlen, x + tx + sx, y + ty + sy, text);
						extidx ++;
					}
					if (code[1] != 0) {
						// 追加
						canvas.drawText(code, 1, 1, x + tx + sx2, y + ty + sy2, text);
					}
				}
				else if (mAscRotate && code[0] < 0x80) {
					// 半角回転モードかつ半角
					canvas.save();
					float rotateX = x + mDrawWidth - ((float) tdd.mTextX) * mDrawScale - textpos;
					float rotateY = y + (float) tdd.mTextY * mDrawScale + ypos;
					canvas.rotate(90, rotateX, rotateY);
					// 描画
					text.setTextAlign(Align.LEFT);
					canvas.drawText(mTextBuff, idx, len, rotateX, rotateY, text);
					text.setTextAlign(Align.CENTER);
					// 回転を元に戻す
					canvas.restore();
					break;
				}
				else if (code[0] == '―') {
					int cnt = 1;
					while (j + cnt < len) {
						if (mTextBuff[idx + j + cnt] != '―') {
							break;
						}
						cnt++;
					}
					float ty1 = (float) tdd.mTextY * mDrawScale + ypos + textscl_h / 8;
					float ty2 = (float) tdd.mTextY * mDrawScale + ypos + textscl_h * cnt - textscl_h / 8;
					canvas.drawLine(x + tx, y + ty1, x + tx, y + ty2, line);
					j += cnt - 1;
					skip = cnt;
				}
				else if (code[0] == '…') {
					int cnt = 1;
					while (j + cnt < len) {
						if (mTextBuff[idx + j + cnt] != '…') {
							break;
						}
						cnt++;
					}
					float ty1 = (float) tdd.mTextY * mDrawScale + ypos;
					float ty2 = ty1 + textscl_h * cnt;
					float r = textscl / 18.0f;
					float sp = (int)((ty2 - ty1) / (cnt * 3));
					float gy = sp / 2;
					for (int dot = 0 ; dot < cnt * 3 ; dot ++) {
						canvas.drawCircle(x + tx, y + ty1 + gy + sp * dot, r, circle);
					}
					j += cnt - 1;
					skip = cnt;
				}
				else if ((charInfo & CHINFO_REVERSE) != 0) {
					canvas.save();
					float rotateX = x + mDrawWidth - ((float) tdd.mTextX) * mDrawScale - textscl + textpos;
					float rotateY = y + (float) tdd.mTextY * mDrawScale + ypos + textscl / 2;
					float sx = textscl * shiftX[shift];
					float sy = -textscl * shiftY[shift];

					// 横反転
					canvas.scale(-1.0f, 1.0f, rotateX, rotateY);
					canvas.rotate(90, rotateX, rotateY);
					// 描画
					canvas.drawText(code, 0, 1, rotateX + sx, rotateY - sy, text);
					// 回転を元に戻す
					canvas.restore();
				}
				else if ((charInfo & CHINFO_TURN) != 0) {
					canvas.save();
					float rotateX = x + mDrawWidth - ((float) tdd.mTextX) * mDrawScale - textpos;
					float rotateY = y + (float) tdd.mTextY * mDrawScale + ypos;
					float sx = textscl * shiftX[shift];
					float sy = -textscl * shiftY[shift];
					canvas.rotate(180, rotateX, rotateY);
					// 描画
					canvas.drawText(code, 0, 1, rotateX + sx, rotateY + sy, text);
					// 回転を元に戻す
					canvas.restore();
				}
				else if (charInfo != 0) {
					canvas.save();
					float rotateX = x + mDrawWidth - ((float) tdd.mTextX) * mDrawScale - textpos;
					float rotateY = y + (float) tdd.mTextY * mDrawScale + ypos + textscl / 2;
					float sx = textscl * shiftX[shift];
					float sy = -textscl * shiftY[shift];
					canvas.rotate(90, rotateX, rotateY);
					// 描画
					canvas.drawText(code, 0, 1, rotateX + sx, rotateY + sy, text);
					// 回転を元に戻す
					canvas.restore();
				}
				else {
					// そのまま描画
					float ty = (float) tdd.mTextY * mDrawScale + ypos + textpos;
					float sx = textscl * shiftX[shift];
					float sy = -textscl * shiftY[shift];
					canvas.drawText(code, 0, 1, x + tx + sx, y + ty + sy, text);
				}

				// 1文字分進める
				ypos += textscl_h * skip;
//				}
			}
		}
//		Log.d("TextDraw", "index=" + index);
		return;
	}

	// 挿絵管理
	private BitmapDrawable loadPicture(int picindex, String filename) {
		if (filename == null) {
			return null;
		}
		if (mPicMapPage != mCurrentPage) {
			// ページが変わったとき
			if (mPicMap1.size() > 0) {
				// データがあるとき
				SparseArray<BitmapDrawable> map = mPicMap1;
				mPicMap2.clear();
				mPicMap1 = mPicMap2;
				mPicMap2 = map;
				System.gc();	// メモリ解放
			}
			mPicMapPage = mCurrentPage;
		}

		BitmapDrawable bmdraw = mPicMap1.get(picindex);
		if (bmdraw == null) {
			bmdraw = mPicMap2.get(picindex);
			if (bmdraw != null) {
				mPicMap1.put(picindex, bmdraw);
			}
		}
		if (bmdraw == null) {
			Bitmap bm = null;
			try {
				// ビットマップの読み込み
				if (mImageMgr != null) {
					if (mImageMgr.getFileType() != ImageManager.FILETYPE_DIR) {
						bm = mImageMgr.loadBitmapByName(filename);
					}
					else {
						mTextStream.fileAccessInit(filename, mUser, mPass);
						try {
							bm = BitmapFactory.decodeStream(mTextStream);
						}
						catch (OutOfMemoryError e) {
							;
						}
						mTextStream.fileClose();
					}
				}
				else {
					mTextStream.fileAccessInit(filename, mUser, mPass);
					try {
						bm = BitmapFactory.decodeStream(mTextStream);
					}
					catch (OutOfMemoryError e) {
						;
					}
					mTextStream.fileClose();
				}
			}
			catch (IOException e) {
				// 読み込みエラー
				String s = "";
				if (e != null && e.getMessage() != null) {
					s = e.getMessage();
				}
				Log.e("Text/BitmapLoad", s);
			}
			// ハッシュに登録
			if (bm != null) {
				bmdraw = new BitmapDrawable(bm);
				bmdraw.setBounds(0, 0, bm.getWidth(), bm.getHeight());
				mPicMap1.put(picindex, bmdraw);
			}
		}
		return bmdraw;
	}

	private int getCharInfo(char ch) {
// TODO 半角対応
//		if (0x0020 <= ch && ch <= 0x007f) {
//			return CHINFO_HALF;
//		}
		switch (ch) {
			case '「':
			case '」':
			case '【':
			case '】':
			case '『':
			case '』':
			case '［':
			case '］':
			case '｛':
			case '｝':
			case '…':
			case '‥':
			case '－':
			case '-':
			case 'ｰ':
			case '〔':
			case '〕':
			case '《':
			case '》':
			case '〈':
			case '〉':
			case '≫':
			case '≪':
			case '＞':
			case '＜':
			case '｟':
			case '｠':
			case '〘':
			case '〙':
			case '〖':
			case '〗':
			case '«':
			case '»':
			case '→':
			case '↑':
			case '↓':
			case '←':
			case '―':
			case '～':
			case '<':
			case '>':
			case '(':
			case ')':
			case '[':
			case ']':
			case '｢':
			case '｣':
			case ':':
			case ';':
			case '（':
			case '）':
			case '｜':
			case '：':
			case '|':
			case '{':
			case '}':
			case '⇒':
			case '⇔':
			case '＝':
			case '=':
			case '≠':
			case '≒':
			case '─':
			case '│':
			case '┌':
			case '┐':
			case '┘':
			case '└':
			case '├':
			case '┬':
			case '┤':
			case '┴':
			case '┼':
			case '━':
			case '┃':
			case '┏':
			case '┓':
			case '┛':
			case '┗':
			case '┣':
			case '┳':
			case '┫':
			case '┻':
			case '╋':
			case '┠':
			case '┯':
			case '┨':
			case '┷':
			case '┿':
			case '┝':
			case '┰':
			case '┥':
			case '┸':
			case '╂':
				return CHINFO_ROTATE;
			case '“':
			case '”':
				return CHINFO_TURN;
			case 'ー':
				return CHINFO_ROTATE | CHINFO_REVERSE;
			default:
//				if (mAscRotate) {
//					// 半角回転モード
//					switch (ch) {
//						case '\'':
//						case '\"':
//						case '/':
//							return CHINFO_ROTATE;
//					}
//				}
				break;
		}
		return 0;
	}

	private int isShift(char ch) {
		switch (ch) {
			case '、':
			case '。':
			case '，':
			case '．':
				return 1;
			case 'ぁ':
			case 'ぃ':
			case 'ぅ':
			case 'ぇ':
			case 'ぉ':
			case 'ゃ':
			case 'ゅ':
			case 'ょ':
			case 'ゎ':
			case 'っ':
			case 'ァ':
			case 'ィ':
			case 'ゥ':
			case 'ェ':
			case 'ォ':
			case 'ャ':
			case 'ュ':
			case 'ョ':
			case 'ッ':
			case 'ｧ':
			case 'ｨ':
			case 'ｩ':
			case 'ｪ':
			case 'ｫ':
			case 'ｬ':
			case 'ｭ':
			case 'ｮ':
			case 'ｯ':
				return 2;
			case '：':
// TODO 半角対応				
//			case '+':
//			case '!':
//			case '"':
//			case '#':
//			case '$':
//			case '%':
//			case '&':
//			case '\'':
//			case '(':
//			case '*':
//			case ')':
//			case '-':
//			case '/':
//			case ':':
//			case ';':
//			case '<':
//			case '=':
//			case '>':
//			case '?':
//			case '@':
//			case '[':
//			case '\\':
//			case ']':
//			case '^':
//			case '_':
//			case '`':
//			case '~':
				return 3;
			case '―':
				return 4;
			case ',':
			case '.':
				return 5;
			case '〳':
			case '〴':
				return 6;
			case '〵':
				return 7;
			case 'ー':
				return 8;
			case '“':
				return 9;
			case '”':
				return 10;
		}
		return 0;
	}

	// 余白色を設定
	public boolean setConfig(int mclr, int cclr, int gclr, int vp, int mgn, int cen, int sdw, int srngw, int srngh, int svol, boolean pr, boolean cmgn, boolean csdw, boolean psel, boolean effect, int effecttime, String fontfile, boolean ascrt) {
		boolean result = true;

		mMgnColor = mclr;
		mCenColor = cclr;
		mGuiColor = gclr;
		mViewPoint = vp;
		mMargin = mgn;
		if (cmgn) {
			mCenter = cen;
		}
		else {
			mCenter = 0;
		}
		mShadow = sdw;
		mPrevRev = pr;
		mIsMargin = cmgn;
		mIsShadow = csdw;
		mPseLand = psel;
		mScrlRangeW = srngw;
		mScrlRangeH = srngh;
		mVolScrl = svol;
		mEffect = effect;
		mEffectTime = effecttime;
		if (mBackBitmap == null) {
			int cx = getWidth();
			int cy = getHeight();
			if (cx > 0 && cy > 0) {
				for (int retry = 0 ; retry < 3 ; retry ++) {
					try {
						mBackBitmap = Bitmap.createBitmap(cx, cy, Config.RGB_565);
						break;
					}
					catch (OutOfMemoryError e) {
						Log.i("TextView", "setConfig - OutOfMemoryError");
						System.gc();
					}
				}
			}
		}
		mAscRotate = ascrt;

		mBackPaint.setColor(mMgnColor);
		mCenterPaint.setColor(mCenColor);
		mPaperPaint.setShader(null);

		// フォントファイルの場所　/sdcard/ipam.ttf
		// テキストビューにフォントファイルを指定
		Typeface face = null;
		if (fontfile != null && fontfile.length() > 0) {
			try {
				face = Typeface.createFromFile(fontfile);
			}
			catch (RuntimeException e) {
				result = false;
			}
			if (face != null) {
				mTextPaint.setTypeface(face);
			}
		}
		if (face == null) {
			mTextPaint.setTypeface(Typeface.DEFAULT);
		}
		
		Paint test = new Paint(mTextPaint);

		// 'ー'のずれを調べる
		test.setTextSize(32);
		test.setColor(0xffffffff);

		FontMetrics fm = test.getFontMetrics();
		float textpos = -fm.top + (32 - (-fm.top + fm.bottom)) / 2;
		
		Bitmap bm = Bitmap.createBitmap(32, 32, Config.RGB_565);
		Canvas canvas = new Canvas(bm);
		canvas.drawColor(0);

		// 横反転
		float dx = textpos;
		float dy = 16;
		canvas.scale(-1.0f, 1.0f, dx, dy);
		canvas.rotate(90, dx, dy);
		// 描画
		canvas.drawText("ー", 16, 16, test);
//		canvas.drawText("ー", 15.5f, textpos, mTextPaint);

		int xcnt[] = new int[32];
		int pics[] = new int[32 * 32];
		bm.getPixels(pics, 0, 32, 0, 0, 32, 32);

		int maxline = 0;
		for (int x = 0; x < 32; x ++) {
			xcnt[x] = 0;
			for (int y = 0; y < 32; y ++) {
				int color = bm.getPixel(x, y);
				xcnt[x] += (color & 0xFF);
			}
			if (xcnt[maxline] < xcnt[x]) {
				maxline = x;
			}
		}
		mShiftY[8] = (maxline - 16.0f) / -16.0f / 2;
		
		synchronized (mLock) {
			// イメージデータを解放
			CallTxtLibrary.FreeTextImage();
		}
		return result;
	}

	public void setPicturePath(String path, ImageManager imagemgr, String user, String pass) {
		// 挿絵のパス
		// mPicturePath = path;
		mImageMgr = imagemgr;
		mUser = user;
		mPass = pass;
	}

	public void setDispMode(int dmode) {
		mDispMode = dmode; // 表示モード
	}

	public void setColor(int clrtxt, int clrbak, int clrgrd, int gradation, int clrsrh) {
		mTextColor = clrtxt;
		mBackColor = clrbak;
		mGradColor = clrgrd;
		mGradation = gradation;
		mSrchColor = clrsrh;

		mTextPaint.setColor(mTextColor);
		mLinePaint.setColor(mTextColor);
		mCirclePaint.setColor(mTextColor);
		mPaperPaint.setColor(mBackColor);
		mMarkerPaint.setColor(mSrchColor);
	}

	// 拡大率設定 (テキスト用)
	public void setTextScale(int sclmode, int pinchscl) {
		mSclMode = sclmode;
		mPinchScl = pinchscl;
	}

	// エフェクト処理
	public void setEffectRate(float rate) {
		mEffectRate = rate;
		update(false);
	}

	public void setTextBuffer(char textbuff[], String title, TextDrawData td[][]) {
		mTextBuff = textbuff;
		mTitle = title;
		mTextData = td;
	}

	public void setMarker(SparseArray<ArrayList<MarkerDrawData>> marker) {
		mMarker = marker;
		synchronized (mLock) {
			// イメージデータを解放
			CallTxtLibrary.FreeTextImage();
		}
		update(false);
	}

	public void setPictures(PictureData pictures[]) {
		mPictures = pictures;
	}

	// 指定幅分だけ文字列を切り出す
	public static String lengthSubstring(String str, int cx, Paint text) {
		if (str == null) {
			return null;
		}

		// 幅情報取得
		float dotwidth = text.measureText("...");
		float result[] = new float[str.length()];
		text.getTextWidths(str, result);

		int lastpos = 0;
		int dotpos = 0;

		float sum = 0.0f;

		// 1行に入る文字長をしらべる
		for (int i = 0; i < result.length && (dotpos == 0 || lastpos == 0); i++) {
			if (sum + result[i] > cx - dotwidth && dotpos == 0) {
				dotpos = i;
			}
			if (sum + result[i] > cx && lastpos == 0) {
				lastpos = i;
			}
			sum += result[i];
		}

		String resultStr;
		if (lastpos == 0) {
			resultStr = str;
		}
		else {
			resultStr = str.substring(0, dotpos) + "...";
		}
		return resultStr;
	}

	public void setPage(int page, boolean isBack, boolean isFirst) {
		try {
			if (mTextData == null) {
				return;
			}
			if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
				int total = mTextData.length;

				mMoveToLeft = (total - page - 1) * (mDrawWidth + mCenter) * -1;
				if (mDrawWidth < mDispWidth) {
					mMoveToLeft += (mDispWidth - mDrawWidth) / 2;
				}
				else if (mCurrentPage <= page) {
					// 指定ページの右端が画面の右端に来るように座標を設定
					mMoveToLeft -= (mDrawWidth - mDispWidth);
				}
				else {
					// 指定ページの左端が画面の左端に来るように座標を設定
					;
				}
				if (Math.abs(mMoveToLeft - mDrawLeft) <= mDrawWidth * 1.5) {
					startPageMove();
					return;
				}
				else {
					// スクロール停止
					stopPageMove();
				}
			}

			Canvas canvas;
			if (mEffect && mBackBitmap != null) {
				// 旧ページのビットマップ作成
				canvas = new Canvas(mBackBitmap);
				drawScreen(canvas);
			}

			// 新しいページの設定
//			isBack = mCurrentPage > page;
			//mCurrentPage = page;
			setCurrentPage(page);
			updateOverSize(isBack);

			if (isFirst == false) {
				// 初回はエフェクトしない
				if (mEffect && mBackBitmap != null) {
					mEffectDraw = true;
					startEffect();
				}
			}
		}
		finally {
			// ページ設定されたあとは描画可能
			mInitialize = false;
		}
	}

	public int getPage() {
		return mCurrentPage;
	}

	private void setCurrentPage(int page) {
		if (mCurrentPage != page) {
			mCurrentPage = page;

			// 設定スレッド開始
			runDrawBitmapThread();
		}
	}

	public void setTextConfig(int width, int height, int info, int margin_h) {
		mTextWidth = width;
		mTextHeight = height;
		mTextInfo = info;
		// mTextMarginW = margin_w;
		mTextMarginH = margin_h;
	}

	/**
	 * 画面サイズ変更時の通知
	 *
	 * @param w
	 *            , h, oldw, oldh
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		for (int retry = 0 ; retry < 3 ; retry ++) {
			try {
				mBackBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
				break;
			}
			catch (OutOfMemoryError e) {
				Log.i("TextView", "onSizeChanged - OutOfMemoryError");
				System.gc();
			}
		}
//		mNewBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);

		if (mPseLand == false) {
			mDispWidth = w;
			mDispHeight = h;

		}
		else {
			mDispWidth = h;
			mDispHeight = w;
		}

		// リサイズ処理
		TextScaling();
		updateOverSize();
	}

	// 画面サイズの更新
	public void updateScreenSize() {
		int w = getWidth();
		int h = getHeight();
		if (w == 0 || h == 0) {
			return;
		}

		if (mPseLand == false) {
			mDispWidth = w;
			mDispHeight = h;

		}
		else {
			mDispWidth = h;
			mDispHeight = w;
		}
	}

	public void updateOverSize(boolean isPageBack) {
		mIsPageBack = isPageBack;
		updateOverSize();
	}

	public void updateOverSize() {
		if (mTextData == null) {
			return;
		}

		int view_x;
		int view_y;

		int disp_x = mDispWidth;
		int disp_y = mDispHeight;

		if (mDrawWidth == 0 || mDrawHeight == 0) {
			// 未初期化
			return;
		}

		// 表示幅/高さ
		if (mDispMode == DEF.DISPMODE_TX_HALF || (mDispMode == DEF.DISPMODE_TX_DUAL && (mCurrentPage < 0 || mCurrentPage >= mTextData.length - 1))) {
			// 端ページ
			mDrawWidthSum = mDrawWidth;
		}
		else if (mDispMode == DEF.DISPMODE_TX_DUAL) {
			mDrawWidthSum = mDrawWidth * 2 + mCenter;
		}
		else {
			mDrawWidthSum = mDrawWidth * mTextData.length + mCenter * (mTextData.length - 1);
		}

		// 余白を求める
		if (mDispMode == DEF.DISPMODE_TX_HALF || mDispMode == DEF.DISPMODE_TX_DUAL) {
			// 1～2ページ幅
			view_x = mDrawWidthSum;
		}
		else {
			// 1ページ幅
			view_x = mDrawWidth;
		}
		view_y = mDrawHeight;

		mMgnLeft = 0;
		mMgnRight = 0;
		if (disp_x < view_x) {
			if (mDispMode != DEF.DISPMODE_TX_SERIAL) {
				// 連続モード以外
				mMgnLeft = mMargin;
				mMgnRight = mMargin;
			}
		}
		else if (disp_x > view_x) {
			mMgnLeft = (disp_x - view_x) / 2;
			mMgnRight = (disp_x - view_x) - mMgnLeft;
		}

		mMgnTop = 0;
		mMgnBottom = 0;
		if (disp_y < view_y) {
			mMgnTop = mMargin;
			mMgnBottom = mMargin;
		}
		else if (disp_y > view_y) {
			mMgnTop = (disp_y - view_y) / 2;
			mMgnBottom = (disp_y - view_y) - mMgnTop;
		}

		// イメージ表示位置の初期化
		mDrawLeft = 0;
		mDrawTop = 0;
		if (mDispMode == DEF.DISPMODE_TX_HALF || mDispMode == DEF.DISPMODE_TX_DUAL) {
			if (mViewPoint == DEF.VIEWPT_CENTER) {
				mDrawLeft = (disp_x - view_x) / 2;
				mDrawTop = (disp_y - view_y) / 2;
			}
			else {
				Boolean isLeft = false;
				if (mViewPoint == DEF.VIEWPT_LEFTTOP || mViewPoint == DEF.VIEWPT_LEFTBTM) {
					isLeft = true;
				}
				// ページ戻りの場合は左右の基準点を反対にする
				if (mPrevRev && mIsPageBack) {
					isLeft = !isLeft;
				}

				if (isLeft) {
					mDrawLeft = 0 + mMgnLeft;
				}
				else {
					mDrawLeft = disp_x - view_x - mMgnRight;
				}
			}
		}
		else {
			// 1ページ目の左上座標
			int page1_x = mDrawWidthSum - mDrawWidth * (mCurrentPage + 1) - (mCenter * mCurrentPage);

			if (mViewPoint == DEF.VIEWPT_CENTER) {
				mDrawLeft = (disp_x - view_x) / 2;
				// 右基準
				mDrawLeft = (mDispWidth - mDrawWidth) / 2 - page1_x;
			}
			else {
				Boolean isLeft = false;
				if (mViewPoint == DEF.VIEWPT_LEFTTOP || mViewPoint == DEF.VIEWPT_LEFTBTM) {
					isLeft = true;
				}
				// ページ戻りの場合は左右の基準点を反対にする
				if (mPrevRev && mIsPageBack) {
					isLeft = !isLeft;
				}

				if (isLeft) {
					// 左基準
					mDrawLeft = mMgnLeft - page1_x;
				}
				else {
					// 右基準
					mDrawLeft = mDispWidth - mDrawWidth - mMgnRight - page1_x;
				}

			}
		}

		// Y座標の表示位置
		if (mViewPoint == DEF.VIEWPT_CENTER) {
			mDrawTop = (disp_y - view_y) / 2;
		}
		else if (mViewPoint == DEF.VIEWPT_LEFTTOP || mViewPoint == DEF.VIEWPT_RIGHTTOP) {
			// 上基準
			mDrawTop = 0 + mMgnTop;
		}
		else {
			// 下基準
			mDrawTop = disp_y - view_y - mMgnBottom;
		}

		if (mEffect != true) {
			// エフェクトありのときは更新しない
			update(false);
		}

		// ページ送り位置の設定
		setScrollPos();
	}

	public int checkFlick() {
		int overX = mOverScrollX;
		mOverScrollX = 0;
		if (Math.abs(overX) * 90 / mOverScrollMax >= 90) {
			// 80% 以上引っ張っているとき
			// mLastAttenuate = null;
			return overX;
		}
		return 0;
	}

	public void scrollStart(float x, float y, int flickWidth, int scroll) {
		mScrollBaseX = x;
		mScrollBaseY = y;
		mOverScrollX = 0;
		mOverScrollMax = flickWidth * scroll; 
	}

	/**
	 * 慣性スクロールの開始
	 * @param x      開始X位置
	 * @param y      開始Y位置
	 * @param scroll スクロール倍率
	 * @param sx     x方向速度
	 * @param sy     y方向速度
	 * @param time   開始時間
	 */
	public void momentiumStart(float x, float y, int scroll, float sx, float sy, int term, int mode) {
		if (Math.abs(sx) < 2 && Math.abs(sy) < 2) {
			return;
		}
		mScrollBaseX = x;
		mScrollBaseY = y;
		mMomentiumX = sx * MOMENTIUM_TERM / term;
		mMomentiumY = sy * MOMENTIUM_TERM / term;
		mMomentiumTime = SystemClock.uptimeMillis();
		mMomentiumNum = 0;
		if (mode == 0) {
			// 減速しない
			mMomentDrain = 0; 
		}
		else {
			// 減速なし
//			mMomentDrain = DEF.MAX_MOMENTMODE - mode;
			mMomentDrain = mode;
		}

		mMomentiumMsg = mHandler.obtainMessage(EVENT_MOMENTIUM, scroll, term);
		mHandler.sendMessageAtTime(mMomentiumMsg, mMomentiumTime + MOMENTIUM_TERM);
	}

	/**
	 * スクロールの停止
	 */
	public boolean scrollStop() {
		if (mMomentiumMsg != null) {
			mMomentiumMsg = null;
			return true;
		}
		return false;
	}

	/**
	 * 画像をスクロールさせます。
	 * 
	 * @param	x	X軸の移動量
	 * @param	y	Y軸の移動量
	 * @param	scroll	スクロール倍率
	 * @param	flag	スクロール倍率
	 */
	public void scrollMoveAmount(float x, float y, int scroll, boolean flag) {
		if (x == 0 && y == 0) {
			return;
		}

		scrollMove(x + mScrollBaseX, y + mScrollBaseY, scroll, flag);
	}

	/**
	 * 画像をスクロールさせます。
	 * 
	 * @param	x	移動先の基準となる画面内の X 軸の座標
	 * @param	y	移動先の基準となる画面内の Y 軸の座標
	 * @param	scroll	スクロール倍率
	 * @param	flag	
	 * @return	端だったのでスクロールしなかった量
	 */
	public void scrollMove(float x, float y, int scroll, boolean flag) {
		// 再描画フラグ
		boolean fUpdate = false;

		if (x == 0 && y == 0) {
			return;
		}

		float orgLeft = (int)mDrawLeft;
		float moveX = ((x - mScrollBaseX) * scroll) + mOverScrollX;
		float moveY = ((y - mScrollBaseY) * scroll);
//		float moveX = (x * scroll) + mOverScrollX;
//		float moveY = y * scroll;

		float left = mDrawLeft + moveX;
		float top  = mDrawTop  + moveY;  

		if (left < mDispWidth - mDrawWidthSum - mMgnRight) {
			left = mDispWidth - mDrawWidthSum - mMgnRight;
		}
		if (left > mMgnLeft) {
			left = mMgnLeft;
		}
		if (top < mDispHeight - mDrawHeight - mMgnBottom) {
			top = mDispHeight - mDrawHeight - mMgnBottom;
		}
		if (top > mMgnTop) {
			top = mMgnTop;
		}

		if (left != mDrawLeft || top != mDrawTop) {
			mDrawLeft = left;
			mDrawTop = top;
			fUpdate = true;
		}

		mOverScrollX = (int)(moveX - (mDrawLeft - orgLeft));

/*
		if (Math.abs(mOverScrollX) > mOverScrollMax) {
			mOverScrollX = mOverScrollMax * (mOverScrollX > 0 ? 1 : -1);
		}
//		Log.d("overscroll", "overScroll=" + mOverScrollX + ", moveX=" + moveX + ", move=" + (int)(mDrawLeft - orgLeft));
		if (mOverScrollX != 0) {
			// 減衰開始
			attenuate();
			// 描画
			fUpdate = true;
		}
*/

		//mCurrentPage = getCurrentPage(PAGEBASE_CENTER);
		setCurrentPage(getCurrentPage(PAGEBASE_CENTER));

		if (fUpdate) {
			updateNotify();
		}

//		mScrollBaseX = x;
//		mScrollBaseY = y;

		// 設定スレッド開始
//		runDrawBitmapThread();
		return;
	}

	// スクロールの中間位置を計算
	public void setScrollPos() {
		int x_cnt;
		int y_cnt;
		int x_gap = mDispWidth / 64;
		int y_gap = mDispHeight / 64;
		int gap = x_gap <= y_gap ? x_gap : y_gap;
		int x_pos[][];
		int y_pos[];

		// スクロール量は最大でも画面の2/3
		int max_scroll_x = mDispWidth * mScrlRangeW / 100;
		int max_scroll_y = mDispHeight * mScrlRangeH / 100;

		int drawWidth;
		if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
			drawWidth = mDrawWidth;
		}
		else {
			drawWidth = mDrawWidthSum;
		}

		if (drawWidth <= mDispWidth) {
			// 画面幅より画像小さい
			x_cnt = 1;
			x_pos = new int[1][];
			x_pos[0] = new int[1];
			x_pos[0][0] = (mDispWidth - drawWidth) / 2;
		}
		else if (drawWidth > mDrawHeight) {
			// 見開き表示(横長)のとき
			if ((drawWidth + 1) / 2 + mMgnRight > mDispWidth) {
				// 画面幅よりも余分を含めた1ページの幅が大きいときは1ページ内でスクロール
				int x_wk = (drawWidth + 1) / 2 + gap + mMgnRight - mDispWidth;
				// 最大でも2/3ページ単位でスクロール
				x_cnt = (x_wk + max_scroll_x - 1) / max_scroll_x;
				x_pos = new int[2][];
				x_pos[0] = new int[x_cnt + 1];
				x_pos[1] = new int[x_cnt + 1];

				int x_st;
				// 見開き中央をちょっと右にずらす
				x_st = drawWidth / 2 * -1 + gap;
				for (int i = 0; i <= x_cnt; i++) {
					int idx = x_cnt - i;
					x_pos[0][i] = x_st - x_wk * idx / x_cnt;
				}

				// 見開き左端
				x_st = mMgnLeft;
				for (int i = 0; i <= x_cnt; i++) {
					int idx = x_cnt - i;
					x_pos[1][i] = x_st - x_wk * idx / x_cnt;
				}
			}
			else {
				// 見開き幅の半分＋余分＜画面幅のときは2
				x_cnt = 1;
				x_pos = new int[2][];
				// 右端まで表示する座標
				x_pos[0] = new int[1];
				x_pos[0][0] = (drawWidth + mMgnRight) * -1 + mDispWidth;
				// 左端を表示する座標
				x_pos[1] = new int[1];
				x_pos[1][0] = mMgnLeft;
			}
		}
		else {
			// 画像幅全体を均等スクロール
			// 画面からのはみ出し分を算出
			int x_wk = drawWidth + mMgnLeft + mMgnRight - mDispWidth;
			// 最大でも2/3ページ単位でスクロール
			x_cnt = (x_wk + max_scroll_x - 1) / max_scroll_x;
			x_pos = new int[1][];
			x_pos[0] = new int[x_cnt + 1];

			int x_st = mMgnLeft;
			for (int i = 0; i <= x_cnt; i++) {
				int idx = x_cnt - i;
				x_pos[0][i] = x_st - x_wk * idx / x_cnt;
			}
		}

		if (mDrawHeight <= mDispHeight) {
			// 画面高さより画像が小さい
			y_cnt = 1;
			y_pos = new int[y_cnt];
			y_pos[0] = (mDispHeight - mDrawHeight) / 2;
		}
		else {
			// 画像幅全体を均等スクロール
			int y_wk = mDrawHeight + mMgnTop + mMgnBottom - mDispHeight;
			// 最大でも半ページ単位でスクロール
			y_cnt = (y_wk + max_scroll_y - 1) / max_scroll_y;
			y_pos = new int[y_cnt + 1];

			int y_st = mMgnTop;
			for (int i = 0; i <= y_cnt; i++) {
				y_pos[i] = y_st - y_wk * i / y_cnt;
			}
		}

		// 配列に記録
		int p_cnt = x_pos.length;
		x_cnt = x_pos[0].length;
		y_cnt = y_pos.length;
		mScrollPos = new Point[x_cnt * y_cnt * p_cnt];
		// 左右ページごとの設定
		for (int p = 0; p < p_cnt; p++) {
			// 縦→横の順番で位置設定
			for (int x = 0; x < x_cnt; x++) {
				// ページ内横位置の設定
				for (int y = 0; y < y_cnt; y++) {
					// ページ内縦位置の設定
					mScrollPos[p * (x_cnt * y_cnt) + (x * y_cnt) + y] = new Point(x_pos[p][x], y_pos[y]);
				}
			}
		}
		return;
	}

	// 次の位置へスクロールする
	public boolean setViewPosScroll(int move) {
		//オーバースクロールとめくり方向が同じなら次のページ
		if (mOverScrollX != 0) {
			return false;
		}

		if (mScrollPos == null) {
			return true;
		}

		if (mScrollPoint != null) {
			// スクロール中はキャンセル
			mDrawLeft = mScrollPoint.x;
			mDrawTop = mScrollPoint.y;
			mScrollPoint = null;
		}

		// 現在位置に一番近いもの
		int min_x;
		int min_y;
		int index = 0;

		min_x = -1;
		min_y = -1;

		if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
			int page = getCurrentPage(move >= 0 ? PAGEBASE_LEFT : PAGEBASE_RIGHT);
			int base = (mTextData.length - page - 1) * (mDrawWidth + mCenter);

			for (int i = 0; i < mScrollPos.length; i++) {
				// 一番近いポイントを探す
				int wk_x = mScrollPos[i].x - base - (int)(mDrawLeft + mOverScrollX);
				int wk_y = mScrollPos[i].y - (int)mDrawTop;
				if (wk_x >= 0 && wk_y >= 0) {
					if (min_x == -1 || min_x >= wk_x && min_y >= wk_y) {
						// 最初のループ又はさらに近い
						min_x = wk_x;
						min_y = wk_y;
						index = i;
					}
				}
			}
			if (mScrollPos[index].x - base == (int)(mDrawLeft + mOverScrollX) && mScrollPos[index].y == (int)mDrawTop) {
				index += move >= 0 ? 1 : -1;
				if (index < 0 || index >= mScrollPos.length) {
					// 移動がない
					if (index >= mScrollPos.length) {
						page++;
						index = 0;
					}
					else {
						page--;
						index = mScrollPos.length - 1;
					}
					if (page < 0) {
						return false;
					}
					else if (page >= mTextData.length) {
						return false;
					}
					base = (mTextData.length - page - 1) * (mDrawWidth + mCenter);
				}
			}
			mScrollPoint = new Point(mScrollPos[index].x - base, mScrollPos[index].y);
		}
		else {
			for (int i = 0; i < mScrollPos.length; i++) {
				int wk_x = mScrollPos[i].x - (int) (mDrawLeft + mOverScrollX);
				int wk_y = mScrollPos[i].y - (int) mDrawTop;
				if (wk_x >= 0 && wk_y >= 0) {
					if (min_x == -1 || min_x >= wk_x && min_y >= wk_y) {
						// 最初のループ又はさらに近い
						min_x = wk_x;
						min_y = wk_y;
						index = i;
					}
				}
			}
			if (mScrollPos[index].x == (int)(mDrawLeft + mOverScrollX) && mScrollPos[index].y == (int)mDrawTop) {
				// 丁度その位置なら次へ
				index += move >= 0 ? 1 : -1;
				if (index < 0 || index >= mScrollPos.length) {
					// 端っこの場合はページ遷移
					return false;
				}
			}
			mScrollPoint = new Point(mScrollPos[index].x, mScrollPos[index].y);
		}
//		moveToNextPoint();
		return true;
	}

	public boolean checkScrollPoint() {
		return mScrollPoint != null;
	}

	// 小単位でスクロールしながら目的のポイントへ
	public boolean moveToNextPoint(int range) {
		if (mEffectDraw == true) {
			return true;
		}

		int x_range, x_cnt, x_move;
		int y_range, y_cnt, y_move;
		int move_cnt;

		boolean result = true;
		if (mScrollPoint == null) {
			// 設定なし
			return false;
		}

		x_range = mScrollPoint.x - (int) (mDrawLeft + mOverScrollX);
		y_range = mScrollPoint.y - (int) mDrawTop;

		x_cnt = (Math.abs(x_range) + range - 1) / range;
		y_cnt = (Math.abs(y_range) + range - 1) / range;

		move_cnt = x_cnt > y_cnt ? x_cnt : y_cnt;

		if (move_cnt > 0) {
			x_move = x_range / move_cnt;
			y_move = y_range / move_cnt;

			if (mOverScrollX == 0) {
				mDrawLeft += x_move;
			}
			// オーバースクロールしている場合は、先にそちらを消費する
			else if (mOverScrollX > 0) {
				mOverScrollX += x_move;
				if (mOverScrollX < 0) {
					mDrawLeft += mOverScrollX;
					mOverScrollX = 0;
				}
			}
			else if (mOverScrollX < 0) {
				mOverScrollX += x_move;
				if (mOverScrollX > 0) {
					mDrawLeft += mOverScrollX;
					mOverScrollX = 0;
				}
			}

			mDrawTop += y_move;
			updateNotify();
		}
		if (move_cnt <= 1) {
			mScrollPoint = null;
			result = false;
		}
		// 現在ページを更新
		if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
			// mCurrentPage = getCurrentPage(PAGEBASE_CENTER);
			setCurrentPage(getCurrentPage(PAGEBASE_CENTER));

			// 設定スレッド開始
			runDrawBitmapThread();
		}
		return result;
	}

	// 小単位でスクロールしながら目的のポイントへ
	public boolean moveToNextPage() {
		if (mEffectDraw == true) {
			return true;
		}

//		int x_range, y_range, x_cnt, y_cnt;
		int x_move, y_move;
//		int move_cnt;

		boolean result = true;

//		if (mMoveToLeft + mDispWidth < mDrawLeft) {
//			mDrawLeft = mMoveToLeft + mDispWidth;
//		}
//		else if (mMoveToLeft - mDispWidth > mDrawLeft) {
//			mDrawLeft = mMoveToLeft - mDispWidth;
//		}

		int t = (int)(SystemClock.uptimeMillis() - mMoveStart);
		int w = (mMoveToLeft - mMoveFromLeft);
		int movetime = mEffectTime * w / mDrawWidth * (w < 0 ? -1 : 1);
		if (movetime == 0) {
			return false;
		}
		else if (t <= 0 || t >= movetime) {
			t = movetime;
			// 移動終了
			result = false;
		}

//		x_range = mMoveToLeft - (int)mDrawLeft;
//		y_range = mMgnTop - (int)mDrawTop;

//		x_cnt = (Math.abs(x_range) + mVolScrl - 1) / mVolScrl;
//		y_cnt = (Math.abs(y_range) + mVolScrl - 1) / mVolScrl;

//		move_cnt = x_cnt > y_cnt ? x_cnt : y_cnt;

//		if (move_cnt > 0) {
//			x_move = x_range / move_cnt;
//			y_move = y_range / move_cnt;

		x_move = (mMoveToLeft - mMoveFromLeft) * t / movetime;
		y_move = (mMgnTop - mMoveFromTop) * t / movetime;
		mDrawLeft = mMoveFromLeft + x_move;
		mDrawTop = mMoveFromTop + y_move;
		update(false);
//		}
//		if (move_cnt <= 1) {
//			result = false;
//		}
		// 現在ページを更新
		//mCurrentPage = getCurrentPage(PAGEBASE_CENTER);
		setCurrentPage(getCurrentPage(PAGEBASE_CENTER));

		// 設定スレッド開始
		runDrawBitmapThread();
		return result;
	}

	public boolean isScrolling() {
		return mScrollPoint != null ? true : false;
	}

	public float getDrawLeft() {
		return mDrawLeft;
	}

	public int TextScaling() {
		if (mTextData == null) {
			return 100;
		}

		int src_x[] = { 0, 0 };
		int src_y[] = { 0, 0 };
		int view_x; // 1～2画像のまとめたサイズ
		int view_y;
		boolean fWidth;

		// 画面サイズ
		int disp_x = mDispWidth;
		int disp_y = mDispHeight;

		// 用紙サイズ
		src_x[0] = mTextWidth;
		src_y[0] = mTextHeight;

		if (mDispMode == DEF.DISPMODE_TX_DUAL && (mCurrentPage >= 0 && mCurrentPage < mTextData.length - 1)) {
			// dual viewの時は中央マージン分幅が使えない
			if (mIsMargin && mCenter > 0) {
				disp_x -= mCenter;
			}

			// 用紙サイズ
			src_x[1] = mTextWidth;
			src_y[1] = mTextHeight;
		}

		// 1～2映像を足したサイズ
		int src_cx = src_x[0] + src_x[1];
		int src_cy = src_y[0];

		// サイズ0だと0除算なので終了
		if (src_cx == 0 || src_cy == 0) {
			return 100;
		}

		if (mSclMode == DEF.SCALE_PINCH) {
			// 任意のスケール
			view_x = src_cx * mPinchScl / 100;
			view_y = src_cy * mPinchScl / 100;
		}
		else if (mSclMode == DEF.SCALE_ORIGINAL) {
			// 元サイズのまま
			view_x = src_cx;
			view_y = src_cy;
		}
		else if (mSclMode == DEF.SCALE_FIT_ALLMAX) {
			// 縦横比無視で拡大
			view_x = disp_x;
			view_y = disp_y;
		}
		else {
			if (mSclMode == DEF.SCALE_FIT_ALL) {
				if (disp_x * 1000 / src_cx < disp_y * 1000 / src_cy) {
					// Y方向よりもX方向の方が拡大率が小さく画面いっぱいになる
					fWidth = true;
				}
				else {
					// その逆
					fWidth = false;
				}
			}
			else if (mSclMode == DEF.SCALE_FIT_WIDTH) {
				fWidth = true;
			}
			else {
				fWidth = false;
			}

			if (fWidth) {
				// 幅基準
				view_x = disp_x;
				view_y = src_cy * disp_x / src_cx;
			}
			else {
				// 高さ基準
				view_x = src_cx * disp_y / src_cy;
				view_y = disp_y;
			}
		}

		// サイズ算出 & リサイズ
		mDrawWidth = view_x * src_x[0] / (src_x[0] + src_x[1]);
		mDrawHeight = view_y * src_y[0] / src_cy;

		// 縮尺後のサイズ
		mDrawScale = (float) mDrawHeight / (float) mTextHeight;
		if (mDrawScale <= 1.10) {
			for (int retry = 0 ; retry < 3 ; retry ++) {
				try {
					mDrawBitmap = Bitmap.createBitmap(mDrawWidth, mDrawHeight, Config.RGB_565);
					break;
				}
				catch (OutOfMemoryError e) {
					Log.i("TextView", "onSizeChanged - OutOfMemoryError");
					System.gc();
				}
			}
			mSrcRect.left = 0;
			mSrcRect.top = 0;
			mSrcRect.right = mDrawWidth;
			mSrcRect.bottom = mDrawHeight;
//			mDrawable = new BitmapDrawable(mDrawBitmap);
//		    mDrawable.setBounds(mSrcRect);
		}
		else {
			mDrawBitmap = null;
		}

		// 描画用タイトル文字列
		float infosize = mTextInfo * mDrawScale;
		mTextPaint.setTextSize(infosize);
		mDrawTitle = lengthSubstring(mTitle, mDrawWidth, mTextPaint);

		// 再描画
//		update();
		return (int) (mDrawScale * 100);
	}

	public boolean nextPage() {
		int page = getCurrentPage(PAGEBASE_RIGHT);
		int totalpage = mTextData.length;
		if (page >= totalpage - 1 || (mDispMode == DEF.DISPMODE_TX_DUAL && page >= totalpage - 2)) {
			// 最終ページ
			if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
				// 端までスクロール開始
				mMoveToLeft = mMgnLeft;
				if (mMoveToLeft > mDrawLeft) {
					// まだ端ではないとき
					startPageMove();
					return true;
				}
			}
			return false;
		}

		if (mDispMode == DEF.DISPMODE_TX_DUAL) {
			// 並べて表示中は2ページ進む
			page += 2;
		}
		else {
			// 1ページ進む
			page++;
		}
		setPage(page, false, false);
		return true;
	}

	public boolean prevPage() {
		int page = getCurrentPage(PAGEBASE_LEFT);

		// 前ページへ
		if (page <= 0) {
			// 先頭ページかつ分割表示中かつ2ページ目でないなら前ページはない
			// 先頭ページかつ分割表示でないなら前ページはない
			if (mDispMode == DEF.DISPMODE_TX_SERIAL) {
				// 端までスクロール開始
				mMoveToLeft = mDrawWidthSum * -1 - mMgnRight + mDispWidth;
				if (mMoveToLeft < mDrawLeft) {
					// まだ端ではないとき
					startPageMove();
					return true;
				}
			}
			return false;
		}
		if (mDispMode == DEF.DISPMODE_TX_DUAL) {
			// 並べて表示中は2ページ戻る
			page -= 2;
		}
		else {
			// 1ページ戻る
			page--;
		}
		setPage(page, true, false);
		return true;
	}

	private int getCurrentPage(int base) {
		if (mTextData == null) {
			return 0;
		}

		if (mDispMode != DEF.DISPMODE_TX_SERIAL) {
			return mCurrentPage;
		}

		// 連続モードでは中央表示がカレントページ
		int pos;
		int page = mCurrentPage;
		if (base == PAGEBASE_LEFT) {
			// 左端基準
			pos = (int) (mDrawWidthSum + mDrawLeft);
			if (mDispWidth > mDrawWidth) {
				pos -= (mDispWidth - mDrawWidth) / 2;
			}
		}
		else if (base == PAGEBASE_RIGHT) {
			// 右端基準
			pos = (int) (mDrawWidthSum + mDrawLeft);
			if (mDispWidth <= mDrawWidth) {
				pos -= mDispWidth;
			}
			else {
				pos -= mDrawWidth + (mDispWidth - mDrawWidth) / 2;
			}
		}
		else if (base == PAGEBASE_CENTER) {
			// 中央
			pos = (int) (mDrawWidthSum - mDispWidth / 2 + mDrawLeft);
		}
		else {
			return -1;
		}

		page = pos / (mDrawWidth + mCenter);
		if (page < 0) {
			page = 0;
		}
		else if (page >= mTextData.length) {
			page = mTextData.length - 1;
		}
		return page;
	}

	public boolean getScrolling() {
		return mScrolling;
	}

	private final int EVENT_EFFECT = 201;
	private final int EVENT_SCROLL = 202;
	private final int EVENT_PAGE = 203;
	private final int EVENT_ATTENUATE = 204;
	private final int EVENT_MOMENTIUM = 205;

	private final int EFFECT_TERM = 1;
	private final int SCROLL_TERM = 4;
	private final int PAGE_TERM = 1;
	private final int ATTENUATE_TERM = 10;
	private final int MOMENTIUM_TERM = 10;
	
	private int mAttenuate = 0;

	// フリックを減衰させる
	public void attenuate() {
		long NextTime = SystemClock.uptimeMillis() + ATTENUATE_TERM * 5;

		Message msg = mHandler.obtainMessage(EVENT_ATTENUATE); 
		msg.arg1 = ++ mAttenuate;
		mHandler.sendMessageAtTime(msg, NextTime);
	}

	// エフェクト開始
	public void startEffect() {
		if (mEffectTime > 0) {
			// エフェクト開始
			long NextTime = SystemClock.uptimeMillis() + 1;
			mEffectStart = NextTime;

			Message msg = mHandler.obtainMessage(EVENT_EFFECT);
			msg.what = EVENT_EFFECT;

//			mEffectRate = 0.99f;
//			setEffectRate(mEffectRate);
			NextTime += EFFECT_TERM;
			mHandler.sendMessageAtTime(msg, NextTime);
		}
		else {
			setEffectRate(0.0f);
		}
		return;
	}

	// タイマー開始
	public void startScroll() {
		Message msg = mHandler.obtainMessage(EVENT_SCROLL);
		msg.what = EVENT_SCROLL;
		long NextTime = SystemClock.uptimeMillis();

		// エフェクト開始
		if (moveToNextPoint(mVolScrl) == false) {
			return;
		}
		NextTime += SCROLL_TERM;
		mScrolling = true;

		mHandler.sendMessageAtTime(msg, NextTime);
		return;
	}

	// エフェクト開始
	public void startPageMove() {
		mEventPageMsg = mHandler.obtainMessage(EVENT_PAGE);
		mEventPageMsg.what = EVENT_PAGE;
		long NextTime = SystemClock.uptimeMillis();
		mMoveStart = NextTime;
		mMoveFromLeft = (int)mDrawLeft;
		mMoveFromTop = (int)mDrawTop;

//		// ページ移動開始
//		if (moveToNextPage() == false) {
//			return;
//		}
		NextTime += PAGE_TERM;

		mHandler.sendMessageAtTime(mEventPageMsg, NextTime);
		return;
	}

	// スクロール停止
	private void stopPageMove() {
		mEventPageMsg = null;
	}

	// 終了時に呼ばれてスレッドを止める
	public void close() {
		mMessageBreak = true;

		if (mDrawThread != null) {
			mDrawThread.breakThread();
			mDrawThread = null;
		}
		synchronized (mLock) {
			mDrawBitmap = null;

			CallTxtLibrary.FreeTextImage();
//			Log.d("close", "free");
		}
	}

	public void breakMessage(boolean flag) {
		mMessageBreak = flag;
	}
	
	// サムネイル読み込み
	private void runDrawBitmapThread() {
		if (mCurrentPage == mDrawPage || mDrawBitmap == null) {
			return;
		}

		if (mDrawThread != null) {
			if (mDrawThread.getPage() == mCurrentPage) {
				// 既に処理中なので何もしない
				return;
			}
			// 今動いてるのは止める
			mDrawThread.breakThread();
		}
		if (mTextData != null) {
			mDrawThread = new DrawThread(mCurrentPage, mTextData.length, mLock);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (mMessageBreak) {
			return false;
		}
		
		// 次のイベント時間
		long NextTime = SystemClock.uptimeMillis();
		boolean nextEvent = false;

		switch (msg.what) {
			case EVENT_ATTENUATE:
			{
				if (mAttenuate == msg.arg1) {
					// 最後に登録したメッセージ
					if (mOverScrollX > 0) {
						mOverScrollX -= mOverScrollMax / 8;
						if (mOverScrollX < 0) {
							mOverScrollX = 0;
						}
					}
					else if (mOverScrollX < 0) {
						mOverScrollX += mOverScrollMax / 8;
						if (mOverScrollX > 0) {
							mOverScrollX = 0;
						}
					}
					update(false);
					if (mOverScrollX != 0) {
						NextTime += ATTENUATE_TERM;
						Message nextmsg = mHandler.obtainMessage(msg.what);
						nextmsg.arg1 = ++ mAttenuate;
						mHandler.sendMessageAtTime(nextmsg, NextTime);
					}
//					Log.d("overscrollMsg", "overScroll=" + mOverScrollX);
					return true;
				}
				break;
			}
			case EVENT_MOMENTIUM:
			{
				// 慣性スクロール
				if (mMomentiumMsg == msg) {
//					Log.d("moment", "num=" + mMomentiumNum + ", X=" + mMomentiumX + ", Y=" + mMomentiumY);
					// 最後に登録したメッセージから変化なし
					mMomentiumNum ++;

					 int drain = 0;
//					if (mMomentDrain != 0) {
//					// 時間が経つほど減速率を上げるその2
//						 drain = mMomentiumNum / mMomentDrain;// / msg.arg1;
//					}
					if (mMomentDrain == 0) {
						;
					}
					else if (mMomentDrain <= 4) {
						 // 毎ターン4～1ずつ減らす
						 drain = 5 - mMomentDrain;
					}
					else if (mMomentiumNum % (mMomentDrain - 3) == 0) {
						 // 周期的に1減らす
						 drain = 1;//
					}

					// 横方向のスクロール位置
					if ((Math.abs(mMomentiumX) > drain)) {
						mMomentiumX = (Math.abs(mMomentiumX) - drain) * (mMomentiumX >= 0 ? 1 : -1);	
					}
					else {
						mMomentiumX = 0;
					}
					// 縦方向のスクロール位置
					if ((Math.abs(mMomentiumY) > drain)) {
						mMomentiumY = (Math.abs(mMomentiumY) - drain) * (mMomentiumY >= 0 ? 1 : -1);
					}
					else {
						mMomentiumY = 0;
					}

					// スクロール位置とスクロール結果の判定
					float sx = mMomentiumX;
					float sy = mMomentiumY;
					int ox = (int)mDrawLeft;
					int oy = (int)mDrawTop;
					scrollMove(mScrollBaseX + sx, mScrollBaseY + sy, msg.arg1, false);
					// 次のメッセージ
					if ((ox != (int)mDrawLeft || oy != (int)mDrawTop)
								&& (Math.abs(mMomentiumX) >= 2.0f || Math.abs(mMomentiumY) >= 2.0f)) {
						NextTime += MOMENTIUM_TERM;
						mMomentiumMsg = mHandler.obtainMessage(EVENT_MOMENTIUM, msg.arg1, msg.arg2);
						mHandler.sendMessageAtTime(mMomentiumMsg, NextTime);
					}
					else {
						mMomentiumMsg = null;
					}
					return true;
				}
				break;
			}
			case EVENT_EFFECT:
				// 稼働中のみ次のイベント登録
				int t = (int)(NextTime - mEffectStart);
				if (t >= mEffectTime) {
					// mEffectTimeミリ秒を超えている
					mEffectRate = 0.0f;

					// エフェクト終了
					mEffectDraw = false;
					update(false);
				}
				else {
					// mEffectTimeミリ秒未満
					mEffectRate = ((float)(mEffectTime - t)) / (float)mEffectTime;
					// エフェクト位置
					setEffectRate(mEffectRate);
				}

				if (mEffectRate != 0.0f) {
					// エフェクト中は次のイベントを登録
					nextEvent = true;
					NextTime += EFFECT_TERM;
				}
				break;
			case EVENT_SCROLL:
				// スクロールで移動
				if (moveToNextPoint(mVolScrl)) {
					// エフェクト中は次のイベントを登録
					nextEvent = true;
					NextTime += SCROLL_TERM;
				}
				else {
					mScrolling = false;
				}
				break;
			case EVENT_PAGE:
				if (mEventPageMsg == msg) {
    				// スクロールで移動
    				if (moveToNextPage()) {
    					// エフェクト中は次のイベントを登録
    					nextEvent = true;
    					NextTime += PAGE_TERM;
    				}
				}
				break;
		}
		if (nextEvent) {
			// 次のイベントあり
			msg = mHandler.obtainMessage(msg.what);
			if (msg.what == EVENT_PAGE) {
				mEventPageMsg = msg;
			}
			this.mHandler.sendMessageAtTime(msg, NextTime);
		}
		return false;
	}

	@Override
	public void run() {
		// リスト描画処理監視
		while (true) {
			// リストの描画が必要な時にtrue復帰
			try {
				Thread.sleep(1000);
				update(false);
			} catch (InterruptedException e) {
				// 描画発生による割り込み
				if (mIsRunning == false) {
					break;
				}
				else {
					update(false);
				}
			}
			;
		}
	}

	public class DrawThread implements Runnable {
		private int mPage;
		private int mMaxPage;
		private boolean mThreadBreak;

		// 排他用オブジェクト
		private Object mLock;

		public DrawThread(int page, int maxpage, Object lock) {
			super();
			mThreadBreak = false;
			mPage = page;
			mMaxPage = maxpage;
			mLock = lock;

			// スレッド起動
			Thread thread = new Thread(this);
			thread.start();
			return;
		}

		// スレッド停止
		public void breakThread() {
			mThreadBreak = true;
			return;
		}

		// 処理対象ページ
		public int getPage() {
			return mPage;
		}

		// スレッド開始
		public void run() {
			int p;

			if (mThreadBreak == true) {
				return;
			}

			// sleepThread(500);

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
						setDrawBitmap(p);
						if (mDrawBreak) {
							continue;
						}
//						Log.d("setDrawBitmap.run", "page=" + p);
					}
				}

				try {
					Thread.sleep(1);
				}
				catch (InterruptedException e) {
					// 何もしない
					;
				}
				i ++;
			}
		}

		// 中断可能なスリープ
		void sleepThread(int t) {
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
}
