package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class InputDialog extends Dialog implements OnClickListener, OnDismissListener {
	private Activity mContext;

	private TextView mTitleText;
	private EditText mEditText;
	private Button mBtnSearch;
	private Button mBtnCancel;

	private String mTitle;
	private String mEdit;

	SearchListener mListener;

	public InputDialog(Activity context, String title, String edit, SearchListener listener) {
		super(context);
		Window dlgWindow = getWindow();

		dlgWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		PaintDrawable paintDrawable = new PaintDrawable(0xC0000000);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mContext = context;
		mTitle = title;
		mEdit = edit != null ? edit : "";
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.inputdialog);

		mTitleText = (TextView)this.findViewById(R.id.text_title);
		mEditText = (EditText)this.findViewById(R.id.edit_text);
		mBtnSearch  = (Button)this.findViewById(R.id.btn_search);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);

		mTitleText.setText(mTitle);
		mEditText.setText(mEdit);

		// キャンセルボタン
		mBtnSearch.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
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
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MENU:
					dismiss();
					break;
				case KeyEvent.KEYCODE_ENTER:
					onClick(mBtnSearch);
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	public interface SearchListener extends EventListener {
	    // 入力
	    public void onSearch(String text);
	    public void onCancel();
	    public void onClose();
	}

	@Override
	public void onClick(View v) {
		// キャンセルクリック
		if (v == mBtnSearch) {
			String text = null;
			if (mEditText.getText() != null) {
				text = mEditText.getText().toString();
			}
			mListener.onSearch(text);
		}
		else {
			mListener.onCancel();
		}
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}
}