package src.comitton.fileaccess;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;

public class WorkStream extends InputStream {
	private static final String TAG = "WorkStream";

	private final Activity mActivity;
	private String mURI;
	private String mUser;
	private String mPass;
	private FileAccess mFileAccess;
	private long mPos;
	private Handler mHandler;

	private int mRetry = 0;
	private final static int MAX_RETRY = -1;
	private final static int SLEEP_MILLIS = 10;

	public WorkStream(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @NonNull final Handler handler) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", user=" + user + ", pass=" + pass);
		mActivity = activity;
		mURI = uri;
		mUser = user;
		mPass = pass;
		mHandler = handler;
		Open();
	}

	private void Open() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + mURI + ", user=" + mUser + ", pass=" + mPass);
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "OPEN");

		try {
			mFileAccess = new FileAccess(mActivity, mURI, mUser, mPass, null);
			mFileAccess.openRandomAccessFile("r");
		} catch (FileAccessException e) {
			DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
			throw new IOException(TAG + ": Open: " + e.getLocalizedMessage());
		}
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
	}

	private void reOpen() {
		if (mRetry <= MAX_RETRY || MAX_RETRY == -1) {
			++mRetry;
			try {
				Thread.sleep(SLEEP_MILLIS);
				Open();
				seek(mPos);
			} catch (IOException | InterruptedException ignored) {
				;
			}
        }
		mRetry = 0;
	}

	@Override
	public void close() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "CLOSE");
		if (mFileAccess != null) {
			mFileAccess.close();
			mFileAccess = null;
		}
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int read(byte[] b, int off, int size) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, MessageFormat.format("開始します. pos={0}, off={1}, size={2}, pos+off+size={3}, length={4}", new Object[]{mPos, off, size, mPos+off+size, length()}));
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "READ");

		mPos += off;
		int ret = 0;
		try {
			ret = mFileAccess.read(b, off, size);
			if (ret > 0) {
				mPos += ret;
			}
		}
		catch (Exception e) {
			Logcat.w(logLevel, "", e);
			reOpen();
			ret = mFileAccess.read(b, off, size);
			if (ret > 0) {
				mPos += ret;
			}
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. ret={0}, off={1}, mPos={2}, length={3}", new Object[]{ret, off, mPos, length()}));
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
		return ret;
	}

	public void seek(long pos) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, MessageFormat.format("開始します. pos={0}", new Object[]{pos}));
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "SEEK");
		try {
			mFileAccess.seek(pos);
		}
		catch (Exception e) {
			Logcat.e(logLevel, MessageFormat.format("Catch Exception. pos={0} {1}", new Object[]{pos, e.toString()}));
		}
		mPos = pos;
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
	}


	public long getFilePointer() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "GET_FILEPOINTER");

		long result = 0L;
		try {
			result = mFileAccess.getFilePointer();
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
			reOpen();
			result = mFileAccess.getFilePointer();
		}
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
		return result;
	}

	public long length() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, " 開始します.");
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "LENGTH");

		long length = 0;
		try {
			length = mFileAccess.length();
			Logcat.d(logLevel, MessageFormat.format("終了します. length={0}", new Object[]{length}));
		}
		catch (Exception e) {
			Logcat.e(logLevel, "", e);
			reOpen();
			length = mFileAccess.length();
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. length={0}", new Object[]{length}));
		DEF.sendMessage(mHandler, DEF.HMSG_WORKSTREAM, 0, 0, "");
		return length;
	}
}
