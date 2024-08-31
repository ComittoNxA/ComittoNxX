package src.comitton.view.list;

import java.util.ArrayList;

import src.comitton.config.SetFileColorActivity;
import src.comitton.data.FileData;
import src.comitton.data.RecordItem;
import src.comitton.filelist.RecordList;
import src.comitton.listener.DrawNoticeListener;
import src.comitton.view.list.ListSwitcher.ListSwitcherListener;

import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.preference.PreferenceManager;
//import android.util.Log;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ListScreenView extends SurfaceView implements SurfaceHolder.Callback, DrawNoticeListener, ListSwitcherListener, Runnable {
	// 描画領域の種別
	public final static short AREATYPE_NONE = 0x00;
	public final static short AREATYPE_TITLE = 0x01;
	public final static short AREATYPE_TOOLBAR = 0x02;
	public final static short AREATYPE_FILELIST = 0x04;
	public final static short AREATYPE_DIRLIST = 0x08;
	public final static short AREATYPE_SERVERLIST = 0x10;
	public final static short AREATYPE_FAVOLIST = 0x20;
	public final static short AREATYPE_HISTLIST = 0x40;
	public final static short AREATYPE_MENULIST = 0x80;
	public final static short AREATYPE_SELECTOR = 0x100;
	public final static short AREATYPE_LIST = 0x200;

	public final static short AREATYPE_ALL = 0xFFF;

