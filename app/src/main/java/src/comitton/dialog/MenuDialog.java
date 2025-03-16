package src.comitton.dialog;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;
import src.comitton.fileview.view.MenuItemView;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NewApi")
public class MenuDialog extends ImmersiveDialog implements OnTouchListener, OnDismissListener {
	private static int RANGE_CANCEL;

	private MenuSelectListener mListener = null;
	private Activity mActivity;

	private List<MenuList> mMenuList;

	private ScrollView mScrlView;
	private LinearLayout mLinear;

	private int mWidth;
	private int mHeight;
	private float mScale;
	private boolean mSelected;
	private boolean mIsClose;

	public MenuDialog(AppCompatActivity activity, @StyleRes int themeResId, boolean isclose, MenuSelectListener listener) {
		super(activity, themeResId);
		MenuDialogProc(activity, isclose, false, false, false, listener);
	}

	public MenuDialog(AppCompatActivity activity, @StyleRes int themeResId, boolean isclose, boolean halfview, MenuSelectListener listener) {
		super(activity, themeResId);
		MenuDialogProc(activity, isclose, halfview, false, false, listener);
	}

	public MenuDialog(AppCompatActivity activity, @StyleRes int themeResId, boolean isclose, boolean halfview, boolean top, MenuSelectListener listener) {
		super(activity, themeResId);
		MenuDialogProc(activity, isclose, halfview, top, false, listener);
	}

	public MenuDialog(AppCompatActivity activity, @StyleRes int themeResId, boolean isclose, boolean halfview, boolean top, boolean wide, MenuSelectListener listener) {
		super(activity, themeResId);
		MenuDialogProc(activity, isclose, halfview, top, wide, listener);
	}

	private void MenuDialogProc(Activity activity, boolean isclose, boolean halfview, boolean top, boolean wide, MenuSelectListener listener) {
		boolean debug = false;
		Window dlgWindow = getWindow();

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe_transparent);

		// 表示位置を決定
		WindowManager.LayoutParams wmlp=dlgWindow.getAttributes();
		wmlp.gravity =(top ? Gravity.TOP : Gravity.CENTER) | (halfview ? Gravity.RIGHT : 0);
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mActivity = activity;
		mMenuList = new ArrayList<MenuList>();
		mScale = mActivity.getResources().getDisplayMetrics().scaledDensity;
		RANGE_CANCEL = (int)(20 * getContext().getResources().getDisplayMetrics().scaledDensity);

		Rect size = new Rect();
		mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(size);
		int cx = size.width();
		int cy = size.height();

		if (halfview) {
			mWidth = Math.min(cx, cy) * 20 / 100;
		}
		else {
			mWidth = Math.min(cx, cy) * 80 / 100;
		}
		int maxWidth = (int)(20 * mScale * 16);
		if (!wide) {
			mWidth = Math.min(mWidth, maxWidth);
		}
		mHeight = cy * 80 / 100;

		mSelected = false;
		mIsClose = isclose;

		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		mScrlView = new ScrollView(mActivity);
		mScrlView.setBackgroundColor(0x00000000);

		mLinear = new LinearLayout(mActivity);
		mLinear.setOrientation(LinearLayout.VERTICAL);
		mLinear.setBackgroundColor(0x00000000);

		mScrlView.addView(mLinear);
		mScrlView.setOnTouchListener(this);

		for (int i = 0 ; i < mMenuList.size() ; i ++) {
			MenuList ml = mMenuList.get(i);
			int type = ml.getType();
			int subtype = ml.getSubType();
			String text = ml.getText();
			String sub1 = ml.getSubText1();
			String sub2 = ml.getSubText2();
			int id = ml.getId();
			int index = ml.getSelect();
			int txtcolor, bakcolor, curcolor;
			int txtsize;
			curcolor = 0x90008000;
			if (type == MenuItemView.TYPE_SECTION) {
				// カテゴリ
				txtcolor = 0xFFFFFFFF;
				bakcolor = 0xA0000080;
				txtsize =  (int)(18 * mScale);
			}
			else {
				txtcolor = 0xFFFFFFFF;
				bakcolor = 0x80000000;
				txtsize = (int)(20 * mScale);
			}
			MenuItemView itemview = new MenuItemView(mActivity, type, subtype, text, sub1, sub2, index, id, txtsize, mWidth, txtcolor, bakcolor, curcolor);
			mLinear.addView(itemview);

			if (i != mMenuList.size() - 1) {
				txtcolor = 0x80808080;
				bakcolor = 0x00000000;
				MenuItemView sepview = new MenuItemView(mActivity, mWidth, txtcolor, bakcolor);
				mLinear.addView(sepview);
			}
		}

