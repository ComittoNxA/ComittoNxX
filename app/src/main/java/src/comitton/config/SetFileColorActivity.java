package src.comitton.config;

import src.comitton.config.color.ColorAftSetting;
import src.comitton.config.color.ColorBakSetting;
import src.comitton.config.color.ColorBefSetting;
import src.comitton.config.color.ColorCurSetting;
import src.comitton.config.color.ColorDirSetting;
import src.comitton.config.color.ColorImgSetting;
import src.comitton.config.color.ColorInfSetting;
import src.comitton.config.color.ColorMrkSetting;
import src.comitton.config.color.ColorNowSetting;
import src.comitton.config.color.ColorTibSetting;
import src.comitton.config.color.ColorTitSetting;
import src.comitton.config.color.ColorTlbSetting;
import src.comitton.config.color.ColorTldSetting;
import src.comitton.config.color.ColorTxtSetting;
import src.comitton.helpview.HelpActivity;
import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SetFileColorActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference mPreset;

	private PreferenceScreen mCustomUpdate;
//	private ListPreference mTitleColor;
//	private ListPreference mToolbarColor;

	private ColorTxtSetting mTxtColor;
	private ColorDirSetting mDirColor;
	private ColorBefSetting mBefColor;
	private ColorNowSetting mNowColor;
	private ColorAftSetting mAftColor;
	private ColorImgSetting mImgColor;
	private ColorInfSetting mInfColor;
	private ColorMrkSetting mMrkColor;
	private ColorBakSetting mBakColor;
	private ColorCurSetting mCurColor;

	private ColorTitSetting mTitColor;
	private ColorTibSetting mTibColor;
	private ColorTldSetting mTldColor;	// ツールバーの描画
	private ColorTlbSetting mTlbColor;	// ツールバーの背景

	SharedPreferences mSharedPreferences;

	static final int mPresetName[] =
	{ R.string.preset00		// カスタム
	, R.string.preset01		// 標準（黒）
	, R.string.preset02		// 標準（白）
	, R.string.preset03		// 桜
	, R.string.preset04		// 藍
	, R.string.preset05		// 若葉
	, R.string.preset06		// 蜜柑
	, R.string.preset07 };	// 墨

	static final int PRESET_TXT = 0;
	static final int PRESET_DIR = 1;
	static final int PRESET_BEF = 2;
	static final int PRESET_NOW = 3;
	static final int PRESET_AFT = 4;
	static final int PRESET_IMG = 5;
	static final int PRESET_INF = 6;
	static final int PRESET_MRK = 7;
	static final int PRESET_BAK = 8;
	static final int PRESET_CUR = 9;
	static final int PRESET_TIT = 10;
	static final int PRESET_TIB = 11;
	static final int PRESET_TLD = 12;
	static final int PRESET_TLB = 13;

	static final int mPresetColor[][] =
	//   ----TXT----, ---DIR----, ---BEF----, ---NOW----, ---AFT----, ---IMG----, ---INF----, ---MRK----, ---BAK----, ---Cur----, ---TIT----, ---TIB----, ---TLD----, ---TLB----
	{
/*標*/	{ 0xFFFFFFFF, 0xFF00FF00, 0xFFFFFFFF, 0xFF00FFFF, 0xFF808080, 0xFFFFFF00, 0xFF9F9F9F, 0xFFFFFF00, 0xFF000000, 0xFF0080FF, 0xFFFFFFFF, 0xFF202020, 0xFF000000, 0xFF808080 },
/*黒*/ 	{ 0xFFFFFFFF, 0xFF00FF00, 0xFFFFFFFF, 0xFF00FFFF, 0xFF808080, 0xFFFFFF00, 0xFF9F9F9F, 0xFFC00000, 0xFF000000, 0xFF0040C0, 0xFFFFFFFF, 0xFF202020, 0xFF404040, 0xFFA0A0A0 },
/*白*/	{ 0xFF000000, 0xFF008039, 0xFF000000, 0xFF1C5593, 0xFF808080, 0xFFB17A25, 0xFF3B373A, 0xFFFFFF40, 0xFFF0F0F0, 0xFF00C0FF, 0xFF232323, 0xFF8A8A8A, 0xFFD0D0D0, 0xFF606060 },
/*桜*/	{ 0xFFDF1E6B, 0xFF5C9A00, 0xFFDF1E6B, 0xFFE99697, 0xFF968E8F, 0xFF8A67A7, 0xFFA3B0C1, 0xFFFFFF7E, 0xFFFFE5E9, 0xFF89CFFF, 0xFFFFEDF1, 0xFFC77E90, 0xFF64263A, 0xFFE7AABC },
/*藍*/	{ 0xFF00217A, 0xFF2D5155, 0xFF00237B, 0xFFD93D4A, 0xFF968E8F, 0xFF4B418C, 0xFF393FD3, 0xFFF1FF82, 0xFFE9EFFF, 0xFFF5B5CE, 0xFFFAF2FF, 0xFF000B4B, 0xFF111656, 0xFF889EB0 },
/*葉*/	{ 0xFF10832A, 0xFF354E83, 0xFF10832A, 0xFF16A686, 0xFF82A18A, 0xFF92B410, 0xFF228034, 0xFFFDFB9A, 0xFFF2FFF5, 0xFFA2DAFF, 0xFFFAFFF2, 0xFF165826, 0xFF153A10, 0xFFA2B99F }, // O
/*橙*/	{ 0xFF8E6216, 0xFFA98A34, 0xFF8E6216, 0xFFCC9E43, 0xFFA7966D, 0xFFB07028, 0xFF88661B, 0xFFFFCEE3, 0xFFFFFAF2, 0xFF8DFFBB, 0xFFFFFEF3, 0xFFC36214, 0xFF663B10, 0xFFDDA268 }, // O
/*墨*/	{ 0xFFDEDEE1, 0xFF2DA6C8, 0xFFDEDED1, 0xFF76AFC3, 0xFF4A4A4B, 0xFFC5C123, 0xFF9F9F9F, 0xFF004D24, 0xFF282828, 0xFF1F4594, 0xFFFFFFFF, 0xFF202020, 0xFF3A3A3F, 0xFF6F767C }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.filecolor);

		mPreset = (ListPreference) getPreferenceScreen().findPreference(DEF.KEY_PRESET);
