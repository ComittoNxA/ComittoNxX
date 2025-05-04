package src.comitton.imageview;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.view.DrawNoticeListener;
import src.comitton.jni.CallImgLibrary;
import src.comitton.common.GuideView;
import src.comitton.common.GuideView.UpdateListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MyImageView extends SurfaceView implements SurfaceHolder.Callback, UpdateListener, DrawNoticeListener, Callback, Runnable {
	private static final String TAG = "MyImageView";
	// 描画領域の種別
	public final static short AREATYPE_THUMBNAIL = 0x01;

	public final int ZOOM_NONE   = 0;
	public final int ZOOM_LEFT   = 0x01;
	public final int ZOOM_RIGHT  = 0x02;
	public final int ZOOM_TOP    = 0x10;
	public final int ZOOM_BOTTOM = 0x20;

	public final int ZOOM_GAP = 0;
	public final int ZOOM_FRAME = 2;
	public final int ZOOM_TOUCH = 40;

	private final int HMSG_ATTENUATE = 4002;
	private final int HMSG_MOMENTIUM = 4003;

	private final int ATTENUATE_TERM = 10;
	private final int MOMENTIUM_TERM = 10;

	private int mMgnColor;
	private int mCenColor;
	private int mGuiColor;
	private int mMargin       = 0;
	private int mCenter       = 0;		// 中央のすき間幅
	private int mShadow       = 0;
	private int mViewPoint    = DEF.VIEWPT_RIGHTTOP;
	private int mZoomType     = 0;
	private int mRotate       = 0;
	private int mPageWay      = DEF.PAGEWAY_RIGHT;
	private int mScrlWay      = DEF.SCRLWAY_H;
	private int mScrlRangeW;
	private int mScrlRangeH;
	private int mEffect;
	private boolean mPrevRev  = false;
	private boolean mIsMargin = false;	// 中央のすき間あり
	private boolean mIsShadow = false;
	private boolean mPseLand  = false;
	private boolean mScrlNext  = false;
	private boolean mScrlNextStop = false;
	private boolean mViewNext  = false;
	private boolean mNextFilter = true;

	private int mDispWidth  = 0;
	private int mDispHeight = 0;

	private int mAttenuateCount = 0;
	private int mOverScrollX = 0;
	private int mLastOverScrollX = 0;
	private int mOverScrollMax = 0;
	private float mMomentiumX;
	private float mMomentiumY;
	private long mMomentiumTime;
	private int mMomentiumNum;
	private Message mMomentiumMsg;
	private int mMomentDrain;

	// 画像が画面からはみ出るサイズ(0～)
	private int mMgnLeft = 0;	// 左
	private int mMgnRight = 0;	// 右
	private int mMgnTop = 0; 	// 上
	private int mMgnBottom = 0;	// 下

	private ImageManager mImageManager;
	private final ImageData[] mImage = new ImageData[2];
	private Bitmap mBackBitmap;
	private Bitmap mCanvasBitmap;

	private Point[] mScrollPos;
	private Point mScrollPoint;
	private Point mScrollStartPos = new Point();
	private long mScrollStart;

	// ルーペ機能
	private int mZoomView = 300;

	private ImageActivity mParentAct = null;
	private GuideView mGuideView;
	private final Paint mDrawPaint;
	private final Paint mAlphaPaint;
	private final Rect mDrawRect;
	private final Rect mWorkRect;

	private final Activity mActivity;
	private final Handler mHandler;
	private int mCacheIndex;
	private final SurfaceHolder mHolder;
	private Thread mUpdateThread;
	private boolean mIsRunning;

	// イメージ更新処理
	private boolean mIsPageBack = false;

	private float mDrawLeft    = 0;
	private float mDrawTop     = 0;
	private final int[] mDrawWidth = {0, 0};
	private final int[] mDrawHeight = {0, 0};
	private int mDrawWidthSum  = 0;
	private int mDrawHeightMax = 0;
	private int mCurrentPage = 0;
	private boolean mPageLock = false;

	private int mThreadWaitLoop = 0;

	// ルーペ表示
	private int mZoomMode = ZOOM_NONE;
	private int mLoupeSize = 0;

	// クリック位置
	private int mTouchX = 0;
	private int mTouchY = 0;

	// ピンチインアウト
	private int mPinchSel;

	// エフェクト処理
	private float mEffectRate = 0;

	// 一時保存画像で描画すべき状態
	private boolean mIsBackDraw = false;

	public MyImageView(Activity activity) {
		super(activity);
		mActivity = activity;
		mDrawPaint = new Paint();
		mAlphaPaint = new Paint();
		mDrawRect = new Rect();
		mWorkRect = new Rect();
		mHandler = new Handler(this);

		requestFocus();
		mHolder = getHolder();
		mHolder.addCallback(this);
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
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Surface の属性が変更された際にコールされる
		updateNotify();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface が破棄された際にコールされる
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
	public void run() {
		// リスト描画処理監視
		while (true) {
			// リストの描画が必要な時にtrue復帰
			try {
				Thread.sleep(3600000);
//				update(false);
			} catch (InterruptedException e) {
				// 描画発生による割り込み
				if (!mIsRunning) {
					break;
				}
				else {
					if	(mThreadWaitLoop < 1000)	{
						mThreadWaitLoop++;
						update(false);
					}
					else
					{
						mThreadWaitLoop = 0;
					}
				}
			}
			;
		}
	}

	@Override
	public void onUpdate() {
		// ガイドの更新
		updateNotify();
	}

	private boolean mDrawLock;
	public void lockDraw() {
		mThreadWaitLoop = 0;
		mDrawLock = true;
	}

	public void unlockDraw() {
		mDrawLock = false;
	}

	public boolean update(boolean unlock) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (mDrawLock && !unlock) {
			return false;
		}
		if (unlock) {
			// 描画ロック解除
			mDrawLock = false;
		}

		Canvas canvas = null;
		SurfaceHolder surfaceHolder = getHolder();
		try {
			canvas = surfaceHolder.lockCanvas(); // ロックして、書き込み用のcanvasを受け取る
			if (canvas == null) {
				Logcat.d(logLevel, "canvasが受け取れませんでした.");
				return false; // canvasが受け取れてなかったら抜ける
			}

			draw(canvas, false);
		}
		catch (Exception e) {
				Logcat.e(logLevel, "エラーが発生しました.", e);
		}
        finally {
			if (canvas != null) {
				try {
					surfaceHolder.unlockCanvasAndPost(canvas); // 例外が出て、canvas受け取ってたらロックはずす
				}
				catch (IllegalStateException e) {
					// Surface has already been released.
				}
				catch (IllegalArgumentException e) {
					;
				}
			}
		}
		return true;
	}

	public void attenuate() {
		// フリックを減衰させる
		long NextTime = SystemClock.uptimeMillis() + ATTENUATE_TERM * 5;

		Message msg = mHandler.obtainMessage(HMSG_ATTENUATE);
		msg.arg1 = ++ mAttenuateCount;
		mHandler.sendMessageAtTime(msg, NextTime);
	}

	@SuppressLint("SuspiciousIndentation")
    public void draw(Canvas canvas, boolean isCreateBack) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		int drawLeft;
		int drawTop;
		int drawWidthSum;
		int drawWidth0;
		int drawWidth1;
		int drawHeightMax;
		int drawHeight0;
		int drawHeight1;
		int touchX;
		int touchY;
		boolean isBackDraw;
		float effectRate;
		int effect;
		int zoomMode;
		boolean pseLand;
		int pinchsel;

		if (canvas == null) {
			return;
		}

		if (mImage[0] == null) {
			return;
		}

		synchronized (this) {
			drawLeft = (int)mDrawLeft;
			drawTop = (int)mDrawTop;
			drawWidthSum = mDrawWidthSum;
			drawWidth0 = mDrawWidth[0];
			drawWidth1 = mDrawWidth[1];
			drawHeightMax = mDrawHeightMax;
			drawHeight0 = mDrawHeight[0];
			drawHeight1 = mDrawHeight[1];
			touchX = mTouchX;
			touchY = mTouchY;
			isBackDraw = mIsBackDraw;
			effectRate = mEffectRate;
			zoomMode = mZoomMode;
			pseLand = mPseLand;
			pinchsel = mPinchSel;
			effect = mEffect;

    		int cx;
    		int cy;

			if (!pseLand) {
    			cx = getWidth();
    			cy = getHeight();
    		}
    		else {
    			cx = getHeight();
    			cy = getWidth();
    		}

			if (mCanvasBitmap == null) {
				return;
			}

			if (mImage[0] == null) {
				return;
			}
			if (!isCreateBack && isBackDraw && mBackBitmap != null) {
				// 次ページ読み込み中の表示
				// ビットマップ作成ではない
				canvas.drawBitmap(mBackBitmap, 0, 0, mDrawPaint);
				if (effectRate == 0.0f) {
					if (pseLand) {
						int pos;
						if (cx < cy) {
							pos = cx / 2;
						}
						else {
							pos = cy / 2;
						}
						canvas.save();
						canvas.rotate(90.0f, pos, pos);
					}
					mGuideView.drawLoading(canvas, cx, cy);
					if (pseLand) {
						canvas.restore();
					}
					return;
				}
			}

			Paint paint = mDrawPaint;

			if (effectRate != 0.0f) {
				// オーバースクロールが画面幅を超えるときには、1画面先のページを表示する
				if (mOverScrollX != 0) {
/*
					if (mOverScrollX > mDispWidth) {
						mDrawLeft += mDispWidth;
						drawLeft = (int)mDrawLeft;
						mOverScrollX -= mDispWidth;
					}
					else if (mOverScrollX < - mDispWidth) {
						mDrawLeft -= mDispWidth;
						drawLeft = (int)mDrawLeft;
						mOverScrollX += mDispWidth;
					}
*/
					mLastOverScrollX = mOverScrollX;
					mOverScrollX = 0;
				}
				float tx = 0;
				float ty = 0;
				if (!pseLand) {
					if(effectRate > 0) {
						tx = Math.min(cx * effectRate * -1 + mLastOverScrollX, 0);
						//Logcat.d(logLevel, "tx=" + tx + ", cx=" + cx + ", effectRate=" + effectRate + ", mLastOverScrollX=" + mLastOverScrollX);
					}
					else {
						//Logcat.d(logLevel, "tx=" + tx + ",cx=" + cx + ", effectRate=" + effectRate + ", mLastOverScrollX=" + mLastOverScrollX);
						tx = Math.max(cx * effectRate * -1 + mLastOverScrollX, 0);
					}
				} else {
					if(effectRate > 0) {
						//Logcat.d(logLevel, "ty=" + ty + ", cy=" + cy + ", effectRate=" + effectRate + ", mLastOverScrollX=" + mLastOverScrollX);
						ty = Math.min(cy * effectRate * -1 + mLastOverScrollX, 0);
					}
					else {
						//Logcat.d(logLevel, "ty=" + ty + ", cy=" + cy + ", effectRate=" + effectRate + ", mLastOverScrollX=" + mLastOverScrollX);
						ty = Math.max(cy * effectRate * -1 + mLastOverScrollX, 0);
					}
				}
//					if (mScrlNext == false || mOverScrollX == 0) {
				if (effect == 1) {	// ページめくりフリップ
					canvas.save();
					if (!pseLand) {
						canvas.translate(tx, 0);
					} else {
						canvas.translate(0, ty);
					}
				}
				if (effect == 3) {	// ページめくりスクロール
					if (!pseLand) {
						drawLeft += tx;
					} else {
						drawTop += ty;
					}
				}
//					}
			}
			else {
				mLastOverScrollX = 0;
			}
			// ピンチイン/アウト中
			if (pinchsel > 0) {
				// 画面の描画位置
				mDrawRect.set(0, 0, cx, cy);

				// ズームの描画
				drawScaling(canvas, mDrawRect, cx / 2, cy / 2, pinchsel, pseLand);
			}
			else {
				// ピンチイン/アウトしていない
				// ■■■ 通常時の画面描画
				int dl;
				int dt;
				int ret;

				// 前後のページを淡く表示するためのグラデーション色
				int[] colors = {
						0x88000000 | (mMgnColor & 0x00FFFFFF),
						0xCC000000 | (mMgnColor & 0x00FFFFFF),
						0xEE000000 | (mMgnColor & 0x00FFFFFF),
						0xFF000000 | (mMgnColor & 0x00FFFFFF)};

				Canvas bmpCanvas = new Canvas(mCanvasBitmap);
    			bmpCanvas.drawColor(mMgnColor);

				int prev2Page = -1;
				int prevPage = -1;
				int nextPage = -1;
				int next2Page = -1;

				// 前のページと次のページのページ番号を求める
				if (mImage[0] != null && mImage[1] == null ) {
					prevPage = mImage[0].Page - 1;
					nextPage = mImage[0].Page + 1;
				}
				if (mImage[0] == null && mImage[1] != null ) {
					prevPage = mImage[1].Page - 1;
					nextPage = mImage[1].Page + 1;
				}
				if (mImage[0] != null && mImage[1] != null ) {
					prevPage = Math.min(mImage[0].Page, mImage[1].Page) - 1;
					nextPage = Math.max(mImage[0].Page, mImage[1].Page) + 1;
				}
				prev2Page = prevPage - 1;
				next2Page = nextPage + 1;

				// 「スクロールで前後のページへ移動」の設定が有効のとき
				if (mScrlNext && effect == 3) {
					mScrlNextStop = false;

					if (mOverScrollX > 0) {
						if (mPageWay == DEF.PAGEWAY_RIGHT) {
							if (nextPage >= mImageManager.length()) {
								mScrlNextStop = true;
							}
						} else {
							if (prevPage < 0) {
								mScrlNextStop = true;
							}
						}
					}
					if (mOverScrollX < 0) {
						if (mPageWay == DEF.PAGEWAY_RIGHT) {
							if (prevPage < 0) {
								mScrlNextStop = true;
							}
						} else {
							if (nextPage >= mImageManager.length()) {
								mScrlNextStop = true;
							}
						}
					}

					if (!mPageLock) {	// ページロック中じゃなければ
						// 現在のページ幅合計以上移動したら前後のページに移動する
						if (mOverScrollX > mDrawWidthSum || mOverScrollX > mDispWidth) {
							mPageLock = true;
							if (mPageWay == DEF.PAGEWAY_RIGHT) {
								if (nextPage <= mImageManager.length()) {
									mParentAct.nextPage();
								}
							} else {
								if (prevPage >= 0) {
									mParentAct.prevPage();
								}
							}
						}
						if (mOverScrollX < -(mDrawWidthSum) || mOverScrollX < -(mDispWidth)) {
							mPageLock = true;
							if (mPageWay == DEF.PAGEWAY_RIGHT) {
								if (prevPage >= 0) {
									mParentAct.prevPage();
								}
							} else {
								if (nextPage <= mImageManager.length()) {
									mParentAct.nextPage();
								}
							}
						}
					}

					// ページ番号が変わったときにスクロールが超過したままなら超過分を再計算する
					if (mImage[0] != null && mImage[0].Page != mCurrentPage) {
						if (mOverScrollX > 0) {
							mOverScrollX = mOverScrollX - Math.min(drawWidthSum, cx);
						} else if (mOverScrollX < 0) {
							mOverScrollX = mOverScrollX + Math.min(drawWidthSum, cx);
						}
						mCurrentPage = mImage[0].Page;
						mPageLock = false;
					}

					// スクロール量が超過している分だけ描画する位置をずらす
					if (!pseLand) {
						drawLeft += mOverScrollX;
					} else {
						drawTop = mOverScrollX;
					}
				}

				ImageData prev2Image = null;
				ImageData prevImage = null;
				ImageData next2Image = null;
				ImageData nextImage = null;

				PaintDrawable prevMask = null;
				PaintDrawable nextMask = null;
				GradientDrawable prevGrad = null;
				GradientDrawable nextGrad = null;

				if ((mScrlNext && !mScrlNextStop) && effect == 3) {

					// 前のページを表示用Bitmapに書き込む
					if (prevPage > 0 && prevPage < mImageManager.length()) {
						prevImage = mImageManager.getImageData(prevPage);

						if (prevImage != null) {
							if (mPageWay == DEF.PAGEWAY_RIGHT) {
								// 右開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft + drawWidthSum;
									dt = (int) drawTop + ((mDrawHeightMax - prevImage.SclHeight) / 2);
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - prevImage.SclHeight + ((mDrawHeightMax - prevImage.SclHeight) / 2);
									dt = (int) drawLeft + drawWidthSum;
								}
							} else {
								// 左開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft - prevImage.SclWidth;
									dt = (int) drawTop;
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - prevImage.SclHeight;
									dt = (int) drawLeft - prevImage.SclWidth;
								}
							}
//						Logcat.d(logLevel, "prevImage page=" + prevPage + ", dl=" + dl + ", dt=" + dt);
							ret = CallImgLibrary.ImageDraw(mActivity, mHandler, mCacheIndex, prevImage.Page, prevImage.HalfMode, dl, dt, mCanvasBitmap);
							if (ret < 0) {
								// 描画エラー
								;
							}
						}
					}

					// ２つ前のページを表示用Bitmapに書き込む
					if (prev2Page > 0 && prev2Page < mImageManager.length()) {
						prev2Image = mImageManager.getImageData(prev2Page);

						if (prevImage != null && prev2Image != null) {
							if (mPageWay == DEF.PAGEWAY_RIGHT) {
								// 右開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft + drawWidthSum + prevImage.SclWidth;
									dt = (int) drawTop + ((mDrawHeightMax - prev2Image.SclHeight) / 2);
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - prev2Image.SclHeight + ((mDrawHeightMax - prev2Image.SclHeight) / 2);
									dt = (int) drawLeft + drawWidthSum + prevImage.SclWidth;
								}
							} else {
								// 左開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft - prevImage.SclWidth - prev2Image.SclWidth;
									dt = (int) drawTop;
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - prev2Image.SclHeight;
									dt = (int) drawLeft - prevImage.SclWidth - prev2Image.SclWidth;
								}
							}
//						Logcat.d(logLevel, "prevImage page=" + prevPage + ", dl=" + dl + ", dt=" + dt);
							ret = CallImgLibrary.ImageDraw(mActivity, mHandler, mCacheIndex, prev2Image.Page, prev2Image.HalfMode, dl, dt, mCanvasBitmap);
							if (ret < 0) {
								// 描画エラー
								;
							}
						}
					}

					// 次のページを表示用Bitmapに書き込む
					if (nextPage > 0 && nextPage < mImageManager.length()) {
						nextImage = mImageManager.getImageData(nextPage);

						if (nextImage != null) {
							if (mPageWay == DEF.PAGEWAY_RIGHT) {
								// 右開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft - nextImage.SclWidth;
									dt = (int) drawTop + ((mDrawHeightMax - nextImage.SclHeight) / 2);
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - nextImage.SclHeight;
									dt = (int) drawLeft - nextImage.SclWidth + ((mDrawHeightMax - nextImage.SclHeight) / 2);
								}
							} else {
								// 左開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft + drawWidthSum;
									dt = (int) drawTop;
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - nextImage.SclHeight;
									dt = (int) drawLeft + drawWidthSum;
								}
							}
