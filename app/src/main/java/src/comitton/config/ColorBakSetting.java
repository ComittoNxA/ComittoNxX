package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorBakSetting extends ColorPreference {

	public ColorBakSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(DEF.KEY_BAKCOLOR, DEF.KEY_BAKRGB, DEF.ColorList[0], false);
	}
}
