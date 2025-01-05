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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("NewApi")
public class MarkerInputDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {

	private EditText mEditText;
	private CheckBox chkFilter;
	private CheckBox chkApplyDir;
	private Button mBtnCancel;
	private Button mBtnSearch;

	private String mEdit;
	private boolean mFilter;
	private boolean mApplyDir;

	SearchListener mListener;

	public MarkerInputDialog(AppCompatActivity activity, @StyleRes int themeResId, String edit, boolean filter, boolean applyDir, SearchListener listener) {
		super(activity, themeResId, true);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		Resources res = mActivity.getResources();

		mEdit = edit != null ? edit : "";
		mFilter = filter;
		mApplyDir = applyDir;
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.marker_input);
		mEditText = (EditText)this.findViewById(R.id.edit_text);
		chkFilter = (CheckBox) this.findViewById(R.id.chk1);
		chkApplyDir = (CheckBox) this.findViewById(R.id.chk2);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mBtnSearch  = (Button)this.findViewById(R.id.btn_search);

		mEditText.setText(mEdit);
		chkFilter.setChecked(mFilter);
		chkApplyDir.setChecked(mApplyDir);

		// キャンセルボタン
		mBtnCancel.setOnClickListener(this);
		mBtnSearch.setOnClickListener(this);
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
	    public void onSearch(String text, boolean filter, boolean applyDir);
	    public void onCancel();
	    public void onClose();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnSearch) {
			String text = null;
			if (mEditText.getText() != null) {
				text = mEditText.getText().toString().trim();
			}
			mListener.onSearch(text, chkFilter.isChecked(), chkApplyDir.isChecked());
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