//		mTitleColor = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TITLECLR);
//		mToolbarColor = (ListPreference)getPreferenceScreen().findPreference(DEF.KEY_TOOLBCLR);

		mTxtColor = (ColorTxtSetting) getPreferenceScreen().findPreference(DEF.KEY_TXTRGB);
		mDirColor = (ColorDirSetting) getPreferenceScreen().findPreference(DEF.KEY_DIRRGB);
		mBefColor = (ColorBefSetting) getPreferenceScreen().findPreference(DEF.KEY_BEFRGB);
		mNowColor = (ColorNowSetting) getPreferenceScreen().findPreference(DEF.KEY_NOWRGB);
		mAftColor = (ColorAftSetting) getPreferenceScreen().findPreference(DEF.KEY_AFTRGB);
		mImgColor = (ColorImgSetting) getPreferenceScreen().findPreference(DEF.KEY_IMGRGB);
		mInfColor = (ColorInfSetting) getPreferenceScreen().findPreference(DEF.KEY_INFRGB);
		mMrkColor = (ColorMrkSetting) getPreferenceScreen().findPreference(DEF.KEY_MRKRGB);
		mBakColor = (ColorBakSetting) getPreferenceScreen().findPreference(DEF.KEY_BAKRGB);
		mCurColor = (ColorCurSetting) getPreferenceScreen().findPreference(DEF.KEY_CURRGB);

		mTitColor = (ColorTitSetting) getPreferenceScreen().findPreference(DEF.KEY_TITRGB);
		mTibColor = (ColorTibSetting) getPreferenceScreen().findPreference(DEF.KEY_TIBRGB);
		mTldColor = (ColorTldSetting) getPreferenceScreen().findPreference(DEF.KEY_TLDRGB);
		mTlbColor = (ColorTlbSetting) getPreferenceScreen().findPreference(DEF.KEY_TLBRGB);

		// 項目選択
		PreferenceScreen onlineHelp = (PreferenceScreen) findPreference(DEF.KEY_FCLRHELP);
		onlineHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// ヘルプを起動
				Resources res = getResources();
				String url = res.getString(R.string.url_filecolor);	// 設定画面
				Intent intent;
				intent = new Intent(SetFileColorActivity.this, HelpActivity.class);
				intent.putExtra("Url", url);
				startActivity(intent);
				return true;
			}
		});

		mCustomUpdate = (PreferenceScreen) findPreference(DEF.KEY_CSTUPDATE);
		mCustomUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// プリセットを反映
				int index = getPreset(mSharedPreferences);
				if (index > 0 || index < mPresetColor.length) {
					String newKeys[] = { DEF.KEY_TXTRGB, DEF.KEY_DIRRGB, DEF.KEY_BEFRGB, DEF.KEY_NOWRGB, DEF.KEY_AFTRGB, DEF.KEY_IMGRGB, DEF.KEY_INFRGB, DEF.KEY_MRKRGB, DEF.KEY_BAKRGB, DEF.KEY_CURRGB, DEF.KEY_TITRGB, DEF.KEY_TIBRGB, DEF.KEY_TLDRGB, DEF.KEY_TLBRGB };

					Editor ed = mSharedPreferences.edit();
					for (int i = 0 ; i < newKeys.length ; i ++) {
						ed.putInt(newKeys[i], mPresetColor[index][i]);
					}
					ed.commit();
					updateSummarys();
				}
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences = getPreferenceScreen().getSharedPreferences();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		// 色設定
		int val = getPreset(mSharedPreferences);
		mPreset.setSummary(getPresetSummary(val)); // プリセット
		setEnableViews(val == 0);

