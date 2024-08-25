package src.comitton.common;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.Arrays;


public class ImageAccess {
	public static final int BMPALIGN_LEFT = 0;
	public static final int BMPALIGN_CENTER = 1;
	public static final int BMPALIGN_RIGHT = 2;
	public static final int BMPALIGN_AUTO = 3;

	private static boolean COLOR_CHECK(int rgb1, int rgb2) { return (((rgb1 ^ rgb2) & 0x00808080) == 0x00000000); }

	// ビットマップをリサイズして切り出し
	public static Bitmap resizeTumbnailBitmap(Bitmap bm, int thum_cx, int thum_cy, int align) {
		if (bm == null || bm.isRecycled()) {
			return null;
		}

		Log.d("ImageAccess", "resizeTumbnailBitmap: Start. thum_cx=" + thum_cx + ", thum_cy=" + thum_cy + ", align=" + align);
		int src_cx = bm.getWidth();
		int src_cy = bm.getHeight();
		Log.d("ImageAccess", "resizeTumbnailBitmap: Start. src_cx=" + src_cx + ", src_cy=" + src_cy);

		int x = 0;
		int y = 0;

		if (align == BMPALIGN_AUTO) {
			// 余白を削除する
			int CutL = 0;
			int CutR = 0;
			int CutT = 0;
			int CutB = 0;

			int ColorL = 0;
			int ColorR = 0;
			int ColorT = 0;
			int ColorB = 0;

			int ColorArrayL[] = new int[src_cy];
			int ColorArrayR[] = new int[src_cy];
			int ColorArrayT[] = new int[src_cx];
			int ColorArrayB[] = new int[src_cx];

			int CheckCX = (int) (src_cx * 0.3);
			int CheckCY = (int) (src_cy * 0.3);
			int xx;
			int	yy;
			int overcnt;

			// 上下左右の端のラインの色の最頻値を調べる
			// 配列に左右端のラインの色を代入
			for (yy = 0 ; yy < src_cy ; yy++) {
				ColorArrayL[yy] = bm.getPixel(0, yy);
				ColorArrayR[yy] = bm.getPixel(src_cx - 1, yy);
			}
			// 配列に上下端のラインの色を代入
			for (xx = 0 ; xx < src_cx ; xx++) {
				ColorArrayT[xx] = bm.getPixel(xx, 0);
				ColorArrayB[xx] = bm.getPixel(xx, src_cy - 1);
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
			modeL = ColorArrayL[0];	// 色の最頻値の初期値
			modeR = ColorArrayR[0];	// 色の最頻値の初期値
			modeT = ColorArrayT[0];	// 色の最頻値の初期値
			modeB = ColorArrayB[0];	// 色の最頻値の初期値

			pre_modeL = ColorArrayL[0];	// 出現する回数を数える値
			pre_modeR = ColorArrayR[0];	// 出現する回数を数える値
			pre_modeT = ColorArrayL[0];	// 出現する回数を数える値
			pre_modeB = ColorArrayR[0];	// 出現する回数を数える値

			// 左右端のラインの色が出現する最頻値を求める
			for (yy = 0 ; yy < src_cy ; yy++) {
				// 左のライン
				if (pre_modeL == ColorArrayL[yy]) {
					// 同じ値の場合
					// 出現回数に1を足す
					++ numL;
				}
				else {
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
					++ numR;
				}
				else {
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
			for (xx = 0 ; xx < src_cx ; xx++) {
				// 上のライン
				if (pre_modeT == ColorArrayT[xx]) {
					// 同じ値の場合
					// 出現回数に1を足す
					++ numT;
				}
				else {
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
					++ numB;
				}
				else {
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
			for (yy = 0 ; yy < CheckCY ; yy ++) {
				//Log.d("comitton", "resizeTumbnailBitmap yy=" + yy);
				overcnt = 0;	// 余白でないカウンタ
				CutT = yy;
				for (xx = 0 ; xx < src_cx ; xx ++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorT)) {
						overcnt ++;
					}
				}
				// 6%以上がオーバーしたら余白ではないとする
				if (overcnt >= src_cx * 0.02) {
					// 6%以上
					break;
				}
			}
			// 下の余白チェック
			for (yy = src_cy - 1 ; yy >= src_cy - CheckCY ; yy --) {
				//Log.d("comitton", "resizeTumbnailBitmap yy=" + yy);
				overcnt = 0;	// 余白でないカウンタ
				CutB = src_cy - 1 - yy;
				for (xx = 0 ; xx < src_cx ; xx ++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorB)) {
						overcnt ++;
					}
				}
				// 6%以上がオーバーしたら余白ではないとする
				if (overcnt >= src_cx * 0.02) {
					// 6%以上
					break;
				}
			}
			// 左の余白チェック
			for (xx = 0 ; xx < CheckCX ; xx ++) {
				//Log.d("comitton", "resizeTumbnailBitmap xx=" + xx);
				overcnt = 0;	// 余白でないカウンタ
				CutL = xx;
				for (yy = CutT + 1 ; yy < src_cy - CutB ; yy ++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorL)) {
						overcnt ++;
					}
				}
				// 6%以上がオーバーしたら余白ではないとする
				if (overcnt >= (src_cy - CutT - CutB) * 0.02) {
					// 6%以上
					break;
				}
			}
			// 右の余白チェック
			for (xx = src_cx - 1 ; xx >= src_cx - CheckCX ; xx --) {
				//Log.d("comitton", "resizeTumbnailBitmap xx=" + xx);
				overcnt = 0;	// 余白でないカウンタ
				CutR = src_cx - 1 - xx;
				for (yy = CutT + 1 ; yy < src_cy - CutB ; yy ++) {
					// 余白チェック
					if (!COLOR_CHECK(bm.getPixel(xx, yy), ColorR)) {
						overcnt ++;
					}
				}
				// 6%以上がオーバーしたら余白ではないとする
				if (overcnt >= (src_cy - CutT - CutB) * 0.02) {
					// 6%以上
					Log.d("ImageAccess", "resizeTumbnailBitmap: Start. break xx=" + xx + ", overcnt=" + overcnt + "/" + (src_cy - CutT - CutB) + "=" + (double)overcnt / (src_cy - CutT - CutB));
					break;
				}
			}

			Log.d("ImageAccess", "resizeTumbnailBitmap: Start. CutT=" + CutT + ", CutB=" + CutB + ", CutL=" + CutL + ", CutR=" + CutR);

			// 余白削除後のサイズを初期サイズに設定
			src_cx = src_cx - CutL -CutR;
			src_cy = src_cy - CutT -CutB;
			x = CutL;
			y = CutT;
		}
		//Log.d("comitton", "resizeTumbnailBitmap src_cx=" + src_cx + ", src_cy=" + src_cy + ", x=" + x + ", y=" + y);

