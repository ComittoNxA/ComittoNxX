package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SetFileListActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference mListRota;
	private ListPreference mListSort;
	private ListPreference mBackMode;

	private ListPreference mMaxLines;
	private ListPreference mFileDelMenu;
	private ListPreference mFileRenMenu;

	private FontTitleSeekbar  mFontTitle;
	private FontMainSeekbar   mFontMain;
	private FontSubSeekbar    mFontSub;
	private FontTileSeekbar   mFontTile;
	private ItemMarginSeekbar mItemMrgn;

	private ListPreference mThumbCache;

	private ToolbarSeekbar mToolbarSeek;
	private ListThumbSeekbar mListThumbSeek;

	private ThumbnailPreference mThumbnail;

 	public static final int ListSortName[] =
		{ R.string.lsort00		// ソートなし
		, R.string.lsort01		// ファイル名順(ディレクトリ混在)
		, R.string.lsort02		// ファイル名順(ディレクトリ分離)
		, R.string.lsort03		// 新しい順(ディレクトリ混在)
		, R.string.lsort04		// 新しい順(ディレクトリ分離)
		, R.string.lsort05		// 古い順(ディレクトリ混在)
		, R.string.lsort06 };	// 古い順(ディレクトリ分離)
	public static final int FileSortName[] =
		{ R.string.fsort00		// ソートなし
		, R.string.fsort01		// ファイル名-昇順
		, R.string.fsort02 };	// ファイル名-降順
	public static final int RotateName[] =
		{ R.string.rota00		// 回転あり
		, R.string.rota01		// 縦固定
		, R.string.rota02 };	// 横固定
	public static final int BackModeName[] =
		{ R.string.bkmode00		// アプリ終了
		, R.string.bkmode01		// 親ディレクトリ
		, R.string.bkmode02 };	// 遷移を戻る
	public static final int ShowMenu[] =
		{ R.string.showmenu00		// 表示しない
		, R.string.showmenu01		// 表示する
		, R.string.showmenu02		// ローカルのみ表示
		, R.string.showmenu03 };	// サーバのみ表示
	public static final int ThumCacheName[] =
		{ R.string.thumbcache00		// 保持しない
		, R.string.thumbcache01		// 100まで
		, R.string.thumbcache02		// 500まで
		, R.string.thumbcache03		// 1000まで
		, R.string.thumbcache04 };	// 手動で削除

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.filelist);
		mListRota  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LISTROTA);
		mListSort  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_LISTSORT);
		mBackMode  = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_BACKMODE);
		mMaxLines = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_MAX_LINES);
		mFileDelMenu = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_FILEDELMENU);
		mFileRenMenu = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_FILERENMENU);
		mFontTitle = (FontTitleSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTTITLE);
		mFontMain  = (FontMainSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTMAIN);
		mFontSub   = (FontSubSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTSUB);
		mFontTile  = (FontTileSeekbar)getPreferenceScreen().findPreference(DEF.KEY_FONTTILE);
		mItemMrgn  = (ItemMarginSeekbar)getPreferenceScreen().findPreference(DEF.KEY_ITEMMRGN);
		mThumbnail = (ThumbnailPreference)getPreferenceScreen().findPreference(DEF.KEY_THUMBSEEK);
		mThumbCache = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_THUMBCACHE);
		mToolbarSeek = (ToolbarSeekbar)getPreferenceScreen().findPreference(DEF.KEY_TOOLBARSEEK);
		mListThumbSeek = (ListThumbSeekbar)getPreferenceScreen().findPreference(DEF.KEY_LISTTHUMBSEEK);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_FILEHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// Activityの遷移
				Resources res = getResources();
				String url = res.getString(R.string.url_filelist);	// 設定画面
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mListRota.setSummary(getListRotaSummary(sharedPreferences));	// ファイル選択画面の回転制御
		mListSort.setSummary(getListSortSummary(sharedPreferences));	// ソート
		mBackMode.setSummary(getBackModeSummary(sharedPreferences));	// 戻るボタン動作
		mMaxLines.setSummary(getMaxLinesSummary(sharedPreferences));		// ファイル名の行数
		mFileDelMenu.setSummary(getFileDelMenuSummary(sharedPreferences));		// 削除メニュー表示
		mFileRenMenu.setSummary(getFileRenMenuSummary(sharedPreferences));		// 名前変更メニュー表示
		mFontTitle.setSummary(getFontTitleSummary(sharedPreferences));	// フォントサイズ(px)
		mFontMain.setSummary(getFontMainSummary(sharedPreferences));	// フォントサイズ(px)
		mFontSub.setSummary(getFontSubSummary(sharedPreferences));		// フォントサイズ(px)
		mFontTile.setSummary(getFontTileSummary(sharedPreferences));	// フォントサイズ(px)
		mItemMrgn.setSummary(getItemMarginSummary(sharedPreferences));	// 余白サイズ
		mThumbnail.setSummary(getThumbnailSummary(sharedPreferences));	// サムネイルサイズ
		mThumbCache.setSummary(getThumbCacheSummary(sharedPreferences));	// サムネイルキャッシュ保持数
		mToolbarSeek.setSummary(getToolbarSeekSummary(sharedPreferences));		// ツールバー表示
		mListThumbSeek.setSummary(getListThumbSeekSummary(sharedPreferences));		// リストサムネイルサイズ表示
}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equals(DEF.KEY_LISTROTA)){
			//
			mListRota.setSummary(getListRotaSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_LISTSORT)){
			//
			mListSort.setSummary(getListSortSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FONTTITLE)){
			// テキストのフォントサイズ
			mFontTitle.setSummary(getFontTitleSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FONTMAIN)){
			// テキストのフォントサイズ
			mFontMain.setSummary(getFontMainSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FONTSUB)){
			// サマリのフォントサイズ
			mFontSub.setSummary(getFontSubSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FONTTILE)){
			// テキストのフォントサイズ
			mFontTile.setSummary(getFontTileSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_ITEMMRGN)){
			// 余白サイズ
			mItemMrgn.setSummary(getItemMarginSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_BACKMODE)){
			//
			mBackMode.setSummary(getBackModeSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_THUMBSIZEW) || key.equals(DEF.KEY_THUMBSIZEH)){
			// サムネイルサイズ
			mThumbnail.setSummary(getThumbnailSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_THUMBCACHE)){
			//
			mThumbCache.setSummary(getThumbCacheSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_TOOLBARSEEK)){
			//
			mToolbarSeek.setSummary(getToolbarSeekSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_LISTTHUMBSEEK)){
			//
			mListThumbSeek.setSummary(getListThumbSeekSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_MAX_LINES)){
			//
			mMaxLines.setSummary(getMaxLinesSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FILEDELMENU)){
			//
			mFileDelMenu.setSummary(getFileDelMenuSummary(sharedPreferences));
		}
		else if(key.equals(DEF.KEY_FILERENMENU)){
			//
			mFileRenMenu.setSummary(getFileRenMenuSummary(sharedPreferences));
		}
	}

	// 設定の読込
	// フォントサイズ(px)
	public static int getListRota(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_LISTROTA, "0");
		if( val < 0 || val > 2 ){
			val = 0;
		}
		return val;
	}

	public static int getListSort(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_LISTSORT, "2");
		if(val < 0 || val > ListSortName.length){
			val = 0;
		}
		return val;
	}

	public static int getMaxLines(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_MAX_LINES, "2");
		if(val <= 0 || val > 10){
			val = 2;
		}
		return val;
	}

	public static int getFileDelMenu(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FILEDELMENU, "2");
		if(val < 0 || val >= ShowMenu.length){
			val = 2;
		}
		return val;
	}

	public static int getFileRenMenu(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_FILERENMENU, "0");
		if(val < 0 || val >= ShowMenu.length){
			val = 0;
		}
		return val;
	}

	public static int getFontTitle(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTTITLE, DEF.DEFAULT_FONTTITLE);
		return num;
	}

	public static int getFontMain(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTMAIN, DEF.DEFAULT_FONTMAIN);
		return num;
	}

	public static int getFontSub(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTSUB, DEF.DEFAULT_FONTSUB);
		return num;
	}

	public static int getFontTile(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_FONTTILE, DEF.DEFAULT_FONTTILE);
		return num;
	}

	public static int getItemMargin(SharedPreferences sharedPreferences){
		int num =  DEF.getInt(sharedPreferences, DEF.KEY_ITEMMRGN, DEF.DEFAULT_ITEMMARGIN);
		return num;
	}

	public static int getThumbSizeW(SharedPreferences sharedPreferences){
		int num = DEF.getInt(sharedPreferences, DEF.KEY_THUMBSIZEW, DEF.DEFAULT_THUMBSIZEW);
		return num;
	}

	public static int getThumbSizeH(SharedPreferences sharedPreferences){
		int num = DEF.getInt(sharedPreferences, DEF.KEY_THUMBSIZEH, DEF.DEFAULT_THUMBSIZEH);
		return num;
	}

	public static int getToolbarSize(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TOOLBARSEEK, DEF.DEFAULT_TOOLBARSEEK);
		return val;
	}
	public static int getListThumbSizeH(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_LISTTHUMBSEEK, DEF.DEFAULT_LISTTHUMBSIZEH);
		return val;
	}