//						Logcat.d(logLevel, "nextImage page=" + nextPage + ", dl=" + dl + ", dt=" + dt);
							ret = CallImgLibrary.ImageDraw(mActivity, mHandler, mCacheIndex, nextImage.Page, nextImage.HalfMode, dl, dt, mCanvasBitmap);
							if (ret < 0) {
								// 描画エラー
								;
							}
						}
					}

					// 2つ次のページを表示用Bitmapに書き込む
					if (next2Page > 0 && next2Page < mImageManager.length()) {
						next2Image = mImageManager.getImageData(next2Page);

						if (nextImage != null && next2Image != null) {
							if (mPageWay == DEF.PAGEWAY_RIGHT) {
								// 右開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft - nextImage.SclWidth - next2Image.SclWidth;
									dt = (int) drawTop + ((mDrawHeightMax - next2Image.SclHeight) / 2);
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - next2Image.SclHeight + ((mDrawHeightMax - next2Image.SclHeight) / 2);
									dt = (int) drawLeft - nextImage.SclWidth - next2Image.SclWidth;
								}
							} else {
								// 左開き
								if (!pseLand) {
									// 横持ち
									dl = (int) drawLeft + drawWidthSum + nextImage.SclWidth;
									dt = (int) drawTop;
								} else {
									// 縦持ち
									dl = cy - (int) drawTop - next2Image.SclHeight;
									dt = (int) drawLeft + drawWidthSum + nextImage.SclWidth;
								}
							}
//						Logcat.d(logLevel, "nextImage page=" + nextPage + ", dl=" + dl + ", dt=" + dt);
							ret = CallImgLibrary.ImageDraw(mActivity, mHandler, mCacheIndex, next2Image.Page, next2Image.HalfMode, dl, dt, mCanvasBitmap);
							if (ret < 0) {
								// 描画エラー
								;
							}
						}
					}
				}
				// 左ページを表示用Bitmapに書き込む
				if (mImage[1] != null) {
    				if (!pseLand) {
						// 横持ち
    					dl = (int)drawLeft;
    					dt = (int)drawTop;
    				}
    				else {
						// 縦持ち
    					dl = cy - (int)drawTop - (int)drawHeight1;
    					dt = (int)drawLeft;
    				}
//					Logcat.d(logLevel, "mImage[1] page=" + mImage[1].Page + ", dl=" + dl + ", dt=" + dt);
    				ret = CallImgLibrary.ImageDraw(mActivity, mHandler, mCacheIndex, mImage[1].Page, mImage[1].HalfMode, dl, dt, mCanvasBitmap);
    				if (ret < 0) {
    					// 描画エラー
    					;
    				}
    			}

				// 右ページを表示用Bitmapに書き込む
    			if (mImage[0] != null) {
    				if (!pseLand) {
						// 横持ち
    					dl = (int)drawLeft + drawWidthSum - drawWidth0;
    					dt = (int)drawTop;
    				}
    				else {
						// 縦持ち
    					dl = cy - (int)drawTop - (int)drawHeight0;
    					dt = (int)drawLeft + drawWidthSum - drawWidth0;
    				}
//					Logcat.d(logLevel, "mImage[0] page=" + mImage[0].Page + ", dl=" + dl + ", dt=" + dt);
    				ret = CallImgLibrary.ImageDraw(mActivity, mHandler, mCacheIndex, mImage[0].Page, mImage[0].HalfMode, dl, dt, mCanvasBitmap);
    				if (ret < 0) {
    					// 描画エラー
    					;
    				}
    			}

				if (!mViewNext && mOverScrollX == 0) {
					if (!pseLand) {
						// 横持ち
						dl = (int) drawLeft;
						dt = (int) drawTop;

						prevMask = new PaintDrawable(mMgnColor);
						mDrawRect.set(0, 0, dl, cy);
						prevMask.setBounds(mDrawRect);
					} else {
						// 縦持ち
						dl = cy - (int) drawTop - (int) drawHeight1;
						dt = (int) drawLeft;

						prevMask = new PaintDrawable(mMgnColor);
						mDrawRect.set(0, 0, dt, cy);
						prevMask.setBounds(mDrawRect);
					}

					if (!pseLand) {
						// 横持ち
						dl = (int) drawLeft + drawWidthSum - drawWidth0;
						dt = (int) drawTop;

						nextMask = new PaintDrawable(mMgnColor);
						mDrawRect.set((int) drawLeft + drawWidthSum, 0, cx, cy);
						nextMask.setBounds(mDrawRect);
					} else {
						// 縦持ち
						dl = cy - (int) drawTop - (int) drawHeight0;
						dt = (int) drawLeft + drawWidthSum - drawWidth0;

						nextMask = new PaintDrawable(mMgnColor);
						mDrawRect.set(0, (int) drawLeft + drawWidthSum, cx, cy);
						nextMask.setBounds(mDrawRect);
					}
				}
				// 「スクロールで前後のページへ移動」の設定が有効かつ、スクロール量超過していないなら前後のページにグラデーションを重ねる
				// グラデーションOFFの時は表示しない
				else if ((mScrlNext && !mScrlNextStop) && effect == 3 && mOverScrollX == 0 && mViewNext && mNextFilter) {
					if (!pseLand) {
						// 横持ち
						dl = (int) drawLeft;
						dt = (int) drawTop;

						prevGrad = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
						mDrawRect.set(0, 0, dl, cy);
						prevGrad.setBounds(mDrawRect);
					} else {
						// 縦持ち
						dl = cy - (int) drawTop - (int) drawHeight1;
						dt = (int) drawLeft;

						prevGrad = new GradientDrawable(Orientation.BOTTOM_TOP, colors);
						mDrawRect.set(0, 0, dt, cy);
						prevGrad.setBounds(mDrawRect);
					}

					if (!pseLand) {
						// 横持ち
						dl = (int) drawLeft + drawWidthSum - drawWidth0;
						dt = (int) drawTop;

						nextGrad = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
						mDrawRect.set((int) drawLeft + drawWidthSum, 0, cx, cy);
						nextGrad.setBounds(mDrawRect);
					} else {
						// 縦持ち
						dl = cy - (int) drawTop - (int) drawHeight0;
						dt = (int) drawLeft + drawWidthSum - drawWidth0;

						nextGrad = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
						mDrawRect.set(0, (int) drawLeft + drawWidthSum, cx, cy);
						nextGrad.setBounds(mDrawRect);
					}
				}

    			if (effectRate == 0.0f) {
					// 作成した画像を表示
					canvas.drawBitmap(mCanvasBitmap, 0, 0, null);

					if (!mViewNext && mOverScrollX == 0) {
						if (prevMask != null) {
							prevMask.draw(canvas);
						}
						if (nextMask != null) {
							nextMask.draw(canvas);
						}
					}
					// 「スクロールで前後のページへ移動」の設定が有効かつ、スクロール量超過していないなら前後のページにグラデーションを重ねる
					if ((mScrlNext && !mScrlNextStop) && effect == 3 && mOverScrollX == 0 && mViewNext) {
						if (prevGrad != null) {
							prevGrad.draw(canvas);
						}
						if (nextGrad != null) {
							nextGrad.draw(canvas);
						}
					}
    			}
    			else {
//    				// エフェクト中
//    				Rect rc = mDrawRect;
//    				if (pseLand == false) {
//        				rc.set(0, 0, cx, cy);
//        				if (effectRate > 0) {
//        					rc.right -= mMgnRight - 1;
//        				}
//        				else {
//        					rc.left += mMgnLeft + 1;
//        				}
//    				}
//    				else {
//        				rc.set(0, 0, cy, cx);
//        				if (effectRate > 0) {
//        					rc.bottom -= mMgnRight - 1;
//        				}
//        				else {
//        					rc.top += mMgnLeft + 1;
//        				}
//    				}
//					if (mScrlNext == false || mOverScrollX == 0) {
						Paint bmpPaint = null;
						if (effect == 2) {	// ページめくりフェードイン
							scrollReset();
							bmpPaint = mAlphaPaint;
							bmpPaint.setAlpha((int) (255 * (1.0f - effectRate) * (effectRate > 0 ? 1 : -1)));
						}
						canvas.drawBitmap(mCanvasBitmap, 0, 0, bmpPaint);
//					}
    			}
			}

			if (pseLand) {
				int pos;
				if (cx < cy) {
					pos = cx / 2;
				}
				else {
					pos = cy / 2;
				}
				canvas.rotate(90.0f, pos, pos);
			}

			if (pinchsel == 0) {
				// 任意サイズ変更中ではない
    			if (mImage[1] != null) {
    				int center = 0;
    				// 中央のすき間
    				if (mIsMargin && mCenter > 0) {
    					paint.setColor(mCenColor);
    					canvas.drawRect(drawLeft + drawWidth1, drawTop, drawLeft + drawWidth1 + mCenter, drawTop + drawHeightMax, paint);
    					center = mCenter;
    				}

    				// 中央影
    				if (mIsShadow && mShadow > 0) {
    					// グラデーション幅算出
    					int grad_cx = 0;
    					int cen_x1, cen_x2;
    					GradientDrawable grad;
    					int[] colors = {0x00000000, 0x06000000, 0x10000000, 0x30000000, 0x80000000};

    					if (mShadow > 0) {
    						grad_cx = ((drawWidth0 + drawWidth1) / 2 * mShadow) / 100;
    					}

    					cen_x1 = (int)drawLeft + drawWidth1 + center;
    					cen_x2 = cen_x1 + grad_cx;
    					grad = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
    					mDrawRect.set(cen_x1, (int)drawTop, cen_x2, (int)drawTop + drawHeight0);
    					grad.setBounds(mDrawRect);
    					grad.draw(canvas);

    					cen_x1 = (int)drawLeft + drawWidth1 - grad_cx;
    					cen_x2 = cen_x1 + grad_cx;
    					grad = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
    					mDrawRect.set(cen_x1, (int)drawTop, cen_x2, (int)drawTop + drawHeight1);
    					grad.setBounds(mDrawRect);
    					grad.draw(canvas);
    				}
    			}

    			// オーバースクロール
				//「スクロールで前後のページへ移動」の設定が無効のときスクロール量が超過していれば、引っ張りエフェクトを表示
				if ((!mScrlNext || mScrlNextStop) && mOverScrollX != 0) {
    				// グラデーション幅算出
    				int grad_cx = Math.min(cx, cy) / 20;
    				int cen_x1, cen_x2;
    				GradientDrawable grad;
    				int[] colors = {0,
    						0x06000000 | (mGuiColor & 0x00FFFFFF),
    						0x10000000 | (mGuiColor & 0x00FFFFFF),
    						0x30000000 | (mGuiColor & 0x00FFFFFF),
    						0x80000000 | (mGuiColor & 0x00FFFFFF)};
    				if (mOverScrollX < 0) {
    					cen_x1 = cx + grad_cx * mOverScrollX / mOverScrollMax;
    					cen_x2 = cx;
    					grad = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
    					mDrawRect.set(cen_x1, 0, cen_x2, cy);
    					grad.setBounds(mDrawRect);
    					grad.draw(canvas);
    				}
    				else if (mOverScrollX > 0) {
    					cen_x1 = 0;
    					cen_x2 = grad_cx * mOverScrollX / mOverScrollMax;
    					grad = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
    					mDrawRect.set(cen_x1, 0, cen_x2, cy);
    					grad.setBounds(mDrawRect);
    					grad.draw(canvas);
    				}
    			}

    			// ルーペ機能
    			if (zoomMode != ZOOM_NONE) {
    				// 画面の描画位置
    				Rect rcDst = getZoomAreaRect(zoomMode, ZOOM_GAP, mZoomView, cx, cy, mDrawRect);

    				Paint paintFill = new Paint();
    				paintFill.setStyle(Paint.Style.FILL);
    				// ルーペの枠の描画
    				paintFill.setColor(Color.BLACK);
    				canvas.drawRect(rcDst.left - ZOOM_FRAME, rcDst.top - ZOOM_FRAME,
    						rcDst.right + ZOOM_FRAME, rcDst.bottom + ZOOM_FRAME,
    						paintFill);

    				// ズームの描画
//    				Logcat.d(logLevel, "left=" + rcDst.left + ", top=" + rcDst.top + ", right=" + rcDst.right + ", bottom=" + rcDst.bottom);
    				drawZoomArea(canvas, rcDst, touchX, touchY);
    			}
			}
			// ガイド表示
			if (mGuideView != null) {
				mGuideView.draw(canvas, cx, cy);
			}

			if (effectRate != 0.0f) {
				if ((!mScrlNext || mScrlNextStop) || mOverScrollX == 0) {
					if (effect == 1) {
						canvas.restore();
					}
				}
			}
		}
	}

	// 余白色を設定
	public void setConfig(ImageActivity parent, int mclr, int cclr, int gclr, int vp, int mgn, int cen, int sdw, int zom, int way, int sway, int srngw, int srngh, boolean pr, boolean ne, boolean fit, boolean cmgn, boolean csdw, boolean psel, int effect, boolean scrlNext, boolean viewNext, boolean nextFilter){
		mParentAct = parent;
		mMgnColor  = mclr;
		mCenColor  = cclr;
		mGuiColor  = gclr;
		mViewPoint = vp;
		mMargin    = mgn;
		if (cmgn) {
			mCenter    = cen;
		}
		else {
			mCenter    = 0;
		}
		mShadow    = sdw;
		mZoomType  = zom;
		mPrevRev   = pr;
		mIsMargin  = cmgn;
		mIsShadow  = csdw;
		mPseLand   = psel;
		mPageWay   = way;
		mScrlWay   = sway;
		mScrlRangeW = srngw;
		mScrlRangeH = srngh;
		if (mBackBitmap == null) {
			int cx = getWidth();
			int cy = getHeight();
			if (cx > 0 && cy > 0) {
				mBackBitmap = Bitmap.createBitmap(cx, cy, Config.ARGB_8888);
			}
		}
		mEffect = effect;
		mScrlNext = scrlNext;
		mViewNext = viewNext;
		mNextFilter = nextFilter;
	}

	public void setLoupeConfig(int size) {
		mLoupeSize = size;
		return;
	}

	// 回転モード反映
	public void setRotate(int rot) {
		mRotate = rot;
		return;
	}

	// ガイド表示用クラス
	public void setGuideView(GuideView view) {
		mGuideView = view;
		view.setParentView(this);
		view.setUpdateListear(this);
	}

	// ガイド表示用クラス
	public void setImageManager(ImageManager imgMgr) {
		mImageManager = imgMgr;
		mCacheIndex = mImageManager.getCacheIndex();
	}

	public void createBackground(boolean flag) {
		// 一時保存モード
		if (flag) {
			// ローディング前の待避
			if (mBackBitmap != null) {
				Canvas canvas = new Canvas(mBackBitmap);
				draw(canvas, true);
			}
		}
		else {
			// ページ移動終了
		}
		mIsBackDraw = flag;
	}

	// エフェクト処理
	public void setEffectRate(float rate) {
		mEffectRate = rate;
		updateNotify();
	}

	public void setImageBitmap(ImageData[] bm) {
		if (bm != null) {
			mImage[0] = bm[0];
			mImage[1] = bm[1];
		}
		else {
			mImage[0] = null;
			mImage[1] = null;
		}
	}

	/**
	 * 画面サイズ変更時の通知
	 * @param w, h, oldw, oldh
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		int logLevel = Logcat.LOG_LEVEL_WARN;
/*		// 現在の表示を保存
		String str_bkup = mTextView.getText().toString();
		mTextView.setText(mResizingStr);
		mTextView.setBackgroundColor(mTopColor);
*/
//		if (mBackMode) {
		for (int retry = 0 ; retry < 3 ; retry ++) {
			try {
				mBackBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				Canvas canvas = new Canvas(mBackBitmap);
				canvas.drawColor(mMgnColor);
				break;
			}
			catch (OutOfMemoryError e) {
				Logcat.i(logLevel, "", e);
				System.gc();
			}
		}
