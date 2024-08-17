package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class CenterSeekbar extends SeekBarPreference {

	public CenterSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_CENTER;
		mMaxValue = DEF.MAX_CENTER;
		super.setKey(DEF.KEY_CENTER);
	}
}
