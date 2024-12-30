package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

@SuppressLint("NewApi")
public class PageSelectCheckDialog extends ImmersiveDialog implements OnClickListener {
	private Activity mActivity;

	private TextView mTitleText;
	private Button mBtnOk;
	private Button mBtnCancel;
	private ListView mListView;
	private LinearLayout mFooter;

	private String mTitle;
	private boolean mStates[];
	private String mItems[];

	private int mWidth;
	private int mHeight;
	private float mScale;

	private ItemArrayAdapter mItemArrayAdapter;

	private static final int COMMAND_DRAWABLE[] =
			{
					R.drawable.arrow_left_to_line,
					R.drawable.arrow_left_100,
					R.drawable.arrow_left_10,
					R.drawable.arrow_left,
					R.drawable.arrow_right,
					R.drawable.arrow_right_10,
					R.drawable.arrow_right_100,
					R.drawable.arrow_right_to_line,
					R.drawable.reset,
					R.drawable.book_arrow_left,
					R.drawable.book_arrow_right,
					R.drawable.book_arrow_left_bookmark,
					R.drawable.book_arrow_right_bookmark,
					R.drawable.thumb_slider,
					R.drawable.directory_tree,
					R.drawable.table_of_contents,
					R.drawable.list_favorite,
					R.drawable.add_favorite,
					R.drawable.toolbar_search,
					R.drawable.share,
					R.drawable.rotate,
					R.drawable.rotate_image,
					R.drawable.select_thumb,
					R.drawable.trimming_thumb,
					R.drawable.control,
					R.drawable.navi_menu,
					R.drawable.config,
					R.drawable.pen,
			};

	public static final int COMMAND_ID[] =
		{
			DEF.TOOLBAR_LEFTMOST,
			DEF.TOOLBAR_LEFT100,
			DEF.TOOLBAR_LEFT10,
			DEF.TOOLBAR_LEFT1,
			DEF.TOOLBAR_RIGHT1,
			DEF.TOOLBAR_RIGHT10,
			DEF.TOOLBAR_RIGHT100,
			DEF.TOOLBAR_RIGHTMOST,
			DEF.TOOLBAR_PAGE_RESET,
			DEF.TOOLBAR_BOOK_LEFT,
			DEF.TOOLBAR_BOOK_RIGHT,
			DEF.TOOLBAR_BOOKMARK_LEFT,
			DEF.TOOLBAR_BOOKMARK_RIGHT,
			DEF.TOOLBAR_THUMB_SLIDER,
			DEF.TOOLBAR_DIR_TREE,
			DEF.TOOLBAR_TOC,
			DEF.TOOLBAR_FAVORITE,
			DEF.TOOLBAR_ADD_FAVORITE,
			DEF.TOOLBAR_SEARCH,
			DEF.TOOLBAR_SHARE,
			DEF.TOOLBAR_ROTATE,
			DEF.TOOLBAR_ROTATE_IMAGE,
			DEF.TOOLBAR_SELECT_THUMB,
			DEF.TOOLBAR_TRIM_THUMB,
			DEF.TOOLBAR_CONTROL,
			DEF.TOOLBAR_MENU,
			DEF.TOOLBAR_CONFIG,
			DEF.TOOLBAR_EDIT_TOOLBAR,
		};

	private static final boolean DEFAULT_VALUES[] =
		{
			true,		// 一番左のページ
			false,		// 左へ100ページ
			false,			// 左へ10ページ
			true,			// 左へ1ページ
			true,			// 右へ1ページ
			false,		// 右へ10ページ
			false,		// 右へ100ページ
			true,		// 一番右のページ
			true,		// ページ選択をリセット
			false,		// 前のファイル(最終ページ)/次のファイル(先頭ページ)
			false,		// 次のファイル(先頭ページ)/前のファイル(最終ページ)
			true,	// 前(次)のファイル(しおり位置)
			true,	// 次(前)のファイル(しおり位置)
			false,	// サムネイル/スライダー切り替え(イメージビュワーのみ)
			false,		// サブディレクトリ選択(イメージビュワーのみ)
			false,			// 見出し選択(テキストビュワーのみ)
			false,		// ブックマーク選択
			false,	// ブックマーク追加
			false,			// 検索文字列設定(テキストビュワーのみ)
			false,			// 共有(イメージビュワーのみ)
			false,			// 画面方向切り替え(イメージビュワーのみ)
			false,	// 画像の回転(イメージビュワーのみ)
			false,		// サムネイルに設定(イメージビュワーのみ)
			false,		// 範囲選択してサムネイルに設定(イメージビュワーのみ)
			false,		// 画像/テキスト表示設定
			true,			// メニューを開く
			false,			// 設定画面を開く
			true,			// ツールバーを編集
		};

