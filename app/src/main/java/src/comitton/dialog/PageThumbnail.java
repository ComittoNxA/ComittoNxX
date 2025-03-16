package src.comitton.dialog;

import src.comitton.common.Logcat;
import src.comitton.imageview.ImageManager;
import src.comitton.imageview.ThumbnailView;
import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NewApi")
public class PageThumbnail extends ToolbarDialog implements OnTouchListener,
		OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener {
	// 表示中フラグ
	public static boolean mIsOpened = false;

	// パラメータ
	private ImageManager mImageMgr;
	private long mThumID;

	private HorizontalScrollView mScroll;
	private ThumbnailView mThumView;

	public PageThumbnail(AppCompatActivity activity, @StyleRes int themeResId) {
		super(activity, themeResId);
		mAutoApply = false;
	}

	public void setParams(boolean viwer, int page, boolean reverse, ImageManager imgr, long thumid, boolean dirtree) {
		super.setParams(viwer, page, imgr.length(), reverse, dirtree);
		mPage = page;
		mReverse = reverse;
		mMaxPage = imgr.length();
		mImageMgr = imgr;
		mThumID = thumid;
	}

	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.pagethumbnail);

		mScroll = (HorizontalScrollView) this.findViewById(R.id.scrl_view);
		mThumView = (ThumbnailView) this.findViewById(R.id.thumb_view);
		mThumView.initialize(mPage, mMaxPage, mReverse, mImageMgr, this, mScroll, mThumID, 1);
		mThumView.setOnTouchListener(this);

		// バックグラウンドでのキャッシュ読み込み停止
		mImageMgr.setCacheSleep(true);

		// 表示中フラグ
		mIsOpened = true;

		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "View=" + v + ", event=" + event);
		if (mThumView == v) {
			int action = event.getAction();
			int x = (int) event.getX();
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					Logcat.d(logLevel, "MotionEvent=ACTION_DOWN");
					break;
				case MotionEvent.ACTION_UP:
					Logcat.d(logLevel, "MotionEvent=ACTION_UP");
					// ページ選択
					int page = mThumView.getCurrentPage(x);
					Logcat.d(logLevel, "page=" + page);
					if (page >= 0) {
						mListener.onSelectPage(page);
						dismiss();
					}
					break;
			}
			// trueにするとonTouchのACTION_UPが呼ばれる
			return true;
		}
		// falseにするとonClickが呼ばれる
		return false;
	}

	@Override
	public void onClick(View v) {
		// ボタンクリック
		super.onClick(v);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		// ダイアログ終了
		mThumView.close();
		mIsOpened = false;
		mImageMgr.setCacheSleep(false);
	}

	public void onScrollChanged(int pos) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "calcProgress(mSeekPage.getProgress())=" + calcProgress(mSeekPage.getProgress()) + ", pos=" + pos);
		if (calcProgress(mSeekPage.getProgress()) != pos) {
			setProgress(pos, true);
		}
	}

	protected void setProgress(int pos, boolean fromThumb) {
		super.setProgress(pos, fromThumb);
		if (!fromThumb) {
			mThumView.setPosition(pos);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int page, boolean fromUser) {
		// 変更
		if (fromUser) {
		int cnvpage = calcProgress(page);
		mThumView.setPosition(cnvpage);
		}
	}
}