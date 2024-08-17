package src.comitton.noise;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class Recorder implements Runnable {
	private volatile boolean isPaused;
	private volatile boolean isRecording;
	private final Object mutex = new Object();
   
	private NoiseHandler handler;

	// Changing the sample resolution changes sample type. byte vs. short.
	private static final int SAMPLE_RATE = 8000;
	private static final int SAMPLE_PAR_SEC = 10;
	

	/**
	 *
	 */
	public Recorder(NoiseHandler handler) {
		super();
		this.handler = handler;

		this.setPaused(false);
	}

	public void run() {
		// データ取得
		synchronized (mutex) {
			while (!this.isRecording) {
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					throw new IllegalStateException("Wait() interrupted!", e);
				}
			}
		}

		// We're important...
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		// Allocate Recorder and Start Recording...
		int bufferRead = 0;
		int bufferMinSize = AudioRecord.getMinBufferSize(
				SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		int bufferCount = 1;
		int bufferSize = SAMPLE_RATE / SAMPLE_PAR_SEC;
//		if (bufferSize < bufferMinSize) {
//			// 最小バッファが0.25秒分以上なら0.25の倍数でバッファをとる
//			bufferCount = (bufferMinSize + bufferSize - 1) / bufferSize;  
//			bufferSize = bufferSize * bufferCount;
//		}
		AudioRecord recordInstance = new AudioRecord(
				MediaRecorder.AudioSource.MIC,
				SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				bufferMinSize);

		short tempBuffer[] = new short[bufferSize];
		recordInstance.startRecording();
		while (this.isRecording) {
			// Are we paused?
			synchronized (mutex) {
				if (this.isPaused) {
					try {
						mutex.wait(250);
					} catch (InterruptedException e) {
						throw new IllegalStateException("Wait() interrupted!", e);
					}
					continue;
				}
			}

			bufferRead = recordInstance.read(tempBuffer, 0, tempBuffer.length);
//			Log.d("Record", "read=" + bufferRead);
			if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
				throw new IllegalStateException(
						"read() returned AudioRecord.ERROR_INVALID_OPERATION");
			} else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
				throw new IllegalStateException(
						"read() returned AudioRecord.ERROR_BAD_VALUE");
			} else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
				throw new IllegalStateException(
						"read() returned AudioRecord.ERROR_INVALID_OPERATION");
			}

//			long sum = 0;
			short val, max;
			
			int avg[] = new int[bufferCount];
			for (int idx = 0 ; idx < avg.length ; idx ++){
//				sum = 0;
				max = 0;
				int st = bufferSize / avg.length * idx;
				int ed = bufferSize / avg.length * (idx + 1);
				for(int i = st ; i < ed ; i ++){
//					sum += Math.abs(tempBuffer[i]);
					val = (short)Math.abs((int)tempBuffer[i]);
					if (max < val) {
						max = val;
					}
				}
//				avg[idx] = (int)(sum / (bufferSize / avg.length / 4));
				avg[idx] = max;
			}

			// 通知
			handler.onNotice(avg);
		}
		recordInstance.stop();
		recordInstance.release();
	}

	/**
	 * @param isRecording
	 *          the isRecording to set
	 */
	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
			if (this.isRecording) {
				mutex.notify();
			}
		}
	}

	/**
	 * @return the isRecording
	 */
	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}

	/**
	 * @param isPaused
	 *          the isPaused to set
	 */
	public void setPaused(boolean isPaused) {
		synchronized (mutex) {
			this.isPaused = isPaused;
		}
	}

	/**
	 * @return the isPaused
	 */
	public boolean isPaused() {
		synchronized (mutex) {
			return isPaused;
		}
	}

	public void stop(){
		setPaused(true);
		setRecording(false);
	}
}
