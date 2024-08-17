package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class WScalingSeekbar extends SeekBarPreference {

	public WScalingSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_WSCALING;
		mMaxValue = DEF.MAX_WSCALING;
		super.setKey(DEF.KEY_WSCALING);
	}
}