//		}

		if (!mPseLand) {
			mDispWidth = w;
			mDispHeight = h;

		}
		else {
			mDispWidth = h;
			mDispHeight = w;
		}

		for (int retry = 0 ; retry < 3 ; retry ++) {
			try {
				mCanvasBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			}
			catch (OutOfMemoryError e) {
				Logcat.i(logLevel, "", e);
				System.gc();
			}
		}

		// リサイズ処理
		mParentAct.setImageViewSize(mDispWidth, mDispHeight);
		updateOverSize(true);
		updateNotify();

/*		// 表示を戻す
		mTextView.setText(str_bkup);
		mTextView.setBackgroundColor(str_bkup.equals("") ? Color.argb(0, 0, 0, 0) : mTopColor);
*/	}

	// 画面サイズの更新
	public void updateScreenSize() {
		int w = getWidth();
		int h = getHeight();
		if (w == 0 || h == 0) {
			return;
		}
		onSizeChanged(w, h, 0, 0);
	}

	public void updateOverSize(boolean isPageBack, boolean isResize) {
/*		// 表示を戻す
		String str_bkup = mTextView.getText().toString();
		mTextView.setText(mResizingStr);
		mTextView.setBackgroundColor(mTopColor);
*/
		mIsPageBack = isPageBack;
//		int disp_x = getWidth();
//		int disp_y = getHeight();
//
//		if (mPseLand == false) {
//			// 疑似横画面ではないのでそのまま
//			mDispWidth  = disp_x;	// 画面サイズ
//			mDispHeight = disp_y;
//		}
//		else {
//			// 疑似横画面
//			mDispWidth  = disp_y;	// 画面サイズ
//			mDispHeight = disp_x;
//			disp_x = mDispWidth;
//			disp_y = mDispHeight;
//		}
		updateOverSize(isResize);

/*		// 表示を戻す
		mTextView.setText(str_bkup);
		mTextView.setBackgroundColor(str_bkup.equals("") ? Color.argb(0, 0, 0, 0) : mTopColor);
*/	}

	public void updateOverSize(boolean isResize) {
		int view_x;
		int view_y;

		int disp_x = mDispWidth;
		int disp_y = mDispHeight;

		if (mImage[0] == null) {
			return;
		}

		float resRateX = 0.0f;
		float resRateY = 0.0f;
		if (isResize) {
			// 現在の表示位置を原寸位置で記憶
			resRateX = (mDispWidth / 2 - mDrawLeft) / mDrawWidthSum;
			resRateY = (mDispHeight / 2 - mDrawTop) / mDrawHeightMax;
		}

		// 拡大縮小後のサイズ
		// ImageManagerクラスで算出済み
		int drawWidth0  = mImage[0].SclWidth;
		int drawHeight0 = mImage[0].SclHeight;
		int drawWidth1;
		int drawHeight1;
		if (mImage[1] != null) {
			drawWidth1  = mImage[1].SclWidth;
			drawHeight1 = mImage[1].SclHeight;
		}
		else {
			drawWidth1  = 0;
			drawHeight1 = 0;
		}

		int drawWidthSum = drawWidth0 + drawWidth1 + (drawWidth1 > 0 ? mCenter : 0);
		int drawHeightMax = drawHeight0 > drawHeight1 ? drawHeight0 : drawHeight1;

		view_x = drawWidth0 + drawWidth1;
		if (drawWidth1 > 0) {
			// 並べて表示のときは中央余白を加算
			view_x += mCenter;
		}
		view_y = drawHeightMax;

		mMgnLeft   = 0;
		mMgnRight  = 0;
		if (disp_x < view_x) {
			mMgnLeft  = mMargin;
			mMgnRight = mMargin;
		}
		else if (disp_x > view_x) {
			mMgnLeft  = (disp_x - view_x) / 2;
			mMgnRight = (disp_x - view_x) - mMgnLeft;
		}

		mMgnTop    = 0;
		mMgnBottom = 0;
		if (disp_y < view_y) {
			mMgnTop    = mMargin;
			mMgnBottom = mMargin;
		}
		else if (disp_y > view_y) {
			mMgnTop    = (disp_y - view_y) / 2;
			mMgnBottom = (disp_y - view_y) - mMgnTop;
		}

		// イメージ表示位置
		mDrawWidth[0] = drawWidth0;
		mDrawWidth[1] = drawWidth1;
		mDrawWidthSum = drawWidthSum;
		mDrawHeight[0] = drawHeight0;
		mDrawHeight[1] = drawHeight1;
		mDrawHeightMax = drawHeightMax;
		int drawLeft = 0;
		int drawTop  = 0;
		if (isResize) {
			// リサイズの場合はページ表示位置を変えない
			drawLeft = (int)((mDispWidth / 2) - (drawWidthSum * resRateX));
			drawTop = (int)((mDispHeight / 2) - (drawHeightMax * resRateY));
			if (drawLeft < mDispWidth - mDrawWidthSum - mMgnRight) {
				drawLeft = mDispWidth - mDrawWidthSum - mMgnRight;
			}
			if (drawLeft > mMgnLeft) {
				drawLeft = mMgnLeft;
			}
			if (drawTop < mDispHeight - mDrawHeightMax - mMgnBottom) {
				drawTop = mDispHeight - mDrawHeightMax - mMgnBottom;
			}
			if (drawTop > mMgnTop) {
				drawTop = mMgnTop;
			}
		}
		else {
    		if (mViewPoint == DEF.VIEWPT_CENTER) {
    				drawLeft = (disp_x - view_x) / 2;
    				drawTop  = (disp_y - view_y) / 2;
    		}
    		else {
    			Boolean isLeft = false;
    			if (mViewPoint == DEF.VIEWPT_LEFTTOP || mViewPoint == DEF.VIEWPT_LEFTBTM) {
    				isLeft = true;
    			}

    			Boolean isTop = false;
    			if (mViewPoint == DEF.VIEWPT_LEFTTOP || mViewPoint == DEF.VIEWPT_RIGHTTOP) {
    				isTop = true;
    			}

				if (mPageWay != 0) {
					// ページ方向が左表紙の場合は左へ移動
					isLeft = true;
				}

				if (mIsPageBack) {
					// ページ戻り時は画面下に移動
					isTop = false;
					if (mPrevRev) {
						// ページ戻り時の左右位置反転は左右逆
						isLeft = !isLeft;
					}
				}

    			if (isLeft){
    				drawLeft = 0 + mMgnLeft;
    			}
    			else{
    				drawLeft = disp_x - view_x - mMgnRight;
    			}

    			if (isTop){
    				drawTop  = 0 + mMgnTop;
    			}
    			else {
    				drawTop  = disp_y - view_y - mMgnBottom;
    			}
    		}
		}
		mDrawLeft = drawLeft;
		mDrawTop = drawTop;

 		// ページ送り位置の設定
 		setScrollPos();
 	}

	public int checkFlick() {
		int overX = mOverScrollX;
		// 「スクロールで前後のページへ移動」の設定が無効なら、指をあげたらスクロール超過をリセットする
		if (!mScrlNext || mScrlNextStop) {
			mOverScrollX = 0;
		}
		if (checkFlick(overX, mOverScrollMax)) {
			// 100% 以上引っ張っているとき
			// mLastAttenuate = null;
			return overX;
		}
//		}
		return 0;
	}

	private boolean checkFlick(int x, int max) {
		if (max == 0) {
			return false;
		}
		return (Math.abs(x) * 100 / max >= 90);
	}

	public void scrollReset() {
		mOverScrollX = 0;
//		mDrawLeft = 0 + mMgnLeft;
//		mDrawTop = 0 + mMgnTop;
		mMomentiumMsg = null;
	}

	public boolean getPageLock() {
		return mPageLock;
	}

	/**
	 * スクロールの停止
	 */
	public void scrollStop() {
		mMomentiumMsg = null;
	}

	public void scrollStart(float x, float y, int flickWidth, int scroll) {
//		mScrollBaseX = x;
//		mScrollBaseY = y;
		// スクロールで前後のページへ移動が有効なら、スクロール超過をリセットしない
		if (!mScrlNext  || mScrlNextStop) {
			mOverScrollX = 0;
		}
		mOverScrollMax = flickWidth * scroll;
		mMomentiumMsg = null;
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
//		mScrollBaseX = x;
//		mScrollBaseY = y;
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

		mMomentiumMsg = mHandler.obtainMessage(HMSG_MOMENTIUM, scroll, term);
		mHandler.sendMessageAtTime(mMomentiumMsg, mMomentiumTime + MOMENTIUM_TERM);
	}

	/**
	 * 画像をスクロールさせます。
	 *
	 * @param	x	X軸の移動量
	 * @param	y	Y軸の移動量
	 * @param	scroll	スクロール倍率
	 * @param	flag	オーバースクロールを計算する
	 */
	public void scrollMoveAmount(float x, float y, int scroll, boolean flag) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
