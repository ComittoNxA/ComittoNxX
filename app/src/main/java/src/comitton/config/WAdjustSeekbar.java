package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class WAdjustSeekbar extends SeekBarPreference {

	public WAdjustSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WADJUST;
		mMaxValue = DEF.MAX_WADJUST;
		super.setKey(DEF.KEY_WADJUST);
	}
}
