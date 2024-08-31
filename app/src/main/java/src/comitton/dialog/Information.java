package src.comitton.dialog;

import src.comitton.common.DEF;
import src.comitton.common.MODULE;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import jp.dip.muracoro.comittonx.R;
import android.view.View.OnClickListener;
import android.widget.TextView;

@SuppressLint("NewApi")
public class Information implements DialogInterface.OnDismissListener {
	// 表示中フラグ
	public static boolean mIsOpened = false;
	private Activity mContext;
	private Dialog mDialog;

	public Information(Activity context) {
		mContext = context;
	}

	public void showNotice() {
		if (mIsOpened == false) {
			mDialog = (Dialog)new NoticeDialog(mContext);

			// ダイアログ終了通知を受け取る
			mDialog.setOnDismissListener(this);
			mDialog.show();
		}
	}

	public void showAbout() {
		if (mIsOpened == false) {
			mDialog = (Dialog)new AboutDialog(mContext);
			// ダイアログ終了通知を受け取る
			mDialog.setOnDismissListener(this);
			mDialog.show();
		}
	}

	public void close() {
		if (mDialog != null) {
			try {
				mDialog.dismiss();
			}
			catch (IllegalArgumentException e) {
				;
			}
			mDialog = null;
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// ダイアログ終了
		mIsOpened = false;
	}

	public class NoticeDialog extends Dialog implements OnClickListener {
		private Button mBtnDonate;
		private Button mBtnOk;
		private WebView mWebView;

		public NoticeDialog(Context context) {
			super(context);
			Window dlgWindow = getWindow();

			// タイトルなし
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			// 画面中央に表示
			WindowManager.LayoutParams wmlp = dlgWindow.getAttributes();
			wmlp.gravity = Gravity.CENTER;
			dlgWindow.setAttributes(wmlp);
			setCanceledOnTouchOutside(true);

			mIsOpened = true;
		}

		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			int layout;
			if (MODULE.isFree()) {
				layout = R.layout.notice_donate;
			} else {
				layout = R.layout.notice;
			}
			setContentView(layout);

			Window win = getWindow();
			WindowManager.LayoutParams lpCur = win.getAttributes();
			WindowManager.LayoutParams lpNew = new WindowManager.LayoutParams();
			lpNew.copyFrom(lpCur);
			lpNew.width = WindowManager.LayoutParams.FILL_PARENT;
			lpNew.height = WindowManager.LayoutParams.WRAP_CONTENT;
			win.setAttributes(lpNew);

			mWebView = (WebView) this.findViewById(R.id.web_text);
			mBtnDonate = (Button) this.findViewById(R.id.btn_donate);
			mBtnOk = (Button) this.findViewById(R.id.btn_ok);
			mBtnOk.setOnClickListener(this);
			if (mBtnDonate != null) {
				mBtnDonate.setOnClickListener(this);
			}

			Resources res = mContext.getResources();
			String localurl = res.getString(R.string.noticeText);
			mWebView.loadUrl("file:///android_asset/" + localurl);
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

		@Override
		public void onClick(View v) {
			// クリックイベント
			if (v == mBtnDonate) {
				// 寄付版
				MODULE.donate(mContext);
			}
			dismiss();
		}
	}

	public class AboutDialog extends AlertDialog {
		private WebView mWebView;

		@SuppressWarnings("deprecation")
		public AboutDialog(Context context) {
			super(context);
			setIcon(R.drawable.icon);
			setTitle(MODULE.aboutTitle(mContext));
			TextView tv = new TextView(mContext);
			tv.setText(Html.fromHtml(MODULE.ABOUT_INFO));
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			setView(tv);
			setButton(context.getResources().getString(MODULE.getAboutOk()),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// 寄付
							MODULE.donate(mContext);
						}
					});
			setButton2("Source Download Page",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// ソースダウンロード
							Uri uri = Uri.parse(DEF.DOWNLOAD_URL);
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							mContext.startActivity(intent);
						}
					});
			mIsOpened = true;
		}
	}
}
