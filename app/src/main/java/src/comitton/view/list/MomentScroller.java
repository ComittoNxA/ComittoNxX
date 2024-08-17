package src.comitton.view.list;

import java.util.EventListener;

import android.graphics.PointF;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;

public class MomentScroller implements Callback {
	// 慣性スクロール
	private final int MAX_TOUCHPOINT = 5;
	private final int TERM_MOMENT = 150;
	private int mTouchPointNum;
	private PointF mTouchPoint[];
	private long mTouchPointTime[];

	private final int HMSG_MOMENTIUM = 6001;

	private final int MOMENTIUM_TERM = 8;
	private final int MOMENTMODE_DRAIN = 6;
	private float mMomentiumX;
	private float mMomentiumY;
	private long mMomentiumTime;
	private int mMomentiumNum;
	private Message mMomentiumMsg;
	private int mMomentDrain;
	
	private Handler mHandler;
	private ScrollMoveListener mScrollMoveListener;

	public MomentScroller(ScrollMoveListener listener, float density) {
		// タイマー制御用ハンドラ
		mHandler = new Handler(this);

		// スクロール通知リスナ
		mScrollMoveListener = listener;

		mMomentDrain = (int)(MOMENTMODE_DRAIN * 3 / density); 
		if (mMomentDrain <= 0) {
			mMomentDrain = 1;
		}

		// 慣性スクロール初期化
		mTouchPointNum = 0;
		mTouchPoint = new PointF[MAX_TOUCHPOINT];
		mTouchPointTime = new long[MAX_TOUCHPOINT];
		for (int i = 0 ; i < MAX_TOUCHPOINT ; i ++) {
			mTouchPoint[i] = new PointF();
		}
	}

	public boolean sendTouchEvent(int action, float x, float y) {
		// イベント処理
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				// 慣性スクロール
				mMomentiumMsg = null;	// 前回のを停止
				mTouchPoint[0].x = x;
				mTouchPoint[0].y = y;
				mTouchPointTime[0] = SystemClock.uptimeMillis();
				mTouchPointNum = 1;
				// Log.d("moment", "x=" + x + ", y=" + y);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				long now = SystemClock.uptimeMillis();

				for (int i = MAX_TOUCHPOINT - 1 ; i >= 1 ; i --) {
					mTouchPoint[i].x = mTouchPoint[i - 1].x;
					mTouchPoint[i].y = mTouchPoint[i - 1].y;
					mTouchPointTime[i] = mTouchPointTime[i - 1];
				}
				mTouchPoint[0].x = x;
				mTouchPoint[0].y = y;
				mTouchPointTime[0] = now;
				if (mTouchPointNum < MAX_TOUCHPOINT) {
					mTouchPointNum ++;
				}
				// Log.d("moment", "x=" + x + ", y=" + y);
				break;
			}
			case MotionEvent.ACTION_UP: {
				long now = SystemClock.uptimeMillis();

				int l = 0;
				for (int i = 1 ; i < mTouchPointNum && i < MAX_TOUCHPOINT ; i ++) {
					if (now - mTouchPointTime[i]> TERM_MOMENT) {
						// 過去0.2秒の範囲
						break;
					}
					l = i;
				}

				float sx;
				float sy;
				long term;
				if (l >= 1) { 
    				if (l >= 3) {
    					sx = mTouchPoint[2].x - mTouchPoint[l].x;
    					sy = mTouchPoint[2].y - mTouchPoint[l].y;
    					term = mTouchPointTime[2] - mTouchPointTime[l];
    				}
    				else if (l >= 2) {
    					sx = mTouchPoint[1].x - mTouchPoint[l].x;
    					sy = mTouchPoint[1].y - mTouchPoint[l].y;
    					term = mTouchPointTime[1] - mTouchPointTime[l];
    				}
    				else {
    					// cnt == 1
    					sx = mTouchPoint[0].x - mTouchPoint[1].x;
    					sy = mTouchPoint[0].y - mTouchPoint[1].y;
    					term = mTouchPointTime[0] - mTouchPointTime[1];
    				}
					momentiumStart(x, y, sx, sy, (int)term);
					// Log.d("moment_up", "sx=" + sx + ", sy=" + sy + ", term=" + term + ", l=" + l);
				}
				break;
			}
		}
		return false;
	}


	/**
	 * 慣性スクロールの開始
	 * @param x      開始X位置
	 * @param y      開始Y位置
	 * @param sx     x方向速度
	 * @param sy     y方向速度
	 * @param time   開始時間
	 */
	public void momentiumStart(float x, float y, float sx, float sy, int term) {
		if (Math.abs(sx) < 2 && Math.abs(sy) < 2) {
			return;
		}
//		mScrollBaseX = x;
//		mScrollBaseY = y;
		mMomentiumX = sx * MOMENTIUM_TERM / term;
		mMomentiumY = sy * MOMENTIUM_TERM / term;
		mMomentiumTime = SystemClock.uptimeMillis();
		mMomentiumNum = 0;

		mMomentiumMsg = mHandler.obtainMessage(HMSG_MOMENTIUM, 1, term);
		mHandler.sendMessageAtTime(mMomentiumMsg, mMomentiumTime + MOMENTIUM_TERM);
	}


	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == HMSG_MOMENTIUM) {
    		// 慣性スクロール
    		if (mMomentiumMsg == msg) {
    //				Log.d("moment", "num=" + mMomentiumNum + ", X=" + mMomentiumX + ", Y=" + mMomentiumY);
    			long NextTime = SystemClock.uptimeMillis() + MOMENTIUM_TERM;
    
    			mMomentiumNum ++;
    			// 時間が経つほど減速率を上げるその2
    			int drain = 0;
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
    			boolean isContinue = mScrollMoveListener.onScrollMove(mMomentiumX, mMomentiumY);

    			// 次のメッセージ
    			if (isContinue && (Math.abs(mMomentiumX) >= 2.0f || Math.abs(mMomentiumY) >= 2.0f)) {
    				mMomentiumMsg = mHandler.obtainMessage(HMSG_MOMENTIUM, msg.arg1, msg.arg2);
    				mHandler.sendMessageAtTime(mMomentiumMsg, NextTime);
    			}
    //				Log.d("momentiumMsg", "x=" + mMomentiumX + ", y=" + mMomentiumY);
    		}
		}
		return false;
	}
	
	public interface ScrollMoveListener extends EventListener {
		// 更新通知
		public boolean onScrollMove(float xrange, float yrange);
	}
}