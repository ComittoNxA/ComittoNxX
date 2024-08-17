package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextFontRubiSeekbar extends SeekBarPreference {

	public TextFontRubiSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_FONTRUBI;
		mMaxValue = DEF.MAX_TX_FONTRUBI;
		super.setKey(DEF.KEY_TX_FONTRUBI);
	}
}
