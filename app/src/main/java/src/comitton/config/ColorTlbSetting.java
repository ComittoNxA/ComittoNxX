package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTlbSetting extends ColorPreference {

	public ColorTlbSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_TLBRGB, 0xFFA0A0A0, false);
	}
}