//	public final static short LISTINDEX_FILE = 0;
//	public final static short LISTINDEX_DIR = 1;
//	public final static short LISTINDEX_SERVER = 2;
//	public final static short LISTINDEX_FAVO = 3;
//	public final static short LISTINDEX_HIST = 4;
//	public final static short LISTINDEX_MENU = 5;

	// 描画情報
	public TitleArea mTitleArea;
	public ToolbarArea mToolbarArea;
	public SelectorArea mSelectorArea;
	public FileListArea mFileListArea;
	public RecordListArea mDirListArea;
	public RecordListArea mServerListArea;
	public RecordListArea mFavoListArea;
	public RecordListArea mHistListArea;
	public RecordListArea mMenuListArea;
	// public ScrollerArea mScrollerArea;

	private Rect mTitleRect;
	private Rect mToolbarRect;
	private Rect mSelectorRect;
	private Rect mFileListRect;
	private Rect mDirListRect;
	private Rect mServerListRect;
	private Rect mFavoListRect;
	private Rect mHistListRect;
	private Rect mMenuListRect;
	// private Rect mScrollerRect;

	private int mListBorder;

	private int mOrientation;

	private int mDuration = 800; // 長押し時間

	// アプリ制御
	private SurfaceHolder mHolder;
	private Thread mUpdateThread;
	private boolean mIsRunning;
	private ListSwitcher mListSwitcher;
	private Rect mTargetRect;
	private Rect mListRect;

	private boolean mDrawEnable;

	private ListNoticeListener mListNoticeListener = null;

	// 現在表示中のリスト情報
	private short mListType[];
	// private short mListIndex;
	// private int mListOffsetX;
	private long mListPosition;
	private short mUpdateArea;

	private Paint mFillPaint;
	private int mBakColor;

	public ListScreenView(Context context, int duration) {
		this(context);
		Log.d("ListScreenView", "ロングタッチ遅延時間=" + duration);
		mFileListArea.setDuration(duration);
	}

	public ListScreenView(Context context) {
		super(context);

		Resources res = context.getResources();
		mTitleArea = new TitleArea(context, this);
		mToolbarArea = new ToolbarArea(context, this);
		mFileListArea = new FileListArea(context, this);
		mDirListArea = new RecordListArea(context, AREATYPE_DIRLIST, RecordList.TYPE_DIRECTORY, res.getString(R.string.drTitle), this);
		mServerListArea = new RecordListArea(context, AREATYPE_SERVERLIST, RecordList.TYPE_SERVER, res.getString(R.string.saTitle1), this);
		mFavoListArea = new RecordListArea(context, AREATYPE_FAVOLIST, RecordList.TYPE_BOOKMARK, res.getString(R.string.bmTitle), this);
		mHistListArea = new RecordListArea(context, AREATYPE_HISTLIST, RecordList.TYPE_HISTORY, res.getString(R.string.hsTitle), this);
		mMenuListArea = new RecordListArea(context, AREATYPE_MENULIST, RecordList.TYPE_MENU, res.getString(R.string.listname05), this);
		mSelectorArea = new SelectorArea(context, this);
		// mScrollerArea = new ScrollerArea(context, this);

		// 履歴系のタイトル設定
		mDirListArea.setListTitle("[" + res.getString(R.string.drTitle) + "]", null);
		mServerListArea.setListTitle("[" + res.getString(R.string.saTitle1) + "]", res.getString(R.string.saTitle2));
		mFavoListArea.setListTitle("[" + res.getString(R.string.bmTitle) + "]", null);
		mHistListArea.setListTitle("[" + res.getString(R.string.hsTitle) + "]", null);
		mMenuListArea.setListTitle("[" + res.getString(R.string.listname05) + "]", null);

		float density = context.getResources().getDisplayMetrics().scaledDensity;
		mListBorder = (int) (3 * density);

		// コールバックの登録
		mHolder = getHolder();
		mHolder.addCallback(this);
		mListSwitcher = new ListSwitcher(this);

		mFillPaint = new Paint();
		mFillPaint.setStyle(Style.FILL);

		// 描画領域ロック用
		mTargetRect = new Rect();
		mListRect = new Rect();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		mBakColor = SetFileColorActivity.getTibColor(sp);
		mUpdateArea = 0;
	}

	/*
	 * private String mResizingStr; private TextView mTextView;
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 呼ばなくてもChangedが発生
		mIsRunning = true;
		mDrawEnable = true;

		// 描画スレッド起動
		mUpdateThread = new Thread(this);
		mUpdateThread.setPriority(Thread.MAX_PRIORITY);
		mUpdateThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
		// Surface の属性が変更された際にコールされる
		synchronized (surfaceHolder) {
			mDrawEnable = true;
			int listIndex = getIntHigh(mListPosition);
			mListPosition = makeLong(listIndex, 0);
			updateLayout();
		}
		update(AREATYPE_ALL);
		// mUpdateLayoutMsg = mHandler.obtainMessage(EVENT_UPDATELAYOUT);
		// mHandler.sendMessage(mUpdateLayoutMsg);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface が破棄された際にコールされる
		mIsRunning = false;
		mUpdateThread.interrupt();
	}

	@Override
	public void onUpdateArea(short areatype, boolean isRealtime) {
		if (mUpdateThread != null) {
			if (isRealtime == false) {
				mUpdateArea |= areatype;
				mUpdateThread.interrupt();
			}
			else {
				update(areatype);
			}
		}
	}

	public short findAreaType(int x, int y) {
		int listIndex = getIntHigh(mListPosition);
		short areatype = AREATYPE_NONE;
		if (checkInRect(mTitleRect, x, y)) {
			areatype = AREATYPE_TITLE;
		}
		else if (checkInRect(mSelectorRect, x, y)) {
			areatype = AREATYPE_SELECTOR;
		}
		else if (mListType[listIndex] == RecordList.TYPE_FILELIST) {
			if (checkInRect(mToolbarRect, x, y)) {
				areatype = AREATYPE_TOOLBAR;
			}
			else if (checkInRect(mFileListRect, x, y)) {
				areatype = AREATYPE_FILELIST;
			}
		}
		else if (mListType[listIndex] == RecordList.TYPE_DIRECTORY) {
			if (checkInRect(mDirListRect, x, y)) {
				areatype = AREATYPE_DIRLIST;
			}
		}
		else if (mListType[listIndex] == RecordList.TYPE_SERVER) {
			if (checkInRect(mServerListRect, x, y)) {
				areatype = AREATYPE_SERVERLIST;
			}
		}
		else if (mListType[listIndex] == RecordList.TYPE_BOOKMARK) {
			if (checkInRect(mFavoListRect, x, y)) {
				areatype = AREATYPE_FAVOLIST;
			}
		}
		else if (mListType[listIndex] == RecordList.TYPE_HISTORY) {
			if (checkInRect(mHistListRect, x, y)) {
				areatype = AREATYPE_HISTLIST;
			}
		}
		else if (mListType[listIndex] == RecordList.TYPE_MENU) {
			if (checkInRect(mMenuListRect, x, y)) {
				areatype = AREATYPE_MENULIST;
			}
		}
		// else if (checkInRect(mScrollerRect, x, y)) {
		// areatype = AREATYPE_SCROLLER;
		// }
		return areatype;
	}

	// 座標が矩形内にあるかをチェック
	private boolean checkInRect(Rect rc, int x, int y) {
		if (rc.left <= x && x <= rc.right && rc.top <= y && y <= rc.bottom) {
			return true;
		}
		return false;
	}

	public void updateLayout() {
		mOrientation = this.getResources().getConfiguration().orientation;

		int cx = getWidth();
		int cy = getHeight();

		mTitleRect = mTitleArea.setDrawArea(0, 0, cx, cy, mOrientation);
		mToolbarRect = mToolbarArea.setDrawArea(0, mTitleRect.bottom, cx, cy, mOrientation);
		if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 横画面
			int x = mToolbarRect.right;
			int y = mTitleRect.bottom;
			mSelectorRect = mSelectorArea.setDrawArea(x, y, cx, cy, mOrientation);
			mFileListRect = mFileListArea.setDrawArea(x, y, mSelectorRect.left, cy, mOrientation);
			mDirListRect = mDirListArea.setDrawArea(0, y, mSelectorRect.left, cy, mOrientation);
			mServerListRect = mServerListArea.setDrawArea(0, y, mSelectorRect.left, cy, mOrientation);
			mFavoListRect = mFavoListArea.setDrawArea(0, y, mSelectorRect.left, cy, mOrientation);
			mHistListRect = mHistListArea.setDrawArea(0, y, mSelectorRect.left, cy, mOrientation);
			mMenuListRect = mMenuListArea.setDrawArea(0, y, mSelectorRect.left, cy, mOrientation);
		}
		else {
			// 縦画面
			int y1 = mToolbarRect.bottom;
			int y2 = mTitleRect.bottom;
			mSelectorRect = mSelectorArea.setDrawArea(0, y1, cx, cy, mOrientation);
			mFileListRect = mFileListArea.setDrawArea(0, y1, cx, mSelectorRect.top, mOrientation);
			mDirListRect = mDirListArea.setDrawArea(0, y2, cx, mSelectorRect.top, mOrientation);
			mServerListRect = mServerListArea.setDrawArea(0, y2, cx, mSelectorRect.top, mOrientation);
			mFavoListRect = mFavoListArea.setDrawArea(0, y2, cx, mSelectorRect.top, mOrientation);
			mHistListRect = mHistListArea.setDrawArea(0, y2, cx, mSelectorRect.top, mOrientation);
			mMenuListRect = mMenuListArea.setDrawArea(0, y2, cx, mSelectorRect.top, mOrientation);
		}
		mListRect.set(mToolbarRect.left, mToolbarRect.top, mFileListRect.right, mFileListRect.bottom);
		int minwidth = Math.min(cx, cy);
		mListSwitcher.setWidth(mFileListRect.right, minwidth / 10, minwidth / 2, mListBorder);

		// リストのレイアウト更新
		notifyUpdate();

		requestFocus();
	}

	public void update(short areatype) {
		if (mDrawEnable == false) {
			// 描画停止
			return;
		}

		Rect rc = mTargetRect;
		int listOffsetX = getIntLow(mListPosition);
		// if (areatype == AREATYPE_ALL) {
		// // 全体描画
		// rc = mAllRect;
		// }
		// else {
		if (rc != null && mTitleRect != null) {
			rc.set(getWidth(), getHeight(), 0, 0);
			if ((areatype & AREATYPE_TITLE) != 0) {
				rc.union(mTitleRect);
			}
			if (((areatype & AREATYPE_LIST) != 0) || (((areatype & AREATYPE_FILELIST) != 0) && listOffsetX != 0)) {
				// リストとツールバーを一緒に
				rc.union(mListRect);
			}
			if ((areatype & AREATYPE_TOOLBAR) != 0) {
				rc.union(mToolbarRect);
			}
			if ((areatype & AREATYPE_FILELIST) != 0 && getListType() == RecordList.TYPE_FILELIST) {
				rc.union(mFileListRect);
			}
			if ((areatype & AREATYPE_SELECTOR) != 0) {
				rc.union(mSelectorRect);
			}
			if (rc.left < rc.right && rc.top < rc.bottom) {
				update(areatype, rc);
			}
			// }
		}

		// if (areatype == AREATYPE_ALL) {
		// // 全体描画
		// update(AREATYPE_ALL, mAllRect);
		// }
		// else if (areatype == AREATYPE_TITLE) {
		// update(areatype, mTitleRect);
		// }
		// else if (areatype == AREATYPE_LIST || (areatype == AREATYPE_FILELIST
		// && mListOffsetX != 0)) {
		// // リストとツールバーを一緒に
		// update(areatype, mListRect);
		// }
		// else if (areatype == AREATYPE_TOOLBAR) {
		// update(areatype, mToolbarRect);
		// }
		// else if (areatype == AREATYPE_FILELIST) {
		// update(areatype, mFileListRect);
		// }
		// else if ((areatype & AREATYPE_SELECTOR) != 0) {
		// update(areatype, mSelectorRect);
		// }
		// else if () {
		// update(AREATYPE_TITLE, mTitleRect);
		// update(AREATYPE_TOOLBAR, mToolbarRect);
		// update(AREATYPE_FILELIST, mFileListRect);
		// update(AREATYPE_SELECTOR, mSelectorRect);
		// }
		return;
	}

	// private Object mLock = new Object();
	// private int mLockCount = 0;
	private boolean update(short areatype, Rect rc) {
		// if (areatype != mAreaTypeLog) {
		// Log.d("ListScreenView", "update at=" + Integer.toHexString(areatype)
		// + ", de=" + mDrawEnable + ", rc=(" + rc.left + "," + rc.top + "," +
		// rc.right + "," + rc.bottom + ")");
		// mAreaTypeLog = areatype;
		// }
		if (rc == null) {
			return false;
		}

		// synchronized (mLock) {
		Rect lockRect = new Rect(rc);
		// if (mLockCount % 8 == 0) {
		// rcClone = new Rect(0, 0, getWidth(), getHeight());
		// }
		// else {
		// rcClone = new Rect(0, 0, getWidth()/2, getHeight()/2);
		// }
		// mLockCount ++;
		// Log.d("update", "ct=" + mLockCount + ", at=" + areatype + ", l="
		// +rc.left+ ", t=" +rc.top+ ", r=" +rc.right+ ", b=" + rc.bottom);

		// リストの切り替え対応
		// if (areatype == AREATYPE_TOOLLIST) {
		// rcClone.offset(mListOffsetX, 0);
		// if (mOrientation == Configuration.ORIENTATION_LANDSCAPE &&
		// rcClone.right > mSelectorRect.left) {
		// // 横画面の場合はセレクタをつぶさないように
		// rcClone.right = mSelectorRect.left;
		// }
		//
		// }

		Canvas canvas = null;
		SurfaceHolder surfaceHolder = getHolder();
		try {

			canvas = surfaceHolder.lockCanvas(lockRect); // ロックして、書き込み用のcanvasを受け取る
			if (canvas == null) {
				return false; // canvasが受け取れてなかったら抜ける
			}

			synchronized (surfaceHolder) {
				draw(canvas, areatype, lockRect);
			}
		} finally {
			if (canvas != null)
				surfaceHolder.unlockCanvasAndPost(canvas); // 例外が出て、canvas受け取ってたらロックはずす
		}
		return true;
	}

	public void draw(Canvas canvas, short areatype, Rect rc) {
		long listPosition = mListPosition;
		int stListIndex = getIntHigh(listPosition);
		int stListOffsetX = getIntLow(listPosition);
		// Log.d("draw", "lp=" + mListPosition + ", idx=" + stListIndex +
		// ", ofx=" + stListOffsetX);
		if (stListOffsetX != 0) {
			// 背景塗りつぶし
			canvas.drawColor(mBakColor);
			if (Rect.intersects(mListRect, rc)) {
				for (int i = 0; i < 2; i++) {
					int type = -1;
					int offsetX;
					int listindex;
					if (i == 0) {
						// 1週目
						type = mListType[stListIndex];
						offsetX = stListOffsetX;
					}
					else {
						if (Math.abs(stListOffsetX) < mListBorder) {
							break;
						}
						else if (stListOffsetX < 0) {
							offsetX = stListOffsetX + (mListRect.right + mListBorder);
							listindex = calcListIndex(+1, stListIndex);
						}
						else {
							offsetX = stListOffsetX - (mListRect.right + mListBorder);
							listindex = calcListIndex(-1, stListIndex);
						}
						type = mListType[listindex];
					}

					if (type == RecordList.TYPE_FILELIST) {
						mFileListArea.drawArea(canvas, mFileListRect.left + offsetX, mFileListRect.top);
						mToolbarArea.drawArea(canvas, mToolbarRect.left + offsetX, mToolbarRect.top);
					}
					else if (type == RecordList.TYPE_DIRECTORY) {
						mDirListArea.drawArea(canvas, mDirListRect.left + offsetX, mDirListRect.top);
					}
					else if (type == RecordList.TYPE_SERVER) {
						mServerListArea.drawArea(canvas, mServerListRect.left + offsetX, mServerListRect.top);
					}
					else if (type == RecordList.TYPE_BOOKMARK) {
						mFavoListArea.drawArea(canvas, mFavoListRect.left + offsetX, mFavoListRect.top);
					}
					else if (type == RecordList.TYPE_HISTORY) {
						mHistListArea.drawArea(canvas, mHistListRect.left + offsetX, mHistListRect.top);
					}
					else if (type == RecordList.TYPE_MENU) {
						mMenuListArea.drawArea(canvas, mMenuListRect.left + offsetX, mMenuListRect.top);
					}
				}
			}
		}
		else {
			int listtype = mListType[stListIndex];
			if (listtype == RecordList.TYPE_FILELIST) {
				if (Rect.intersects(mFileListRect, rc)) {
					// ファイルリスト領域更新
					mFileListArea.drawArea(canvas, mFileListRect.left, mFileListRect.top);
				}
				if (Rect.intersects(mToolbarRect, rc)) {
					// ツールバー領域更新
					mToolbarArea.drawArea(canvas, mToolbarRect.left, mToolbarRect.top);
				}
			}
			else if (listtype == RecordList.TYPE_DIRECTORY) {
				mDirListArea.drawArea(canvas, mDirListRect.left, mDirListRect.top);
			}
			else if (listtype == RecordList.TYPE_SERVER) {
				mServerListArea.drawArea(canvas, mServerListRect.left, mServerListRect.top);
			}
			else if (listtype == RecordList.TYPE_BOOKMARK) {
				mFavoListArea.drawArea(canvas, mFavoListRect.left, mFavoListRect.top);
			}
			else if (listtype == RecordList.TYPE_HISTORY) {
				mHistListArea.drawArea(canvas, mHistListRect.left, mHistListRect.top);
			}
			else if (listtype == RecordList.TYPE_MENU) {
				mMenuListArea.drawArea(canvas, mMenuListRect.left, mMenuListRect.top);
			}
		}
		if (Rect.intersects(mTitleRect, rc)) {
			// タイトル領域更新
			mTitleArea.drawArea(canvas);
		}
		if (Rect.intersects(mSelectorRect, rc)) {
			// セレクタ領域更新
			mSelectorArea.drawArea(canvas, mSelectorRect.left, mSelectorRect.top);
		}

		// if ((areatype & AREATYPE_TOOLLIST) == AREATYPE_TOOLLIST) {
		// // 背景塗りつぶし
		// canvas.drawColor(mBakColor);
		// }
		// if ((areatype & AREATYPE_FILELIST) != 0) {
		// mFileListArea.drawArea(canvas, mFileListRect.left + mListOffsetX,
		// mFileListRect.top);
		// }
		// if ((areatype & AREATYPE_TOOLBAR) != 0) {
		// mToolbarArea.drawArea(canvas, mToolbarRect.left + mListOffsetX,
		// mToolbarRect.top);
		// }
		// if ((areatype & AREATYPE_TITLE) != 0) {
		// mTitleArea.drawArea(canvas);
		// }
		// if ((areatype & AREATYPE_SELECTOR) != 0) {
		// mSelectorArea.drawArea(canvas, mSelectorRect.left,
		// mSelectorRect.top);
		// }

		// // 背景塗りつぶし
		// canvas.drawColor(mBakColor);
		//
		// if (mListOffsetX < 0) {
		//
		// }
		// for (int i = 0; i < 2; i++) {
		// int type = -1;
		// int offsetX;
		// int listindex;
		// if (i == 0) {
		// // 1週目
		// type = mListType[mListIndex];
		// offsetX = mListOffsetX;
		// }
		// else {
		// if (Math.abs(mListOffsetX) < mListBorder) {
		// break;
		// }
		// else if (mListOffsetX < 0) {
		// offsetX = mListOffsetX + (mFileListRect.right + mListBorder);
		// listindex = calcListIndex(+1);
		// }
		// else {
		// offsetX = mListOffsetX - (mFileListRect.right + mListBorder);
		// listindex = calcListIndex(-1);
		// }
		// type = mListType[listindex];
		// }
		//
		// if (type == RecordList.TYPE_FILELIST) {
		// mFileListArea.drawArea(canvas, mFileListRect.left + offsetX,
		// mFileListRect.top);
		// mToolbarArea.drawArea(canvas, mToolbarRect.left + offsetX,
		// mToolbarRect.top);
		// }
		// else if (type == RecordList.TYPE_DIRECTORY) {
		// mDirListArea.drawArea(canvas, mDirListRect.left + offsetX,
		// mDirListRect.top);
		// }
		// else if (type == RecordList.TYPE_BOOKMARK) {
		// mFavoListArea.drawArea(canvas, mFavoListRect.left + offsetX,
		// mFavoListRect.top);
		// }
		// else if (type == RecordList.TYPE_HISTORY) {
		// mHistListArea.drawArea(canvas, mHistListRect.left + offsetX,
		// mHistListRect.top);
		// }
		// }
		// mTitleArea.drawArea(canvas);
		// mSelectorArea.drawArea(canvas, mSelectorRect.left,
		// mSelectorRect.top);
	}

	public float areaPosX(short areatype, float x) {
		if (areatype == AREATYPE_TITLE) {
			x -= mTitleRect.left;
		}
		else if (areatype == AREATYPE_TOOLBAR) {
			x -= mToolbarRect.left;
		}
		else if (areatype == AREATYPE_FILELIST) {
			x -= mFileListRect.left;
		}
		else if (areatype == AREATYPE_SELECTOR) {
			x -= mSelectorRect.left;
		}
		else if (areatype == AREATYPE_DIRLIST) {
			x -= mDirListRect.left;
		}
		else if (areatype == AREATYPE_SERVERLIST) {
			x -= mServerListRect.left;
		}
		else if (areatype == AREATYPE_FAVOLIST) {
			x -= mFavoListRect.left;
		}
		else if (areatype == AREATYPE_HISTLIST) {
			x -= mHistListRect.left;
		}
		else if (areatype == AREATYPE_MENULIST) {
			x -= mMenuListRect.left;
		}
		return x;
	}

	public float areaPosY(short areatype, float y) {
		if (areatype == AREATYPE_TITLE) {
			y -= mTitleRect.top;
		}
		else if (areatype == AREATYPE_TOOLBAR) {
			y -= mToolbarRect.top;
		}
		else if (areatype == AREATYPE_FILELIST) {
			y -= mFileListRect.top;
		}
		else if (areatype == AREATYPE_SELECTOR) {
			y -= mSelectorRect.top;
		}
		else if (areatype == AREATYPE_DIRLIST) {
			y -= mDirListRect.top;
		}
		else if (areatype == AREATYPE_SERVERLIST) {
			y -= mServerListRect.top;
		}
		else if (areatype == AREATYPE_FAVOLIST) {
			y -= mFavoListRect.top;
		}
		else if (areatype == AREATYPE_HISTLIST) {
			y -= mHistListRect.top;
		}
		else if (areatype == AREATYPE_MENULIST) {
			y -= mMenuListRect.top;
		}
		return y;
	}

	public boolean sendTouchEvent(int action, float x, float y) {
		return mListSwitcher.sendTouchEvent(action, x, y);
	}

	@Override
	public void run() {
		// リスト描画処理監視
		while (true) {
			// リストの描画が必要な時にtrue復帰
			try {
				Thread.sleep(3600000);
			} catch (InterruptedException e) {
				// 描画発生による割り込み
				if (mIsRunning) {
					short areatype = mUpdateArea;
					mUpdateArea = 0;
					// if ((areatype & AREATYPE_TITLE) != 0) {
					// if((areatype & (AREATYPE_LIST | AREATYPE_FILELIST)) != 0)
					// {
					// update(AREATYPE_ALL);
					// }
					// else {
					// update(AREATYPE_TITLE);
					// }
					// }
					// else if (mListType[mListIndex] ==
					// RecordList.TYPE_FILELIST) {
					// update(AREATYPE_FILELIST);
					// }
					// else {
					// update(AREATYPE_LIST);
					// }
					update(areatype);
				}
				else {
					break;
				}
			}
			;
		}
	}

	@Override
	public boolean onListSwitching(float xrange, boolean isFirst) {
		// 横スライド中
		int listIndex = getIntHigh(mListPosition);
		int listOffsetX = (int) xrange;
		if (Math.abs(listOffsetX) > (mListRect.right - mListRect.left) + mListBorder) {
			listOffsetX = ((mListRect.right - mListRect.left) + mListBorder) * (listOffsetX >= 0 ? 1 : -1);
		}

		mListPosition = makeLong(listIndex, listOffsetX);
		if (isFirst) {
			// 日付チェックでリストを再読み込み
			updateRecordList();
		}
		onUpdateArea(AREATYPE_LIST, false);
		return false;
	}

	@Override
	public boolean onListSwitch(int diff, int offsetx) {
		// 横スライド完了でインデックスを変更
		int listIndex = getIntHigh(mListPosition);
		// if (mListOffsetX > mAllRect.right / 3) {
		// listindex--;
		// }
		// else if (mListOffsetX < (mAllRect.right / 3) * -1) {
		// listindex++;
		// }
		listIndex += diff;
		if (listIndex < 0) {
			listIndex = (short) (mListType.length - 1);
		}
		if (listIndex >= mListType.length) {
			listIndex = 0;
		}
		// 表示リストの選択
		setListIndex(listIndex, offsetx, false);

		onUpdateArea(AREATYPE_ALL, false);
		return false;
	}

	// 履歴系リストを更新
	public void updateRecordList() {
		for (int i = 0; i < mListType.length; i++) {
			// 履歴系ならリスト更新チェック
			updateRecordList(mListType[i]);
		}
	}

	// 履歴系リストを更新(リスト種別指定)
	public void updateRecordList(int listtype) {
		// 履歴系ならリスト更新チェック
		if (listtype != RecordList.TYPE_FILELIST) {
			RecordListArea recordList = (RecordListArea) getListByType(listtype);
			mListNoticeListener.onRequestUpdate(recordList, listtype);
		}
	}

	// isForce : 初期化など設定を強制する
	public void setListIndex(int listindex, int offsetx, boolean isForce) {
		if (mListType == null) {
			return;
		}

		// 変化がある場合設定する
		// if (isForce || (0 <= listindex && listindex < mListType.length &&
		// listindex != getIntHigh(mListPosition))) {
		int listtype = mListType[listindex];
		mListPosition = makeLong(listindex, offsetx);
		mSelectorArea.setSelect(listindex);
		updateTitle(listtype);
		updateTitleSortName(listtype);
		// }
		// else {
		// mListPosition = makeLong(getIntHigh(mListPosition), offsetx);
		// }
		update(AREATYPE_ALL);
	}

	// 指定されたリスト種別のインデックスを返す
	public int getListIndex(int listtype) {
		for (int i = 0; i < mListType.length; i++) {
			if (mListType[i] == listtype) {
				return i;
			}
		}
		return -1;
	}

	public void setDrawColor(int clr_dir, int clr_img, int clr_bef, int clr_now, int clr_aft, int clr_bak, int clr_cur, int clr_mrk, int clr_frm, int clr_txt, int clr_inf) {
		mFileListArea.setDrawColor(clr_dir, clr_img, clr_bef, clr_now, clr_aft, clr_bak, clr_cur, clr_mrk, clr_frm, clr_inf);
		mDirListArea.setDrawColor(clr_txt, clr_inf, clr_bak, clr_cur, clr_frm);
		mServerListArea.setDrawColor(clr_txt, clr_inf, clr_bak, clr_cur, clr_frm);
		mFavoListArea.setDrawColor(clr_txt, clr_inf, clr_bak, clr_cur, clr_frm);
		mHistListArea.setDrawColor(clr_txt, clr_inf, clr_bak, clr_cur, clr_frm);
		mMenuListArea.setDrawColor(clr_txt, clr_inf, clr_bak, clr_cur, clr_frm);
	}

	public void setDrawInfo(int tilesize, int titlesize, int infosize, int margin, boolean showext, boolean splitfilename, int maxlines) {
		mFileListArea.setDrawInfo(tilesize, titlesize, infosize, margin, showext, splitfilename, maxlines);
		mDirListArea.setDrawInfo(titlesize, infosize, margin);
		mServerListArea.setDrawInfo(titlesize, infosize, margin);
		mFavoListArea.setDrawInfo(titlesize, infosize, margin);
		mHistListArea.setDrawInfo(titlesize, infosize, margin);
		mMenuListArea.setDrawInfo(titlesize, infosize, margin);
	}

	public void switchListType(boolean isReverse) {
		int listIndex = getIntHigh(mListPosition);
		listIndex += isReverse ? -1 : 1;
		if (listIndex >= mListType.length) {
			listIndex = 0;
		}
		else if (listIndex < 0) {
			listIndex = mListType.length - 1;
		}
		updateRecordList();
		setListIndex(listIndex, 0, false);
		onUpdateArea(ListScreenView.AREATYPE_ALL, false);
	}

	public void setListType(short listtype[]) {
		mListType = listtype;
		// リストの選択
		setListIndex(0, 0, true);
	}

	// タイトル文字列をリストに保持
	public void setListSortType(int listtype, int sorttype) {
		ListArea listarea = getListByType(listtype);
		if (listarea == null) {
			return;
		}

		listarea.setSortType(sorttype);
		updateTitleSortName(listtype);
	}

	// タイトル文字列をリストに保持
	public void setListTitle(int listtype, String title1, String title2) {
		ListArea listarea = getListByType(listtype);
		if (listarea == null) {
			return;
		}

		listarea.setListTitle(title1, title2);
		updateTitle(listtype);
	}

	// 画面を更新する
	public void updateList(int listtype, boolean isUpdate) {
		ListArea listarea = getListByType(listtype);
		if (listarea == null) {
			return;
		}

		listarea.update(isUpdate);
	}

	private void updateTitle(int listtype) {
		ListArea listarea = getListByType(listtype);
		if (listarea == null) {
			return;
		}

		String title1 = listarea.getListTitle1();
		String title2 = listarea.getListTitle2();
		if (listtype == mListType[getIntHigh(mListPosition)]) {
			mTitleArea.setTitle(title1, title2);
		}
	}

	private void updateTitleSortName(int listtype) {
		ListArea listarea = getListByType(listtype);
		if (listarea == null) {
			return;
		}

		int sorttype = listarea.getSortType();
		String name = null;
		boolean way = true;
		Resources res = getResources();
		;
		if (listtype == RecordList.TYPE_FILELIST) {
			switch (sorttype) {
				case 0:
					name = null;
					break;
				case 1:
					name = res.getString(R.string.flSort1);
					break;
				case 2:
					name = res.getString(R.string.flSort2);
					break;
				case 3:
				case 5:
					name = res.getString(R.string.flSort3);
					break;
				case 4:
				case 6:
					name = res.getString(R.string.flSort4);
					break;
			}
			switch (sorttype) {
				case 3:
				case 4:
					way = false;
					break;
			}
		}
		else if (listtype == RecordList.TYPE_SERVER || listtype == RecordList.TYPE_MENU) {
			name = "";
			way = true;
		}
		else {
			switch (sorttype / 2) {
				case 0:
					name = res.getString(R.string.bmSort0);
					break;
				case 1:
					name = res.getString(R.string.bmSort1);
					break;
				case 2:
					name = res.getString(R.string.bmSort2);
					break;
			}
			// 偶数が昇順
			way = (sorttype % 2 == 0);
		}
		if (listtype == mListType[getIntHigh(mListPosition)]) {
			mTitleArea.setSortMode(name, way);
		}
	}

	// タイトル文字列をリストに保持
	private ListArea getListByType(int listtype) {
		if (listtype == RecordList.TYPE_FILELIST) {
			return mFileListArea;
		}
		else if (listtype == RecordList.TYPE_DIRECTORY) {
			return mDirListArea;
		}
		else if (listtype == RecordList.TYPE_SERVER) {
			return mServerListArea;
		}
		else if (listtype == RecordList.TYPE_BOOKMARK) {
			return mFavoListArea;
		}
		else if (listtype == RecordList.TYPE_HISTORY) {
			return mHistListArea;
		}
		else if (listtype == RecordList.TYPE_MENU) {
			return mMenuListArea;
		}
		return null;
	}

	// リストの通知リスナーを設定
	public void setListNoticeListener(ListNoticeListener listener) {
		mListNoticeListener = listener;
		mFileListArea.setListNoticeListener(listener);
		mDirListArea.setListNoticeListener(listener);
		mServerListArea.setListNoticeListener(listener);
		mFavoListArea.setListNoticeListener(listener);
		mHistListArea.setListNoticeListener(listener);
		mMenuListArea.setListNoticeListener(listener);
	}

	// listindexの加減算後の値(範囲考慮)
	private int calcListIndex(int diff, int nowindex) {
		int listindex = nowindex + diff;
		if (listindex < 0) {
			listindex = mListType.length - 1;
		}
		else if (listindex >= mListType.length) {
			listindex = 0;
		}
		return listindex;
	}

	public void setFileList(ArrayList<FileData> filelist, boolean isRefresh) {
		synchronized (getHolder()) {
			mFileListArea.setList(filelist, isRefresh);
		}
	}

	public void removeFileList(FileData fd) {
		synchronized (getHolder()) {
			mFileListArea.remove(fd);
		}
	}

	public void clearFileList() {
		synchronized (getHolder()) {
			mFileListArea.clear();
		}
	}

	public void setRecordList(RecordListArea list, ArrayList<RecordItem> recordlist) {
		synchronized (getHolder()) {
			list.setList(recordlist);
		}
	}

	public void setListMode(short listmode) {
		synchronized (getHolder()) {
			mFileListArea.setListMode(listmode);
		}
	}

	// サムネイル設定
	public void setThumbnail(boolean thumbflag, int sizew, int sizeh, int listThumbH) {
		synchronized (getHolder()) {
			mFileListArea.setThumbnail(thumbflag, sizew, sizeh, listThumbH);
		}
	}

	// 現在の種別を返す
	public int getListType() {
		return mListType[getIntHigh(mListPosition)];
	}

	// リストのインデックスから種別を返す
	public int getListType(int listindex) {
		if (listindex < 0 || listindex >= mListType.length) {
			return -1;
		}
		return mListType[listindex];
	}

	// 描画停止
	public void setDrawEnable(boolean flag) {
		mDrawEnable = flag;
	}

	public RecordItem getRecordItem(int listtype, int index) {
		RecordListArea recordList = (RecordListArea) getListByType(listtype);
		return recordList.getRecordItem(index);
	}

	public ArrayList<RecordItem> getList(int listtype) {
		RecordListArea recordList = (RecordListArea) getListByType(listtype);
		if (recordList == null) {
			return null;
		}
		return recordList.getList();
	}

	public void notifyUpdate() {
		synchronized (getHolder()) {
			for (int i = 0; i < mListType.length; i++) {
				notifyUpdate(mListType[i]);
			}
		}
	}

	public void notifyUpdate(int listtype) {
		synchronized (getHolder()) {
			if (listtype == RecordList.TYPE_FILELIST) {
				mFileListArea.notifyUpdate();
			}
			else {
				RecordListArea recordList = (RecordListArea) getListByType(listtype);
				if (recordList == null) {
					return;
				}
				recordList.notifyUpdate();
			}
		}
	}

	public void moveListUp(boolean isMarker) {
		int listindex = getIntHigh(mListPosition);
		ListArea listArea = getListByType(mListType[listindex]);
		if (mListType[listindex] != RecordList.TYPE_FILELIST) {
			isMarker = false;
		}
		listArea.moveListUp(isMarker);
	}

	public void moveListDown(boolean isMarker) {
		int listindex = getIntHigh(mListPosition);
		ListArea listArea = getListByType(mListType[listindex]);
		if (mListType[listindex] != RecordList.TYPE_FILELIST) {
			isMarker = false;
		}
		listArea.moveListDown(isMarker);
	}

	public void moveCursor(int keycode, boolean isDown) {
		int listindex = getIntHigh(mListPosition);
		ListArea listArea = getListByType(mListType[listindex]);
		listArea.moveCursor(keycode, isDown);
	}

	private long makeLong(int high, int low) {
		long l = ((long) low) & 0x00000000FFFFFFFFL;
		long h = ((long) high) << 32;
		return h | l;
	}

	private int getIntHigh(long pos) {
		return (int) (pos >> 32);
	}

	private int getIntLow(long pos) {
		return (int) (pos & 0xFFFFFFFF);
	}
}