	private static final int COMMAND_RES[] =
			{
					R.string.ToolbarLeftmost,		// 一番左のページ
					R.string.ToolbarLeft100,		// 左へ100ページ
					R.string.ToolbarLeft10,			// 左へ10ページ
					R.string.ToolbarLeft1,			// 左へ1ページ
					R.string.ToolbarRight1,			// 右へ1ページ
					R.string.ToolbarRight10,		// 右へ10ページ
					R.string.ToolbarRight100,		// 右へ100ページ
					R.string.ToolbarRightMost,		// 一番右のページ
					R.string.ToolbarPageReset,		// ページ選択をリセット
					R.string.ToolbarBookLeft,		// 前のファイル(最終ページ)/次のファイル(先頭ページ)
					R.string.ToolbarBookRight,		// 次のファイル(先頭ページ)/前のファイル(最終ページ)
					R.string.ToolbarBookmarkLeft,	// 前(次)のファイル(しおり位置)
					R.string.ToolbarBookmarkRight,	// 次(前)のファイル(しおり位置)
					R.string.ToolbarThumbSlider,	// サムネイル/スライダー切り替え(イメージビュワーのみ)
					R.string.ToolbarDirTree,		// サブディレクトリ選択(イメージビュワーのみ)
					R.string.ToolbarTOC,			// 見出し選択(テキストビュワーのみ)
					R.string.ToolbarFavorite,		// ブックマーク選択
					R.string.ToolbarAddFavorite,	// ブックマーク追加
					R.string.ToolbarSearch,			// 検索文字列設定(テキストビュワーのみ)
					R.string.ToolbarShare,			// 共有(イメージビュワーのみ)
					R.string.ToolbarRotate,			// 画面方向切り替え(イメージビュワーのみ)
					R.string.ToolbarRotateImage,	// 画像の回転(イメージビュワーのみ)
					R.string.ToolbarSelectThum,		// サムネイルに設定(イメージビュワーのみ)
					R.string.ToolbarTrimThumb,		// 範囲選択してサムネイルに設定(イメージビュワーのみ)
					R.string.ToolbarControl,		// 画像/テキスト表示設定
					R.string.ToolbarMenu,			// メニューを開く
					R.string.ToolbarConfig,			// 設定画面を開く
					R.string.ToolbarEditToolbar			// ツールバーを編集
			};

	public PageSelectCheckDialog(Activity activity, int cx, int cy) {
		super(activity);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

		setCanceledOnTouchOutside(true);

		mActivity = activity;

		mTitle = activity.getString(R.string.ToolbarEditTitle);
		mStates = loadToolbarState(mActivity);

		String[] items = null;
		items = new String[COMMAND_RES.length];
		for (int i = 0; i < COMMAND_RES.length; i++) {
			items[i] = activity.getResources().getString(COMMAND_RES[i]);
		}
		mItems = items;
		mScale = activity.getResources().getDisplayMetrics().scaledDensity;
		mScale = mActivity.getResources().getDisplayMetrics().scaledDensity;
		mWidth = Math.min(cx, cy) * 80 / 100;
		int maxWidth = (int)(20 * mScale * 16);
		mWidth = Math.min(mWidth, maxWidth);
		mHeight = cy * 80 / 100;
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
		ViewGroup.LayoutParams layoutParams = mListView.getLayoutParams();
		layoutParams.width = mWidth;
		layoutParams.height = mHeight - mTitleText.getHeight() - mFooter.getHeight();
		mListView.setLayoutParams(layoutParams);
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

	// 設定を読み込み
	public static boolean[] loadToolbarState(Context context) {
		boolean debug = false;
		if (debug) {Log.d("PageSelectCheckDialog", "loadToolbarState: 開始します.");}

		boolean states[] = null;
		try {
			Resources res = context.getResources();
			SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);

			if (debug) {Log.d("PageSelectCheckDialog", "loadToolbarState: 保存された設定を取得します.");}
			states = new boolean[COMMAND_ID.length];
			int count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					states[i] = sharedPreference.getBoolean(DEF.KEY_PAGE_SELECT_TOOLBAR + COMMAND_ID[i], DEFAULT_VALUES[i]);
					if (states[i] == true) {
						// 表示する個数
						count++;
					}
				}
				catch (Exception e) {
					Log.e("PageSelectCheckDialog", "loadToolbarState: ループ1でエラーが発生しました.");
					if (e != null && e.getMessage() != null) {
						Log.e("PageSelectCheckDialog", "loadToolbarState: エラーメッセージ. " + e.getMessage());
					}
					Log.e("PageSelectCheckDialog", "loadToolbarState: i=" + i);
					Log.e("PageSelectCheckDialog", "loadToolbarState: COMMAND_ID[i]=" + COMMAND_ID[i]);
				}
			}

