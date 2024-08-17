package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTvtSetting extends ColorPreference {

	public ColorTvtSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// テキストビューアのテキスト
		super.setConfig(null, DEF.KEY_TX_TVTRGB, DEF.COLOR_TX_TVTRGB, true);
	}
}
