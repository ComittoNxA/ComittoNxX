package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class PageNumberPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private CheckBox mChkDisp;
	private Spinner mSpinFormat;
	private Spinner mSpinPos;
	private TextView mTextSize;
	private SeekBar mSeekSize;
	private Spinner mSpinColor;

	private String mDots;

	private static SharedPreferences mSP;

	public PageNumberPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		mSP = PreferenceManager.getDefaultSharedPreferences(context);
		setDialogLayoutResource(R.layout.pagenumber);
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
		ed.putBoolean(DEF.KEY_PNUMDISP, disp);
		ed.putInt(DEF.KEY_PNUMFORMAT, format);
		ed.putInt(DEF.KEY_PNUMPOS, pos);
		ed.putInt(DEF.KEY_PNUMSIZE, size);
		ed.putInt(DEF.KEY_PNUMCOLOR, color);
		ed.commit();
	}

	private boolean getDispValue() {
		boolean val = mSP.getBoolean(DEF.KEY_PNUMDISP, DEF.DEFAULT_PNUMDISP);
		return val;
	}


	private int getFormatValue() {
		int val = mSP.getInt(DEF.KEY_PNUMFORMAT, DEF.DEFAULT_PNUMFORMAT);
		return val;
	}


	private int getPosValue() {
		int val = mSP.getInt(DEF.KEY_PNUMPOS, DEF.DEFAULT_PNUMPOS);
		return val;
	}


	private int getSizeValue() {
		int val = mSP.getInt(DEF.KEY_PNUMSIZE, DEF.DEFAULT_PNUMSIZE);
		return val;
	}

	private int getColorValue() {
		int val = mSP.getInt(DEF.KEY_PNUMCOLOR, DEF.DEFAULT_PNUMCOLOR);
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
