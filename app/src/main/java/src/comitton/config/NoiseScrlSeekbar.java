package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class NoiseScrlSeekbar extends SeekBarPreference {

	public NoiseScrlSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_NOISESCRL;
		mMaxValue = DEF.MAX_NOISESCRL;
		super.setKey(DEF.KEY_NOISESCRL);
	}
}
