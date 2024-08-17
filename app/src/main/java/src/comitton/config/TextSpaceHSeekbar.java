package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextSpaceHSeekbar extends SeekBarPreference {

	public TextSpaceHSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_SPACEH;
		mMaxValue = DEF.MAX_TX_SPACEH;
		super.setKey(DEF.KEY_TX_SPACEH);
	}
}
