package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class MarginSeekbar extends SeekBarPreference {

	public MarginSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MARGIN;
		mMaxValue = DEF.MAX_MARGIN;
		super.setKey(DEF.KEY_MARGIN);
	}
}
