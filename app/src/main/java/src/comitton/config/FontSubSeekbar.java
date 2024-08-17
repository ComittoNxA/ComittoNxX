package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class FontSubSeekbar extends SeekBarPreference {

	public FontSubSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_FONTSUB;
		mMaxValue = DEF.MAX_FONTSUB;
		super.setKey(DEF.KEY_FONTSUB);
	}
}
