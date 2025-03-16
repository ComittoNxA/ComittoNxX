package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.Logcat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
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

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NewApi")
public class ListDialog extends ImmersiveDialog implements OnClickListener, OnItemClickListener, OnDismissListener {
	private ListSelectListener mListener = null;
	private Activity mActivity;

	private TextView mTitleText;
	private Button mBtnCancel;
	private ListView mListView;

	private final String mTitle;
	private final String[] mItems;
	private final int mSelect;

	private ItemArrayAdapter mItemArrayAdapter;

	public ListDialog(AppCompatActivity activity, @StyleRes int themeResId, String title, String[] items, int select, ListSelectListener listener) {
		super(activity, themeResId);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mActivity = activity;
		mTitle = title;
		mItems = items;
		mSelect = select;
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します.");

		setContentView(R.layout.listdialog);

		mTitleText = (TextView)this.findViewById(R.id.text_title);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mListView = (ListView)this.findViewById(R.id.listview);

		mTitleText.setText(mTitle);
		// リストの設定
		mListView.setScrollingCacheEnabled(false);
		mListView.setOnItemClickListener(this);
		mItemArrayAdapter = new ItemArrayAdapter(mActivity, -1, mItems);
		mListView.setAdapter(mItemArrayAdapter);

		// デフォルトはしおりを記録する
		mBtnCancel.setOnClickListener(this);
		Logcat.d(logLevel, "終了します.");

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// スクロールビューの最大サイズを設定する
		// 最大サイズ以下ならそのまま表示する
		int maxheight = mHeight - mTitleText.getHeight() - mBtnCancel.getHeight();
		mListView.getLayoutParams().width = mWidth;
		mListView.getLayoutParams().height = Math.min(mListView.getHeight(), maxheight);
		mListView.requestLayout();
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
		private final String[] mItems; // ファイル情報リスト

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
				int margin1 = (int)(10 * mScale);
				int margin2 = (int)(18 * mScale);
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
			int backcolor = Color.TRANSPARENT;
			if (mSelect == index) {
				color = 0xFF80FFFF;
				//backcolor = 0x90008000;
				textview.setSelected(true);
			}
			textview.setTextColor(color);
			textview.setBackgroundColor(backcolor);
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