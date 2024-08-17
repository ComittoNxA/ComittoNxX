package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TapRangeSeekbar extends SeekBarPreference {

	public TapRangeSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TAPRANGE;
		mMaxValue = DEF.MAX_TAPRANGE;
		super.setKey(DEF.KEY_TAPRANGE);
	}
}
