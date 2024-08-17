package src.comitton.config;

import android.content.Context;
import android.util.AttributeSet;

import src.comitton.common.DEF;

public class ScalingSeekbar extends SeekBarPreference {

	public ScalingSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_SCALING;
		mMaxValue = DEF.MAX_SCALING;
		super.setKey(DEF.KEY_SCALING);
	}
}
