package src.comitton.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class ImageAccess {
	public static final int BMPFIT_WIDTH = 0;
	public static final int BMPFIT_HEIGHT = 1;

	public static final int BMPCROP_NONE = -1;
	public static final int BMPCROP_CENTER = 0;
	public static final int BMPCROP_LEFT = 1;
	public static final int BMPCROP_RIGHT = 2;
	public static final int BMPCROP_FIT_SCREEN = 3;
	public static final int BMPCROP_AUTO_COVER = 4;

	public static final int BMPMARGIN_NONE = 0;
	public static final int BMPMARGIN_WEAK = 1;
	public static final int BMPMARGIN_MEDIUM = 2;
	public static final int BMPMARGIN_STRONG = 3;
	public static final int BMPMARGIN_SPECIAL = 4;
	public static final int BMPMARGIN_OVERKILL = 5;
	public static final int BMPMARGIN_IGNORE_ASPECT_RATIO = 6;

	public static final int MARGIN_COLOR_WHITE_AND_BLACK = 0;
	public static final int MARGIN_COLOR_ALL_COLORS = 1;

	private static boolean COLOR_CHECK(int rgb1, int rgb2, int mask) {
		int red1 = ((rgb1>>16) & 0x00FF);
		int red2 = ((rgb2>>16) & 0x00FF);
		int red3 = Math.abs(red1 - red2);

		int green1 = ((rgb1>>8) & 0x00FF);
		int green2 = ((rgb2>>8) & 0x00FF);
		int green3 = Math.abs(green1 - green2);

		int blue1 = (rgb1 & 0x00FF);
		int blue2 = (rgb2 & 0x00FF);
		int blue3 = Math.abs(blue1 - blue2);

		int rgb3 = (((red3<<16) & 0x00FF0000) | ((green3<<8) & 0x0000FF00) | (blue3 & 0x000000FF));
		return ((rgb3 & mask) == 0x00000000);
	}

	// ビットマップをリサイズして切り出し
	public static Bitmap setText(Bitmap bitmap, String text, int color, int align) {
		Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas();
		canvas.setBitmap(bm);
		canvas.drawBitmap(bitmap, 0, 0, new Paint());

		Paint mTextPaint;
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize((float)bitmap.getHeight() / 3);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		float text_x = (float) bm.getWidth() / 2;
		float text_y = ((float) bm.getHeight() / 6)  + ((float) bm.getHeight() * align / 3) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2);

		mTextPaint.setShader(null);
		mTextPaint.setStyle(Paint.Style.STROKE);
		mTextPaint.setStrokeWidth(2.0f);
		mTextPaint.setColor(Color.DKGRAY);
		canvas.drawText(text, text_x, text_y, mTextPaint);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(color);
		canvas.drawText(text, text_x, text_y, mTextPaint);

		return bm;
	}


	// ビットマップをリサイズして切り出し
	public static Bitmap resizeTumbnailBitmap(Bitmap bm, int thum_cx, int thum_cy, int crop, int margin) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		if (bm == null || bm.isRecycled()) {
			return null;
		}

		int x = 0;
		int y = 0;

		Logcat.d(logLevel, "開始します. thum_cx=" + thum_cx + ", thum_cy=" + thum_cy + ", crop=" + crop);
		int src_cx = bm.getWidth();
		int src_cy = bm.getHeight();
		//Logcat.d(logLevel, "ソース幅を求めました. src_cx=" + src_cx + ", src_cy=" + src_cy);

		if (margin != 0) {
			// 余白を削除する

			/** 違う色の出現回数の許容する割合(単位0.1%)\n
			 *  違う色の回数がこの数値を超えると余白でないと判定する
			 */
			int limit;
			/** 余白判定された幅のうち画像から削除する割合(単位1%)\n
			 *  100%からこの値を引いた割合に応じて画像の縁に余白を残す
			 */
			int space;
			/** 余白判定を行う範囲(単位1%)\n
			 *  画像のほとんどが余白のときカットする量を制限する
			 */
			int range;
			/** 余白判定を開始するまでの無視区間(単位0.1%)\n
			 *  画面端にノイズがあっても余白削除を失敗させない
			 */
			int start;
			/** 色判定時のビットマスク深度\n
			 *  RGBすべてが上位ビットからこのビット数一致すれば同じ色と判定する
			 */
			int bitmask;

			switch (margin) {
				case BMPMARGIN_NONE:		// なし
					return null;
				case BMPMARGIN_WEAK:		// 弱
					limit = 5;
					space = 60;
					range = 25;
					start = 1;
					bitmask = 4;
					break;
				case BMPMARGIN_MEDIUM:		// 中
					limit = 6;
					space = 80;
					range = 30;
					start = 2;
					bitmask = 4;
					break;
				case BMPMARGIN_STRONG:		// 強
					limit = 8;
					space = 90;
					range = 45;
					start = 3;
					bitmask = 3;
					break;
				case BMPMARGIN_SPECIAL:		// 特上
					limit = 20;
					space = 95;
					range = 50;
					start = 5;
					bitmask = 3;
					break;
				default:	// 最強
					limit = 50;
					space = 100;
					range = 100;
					start = 10;
					bitmask = 2;
					break;
			}


			int startH = (int)Math.ceil((float)src_cy * start / 1000);
			int startW = (int)Math.ceil((float)src_cx * start / 1000);

			int mask;

			switch (bitmask) {
				case 0:
					mask = 0x00000000;  // 上位0ビット
					break;
				case 1:
					mask = 0x00808080;  // 上位1ビット
					break;
				case 2:
					mask = 0x00C0C0C0;  // 上位2ビット
					break;
				case 3:
					mask = 0x00E0E0E0;  // 上位3ビット
					break;
				case 4:
					mask = 0x00F0F0F0;  // 上位4ビット
					break;
				default:
					mask = 0x00F0F0F0;  // 上位4ビット
			}

			int CutL = 0;
			int CutR = 0;
			int CutT = 0;
			int CutB = 0;
			int work_cx = 0;
			int work_cy = 0;

			int ColorL = 0;
			int ColorR = 0;
			int ColorT = 0;
			int ColorB = 0;

			int[] ColorArrayL = new int[src_cy];
			int[] ColorArrayR = new int[src_cy];
			int[] ColorArrayT = new int[src_cx];
			int[] ColorArrayB = new int[src_cx];

			int CheckCX = src_cx * range / 100;
			int CheckCY = src_cy * range / 100;
			int xx;
			int yy;
			int overcnt;

			// 上下左右の端のラインの色の最頻値を調べる
			// 配列に左右端のラインの色を代入
			for (yy = 0; yy < src_cy; yy++) {
				ColorArrayL[yy] = bm.getPixel(startW, yy);
				ColorArrayR[yy] = bm.getPixel(src_cx - startW - 1, yy);
			}
			// 配列に上下端のラインの色を代入
			for (xx = 0; xx < src_cx; xx++) {
				ColorArrayT[xx] = bm.getPixel(xx, startH);
				ColorArrayB[xx] = bm.getPixel(xx, src_cy - startH - 1);
			}
			// 昇順ソート
			Arrays.sort(ColorArrayL);
			Arrays.sort(ColorArrayR);
			Arrays.sort(ColorArrayT);
			Arrays.sort(ColorArrayB);

			// 最頻値
			int pre_modeL, pre_modeR, pre_modeT, pre_modeB;
			int numL = 1, numR = 1, numT = 1, numB = 1; // テンポラリの出現回数
			int max_numL = 1, max_numR = 1, max_numT = 1, max_numB = 1; // 最頻値の出現回数
			int modeL, modeR, modeT, modeB;

			// 初期値を代入
			modeL = ColorArrayL[0];    // 色の最頻値の初期値
			modeR = ColorArrayR[0];    // 色の最頻値の初期値
			modeT = ColorArrayT[0];    // 色の最頻値の初期値
			modeB = ColorArrayB[0];    // 色の最頻値の初期値

			pre_modeL = ColorArrayL[0];    // 出現する回数を数える値
			pre_modeR = ColorArrayR[0];    // 出現する回数を数える値
			pre_modeT = ColorArrayL[0];    // 出現する回数を数える値
			pre_modeB = ColorArrayR[0];    // 出現する回数を数える値

			// 左右端のラインの色が出現する最頻値を求める
			for (yy = 0; yy < src_cy; yy++) {
				// 左のライン
				if (pre_modeL == ColorArrayL[yy]) {
					// 同じ値の場合
					// 出現回数に1を足す
					++numL;
				} else {
					// 違う値の場合
					// 出現回数が最頻値の出現回数より多ければ最頻値を更新する
					if (numL > max_numL) {
						modeL = pre_modeL;
						max_numL = numL;
					}

					// 出現する回数を数える値を変更
					pre_modeL = ColorArrayL[yy];
					numL = 1;
				}

				// 右のライン
				if (pre_modeR == ColorArrayR[yy]) {
					// 同じ値の場合
					// 出現回数に1を足す
					++numR;
				} else {
					// 違う値の場合
					// 出現回数が最頻値の出現回数より多ければ最頻値を更新する
					if (numR > max_numR) {
						modeR = pre_modeR;
						max_numR = numR;
					}

					// 出現する回数を数える値を変更
					pre_modeR = ColorArrayR[yy];
					numR = 1;
				}
			}
			// 後処理
			if (numL > max_numL) {
				modeL = pre_modeL;
				max_numL = numL;
			}
			if (numR > max_numR) {
				modeR = pre_modeR;
				max_numR = numR;
			}
			ColorL = modeL;
			ColorR = modeR;

			// 上下端のラインの色が出現する最頻値を求める
			for (xx = 0; xx < src_cx; xx++) {
				// 上のライン
				if (pre_modeT == ColorArrayT[xx]) {
					// 同じ値の場合
					// 出現回数に1を足す
					++numT;
				} else {
					// 違う値の場合
					// 出現回数が最頻値の出現回数より多ければ最頻値を更新する
					if (numT > max_numT) {
						modeT = pre_modeT;
						max_numT = numT;
					}

					// 出現する回数を数える値を変更
					pre_modeT = ColorArrayT[xx];
					numT = 1;
				}
				// 下のライン
				if (pre_modeB == ColorArrayB[xx]) {
					// 同じ値の場合
					// 出現回数に1を足す
					++numB;
				} else {
					// 違う値の場合
					// 出現回数が最頻値の出現回数より多ければ最頻値を更新する
					if (numB > max_numB) {
						modeB = pre_modeB;
						max_numB = numB;
					}

					// 出現する回数を数える値を変更
					pre_modeB = ColorArrayB[xx];
					numB = 1;
				}
			}
			// 後処理
			if (numT > max_numT) {
				modeT = pre_modeT;
				max_numT = numT;
			}
			if (numB > max_numB) {
				modeB = pre_modeB;
				max_numB = numB;
			}
			ColorT = modeT;
			ColorB = modeB;

			// 上の余白チェック
			for (yy = startH; yy < CheckCY; yy++) {
				//Logcat.d(logLevel, "yy=" + yy);
				overcnt = 0;    // 余白でないカウンタ
				CutT = yy;
				for (xx = 0; xx < src_cx; xx++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorT, mask)) {
						overcnt++;
					}
				}
				// 2%以上がオーバーしたら余白ではないとする
				if (overcnt >= src_cx * limit / 1000) {
					break;
				}
			}
			// 下の余白チェック
			for (yy = src_cy - startH - 1; yy >= src_cy - CheckCY; yy--) {
				//Logcat.d(logLevel, "yy=" + yy);
				overcnt = 0;    // 余白でないカウンタ
				CutB = src_cy - 1 - yy;
				for (xx = 0; xx < src_cx; xx++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorB, mask)) {
						overcnt++;
					}
				}
				// 2%以上がオーバーしたら余白ではないとする
				if (overcnt >= src_cx * limit / 1000) {
					break;
				}
			}
			// 左の余白チェック
			for (xx = startW; xx < CheckCX; xx++) {
				//Logcat.d(logLevel, "xx=" + xx);
				overcnt = 0;    // 余白でないカウンタ
				CutL = xx;
				for (yy = CutT + 1; yy < src_cy - CutB; yy++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorL, mask)) {
						overcnt++;
					}
				}
				// 6%以上がオーバーしたら余白ではないとする
				if (overcnt >= (src_cy - CutT - CutB) * limit / 1000) {
					// 6%以上
					break;
				}
			}
			// 右の余白チェック
			for (xx = src_cx - startW - 1; xx >= src_cx - CheckCX; xx--) {
				//Logcat.d(logLevel, "xx=" + xx);
				overcnt = 0;    // 余白でないカウンタ
				CutR = src_cx - 1 - xx;
				for (yy = CutT + 1; yy < src_cy - CutB; yy++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorR, mask)) {
						overcnt++;
					}
				}
				// 2%以上がオーバーしたら余白ではないとする
				if (overcnt >= (src_cy - CutT - CutB) * limit / 1000) {
					break;
				}
			}

			if(CutL <= startW){CutL = 0;}
			if(CutR <= startW){CutR = 0;}
			if(CutT <= startH){CutT = 0;}
			if(CutB <= startH){CutB = 0;}

			CutL = CutL * space / 100;
			CutR = CutR * space / 100;
			CutT = CutT * space / 100;
			CutB = CutB * space / 100;

			Logcat.d(logLevel, "カット幅を求めました. CutT=" + CutT + ", CutB=" + CutB + ", CutL=" + CutL + ", CutR=" + CutR);

			// 余白削除後のサイズを初期サイズに設定
			work_cx = src_cx - CutL - CutR;
			work_cy = src_cy - CutT - CutB;

			//Logcat.d(logLevel, "src_cx=" + src_cx + ", src_cy=" + src_cy + ", x=" + x + ", y=" + y);

			Logcat.d(logLevel, "縦横比を補正します. work_cx=" + work_cx + ", work_cy=" + work_cy);
			// 横幅がサムネイルの縦横比より細い場合、横のカットを戻す
			if (CutL + CutR > 0) {
				if (work_cx * 1000 / thum_cx < work_cy * 1000 / thum_cy) {
					Logcat.d(logLevel, " 横幅を補正します.");
					int margin_x = (int) ((float) src_cx - ((float) work_cy * ((float) thum_cx / (float) thum_cy)));
					Logcat.d(logLevel, "横幅を補正します. margin_x=" + margin_x);
					margin_x = Math.max(0, margin_x);
					CutL = margin_x * CutL / (CutL + CutR);
					CutR = margin_x - CutL;
				}
			}
			// 縦幅がサムネイルの縦横比より細い場合、横のカットを戻す
			if (CutT + CutB > 0) {
				if (work_cx * 1000 / thum_cx > work_cy * 1000 / thum_cy) {
					Logcat.d(logLevel, "縦幅を補正します.");
					int margin_y = (int) ((float) src_cy - ((float) work_cx * ((float) thum_cy / (float) thum_cx)));
					Logcat.d(logLevel, "横幅を補正します. margin_y=" + margin_y);
					margin_y = Math.max(0, margin_y);
					CutT = margin_y * CutT / (CutT + CutB);
					CutB = margin_y - CutT;
				}
			}

			// 余白削除後のサイズを初期サイズに設定
			src_cx = src_cx - CutL - CutR;
			src_cy = src_cy - CutT - CutB;
			x = CutL;
			y = CutT;
			Logcat.d(logLevel, "縦横比を補正しました. x=" + x + ", y=" + y + ", src_cx=" + src_cx + ", src_cy=" + src_cy);
		}

		Logcat.d(logLevel, "縮小サイズを計算します.  x=" + x + ", y=" + y + ", src_cx=" + src_cx + ", src_cy=" + src_cy);
		int dst_cx;
		int dst_cy;
		Logcat.d(logLevel, "src_cx/src_cy=" + ((float)src_cx / (float)src_cy) + ", thum_cx/thum_cy=" + ((float)thum_cx / (float)thum_cy));
		if (crop == BMPCROP_FIT_SCREEN) {
			Logcat.d(logLevel, "BMPCROP_FIT_SCREEN です. src_cx/src_cy=" + ((float)src_cx / (float)src_cy) + ", thum_cx/thum_cy=" + ((float)thum_cx / (float)thum_cy));
			if ((float)src_cx / src_cy > (float)thum_cx / thum_cy) {
				Logcat.d(logLevel, "幅に合わせます.  src_cx=" + src_cx + ", src_cx=" + src_cx + ", thum_cx=" + thum_cx + ", thum_cy=" + thum_cy);
				// 幅に合わせる
				dst_cx = thum_cx;
				dst_cy = src_cy * thum_cx / src_cx;
			}
			else {
				Logcat.d(logLevel, " 高さに合わせます.  src_cx=" + src_cx + ", src_cx=" + src_cx + ", thum_cx=" + thum_cx + ", thum_cy=" + thum_cy);
				// 高さに合わせる
				dst_cx = src_cx * thum_cy / src_cy;
				dst_cy = thum_cy;
			}
		}
		else {
			Logcat.d(logLevel, "BMPCROP_FIT_SCREEN ではありません.");
			Logcat.d(logLevel, "高さに合わせます.  src_cx=" + src_cx + ", src_cx=" + src_cx + ", thum_cx=" + thum_cx + ", thum_cy=" + thum_cy);
			// 幅に合わせる
			dst_cx = src_cx * thum_cy / src_cy;
			dst_cy = thum_cy;
		}
		float scale_x = (float) dst_cx / (float) src_cx;
		float scale_y = (float) dst_cy / (float) src_cy;

		if (scale_x * src_cx < 1) {
			scale_x = 1.0f / src_cx;
		}
		if (scale_y * src_cy < 1) {
			scale_y = 1.0f / src_cy;
		}
		Logcat.d(logLevel, "縮小サイズを計算しました. dst_cx=" + dst_cx + ", dst_cy=" + dst_cy);

