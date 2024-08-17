package src.comitton.config;

import src.comitton.common.DEF;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private Context mContext;
	private TextView mMsgView;
	private SeekBar mSeekBar[] = {null, null, null};
	private TextView mTextView[] = {null, null, null};
	private TextView mSampleView[] = {null, null};
	private static SharedPreferences mSP;

	private static final int LAYOUT_PADDING = 10;

	private String mKeyOld;
	private String mKey;
	public int mDefValue;
	private boolean mTextMode;

	private final int SLIDER_MAX = 255;
	private final String SAMPLE_TEXT = "ABCDEFG123456";

	public ColorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 選択された数をテキストに
		for (int i = 0 ; i < mSeekBar.length ; i ++) {
			if (seekBar == mSeekBar[i]) {
				mTextView[i].setText(getSepSummaryString(progress, i));
			}
		}
		int value = getSliderValue();
		for (int i = 0 ; i < mSampleView.length ; i ++) {
			if (mTextMode) {
				mSampleView[i].setTextColor(value);
			}
			else {
				mSampleView[i].setBackgroundColor(value);
			}
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.setPadding(LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING);

		// メッセージ
		mMsgView = new TextView(mContext);
		mMsgView.setTextSize(DEF.TEXTSIZE_MESSAGE);
		layout.addView(mMsgView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		// 3セット
		for (int i = 0 ; i < mSeekBar.length ; i ++) {
			// スライダ
			mSeekBar[i] = new SeekBar(mContext);
			layout.addView(mSeekBar[i], new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			// サマリ
			mTextView[i] = new TextView(mContext);
			mTextView[i].setTypeface(Typeface.MONOSPACE);
			mTextView[i].setTextSize(DEF.TEXTSIZE_SUMMARY);
			layout.addView(mTextView[i], new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}

		// 色のサンプル
		for (int i = 0 ; i < mSampleView.length ; i ++) {
			mSampleView[i] = new TextView(mContext);
			mSampleView[i].setText(SAMPLE_TEXT);
			mSampleView[i].setTextSize(DEF.TEXTSIZE_MESSAGE);
			mSampleView[i].setTypeface(Typeface.DEFAULT_BOLD);
			mSampleView[i].setGravity(Gravity.CENTER);

			layout.addView(mSampleView[i], new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

//        Button buttonOK = new Button(mContext);
//        buttonOK.setText("OK");
//        buttonOK.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                setValue();
//            }
//        });
//        layout.addView(buttonOK, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//
//        Button buttonCancel = new Button(mContext);
//        buttonCancel.setText("Cancel");
//        layout.addView(buttonCancel, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		int progress = getValue();
		for (int i = 0 ; i < mSeekBar.length ; i ++) {
			mSeekBar[i].setOnSeekBarChangeListener(this);
			mSeekBar[i].setMax(SLIDER_MAX);
			mSeekBar[i].setProgress(getValue(progress, i));
		}

		String str = (String) getDialogMessage();
		mMsgView.setText(str);
		for (int i = 0 ; i < mTextView.length ; i ++) {
			mTextView[i].setText(getSummaryString(progress, i));
		}

		setSampleColor(progress, true);
		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			setValue();
		}
	}

	public void setConfig(String oldkey, String key, int defValue, boolean text) {
		mKeyOld = oldkey;
		mKey = key;
		mDefValue = defValue;
		mTextMode = text;
	}

	private void setValue() {
		int value = getSliderValue();
		String str = null;
		if (mKeyOld != null) {
			str = mSP.getString(mKeyOld, null);
		}

		Editor ed = mSP.edit();
		ed.putInt(mKey, value);
		if (str != null) {
			ed.remove(mKeyOld);
		}
		ed.commit();
	}

	private int getValue() {
		int index = -1; 
		int val; 
		
		// 旧設定から読み込み
		if (mKeyOld != null) {
			String str = mSP.getString(mKeyOld, null);
			if (str != null) {
				index = Integer.parseInt(str);
			}
		}
		if (0 <= index && index < DEF.ColorList.length) {
			// 旧設定がある場合は色情報を取得
			val = DEF.ColorList[index];
		}
		else {
			// 色情報
			val = mSP.getInt(mKey, mDefValue);
		}
		return val;
	}

	private int getValue(int value, int index) {
		int ret = (value >> (8 * (mSeekBar.length - 1 - index))) & 0x000000FF; 
		return ret;
	}
	
	private void setSampleColor(int value, boolean flag) {
		for (int i = 0 ; i < mSampleView.length ; i ++) {
			if (mTextMode) {
				mSampleView[i].setTextColor(value);
				if (flag) {
					mSampleView[i].setBackgroundColor(i == 0 ? Color.BLACK : Color.WHITE);
				}
			}
			else {
				if (flag) {
					mSampleView[i].setTextColor(i == 0 ? Color.WHITE : Color.BLACK);
				}
				mSampleView[i].setBackgroundColor(value);
			}
		}
	}
	
	private int getSliderValue() {
		return 0xFF000000 | ((mSeekBar[0].getProgress() << 16) & 0x00FF0000) | ((mSeekBar[1].getProgress() << 8) & 0x0000FF00) | (mSeekBar[2].getProgress() & 0x000000FF);
	}

	private String getSummaryString(int value, int index) {
		String str[] = {"Red  ", "Green", "Blue "};
		int val;
		if (index == 0) {
			val = (value >> 16) & 0x000000FF;
		}
		else if (index == 1) {
			val = (value >> 8) & 0x000000FF;
		}
		else {
			val = (value >> 0) & 0x000000FF;
		}
		return str[index] + " : " + String.format("%1$3d(%2$02X)", val, val);   
	}

	private String getSepSummaryString(int val, int index) {
		String str[] = {"Red  ", "Green", "Blue "};
		return str[index] + " : " + String.format("%1$3d(%2$02X)", val, val);  
	}
}
