package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import jp.dip.muracoro.comittonx.R;
import src.comitton.listener.ChapterPageSelectListener;
import src.comitton.listener.PageSelectListener;

@SuppressLint("NewApi")
public class ChapterPageSelectDialog extends Dialog implements Handler.Callback, OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener, OnEditorActionListener {
	// 表示中フラグ
	public static boolean mIsOpened = false;

	private final int HMSG_PAGESELECT = 5001;
	private final int HMSG_CHAPTERSELECT = 5002;
	private final int TERM_PAGESEELCT = 100;
	private final int TERM_CHAPTERSEELCT = 101;

	private ChapterPageSelectListener mListener = null;
	private Activity mContext;

	// パラメータ
	private int mChapter;
	private int mMaxChapter;
	private int mPage;
	private int mMaxPage;
	private boolean mReverse;
	private boolean mAutoApply;

	// OKを押して終了のフラグ
	private boolean mIsCancel;
	private Object mObject;
	private Handler mHandler;

	private SeekBar mSeekChapter;
	private SeekBar mSeekPage;
	private EditText mEditChapter;
	private EditText mEditPage;
	private Button mBtnAdd100;
	private Button mBtnAdd10;
	private Button mBtnAdd1;
	private Button mBtnSub100;
	private Button mBtnSub10;
	private Button mBtnSub1;
	private Button mBtnCancel;
	private Button mBtnOK;
	private Button mBtnChapterSearch;

	public ChapterPageSelectDialog(Activity context, boolean immmode) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		PaintDrawable paintDrawable = new PaintDrawable(0x80000000);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		// 画面下に表示
		WindowManager.LayoutParams wmlp = dlgWindow.getAttributes();
		wmlp.gravity = Gravity.BOTTOM;
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mContext = context;
		mIsCancel = false;

		// ダイアログ終了通知設定
		setOnDismissListener(this);

		mHandler = new Handler(this);

