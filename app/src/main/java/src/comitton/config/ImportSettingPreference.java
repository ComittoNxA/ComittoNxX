package src.comitton.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ImportSettingPreference extends DialogPreference implements OnItemClickListener, OnClickListener {
	private Context mContext;
	private SharedPreferences mSp;

	private String mNoName = "(No Name)";

	private Button mButtonOk;
	private EditText mEditView;
	private TextView mMsgView;
	private ListView mListView;
	private ItemArrayAdapter mItemArrayAdapter;

	private static final int LAYOUT_PADDING = 10;

	public ImportSettingPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSp = PreferenceManager.getDefaultSharedPreferences(context);

		// OKボタンを非表示にする
		//setPositiveButtonText(null);
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.setPadding(LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING);
		mMsgView = new TextView(mContext);
		mEditView = new EditText(mContext);
		mEditView.setMaxLines(1);
		mEditView.setHint(R.string.selectFile);
		mEditView.setFocusable(false);
		mListView = new ListView(mContext);
		mListView.setScrollingCacheEnabled(false);
		mListView.setOnItemClickListener(this);
		updateImportList();

		layout.addView(mMsgView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		layout.addView(mEditView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		layout.addView(mListView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		String str = (String) getDialogMessage();
		mMsgView.setText(str);

		return layout;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);

		Dialog dialog = getDialog();
		if (dialog != null) {
			mButtonOk = (Button)dialog.findViewById(android.R.id.button1);
			if (mButtonOk != null) {
				mButtonOk.setOnClickListener(this);
			}
		}
	}

	@Override
	protected void onDialogClosed (boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult == true) {
		}
	}

	private void updateImportList() {
		String fontpath = DEF.getConfigDirectory();
		List<String> items = new ArrayList<String>();

		File files[] = new File(fontpath).listFiles(getFileExtensionFilter(DEF.EXTENSION_SETTING));
		if (files != null) {
			// 設定
			for (File file : files) {
				if (file != null && file.isFile()) {
					String filename = file.getName();
					filename = filename.substring(0, filename.length()-4);
					if (filename.length() == 0){
						filename = mNoName;
					}
					items.add(filename);
				}
			}
			Collections.sort(items);
		}

		// リストの設定
		mItemArrayAdapter = new ItemArrayAdapter(mContext, -1, items);
		mListView.setAdapter(mItemArrayAdapter);
	}


    public FilenameFilter getFileExtensionFilter(String ext) {
        final String mExt = ext;
        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                boolean ret = name.endsWith(mExt);
                return ret;
            }
        };
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (parent == mListView) {
			// ファイルリスト選択
			String filename = mItemArrayAdapter.mItems.get(position);
			mEditView.setText(filename, TextView.BufferType.NORMAL);
		}

	}

	//	@Override
	public void onClick(View v) {
		boolean debug = false;
		if (debug) {Log.d("ImportSettingPreference", "onClick: 開始します.");}

		if (v == mButtonOk) {
			// OKボタン処理
			String filename = mEditView.getText().toString();
			if (filename.length() == 0) {
				if (debug) {Log.d("ImportSettingPreference", "onClick: ファイルを選択してください.");}
				Toast.makeText(mContext, R.string.selectFile, Toast.LENGTH_SHORT).show();
				return;
			} else if (filename.equals(mNoName)) {
				if (debug) {Log.d("ImportSettingPreference", "onClick: " + mNoName + " を選択しました.");}
				filename = "";
			}

			String filepath = DEF.getConfigDirectory() + filename + DEF.EXTENSION_SETTING;

			try {
				File file = new File(filepath);
				if (!file.exists()) {
					// ファイルが存在しない
					Toast.makeText(mContext, mContext.getResources().getString(R.string.FileNotExist) + "\n" + filepath, Toast.LENGTH_SHORT).show();
					return;
				}
			} catch (Exception ex) {
				// 例外発生
				String msg = "";
				if (ex != null && ex.getMessage() != null) {
					msg = ex.getMessage();
				}
				Log.e("Bookmark/Load", msg);
				return;
			}


			FileInputStream is = null;
			InputStreamReader sr = null;
			BufferedReader br = null;
			Editor ed = mSp.edit();
			try {
				is = new FileInputStream(filepath);
				sr = new InputStreamReader(is, "UTF-8");
				br = new BufferedReader(sr, 8192);

				// 既存の設定を削除
				Map<String, ?> keys = mSp.getAll();
				if (keys != null) {
					for (String key : keys.keySet()) {
						if (DEF.checkExportKey(key) == true) {
							// 出力対象
							ed.remove(key);
						}
					}
				}

				// ファイル読み込み
				String line;
				while ((line = br.readLine()) != null) {
					// 種別取得
					char valuetype = line.charAt(0);
					// '='の前後をキーと値にする
					int index = line.indexOf('=', 2);
					if (index <= 0) {
						continue;
					}
					// キーと値を取得
					String key = line.substring(2, index);
					String value = line.substring(index + 1);
					switch (valuetype) {
						case 'S':
						case 's': // NxT専用キー
						{
							ed.putString(key, value);
							break;
						}
						case 'B':
						case 'b': // NxT専用キー
						{
							boolean work = Boolean.parseBoolean(value);
							ed.putBoolean(key, work);
							break;
						}
						case 'F':
						case 'f': // NxT専用キー
						{
							float work = Float.parseFloat(value);
							ed.putFloat(key, work);
							break;
						}
						case 'I':
						case 'i': // NxT専用キー
						{
							int work = Integer.parseInt(value);
							ed.putInt(key, work);
							break;
						}
						case 'L':
						case 'l': // NxT専用キー
						{
							long work = Long.parseLong(value);
							ed.putLong(key, work);
							break;
						}
					}
				}
			} catch (Exception ex) {
				// 例外発生
				String msg = "";
				if (ex != null && ex.getMessage() != null) {
					msg = ex.getMessage();
				}
				Log.e("Bookmark/Load", msg);
				return;
			} finally {
				try {
					if (br != null) {
						br.close();
					}
					if (sr != null) {
						sr.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
					;
				}
				if (ed != null) {
					ed.commit();
				}
			}
			if (debug) {Log.d("ImportSettingPreference", "onClick: 設定を読み込みました. " + filepath);}
			Toast.makeText(mContext, mContext.getResources().getString(R.string.LoadConfig) + "\n" + filepath, Toast.LENGTH_SHORT).show();
			Dialog dialog = getDialog();
			if (dialog != null) {
				dialog.dismiss();
			}
		}
	}

	public class ItemArrayAdapter extends ArrayAdapter<String>
	{
		private List<String>	mItems; // ファイル情報リスト

		// コンストラクタ
		public ItemArrayAdapter(Context context, int resId, List<String> items)
		{
			super(context, resId, items);
			mItems = items;
		}

		// 一要素のビューの生成
		@Override
		public View getView(int index, View view, ViewGroup parent)
		{
			// レイアウトの生成
			if(view == null) {
				Context context = getContext();
				// レイアウト
				LinearLayout layout = new LinearLayout( context );
				layout.setPadding( 10, 10, 10, 10 );
				layout.setBackgroundColor(Color.WHITE);
				view = layout;
				// テキスト
				TextView textview = new TextView(context);
				textview.setTag("text");
				textview.setTextColor(Color.BLACK);
				textview.setPadding(10, 10, 10, 10);
				textview.setTextSize(18);
				layout.addView(textview);
			}

			// 値の指定
			String item = mItems.get(index);
			TextView textview = (TextView)view.findViewWithTag("text");
			textview.setText(item);
			return view;
		}
	}
}
