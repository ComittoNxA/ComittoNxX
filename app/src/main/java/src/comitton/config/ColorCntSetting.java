package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorCntSetting extends ColorPreference {

	public ColorCntSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(DEF.KEY_CNTCOLOR, DEF.KEY_CNTRGB, DEF.ColorList[22], false);
	}
}
