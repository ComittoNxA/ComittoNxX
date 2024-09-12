package src.comitton.config;

import src.comitton.activity.HelpActivity;
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

public class SetImageTextDetailActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ScrollSeekbar mScroll;
	private ClickAreaSeekbar mClickArea;
	private PageRangeSeekbar mPageRange;
	private TapRangeSeekbar mTapRange;
	private MarginSeekbar mMargin;
	private CenterSeekbar mCenter;
	private GradationSeekbar mGradation;
	private VolScrlSeekbar mVolScrl;
	private EffectTimeSeekbar mEffectTime;
	private MomentModeSeekbar mMomentMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.imagetextdetail);

		mScroll = (ScrollSeekbar) getPreferenceScreen().findPreference(DEF.KEY_SCROLL);
		mClickArea = (ClickAreaSeekbar) getPreferenceScreen().findPreference(DEF.KEY_CLICKAREA);
		mPageRange = (PageRangeSeekbar) getPreferenceScreen().findPreference(DEF.KEY_PAGERANGE);
		mTapRange = (TapRangeSeekbar) getPreferenceScreen().findPreference(DEF.KEY_TAPRANGE);
		mMargin = (MarginSeekbar) getPreferenceScreen().findPreference(DEF.KEY_MARGIN);
		mCenter = (CenterSeekbar) getPreferenceScreen().findPreference(DEF.KEY_CENTER);
		mGradation = (GradationSeekbar) getPreferenceScreen().findPreference(DEF.KEY_GRADATION);
		mVolScrl = (VolScrlSeekbar) getPreferenceScreen().findPreference(DEF.KEY_VOLSCRL);
		mEffectTime = (EffectTimeSeekbar) getPreferenceScreen().findPreference(DEF.KEY_EFFECTTIME);
		mMomentMode = (MomentModeSeekbar) getPreferenceScreen().findPreference(DEF.KEY_MOMENTMODE);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_ITDTLHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_imagetextdetail);	// 設定画面
				Intent intent;
				intent = new Intent(SetImageTextDetailActivity.this, HelpActivity.class);
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
		mScroll.setSummary(getScrollSummary(sharedPreferences)); // スクロール倍率
		mClickArea.setSummary(getClickAreaSummary(sharedPreferences));
		mPageRange.setSummary(getPageRangeSummary(sharedPreferences));
		mTapRange.setSummary(getTapRangeSummary(sharedPreferences));
		mMargin.setSummary(getMarginSummary(sharedPreferences));
		mCenter.setSummary(getCenterSummary(sharedPreferences)); // 中央余白幅
		mGradation.setSummary(getGradationSummary(sharedPreferences)); // 中央影幅
		mVolScrl.setSummary(getVolScrlSummary(sharedPreferences));
		mEffectTime.setSummary(getEffectTimeSummary(sharedPreferences));
		mMomentMode.setSummary(getMomentModeSummary(sharedPreferences));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		// シークバー
		if (key.equals(DEF.KEY_SCROLL)) {
			// スクロール倍率
			mScroll.setSummary(getScrollSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_CLICKAREA)) {
			// 操作領域サイズ
			mClickArea.setSummary(getClickAreaSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_PAGERANGE)) {
			// ページ選択感度
			mPageRange.setSummary(getPageRangeSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_TAPRANGE)) {
			// スクロール開始感度
			mTapRange.setSummary(getTapRangeSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_MARGIN)) {
			// 余白サイズ
			mMargin.setSummary(getMarginSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_CENTER)) {
			// 中央余白幅
			mCenter.setSummary(getCenterSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_GRADATION)) {
			// 中央影幅
			mGradation.setSummary(getGradationSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_VOLSCRL)) {
			// スクロール速度
			mVolScrl.setSummary(getVolScrlSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_EFFECTTIME)) {
			// スクロール速度
			mEffectTime.setSummary(getEffectTimeSummary(sharedPreferences));
		}
		else if (key.equals(DEF.KEY_MOMENTMODE)) {
			// スクロール速度
			mMomentMode.setSummary(getMomentModeSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static boolean getImmEnable(SharedPreferences sharedPreferences){
		return DEF.getBoolean(sharedPreferences, DEF.KEY_IMMENABLE, false);
	}

	public static boolean getOldMenu(SharedPreferences sharedPreferences){
		return DEF.getBoolean(sharedPreferences, DEF.KEY_OLDMENU, false);
	}

	public static boolean getPinchEnable(SharedPreferences sharedPreferences){
		return DEF.getBoolean(sharedPreferences, DEF.KEY_PINCHENABLE, true);
	}

	public static boolean getBottomFile(SharedPreferences sharedPreferences){
		return DEF.getBoolean(sharedPreferences, DEF.KEY_BOTTOMFILE, true);
	}

	public static int getScroll(SharedPreferences sharedPreferences) {
		int num = DEF.getInt(sharedPreferences, DEF.KEY_SCROLL, DEF.DEFAULT_SCROLL);
		return num;
	}

	public static int getClickArea(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_CLICKAREA, DEF.DEFAULT_CLICKAREA);
		return num;
	}

	public static int getPageRange(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_PAGERANGE, DEF.DEFAULT_PAGERANGE);
		return num;
	}

	public static int getTapRange(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_TAPRANGE, DEF.DEFAULT_TAPRANGE);
		return num;
	}

	public static int getMargin(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MARGIN, DEF.DEFAULT_MARGIN);
		return num;
	}

	public static int getCenter(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_CENTER, DEF.DEFAULT_CENTER);
		return num;
	}

	public static int getGradation(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_GRADATION, DEF.DEFAULT_GRADATION);
		return num;
	}

	public static int getVolScrl(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_VOLSCRL, DEF.DEFAULT_VOLSCRL);
		return num;
	}

	public static int getEffectTime(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_EFFECTTIME, DEF.DEFAULT_EFFECTTIME);
		return num;
	}

	public static int getMomentMode(SharedPreferences sharedPreferences) {
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_MOMENTMODE, DEF.DEFAULT_MOMENTMODE);
		return num;
	}

	private String getScrollSummary(SharedPreferences sharedPreferences) {
		int val = getScroll(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.scrlSumm1);
		String summ2 = res.getString(R.string.scrlSumm2);

		return DEF.getScrollStr(val, summ1, summ2);
	}

	private String getClickAreaSummary(SharedPreferences sharedPreferences) {
		int val = getClickArea(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return DEF.getClickAreaStr(val, summ1);
	}

	private String getPageRangeSummary(SharedPreferences sharedPreferences) {
		int val = getPageRange(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return DEF.getPageRangeStr(val, summ1);
	}

	private String getTapRangeSummary(SharedPreferences sharedPreferences) {
		int val = getTapRange(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return DEF.getTapRangeStr(val, summ1);
	}

	private String getMarginSummary(SharedPreferences sharedPreferences) {
		int val = getMargin(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return DEF.getMarginStr(val, summ1);
	}

	private String getCenterSummary(SharedPreferences sharedPreferences) {
		int val = getCenter(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.centSumm1);

		return DEF.getCenterStr(val, summ1);
	}

	private String getGradationSummary(SharedPreferences sharedPreferences) {
		int val = getGradation(sharedPreferences);

		return DEF.getGradationStr(val);
	}

	private String getVolScrlSummary(SharedPreferences sharedPreferences) {
		int val = getVolScrl(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return DEF.getScrlSpeedStr(val, summ1);
	}

	private String getEffectTimeSummary(SharedPreferences sharedPreferences) {
		int val = getEffectTime(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.msecSumm1);

		return DEF.getEffectTimeStr(val, summ1);
	}

	private String getMomentModeSummary(SharedPreferences sharedPreferences) {
		int val = getMomentMode(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.mmentSumm1);
		String summ2 = res.getString(R.string.mmentSumm2);

		return DEF.getMomentModeStr(val, summ1, summ2);
	}
}