		setContentView(mScrlView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// スクロールビューの最大サイズを設定する
		// 最大サイズ以下ならそのまま表示する
		mScrlView.getLayoutParams().width = mWidth;
		mScrlView.getLayoutParams().height = Math.min(mHeight, mLinear.getHeight());
		mScrlView.requestLayout();

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MENU:
					dismiss();
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	public void addSection(String text) {
		mMenuList.add(new MenuList(MenuItemView.TYPE_SECTION, text, 0));
	}

	public void addItem(int id, String text) {
		mMenuList.add(new MenuList(MenuItemView.TYPE_ITEM, text, id));
	}

	public void addItem(int id, String text, String sub1) {
		mMenuList.add(new MenuList(MenuItemView.TYPE_ITEM, text, id, sub1));
	}

	public void addItem(int id, String text, boolean flag) {
		mMenuList.add(new MenuList(MenuItemView.TYPE_ITEM, text, id, flag));
	}

	public void addItem(int id, String text, String sub1, String sub2, int index) {
		mMenuList.add(new MenuList(MenuItemView.TYPE_ITEM, text, id, sub1, sub2, index));
	}

	private int mScrlPos;
	private int mStartX, mStartY;
	private MenuItemView mSelectView;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int logLevel = Logcat.LOG_LEVEL_WARN;

		// タッチイベント
		int action = event.getAction();

//		Logcat.d(logLevel, v.toString() + "," + action + " (" + event.getX() + ", " + event.getY() + "), " + mScrlView.getScrollY());
		if (mScrlView == v) {
			if (action == MotionEvent.ACTION_DOWN) {
				mScrlPos = mScrlView.getScrollY();
				int y = mScrlPos + (int)event.getY();

				for (int i = 0 ; i < mLinear.getChildCount() ; i ++) {
					 MenuItemView menuView = (MenuItemView)mLinear.getChildAt(i);
					 if (menuView != null && menuView.getType() == MenuItemView.TYPE_ITEM) {
						 if (y >= menuView.getTop() && y <= menuView.getBottom()) {
							mSelectView = menuView;
							mSelectView.setSelect(true);
							mStartX = (int)event.getX();
							mStartY = (int)event.getY();
						 }
					 }
				}
//				Logcat.d(logLevel, "" + mScrlPos);
			}
			else if (action == MotionEvent.ACTION_MOVE) {
//				Logcat.d(logLevel, "" + mScrlView.getScrollY());
				int scrlY = (int)mScrlView.getScrollY();
				int x = (int)event.getX();
				int y = (int)event.getY();
				if ((Math.abs(mScrlPos - scrlY) > RANGE_CANCEL || Math.abs(mStartX - x) > RANGE_CANCEL || Math.abs(mStartY - y) > RANGE_CANCEL) && mSelectView != null) {
					mSelectView.setSelect(false);
					mSelectView = null;
				}
			}
			else if (action == MotionEvent.ACTION_UP) {
				if (mSelectView != null) {
					if (!mSelected) {
						mListener.onSelectMenuDialog(mSelectView.getMenuId());
					}
					mSelectView.setSelect(false);
					if (mIsClose) {
						mSelectView = null;
						mSelected = true;
						this.dismiss();
					}
				}
			}
			return false;
		}
		else if (mLinear != v) {
			if (action == MotionEvent.ACTION_DOWN) {
//				Logcat.d(logLevel, v.toString() + ", " + action);
				MenuItemView itemview  = (MenuItemView)v;
				if (itemview.getType() == MenuItemView.TYPE_ITEM) {
					mSelectView = itemview;
					mSelectView.setSelect(true);
					mStartX = mSelectView.getLeft() + (int)event.getX();
					mStartY = mSelectView.getTop() + (int)event.getY();
					mScrlPos = mScrlView.getScrollY();
				}
				else {
					mSelectView = null;
				}
			}
			else if (action == MotionEvent.ACTION_UP) {
				mSelectView.setSelect(false);
				mSelectView.getId();
				mSelectView = null;
			}
			return false;
		}
		return false;
	}

	private class MenuList {
		private int mType;
		private int mSubType;
		private String mText;
		private String mSubText1;
		private String mSubText2;
		private int mId;
		private int mSelect;

		public MenuList(int type, String text, int id) {
			mType = type;
			mSubType = MenuItemView.SUBTYPE_STRING;
			mText = text;
			mSubText1 = null;
			mSubText2 = null;
			mId = id;
		}

		public MenuList(int type, String text, int id, String sub1) {
			mType = type;
			mSubType = MenuItemView.SUBTYPE_STRING;
			mSubText1 = sub1;
			mSubText2 = null;
			mText = text;
			mId = id;
		}

		public MenuList(int type, String text, int id, boolean flag) {
			mType = type;
			mSubType = MenuItemView.SUBTYPE_CHECK;
			mText = text;
			mId = id;
			mSelect = flag ? 1 : 0;
		}

		public MenuList(int type, String text, int id, String sub1, String sub2, int index) {
			mType = type;
			mSubType = MenuItemView.SUBTYPE_RADIO;
			mText = text;
			mId = id;
			mSubText1 = sub1;
			mSubText2 = sub2;
			mSelect = index;
		}

		public int getType() {
			return mType;
		}

		public int getSubType() {
			return mSubType;
		}

		public String getText() {
			return mText;
		}

		public String getSubText1() {
			return mSubText1;
		}

		public String getSubText2() {
			return mSubText2;
		}

		public int getId() {
			return mId;
		}

		public int getSelect() {
			return mSelect;
		}
	}

	public interface MenuSelectListener extends EventListener {
	    // メニュー選択された
	    public void onSelectMenuDialog(int menuId);
	    public void onCloseMenuDialog();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// ダイアログ終了
		if (mListener != null) {
			mListener.onCloseMenuDialog();
		}
	}
}
