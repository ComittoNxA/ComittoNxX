package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class OrgHeightSeekbar extends SeekBarPreference {

	public OrgHeightSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_ORGHEIGHT;
		mMaxValue = DEF.MAX_ORGHEIGHT;
		super.setKey(DEF.KEY_ORGHEIGHT);
	}
}
