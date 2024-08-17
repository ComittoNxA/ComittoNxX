package src.comitton.dialog;

import src.comitton.listener.PageSelectListener;
import jp.dip.muracoro.comittonx.R;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class PageSelectDialog extends Dialog implements Handler.Callback, OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener, OnEditorActionListener {
	// 表示中フラグ
	public static boolean mIsOpened = false;

	private final int HMSG_PAGESELECT = 5001;
	private final int TERM_PAGESEELCT = 100;

	private PageSelectListener mListener = null;
	private Activity mContext;

	// パラメータ
	private int mPage;
	private int mMaxPage;
	private boolean mReverse;
	private boolean mAutoApply;

	// OKを押して終了のフラグ
	private boolean mIsCancel;
	private Object mObject;
	private Handler mHandler;

	private SeekBar mSeekPage;
	private EditText mEditPage;
	private Button mBtnAdd100;
	private Button mBtnAdd10;
	private Button mBtnAdd1;
	private Button mBtnSub100;
	private Button mBtnSub10;
	private Button mBtnSub1;
	private Button mBtnCancel;
	private Button mBtnOK;

	public PageSelectDialog(Activity context, boolean immmode) {
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

	public void setParams(int page, int maxpage, boolean reverse) {
		mPage = page;
		mMaxPage = maxpage;
		mReverse = reverse;
		mAutoApply = true;
//		mIsFirst = true;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pageselect);

		// 一度ダイアログを表示すると画面回転時に呼び出される
		TextView slash = (TextView) findViewById(R.id.text_slash);
		slash.setText("/");
		TextView maxpage = (TextView) findViewById(R.id.text_maxpage);
		maxpage.setText("" + mMaxPage);

        Window win = getWindow();
        WindowManager.LayoutParams lpCur = win.getAttributes();
        WindowManager.LayoutParams lpNew = new WindowManager.LayoutParams();
        lpNew.copyFrom(lpCur);
        lpNew.width = WindowManager.LayoutParams.FILL_PARENT;
        lpNew.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lpNew);

		mSeekPage = (SeekBar) findViewById(R.id.seek_page);
		mSeekPage.setMax(mMaxPage - 1);
		setProgress(mPage);
		mSeekPage.setOnSeekBarChangeListener(this);

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

	public void setPageSelectListear(PageSelectListener listener) {
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

		if (mBtnOK == v) {
			if (page < 0) {
				page = 0;
			}
			else if(page >= mMaxPage){
				page = mMaxPage - 1;
			}
			// 選択して終了
			mListener.onSelectPage(page);
			setProgress(page);
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
		setProgress(page);
	}

	private void setProgress(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		mSeekPage.setProgress(convpos);
	}

	private int calcProgress(int pos) {
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
		// 変更済み
		int cnvpage = calcProgress(page);
		String pageStr = "" + (cnvpage + 1);
		mEditPage.setText(pageStr);
		mEditPage.setSelection(pageStr.length());

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

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// 開始

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 終了
		if (mAutoApply) {
			int cnvpage = calcProgress(seekBar.getProgress());
			mListener.onSelectPage(cnvpage);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// ダイアログ終了
		mObject = null;
		mIsOpened = false;

		if (mIsCancel == true && mAutoApply) {
			// キャンセルなら元ページへ
			mListener.onSelectPage(mPage);
			Toast.makeText(mContext, "Canceled.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int action, KeyEvent event) {
	    if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
			if (mAutoApply) {
				// 確定されたときにページ遷移
				String text = mEditPage.getText().toString();
				int page = 0;
				try {
					page = Integer.parseInt(text) - 1;
				}
				catch (NumberFormatException e) {
					;
				}

				// 設定と通知
				if (mAutoApply) {
					mListener.onSelectPage(page);
				}
				setProgress(page);
			}
		}
		return false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// ページ選択
		if (msg.what == HMSG_PAGESELECT) {
			if (msg.obj == mObject && msg.obj != null) {
				mListener.onSelectPage(msg.arg1);
			}
		}
		return false;
	}
}