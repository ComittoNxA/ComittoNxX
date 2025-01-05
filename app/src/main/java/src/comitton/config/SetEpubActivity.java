package src.comitton.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class SetEpubActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	Resources mResources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.epub);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

	// 設定の読込
	public static boolean getViewer(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_VIEWER, false);
		return flag;
	}

	// 設定の読込
	public static boolean getEpubOrder(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_ORDER, true);
		return flag;
	}

	// 設定の読込
	public static boolean getEpubThumb(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_THUMB, true);
		return flag;
	}
}
