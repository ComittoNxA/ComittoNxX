package src.comitton.config;

import src.comitton.common.DEF;
import src.comitton.view.SelectIconView;
import jp.dip.muracoro.comittonx.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
// import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class OperationPreference extends DialogPreference implements OnClickListener {
	private Context mContext;
	private static SharedPreferences mSP;
	private float mDensity;

	private static int LAYOUT_PADDING;
	private static final int ICON_NUM = 4;

	public int mDefValue;
	public int mMaxValue;

	private Spinner mRateSel;
	private SelectIconView mSelIcon[];
	private int mSelIndex;

	public OperationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSP = PreferenceManager.getDefaultSharedPreferences(context);
		mDensity = context.getResources().getDisplayMetrics().scaledDensity;
		mSelIcon = new SelectIconView[ICON_NUM];

		LAYOUT_PADDING = (int)(4 * mDensity);
	}

	@Override
	protected View onCreateDialogView() {
		ScrollView scroll = new ScrollView(mContext);

		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING, LAYOUT_PADDING);

		LinearLayout layout2 = new LinearLayout(mContext);
		layout2.setOrientation(LinearLayout.HORIZONTAL);
		layout2.setGravity(Gravity.CENTER);

		LinearLayout layout3 = new LinearLayout(mContext);
		layout3.setOrientation(LinearLayout.HORIZONTAL);
		layout3.setGravity(Gravity.CENTER);

		TextView text = new TextView(mContext);
		text.setTextSize(DEF.TEXTSIZE_MESSAGE);

		int size = (int)(120 * mDensity);
		mSelIcon[0] = new SelectIconView(mContext, R.drawable.tappattern00, 0xFF000000, 0xFF0090FF);
		mSelIcon[1] = new SelectIconView(mContext, R.drawable.tappattern01, 0xFF000000, 0xFF0080FF);
		mSelIcon[2] = new SelectIconView(mContext, R.drawable.tappattern02, 0xFF000000, 0xFF0070FF);
		mSelIcon[3] = new SelectIconView(mContext, R.drawable.tappattern03, 0xFF000000, 0xFF00A0FF);

		mRateSel = new Spinner(mContext);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 選択肢追加
        Resources res = mContext.getResources();
        String rateStr = res.getString(R.string.opRateStr);
        for (int i = 0 ; i < SetImageText.RateDisp.length ; i ++) {
        	adapter.add(rateStr + " " + SetImageText.RateDisp[i]);
        }
        // アダプターを設定します
        mRateSel.setAdapter(adapter);

		scroll.addView(layout, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.addView(text, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_HORIZONTAL;
		layout.addView(layout2, lp);
		layout.addView(layout3, lp);

		layout2.addView(mSelIcon[0], new LinearLayout.LayoutParams(size, size));
		layout2.addView(mSelIcon[1], new LinearLayout.LayoutParams(size, size));
		layout3.addView(mSelIcon[2], new LinearLayout.LayoutParams(size, size));
		layout3.addView(mSelIcon[3], new LinearLayout.LayoutParams(size, size));

		layout.addView(mRateSel, lp);

		String str = (String) getDialogMessage();
		text.setText(str);
		// 項目作成
//		LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View layout = layoutInflater.inflate(R.layout.operation, null);
//
//		mSelIcon[0] = (SelectIconView)layout.findViewById(R.id.icon_00);
//		mSelIcon[1] = (SelectIconView)layout.findViewById(R.id.icon_01);
//		mSelIcon[2] = (SelectIconView)layout.findViewById(R.id.icon_02);
//		mSelIcon[3] = (SelectIconView)layout.findViewById(R.id.icon_03);
////		mRevCheck = (CheckBox)layout.findViewById(R.id.check_rev);

		int val;
		val = getPatternValue();
		for (int i = 0 ; i < ICON_NUM ; i ++) {
			mSelIcon[i].setSelect(i == val ? true : false);
			mSelIcon[i].setOnClickListener(this);
		}
		mSelIndex = val;

		val = getRateValue();
        mRateSel.setSelection(val);
		return scroll;
	}

	@Override
	public void onClick(View v) {
		// 選択
		for (int i = 0 ; i < ICON_NUM ; i ++) {
			mSelIcon[i].setSelect(mSelIcon[i] == v ? true : false);
			if (mSelIcon[i] == v) {
				mSelIndex = i;
			}
		}
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			int rate = mRateSel.getSelectedItemPosition();
			setValue(mSelIndex, rate);
		}
	}

	private void setValue(int pattern, int rate) {
		Editor ed = mSP.edit();
		ed.putInt(DEF.KEY_TAPPATTERN, pattern);
		ed.putInt(DEF.KEY_TAPRATE, rate);
		ed.commit();
	}

	private int getPatternValue() {
		int val = mSP.getInt(DEF.KEY_TAPPATTERN, DEF.DEFAULT_TAPPATTERN);
		return val;
	}

	private int getRateValue() {
		int val = mSP.getInt(DEF.KEY_TAPRATE, DEF.DEFAULT_TAPRATE);
		return val;
	}
}