//		mTitleColor.setSummary(getTitleColorSummary(getTitleColor(mSharedPreferences))); // タイトル
//		mToolbarColor.setSummary(getToolbarColorSummary(getToolbarColor(mSharedPreferences))); // ツールバー
		updateSummarys();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// 色
		if (key.equals(DEF.KEY_PRESET)) {
			// プリセット選択
			int val = getPreset(sharedPreferences);
			mPreset.setSummary(getPresetSummary(val));
			setEnableViews(val == 0);
		}
//		if(key.equals(DEF.KEY_TITLECLR)){
//			//
//			mTitleColor.setSummary(getTitleColorSummary(getTitleColor(sharedPreferences)));
//		}
//		else if(key.equals(DEF.KEY_TOOLBCLR)){
//			//
//			mToolbarColor.setSummary(getToolbarColorSummary(getToolbarColor(sharedPreferences)));
//		}
		else if (key.equals(DEF.KEY_TXTRGB)) {
			//
			mTxtColor.setSummary(getColorSummary(getTxtColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_DIRRGB)) {
			//
			mDirColor.setSummary(getColorSummary(getDirColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_BEFRGB)) {
			//
			mBefColor.setSummary(getColorSummary(getBefColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_NOWRGB)) {
			//
			mNowColor.setSummary(getColorSummary(getNowColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_AFTRGB)) {
			//
			mAftColor.setSummary(getColorSummary(getAftColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_IMGRGB)) {
			//
			mImgColor.setSummary(getColorSummary(getImgColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_INFRGB)) {
			//
			mInfColor.setSummary(getColorSummary(getInfColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_MRKRGB)) {
			//
			mMrkColor.setSummary(getColorSummary(getMrkColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_BAKRGB)) {
			//
			mBakColor.setSummary(getColorSummary(getBakColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_CURRGB)) {
			//
			mCurColor.setSummary(getColorSummary(getCurColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_TITRGB)) {
			//
			mTitColor.setSummary(getColorSummary(getTitColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_TLBRGB)) {
			//
			mTibColor.setSummary(getColorSummary(getTibColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_TLDRGB)) {
			//
			mTldColor.setSummary(getColorSummary(getTldColor(sharedPreferences, true)));
		}
		else if (key.equals(DEF.KEY_TLBRGB)) {
			//
			mTlbColor.setSummary(getColorSummary(getTlbColor(sharedPreferences, true)));
		}
	}

	private void setEnableViews(boolean enable) {
		mCustomUpdate.setEnabled(!enable);
		mTxtColor.setEnabled(enable);
		mDirColor.setEnabled(enable);
		mBefColor.setEnabled(enable);
		mNowColor.setEnabled(enable);
		mAftColor.setEnabled(enable);
		mImgColor.setEnabled(enable);
		mInfColor.setEnabled(enable);
		mMrkColor.setEnabled(enable);
		mBakColor.setEnabled(enable);
		mCurColor.setEnabled(enable);
		mTitColor.setEnabled(enable);
		mTibColor.setEnabled(enable);
		mTldColor.setEnabled(enable);
		mTlbColor.setEnabled(enable);
	}

	private void updateSummarys() {
		mTxtColor.setSummary(getColorSummary(getTxtColor(mSharedPreferences, true))); // サーバ名
		mDirColor.setSummary(getColorSummary(getDirColor(mSharedPreferences, true))); // ディレクトリ
		mBefColor.setSummary(getColorSummary(getBefColor(mSharedPreferences, true))); // 未読
		mNowColor.setSummary(getColorSummary(getNowColor(mSharedPreferences, true))); // 読中
		mAftColor.setSummary(getColorSummary(getAftColor(mSharedPreferences, true))); // 既読
		mImgColor.setSummary(getColorSummary(getImgColor(mSharedPreferences, true))); // 画像
		mInfColor.setSummary(getColorSummary(getInfColor(mSharedPreferences, true))); // ファイル情報
		mMrkColor.setSummary(getColorSummary(getMrkColor(mSharedPreferences, true))); // マーカー
		mBakColor.setSummary(getColorSummary(getBakColor(mSharedPreferences, true))); // 背景
		mCurColor.setSummary(getColorSummary(getCurColor(mSharedPreferences, true))); // カーソル

		mTitColor.setSummary(getColorSummary(getTitColor(mSharedPreferences, true))); // タイトルテキスト
		mTibColor.setSummary(getColorSummary(getTibColor(mSharedPreferences, true))); // タイトル背景
		mTldColor.setSummary(getColorSummary(getTldColor(mSharedPreferences, true))); // ツールバー描画
		mTlbColor.setSummary(getColorSummary(getTlbColor(mSharedPreferences, true))); // ツールバー背景
	}

	// 設定の読込（リストビュー）
	public static int getPreset(SharedPreferences sp) {
		int val = Integer.parseInt(sp.getString(DEF.KEY_PRESET, "1"));
		return val;
	}

//
//	public static int getTitleColor(SharedPreferences sp) {
//		int val = Integer.parseInt(sp.getString(DEF.KEY_TITLECLR, "0"));
//		return val;
//	}
//
//	public static int getToolbarColor(SharedPreferences sp) {
//		int val = Integer.parseInt(sp.getString(DEF.KEY_TOOLBCLR, "0"));
//		return val;
//	}

	// 設定の読込（Activityからのアクセス）
	public static int getTxtColor(SharedPreferences sp) {
		return getTxtColor(sp, false);
	}

	public static int getDirColor(SharedPreferences sp) {
		return getDirColor(sp, false);
	}

	public static int getBefColor(SharedPreferences sp) {
		return getBefColor(sp, false);
	}

	public static int getNowColor(SharedPreferences sp) {
		return getNowColor(sp, false);
	}

	public static int getAftColor(SharedPreferences sp) {
		return getAftColor(sp, false);
	}

	public static int getImgColor(SharedPreferences sp) {
		return getImgColor(sp, false);
	}

	public static int getInfColor(SharedPreferences sp) {
		return getInfColor(sp, false);
	}

	public static int getMrkColor(SharedPreferences sp) {
		return getMrkColor(sp, false);
	}

	public static int getBakColor(SharedPreferences sp) {
		return getBakColor(sp, false);
	}

	public static int getCurColor(SharedPreferences sp) {
		return getCurColor(sp, false);
	}

	public static int getTitColor(SharedPreferences sp) {
		return getTitColor(sp, false);
	}

	public static int getTibColor(SharedPreferences sp) {
		return getTibColor(sp, false);
	}

	public static int getTldColor(SharedPreferences sp) {
		return getTldColor(sp, false);
	}

	public static int getTlbColor(SharedPreferences sp) {
		return getTlbColor(sp, false);
	}

	// 設定の読込（スライダー）
	public static int getTxtColor(SharedPreferences sp, boolean summary) {
		return getColor(sp, DEF.KEY_TXTCOLOR, DEF.KEY_TXTRGB, PRESET_TXT, 1, summary);
	}

	public static int getDirColor(SharedPreferences sp, boolean summary) {
		return getColor(sp, DEF.KEY_DIRCOLOR, DEF.KEY_DIRRGB, PRESET_DIR, 12, summary);
	}

	public static int getBefColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_BEFCOLOR, DEF.KEY_BEFRGB, PRESET_BEF, 1, summary);
		return val;
	}

	public static int getNowColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_NOWCOLOR, DEF.KEY_NOWRGB, PRESET_NOW, 7, summary);
		return val;
	}

	public static int getAftColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_AFTCOLOR, DEF.KEY_AFTRGB, PRESET_AFT, 8, summary);
		return val;
	}

	public static int getImgColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_IMGCOLOR, DEF.KEY_IMGRGB, PRESET_IMG, 13, summary);
		return val;
	}

	public static int getInfColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_INFCOLOR, DEF.KEY_INFRGB, PRESET_INF, 15, summary);
		return val;
	}

	public static int getMrkColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_MRKRGB, PRESET_MRK, 20, summary);
		return val;
	}

	public static int getBakColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, DEF.KEY_BAKCOLOR, DEF.KEY_BAKRGB, PRESET_BAK, 0, summary);
		return val;
	}

	public static int getCurColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_CURRGB, PRESET_CUR, 9, summary);
		return val;
	}

	public static int getTitColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TITRGB, PRESET_TIT, 1, summary);
		return val;
	}

	public static int getTibColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TIBRGB, PRESET_TIB, 8, summary);
		return val;
	}

	public static int getTldColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TLDRGB, PRESET_TLD, 0, summary);
		return val;
	}

	public static int getTlbColor(SharedPreferences sp, boolean summary) {
		int val = getColor(sp, null, DEF.KEY_TLBRGB, PRESET_TLB, 15, summary);
		return val;
	}

	private static int getColor(SharedPreferences sp, String keyColor, String keyRGB, int index, int def, boolean summary) {
		int preset = getPreset(sp);
		int val;

		if (preset == 0 || summary) {
			val = DEF.getColorValue(sp, keyColor, keyRGB, def);
		}
		else {
			val = mPresetColor[preset][index];
		}
		return val;
	}

	private String getPresetSummary(int val) {
		Resources res = getResources();
		return res.getString(mPresetName[val]);
	}

	private String getColorSummary(int val) {
		String str[] = { "Red", "Green", "Blue" };
		String result = "";
		for (int i = 0; i < 3; i++) {
			int v = ((val >> 8 * (2 - i)) & 0x000000FF);
			result += str[i] + "=" + v;
			if (i != 2) {
				result += ", ";
			}
		}
		result += String.format(" (%1$06X)", val & 0x00FFFFFF);
		return result;
	}
}
