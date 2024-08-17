package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ScrlRngWSeekbar extends SeekBarPreference {

	public ScrlRngWSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_SCRLRNGW;
		mMaxValue = DEF.MAX_SCRLRNGW;
		super.setKey(DEF.KEY_SCRLRNGW);
	}
}