		// 表示中フラグ
		mIsOpened = true;
	}

	public void setParams(int chapter, int maxchapter, int page, int maxpage, boolean reverse) {
		mChapter = chapter;
		mMaxChapter = maxchapter;
		mPage = page;
		mMaxPage = maxpage;
		mReverse = reverse;
		mAutoApply = true;
//		mIsFirst = true;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chapterpageselect);

		// 一度ダイアログを表示すると画面回転時に呼び出される
		TextView maxchapter = (TextView) findViewById(R.id.text_maxchapter);
		maxchapter.setText("" + mMaxChapter);
		TextView maxpage = (TextView) findViewById(R.id.text_maxpage);
		maxpage.setText("" + mMaxPage);

        Window win = getWindow();
        WindowManager.LayoutParams lpCur = win.getAttributes();
        WindowManager.LayoutParams lpNew = new WindowManager.LayoutParams();
        lpNew.copyFrom(lpCur);
        lpNew.width = WindowManager.LayoutParams.FILL_PARENT;
        lpNew.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lpNew);

		mSeekChapter = (SeekBar) findViewById(R.id.seek_chapter);
		mSeekChapter.setMax(mMaxChapter - 1);
		setProgressChapter(mChapter);
		mSeekChapter.setOnSeekBarChangeListener(this);

		mSeekPage = (SeekBar) findViewById(R.id.seek_page);
		mSeekPage.setMax(mMaxPage - 1);
		setProgressPage(mPage);
		mSeekPage.setOnSeekBarChangeListener(this);

		mEditChapter = (EditText) findViewById(R.id.edit_chapter);
		mEditChapter.setInputType(InputType.TYPE_CLASS_NUMBER);
		String chapterStr = "" + (mChapter + 1);
		mEditChapter.setText(chapterStr);
		mEditChapter.setSelection(chapterStr.length());
		mEditChapter.setOnEditorActionListener(this);

		mEditPage = (EditText) findViewById(R.id.edit_page);
		mEditPage.setInputType(InputType.TYPE_CLASS_NUMBER);
		String pageStr = "" + (mPage + 1);
		mEditPage.setText(pageStr);
		mEditPage.setSelection(pageStr.length());
		mEditPage.setOnEditorActionListener(this);

		mBtnAdd100 = (Button) this.findViewById(R.id.btn_add100);
		mBtnAdd10  = (Button) this.findViewById(R.id.btn_add10);
		mBtnAdd1   = (Button) this.findViewById(R.id.btn_add1);
		mBtnSub100 = (Button) this.findViewById(R.id.btn_sub100);
		mBtnSub10  = (Button) this.findViewById(R.id.btn_sub10);
		mBtnSub1   = (Button) this.findViewById(R.id.btn_sub1);
		mBtnCancel = (Button) this.findViewById(R.id.btn_cancel);
		mBtnOK     = (Button) this.findViewById(R.id.btn_ok);
		mBtnChapterSearch = (Button) this.findViewById(R.id.btn_chapter_search);
		mBtnAdd100.setOnClickListener(this);
		mBtnAdd10.setOnClickListener(this);
		mBtnAdd1.setOnClickListener(this);
		mBtnSub100.setOnClickListener(this);
		mBtnSub10.setOnClickListener(this);
		mBtnSub1.setOnClickListener(this);
		if (mBtnCancel != null) {
			mBtnCancel.setOnClickListener(this);
		}
		mBtnOK.setOnClickListener(this);
		mBtnChapterSearch.setOnClickListener(this);
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

	public void setChapterPageSelectListear(ChapterPageSelectListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		// ボタンクリック
		String text = mEditPage.getText().toString();
		int page = 0;
		try {
			page = Integer.parseInt(text) - 1;
		}
		catch (NumberFormatException e) {
			;
		}

		if (mBtnChapterSearch == v) {
			mListener.onChapterSearch();
		}
		else if (mBtnOK == v) {
			if (page < 0) {
				page = 0;
			}
			else if(page >= mMaxPage){
				page = mMaxPage - 1;
			}
			// 選択して終了
			mListener.onSelectPage(page);
			setProgressPage(page);
			dismiss();
			return;
		}
		else if (mBtnCancel == v) {
			// ダイアログ終了
			mIsCancel = true;
			dismiss();
			return;
		}
		else if (mBtnAdd100 == v) {
			page += 100;
		}
		else if (mBtnAdd10 == v) {
			page += 10;
		}
		else if (mBtnAdd1 == v) {
			page += 1;
		}
		else if (mBtnSub100 == v) {
			page -= 100;
		}
		else if (mBtnSub10 == v) {
			page -= 10;
		}
		else if (mBtnSub1 == v) {
			page -= 1;
		}
		if (page < 0) {
			page = 0;
		}
		else if(page >= mMaxPage){
			page = mMaxPage - 1;
		}
		String pageStr = "" + (page + 1);
		mEditPage.setText(pageStr);
		mEditPage.setSelection(pageStr.length());

		// 設定と通知
		if (mAutoApply) {
			mListener.onSelectPage(page);
		}
		setProgressPage(page);
	}

	private void setProgressChapter(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekChapter.getMax() - pos;
		}
		mSeekChapter.setProgress(convpos);
	}

	private void setProgressPage(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		mSeekPage.setProgress(convpos);
	}

	private int calcProgressChapter(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekChapter.getMax() - pos;
		}
		return convpos;
	}

	private int calcProgressPage(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		return convpos;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int page, boolean fromUser) {
		Log.d("ChapterPageSelectDialog", "onProgressChanged: 開始します.");
		// 変更済み
		if (mSeekChapter == seekBar) {
			Log.d("ChapterPageSelectDialog", "onProgressChanged: mSeekChapter");

			int cnvChapter = calcProgressChapter(page);
			String chapterStr = "" + (cnvChapter + 1);
			mEditChapter.setText(chapterStr);
			mEditChapter.setSelection(chapterStr.length());
			String pageStr = "1";
			mEditPage.setText(pageStr);
			mEditPage.setSelection(pageStr.length());

			Log.d("ChapterPageSelectDialog", "onProgressChanged: cnvChapter=" + cnvChapter);

			if (mAutoApply) {
				// データ更新チェック用オブジェクト
//			mListener.onSelectPage(cnvpage);
				long nextTime = SystemClock.uptimeMillis() + TERM_CHAPTERSEELCT;
				mObject = new Object();
				Message msg = mHandler.obtainMessage(HMSG_CHAPTERSELECT);
				msg.arg1 = cnvChapter;
				msg.obj = mObject;
				mHandler.sendMessageAtTime(msg, nextTime);
			}
		}
		else if (mSeekPage == seekBar) {
			Log.d("ChapterPageSelectDialog", "onProgressChanged: mSeekPage");
			int cnvpage = calcProgressPage(page);
			String pageStr = "" + (cnvpage + 1);
			mEditPage.setText(pageStr);
			mEditPage.setSelection(pageStr.length());

			Log.d("ChapterPageSelectDialog", "onProgressChanged: cnvpage=" + cnvpage);

			if (mAutoApply) {
				// データ更新チェック用オブジェクト
//			mListener.onSelectPage(cnvpage);
				long nextTime = SystemClock.uptimeMillis() + TERM_PAGESEELCT;
				mObject = new Object();
				Message msg = mHandler.obtainMessage(HMSG_PAGESELECT);
				msg.arg1 = cnvpage;
				msg.obj = mObject;
				mHandler.sendMessageAtTime(msg, nextTime);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// 開始

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d("ChapterPageSelectDialog", "onStopTrackingTouch: 開始します.");
		// 終了
		if (mAutoApply) {
			if (mSeekChapter == seekBar) {
				Log.d("ChapterPageSelectDialog", "onStopTrackingTouch: mSeekChapter");
				int cnvChapter = calcProgressChapter(seekBar.getProgress());
				mListener.onSelectChapter(cnvChapter);
				mListener.onSelectPage(0);
			}
			else if (mSeekPage == seekBar) {
				Log.d("ChapterPageSelectDialog", "onStopTrackingTouch: mSeekPage");
				int cnvpage = calcProgressPage(seekBar.getProgress());
				mListener.onSelectPage(cnvpage);
			}
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.d("ChapterPageSelectDialog", "onDismiss: 開始します.");

		// ダイアログ終了
		mObject = null;
		mIsOpened = false;

		if (mIsCancel == true && mAutoApply) {
			// キャンセルなら元ページへ
			mListener.onSelectChapter(mChapter);
			mListener.onSelectPageRate((float) mPage / mMaxPage);
			Toast.makeText(mContext, "Canceled.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int action, KeyEvent event) {
		if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
			Log.d("ChapterPageSelectDialog", "onEditorAction: ACTION_UP");
			if (mAutoApply) {

				if (mEditChapter == v) {
					Log.d("ChapterPageSelectDialog", "onEditorAction: mEditChapter");
					// 確定されたときに章遷移
					String text = mEditChapter.getText().toString();
					int chapter = 0;
					try {
						chapter = Integer.parseInt(text) - 1;
					} catch (NumberFormatException e) {
						;
					}
					Log.d("ChapterPageSelectDialog", "onEditorAction: chapter=" + chapter);

					// 設定と通知
					mListener.onSelectChapter(chapter);
					setProgressChapter(chapter);
					setPage(1);

				}
				else if (mEditPage == v) {
					Log.d("ChapterPageSelectDialog", "onEditorAction: mEditPage");
					// 確定されたときにページ遷移
					String text = mEditPage.getText().toString();
					int page = 0;
					try {
						page = Integer.parseInt(text) - 1;
					} catch (NumberFormatException e) {
						;
					}
					Log.d("ChapterPageSelectDialog", "onEditorAction: page=" + page);

					// 設定と通知
					mListener.onSelectPage(page);
					setProgressPage(page);
				}
			}
		}
		return false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// ページ選択
		if (msg.what == HMSG_CHAPTERSELECT) {
			Log.d("ChapterPageSelectDialog", "handleMessage: HMSG_CHAPTERSELECT arg1=" + msg.arg1);
			if (msg.obj == mObject && msg.obj != null) {
				mListener.onSelectChapter(msg.arg1);
			}
		}
		else if (msg.what == HMSG_PAGESELECT) {
			Log.d("ChapterPageSelectDialog", "handleMessage: HMSG_PAGESELECT arg1=" + msg.arg1);
			if (msg.obj == mObject && msg.obj != null) {
				mListener.onSelectPage(msg.arg1);
			}
		}
		return false;
	}

	public void setChapter(int chapter) {
		Log.d("ChapterPageSelectDialog", "setChapter: 開始します. chapter=" + chapter);
		String chapterStr = "" + (chapter + 1);
		mEditChapter.setText(chapterStr);
		mEditChapter.setSelection(chapterStr.length());
		setProgressChapter(chapter);
	}

	public void setPage(int page) {
		Log.d("ChapterPageSelectDialog", "setPage: 開始します. page=" + page);
		String pageStr = "" + (page + 1);
		mEditPage.setText(pageStr);
		mEditPage.setSelection(pageStr.length());
		setProgressPage(page);
	}

	public void setMaxPage(int maxpage, int page) {
		Log.d("ChapterPageSelectDialog", "setPage: 開始します. maxpage=" + maxpage);
		String maxPageStr = "" + maxpage;
		mSeekPage.setMax(maxpage - 1);
		TextView maxpageview = (TextView) findViewById(R.id.text_maxpage);
		maxpageview.setText(maxPageStr);
		setProgressPage(page);
	}

}