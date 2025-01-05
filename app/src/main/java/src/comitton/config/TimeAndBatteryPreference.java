package src.comitton.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.DialogPreference;
import androidx.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class TimeAndBatteryPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private CheckBox mChkDisp;
	private Spinner mSpinFormat;
	private Spinner mSpinPos;
	private TextView mTextSize;
	private SeekBar mSeekSize;
	private Spinner mSpinColor;

	private String mDots;

	private static SharedPreferences mSP;

	public TimeAndBatteryPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		mSP = PreferenceManager.getDefaultSharedPreferences(context);
		setDialogLayoutResource(R.layout.timeandbattery);
		Resources res = context.getResources();
		mDots = res.getString(R.string.unitSumm1);
	}

	@Override
	protected View onCreateDialogView() {
		View root = super.onCreateDialogView();
		mChkDisp = (CheckBox)root.findViewById(R.id.chk_disp);
		mSpinFormat = (Spinner)root.findViewById(R.id.spin_format);
		mSpinPos = (Spinner)root.findViewById(R.id.spin_pos);
		mTextSize = (TextView)root.findViewById(R.id.text_size);
		mSeekSize = (SeekBar)root.findViewById(R.id.seek_size);
		mSeekSize.setMax(DEF.MAX_PNUMSIZE);
		mSeekSize.setOnSeekBarChangeListener(this);
		mSpinColor = (Spinner)root.findViewById(R.id.spin_color);

		mChkDisp.setChecked(getDispValue());
		mSpinFormat.setSelection(getFormatValue());
		mSpinPos.setSelection(getPosValue());
		int size = getSizeValue();
		mSpinColor.setSelection(getColorValue());
//		mTextSize.setText(DEF.getPnumSizeStr(size), );
		mSeekSize.setProgress(size);
		return root;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			boolean disp = mChkDisp.isChecked();
			int format = mSpinFormat.getSelectedItemPosition();
			int pos = mSpinPos.getSelectedItemPosition();
			int size = mSeekSize.getProgress();
			int color = mSpinColor.getSelectedItemPosition();
			setValue(disp, format, pos, size, color);
		}
	}

	private void setValue(boolean disp, int format, int pos, int size, int color) {
		Editor ed = mSP.edit();
		ed.putBoolean(DEF.KEY_TIMEDISP, disp);
		ed.putInt(DEF.KEY_TIMEFORMAT, format);
		ed.putInt(DEF.KEY_TIMEPOS, pos);
		ed.putInt(DEF.KEY_TIMESIZE, size);
		ed.putInt(DEF.KEY_TIMECOLOR, color);
		ed.commit();
	}

	private boolean getDispValue() {
		boolean val = mSP.getBoolean(DEF.KEY_TIMEDISP, DEF.DEFAULT_TIMEDISP);
		return val;
	}


	private int getFormatValue() {
		int val = mSP.getInt(DEF.KEY_TIMEFORMAT, DEF.DEFAULT_TIMEFORMAT);
		return val;
	}


	private int getPosValue() {
		int val = mSP.getInt(DEF.KEY_TIMEPOS, DEF.DEFAULT_TIMEPOS);
		return val;
	}


	private int getSizeValue() {
		int val = mSP.getInt(DEF.KEY_TIMESIZE, DEF.DEFAULT_TIMESIZE);
		return val;
	}

	private int getColorValue() {
		int val = mSP.getInt(DEF.KEY_TIMECOLOR, DEF.DEFAULT_TIMECOLOR);
		return val;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 変更通知
		mTextSize.setText(DEF.getPnumSizeStr(progress, mDots));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		;
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		;
	}
}
