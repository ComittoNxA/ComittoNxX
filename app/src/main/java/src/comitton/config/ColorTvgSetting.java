package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTvgSetting extends ColorPreference {

	public ColorTvgSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストビューアの背景(グラデーション用)
		super.setConfig(null, DEF.KEY_TX_TVGRGB, DEF.COLOR_TX_TVGRGB, false);
	}
}
