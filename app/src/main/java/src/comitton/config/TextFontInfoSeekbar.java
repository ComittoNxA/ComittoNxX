package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextFontInfoSeekbar extends SeekBarPreference {

	public TextFontInfoSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_FONTINFO;
		mMaxValue = DEF.MAX_TX_FONTINFO;
		super.setKey(DEF.KEY_TX_FONTINFO);
	}
}
