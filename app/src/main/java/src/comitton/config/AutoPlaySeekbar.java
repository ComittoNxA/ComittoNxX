package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class AutoPlaySeekbar extends SeekBarPreference {

	public AutoPlaySeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_AUTOPLAY;
		mMaxValue = DEF.MAX_AUTOPLAY;
		super.setKey(DEF.KEY_AUTOPLAY);
	}
}
