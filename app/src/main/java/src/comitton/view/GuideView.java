package src.comitton.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.view.View;

public class GuideView {
	private static final int FROMTYPE_ERROR = -1;
	private static final int FROMTYPE_NONE = 0;
	private static final int FROMTYPE_MEM = 1;
	private static final int FROMTYPE_CACHE = 2;
	private static final int FROMTYPE_LOCAL = 3;
	private static final int FROMTYPE_SERVER = 4;

	private static final int NOISESTATE_TON = 1;
	private static final int NOISESTATE_TOO = 2;
	private static final int NOISESTATE_YET = 4;

	public static final int PAGEPOS_TL = 0; // ページ番号表示位置
	public static final int PAGEPOS_TC = 1;
	public static final int PAGEPOS_TR = 2;
	public static final int PAGEPOS_BL = 3;
	public static final int PAGEPOS_BC = 4;
	public static final int PAGEPOS_BR = 5;

	public static final int GUIDE_NONE = -2;
	public static final int GUIDE_NOSEL = -1;

	public static final int GUIDE_BLEFT = 0;
	public static final int GUIDE_BCENTER = 1;
	public static final int GUIDE_BRIGHT = 2;
	public static final int GUIDE_BOTTOM_NUM = 3;

	private UpdateListener mListener = null;
	private View mParentView;
	private Context mContext;

	private String mLoadingStr;
	private int mFromType;

	private int mGuideColor1;
	private int mGuideColor2;
	private int mButtonSize;
	private int mTapPattern;
	private int mTapRate;
	private int mSelIndex;
	private String mGuideText = "";
	private boolean mChgPage;
	private boolean mOldMenu;

	private int mBattery;

	private String mTopCommandStr[];

	private class CommandInfo {
		public String str;
		public Rect rect;
		public int id;
		public boolean disp;
		public boolean old;

		public CommandInfo(String s, Rect rc, int id, boolean d, boolean o) {
			this.str = s;
			this.rect = rc;
			this.id = id;
			this.disp = d;
			this.old = o;
		}
	}
	private CommandInfo mCommand[];
	private int mCommandIndex;
	private int mCommandSize;
	private boolean mCommandOn;

	private boolean mIsTopCommand = true;

	private Bitmap mBackBitmap;
	private Bitmap mMenuBitmap;
	private Bitmap mRightBitmap;
	private Bitmap mLeftBitmap;

	private byte mNoiseState[] = new byte[4];
	private int mNoiseLevel;

	private int mCacheMark;

	private Bitmap mIconServer;
	private Bitmap mIconLocal;
	private Bitmap mIconCache;
	private Bitmap mIconMemory;

	private Bitmap mIconSmpl;
	private Bitmap mIconMic;
	private Bitmap mIconTon;
	private Bitmap mIconToo;
	private Bitmap mIconYet;

	private String mPageText;
	private int mPageColor;

	private boolean mPageDisplay = false;
	private String mPageStr;
	private int mPageFormatInt;
	private int mPagePos;
	private int mPageSize;
	private int mPnumColor = 0;
	private boolean mTimeDisplay = false;
	private int mTimeFormatInt;
	private String mTimeStr;
	private int mTimePos;
	private int mTimeSize;
	private int mTimeColor = 0;
	private Intent mBatteryIntent;

	private boolean mDualView;	// 並べて表示
	private boolean mNextFile;	// 次/前ファイル移動表示
	private boolean mIsRtoL;	// 右表示か
	private int mPageMode;		// ページ選択モード
	private boolean mImmEnable;	// IMMERSIVEモード

	private int mFirstX;
	private int mFirstY;
	private int mFirstCX;
	private int mFirstCY;

	private boolean mOperationMode;

	private Paint mDrawPaint;
	private Paint mTextPaint;
	private Paint mFramePaint;

	private float mTextShadow;

	// Loading表示
	private Point mLoadPoint[];
	private Paint mLoadPaint;
	private Paint mPagePaint;
	private Paint mNumPaint;
	private Paint mTimePaint;
	private int mLoadingCount = -1;
	private int mLoadColor[];

	SimpleDateFormat mDateFormat;
	SimpleDateFormat mTimeFormat;

	// コンストラクタ
	public GuideView(Context context) {
//		setBackgroundColor(Color.argb(0, 0, 0, 0));
		mContext = context;
		mCacheMark = 0;
		Resources res = context.getResources();
		float density = res.getDisplayMetrics().scaledDensity;

		mIconServer = BitmapFactory.decodeResource(res, R.drawable.ic_ld_server);
		mIconLocal = BitmapFactory.decodeResource(res, R.drawable.ic_ld_local);
		mIconCache = BitmapFactory.decodeResource(res, R.drawable.ic_ld_cache);
		mIconMemory = BitmapFactory.decodeResource(res, R.drawable.ic_ld_memory);

		mIconSmpl = BitmapFactory.decodeResource(res, R.drawable.ic_mic_sample);
		mIconMic = BitmapFactory.decodeResource(res, R.drawable.ic_mic_mic);
		mIconTon = BitmapFactory.decodeResource(res, R.drawable.ic_mic_ton);
		mIconToo = BitmapFactory.decodeResource(res, R.drawable.ic_mic_too);
		mIconYet = BitmapFactory.decodeResource(res, R.drawable.ic_mic_yet);

		mLoadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLoadPaint.setStyle(Paint.Style.FILL);

		mLoadColor = new int[8];
		for (int i = 0; i < 8; i++) {
			mLoadColor[i] = DEF.margeColor(0x005F5F5F, 0x00E0E0E0, i, 8 - 1);
		}

		mPagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPagePaint.setTextAlign(Paint.Align.CENTER);
		mPagePaint.setStrokeWidth(1.0f);
		mPagePaint.setStyle(Paint.Style.FILL);
		mPagePaint.setColor(Color.WHITE);

		mNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mNumPaint.setTypeface(Typeface.MONOSPACE);
		mTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mTimePaint.setTypeface(Typeface.MONOSPACE);


		mDrawPaint = new Paint();
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setStrokeWidth(1.0f);

		mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFramePaint.setStyle(Paint.Style.STROKE);
		mFramePaint.setColor(Color.BLACK);

		mTextShadow = 2.0f * density;

		mSelIndex  = GUIDE_NONE;
		mCommandIndex = -1;
		mCommand = null;

		mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		mTimeFormat = new SimpleDateFormat("HH:mm");
	}

