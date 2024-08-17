package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class LongTapSeekbar extends SeekBarPreference {

	public LongTapSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_LONGTAP;
		mMaxValue = DEF.MAX_LONGTAP;
		super.setKey(DEF.KEY_LONGTAP);
	}
}
