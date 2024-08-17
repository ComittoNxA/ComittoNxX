package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTvbSetting extends ColorPreference {

	public ColorTvbSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストビューアの背景
		super.setConfig(null, DEF.KEY_TX_TVBRGB, DEF.COLOR_TX_TVBRGB, false);
	}
}
