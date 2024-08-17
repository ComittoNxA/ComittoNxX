package src.comitton.config;

import src.comitton.common.DEF;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ThumbnailPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static SharedPreferences mSP;
	private Context mContext;
	private TextView mMsgView;
	private SeekBar mSeekBar[] = {null, null};
	private TextView mTextView;
	private String mSizeUnit;

	private static final int LAYOUT_PADDING = 10;

	private final int SLIDER_MAX = 50;

	public ThumbnailPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
		mSizeUnit = context.getString(R.string.rangeSumm1);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 選択された数をテキストに
		int sizeW = mSeekBar[0].getProgress();
		int sizeH = mSeekBar[1].getProgress();

		mTextView.setText(getSummaryString(sizeW, sizeH));
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

		// 2セット
		for (int i = 0 ; i < mSeekBar.length ; i ++) {
			// スライダ
			mSeekBar[i] = new SeekBar(mContext);
			layout.addView(mSeekBar[i], new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

		// サマリ
		mTextView = new TextView(mContext);
		mTextView.setTypeface(Typeface.MONOSPACE);
		mTextView.setTextSize(DEF.TEXTSIZE_SUMMARY);
		layout.addView(mTextView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		// 幅の値設定
		int sizeW = getValueW();
		int sizeH = getValueH();
		mSeekBar[0].setOnSeekBarChangeListener(this);
		mSeekBar[0].setMax(SLIDER_MAX);
		mSeekBar[0].setProgress(sizeW);

		// 高さの値設定
		mSeekBar[1].setOnSeekBarChangeListener(this);
		mSeekBar[1].setMax(SLIDER_MAX);
		mSeekBar[1].setProgress(sizeH);

		String str = (String) getDialogMessage();
		mMsgView.setText(str);

		// テキストに現在値を設定
		mTextView.setText(getSummaryString(sizeW, sizeH));
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

	private void setValue() {
		Editor ed = mSP.edit();
		ed.putInt(DEF.KEY_THUMBSIZEW, mSeekBar[0].getProgress());
		ed.putInt(DEF.KEY_THUMBSIZEH, mSeekBar[1].getProgress());
		ed.commit();
	}

	// 幅情報
	private int getValueW() {
		int val = mSP.getInt(DEF.KEY_THUMBSIZEW, DEF.DEFAULT_THUMBSIZEW);
		return val;
	}

	// 高さ情報
	private int getValueH() {
		int val = mSP.getInt(DEF.KEY_THUMBSIZEH, DEF.DEFAULT_THUMBSIZEH);
		return val;
	}

	private String getSummaryString(int sizew, int sizeh) {
		return DEF.calcThumbnailSize(sizew) + " " + mSizeUnit + " x " + DEF.calcThumbnailSize(sizeh) + " " + mSizeUnit;
	}
}
