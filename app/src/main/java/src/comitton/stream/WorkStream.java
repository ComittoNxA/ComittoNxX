package src.comitton.stream;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.smb.SmbException;

import src.comitton.common.DEF;
import src.comitton.common.FileAccess;

import jcifs.smb.SmbRandomAccessFile;

public class WorkStream extends InputStream {
	private static final String TAG = "WorkStream";

	public static final int OFFSET_LCL_FNAME_LEN = 26;
	public static final int SIZE_LOCALHEADER = 30;

	private String mURI;
	private String mPath;
	private String mUser;
	private String mPass;
	private SmbRandomAccessFile mJcifsFile;
	private RandomAccessFile mLocalFile;
	private long mPos;
	private boolean mZipFlag;

	public WorkStream(String uri, String path, String user, String pass, boolean zipflag) throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, "WorkStream: 開始します. uri=" + uri + ", user=" + user + ", pass=" + pass + ", zipflag=" + zipflag);}
		mURI = uri;
		mPath = path;
		mUser = user;
		mPass = pass;
		mZipFlag = zipflag;
		Open(uri, path, user, pass, zipflag);
		seek(0);
		mPos = 0;
	}

	private void Open(String uri, String path, String user, String pass, boolean zipflag) throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, "Open: 開始します. uri=" + uri + ", user=" + user + ", pass=" + pass + ", zipflag=" + zipflag);}
		if (uri != null && !uri.isEmpty()) {
			if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
				mJcifsFile = FileAccess.jcifsAccessFile(uri + path, user, pass);
			}
		} else {
			mLocalFile = new RandomAccessFile(path, "r");
		}
	}
	
	public void seek(long pos) throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, MessageFormat.format("seek: 開始します. pos={0}", new Object[]{pos}));}
		try {
			if (mURI != null && mURI.length() > 0) {
				if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
					mJcifsFile.seek(pos);
				}
			} else {
				mLocalFile.seek(pos);
			}
		}
		catch (Exception e) {
			Open(mURI, mPath, mUser, mPass, mZipFlag);
			seek(pos);
		}
		mPos = pos;
	}

	public long getFilePointer() throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, "getFilePointer: 開始します.");}
		try {
			if (mURI != null && mURI.length() > 0) {
				if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
					return mJcifsFile.getFilePointer();
				}
			} else {
				return mLocalFile.getFilePointer();
			}
		}
		catch (Exception e) {
			Open(mURI, mPath, mUser, mPass, mZipFlag);
			seek(mPos);
			getFilePointer();
		}
		return 0;
	}

	public long length() throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, "length: 開始します.");}
		try {
			if (mURI != null && mURI.length() > 0) {
				if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
					if(debug) {Log.d(TAG, MessageFormat.format("length: length={0}", new Object[]{mJcifsFile.length()}));}
					return mJcifsFile.length();
				}
			}
			else {
				if(debug) {Log.d(TAG, MessageFormat.format("length: length={0}", new Object[]{mLocalFile.length()}));}
				return mLocalFile.length();
			}
		}
		catch (Exception e) {
			Open(mURI, mPath, mUser, mPass, mZipFlag);
			seek(mPos);
			if(debug) {Log.d(TAG, MessageFormat.format("length: length={0}", new Object[]{length()}));}
			return length();
		}
		return 0;
	}

	@Override
	public int read() throws IOException {
		// 読み込み
		return 0;
	}

	public int read(byte buf[], int off, int size) throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, MessageFormat.format("read: 開始します. mPos={0}, off={1}, size={2}, mPos+off+size={3}", new Object[]{mPos, off, size, mPos+off+size}));}
		int ret = 0;
		try {
			if (mURI != null && mURI.length() > 0) {
				if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {

					if (!DEF.isUiThread()) {
						// UIスレッドではない時はそのまま実行
						if(debug) {Log.d(TAG, "read: UIスレッドではありません.");}
						ret = mJcifsFile.read(buf, off, size);
					} else {
						// UIスレッドの時は新しいスレッド内で実行
						if(debug) {Log.d(TAG, "read: UIスレッドです.");}
						ExecutorService executor = Executors.newSingleThreadExecutor();
						Future<Integer> future = executor.submit(new Callable<Integer>() {

							@Override
							public Integer call() throws SmbException {
								return mJcifsFile.read(buf, off, size);
							}
						});

						try {
							ret = future.get();
						} catch (Exception e) {
							Log.e(TAG, "read: File read error. " + e.getMessage());
						}
					}
				}
			} else {
				ret = mLocalFile.read(buf, off, size);
			}
			if (mPos == 0 && mZipFlag) {
				if (ret >= OFFSET_LCL_FNAME_LEN + 2) {
					int lenFName = DEF.getShort(buf, OFFSET_LCL_FNAME_LEN);

					if (ret >= SIZE_LOCALHEADER + lenFName) {
						for (int i = 0; i < lenFName - 4; i++) {
							buf[off + SIZE_LOCALHEADER + i] = '0';
						}
					}
				}
			}
			if (ret > 0) {
				mPos += ret;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "read: Exception: " + e.getMessage());
			Open(mURI, mPath, mUser, mPass, mZipFlag);
			seek(mPos);
			return read(buf, off, size);
		}
		if(debug) {Log.d(TAG, MessageFormat.format("read: 終了します. ret={0}", new Object[]{ret}));}
		return ret;
	}

	@Override
	public void close() throws IOException {
		boolean debug = false;
		if(debug) {Log.d(TAG, "close: 開始します.");}
		if (mJcifsFile != null) {
// 閲覧終了時に固まるのでコメントアウト
//			mJcifsFile.close();
			mJcifsFile = null;
		}
		if (mLocalFile != null) {
			mLocalFile.close();
			mLocalFile = null;
		}
	}

}

