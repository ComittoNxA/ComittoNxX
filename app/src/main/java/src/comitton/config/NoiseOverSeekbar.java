package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class NoiseOverSeekbar extends SeekBarPreference {

	public NoiseOverSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_NOISEOVER;
		mMaxValue = DEF.MAX_NOISEOVER;
		super.setKey(DEF.KEY_NOISEOVER);
	}
}
