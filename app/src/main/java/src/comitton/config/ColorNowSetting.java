package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorNowSetting extends ColorPreference {

	public ColorNowSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストモード
		super.setConfig(DEF.KEY_NOWCOLOR, DEF.KEY_NOWRGB, DEF.ColorList[7], true);
	}
}