	public void draw(Canvas canvas, int cx, int cy) {
		//
		if (mCacheMark != 0) {
			int pos = (cx + cy) / 320;

			mDrawPaint.setStyle(Paint.Style.FILL);
			mDrawPaint.setColor(Color.WHITE);
			canvas.drawCircle(pos, pos, pos * 3 / 4, mDrawPaint);
			switch (mCacheMark) {
				case 1:
					mDrawPaint.setColor(Color.rgb(0, 0, 128));
					break;
				case 2:
					mDrawPaint.setColor(Color.rgb(255, 0, 0));
					break;
				case 3:
					mDrawPaint.setColor(Color.rgb(0, 255, 0));
					break;
			}
			canvas.drawCircle(pos, pos, pos / 2, mDrawPaint);
		}

		if (mOperationMode == true) {
			// 操作説明描画
			drawOperation(canvas, cx, cy);
			return;
		}

		int tcx = (cx + cy) / 48;
		Resources res = mContext.getResources();

		// ページ番号表示
		if (mPageDisplay && mPageStr != null) {
			int psize = (cx + cy) / 80;
			int psx, psy;
			switch (mPagePos) {
				case PAGEPOS_TL:
				case PAGEPOS_BL:
					psx = psize / 2;
					mNumPaint.setTextAlign(Paint.Align.LEFT);
					break;
				case PAGEPOS_TC:
				case PAGEPOS_BC:
					psx = cx / 2;
					mNumPaint.setTextAlign(Paint.Align.CENTER);
					break;
				case PAGEPOS_TR:
				case PAGEPOS_BR:
				default:
					psx = cx - psize / 2;
					mNumPaint.setTextAlign(Paint.Align.RIGHT);
					break;
			}

			switch (mPagePos) {
				case PAGEPOS_TL:
				case PAGEPOS_TC:
				case PAGEPOS_TR:
					psy = psize / 2;
					break;
				case PAGEPOS_BL:
				case PAGEPOS_BC:
				case PAGEPOS_BR:
				default:
					psy = cy - mPageSize - psize / 2;
					break;
			}

			FontMetrics fm = mNumPaint.getFontMetrics();
			int ascent = (int) (-fm.ascent);

			mNumPaint.setStrokeWidth(mTextShadow);
			mNumPaint.setStyle(Paint.Style.STROKE);
			if (mPnumColor == 0) {
				mNumPaint.setColor(0x88000000);
			}
			else {
				mNumPaint.setColor(0xCCFFFFFF);
			}
			canvas.drawText(mPageStr, psx, psy + ascent, mNumPaint);

			mNumPaint.setStrokeWidth(0.0f);
			mNumPaint.setStyle(Paint.Style.FILL);
			if (mPnumColor == 0) {
				mNumPaint.setColor(Color.WHITE);
			}
			else {
				mNumPaint.setColor(Color.BLACK);
			}
			canvas.drawText(mPageStr, psx, psy + ascent, mNumPaint);
		}

		// 時刻＆バッテリー表示
		setTimeString();
		if (mTimeDisplay) {
			int psize = (cx + cy) / 80;
			int psx, psy;
			switch (mTimePos) {
				case PAGEPOS_TL:
				case PAGEPOS_BL:
					psx = psize / 2;
					mTimePaint.setTextAlign(Paint.Align.LEFT);
					break;
				case PAGEPOS_TC:
				case PAGEPOS_BC:
					psx = cx / 2;
					mTimePaint.setTextAlign(Paint.Align.CENTER);
					break;
				case PAGEPOS_TR:
				case PAGEPOS_BR:
				default:
					psx = cx - psize / 2;
					mTimePaint.setTextAlign(Paint.Align.RIGHT);
					break;
			}

			switch (mTimePos) {
				case PAGEPOS_TL:
				case PAGEPOS_TC:
				case PAGEPOS_TR:
					psy = psize / 2;
					break;
				case PAGEPOS_BL:
				case PAGEPOS_BC:
				case PAGEPOS_BR:
				default:
					psy = cy - mTimeSize - psize / 2;
					break;
			}

			FontMetrics fm = mTimePaint.getFontMetrics();
			int ascent = (int) (-fm.ascent);

			mTimePaint.setStrokeWidth(mTextShadow);
			mTimePaint.setStyle(Paint.Style.STROKE);
			if (mTimeColor == 0) {
				mTimePaint.setColor(0x88000000);
			}
			else {
				mTimePaint.setColor(0xCCFFFFFF);
			}
			canvas.drawText(mTimeStr, psx, psy + ascent, mTimePaint);

			mTimePaint.setStrokeWidth(0.0f);
			mTimePaint.setStyle(Paint.Style.FILL);
			if (mTimeColor == 0) {
				mTimePaint.setColor(Color.WHITE);
			}
			else {
				mTimePaint.setColor(Color.BLACK);
			}
			canvas.drawText(mTimeStr, psx, psy + ascent, mTimePaint);
		}

		// 描画
		mDrawPaint.setStyle(Paint.Style.FILL);

		CommandInfo cmd[] = mCommand;
		int cmdidx = mCommandIndex;
		int cmdsize = mCommandSize;
		boolean cmdon = mCommandOn;

		mTextPaint.setTextSize(cmdsize);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mFramePaint.setTextSize(cmdsize);
		mFramePaint.setTypeface(Typeface.DEFAULT);
		mFramePaint.setTextAlign(Paint.Align.CENTER);
		mFramePaint.setStrokeWidth(mTextShadow);

		String str = null;

		// 上部/下部タッチのコマンド選択描画
		if (cmd != null) {
			for (int i = 0 ; i < cmd.length ; i ++) {
				if (cmdon == true || cmd[i].disp == true) {
					// 選択中の物は色を変える
					if (i == cmdidx) {
						mDrawPaint.setColor(mGuideColor1);
					}
					else {
						mDrawPaint.setColor(mGuideColor2);
					}
					canvas.drawRect(cmd[i].rect, mDrawPaint);
					if ((cmd[i].id & 0x4000) != 0) {
						// 戻る/メニューボタン
						Bitmap bm;
						switch (cmd[i].id) {
							case 0x4000:
								bm = mBackBitmap;
								break;
							case 0x4001:
								bm = mMenuBitmap;
								break;
							case 0x4003:
								bm = mLeftBitmap;
								break;
							case 0x4002:
								bm = mRightBitmap;
								break;
							default:
								 continue;
						}
						if (bm == null) {
							// ビットマップなし
							continue;
						}
						int bmcx = bm.getWidth();
						int bmcy = bm.getHeight();
						int areacx = cmd[i].rect.right - cmd[i].rect.left;
						int areacy = cmd[i].rect.bottom - cmd[i].rect.top;
						int bmx = cmd[i].rect.left + (areacx - bmcx) / 2;
						int bmy = cmd[i].rect.top + (areacy - bmcy) / 2;
						mDrawPaint.setColor(Color.WHITE);
						canvas.drawBitmap(bm, bmx, bmy, mDrawPaint);
					}
					else if (cmd[i].old == false) {
						// テキストを描画
						int txtcy = (mButtonSize - cmdsize) / 2 + cmdsize * 4 / 5;
						canvas.drawText(cmd[i].str, (cmd[i].rect.left + cmd[i].rect.right) / 2, cmd[i].rect.top + txtcy, mFramePaint);
						canvas.drawText(cmd[i].str, (cmd[i].rect.left + cmd[i].rect.right) / 2, cmd[i].rect.top + txtcy, mTextPaint);
					}
					else if (i == cmdidx) {
						// 中央に表示するテキストを設定
						str = cmd[i].str;
					}
				}
			}

			// 上部コマンド選択中はバッテリーと時刻を表示
			if (mIsTopCommand) {
				// テキストを描画
				mTextPaint.setTextAlign(Paint.Align.RIGHT);
				mFramePaint.setTextAlign(Paint.Align.RIGHT);

				int txtcy = (mButtonSize - cmdsize) / 2 + cmdsize * 4 / 5;

				// バッテリー
				String strwk = "Battery : " + mBattery +  "%";
				int strx = cx - cmdsize / 2;
				int stry = cy - cmdsize / 2;
				canvas.drawText(strwk, strx, stry, mFramePaint);
				canvas.drawText(strwk, strx, stry, mTextPaint);

				// 時刻
				strwk = mDateFormat.format(new Date());
				stry -= cmdsize + cmdsize / 5;
				canvas.drawText(strwk, strx, stry, mFramePaint);
				canvas.drawText(strwk, strx, stry, mTextPaint);
			}
		}

		if (mSelIndex != GUIDE_NONE) {
			Rect rc = new Rect(0, cy - mButtonSize, cx, cy);
			for (int i = 0; i < GUIDE_BOTTOM_NUM; i++) {
				// 選択中の物は色を変える
				if (mSelIndex == i) {
					mDrawPaint.setColor(mGuideColor1);
				}
				else {
					mDrawPaint.setColor(mGuideColor2);
				}
				switch (i) {
					case GUIDE_BLEFT:// 先頭ページ
						rc.set(0, cy - mButtonSize + 1, mButtonSize - 1, cy);
						break;
					case GUIDE_BCENTER:// ページ選択
						rc.set(mButtonSize, cy - mButtonSize + 1, cx - mButtonSize, cy);
						break;
					case GUIDE_BRIGHT:// 最終ページ
						rc.set(cx - mButtonSize + 1, cy - mButtonSize + 1, cx, cy);
						break;
				}
				canvas.drawRect(rc, mDrawPaint);
			}
		}

		if (mGuideText != null && mGuideText.length() > 0) {
			str = mGuideText;
		}
		else if (str == null) {
    		if ((mSelIndex == GUIDE_BLEFT && !mIsRtoL) || (mSelIndex == GUIDE_BRIGHT && mIsRtoL)) {
    			// 先頭ページ
    			str = res.getString(R.string.pageTop);
    		}
    		else if ((mSelIndex == GUIDE_BLEFT && mIsRtoL) || (mSelIndex == GUIDE_BRIGHT && !mIsRtoL)) {
    			// 最終ページ
    			str = res.getString(R.string.pageLast);
    		}
		}

		mTextPaint.setTextSize(tcx);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mFramePaint.setTextSize(tcx);
		mFramePaint.setTypeface(Typeface.DEFAULT_BOLD);
		mFramePaint.setTextAlign(Paint.Align.CENTER);

		// 中央にテキスト描画
		if (str != null) {
			canvas.drawText(str, cx / 2, (cy - tcx) / 2, mFramePaint);
			canvas.drawText(str, cx / 2, (cy - tcx) / 2, mTextPaint);
		}

		// 上部にファイル名とページ情報を描画
		String pageline[] = getPageLine(mPageText);
		if (pageline != null) {
			int pagecy = (cx + cy) / 80;	// ページ数表示の高さ

			//
			mDrawPaint.setColor(mPageColor);

			canvas.drawRect(0, 0, cx, pagecy * pageline.length + pagecy / 3, mDrawPaint);
			for (int line = 0; line < pageline.length; line++) {
				if (pageline[line] != null) {
					canvas.drawText(pageline[line], cx / 2, pagecy * (line + 1), mPagePaint);
				}
			}
		}

		// 音操作の描画内容を準備
		if (mNoiseState[0] > 1) {
			canvas.drawBitmap(mIconSmpl, cx - 16 * 4 - 2, 2, mDrawPaint);
		}
		else if (mNoiseState[0] == 1) {
			canvas.drawBitmap(mIconMic, cx - 16 * 4 - 2, 2, mDrawPaint);
			if (mNoiseLevel >= 0) {
				mFramePaint.setTextSize(16);
				mFramePaint.setTypeface(Typeface.MONOSPACE);
				mFramePaint.setTextAlign(Paint.Align.RIGHT);
				mFramePaint.setStrokeWidth(mTextShadow);
				canvas.drawText("" + mNoiseLevel, cx - 4, 32, mFramePaint);

				mTextPaint.setTextSize(16);
				mTextPaint.setTypeface(Typeface.MONOSPACE);
				mTextPaint.setTextAlign(Paint.Align.RIGHT);
				canvas.drawText("" + mNoiseLevel, cx - 4, 32, mTextPaint);
			}
		}
		// 音操作の描画
		for (int i = 1; i < mNoiseState.length; i++) {
			if (mNoiseState[i] == 0) {
				break;
			}
			else {
				// 状態
				Bitmap bm = null;
				switch (mNoiseState[i]) {
					case NOISESTATE_TON:
						bm = mIconTon;
						break;
					case NOISESTATE_TOO:
						bm = mIconToo;
						break;
					case NOISESTATE_YET:
						bm = mIconYet;
						break;
				}
				canvas.drawBitmap(bm, cx - 16 * (4 - i) - 2, 2, mDrawPaint);
			}
		}
	}

