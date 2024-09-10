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

public class SetImageActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference mViewRota;
	private ListPreference mFileSort;
	private ListPreference mIniScale;
	private ListPreference mQuality;
	private ListPreference mPageWay;
	private ListPreference mInitView;
	private ListPreference mAlgoMode;
	private ListPreference mZoomType;
	private ListPreference mScrlWay;
	private ListPreference mMgnCut;
	private ListPreference mMgnCutColor;
	private ListPreference mEffect;
	private ListPreference mViewPt;
	private ListPreference mVolKey;
	private ListPreference mLastPage;
	private ListPreference mPageSel;

	private OperationPreference mTapPattern;
	private PageNumberPreference mPageNumber;
	private TimeAndBatteryPreference mTimeAndBattery;

	public static final int FileSortName[] =
		{ R.string.fsort00		// ソートなし
		, R.string.fsort01		// ファイル名-昇順
		, R.string.fsort02 };	// ファイル名-降順
	public static final int ViewPtName[] =
		{ R.string.posi00		// 右上
		, R.string.posi01		// 左上
		, R.string.posi02		// 右下
		, R.string.posi03		// 左下
		, R.string.posi04 };	// 左下
	public static final int ScaleName[] =
		{ R.string.selsize00	// 元のサイズで表示
		, R.string.selsize01	// 幅に合わせて表示
		, R.string.selsize02	// 高さに合わせて表示
		, R.string.selsize03	// 全体を表示
		, R.string.selsize04	// 画面全体で表示
		, R.string.selsize05	// 画面全体で表示(見開き対応)
		, R.string.selsize06	// 幅に合わせて表示(見開き対応)
		, R.string.selsize07 };	// 全体を表示(見開き対応)
	public static final int PageWayName[] =
		{ R.string.pgway00		// 右から左
		, R.string.pgway01 };	// 左から右
	public static final int ViewName[] =
		{ R.string.selview00	// そのまま表示
		, R.string.selview01	// 見開き表示
		, R.string.selview02	// 単ページ表示
		, R.string.selview04 };	// 単ページ／見開き
	public static final int RotateName[] =
		{ R.string.rota00		// 回転あり
		, R.string.rota01		// 縦固定
		, R.string.rota02		// 横固定
		, R.string.rota03 };	// 縦固定(90°回転)
	public static final int LoupeName[] =
		{ R.string.loupe00		// 原寸x1.0
		, R.string.loupe01		// 原寸x2.0
		, R.string.loupe02		// 原寸x3.0
		, R.string.loupe03		// 表示x1.5
		, R.string.loupe04		// 表示x2.0
		, R.string.loupe05		// 表示x2.5
		, R.string.loupe06 };	// 表示x3.0
	public static final int AlgoModeName[] =
		{ R.string.selalgo00	// 最近傍補間
		, R.string.selalgo01	// 双一次補間
		, R.string.selalgo02	// 双一次補間(2Step)
		, R.string.selalgo03	// 双三次補間
		, R.string.selalgo04 };	// 双三次補間(2Step)
