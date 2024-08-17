package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorBefSetting extends ColorPreference {

	public ColorBefSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_BEFCOLOR, DEF.KEY_BEFRGB, DEF.ColorList[1], true);
	}
}
