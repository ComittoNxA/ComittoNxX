package src.comitton.fileaccess;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.data.FileData;

public class FileAccess {
	private static final String TAG = "FileAccess";

	private final Activity mActivity;
	private final String mURI;
	private final String mUser;
	private final String mPass;
	private final Handler mHandler;

	private File mLocalFile;
	private SmbFile mSmbFile;
	private DocumentFile mSafFile;

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	private RandomAccessFile mRandomAccessFile;
	private SmbRandomAccessFile mSmbRandomAccessFile;
	private SafRandomAccessFile mSafRandomAccessFile;


	public FileAccess (@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @Nullable Handler handler) {
		mActivity = activity;
		mURI = uri;
		mUser = user;
		mPass = pass;
		mHandler = handler;
	}

	public static int accessType(@NonNull final String uri) {
		if (uri.startsWith("/")) {
			return DEF.ACCESS_TYPE_LOCAL;
		}
		else if (uri.startsWith("smb://")) {
			return DEF.ACCESS_TYPE_SMB;
		}
		else if (uri.startsWith("content://")) {
			return DEF.ACCESS_TYPE_SAF;
		}
		return DEF.ACCESS_TYPE_LOCAL;
	}

	// 相対パスを絶対パスに変換
	public static String filename(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		String result = "";
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.filename(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.filename(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.filename(context, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// ファイル存在チェック
	public static String parent(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String result = "";
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.parent(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.parent(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.parent(context, uri);
				break;
			}
		}
		return result;
	}

	// 相対パスを絶対パスに変換
	public static String relativePath(@NonNull final Context context, @NonNull final String base, @NonNull final String target) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. base=" + base + ", target=" + target);

		String result = target;
		switch (accessType(base)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.relativePath(base, target);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.relativePath(base, target);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.relativePath(context, base, target);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static long length(@NonNull final Context context, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"開始します. uri=" + uri);

		long length = 0;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				length = LocalFileAccess.length(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				length = SmbFileAccess.length(uri, user, pass);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				length = SafFileAccess.length(context, uri);
				break;
			}
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. length={0}", new Object[]{length}));
		return length;
	}

	public long length() throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"開始します. uri=" + mURI);

		long length = 0;
		switch (accessType(mURI)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				Logcat.d(logLevel, "LOCAL:");
				try {
					if (mRandomAccessFile == null) {
						open("r");
					}
					length = mRandomAccessFile.length();
				} catch (IOException e) {
					Logcat.e(logLevel, "LOCAL:", e);
					throw new FileAccessException(TAG + ": length: LOCAL: " + e.getLocalizedMessage());
				}
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				Logcat.d(logLevel, "SMB:");
				try {
					if (mSmbRandomAccessFile == null) {
						open("r");
					}
					length = mSmbRandomAccessFile.length();
				} catch (IOException e) {
					Logcat.e(logLevel, "SMB:", e);
					throw new FileAccessException(TAG + ": length: SMB: " + e.getLocalizedMessage());
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				try {
					if (mSafRandomAccessFile == null) {
						open("r");
					}
					length = mSafRandomAccessFile.length();
				} catch (IOException e) {
					Logcat.e(logLevel, "SAF:", e);
					throw new FileAccessException(TAG + ": length: SAF: " + e.getLocalizedMessage());
				}
				break;
			}
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. length={0}", new Object[]{length}));
		return length;
	}

	public ParcelFileDescriptor openParcelFileDescriptor() throws FileAccessException {
		return openParcelFileDescriptor(mActivity, mURI, mUser, mPass, mActivity, null);
	}

	public static ParcelFileDescriptor openParcelFileDescriptor(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @NonNull final Context context, @Nullable Handler handler) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		ParcelFileDescriptor parcelFileDescriptor = null;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				parcelFileDescriptor = LocalFileAccess.openParcelFileDescriptor(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				parcelFileDescriptor = SmbFileAccess.openParcelFileDescriptor(activity, uri, user, pass, handler);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				parcelFileDescriptor = SafFileAccess.openParcelFileDescriptor(activity, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
		return parcelFileDescriptor;
	}

	public void open(@NonNull final String mode) throws FileAccessException {
		openRandomAccessFile(mode);
	}

	// RandomAccessFile
	public void openRandomAccessFile(@NonNull final String mode) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + mURI + ", mode=" + mode);

		switch (accessType(mURI)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				mRandomAccessFile = LocalFileAccess.openRandomAccessFile(mURI, mode);
				if (mRandomAccessFile == null) {
					Logcat.e(logLevel, "LOCAL: mRandomAccessFile == null");
					throw new FileAccessException(TAG + "openRandomAccessFile: LOCAL: mRandomAccessFile == null");
				}
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				mSmbRandomAccessFile = SmbFileAccess.openRandomAccessFile(mURI, mUser, mPass, mode);
				if (mSmbRandomAccessFile == null) {
					Logcat.e(logLevel, "SMB: mSmbRandomAccessFile == null");
					throw new FileAccessException(TAG + "openRandomAccessFile: SMB: mSmbRandomAccessFile == null");
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				mSafRandomAccessFile = SafFileAccess.openRandomAccessFile(mActivity, mURI, mode);
				if (mSafRandomAccessFile == null) {
					Logcat.e(logLevel, "SAF: mSafRandomAccessFile == null");
					throw new FileAccessException(TAG + "openRandomAccessFile: SAF: mSafRandomAccessFile == null");
				}
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
	}

	public void seek(@NonNull final long pos) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + mURI + ", pos=" + pos);
		switch (accessType(mURI)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				mRandomAccessFile.seek(pos);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					mSmbRandomAccessFile.seek(pos);
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.submit(new Runnable() {
						@Override
						public void run() {
							mSmbRandomAccessFile.seek(pos);
						}
					});
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				mSafRandomAccessFile.seek(pos);
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
	}

	public long getFilePointer() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");
		long result = 0l;
		switch (accessType(mURI)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				// ローカルファイルなら
				result = mRandomAccessFile.getFilePointer();
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					result = mSmbRandomAccessFile.getFilePointer();
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Long> future = executor.submit(new Callable<Long>() {

						@Override
						public Long call() {
							return mSmbRandomAccessFile.getFilePointer();
						}
					});

					try {
						result = future.get();
					} catch (Exception e) {
						Logcat.e(logLevel, "File read error.", e);
						result = 0l;
					}
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = mSafRandomAccessFile.getFilePointer();
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
		return result;
	}

	public int read(@NonNull byte[] buf, int off, int size) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, MessageFormat.format("開始します. pos={0}, off={1}, size={2}, pos+off+size={3}, length={4}", new Object[]{getFilePointer(), off, size, getFilePointer()+off+size, length()}));
		int result = 0;
		switch (accessType(mURI)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				// ローカルファイルなら
				result = mRandomAccessFile.read(buf, off, size);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					result = mSmbRandomAccessFile.read(buf, off, size);
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Integer> future = executor.submit(new Callable<Integer>() {

						@Override
						public Integer call() throws SmbException {
							return mSmbRandomAccessFile.read(buf, off, size);
						}
					});

					try {
						result = future.get();
					} catch (Exception e) {
						Logcat.e(logLevel, "read: File read error.", e);
						result = 0;
					}
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = mSafRandomAccessFile.read(buf, off, size);
				break;
			}
		}
		Logcat.d(logLevel, MessageFormat.format("終了します. ret={0}, off={1}, mPos={2}, length={3}", new Object[]{result, off, getFilePointer(), length()}));
		return result;
	}

