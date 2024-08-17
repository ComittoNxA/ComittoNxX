package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class MemNextSeekbar extends SeekBarPreference {

	public MemNextSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MEMNEXT;
		mMaxValue = DEF.MAX_MEMNEXT;
		super.setKey(DEF.KEY_MEMNEXT);
	}
}