//		float bx = mScrollBaseX;
//		float by = mScrollBaseY;
		scrollMove(x, y, scroll, flag);
//		Logcat.d(logLevel, "bx=" + bx + ", by=" + by + " -> bx=" + mScrollBaseX + ", by=" + mScrollBaseY + " (x=" + x + ", y=" + y + ")");
//		Logcat.d(logLevel, "left=" + mDrawLeft + ", top=" + mDrawTop + " (x=" + x + ", y=" + y + ")");
	}

	/**
	 * 画像をスクロールさせます。
	 *
	 * @param	x	x方向の移動量
	 * @param	y	y方向の移動量
	 * @param	scroll	スクロール倍率
	 */
	public void scrollMove(float x, float y, int scroll, boolean flag) {
		// 再描画フラグ
		boolean fUpdate = false;

		if (x == 0 && y == 0) {
			return;
		}

		float orgLeft = mDrawLeft;
//		float moveX = ((x - mScrollBaseX) * scroll) + mOverScrollX;
//		float moveY = (y - mScrollBaseY) * scroll;
		float moveX = (x * scroll) + mOverScrollX;
		float moveY = y * scroll;

		float left = mDrawLeft + moveX;
		float top  = mDrawTop  + moveY;

		if (left < mDispWidth - mDrawWidthSum - mMgnRight) {
			left = mDispWidth - mDrawWidthSum - mMgnRight;
		}
		if (left > mMgnLeft) {
			left = mMgnLeft;
		}
		if (top < mDispHeight - mDrawHeightMax - mMgnBottom) {
			top = mDispHeight - mDrawHeightMax - mMgnBottom;
		}
		if (top > mMgnTop) {
			top = mMgnTop;
		}

		if (left != mDrawLeft || top != mDrawTop) {
//			synchronized (mDrawParams) {
				mDrawLeft = left;
				mDrawTop = top;
//			}
			fUpdate = true;
		}

		if (flag) {
			// スクロール超過量を設定
			mOverScrollX = (int)(moveX - (mDrawLeft - orgLeft));

			if (!mScrlNext || mScrlNextStop) {
				// スクロールで前後のページへ移動が無効ならスクロール量を減衰キャンセルさせる
				if (Math.abs(mOverScrollX) > mOverScrollMax) {
					mOverScrollX = mOverScrollMax * (mOverScrollX > 0 ? 1 : -1);
				}
				//Logcat.d(logLevel, "overScroll=" + mOverScrollX + ", moveX=" + moveX + ", move=" + (int)(mDrawLeft - orgLeft));
				if (mOverScrollX != 0) {
					// 減衰開始
					attenuate();
					// 描画
					fUpdate = true;
				}
			}
		}

		if (fUpdate) {
			updateNotify();
		}

//		mScrollBaseX += x;
//		mScrollBaseY += y;
		return;
	}

	// スクロールの中間位置を計算
	public void setScrollPos()
	{
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

		if (mDrawWidthSum <= mDispWidth) {
			// 画面幅より大
			x_cnt = 1;
			x_pos = new int [1][];
			x_pos[0] = new int[1];
			x_pos[0][0] = (mDispWidth - mDrawWidthSum) / 2 * -1;
		}
		else if (mDrawWidthSum > mDrawHeightMax) {
			// 見開き表示のとき
			int halfWidth = (mDrawWidthSum + 1) / 2 + mMgnRight;
//			if (halfWidth - mMgnLeft - gap <= mDispWidth && mDispWidth <= halfWidth + mMgnRight + gap * 3 && mDrawHeightMax <= mDispHeight) {
//				// 画面幅より大
//				x_cnt = 1;
//				x_pos = new int [3][];
//				x_pos[0] = new int[1];
//				x_pos[0][0] = mDrawWidthSum + mMgnRight - mDispWidth;
//				x_pos[1] = new int[1];
//				x_pos[1][0] = mDrawWidthSum / 4;
//				x_pos[2] = new int[1];
//				x_pos[2][0] = - mMgnLeft;
//			}
//			else
			if (halfWidth > mDispWidth) {
				// 画面幅よりも余分を含めた1ページの幅が大きいときは1ページ内でスクロール
				int x_wk = (mDrawWidthSum + 1) / 2 + gap + mMgnRight - mDispWidth;
				// 最大でも2/3ページ単位でスクロール
				x_cnt = (x_wk + max_scroll_x - 1) / max_scroll_x;
				x_pos = new int [2][];
				x_pos[0] = new int[x_cnt + 1];
				x_pos[1] = new int[x_cnt + 1];

				int x_st;
				x_st = mDrawWidthSum / 2 - gap;
				for (int i = 0 ; i <= x_cnt ; i ++) {
					int idx =  x_cnt - i;
					x_pos[0][i] = x_st + x_wk * idx / x_cnt;
				}
				x_st = - mMgnLeft;
				for (int i = 0 ; i <= x_cnt ; i ++) {
					int idx =  x_cnt - i;
					x_pos[1][i] = x_st + x_wk * idx / x_cnt;
				}
			}
			else {
				// 画面幅より大
				x_cnt = 1;
				x_pos = new int [2][];
				x_pos[0] = new int[1];
				x_pos[0][0] = mDrawWidthSum + mMgnRight - mDispWidth;
				x_pos[1] = new int[1];
				x_pos[1][0] = - mMgnLeft;
			}
		}
		else {
			// 画像幅全体を均等スクロール
			int x_wk = mDrawWidthSum + mMgnLeft + mMgnRight - mDispWidth;
			// 最大でも2/3ページ単位でスクロール
			x_cnt = (x_wk + max_scroll_x - 1) / max_scroll_x;
			x_pos = new int [1][];
			x_pos[0] = new int[x_cnt + 1];

			int x_st = - mMgnLeft;
			for (int i = 0 ; i <= x_cnt ; i ++) {
				int idx =  x_cnt - i;
				x_pos[0][i] = x_st + x_wk * idx / x_cnt;
			}
		}

		if (mDrawHeightMax <= mDispHeight) {
			// 画面幅より大
			y_cnt = 1;
			y_pos = new int[y_cnt];
			y_pos[0] = (mDispHeight - mDrawHeightMax) / 2 * -1;
		}
		else {
			// 画像幅全体を均等スクロール
			int y_wk = mDrawHeightMax + mMgnTop + mMgnBottom - mDispHeight;
			// 最大でも半ページ単位でスクロール
			y_cnt = (y_wk + max_scroll_y - 1) / max_scroll_y;
			y_pos = new int[y_cnt + 1];

			int y_st = - mMgnTop;
			for (int i = 0 ; i <= y_cnt ; i ++) {
				y_pos[i] = y_st + y_wk * i / y_cnt;
			}
		}

		// 配列に記録
		int p_cnt = x_pos.length;
		x_cnt = x_pos[0].length;
		y_cnt = y_pos.length;
		mScrollPos = new Point[x_cnt * y_cnt * p_cnt];
		// 左右ページごとの設定
		for (int p = 0 ; p < p_cnt ; p ++) {
			if (mScrlWay == DEF.SCRLWAY_H) {	// 横→縦の順番
				// 縦位置の設定の設定
				for (int y = 0 ; y < y_cnt ; y ++) {
					// ページ内横位置の設定の設定
					for (int x = 0 ; x < x_cnt ; x ++) {
						if (mPageWay == DEF.PAGEWAY_RIGHT) {
							mScrollPos[p * (x_cnt * y_cnt) + (y * x_cnt) + x] = new Point(x_pos[p][x] * -1, y_pos[y] * -1);
						}
						else {
							int xx = x_cnt - x - 1;
							int pp = p_cnt - p - 1;
							mScrollPos[p * (x_cnt * y_cnt) + (y * x_cnt) + x] = new Point(x_pos[pp][xx] * -1, y_pos[y] * -1);
						}
					}
				}
			}
			else {	// 縦→横の順番
				// 横位置の設定の設定
				for (int x = 0 ; x < x_cnt ; x ++) {
					// ページ内縦位置の設定の設定
					for (int y = 0 ; y < y_cnt ; y ++) {
						if (mPageWay == DEF.PAGEWAY_RIGHT) {
							mScrollPos[p * (x_cnt * y_cnt) + (x * y_cnt) + y] = new Point(x_pos[p][x] * -1, y_pos[y] * -1);
						}
						else {
							int xx = x_cnt - x - 1;
							int pp = p_cnt - p - 1;
							mScrollPos[p * (x_cnt * y_cnt) + (x * y_cnt) + y] = new Point(x_pos[pp][xx] * -1, y_pos[y] * -1);
						}
					}
				}
			}
		}
		return;
	}

	// 次の位置へスクロールする
	public boolean setViewPosScroll(int move) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. move=" + move);

		mScrollStart = SystemClock.uptimeMillis();

//		Logcat.d(logLevel, "setViewPosScroll(move=" + move + ", mOverScrollX=" + mOverScrollX +
//				", mPageWay=" + (mPageWay == DEF.PAGEWAY_RIGHT ? "RIGHT" : "LEFT") +
//				", mDrawLeft=" + mDrawLeft + ", mDrawWidthSum=" + mDrawWidthSum +
//				", mMgnRight=" + mMgnRight + ", mDispWidth=" + mDispWidth);
//		Logcat.d(logLevel, "setViewPosScroll(move=" + move +
//				", mDrawLeft + mOverScrollX=" + (mDrawLeft + mOverScrollX) +
//				", -(mDrawWidthSum + mMgnRight - mDispWidth)=" + (-(mDrawWidthSum + mMgnRight - mDispWidth)));
		
		//オーバースクロールとめくり方向が同じなら次のページ
		if (mOverScrollX > 0) {
			if (mPageWay == DEF.PAGEWAY_RIGHT && move > 0 ||
					mPageWay == DEF.PAGEWAY_LEFT && move < 0) {
				return false;
			}
		}

		//オーバースクロールとめくり方向が同じなら次のページ
		if (mOverScrollX < 0) {
			if (mPageWay == DEF.PAGEWAY_RIGHT && move < 0 ||
					mPageWay == DEF.PAGEWAY_LEFT && move > 0) {
				return false;
			}
		}


