package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SetCommonActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference mRotateBtn;
	private ListPreference mCharset;

	public static final int RotateBtnName[] =
		{ R.string.rotabtn00	// 使用しない
		, R.string.rotabtn01	// フォーカスキー
		, R.string.rotabtn02 };	// シャッターキー

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.common);
		mRotateBtn  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_ROTATEBTN);
		mCharset    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CHARSET);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_COMMHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_common);	// 設定画面
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mRotateBtn.setSummary(getRotateBtnSummary(sharedPreferences));	// 回転用ボタン
		mCharset.setSummary(getCharsetSummary(sharedPreferences));		// 文字コード
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_ROTATEBTN)){
			//
			mRotateBtn.setSummary(getRotateBtnSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_CHARSET)){
			//
			mCharset.setSummary(getCharsetSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getRotateBtn(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_ROTATEBTN, "1");
		if (val < 0 || val >= RotateBtnName.length){
			val = 0;
		}
		return val;
	}

	public static int getCharset(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_CHARSET, "1");
		if (val < 0 || val >= DEF.CharsetList.length){
			val = 1;
		}
		return val;
	}

	public static boolean getHiddenFile(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_HIDDENFILE, true);
		return flag;
	}

	// 設定の読込(定義変更中)
	private String getRotateBtnSummary(SharedPreferences sharedPreferences){
		int val = getRotateBtn(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateBtnName[val]);
	}

	private String getCharsetSummary(SharedPreferences sharedPreferences){
		int val = getCharset(sharedPreferences);
		return DEF.CharsetList[val];
	}
}
