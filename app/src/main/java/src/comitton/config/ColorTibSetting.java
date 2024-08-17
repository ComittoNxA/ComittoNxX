package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTibSetting extends ColorPreference {

	public ColorTibSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// タイトルの背景
		super.setConfig(null, DEF.KEY_TIBRGB, 0xFF202020, false);
	}
}