		int dst_cx = src_cx * thum_cy / src_cy;
		int dst_cy = thum_cy;

		float scale_x = (float) dst_cx / (float) src_cx;
		float scale_y = (float) dst_cy / (float) src_cy;

		if (scale_x * src_cx < 1) {
			scale_x = 1.0f / src_cx;
		}
		if (scale_y * src_cy < 1) {
			scale_y = 1.0f / src_cy;
		}

//		if (src_cy > dst_cy) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale_x, scale_y);
		try {
			bm = Bitmap.createBitmap(bm, x, y, src_cx, src_cy, matrix, true);
		}
		catch (OutOfMemoryError e) {
			// 異常なサイズのときに落ちる不具合のため
			return null;
		}
//		}

		int bmp_cx = bm.getWidth();
		if (bmp_cx > thum_cx) {
			if (dst_cy > bm.getHeight()) {
				dst_cy = bm.getHeight();
			}
			if (align == BMPALIGN_LEFT) {
				// 左側
				bm = Bitmap.createBitmap(bm, 0, 0, thum_cx, dst_cy);
			}
			else if (align == BMPALIGN_CENTER) {
				// 中央
				x = (bmp_cx - thum_cx) / 2;
				bm = Bitmap.createBitmap(bm, x, 0, thum_cx, dst_cy);
			}
			else if (align == BMPALIGN_RIGHT) {
				// 右側
				x = bmp_cx - thum_cx;
				bm = Bitmap.createBitmap(bm, x, 0, thum_cx, dst_cy);
			}
			else if (align == BMPALIGN_AUTO) {
				if ( (float) dst_cx / (float) dst_cy < 1.3 ){
					// 右側
					x = bmp_cx - thum_cx;
				}
				else {
					// 中央左（背表紙を避ける）
					x = (int) ((bmp_cx / 2) - (thum_cx * 1.05));
					if (x < 0) {
						// 左側
						x = 0;
					}
				}
				bm = Bitmap.createBitmap(bm, x, 0, thum_cx, dst_cy);
			}
		}
		return bm;
	}

	// ベクターxmlファイルからアイコンを作成(正方形)
	public static Bitmap createIcon(Resources res, int resid, int size, Integer drawcolor) {
		return createIcon(res, resid, size, size, drawcolor);
	}

	// ベクターxmlファイルからアイコンを作成(比率不定)
	public static Bitmap createIcon(Resources res, int resid, int sizeW, int sizeH, Integer drawcolor) {
		Bitmap bm = null;
		try {
			Drawable drawable = ResourcesCompat.getDrawable(res, resid, null);
			Canvas canvas = new Canvas();
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
			canvas.setBitmap(bm);
			drawable.setBounds(0, 0, w, h);
			float dsW = (float)sizeW / (float)w;
			float dsH = (float)sizeH / (float)h;
			canvas.scale(dsW, dsH);
			drawable.draw(canvas);
			if (drawcolor != null) {
				bm = setColor(bm,drawcolor);
			}
		}
		catch (Exception ex) {
			// 読み込み失敗
		}

		return bm;
	}

	// ベクターxmlファイルからアイコンを作成
	public static Bitmap createIconFromRawPicture(Resources res, int resid, int size) {
		return createIcon(res, resid, size, size, null);
	}

	/**
	 * Bitmapデータの色を変更する。
	 */
	private static Bitmap setColor(Bitmap bitmap, int color) {
		//mutable化する
		Bitmap mutableBitmap = bitmap.copy(Config.ARGB_8888, true);
		bitmap.recycle();

		Canvas myCanvas = new Canvas(mutableBitmap);

		int myColor = mutableBitmap.getPixel(0,0);
		ColorFilter filter = new LightingColorFilter(myColor, color);

		Paint pnt = new Paint();
		pnt.setColorFilter(filter);
		myCanvas.drawBitmap(mutableBitmap,0,0,pnt);

		return mutableBitmap;
	}
}
