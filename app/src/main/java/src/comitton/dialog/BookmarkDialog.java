package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class BookmarkDialog extends Dialog implements OnClickListener {
	public static final int CLICK_CANCEL   = 0;
	public static final int CLICK_OK       = 1;

	private BookmarkListenerInterface mListener = null;
	private Activity mContext;

	Button mBtnOk;
	Button mBtnCancel;
	EditText mEditName;

	String mDefaultName;
	int mLayoutId;

	public BookmarkDialog(Activity context) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		PaintDrawable paintDrawable = new PaintDrawable(0x80000000);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		mContext = context;
	}

	public void setName(String name) {
		mDefaultName = name;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.addbookmarkdialog);

		mBtnOk  = (Button) this.findViewById(R.id.btn_ok);
		mBtnCancel  = (Button) this.findViewById(R.id.btn_cancel);
		mEditName = (EditText) this.findViewById(R.id.edit_name);

		// デフォルトはしおりを記録する
		mEditName.setText(mDefaultName);

		mBtnOk.setOnClickListener(this);
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

	public void setBookmarkListear(BookmarkListenerInterface listener) {
		mListener = listener;
	}

	public interface BookmarkListenerInterface extends EventListener {
	    // ボタンが選択された
	    public void onAddBookmark(String name);
	}

	@Override
	public void onClick(View v) {
		// ボタンクリック
		if (mBtnOk == v) {
			// キャンセル以外
			String name = mEditName.getText().toString().trim();
			if (name.length() == 0) {
				// 未設定なら設定不可
				Toast.makeText(mContext, "Name is empty.", Toast.LENGTH_SHORT).show();
				return;
			}
			mListener.onAddBookmark(name);
		}
		dismiss();
	}
}