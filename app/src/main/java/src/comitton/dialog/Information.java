package src.comitton.dialog;

import jp.dip.muracoro.comittonx.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import src.comitton.common.DEF;
import android.annotation.SuppressLint;

import androidx.appcompat.app.AlertDialog;
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
import org.json.JSONArray;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;

@SuppressLint("NewApi")
public class Information implements DialogInterface.OnDismissListener {
	private static final String TAG = "Information";
	// 表示中フラグ
	public static boolean mIsOpened = false;
	private AppCompatActivity mActivity;
	private Dialog mDialog;
	private AlertDialog mDownLoadsDialog;
	private String mDownloadsMessage;

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

	public void showDownloads(Handler handler) {
		if (!mIsOpened) {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity, R.style.MyDialog);
			dialogBuilder.setTitle(R.string.downloadCountMenu);

			dialogBuilder.setMessage(R.string.Connecting);

			dialogBuilder.setNegativeButton(R.string.btnOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// dialog.cancel();
				}
			});
			mDownLoadsDialog = dialogBuilder.create();
			mDownLoadsDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
			mDownLoadsDialog.getWindow().getDecorView().setSystemUiVisibility(
					mActivity.getWindow().getDecorView().getSystemUiVisibility());
			mDownLoadsDialog.show();
			mDownLoadsDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

			// ダイアログ終了通知を受け取る
			mDownLoadsDialog.setOnDismissListener(this);
			mDownLoadsDialog.show();

			DownloadsLoad downloads = new DownloadsLoad(handler);
			Thread thread = new Thread(downloads);
			thread.start();

		}
	}

	public void setDownloadsMessage() {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		if (mDownLoadsDialog != null) {
			if (mDownloadsMessage != null) {
				mDownLoadsDialog.setMessage(mDownloadsMessage);
			} else {
				mDownLoadsDialog.setMessage(mActivity.getString(R.string.NoResult));
			}
		}
		else {
			Logcat.e(logLevel, "mDownLoadsDialog が null です.");
		}
	}

	public void showRecentRelease() {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		if (!mManually) {
			// 自動実行ならダイアログを表示した時刻を保存する
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
			SharedPreferences.Editor ed = sp.edit();
			ed.putLong(DEF.KEY_TIME_CHECK_RELEASE, System.currentTimeMillis());
			ed.apply();
		}
		Logcat.d(logLevel, "mRecentVersion=" + mRecentVersion + ", mRecentDate=" + mRecentDate + ", mRecentHtml=" + mRecentHtml);
		Logcat.d(logLevel, "BuildConfig.VERSION_NAME=" + BuildConfig.VERSION_NAME + ", DEF.BUILD_DATE=" + DEF.BUILD_DATE);

		long recent_time = 0L;
		try {
			// タイムゾーン(グリニッジ標準時)
			final TimeZone DATE_TIME_ZONE = TimeZone.getTimeZone("Europe/London");
			// 時刻の形式
			final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

			//日時のテキストから、地域と形式の２つを使ってUNIX時間を得る
			final SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_PATTERN);
			format.setTimeZone(DATE_TIME_ZONE);
			recent_time = format.parse(mRecentDate).getTime();
		}
		catch (ParseException e) {
			;
		}

		Logcat.d(logLevel, "BUILD_DATE = " + (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())).format(BuildConfig.BUILD_DATE));
		Logcat.d(logLevel, "recent_time = " + (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())).format(recent_time));

		if (!mRecentVersion.equals(BuildConfig.VERSION_NAME) && recent_time > BuildConfig.BUILD_DATE) {
			// 1.リリースバージョンとアプリのバージョンが違う
			// 2.リリース時刻がアプリのビルド時刻より新しい
			// ならダイアログを表示する
			mDialog = (Dialog) new RecentReleaseDialog(mActivity, R.style.MyDialog);
			mDialog.show();
		}
	}

	public void checkRecentRelease(Handler handler, boolean manually) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		// 新しいリリースのチェック
		mTurnOff = !manually;
		boolean flag = false;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
		if (manually) {
			// 手動実行なら実行する
			flag = true;
		}
		else if (sp.getBoolean(DEF.KEY_CHECK_RELEASE, true)) {
			if (sp.getLong(DEF.KEY_TIME_CHECK_RELEASE, 0L) < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L)) {
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
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");

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
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");

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
					Logcat.d(logLevel, "url=" + url);
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
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");

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
								ed.apply();
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
		int logLevel = Logcat.LOG_LEVEL_WARN;
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
			Logcat.e(logLevel, "ファイルが読み込めませんでした. filename=" + filename);
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
			int logLevel = Logcat.LOG_LEVEL_WARN;
			boolean result = getRecentVersion();
			if (result) {
				Logcat.d(logLevel, "mRecentVersion=" + mRecentVersion + ", mRecentDate=" + mRecentDate + ", mRecentHtml=" + mRecentHtml);
				// 終了通知
				Message message = new Message();
				message.what = DEF.HMSG_RECENT_RELEASE;
				handler.sendMessage(message);
			}
			else {
				Logcat.e(logLevel, "バージョン情報の取得に失敗しました.");
			}
		}

		private boolean getRecentVersion() {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			try {
				URL url = new URL(DEF.API_RECENT_RELEASE);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setRequestProperty("Accept", "application/vnd.github+json");
				return InputStreamToString(con.getInputStream());
			}
			catch (MalformedURLException e) {
				Logcat.e(logLevel, "", e);
				return false;
			}
			catch (IOException e) {
				Logcat.e(logLevel, "", e);
				return false;
			}
		}

		// InputStream -> String
		private boolean InputStreamToString(InputStream is) throws IOException {
			int logLevel = Logcat.LOG_LEVEL_WARN;
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
				Logcat.e(logLevel, "", e);
				return false;
			}
		}
	}

	public class DownloadsLoad implements Runnable {
		private final Handler handler;

		public DownloadsLoad(Handler handler) {
			super();
			this.handler = handler;
		}

		public void run() {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");
			getDownloads();
		}

		private void getDownloads() {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");
			try {
				URL url = new URL(DEF.APP_DOWNLOADS);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setRequestProperty("Accept", "application/vnd.github+json");
				setMessage(con.getInputStream());
			}
			catch (MalformedURLException e) {
				Logcat.e(logLevel, "", e);
				mDownloadsMessage = mActivity.getString(R.string.GetError);
			}
			catch (IOException e) {
				Logcat.e(logLevel, "", e);
				mDownloadsMessage = mActivity.getString(R.string.GetError);
			}
			Message message = new Message();
			message.what = DEF.HMSG_APP_DOWNLOADS;
			handler.sendMessage(message);

		}

		// InputStream -> String
		private void setMessage(InputStream is) throws IOException {
			int logLevel = Logcat.LOG_LEVEL_WARN;
			Logcat.d(logLevel, "開始します.");

			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();

			String string = sb.toString();
			mDownloadsMessage = "";

			try {
				JSONArray top = new JSONArray(string);
				for (int i = 0; i < top.length(); i++) {
					JSONObject top_child = top.getJSONObject(i);
					mDownloadsMessage += "Version: " + top_child.getString("tag_name");
					mDownloadsMessage += "  :  " + top_child.getString("published_at") + "\n";

					JSONArray assets = top_child.getJSONArray("assets");
					for (int j = 0; j < assets.length(); j++) {
						JSONObject assets_child = assets.getJSONObject(j);
						mDownloadsMessage += "    " + assets_child.getString("name");
						mDownloadsMessage += "  :  " + assets_child.getString("download_count") + "\n";
					}
					mDownloadsMessage += "\n";
				}

			} catch (JSONException e) {
				Logcat.e(logLevel, "", e);
				mDownloadsMessage = mActivity.getString(R.string.ParseError);
			}
		}
	}
}