//	public static int getFontSize(SharedPreferences sharedPreferences){
//		int val = DEF.getInt(sharedPreferences, DEF.KEY_FONTSIZE, "2"));
//		if( val < 0 || val > 4 ){
//			val = 2;
//		}
//		return val;
//	}

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

	public static int getViewPt(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_VIEWPT, "0");
		if( val < 0 || val > 4 ){
			val = 0;
		}
		return val;
	}

	public static int getIniScale(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_INISCALE, DEF.DEFAULT_INISCHALE);
		if( val < 0 || val > 7 ){
			val = 5;
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
		int val = DEF.getInt(sharedPreferences, DEF.KEY_INITVIEW, DEF.DEFAULT_INITVIEW);
		if( val < 0 || val > 3 ){
			val = 0;
		}
		return val;
	}

	public static int getBackMode(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_BACKMODE, "0");
		if (val < 0 || val > BackModeName.length){
			val = 0;
		}
		return val;
	}

	public static int getThumbCache(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_THUMBCACHE, "2");
		if (val < 0 || val >= ThumCacheName.length){
			val = 2;
		}
		return val;
	}

	public static int getThumbCacheNum(SharedPreferences sharedPreferences){
		int val = getThumbCache(sharedPreferences);
		int num = 500;
		switch (val) {
			case 0:
				num = 0;
				break;
			case 1:
				num = 100;
				break;
			case 2:
				num = 500;
				break;
			case 3:
				num = 1000;
				break;
			case 4:
				num = -1;
				break;
		}
		return num;
	}

	public static boolean getTapExpand(SharedPreferences sharedPreferences){
		boolean flag;
		flag = DEF.getBoolean(sharedPreferences, DEF.KEY_TAPEXPAND, false);
		return flag;
	}

	public static boolean getThumbnail(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_THUMBNAIL, false);
		return flag;
	}

	public static boolean getCrearTop(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CLEARTOP, false);
		return flag;
	}

	public static boolean getExtension(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_EXTENSION, true);
		return flag;
	}

	public static boolean getSplitFilename(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_SPLIT_FILENAME, true);
		return flag;
	}

	public static boolean getThumbnailSort(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_THUMBSORT, false);
		return flag;
	}

	public static boolean getParentMove(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_PARENTMOVE, true);
		return flag;
	}

	public static boolean getShowToolbar(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_SHOWTOOLBAR, true);
		return flag;
	}

	public static boolean getToolbarName(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TOOLBARNAME, true);
		return flag;
	}

	public static boolean getThumbnailTap(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_THUMBNAILTAP, true);
		return flag;
	}

	// 設定を保存
	public static void setThumbnail(SharedPreferences sharedPreferences, boolean value){
		Editor ed = sharedPreferences.edit();
		ed.putBoolean(DEF.KEY_THUMBNAIL, value);
		ed.commit();
	}

	// 設定の読込(定義変更中)
	private String getListRotaSummary(SharedPreferences sharedPreferences){
		int val = getListRota(sharedPreferences);
		Resources res = getResources();
		return res.getString(RotateName[val]);
	}

	private String getListSortSummary(SharedPreferences sharedPreferences){
		int val = getListSort(sharedPreferences);
		Resources res = getResources();
		return res.getString(ListSortName[val]);
	}

	private String getMaxLinesSummary(SharedPreferences sharedPreferences){
		int val = getMaxLines(sharedPreferences);
		return String.valueOf(val);
	}

	private String getFileDelMenuSummary(SharedPreferences sharedPreferences){
		int val = getFileDelMenu(sharedPreferences);
		Resources res = getResources();
		return res.getString(ShowMenu[val]);
	}

	private String getFileRenMenuSummary(SharedPreferences sharedPreferences){
		int val = getFileRenMenu(sharedPreferences);
		Resources res = getResources();
		return res.getString(ShowMenu[val]);
	}

	private String getFontTitleSummary(SharedPreferences sharedPreferences){
		int val = getFontTitle(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontMainSummary(SharedPreferences sharedPreferences){
		int val = getFontMain(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontSubSummary(SharedPreferences sharedPreferences){
		int val = getFontSub(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getFontTileSummary(SharedPreferences sharedPreferences){
		int val = getFontTile(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getFontSpStr(val, summ1);
	}

	private String getItemMarginSummary(SharedPreferences sharedPreferences){
		int val = getItemMargin(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);

		return	DEF.getMarginSpStr(val, summ1);
	}

	private String getBackModeSummary(SharedPreferences sharedPreferences){
		int val = getBackMode(sharedPreferences);
		Resources res = getResources();
		return res.getString(BackModeName[val]);
	}

	private String getThumbnailSummary(SharedPreferences sharedPreferences){
		int w = getThumbSizeW(sharedPreferences);
		int h = getThumbSizeH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);
		return DEF.getThumbnailStr(w, h, summ1);
	}

	private String getToolbarSeekSummary(SharedPreferences sharedPreferences){
		int size = getToolbarSize(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.unitSumm1);
		return DEF.getToolbarSeekStr(size, summ1);
	}

	private String getListThumbSeekSummary(SharedPreferences sharedPreferences){
		int size = getListThumbSizeH(sharedPreferences);
		Resources res = getResources();
		String summ1 = res.getString(R.string.rangeSumm1);
		return DEF.getListThumbSeekStr(size, summ1);
	}

	private String getThumbCacheSummary(SharedPreferences sharedPreferences){
		int val = getThumbCache(sharedPreferences);
		Resources res = getResources();
		return res.getString(ThumCacheName[val]);
	}
}
