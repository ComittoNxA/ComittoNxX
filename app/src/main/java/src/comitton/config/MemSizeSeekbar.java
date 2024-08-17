package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class MemSizeSeekbar extends SeekBarPreference {

	public MemSizeSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_MEMSIZE;
		mMaxValue = DEF.MAX_MEMSIZE;
		super.setKey(DEF.KEY_MEMSIZE);
	}
}