	public void write(@NonNull final byte[] buf, final int off, final int size) throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, MessageFormat.format("開始します. off={0}, size={1}", new Object[]{off, size}));

		switch (accessType(mURI)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				// ローカルファイルなら
				mRandomAccessFile.write(buf, off, size);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					mSmbRandomAccessFile.write(buf, off, size);
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Boolean> future = executor.submit(new Callable<Boolean>() {

						@Override
						public Boolean call() throws SmbException {
							mSmbRandomAccessFile.write(buf, off, size);
							return true;
						}
					});

					try {
						future.get();
					} catch (Exception e) {
						Logcat.e(logLevel, "File read error.", e);
					}
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				mSafRandomAccessFile.write(buf, off, size);
				break;
			}
		}
		Logcat.d(logLevel,"終了します.");
	}

	public void close() throws IOException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// たまにフリーズするので非同期処理にする
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (mRandomAccessFile != null) {
						mRandomAccessFile.close();
						mRandomAccessFile = null;
					}
					if (mLocalFile != null) {
						mLocalFile = null;
					}
					if (mSmbRandomAccessFile != null) {
						mSmbRandomAccessFile.close();
						mSmbRandomAccessFile = null;
					}
					if (mSmbFile != null) {
						mSmbFile.close();
						mSmbFile = null;
					}
					if (mSafRandomAccessFile != null) {
						mSafRandomAccessFile.close();
						mSafRandomAccessFile = null;
					}
					if (mSafFile != null) {
						mSafFile = null;
					}
					Logcat.d(logLevel, "非同期処理を終了します.");
				} catch (Exception e) {
					Logcat.e(logLevel, "", e);
				}
            }
		});

		Logcat.d(logLevel, "終了します.");
	}

	// InputStreamを返す
	public InputStream getInputStream() throws FileAccessException {
		mInputStream = getInputStream(mActivity, mURI, mUser, mPass);
		return mInputStream;
	}

	// InputStreamを返す
	public static InputStream getInputStream(@NonNull final Context context, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		InputStream result = null;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.getInputStream(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.getInputStream(uri, user, pass);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.getInputStream(context, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
		return result;
	}

	// OutputStreamを返す
	public OutputStream getOutputStream() throws FileAccessException {
		mOutputStream = getOutputStream(mActivity, mURI, mUser, mPass);
		return mOutputStream;
	}

	// OutputStreamを返す
	public static OutputStream getOutputStream(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		OutputStream result = null;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.getOutputStream(activity, uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.getOutputStream(uri, user, pass);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.getOutputStream(activity, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
		return result;
	}

	public boolean exists() throws FileAccessException {
		return exists(mActivity, mURI, mUser, mPass);
	}

	// ファイル存在チェック
	public static boolean exists(@NonNull final Context context, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		boolean result = false;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.exists(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					result = SmbFileAccess.exists(uri, user, pass);
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Boolean> future = executor.submit(new Callable<Boolean>() {

						@Override
						public Boolean call() throws FileAccessException {
							return SmbFileAccess.exists(uri, user, pass);
						}
					});

					try {
						result = future.get();
					} catch (Exception e) {
						Logcat.e(logLevel, "File read error.", e);
						result = false;
					}
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.exists(context, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します.");
		return result;
	}

	public boolean isDirectory() throws FileAccessException {
		return isDirectory(mActivity, mURI, mUser, mPass);
	}

	public static boolean isDirectory(@NonNull final Context context, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		boolean result = false;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.isDirectory(uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.isDirectory(uri, user, pass);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.isDirectory(context, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public ArrayList<FileData> listFiles() throws FileAccessException {
		return listFiles(mActivity, mURI, mUser, mPass, mHandler);
	}

	public static ArrayList<FileData> listFiles(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass, @Nullable Handler handler) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", user=" + user + ", pass=" + pass);

		ArrayList<FileData> result = new ArrayList<FileData>(0);
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.listFiles(activity, uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.listFiles(activity, uri, user, pass, handler);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.listFiles(activity, uri, handler);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result.size()=" + result.size());
		return result;
	}

	public boolean renameTo(@NonNull final String fromfile, @NonNull final String tofile) throws FileAccessException {
		return renameTo(mActivity, mURI, fromfile, tofile, mUser, mPass);
	}

	public static boolean renameTo(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String fromfile, @NonNull final String tofile, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile);
		boolean result = false;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.renameTo(activity, uri, fromfile, tofile);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.renameTo(uri, fromfile, tofile, user, pass);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.renameTo(activity, uri, fromfile, tofile);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public boolean date() throws FileAccessException {
		return delete(mActivity, mURI, mUser, mPass);
	}

	// タイムスタンプ
	public static long date(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		long result = 0L;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.date(activity, uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					result = SmbFileAccess.date(uri, user, pass);
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Long> future = executor.submit(new Callable<Long>() {

						@Override
						public Long call() throws FileAccessException {
							return SmbFileAccess.date(uri, user, pass);
						}
					});

					try {
						result = future.get();
					} catch (Exception e) {
						Logcat.e(logLevel, "File read error.", e);
						result = 0L;
					}
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.date(activity, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public boolean delete() throws FileAccessException {
		return delete(mActivity, mURI, mUser, mPass);
	}

	// ファイル削除
	public static boolean delete(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		boolean result = false;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.delete(activity, uri);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				// SMBなら
				if (!DEF.isUiThread()) {
					// UIスレッドではない時はそのまま実行
					Logcat.d(logLevel, "UIスレッドではありません.");
					result = SmbFileAccess.delete(uri, user, pass);
				} else {
					// UIスレッドの時は新しいスレッド内で実行
					Logcat.d(logLevel, "UIスレッドです.");
					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Boolean> future = executor.submit(new Callable<Boolean>() {

						@Override
						public Boolean call() throws FileAccessException {
							return SmbFileAccess.delete(uri, user, pass);
						}
					});

					try {
						result = future.get();
					} catch (Exception e) {
						Logcat.e(logLevel, "File read error.", e);
						result = false;
					}
				}
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.delete(activity, uri);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public boolean mkdir(@NonNull final String item) {
		return mkdir(mActivity, mURI, item, mUser, mPass);
	}

	// ディレクトリ作成
	public static boolean mkdir(@NonNull final Context context, @NonNull final String uri, @NonNull final String item, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", item=" + item);
		boolean result = false;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.mkdir(uri, item);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.mkdir(uri, user, pass, item);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.mkdir(context, uri, item);
				break;
			}
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// ファイル作成
	public static boolean createFile(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String item, @NonNull final String user, @NonNull final String pass) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", item=" + item);
		boolean result = false;
		switch (accessType(uri)) {
			case DEF.ACCESS_TYPE_LOCAL: {
				result = LocalFileAccess.createFile(activity, uri, item);
				break;
			}
			case DEF.ACCESS_TYPE_SMB: {
				result = SmbFileAccess.createFile(uri, user, pass, item);
				break;
			}
			case DEF.ACCESS_TYPE_SAF: {
				result = SafFileAccess.createFile(activity, uri, item);
				break;
			}
		}
		Logcat.d(logLevel, " 終了します. result=" + result);
		return result;
	}

	/**
	 * Get a list of external SD card paths. (KitKat or higher.)
	 *
	 * @return A list of external SD card paths.
	 */
	public static String[] getExtSdCardPaths(@NonNull final Context context) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		List<String> paths = new ArrayList<>();
		for (File file : context.getExternalFilesDirs("external")) {
			if (file != null) {
				Logcat.d(logLevel, "SDカードのパス=" + file.toString());
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) {
					Logcat.d(logLevel, "Unexpected external file dir: " + file.getAbsolutePath());
				}
				else {
					String path = file.getAbsolutePath().substring(0, index);
					try {
						path = new File(path).getCanonicalPath() + "/";
					}
					catch (IOException e) {
						// Keep non-canonical path.
					}
					paths.add(path);
				}

			}
		}

		if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
			// Android 7-10 (API 24-29)
			StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
			List<StorageVolume> sv = sm.getStorageVolumes();
			for(int i = 0; i < sv.size(); ++i) {
				String path = null;
				Logcat.d(logLevel, "StorageVorume=" + sv.get(i).toString());
				if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.R && sv.get(i).getDirectory() != null) {
					// Android 11- (API 30)
					Logcat.d(logLevel, ": Directory=" + sv.get(i).getDirectory());
					path = sv.get(i).getDirectory().getAbsolutePath() + "/";
				}
				else if (sv.get(i).getUuid() != null) {
					Logcat.d(logLevel, ": UUID=" + sv.get(i).getUuid());
					path = "/storage/" + sv.get(i).getUuid() + "/";
				}

				if (path != null && !paths.contains(path)) {
					paths.add(path);
				}
			}
		}
		else {
			// Android 4-6 (API 14-23)
			try {
				StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
				Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
				Object svObj = getVolumeList.invoke(sm);
				final int length = Array.getLength(svObj);
				for (int i = 0; i < length; i++) {
					Object sv = Array.get(svObj, i);
					Logcat.d(logLevel, "StorageVorume=" + sv.toString());
					Method getPath = sv.getClass().getMethod("getPath");
					String path = (String) getPath.invoke(sv);
					if (path != null) {
						path += "/";
					}
					Logcat.d(logLevel, ": Path=" + path);

					if (path != null && !paths.contains(path)) {
						paths.add(path);
					}
				}
			}
			catch (Exception e) {
				;
			}
		}
		return paths.toArray(new String[0]);
	}

	public static class FileDataComparator implements Comparator<FileData> {

		@Override
		public int compare(FileData f1, FileData f2) {
			if (f1.getType() != FileData.FILETYPE_DIR && f2.getType() == FileData.FILETYPE_DIR) {
				return -1;
			}
			else if (f1.getType() == FileData.FILETYPE_DIR && f2.getType() != FileData.FILETYPE_DIR) {
				return 1;
			}
			else {
				return DEF.compareFileName(f1.getName(), f2.getName(), DEF.SORT_BY_FILE_TYPE);
			}
		}
	}

}
