package src.comitton.dialog;

import jp.dip.muracoro.comittonx.BuildConfig;
import src.comitton.common.DEF;
import src.comitton.common.MODULE;
import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import jp.dip.muracoro.comittonx.R;


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
			mDialog = (Dialog)new NoticeDialog(mContext, R.style.MyDialog);

			// ダイアログ終了通知を受け取る
			mDialog.setOnDismissListener(this);
			mDialog.show();
		}
	}

	public void showAbout() {
		if (mIsOpened == false) {
			mDialog = (Dialog)new AboutDialog(mContext, R.style.MyDialog);
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

	public class NoticeDialog extends AlertDialog {

		public NoticeDialog(Context context, int themeResId) {
			super(context, themeResId);
			boolean debug = false;
			if (debug) {Log.d("Information", "NoticeDialog: NoticeDialog: 開始します.");}

			Window dlgWindow = getWindow();
			// タイトルなし
			//requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Activityを暗くしない
			dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			// 背景を透明に
			dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

			String text = "";
			setIcon(BuildConfig.icon);
			setTitle(R.string.noticeTitle);

			WebView webView = new WebView(mContext);
			Resources res = mContext.getResources();
			String filename = res.getString(R.string.noticeText);
			webView.loadUrl("file:///android_asset/" + filename);
			webView.setBackgroundColor(Color.TRANSPARENT);
			setView(webView);
			setButton(DialogInterface.BUTTON_POSITIVE, res.getString(R.string.btnOK),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dismiss();
						}
					});
			mIsOpened = true;
		}

	}

	public class AboutDialog extends AlertDialog {

		@SuppressWarnings("deprecation")
		public AboutDialog(Context context, int themeResId) {
			super(context, themeResId);
			boolean debug = false;

			Window dlgWindow = getWindow();
			// タイトルなし
			//requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Activityを暗くしない
			dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			// 背景を透明に
			dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

			setIcon(BuildConfig.icon);
			setTitle(MODULE.aboutTitle(mContext));
			WebView webView = new WebView(mContext);
			Resources res = mContext.getResources();
			webView.loadDataWithBaseURL(null,MODULE.aboutText(context),"text/html", "utf-8", null);
			webView.setBackgroundColor(Color.TRANSPARENT);
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if(debug) {Log.d("Information", "AboutDialog: AboutDialog: url=" + url);}
					// ブラウザ起動
					view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
			});
			setView(webView);
			setButton(DialogInterface.BUTTON_POSITIVE, res.getString(MODULE.getAboutOk()),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// 寄付
							MODULE.donate(mContext);
						}
					});
			setButton(DialogInterface.BUTTON_NEUTRAL, res.getString(R.string.aboutSource),
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
