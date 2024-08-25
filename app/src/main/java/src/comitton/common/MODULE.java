package src.comitton.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import jp.dip.muracoro.comittonx.R;
import jp.dip.muracoro.comittonx.BuildConfig;

public class MODULE {
	public static String aboutTitle(Context context) {
		return context.getString(R.string.app_name);
	}
	public static final String ABOUT_INFO = "\nLast Update : " + DEF.BUILD_DATE + "\n"
			+ "  Version " + BuildConfig.VERSION_NAME + "\n\n"
			+ "Using Library\n"
			+ "  jcifs (codelibs) 2.1.38 (LGPL v2.1)\n"
			+ "  unrar 6.20.1\n"			// (UnRAR licence)
			+ (DEF.WITH_AVIF ? "  dav1d 1.4.3\n" : "")				// (BSD-2-Clause)
			+ (DEF.WITH_AVIF ? "  libavif 1.1.1\n" : "")			// (LicenseRef-libavif)
			//+ (DEF.WITH_JPEG ? "  libjpg-turbo 2.1.91\n" : "") 		//  (BSD-3-Clause, IJG)
			//+ (DEF.WITH_PNG ? "  libpng 1.6.43\n" : "")				// (libpng License)
			//+ (DEF.WITH_WEBP ? "  libwebp 1.4.0\n" : "")			// (BSD-3-Clause)
			//+ "  AndroidSVG 1.4 (ASL v2.0)\n"
			;

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
