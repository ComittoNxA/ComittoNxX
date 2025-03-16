package src.comitton.fileview.view.list;

import java.util.ArrayList;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.common.Logcat;
import src.comitton.common.TextFormatter;
import src.comitton.fileview.data.FileData;
import src.comitton.fileview.filelist.RecordList;
import src.comitton.fileview.view.DrawNoticeListener;
import src.comitton.jni.CallImgLibrary;

import jp.dip.muracoro.comittonx.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;

public class FileListArea extends ListArea implements Handler.Callback {
	private static final String TAG = "FileListArea";


	private final int[] ICON_ID =
	{
		R.drawable.thumb_parent, R.drawable.txt, R.drawable.pdf
	};
	private final int[] FILEMARK_ID =
	{
		R.drawable.thumb_dir, R.drawable.thumb_pdf, R.drawable.thumb_zip, R.drawable.thumb_rar, R.drawable.thumb_epub
	};

	public static final short LISTMODE_LIST = 0;
	public static final short LISTMODE_TILE = 1;

	private final int ICON_PARENT = 0;
	private final int ICON_TEXT = 1;
	private final int ICON_PDF = 2;

	private final int FILEMARK_DIR = 0;
	private final int FILEMARK_PDF = 1;
	private final int FILEMARK_ZIP = 2;
	private final int FILEMARK_RAR = 3;
	private final int FILEMARK_EPUB = 4;

	private final int MAXLINE_TITLE = 3;
	private final int DATETIME_LENGTH = 21;

	private Bitmap[] mIcon;
	private Bitmap[] mMark;

	private long mThumbnailId;
	private ArrayList<FileData> mFileList;

	private int mDirColor;
	private int mImgColor;
	private int mBefColor;
	private int mAftColor;
	private int mNowColor;
	private int mBakColor;
	private int mCurColor;
	private int mMrkColor;
	private int mInfColor;
	private int mBdrColor;

	private boolean mThumbFlag;
	private short mItemMargin;
	private short mMarkSizeW;
	private short mMarkSizeH;

	private short mListMode;

	private int mMaxLines = 2;
	private boolean mShowExt;
	private boolean mSplitFilename;
//	private boolean mSeparate = true;

	private Paint mBitmapPaint;
	private Paint mFillPaint;
	private Paint mLinePaint;
	private Paint mNamePaint;
	private Paint mInfoPaint;
	private Paint mReadPaint;
	private Rect mSrcRect;
	private Rect mDstRect;

	private int mLoadingSize;

	private String[][] mTitleSep;
	private String[][] mInfoSep;
	private String[] mReadInfo;

	private int mTileSize;
	private int mTileAscent;
	private int mTileDescent;

	private int mTitleSize;
	private int mTitleAscent;
	private int mTitleDescent;

	private int mInfoSize;
	private int mInfoAscent;
	private int mInfoDescent;

	private short mItemWidth;
	private short mItemHeight;
	private short mIconWidth;
	private short mIconHeight;

	private short mListIconHeight = 160;

	private int mDrawLeft;
	private Bitmap mDrawBitmap;

	private String[][][] mText;

	private Context mContext;

	private DrawNoticeListener mDrawNoticeListener = null;

	private boolean mChangeLayout = false;

	// コンストラクタ
	public FileListArea(Context context, DrawNoticeListener listener) {
		super(context, RecordList.TYPE_FILELIST);

		mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBitmapPaint.setTypeface(Typeface.DEFAULT);
		mBitmapPaint.setTextAlign(Paint.Align.CENTER);
		mFillPaint = new Paint();
		mFillPaint.setStyle(Style.FILL);
		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		mNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNamePaint.setTypeface(Typeface.DEFAULT);
		mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mInfoPaint.setTypeface(Typeface.MONOSPACE);
		mInfoPaint.setTextAlign(Paint.Align.LEFT);
		mReadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mReadPaint.setTypeface(Typeface.MONOSPACE);
		mReadPaint.setTextAlign(Paint.Align.LEFT);

		mSrcRect = new Rect();
		mDstRect = new Rect();

		mContext = context;
		mDrawNoticeListener = listener;
	}

	@Override
	public void drawArea(Canvas canvas, int baseX, int baseY) {
		// 背景色
		mFillPaint.setColor(mBakColor);
		canvas.drawRect(baseX, baseY, baseX + mAreaWidth, baseY + mAreaHeight, mFillPaint);

		if (mListMode == LISTMODE_LIST) {
			drawListItems(canvas, baseX, baseY);
		}
		else {
			drawTileItems(canvas, baseX, baseY);
		}
		super.drawArea(canvas, baseX, baseY);
	}

	// タイルモード時の描画
	@SuppressLint("SuspiciousIndentation")
    private void drawTileItems(Canvas canvas, int baseX, int baseY) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

//		int cx = mAreaWidth;
		int cy = mAreaHeight;
		int x;
		int y;
		int index;

		if (mFileList == null) {
			return;
		}

		mNamePaint.setTextAlign(Paint.Align.CENTER);
		mNamePaint.setTextSize(mTileSize);

		// サマリ描画
		mInfoPaint.setTextAlign(Paint.Align.CENTER);
		mInfoPaint.setColor(mInfColor);
		mInfoPaint.setTextSize(mTileSize);