	public void drawLoading(Canvas canvas, int cx, int cy) {
		if (mLoadPoint == null || ((cx < cy) != (mLoadPoint[0].x < mLoadPoint[0].y))) {
			mLoadPoint = new Point[8];
			int x = cx / 2;
			int y = cy / 2;
			int r1 = (cx < cy ? cx : cy) / 16;
			int r2 = r1 * 7 / 10;

			mLoadPoint[0] = new Point(x, y - r1);
			mLoadPoint[1] = new Point(x - r2, y - r2);
			mLoadPoint[2] = new Point(x - r1, y);
			mLoadPoint[3] = new Point(x - r2, y + r2);
			mLoadPoint[4] = new Point(x, y + r1);
			mLoadPoint[5] = new Point(x + r2, y + r2);
			mLoadPoint[6] = new Point(x + r1, y);
			mLoadPoint[7] = new Point(x + r2, y - r2);
		}

		// ローディング中
		if (mLoadingCount >= 0) {
			int radius = (cx < cy ? cx : cy) / 120;
			for (int i = 0; i < 8; i++) {
				mLoadPaint.setColor(mLoadColor[(i + mLoadingCount) % 8]);
				canvas.drawCircle((float) mLoadPoint[i].x, (float) mLoadPoint[i].y, radius, mLoadPaint);
			}
		}

		if (mFromType != FROMTYPE_NONE) {
			int str_x;
			if (mFromType != FROMTYPE_ERROR) {
				Bitmap bm = null;
				switch (mFromType) {
					case FROMTYPE_MEM:
						bm = mIconMemory;
						break;
					case FROMTYPE_CACHE:
						bm = mIconCache;
						break;
					case FROMTYPE_LOCAL:
						bm = mIconLocal;
						break;
					case FROMTYPE_SERVER:
						bm = mIconServer;
						break;
				}
				canvas.drawBitmap(bm, 2, 2, mDrawPaint);
				str_x = 34;
			}
			else {
				str_x = 2;
			}
			String loadingStr = mLoadingStr;
			if (loadingStr != null) {
				mTextPaint.setTypeface(Typeface.MONOSPACE);
				mTextPaint.setTextSize(16);
				mTextPaint.setTextAlign(Paint.Align.LEFT);

				mFramePaint.setTypeface(Typeface.MONOSPACE);
				mFramePaint.setTextSize(16);
				mFramePaint.setTextAlign(Paint.Align.LEFT);
				mFramePaint.setStrokeWidth(mTextShadow);

				canvas.drawText(loadingStr, str_x, 16, mFramePaint);
				canvas.drawText(loadingStr, str_x, 16, mTextPaint);
			}
		}
	}