			if (debug) {Log.d("PageSelectCheckDialog", "loadToolbarState: 表示するコマンドを設定します.");}
			int commandId[] = new int[count];
			String commandStr[] = new String[count];
			count = 0;
			for (int i = 0; i < states.length; i++) {
				try {
					if (states[i]) {
						// 表示するコマンドを設定
						commandId[count] = COMMAND_ID[i];
						commandStr[count] = res.getString(COMMAND_RES[i]).replaceAll("\\(%\\)", "");
						count++;
					}
				}
				catch (Exception e) {
					Log.e("PageSelectCheckDialog", "loadToolbarState: ループ2でエラーが発生しました.");
					if (e != null && e.getMessage() != null) {
						Log.e("PageSelectCheckDialog", "loadToolbarState: エラーメッセージ. " + e.getMessage());
					}
					Log.e("PageSelectCheckDialog", "loadToolbarState: i=" + i);
					Log.e("PageSelectCheckDialog", "loadToolbarState: COMMAND_ID[i]=" + COMMAND_ID[i]);
				}
			}
			if (debug) {
				Log.d("PageSelectCheckDialog", "loadToolbarState: 終了します.");
			}
		}
		catch (Exception e) {
			Log.e("PageSelectCheckDialog", "loadToolbarState: エラーが発生しました.");
			if (e != null && e.getMessage() != null) {
				Log.e("PageSelectCheckDialog", "loadToolbarState: エラーメッセージ. " + e.getMessage());
			}
		}
		if (debug) {Log.d("PageSelectCheckDialog", "loadToolbarState: 終了します.");}
		return states;
	}

	// 設定を保存
	private void saveToolbarState(Context context, boolean states[]) {
		boolean debug = false;
		if (debug) {Log.d("ImageActivity", "saveToolbarState: 開始します. states.length=" + states.length);}

		SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = sharedPreference.edit();
		for (int i = 0 ; i < states.length ; i ++) {
			try {
				ed.putBoolean(DEF.KEY_PAGE_SELECT_TOOLBAR + COMMAND_ID[i], states[i]);
			}
			catch (Exception e) {
				Log.e("PageSelectCheckDialog", "saveToolbarState: エラーが発生しました.");
				if (e != null && e.getMessage() != null) {
					Log.e("PageSelectCheckDialog", "saveToolbarState: エラーメッセージ. " + e.getMessage());
				}
			}
		}
		ed.commit();
		if (debug) {Log.d("PageSelectCheckDialog", "saveToolbarState: 終了します.");}
	}


	public class ItemArrayAdapter extends ArrayAdapter<String>
	{
		List<HashMap<String, Object>> mMap;
		private String[] mItems; // ファイル情報リスト

		// コンストラクタ
		public ItemArrayAdapter(Context context, int resId, String[] items)
		{
			super(context, resId, items);
			mItems = items;
		}

		// 一要素のビューの生成
		@Override
		public View getView(int index, View view, ViewGroup parent) {
			boolean debug = false;
			if (debug) {Log.d("PageSelectCheckDialog", "ItemArrayAdapter: getView: 開始します. index=" + index);}

			// レイアウトの生成
			CheckBox checkbox;
			ImageView imageview;
			TextView textview;
			Drawable drawable;

			if(view == null) {
				int marginW = (int)(4 * mScale);
				int marginH = (int)(0 * mScale);
				Context context = getContext();
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
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.gravity= Gravity.LEFT | Gravity.CENTER_VERTICAL;
				checkbox.setLayoutParams(layoutParams);
				inLayout.addView(checkbox);

				imageview = new ImageView(context);
				imageview.setId(1);
				int sp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 32, context.getResources().getDisplayMetrics());
				layoutParams = new LinearLayout.LayoutParams(sp, sp);
				layoutParams.gravity=Gravity.CENTER;
				layoutParams.setMargins(0, marginW, marginW, marginW);
				imageview.setLayoutParams(layoutParams);
				imageview.setScaleType(ImageView.ScaleType.CENTER);
				inLayout.addView(imageview);

				textview = new TextView(context);
				textview.setId(2);
				layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.gravity= Gravity.LEFT | Gravity.CENTER_VERTICAL;
				textview.setLayoutParams(layoutParams);
				textview.setPadding(marginW, marginW, 0, marginW);
				inLayout.addView(textview);

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
				if (debug) {Log.d("PageSelectCheckDialog", "ItemArrayAdapter: getView: 設定済みのビューを呼び出します. index=" + index);}
				checkbox = (CheckBox)view.findViewById(0);
				imageview = (ImageView)view.findViewById(1);
				textview = (TextView)view.findViewById(2);
			}

			// 値の指定
			checkbox.setTag(index);
			checkbox.setChecked(mStates[index]);

			if (imageview != null) {
				if (0 <= index && index < COMMAND_DRAWABLE.length) {
					if (debug) {
						Log.d("PageSelectCheckDialog", "ItemArrayAdapter: getView: アイコンをセットします. index=" + index);
					}
					drawable = mActivity.getDrawable(COMMAND_DRAWABLE[index]);
					drawable.setTint(mActivity.getResources().getColor(R.color.white1));
					imageview.setImageDrawable(drawable);
				}
			}
			else {
				Log.d("PageSelectCheckDialog", "ItemArrayAdapter: getView: ImageViewがnullです. index=" + index);
			}

			textview.setText(mItems[index]);
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		// キャンセルクリック
		if (v.getId() == R.id.btn_ok) {
			// 選択完了
			saveToolbarState(mActivity, mStates);
		}
		dismiss();
	}

}