package src.comitton.common;

import android.graphics.Paint;

public class TextFormatter {
	public static String[] getMultiLine(String str, int cx, Paint text, int maxline) {
		if (str == null) {
			return new String[0];
		}
		float result[] = new float[str.length()];
		text.getTextWidths(str, result);

		int i;
		int lastpos = 0;
		int line = 0;
		float sum = 0.0f;
		int pos[] = new int[maxline];

		float dotwidth = text.measureText("...");

		// 1行に入る文字長をしらべる
		for (i = 0; i < result.length; i++) {
			if (line == maxline - 1 && sum + result[i] > cx - dotwidth) {
				if (lastpos == 0) {
					lastpos = i;
				}
			}
			String ch = str.substring(i, i + 1);
			if (sum + result[i] > cx || ch.equals("\n")) {
				if (ch.equals("\n")) {
					sum = 0;
				}
				else {
					sum = result[i];
				}
				if (line == maxline - 1) {
					pos[line] = lastpos;
					break;
				}
				else {
					pos[line++] = i;
				}
			}
			else {
				sum += result[i];
			}
		}

		String strSep[] = new String[line + 1];
		int st = 0;
		// 文字列の切り出し
		for (i = 0; i <= line; i++) {
			String work;
			if (pos[i] == 0) {
				work = str.substring(st);

			}
			else {
				work = str.substring(st, pos[i]);
				if (i == maxline - 1) {
					work += "...";
				}
			}
			strSep[i] = work.replace("\n", "");
			st = pos[i];
		}
		return strSep;
	}

	public static String[] getShortening(String str, int cx, Paint text) {
		if (str == null || str.length() == 0) {
			return new String[0];
		}

		String strSep[] = new String[1];
		strSep[0] = getShorteningSingle(str, cx, text, false);
		return strSep;
	}

	public static String getShorteningSingle(String str, int cx, Paint text, boolean direction) {
		if (str == null || str.length() == 0) {
			return "";
		}
		float result[] = new float[str.length()];
		text.getTextWidths(str, result);

		int i;
		float sum = 0.0f;

		float dotwidth = text.measureText("...");
		String strSep = str;

			// 1行に入る文字長をしらべる
		if (direction) {
			// 先頭から詰める
			for (i = 0; i < result.length; i++) {
				if (sum + result[i] > cx - dotwidth) {
					strSep = str.substring(0, i) + "...";
					break;
				}
				sum += result[i];
			}
		}
		else {
			// 末尾から詰める
			for (i = result.length - 1; i >= 0; i--) {
				if (sum + result[i] > cx - dotwidth) {
					strSep = "..." + str.substring(i + 1);
					break;
				}
				sum += result[i];
			}
		}
		return strSep;
	}
}
