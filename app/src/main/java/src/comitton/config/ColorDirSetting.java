package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorDirSetting extends ColorPreference {

	public ColorDirSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_DIRCOLOR, DEF.KEY_DIRRGB, DEF.ColorList[12], true);
	}
}
