package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorMgnSetting extends ColorPreference {

	public ColorMgnSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(DEF.KEY_MGNCOLOR, DEF.KEY_MGNRGB, DEF.ColorList[22], false);
	}
}
