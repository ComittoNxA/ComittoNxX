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
	public static final String ABOUT_INFO =
			  "		Build date :		" + DEF.BUILD_DATE + "<br>"
			+ "		Version :				" + BuildConfig.VERSION_NAME + "<br>"
			+ "		License :				<a href=\"https://raw.githubusercontent.com/ComittoNxA/ComittoNxX/master/LICENSE\">Unlicense license</a><br><br>"
			+ "Using Library<br><br>"
			+ "		jcifs (codelibs) 2.1.38 :<br>"
			+ "				License :		<a href=\"https://raw.githubusercontent.com/codelibs/jcifs/master/LICENSE\">LGPL v2.1</a><br><br>"
			+ "		unrar 7.1.1 :<br>"
			+ "				License :		<a href=\"https://raw.githubusercontent.com/pmachapman/unrar/master/license.txt\">unRAR restriction</a><br><br>"
			+ "		dav1d 1.5.0 :<br>"
			+ "				License :		<a href=\"https://raw.githubusercontent.com/videolan/dav1d/master/COPYING\">BSD-2-Clause</a><br><br>"
			+ "		libavif 1.1.1 :<br>"
			+ "				License :		<a href=\"https://raw.githubusercontent.com/AOMediaCodec/libavif/main/LICENSE\">BSD License</a><br><br>"
			+ "		epub4j 4.2.0 :<br>"
			+ "				License :		<a href=\"https://raw.githubusercontent.com/documentnode/epub4j/main/LICENSE\">ASL v2.0</a><br><br>"
			// 以下利用を終了したライブラリ
			//+ " 		libjpg-turbo 2.1.91 :<br>"
			//+ "				BSD-3-Clause, IJG<br>"
			//+ "		libpng 1.6.43 :<br>"
			//+ "				libpng License<br><br>"
			//+ "		libwebp 1.4.0 :<br>"
			//+ "				BSD-3-Clause<br><br>"
			//+ "		AndroidSVG 1.4 :<br>"
			//+ "				ASL v2.0<br><br>"
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
