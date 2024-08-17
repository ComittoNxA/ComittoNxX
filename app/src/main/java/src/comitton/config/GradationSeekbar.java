package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class GradationSeekbar extends SeekBarPreference {

	public GradationSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_GRADATION;
		mMaxValue = DEF.MAX_GRADATION;
		super.setKey(DEF.KEY_GRADATION);
	}
}
