package src.comitton.config;

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

import java.io.File;

import jp.dip.muracoro.comittonx.R;
import src.comitton.activity.FontDownloadActivity;
import src.comitton.activity.HelpActivity;
import src.comitton.common.DEF;

public class SetEpubActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	// 表示方向
	private ListPreference mViewRota;

	// フォントファイルの名前
	private ListPreference	mFontName;
	// 本文のフォントサイズ
	private TextFontBodySeekbar	mFontBody;
	// ヘッダ/フッタのフォントサイズ
	private TextFontInfoSeekbar	mFontInfo;
	// 左右の余白サイズ
	private TextMarginWSeekbar	mMarginW;
	// 上下の余白サイズ
	private TextMarginHSeekbar	mMarginH;

	// 時刻と充電表示
	private TimeAndBatteryPreference mTimeAndBattery;

	// 最終/先頭ページでの動作
	private ListPreference mLastPage;
	// タップ操作のパターン
	private OperationPreference mTapPattern;
	// 音量ボタンでのページめくり
	private ListPreference mVolKey;

	// 表示方向
	public static final int RotateName[] =
		{ R.string.rota00		// 回転あり
		, R.string.rota01		// 縦固定
		, R.string.rota02		// 横固定
		, R.string.rota03 };	// 縦固定(90°回転)
	// 時刻と充電表示の表示位置
	public static final int TimePosName[] =
		{ R.string.pnumpos00	// 左上
		, R.string.pnumpos01	// 中央上
		, R.string.pnumpos02	// 右上
		, R.string.pnumpos03	// 左下
		, R.string.pnumpos04	// 中央下
		, R.string.pnumpos05 };	// 右下
	// 時刻と充電表示の色
	public static final int PnumColorName[] =
		{ R.string.pnumcolor00		// 白
		, R.string.pnumcolor01 };		// 黒
	// 時刻と充電表示の表示書式
	public static final int TimeFormatName[] =
		{ R.string.timeformat00		// 24:00
		, R.string.timeformat01		// 24:00 [100%]
		, R.string.timeformat02		// 24:00 [100%] [AC]
		, R.string.timeformat03		// 24:00
		, R.string.timeformat04		// 24:00 [100%]
		, R.string.timeformat05 };	// 24:00 [100%] [AC]

	Resources mResources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.epub);

		// 表示方向
		mViewRota = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_VIEWROTA);

		// フォントファイルの名前
		mFontName = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTNAME);
		// 本文のフォントサイズ
		mFontBody = (TextFontBodySeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTBODY);
		// ヘッダ/フッタのフォントサイズ
		mFontInfo = (TextFontInfoSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTINFO);
		// 左右の余白サイズ
		mMarginW  = (TextMarginWSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_MARGINW);
		// 上下の余白サイズ
		mMarginH  = (TextMarginHSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_MARGINH);

		// 最終/先頭ページでの動作
		mLastPage = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LASTPAGE);
		// タップ操作のパターン
		mTapPattern = (OperationPreference)getPreferenceScreen().findPreference(DEF.KEY_TAPPATTERN);
		// 音量ボタンでのページめくり
		mVolKey = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VOLKEY);

		// 時刻と充電表示
		mTimeAndBattery = (TimeAndBatteryPreference) getPreferenceScreen().findPreference(DEF.KEY_TIMEANDBATTERY);

		mResources = getResources();

		String fontpath = DEF.getFontDirectory();
		CharSequence[] items;
		CharSequence[] values;
		// キャッシュ保存先

		File files[] = new File(fontpath).listFiles();
		if (files == null) {
			// ファイルなし
			items = new CharSequence[1];
			values = new CharSequence[1];
		}
		else {
			// 数える
			int i = 1;
			for (File file : files) {
				if (file != null && file.isFile()) {
					i ++;
				}
			}
			items = new CharSequence[i];
			values = new CharSequence[i];

			// 設定
			i = 1;
			for (File file : files) {
				if (file != null && file.isFile()) {
					if (i < items.length) {
						items[i] = file.getName();
						values[i] = file.getName();
						i ++;
					}
				}
			}
		}

		// リソースから読み込み
		Resources res = getResources();
		items[0] = res.getString(R.string.defaultFont);
		values[0] = "";

		mFontName.setEntries(items);
		mFontName.setEntryValues(values);
		mFontName.setDefaultValue(values[0]);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_TEXTHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_epub);	// 設定画面
				Intent intent;
				intent = new Intent(SetEpubActivity.this, HelpActivity.class);
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

		mFontBody.setSummary(getFontBodySummary(sharedPreferences));	// 本文のフォントサイズ(px)
		mFontInfo.setSummary(getFontInfoSummary(sharedPreferences));	// ヘッダ/フッタのフォントサイズ(px)
		mMarginW.setSummary(getMarginWSummary(sharedPreferences));		// 左右の余白サイズ(px)
		mMarginH.setSummary(getMarginHSummary(sharedPreferences));		// 上下の余白サイズ(px)

		mViewRota.setSummary(getViewRotaSummary(sharedPreferences));	// イメージ画面の回転制御

		mFontName.setSummary(getFontNameSummary(sharedPreferences));	// フォント名

		mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));	// タップ操作のパターン

		mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));		// Volキー動作
		mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));	// 確認メッセージ
		mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));	// 時刻と充電表示

	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_TX_VIEWROTA)){
			// 表示方向
			mViewRota.setSummary(getViewRotaSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTBODY)){
			// テキストのフォントサイズ
			mFontBody.setSummary(getFontBodySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTINFO)){
			// テキストのフォントサイズ
			mFontInfo.setSummary(getFontInfoSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_MARGINW)){
			// テキストのフォントサイズ
			mMarginW.setSummary(getMarginWSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_MARGINH)){
			// テキストのフォントサイズ
			mMarginH.setSummary(getMarginHSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTNAME)){
			// スクロール量
			mFontName.setSummary(getFontNameSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TAPPATTERN) || key.equals(DEF.KEY_TAPRATE)){
			// タップ操作のパターン
			mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_VOLKEY)){
			// 音量ボタンでのページめくり
			mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_LASTPAGE)){
			// 最終/先頭ページでの動作
			mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TIMEDISP) || key.equals(DEF.KEY_TIMEFORMAT) || key.equals(DEF.KEY_TIMEPOS) || key.equals(DEF.KEY_TIMESIZE) || key.equals(DEF.KEY_TIMECOLOR)){
			// 時刻と充電表示
			mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static boolean getEpubViewer(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EP_VIEWER, false);
		return flag;
	}

	public static int getViewRota(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_VIEWROTA, "0");
		return val;
	}

	public static int getBkLight(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_BKLIGHT, "11");
		return val;
	}

	public static String getFontName(SharedPreferences sharedPreferences){
		return sharedPreferences.getString(DEF.KEY_TX_FONTNAME, "");
	}

	public static int getFontBody(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_FONTBODY, DEF.DEFAULT_TX_FONTBODY);
		return num;
	}

	public static int getFontInfo(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_FONTINFO, DEF.DEFAULT_TX_FONTINFO);
		return num;
	}

	public static int getMarginW(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_MARGINW, DEF.DEFAULT_TX_MARGINW);
		return num;
	}

	public static int getMarginH(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_MARGINH, DEF.DEFAULT_TX_MARGINH);
		return num;
	}

	public static boolean getNotice(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TX_NOTICE, true);
		return flag;
	}

	public static boolean getNoSleep(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TX_NOSLEEP, false);
		return flag;
	}

	public static boolean getEffect(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TX_EFFECT, true);
		return flag;
	}

	public static int getTimeFormat(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMEFORMAT, DEF.DEFAULT_TIMEFORMAT);
		if( val < 0 || val >= TimeFormatName.length){
			val = 1;
		}
		return val;
	}

	public static int getTimePos(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMEPOS, DEF.DEFAULT_TIMEPOS);
		if( val < 0 || val >= TimePosName.length){
			val = 5;
		}
		return val;
	}

	public static int getTimeSize(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMESIZE, DEF.DEFAULT_TIMESIZE);
		return val;
	}

	public static boolean getTimeDisp(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TIMEDISP, false);
		return flag;
	}

	public static int getTimeColor(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMECOLOR, DEF.DEFAULT_TIMECOLOR);
		if( val < 0 || val >= PnumColorName.length){
			val = 1;
		}
		return val;
	}

	// 設定の読込(定義変更中)
	private String getViewRotaSummary(SharedPreferences sharedPreferences){
		int val = getViewRota(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateName[val]);
	}

	private String getFontNameSummary(SharedPreferences sharedPreferences){
		String val = getFontName(sharedPreferences);
		if (val != null && val.length() > 0) {
			return val;
		}
		Resources res = getResources();
		return res.getString(R.string.defaultFont);
	}

	private String getFontBodySummary(SharedPreferences sharedPreferences){
		int val = getFontBody(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontInfoSummary(SharedPreferences sharedPreferences){
		int val = getFontInfo(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getMarginWSummary(SharedPreferences sharedPreferences){
		int val = getMarginW(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return	DEF.getDispMarginStr(val, summ1);
	}

	private String getMarginHSummary(SharedPreferences sharedPreferences){
		int val = getMarginH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return	DEF.getDispMarginStr(val, summ1);
	}

	private String getTimeSummary(SharedPreferences sharedPreferences){
		boolean disp = getTimeDisp(sharedPreferences);
		int format = getTimeFormat(sharedPreferences);
		int pos = getTimePos(sharedPreferences);
		int size = getTimeSize(sharedPreferences);
		int color = getTimeColor(sharedPreferences);
		Resources res = getResources();

		String summ;
		if (disp) {
			summ = res.getString(TimeFormatName[format])
					+ ", " + res.getString(TimePosName[pos])
					+ ", " + DEF.getPnumSizeStr(size, res.getString(R.string.unitSumm1))
					+ ", " + res.getString(PnumColorName[color]);
		}
		else {
			summ = res.getString(R.string.pnumnodisp);
		}
		return summ;
	}
}
