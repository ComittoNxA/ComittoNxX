package src.comitton.view.image;

import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class ThumbnailScrollView extends HorizontalScrollView {
	ThumbnailView mThumbnailView;
	
	public ThumbnailScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// setDrawingCacheEnabled(false);

		try {
			Method setLayerTypeMethod = getClass().getMethod("setLayerType", new Class[] {int.class, Paint.class});
			setLayerTypeMethod.invoke(this, new Object[] {View.LAYER_TYPE_SOFTWARE, null});
		} catch (Exception e) {
			;
		}
	}

	/**
	 * 画面サイズ変更時の通知
	 * @param	w	新しい幅
	 * @param	h	新しい高さ
	 * @param	oldw	前の幅
	 * @param	oldh	前の高さ
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		if (mThumbnailView == null) {
			int num = this.getChildCount();
			for (int i = 0 ; i < num ; i ++) {
				mThumbnailView = (ThumbnailView)getChildAt(i);
			}
		}
		if (mThumbnailView != null) {
			// サイズ変更通知
			mThumbnailView.setLayoutChange(w, h);
		}
	}

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
		if (mThumbnailView != null) {
			// サイズ変更通知
			mThumbnailView.onScrollChanged(x, y, oldx, oldy);
		}
    }
}
