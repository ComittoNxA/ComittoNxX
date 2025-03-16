package src.comitton.config;

import src.comitton.common.Logcat;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceHeaderFragmentCompat;

import java.util.ArrayList;
import java.util.Arrays;


public class SetCommonActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "SetCommonActivity";


	public class SetCommonFragment extends PreferenceHeaderFragmentCompat implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

		public SetCommonFragment () {

		}

		@Override
		public PreferenceFragmentCompat onCreatePreferenceHeader() {
			return new HeaderFragment();
		}

	}

	public class HeaderFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.common, rootKey);
		}
	}


	private ListPreference mRotateBtn;
	private ListPreference mCharset;

	private EditTextPreference mPriorityWord01;
	private EditTextPreference mPriorityWord02;
	private EditTextPreference mPriorityWord03;
	private EditTextPreference mPriorityWord04;
	private EditTextPreference mPriorityWord05;
	private EditTextPreference mPriorityWord06;
	private EditTextPreference mPriorityWord07;
	private EditTextPreference mPriorityWord08;
	private EditTextPreference mPriorityWord09;
	private EditTextPreference mPriorityWord10;

	public static final int[] RotateBtnName =
		{ R.string.rotabtn00	// 使用しない
		, R.string.rotabtn01	// フォーカスキー
		, R.string.rotabtn02 };	// シャッターキー

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.common);
		mRotateBtn  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_ROTATEBTN);
		mCharset    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_CHARSET);

		mPriorityWord01 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_01);
		mPriorityWord02 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_02);
		mPriorityWord03 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_03);
		mPriorityWord04 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_04);
		mPriorityWord05 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_05);
		mPriorityWord06 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_06);
		mPriorityWord07 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_07);
		mPriorityWord08 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_08);
		mPriorityWord09 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_09);
		mPriorityWord10 = (EditTextPreference)getPreferenceScreen().findPreference(DEF.KEY_SORT_PRIORITY_WORD_10);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_COMMHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_common);	// 設定画面
				Intent intent;
				intent = new Intent(SetCommonActivity.this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("Url", url);
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

		mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_01, ""));
		mPriorityWord02.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_02, ""));
		mPriorityWord03.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_03, ""));
		mPriorityWord04.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_04, ""));
		mPriorityWord05.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_05, ""));
		mPriorityWord06.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_06, ""));
		mPriorityWord07.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_07, ""));
		mPriorityWord08.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_08, ""));
		mPriorityWord09.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_09, ""));
		mPriorityWord10.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_10, ""));
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

		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_01)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_01, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_02)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_02, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_03)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_03, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_04)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_04, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_05)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_05, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_06)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_06, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_07)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_07, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_08)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_08, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_09)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_09, ""));
		}
		else if(key.equals(DEF.KEY_SORT_PRIORITY_WORD_10)){
			mPriorityWord01.setSummary(sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_10, ""));
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

	private static String getCharsetSummary(SharedPreferences sharedPreferences){
		int val = getCharset(sharedPreferences);
		return DEF.CharsetList[val];
	}

	public static void loadSettings(SharedPreferences sharedPreferences) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		DEF.CHAR_DETECT = sharedPreferences.getBoolean(DEF.KEY_CHAR_DETECT, true);
		DEF.CHARSET = getCharsetSummary(sharedPreferences);

		DEF.SORT_BY_IGNORE_WIDTH = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_IGNORE_WIDTH, true);
		DEF.SORT_BY_IGNORE_CASE = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_IGNORE_CASE, true);
		DEF.SORT_BY_SYMBOL = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_SYMBOL, true);
		DEF.SORT_BY_NATURAL_NUMBERS = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_NATURAL_NUMBERS, true);
		DEF.SORT_BY_KANJI_NUMERALS = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_KANJI_NUMERALS, true);
		DEF.SORT_BY_JAPANESE_VOLUME_NAME = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_JAPANESE_VOLUME_NAME, true);
		DEF.SORT_BY_FILE_TYPE = sharedPreferences.getBoolean(DEF.KEY_SORT_BY_FILE_TYPE, true);

		ArrayList<String> priorityWords = new ArrayList<String>();
		String word = "";
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_01, "cover");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_02, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_03, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_04, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_05, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_06, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_07, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_08, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_09, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		word = sharedPreferences.getString(DEF.KEY_SORT_PRIORITY_WORD_10, "");
		if (word.length() > 0) {
			priorityWords.add(word);
		}
		DEF.PRIORITY_WORDS = priorityWords.toArray(new String[0]);

		Logcat.d(logLevel, "DEF.PRIORITY_WORDS=" + Arrays.toString(DEF.PRIORITY_WORDS));
	}
	// 終了処理
	protected void onDestroy() {
		super.onDestroy();
		loadSettings(getPreferenceScreen().getSharedPreferences());
	}
}
