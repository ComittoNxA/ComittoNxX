package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ClickAreaSeekbar extends SeekBarPreference {

	public ClickAreaSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_CLICKAREA;
		mMaxValue = DEF.MAX_CLICKAREA;
		super.setKey(DEF.KEY_CLICKAREA);
	}
}