//		if (src_cy > dst_cy) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale_x, scale_y);
		try {
			Logcat.d(logLevel, "ビットマップをリサイズします. x=" + x + ", y=" + y + ", width=" + src_cx + ", height=" + src_cy + ", m=(" + scale_x + ", " + scale_x + ")");
			bm = Bitmap.createBitmap(bm, x, y, src_cx, src_cy, matrix, true);
			if (bm == null) {
				Logcat.e(logLevel, "r ビットマップをリサイズできませんでした.");
				return null;
			}
			//Logcat.d(logLevel, "ビットマップをリサイズしました. width=" + bm.getWidth() + ", height=" + bm.getHeight());
		}
		catch (Exception e) {
			// 異常なサイズのときに落ちる不具合のため
			Logcat.e(logLevel, "リサイズでエラーになりました.", e);
			return null;
		}

		int bmp_cx = bm.getWidth();
		if (bmp_cx > thum_cx) {
			Logcat.d(logLevel, "横幅に合わせて画像を切り取ります.");
			if (dst_cy > bm.getHeight()) {
				dst_cy = bm.getHeight();
			}
			if (crop == BMPCROP_NONE) {
				Logcat.d(logLevel, "切り取り=BMPCROP_NONE");
				// なにもしない
			}
			if (crop == BMPCROP_CENTER) {
				Logcat.d(logLevel, "切り取り=BMPCROP_CENTER");
				// 中央
				x = (bmp_cx - thum_cx) / 2;
				bm = Bitmap.createBitmap(bm, x, 0, thum_cx, dst_cy);
			}
			else if (crop == BMPCROP_LEFT) {
				Logcat.d(logLevel, "切り取り=BMPCROP_LEFT");
				// 左側
				bm = Bitmap.createBitmap(bm, 0, 0, thum_cx, dst_cy);
			}
			else if (crop == BMPCROP_RIGHT) {
				Logcat.d(logLevel, "切り取り=BMPCROP_RIGHT");
				// 右側
				x = bmp_cx - thum_cx;
				bm = Bitmap.createBitmap(bm, x, 0, thum_cx, dst_cy);
			}
			else if (crop == BMPCROP_FIT_SCREEN) {
				Logcat.d(logLevel, "切り取り=BMPCROP_FIT_SCREEN");
				// なにもしない
			}
			else if (crop == BMPCROP_AUTO_COVER) {
				Logcat.d(logLevel, "切り取り=BMPCROP_AUTO_COVER");
				if ( (float) dst_cx / (float) dst_cy < 1.3 ){
					Logcat.d(logLevel, "右側を表示します.");
					x = bmp_cx - thum_cx;
				}
				else {
					Logcat.d(logLevel, "中央左を表示します.");
					// 中央左（背表紙を避ける）
					x = (int) ((bmp_cx / 2d) - (thum_cx * 1.05));
					if (x < 0) {
						// 左側
						x = 0;
					}
				}
				try {
					Logcat.d(logLevel, "ビットマップを切り取ります. x=" + x + ", y=0, width=" + dst_cx + ", height=" + dst_cy);
					bm = Bitmap.createBitmap(bm, x, 0, thum_cx, dst_cy);
					Logcat.d(logLevel, "ビットマップを切り取りました.");
				}
				catch (Exception e) {
					Logcat.e(logLevel, "ビットマップの切り取りでエラーになりました.", e);
					return null;
				}
			}
		}
		if (bm.getConfig() != Config.RGB_565) {
			Logcat.d(logLevel, "RGB_565に変換します.");
			bm = bm.copy(Config.RGB_565, true);
			Logcat.d(logLevel, "RGB_565に変換しました.");
		}
		Logcat.d(logLevel, "終了します. width=" + bm.getWidth() + ", height=" + bm.getHeight());
		return bm;
	}

	// ベクターxmlファイルからアイコンを作成(正方形)
	public static Bitmap createIcon(@NonNull Resources res, @DrawableRes int resid, int size, @ColorInt Integer drawcolor) {
		return createIcon(res, resid, size, size, drawcolor);
	}

	// ベクターxmlファイルからアイコンを作成(比率不定)
	public static Bitmap createIcon(@NonNull Resources res, @DrawableRes int resid, int sizeW, int sizeH, @ColorInt Integer drawcolor) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Bitmap bm = null;
		try {
			Drawable drawable = ResourcesCompat.getDrawable(res, resid, null);
			Canvas canvas = new Canvas();
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
			canvas.setBitmap(bm);
			drawable.setBounds(0, 0, w, h);
			drawable.draw(canvas);

			float dsW = (float)sizeW / (float)w;
			float dsH = (float)sizeH / (float)h;
			float dsMin = Math.min(dsW, dsH);

			bm = Bitmap.createScaledBitmap(bm, (int)(w * dsMin), (int)(h * dsMin), true);
			if (drawcolor != null) {
				bm = setColor(bm,drawcolor);
			}
		}
		catch (Exception e) {
			// 読み込み失敗
			Logcat.e(logLevel, "ビットマップの作成でエラーになりました.", e);
			return null;
		}

		return bm;
	}

	// ベクターxmlファイルからアイコンを作成
	public static Bitmap createIconFromRawPicture(@NonNull Resources res, @DrawableRes int resid, int size) {
		return createIcon(res, resid, size, size, null);
	}

	/**
	 * Bitmapデータの色を変更する。
	 */
	public static Bitmap setColor(Bitmap bitmap, @ColorInt int color) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Bitmap mutableBitmap = null;
		try {
			//mutable化する
			mutableBitmap = bitmap.copy(Config.ARGB_8888, true);
			//bitmap.recycle();

			Canvas myCanvas = new Canvas(mutableBitmap);

			int myColor = mutableBitmap.getPixel(0, 0);
			ColorFilter filter = new LightingColorFilter(myColor, color);

			Paint pnt = new Paint();
			pnt.setColorFilter(filter);
			myCanvas.drawBitmap(mutableBitmap, 0, 0, pnt);
		}
		catch (Exception e) {
			Logcat.e(logLevel, "色の変更でエラーになりました.", e);
			return null;
		}
		return mutableBitmap;
	}

	// Drawableのリサイズ
	public static Drawable zoom(@NonNull Resources res, Drawable drawable, float ratio) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * ratio), (int)(bitmap.getHeight() * ratio), false);
		return new BitmapDrawable(res, bitmapResized);
	}

	// デバッグ用に画像をファイル出力する
	public static int SaveFile (Bitmap bitmap) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. bitmap=[" + bitmap.getWidth() + ", " + bitmap.getHeight() + "]");
		try {
			// sdcardフォルダを指定
			File root = Environment.getExternalStorageDirectory();

			// 日付でファイル名を作成　
			Date mDate = new Date();
			SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd_HHmmss.SS");

			// 保存処理開始
			FileOutputStream fos = null;
			fos = new FileOutputStream(new File(DEF.getBaseDirectory() + "share/", fileName.format(mDate) + ".jpg"));

			// jpegで保存
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

			// 保存処理終了
			fos.close();
		} catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました.", e);
		}
		return 0;
	}

}
