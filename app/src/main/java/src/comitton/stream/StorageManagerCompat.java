package src.comitton.stream;

/**
 * No Licenses. Use it in your favourite way.
 */

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.ProxyFileDescriptorCallback;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;

/**
 * @author Fung Gwo (fythonx@gmail.com)
 */
public final class StorageManagerCompat {

    private final static String TAG = StorageManagerCompat.class.getSimpleName();

    @NonNull
    public static final StorageManagerCompat from(@NonNull Context context) {
        return new StorageManagerCompat(context);
    }

    private StorageManager mStorageManager;

    private StorageManagerCompat(@NonNull Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
    }

    @NonNull
    public ParcelFileDescriptor openProxyFileDescriptor(int mode, @NonNull ProxyFileDescriptorCallbackCompat callback, @NonNull Handler handler) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mStorageManager.openProxyFileDescriptor(
                    mode,
                    callback.toAndroidOsProxyFileDescriptorCallback(),
                    handler
            );
        } else {
            if (ParcelFileDescriptor.MODE_READ_ONLY != mode
                    && ParcelFileDescriptor.MODE_WRITE_ONLY != mode) {
                throw new UnsupportedOperationException("Mode " + mode + " is not supported");
            }

            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createReliablePipe();
            switch (mode) {
                case ParcelFileDescriptor.MODE_READ_ONLY : {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try (final ParcelFileDescriptor.AutoCloseOutputStream os =
                                         new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])) {
                                int size = (int) callback.onGetSize();
                                byte[] buf = new byte[size];

                                callback.onRead(0, size, buf);
                                os.write(buf);
                                callback.onRelease();
                            } catch (IOException | ErrnoException e) {
                                Log.e(TAG, "Failed to read file.", e);

                                try {
                                    pipe[1].closeWithError(e.getMessage());
                                } catch (IOException exc) {
                                    Log.e(TAG, "Can't even close PFD with error.", exc);
                                }
                            }
                        }
                    });
                    return pipe[0];
                }
                case ParcelFileDescriptor.MODE_WRITE_ONLY : {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try (final ParcelFileDescriptor.AutoCloseInputStream is =
                                         new ParcelFileDescriptor.AutoCloseInputStream(pipe[0])) {
                                byte[] buf = new byte[is.available()];
                                is.read(buf);

                                callback.onWrite(0, buf.length, buf);
                                callback.onRelease();
                            } catch (IOException | ErrnoException e) {
                                Log.e(TAG, "Failed to write file.", e);

                                try {
                                    pipe[0].closeWithError(e.getMessage());
                                } catch (IOException exc) {
                                    Log.e(TAG, "Can't even close PFD with error.", exc);
                                }
                            }
                        }
                    });
                    return pipe[1];
                }
                default: {
                    // Should never happen.
                    pipe[0].close();
                    pipe[1].close();
                    throw new UnsupportedOperationException("Mode " + mode + " is not supported.");
                }
            }
        }
    }

    public static abstract class ProxyFileDescriptorCallbackCompat {

        /**
         * Returns size of bytes provided by the file descriptor.
         * @return Size of bytes.
         * @throws ErrnoException ErrnoException containing E constants in OsConstants.
         */
        public long onGetSize() throws ErrnoException {
            throw new ErrnoException("onGetSize", OsConstants.EBADF);
        }

        /**
         * Provides bytes read from file descriptor.
         * It needs to return exact requested size of bytes unless it reaches file end.
         * @param offset Offset in bytes from the file head specifying where to read bytes. If a seek
         * operation is conducted on the file descriptor, then a read operation is requested, the
         * offset refrects the proper position of requested bytes.
         * @param size Size for read bytes.
         * @param data Byte array to store read bytes.
         * @return Size of bytes returned by the function.
         * @throws ErrnoException ErrnoException containing E constants in OsConstants.
         */
        public int onRead(long offset, int size, byte[] data) throws ErrnoException {
            throw new ErrnoException("onRead", OsConstants.EBADF);
        }

        /**
         * Handles bytes written to file descriptor.
         * @param offset Offset in bytes from the file head specifying where to write bytes. If a seek
         * operation is conducted on the file descriptor, then a write operation is requested, the
         * offset refrects the proper position of requested bytes.
         * @param size Size for write bytes.
         * @param data Byte array to be written to somewhere.
         * @return Size of bytes processed by the function.
         * @throws ErrnoException ErrnoException containing E constants in OsConstants.
         */
        public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
            throw new ErrnoException("onWrite", OsConstants.EBADF);
        }

        /**
         * Ensures all the written data are stored in permanent storage device.
         * For example, if it has data stored in on memory cache, it needs to flush data to storage
         * device.
         * @throws ErrnoException ErrnoException containing E constants in OsConstants.
         */
        public void onFsync() throws ErrnoException {
            throw new ErrnoException("onFsync", OsConstants.EINVAL);
        }

        /**
         * Invoked after the file is closed.
         */
        abstract void onRelease();

        @RequiresApi(Build.VERSION_CODES.O)
        @NonNull
        ProxyFileDescriptorCallback toAndroidOsProxyFileDescriptorCallback() {
            return new ProxyFileDescriptorCallback() {
                @Override
                public int onRead(long offset, int size, byte[] data) throws ErrnoException {
                    return ProxyFileDescriptorCallbackCompat.this.onRead(offset, size, data);
                }

                @Override
                public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
                    return ProxyFileDescriptorCallbackCompat.this.onWrite(offset, size, data);
                }

                @Override
                public long onGetSize() throws ErrnoException {
                    return ProxyFileDescriptorCallbackCompat.this.onGetSize();
                }
                @Override
                public void onFsync() throws ErrnoException {
                    ProxyFileDescriptorCallbackCompat.this.onFsync();
                }

                @Override
                public void onRelease() {
                    ProxyFileDescriptorCallbackCompat.this.onRelease();
                }
            };
        }

    }

}