	private void drawOperation(Canvas canvas, int width, int height) {
		// 背景色の設定
		canvas.drawColor(0x60000000);
		int tcx = (width + height) / 142 * 2;
		int margin = 0;

		Resources res = mContext.getResources();
		String tope01 = res.getString(R.string.topMenu); // 上部メニュー
		String bope01 = res.getString(R.string.pageSelect); // ページ選択
		String mope01 = res.getString(R.string.prevPage); // 左ページ
		String mope02 = res.getString(R.string.nextPage); // 次ページ

		if (mButtonSize > tcx) {
			margin = (mButtonSize - tcx) / 2;
		}

		Paint drwPaint = new Paint();
		// 上部
		drwPaint.setColor(Color.WHITE);
		drwPaint.setStrokeWidth(1);
		for (int i = 1; i <= 3; i++) {
			canvas.drawLine(width * i / 4, 0, width * i / 4, mButtonSize, drwPaint); // 左の縦線
		}
		drwPaint.setStrokeWidth(2);
		canvas.drawLine(0, mButtonSize, width, mButtonSize, drwPaint); // 横線

		Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		txtPaint.setTextSize(tcx);
		txtPaint.setColor(Color.WHITE);

		float rate = mTapRate + 1;
		float rcx = width / 10.0f;
		float rcy = (height - mButtonSize * 2) / 10.0f;
		int l1 = 0;
		int l2 = 0;
		switch (mTapPattern) {
			case 0: {
				// 左右の区切り
				l1 = (int) (width - rcx * rate);
				canvas.drawLine(l1, mButtonSize, l1, height - mButtonSize, drwPaint);

				if (l1 < tcx * 4) {
					txtPaint.setTextAlign(Paint.Align.LEFT);
					drawTextShadow(canvas, mChgPage ? mope02 : mope01, 10, (height - tcx) / 2, txtPaint);
				}
				else {
					txtPaint.setTextAlign(Paint.Align.CENTER);
					drawTextShadow(canvas, mChgPage ? mope02 : mope01, l1 / 2, (height - tcx) / 2, txtPaint);
				}
				if (width - l1 < tcx * 4) {
					txtPaint.setTextAlign(Paint.Align.RIGHT);
					drawTextShadow(canvas, mChgPage ? mope01 : mope02, width - 10, (height - tcx) / 2, txtPaint);
				}
				else {
					txtPaint.setTextAlign(Paint.Align.CENTER);
					drawTextShadow(canvas, mChgPage ? mope01 : mope02, l1 + (width - l1) / 2, (height - tcx) / 2, txtPaint);
				}
				break;
			}
			case 1:
				l1 = (int) (rcx * rate / 2);
				l2 = (int) (width - rcx * rate / 2);
				canvas.drawLine(l1, mButtonSize, l1, height - mButtonSize, drwPaint);
				canvas.drawLine(l2, mButtonSize, l2, height - mButtonSize, drwPaint);

				if (l1 < tcx * 4) {
					txtPaint.setTextAlign(Paint.Align.LEFT);
					drawTextShadow(canvas, mChgPage ? mope01 : mope02, 10, (height - tcx) / 2, txtPaint);
					txtPaint.setTextAlign(Paint.Align.RIGHT);
					drawTextShadow(canvas, mChgPage ? mope01 : mope02, width - 10, (height - tcx) / 2, txtPaint);
				}
				else {
					txtPaint.setTextAlign(Paint.Align.CENTER);
					drawTextShadow(canvas, mChgPage ? mope01 : mope02, l1 / 2, (height - tcx) / 2, txtPaint);
					drawTextShadow(canvas, mChgPage ? mope01 : mope02, l2 + l1 / 2, (height - tcx) / 2, txtPaint);
				}

				txtPaint.setTextAlign(Paint.Align.CENTER);
				drawTextShadow(canvas, mChgPage ? mope02 : mope01, width / 2, (height - tcx) / 2, txtPaint);
				break;
			case 2:
				l1 = (int) (height - mButtonSize - rcy * rate);
				canvas.drawLine(0, l1, width, l1, drwPaint);

				txtPaint.setTextAlign(Paint.Align.CENTER);
				drawTextShadow(canvas, mChgPage ? mope02 : mope01, width / 2, mButtonSize + (l1 - mButtonSize + tcx) / 2, txtPaint);
				drawTextShadow(canvas, mChgPage ? mope01 : mope02, width / 2, l1 + (height - mButtonSize - l1 + tcx) / 2, txtPaint);
				break;
			case 3:
				l1 = (int) (mButtonSize + rcy * rate / 2);
				l2 = (int) (height - mButtonSize - rcy * rate / 2);
				canvas.drawLine(0, l1, width, l1, drwPaint);
				canvas.drawLine(0, l2, width, l2, drwPaint);

				txtPaint.setTextAlign(Paint.Align.CENTER);
				drawTextShadow(canvas, mChgPage ? mope01 : mope02, width / 2, mButtonSize + (l1 - mButtonSize + tcx) / 2, txtPaint);
				drawTextShadow(canvas, mChgPage ? mope01 : mope02, width / 2, l2 + (height - l2 - mButtonSize + tcx) / 2, txtPaint);
				drawTextShadow(canvas, mChgPage ? mope02 : mope01, width / 2, (height - tcx) / 2, txtPaint);
				break;
		}

		// 下部
		canvas.drawLine(0, height - mButtonSize, width, height - mButtonSize, drwPaint);
		drwPaint.setStrokeWidth(1);
		canvas.drawLine(mButtonSize, height - mButtonSize, mButtonSize, height, drwPaint);
		canvas.drawLine(width - mButtonSize, height - mButtonSize, width - mButtonSize, height, drwPaint);

		txtPaint.setTextAlign(Paint.Align.CENTER);

		if (mButtonSize > 0) {
			drawTextShadow(canvas, tope01, width / 2, margin + tcx, txtPaint);
			drawTextShadow(canvas, bope01, width / 2, height - margin, txtPaint);
		}
		return;
	}

