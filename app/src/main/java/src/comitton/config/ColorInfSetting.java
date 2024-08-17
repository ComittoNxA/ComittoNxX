package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorInfSetting extends ColorPreference {

	public ColorInfSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_INFCOLOR, DEF.KEY_INFRGB, DEF.ColorList[15], true);
	}
}
