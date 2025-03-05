package src.comitton.config;

import src.comitton.config.seekbar.NoiseOverSeekbar;
import src.comitton.config.seekbar.NoiseScrlSeekbar;
import src.comitton.config.seekbar.NoiseUnderSeekbar;
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

public class SetNoiseActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private NoiseScrlSeekbar mNoiseScrl;
	private NoiseUnderSeekbar mNoiseUnder;
	private NoiseOverSeekbar mNoiseOver;
	private ListPreference mNoiseDec;

	public static final int[] NoiseDecName =
		{ R.string.noisedec00		// ゆっくり
		, R.string.noisedec01		// 普通
		, R.string.noisedec02 };	// 迅速

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.noise);

		mNoiseScrl  = (NoiseScrlSeekbar)getPreferenceScreen().findPreference(DEF.KEY_NOISESCRL);
		mNoiseUnder = (NoiseUnderSeekbar)getPreferenceScreen().findPreference(DEF.KEY_NOISEUNDER);
		mNoiseOver  = (NoiseOverSeekbar)getPreferenceScreen().findPreference(DEF.KEY_NOISEOVER);

		mNoiseDec  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_NOISEDEC);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_NOISEHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_noise);	// 設定画面
				Intent intent;
				intent = new Intent(SetNoiseActivity.this, HelpActivity.class);
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

		// シークバー
		mNoiseScrl.setSummary(getNoiseScrlSummary(sharedPreferences));
		mNoiseUnder.setSummary(getNoiseUnderSummary(sharedPreferences));
		mNoiseOver.setSummary(getNoiseOverSummary(sharedPreferences));

		mNoiseDec.setSummary(getNoiseDecSummary(sharedPreferences));	// 判定速度
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_NOISESCRL)){
			// スクロール速度
			mNoiseScrl.setSummary(getNoiseScrlSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_NOISEUNDER)){
			// スクロール速度
			mNoiseUnder.setSummary(getNoiseUnderSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_NOISEOVER)){
			// スクロール速度
			mNoiseOver.setSummary(getNoiseOverSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_NOISEDEC)){
			//
			mNoiseDec.setSummary(getNoiseDecSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getNoiseScrl(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_NOISESCRL, DEF.DEFAULT_NOISESCRL);
		return num;
	}

	public static int getNoiseUnder(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_NOISEUNDER, DEF.DEFAULT_NOISEUNDER);
		return num;
	}

	public static int getNoiseOver(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_NOISEOVER, DEF.DEFAULT_NOISEOVER);
		return num;
	}

	public static boolean getNoiseLevel(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_NOISELEVEL, false);
		return flag;
	}

	public static int getNoiseDec(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_NOISEDEC, "1");
		if( val < 0 || val >= NoiseDecName.length){
			val = 1;
		}
		return val;
	}

	private String getNoiseScrlSummary(SharedPreferences sharedPreferences){
		int val = getNoiseScrl(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getScrlSpeedStr(val, summ1);
	}

	private String getNoiseUnderSummary(SharedPreferences sharedPreferences){
		int val = getNoiseUnder(sharedPreferences);

		return	DEF.getNoiseLevelStr(val);
	}

	private String getNoiseOverSummary(SharedPreferences sharedPreferences){
		int val = getNoiseOver(sharedPreferences);

		return	DEF.getNoiseLevelStr(val);
	}

	private String getNoiseDecSummary(SharedPreferences sharedPreferences){
		int val = getNoiseDec(sharedPreferences);
		Resources res = getResources();
		return res.getString(NoiseDecName[val]);
	}
}
