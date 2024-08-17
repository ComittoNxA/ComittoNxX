package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ScrlRngHSeekbar extends SeekBarPreference {

	public ScrlRngHSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_SCRLRNGH;
		mMaxValue = DEF.MAX_SCRLRNGH;
		super.setKey(DEF.KEY_SCRLRNGH);
	}
}
