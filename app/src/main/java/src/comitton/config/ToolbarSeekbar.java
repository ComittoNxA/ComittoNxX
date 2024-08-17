package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ToolbarSeekbar extends SeekBarPreference {

	public ToolbarSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TOOLBARSEEK;
		mMaxValue = DEF.MAX_TOOLBARSEEK;
		super.setKey(DEF.KEY_TOOLBARSEEK);
	}
}
