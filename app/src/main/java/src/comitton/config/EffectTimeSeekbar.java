package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class EffectTimeSeekbar extends SeekBarPreference {

	public EffectTimeSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_EFFECTTIME;
		mMaxValue = DEF.MAX_EFFECTTIME;
		super.setKey(DEF.KEY_EFFECTTIME);
	}
}
