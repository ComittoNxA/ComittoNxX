package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class FontTileSeekbar extends SeekBarPreference {

	public FontTileSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_FONTTILE;
		mMaxValue = DEF.MAX_FONTTILE;
		super.setKey(DEF.KEY_FONTTILE);
	}
}
