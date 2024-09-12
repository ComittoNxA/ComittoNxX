package src.comitton.dialog;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import jp.dip.muracoro.comittonx.R;
import src.comitton.view.MenuItemView;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.PaintDrawable;
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

@SuppressLint("NewApi")
public class MenuDialog extends Dialog implements OnTouchListener, OnDismissListener {
	private static int RANGE_CANCEL;

	private MenuSelectListener mListener = null;
	private Activity mActivity;
	private Context mContext;

	private List<MenuList> mMenuList;

	private ScrollView mScrlView;
	private LinearLayout mLinear;

	private int mWidth;
	private float mScale;
	private boolean mSelected;
	private boolean mIsClose;

	public MenuDialog(Activity context, int cx, int cy, boolean isclose, MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, false, false, listener);
	}

	public MenuDialog(Activity context, int cx, int cy, boolean isclose, boolean halfview, MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, halfview, false, listener);
	}

	public MenuDialog(Activity context, int cx, int cy, boolean isclose, boolean halfview, boolean top, MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, halfview, top, listener);
	}

	private void MenuDialogProc(Activity context, int cx, int cy, boolean isclose, boolean halfview, boolean top, MenuSelectListener listener) {
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		PaintDrawable paintDrawable = new PaintDrawable(0);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		// 画面下に表示
		WindowManager.LayoutParams wmlp=dlgWindow.getAttributes();
		wmlp.gravity =(top ? Gravity.TOP : Gravity.CENTER) | (halfview ? Gravity.RIGHT : 0);
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mActivity = context;
		mContext = context.getApplicationContext();
		mMenuList = new ArrayList<MenuList>();
		mScale = mContext.getResources().getDisplayMetrics().scaledDensity;
		RANGE_CANCEL = (int)(20 * getContext().getResources().getDisplayMetrics().scaledDensity);
		if (halfview) {
			mWidth = Math.min(cx, cy) * 20 / 100;
		}
		else {
			mWidth = Math.min(cx, cy) * 95 / 100;
		}
		int maxWidth = (int)(20 * mScale * 16);
		if (mWidth > maxWidth) {
			mWidth = maxWidth;
		}

		mSelected = false;
		mIsClose = isclose;

		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		mScrlView = new ScrollView(mContext);
		mScrlView.setBackgroundColor(0x00000000);

		mLinear = new LinearLayout(mContext);
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
				bakcolor = 0x70000000;
				txtsize = (int)(20 * mScale);
			}
			MenuItemView itemview = new MenuItemView(mContext, type, subtype, text, sub1, sub2, index, id, txtsize, mWidth, txtcolor, bakcolor, curcolor);
			mLinear.addView(itemview);

			if (i != mMenuList.size() - 1) {
				txtcolor = 0x80808080;
				bakcolor = 0x00000000;
				MenuItemView sepview = new MenuItemView(mContext, mWidth, txtcolor, bakcolor);
				mLinear.addView(sepview);
			}
		}
		setContentView(mScrlView);
	}

	// ダイアログを表示してもIMMERSIVEが解除されない方法
	// http://stackoverflow.com/questions/22794049/how-to-maintain-the-immersive-mode-in-dialogs
	/**
	 * An hack used to show the dialogs in Immersive Mode (that is with the NavBar hidden). To
	 * obtain this, the method makes the dialog not focusable before showing it, change the UI
	 * visibility of the window like the owner activity of the dialog and then (after showing it)
	 * makes the dialog focusable again.
	 */
	@Override
	public void show() {
		// Set the dialog to not focusable.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		// 設定をコピー
		copySystemUiVisibility();

		// Show the dialog with NavBar hidden.
		super.show();

		// Set the dialog to focusable again.
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	/**
	 * Copy the visibility of the Activity that has started the dialog {@link mActivity}. If the
	 * activity is in Immersive mode the dialog will be in Immersive mode too and vice versa.
	 */
	@SuppressLint("NewApi")
	private void copySystemUiVisibility() {
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
	        getWindow().getDecorView().setSystemUiVisibility(
	                mActivity.getWindow().getDecorView().getSystemUiVisibility());
	    }
	}

//	// オプションメニューが表示される度に呼び出されます
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		dismiss();
//		return false;
//	}
	@Override
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
		// タッチイベント
		int action = event.getAction();

//		Log.d("onTouch", v.toString() + "," + action + " (" + event.getX() + ", " + event.getY() + "), " + mScrlView.getScrollY());
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
							mStartY = (int)event.getY();						 }
					 }
				}
//				Log.d("scrlPos", "" + mScrlPos);
			}
			else if (action == MotionEvent.ACTION_MOVE) {
//				Log.d("scrlPos", "" + mScrlView.getScrollY());
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
					if (mSelected == false) {
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
//				Log.d("onTouch", v.toString() + ", " + action);
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
