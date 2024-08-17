package src.comitton.view.list;

import src.comitton.view.list.MomentScroller.ScrollMoveListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import android.graphics.Point;

public class ListArea implements Handler.Callback, ScrollMoveListener {
	protected final int BORDER_WIDTH = 1;
	protected final int BORDER_HEIGHT = 1;
	
	private short STATE_NONE = 0;
	private short STATE_FIRST = 1;
	private short STATE_MOVE = 2;

	private int mRangeCancel;
	private int mRangeScrollerX;
	private int mRangeScrollerY;
	private int mRangeScrollerKnob;
	private int mRangeHeight;
	private float mStartY;
	private float mTouchY;
	private long mTouchTime;
	protected int mTouchIndex;
	protected short mTouchState;
	protected boolean mTouchDraw;

	protected int mCursorPosX;
	protected int mCursorPosY;
	protected boolean mCursorDisp;

	private int mVisibleCounter;
	private long mOperationTime;
	private boolean mIsOperation;

	protected short mListType;
	protected int mListSize;
	protected int mTopRow;
	protected int mTopPos;
	private int mBottomRow;
//	private int mBottomPos;
	private int mDispRange;
	private int mTailRow;
	private int mTailPos;
	private short mRowHeight[];

	protected int mTouchCounter;

	protected int mDrawHeight;
	protected int mRowNum;
	protected int mColumnNum;

	private Message mLongClickMsg;
	private Message mOperationMsg;
	private Handler mHandler;

	private final int TERM_LONGCLICK = 20;
	private final int TERM_OPERATION = 50;
	private final int TERM_VISIBLE = 750;

	private final int HMSG_LONGCLICK = 5001;
	private final int HMSG_OPERATION = 5002;
	private final int HMSG_ITEMCLICK = 5003;

	private final int MAX_VISIBLECOUNTER = 10;

	private ListNoticeListener mListNoticeListener = null;
	private MomentScroller mMomentScroller;

	protected int mAreaWidth;
	protected int mAreaHeight;

	private Paint mFillPaint;
	private Paint mLinePaint;

	protected short mSortType;
	protected String mListTitle1;
	protected String mListTitle2;

	private float mDensity;

	// コンストラクタ
	public ListArea(Context context, int listtype) {
		mDensity = context.getResources().getDisplayMetrics().scaledDensity;
		mRangeCancel = (int) (20 * mDensity);
		mRangeScrollerX = (int) (10 * mDensity);
		mRangeScrollerY = (int) (20 * mDensity);
		mRangeScrollerKnob = (int) (64 * mDensity);
		mTouchIndex = -1;
		mCursorDisp = false;
		mCursorPosX = mCursorPosY = 0;
		mTailRow = -1;
		mListType = (short)listtype; 

		mHandler = new Handler(this);

		mFillPaint = new Paint();
		mFillPaint.setStyle(Style.FILL);
		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(1.5f * mDensity);
		mMomentScroller = new MomentScroller(this, mDensity);
	}

	protected void drawArea(Canvas canvas, int baseX, int baseY) {
		int y = calcScrollPosY();
		int x = baseX + mAreaWidth - mRangeScrollerX;
		int y1 = baseY + mRangeScrollerY;
		int y2 = baseY + mAreaHeight - mRangeScrollerY;

		int color;
		mFillPaint.setColor((mVisibleCounter << 28) + 0x000080FF);
		canvas.drawCircle(x, baseY + y + mRangeScrollerY, mRangeScrollerX / 2, mFillPaint);
		canvas.drawRect(x - 1, y1, x + 2, y2, mFillPaint);

		RectF rc = new RectF(baseX + mAreaWidth - mRangeScrollerY * 2, baseY + y + 1, baseX + mAreaWidth, baseY + y + mRangeScrollerY * 2 - 1);
		if (mVisibleCounter > MAX_VISIBLECOUNTER * 3 / 4) {
    		if (mFirstScroll == true) {
    			// 高速スクロール中
    			//canvas.drawCircle(x, baseY + y + mRangeScrollerX, mRangeScroller, mLinePaint);
    			color = 0x40FF4040;
    		}
    		else {
    			color = 0x404080FF;
    		}
    		mLinePaint.setColor(color);
    		canvas.drawRoundRect(rc, mRangeScrollerY / 4, mRangeScrollerY / 4, mLinePaint);
		}
	}

