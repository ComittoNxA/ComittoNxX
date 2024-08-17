package src.comitton.view.list;

import java.util.ArrayList;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.common.TextFormatter;
import src.comitton.data.FileData;
import src.comitton.data.RecordItem;
import src.comitton.filelist.ServerSelect;
import src.comitton.listener.DrawNoticeListener;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.Log;

public class RecordListArea extends ListArea {
	private int mBakColor;
	private int mCurColor;
	private int mSepColor;

	private ArrayList<RecordItem> mRecordList;
	private long mLastModified;
	private int mItemMargin;

	private Paint mFillPaint;
	private Paint mLinePaint;
	private Paint mTitlePaint;
	private Paint mInfoPaint;
	private Paint mBitmapPaint;

	private String mTitleName;

	private String mTitleSep[][][];
	private String mInfoSep[][][];
	private Bitmap mBitmap[];

	private int mTitleSize;
	private int mTitleAscent;
	private int mTitleDescent;

	private int mInfoSize;
	private int mInfoAscent;
	private int mInfoDescent;

	private int mBitmapSize;
	private int mBitmapColor;

	private Context mContext;

	private DrawNoticeListener mDrawNoticeListener = null;

	private boolean mChangeLayout = false;

	// コンストラクタ
	public RecordListArea(Context context, short areatype, int listtype, String title, DrawNoticeListener listener) {
		super(context, listtype);
		mContext = context;
//		mAreaType = areatype;
		mTitleName = title;
		// リストファイルの更新日時
		mLastModified = 0;

		mFillPaint = new Paint();
		mFillPaint.setStyle(Style.FILL);
		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTitlePaint.setTypeface(Typeface.DEFAULT);
		mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mInfoPaint.setTypeface(Typeface.MONOSPACE);
		mInfoPaint.setTextAlign(Paint.Align.LEFT);
		mBitmapPaint = new Paint();

		mDrawNoticeListener = listener;
	}

	/**
	 * リストを描画
	 */
	@Override
	public void drawArea(Canvas canvas, int baseX, int baseY) {
		// 背景色
		mFillPaint.setColor(mBakColor);
		canvas.drawRect(baseX, baseY, baseX + mAreaWidth, baseY + mAreaHeight, mFillPaint);

		drawListItems(canvas, baseX, baseY);
		super.drawArea(canvas, baseX, baseY);
	}

