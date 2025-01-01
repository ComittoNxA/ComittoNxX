package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;

@SuppressLint("NewApi")
public class CloseDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {
	public static final int LAYOUT_BACK = 0;
	public static final int LAYOUT_TOP  = 1;
	public static final int LAYOUT_LAST = 2;
//	public static final int LAYOUT_TEXT = 3;

	public static final int CLICK_CANCEL   = 0;
	public static final int CLICK_CLOSE    = 1;
//	public static final int CLICK_EXITAPP  = 2;
	public static final int CLICK_NEXT     = 3;
	public static final int CLICK_PREV     = 4;
	public static final int CLICK_NEXTTOP  = 5;
	public static final int CLICK_NEXTLAST = 6;
	public static final int CLICK_PREVTOP  = 7;
	public static final int CLICK_PREVLAST = 8;
	public static final int CLICK_THIS     = 9;

	private CloseListenerInterface mListener = null;

	Button mBtnClose;
	Button mBtnExit;
	Button mBtnCancel;
	Button mBtnNext1;
	Button mBtnNext2;
	Button mBtnPrev1;
	Button mBtnPrev2;
	CheckBox mChkResume;
	CheckBox mChkMark;

	String mTitle;
	int mLayoutId;

	public CloseDialog(Activity activity, @StyleRes int themeResId) {
		super(activity, themeResId);

		// 外をタッチすると閉じる
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);
	}

	public void setTitleText(int layout) {
		Resources res = mActivity.getResources();
		switch (layout) {
			case LAYOUT_BACK:
				mLayoutId = R.layout.closedialog_back;
				break;
			case LAYOUT_TOP:
				mLayoutId = R.layout.closedialog_top;
				break;
			case LAYOUT_LAST:
				mLayoutId = R.layout.closedialog_last;
				break;
		}
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(mLayoutId);

		mBtnClose  = (Button) this.findViewById(R.id.btn_close);
		mBtnNext1  = (Button) this.findViewById(R.id.btn_nexttop);
		mBtnNext2  = (Button) this.findViewById(R.id.btn_next);
		mBtnPrev1  = (Button) this.findViewById(R.id.btn_prevlast);
		mBtnPrev2  = (Button) this.findViewById(R.id.btn_prev);
		mChkResume = (CheckBox) this.findViewById(R.id.chk_resume);
		mChkMark   = (CheckBox) this.findViewById(R.id.chk_mark);

		if (mChkResume != null) {
			// デフォルトは最終ファイルを記録する
			mChkResume.setChecked(true);
		}
		if (mChkMark != null) {
			// デフォルトはしおりを記録する
			mChkMark.setChecked(true);
		}

		mBtnClose.setOnClickListener(this);
//		mBtnExit.setOnClickListener(this);
		if (mBtnCancel != null) {
			mBtnCancel.setOnClickListener(this);
		}
		if (mBtnNext1 != null) {
			mBtnNext1.setOnClickListener(this);
		}
		if (mBtnNext2 != null) {
			mBtnNext2.setOnClickListener(this);
		}
		if (mBtnPrev1 != null) {
			mBtnPrev1.setOnClickListener(this);
		}
		if (mBtnPrev2 != null) {
			mBtnPrev2.setOnClickListener(this);
		}

		if (mTitle != null) {
			TextView message  = (TextView) this.findViewById(R.id.text_message);
			message.setText(mTitle);
		}
	}

	public void setCloseListear(CloseListenerInterface listener) {
		mListener = listener;
	}

	public interface CloseListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onCloseSelect(int select, boolean isResume, boolean isMark);
	    public void onClose();
	}

	@Override
	public void onClick(View v) {
		boolean isResume = false;
		boolean isMark = false;
		int select = CLICK_CANCEL;

		// ボタンクリック
		if (mBtnClose == v) {
			select = CLICK_CLOSE;
		}
//		else if (mBtnExit == v) {
//			select = CLICK_EXITAPP;
//		}
		else if (mBtnNext1 == v) {
			select = CLICK_NEXTTOP;
		}
		else if (mBtnPrev1 == v) {
			select = CLICK_PREVLAST;
		}
		else if (mBtnNext2 == v) {
			select = CLICK_NEXT;
		}
		else if (mBtnPrev2 == v) {
			select = CLICK_PREV;
		}

		if (select != CLICK_CANCEL) {
			if (mChkResume != null) {
				// 存在する場合のみ
				isResume = mChkResume.isChecked();
			}
			if (mChkMark != null) {
				// 存在する場合のみ
				isMark = mChkMark.isChecked();
			}
			// キャンセル以外
			mListener.onCloseSelect(select, isResume, isMark);
		}
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}
}