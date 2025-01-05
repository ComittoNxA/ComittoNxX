package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("NewApi")
public class TextInputDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {

	private TextView mTitleTextView;
	private TextView mMessageTextView;
	private TextView mNoticeTextView;
	private EditText mEditText;
	private Button mBtnCancel;
	private Button mBtnOK;

	private String mTitle;
	private String mMessage;
	private String mNotice;
	private String mEdit;

	SearchListener mListener;

	public TextInputDialog(AppCompatActivity activity, @StyleRes int themeResId, String title, String message, String notice, String edit, SearchListener listener) {
		super(activity, themeResId, true);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		Resources res = mActivity.getResources();

		mTitle = title;
		mMessage = message;
		mNotice = notice;
		mEdit = edit;
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.textinput);
		mTitleTextView = (TextView)this.findViewById(R.id.text_title);
		mMessageTextView = (TextView)this.findViewById(R.id.text_message);
		mNoticeTextView = (TextView)this.findViewById(R.id.text_notice);
		mEditText = (EditText)this.findViewById(R.id.edit_text);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mBtnOK  = (Button)this.findViewById(R.id.btn_ok);

		mTitleTextView.setText(mTitle);
		mMessageTextView.setText(mMessage);
		mNoticeTextView.setText(mNotice);
		mEditText.setText(mEdit);

		if (mMessage == null || mMessage.length() == 0) {
			mMessageTextView.setVisibility(View.GONE);
		}
		if (mNotice == null || mNotice.length() == 0) {
			mNoticeTextView.setVisibility(View.GONE);
		}

		// キャンセルボタン
		mBtnCancel.setOnClickListener(this);
		mBtnOK.setOnClickListener(this);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MENU:
					dismiss();
					break;
				case KeyEvent.KEYCODE_ENTER:
					onClick(mBtnOK);
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
		if (v == mBtnOK) {
			String text = null;
			if (mEditText.getText() != null) {
				text = mEditText.getText().toString().trim();
			}
			mListener.onSearch(text);
		}
		else {
			// キャンセルクリック
			mListener.onCancel();
		}
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}
}