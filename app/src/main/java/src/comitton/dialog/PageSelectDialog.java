package src.comitton.dialog;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;

@SuppressLint("NewApi")
public class PageSelectDialog extends ToolbarDialog implements Handler.Callback,
		OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener, OnEditorActionListener {
	private final int HMSG_PAGESELECT		 = 5001;
	private final int TERM_PAGESEELCT		 = 100;

	// 表示中フラグ
	public static boolean mIsOpened = false;

	// OKを押して終了のフラグ
	private boolean mIsCancel;
	private Object mObject;
	private Handler mHandler;

	private EditText mEditPage;
	private Button mBtnCancel;
	private Button mBtnOK;

	public PageSelectDialog(Activity activity, @StyleRes int themeResId) {
		super(activity, themeResId);
		mIsCancel = false;
		mHandler = new Handler(this);
		mAutoApply = true;
	}

	public void setParams(boolean viwer, int page, int maxpage, boolean reverse) {
		super.setParams(viwer, page, maxpage, reverse, false);
	}

	public void setParams(boolean viwer, int page, int maxpage, boolean reverse, boolean dirtree) {
		super.setParams(viwer, page, maxpage, reverse, dirtree);
	}

	protected void onCreate(Bundle savedInstanceState){
		setContentView(R.layout.pageselect);

		// 一度ダイアログを表示すると画面回転時に呼び出される
		//TextView slash = (TextView) findViewById(R.id.text_slash);
		//slash.setText("/");
		TextView maxpage = (TextView) findViewById(R.id.text_maxpage);
		maxpage.setText("" + mMaxPage);

		mEditPage = (EditText) findViewById(R.id.edit_page);
		mEditPage.setInputType(InputType.TYPE_CLASS_NUMBER);
		String pageStr = "" + (mPage + 1);
		mEditPage.setText(pageStr);
		mEditPage.setSelection(pageStr.length());
		mEditPage.setOnEditorActionListener(this);

		mBtnCancel = (Button) this.findViewById(R.id.btn_cancel);
		mBtnOK     = (Button) this.findViewById(R.id.btn_ok);
		if (mBtnCancel != null) {
			mBtnCancel.setOnClickListener(this);
		}
		mBtnOK.setOnClickListener(this);

		// 表示中フラグ
		mIsOpened = true;

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		// ボタンクリック
		super.onClick(v);
		boolean pageSelect = false;

		String text = mEditPage.getText().toString();
		int page = 0;
		try {
			page = Integer.parseInt(text) - 1;
		} catch (NumberFormatException e) {
			;
		}

		if (mBtnOK == v) {
			pageSelect = true;
			if (page < 0) {
				page = 0;
			} else if (page >= mMaxPage) {
				page = mMaxPage - 1;
			}
			// 選択して終了
			mListener.onSelectPage(page);
			setProgress(page, false);
			dismiss();
			return;
		} else if (mBtnCancel == v) {
			pageSelect = true;
			// ダイアログ終了
			mIsCancel = true;
			dismiss();
			return;
		}

		if (page < 0) {
			page = 0;
		} else if (page >= mMaxPage) {
			page = mMaxPage - 1;
		}
		String pageStr = "" + (page + 1);
		mEditPage.setText(pageStr);
		mEditPage.setSelection(pageStr.length());

		// 設定と通知
		if (mAutoApply) {
			mListener.onSelectPage(page);
		}
		setProgress(page, false);
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
		super.onDismiss(dialog);
		// ダイアログ終了
		mObject = null;

		if (mIsCancel == true && mAutoApply) {
			// キャンセルなら元ページへ
			mListener.onSelectPage(mPage);
			Toast.makeText(mActivity, "Canceled.", Toast.LENGTH_SHORT).show();
		}
		mIsOpened = false;
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
				setProgress(page, false);
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