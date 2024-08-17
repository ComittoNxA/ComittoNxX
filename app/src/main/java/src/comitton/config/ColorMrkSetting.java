package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorMrkSetting extends ColorPreference {

	public ColorMrkSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_MRKRGB, 0xFFFF8000, false);
	}
}
