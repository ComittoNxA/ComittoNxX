package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorImgSetting extends ColorPreference {

	public ColorImgSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_IMGCOLOR, DEF.KEY_IMGRGB, DEF.ColorList[13], true);
	}
}