	private void drawListItems(Canvas canvas, int baseX, int baseY) {
		int cx = mAreaWidth;
		int cy = mAreaHeight;
		int y;
		int x;
		int index;
		int listnum = 0;
		if (mRecordList == null) {
			return;
		}
		listnum = mRecordList.size();

		mTitlePaint.setTextAlign(Paint.Align.LEFT);

		int ypos = mTopPos;
		for (int iy = mTopRow; ypos < cy; iy++) {
			index = iy;
			if (index >= listnum) {
				break;
			}
			int itemHeight = 0;
			RecordItem rd = null;
			try {
				// タイミングによってｍRecordListが0個になっていて落ちるため
				itemHeight = getRowHeight(index);
				rd = mRecordList.get(index);
			}
			catch (Exception e) {
				;
			}
			x = baseX;
			y = baseY + ypos;

			// 選択背景塗り
			int color;
			boolean isTouchDraw = (mTouchIndex == index && mTouchDraw);
			boolean isCursorDraw = (mCursorDisp == true && mCursorPosY * mColumnNum + mCursorPosX == index); 
			color = mBakColor;
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
				canvas.drawRect(x, y, x + cx, y + mh, mFillPaint);
				canvas.drawRect(x, y + itemHeight - mh, x + cx, y + itemHeight, mFillPaint);
			}
			
			
			// 項目区切り
			mFillPaint.setColor(mSepColor);
			canvas.drawRect(x, y + itemHeight, x + cx, y + itemHeight + BORDER_HEIGHT, mFillPaint);

			x = baseX + mItemMargin;
			y = baseY + ypos + mItemMargin;

			// タイトル描画
			if (rd != null && rd.getType() == RecordItem.TYPE_MENU) {
				// アイコン描画
				Bitmap bm = mBitmap[index];
				canvas.drawBitmap(bm,  x, y + (int)(mTitleSize / 4), mBitmapPaint);

				// タイトル描画
				for (int j = 0; j < mTitleSep[index].length; j++) {
					for (int i = 0; i < mTitleSep[index][j].length; i++) {
						canvas.drawText(mTitleSep[index][j][i], x + mBitmapSize + mItemMargin, y + (mTitleSize / 2) + mTitleAscent, mTitlePaint);
						y += mTitleSize + mTitleDescent;
					}
				}

				// サマリ描画
				for (int j = 0; j < mInfoSep[index].length; j++) {
					if (mInfoSep[index][j] == null) {
						continue;
					}
					for (int i = 0; i < mInfoSep[index][j].length; i++) {
						canvas.drawText(mInfoSep[index][j][i], x + mBitmapSize + mItemMargin, y + mInfoAscent, mInfoPaint);
						y += mInfoSize + mInfoDescent;
					}
				}
			}
			else if (mTitleSep != null && mInfoSep != null && mTitleSep[index] != null && mInfoSep[index] != null) {
				// タイトル描画
				for (int j = 0; j < mTitleSep[index].length; j++) {
					for (int i = 0; i < mTitleSep[index][j].length; i++) {
						canvas.drawText(mTitleSep[index][j][i], x, y + mTitleAscent, mTitlePaint);
						y += mTitleSize + mTitleDescent;
					}
				}

				// サマリ描画
				for (int j = 0; j < mInfoSep[index].length; j++) {
					if (mInfoSep[index][j] == null) {
						continue;
					}
					for (int i = 0; i < mInfoSep[index][j].length; i++) {
						canvas.drawText(mInfoSep[index][j][i], x, y + mInfoAscent, mInfoPaint);
						y += mInfoSize + mInfoDescent;
					}
				}
			}
			ypos += itemHeight + BORDER_HEIGHT;
		}
		return;
	}

	// リスト設定
	public void setList(ArrayList<RecordItem> recordList) {
		mRecordList = recordList;
		requestLayout(false);
		mChangeLayout = false;
	}

	public ArrayList<RecordItem> getList() {
		return mRecordList;
	}

	// 描画設定
	public void setDrawColor(int clr_txt, int clr_inf, int clr_bak, int clr_cur, int clr_frm) {
		mTitlePaint.setColor(clr_txt);
		mInfoPaint.setColor(clr_inf);
		mBakColor = clr_bak;
		mCurColor = clr_cur;
		mSepColor = clr_inf;
		mLinePaint.setColor(clr_frm);
//		mBitmapPaint.setColor(clr_inf);
		mBitmapColor = clr_txt;
	}

	public void setDrawInfo(int titlesize, int infosize, int margin) {
		mTitleSize = titlesize;
		mInfoSize = infosize;
		mBitmapSize = (int)(mTitleSize * 1.6);

		mItemMargin = (short) margin;

		// テキスト描画属性設定
		FontMetrics fm;
		mTitlePaint.setTextSize(titlesize);
		fm = mTitlePaint.getFontMetrics();
		mTitleAscent = (int) (-fm.ascent);
		mTitleDescent = (int) (fm.descent);

		mInfoPaint.setTextSize(infosize);
		fm = mInfoPaint.getFontMetrics();
		mInfoAscent = (int) (-fm.ascent);
		mInfoDescent = (int) (fm.descent);

		mTitleSep = null;
		mInfoSep = null;

		mChangeLayout = true;
	}

	// リストのタイトルを返す
	public String getTitle() {
		return mTitleName;
	}

	public long getLastModefied() {
		return mLastModified;
	}

	public void setLastModefied(long modified) {
		mLastModified = modified;
	}

	// リストのデータを取得
	public RecordItem getRecordItem(int index) {
		RecordItem rd = null;
		if (0 <= index && index < mRecordList.size()) {
			rd = mRecordList.get(index);
		}
		return rd;
	}

	// リストから削除
	public void remove(FileData fd) {
		if (mRecordList != null) {
			int index = mRecordList.indexOf(fd);
			if (index >= 0) {
				mRecordList.remove(fd);
			}
			mChangeLayout = true;
			update(false);
		}
	}

	// リストをクリア
	public void clear() {
		mRecordList = null;
		mChangeLayout = true;
		update(false);
	}

	public void notifyUpdate() {
		mChangeLayout = true;
		update(false);
	}

	/**
	 * リストのレイアウト情報を保存
	 */
	private void requestLayout(boolean isRefresh) {
		int width = mAreaWidth;
		int listSize = 0;

		if (mRecordList != null && mAreaWidth != 0 && mAreaHeight != 0) {
			// 項目数
			listSize = mRecordList.size();

			// テキストの描画範囲
			int tcx = width - mItemMargin * 2;

			mTitleSep = new String[listSize][][];
			mInfoSep = new String[listSize][][];
			mBitmap = new Bitmap[listSize];

			// 各項目の行数分割
			for (int index = 0; index < listSize; index++) {
				RecordItem rd = mRecordList.get(index);
				Resources res = mContext.getResources();

				String work;
				if (rd.getType() == RecordItem.TYPE_FOLDER) {
					mTitleSep[index] = new String[2][];
					mTitleSep[index][0] = TextFormatter.getMultiLine("[" + rd.getServerName() + "]", tcx, mTitlePaint, 1);
					mTitleSep[index][1] = TextFormatter.getMultiLine(rd.getPath(), tcx, mTitlePaint, 99);
				}
				else if (rd.getType() == RecordItem.TYPE_SERVER){
					mTitleSep[index] = new String[1][];
					if (rd.getServerName().equals("")) {
						mTitleSep[index][0] = TextFormatter.getMultiLine(res.getString(R.string.undefine), tcx, mTitlePaint, 1);
					}
					else {
						mTitleSep[index][0] = TextFormatter.getMultiLine("[" + rd.getServerName() + "]", tcx, mTitlePaint, 1);
					}
				}
				else if (rd.getType() == RecordItem.TYPE_MENU){
					mTitleSep[index] = new String[1][];
					mTitleSep[index][0] = TextFormatter.getMultiLine(rd.getDispName(), tcx, mTitlePaint, 1);
				}
				else {
					String name = rd.getDispName();
					mTitleSep[index] = new String[2][];
					mTitleSep[index][0] = TextFormatter.getMultiLine(name, tcx, mTitlePaint, 1);
					if (rd.getType() == RecordItem.TYPE_IMAGE) {
						work = rd.getImage();
					}
					else {
						work = "Page : " + (rd.getPage() + 1);
					}
					mTitleSep[index][1] = TextFormatter.getMultiLine(work, tcx, mTitlePaint, 99);
				}

				if (rd.getType() == RecordItem.TYPE_FOLDER) {
					mInfoSep[index] = new String[0][];
				}
				else if (rd.getType() == RecordItem.TYPE_SERVER) {
					String name = rd.getServerName();
					String uri = "";
					if (rd.getServer() == ServerSelect.INDEX_LOCAL) {
						uri = rd.getPath();
					}
					else {
						if (name != null && !name.equals("")) {
							String user = rd.getUser();
							String pass = rd.getPass();
							String host = rd.getHost();
							uri = "smb://";
							if (user != null && !user.equals("")) {
								uri += user;
								if (pass != null && !pass.equals("")) {
									uri += ":******";
								}
								uri += "@";
							}
							uri += host + "/";
						}
					}
					mInfoSep[index] = new String[1][];
					mInfoSep[index][0] = TextFormatter.getMultiLine(uri, tcx, mInfoPaint, 5);
				}
				else if (rd.getType() == RecordItem.TYPE_MENU){
					int icon = rd.getIcon();
					mInfoSep[index] = new String[1][];
					mInfoSep[index][0] = TextFormatter.getMultiLine("", tcx, mInfoPaint, 5);

					// ビットマップリソースを読み込み
					mBitmap[index] = ImageAccess.createIcon(res, icon, mBitmapSize, mBitmapColor);
				}
				else {
					mInfoSep[index] = new String[3][];
					mInfoSep[index][0] = TextFormatter.getMultiLine("[" + rd.getServerName() + "]", tcx, mInfoPaint, 1);
					mInfoSep[index][1] = TextFormatter.getShortening(rd.getPath(), tcx, mInfoPaint);
					if (rd.getFile().length() > 0) {
						mInfoSep[index][2] = TextFormatter.getMultiLine(rd.getFile(), tcx, mInfoPaint, 99);
					}
				}
			}
		}

		int disprange = mAreaHeight / (mTitleSize + mTitleDescent + mInfoSize + mInfoDescent + mItemMargin * 2); 
		// リストサイズ
		super.setListSize(listSize, 1, listSize, disprange, isRefresh);
	}

	/**
	 * リストの描画更新
	 */
	@Override
	public void update(boolean isRealtime) {
		if (mChangeLayout) {
			// リスト構成の変化がある
			requestLayout(true);
			mChangeLayout = false;
		}
		mDrawNoticeListener.onUpdateArea(ListScreenView.AREATYPE_LIST, false);
	}

	@Override
	protected boolean isMarker(int index) {
		return false;
	}

	/**
	 * 項目の高さを計算する
	 */
	@Override
	protected short calcRowHeight(int row) {
		// タイトルの行数を求める
		int textLine = 0;
		int infoLine = 0;
		if (mTitleSep != null && mInfoSep != null && mTitleSep[row] != null && mInfoSep[row] != null && 0 <= row && row < mTitleSep.length) {
    		for (int i = 0; i < mTitleSep[row].length; i++) {
    			textLine += mTitleSep[row][i].length;
    		}
    
    		// 情報の行数を求める
    		for (int i = 0; i < mInfoSep[row].length; i++) {
    			if (mInfoSep[row][i] != null) {
    				infoLine += mInfoSep[row][i].length;
    			}
    		}
		}
		else {
			return -1;
		}
		// 項目高さを求める
		int height = (mTitleSize + mTitleDescent) * textLine + (mInfoSize + mInfoDescent) * infoLine;
		return (short)(height + mItemMargin * 2);
	}

}
