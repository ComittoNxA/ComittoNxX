package src.comitton.config;

import java.io.File;

import src.comitton.config.seekbar.TextFontBodySeekbar;
import src.comitton.config.seekbar.TextFontInfoSeekbar;
import src.comitton.config.seekbar.TextFontRubiSeekbar;
import src.comitton.config.seekbar.TextFontTopSeekbar;
import src.comitton.config.seekbar.TextMarginHSeekbar;
import src.comitton.config.seekbar.TextMarginWSeekbar;
import src.comitton.config.seekbar.TextScrlRngHSeekbar;
import src.comitton.config.seekbar.TextScrlRngWSeekbar;
import src.comitton.config.seekbar.TextSpaceHSeekbar;
import src.comitton.config.seekbar.TextSpaceWSeekbar;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;

import jp.dip.muracoro.comittonx.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SetTextActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private TextFontTopSeekbar mFontTop;
	private TextFontBodySeekbar mFontBody;
	private TextFontRubiSeekbar mFontRubi;
	private TextFontInfoSeekbar mFontInfo;
	private TextSpaceWSeekbar mSpaceW;
	private TextSpaceHSeekbar mSpaceH;
	private TextMarginWSeekbar mMarginW;
	private TextMarginHSeekbar mMarginH;

	private TextScrlRngWSeekbar mScrlRngW;
	private TextScrlRngHSeekbar mScrlRngH;

	private ListPreference mViewRota;
	private ListPreference mIniScale;
	private ListPreference mInitView;
	private ListPreference mPaper;
	private ListPreference mPicSize;

	private ListPreference	mFontName;
	private ListPreference mViewPt;
	private ListPreference mVolKey;
	private ListPreference mLastPage;
	private ListPreference mPageSel;

	private OperationPreference mTapPattern;
	private TimeAndBatteryPreference mTimeAndBattery;

	public static final int[] ScaleName =
		{ R.string.selsize00	// 元のサイズで表示
		, R.string.selsize01	// 幅に合わせて表示
		, R.string.selsize02	// 高さに合わせて表示
		, R.string.selsize03 };	// 全体を表示
	public static final int[] ViewName =
		{ R.string.selview01	// 見開き表示
		, R.string.selview02	// 単ページ表示
		, R.string.selview03 };	// 連続表示
	public static final int[] RotateName =
		{ R.string.rota00		// 回転あり
		, R.string.rota01		// 縦固定
		, R.string.rota02		// 横固定
		, R.string.rota03 };	// 縦固定(90°回転)
	public static final int[] PaperName =
		{ R.string.paper00		// 縦画面サイズ
		, R.string.paper01		// 800×1280
		, R.string.paper02		// 720×1280
		, R.string.paper03		// 540×960
		, R.string.paper04 };	// 480×800
	public static final int[] PicSizeName =
		{ R.string.picsize00	// 元画像サイズ
		, R.string.picsize01	// 2倍
		, R.string.picsize02	// 3倍
		, R.string.picsize03	// 4倍
		, R.string.picsize04	// 画面サイズに拡大
		, R.string.picsize05	// 画面サイズに拡大(余白無視)
		, R.string.picsize06	// 画面サイズに拡大(見開き対応)
		, R.string.picsize07 };	// 画面サイズに拡大(見開き対応:余白無視)
	public static final int[] AscModeName =
		{ R.string.ascmode00	// 縦表示
		, R.string.ascmode01	// 横表示
		, R.string.ascmode02 };	// 2桁は縦
	public static final int[] TimePosName =
		{ R.string.pnumpos00	// 左上
		, R.string.pnumpos01	// 中央上
		, R.string.pnumpos02	// 右上
		, R.string.pnumpos03	// 左下
		, R.string.pnumpos04	// 中央下
		, R.string.pnumpos05 };	// 右下
	public static final int[] PnumColorName =
		{ R.string.pnumcolor00		// 白
		, R.string.pnumcolor01 };		// 黒
	public static final int[] TimeFormatName =
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

		addPreferencesFromResource(R.xml.text);
		mFontTop  = (TextFontTopSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTTOP);
		mFontBody = (TextFontBodySeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTBODY);
		mFontRubi = (TextFontRubiSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTRUBI);
		mFontInfo = (TextFontInfoSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTINFO);
		mSpaceW   = (TextSpaceWSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_SPACEW);
		mSpaceH   = (TextSpaceHSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_SPACEH);
		mMarginW  = (TextMarginWSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_MARGINW);
		mMarginH  = (TextMarginHSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_MARGINH);

		mIniScale = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_INISCALE);
		mInitView = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_INITVIEW);
		mViewRota = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_VIEWROTA);
		mPaper    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_PAPER);
		mPicSize  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_PICSIZE);

		mScrlRngW = (TextScrlRngWSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_SCRLRNGW);
		mScrlRngH = (TextScrlRngHSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TX_SCRLRNGH);

		mFontName = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_FONTNAME);
		mFontName.setTitle(mFontName.getTitle().toString().replace("[sdcard]", "[" + Environment.getExternalStorageDirectory().getAbsolutePath() + "]"));

		mTapPattern = (OperationPreference)getPreferenceScreen().findPreference(DEF.KEY_TAPPATTERN);

		mViewPt = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VIEWPT);
		mVolKey = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VOLKEY);

		mLastPage = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LASTPAGE);
		mPageSel = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TX_PAGESELECT);

		mTimeAndBattery = (TimeAndBatteryPreference) getPreferenceScreen().findPreference(DEF.KEY_TIMEANDBATTERY);


		mResources = getResources();

		String fontpath = DEF.getFontDirectory();
		CharSequence[] items;
		CharSequence[] values;
		// キャッシュ保存先

		File[] files = new File(fontpath).listFiles();
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
				String url = res.getString(R.string.url_text);	// 設定画面
				Intent intent;
				intent = new Intent(SetTextActivity.this, HelpActivity.class);
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

		mFontTop.setSummary(getFontTopSummary(sharedPreferences));		// フォントサイズ(px)
		mFontBody.setSummary(getFontBodySummary(sharedPreferences));	// フォントサイズ(px)
		mFontRubi.setSummary(getFontRubiSummary(sharedPreferences));	// フォントサイズ(px)
		mFontInfo.setSummary(getFontInfoSummary(sharedPreferences));	// フォントサイズ(px)
		mSpaceW.setSummary(getSpaceWSummary(sharedPreferences));		// 行間(px)
		mSpaceH.setSummary(getSpaceHSummary(sharedPreferences));		// 字間(px)
		mMarginW.setSummary(getMarginWSummary(sharedPreferences));		// 左右余白(px)
		mMarginH.setSummary(getMarginHSummary(sharedPreferences));		// 上下余白(px)

		mIniScale.setSummary(getIniScaleSummary(sharedPreferences));	// 初期拡大モード
		mInitView.setSummary(getInitViewSummary(sharedPreferences));	// 表示モード
		mViewRota.setSummary(getViewRotaSummary(sharedPreferences));	// イメージ画面の回転制御
		mPaper.setSummary(getPaperSummary(sharedPreferences));			// 用紙サイズ
		mPicSize.setSummary(getPicSizeSummary(sharedPreferences));		// 挿絵サイズ

		mScrlRngW.setSummary(getScrlRngWSummary(sharedPreferences));	// 音量等でのスクロール量(幅)
		mScrlRngH.setSummary(getScrlRngHSummary(sharedPreferences));	// 音量等でのスクロール量(高さ)

		mFontName.setSummary(getFontNameSummary(sharedPreferences));	// フォント名

		mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));	// 操作パターン

		mViewPt.setSummary(SetImageText.getViewPtSummary(mResources, sharedPreferences));		// イメージ画面の回転制御
		mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));		// Volキー動作
		mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));	// 確認メッセージ
		mPageSel.setSummary(SetImageText.getTxPageSelectSummary(mResources, sharedPreferences));		// ページ選択方法
		mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));	// 時刻と充電表示

	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_TX_INISCALE)){
			//
			mIniScale.setSummary(getIniScaleSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_INITVIEW)){
			//
			mInitView.setSummary(getInitViewSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_VIEWROTA)){
			//
			mViewRota.setSummary(getViewRotaSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_PAPER)){
			//
			mPaper.setSummary(getPaperSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_PICSIZE)){
			//
			mPicSize.setSummary(getPicSizeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTTOP)){
			// テキストのフォントサイズ
			mFontTop.setSummary(getFontTopSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTBODY)){
			// テキストのフォントサイズ
			mFontBody.setSummary(getFontBodySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTRUBI)){
			// テキストのフォントサイズ
			mFontRubi.setSummary(getFontRubiSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTINFO)){
			// テキストのフォントサイズ
			mFontInfo.setSummary(getFontInfoSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_SPACEW)){
			// テキストのフォントサイズ
			mSpaceW.setSummary(getSpaceWSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_SPACEH)){
			// テキストのフォントサイズ
			mSpaceH.setSummary(getSpaceHSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_MARGINW)){
			// テキストのフォントサイズ
			mMarginW.setSummary(getMarginWSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_MARGINH)){
			// テキストのフォントサイズ
			mMarginH.setSummary(getMarginHSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_SCRLRNGW)){
			// スクロール量
			mScrlRngW.setSummary(getScrlRngWSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_SCRLRNGH)){
			// スクロール量
			mScrlRngH.setSummary(getScrlRngHSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_FONTNAME)){
			// スクロール量
			mFontName.setSummary(getFontNameSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TAPPATTERN) || key.equals(DEF.KEY_TAPRATE)){
			// タップパターン
			mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_VIEWPT)){
			//
			mViewPt.setSummary(SetImageText.getViewPtSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_VOLKEY)){
			//
			mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_LASTPAGE)){
			//
			mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TX_PAGESELECT)){
			//
			mPageSel.setSummary(SetImageText.getTxPageSelectSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TIMEDISP) || key.equals(DEF.KEY_TIMEFORMAT) || key.equals(DEF.KEY_TIMEPOS) || key.equals(DEF.KEY_TIMESIZE) || key.equals(DEF.KEY_TIMECOLOR)){
			//
			mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));
		}
	}

	// 設定の読込
	public static int getPinScale(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences,DEF.KEY_PinchScaleText, "100");
		if( val < 0 || val > 250 ){
			val = 100;
		}
		return val;
	}

	public static int getViewRota(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_VIEWROTA, "0");
		return val;
	}

	public static int getIniScale(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_INISCALE, "2");
		return val;
	}

	public static int getInitView(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_INITVIEW, "2");
		return val;
	}

	public static int getPaper(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_PAPER, "0");
		return val;
	}

	public static int getPicSize(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_PICSIZE, "0");
		return val;
	}

	public static int getBkLight(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_BKLIGHT, "11");
		return val;
	}

	public static int getAscMode(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_ASCMODE, 2);
		return val;
	}

	public static String getFontName(SharedPreferences sharedPreferences){
		return sharedPreferences.getString(DEF.KEY_TX_FONTNAME, "");
	}

	public static int getFontTop(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_FONTTOP, DEF.DEFAULT_TX_FONTTOP);
		return num;
	}

	public static int getFontBody(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_FONTBODY, DEF.DEFAULT_TX_FONTBODY);
		return num;
	}

	public static int getFontRubi(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_FONTRUBI, DEF.DEFAULT_TX_FONTRUBI);
		return num;
	}

	public static int getFontInfo(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_FONTINFO, DEF.DEFAULT_TX_FONTINFO);
		return num;
	}

	public static int getSpaceW(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_SPACEW, DEF.DEFAULT_TX_SPACEW);
		return num;
	}

	public static int getSpaceH(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_TX_SPACEH, DEF.DEFAULT_TX_SPACEH);
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

	public static boolean getCenterMargin(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TX_CMARGIN, true);
		return flag;
	}

	public static boolean getCenterShadow(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TX_CSHADOW, false);
		return flag;
	}

	public static boolean getEffect(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TX_EFFECT, true);
		return flag;
	}

	public static int getScrlRngW(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_TX_SCRLRNGW, DEF.DEFAULT_TX_SCRLRNGW);
		return num;
	}

	public static int getScrlRngH(SharedPreferences sharedPreferences){
		int num;
		num = DEF.getInt(sharedPreferences, DEF.KEY_TX_SCRLRNGH, DEF.DEFAULT_TX_SCRLRNGH);
		return num;
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

	private String getIniScaleSummary(SharedPreferences sharedPreferences){
		int val = getIniScale(sharedPreferences);
		Resources res = getResources();
		return res.getString(ScaleName[val]);
	}

	private String getInitViewSummary(SharedPreferences sharedPreferences){
		int val = getInitView(sharedPreferences);
		Resources res = getResources();
		return res.getString(ViewName[val]);
	}

	private String getPaperSummary(SharedPreferences sharedPreferences){
		int val = getPaper(sharedPreferences);
		Resources res = getResources();
		return res.getString(PaperName[val]);
	}

	private String getPicSizeSummary(SharedPreferences sharedPreferences){
		int val = getPicSize(sharedPreferences);
		Resources res = getResources();
		return res.getString(PicSizeName[val]);
	}

	private String getAscModeSummary(SharedPreferences sharedPreferences){
		int val = getAscMode(sharedPreferences);
		Resources res = getResources();
		return res.getString(AscModeName[val]);
	}

	private String getFontNameSummary(SharedPreferences sharedPreferences){
		String val = getFontName(sharedPreferences);
		if (val != null && val.length() > 0) {
			return val;
		}
		Resources res = getResources();
		return res.getString(R.string.defaultFont);
	}

	private String getFontTopSummary(SharedPreferences sharedPreferences){
		int val = getFontTop(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontBodySummary(SharedPreferences sharedPreferences){
		int val = getFontBody(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontRubiSummary(SharedPreferences sharedPreferences){
		int val = getFontRubi(sharedPreferences);
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

	private String getSpaceWSummary(SharedPreferences sharedPreferences){
		int val = getSpaceW(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return	DEF.getTextSpaceStr(val, summ1);
	}

	private String getSpaceHSummary(SharedPreferences sharedPreferences){
		int val = getSpaceH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);

		return	DEF.getTextSpaceStr(val, summ1);
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

	private String getScrlRngWSummary(SharedPreferences sharedPreferences){
		int val = getScrlRngW(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.srngSumm1);
		String summ2 = res.getString(R.string.srngSumm2);

		return	DEF.getScrlRangeStr(val, summ1, summ2);
	}

	private String getScrlRngHSummary(SharedPreferences sharedPreferences){
		int val = getScrlRngH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.srngSumm1);
		String summ2 = res.getString(R.string.srngSumm2);

		return	DEF.getScrlRangeStr(val, summ1, summ2);
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
