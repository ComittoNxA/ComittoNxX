package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.util.AttributeSet;

public class MenuLongTapSeekbar extends SeekBarPreference {

    public MenuLongTapSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefValue = DEF.DEFAULT_MENULONGTAP;
        mMaxValue = DEF.MAX_MENULONGTAP;
        super.setKey(DEF.KEY_MENULONGTAP);
    }
}