package src.comitton.dialog;

import jp.dip.muracoro.comittonx.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class LoadingDialog extends Dialog {
	public LoadingDialog(Context context) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		PaintDrawable paintDrawable = new PaintDrawable(0);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		// 画面下に表示
		WindowManager.LayoutParams wmlp=dlgWindow.getAttributes();
		wmlp.gravity = Gravity.RIGHT | Gravity.TOP;
		dlgWindow.setAttributes(wmlp);
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.progress);
	}
}
