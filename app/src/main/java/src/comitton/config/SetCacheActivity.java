package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SetCacheActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private MemSizeSeekbar mMemSize;
	private MemNextSeekbar mMemNext;
	private MemPrevSeekbar mMemPrev;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.cache);

		mMemSize  = (MemSizeSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MEMSIZE);
		mMemNext  = (MemNextSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MEMNEXT);
		mMemPrev  = (MemPrevSeekbar)getPreferenceScreen().findPreference(DEF.KEY_MEMPREV);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_CACHEHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_cache);	// 設定画面
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

		// シークバー
		mMemSize.setSummary(getMemSizeSummary(sharedPreferences));	// 使用メモリサイズ
		mMemNext.setSummary(getMemNextSummary(sharedPreferences));	// 次ページ数
		mMemPrev.setSummary(getMemPrevSummary(sharedPreferences));	// 前ページ数
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_MEMSIZE)){
			// 使用メモリサイズ
			mMemSize.setSummary(getMemSizeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MEMNEXT)){
			// 次ページ数
			mMemNext.setSummary(getMemNextSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MEMPREV)){
			// 前ページ数
			mMemPrev.setSummary(getMemPrevSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getMemSize(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMSIZE, DEF.DEFAULT_MEMSIZE);
		return num;
	}

	public static int getMemNext(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMNEXT, DEF.DEFAULT_MEMNEXT);
		return num;
	}

	public static int getMemPrev(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_MEMPREV, DEF.DEFAULT_MEMPREV);
		return num;
	}

	private String getMemSizeSummary(SharedPreferences sharedPreferences){
		int val = getMemSize(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mSizeSumm1);
		String summ2 = res.getString(R.string.mSizeSumm2);

		return	DEF.getMemSizeStr(val, summ1, summ2);
	}

	private String getMemNextSummary(SharedPreferences sharedPreferences){
		int val = getMemNext(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mPageSumm1);

		return	DEF.getMemPageStr(val, summ1);
	}

	private String getMemPrevSummary(SharedPreferences sharedPreferences){
		int val = getMemPrev(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mPageSumm1);

		return	DEF.getMemPageStr(val, summ1);
	}
}
