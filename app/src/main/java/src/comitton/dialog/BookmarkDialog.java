package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;

@SuppressLint("NewApi")
public class BookmarkDialog extends ImmersiveDialog implements OnClickListener {
	public static final int CLICK_CANCEL   = 0;
	public static final int CLICK_OK       = 1;

	private BookmarkListenerInterface mListener = null;

	EditText mEditName;
	Button mBtnCancel;
	Button mBtnOk;

	String mDefaultName;

	public BookmarkDialog(Activity activity, @StyleRes int themeResId) {
		super(activity, themeResId);
	}

	public void setName(String name) {
		mDefaultName = name;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.addbookmarkdialog);

		mEditName = (EditText) this.findViewById(R.id.edit_name);
		mBtnCancel  = (Button) this.findViewById(R.id.btn_cancel);
		mBtnOk  = (Button) this.findViewById(R.id.btn_ok);

		// デフォルトはしおりを記録する
		mEditName.setText(mDefaultName);

		mBtnCancel.setOnClickListener(this);
		mBtnOk.setOnClickListener(this);
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
				Toast.makeText(mActivity, "Name is empty.", Toast.LENGTH_SHORT).show();
				return;
			}
			mListener.onAddBookmark(name);
		}
		dismiss();
	}
}