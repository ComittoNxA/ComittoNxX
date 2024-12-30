package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

import net.cachapa.expandablelayout.ExpandableLayout;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.stream.FileListItem;
import src.comitton.view.MenuItemView;
import src.comitton.view.list.PageTextView;

@SuppressLint("NewApi")
public class DirTreeDialog extends ImmersiveDialog implements OnTouchListener, OnDismissListener {

	private LinearLayout mTopLayout;
	private MenuDialog.MenuSelectListener mListener = null;
	private Activity mActivity;
	private Context mContext;

	private ScrollView mScrlView;
	private LinearLayout mLinear;

	private int mWidth;
	private int mHeight;
	private float mScale;
	private boolean mIsClose;

	private static int curcolor = 0x90008000;
	private static int title_txtcolor = 0xFFFFFFFF;
	private static int title_bakcolor = 0xA0000080;
	private int title_txtsize;
	private static int item_txtcolor = 0xFFFFFFFF;
	private static int item_bakcolor = 0x00000000;
	private static int separater_txtcolor = 0xBBFFFFFF;
	private static int separater_bakcolor = 0xBBFFFFFF;
	private int item_txtsize;

	public DirTreeDialog(Activity context, int cx, int cy, boolean isclose, MenuDialog.MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, false, false, false, listener);
	}

	public DirTreeDialog(Activity context, int cx, int cy, boolean isclose, boolean halfview, MenuDialog.MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, halfview, false, false, listener);
	}

	public DirTreeDialog(Activity context, int cx, int cy, boolean isclose, boolean halfview, boolean top, MenuDialog.MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, halfview, top, false, listener);
	}

	public DirTreeDialog(Activity context, int cx, int cy, boolean isclose, boolean halfview, boolean top, boolean wide, MenuDialog.MenuSelectListener listener) {
		super(context);
		MenuDialogProc(context, cx, cy, isclose, halfview, top, wide, listener);
	}

	private void MenuDialogProc(Activity context, int cx, int cy, boolean isclose, boolean halfview, boolean top, boolean wide, MenuDialog.MenuSelectListener listener) {
		boolean debug = false;
		Dialog dialog = this;
		Window dlgWindow = dialog.getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe_transparent);

		// 画面下に表示
		WindowManager.LayoutParams wmlp=dlgWindow.getAttributes();
		wmlp.gravity =(top ? Gravity.TOP : Gravity.CENTER) | (halfview ? Gravity.RIGHT : 0);
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mActivity = context;
		mContext = context.getApplicationContext();
		mScale = mContext.getResources().getDisplayMetrics().scaledDensity;
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

		mScale = mContext.getResources().getDisplayMetrics().scaledDensity;
		title_txtsize = (int)(18 * mScale);
		item_txtsize = (int)(20 * mScale);

		mIsClose = isclose;
		mListener = listener;

		dialog.getWindow().setLayout(mWidth, mHeight);

		ViewGroup.LayoutParams layoutParams;

		mTopLayout = new LinearLayout(mContext);
		mTopLayout.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new ViewGroup.LayoutParams(mWidth, mHeight);
		mTopLayout.setLayoutParams(layoutParams);
		mTopLayout.setBackgroundColor(0x00000000);

		mScrlView = new ScrollView(mContext);
		layoutParams = new ViewGroup.LayoutParams(mWidth, mHeight - mTopLayout.getHeight());
		mScrlView.setLayoutParams(layoutParams);
		mScrlView.setBackgroundColor(0x80000000);

		mLinear = new LinearLayout(mContext);
		mLinear.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		mLinear.setLayoutParams(layoutParams);
		mLinear.setBackgroundColor(0x00000000);

		mScrlView.addView(mLinear);
		mTopLayout.addView(mScrlView);
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(mTopLayout);
	}

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
		// カテゴリ
		MenuItemView itemview = new MenuItemView(mContext, MenuItemView.TYPE_SECTION, MenuItemView.SUBTYPE_STRING, text, null, null, 0, 0, title_txtsize, mWidth, title_txtcolor, title_bakcolor, curcolor);
		mTopLayout.addView(itemview, 0);
	}

	public void addFileList(FileListItem[] fileList) {
		boolean debug = false;

		ArrayList<MyItem> itemList = new ArrayList<MyItem>(0);
		String file = "";
		String prev_dir = "", dir = "";
		for (int i = 0; i < fileList.length; ++i) {
			file = fileList[i].name;
			dir = file.substring(0, file.lastIndexOf('/') + 1);
			if (!dir.equals(prev_dir)) {
				if (debug) {Log.d("DirTreeDialog", "addFileList: 新しいディレクトリです. i=" + i + ", length=" + dir.length() + ", dir=" + dir);}
				int index = 0;
				while ((index = dir.indexOf('/', index + 1)) >= 0) {
					boolean exist = false;
					String sub_dir = dir.substring(0, index);
					for (int j = 0; j < itemList.size(); ++j) {
						if (sub_dir.equals(itemList.get(j).getTitle())) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						if (debug) {Log.d("DirTreeDialog", "addFileList: リストに追加します. i=" + i + ", sub_dir=" + sub_dir);}
						itemList.add(new MyItem(i, dir.substring(0, index)));
					}
				}
			}
			prev_dir = dir;
		}

		String sub_dir = "";
		LinearLayout parentLayout;
		LinearLayout parentExpandItem = null;
		PageTextView parentTitleView = null;
		ImageView parentArrow = null;

		for (int i = 0; i < itemList.size(); ++i) {
			parentLayout = mLinear;
			int page = itemList.get(i).getPage();
			String title = itemList.get(i).getTitle();
			if (debug) {Log.d("DirTreeDialog", "addFileList: レイアウトに追加します. i=" + i + ", page=" + page + ", title=" + title);}
			int pre_index = -1;
			int index = title.indexOf('/');
			if (debug) {Log.d("DirTreeDialog", "addFileList: 文字列を分割します. index=" + index + ", pre_index=" + pre_index + ", length=" + title.length());}
			while (index >= 0) {
				sub_dir = title.substring(pre_index + 1, index);
				for (int j = 0; j < parentLayout.getChildCount(); j++) {
					View v = parentLayout.getChildAt(j);
					if (v instanceof LinearLayout) {
						parentExpandItem = (LinearLayout) v;
						parentTitleView = (PageTextView) parentExpandItem.findViewById(R.id.title);
						if (sub_dir.equals(parentTitleView.getText())) {
							if (debug) {Log.d("DirTreeDialog", "addFileList: 親に設定します. parentTitleView.getText()=" + parentTitleView.getText());}
							parentArrow = (ImageView) parentExpandItem.findViewById(R.id.expand_arrow);
							parentArrow.setImageDrawable(mContext.getDrawable(R.drawable.expand_arrow));
							parentLayout = (LinearLayout) parentExpandItem.findViewById(R.id.child_layout);
						}
					}
				}
				pre_index = index;
				index = title.indexOf('/', pre_index + 1);
				if (debug) {Log.d("DirTreeDialog", "addFileList: 文字列を分割します2. index=" + index + ", pre_index=" + pre_index + ", length=" + title.length());}
			}

			if (parentLayout.equals(mLinear)) {
				if (debug) {Log.d("DirTreeDialog", "addFileList: parentLayout=mLinear, dir=" + dir);}
			}
			else {
				if (debug) {Log.d("DirTreeDialog", "addFileList: parentLayout=" + parentTitleView.getText() + ", dir=" + dir);}
			}

			LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.expand_item, null, false);
			PageTextView titleView = (PageTextView) itemView.findViewById(R.id.title);
			PageTextView pageView = (PageTextView) itemView.findViewById(R.id.page);
			ImageView arrow = (ImageView) itemView.findViewById(R.id.expand_arrow);

			titleView.setText(title.substring(pre_index + 1));
			pageView.setText("P." + (page + 1));
			titleView.setPage(page);
			pageView.setPage(page);
			titleView.setOnTouchListener(this);
			pageView.setOnTouchListener(this);
			arrow.setOnTouchListener(this);

			parentLayout.addView(itemView);
		}

		// トップレベルのフォルダが1個だけなら開いておく
		if (mLinear.getChildCount() == 1) {
			View v = mLinear.getChildAt(0);
			if (v instanceof LinearLayout) {
				parentExpandItem = (LinearLayout) v;
				parentArrow = (ImageView) parentExpandItem.findViewById(R.id.expand_arrow);
				parentArrow.setSelected(true);
				ExpandableLayout expandableLayout = (ExpandableLayout) parentExpandItem.findViewById(R.id.expandable_layout);
				expandableLayout.expand();
			}
		}
	}

	private View mSelectView;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean debug = false;
		// タッチイベント
		int action = event.getAction();

		String eventName[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
				"POINTER_DOWN" , "POINTER_UP" , "HOVER_MOVE" , "SCROLL" , "HOVER_ENTER" ,
				"HOVER_EXIT" , "BUTTON_PRESS" , "BUTTON_RELEASE" };
		if (debug) {Log.d("TabDialogFragment", "onTouch: view=" + v + ", action=" + eventName[action]);}

		if (v instanceof PageTextView) {
			if (action == MotionEvent.ACTION_DOWN) {
				mSelectView = v;
			}
			else if (action == MotionEvent.ACTION_CANCEL) {
				if (mSelectView != null) {
					mSelectView = null;
				}
			}
			else if (action == MotionEvent.ACTION_UP) {
				if (mSelectView != null) {
					mListener.onSelectMenuDialog(((PageTextView)v).getPage() + DEF.MENU_DIR_TREE);
					if (mIsClose) {
						mSelectView = null;
						this.dismiss();
					}
				}
			}
		}
		else if (v instanceof ImageView) {
			if (action == MotionEvent.ACTION_DOWN) {
				mSelectView = v;
			}
			else if (action == MotionEvent.ACTION_CANCEL) {
				if (mSelectView != null) {
					mSelectView = null;
				}
			}
			else if (action == MotionEvent.ACTION_UP) {
				if (mSelectView != null) {
					ExpandableLayout expandableLayout = (ExpandableLayout)((View)v.getParent().getParent()).findViewById(R.id.expandable_layout);
					expandableLayout.toggle();
					v.setSelected(expandableLayout.isExpanded());
					mSelectView = null;
				}
			}
		}
		return true;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// ダイアログ終了
		if (mListener != null) {
			mListener.onCloseMenuDialog();
		}
	}

	protected class MyItem {
		private int mPage;
		private String mTitle;

		public MyItem(int page, String title) {
			mPage = page;
			mTitle = title;
		}

		public void setPage(int page) {
			mPage = page;
		}

		public void setTitle(String title) {
			mTitle = title;
		}

		public int getPage() {
			return mPage;
		}

		public String getTitle() {
			return mTitle;
		}
	}
}
