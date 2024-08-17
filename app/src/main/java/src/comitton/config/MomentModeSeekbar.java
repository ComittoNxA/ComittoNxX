package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class MomentModeSeekbar extends SeekBarPreference {

	public MomentModeSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MOMENTMODE;
		mMaxValue = DEF.MAX_MOMENTMODE;
		super.setKey(DEF.KEY_MOMENTMODE);
	}
}
