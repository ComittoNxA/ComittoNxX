package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTitSetting extends ColorPreference {

	public ColorTitSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// タイトルのテキスト
		super.setConfig(null, DEF.KEY_TITRGB, 0xFFFFFFFF, true);
	}
}