//		, R.string.selalgo05 };	// Lanczos3
	public static final int ImgRotaName[] =
		{ R.string.selrota00	// 回転無しで表示
		, R.string.selrota01	// 90°回転して表示
		, R.string.selrota02	// 180°回転して表示
		, R.string.selrota03 };	// 270°回転して表示
	public static final int VolKeyName[] =
		{ R.string.volkey00		// 使用しない
		, R.string.volkey01		// VolUp:前/Down:次
		, R.string.volkey02 };	// VolUp:次/Down:前
	public static final int ScrlWayName[] =
		{ R.string.scrlway00	// 横→縦
		, R.string.scrlway01 };	// 縦→横
	public static final int MgnCutName[] =
		{ R.string.mgncut00		// なし
		, R.string.mgncut01		// 弱
		, R.string.mgncut02		// 中
		, R.string.mgncut03		// 強
		, R.string.mgncut04		// 特上
		, R.string.mgncut05		// 最強
		, R.string.mgncut06 };	// 縦横比無視
	public static final int MgnCutColorName[] =
			{ R.string.mgncutcolor00		// 白と黒
			, R.string.mgncutcolor01 };		// 全ての色
	public static final int EffectName[] =
		{ R.string.effect00		// なし
		, R.string.effect01		// フリップ
		, R.string.effect02		// フェードイン
		, R.string.effect03 };	// スクロール
	public static final int PnumFormatName[] =
		{ R.string.pnumformat00		// page / total
		, R.string.pnumformat01 };	// page1-2 / total
	public static final int PnumPosName[] =
		{ R.string.pnumpos00	// 左上
		, R.string.pnumpos01	// 中央上
		, R.string.pnumpos02	// 右上
		, R.string.pnumpos03	// 左下
		, R.string.pnumpos04	// 中央下
		, R.string.pnumpos05 };	// 右下
	public static final int PnumColorName[] =
			{ R.string.pnumcolor00		// 白
		, R.string.pnumcolor01 };		// 黒
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

		addPreferencesFromResource(R.xml.image);
		mViewRota = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VIEWROTA);
		mFileSort = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_FILESORT);

		mIniScale  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_INISCALE);
		mPageWay   = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_PAGEWAY);
		mInitView  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_INITVIEW);

		mZoomType  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_ZOOMTYPE);

		mAlgoMode   = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_ALGOMODE);
		mScrlWay    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_SCRLWAY);

		mMgnCut     = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_MARGINCUT);
		mMgnCutColor = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_MARGINCUTCOLOR);
		mEffect     = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_EFFECTLIST);

		mTapPattern = (OperationPreference)getPreferenceScreen().findPreference(DEF.KEY_TAPPATTERN);
		mPageNumber = (PageNumberPreference)getPreferenceScreen().findPreference(DEF.KEY_PAGENUMBER);

		mTimeAndBattery = (TimeAndBatteryPreference) getPreferenceScreen().findPreference(DEF.KEY_TIMEANDBATTERY);

		mViewPt    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VIEWPT);
		mVolKey    = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_VOLKEY);

		mQuality  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_QUALITY);

		mLastPage = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LASTPAGE);
		mPageSel = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_PAGESELECT);

		mResources = getResources();

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_IMAGEHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_image);	// 設定画面
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

		mViewRota.setSummary(getViewRotaSummary(sharedPreferences));	// イメージ画面の回転制御
		mFileSort.setSummary(getFileSortSummary(sharedPreferences));	// 書庫内ファイルソート

		mIniScale.setSummary(getIniScaleSummary(sharedPreferences));	// 初期拡大モード
		mPageWay.setSummary(getPageWaySummary(sharedPreferences));		// ページめくり方向
		mInitView.setSummary(getInitViewSummary(sharedPreferences));	// 表示モード

		mZoomType.setSummary(getZoomTypeSummary(sharedPreferences));	// ルーペ拡大率

		mAlgoMode.setSummary(getAlgoModeSummary(sharedPreferences));	// 補間方式

		mScrlWay.setSummary(getScrlWaySummary(sharedPreferences));		// スクロール方向

		mMgnCut.setSummary(getMgnCutSummary(sharedPreferences));		// 余白削除
		mMgnCutColor.setSummary(getMgnCutColorSummary(sharedPreferences));		// 余白削除の色
		mEffect.setSummary(getEffectSummary(sharedPreferences));		// エフェクト

		mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));	// 操作パターン
		mPageNumber.setSummary(getPageNumberSummary(sharedPreferences));	// ページ表示

		mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));	// 時刻と充電表示

		mViewPt.setSummary(SetImageText.getViewPtSummary(mResources, sharedPreferences));		// イメージ画面の回転制御
		mVolKey.setSummary(SetImageText.getVolKeySummary(mResources, sharedPreferences));		// Volキー動作

		mLastPage.setSummary(SetImageText.getLastPageSummary(mResources, sharedPreferences));	// 最終ページでの確認
		mPageSel.setSummary(SetImageText.getPageSelectSummary(mResources, sharedPreferences));		// ページ選択方法
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(DEF.KEY_VIEWROTA)){
			//
			mViewRota.setSummary(getViewRotaSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FILESORT)){
			//
			mFileSort.setSummary(getFileSortSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_INISCALE)){
			//
			mIniScale.setSummary(getIniScaleSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_PAGEWAY)){
			//
			mPageWay.setSummary(getPageWaySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_INITVIEW)){
			//
			mInitView.setSummary(getInitViewSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_ZOOMTYPE)){
			//
			mZoomType.setSummary(getZoomTypeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_ALGOMODE)){
			//
			mAlgoMode.setSummary(getAlgoModeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_SCRLWAY)){
			//
			mScrlWay.setSummary(getScrlWaySummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MARGINCUT)){
			//
			mMgnCut.setSummary(getMgnCutSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MARGINCUTCOLOR)){
			//
			mMgnCutColor.setSummary(getMgnCutColorSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_EFFECTLIST)){
			//
			mEffect.setSummary(getEffectSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TAPPATTERN) || key.equals(DEF.KEY_TAPRATE)){
			//
			mTapPattern.setSummary(SetImageText.getTapPatternSummary(mResources, sharedPreferences));
		}
		else if(key.equals(DEF.KEY_PNUMDISP) || key.equals(DEF.KEY_PNUMFORMAT) || key.equals(DEF.KEY_PNUMPOS) || key.equals(DEF.KEY_PNUMSIZE) || key.equals(DEF.KEY_PNUMCOLOR)){
			//
			mPageNumber.setSummary(getPageNumberSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TIMEDISP) || key.equals(DEF.KEY_TIMEFORMAT) || key.equals(DEF.KEY_TIMEPOS) || key.equals(DEF.KEY_TIMESIZE) || key.equals(DEF.KEY_TIMECOLOR)){
			//
			mTimeAndBattery.setSummary(getTimeSummary(sharedPreferences));
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
		else if(key.equals(DEF.KEY_PAGESELECT)){
			//
			mPageSel.setSummary(SetImageText.getPageSelectSummary(mResources, sharedPreferences));
		}
	}

	// 設定の読込
	public static int getViewRota(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_VIEWROTA, "0");
		if( val < 0 || val > 3 ){
			val = 0;
		}
		return val;
	}

	public static int getFileSort(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FILESORT, "1");
		if( val < 0 || val > 2 ){
			val = 0;
		}
		return val;
	}

	public static int getIniScale(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_INISCALE, "5");
		if( val < 0 || val >= ScaleName.length ){
			val = 0;
		}
		return val;
	}

	public static int getPageWay(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_PAGEWAY, "0");
		if( val < 0 || val > 2 ){
			val = 0;
		}
		return val;
	}

	public static int getInitView(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_INITVIEW, "1");
		if(val < 0 || val >= ViewName.length){
			val = 1;
		}
		return val;
	}

	public static int getZoomType(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_ZOOMTYPE, "4");
		if (val < 0 || val >= LoupeName.length){
			val = 0;
		}
		return val;
	}

	public static int getAlgoMode(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_ALGOMODE, "2");
		if (val < 0 || val >= AlgoModeName.length){
			val = 2;
		}
		return val;
	}

	public static int getScrlWay(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SCRLWAY, "0");
		if (val < 0 || val >= ScrlWayName.length){
			val = 0;
		}
		return val;
	}

	public static int getEffect(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_EFFECTLIST, "1");
		if( val < 0 || val > EffectName.length){
			val = 1;
		}
		return val;
	}

	public static int getMgnCut(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_MARGINCUT, "0");
		if (val < 0 || val >= MgnCutName.length){
			val = 0;
		}
		return val;
	}

	public static int getMgnCutColor(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_MARGINCUTCOLOR, "0");
		if (val < 0 || val >= MgnCutColorName.length){
			val = 0;
		}
		return val;
	}

	public static int getBright(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_BRIGHT, "0");
		return val;
	}

	public static int getGamma(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_GAMMA, "0");
		return val;
	}

	public static int getBkLight(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_BKLIGHT, "11");
		return val;
	}

	public static int getPnumFormat(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_PNUMFORMAT, DEF.DEFAULT_PNUMFORMAT);
		if( val < 0 || val >= PnumFormatName.length){
			val = 1;
		}
		return val;
	}

	public static int getPnumPos(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_PNUMPOS, DEF.DEFAULT_PNUMPOS);
		if( val < 0 || val >= PnumPosName.length){
			val = 5;
		}
		return val;
	}

	public static int getPnumSize(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_PNUMSIZE, DEF.DEFAULT_PNUMSIZE);
		return val;
	}

	public static boolean getPnumDisp(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_PNUMDISP, DEF.DEFAULT_PNUMDISP);
		return flag;
	}
	
	public static int getPnumColor(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_PNUMCOLOR, DEF.DEFAULT_PNUMCOLOR);
		if( val < 0 || val >= PnumColorName.length){
			val = 1;
		}
		return val;
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
		if( val < 0 || val >= PnumPosName.length){
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
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TIMEDISP, DEF.DEFAULT_TIMEDISP);
		return flag;
	}

	public static int getTimeColor(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TIMECOLOR, DEF.DEFAULT_TIMECOLOR);
		if( val < 0 || val >= PnumColorName.length){
			val = 1;
		}
		return val;
	}

	public static int getSharpen(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_SHARPEN, "0");
		return val;
	}

	public static boolean getGray(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_GRAY, false);
		return flag;
	}

	public static boolean getInvert(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_INVERT, false);
		return flag;
	}

	public static boolean getMoire(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_MOIRE, false);
		return flag;
	}

	public static boolean getTopSingle(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TOPSINGLE, false);
		return flag;
	}

	public static boolean getNotice(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_NOTICE, true);
		return flag;
	}

	public static boolean getNoSleep(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_NOSLEEP, false);
		return flag;
	}

	public static boolean getFitDual(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_FITDUAL, true);
		return flag;
	}

	public static boolean getCenterMargin(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CMARGIN, true);
		return flag;
	}

	public static boolean getCenterShadow(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CSHADOW, false);
		return flag;
	}

	public static boolean getNoExpand(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_NOEXPAND, true);
		return flag;
	}

	public static boolean getScrlNext(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_SCRLNEXT, false);
		return flag;
	}

	public static boolean getViewNext(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_VIEWNEXT, false);
		return flag;
	}

	public static boolean getNextFilter(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_NEXTFILTER, true);
		return flag;
	}

	//	public static boolean getEffect(SharedPreferences sharedPreferences){