	public int getTopIndex() {
		// リスト先頭項目を返す
		int toprow = mTopRow; 
		if (mTopPos < 0) {
			toprow ++;
		}
		int index = toprow * mColumnNum;
		if (toprow >= mListSize) {
			index = mListSize - 1;
		}
		return index;
	}

	public int getTopRow() {
		// リスト先頭行を返す
		return mTopRow;
	}

	public int getTopPos() {
		// リスト先頭行の相対位置を返す
		return mTopPos;
	}

	public void setTopIndex(int index, int pos) {
		// リスト先頭項目を設定
		if (index < mListSize) {
			scrollMove(index / mColumnNum, pos);
		}
	}

	public void setTopRowPos(int row, int pos) {
		// リスト先頭項目を設定
		if (row < mRowNum) {
			scrollMove(row, pos);
		}
	}

	public Rect setDrawArea(int x1, int y1, int x2, int y2, int orientation) {
//		clearDrawInfo();

		int cx = x2 - x1;
		int cy = y2 - y1;

		mAreaWidth = cx;
		mAreaHeight = cy;
		mDrawHeight = cy;
		return new Rect(x1, y1, x2, y2);
	}

	private boolean mFirstScroll;
	public int sendTouchEvent(int action, float x, float y) {
		if (mListSize <= 0) {
			return -1;
		}
		int ret = -1;

		// 高速スクロール開始判定
		if (action == MotionEvent.ACTION_DOWN && mVisibleCounter > MAX_VISIBLECOUNTER / 2) {
			int ypos = calcScrollPosY() + mRangeScrollerY;
			if (mAreaWidth - mRangeScrollerKnob <= x && Math.abs(y - ypos) <= mRangeScrollerKnob / 2) {
				mFirstScroll = true;
				doOperation(true);
			}
		}
		if (mFirstScroll == true) {
			// 高速スクロール中
			if (action == MotionEvent.ACTION_MOVE) {
				try {
					int height = mAreaHeight - mRangeScrollerY * 2;
					int ypos = (int) (y - mRangeScrollerY);
					int row, pos;
					if (ypos >= height) {
						row = mTailRow;
						pos = mTailPos;
					} else {
						row = (int) (ypos * mTailRow / mRangeHeight);
						pos = 0;
					}
					scrollMove(row, pos);
				}
				catch (ArithmeticException e){
					// 0で除算すると落ちるため
					// (スクロールする余地のないときにスクロールバーを操作)
					return -1;
				}
			}
			else if (action == MotionEvent.ACTION_UP) {
				mFirstScroll = false;
				doOperation(false);
			}
			// 操作あり
			update(false);
			return -1;
		}
		
		// タッチした位置の項目インデックスを求める
		Point point = new Point(0,0);
		int index = getRowFromPoint((int) x, (int) y, point);

		if (mMomentScroller.sendTouchEvent(action, x, y) != false) {
			return -1;
		}
		// 操作あり
		doOperation(action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL);

		// イベント処理
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				// 押された
				mTouchIndex = index;
				mStartY = y;
				mTouchY = y;
				mTouchCounter = 0;
				mTouchState = STATE_FIRST;
				mTouchDraw = false;
				mTouchTime = SystemClock.uptimeMillis();

				// タッチによりカーソル非表示
				mCursorDisp = false;
//				mCursorPosX = mTouchIndex % mColumnNum;
//				mCursorPosY = mTouchIndex / mColumnNum;

				// ロングクリックのイベント発生用メッセージ
				if (index >= 0) {
					startItemLongClick(index);
				}
				update(false);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// Log.d("scrlPos", "" + mScrlView.getScrollY());
				if (mTouchState == STATE_FIRST) {
    				if (Math.abs(mStartY - y) > mRangeCancel /* || Math.abs(mStartX - x) > mRangeCancel*/) {
    					mTouchIndex = -1;
    					mTouchState = STATE_MOVE;
    					mLongClickMsg = null;
    				}
				}
				if (mTouchState == STATE_MOVE) {
					scrollMove((int) (y - mTouchY));
					mTouchY = y;
				}
				update(false);
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (mTouchIndex != -1 && mLongClickMsg != null) {
					mTouchDraw = true;
					update(true);
					mListNoticeListener.onItemClick(mListType, mTouchIndex, point);
					mTouchIndex = -1;
					mCursorPosX = mTouchIndex % mColumnNum;
					mCursorPosY = mTouchIndex / mColumnNum;
				}
				mLongClickMsg = null;
				break;
			}
		}
		return ret;
	}

	// 高速スクロール時の位置を求める
	private int calcScrollPosY() {
		int height = mAreaHeight - mRangeScrollerY * 2;
		int y;
		if (mTailRow < 0 || (mTailRow <= 0 && mTailPos >= 0)) {
			return 0;
		}
		if (mTopRow == mTailRow && mTopPos == mTailPos) {
			y = height;
		}
		else {
    		float topheight = getRowHeight(mTopRow);
    		float bottomheight = getRowHeight(mTailRow);
    		float range;
    		if (bottomheight > 0) {
    			range = mTailRow + ((float)(mTailPos * -1) / bottomheight);
    		}
    		else {
    			range = mTailRow;
    		}
    		float pos;
    		if (topheight > 0) {
    			pos = mTopRow + ((float)(mTopPos * -1) / topheight);
    		}
    		else {
    			pos = mTopRow;
    		}
    		y = (int)(height * (pos / range));
    		if (y >= height) {
    			y = height;
    		}
		}
		return y;
	}
	
	public void cancelOperation() {
		mTouchState = STATE_NONE;
		mLongClickMsg = null;
		mTouchIndex = -1;
		mOperationMsg = null;
		doOperation(false);
		return;
	}

	public void clearDrawInfo() {
		mTouchIndex = -1;
		mLongClickMsg = null;
		mOperationMsg = null;
		mVisibleCounter = 0;
		mIsOperation = false;
	}
	
	// リストの通知リスナーを設定
	public void setListNoticeListener(ListNoticeListener listener) {
		mListNoticeListener = listener;
	}

	// リスト設定
	public void setListSize(int size, int column, int row, int disprange, boolean isRefresh) {
		int oldTopRow = mTopRow;
		int oldTopPos = mTopPos;
		int oldColnumNum = mColumnNum;

		mListSize = size;
		mColumnNum = column;
		mRowNum = row;
		mDispRange = disprange;

		if (row >= 0) {
			mRowHeight = new short[row];
		}
		mTouchCounter = 0;
		// 指定がおかしい場合は先頭表示
		mTopRow = 0;
		mTopPos = 0;

//		mBottomRow = -1;
		int h = BORDER_HEIGHT * -1;
		mTailRow = 0;
		mTailPos = 0;
		for (int i = mRowNum - 1; i >= 0; i--) {
			int rh = getRowHeight(i);
			h += rh + BORDER_HEIGHT;
			if (h >= mAreaHeight) {
				mTailRow = i;
				mTailPos = mAreaHeight - h;
				// 範囲が4.7項目分だとすると、4.0の位置の高さを設定
				float r = (float)mTailPos / rh * -1; 
				mRangeHeight = (int)((mAreaHeight - mRangeScrollerY * 2) * mTailRow / (mTailRow + r));
				break;
			}
		}

		if (isRefresh == true) {
			if (mColumnNum >= 1) {
				if (oldColnumNum != mColumnNum) {
	    			int idx = oldTopRow * oldColnumNum;	// 前回の先頭インデックス
	    			setTopIndex(idx, 0);
				}
				else {
					scrollMove(oldTopRow, oldTopPos);
				}
			}
		}
		else {
			mCursorDisp = false;
			mCursorPosX = mCursorPosY = 0;
		}

		// 初期状態を通知
		mListNoticeListener.onScrollChanged(mListType, mTopRow * mColumnNum, mTopRow * mColumnNum + mDispRange);
	}

	// 座標からインデックスを求める
	private int getRowFromPoint(int x, int y, Point point) {//アイテム内の座標も返す
		int row = -1;
		int column;
		int posY = mTopPos;
		for (int i = mTopRow; i < mRowNum && posY <= y; i++) {
			int h = getRowHeight(i);
			if (h <= 0) {
				break;
			}
			if (posY <= y && y < posY + h) {
				row = i;
				if(point != null)
					point.y = y- posY;
			}
			posY += h + BORDER_HEIGHT;
		}
		int index = -1;
		column = x * mColumnNum / mAreaWidth;
		if (0 <= column && column < mColumnNum) {
			index = row * mColumnNum + column;
			point.x = x % (mAreaWidth/mColumnNum);
		}

		if (index >= mListSize) {
			index = -1;
		}
		return index;
	}

	@Override
	public boolean onScrollMove(float sx, float sy) {
		// スクロール位置表示
		mOperationTime = SystemClock.uptimeMillis();

		boolean result = false;
		int otr = mTopRow;
		int otp = mTopPos;
		
		// スクロール実行
		scrollMove((int)sy);

		// 少しでもスクロールしたかのチェック
		if (otr != mTopRow || otp != mTopPos) {
			result = true;
		}
		// 表示更新
		update(false);
		return result;
	}

	// スクロール (range<0:下にスクロール, range>0:下にスクロール)
	public void scrollMove(int range) {
		scrollMove(mTopRow, mTopPos + range);
//		int ih;
//
//		while (0 <= row && row < mListSize) {
//			ih = getRowHeight(row);
//			if (pos > 0) {
//				// 前のが見えてる状態
//				row--;
//				ih = getRowHeight(row);
//				pos -= ih + BORDER_HEIGHT;
//			}
//			else if (pos * -1 > ih + BORDER_HEIGHT) {
//				// 隠れててしまってる状態
//				row++;
//				pos += ih + BORDER_HEIGHT;
//			}
//			else {
//				break;
//			}
//		}
//		if (row < 0) {
//			// 先頭よりも前は表示しない
//			row = 0;
//			pos = 0;
//		}
//		else {
//			// スクロール下限チェック
//			if (row > mTailRow) {
//				// 最終項目は隠さない
//				row = mTailRow;
//				pos = mTailPos;
//			}
//			else if (row == mTailRow) {
//				// 最終項目は丸まま表示
//				if (pos < mTailPos) {
//					pos = mTailPos;
//				}
//			}
//		}
//		mTopRow = row;
//		mTopPos = pos;
//		if (oldtop != mTopRow) {
//			// 先頭インデックスの変更通知
//			mListNoticeListener.onScrollChanged(mListType, mTopRow * mColumnNum, mTopRow * mColumnNum + mDispRange);
//		}
	}

	// スクロール (range<0:下にスクロール, range>0:下にスクロール)
	public void scrollMove(int row, int pos) {
		int oldtop = mTopRow;
		int ih;

		while (0 <= row && row < mListSize) {
			ih = getRowHeight(row);
			if (pos > 0) {
				// 前のが見えてる状態
				row--;
				ih = getRowHeight(row);
				pos -= ih + BORDER_HEIGHT;
			}
			else if (pos * -1 > ih + BORDER_HEIGHT) {
				// 隠れててしまってる状態
				row++;
				pos += ih + BORDER_HEIGHT;
			}
			else {
				break;
			}
		}
		if (row < 0) {
			// 先頭よりも前は表示しない
			row = 0;
			pos = 0;
		}
		else {
			// スクロール下限チェック
			if (row > mTailRow) {
				// 最終項目は隠さない
				row = mTailRow;
				pos = mTailPos;
			}
			else if (row == mTailRow) {
				// 最終項目は丸まま表示
				if (pos < mTailPos) {
					pos = mTailPos;
				}
			}
		}
		mTopRow = row;
		mTopPos = pos;
		if (oldtop != mTopRow) {
			// 先頭インデックスの変更通知
			mListNoticeListener.onScrollChanged(mListType, mTopRow * mColumnNum, mTopRow * mColumnNum + mDispRange);
		}
	}

	protected short getRowHeight(int row) {
		// long st = SystemClock.uptimeMillis();
		if (mRowHeight == null || row < 0 || row >= mRowHeight.length) {
			return 0;
		}

		short h = mRowHeight[row];
		if (h <= 0) {
			h = calcRowHeight(row);
			mRowHeight[row] = h;
		}
		// Log.d("getRowHeight", "proctime=" + (SystemClock.uptimeMillis() -
		// st));
		return h;
	}


	protected boolean isMarker(int index) {
		return isMarker(index);
	}

	protected short calcRowHeight(int row) {
		return -1;
	}

	public void doOperation(boolean operation) {
		mIsOperation = operation;
		if (operation == true) {
			mOperationTime = SystemClock.uptimeMillis();
		}
		if (mOperationMsg == null) {
			// 通知
			mOperationMsg = mHandler.obtainMessage(HMSG_OPERATION);
			mHandler.sendMessageAtTime(mOperationMsg, mOperationTime + TERM_OPERATION);
		}
	}

	private void startItemLongClick(int index) {
		long NextTime = SystemClock.uptimeMillis() + TERM_LONGCLICK;
		mLongClickMsg = mHandler.obtainMessage(HMSG_LONGCLICK, index, 0);
		mHandler.sendMessageAtTime(mLongClickMsg, NextTime);
	}
	
	private void startItemClick(int index) {
		Message msg = mHandler.obtainMessage(HMSG_ITEMCLICK, index, 0);
		mHandler.sendMessage(msg);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == HMSG_OPERATION) {
			if (mOperationMsg == msg) {
    			long nowtime = SystemClock.uptimeMillis();
    
    			int vc = mVisibleCounter;
    			if (mIsOperation || nowtime < mOperationTime + TERM_VISIBLE) {
    				mVisibleCounter ++;	
    			}
    			else {
    				mVisibleCounter --;
    			}
    
    			if (mVisibleCounter > MAX_VISIBLECOUNTER) {
    				mVisibleCounter = MAX_VISIBLECOUNTER;
    			}
    			else if (mVisibleCounter < 0) {
    				mVisibleCounter = 0;
    			}
    
    			if (vc != mVisibleCounter) {
    				update(false);
    			}
    
    			// 稼働中のみ次のイベント登録
    			if (mVisibleCounter > 0) {
    				mOperationMsg = mHandler.obtainMessage(HMSG_OPERATION);
    				mHandler.sendMessageAtTime(mOperationMsg, nowtime + TERM_OPERATION);
    			}
    			else {
    				mOperationMsg = null;
    			}
			}
		}
		else if (msg.what == HMSG_ITEMCLICK) {
			// ワンテンポ遅らせないとなんか落ちる
			mListNoticeListener.onItemClick(mListType, msg.arg1, null);
		}
		else if (msg.what == HMSG_LONGCLICK) {
			// ロングタップ
			if (mLongClickMsg == msg) {
				if (mTouchCounter < 32) {
					if (mTouchTime < SystemClock.uptimeMillis() - 200) {
						mTouchDraw = true;
						mTouchCounter++;
					}

					// 次のメッセージ
					long NextTime = SystemClock.uptimeMillis() + TERM_LONGCLICK;
					mLongClickMsg = mHandler.obtainMessage(HMSG_LONGCLICK, msg.arg1, msg.arg2);
					mHandler.sendMessageAtTime(mLongClickMsg, NextTime);
				}
				else {
					mLongClickMsg = null;
					mTouchIndex = -1;
					mTouchDraw = false;
					mTouchCounter = 0;
					mListNoticeListener.onItemLongClick(mListType, msg.arg1);
				}
				update(false);
			}
		}
		return false;
	}

	protected void update(boolean isUpdate) {
		;
	}

	public void setSortType(int sorttype) {
		mSortType = (short)sorttype;
	}

	protected short getSortType() {
		return mSortType;
	}

	public void setListTitle(String title1, String title2) {
		mListTitle1 = title1;
		mListTitle2 = title2;
	}

	protected String getListTitle1() {
		return mListTitle1;
	}

	protected String getListTitle2() {
		return mListTitle2;
	}

	public void moveListUp(boolean isMarker) {
		mCursorDisp = false;
		// 先頭を求める
		int currentTop = mTopRow;
		if (mTopPos < 0) {
			currentTop ++;
		}
		currentTop --;
		if (currentTop < 0) {
			return;
		}

		if (isMarker == true) {
			boolean marker = false;
			for (int i = currentTop ; i >= 0 && marker == false ; i --) {
    			for (int j = 0 ; j < mColumnNum ; j ++) {
    				marker = isMarker(i * mColumnNum + j);
    				if (marker) {
    					currentTop = i;
    					break;
    				}
    			}
			}
			if (marker == false) {
				return;
			}
		}

		// 見つけた
		scrollMove(currentTop, 0);
		update(false);
	}

	public void moveListDown(boolean isMarker) {
		mCursorDisp = false;
		updateBottomRowPos();

		if (mBottomRow >= mRowNum - 1) {
			return;
		}
		// 次の行を求める
		int nextrow = mBottomRow + 1; 
		int h = getRowHeight(nextrow);
		if (isMarker == true) {
			// 検索あり
			boolean marker = false;
			for (int i = nextrow ; i < mRowNum && marker == false ; i ++) {
    			for (int j = 0 ; j < mColumnNum ; j ++) {
    				marker = isMarker(i * mColumnNum + j);
    				if (marker) {
    					nextrow = i;
    					break;
    				}
    			}
			}
			if (marker == false) {
				return;
			}
			// 見つけた
    		h = getRowHeight(nextrow);
		}
		scrollMove(nextrow, mAreaHeight - h);
		update(false);
	}

	// キー入力でのリスト選択処理
	public void moveCursor(int keycode, boolean isDown) {
		doOperation(true);
		updateBottomRowPos();

		if (mCursorDisp == true && keycode == KeyEvent.KEYCODE_ENTER) {
			// 改行はカーソル移動とは分けて処理
			int index = mCursorPosY * mColumnNum + mCursorPosX; 
			if (isDown) {
				// 改行DOWN
				if (mLongClickMsg == null) {
					// 長押しメニュータイマー開始
					startItemLongClick(index);
				}
    		}
    		else {
				// 改行UP
    			if (mLongClickMsg != null) {
    				// 長押しメニュー未表示
    				mLongClickMsg = null;
    				startItemClick(index);
    			}
    		}
			return;
		}

		
		// カーソル移動
		int x = mCursorPosX;
		int y = mCursorPosY;
		int top = mTopRow;
		if (mTopRow < mRowNum - 1 && mTopPos < 0) {
			// 少しでも隠れていたら次の項目
			top  ++;
		}

		if (mCursorDisp == false) {
			// カーソル非表示中は表示するだけで移動しない
    		if (y < top) {
    			y = top;
    		}
    		if (y > mBottomRow) {
    			y = mBottomRow;
    		}
		}
		else {
    		switch (keycode) {
    			case KeyEvent.KEYCODE_DPAD_DOWN:
    				y ++;
    				break;
    			case KeyEvent.KEYCODE_DPAD_UP:
    				y --;
    				break;
    			case KeyEvent.KEYCODE_DPAD_RIGHT:
    				x ++;
    				break;
    			case KeyEvent.KEYCODE_DPAD_LEFT:
    				x --;
    				break;
				case KeyEvent.KEYCODE_MOVE_HOME:
					y = 0;
					break;
				case KeyEvent.KEYCODE_MOVE_END:
					y = mRowNum - 1;
					break;
    		}

    		if (y < 0) {
    			y = 0;
    		}
    		else if (y >= mRowNum) {
    			y = mRowNum - 1;
    		}
    		
			if (x >= mColumnNum) {
    			if (y < mRowNum - 1) {
    				y ++;
    				x = 0;
    			}
    			else {
    				x = mColumnNum - 1;
    			}
    		}
    		else if (x < 0) {
    			if (y > 0) {
    				y --;
    				x = mColumnNum - 1;
    			}
    			else {
    				x = 0;
    			}
    		}

			// リスト数を超えた場合
			if (y * mColumnNum + x >= mListSize) {
				y = mRowNum - 1;
				x = (mListSize - 1) % mColumnNum;
			}
		}
		
		if (y < top) {
			setTopRowPos(y, (int)(20 * mDensity));
		}
		if (y > mBottomRow) {
			int h = getRowHeight(y); 
			setTopRowPos(y, mAreaHeight - h - 20);
		}

		mCursorPosY = y;
		mCursorPosX = x;

		// カーソル表示
		mCursorDisp = true;
		update(false);
	}

	private void updateBottomRowPos() {
		// 画面表示の再下段のインデックスを求める
		int row = 0;
		int pos = 0; 
		int totalh = mTopPos;
		for (int i = mTopRow; i < mRowNum && totalh <= mAreaHeight; i++) {
			int h = getRowHeight(i);
			if (h <= 0) {
				break;
			}
			if (totalh + h > mAreaHeight) {
				break;
			}
			row = i;
			pos = totalh;
			totalh += h + BORDER_HEIGHT;
		}
		mBottomRow = row;
//		mBottomPos = pos;
	}

	protected String[] getMultiLine(String str, int cx, int maxline, Paint text) {
		float result[] = new float[str.length()];
		text.getTextWidths(str, result);

		int i;
		int lastpos = 0;
		int line = 0;
		float sum = 0.0f;
		int pos[] = { 0, 0, 0 };

		float dotwidth = text.measureText("...");

		// 1行に入る文字長をしらべる
		for (i = 0; i < result.length; i++) {
			if (line == maxline - 1 && sum + result[i] > cx - dotwidth) {
				if (lastpos == 0) {
					lastpos = i;
				}
			}
			if (sum + result[i] > cx) {
				sum = result[i];
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
			if (pos[i] == 0) {
				strSep[i] = str.substring(st);

			}
			else {
				strSep[i] = str.substring(st, pos[i]);
				if (i == maxline - 1) {
					strSep[i] += "...";
				}
			}
			st = pos[i];
		}
		return strSep;
	}

	public void run() {
	}
}
