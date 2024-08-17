package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class FontMainSeekbar extends SeekBarPreference {

	public FontMainSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_FONTMAIN;
		mMaxValue = DEF.MAX_FONTMAIN;
		super.setKey(DEF.KEY_FONTMAIN);
	}
}
