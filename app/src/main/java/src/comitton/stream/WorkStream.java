package src.comitton.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import src.comitton.common.DEF;
import src.comitton.common.FileAccess;

import jcifs.smb.SmbRandomAccessFile;

public class WorkStream extends InputStream {
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
		mURI = uri;
		mPath = path;
		mUser = user;
		mPass = pass;
		mZipFlag = zipflag;
		if (uri != null && uri.length() > 0) {
			if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
				mJcifsFile = FileAccess.jcifsAccessFile(uri + path, user, pass);
			}
		}
		else {
			mLocalFile = new RandomAccessFile(path, "r");
		}
		mPos = 0;
	}

	private void Open(String uri, String path, String user, String pass, boolean zipflag) throws IOException {
		if (uri != null && uri.length() > 0) {
			if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
				mJcifsFile = FileAccess.jcifsAccessFile(uri + path, user, pass);
			}
		} else {
			mLocalFile = new RandomAccessFile(path, "r");
		}
	}
	
	public void seek(long pos) throws IOException {
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
		try {
			if (mURI != null && mURI.length() > 0) {
				if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
					return mJcifsFile.length();
				}
			}
			else {
				return mLocalFile.length();
			}
		}
		catch (Exception e) {
			Open(mURI, mPath, mUser, mPass, mZipFlag);
			seek(mPos);
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
		int ret = 0;
		try {
			if (mURI != null && mURI.length() > 0) {
				if (FileAccess.getSmbMode() == FileAccess.SMBLIB_JCIFS) {
					ret = mJcifsFile.read(buf, off, size);
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
				Open(mURI, mPath, mUser, mPass, mZipFlag);
				seek(mPos);
				return read(buf, off, size);
			}
		return ret;
	}

	@Override
	public void close() throws IOException {
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

