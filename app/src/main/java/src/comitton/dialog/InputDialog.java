package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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
public class InputDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {
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

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

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