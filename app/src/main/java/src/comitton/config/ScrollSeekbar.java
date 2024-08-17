package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ScrollSeekbar extends SeekBarPreference {

	public ScrollSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_SCROLL;
		mMaxValue = DEF.MAX_SCROLL;
		super.setKey(DEF.KEY_SCROLL);
	}
}
