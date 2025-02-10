package src.comitton.config;

import src.comitton.config.seekbar.AutoPlaySeekbar;
import src.comitton.config.seekbar.LongTapSeekbar;
import src.comitton.config.seekbar.ScalingSeekbar;
import src.comitton.config.seekbar.ScrlRngHSeekbar;
import src.comitton.config.seekbar.ScrlRngWSeekbar;
import src.comitton.config.seekbar.WAdjustSeekbar;
import src.comitton.config.seekbar.WScalingSeekbar;
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

public class SetImageDetailActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private WAdjustSeekbar mWAdjust;
	private WScalingSeekbar mWScaling;
	private ScalingSeekbar mScaling;
	private LongTapSeekbar mLongTap;
	private ScrlRngWSeekbar mScrlRngW;
	private ScrlRngHSeekbar mScrlRngH;
	private AutoPlaySeekbar mAutoPlay;
	private ListPreference mMaxThread;
	private ListPreference mLoupeSize;

	public static final int MaxThread[] =
		{ R.string.maxthread00		// 自動
		, R.string.maxthread01		// 1スレッド
		, R.string.maxthread02		// 2スレッド
		, R.string.maxthread03		// 3スレッド
		, R.string.maxthread04		// 4スレッド
		, R.string.maxthread05		// 5スレッド
		, R.string.maxthread06		// 6スレッド
		, R.string.maxthread07		// 7スレッド
		, R.string.maxthread08 };	// 8スレッド
	public static final int LoupeSize[] =
		{
			R.string.loupesize00,
			R.string.loupesize01,
			R.string.loupesize02
		};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.imagedetail);

		mLongTap   = (LongTapSeekbar)getPreferenceScreen().findPreference(DEF.KEY_LONGTAP);
		mWAdjust   = (WAdjustSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WADJUST);
		mWScaling   = (WScalingSeekbar)getPreferenceScreen().findPreference(DEF.KEY_WSCALING);
		mScaling   = (ScalingSeekbar)getPreferenceScreen().findPreference(DEF.KEY_SCALING);
		mScrlRngW  = (ScrlRngWSeekbar)getPreferenceScreen().findPreference(DEF.KEY_SCRLRNGW);
		mScrlRngH  = (ScrlRngHSeekbar)getPreferenceScreen().findPreference(DEF.KEY_SCRLRNGH);
		mAutoPlay = (AutoPlaySeekbar) getPreferenceScreen().findPreference(DEF.KEY_AUTOPLAY);
		mMaxThread = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_MAXTHREAD);
		mLoupeSize = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LOUPESIZE);


		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_IDTLHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_imagedetail);	// 設定画面
				Intent intent;
				intent = new Intent(SetImageDetailActivity.this, HelpActivity.class);
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
		mLongTap.setSummary(getLongTapSummary(sharedPreferences));
		mWAdjust.setSummary(getWAdjustSummary(sharedPreferences));
		mWScaling.setSummary(getWScalingSummary(sharedPreferences));	// イメージ幅調整
		mScaling.setSummary(getScalingSummary(sharedPreferences));		// イメージ拡大縮小
		mScrlRngW.setSummary(getScrlRngWSummary(sharedPreferences));
		mScrlRngH.setSummary(getScrlRngHSummary(sharedPreferences));
		mAutoPlay.setSummary(getAutoPlaySummary(sharedPreferences));
		mMaxThread.setSummary(getMaxThreadSummary(sharedPreferences));	// 最大スレッド数
		mLoupeSize.setSummary(getLoupeSizeSummary(sharedPreferences));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		// シークバー
		if(key.equals(DEF.KEY_LONGTAP)){
			// 長押し時間
			mLongTap.setSummary(getLongTapSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WADJUST)){
			// 縦横比調整
			mWAdjust.setSummary(getWAdjustSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_WSCALING)){
			// 幅調整
			mWScaling.setSummary(getWScalingSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SCALING)){
			// 拡大縮小
			mScaling.setSummary(getScalingSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SCRLRNGW)){
			// スクロール量
			mScrlRngW.setSummary(getScrlRngWSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SCRLRNGH)){
			// スクロール量
			mScrlRngH.setSummary(getScrlRngHSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_AUTOPLAY)) {
			// 自動再生
			mAutoPlay.setSummary(getAutoPlaySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MAXTHREAD)){
			// 最大スレッド数
			mMaxThread.setSummary(getMaxThreadSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_LOUPESIZE)){
			// ルーペサイズ
			mLoupeSize.setSummary(getLoupeSizeSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getLongTap(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_LONGTAP, DEF.DEFAULT_LONGTAP);
		return num;
	}

	public static int getWAdjust(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_WADJUST, DEF.DEFAULT_WADJUST);
		return num;
	}

	public static int getWScaling(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_WSCALING, DEF.DEFAULT_WSCALING);
		return num;
	}

	public static int getScaling(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_SCALING, DEF.DEFAULT_SCALING);
		return num;
	}

	public static int getScrlRngW(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_SCRLRNGW, DEF.DEFAULT_SCRLRNGW);
		return num;
	}

	public static int getScrlRngH(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_SCRLRNGH, DEF.DEFAULT_SCRLRNGH);
		return num;
	}

	public static int getAutoPlay(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_AUTOPLAY, DEF.DEFAULT_AUTOPLAY);
		return num;
	}

	public static boolean getAccessLamp(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_ACCESSLAMP, true);
		return flag;
	}

	public static int getMaxThread(SharedPreferences sharedPreferences){
		int val;
		val = DEF.getInt(sharedPreferences, DEF.KEY_MAXTHREAD, "2");
		if (val <= 0 || val >= MaxThread.length){
			val = 1;
		}
		return val;
	}

	public static int getLoupeSize(SharedPreferences sharedPreferences){
		int val;
		val = DEF.getInt(sharedPreferences, DEF.KEY_LOUPESIZE, "0");
		if (val < 0 || val >= LoupeSize.length){
			val = 0;
		}
		return val;
	}

	private String getLongTapSummary(SharedPreferences sharedPreferences){
		int val = getLongTap(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.msecSumm1);

		return	DEF.getMSecStr200(val, summ1);
	}

	private String getWAdjustSummary(SharedPreferences sharedPreferences){
		int val = getWAdjust(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.aspcSumm1);
		String summ2 = res.getString(R.string.aspcSumm2);

		return	DEF.getAdjustStr(val, summ1, summ2);
	}

	private String getWScalingSummary(SharedPreferences sharedPreferences){
		int val = getWScaling(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.scalSumm1);
		String summ2 = res.getString(R.string.scalSumm2);

		return	DEF.getWScalingStr(val, summ1, summ2);
	}

	private String getScalingSummary(SharedPreferences sharedPreferences){
		int val = getScaling(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.scalSumm1);
		String summ2 = res.getString(R.string.scalSumm2);

		return	DEF.getScalingStr(val, summ1, summ2);
	}

	private String getScrlRngWSummary(SharedPreferences sharedPreferences) {
		int val = getScrlRngW(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.srngSumm1);
		String summ2 = res.getString(R.string.srngSumm2);

		return	DEF.getScrlRangeStr(val, summ1, summ2);
	}

	private String getScrlRngHSummary(SharedPreferences sharedPreferences) {
		int val = getScrlRngH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.srngSumm1);
		String summ2 = res.getString(R.string.srngSumm2);

		return	DEF.getScrlRangeStr(val, summ1, summ2);
	}

	private String getAutoPlaySummary(SharedPreferences sharedPreferences) {
		int val = getAutoPlay(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.msecSumm1);

		return DEF.getAutoPlayStr(val, summ1);
	}

	private String getMaxThreadSummary(SharedPreferences sharedPreferences){
		int val = getMaxThread(sharedPreferences);
		String str;
		Resources res = getResources();
		str = res.getString(MaxThread[val]);
		if (val == 0) {
			int threads = Runtime.getRuntime().availableProcessors();
			if (threads > 7) {
				threads = 7;
			}
			else if (threads <= 0) {
				threads = 1;
			}
			str += " (" + res.getString(MaxThread[threads]) + ")";
		}
		return str;
	}

	private String getLoupeSizeSummary(SharedPreferences sharedPreferences){
		int val = getLoupeSize(sharedPreferences);
		String str;
		Resources res = getResources();
		str = res.getString(LoupeSize[val]);
		return str;
	}

}
