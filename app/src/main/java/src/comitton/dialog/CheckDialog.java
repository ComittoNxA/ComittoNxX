package src.comitton.dialog;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NewApi")
public class CheckDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {
	private CheckListener mListener = null;

	private TextView mTitleText;
	private Button mBtnOk;
	private Button mBtnCancel;
	private ListView mListView;
	private LinearLayout mFooter;

	private String mTitle;
	private boolean mStates[];
	private String mItems[];

	private ItemArrayAdapter mItemArrayAdapter;

	public CheckDialog(AppCompatActivity activity, @StyleRes int themeResId, String title, boolean states[], String[] items, CheckListener listener) {
		super(activity, themeResId);

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mTitle = title;
		mStates = states;
		mItems = items;
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.checkdialog);
		mTitleText = (TextView)this.findViewById(R.id.text_title);
		mBtnOk  = (Button)this.findViewById(R.id.btn_ok);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mListView = (ListView)this.findViewById(R.id.listview);
		mFooter = (LinearLayout)this.findViewById(R.id.footer);

		mTitleText.setText(mTitle);
		// リストの設定
		mListView.setScrollingCacheEnabled(false);
		mItemArrayAdapter = new ItemArrayAdapter(mActivity, -1, mItems);
		mListView.setAdapter(mItemArrayAdapter);

		// デフォルトはしおりを記録する
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// スクロールビューの最大サイズを設定する
		// 最大サイズ以下ならそのまま表示する
		int maxheight = mHeight - mTitleText.getHeight() - mFooter.getHeight();
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
			CheckBox checkbox;
			if(view == null) {
				Context context = getContext();
				int marginW = (int)(4 * mScale);
				int marginH = (int)(0 * mScale);
				// レイアウト
				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				layout.setBackgroundColor(0);
				view = layout;

				LinearLayout inLayout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				layout.setBackgroundColor(0);

				checkbox = new CheckBox(context);
				checkbox.setId(0);
				checkbox.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				inLayout.addView(checkbox);
				inLayout.setPadding(marginW, marginH, 0, marginH);
				layout.addView(inLayout);

				checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton view, boolean state) {
						Integer index = (Integer)view.getTag();
						if (index != null) {
    						if (0 <= index && index < mStates.length) {
    							mStates[index] = state;
    						}
						}
					}
				});
			}
			else {
				checkbox = (CheckBox)view.findViewById(0);
			}

			// 値の指定
			checkbox.setTag(index);
			checkbox.setChecked(mStates[index]);
			checkbox.setText(mItems[index]);
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		// キャンセルクリック
		if (v.getId() == R.id.btn_ok) {
			mListener.onSelected(mStates);
		}
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}

	public interface CheckListener extends EventListener {
	    // メニュー選択された
	    public void onSelected(boolean states[]);
	    public void onClose();
	}
}