/*
		if (mPageWay == DEF.PAGEWAY_RIGHT && move > 0 ||
				mPageWay == DEF.PAGEWAY_LEFT && move < 0) {
			if (mDrawLeft + mOverScrollX >= 0) {
				return false;
			}
		}

		if (mPageWay == DEF.PAGEWAY_RIGHT && move < 0 ||
				mPageWay == DEF.PAGEWAY_LEFT && move > 0) {
			if (mDrawLeft + mOverScrollX <= - (mDrawWidthSum + mMgnRight - mDispWidth)) {
				return false;
			}
		}
*/


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
		for (int i = 0 ; i < mScrollPos.length ; i++) {
			int wk_x = (mScrollPos[i].x - (int)(mDrawLeft + mOverScrollX)) * move;
			int wk_y = (mScrollPos[i].y - (int)mDrawTop) * move;
			mScrollStartPos = new Point(wk_x, wk_y);
			//Logcat.d(logLevel, "mScrollPos[" + i +"]=(" + mScrollPos[i].x  + ", " + mScrollPos[i].y + ")" );
			//Logcat.d(logLevel, "wk_x=" + wk_x  + ", wk_y=" + wk_y );
			if (wk_x >= 0 && wk_y >= 0) {
				if (min_x == -1 || min_x >= wk_x && min_y >= wk_y) {
					// 最初のループ又はさらに近い
					min_x = wk_x;
					min_y = wk_y;
					index = i;
				}
			}
		}
		//Logcat.d(logLevel, "index=" + index + ", min_x=" + min_x  + ", min_y=" + min_y );
		if (mScrollPos[index].x == (int)(mDrawLeft + mOverScrollX) && mScrollPos[index].y == (int)mDrawTop) {
			// 丁度その位置なら次へ
			index += move >= 0 ? 1 : -1;
			if (index < 0 || index >= mScrollPos.length) {
				// 端っこの場合はページ遷移
				return false;
			}
		}
		mScrollPoint = new Point(mScrollPos[index].x, mScrollPos[index].y);
/*
		// オーバースクロールの量が画面幅を超えている場合、枠外のページでスクロールを停止する
		if (mOverScrollX > mDispWidth) {
			mScrollPoint = new Point(mScrollPos[index].x + mDispWidth, mScrollPos[index].y);
		}
		if (mOverScrollX < - mDispWidth) {
			mScrollPoint = new Point(mScrollPos[index].x - mDispWidth, mScrollPos[index].y);
		}
*/
//		moveToNextPoint();
		return true;
	}

	/**
	 * 	スクロール所要時間が過ぎていればfalse
	 */
	public boolean checkScrollTime(int scrlRange) {
		boolean result = true;
		long now = SystemClock.uptimeMillis();

		if (mScrollPoint == null || mScrollStartPos == null) {
			return true;
		}

		int x_range, x_cnt;
		int y_range, y_cnt;
		int move_cnt;

		x_range = mScrollPoint.x - mScrollStartPos.x;
		y_range = mScrollPoint.y - mScrollStartPos.y;

		x_cnt = (Math.abs(x_range) + scrlRange - 1) / scrlRange;
		y_cnt = (Math.abs(y_range) + scrlRange - 1) / scrlRange;

		move_cnt = x_cnt > y_cnt ? x_cnt : y_cnt;

		if (now - mScrollStart > (long)move_cnt * DEF.INTERVAL_SCROLL_NEXT) {
			result = false;
		}
		return result;
	}

	public boolean checkScrollPoint() {
		return mScrollPoint != null;
	}

	// 小単位でスクロールしながら目的のポイントへ
	public boolean moveToNextPoint(int scrlRange){
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.v(logLevel, " 開始します. scrlRange=" + scrlRange);

		int x_range, x_cnt, x_move;
		int y_range, y_cnt, y_move;
		int move_cnt;

		boolean result = true;
		if (mScrollPoint == null) {
			// 設定なし
			Logcat.v(logLevel, " 終了します. mScrollPoint == null");
			return false;
		}

		x_range = mScrollPoint.x - (int)(mDrawLeft + mOverScrollX);
		y_range = mScrollPoint.y - (int)mDrawTop;

		x_cnt = (Math.abs(x_range) + scrlRange - 1) / scrlRange;
		y_cnt = (Math.abs(y_range) + scrlRange - 1) / scrlRange;

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
		Logcat.v(logLevel, " 終了します. result=" + result);
		return result;
	}

	public boolean isScrolling() {
		return mScrollPoint != null ? true : false;
	}

	//	public boolean setPosLeftBottom() {
