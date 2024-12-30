package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class BookmarkDialog extends ImmersiveDialog implements OnClickListener {
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

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

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