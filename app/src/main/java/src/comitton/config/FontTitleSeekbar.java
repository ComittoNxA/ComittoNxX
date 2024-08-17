package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class FontTitleSeekbar extends SeekBarPreference {

	public FontTitleSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_FONTTITLE;
		mMaxValue = DEF.MAX_FONTTITLE;
		super.setKey(DEF.KEY_FONTTITLE);
	}
}
