package src.comitton.dialog;

import jp.dip.muracoro.comittonx.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import src.comitton.common.DEF;
import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import jp.dip.muracoro.comittonx.R;

@SuppressLint("NewApi")
public class Information implements DialogInterface.OnDismissListener {
	private static final String TAG = "Information";
	// 表示中フラグ
	public static boolean mIsOpened = false;
	private AppCompatActivity mActivity;
	private Dialog mDialog;

	public String mRecentVersion = null;
	public String mRecentHtml = null;
	public String mRecentDate = null;
	private boolean mManually  = false;
	private boolean mTurnOff  = false;

	public Information(AppCompatActivity activity) {
		mActivity = activity;
	}

	public void showNotice() {
		if (!mIsOpened) {
			mDialog = (Dialog)new NoticeDialog(mActivity, R.style.MyDialog);

			// ダイアログ終了通知を受け取る
			mDialog.setOnDismissListener(this);
			mDialog.show();
		}
	}

	public void showAbout() {
		if (!mIsOpened) {
			mDialog = (Dialog)new AboutDialog(mActivity, R.style.MyDialog);
			// ダイアログ終了通知を受け取る
			mDialog.setOnDismissListener(this);
			mDialog.show();
		}
	}

	public void showRecentRelease() {

		if (!mManually) {
			// 自動実行ならダイアログを表示した時刻を保存する
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
			SharedPreferences.Editor ed = sp.edit();
			ed.putLong(DEF.KEY_TIME_CHECK_RELEASE, System.currentTimeMillis());
			ed.commit();
		}

		if (!mRecentVersion.equals(BuildConfig.VERSION_NAME) &&  mRecentDate.substring(0, 10).replace("-", "/").compareTo(DEF.BUILD_DATE) > 0) {
			// 1.リリースバージョンとアプリのバージョンが違う
			// 2.リリース日がアプリのビルド日より新しい
			// ならダイアログを表示する
			mDialog = (Dialog) new RecentReleaseDialog(mActivity, R.style.MyDialog);
			mDialog.show();
		}
	}

	public void checkRecentRelease(Handler handler, boolean manually) {
		// 新しいリリースのチェック
		mTurnOff = !manually;
		boolean flag = false;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
		if (manually) {
			// 手動実行なら実行する
			flag = true;
		}
		else if (sp.getBoolean(DEF.KEY_CHECK_RELEASE, true)) {
			if (sp.getLong(DEF.KEY_TIME_CHECK_RELEASE, 0l) < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L)) {
				// 自動実行なら1日以上経過していたら実行する
				flag = true;
			}
		}

