package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextFontTopSeekbar extends SeekBarPreference {

	public TextFontTopSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_FONTTOP;
		mMaxValue = DEF.MAX_TX_FONTTOP;
		super.setKey(DEF.KEY_TX_FONTTOP);
	}
}
