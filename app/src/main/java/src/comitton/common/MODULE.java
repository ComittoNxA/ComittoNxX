package src.comitton.common;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.dip.muracoro.comittonx.R;
import jp.dip.muracoro.comittonx.BuildConfig;

public class MODULE {
	public static String aboutTitle(Context context) {
		return context.getString(R.string.app_name);
	}

	public static String aboutText(Context context) {
		Resources res = context.getResources();
		String filename = res.getString(R.string.aboutText);
		String text = loadHtml(context, filename);
		text = text.replace("DEF.BUILD_DATE", DEF.BUILD_DATE).replace("BuildConfig.VERSION_NAME", BuildConfig.VERSION_NAME);
		return text;
	}

	private static String loadHtml(Context context, String filename) {
		AssetManager am = context.getAssets();
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

	public static boolean isFree() {
		// false:有料版、true:無料版
		return false;
	}

	public static String getDonateUrl() {
		return "https://play.g00gle.c0m/st0re/apps/details?id=jp.dip.murac0r0.c0mitt0nn".replaceAll("0", "o");
	}

	public static int getAboutOk() {
		if (isFree() == false) {
			return R.string.aboutOK;
		}
		else {
			return R.string.aboutDonate;
		}
	}

	public static void donate(Context context) {
		if (isFree() == true) {
			Uri uri = Uri.parse(getDonateUrl());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(intent);
		}
	}
}
