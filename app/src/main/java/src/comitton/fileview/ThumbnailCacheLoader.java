package src.comitton.fileview;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.common.Logcat;
import src.comitton.common.ThumbnailLoader;
import src.comitton.common.WaitFor;
import src.comitton.config.SetEpubActivity;
import src.comitton.fileview.data.FileData;
import src.comitton.imageview.ImageManager;
import src.comitton.jni.CallImgLibrary;
import src.comitton.textview.TextManager;

public class ThumbnailCacheLoader extends ThumbnailLoader implements Runnable {
    private static final String TAG = "ThumbnailCacheLoader";

    private static SharedPreferences mSp;

    private FileThumbnailLoader mFileThumbnailLoader;
    private FileRangeComparator mComparator;
    private FileData mCurrentFile;

    private WaitFor mWaitFor;
    private boolean mSkip;
    private boolean mSleep;

    public ThumbnailCacheLoader(AppCompatActivity activity, FileThumbnailLoader loader, String uri, String path, String user, String pass, Handler handler, long id, ArrayList<FileData> files, int sizeW, int sizeH, int cachenum, boolean hidden, int crop, int margin, boolean epubViewer) {
        super(activity, uri, path, handler, id, files, sizeW, sizeH, cachenum, crop, margin);
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "開始します.");

        mSp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSkip = false;
        mSleep = false;

        mFileThumbnailLoader = loader;
        mComparator = new FileRangeComparator();
        mComparator.setDispRange(mFirstIndex, mLastIndex);

        mWaitFor = new WaitFor(60000);

