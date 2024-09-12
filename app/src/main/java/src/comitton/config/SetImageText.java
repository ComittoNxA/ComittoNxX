package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.SharedPreferences;
import android.content.res.Resources;

public class SetImageText {
	public static final int ViewPtName[] =
		{ R.string.posi00		// 右上
		, R.string.posi01		// 左上
		, R.string.posi02		// 右下
		, R.string.posi03		// 左下
		, R.string.posi04 };	// 中央
	public static final int VolKeyName[] =
		{ R.string.volkey00		// 使用しない
		, R.string.volkey01		// VolUp:前/Down:次
		, R.string.volkey02 };	// VolUp:次/Down:前
	public static final String RateDisp[] =
		{ "(10%:90%)"
		, "(20%:80%)"
		, "(30%:70%)"
		, "(40%:60%)"
		, "(50%:50%)"
		, "(60%:40%)"
		, "(70%:30%)"
		, "(80%:20%)"
		, "(90%:10%)" };	// VolUp:次/Down:前
	public static final int LastPageName[] =
		{ R.string.lastpage00		// 画面を閉じる
		, R.string.lastpage01		// 確認ダイアログ
		, R.string.lastpage02 };	// 次のファイルへ移動
	public static final int PageSelectName[] =
		{ R.string.pageselect00		// フリック
		, R.string.pageselect01		// スライダー
		, R.string.pageselect02 };	// サムネイル

	// 設定の読込
	public static int getViewPt(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_VIEWPT, "0");
		if( val < 0 || val > 4 ){
			val = 0;
		}
		return val;
	}

	public static int getVolKey(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_VOLKEY, "1");
		if (val < 0 || val >= VolKeyName.length){
			val = 1;
		}
		return val;
	}

	public static int getLastPage(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_LASTPAGE, "1");
		if (val < 0 || val >= LastPageName.length){
			val = 1;
		}
		return val;
	}

	public static int getPageSelect(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_PAGESELECT, "2");
		if(val < 0 || val >= PageSelectName.length){
			val = 0;
		}
		return val;
	}

	public static int getTxPageSelect(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TX_PAGESELECT, "1");
		if(val < 0 || val >= PageSelectName.length - 1){
			val = 0;
		}
		return val;
	}

	public static boolean getPrevRev(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_PREVREV, DEF.DEFAULT_PREVREV);
		return flag;
	}

	public static boolean getChgPage(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CHGPAGE, DEF.DEFAULT_CHGPAGE);
		return flag;
	}

	public static boolean getChgFlick(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CHGFLICK, false);
		return flag;
	}

	public static boolean getVibFlag(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_VIBFLAG, false);
		return flag;
	}

	public static boolean getFlickEdge(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_FLICKEDGE, true);
		return flag;
	}

	public static boolean getTapScrl(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_TAPSCRL, false);
		return flag;
	}

	public static boolean getFlickPage(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_FLICKPAGE, true);
		return flag;
	}

	public static boolean getOldPageSel(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_OLDPAGESEL, false);
		return flag;
	}

	public static boolean getResumeOpen(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_RESUMEOPEN, false);
		return flag;
	}

	public static boolean getConfirmBack(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CONFIRMBACK, true);
		return flag;
	}

	public static boolean getSavePage(SharedPreferences sharedPreferences){
		boolean flag = DEF.getBoolean(sharedPreferences, DEF.KEY_SAVEPAGE, DEF.DEF_SAVEPAGE);
		return flag;
	}

	public static int getTapPattern(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TAPPATTERN, DEF.DEFAULT_TAPPATTERN);
		return val;
	}

	public static int getTapRate(SharedPreferences sharedPreferences){
		int val = DEF.getInt(sharedPreferences, DEF.KEY_TAPRATE, DEF.DEFAULT_TAPRATE);
		return val;
	}

	// 設定の読込(定義変更中)
	public static String getViewPtSummary(Resources res, SharedPreferences sharedPreferences){
		int val = getViewPt(sharedPreferences);
		return res.getString(ViewPtName[val]);
	}

	public static String getVolKeySummary(Resources res, SharedPreferences sharedPreferences){
		int val = getVolKey(sharedPreferences);
		return res.getString(VolKeyName[val]);
	}

	public static String getTapPatternSummary(Resources res, SharedPreferences sharedPreferences){
		int val1 = getTapPattern(sharedPreferences);
		int val2 = getTapRate(sharedPreferences);
		return res.getString(R.string.opPattern) + " " + (val1 + 1) + ", " + RateDisp[val2];
	}

	public static String getLastPageSummary(Resources res, SharedPreferences sharedPreferences){
		int val = getLastPage(sharedPreferences);
		return res.getString(LastPageName[val]);
	}

	public static String getPageSelectSummary(Resources res, SharedPreferences sharedPreferences){
		int val = getPageSelect(sharedPreferences);
		return res.getString(PageSelectName[val]);
	}

	public static String getTxPageSelectSummary(Resources res, SharedPreferences sharedPreferences){
		int val = getTxPageSelect(sharedPreferences);
		return res.getString(PageSelectName[val]);
	}

	public static boolean getChgPageKey(SharedPreferences sharedPreferences){
		boolean flag;
		flag =  DEF.getBoolean(sharedPreferences, DEF.KEY_CHGPAGEKEY, DEF.DEFAULT_CHGPAGEKEY);
		return flag;
	}
}
