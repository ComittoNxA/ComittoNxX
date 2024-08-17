package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorAftSetting extends ColorPreference {

	public ColorAftSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_AFTCOLOR, DEF.KEY_AFTRGB, DEF.ColorList[8], true);
	}
}