        // スレッド起動
        Logcat.d(logLevel, "スレッドを開始します.");
        Thread thread = new Thread(this);
        thread.start();
        Logcat.d(logLevel, "終了します.");
    }

    // 解放
    public void releaseThumbnail() {
    }

    // スレッド停止
    public void breakThread() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "開始します.");
        super.breakThread();
    }

    protected void interruptThread() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel,"開始します.");
        mSkip = true;
        if (mWaitFor != null) {
            mWaitFor.interrupt();
        }
    }

    // 表示中の範囲を設定
    public void setDispRange(int firstindex, int lastindex) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel,"開始します. firstindex=" + firstindex + ", lastindex=" + lastindex);

        if (mFirstIndex != firstindex || mLastIndex != lastindex) {
            // スクロール位置に変化があったら実行する
            mFirstIndex = firstindex;
            mLastIndex = lastindex;
            mComparator.setDispRange(mFirstIndex, mLastIndex);

            if (CallImgLibrary.ThumbnailCheckAll(mID) != 0) {
                // メモリキャッシュに全部入り切れていない時は実行する

                // 中でループさせたいので非同期処理にする
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mFileListLock) {
                            if (mFiles != null && !mFiles.isEmpty()) {
                                while(true) {
                                    // ソートが成功するまでループする
                                    try {
                                        Collections.sort(mFiles, mComparator);
                                        break;
                                    } catch (ConcurrentModificationException e) {
                                        continue;
                                    }
                                }
                                interruptThread();
                            }
                        }
                    }
                });
            }
        }
        Logcat.d(logLevel,"終了します.");
    }

    // スレッド開始
    public void run() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "開始します. mPath=" + mPath);

        if (mFiles.isEmpty()) {
            return;
        }

        while(!mThreadBreak) {
            Logcat.d(logLevel,"while: 開始します. mFirstIndex=" + mFirstIndex + ", mLastIndex=" + mLastIndex);

            mSleep = false;
            mSkip = false;

            for (int i = 0; i < mFiles.size() && !mThreadBreak && !mSkip; ++i) {
                Logcat.d(logLevel,"while: for(" + i + " < "+ mFiles.size() + "): 開始します.");

                if (CallImgLibrary.ThumbnailCheckAll(mID) == 0) {
                    // メモリキャッシュに全部入り切れたら
                    // ファイルキャッシュが更新されるまで待機する
                    mSleep = true;
                    break;
                }

                boolean isChanged;
                mCurrentFile = mFiles.get(i);

                Logcat.d(logLevel,"while: for(" + i + "): index=" + mCurrentFile.getIndex() + ", mCurrentFile=" + mCurrentFile.getName());
                if (CallImgLibrary.ThumbnailCheck(mID, mCurrentFile.getIndex()) > 0) {
                    // 既に読み込み済み
                    Logcat.d(logLevel,"while: for(" + i + "): メモリキャッシュに格納されています. index=" + mCurrentFile.getIndex() + ", mCurrentFile=" + mCurrentFile.getName());
                    continue;
                }

                try {
                    isChanged = loadCache(mCurrentFile);
                }
                catch (CacheException e) {
                    Logcat.d(logLevel,"while: for(" + i + "): CacheException: ループを抜けます.  index=" + mCurrentFile.getIndex() + ", mCurrentFile=" + mCurrentFile.getName());
                    mSleep = true;
                    break;
                }

                Logcat.d(logLevel,"while: for(" + i + "): isChanged=" + isChanged + ", index=" + mCurrentFile.getIndex() + ", mCurrentFile=" + mCurrentFile.getName());

                if (isChanged) {
                    Logcat.d(logLevel,"while: for(" + i + "): 変更を通知します. index=" + mCurrentFile.getIndex() + ", mCurrentFile=" + mCurrentFile.getName());
                    DEF.sendMessage(mHandler, DEF.HMSG_THUMBNAIL, mCurrentFile.getIndex(), 0, mCurrentFile.getName());
                }

                if (i >= mFiles.size() - 1) {
                    mSleep = true;
                    break;
                }

                Logcat.d(logLevel,"while: for(" + i + "): 次のファイルを実行します. index=" + mCurrentFile.getIndex() + ", mCurrentFile=" + mCurrentFile.getName());
            }

            if (mSleep) {
                // メモリキャッシュが空けられなかったかファイルを最後まで処理したら
                // スクロールするかファイルキャッシュが更新されるまで待機する
                Logcat.d(logLevel,"while: スリープします.");
                mWaitFor.sleep();
                Logcat.d(logLevel,"while: スリープを解除します.");
            }
            Logcat.d(logLevel,"while: 先頭に戻ります.");
        }

    }

    /**
     * サムネイルのファイルキャッシュが存在すればメモリキャッシュに格納する
     * @param fileData
     * @return メモリキャッシュに変動があればtrue、それ以外はfalse
     * @exception CacheException メモリキャッシュが空けられなかった場合
     */
    private boolean loadCache(FileData fileData) throws CacheException {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "開始します. fileData=" + fileData.getName());

        String uri = DEF.relativePath(mActivity, mURI, mPath, fileData.getName());
        String pathcode = DEF.makeCode(uri, mThumbSizeW, mThumbSizeH);
        Bitmap bm;
        Logcat.d(logLevel, "uri=" + uri);

        // キャッシュから読込
        if (checkThumbnailCache(pathcode)) {
            bm = loadThumbnailCache(pathcode);
            if (bm == null) {
                Logcat.d(logLevel,"ファイルキャッシュが空でした.スキップします. fileData=" + fileData.getName());
                return false;
            }
            else {
                Logcat.d(logLevel,"ファイルキャッシュがありました. fileData=" + fileData.getName());
                if (logLevel <= Logcat.LOG_LEVEL_DEBUG) {
                    bm = ImageAccess.setText(bm, String.valueOf(fileData.getIndex()), Color.RED, DEF.ALIGN_TOP);
                }
                return loadMemory(fileData, bm);
            }
        }

        return false;
    }

    /**
     * ビットマップデータをメモリキャッシュに格納する
     * @param fileData
     * @param bm
     * @return メモリキャッシュに変動があればtrue、それ以外はfalse
     * @exception CacheException メモリキャッシュが空けられなかった場合
     */
    public boolean loadMemory(FileData fileData, Bitmap bm) throws CacheException {
        if (mFirstIndex <= fileData.getIndex() && fileData.getIndex() <= mLastIndex) {
            return mFileThumbnailLoader.loadMemory(fileData.getIndex(), mThumbSizeW, mThumbSizeH, bm, true);
        }
        else {
            return mFileThumbnailLoader.loadMemory(fileData.getIndex(), mThumbSizeW, mThumbSizeH, bm, false);
        }
    }

}