		if (flag) {
			RecentVersionLoad version = new RecentVersionLoad(handler);
			Thread thread = new Thread(version);
			thread.start();
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

	public class NoticeDialog extends ImmersiveAlertDialog {

		public NoticeDialog(AppCompatActivity activity, int themeResId) {
			super(activity, themeResId, true);
			boolean debug = false;
			if (debug) {Log.d(TAG, "NoticeDialog: NoticeDialog: 開始します.");}

			Window dlgWindow = getWindow();
			// タイトルなし
			//requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Activityを暗くしない
			dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			// 背景を設定
			dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

			String text = "";
			setIcon(BuildConfig.icon);
			setTitle(R.string.noticeTitle);

			WebView webView = new WebView(mActivity);
			Resources res = mActivity.getResources();
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

	public class AboutDialog extends ImmersiveAlertDialog {

		public AboutDialog(AppCompatActivity activity, int themeResId) {
			super(activity, themeResId, true);
			boolean debug = false;
			if (debug) {Log.d(TAG, "AboutDialog: AboutDialog: 開始します.");}

			Window dlgWindow = getWindow();

			// Activityを暗くしない
			dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			// 背景を設定
			dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

			setIcon(BuildConfig.icon);
			setTitle(mActivity.getString(R.string.app_name));

			WebView webView = new WebView(mActivity);
			Resources res = mActivity.getResources();
			webView.loadDataWithBaseURL(null, aboutText(),"text/html", "utf-8", null);
			webView.setBackgroundColor(Color.TRANSPARENT);
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if(debug) {Log.d(TAG, "AboutDialog: AboutDialog: url=" + url);}
					// ブラウザ起動
					view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
			});
			setView(webView);
			setButton(DialogInterface.BUTTON_POSITIVE, res.getString(R.string.aboutOK),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dismiss();
						}
					});
			setButton(DialogInterface.BUTTON_NEUTRAL, res.getString(R.string.aboutSource),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// ソースダウンロード
							Uri uri = Uri.parse(DEF.DOWNLOAD_URL);
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							mActivity.startActivity(intent);
						}
					});
			mIsOpened = true;
		}
	}

	public class RecentReleaseDialog extends ImmersiveAlertDialog {

		public RecentReleaseDialog(AppCompatActivity activity, int themeResId) {
			super(activity, themeResId, true);
			boolean debug = false;
			if (debug) {Log.d(TAG, "RecentReleaseDialog: RecentReleaseDialog: 開始します.");}

			Window dlgWindow = getWindow();

			// Activityを暗くしない
			dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			// 背景を設定
			dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

			setIcon(BuildConfig.icon);
			setTitle(mActivity.getString(R.string.app_name));
			String releaseMessage = mActivity.getString(R.string.releaseMessage) + "\n\n Version : " + mRecentVersion + "\n Release date : " + mRecentDate.substring(0, 10).replace("-", "/");

			setMessage(releaseMessage);
			Resources res = mActivity.getResources();

			setButton(DialogInterface.BUTTON_POSITIVE, res.getString(R.string.releasePositive),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// 最新版ダウンロード
							Uri uri = Uri.parse(mRecentHtml);
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							mActivity.startActivity(intent);
						}
					});
			setButton(DialogInterface.BUTTON_NEGATIVE, res.getString(R.string.releaseNegative),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dismiss();
						}
					});
			if (mTurnOff) {
				setButton(DialogInterface.BUTTON_NEUTRAL, res.getString(R.string.releaseNeutral),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// 通知設定をOFFにする
								SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
								SharedPreferences.Editor ed = sp.edit();
								ed.putBoolean(DEF.KEY_CHECK_RELEASE, false);
								ed.commit();
								dismiss();
							}
						});
			}
		}
	}

	public String aboutText() {
		Resources res = mActivity.getResources();
		String filename = res.getString(R.string.aboutText);
		String text = loadHtml(filename);
		text = text.replace("DEF.BUILD_DATE", DEF.BUILD_DATE).replace("BuildConfig.VERSION_NAME", BuildConfig.VERSION_NAME);
		return text;
	}

	private String loadHtml(String filename) {
		AssetManager am = mActivity.getAssets();
		StringBuilder stringBuilder = new StringBuilder();
		try {
			InputStream inputStream = am.open(filename);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e("MODULE", "loadHtml: ファイルが読み込めませんでした. filename=" + filename);
		}
		return stringBuilder.toString();
	}

	public class RecentVersionLoad implements Runnable {
		private final Handler handler;

		public RecentVersionLoad(Handler handler) {
			super();
			this.handler = handler;
		}

		public void run() {
			boolean debug = false;
			boolean result = getRecentVersion();
			if (result) {
				if (debug) {Log.d(TAG, "AboutDialog: AboutDialog: mRecentVersion=" + mRecentVersion + ", mRecentDate=" + mRecentDate + ", mRecentHtml=" + mRecentHtml);}
				// 終了通知
				Message message = new Message();
				message.what = DEF.HMSG_RECENT_RELEASE;
				handler.sendMessage(message);
			}
			else {
				Log.e(TAG, "RecentVersionLoad: バージョン情報の取得に失敗しました.");
			}
		}

		private boolean getRecentVersion() {
			try {
				URL url = new URL(DEF.API_RECENT_RELEASE);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setRequestProperty("Accept", "application/vnd.github+json");
				return InputStreamToString(con.getInputStream());
			}
			catch (MalformedURLException e) {
				Log.e(TAG, "getRecentVersion: " + e.getLocalizedMessage());
				return false;
			}
			catch (IOException e) {
				Log.e(TAG, "getRecentVersion: " + e.getLocalizedMessage());
				return false;
			}
		}

		// InputStream -> String
		private boolean InputStreamToString(InputStream is) throws IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();

			String string = sb.toString();

			try {
				JSONObject jsonObject = new JSONObject(string);
				mRecentVersion = jsonObject.getString("name");
				mRecentHtml = jsonObject.getString("html_url");
				mRecentDate = jsonObject.getString("published_at");
				return true;
			} catch (JSONException e) {
				Log.e(TAG, "InputStreamToString: " + e.getLocalizedMessage());
				return false;
			}
		}
	}

}
