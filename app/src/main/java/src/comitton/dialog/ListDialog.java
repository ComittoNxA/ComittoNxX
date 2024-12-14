package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.appcompat.app.AlertDialog;

@SuppressLint("NewApi")
public class ListDialog extends Dialog implements OnClickListener, OnItemClickListener, OnDismissListener {
	private ListSelectListener mListener = null;
	private Activity mContext;
	private float mDensity;

	private TextView mTitleText;
	private Button mBtnCancel;
	private ListView mListView;

	private String mTitle;
	private String mItems[];
	private int mSelect;

	private ItemArrayAdapter mItemArrayAdapter;

	public ListDialog(Activity context, String title, String[] items, int select, boolean backcolor, ListSelectListener listener) {
		super(context);
		boolean debug = false;
		if (debug) {Log.d("ListDialog", "ListDialog: 開始します.");}

		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		//PaintDrawable paintDrawable = new PaintDrawable(backcolor ? 0xC0000000 : 0xFF000000);
		//dlgWindow.setBackgroundDrawable(paintDrawable);
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mContext = context;
		mTitle = title;
		mItems = items;
		mSelect = select;
		mListener = listener;
		mDensity = context.getResources().getDisplayMetrics().scaledDensity;

		if (debug) {Log.d("ListDialog", "ListDialog: 終了します.");}
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		boolean debug = false;
		if (debug) {Log.d("ListDialog", "onCreate: 開始します.");}

		setContentView(R.layout.listdialog);

		mTitleText = (TextView)this.findViewById(R.id.text_title);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mListView = (ListView)this.findViewById(R.id.listview);

		mTitleText.setText(mTitle);
		// リストの設定
		mListView.setScrollingCacheEnabled(false);
		mListView.setOnItemClickListener(this);
		mItemArrayAdapter = new ItemArrayAdapter(mContext, -1, mItems);
		mListView.setAdapter(mItemArrayAdapter);

		// デフォルトはしおりを記録する
		mBtnCancel.setOnClickListener(this);
		if (debug) {Log.d("ListDialog", "onCreate: 終了します.");}

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
		boolean debug = false;
		if (debug) {Log.d("ListDialog", "show: 開始します.");}
		// Set the dialog to not focusable.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		// 設定をコピー
		copySystemUiVisibility();

		// Show the dialog with NavBar hidden.
		super.show();

		// Set the dialog to focusable again.
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		if (debug) {Log.d("ListDialog", "show: 終了します.");}
	}

	/**
	 * Copy the visibility of the Activity that has started the dialog {@link mActivity}. If the
	 * activity is in Immersive mode the dialog will be in Immersive mode too and vice versa.
	 */
	@SuppressLint("NewApi")
	private void copySystemUiVisibility() {
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
	        getWindow().getDecorView().setSystemUiVisibility(
	                mContext.getWindow().getDecorView().getSystemUiVisibility());
	    }
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

	public class ItemArrayAdapter extends ArrayAdapter<String>
	{
		private String[] mItems; // ファイル情報リスト

		// コンストラクタ
		public ItemArrayAdapter(Context context, int resId, String[] items)
		{
			super(context, resId, items);
			mItems = items;
		}

		// 一要素のビューの生成
		@Override
		public View getView(int index, View view, ViewGroup parent)
		{
			// レイアウトの生成
			TextView textview;
			if(view == null) {
				Context context = getContext();
				int margin1 = (int)(10 * mDensity);
				int margin2 = (int)(18 * mDensity);
				// レイアウト
				LinearLayout layout = new LinearLayout( context );
				layout.setBackgroundColor(0);
				view = layout;
				// テキスト
				textview = new TextView(context);
				textview.setTag("text");
				textview.setPadding(margin1, margin2, 0, margin2);
				textview.setTextSize(18);
				textview.setGravity(Gravity.CENTER);
				layout.addView(textview);
			}
			else {
				textview = (TextView)view.findViewWithTag("text");
			}

			// 値の指定
			String item = mItems[index];
			textview.setText(item);
			int color = Color.WHITE;
			if (mSelect == index) {
				color = 0xFF80FFFF;
			}
			textview.setTextColor(color);
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		// キャンセルクリック
		dismiss();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// 選択
		mListener.onSelectItem(position);
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}

	public interface ListSelectListener extends EventListener {
	    // メニュー選択された
	    public void onSelectItem(int pos);
	    public void onClose();
	}
}