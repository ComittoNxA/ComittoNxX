package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class OrgWidthSeekbar extends SeekBarPreference {

	public OrgWidthSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_ORGWIDTH;
		mMaxValue = DEF.MAX_ORGWIDTH;
		super.setKey(DEF.KEY_ORGWIDTH);
	}
}