//		boolean flag = false;
//		if (mDispWidth / 2 < mDrawWidthSum / 3 || mDispHeight / 2 < mDrawHeightMax / 3) {
//			if (mDrawLeft <  (mDispWidth - mDrawWidthSum) / 2) {
//				flag = true;
//			}
//			if (mDrawTop <  (mDispHeight - mDrawHeightMax) / 2) {
//				flag = true;
//			}
//			if (flag) {
//				mDrawLeft = (mDispWidth - mDrawWidthSum) / 2;
//				mDrawTop = (mDispHeight - mDrawHeightMax) / 2;
//				update();
//				return true;
//			}
//		}
//		if (mDrawLeft < mMgnLeft) {
//			flag = true;
//		}
//		if (mDrawTop < mMgnTop) {
//			flag = true;
//		}
//		if (!flag) {
//			return false;
//		}
//		mDrawLeft =  mMgnLeft;
//		mDrawTop =  mMgnTop;
//		update();
//		return true;
//	}
//
//	//
//	public boolean setPosRightTop() {
//		boolean flag = false;
//		if (mDispWidth / 2 < mDrawWidthSum / 3 || mDispHeight / 2 < mDrawHeightMax / 3) {
//			if (mDrawLeft >  (mDispWidth - mDrawWidthSum) / 2) {
//				flag = true;
//			}
//			if (mDrawTop >  (mDispHeight - mDrawHeightMax) / 2) {
//				flag = true;
//			}
//			if (flag) {
//				mDrawLeft = (mDispWidth - mDrawWidthSum) / 2;
//				mDrawTop = (mDispHeight - mDrawHeightMax) / 2;
//				update();
//				return true;
//			}
//		}
//		if (mDrawLeft >  mDispWidth - mDrawWidthSum - mMgnRight) {
//			flag = true;
//		}
//		if (mDrawTop >  mDispHeight - mDrawHeightMax - mMgnBottom) {
//			flag = true;
//		}
//		if (!flag) {
//			return false;
//		}
//		mDrawLeft = mDispWidth - mDrawWidthSum - mMgnRight;
//		mDrawTop = mDispHeight - mDrawHeightMax - mMgnBottom;
//		update();
//		return true;
//	}

	public float getDrawLeft() {
		return mDrawLeft;
	}

	// ルーペ用のビットマップを作成
	public void drawZoomArea(Canvas canvas, Rect rcDraw, int tch_x, int tch_y){
		// 拡大対象位置
		double src_cx;	// 元画像でのサイズ
		double src_cy;
		int src1_cx = 0;	// 元画像でのサイズ
		int src1_cy = 0;
		int src2_cx = 0;	// 元画像でのサイズ
		int src2_cy = 0;

		int org_cy_max;
		float scale_x;
		float scale_x1;
		float scale_x2;
		float scale_y;

		int orgWidth[]  = {0, 0};
		int orgHeight[] = {0, 0};
		int offsetX = 0;

		// 半分表示の幅補正と回転対応
		if (mImage[0] != null) {
			// 回転対応
			if (mRotate == 0 || mRotate == 2) {
				orgWidth[0] = mImage[0].Width;
				orgHeight[0] = mImage[0].Height;
			}
			else {
				orgWidth[0] = mImage[0].Height;
				orgHeight[0] = mImage[0].Width;
			}
			// 幅補正
			if (mImage[0].HalfMode != 0) {
				orgWidth[0] = (orgWidth[0] + 1) / 2;
				// 開始位置
				if (mImage[0].HalfMode == 2) {
					offsetX = orgWidth[0];
				}
			}
		}
		if (mImage[1] != null) {
			// 回転対応
			if (mRotate == 0 || mRotate == 2) {
				orgWidth[1] = mImage[1].Width;
				orgHeight[1] = mImage[1].Height;
			}
			else {
				orgWidth[1] = mImage[1].Height;
				orgHeight[1] = mImage[1].Width;
			}
			if (mImage[1].HalfMode != 0) {
				orgWidth[1] = (orgWidth[1] + 1) / 2;
			}
		}

		if (mImage[1] == null) {
			scale_x = (float)mImage[0].SclWidth / (float)orgWidth[0];
			scale_x1 = scale_x;
			scale_x2 = scale_x;
			org_cy_max = orgHeight[0];
		}
		else {
			scale_x1 = (float)mImage[0].SclWidth / (float)orgWidth[0];
			scale_x2 = (float)mImage[1].SclWidth / (float)orgWidth[1];
			scale_x = (scale_x1 < scale_x2 ? scale_x1 : scale_x2);
			org_cy_max = (orgHeight[0] > orgHeight[1] ? orgHeight[0] : orgHeight[1]);
		}
		scale_y = (float)mDrawHeightMax / (float)org_cy_max;

		// 元サイズ算出
		if (DEF.ZOOMTYPE_ORIG10 <= mZoomType && mZoomType <= DEF.ZOOMTYPE_ORIG20) {
			src_cx = (rcDraw.right - rcDraw.left) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
			src_cy = (rcDraw.bottom - rcDraw.top) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;

			src1_cx = (int)src_cx;
			src1_cy = (int)src_cy;
			src2_cx = (int)src_cx;
			src2_cy = (int)src_cy;
		}
		else {
			src_cx = (rcDraw.right - rcDraw.left) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) /2;
			src_cy = (rcDraw.bottom - rcDraw.top) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) /2;
			//
			if (mImage[0] != null) {
				src1_cx = (int)src_cx * orgWidth[0] / mImage[0].SclWidth;	//* mImage[0].SclWidth / orgWidth[0];
				src1_cy = (int)src_cy * orgHeight[0] / mImage[0].SclHeight;	//* mImage[0].SclHeight / orgHeight[0];
			}
			if (mImage[1] != null) {
				src2_cx = (int)src_cx * orgWidth[1] / mImage[1].SclWidth; //* mImage[1].SclWidth / orgWidth[1];
				src2_cy = (int)src_cy * orgHeight[1] / mImage[1].SclHeight; //* mImage[1].SclHeight / orgHeight[1];
			}
		}

		float fitScale1 = 1.0f;
		float fitScale2 = 1.0f;
		if (mImage[0] != null && mImage[1] != null && mImage[1].SclHeight == mImage[0].SclHeight) {
			// 高さを拡大していた場合は拡大率を上げる
			if (orgHeight[0] < orgHeight[1]) {
				fitScale1 = (float)orgHeight[1] / (float)orgHeight[0];
			}
			else if (orgHeight[0] > orgHeight[1]) {
				fitScale2 = (float)orgHeight[0] / (float)orgHeight[1];
			}
		}

		int cmgn = (mDrawWidth[1] == 0 ? 0 : mCenter);
		// タッチ位置は元画像のどこになるのか
		float pos_x[] = new float[2];
		float pos_y[] = new float[2];

		if ((mScrlNext && !mScrlNextStop) && mEffect == 3) {
			// 「スクロールで前後のページへ移動」の設定が有効ならスクロール分を補正する
			pos_x[0] = (tch_x - (mDrawLeft + mDrawWidth[1] + cmgn) - mOverScrollX) / scale_x / fitScale1;
		}
		else {
			pos_x[0] = (tch_x - (mDrawLeft + mDrawWidth[1] + cmgn)) / scale_x / fitScale1;
		}
		pos_y[0] = (tch_y - mDrawTop) / scale_y / fitScale1;

		
		RectF rcSrc[] = new RectF[2];
		rcSrc[0] = new RectF(pos_x[0] - src1_cx + offsetX, pos_y[0] - src1_cy, pos_x[0] + src1_cx + offsetX, pos_y[0] + src1_cy);

		if (mDrawWidth[1] > 0) {
			if ((mScrlNext && !mScrlNextStop) && mEffect == 3) {
				// 「スクロールで前後のページへ移動」の設定が有効ならスクロール分を補正する
				pos_x[1] = (int)((tch_x - (mDrawLeft) - mOverScrollX) / scale_x / fitScale2);
			}
			else {
				pos_x[1] = (int)((tch_x - mDrawLeft) / scale_x / fitScale2);
			}
			pos_y[1] = (int)((tch_y - mDrawTop) / scale_y / fitScale2);

			rcSrc[1] = new RectF(pos_x[1] - src2_cx, pos_y[1] - src2_cy, pos_x[1] + src2_cx, pos_y[1] + src2_cy);
		}

		Paint paint = mDrawPaint;
		paint.setColor(mMgnColor);

		// 塗りつぶし
		Canvas bmpCanvas = new Canvas(mCanvasBitmap);
		bmpCanvas.drawRect(0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top, paint);

		for (int i = 0 ; i < 2 ; i ++) {
			if (mImage[i] == null) {
				// 無ければスキップ
				continue;
			}

			// 拡大描画処理
			if (0 <= rcSrc[i].right && rcSrc[i].left < orgWidth[i] + offsetX && 0 <= rcSrc[i].bottom &&  rcSrc[i].top < orgHeight[i]) {
				CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, mImage[i].Page, mRotate
						, Math.round(rcSrc[i].left), Math.round(rcSrc[i].top), Math.round(rcSrc[i].right - rcSrc[i].left), Math.round(rcSrc[i].bottom - rcSrc[i].top)
						, 0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top
						, 0, mCanvasBitmap
						, mImage[i].CutLeft, mImage[i].CutRight, mImage[i].CutTop, mImage[i].CutBottom);

			}
		}


		// 「スクロールで前後のページへ移動」の設定が有効のとき
		if ((mScrlNext && !mScrlNextStop) && mEffect == 3) {

			int prev2Page = -1;
			int prevPage = -1;
			int nextPage = -1;
			int next2Page = -1;

			ImageData prev2Image = null;
			ImageData prevImage = null;
			ImageData next2Image = null;
			ImageData nextImage = null;

			GradientDrawable prevGrad = null;
			GradientDrawable nextGrad = null;

			// 前のページと次のページのページ番号を求める
			if (mImage[0] != null && mImage[1] == null) {
				prevPage = mImage[0].Page - 1;
				nextPage = mImage[0].Page + 1;
			}
			if (mImage[0] == null && mImage[1] != null) {
				prevPage = mImage[1].Page - 1;
				nextPage = mImage[1].Page + 1;
			}
			if (mImage[0] != null && mImage[1] != null) {
				prevPage = Math.min(mImage[0].Page, mImage[1].Page) - 1;
				nextPage = Math.max(mImage[0].Page, mImage[1].Page) + 1;
			}
			prev2Page = prevPage - 1;
			next2Page = nextPage + 1;

			int origWidth = 0;
			int origHeight = 0;

			// 前のページを表示用Bitmapに書き込む
			if (prevPage > 0 && prevPage < mImageManager.length()) {
				prevImage = mImageManager.getImageData(prevPage);

				if (prevImage != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = prevImage.Width;
						origHeight = prevImage.Height;
					} else {
						origWidth = prevImage.Height;
						origHeight = prevImage.Width;
					}
					if (prevImage.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) prevImage.SclWidth / (float) origWidth;
					scale_y = (float) prevImage.SclHeight / (float) origHeight;

					// 元サイズ算出
					if (DEF.ZOOMTYPE_ORIG10 <= mZoomType && mZoomType <= DEF.ZOOMTYPE_ORIG20) {
						src_cx = (rcDraw.right - rcDraw.left) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
						src_cy = (rcDraw.bottom - rcDraw.top) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
					}
					else {
						src_cx = ((rcDraw.right - rcDraw.left) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origWidth / prevImage.SclWidth;
						src_cy = ((rcDraw.bottom - rcDraw.top) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origHeight / prevImage.SclHeight;
					}
					
					// 表示開始位置
					float tmpPos_x = (tch_x - (mDrawLeft + mDrawWidth[1] + cmgn) - prevImage.SclWidth - mOverScrollX) / scale_x;
					float tmpPos_y = (tch_y - (mDrawTop)) / scale_y;

					RectF rectSrc = new RectF((int)tmpPos_x - (int)src_cx + offsetX, (int)tmpPos_y - (int)src_cy, (int)tmpPos_x + (int)src_cx + offsetX, (int)tmpPos_y + (int)src_cy);

					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, prevImage.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, 0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top
								, 0, mCanvasBitmap
								, prevImage.CutLeft, prevImage.CutRight, prevImage.CutTop, prevImage.CutBottom);
					}
				}
			}

			// 前の前のページを表示用Bitmapに書き込む
			if (prev2Page > 0 && prev2Page < mImageManager.length()) {
				prev2Image = mImageManager.getImageData(prev2Page);

				if (prevImage != null && prev2Image != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = prev2Image.Width;
						origHeight = prev2Image.Height;
					} else {
						origWidth = prev2Image.Height;
						origHeight = prev2Image.Width;
					}
					if (prev2Image.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) prev2Image.SclWidth / (float) origWidth;
					scale_y = (float) prev2Image.SclHeight / (float) origHeight;

					// 元サイズ算出
					if (DEF.ZOOMTYPE_ORIG10 <= mZoomType && mZoomType <= DEF.ZOOMTYPE_ORIG20) {
						src_cx = (rcDraw.right - rcDraw.left) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
						src_cy = (rcDraw.bottom - rcDraw.top) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
					}
					else {
						src_cx = ((rcDraw.right - rcDraw.left) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origWidth / prev2Image.SclWidth;
						src_cy = ((rcDraw.bottom - rcDraw.top) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origHeight / prev2Image.SclHeight;
					}

					// 表示開始位置
					float tmpPos_x = (int) ((tch_x - (mDrawLeft + mDrawWidth[1] + cmgn) - prevImage.SclWidth - prev2Image.SclWidth - mOverScrollX) / scale_x);
					float tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - (float)src_cx, tmpPos_y - (float)src_cy, tmpPos_x + (float)src_cx, tmpPos_y + (float)src_cy);

					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, prev2Image.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, 0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top
								, 0, mCanvasBitmap
								, prev2Image.CutLeft, prev2Image.CutRight, prev2Image.CutTop, prev2Image.CutBottom);
					}
				}
			}

			// 次のページを表示用Bitmapに書き込む
			if (nextPage > 0 && nextPage < mImageManager.length()) {
				nextImage = mImageManager.getImageData(nextPage);

				if (nextImage != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = nextImage.Width;
						origHeight = nextImage.Height;
					} else {
						origWidth = nextImage.Height;
						origHeight = nextImage.Width;
					}
					if (nextImage.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) nextImage.SclWidth / (float) origWidth;
					scale_y = (float) nextImage.SclHeight / (float) origHeight;

					// 元サイズ算出
					if (DEF.ZOOMTYPE_ORIG10 <= mZoomType && mZoomType <= DEF.ZOOMTYPE_ORIG20) {
						src_cx = (rcDraw.right - rcDraw.left) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
						src_cy = (rcDraw.bottom - rcDraw.top) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
					}
					else {
						src_cx = ((rcDraw.right - rcDraw.left) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origWidth / nextImage.SclWidth;
						src_cy = ((rcDraw.bottom - rcDraw.top) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origHeight / nextImage.SclHeight;
					}

					// 表示開始位置
					float tmpPos_x = (int) ((tch_x - (mDrawLeft) + nextImage.SclWidth - mOverScrollX) / scale_x);
					float tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - (float)src_cx + offsetX, tmpPos_y - (float)src_cy, tmpPos_x + (float)src_cx + offsetX, tmpPos_y + (float)src_cy);

					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, nextImage.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, 0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top
								, 0, mCanvasBitmap
								, nextImage.CutLeft, nextImage.CutRight, nextImage.CutTop, nextImage.CutBottom);
					}
				}
			}

			// 次の次のページを表示用Bitmapに書き込む
			if (next2Page > 0 && next2Page < mImageManager.length()) {
				next2Image = mImageManager.getImageData(next2Page);

				if (nextImage != null && next2Image != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = next2Image.Width;
						origHeight = next2Image.Height;
					} else {
						origWidth = next2Image.Height;
						origHeight = next2Image.Width;
					}
					if (next2Image.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) next2Image.SclWidth / (float) origWidth;
					scale_y = (float) next2Image.SclHeight / (float) origHeight;

					// 元サイズ算出
					if (DEF.ZOOMTYPE_ORIG10 <= mZoomType && mZoomType <= DEF.ZOOMTYPE_ORIG20) {
						src_cx = (rcDraw.right - rcDraw.left) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
						src_cy = (rcDraw.bottom - rcDraw.top) / (1 + ((mZoomType - DEF.ZOOMTYPE_ORIG10) * 1.0)) / 2;
					}
					else {
						src_cx = ((rcDraw.right - rcDraw.left) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origWidth / next2Image.SclWidth;
						src_cy = ((rcDraw.bottom - rcDraw.top) / (1.5 + ((mZoomType - DEF.ZOOMTYPE_DISP15) * 0.5)) / 2) * origHeight / next2Image.SclHeight;
					}

					// 表示開始位置
					float tmpPos_x = (int) ((tch_x - (mDrawLeft) + nextImage.SclWidth + next2Image.SclWidth - mOverScrollX) / scale_x);
					float tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - (float)src_cx + offsetX, tmpPos_y - (float)src_cy, tmpPos_x + (float)src_cx + offsetX, tmpPos_y + (float)src_cy);

					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, next2Image.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, 0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top
								, 0, mCanvasBitmap
								, next2Image.CutLeft, next2Image.CutRight, next2Image.CutTop, next2Image.CutBottom);
					}
				}
			}
		}

		Rect rcDrawSrc = new Rect(0, 0, rcDraw.right - rcDraw.left, rcDraw.bottom - rcDraw.top);
		canvas.drawBitmap(mCanvasBitmap, rcDrawSrc, rcDraw, null);
