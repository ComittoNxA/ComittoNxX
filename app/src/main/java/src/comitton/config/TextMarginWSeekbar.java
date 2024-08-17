package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextMarginWSeekbar extends SeekBarPreference {

	public TextMarginWSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_MARGINW;
		mMaxValue = DEF.MAX_TX_MARGINW;
		super.setKey(DEF.KEY_TX_MARGINW);
	}
}
