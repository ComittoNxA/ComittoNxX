package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class PageRangeSeekbar extends SeekBarPreference {

	public PageRangeSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_PAGERANGE;
		mMaxValue = DEF.MAX_PAGERANGE;
		super.setKey(DEF.KEY_PAGERANGE);
	}
}
