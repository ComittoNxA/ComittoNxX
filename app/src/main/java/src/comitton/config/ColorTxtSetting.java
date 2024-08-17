package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTxtSetting extends ColorPreference {

	public ColorTxtSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(DEF.KEY_TXTCOLOR, DEF.KEY_TXTRGB, DEF.ColorList[1], true);
	}
}
