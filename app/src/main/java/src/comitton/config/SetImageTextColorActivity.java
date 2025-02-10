package src.comitton.config;

import src.comitton.config.color.ColorCntSetting;
import src.comitton.config.color.ColorGuiSetting;
import src.comitton.config.color.ColorHitSetting;
import src.comitton.config.color.ColorMgnSetting;
import src.comitton.config.color.ColorTvbSetting;
import src.comitton.config.color.ColorTvgSetting;
import src.comitton.config.color.ColorTvtSetting;
import src.comitton.config.color.ColorTxtCntSetting;
import src.comitton.config.color.ColorTxtGuiSetting;
import src.comitton.config.color.ColorTxtMgnSetting;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SetImageTextColorActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ColorMgnSetting mMgnColor;
	private ColorCntSetting mCntColor;
	private ColorGuiSetting mGuiColor;

	private ColorTxtMgnSetting mTxtMgnColor;
	private ColorTxtCntSetting mTxtCntColor;
	private ColorTxtGuiSetting mTxtGuiColor;

	private ColorTvtSetting mTvtColor;
	private ColorTvbSetting mTvbColor;
	private ColorTvgSetting mTvgColor;

	private ColorHitSetting mHitColor;

	private ListPreference mGradation;

 	public static final int GradationName[] =
		{ R.string.txgrad00		// グラデーションなし
		, R.string.txgrad01		// 左上→右下
		, R.string.txgrad02		// 上  →下
		, R.string.txgrad03		// 右上→左下
		, R.string.txgrad04		// 右  →左
		, R.string.txgrad05		// 右下→左下
		, R.string.txgrad06		// 下  →上
		, R.string.txgrad07		// 左下→右上
		, R.string.txgrad08 };	// 左  →右

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.imagetextcolor);

		mMgnColor = (ColorMgnSetting) getPreferenceScreen().findPreference(DEF.KEY_MGNRGB);
		mCntColor = (ColorCntSetting) getPreferenceScreen().findPreference(DEF.KEY_CNTRGB);
		mGuiColor = (ColorGuiSetting) getPreferenceScreen().findPreference(DEF.KEY_GUIRGB);

		mTxtMgnColor = (ColorTxtMgnSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_MGNRGB);
		mTxtCntColor = (ColorTxtCntSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_CNTRGB);
		mTxtGuiColor = (ColorTxtGuiSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_GUIRGB);

		mTvtColor = (ColorTvtSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_TVTRGB);
		mTvbColor = (ColorTvbSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_TVBRGB);
		mTvgColor = (ColorTvgSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_TVGRGB);

		mHitColor = (ColorHitSetting) getPreferenceScreen().findPreference(DEF.KEY_TX_HITRGB);

		mGradation  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_GRADATION);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_ITCLRHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_imagetextcolor);	// 設定画面
				Intent intent;
				intent = new Intent(SetImageTextColorActivity.this, HelpActivity.class);
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

		// 色設定
		mMgnColor.setSummary(getColorSummary(getMgnColor(sharedPreferences)));	// 余白
		mCntColor.setSummary(getColorSummary(getCntColor(sharedPreferences)));	// 中央余白
		mGuiColor.setSummary(getColorSummary(getGuiColor(sharedPreferences)));	// ガイド

		mTxtMgnColor.setSummary(getColorSummary(getTxtMgnColor(sharedPreferences)));	// 余白
		mTxtCntColor.setSummary(getColorSummary(getTxtCntColor(sharedPreferences)));	// 中央余白
		mTxtGuiColor.setSummary(getColorSummary(getTxtGuiColor(sharedPreferences)));	// ガイド

		mTvtColor.setSummary(getColorSummary(getTvtColor(sharedPreferences)));	// テキスト
		mTvbColor.setSummary(getColorSummary(getTvbColor(sharedPreferences)));	// 背景１
		mTvgColor.setSummary(getColorSummary(getTvgColor(sharedPreferences)));	// 背景２

		mHitColor.setSummary(getColorSummary(getHitColor(sharedPreferences)));	// 検索ヒット

		mGradation.setSummary(getGradationSummary(sharedPreferences));	// ファイル選択画面の回転制御
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// 色
		if(key.equals(DEF.KEY_MGNRGB)){
			//
			mMgnColor.setSummary(getColorSummary(getMgnColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_CNTRGB)){
			//
			mCntColor.setSummary(getColorSummary(getCntColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_GUIRGB)){
			//
			mGuiColor.setSummary(getColorSummary(getGuiColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_MGNRGB)){
			//
			mTxtMgnColor.setSummary(getColorSummary(getMgnColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_CNTRGB)){
			//
			mTxtCntColor.setSummary(getColorSummary(getCntColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_GUIRGB)){
			//
			mTxtGuiColor.setSummary(getColorSummary(getGuiColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_TVTRGB)){
			//
			mTvtColor.setSummary(getColorSummary(getTvtColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_TVBRGB)){
			//
			mTvbColor.setSummary(getColorSummary(getTvbColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_TVGRGB)){
			//
			mTvgColor.setSummary(getColorSummary(getTvgColor(sharedPreferences)));
		}
		else if(key.equals(DEF.KEY_TX_GRADATION)){
			//
			mGradation.setSummary(getGradationSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_HITRGB)){
			//
			mHitColor.setSummary(getColorSummary(getHitColor(sharedPreferences)));
		}
	}

	// 設定の読込
	public static int getMgnColor(SharedPreferences sp){
		int val = DEF.getColorValue(sp, DEF.KEY_MGNCOLOR, DEF.KEY_MGNRGB, 22);
		return val;
	}

	// 中央余白の色
	public static int getCntColor(SharedPreferences sp){
		int val = DEF.getColorValue(sp, DEF.KEY_CNTCOLOR, DEF.KEY_CNTRGB, 22);
		return val;
	}

	public static int getGuiColor(SharedPreferences sp){
		int val = DEF.getGuideValue(sp, null, DEF.KEY_GUIRGB, 1);
		return val & 0x00FFFFFF | 0x80000000;
	}

	public static int getTxtMgnColor(SharedPreferences sp){
		int val = DEF.getColorValue(sp, null, DEF.KEY_TX_MGNRGB, 22);
		return val;
	}

	// 中央余白の色
	public static int getTxtCntColor(SharedPreferences sp){
		int val = DEF.getColorValue(sp, null, DEF.KEY_TX_CNTRGB, 22);
		return val;
	}

	public static int getTxtGuiColor(SharedPreferences sp){
		int val = DEF.getGuideValue(sp, null, DEF.KEY_TX_GUIRGB, 1);
		return val & 0x00FFFFFF | 0x80000000;
	}

	public static int getTvtColor(SharedPreferences sp){
		int val = sp.getInt(DEF.KEY_TX_TVTRGB, DEF.COLOR_TX_TVTRGB);
		return val;
	}

	// 背景１
	public static int getTvbColor(SharedPreferences sp){
		int val = sp.getInt(DEF.KEY_TX_TVBRGB, DEF.COLOR_TX_TVBRGB);
		return val;
	}

	// 背景２
	public static int getTvgColor(SharedPreferences sp){
		int val = sp.getInt(DEF.KEY_TX_TVGRGB, DEF.COLOR_TX_TVGRGB);
		return val;
	}

	// 検索ヒット
	public static int getHitColor(SharedPreferences sp){
		int val = sp.getInt(DEF.KEY_TX_HITRGB, DEF.COLOR_TX_HITRGB);
		return val;
	}

	// グラデーション有無
	public static int getGradation(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_GRADATION, "1");
		return val;
	}

	private String getGradationSummary(SharedPreferences sharedPreferences){
		int val = getGradation(sharedPreferences);
		Resources res = getResources();
		return res.getString(GradationName[val]);
	}

	private String getColorSummary(int val) {
		String str[] = { "Red", "Green", "Blue" };
		String result = "";
		for (int i = 0; i < 3; i++) {
			int v = ((val >> 8 * (2 - i)) & 0x000000FF);
			result += str[i] + "=" + v;
			if (i != 2) {
				result += ", ";
			}
		}
		result += String.format(" (%1$06X)", val & 0x00FFFFFF);
		return result;
	}
}
