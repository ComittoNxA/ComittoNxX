package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextScrlRngHSeekbar extends SeekBarPreference {

	public TextScrlRngHSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_SCRLRNGH;
		mMaxValue = DEF.MAX_TX_SCRLRNGH;
		super.setKey(DEF.KEY_TX_SCRLRNGH);
	}
}