//		Logcat.d(logLevel, "left=" + rcDraw.left + ", top=" + rcDraw.top + ", right=" + rcDraw.right + ", bottom=" + rcDraw.bottom);
	}

	// ズーム用のビットマップを作成
	public void drawScaling(Canvas canvas, Rect rcDraw, int tch_x, int tch_y, int pinchsel2, boolean pseLand){
		int logLevel = Logcat.LOG_LEVEL_WARN;

		// 拡大対象位置
		int src1_cx = 0;	// 元画像でのサイズ
		int src1_cy = 0;
		int src2_cx = 0;	// 元画像でのサイズ
		int src2_cy = 0;

		int org_cy_max;
		float scale_x;
		float scale_x1;
		float scale_x2;
		float scale_y;

		float pinch_scale = pinchsel2 / 100.0f;

		float orgWidth[]  = {0, 0};
		float orgHeight[] = {0, 0};
		int offsetX = 0;

		// 半分表示の幅補正と回転対応
		if (mImage[0] != null) {
			// 回転対応
			if (mRotate == 0 || mRotate == 2) {
				orgWidth[0] = mImage[0].Width;
				orgHeight[0] = mImage[0].Height;
			}
			else {
				orgWidth[0] = mImage[0].Height;
				orgHeight[0] = mImage[0].Width;
			}
			// 幅補正
			if (mImage[0].HalfMode != 0) {
				orgWidth[0] = (orgWidth[0] + 1) / 2;
				// 開始位置
				if (mImage[0].HalfMode == 2) {
					offsetX = (int)orgWidth[0];
				}
			}
		}
		if (mImage[1] != null) {
			// 回転対応
			if (mRotate == 0 || mRotate == 2) {
				orgWidth[1] = mImage[1].Width;
				orgHeight[1] = mImage[1].Height;
			}
			else {
				orgWidth[1] = mImage[1].Height;
				orgHeight[1] = mImage[1].Width;
			}
			if (mImage[1].HalfMode != 0) {
				orgWidth[1] = (orgWidth[1] + 1) / 2;
			}
		}

		if (mImage[1] == null) {
			scale_x = (float)mImage[0].SclWidth / (float)orgWidth[0];
			scale_x1 = scale_x;
			scale_x2 = scale_x;
			org_cy_max = (int)orgHeight[0];
		}
		else {
			scale_x1 = (float)mImage[0].SclWidth / (float)orgWidth[0];
			scale_x2 = (float)mImage[1].SclWidth / (float)orgWidth[1];
			scale_x = (scale_x1 < scale_x2 ? scale_x1 : scale_x2);
			org_cy_max = (int)(orgHeight[0] > orgHeight[1] ? orgHeight[0] : orgHeight[1]);
		}
		scale_y = (float)mDrawHeightMax / (float)org_cy_max;

		// 元サイズ算出
		int view_cx = (rcDraw.right - rcDraw.left);
		int view_cy = (rcDraw.bottom - rcDraw.top);
		if (mImage[0] != null) {
			src1_cx = (int)(orgWidth[0] * view_cx / pinch_scale / mImage[0].FitWidth / 2);
			src1_cy = (int)(orgHeight[0] * view_cy / pinch_scale / mImage[0].FitHeight / 2);
		}
		if (mImage[1] != null) {
			src2_cx = (int)(orgWidth[1] * view_cx / pinch_scale / mImage[1].FitWidth / 2);
			src2_cy = (int)(orgHeight[1] * view_cy / pinch_scale / mImage[1].FitHeight / 2);
		}

		float fitScale1 = 1.0f;
		float fitScale2 = 1.0f;
		if (mImage[0] != null && mImage[1] != null && mImage[1].SclHeight == mImage[0].SclHeight) {
			// 高さを拡大していた場合は拡大率を上げる
			if (orgHeight[0] < orgHeight[1]) {
				fitScale1 = (float)orgHeight[1] / (float)orgHeight[0];
			}
			else if (orgHeight[0] > orgHeight[1]) {
				fitScale2 = (float)orgHeight[0] / (float)orgHeight[1];
			}
		}

		int cmgn = (mDrawWidth[1] == 0 ? 0 : mCenter);
		int lmgn = mDrawWidthSum > mDispWidth ? mMargin : 0;
		int rmgn = lmgn;
		int tmgn = mDrawHeightMax > mDispHeight ? mMargin : 0;
		int bmgn = tmgn;
		// タッチ位置は元画像のどこになるのか
		float pos_x[] = new float[2];
		float pos_y[] = new float[2];

		if ((mScrlNext && !mScrlNextStop) && mEffect == 3) {
			// 「スクロールで前後のページへ移動」の設定が有効ならスクロール分を補正する
			pos_x[0] = (tch_x - (mDrawLeft + mDrawWidth[1] + cmgn) - mOverScrollX) / scale_x / fitScale1;
		}
		else {
			pos_x[0] = (tch_x - (mDrawLeft + mDrawWidth[1] + cmgn)) / scale_x / fitScale1;
		}
		pos_y[0] = (tch_y - (mDrawTop)) / scale_y / fitScale1;


		RectF rcSrc[] = new RectF[2];
		rcSrc[0] = new RectF(pos_x[0] - src1_cx + offsetX, pos_y[0] - src1_cy, pos_x[0] + src1_cx + offsetX, pos_y[0] + src1_cy);
		if (cmgn != 0) {
			int cmtn_s = (int)(cmgn / scale_x / fitScale1);
			int cmtn_d = (int)(cmtn_s * mImage[0].SclWidth / mImage[0].FitWidth / pinch_scale);
			rcSrc[0].offset((cmtn_s - cmtn_d) / 2, 0);
		}

		if (mDrawWidth[1] > 0) {
			if ((mScrlNext && !mScrlNextStop) && mEffect == 3) {
				// 「スクロールで前後のページへ移動」の設定が有効ならスクロール分を補正する
				pos_x[1] = (int)((tch_x - (mDrawLeft) - mOverScrollX) / scale_x / fitScale2);
			}
			else {
				pos_x[1] = (int)((tch_x - (mDrawLeft)) / scale_x / fitScale2);
			}
			pos_y[1] = (int)((tch_y - (mDrawTop)) / scale_y / fitScale2);
			
			rcSrc[1] = new RectF(pos_x[1] - src2_cx, pos_y[1] - src2_cy, pos_x[1] + src2_cx, pos_y[1] + src2_cy);
			if (cmgn != 0) {
				int cmtn_s = (int)(cmgn / scale_x / fitScale2);
				int cmtn_d = (int)(cmtn_s * mImage[1].SclWidth / mImage[1].FitWidth / pinch_scale);
				rcSrc[1].offset((cmtn_s - cmtn_d) / 2 * -1, 0);
			}
		}

		// はみ出したらセンタリング
		float dwidth = rcDraw.right - rcDraw.left;
		float dheight = rcDraw.bottom - rcDraw.top;
		float fwidth = mImage[0].FitWidth + (mImage[1] != null ? mImage[1].FitWidth : 0);
		float fheight = mImage[0].FitHeight;
		if (mImage[1] != null && fheight < mImage[1].FitHeight) {
			fheight = mImage[1].FitHeight;
		}
		float pwidth = fwidth * pinch_scale + cmgn;
		float pheight = fheight * pinch_scale;
		float swidth = mDrawWidthSum;
		float sheight = mDrawHeightMax;
		float pleft = tch_x - (tch_x - mDrawLeft) * pwidth / swidth;
		float ptop = tch_y - (tch_y - mDrawTop) * pheight / sheight;

		float smx = dwidth - pwidth;
		float smy = dheight - pheight;
		float pleftaft = pleft;
		float ptopaft = ptop;
		if (smx < 0) {
			if (pleft < smx - rmgn) {
				pleftaft = smx - rmgn;
			}
			else if (pleft > lmgn) {
				pleftaft = lmgn;
			}
		}
		else {
			// 画面の方が広い
			pleftaft = smx / 2;
		}
		if (smy < 0) {
			if (ptop < smy - bmgn) {
				ptopaft = smy - bmgn;
			}
			else if (ptop > tmgn) {
				ptopaft = tmgn;
			}
		}
		else {
			ptopaft = smy / 2;
		}
		float shiftx = pleft - pleftaft;
		float shifty = ptop - ptopaft;

		// 任意倍率は元画像の何倍なのか
		float pscale_x1 = orgWidth[0] / mImage[0].FitWidth / pinch_scale;
		float pscale_y1 = orgHeight[0] / mImage[0].FitHeight / pinch_scale;
		rcSrc[0].offset(shiftx * pscale_x1, shifty * pscale_y1);
		if (mDrawWidth[1] > 0) {
			float pscale_x2 = orgWidth[1] / mImage[1].FitWidth / pinch_scale;
			float pscale_y2 = orgHeight[1] / mImage[1].FitHeight / pinch_scale;

			rcSrc[1].offset(shiftx * pscale_x2, shifty * pscale_y2);
		}

		Paint paint = mDrawPaint;
		paint.setColor(mMgnColor);

		// 塗りつぶし
		Canvas bmpCanvas = new Canvas(mCanvasBitmap);
		bmpCanvas.drawRect(0, 0, mCanvasBitmap.getWidth(), mCanvasBitmap.getHeight(), paint);
//		if (pseLand) {
//			int pos;
//			if (rcDraw.right < rcDraw.bottom) {
//				pos = rcDraw.right / 2;
//			}
//			else {
//				pos = rcDraw.bottom / 2;
//			}
//		}

		for (int i = 0 ; i < 2 ; i ++) {
			if (mImage[i] == null || rcSrc[i] == null) {
				// 無ければスキップ
				continue;
			}

			// 拡大描画処理
			if (0 <= rcSrc[i].right && rcSrc[i].left < orgWidth[i] + offsetX && 0 <= rcSrc[i].bottom &&  rcSrc[i].top < orgHeight[i]) {
				CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, mImage[i].Page, mRotate
						, Math.round(rcSrc[i].left), Math.round(rcSrc[i].top), Math.round(rcSrc[i].right - rcSrc[i].left), Math.round(rcSrc[i].bottom - rcSrc[i].top)
						, rcDraw.left, rcDraw.top, rcDraw.right, rcDraw.bottom
						, pseLand ? 1 : 0, mCanvasBitmap
						, mImage[i].CutLeft, mImage[i].CutRight, mImage[i].CutTop, mImage[i].CutBottom);

			}
		}


		// 「スクロールで前後のページへ移動」の設定が有効のとき
		if ((mScrlNext && !mScrlNextStop) && mEffect == 3) {

			int prev2Page = -1;
			int prevPage = -1;
			int nextPage = -1;
			int next2Page = -1;

			ImageData prev2Image = null;
			ImageData prevImage = null;
			ImageData next2Image = null;
			ImageData nextImage = null;

			GradientDrawable prevGrad = null;
			GradientDrawable nextGrad = null;

			// 前のページと次のページのページ番号を求める
			if (mImage[0] != null && mImage[1] == null) {
				prevPage = mImage[0].Page - 1;
				nextPage = mImage[0].Page + 1;
			}
			if (mImage[0] == null && mImage[1] != null) {
				prevPage = mImage[1].Page - 1;
				nextPage = mImage[1].Page + 1;
			}
			if (mImage[0] != null && mImage[1] != null) {
				prevPage = Math.min(mImage[0].Page, mImage[1].Page) - 1;
				nextPage = Math.max(mImage[0].Page, mImage[1].Page) + 1;
			}
			prev2Page = prevPage - 1;
			next2Page = nextPage + 1;

			int origWidth = 0;
			int origHeight = 0;

			// 前のページを表示用Bitmapに書き込む
			if (prevPage > 0 && prevPage < mImageManager.length()) {
				prevImage = mImageManager.getImageData(prevPage);

				if (prevImage != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = prevImage.Width;
						origHeight = prevImage.Height;
					} else {
						origWidth = prevImage.Height;
						origHeight = prevImage.Width;
					}
					if (prevImage.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) prevImage.SclWidth / (float) origWidth;
					scale_y = (float) prevImage.SclHeight / (float) origHeight;

					// 元サイズ算出
					int src_cx = (int) (origWidth * view_cx / pinch_scale / prevImage.FitWidth / 2);
					int src_cy = (int) (origHeight * view_cy / pinch_scale / prevImage.FitHeight / 2);

					// 表示開始位置
					int tmpPos_x = (int) ((tch_x - (mDrawLeft + mDrawWidth[1] + cmgn) - prevImage.SclWidth - mOverScrollX) / scale_x);
					int tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - src_cx + offsetX, tmpPos_y - src_cy, tmpPos_x + src_cx + offsetX, tmpPos_y + src_cy);
					if (cmgn != 0) {
						int cmtn_s = (int) (cmgn / scale_x);
						int cmtn_d = (int) (cmtn_s * prevImage.SclWidth / prevImage.FitWidth / pinch_scale);
						rectSrc.offset((cmtn_s - cmtn_d) / 2 * -1, 0);
					}

					Logcat.d(logLevel,"pinch_scale=" + pinch_scale + ", origWidth=" + origWidth + ", origHeight=" + origHeight + ", prevImage.FitWidth=" + prevImage.FitWidth + ", prevImage.FitWidth=" + prevImage.FitHeight);
					if (pinch_scale != 0 && prevImage.FitWidth != 0 && prevImage.FitHeight != 0) {
						// 任意倍率は元画像の何倍なのか
						float pscale_x = origWidth / prevImage.FitWidth / pinch_scale;
						float pscale_y = origHeight / prevImage.FitHeight / pinch_scale;
						rectSrc.offset(shiftx * pscale_x, shifty * pscale_y);
					}
					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, prevImage.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, rcDraw.left, rcDraw.top, rcDraw.right, rcDraw.bottom
								, pseLand ? 1 : 0, mCanvasBitmap
								, prevImage.CutLeft, prevImage.CutRight, prevImage.CutTop, prevImage.CutBottom);
					}
				}
			}

			// 前の前のページを表示用Bitmapに書き込む
			if (prev2Page > 0 && prev2Page < mImageManager.length()) {
				prev2Image = mImageManager.getImageData(prev2Page);

				if (prevImage != null && prev2Image != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = prev2Image.Width;
						origHeight = prev2Image.Height;
					} else {
						origWidth = prev2Image.Height;
						origHeight = prev2Image.Width;
					}
					if (prev2Image.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) prev2Image.SclWidth / (float) origWidth;
					scale_y = (float) prev2Image.SclHeight / (float) origHeight;


					// 元サイズ算出
					int src_cx = (int) (origWidth * view_cx / pinch_scale / prev2Image.FitWidth / 2);
					int src_cy = (int) (origHeight * view_cy / pinch_scale / prev2Image.FitHeight / 2);

					// 表示開始位置
					int tmpPos_x = (int) ((tch_x - (mDrawLeft + mDrawWidth[1] + cmgn) - prev2Image.SclWidth - prev2Image.SclWidth - mOverScrollX) / scale_x);
					int tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - src_cx + offsetX, tmpPos_y - src_cy, tmpPos_x + src_cx + offsetX, tmpPos_y + src_cy);
					if (cmgn != 0) {
						int cmtn_s = (int) (cmgn / scale_x);
						int cmtn_d = (int) (cmtn_s * prev2Image.SclWidth / prev2Image.FitWidth / pinch_scale);
						rectSrc.offset((cmtn_s - cmtn_d) / 2 * -1, 0);
					}

					Logcat.d(logLevel,"pinch_scale=" + pinch_scale + ", origWidth=" + origWidth + ", origHeight=" + origHeight + ", prev2Image.FitWidth=" + prev2Image.FitWidth + ", prev2Image.FitWidth=" + prev2Image.FitHeight);
					if (pinch_scale != 0 && prev2Image.FitWidth != 0 && prev2Image.FitHeight != 0) {
						// 任意倍率は元画像の何倍なのか
						float pscale_x = origWidth / prev2Image.FitWidth / pinch_scale;
						float pscale_y = origHeight / prev2Image.FitHeight / pinch_scale;
						rectSrc.offset(shiftx * pscale_x, shifty * pscale_y);
					}
					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, prev2Image.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, rcDraw.left, rcDraw.top, rcDraw.right, rcDraw.bottom
								, pseLand ? 1 : 0, mCanvasBitmap
								, prev2Image.CutLeft, prev2Image.CutRight, prev2Image.CutTop, prev2Image.CutBottom);
					}
				}
			}

			// 次のページを表示用Bitmapに書き込む
			if (nextPage > 0 && nextPage < mImageManager.length()) {
				nextImage = mImageManager.getImageData(nextPage);

				if (nextImage != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = nextImage.Width;
						origHeight = nextImage.Height;
					} else {
						origWidth = nextImage.Height;
						origHeight = nextImage.Width;
					}
					if (nextImage.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) nextImage.SclWidth / (float) origWidth;
					scale_y = (float) nextImage.SclHeight / (float) origHeight;

					// 元サイズ算出
					int src_cx = (int) (origWidth * view_cx / pinch_scale / nextImage.FitWidth / 2);
					int src_cy = (int) (origHeight * view_cy / pinch_scale / nextImage.FitHeight / 2);

					// 表示開始位置
					int tmpPos_x = (int) ((tch_x - (mDrawLeft) + nextImage.SclWidth - mOverScrollX) / scale_x);
					int tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - src_cx, tmpPos_y - src_cy, tmpPos_x + src_cx, tmpPos_y + src_cy);
					if (cmgn != 0) {
						int cmtn_s = (int) (cmgn / scale_x);
						int cmtn_d = (int) (cmtn_s * nextImage.SclWidth / nextImage.FitWidth / pinch_scale);
						rectSrc.offset((cmtn_s - cmtn_d) / 2 * -1, 0);
					}

					Logcat.d(logLevel,"pinch_scale=" + pinch_scale + ", origWidth=" + origWidth + ", origHeight=" + origHeight + ", nextImage.FitWidth=" + nextImage.FitWidth + ", nextImage.FitWidth=" + nextImage.FitHeight);
					if (pinch_scale != 0 && nextImage.FitWidth != 0 && nextImage.FitHeight != 0) {
						// 任意倍率は元画像の何倍なのか
						float pscale_x = origWidth / nextImage.FitWidth / pinch_scale;
						float pscale_y = origHeight / nextImage.FitHeight / pinch_scale;
						rectSrc.offset(shiftx * pscale_x, shifty * pscale_y);
					}
					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, nextImage.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, rcDraw.left, rcDraw.top, rcDraw.right, rcDraw.bottom
								, pseLand ? 1 : 0, mCanvasBitmap
								, nextImage.CutLeft, nextImage.CutRight, nextImage.CutTop, nextImage.CutBottom);
					}
				}
			}


			// 次の次のページを表示用Bitmapに書き込む
			if (next2Page > 0 && next2Page < mImageManager.length()) {
				next2Image = mImageManager.getImageData(next2Page);

				if (next2Image != null && next2Image != null) {
					// 回転対応
					if (mRotate == 0 || mRotate == 2) {
						origWidth = next2Image.Width;
						origHeight = next2Image.Height;
					} else {
						origWidth = next2Image.Height;
						origHeight = next2Image.Width;
					}
					if (next2Image.HalfMode != 0) {
						origWidth = (origWidth + 1) / 2;
					}

					scale_x = (float) next2Image.SclWidth / (float) origWidth;
					scale_y = (float) next2Image.SclHeight / (float) origHeight;

					// 元サイズ算出
					int src_cx = (int) (origWidth * view_cx / pinch_scale / next2Image.FitWidth / 2);
					int src_cy = (int) (origHeight * view_cy / pinch_scale / next2Image.FitHeight / 2);

					// 表示開始位置
					int tmpPos_x = (int) ((tch_x - (mDrawLeft) + nextImage.SclWidth + next2Image.SclWidth - mOverScrollX) / scale_x);
					int tmpPos_y = (int) ((tch_y - (mDrawTop)) / scale_y);

					RectF rectSrc = new RectF(tmpPos_x - src_cx, tmpPos_y - src_cy, tmpPos_x + src_cx, tmpPos_y + src_cy);
					if (cmgn != 0) {
						int cmtn_s = (int) (cmgn / scale_x);
						int cmtn_d = (int) (cmtn_s * next2Image.SclWidth / next2Image.FitWidth / pinch_scale);
						rectSrc.offset((cmtn_s - cmtn_d) / 2 * -1, 0);
					}

					Logcat.d(logLevel,"pinch_scale=" + pinch_scale + ", origWidth=" + origWidth + ", origHeight=" + origHeight + ", next2Image.FitWidth=" + next2Image.FitWidth + ", next2Image.FitWidth=" + next2Image.FitHeight);
					if (pinch_scale != 0 && next2Image.FitWidth != 0 && next2Image.FitHeight != 0) {
						// 任意倍率は元画像の何倍なのか
						float pscale_x = origWidth / next2Image.FitWidth / pinch_scale;
						float pscale_y = origHeight / next2Image.FitHeight / pinch_scale;
						rectSrc.offset(shiftx * pscale_x, shifty * pscale_y);
					}
					// 拡大描画処理
					if (0 <= rectSrc.right && rectSrc.left < origWidth + offsetX && 0 <= rectSrc.bottom && rectSrc.top < origHeight) {
						CallImgLibrary.ImageScaleDraw(mActivity, mHandler, mCacheIndex, next2Image.Page, mRotate
								, Math.round(rectSrc.left), Math.round(rectSrc.top), Math.round(rectSrc.right - rectSrc.left), Math.round(rectSrc.bottom - rectSrc.top)
								, rcDraw.left, rcDraw.top, rcDraw.right, rcDraw.bottom
								, pseLand ? 1 : 0, mCanvasBitmap
								, next2Image.CutLeft, next2Image.CutRight, next2Image.CutTop, next2Image.CutBottom);
					}
				}
			}
		}


		canvas.drawBitmap(mCanvasBitmap, 0, 0, null);
