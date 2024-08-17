package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorHitSetting extends ColorPreference {

	public ColorHitSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 検索ヒットの背景色
		super.setConfig(null, DEF.KEY_TX_HITRGB, DEF.COLOR_TX_HITRGB, false);
	}
}
