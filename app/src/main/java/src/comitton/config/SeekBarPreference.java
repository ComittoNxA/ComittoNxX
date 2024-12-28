package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private SeekBar mSeekBar;
	private Context mContext;
	private TextView mTextView;
	private TextView mMsgView;

	private static SharedPreferences mSP;

	private String mSummary1 = null;
	private String mSummary2 = null;

	private static final int LAYOUT_PADDING = 10;

	private String mKey;
	public int mMinValue;
	public int mDefValue;
	public int mMaxValue;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 選択された数をテキストに
		mTextView.setText(getSummaryString(progress));
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.setPadding(LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING);
		mMsgView = new TextView(mContext);
		mMsgView.setTextSize(DEF.TEXTSIZE_MESSAGE);
		mSeekBar = new SeekBar(mContext);
		mTextView = new TextView(mContext);
		mTextView.setTextSize(DEF.TEXTSIZE_SUMMARY);
		this.getTitle();

		layout.addView(mMsgView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(mTextView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		int progress;
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setMin(mMinValue);
		mSeekBar.setMax(mMaxValue);
		progress = getValue();
		mSeekBar.setProgress(progress);

		String str = (String) getDialogMessage();
		mMsgView.setText(str);
		mTextView.setText(getSummaryString(progress));
		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			setValue(mSeekBar.getProgress());
		}
	}

	public void setKey(String key) {
		mKey = key;
		if (mKey.equals(DEF.KEY_CLICKAREA) || mKey.equals(DEF.KEY_PAGERANGE) || mKey.equals(DEF.KEY_TAPRANGE)) {
			mSummary1 = mContext.getString(R.string.unitSumm1);
			mSummary2 = "";
		}
		if (mKey.equals(DEF.KEY_MARGIN)) {
			mSummary1 = mContext.getString(R.string.rangeSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_SCROLL)) {
			mSummary1 = mContext.getString(R.string.scrlSumm1);
			mSummary2 = mContext.getString(R.string.scrlSumm2);
		}
		else if (mKey.equals(DEF.KEY_LONGTAP) || mKey.equals(DEF.KEY_MENULONGTAP) || mKey.equals(DEF.KEY_EFFECTTIME) || mKey.equals(DEF.KEY_AUTOPLAY)) {
			mSummary1 = mContext.getString(R.string.msecSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_MOMENTMODE)) {
			mSummary1 = mContext.getString(R.string.mmentSumm1);
			mSummary2 = mContext.getString(R.string.mmentSumm2);
		}
		else if (mKey.equals(DEF.KEY_WADJUST)) {
			mSummary1 = mContext.getString(R.string.aspcSumm1);
			mSummary2 = mContext.getString(R.string.aspcSumm2);
		}
		else if (mKey.equals(DEF.KEY_WSCALING)) {
			mSummary1 = mContext.getString(R.string.scalSumm1);
			mSummary2 = mContext.getString(R.string.scalSumm2);
		}
		else if (mKey.equals(DEF.KEY_SCALING)) {
			mSummary1 = mContext.getString(R.string.scalSumm1);
			mSummary2 = mContext.getString(R.string.scalSumm2);
		}
		else if (mKey.equals(DEF.KEY_GRADATION)) {
			mSummary1 = "";
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_CENTER)) {
			mSummary1 = mContext.getString(R.string.centSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_FONTTITLE) || mKey.equals(DEF.KEY_FONTMAIN) || mKey.equals(DEF.KEY_FONTSUB) || mKey.equals(DEF.KEY_FONTTILE) || mKey.equals(DEF.KEY_ITEMMRGN)) {
			mSummary1 = mContext.getString(R.string.unitSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_TOOLBARSEEK)) {
			mSummary1 = mContext.getString(R.string.unitSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_LISTTHUMBSEEK)) {
			mSummary1 = mContext.getString(R.string.rangeSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_MEMSIZE)) {
			mSummary1 = mContext.getString(R.string.mSizeSumm1);
			mSummary2 = mContext.getString(R.string.mSizeSumm2);
		}
		else if (mKey.equals(DEF.KEY_MEMNEXT) || mKey.equals(DEF.KEY_MEMPREV)) {
			mSummary1 = mContext.getString(R.string.mPageSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_VOLSCRL) || mKey.equals(DEF.KEY_NOISESCRL)) {
			mSummary1 = mContext.getString(R.string.unitSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_NOISEUNDER) || mKey.equals(DEF.KEY_NOISEOVER)) {
			mSummary1 = "";
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_TX_FONTTOP) || mKey.equals(DEF.KEY_TX_FONTBODY) || mKey.equals(DEF.KEY_TX_FONTRUBI) || mKey.equals(DEF.KEY_TX_FONTINFO)) {
			mSummary1 = mContext.getString(R.string.unitSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_TX_SPACEW) || mKey.equals(DEF.KEY_TX_SPACEH)) {
			mSummary1 = mContext.getString(R.string.rangeSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_TX_MARGINW) || mKey.equals(DEF.KEY_TX_MARGINH)) {
			mSummary1 = mContext.getString(R.string.rangeSumm1);
			mSummary2 = "";
		}
		else if (mKey.equals(DEF.KEY_SCRLRNGW) || mKey.equals(DEF.KEY_SCRLRNGH) || mKey.equals(DEF.KEY_TX_SCRLRNGW) || mKey.equals(DEF.KEY_TX_SCRLRNGH)) {
			mSummary1 = mContext.getString(R.string.srngSumm1);
			mSummary2 = mContext.getString(R.string.srngSumm2);
		}
		else {
			mSummary1 = mContext.getString(R.string.pixSumm1);
			mSummary2 = mContext.getString(R.string.pixSumm2);
		}

	}

	private void setValue(int value) {
		Editor ed = mSP.edit();
		ed.putInt(mKey, value);
		ed.commit();
	}

	private int getValue() {
		int val = mSP.getInt(mKey, mDefValue);
		return val;

	}

	private String getSummaryString(int num) {
		String strSummary;

		if (mKey.equals(DEF.KEY_CLICKAREA)) {
			strSummary = DEF.getClickAreaStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_TAPRANGE)) {
			strSummary = DEF.getTapRangeStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_PAGERANGE)) {
			strSummary = DEF.getPageRangeStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_MARGIN)) {
			strSummary = DEF.getMarginStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_SCROLL)) {
			strSummary = DEF.getScrollStr(num, mSummary1, mSummary2);
		}
		else if (mKey.equals(DEF.KEY_LONGTAP)) {
			strSummary = DEF.getMSecStr200(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_MENULONGTAP)) {
			strSummary = DEF.getMSecStr100(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_EFFECTTIME)) {
			strSummary = DEF.getEffectTimeStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_AUTOPLAY)) {
			strSummary = DEF.getAutoPlayStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_MOMENTMODE)) {
			strSummary = DEF.getMomentModeStr(num, mSummary1, mSummary2);
		}
		else if (mKey.equals(DEF.KEY_WADJUST)) {
			strSummary = DEF.getAdjustStr(num, mSummary1, mSummary2);
		}
		else if (mKey.equals(DEF.KEY_WSCALING)) {
			strSummary = DEF.getWScalingStr(num, mSummary1, mSummary2);
		}
		else if (mKey.equals(DEF.KEY_SCALING)) {
			strSummary = DEF.getScalingStr(num, mSummary1, mSummary2);
		}
		else if (mKey.equals(DEF.KEY_CENTER)) {
			strSummary = DEF.getCenterStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_GRADATION)) {
			strSummary = DEF.getGradationStr(num);
		}
		else if (mKey.equals(DEF.KEY_FONTTITLE) || mKey.equals(DEF.KEY_FONTMAIN) || mKey.equals(DEF.KEY_FONTSUB) || mKey.equals(DEF.KEY_FONTTILE)) {
			strSummary = DEF.getFontSpStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_ITEMMRGN)) {
			strSummary = DEF.getMarginSpStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_TOOLBARSEEK)) {
			strSummary = DEF.getToolbarSeekStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_LISTTHUMBSEEK)) {
			strSummary = DEF.getListThumbSeekStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_TX_FONTTOP) || mKey.equals(DEF.KEY_TX_FONTBODY) || mKey.equals(DEF.KEY_TX_FONTRUBI) || mKey.equals(DEF.KEY_TX_FONTINFO)) {
			strSummary = DEF.getFontSpStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_MEMSIZE)) {
			strSummary = DEF.getMemSizeStr(num, mSummary1, mSummary2);
		}
		else if (mKey.equals(DEF.KEY_MEMNEXT) || mKey.equals(DEF.KEY_MEMPREV)) {
			strSummary = DEF.getMemPageStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_VOLSCRL) || mKey.equals(DEF.KEY_NOISESCRL)) {
			strSummary = DEF.getScrlSpeedStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_NOISEUNDER) || mKey.equals(DEF.KEY_NOISEOVER)) {
			strSummary = DEF.getNoiseLevelStr(num);
		}
		else if (mKey.equals(DEF.KEY_TX_SPACEW) || mKey.equals(DEF.KEY_TX_SPACEH)) {
			strSummary = DEF.getTextSpaceStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_TX_MARGINW) || mKey.equals(DEF.KEY_TX_MARGINH)) {
			strSummary = DEF.getDispMarginStr(num, mSummary1);
		}
		else if (mKey.equals(DEF.KEY_SCRLRNGW) || mKey.equals(DEF.KEY_SCRLRNGH) || mKey.equals(DEF.KEY_TX_SCRLRNGW) || mKey.equals(DEF.KEY_TX_SCRLRNGH)) {
			// スクロール範囲
			strSummary = DEF.getScrlRangeStr(num, mSummary1, mSummary2);
		}
		else {
			strSummary = DEF.getSizeStr(num, mSummary1, mSummary2);
		}
		return strSummary;
	}
}
