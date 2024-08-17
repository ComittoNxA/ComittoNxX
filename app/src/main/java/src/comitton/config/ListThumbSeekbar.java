package src.comitton.config;


import android.content.Context;
import android.util.AttributeSet;

import src.comitton.common.DEF;

public class ListThumbSeekbar extends SeekBarPreference {

    public ListThumbSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefValue = DEF.DEFAULT_LISTTHUMBSIZEH;
        mMaxValue = DEF.MAX_LISTTHUMBSIZE;
        super.setKey(DEF.KEY_LISTTHUMBSEEK);
    }
}
