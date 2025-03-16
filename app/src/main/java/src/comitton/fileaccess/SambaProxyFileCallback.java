package src.comitton.fileaccess;

import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;

@TargetApi(26)
public class SambaProxyFileCallback extends StorageManagerCompat.ProxyFileDescriptorCallbackCompat {
    private static final String TAG = "SambaProxyFileCallback";

    private final Activity mActivity;
    private SmbRandomAccessFile mSmbRandomAccessFile = null;

    public SambaProxyFileCallback(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "uri=" + uri + ", user=" + user + ", pass=" + pass);
        mActivity = activity;
        try {
            mSmbRandomAccessFile = SmbFileAccess.openRandomAccessFile(uri, user, pass, "rw");
        } catch (IOException e) {
            Logcat.e(logLevel, "Constructor Error.", e);
        }
    }

    @Override
    public long onGetSize() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
          return mSmbRandomAccessFile.length();
        } catch (IOException e) {
          Logcat.e(logLevel,"Get File Size Error.", e);
        }
        return 0;
    }

    @Override
    public int onRead(long offset, int size, byte[] data) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            if (!DEF.isUiThread()) {
                // UIスレッドではない時はそのまま実行
                mSmbRandomAccessFile.seek(offset);
                return mSmbRandomAccessFile.read(data, 0, size);
            } else {
                // UIスレッドの時は新しいスレッド内で実行
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Integer> future = executor.submit(new Callable<Integer>() {

                    @Override
                    public Integer call() throws SmbException {
                        mSmbRandomAccessFile.seek(offset);
                        return mSmbRandomAccessFile.read(data, 0, size);
                    }
                });

                try {
                    return future.get();
                } catch (Exception e) {
                    Logcat.e(logLevel, "File read error. ", e);
                    return 0;
                }
            }
        } catch (IOException e) {
          Logcat.e(logLevel,"File Read Error.", e);
        }
      return 0;
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        try {
            if (!DEF.isUiThread()) {
                // UIスレッドではない時はそのまま実行
                mSmbRandomAccessFile.seek(offset);
                mSmbRandomAccessFile.write(data, 0, size);
            } else {
                // UIスレッドの時は新しいスレッド内で実行
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Integer> future = executor.submit(new Callable<Integer>() {

                    @Override
                    public Integer call() throws SmbException {
                        mSmbRandomAccessFile.seek(offset);
                        mSmbRandomAccessFile.write(data, 0, size);
                        return size;
                    }
                });

                try {
                    return future.get();
                } catch (Exception e) {
                    Logcat.e(logLevel, "File read error. ", e);
                    return 0;
                }
            }
        } catch (IOException e) {
            Logcat.e(logLevel, "File Read Error.", e);
        }
        return size;
    }

    @Override
    public void onFsync() {
        // Nothing to do
    }

    @Override
    public void onRelease() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        // 非同期処理にする
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mSmbRandomAccessFile.close();
                } catch (SmbException e) {
                    Logcat.e(logLevel, "File Release Error.", e);
                }
            }
        });
    }
}
