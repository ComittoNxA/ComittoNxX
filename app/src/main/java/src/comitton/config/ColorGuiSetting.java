package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorGuiSetting extends ColorPreference {

	public ColorGuiSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_GUIRGB, DEF.GuideList[1], false);
	}
}
