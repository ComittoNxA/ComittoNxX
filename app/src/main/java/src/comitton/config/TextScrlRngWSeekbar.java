package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextScrlRngWSeekbar extends SeekBarPreference {

	public TextScrlRngWSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_SCRLRNGW;
		mMaxValue = DEF.MAX_TX_SCRLRNGW;
		super.setKey(DEF.KEY_TX_SCRLRNGW);
	}
}
