package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class VolScrlSeekbar extends SeekBarPreference {

	public VolScrlSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_VOLSCRL;
		mMaxValue = DEF.MAX_VOLSCRL;
		super.setKey(DEF.KEY_VOLSCRL);
	}
}
