package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.StyleRes;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("NewApi")
public class ImmersiveDialog extends Dialog {
	protected Activity mActivity;
	protected int mWidth;
	protected int mHeight;
	protected float mScale;

	public ImmersiveDialog(Activity activity, int themeResId) {
		this(activity, themeResId, false);
	}

	public ImmersiveDialog(Activity activity, int themeResId, boolean wide) {
		super(activity, themeResId);
		mActivity = activity;
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

		// ソフトウェアキーボードを隠す
		dlgWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// サイズを決定する
		Rect size = new Rect();
		// ソフトウェアキーボードのサイズが引かれるのでgetWindowVisibleDisplayFrame(size)は使用しない
		//mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(size);
		mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(size);
		int cx = size.width();
		int cy = size.height();

		mScale = mActivity.getResources().getDisplayMetrics().scaledDensity;
		mWidth = Math.min(cx, cy) * 80 / 100;
		int maxWidth = (int)(20 * mScale * 16);
		if (!wide) {
			mWidth = Math.min(mWidth, maxWidth);
		}
		mHeight = cy * 80 / 100;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// ダイアログのサイズを設定する
		getWindow().setLayout(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
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
	                mActivity.getWindow().getDecorView().getSystemUiVisibility());
	    }
	}
}
