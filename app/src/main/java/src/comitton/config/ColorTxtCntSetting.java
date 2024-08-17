package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class ColorTxtCntSetting extends ColorPreference {

	public ColorTxtCntSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 背景モード
		super.setConfig(null, DEF.KEY_TX_CNTRGB, DEF.ColorList[22], false);
	}
}
