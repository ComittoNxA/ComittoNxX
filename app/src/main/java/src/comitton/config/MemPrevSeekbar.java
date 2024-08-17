package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class MemPrevSeekbar extends SeekBarPreference {

	public MemPrevSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MEMPREV;
		mMaxValue = DEF.MAX_MEMPREV;
		super.setKey(DEF.KEY_MEMPREV);
	}
}
