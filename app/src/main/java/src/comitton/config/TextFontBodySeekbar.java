package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextFontBodySeekbar extends SeekBarPreference {

	public TextFontBodySeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_FONTBODY;
		mMaxValue = DEF.MAX_TX_FONTBODY;
		super.setKey(DEF.KEY_TX_FONTBODY);
	}
}