	private void drawTextShadow(Canvas canvas, String str, int x, int y, Paint text) {
		// 操作説明
		text.setStrokeWidth(mTextShadow);
		text.setStyle(Paint.Style.STROKE);
		text.setColor(Color.BLACK);
		canvas.drawText(str, x, y, text);

		text.setStrokeWidth(1.0f);
		text.setStyle(Paint.Style.FILL);
		text.setColor(Color.WHITE);
		canvas.drawText(str, x, y, text);
	}

	// 上部コマンドの文字列設定
	public void setTopCommandStr(String [] str) {
		mTopCommandStr = str;
	}

	// ガイド色の設定
	public void setColor(int color1, int color2, int color3) {
		mGuideColor1 = color1;
		mGuideColor2 = color2;
		return;
	}

	// ガイドのサイズ設定
	public void setGuideSize(int size, int pattern, int rate, boolean chgPage, boolean oldmenu) {
		mButtonSize = size;
		mTapPattern = pattern;
		mTapRate = rate;
		mChgPage = chgPage;
		mOldMenu = oldmenu;

		Resources res = mContext.getResources();
		mBackBitmap = ImageAccess.createIcon(res, R.raw.navi_back, size * 2 / 3, Color.WHITE);
		mMenuBitmap = ImageAccess.createIcon(res, R.raw.navi_menu, size * 2 / 3, Color.WHITE);
		mLeftBitmap = ImageAccess.createIcon(res, R.raw.navi_left, size * 2 / 3, Color.WHITE);
		mRightBitmap = ImageAccess.createIcon(res, R.raw.navi_right, size * 2 / 3, Color.WHITE);
//		invalidate();
		return;
	}

	// キャッシュ読み込み通知
	public void setCacheMark(int mark) {
//		Log.d("setCacheMark", "mark=" + mark);
		if (mCacheMark != mark) {
			mCacheMark = mark;
			invalidate();
		}
	}

	// ガイドのサイズ設定
	public void setNoiseState(int state, int level) {
		mNoiseState[0] = (byte) (state & 0xFF);
		mNoiseState[1] = (byte) ((state >> 8) & 0xFF);
		mNoiseState[2] = (byte) ((state >> 16) & 0xFF);
		mNoiseState[3] = (byte) ((state >> 24) & 0xFF);
		mNoiseLevel = level;
		invalidate();
	}

	public void setGuideMode(boolean dual, boolean file, boolean isRtoL, int pagemode, boolean imm) {
		mDualView = dual;
		mNextFile = file;
		mIsRtoL = isRtoL;
		mPageMode = pagemode;
		mImmEnable = imm;
	}

