package src.comitton.view.list;

import java.util.EventListener;

import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;

public class ListSwitcher implements Callback {
	private final int HMSG_LISTSWITCH = 6001;
	private final int LISTSWITCH_TERM = 10;
	
	// リスト選択
	private float mTouchX;
	private float mTouchX1;
	private float mTouchX2;
	private float mTouchY;
	private float mTouchY1;
	private float mTouchY2;
	private long mTouchTime1;
	private long mTouchTime2;
	private short mTouchCount;
	private short mTouchMode;

	private int mOffset;

	private final int GESTURE_NONE = 0;
	private final int GESTURE_FIRST = 1;
	private final int GESTURE_SWITCH = 2;

	private Handler mHandler;
	private Message mSwitchMsg;
	private ListSwitcherListener mListSwitcherListener;
	private int mAreaWidth;
	private int mStartWidth;
	private int mSwitchWidth;
	private int mBorder;

	public ListSwitcher(ListSwitcherListener listener) {
		// タイマー制御用ハンドラ
		mHandler = new Handler(this);

		// スクロール通知リスナ
		mListSwitcherListener = listener;
		mAreaWidth = 0;
	}

	public void setWidth(int areawidth, int startwidth, int switchwidth, int border) {
		mAreaWidth = areawidth;
		mStartWidth = startwidth;
		mSwitchWidth = switchwidth;
		mBorder = border;
	}
	
	public boolean sendTouchEvent(int action, float x, float y) {
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (mAreaWidth == 0) {
					return false;
				}
				mTouchX = x;
				mTouchY = y;
				mTouchMode = GESTURE_FIRST;
				mTouchCount = 0;
				if (mSwitchMsg != null) {
    				mSwitchMsg = null;
    				mOffset = 0;
    				mListSwitcherListener.onListSwitching(mOffset, false);
				}
//				Log.d("listswitch", "d(x=" + x + ", y=" + y + ")");
				break;
			case MotionEvent.ACTION_MOVE:
				mTouchX2 = mTouchX1;
				mTouchX1 = x;
				mTouchY2 = mTouchY1;
				mTouchY1 = y;
				mTouchTime2 = mTouchTime1;
				mTouchTime1 = SystemClock.uptimeMillis();
				mTouchCount ++;
				switch (mTouchMode) {
					case GESTURE_FIRST:
						if (y < mTouchY - mStartWidth || y > mTouchY + mStartWidth) {
							// 縦方向に大きく移動した場合はキャンセル
							mTouchMode = GESTURE_NONE;
						}
						else if (x < mTouchX - mStartWidth || x > mTouchX + mStartWidth) {
							mTouchMode = GESTURE_SWITCH;
							// 現在点を基準とする
							mTouchX = x;
							mListSwitcherListener.onListSwitching(0, true);
						}
						break;
					case GESTURE_SWITCH:
						mOffset = (int)(x - mTouchX);
						mListSwitcherListener.onListSwitching(mOffset, false);
						return false;

//						else if (x < mTouchX - MOVE_RENGTH) {
//							mTouchMode = GESTURE_LEFT;
//						}
//						else if (x > mTouchX + MOVE_RENGTH) {
//							mTouchMode = GESTURE_RIGHT;
//						}
//
//						if (mTouchMode == GESTURE_LEFT || mTouchMode == GESTURE_RIGHT) {
//							// モードが変わったときは新たな位置を保存
//							mTouchX = x;
//							mTouchY = y;
//							mTouchState = true;
//						}
//						break;
//					case GESTURE_RIGHT:
//					case GESTURE_LEFT:
//						if (mTouchMode == GESTURE_RIGHT && x < mTouchX - MOVE_CANCEL) {
//							mTouchMode = GESTURE_CANCEL;
//						}
//						else if (mTouchMode == GESTURE_LEFT && x > mTouchX + MOVE_CANCEL) {
//							mTouchMode = GESTURE_CANCEL;
//						}
//						if (mTouchMode == GESTURE_RIGHT && x > mTouchX) {
//							mTouchX = x;
//						}
//						else if (mTouchMode == GESTURE_LEFT && x < mTouchX) {
//							mTouchX = x;
//						}
//						break;
				}
//				Log.d("listswitch", "m(x=" + x + ", y=" + y + ")");
				break;
			case MotionEvent.ACTION_UP:
//				Log.d("listswitch", "u(x=" + x + ", y=" + y + ")");
				// 選択
				switch (mTouchMode) {
					case GESTURE_SWITCH:
						mTouchMode = GESTURE_NONE;
						int scrollTo = 0;
						int diff = 0;
						float xrange = mTouchX2 - x;
						float yrange = mTouchY2 - y;
						long term = mTouchTime1 - mTouchTime2;
//						Log.d("listswitch", "chg:cnt" + mTouchCount + ", xr=" + xrange + ", yr=" + yrange + ", term=" + term + ", offset=" + mOffset);
						if (((mOffset < 0 ? true : false) != (xrange < 0 ? true : false))
								&& ((mTouchCount >= 2 && term < 200) || (Math.abs(mOffset) >= mSwitchWidth))) { 
							diff = mOffset < 0 ? 1 : -1;
							mOffset = ((mAreaWidth + mBorder) - Math.abs(mOffset)) * diff;
						}
						startSwitch(scrollTo, diff);
						return false;
				}
				break;
		}
		return true;
	}

	public void startSwitch(int scrollTo, int diff) {
		long NextTime = SystemClock.uptimeMillis() + LISTSWITCH_TERM;
		// 完了通知
		mSwitchMsg = mHandler.obtainMessage(HMSG_LISTSWITCH, scrollTo, diff);
		mHandler.sendMessageAtTime(mSwitchMsg, NextTime);

		mListSwitcherListener.onListSwitch(diff, mOffset);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == HMSG_LISTSWITCH) {
			if (mSwitchMsg == msg) { 
    			long NextTime = SystemClock.uptimeMillis() + LISTSWITCH_TERM;
    
    			// 切り替えアニメーション
    			boolean isContinue;
    			int range = msg.arg1 - mOffset;
    			int step = mAreaWidth / 24;
    			if (Math.abs(range) > step) {
    				range = step * (range > 0 ? 1 : -1); 
    				isContinue = true;
    			}
    			else {
    				isContinue = false;
    			}
    			mOffset += range;
    
       			// 位置を通知
   				mListSwitcherListener.onListSwitching(mOffset, false);
        		if (isContinue) {
        			// 次のメッセージ
        			mSwitchMsg = mHandler.obtainMessage(HMSG_LISTSWITCH, msg.arg1, msg.arg2);
        			mHandler.sendMessageAtTime(mSwitchMsg, NextTime);
        		}
        		else {
        			// 完了通知
    				mSwitchMsg = null;
        		}
			}
		}
		return false;
	}
	
	public interface ListSwitcherListener extends EventListener {
		// 移動開始通知
		public boolean onListSwitch(int diff, int offsetx);
		// 移動中通知
		public boolean onListSwitching(float xrange, boolean isfirst);
	}
}