//		boolean flag;
//		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EFFECT, true);
//		return flag;
//	}

	public static boolean getDelShare(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_DELSHARE, true);
		return flag;
	}

	// 設定の読込(定義変更中)
	private String getViewRotaSummary(SharedPreferences sharedPreferences){
		int val = getViewRota(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateName[val]);
	}

	private String getFileSortSummary(SharedPreferences sharedPreferences){
		int val = getFileSort(sharedPreferences);
		Resources res = getResources();
		return res.getString(FileSortName[val]);
	}

	private String getIniScaleSummary(SharedPreferences sharedPreferences){
		int val = getIniScale(sharedPreferences);
		Resources res = getResources();
		return res.getString(ScaleName[val]);
	}

	private String getPageWaySummary(SharedPreferences sharedPreferences){
		int val = getPageWay(sharedPreferences);
		Resources res = getResources();
		return res.getString(PageWayName[val]);
	}

	private String getInitViewSummary(SharedPreferences sharedPreferences){
		int val = getInitView(sharedPreferences);
		Resources res = getResources();
		return res.getString(ViewName[val]);
	}

	private String getZoomTypeSummary(SharedPreferences sharedPreferences){
		int val = getZoomType(sharedPreferences);
		Resources res = getResources();
		return res.getString(LoupeName[val]);
	}

	private String getAlgoModeSummary(SharedPreferences sharedPreferences){
		int val = getAlgoMode(sharedPreferences);
		Resources res = getResources();
		return res.getString(AlgoModeName[val]);
	}

	private String getScrlWaySummary(SharedPreferences sharedPreferences){
		int val = getScrlWay(sharedPreferences);
		Resources res = getResources();
		return res.getString(ScrlWayName[val]);
	}

	private String getMgnCutSummary(SharedPreferences sharedPreferences){
		int val = getMgnCut(sharedPreferences);
		Resources res = getResources();
		return res.getString(MgnCutName[val]);
	}

	private String getMgnCutColorSummary(SharedPreferences sharedPreferences){
		int val = getMgnCutColor(sharedPreferences);
		Resources res = getResources();
		String ret = res.getString(MgnCutColorName[val]);
		return ret;
	}

	private String getEffectSummary(SharedPreferences sharedPreferences){
		int val = getEffect(sharedPreferences);
		Resources res = getResources();
		return res.getString(EffectName[val]);
	}

	private String getPageNumberSummary(SharedPreferences sharedPreferences){
		boolean disp = getPnumDisp(sharedPreferences);
		int format = getPnumFormat(sharedPreferences);
		int pos = getPnumPos(sharedPreferences);
		int size = getPnumSize(sharedPreferences);
		int color = getPnumColor(sharedPreferences);
		Resources res = getResources();

		String summ;
		if (disp) {
			summ = res.getString(PnumFormatName[format])
					+ ", " + res.getString(PnumPosName[pos])
					+ ", " + DEF.getPnumSizeStr(size, res.getString(R.string.unitSumm1))
					+ ", " + res.getString(PnumColorName[color]);
		}
		else {
			summ = res.getString(R.string.pnumnodisp);
		}
		return summ;
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
					+ ", " + res.getString(PnumPosName[pos])
					+ ", " + DEF.getPnumSizeStr(size, res.getString(R.string.unitSumm1))
					+ ", " + res.getString(PnumColorName[color]);
		}
		else {
			summ = res.getString(R.string.pnumnodisp);
		}
		return summ;
	}
}
