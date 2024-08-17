package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextSpaceWSeekbar extends SeekBarPreference {

	public TextSpaceWSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_SPACEW;
		mMaxValue = DEF.MAX_TX_SPACEW;
		super.setKey(DEF.KEY_TX_SPACEW);
	}
}
