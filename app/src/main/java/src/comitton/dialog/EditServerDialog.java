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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("NewApi")
public class EditServerDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {

	private EditText mEditName;
	private EditText mEditHost;
	private EditText mEditUser;
	private EditText mEditPass;
	private Button mBtnCancel;
	private Button mBtnOK;

	private String mName;
	private String mHost;
	private String mUser;
	private String mPass;

	SearchListener mListener;

	public EditServerDialog(Activity activity, @StyleRes int themeResId, @NonNull String name, @NonNull String host, @NonNull String user, @NonNull String pass, SearchListener listener) {
		super(activity, themeResId, true);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		Resources res = mActivity.getResources();

		mName = name;
		mHost = host;
		mUser = user;
		mPass = pass;
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editserverdialog);
		mEditName = (EditText)this.findViewById(R.id.edit_name);
		mEditHost = (EditText)this.findViewById(R.id.edit_host);
		mEditUser = (EditText)this.findViewById(R.id.edit_user);
		mEditPass = (EditText)this.findViewById(R.id.edit_pass);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mBtnOK  = (Button)this.findViewById(R.id.btn_search);

		mEditName.setText(mName);
		mEditHost.setText(mHost);
		mEditUser.setText(mUser);
		mEditPass.setText(mPass);

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
	    public void onSearch(String name, String host, String user, String pass);
	    public void onCancel();
	    public void onClose();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnOK) {
			String name = null;
			String host = null;
			String user = null;
			String pass = null;
			if (mEditName.getText() != null) {
				name = mEditName.getText().toString().trim();
			}
			if (mEditHost.getText() != null) {
				host = mEditHost.getText().toString().trim();
			}
			if (mEditUser.getText() != null) {
				user = mEditUser.getText().toString().trim();
			}
			if (mEditPass.getText() != null) {
				pass = mEditPass.getText().toString().trim();
			}
			mListener.onSearch(name, host, user, pass);
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