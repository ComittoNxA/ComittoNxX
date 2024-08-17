package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class NoiseUnderSeekbar extends SeekBarPreference {

	public NoiseUnderSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_NOISEUNDER;
		mMaxValue = DEF.MAX_NOISEUNDER;
		super.setKey(DEF.KEY_NOISEUNDER);
	}
}