	// 描画するガイドの設定
	public void setGuideIndex(int idx) {
		// 上部と下部のガイドの選択位置変更チェック
		if (mSelIndex != idx) {
			// 位置を保持
			mSelIndex = idx;
			this.invalidate();
		}
		return;
	}

	// downイベント
	public void eventTouchDown(int x, int y, int cx, int cy, boolean enable) {
		mFirstX = x;
		mFirstY = y;
		mFirstCX = cx;
		mFirstCY = cy;
		mCommandOn = enable;

		if (mButtonSize <= 0) {
			return;
		}

		setButtonLayout();

		// 選択ボタン設定
		mCommandIndex = findButton(x, y);

		// バッテリー残量(%)
		Intent battery = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int level = battery.getIntExtra("level", 0);
		int scale = battery.getIntExtra("scale", 100);
		mBattery = level * 100 / scale;

		this.invalidate();
		return;
	}

	public void eventTouchTimer() {
		// ボタンを有効にする
		mCommandOn = true;

		this.invalidate();
		return;
	}

	// ボタン配置
	private void setButtonLayout() {
		CommandInfo cmdinfo[] = null;
		Rect rc_wk;
		int id_wk;
		String str_wk;

		int width;
		int cmdsize;

		if (mFirstCX < mFirstCY) {
			width = mFirstCX * 7 / 16;
		}
		else {
			width = mFirstCY * 7 / 16;
		}

		cmdsize = width / 12;

		mIsTopCommand = false;

		// ボタン設定
		if (mFirstY <= mButtonSize) {
			mIsTopCommand = true;
			if (mTopCommandStr != null) {
				// 画面上部タッチ
				int cmdnum;
				cmdnum = mTopCommandStr.length;
				int allnum = cmdnum + 2;

				cmdinfo = new CommandInfo[allnum];

				if (mOldMenu == true) {
					// 旧形式
					// メニュー形式
					int rownum;
					int basex = mButtonSize + 1;// mButtonSize;
					int basey = 0;// mButtonSize;
					// 画面の方向で表示する個数を変更
					if (mFirstCX < mFirstCY) {
						// 縦長
						rownum = 4;
					}
					else {
						// 横長
						rownum = 8;
					}
					if (rownum > cmdnum) {
						rownum = cmdnum;
					}

					// ボタン位置等を設定
					// 戻るを設定
					id_wk = 0x4000;
					rc_wk = new Rect(0, basey, mButtonSize, basey + mButtonSize);
					cmdinfo[0] = new CommandInfo(null, rc_wk, id_wk, true, true);

					// メニューを設定
					id_wk = 0x4001;
					rc_wk = new Rect(mFirstCX - mButtonSize, basey, mFirstCX, basey + mButtonSize);
					cmdinfo[1] = new CommandInfo(null, rc_wk, id_wk, true, true);

					int index = 2;
					int posx1;
					int posx2;
					int iwk;

					width = mFirstCX - (mButtonSize + 1) * 2;
					for (int i = 0 ; i < cmdnum ; i ++) {
						// ボタンのIDを設定
						id_wk = (0x8000 | i);
						str_wk = mTopCommandStr[i];

						iwk = i % rownum;
						posx1 = basex + width * iwk / rownum;
						if (iwk == rownum - 1) {
							posx2 = mFirstCX - mButtonSize - 1;
						}
						else {
							posx2 = basex + width * (iwk + 1) / rownum - 1;
						}

						// ボタン位置を計算
						rc_wk = new Rect(posx1, basey, posx2, basey + mButtonSize);
						// 設定
						cmdinfo[index] = new CommandInfo(str_wk, rc_wk, id_wk, true, true);

						if (i % rownum == rownum - 1) {
							// 改行
							basey += mButtonSize + 1;
						}
						index ++;
					}
				}
				else {
					// メニュー形式
					int basex;
					int rownum;
					int basey = 0;// mButtonSize;
					// 画面の方向で表示する個数を変更
					if (mFirstCX < mFirstCY) {
						// 縦長
						rownum = 2;
						basex = mFirstCX / 2 - width;
					}
					else {
						// 横長
						rownum = 4;
						basex = mFirstCX / 2 - width * 2 - 1;
					}

					// ボタン位置等を設定
					// 戻るを設定
					id_wk = 0x4000;
					rc_wk = new Rect(0, basey, mFirstCX / 2, basey + mButtonSize);
					cmdinfo[0] = new CommandInfo(null, rc_wk, id_wk, true, false);

					// メニューを設定
					id_wk = 0x4001;
					rc_wk = new Rect(mFirstCX / 2 + 1, basey, mFirstCX, basey + mButtonSize);
					cmdinfo[1] = new CommandInfo(null, rc_wk, id_wk, true, false);
					basey += (mButtonSize + 1) * 3 / 2;

					int index = 2;
					int posx = basex;
					for (int i = 0 ; i < cmdnum ; i ++) {
						// ボタンのIDを設定
						id_wk = (0x8000 | i);
						str_wk = mTopCommandStr[i];

						// ボタン位置を計算
						rc_wk = new Rect(posx, basey, posx + width, basey + mButtonSize);
						// 設定
						cmdinfo[index] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);

						if (i % rownum == rownum - 1) {
							// 改行
							posx = basex;
							basey += mButtonSize + 1;
						}
						else {
							posx += width + 1;
						}
						index ++;
					}
				}
			}
		}
		else if (mFirstY >= mFirstCY - mButtonSize) {
			int num;

			num = 11 + 2;

			if (mPageMode == 0) {
				// スワイプページ選択モードの場合はページ選択なし
				num -= 1;
			}
			if( mImmEnable == false ){
				// IMMERSIVEモードが無効な時は閉じるメニューなし
				num -= 2;
			}
			if (mDualView == false) {
				// 単体表示の場合は1ページずらしなし
				num -= 2;
			}
			if (mNextFile == false) {
				// 次のファイルの非表示
				num -= 6;
			}
			// 画面下部タッチ

			int basex1 = mFirstCX / 2 - width - 1;
			int basex2 = mFirstCX / 2;
			int basey = mFirstCY;

			int i = 0;
			Resources res = mContext.getResources();

			cmdinfo = new CommandInfo[num];

			if (mPageMode != 0) {
				// ページ選択
				str_wk = res.getString(R.string.pageSelect);
				id_wk = 8;
				rc_wk = new Rect(mButtonSize + 1, mFirstCY - mButtonSize, mFirstCX - mButtonSize - 1, mFirstCY);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, true, false);
				i++;
			}

