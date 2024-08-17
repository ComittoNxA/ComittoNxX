package src.comitton.common;

public class WaitFor implements Runnable {
	private int mSleepTime;
	private Thread mThread;
	private boolean mInterrupt;
	
	public WaitFor(int sleeptime) {
		mSleepTime = sleeptime;
		mInterrupt = false;
	}

	public void interrupt() {
		synchronized (this) {
    		if (mThread != null) {
    			mThread.interrupt();
    		}
    		else {
    			mInterrupt = true;
    		}
		}
	}
	
	public void sleep() {
		// スレッド作成
		synchronized (this) {
			if (mInterrupt) {
				// 呼び出しがあった場合は待たずに終了
				mInterrupt = false;
				return;
			}
			mThread = new Thread(this);
		}
		// スレッド開始
		mThread.start();
		// 待ち合わせ
		try {
			mThread.join();
		} catch (InterruptedException e) {
			// 中断
			;
		}
		synchronized (this) {
			mThread = null;
		}
	}

	public void run(){
		try{
			// スレッドの休止
			Thread.sleep(mSleepTime);
		}
		catch(InterruptedException e){
			;
		}
	}
}