		if (mItemHeight <= 0) {
			return;
		}
		// 先頭項目の位置
		int ypos = mTopPos;
//		long clockSt = SystemClock.uptimeMillis();
		for (int iy = mTopRow ; ypos < cy ; iy ++, ypos += mItemHeight + BORDER_HEIGHT) {
			y = baseY + ypos + mItemHeight;

//			mFillPaint.setColor(mInfColor);
//			canvas.drawRect(baseX, y, baseX + cx, y + BORDER_HEIGHT, mFillPaint);

			for (int ix = 0 ; ix < mColumnNum ; ix ++) {
				index = iy * mColumnNum + ix;
				if (index >= mListSize) {
					// 最後の項目まで描画済み
					break;
				}

				FileData fd = null;
				if (mFileList != null) {
					fd = mFileList.get(index);
				}
				if (fd == null) {
					continue;
				}

				x = baseX + mDrawLeft + ix * mItemWidth;
				y = baseY + ypos;

				// 選択背景塗り
				int color;
				boolean isTouchDraw = (mTouchIndex == index && mTouchDraw);
				boolean isCursorDraw = (mCursorDisp && mCursorPosY * mColumnNum + mCursorPosX == index);
    			if (fd.getMarker()) {
					color = mMrkColor;
				}
				else {
					color = mBakColor;
				}
				if (isTouchDraw) {
					color = DEF.margeColor(mCurColor, color, mTouchCounter + 48, 96);
				}
    			else if (isCursorDraw) {
    				color = DEF.margeColor(mCurColor, color, 48, 96);
    			}

				mFillPaint.setColor(color);
				canvas.drawRect(x, y, x + mItemWidth - BORDER_WIDTH, y + mItemHeight, mFillPaint);

				if (isTouchDraw || isCursorDraw) {
					int mh = mItemMargin / 2;
					mFillPaint.setColor(mCurColor);
					canvas.drawRect(x, y, x + mItemWidth, y + mh, mFillPaint);
					canvas.drawRect(x, y + mItemHeight - mh, x + mItemWidth, y + mItemHeight, mFillPaint);
					canvas.drawRect(x, y, x + mh, y + mItemHeight, mFillPaint);
					canvas.drawRect(x + mItemWidth - mh, y, x + mItemWidth, y + mItemHeight, mFillPaint);
				}

				short type = fd.getType();
				short exttype = fd.getExtType();
				x = baseX + mDrawLeft + ix * mItemWidth + (mItemWidth - mIconWidth) / 2;
				y = baseY + ypos + mItemMargin;

				if (type == FileData.FILETYPE_PARENT) {
					color = mDirColor;
				}
				else if (type == FileData.FILETYPE_IMG) {
					color = mImgColor;
				}
				else {
					switch (fd.getState()) {
						case DEF.PAGENUMBER_UNREAD:
							if (type == FileData.FILETYPE_ARC || type == FileData.FILETYPE_PDF || type == FileData.FILETYPE_TXT || type == FileData.FILETYPE_EPUB) {
								color = mBefColor;
							}
							else {
								color = mDirColor;
							}
							break;
						case DEF.PAGENUMBER_READ:
							color = mAftColor;
							break;
						case DEF.PAGENUMBER_NONE:
						default:
							color = mNowColor;
							break;
					}
				}
				mNamePaint.setColor(color);
				mLinePaint.setColor(color);
				mLinePaint.setStrokeWidth(3);

				if (mThumbFlag) {
					if (type == FileData.FILETYPE_ARC || type == FileData.FILETYPE_PDF || type == FileData.FILETYPE_IMG || type == FileData.FILETYPE_DIR || type == FileData.FILETYPE_EPUB) {
						// ビットマップ表示
						canvas.drawRect(x - 1, y - 1, x + mIconWidth, y + mIconHeight, mLinePaint);

						Bitmap bmMark = null;
						if (type == FileData.FILETYPE_DIR) {
							bmMark = mMark[FILEMARK_DIR];
						}
						else {
							if (exttype == FileData.EXTTYPE_ZIP) {
								bmMark = mMark[FILEMARK_ZIP];
							}
							else if (exttype == FileData.EXTTYPE_RAR) {
								bmMark = mMark[FILEMARK_RAR];
							}
							else if (exttype == FileData.EXTTYPE_PDF) {
								bmMark = mMark[FILEMARK_PDF];
							}
							else if (exttype == FileData.EXTTYPE_EPUB) {
								bmMark = mMark[FILEMARK_EPUB];
							}
						}

						int retBitmap = CallImgLibrary.ThumbnailCheck(mThumbnailId, index);
						if (mDrawBitmap != null && retBitmap == 1) {
							int retValue = CallImgLibrary.ThumbnailDraw(mThumbnailId, mDrawBitmap, index);
							int width = (retValue >> 16) & 0xFFFF;
							int height = retValue & 0xFFFF;

							int dstWidth = 0;
							int dstHeight = 0;

							Logcat.d(logLevel, "width/height=" + ((float)width / (float)height) + ", mIconWidth/mIconHeight=" + ((float)mIconWidth / (float)mIconHeight));
							if ((float)width / height > (float)mIconWidth / mIconHeight) {
								Logcat.d(logLevel, "幅に合わせます. width=" + width + ", height=" + height + ", mIconWidth=" + mIconWidth + ", mIconHeight=" + mIconHeight);
								// 幅に合わせる
								dstWidth = mIconWidth;
								dstHeight = height * mIconWidth / width;
							}
							else {
								Logcat.d(logLevel, "高さに合わせます. width=" + width + ", height=" + height + ", mIconWidth=" + mIconWidth + ", mIconHeight=" + mIconHeight);
								// 高さに合わせる
								dstWidth = width * mIconHeight / height;
								dstHeight = mIconHeight;
							}

							int dstX = 0;
							int dstY = 0;
							if (dstWidth < mIconWidth) {
								dstX = (mIconWidth - dstWidth) / 2;
							}
							if (dstHeight < mIconHeight) {
								dstY = (mIconHeight - dstHeight) / 2;
							}

							mSrcRect.set(0, 0, width, height);
							mDstRect.set(x + dstX, y + dstY, x + dstX + dstWidth, y + dstY + dstHeight);
							canvas.drawBitmap(mDrawBitmap, mSrcRect, mDstRect, mBitmapPaint);

							if (bmMark != null) {
								canvas.drawBitmap(bmMark, x + mIconWidth - mMarkSizeW, y + mIconHeight - mMarkSizeH, mBitmapPaint);
							}
							Logcat.d(logLevel,"fd.getState()=" + fd.getState() + ", fd.getMaxpage()=" + fd.getMaxpage() + ", fd.getName()=" + fd.getName());
							drawPagerate(canvas, fd, mIconWidth, mIconHeight, x, y, color);
						}
						else {
							// サムネイルありかつ画像なし
							int loadingSize = Math.min(mIconWidth, mIconHeight) / 6;
							mBitmapPaint.setTextSize(loadingSize);
							String str = retBitmap == 0 ? "Loading..." : "No Image";
							int text_x = x + mIconWidth / 2;
							int text_y = y + (mIconHeight + mLoadingSize) / 2 - mLoadingSize / 4;

							// 中央
							mBitmapPaint.setStrokeWidth(1.0f);
							mBitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
							mBitmapPaint.setColor(Color.DKGRAY);
							canvas.drawText(str, text_x + 1, text_y + 1, mBitmapPaint);

							mBitmapPaint.setColor(color);
							canvas.drawText(str, text_x, text_y, mBitmapPaint);

							if (bmMark != null) {
								canvas.drawBitmap(bmMark, x + mIconWidth - mMarkSizeW, y + mIconHeight - mMarkSizeH, mBitmapPaint);
							}
						}
					}
					else if (type == FileData.FILETYPE_PARENT || type == FileData.FILETYPE_TXT) {
						Bitmap bm =null;
						int iconWidth = mIconWidth;
						int	iconHeight = mIconHeight;
						if (type == FileData.FILETYPE_PARENT) {
							bm = mIcon[ICON_PARENT];
						}
						else if (type == FileData.FILETYPE_TXT) {
							bm = mIcon[ICON_TEXT];
							// 未読・既読で色を変える
							bm = ImageAccess.setColor(bm, mLinePaint.getColor());
							canvas.drawRect(x - 1, y - 1, x + iconWidth, y + iconHeight, mLinePaint);
						}
						int dstWidth = bm.getWidth();
						int dstHeight = bm.getHeight();
						float dsW = (float)iconWidth / (float)dstWidth;
						float dsH = (float)iconHeight / (float)dstHeight;
						float dsMin = Math.min(dsW, dsH);

						int w = (int)(dstWidth * dsMin);
						int h = (int)(dstHeight * dsMin);
						int dstX = 0;
						int dstY = 0;
						if (w < iconWidth) {
							dstX = (iconWidth - w) / 2;
						}
						if (h < iconHeight) {
							dstY = (iconHeight - h) / 2;
						}
						mSrcRect.set(0, 0, dstWidth, dstHeight);
						mDstRect.set(x + dstX, y + dstY, x + dstX + (int)(dstWidth * dsMin), y + dstY + (int)(dstHeight * dsMin));
						canvas.drawBitmap(bm, mSrcRect, mDstRect, mBitmapPaint);
						drawPagerate(canvas, fd, mIconWidth, mIconHeight, x, y, color);
					}
				}else{ // タイル表示・サムネ無しの場合は枠で囲む
					canvas.drawRect(baseX + mDrawLeft + ix * mItemWidth + mItemMargin, y + mIconHeight,
							baseX + mDrawLeft + (ix + 1) * mItemWidth - mItemMargin, y + mItemHeight - mItemMargin*2, mLinePaint);
//					canvas.drawRect(baseX + mDrawLeft + ix * mItemWidth + mItemMargin, y + mIconHeight,
//							baseX + mDrawLeft + (ix + 1) * mItemWidth - mItemMargin, y + mItemHeight - mItemMargin*2, paint);
				}

				if (mText[0][index] == null) {
					String[] name = new String[4];
					name[0] = fd.getName(); // ファイル名
					name[1] = ""; // [角括弧]
					name[2] = ""; // (丸括弧)
					name[3] = ""; // .拡張子

					// 拡張子を取得
					int dot = name[0].lastIndexOf('.');
					if (type != FileData.FILETYPE_DIR && type != FileData.FILETYPE_PARENT) {
						if (dot >= 1 && dot < name[0].length() - 1) {
							if (mSplitFilename && mShowExt) {
								name[3] = name[0].substring(dot + 1);
							}
							if (mSplitFilename || !mShowExt) {
								name[0] = name[0].substring(0, dot);
							}
						}
					}
					if (mSplitFilename) {
						// 角括弧を取得(2個目以降は無視)
						int open_braket = name[0].indexOf('[');
						int close_braket = name[0].indexOf(']');
						if (open_braket != -1 && close_braket != -1 && open_braket < close_braket) {
							name[1] = name[0].substring(open_braket + 1, close_braket);
							name[0] = name[0].substring(0, open_braket) + name[0].substring(close_braket + 1);
						}
						// 丸括弧を取得(2個目以降は無視)
						int open_parenthesis = name[0].indexOf('(');
						int close_parenthesis = name[0].indexOf(')');
						if (open_parenthesis != -1 && close_parenthesis != -1 && open_parenthesis < close_parenthesis) {
							name[2] = name[0].substring(open_parenthesis + 1, close_parenthesis);
							name[0] = name[0].substring(0, open_parenthesis) + name[0].substring(close_parenthesis + 1);
						}
					}
					// 結果を前に詰める
					for (int i = 1; i < mText.length - 1; i++){
						if (name[i].isEmpty() || name[i].equals("/")){
							for (int j = i + 1; j < mText.length; j++) {
								if (!name[j].isEmpty()) {
									name[i] = name[j] + name[i];
									name[j] = "";
									break;
								}
							}
						}
					}
					//Logcat.d("logLevel", "name[0]=\"" + name[0] + "\", name[1]=\"" + name[1] + "\", name[2]=\"" + name[2] + "\", name[3]=\"" + name[3] + "\"");

					mText[0][index] = TextFormatter.getMultiLine(name[0], mItemWidth - mItemMargin * 2, mNamePaint, mMaxLines);
					if (mSplitFilename) {
						mText[1][index] = TextFormatter.getMultiLine(name[1], mItemWidth - mItemMargin * 2, mInfoPaint, mMaxLines);
						mText[2][index] = TextFormatter.getMultiLine(name[2], mItemWidth - mItemMargin * 2, mInfoPaint, mMaxLines);
						mText[3][index] = TextFormatter.getMultiLine(name[3], mItemWidth - mItemMargin * 2, mInfoPaint, mMaxLines);
					}
				}

				if (mText[0][index] != null) {
					x += mIconWidth / 2;
					y += mIconHeight + mItemMargin;
					for (int i = 0; i < mText[0][index].length; i++) {
						canvas.drawText(mText[0][index][i], x, y + mTileAscent, mNamePaint);
						y += mTileSize + mTileDescent;
					}
				}

				if (mSplitFilename) {

					if (mText[1][index] != null) {
						for (int i = 0; i < mText[1][index].length; i++) {
							canvas.drawText(mText[1][index][i], x, y + mTileAscent, mInfoPaint);
							y += mTileSize + mTileDescent;
						}
					}
					if (mText[2][index] != null) {
						for (int i = 0; i < mText[2][index].length; i++) {
							canvas.drawText(mText[2][index][i], x, y + mTileAscent, mInfoPaint);
							y += mTileSize + mTileDescent;
						}
					}
					if (mText[3][index] != null) {
						for (int i = 0; i < mText[3][index].length; i++) {
							canvas.drawText(mText[3][index][i], x, y + mTileAscent, mInfoPaint);
							y += mTileSize + mTileDescent;
						}
					}
				}
			}
		}
//		long clockTerm = SystemClock.uptimeMillis() - clockSt;
//		Logcat.d(logLevel, "top=" + mTopRow + ", pos=" + mTopPos + ", clock=" + clockTerm);
		return;
	}

	@SuppressLint("SuspiciousIndentation")
    private void drawListItems(Canvas canvas, int baseX, int baseY) {
		int logLevel = Logcat.LOG_LEVEL_DEBUG;
		Logcat.d(logLevel, "開始します.");

		int cx = mAreaWidth;
		int cy = mAreaHeight;
		int y;
		int x;
		int index;
		int listnum = 0;

		if (mFileList != null) {
			listnum = mFileList.size();
		}

		mNamePaint.setTextAlign(Paint.Align.LEFT);
		mInfoPaint.setTextAlign(Paint.Align.LEFT);

		int ypos = mTopPos;
		for (int iy = mTopRow ; ypos < cy ; iy ++) {
			index = iy;
			Logcat.d(logLevel, "index=" + index + ", listnum=" + listnum);
			if (index >= listnum) {
				break;
			}
			else if (mTitleSep == null && mInfoSep == null && mTitleSep[index] == null && mInfoSep[index] == null) {
				// 設定されていなければ抜ける
				break;
			}
			int itemHeight = getRowHeight(index);

			x = baseX;
			y = baseY + ypos;

			FileData fd = null;
			if (mFileList != null) {
				fd = mFileList.get(index);
			}
			if (fd == null) {
				continue;
			}

			// 選択背景塗り
			int color;
			boolean isTouchDraw = (mTouchIndex == index && mTouchDraw);
			boolean isCursorDraw = (mCursorDisp && mCursorPosY * mColumnNum + mCursorPosX == index);
			if (fd.getMarker()) {
				color = mMrkColor;
			}
			else {
				color = mBakColor;
			}
			if (isTouchDraw) {
				color = DEF.margeColor(mCurColor, color, mTouchCounter + 48, 96);
			}
			else if (isCursorDraw) {
				color = DEF.margeColor(mCurColor, color, 48, 96);
			}
			mFillPaint.setColor(color);
			canvas.drawRect(x, y, x + cx, y + itemHeight, mFillPaint);

			if (isTouchDraw || isCursorDraw) {
				int mh = mItemMargin / 2;
				mFillPaint.setColor(mCurColor);
				canvas.drawRect(x, y, x + mItemWidth, y + mh, mFillPaint);
				canvas.drawRect(x, y + itemHeight - mh, x + mItemWidth, y + itemHeight, mFillPaint);
			}

			short type = fd.getType();
			short exttype = fd.getExtType();

			// 項目区切り
			mFillPaint.setColor(mBdrColor);
			canvas.drawRect(x, y + itemHeight, x + cx, y + itemHeight + BORDER_HEIGHT, mFillPaint);
			if (type == FileData.FILETYPE_PARENT) {
				color = mDirColor;
			}
			else if (type == FileData.FILETYPE_IMG) {
				color = mImgColor;
			}
			else {
				switch (fd.getState()) {
					case DEF.PAGENUMBER_UNREAD:
						if (type == FileData.FILETYPE_ARC || type == FileData.FILETYPE_PDF || type == FileData.FILETYPE_TXT || type == FileData.FILETYPE_EPUB) {
							color = mBefColor;
						}
						else {
							color = mDirColor;
						}
						break;
					case DEF.PAGENUMBER_READ:
						color = mAftColor;
						break;
					case DEF.PAGENUMBER_NONE:
					default:
						color = mNowColor;
						break;
				}
			}

			mNamePaint.setColor(color);
			mNamePaint.setTextSize(mTitleSize);
			mLinePaint.setColor(color);
			mLinePaint.setStrokeWidth(3);

			x = baseX + mItemMargin;
			y = baseY + ypos + mItemMargin;
			if (mThumbFlag) {
                int iconHeight = mListIconHeight;
                int iconWidth = mIconWidth * iconHeight / mIconHeight;
				int retBitmap = CallImgLibrary.ThumbnailCheck(mThumbnailId, index);

				if (type == FileData.FILETYPE_ARC || type == FileData.FILETYPE_PDF || type == FileData.FILETYPE_IMG || type == FileData.FILETYPE_DIR || type == FileData.FILETYPE_EPUB) {
					// ビットマップ表示
                    canvas.drawRect(x - 1, y - 1, x + iconWidth, y + iconHeight, mLinePaint);

					Bitmap bmMark = null;
					if (type == FileData.FILETYPE_DIR) {
						bmMark = mMark[FILEMARK_DIR];
					}
					else {
						if (exttype == FileData.EXTTYPE_ZIP) {
							bmMark = mMark[FILEMARK_ZIP];
						}
						else if (exttype == FileData.EXTTYPE_RAR) {
							bmMark = mMark[FILEMARK_RAR];
						}
						else if (exttype == FileData.EXTTYPE_PDF) {
							bmMark = mMark[FILEMARK_PDF];
						}
						else if (exttype == FileData.EXTTYPE_EPUB) {
							bmMark = mMark[FILEMARK_EPUB];
						}
					}

					if (mDrawBitmap != null && retBitmap == 1) {
						int retValue = CallImgLibrary.ThumbnailDraw(mThumbnailId, mDrawBitmap, index);
						int width = (retValue >> 16) & 0xFFFF;
						int height = retValue & 0xFFFF;

						int dstWidth = 0;
						int dstHeight = 0;

						Logcat.d(logLevel, "width/height=" + ((float)width / (float)height) + ", iconWidth/iconHeight=" + ((float)iconWidth / (float)iconHeight));
						if ((float)width / height > (float)iconWidth / iconHeight) {
							Logcat.d(logLevel, "幅に合わせます. width=" + width + ", height=" + height + ", iconWidth=" + iconWidth + ", iconHeight=" + iconHeight);
							// 幅に合わせる
							dstWidth = iconWidth;
							dstHeight = height * iconWidth / width;
						}
						else {
							Logcat.d(logLevel, "高さに合わせます. width=" + width + ", height=" + height + ", iconWidth=" + iconWidth + ", iconHeight=" + iconHeight);
							// 高さに合わせる
							dstWidth = width * iconHeight / height;
							dstHeight = iconHeight;
						}

						int dstX = 0;
						int dstY = 0;
						if (dstWidth < iconWidth) {
                            dstX = (iconWidth - dstWidth) / 2;
						}
						if (dstHeight < iconHeight) {
							dstY = (iconHeight - dstHeight) / 2;
						}
						mSrcRect.set(0, 0, width, height);
						mDstRect.set(x + dstX, y +dstY, x + dstX + dstWidth, y + dstY + dstHeight);
						canvas.drawBitmap(mDrawBitmap, mSrcRect, mDstRect, mBitmapPaint);

						if (bmMark != null) {
                            canvas.drawBitmap(bmMark, x + iconWidth - mMarkSizeW, y + iconHeight - mMarkSizeH, mBitmapPaint);
							drawPagerate(canvas, fd, iconWidth, iconHeight, x, y, color);
						}
					}
					else {
						// サムネイルありかつ画像なし
                        int loadingSize = Math.min(iconWidth, iconHeight) / 6;
                        mBitmapPaint.setTextSize(loadingSize);
						String str = retBitmap == 0 ? "Loading..." : "No Image";
                        int text_x = x + iconWidth / 2;
                        int text_y = y + (iconHeight + loadingSize) / 2 - loadingSize / 4;

						// 中央
						mBitmapPaint.setStrokeWidth(1.0f);
						mBitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
						mBitmapPaint.setColor(Color.DKGRAY);
						canvas.drawText(str, text_x + 1, text_y + 1, mBitmapPaint);

						mBitmapPaint.setColor(Color.WHITE);
						canvas.drawText(str, text_x, text_y, mBitmapPaint);

						if (bmMark != null) {
                            canvas.drawBitmap(bmMark, x + iconWidth - mMarkSizeW, y + iconHeight - mMarkSizeH, mBitmapPaint);
						}
					}
				}
				else if (type == FileData.FILETYPE_PARENT || type == FileData.FILETYPE_TXT) {
					Bitmap bm =null;
					if (type == FileData.FILETYPE_PARENT) {
						bm = mIcon[ICON_PARENT];
					}
					else if (type == FileData.FILETYPE_TXT) {
						bm = mIcon[ICON_TEXT];
						// 未読・既読で色を変える
						bm = ImageAccess.setColor(bm, mLinePaint.getColor());
						canvas.drawRect(x - 1, y - 1, x + iconWidth, y + iconHeight, mLinePaint);
					}
					int dstWidth = bm.getWidth();
					int dstHeight = bm.getHeight();
					float dsW = (float)iconWidth / (float)dstWidth;
					float dsH = (float)iconHeight / (float)dstHeight;
					float dsMin = Math.min(dsW, dsH);

					int w = (int)(dstWidth * dsMin);
					int h = (int)(dstHeight * dsMin);
					int dstX = 0;
					int dstY = 0;
					if (w < iconWidth) {
						dstX = (iconWidth - w) / 2;
					}
					if (h < iconHeight) {
						dstY = (iconHeight - h) / 2;
					}
					mSrcRect.set(0, 0, dstWidth, dstHeight);
					mDstRect.set(x + dstX, y + dstY, x + dstX + (int)(dstWidth * dsMin), y + dstY + (int)(dstHeight * dsMin));
					canvas.drawBitmap(bm, mSrcRect, mDstRect, mBitmapPaint);
					if ((fd.getState() >= 0) && (fd.getMaxpage() > 0)) {
						// 読書率を描画
						// 読書率
						float rate = (float)(fd.getState() + 1) / (float)fd.getMaxpage();
						// 文字サイズ
						float fontsize = Math.min(mTitleSize, mInfoSize);
						Paint paint = new Paint();
						paint.setTextSize(fontsize);
						paint.setTypeface(Typeface.MONOSPACE);
						paint.setTextAlign(Paint.Align.CENTER);
						// テキスト幅
						float textWidth = paint.measureText("99%");
						// アイコンサイズの短辺の長さ
						float shortsidelength = Math.min(iconWidth, iconHeight);
						// 円の半径
						float radiud1 = textWidth * 1.5F / 2F;
						float radiud2 = textWidth * 1.2F / 2F;
						float radiud3 = textWidth * 1F / 2F;
						// 円の中心座標
						float centerX = x + (iconWidth / 2F);
						float centerY = y + iconHeight - (radiud1 * 2);

						// 読書率のドーナツチャートを描画
						paint.setColor(Color.DKGRAY);
						canvas.drawArc(centerX - radiud1 - 2, centerY - radiud1 - 2, centerX + radiud1 + 2,centerY + radiud1 + 2, -90F, rate * 360, true, paint);
						paint.setColor(color);
						canvas.drawArc(centerX - radiud1, centerY - radiud1, centerX + radiud1,centerY + radiud1, -90F, rate * 360, true, paint);

						//グラデーション円(中サイズ)を描画
						paint.setColor(Color.LTGRAY);
						Shader s = new LinearGradient(centerX - radiud2, centerY - radiud2, centerX + radiud2,centerY + radiud2, Color.LTGRAY, Color.GRAY, Shader.TileMode.CLAMP);
						paint.setShader(s);
						canvas.drawOval(new RectF(centerX - radiud2, centerY - radiud2, centerX + radiud2,centerY + radiud2), paint);

						//グラデーション円(小サイズ)を描画
						s = new LinearGradient(centerX - radiud3, centerY - radiud3, centerX + radiud3,centerY + radiud3, Color.GRAY, Color.LTGRAY, Shader.TileMode.CLAMP);
						paint.setShader(s);
						canvas.drawOval(new RectF(centerX - radiud3, centerY - radiud3, centerX + radiud3,centerY + radiud3), paint);

						// 読書率の文字列を描画
						String str = (int)(rate * 100) + "%";
						paint.setShader(null);
						paint.setStyle(Paint.Style.STROKE);
						paint.setStrokeWidth(2.0f);
						paint.setColor(Color.DKGRAY);
						float text_x = centerX;
						float text_y = centerY - ((paint.descent() + paint.ascent()) / 2);
						canvas.drawText(str, text_x, text_y, paint);
						paint.setStyle(Paint.Style.FILL);
						paint.setColor(color);
						canvas.drawText(str, text_x, text_y, paint);
					}
				}
				// タイトルはアイコンの右側に表示
                x += iconWidth + mItemMargin;
			}

			// タイトル描画
			int ty = 0;
			if (mTitleSep != null && mInfoSep != null && mTitleSep[index] != null && mInfoSep[index] != null) {

				for (int i = 0; i < mTitleSep[index].length; i++) {
					canvas.drawText(mTitleSep[index][i], x, y + ty + mTitleAscent, mNamePaint);
					ty += (mTitleSize + mTitleDescent);
				}

				// サマリ描画
				mInfoPaint.setColor(mInfColor);
				mInfoPaint.setTextSize(mInfoSize);

				for (int i = 0; i < mInfoSep[index].length; i++) {
					canvas.drawText(mInfoSep[index][i], x, y + ty + mInfoAscent, mInfoPaint);
					ty += (mInfoSize + mInfoDescent);
				}

				mReadPaint.setColor(color);
				mReadPaint.setTextSize(mInfoSize);
				canvas.drawText(mReadInfo[index], x, y + ty + mInfoAscent, mReadPaint);
			}

//			if (mIconHeight > ty) {
//				ypos += mIconHeight;
//			}
//			else {
//				ypos += ty;
//			}
			ypos += itemHeight + BORDER_HEIGHT;
		}
		return;
	}

	private void drawPagerate(Canvas canvas, FileData file, int width, int height, int x, int y, int color) {
		if ((file.getState() >= 0) && (file.getMaxpage() > 0)) {
			// 読書率を描画
			// 読書率
			float rate = (float)(file.getState() + 1) / (float)file.getMaxpage();
			// 文字サイズ
			float fontsize = Math.min(mTitleSize, mInfoSize);
			Paint paint = new Paint();
			paint.setTextSize(fontsize);
			paint.setTypeface(Typeface.MONOSPACE);
			paint.setTextAlign(Paint.Align.CENTER);
			// テキスト幅
			float textWidth = paint.measureText("99%");
			// アイコンサイズの短辺の長さ
			float shortsidelength = Math.min(width, height);
			// 円の半径
			float radiud1 = textWidth * 1.5F / 2F;
			float radiud2 = textWidth * 1.2F / 2F;
			float radiud3 = textWidth * 1F / 2F;
			// 円の中心座標
			float centerX = x + (width / 2F);
			float centerY = y + height - (radiud1 * 2);

			// 読書率のドーナツチャートを描画
			paint.setColor(Color.DKGRAY);
			canvas.drawArc(centerX - radiud1 - 2, centerY - radiud1 - 2, centerX + radiud1 + 2,centerY + radiud1 + 2, -90F, rate * 360, true, paint);
			paint.setColor(color);
			canvas.drawArc(centerX - radiud1, centerY - radiud1, centerX + radiud1,centerY + radiud1, -90F, rate * 360, true, paint);

			//グラデーション円(中サイズ)を描画
			paint.setColor(Color.LTGRAY);
			Shader s = new LinearGradient(centerX - radiud2, centerY - radiud2, centerX + radiud2,centerY + radiud2, Color.LTGRAY, Color.GRAY, Shader.TileMode.CLAMP);
			paint.setShader(s);
			canvas.drawOval(new RectF(centerX - radiud2, centerY - radiud2, centerX + radiud2,centerY + radiud2), paint);

			//グラデーション円(小サイズ)を描画
			s = new LinearGradient(centerX - radiud3, centerY - radiud3, centerX + radiud3,centerY + radiud3, Color.GRAY, Color.LTGRAY, Shader.TileMode.CLAMP);
			paint.setShader(s);
			canvas.drawOval(new RectF(centerX - radiud3, centerY - radiud3, centerX + radiud3,centerY + radiud3), paint);

			// 読書率の文字列を描画
			String str = (int)(rate * 100) + "%";
			paint.setShader(null);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2.0f);
			paint.setColor(Color.DKGRAY);
			float text_x = centerX;
			float text_y = centerY - ((paint.descent() + paint.ascent()) / 2);
			canvas.drawText(str, text_x, text_y, paint);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(color);
			canvas.drawText(str, text_x, text_y, paint);
		}
	}

	// タイルとリストの切り替え
	public void setListMode(short mode) {
		if (mListMode != mode) {
			mListMode = mode;
		}
	}

	// リスト設定
	public void setList(ArrayList<FileData> filelist, boolean isRefresh) {
		mFileList = filelist;
		if (mFileList != null) {
			int listnum = mFileList.size();
			mText = new String[4][listnum][];
		}
		else {
			mText = null;
		}

		requestLayout(isRefresh);
		mChangeLayout = false;
	}

	// リスト設定
	public void setThumbnailId(long mThumbID) {
		mThumbnailId = mThumbID;
	}

	// 描画設定
	public void setDrawColor(int clr_dir, int clr_img, int clr_bef, int clr_now, int clr_aft, int clr_bak, int clr_cur, int clr_mrk, int clr_bdr, int clr_inf) {
		mDirColor = clr_dir;
		mImgColor = clr_img;
		mBefColor = clr_bef;
		mNowColor = clr_now;
		mAftColor = clr_aft;
		mBakColor = clr_bak;
		mCurColor = clr_cur;
		mMrkColor = clr_mrk;
		mInfColor = clr_inf;
		mBdrColor = clr_bdr;
		mLinePaint.setColor(clr_bdr);

		mChangeLayout = true;
	}

	public void setDrawInfo(int tilesize, int titlesize, int infosize, int margin, boolean showext, boolean splitfilename, int maxlines) {
		mTileSize = tilesize;
		mTitleSize = titlesize;
		mInfoSize = infosize;
		mItemMargin = (short)margin;
		mShowExt = showext;
		mSplitFilename = splitfilename;
		mMaxLines = maxlines;
		
		// テキスト描画属性設定
		FontMetrics fm;
		mNamePaint.setTextSize(mTileSize);
		fm = mNamePaint.getFontMetrics();
		mTileAscent = (int) (-fm.ascent);
		mTileDescent = (int) (fm.descent);

		mNamePaint.setTextSize(mTitleSize);
		fm = mNamePaint.getFontMetrics();
		mTitleAscent = (int) (-fm.ascent);
		mTitleDescent = (int) (fm.descent);

		mInfoPaint.setTextSize(mInfoSize);
		fm = mInfoPaint.getFontMetrics();
		mInfoAscent = (int) (-fm.ascent);
		mInfoDescent = (int) (fm.descent);

		mTitleSep = null;
		mInfoSep = null;

		mChangeLayout = true;
	}

	// 描画設定
    public void setThumbnail(boolean thumbflag, int sizew, int sizeh, int listThumbSizeH) {
		mThumbFlag = thumbflag;
		mIconWidth = (short)sizew;
		mIconHeight = (short)sizeh;
        mListIconHeight = (short)listThumbSizeH;

		if (mThumbFlag) {
			mDrawBitmap = Bitmap.createBitmap(mIconWidth, mIconHeight, Config.RGB_565);

			// ビットマップリソースを読み込み
			Resources res = mContext.getResources();
			mIcon = new Bitmap[ICON_ID.length];
			int thumbmin = Math.min(sizew, sizeh);
			for (int i = 0; i < mIcon.length; i++) {
				mIcon[i] = ImageAccess.createIcon(res, ICON_ID[i], mIconWidth, mIconHeight, mDirColor);
			}
			mMark = new Bitmap[FILEMARK_ID.length];
			float density = res.getDisplayMetrics().scaledDensity;
			mMarkSizeW = (short)DEF.calcSpToPix(32, density);
			mMarkSizeH = (short)DEF.calcSpToPix(16, density);
			if (mMarkSizeW > sizew / 2 || mMarkSizeH > sizeh / 4) {
				if (sizew < sizeh ) {
					mMarkSizeW = (short)(sizew / 2);
					mMarkSizeH = (short)(sizew / 4);
				}
				else {
					mMarkSizeW = (short)(sizeh / 2);
					mMarkSizeH = (short)(sizeh / 4);
				}
			}
			for (int i = 0; i < mMark.length; i++) {
				mMark[i] = ImageAccess.createIcon(res, FILEMARK_ID[i], mMarkSizeW, mMarkSizeH, null);
			}
		}
		else {
			mIconHeight = 0;
			mDrawBitmap = null;
			mIcon = null;
			mMark = null;
		}

		mLoadingSize = Math.min(sizew, sizeh) / 6;
		mBitmapPaint.setTextSize(mLoadingSize);
		mChangeLayout = true;
	}

	public boolean isMarker(int index) {
		if (index < mFileList.size()) {
			FileData fd = mFileList.get(index);
			if (fd != null) {
				return fd.getMarker();
			}
		}
		return false;
	}

	// リストから削除
	public void remove(FileData fd) {
		if (mFileList != null) {
			int index = mFileList.indexOf(fd);
			if (index >= 0) {
				mFileList.remove(fd);
			}
			notifyUpdate();
		}
	}

	// リストをクリア
	public void clear() {
		if (mFileList != null) {
    		mFileList = null;
    		mChangeLayout = true;
    		update(false);
		}
	}

	public void notifyUpdate() {
		int num = 0;
		if (mFileList != null) {
			num = mFileList.size();
			mText = new String[4][num][];
		}
		else {
			mText = null;
		}
		mChangeLayout = true;
		update(false);
	}

	private void requestLayout(boolean isRefresh) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel,"開始します.");

		int width;
		int columnNum = 0;
		int rowNum = 0;
		int listSize = 0;

		if (mFileList != null && mAreaWidth != 0 && mAreaHeight != 0) {
			// 項目数
			listSize = mFileList.size();

			int disprange;
			if (mListMode == LISTMODE_LIST) {
				width = mAreaWidth;
				// 横の項目数
				columnNum = 1;
				// 縦の項目数
				rowNum = listSize;

				// リストだと項目の高さは不定
				mItemHeight = 0;

//				// テキストの描画範囲
//				int tcx = width - mItemMargin * 2;
//				if (mThumbFlag) {
//					tcx -= mIconWidth +  mItemMargin;	// サムネイル有の場合は幅が減る
//				}

				// タイトル描画設定
				mNamePaint.setTextSize(mTitleSize);
				mNamePaint.setTextAlign(Paint.Align.LEFT);

				// ファイル情報描画設定
				mInfoPaint.setTextSize(mInfoSize);

				mTitleSep = new String[listSize][];
				mInfoSep = new String[listSize][];
				mReadInfo = new String[listSize];

				// 項目高さを求める
				int height = (mTitleSize + mTitleDescent) + (mInfoSize + mInfoDescent);
				// ビットマップ表示のサイズ確保
				if (mThumbFlag && height < mIconHeight) {
					height = mIconHeight;
				}
				height += mItemMargin * 2;
				disprange = mAreaHeight / height + 2;
			}
			else {
				width = mIconWidth + mItemMargin * 2;

				// 横の項目数
				columnNum = mAreaWidth / width;
				if (columnNum <= 0) {
					columnNum = 1;
				}
				// 縦の項目数
				rowNum = (listSize + (columnNum - 1)) / columnNum;

				// 項目の高さ(タイルは固定)
				mItemHeight = (short)(mIconHeight + (mTileSize + mTileDescent) * mMaxLines + mItemMargin * 3);
				Logcat.d(logLevel,"開始します. mTileSize=" + mTileSize + ", mTileDescent=" + mTileDescent + ", mMaxLines=" + mMaxLines);
				disprange = (int)(Math.ceil((double) mAreaHeight / (mItemHeight + 2))) * columnNum;
				Logcat.d(logLevel,"開始します. disprange=" + disprange + ", mAreaHeight=" + mAreaHeight + ", mItemHeight=" + mItemHeight + ", columnNum=" + columnNum);
			}

			// 項目の幅
			mItemWidth = (short)(mAreaWidth / columnNum);

			// タイル表示中の左余白(割り切れない部分の補正)
			mDrawLeft = (mAreaWidth - (mItemWidth * columnNum)) / 2;

			// リストサイズ
			super.setListSize(listSize, columnNum, rowNum, disprange, isRefresh);
		}
	}

	// リストの描画更新
	@Override
	public void update(boolean isUpdate) {
		if (mChangeLayout) {
			// リスト構成の変化がある
			requestLayout(true);
			mChangeLayout = false;
		}
		mDrawNoticeListener.onUpdateArea(ListScreenView.AREATYPE_FILELIST, isUpdate);
	}

	public void setScrollPos(int pos, boolean update) {
		;
	}

	@Override
	synchronized protected short calcRowHeight(int index) {
		int height;
		if (mFileList == null) {
			return 0;
		}

		if (mListMode == LISTMODE_LIST) {
			if (mTitleSep[index] == null) {
				// テキストの描画範囲
				int tcx = mAreaWidth - mItemMargin * 2;
				if (mThumbFlag) {
                    tcx -= mIconWidth * mListIconHeight / mIconHeight +  mItemMargin;	// サムネイル有の場合は幅が減る
				}

				FileData fd = mFileList.get(index);
				mTitleSep[index] = getMultiLine(fd.getName(), tcx, MAXLINE_TITLE, mNamePaint);

				String info = fd.getFileInfo();

				mReadInfo[index] = "";
				if ((fd.getState() >= 0) && (fd.getMaxpage() > 0)) {
					// 読書率
					float rate = (float) (fd.getState() + 1) / (float) fd.getMaxpage();
					mReadInfo[index] = (int)(rate * 100) + "% Read.";

				}
				else if (fd.getState() == DEF.PAGENUMBER_READ) {
					mReadInfo[index] = "100% Read.";
				}
				else if (fd.getState() == DEF.PAGENUMBER_UNREAD) {
					mReadInfo[index] = "Unread.";
				}
				else if (fd.getState() != DEF.PAGENUMBER_UNREAD && fd.getMaxpage() == DEF.PAGENUMBER_NONE) {
					mReadInfo[index] = "??% Read.";
				}

				if (info != null) {
					float[] result = new float[info.length()];
					float[] len = { 0, 0 };
					mInfoPaint.getTextWidths(info, result);
					for (int i = 0; i < result.length; i++) {
						len[0] += result[i];
						if (i < DATETIME_LENGTH) {
							// 日付部分の長さ
							len[1] += result[i];
						}
					}

					if (tcx >= (int) Math.ceil(len[0])) {
						mInfoSep[index] = new String[1];
						mInfoSep[index][0] = info;
					} else {
						// 更新日時とサイズを分割
						mInfoSep[index] = new String[2];
						mInfoSep[index][0] = info.substring(0, DATETIME_LENGTH);
						mInfoSep[index][1] = info.substring(DATETIME_LENGTH).trim();
						if (len[1] > tcx) {
							// 日付がはいりりきらない
							mInfoSep[index][0] = "[" + mInfoSep[index][0].substring(3, DATETIME_LENGTH - 4) + "]";
						}
					}
				}
			}

			// 項目高さを求める
			height = (mTitleSize + mTitleDescent) * mTitleSep[index].length + (mInfoSize + mInfoDescent) * (mInfoSep[index].length + 1);
			// ビットマップ表示のサイズ確保
            if (mThumbFlag && height < mListIconHeight) {
                height = mListIconHeight;
			}
			height += mItemMargin * 2;
		}
		else {
			// タイルの時はサイズが決まっている
			height = mItemHeight;
		}
		return (short)height;
	}
}