			// 先頭ページ
			id_wk = 0x4002;
			rc_wk = new Rect(mFirstCX - mButtonSize, mFirstCY - mButtonSize, mFirstCX, mFirstCY);
			cmdinfo[i] = new CommandInfo(null, rc_wk, id_wk, false, false);	// 長押し時に表示
			i ++;

			// 最終ページ
			id_wk = 0x4003;
			rc_wk = new Rect(0, mFirstCY - mButtonSize, mButtonSize, mFirstCY);
			cmdinfo[i] = new CommandInfo(null, rc_wk, id_wk, false, false);	// 長押し時に表示
			i ++;

			basey -= (mButtonSize + 1) * 3 / 2;

			if( mImmEnable == true ){
				// 閉じる
				str_wk = res.getString(R.string.cdBtnClose);
				id_wk = 9;
				rc_wk = new Rect(basex1, basey - mButtonSize, basex1 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, true, false);
				i ++;
				// 設定
				str_wk = res.getString(R.string.setMenu);
				id_wk = 10;
				rc_wk = new Rect(basex2, basey - mButtonSize, basex2 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				basey -= mButtonSize + 1;
			}
			if (mDualView == true) {
				// 1ページ次/前にずらす
				str_wk = res.getString(mIsRtoL ? R.string.pageNext : R.string.pagePrev);
				id_wk = mIsRtoL ? 0 : 1;
				rc_wk = new Rect(basex1, basey - mButtonSize, basex1 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				// 1ページ前/次にずらす
				str_wk = res.getString(mIsRtoL ? R.string.pagePrev : R.string.pageNext);
				id_wk = mIsRtoL ? 1 : 0;
				rc_wk = new Rect(basex2, basey - mButtonSize, basex2 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				basey -= mButtonSize + 1;
			}
			if (mNextFile == true) {
				// 次/前のファイル(しおり位置)
				str_wk = res.getString(mIsRtoL ? R.string.fileNextMark : R.string.filePrevMark);
				id_wk = mIsRtoL ? 2 : 5;
				rc_wk = new Rect(basex1, basey - mButtonSize, basex1 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				// 前/次のファイル(しおり位置)
				str_wk = res.getString(mIsRtoL ? R.string.filePrevMark : R.string.fileNextMark);
				id_wk = mIsRtoL ? 5 : 2;
				rc_wk = new Rect(basex2, basey - mButtonSize, basex2 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				basey -= mButtonSize + 1;

				// 次/前のファイル(先頭)
				str_wk = res.getString(mIsRtoL ? R.string.fileNextTop : R.string.filePrevTop);
				id_wk = mIsRtoL ? 3 : 6;
				rc_wk = new Rect(basex1, basey - mButtonSize, basex1 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				// 前/次のファイル(先頭)
				str_wk = res.getString(mIsRtoL ? R.string.filePrevTop : R.string.fileNextTop);
				id_wk = mIsRtoL ? 6 : 3;
				rc_wk = new Rect(basex2, basey - mButtonSize, basex2 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				basey -= mButtonSize + 1;

				// 次/前のファイル(末尾)
				str_wk = res.getString(mIsRtoL ? R.string.fileNextLast : R.string.filePrevLast);
				id_wk = mIsRtoL ? 4 : 7;
				rc_wk = new Rect(basex1, basey - mButtonSize, basex1 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
				// 前/次のファイル(末尾)
				str_wk = res.getString(mIsRtoL ? R.string.filePrevLast : R.string.fileNextLast);
				id_wk = mIsRtoL ? 7 : 4;
				rc_wk = new Rect(basex2, basey - mButtonSize, basex2 + width, basey);
				cmdinfo[i] = new CommandInfo(str_wk, rc_wk, id_wk, false, false);
				i ++;
			}
		}

		// 反映
		mCommandSize = cmdsize;
		mCommand = cmdinfo;
		return;
	}

	public void eventTouchMove(int x, int y) {
		// 押されたボタンのチェック
		int index  = findButton(x, y);
		if (index != mCommandIndex) {
			// 選択を変更後表示更新
			mCommandIndex = index;
			this.invalidate();
		}
		return;
	}

	public void eventTouchCancel() {
//		if (y >= cy - mClickArea * 2) {
//		int ccx = ((cx / 2) - mClickArea) / 2;
//		int selindex = -1;
//		if (x < mClickArea) {
//			selindex = 0;	// 左2
//		}
//		else if (cx - mClickArea <= x) {
//			selindex = 3;	// 右2
//		}
//		else if (mBottomFile) {
//			// ファイル変更選択可能
//			if (mClickArea <= x && x < mClickArea + ccx) {
//				selindex = 1;	// 左3
//			}
//			else if (mClickArea + ccx <= x && x < cx / 2) {
//				selindex = 2;	// 左4
//			}
//			else if (cx / 2 + ccx <= x && x < cx - mClickArea) {
//				selindex = 4;	// 右3
//			}
//			else if (cx / 2 <= x && x < cx / 2 + ccx) {
//				selindex = 5;	// 右4
//			}
//		}
//		if (mPageWay != DEF.PAGEWAY_RIGHT) {
//			// 左表示なら入れ替え
//			if (selindex >= 3) {
//				selindex -= 3;
//			}
//			else {
//				selindex += 3;
//			}
//		}
		mCommand = null;
		this.invalidate();
		return;
	}

	// upイベントによるボタン選択
	public int eventTouchUp(int x, int y) {
		int id = -1;

		// 選択コマンド
		int index = findButton(x, y);
		if (index >= 0 && (mCommandOn || mCommand[index].disp)) {
			// 選択したコマンドが表示されているものであれば
			id = mCommand[index].id;
		}
		eventTouchCancel();
		return id;
	}

	private int findButton(int x, int y) {
		int index = -1;
		if (mCommand != null) {
			for (int i = 0 ; i < mCommand.length; i ++) {
				Rect rc = mCommand[i].rect;
				if (rc.left < x && x < rc.right && rc.top < y && y < rc.bottom) {
					index = i;	//mCommandId[i];
					break;
				}
			}
		}
		return index;
	}

	// ローディング情報
	public void setLodingState() {
		if (mFromType != FROMTYPE_NONE || mLoadingStr != null) {
			mFromType = FROMTYPE_NONE;
			mLoadingStr = null;
			this.invalidate();
		}
	}

	public void setLodingState(int count, int from, int percent, int rate) {
		if (count == 0) {
			mFromType = from;
			mLoadingStr = null;
		}
		else {
			mLoadingStr = percent + "% " + ((float) rate / 10) + "KB/S";

		}
		this.invalidate();
	}

	public void setLodingState(String str) {
		mFromType = FROMTYPE_ERROR;
		mLoadingStr = str;
		this.invalidate();
	}

	// ページ表示文字列の設定
	public void setPageText(String str) {
		mPageText = str;
	}

	public String[] getPageLine(String pagetext) {
		String pageline[] = null;
		if (pagetext != null && mParentView != null) {
			int cx = mParentView.getWidth();
			int cy = mParentView.getHeight();
			int pagecy = (cx + cy) / 80;

			mPagePaint.setTextSize(pagecy);

			int i = 0;
			int pos = -1;
			do {
				pos = pagetext.indexOf('\n', pos + 1);
				i++;
			} while (pos > 0);

			pageline = new String[i];
			pos = -1;
			int st = 0;
			for (i = 0; i < pageline.length; i++) {
				// ページ文字列の分割
				pos = pagetext.indexOf('\n', pos + 1);
				if (pos >= 0) {
					pageline[i] = pagetext.substring(st, pos);
					st = pos + 1;
				}
				else {
					pageline[i] = pagetext.substring(st);
					break;
				}
			}

			int dcx = (int) mPagePaint.measureText("...");
			for (i = 0; i < pageline.length; i++) {
				int tcx = (int) mPagePaint.measureText(pageline[i]);
				if (tcx > cx) {
					// 画面に入らない場合は文字列を切り取る
					float result[] = new float[pageline[i].length()];
					mPagePaint.getTextWidths(pageline[i], result);

					float total = 0.0f;
					for (int j = result.length - 1; j >= 0; j--) {
						if (total + result[j] > cx - dcx) {
							pageline[i] = "..." + pageline[i].substring(j + 1);
							break;
						}
						total += result[j];
					}

				}
			}
		}
		return pageline;
	}

	// ページ表示文字列の取得
	public String getPageText() {
		return mPageText;
	}

	// ページ表示文字列の背景色
	public void setPageColor(int color) {
		mPageColor = color;
		invalidate();
		return;
	}

	// 任意のサイズ変更中の倍率表示
	public void setGuideText(String str) {
		mGuideText = str;
		invalidate();
		return;
	}

	// 操作確認モード
	public boolean getOperationMode() {
		return mOperationMode;
	}

	public void setOperationMode(boolean mode) {
		if (mOperationMode != mode) {
			mOperationMode = mode;
			invalidate();
		}
	}

	// ローディング中表示
	public void countLoading(boolean flag) {
		if (flag) {
			// 表示
			mLoadingCount++;
		}
		else {
			// 非表示
			mLoadingCount = -1;
			mLoadPoint = null;
		}
		invalidate();
		return;
	}

	// ローディング表示中？
	public boolean getLoading() {
		return mLoadingCount != -1;
	}

	// ページ番号表示
	public void setPageNumberFormat(boolean display, int format, int pos, int size, int color) {
		mPageDisplay = display;
		mPageFormatInt = format;
		mPagePos = pos;
		mPageSize = size;
		mPnumColor = color;

		mNumPaint.setTextSize(mPageSize);
		invalidate();
	}

	// ページ番号表示
	public void setPageNumberString(String str) {
		mPageStr = str;
		invalidate();
	}

	// 時刻＆バッテリー表示
	public void setTimeFormat(boolean display, int format, int pos, int size, int color) {
		mTimeDisplay = display;
		mTimeFormatInt = format;
		mTimePos = pos;
		mTimeSize = size;
		mTimeColor = color;

		if (format < 3) {
			// 24時間表示
			mTimeFormat = new SimpleDateFormat("HH:mm");
		}
		else {
			// 12時間表示
			mTimeFormat = new SimpleDateFormat("hh:mm");
		}
		mTimePaint.setTextSize(mTimeSize);

		// バッテリー残量(%)
		mBatteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	// 時刻＆バッテリー表示
	public void setTimeString() {
//		boolean isCharging = false;
		boolean usbCharge = false;
		boolean acCharge = false;

		// バッテリー残量(%)
		mBatteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		if (mBatteryIntent != null) {
			// バッテリー残量(%)
			int level = mBatteryIntent.getIntExtra("level", 0);
			int scale = mBatteryIntent.getIntExtra("scale", 100);
			mBattery = level * 100 / scale;

//			// 充電中？ / 満充電？
//			int status = mBatteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//			isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//					status == BatteryManager.BATTERY_STATUS_FULL;

			// 充電方法 [ AC / USB ]
			int chargePlug = mBatteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		}
		
		// 時刻
		mTimeStr = mTimeFormat.format(new Date());
		
		if (mTimeFormatInt % 3 > 0) {
			// バッテリー
			mTimeStr += " [" + mBattery + "%]";
		}

		if (mTimeFormatInt % 3 > 1) {
			if (usbCharge) {
				mTimeStr += " [USB]";
			}
			if (acCharge) {
				mTimeStr += " [AC]";
			}
		}
		invalidate();
	}

	private void invalidate() {
		if (mListener != null) {
			mListener.onUpdate();
		}
	}

	public void setParentView(View view) {
		mParentView = view;
	}

	public void setUpdateListear(UpdateListener listener) {
		mListener = listener;
	}

	public interface UpdateListener extends EventListener {
		// ガイドが更新された
		public void onUpdate();
	}
}
