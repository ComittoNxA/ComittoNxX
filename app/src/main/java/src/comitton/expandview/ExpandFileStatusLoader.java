package src.comitton.expandview;

import android.content.SharedPreferences;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.common.ThumbnailLoader;
import src.comitton.common.WaitFor;
import src.comitton.config.SetEpubActivity;
import src.comitton.fileview.data.FileData;
import src.comitton.imageview.ImageManager;
import src.comitton.textview.TextManager;

public class ExpandFileStatusLoader extends ThumbnailLoader implements Runnable {
    private static final String TAG = "FileStatusLoader";

    private static SharedPreferences mSp;

    private final String mCmpFile;
    private String mUser;
    private String mPass;
    private final boolean mHidden;

    private ImageManager mImageMgr;
    private TextManager mTextMgr;
    private final FileRangeComparator mComparator;
    private FileData mCurrentFile;

    private WaitFor mWaitFor;
    private boolean mSkip;

    public ExpandFileStatusLoader(AppCompatActivity activity, ImageManager imgMgr, String uri, String path, String cmpFile, String user, String pass, Handler handler, ArrayList<FileData> files, boolean hidden) {
        super(activity, uri, path, handler, 0, files, 0, 0, 0, 0, 0);
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel, "開始します.");

        mSp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSkip = false;

        mImageMgr = imgMgr;
        mCmpFile = cmpFile;
        mUser = user;
        mPass = pass;
        mHidden = hidden;

        mComparator = new FileRangeComparator();
        mComparator.setDispRange(mFirstIndex, mLastIndex);

        for (int i = 0; i < files.size(); ++i) {
            // 削除したらindexが変わるのでfilesで検査してmFilesから削除する
            if (files.get(i).getType() == FileData.FILETYPE_IMG) {
                removeFile(files.get(i));
            }
        }

        readTextConfig();

        mWaitFor = new WaitFor(60000);

        // スレッド起動
        Thread thread = new Thread(this);
        thread.start();
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

    // TextManager を解放する
    private void releaseManager() {
        // 意図的にExceptionを発生させるためスレッドセーフにしない
        if (mTextMgr != null) {
            mTextMgr.release();
            mTextMgr = null;
        }
    }

    protected void interruptThread() {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel,"開始します.");
        mSkip = true;
        releaseManager();
        if (mWaitFor != null) {
            mWaitFor.interrupt();
        }
    }

    public void update(FileData file) {

        if (!mFiles.contains(file)) {
            mFiles.add(file);
        }

        // 中でループさせたいので非同期処理にする
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (mFileListLock) {
                    if (mFiles != null && !mFiles.isEmpty()) {
                        while (true) {
                            // ソートが成功するまでループする
                            try {
                                Collections.sort(mFiles, mComparator);
                                break;
                            } catch (ConcurrentModificationException e) {
                                continue;
                            }
                        }
                        if (mComparator.compare(mFiles.get(0), mCurrentFile) < 0) {
                            interruptThread();
                        }
                    }
                }
            }
        });
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
                            if (mComparator.compare(mFiles.get(0), mCurrentFile) < 0) {
                                interruptThread();
                            }
                        }
                    }
                }
            });
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
            mSkip = false;
            mCurrentFile = null;
            synchronized (mFileListLock) {
                if (mFiles != null && !mFiles.isEmpty()) {
                    mCurrentFile = mFiles.get(0);
                }
            }
            if (mCurrentFile == null) {
                // ファイルがなくなったら新たにファイルが挿入されるまで待機する
                mWaitFor.sleep();
                continue;
            }

            boolean retCode = saveState(mCurrentFile);

            if (retCode) {
                DEF.sendMessage(mHandler, DEF.HMSG_FILE_STATUS, mCurrentFile.getIndex(), 0, mCurrentFile.getName());
            }

            if (!mSkip) {
                removeFile(mCurrentFile);
            }
        }
    }

    /**
     * 既読情報に変更が必要なら変更する
     * @param fileData
     * @return 既読情報に変化があったらtrue、それ以外はfalse
     */
    private boolean saveState(FileData fileData) {
        int logLevel = Logcat.LOG_LEVEL_WARN;
        Logcat.d(logLevel,"開始します. fileData=" + fileData.getName());

        if (fileData.getType() == FileData.FILETYPE_IMG){
            return false;
        }

        int maxpage;
        int state;
        long date;
        long nowdate;

        String currentPath = DEF.relativePath(mActivity, mURI, mPath);
        String name = fileData.getName();
        String uri = DEF.createUrl(DEF.relativePath(mActivity, currentPath, mCmpFile), mUser, mPass);
        Logcat.v(logLevel,"開始します. uri=" + uri);

        SharedPreferences.Editor ed = mSp.edit();
        String dateKey = uri + "#date";
        String maxpageKey;
        String stateKey;
        String textFile;

        switch (fileData.getType()) {
            case FileData.FILETYPE_TXT:
                maxpageKey = uri + name + "#maxpage";
                stateKey = uri + name;
                textFile = name;
                break;
            case FileData.FILETYPE_EPUB_SUB:
                maxpageKey = uri + "META-INF/container.xml" + "#maxpage";
                stateKey = uri + "META-INF/container.xml";
                textFile = "META-INF/container.xml";
                break;
            default:
                // なにもしない
                return false;
        }

        maxpage = mSp.getInt(maxpageKey, DEF.PAGENUMBER_NONE);
        state = mSp.getInt(stateKey, DEF.PAGENUMBER_UNREAD);
        Logcat.v(logLevel,"開始します. maxpage=" + maxpage + ", state=" + state);
        try {
            if (state != DEF.PAGENUMBER_UNREAD) {
                // 未読じゃなければ
                nowdate = mSp.getInt(dateKey, 0);
                date = fileData.getDate();
                if (nowdate != date / 1000 || maxpage == DEF.PAGENUMBER_NONE) {
                    // タイムスタンプが変更されているか、maxpageが保存されていなければ
                    mTextMgr = new TextManager(mImageMgr, textFile, mUser, mPass, mHandler, mActivity, fileData.getType());
                    mTextMgr.formatTextFile(mTextWidth, mTextHeight, mHeadSize, mBodySize, mRubiSize, mSpaceW, mSpaceH, mMarginW, mMarginH, mPicSize, mFontFile, mAscMode);
                    maxpage = mTextMgr.length();

                    if (mTextMgr == null) {
                        // 結果が壊れている可能性あり
                        return false;
                    }

                    releaseManager();

                    ed.putInt(maxpageKey, maxpage);
                    ed.putInt(dateKey, (int) ((date / 1000)));
                    ed.apply();
                }
                if (state == DEF.PAGENUMBER_READ && maxpage >= 0) {
                    ed.putInt(stateKey, Math.max(maxpage - 1, 0));
                    ed.apply();
                }

                return true;
            }
        }
        catch (Exception e) {
            Logcat.w(logLevel, "処理が中断されました. ", e);
        }
        finally {
            releaseManager();
        }
        return false;
    }

}
