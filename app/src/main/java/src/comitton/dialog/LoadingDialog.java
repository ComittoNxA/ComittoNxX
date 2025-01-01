package src.comitton.dialog;

import jp.dip.muracoro.comittonx.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class LoadingDialog extends Dialog {
	public LoadingDialog(Activity activity) {
		super(activity);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景なし
		dlgWindow.setBackgroundDrawableResource(android.R.color.transparent);

		// 画面右上に表示
		WindowManager.LayoutParams wmlp=dlgWindow.getAttributes();
		wmlp.gravity = Gravity.RIGHT | Gravity.TOP;
		dlgWindow.setAttributes(wmlp);
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.progress);
	}
}
