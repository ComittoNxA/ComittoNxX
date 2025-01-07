package src.comitton.stream;

import android.annotation.TargetApi;
import android.util.Log;

import java.io.IOException;

@TargetApi(26)
public class SambaProxyFileCallback extends StorageManagerCompat.ProxyFileDescriptorCallbackCompat {
    private static final String TAG = "SambaProxyFileCallback";

    private WorkStream mWorkStream = null;

    public SambaProxyFileCallback(String uri, String user, String pass) {
        try {
            mWorkStream = new WorkStream(uri, "", user, pass, false);
        } catch (IOException e) {
            Log.e(TAG,"SambaProxyFileCallback: Constructor Error.");
        }
    }

    @Override
    public long onGetSize() {
        try {
          return mWorkStream.length();
        } catch (IOException e) {
          Log.e(TAG,"onGetSize: Get File Size Error.");
        }
        return 0;
    }

    @Override
    public int onRead(long offset, int size, byte[] data) {
        try {
            mWorkStream.seek(offset);
            return mWorkStream.read(data, 0, size);
        } catch (IOException e) {
          Log.e(TAG,"onRead: File Read Error.");
        }
      return 0;
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) {
        return 0;
    }

    @Override
    public void onFsync() {
        // Nothing to do
    }

    @Override
    public void onRelease() {
        try {
            mWorkStream.close();
        } catch (IOException e) {
          Log.e(TAG,"onRelease: File Release Error.");
        }
    }
}
