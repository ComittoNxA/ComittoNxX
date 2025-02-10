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

@TargetApi(26)
public class SambaProxyFileCallback extends StorageManagerCompat.ProxyFileDescriptorCallbackCompat {
    private static final String TAG = "SambaProxyFileCallback";

    private final Activity mActivity;
    private SmbRandomAccessFile mSmbRandomAccessFile = null;

    public SambaProxyFileCallback(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String user, @NonNull final String pass) {
        boolean debug = false;
        if (debug) {Log.d(TAG, "SambaProxyFileCallback: uri=" + uri + ", user=" + user + ", pass=" + pass);}
        mActivity = activity;
        try {
            mSmbRandomAccessFile = SmbFileAccess.openRandomAccessFile(uri, user, pass, "rw");
        } catch (IOException e) {
            Log.e(TAG,"SambaProxyFileCallback: Constructor Error.");
        }
    }

    @Override
    public long onGetSize() {
        try {
          return mSmbRandomAccessFile.length();
        } catch (IOException e) {
          Log.e(TAG,"onGetSize: Get File Size Error.");
        }
        return 0;
    }

    @Override
    public int onRead(long offset, int size, byte[] data) {
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
                    Log.e(TAG, "read: File read error. " + e.getLocalizedMessage());
                    return 0;
                }
            }
        } catch (IOException e) {
          Log.e(TAG,"onRead: File Read Error.");
        }
      return 0;
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) {
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
                    Log.e(TAG, "read: File read error. " + e.getLocalizedMessage());
                    return 0;
                }
            }
        } catch (IOException e) {
            Log.e(TAG,"onRead: File Read Error.");
        }
        return size;
    }

    @Override
    public void onFsync() {
        // Nothing to do
    }

    @Override
    public void onRelease() {
        // 非同期処理にする
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mSmbRandomAccessFile.close();
                } catch (SmbException e) {
                    Log.e(TAG,"onRelease: File Release Error: " + e.getLocalizedMessage());
                }
            }
        });
    }
}
