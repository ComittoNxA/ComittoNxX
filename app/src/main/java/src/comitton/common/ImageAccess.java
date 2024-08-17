package src.comitton.common;

//import com.larvalabs.svgandroid.SVG;
//import com.larvalabs.svgandroid.SVGParser;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.RenderOptions;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.Log;

import jp.dip.muracoro.comittonx.R;
import src.comitton.stream.CallImgLibrary;
import src.comitton.stream.ImageData;
import src.comitton.stream.ImageManager;


public class ImageAccess {
	public static final int BMPALIGN_LEFT = 0;
	public static final int BMPALIGN_CENTER = 1;
	public static final int BMPALIGN_RIGHT = 2;
	public static final int BMPALIGN_AUTO = 3;

	// ビットマップをリサイズして切り出し
	public static Bitmap resizeTumbnailBitmap(Bitmap bm, int thum_cx, int thum_cy, int align) {
		if (bm == null || bm.isRecycled()) {
			return null;
		}

		//Log.d("comitton", "resizeTumbnailBitmap thum_cx=" + thum_cx + ", thum_cy=" + thum_cy + ", align=" + align);
		int src_cx = bm.getWidth();
		int src_cy = bm.getHeight();

		int x = 0;
		int y = 0;

		if (align == BMPALIGN_AUTO) {
			// 余白を削除する
			int CutL = 0;
			int CutR = 0;
			int CutT = 0;
			int CutB = 0;

			int CheckCX = (int) (src_cx * 0.3);
			int CheckCY = (int) (src_cy * 0.3);
			int xx;
			int	yy;
			int overcnt;


			for (yy = 0 ; yy < CheckCY ; yy ++) {
				//Log.d("comitton", "resizeTumbnailBitmap yy=" + yy);
				overcnt = 0;	// 白でないカウンタ
				CutT = yy;
				for (xx = 0 ; xx < src_cx ; xx ++) {
					// 白チェック
					if (bm.getPixel(xx, yy) != Color.WHITE) {
						overcnt ++;
					}
				}
				// 0.8%以上がオーバーしたら余白ではないとする
				if (overcnt >= src_cx * 0.008) {
					// 0.8%以上
					break;
				}
			}
			for (yy = src_cy - 1 ; yy >= src_cy - CheckCY ; yy --) {
				//Log.d("comitton", "resizeTumbnailBitmap yy=" + yy);
				overcnt = 0;	// 白でないカウンタ
				CutB = src_cy - 1 - yy;
				for (xx = 0 ; xx < src_cx ; xx ++) {
					// 白チェック
					if (bm.getPixel(xx, yy) != Color.WHITE) {
						overcnt ++;
					}
				}
				// 0.8%以上がオーバーしたら余白ではないとする
				if (overcnt >= src_cx * 0.008) {
					// 0.8%以上
					break;
				}
			}

			for (xx = 0 ; xx < CheckCX ; xx ++) {
				//Log.d("comitton", "resizeTumbnailBitmap xx=" + xx);
				overcnt = 0;	// 白でないカウンタ
				CutL = xx;
				for (yy = CutT + 1 ; yy < src_cy - CutB ; yy ++) {
					// 白チェック
					if (bm.getPixel(xx, yy) != Color.WHITE) {
						overcnt ++;
					}
				}
				// 0.8%以上がオーバーしたら余白ではないとする
				if (overcnt >= (src_cy - CutT - CutB) * 0.008) {
					// 0.8%以上
					break;
				}
			}
			for (xx = src_cx - 1 ; xx >= src_cx - CheckCX ; xx --) {
				//Log.d("comitton", "resizeTumbnailBitmap xx=" + xx);
				overcnt = 0;	// 白でないカウンタ
				CutR = src_cx - 1 - xx;
				for (yy = CutT + 1 ; yy < src_cy - CutB ; yy ++) {
					// 白チェック
					if (bm.getPixel(xx, yy) != Color.WHITE) {
						overcnt ++;
					}
				}
				// 0.8%以上がオーバーしたら余白ではないとする
				if (overcnt >= (src_cy - CutT - CutB) * 0.008) {
					// 0.8%以上
					break;
				}
			}

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

	// SVGファイルからアイコンを作成(正方形)
	public static Bitmap createIcon(Resources res, int resid, int size, Integer drawcolor) {
		return createIcon(res, resid, size, size, drawcolor);
	}

	// SVGファイルからアイコンを作成(比率不定)
	public static Bitmap createIcon(Resources res, int resid, int sizeW, int sizeH, Integer drawcolor) {
		// bitmap設定
		Bitmap bm = null;
		try {
			bm = Bitmap.createBitmap(sizeW, sizeH, Config.ARGB_4444);
		}
		catch (Exception ex) {
			// 読み込み失敗
		}
		if (bm == null) {
			return null;
		}
		try {
			Canvas canvas = new Canvas(bm);
			SVG svg = SVG.getFromResource(res, resid);
			Picture picture = svg.renderToPicture();
			int w = picture.getWidth();
			int h = picture.getHeight();
			float dsW = (float)sizeW / (float)w;
			float dsH = (float)sizeH / (float)h;
			canvas.scale(dsW, dsH);
			svg.renderToCanvas(canvas);
			if (drawcolor != null) {
				bm = setColor(bm,drawcolor);
			}
		}
		catch (Exception ex) {
			// 読み込み失敗
		}

		return bm;
	}

	// SVGファイルからアイコンを作成
	public static Bitmap createIconFromRawPicture(Resources res, int resid, int size) {
		Bitmap bm = null;
		try {
			// bitmap設定
			bm = Bitmap.createBitmap(size, size, Config.ARGB_4444);
			if (bm == null) {
				return null;
			}

			Canvas canvas = new Canvas(bm);
			SVG svg = SVG.getFromResource(res, resid);
			Picture picture = svg.renderToPicture();
			int w = picture.getWidth();
			int h = picture.getHeight();
			float dsW = (float)size / (float)w;
			float dsH = (float)size / (float)h;
			canvas.scale(dsW, dsH);
			svg.renderToCanvas(canvas);
		}
			catch (Exception ex) {
			// 読み込み失敗
		}

		return bm;
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
