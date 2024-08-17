package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class TextMarginHSeekbar extends SeekBarPreference {

	public TextMarginHSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_TX_MARGINH;
		mMaxValue = DEF.MAX_TX_MARGINH;
		super.setKey(DEF.KEY_TX_MARGINH);
	}
}
