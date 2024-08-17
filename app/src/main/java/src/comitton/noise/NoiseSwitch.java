package src.comitton.noise;

import android.os.Handler;
import android.os.Message;

public class NoiseSwitch implements NoiseHandler {
	private Recorder mRecorder;
	private Thread mThread;
	private Handler mHandler;
	private boolean mPrevOver = false;
	private int mCount = 0;
	private int mOverCount = 0;
	private int mUnderCount = 0;
	private int mPrevOpe = NOISE_NONE;

//	private int mSamplingCount = 0;
//	private int mInvalidCount;
	private int mOverLevel;
	private int mUnderLevel;
	private boolean mLongFoo;
	private int mDecSpeed; 

	private static final int NOISE_NONE = 0;
	private static final int NOISE_NEXTPAGE = 1;
	private static final int NOISE_PREVPAGE = 2;
	private static final int NOISE_NEXTSCRL = 3;
	private static final int NOISE_PREVSCRL = 4;

	public static final int MSG_NOISE = 7;
	public static final int MSG_NOISESTATE = 8;

	private static int NOISE_COUNT_OFF[]  = {4, 3, 2};
	private static int NOISE_COUNT_LONG[] = {12, 8, 4};

	private static final int NOISESTATE_TON = 1; 
	private static final int NOISESTATE_TOO = 2; 
	private static final int NOISESTATE_YET = 4; 

//	private static int SAMPLING_COUNT = 5;
//	private static int SAMPLING_UNDER = 30;
//	private static int SAMPLING_OVER  = 100;

	public NoiseSwitch(Handler handler) {
		// 初期化
		mHandler = handler;
//		mSamplingCount = SAMPLING_COUNT;
//		mInvalidCount = 0;
		mLongFoo = false;
	}

	public void setConfig(int under, int over, int dec) {
		// 初期化
		mUnderLevel = under;
		mOverLevel = over;
		mDecSpeed = dec;
	}

	public void onNotice(int level[]) {
		int state;
//		if (mInvalidCount > 0) {
//			mInvalidCount --;
//			return;
//		}
//		else if (mSamplingCount > 0) {
//			for (int i = 0 ; i < level.length && mSamplingCount > 0 ; i ++) {
//				if (mUnderLevel <= level[i]) {
//					mUnderLevel = level[i];
//				}
//				mSamplingCount --;
//			}
//			if (mSamplingCount == 0) {
//				mOverLevel = mUnderLevel  + 500;	// SAMPLING_OVER;
//				mUnderLevel = mUnderLevel + 200; // + SAMPLING_UNDER;
//			}
//			state = 2 + mSamplingCount;
//		}
//		else {
			int ope = NOISE_NONE;
			boolean isOver = false;
			
			for (int i = 0 ; i < level.length ; i ++) {
				// 音ありチェック
				if (level[i] >= mOverLevel) {
					isOver = true;
				}
				else if (level[i] < mUnderLevel) {
					isOver = false;
				}
				else {
					isOver = mPrevOver;
				}
		
				
				if (isOver) {
					if (mOverCount >= NOISE_COUNT_LONG[mDecSpeed]) {
						// フー状態でこれまでもフーかつ長いこと続いている
						// 戻りスクロール
						if (mCount == 1) {
							// 2回目に長フー
							ope = NOISE_PREVSCRL;
						}
						else if (mCount == 0) {
							// 1回目に長フー
							ope = NOISE_NEXTSCRL;
						}
						mLongFoo = true;;
					}
					// カウント
					mOverCount ++;
					mUnderCount = 0;
				}
				else {
					// 無フー
					if (mUnderCount == 0) {
						if (!mLongFoo && mPrevOpe == NOISE_NONE && mOverCount > 0) {
							// 今までフー状態で無フーに
							mCount ++;
						}
						else {
							mCount = 0;
						}
					}
					else if (mUnderCount == NOISE_COUNT_OFF[mDecSpeed]) {
						if (mLongFoo) {
							mLongFoo = false;
						}
						else if (mCount == 2) {
							// 前ページ
							ope = NOISE_PREVPAGE;
						}
						else if (mCount == 1){
							// 次ページ
							ope = NOISE_NEXTPAGE;
						}
						mCount = 0;
//						mInvalidCount = 3;
					}
					// カウント
					mOverCount = 0;
					mUnderCount ++;
				}
				
				mPrevOpe = ope;
				mPrevOver = isOver;
				if (ope != NOISE_NONE) {
					// 開始
					Message message = new Message();
					message.what = MSG_NOISE;
					message.arg1 = ope;
					message.arg2 = (int)level[i];
					mHandler.sendMessage(message);
					ope = NOISE_NONE;
				}
			}
			
			state = 1;
			if (mCount == 0) {
				if (mLongFoo) {
					state |= NOISESTATE_TOO << 8;	// "=";
				}
				else if (isOver){
					state |= NOISESTATE_YET << 8;	// "-";
				}
			}
			else if (mCount == 1) {
				if (mLongFoo) {
					state |= (NOISESTATE_TON << 8) | (NOISESTATE_TOO << 16);	// "+=";
				}
				else if (isOver){
					state |= (NOISESTATE_TON << 8) | (NOISESTATE_YET << 16);	// "+-";
				}
				else {
					state |= (NOISESTATE_TON << 8);	// "+";
				}
			}
			else if (mCount == 2) {
				if (isOver){
					state += (NOISESTATE_TON << 8) | (NOISESTATE_TON << 16) | (NOISESTATE_YET << 24);	// "++-";
				}
				else {
					state += (NOISESTATE_TON << 8) | (NOISESTATE_TON << 16);	// "++";
				}
			}
//		}
		// 開始
		Message message = new Message();
		message.what = MSG_NOISESTATE;
		message.arg1 = state;
		message.arg2 = level[0];
		mHandler.sendMessage(message);
	}

	public void recordStart() {
		if (mThread != null || mRecorder != null) {
			recordStop();
		}

		mPrevOver = false;
		mOverCount = 0;
		mUnderCount = 0;

		mRecorder = new Recorder(this);
		mThread = new Thread(mRecorder);
		mThread.start();
		mRecorder.setRecording(true);
	}

	public void recordPause(boolean flag) {
		if (mRecorder != null) {
			if (flag) {
				mRecorder.setRecording(flag);
			}
			mRecorder.setPaused(flag);
		}
	}

	public void recordStop() {
		if (mRecorder != null) {
			mRecorder.stop();
		}
		try {
			if (mThread != null) {
				mThread.join();
			}
			mThread = null;
			mRecorder = null;
		} catch (InterruptedException e) {
			;
		}
	}
}