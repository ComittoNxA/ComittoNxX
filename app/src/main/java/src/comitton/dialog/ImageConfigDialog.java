package src.comitton.dialog;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.config.SetImageActivity;
import src.comitton.dialog.ListDialog.ListSelectListener;
import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.fragment.app.FragmentActivity;

@SuppressLint("NewApi")
public class ImageConfigDialog extends TabDialogFragment implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private final int SELLIST_ALGORITHM  = 0;
	private final int SELLIST_VIEW_MODE  = 1;
	private final int SELLIST_SCALE_MODE = 2;
	private final int SELLIST_MARGIN_CUT = 3;
	private final int SELLIST_MARGIN_CUTCOLOR = 4;

	private final int SCALENAME_ORDER[] = { 0, 1, 6, 2, 3, 7, 4, 5 };

	private ImageConfigListenerInterface mListener = null;
	private Activity mActivity;

	private ListDialog mListDialog;

	private boolean mInvert;
	private boolean mGray;
	private boolean mMoire;
	private boolean mTopSingle;
	private int mSharpen;
	private int mBright;
	private int mGamma;
	private int mBkLight;
	private int mAlgoMode;
	private int mDispMode;
	private int mScaleMode;
	private int mMgnCut;
	private int mMgnCutColor;
	private boolean mIsSave;

	private int mAlgoModeTemp;
	private int mDispModeTemp;
	private int mScaleModeTemp;
	private int mMgnCutTemp;
	private int mMgnCutColorTemp;

	private Button mBtnRevert;
	private Button mBtnApply;
	private Button mBtnOK;
	private CheckBox mChkGray;
	private CheckBox mChkInvert;
	private CheckBox mChkMoire;
	private CheckBox mChkTopSingle;
	private CheckBox mChkIsSave;
	private TextView mTxtSharpen;
	private TextView mTxtBright;
	private TextView mTxtGamma;
	private TextView mTxtBkLight;
	private SeekBar mSkbSharpen;
	private SeekBar mSkbBright;
	private SeekBar mSkbGamma;
	private SeekBar mSkbBkLight;
	private TextView mTxtAlgoMode;
	private TextView mTxtDispMode;
	private TextView mTxtScaleMode;
	private TextView mTxtMgncut;
	private TextView mTxtMgncutColor;
	private Button mBtnAlgoMode;
	private Button mBtnDispMode;
	private Button mBtnScaleMode;
	private Button mBtnMgncut;
	private Button mBtnMgncutColor;

	private String mAlgoModeTitle;
	private String mDispModeTitle;
	private String mScaleModeTitle;
	private String mMgnCutTitle;
	private String mMgnCutColorTitle;

	private String mSharpenStr;
	private String mBrightStr;
	private String mGammaStr;
	private String mBkLightStr;

	private String mAutoStr;
	private String mNoneStr;

	private String[] mAlgoModeItems;
	private String[] mDispModeItems;
	private String[] mScaleModeItems;
	private String[] mMgnCutItems;
	private String[] mMgnCutColorItems;

	private int mSelectMode;
	private int mCommandId;

	private int mX;
	private int mY;

	public ImageConfigDialog(FragmentActivity activity, int command_id, int cx, int cy, boolean isclose, MenuDialog.MenuSelectListener listener) {
		super(activity, cx, cy, isclose, listener);

		mActivity = activity;
		mX = cx;
		mY = cy;

		Resources res = activity.getResources();
		mAutoStr = res.getString(R.string.auto);
		mNoneStr = res.getString(R.string.none);

		mCommandId = command_id;

		int nItem;

		// 画像補間法の選択肢設定
		mAlgoModeTitle = res.getString(R.string.algoriMenu);
		nItem = SetImageActivity.AlgoModeName.length;
		mAlgoModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mAlgoModeItems[i] = res.getString(SetImageActivity.AlgoModeName[i]);
		}

		// 見開きモードの選択肢設定
		mDispModeTitle = res.getString(R.string.tguide02);
		nItem = SetImageActivity.ViewName.length;
		mDispModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mDispModeItems[i] = res.getString(SetImageActivity.ViewName[i]);
		}

		// サイズ設定の選択肢設定
		mScaleModeTitle = res.getString(R.string.tguide03);
		nItem = SetImageActivity.ScaleName.length;
		mScaleModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mScaleModeItems[i] = res.getString(SetImageActivity.ScaleName[SCALENAME_ORDER[i]]);
		}

		// 余白削除
		mMgnCutTitle = res.getString(R.string.mgnCutMenu);
		nItem = SetImageActivity.MgnCutName.length;
		mMgnCutItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mMgnCutItems[i] = res.getString(SetImageActivity.MgnCutName[i]);
		}

		// 余白削除の色
		mMgnCutColorTitle = res.getString(R.string.mgnCutColorMenu);
		nItem = SetImageActivity.MgnCutColorName.length;
		mMgnCutColorItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mMgnCutColorItems[i] = res.getString(SetImageActivity.MgnCutColorName[i]);
		}

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		addSection(res.getString(R.string.imgConfFilter));
		addItem(inflater.inflate(R.layout.imageconfig_filter, null, false));

		if (mCommandId == DEF.MENU_IMGCONF) {
			addSection(res.getString(R.string.imgConfOther));
		}
		addItem(inflater.inflate(R.layout.imageconfig_other, null, false));
	}

	public void setConfig(boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave) {
		mGray = gray;
		mInvert = invert;
		mMoire = moire;
		mTopSingle = topsingle;
		mSharpen = sharpen;
		mBright = bright;
		mGamma = gamma;
		mBkLight = bklight;
		mAlgoModeTemp  = mAlgoMode  = algomode;
		mDispModeTemp  = mDispMode  = dispmode;
		mScaleModeTemp = mScaleMode = scalemode;
		mMgnCutTemp    = mMgnCut    = mgncut;
		mMgnCutColorTemp    = mMgnCutColor    = mgncutcolor;

		mIsSave = issave;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		Resources res = mActivity.getResources();
		addHeader(res.getString(R.string.imgConfMenu));

		LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.imagetextconfig_footer, null, false);
		footer.setBackgroundColor(0x80000000);
		addFooter(footer);

		mView.getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
			@Override
			public void onWindowFocusChanged(boolean hasFocus) {
				// ビューページャーのサイズを設定する
				ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
				layoutParams.width = mWidth;
				layoutParams.height = mHeight - mHeader.getHeight() - mTabLayout.getHeight() - mFooter.getHeight();
				mViewPager.setLayoutParams(layoutParams);
			}
		});

		mChkIsSave = (CheckBox) mView.findViewById(R.id.chk_save);

		for( int i = 0; i < mViewArray.size(); ++i) {
			mChkGray = mChkGray != null ? mChkGray : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_gray);
			mChkInvert = mChkInvert != null ? mChkInvert : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_invert);
			mChkMoire = mChkMoire != null ? mChkMoire : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_moire);
			mChkTopSingle = mChkTopSingle != null ? mChkTopSingle : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_topsingle);

			mTxtSharpen = mTxtSharpen != null ? mTxtSharpen : (TextView) mViewArray.get(i).findViewById(R.id.label_sharpen);
			mTxtBright = mTxtBright != null ? mTxtBright : (TextView) mViewArray.get(i).findViewById(R.id.label_bright);
			mTxtGamma = mTxtGamma != null ? mTxtGamma : (TextView) mViewArray.get(i).findViewById(R.id.label_gamma);
			mTxtBkLight = mTxtBkLight != null ? mTxtBkLight : (TextView) mViewArray.get(i).findViewById(R.id.label_bklight);

			mSkbSharpen = mSkbSharpen != null ? mSkbSharpen : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_sharpen);
			mSkbBright = mSkbBright != null ? mSkbBright : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_bright);
			mSkbGamma = mSkbGamma != null ? mSkbGamma : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_gamma);
			mSkbBkLight = mSkbBkLight != null ? mSkbBkLight : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_bklight);

			mTxtAlgoMode = mTxtAlgoMode != null ? mTxtAlgoMode : (TextView) mViewArray.get(i).findViewById(R.id.label_algomode);
			mTxtDispMode = mTxtDispMode != null ? mTxtDispMode : (TextView) mViewArray.get(i).findViewById(R.id.label_spread);
			mTxtScaleMode = mTxtScaleMode != null ? mTxtScaleMode : (TextView) mViewArray.get(i).findViewById(R.id.label_scale);
			mTxtMgncut = mTxtMgncut != null ? mTxtMgncut : (TextView) mViewArray.get(i).findViewById(R.id.label_mgncut);
			mTxtMgncutColor = mTxtMgncutColor != null ? mTxtMgncutColor : (TextView) mViewArray.get(i).findViewById(R.id.label_mgncutcolor);

			mBtnAlgoMode = mBtnAlgoMode != null ? mBtnAlgoMode : (Button) mViewArray.get(i).findViewById(R.id.btn_algomode);
			mBtnDispMode = mBtnDispMode != null ? mBtnDispMode : (Button) mViewArray.get(i).findViewById(R.id.btn_spread);
			mBtnScaleMode = mBtnScaleMode != null ? mBtnScaleMode : (Button) mViewArray.get(i).findViewById(R.id.btn_scale);
			mBtnMgncut = mBtnMgncut != null ? mBtnMgncut : (Button) mViewArray.get(i).findViewById(R.id.btn_mgncut);
			mBtnMgncutColor = mBtnMgncutColor != null ? mBtnMgncutColor : (Button) mViewArray.get(i).findViewById(R.id.btn_mgncutcolor);
		}



		if (mCommandId != DEF.MENU_IMGCONF) {
			mChkGray.setVisibility(View.GONE);
			mChkInvert.setVisibility(View.GONE);
			mChkMoire.setVisibility(View.GONE);
			mChkTopSingle.setVisibility(View.GONE);

			mTxtAlgoMode.setVisibility(View.GONE);
			mTxtDispMode.setVisibility(View.GONE);
			mTxtScaleMode.setVisibility(View.GONE);
			mTxtMgncut.setVisibility(View.GONE);
			mTxtMgncutColor.setVisibility(View.GONE);

			mBtnAlgoMode.setVisibility(View.GONE);
			mBtnDispMode.setVisibility(View.GONE);
			mBtnScaleMode.setVisibility(View.GONE);
			mBtnMgncut.setVisibility(View.GONE);
			mBtnMgncutColor.setVisibility(View.GONE);

		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_SHARPEN) {
			mTxtSharpen.setVisibility(View.GONE);
			mSkbSharpen.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_BRIGHT) {
			mTxtBright.setVisibility(View.GONE);
			mSkbBright.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_GAMMA) {
			mTxtGamma.setVisibility(View.GONE);
			mSkbGamma.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_BKLIGHT) {
			mTxtBkLight.setVisibility(View.GONE);
			mSkbBkLight.setVisibility(View.GONE);
		}

		if (mChkGray != null) mChkGray.setChecked(mGray);
		if (mChkInvert != null) mChkInvert.setChecked(mInvert);
		if (mChkMoire != null) mChkMoire.setChecked(mMoire);
		if (mChkTopSingle != null) mChkTopSingle.setChecked(mTopSingle);
		if (mChkIsSave != null) mChkIsSave.setChecked(mIsSave);

		if (mTxtSharpen != null) mSharpenStr = mTxtSharpen.getText().toString();
		if (mTxtBright != null) mBrightStr = mTxtBright.getText().toString();
		if (mTxtGamma != null) mGammaStr = mTxtGamma.getText().toString();
		if (mTxtBkLight != null) mBkLightStr = mTxtBkLight.getText().toString();

		if (mTxtSharpen != null && mTxtSharpen != null) mTxtSharpen.setText(mSharpenStr.replaceAll("%", getSharpenStr(mSharpen)));
		if (mTxtBright != null && mTxtBright != null) mTxtBright.setText(mBrightStr.replaceAll("%", getBrightGammaStr(mBright)));
		if (mTxtGamma != null && mTxtGamma != null) mTxtGamma.setText(mGammaStr.replaceAll("%", getBrightGammaStr(mGamma)));
		if (mTxtBkLight != null && mTxtBkLight != null) mTxtBkLight.setText(mBkLightStr.replaceAll("%", getBkLight(mBkLight)));

		if (mSkbSharpen != null) mSkbSharpen.setMax(32);
		if (mSkbSharpen != null) mSkbSharpen.setOnSeekBarChangeListener(this);
		if (mSkbBright != null) mSkbBright.setMax(10);
		if (mSkbBright != null) mSkbBright.setOnSeekBarChangeListener(this);
		if (mSkbGamma != null) mSkbGamma.setMax(10);
		if (mSkbGamma != null) mSkbGamma.setOnSeekBarChangeListener(this);
		if (mSkbBkLight != null) mSkbBkLight.setMax(11);
		if (mSkbBkLight != null) mSkbBkLight.setOnSeekBarChangeListener(this);

		if (mSkbSharpen != null) mSkbSharpen.setProgress(mSharpen);
		if (mSkbBright != null) mSkbBright.setProgress(mBright + 5);
		if (mSkbGamma != null) mSkbGamma.setProgress(mGamma + 5);
		if (mSkbBkLight != null) mSkbBkLight.setProgress(mBkLight);

		if (mBtnAlgoMode != null) mBtnAlgoMode.setText(mAlgoModeItems[mAlgoMode]);
		if (mBtnDispMode != null) mBtnDispMode.setText(mDispModeItems[mDispMode]);
		if (mBtnScaleMode != null) mBtnScaleMode.setText(mScaleModeItems[mScaleMode]);
		if (mBtnMgncut != null) mBtnMgncut.setText(mMgnCutItems[mMgnCut]);
		if (mBtnMgncutColor != null) mBtnMgncutColor.setText(mMgnCutColorItems[mMgnCutColor]);

		if (mBtnAlgoMode != null) mBtnAlgoMode.setOnClickListener(this);
		if (mBtnDispMode != null) mBtnDispMode.setOnClickListener(this);
		if (mBtnScaleMode != null) mBtnScaleMode.setOnClickListener(this);
		if (mBtnMgncut != null) mBtnMgncut.setOnClickListener(this);
		if (mBtnMgncutColor != null) mBtnMgncutColor.setOnClickListener(this);

		mBtnOK = (Button) mView.findViewById(R.id.btn_ok);
		mBtnApply = (Button) mView.findViewById(R.id.btn_apply);
		mBtnRevert = (Button) mView.findViewById(R.id.btn_revert);

		mBtnOK.setOnClickListener(this);
		mBtnApply.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);

		return mView;
	}

	public void setImageConfigListner(ImageConfigListenerInterface listener) {
		mListener = listener;
	}

	public interface ImageConfigListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onButtonSelect(int select, boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave);
	    public void onClose();
	}

	private void showSelectList(int index) {
		if (mListDialog != null) {
			return;
		}

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;

		String title;
		int selIndex;
		switch (index) {
			case SELLIST_ALGORITHM:
				// 画像補間法の選択肢設定
				title = mAlgoModeTitle;
				items = mAlgoModeItems;
				selIndex = mAlgoModeTemp;
				break;
			case SELLIST_VIEW_MODE:
				// 見開きモードの選択肢設定
				title = mDispModeTitle;
				items = mDispModeItems;
				selIndex = mDispModeTemp;
				break;
			case SELLIST_SCALE_MODE:
				// サイズ設定の選択肢設定
				title = mScaleModeTitle;
				items = mScaleModeItems;
				selIndex = mScaleModeTemp;
				break;
			case SELLIST_MARGIN_CUT:
				// 余白削除
				title = mMgnCutTitle;
				items = mMgnCutItems;
				selIndex = mMgnCutTemp;
				break;
			case SELLIST_MARGIN_CUTCOLOR:
				// 余白削除
				title = mMgnCutColorTitle;
				items = mMgnCutColorItems;
				selIndex = mMgnCutColorTemp;
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(mActivity, mX, mY, title, items, selIndex, false, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_ALGORITHM:
						// 画像補間法
						mAlgoModeTemp = index;
						mBtnAlgoMode.setText(mAlgoModeItems[index]);
						break;
					case SELLIST_VIEW_MODE:
						// 見開き設定変更
						mDispModeTemp = index;
						mBtnDispMode.setText(mDispModeItems[index]);
						break;
					case SELLIST_SCALE_MODE: {
						// 画像拡大率の変更
						mScaleModeTemp = index;
						mBtnScaleMode.setText(mScaleModeItems[index]);
						break;
					}
					case SELLIST_MARGIN_CUT:
						// 余白削除
						mMgnCutTemp = index;
						mBtnMgncut.setText(mMgnCutItems[index]);
						break;
					case SELLIST_MARGIN_CUTCOLOR:
						// 余白削除
						mMgnCutColorTemp = index;
						mBtnMgncutColor.setText(mMgnCutColorItems[index]);
						break;
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
		return;
	}

	@Override
	public void onClick(View v) {
		if (mBtnAlgoMode == v) {
			// 画像補間法
			showSelectList(SELLIST_ALGORITHM);
			return;
		}
		if (mBtnDispMode == v) {
			// 画像補間法
			showSelectList(SELLIST_VIEW_MODE);
			return;
		}
		if (mBtnScaleMode == v) {
			// 画像補間法
			showSelectList(SELLIST_SCALE_MODE);
			return;
		}
		if (mBtnMgncut == v) {
			// 画像補間法
			showSelectList(SELLIST_MARGIN_CUT);
			return;
		}
		if (mBtnMgncutColor == v) {
			// 画像補間法
			showSelectList(SELLIST_MARGIN_CUTCOLOR);
			return;
		}

		int select = CLICK_REVERT;

		// ボタンクリック
		if (mBtnOK == v) {
			select = CLICK_OK;
		}
		else if (mBtnApply == v) {
			select = CLICK_APPLY;
		}

		if (select == CLICK_REVERT) {
			// 戻すは元の値を通知
			mListener.onButtonSelect(select, mGray, mInvert, mMoire, mTopSingle, mSharpen, mBright, mGamma, mBkLight, mAlgoMode, mDispMode, mScaleMode, mMgnCut, mMgnCutColor, mIsSave);
		}
		else {
			// OK/適用は設定された値を通知
			boolean gray = mChkGray.isChecked();
			boolean invert = mChkInvert.isChecked();
			boolean moire = mChkMoire.isChecked();
			boolean topsingle = mChkTopSingle.isChecked();
			boolean issave = mChkIsSave.isChecked();
			int sharpen = mSkbSharpen.getProgress();
			int bright = mSkbBright.getProgress() - 5;
			int gamma = mSkbGamma.getProgress() - 5;
			int bklight = mSkbBkLight.getProgress();

			mListener.onButtonSelect(select, gray, invert, moire, topsingle, sharpen, bright, gamma, bklight, mAlgoModeTemp, mDispModeTemp, mScaleModeTemp, mMgnCutTemp, mMgnCutColorTemp, issave);
		}

		if (select != CLICK_APPLY) {
			// 適用以外では閉じる
			dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 変更通知
		if (seekBar == mSkbSharpen) {
			String str = getSharpenStr(progress);
			mTxtSharpen.setText(mSharpenStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbBkLight) {
			String str = getBkLight(progress);
			mTxtBkLight.setText(mBkLightStr.replaceAll("%", str));
		}
		else {
			String str = getBrightGammaStr(progress);
			if (seekBar == mSkbBright) {
				mTxtBright.setText(mBrightStr.replaceAll("%", str));
			}
			else {
				mTxtGamma.setText(mGammaStr.replaceAll("%", str));
			}
		}
	}

	private String getSharpenStr(int progress) {
		String str;
		if (progress == 0) {
			str = mNoneStr;
		}
		else if (progress < 16) {
			str = String.valueOf(progress % 16) + "/16";
		}
		else if (progress % 16 == 0) {
			str = String.valueOf(progress / 16);
		}
		else {
			str =  String.valueOf(progress / 16) + " + " + String.valueOf(progress % 16) + "/16";
		}
		return str;
	}

	private String getBkLight(int progress) {
		String str;
		if (progress >= 11) {
			str = mAutoStr;
		}
		else {
			str = String.valueOf(progress * 10) + "%";
		}
		return str;
	}

	private String getBrightGammaStr(int progress) {
		String str;
		if (progress == 5) {
			str = mNoneStr;
		}
		else if (progress < 5) {
			str = String.valueOf(progress - 5);
		}
		else {
			str = "+" + String.valueOf(progress - 5);
		}
		return str;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// シークバーのトラッキング開始
		return;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// シークバーのトラッキング終了
		return;
	}
}