//		if (pseLand) {
//			canvas.restore();
//		}
//		Logcat.d(logLevel, "left=" + rcDraw.left + ", top=" + rcDraw.top + ", right=" + rcDraw.right + ", bottom=" + rcDraw.bottom);
	}

	// 表示するルーペのサイズ
	public void updateZoomView() {
		int cx = getWidth();
		int cy = getHeight();
		// mZoomView = (cx < cy ? cx : cy) / 8 * 5;
		//mZoomView = (cx > cy ? cx : cy) / 16 * 7;
		mZoomView = (cx > cy ? cx : cy) / 16 * (7 + mLoupeSize);
	}

	// x, y : 拡大描画対象位置
	public void setZoomPos(int x, int y) {
		int cx;
		int cy;
		if (mPseLand == false) {
			cx = getWidth();
			cy = getHeight();
		}
		else {
			// 疑似横画面
			cx = getHeight();
			cy = getWidth();
		}

		mTouchX = x;
		mTouchY = y;
		if (mZoomMode != ZOOM_NONE) {
			// 表示位置の再判定
			Rect rc = getZoomAreaRect(mZoomMode, 0, mZoomView + ZOOM_GAP + ZOOM_TOUCH, cx,	cy, mWorkRect);
			if ((rc.left <= x && x <= rc.right)
					&& (rc.top <= y && y <= rc.bottom)) {
				if (y < cy / 2) {
					mZoomMode = (mZoomMode & 0x0F) | ZOOM_BOTTOM;
				}
				else {
					mZoomMode = (mZoomMode & 0x0F) | ZOOM_TOP;
				}

				if (x < cx / 2) {
					mZoomMode = (mZoomMode & 0xF0) | ZOOM_RIGHT;
				}
				else {
					mZoomMode = (mZoomMode & 0xF0) | ZOOM_LEFT;
				}
			}
			this.updateNotify();
		}
		return;
	}

	public void setZoomMode(boolean mode) {
		int cx;
		int cy;
		if (mPseLand == false) {
			cx = getWidth();
			cy = getHeight();
		}
		else {
			// 疑似横画面
			cx = getHeight();
			cy = getWidth();
		}

//		mZoomFlag = mode;
		if (mode) {
			mZoomMode = ZOOM_NONE;
			// 拡大表示の位置を決める
			if (mTouchX < cx / 2) {
				mZoomMode |= ZOOM_RIGHT;
			}
			else{
				mZoomMode |= ZOOM_LEFT;
			}
			if (mTouchY < cy / 2) {
				mZoomMode |= ZOOM_BOTTOM;
			}
			else{
				mZoomMode |= ZOOM_TOP;
			}
		}
		else {
			mZoomMode = ZOOM_NONE;
		}
		updateNotify();
	}

	public void setPinchChanging(int pinchsel) {
		mPinchSel = pinchsel;
	}

	public ImageData[] getImageBitmap() {
		return mImage;
	}

	private Rect getZoomAreaRect(int zoomMode, int gap, int area, int cx, int cy, Rect rc) {
		// 横位置の設定
		if ((zoomMode & 0xF0) == ZOOM_TOP) {
			rc.top = gap;
			rc.bottom = gap + area;
		}
		else {
			rc.top = cy - gap - area;
			rc.bottom = cy - gap;
		}

		// 縦位置の設定
		if ((zoomMode & 0x0F) == ZOOM_LEFT) {
			rc.left = gap;
			rc.right = gap + area;
		}
		else {
			rc.left = cx - gap - area;
			rc.right = cx - gap;
		}
		return (rc);
	}

	// ビットマップの拡大/縮小
	public Bitmap createScaledBitmap(Bitmap bm, int src_cx, int src_cy, int dst_cx, int dst_cy, boolean isGC) {
		if (bm == null || bm.isRecycled()) {
			return null;
		}

		int cnt_loop = 1;
		float scale_x = (float)dst_cx / (float)src_cx;
		float scale_y = (float)dst_cy / (float)src_cy;

		if (src_cx == dst_cx && src_cy == dst_cy) {
			// そのままのサイズの場合
			return bm;
		}
		else if (src_cx <= dst_cx && src_cy <= dst_cy) {
			// 拡大の場合
			cnt_loop = 1; //拡大は１回でOK
		}
		else {
			float scale;

			if (scale_x < scale_y) {
				scale = scale_x;
			}
			else {
				scale = scale_y;
			}

			//縮小回数
			if (scale >= 0.5) {
				cnt_loop = 1;
			}
			else if (scale >= 0.2) {
				cnt_loop = 2;
			}
			else if (scale >= 0.05) {
				cnt_loop = 3;
			}
			else {
				cnt_loop = 4;
			}
		}
		Matrix matrix = new Matrix();
		float sc_x = (float)Math.exp(Math.log(scale_x)/(float)cnt_loop);
		float sc_y = (float)Math.exp(Math.log(scale_y)/(float)cnt_loop);
		matrix.postScale( sc_x, sc_y );
		Bitmap sbm = Bitmap.createBitmap(bm, 0, 0, src_cx, src_cy, matrix, true);
		for (int i = 0 ; i < cnt_loop - 1 ; i ++) {
			sbm = Bitmap.createBitmap(sbm, 0, 0, sbm.getWidth(), sbm.getHeight(), matrix, true);
			if (isGC) {
				System.gc();
			}
		}
		return sbm;
	}

	@Override
	public boolean handleMessage(Message msg) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		// 再描画通知
		switch (msg.what) {
			case HMSG_ATTENUATE:
			{
//				Logcat.d(logLevel, "att_count=" + msg.arg1 + ", lastcnt=" + mAttenuateCount);
				if (msg.arg1 == mAttenuateCount) {
					// 最後に登録したメッセージ
					if (mOverScrollX > 0) {
						mOverScrollX -= mOverScrollMax / 8;
						if (mOverScrollX < 0) {
							mOverScrollX = 0;
						}
					}
					else if (mOverScrollX < 0) {
						// 8段階で減衰
						mOverScrollX += mOverScrollMax / 8;
						if (mOverScrollX > 0) {
							mOverScrollX = 0;
						}
					}
					updateNotify();
					if (mOverScrollX != 0) {
						long NextTime = SystemClock.uptimeMillis() + ATTENUATE_TERM;
						Message nextmsg = mHandler.obtainMessage(HMSG_ATTENUATE);
						nextmsg.arg1 = ++ mAttenuateCount;
						mHandler.sendMessageAtTime(nextmsg, NextTime);
					}
//					Logcat.d(logLevel, "overScroll=" + mOverScrollX);
				}
				break;
			}
			case HMSG_MOMENTIUM:
			{
				// 慣性スクロール
				if (mMomentiumMsg == msg) {
//					Logcat.d(logLevel, "num=" + mMomentiumNum + ", X=" + mMomentiumX + ", Y=" + mMomentiumY);
					long NextTime = SystemClock.uptimeMillis() + MOMENTIUM_TERM;
					// 最後に登録したメッセージから変化なし
//					long now = SystemClock.uptimeMillis();
//					mMomentiumTime = now;

//					int drain = term * 10 / 10 * msg.arg1;
//					int dec = term * drain / 10 / 2;	// 10msec毎に1減らす
//					float sx = mMomentiumX * term - dec;
//					float sy = mMomentiumY * term - dec;

					mMomentiumNum ++;
					// 時間が経つほど減速率を上げる
					//int drain = (mMomentiumNum / 8) ^ 2 / 200 / msg.arg1; //mMomentiumNum;// (mMomentiumNum / 2) ^ 2 / 50;

					// 時間が経つほど減速率を上げるその2
					int drain = 0;
//					if (mMomentDrain != 0) {
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

					// 等速な減速率
					// int drain = 20;


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
					float sx = mMomentiumX;// * msg.arg1;
					float sy = mMomentiumY;// * msg.arg1;
					int ox = (int)mDrawLeft;
					int oy = (int)mDrawTop;
					scrollMove(sx, sy, msg.arg1, false);
					// 次のメッセージ
					if ((ox != (int)mDrawLeft || oy != (int)mDrawTop)
								&& (Math.abs(mMomentiumX) >= 2.0f || Math.abs(mMomentiumY) >= 2.0f)) {
						mMomentiumMsg = mHandler.obtainMessage(HMSG_MOMENTIUM, msg.arg1, msg.arg2);
						mHandler.sendMessageAtTime(mMomentiumMsg, NextTime);
					}
//					Logcat.d(logLevel, "x=" + mMomentiumX + ", y=" + mMomentiumY);
				}
				break;
			}
			case DEF.HMSG_WORKSTREAM:
				// ファイルアクセスの表示
				return true;
		}
		return false;
	}

	@Override
	public void onUpdateArea(short areatype, boolean isRealtime) {
		// サムネイルページ選択の描画更新通知
		update(false);
	}
}
