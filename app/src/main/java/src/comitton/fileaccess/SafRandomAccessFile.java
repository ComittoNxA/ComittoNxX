package src.comitton.fileaccess;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

public class SafRandomAccessFile {
    private static final String TAG = "SafRandomAccessFile";

    private final Context mContext;
    private final String mURI;
    private final String mMode;
    private ParcelFileDescriptor pfd;
    FileInputStream mFileInputStream = null;
    FileOutputStream mFileOutputStream = null;

    private boolean isInput;
    private long mPos = 0;

    public SafRandomAccessFile(@NonNull final Context context, @NonNull final String uri, @NonNull final String mode) throws FileAccessException {
        mContext = context;
        mURI = uri;
        mMode = mode;
        Open();
    }

    private void Open() throws FileAccessException {
        try {
            pfd = SafFileAccess.openParcelFileDescriptor(mContext, mURI);
            if (pfd == null) {
                Log.e(TAG, "SafRandomAccessFile: エラーが発生しました. ParcelFileDescriptor == null");
                throw new FileAccessException(TAG + "SafRandomAccessFile: エラーが発生しました. ParcelFileDescriptor == null");
            }
            if (mFileInputStream != null) {
                mFileInputStream.close();
            }
            mFileInputStream = new FileInputStream(pfd.getFileDescriptor());
            isInput = true;
            mPos = 0;
        } catch (Exception e) {
            Log.e(TAG, "SafRandomAccessFile: エラーが発生しました. mURI=" + mURI + ", mMode=" + mMode);
            if (e.getLocalizedMessage() != null) {
                Log.e(TAG, "SafRandomAccessFile: エラーメッセージ. " + e.getLocalizedMessage());
            }
            throw new FileAccessException(TAG + "SafRandomAccessFile: エラーが発生しました. " + e.getLocalizedMessage());
        }
    }

    public int read(byte[] b, int off, int size) throws IOException {
        boolean debug = false;
        if(debug) {Log.d(TAG, MessageFormat.format("read: 開始します. pos={0}, off={1}, size={2}, pos+off+size={3}, length={4}", new Object[]{mPos, off, size, mPos+off+size, length()}));}

        if (!isInput) {
            if(debug) {Log.d(TAG, "read: FileInputStreamを作成しなおします.");}
            try {
                Open();
                position((int)mPos);
            } catch (Exception e) {
                Log.e(TAG, "read: エラーが発生しました.");
                if (e.getLocalizedMessage() != null) {
                    Log.e(TAG, "read: エラーメッセージ. " + e.getLocalizedMessage());
                }
                throw new IOException(TAG + ": read: " + e.getLocalizedMessage());
            }
        }

        try {
            mPos += off;
            int ret = mFileInputStream.read(b, off, size);
            if (ret != -1) {
                mPos += ret;
            }
            if(debug) {Log.d(TAG, MessageFormat.format("read: 終了します. ret={0}, off={1}, mPos={2}, length={3}", new Object[]{ret, off, mPos, length()}));}
            return ret;
        } catch (Exception e) {
            Log.e(TAG, "read: エラーが発生しました.");
            if (e.getLocalizedMessage() != null) {
                Log.e(TAG, "read: エラーメッセージ. " + e.getLocalizedMessage());
            }
            throw new IOException(TAG + ": read: " + e.getLocalizedMessage());
        }
    }

    public void write(@NonNull byte[] b, final int off, final int size) throws IOException {
        ;
    }

    public long length() throws IOException {
        return pfd.getStatSize();
    }

    public long getFilePointer() throws IOException {
        return mPos;
    }

    public void seek(final long pos) throws IOException {
        boolean debug = false;
        if(debug) {Log.d(TAG, MessageFormat.format("seek: 開始します. pos={0}, mPos={1}, length={2}", new Object[]{pos, mPos, length()}));}
        try {
            if(debug) {Log.d(TAG, MessageFormat.format("seek: セットします. mPos={0}, length={1}", new Object[]{mPos, length()}));}
            mPos = position((int)pos);
            mPos = position();
        } catch (Exception e) {
            throw new IOException(e);
        }
        if(debug) {Log.d(TAG, MessageFormat.format("seek: 終了します. pos={0}, mPos={1}, length={2}", new Object[]{pos, mPos, length()}));}
    }

    /** PFD のシーク位置を変更します。 */
    long position(final int pos) throws Exception {
        boolean debug = false;
        if(debug) {Log.d(TAG, MessageFormat.format("position: 開始します. pos={0}, mPos={1}, length={2}", new Object[]{pos, mPos, length()}));}
        long result = 0;
        try {
            while (result != pos) {
                result += Os.lseek(pfd.getFileDescriptor(), pos, OsConstants.SEEK_SET);
            }
            if (pos != position()) {
                // マイナスシークをすると正しく見える値を返すのに実際にはシークされていないのでオープンからやり直す
                Open();
                while (result != pos) {
                    result += Os.lseek(pfd.getFileDescriptor(), pos, OsConstants.SEEK_SET);
                }
            }
        } catch (Exception e) {
            // pfd が lseek をサポートしてない場合
            // ESPIPE (Illegal seek) エラーが返される
            Log.w(TAG, "position: エラーが発生しました.");
            if (e.getLocalizedMessage() != null) {
                Log.w(TAG, "position: エラーメッセージ. " + e.getLocalizedMessage());
            }
            try {
                // スキップを実行する
                result = mPos;
                while (result != pos) {
                    result += mFileInputStream.skip(pos - mPos);
                }
            } catch (Exception ex) {
                // pos - mPos がマイナスかつ skip がマイナス方向をサポートしてない場合
                Log.w(TAG, "position: エラーが発生しました.");
                if (ex.getLocalizedMessage() != null) {
                    Log.w(TAG, "position: エラーメッセージ. " + ex.getLocalizedMessage());
                }
                // FileInputStream を再作成してからスキップを実行する
                mFileInputStream.close();
                mFileInputStream = new FileInputStream(pfd.getFileDescriptor());
                result = 0;
                while (result != pos) {
                    result += mFileInputStream.skip(pos);
                }
            }
        }
        if(debug) {Log.d(TAG, MessageFormat.format("position: 終了します. pos={0}, mPos={1}, length={2}", new Object[]{pos, mPos, length()}));}
        return result;
    }

    /** PFD の現在のシーク位置を返却します。 */
    long position() throws Exception {
        try {
            return Os.lseek(pfd.getFileDescriptor(), 0, OsConstants.SEEK_CUR);
        } catch (Exception e) {
            Log.e(TAG, "position: エラーが発生しました.");
            if (e.getLocalizedMessage() != null) {
                Log.e(TAG, "position: エラーメッセージ. " + e.getLocalizedMessage());
            }
            throw e;
        }
    }

    public void close() throws IOException {
        if (mFileInputStream != null) {
            mFileInputStream.close();
            mFileInputStream = null;
        }
        if (mFileOutputStream != null) {
            mFileOutputStream.close();
            mFileOutputStream = null;
        }
        if (pfd != null) {
            // 強制停止のためmFileInputStreamより先にcloseしてみる
            pfd.close();
            pfd = null;
        }
    }

}