package src.comitton.dialog;

import src.comitton.listener.PageSelectListener;
import src.comitton.stream.ImageManager;
import src.comitton.view.image.ThumbnailView;
import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressLint("NewApi")
public class PageThumbnail extends Dialog implements OnTouchListener,
		OnSeekBarChangeListener, DialogInterface.OnDismissListener {
	// 表示中フラグ
	public static boolean mIsOpened = false;

	private PageSelectListener mListener = null;
	Activity mContext;

	private int mPage;
	private int mMaxPage;
	private boolean mReverse;
	private ImageManager mImageMgr;
	private long mThumID;

	private SeekBar mSeekPage;
	private HorizontalScrollView mScroll;
	private ThumbnailView mThumView;

	public PageThumbnail(Activity context) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		//PaintDrawable paintDrawable = new PaintDrawable(0x00000000);
		//dlgWindow.setBackgroundDrawable(paintDrawable);
		dlgWindow.setBackgroundDrawableResource(android.R.color.transparent);

		// 画面下に表示
		WindowManager.LayoutParams wmlp = dlgWindow.getAttributes();
		wmlp.gravity = Gravity.BOTTOM;
		wmlp.y=(int)(30 * context.getResources().getDisplayMetrics().density);
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);

		mContext = context;

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// ダイアログ終了通知を受け取る
		setOnDismissListener(this);
		mIsOpened = true;
	}

	public void setParams(int page, boolean reverse, ImageManager imgr, long thumid) {
		mPage = page;
		mReverse = reverse;
		mMaxPage = imgr.length();
		mImageMgr = imgr;
		mThumID = thumid;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pagethumbnail);

		// 一度ダイアログを表示すると画面回転時に呼び出される
		Window win = getWindow();
		WindowManager.LayoutParams lpCur = win.getAttributes();
		WindowManager.LayoutParams lpNew = new WindowManager.LayoutParams();
		lpNew.copyFrom(lpCur);
		lpNew.width = WindowManager.LayoutParams.FILL_PARENT;
		lpNew.height = WindowManager.LayoutParams.WRAP_CONTENT;
		win.setAttributes(lpNew);

		mScroll = (HorizontalScrollView) this.findViewById(R.id.scrl_view);
		mThumView = (ThumbnailView) this.findViewById(R.id.thumb_view);
		mThumView.initialize(mPage, mMaxPage, mReverse, mImageMgr, this, mScroll, mThumID, 1);
		mThumView.setOnTouchListener(this);

		mSeekPage = (SeekBar) findViewById(R.id.seek_page);
		mSeekPage.setMax(mMaxPage - 1);
		setProgress(mPage);
		mSeekPage.setOnSeekBarChangeListener(this);

		// バックグラウンドでのキャッシュ読み込み停止
		mImageMgr.setCacheSleep(true);
	}

	public void setPageSelectListear(PageSelectListener listener) {
		mListener = listener;
	}

	// ダイアログを表示してもIMMERSIVEが解除されない方法
	// http://stackoverflow.com/questions/22794049/how-to-maintain-the-immersive-mode-in-dialogs
	/**
	 * An hack used to show the dialogs in Immersive Mode (that is with the NavBar hidden). To
	 * obtain this, the method makes the dialog not focusable before showing it, change the UI
	 * visibility of the window like the owner activity of the dialog and then (after showing it)
	 * makes the dialog focusable again.
	 */
	@Override
	public void show() {
		// Set the dialog to not focusable.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		// 設定をコピー
		copySystemUiVisibility();

		// Show the dialog with NavBar hidden.
		super.show();

		// Set the dialog to focusable again.
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	/**
	 * Copy the visibility of the Activity that has started the dialog {@link mActivity}. If the
	 * activity is in Immersive mode the dialog will be in Immersive mode too and vice versa.
	 */
	@SuppressLint("NewApi")
	private void copySystemUiVisibility() {
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
	        getWindow().getDecorView().setSystemUiVisibility(
	                mContext.getWindow().getDecorView().getSystemUiVisibility());
	    }
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		int x = (int) event.getX();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_UP:
				// ページ選択
				int page = mThumView.getCurrentPage(x);
				if (page >= 0) {
					mListener.onSelectPage(page);
					dismiss();
				}
				break;
		}
		return true;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// ダイアログ終了
		mThumView.close();
		mIsOpened = false;
		mImageMgr.setCacheSleep(false);
	}

	public void onScrollChanged(int pos) {
		if (calcProgress(mSeekPage.getProgress()) != pos) {
			setProgress(pos);
		}
	}

	private void setProgress(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		} else {
			convpos = mSeekPage.getMax() - pos;
		}
		mSeekPage.setProgress(convpos);
	}

	private int calcProgress(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		} else {
			convpos = mSeekPage.getMax() - pos;
		}
		return convpos;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int page, boolean fromUser) {
		// 変更
		if (fromUser) {
			int cnvpage = calcProgress(page);
			mThumView.setPosition(cnvpage);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// 開始
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// 終了

	}
}