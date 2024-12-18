package src.comitton.dialog;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.config.SetImageActivity;
import src.comitton.dialog.ListDialog.ListSelectListener;
import jp.dip.muracoro.comittonx.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class ImageConfigDialog extends Dialog implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
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
	private Activity mContext;

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
	//private CheckBox mChkSharpen;
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
//	private Spinner mSpnBright;
//	private Spinner mSpnGamma;
//	private Spinner mSpnAlgoMode;
//	private Spinner mSpnDispMode;
//	private Spinner mSpnScaleMode;
//	private Spinner mSpnMgncut;
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

	String mTitle;
	int mLayoutId;

	public ImageConfigDialog(Activity context, int command_id) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Resources res = context.getResources();
		mAutoStr = res.getString(R.string.auto);
		mNoneStr = res.getString(R.string.none);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		//PaintDrawable paintDrawable = new PaintDrawable(0x80000000);
		//dlgWindow.setBackgroundDrawable(paintDrawable);
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

		// 外をタッチすると閉じる
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mCommandId = command_id;
		mContext = context;

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

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		// ビューのレイアウトをxmlファイルから取得し表示する要素を追加する
		setContentView(R.layout.imageconfigdialog);

		if (mCommandId != DEF.MENU_IMGCONF) {
			this.findViewById(R.id.chk_gray).setVisibility(View.GONE);
			this.findViewById(R.id.chk_invert).setVisibility(View.GONE);
			this.findViewById(R.id.chk_moire).setVisibility(View.GONE);
			this.findViewById(R.id.chk_topsingle).setVisibility(View.GONE);

			this.findViewById(R.id.label_algomode).setVisibility(View.GONE);
			this.findViewById(R.id.label_spread).setVisibility(View.GONE);
			this.findViewById(R.id.label_scale).setVisibility(View.GONE);
			this.findViewById(R.id.label_mgncut).setVisibility(View.GONE);
			this.findViewById(R.id.label_mgncutcolor).setVisibility(View.GONE);

			this.findViewById(R.id.btn_algomode).setVisibility(View.GONE);
			this.findViewById(R.id.btn_spread).setVisibility(View.GONE);
			this.findViewById(R.id.btn_scale).setVisibility(View.GONE);
			this.findViewById(R.id.btn_mgncut).setVisibility(View.GONE);
			this.findViewById(R.id.btn_mgncutcolor).setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_SHARPEN) {
			this.findViewById(R.id.label_sharpen).setVisibility(View.GONE);
			this.findViewById(R.id.seek_sharpen).setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_BRIGHT) {
			this.findViewById(R.id.label_bright).setVisibility(View.GONE);
			this.findViewById(R.id.seek_bright).setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_GAMMA) {
			this.findViewById(R.id.label_gamma).setVisibility(View.GONE);
			this.findViewById(R.id.seek_gamma).setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_BKLIGHT) {
			this.findViewById(R.id.label_bklight).setVisibility(View.GONE);
			this.findViewById(R.id.seek_bklight).setVisibility(View.GONE);
		}

		mChkGray = (CheckBox) this.findViewById(R.id.chk_gray);
		mChkInvert = (CheckBox) this.findViewById(R.id.chk_invert);
		mChkMoire = (CheckBox) this.findViewById(R.id.chk_moire);
		mChkTopSingle = (CheckBox) this.findViewById(R.id.chk_topsingle);
		mChkIsSave = (CheckBox) this.findViewById(R.id.chk_save);

		mChkGray.setChecked(mGray);
		mChkInvert.setChecked(mInvert);
		mChkMoire.setChecked(mMoire);
		mChkTopSingle.setChecked(mTopSingle);
		mChkIsSave.setChecked(mIsSave);

		mTxtSharpen = (TextView)this.findViewById(R.id.label_sharpen);
		mTxtBright = (TextView)this.findViewById(R.id.label_bright);
		mTxtGamma = (TextView)this.findViewById(R.id.label_gamma);
		mTxtBkLight = (TextView)this.findViewById(R.id.label_bklight);

		mSharpenStr = mTxtSharpen.getText().toString();
		mBrightStr = mTxtBright.getText().toString();
		mGammaStr = mTxtGamma.getText().toString();
		mBkLightStr = mTxtBkLight.getText().toString();

		mTxtSharpen.setText(mSharpenStr.replaceAll("%", getSharpenStr(mSharpen)));
		mTxtBright.setText(mBrightStr.replaceAll("%", getBrightGammaStr(mBright)));
		mTxtGamma.setText(mGammaStr.replaceAll("%", getBrightGammaStr(mGamma)));
		mTxtBkLight.setText(mBkLightStr.replaceAll("%", getBkLight(mBkLight)));

		mSkbSharpen = (SeekBar)this.findViewById(R.id.seek_sharpen);
		mSkbBright = (SeekBar)this.findViewById(R.id.seek_bright);
		mSkbGamma = (SeekBar)this.findViewById(R.id.seek_gamma);
		mSkbBkLight = (SeekBar)this.findViewById(R.id.seek_bklight);

		mSkbSharpen.setMax(32);
		mSkbSharpen.setOnSeekBarChangeListener(this);
		mSkbBright.setMax(10);
		mSkbBright.setOnSeekBarChangeListener(this);
		mSkbGamma.setMax(10);
		mSkbGamma.setOnSeekBarChangeListener(this);
		mSkbBkLight.setMax(11);
		mSkbBkLight.setOnSeekBarChangeListener(this);

		mSkbSharpen.setProgress(mSharpen);
		mSkbBright.setProgress(mBright + 5);
		mSkbGamma.setProgress(mGamma + 5);
		mSkbBkLight.setProgress(mBkLight);

//		mSpnAlgoMode = (Spinner) this.findViewById(R.id.spin_algomode);
//		mSpnDispMode = (Spinner) this.findViewById(R.id.spin_spread);
//		mSpnScaleMode = (Spinner) this.findViewById(R.id.spin_scale);
//		mSpnMgncut = (Spinner) this.findViewById(R.id.spin_mgncut);
//		mSpnMgncutColor = (Spinner) this.findViewById(R.id.spin_mgncut);

//		mSpnAlgoMode.setSelection(mAlgoMode);
//		mSpnDispMode.setSelection(mDispMode);
//		mSpnScaleMode.setSelection(mScaleMode);
//		mSpnMgncut.setSelection(mMgnCut);

		mBtnAlgoMode = (Button) this.findViewById(R.id.btn_algomode);
		mBtnDispMode = (Button) this.findViewById(R.id.btn_spread);
		mBtnScaleMode = (Button) this.findViewById(R.id.btn_scale);
		mBtnMgncut = (Button) this.findViewById(R.id.btn_mgncut);
		mBtnMgncutColor = (Button) this.findViewById(R.id.btn_mgncutcolor);

		mBtnAlgoMode.setText(mAlgoModeItems[mAlgoMode]);
		mBtnDispMode.setText(mDispModeItems[mDispMode]);
		mBtnScaleMode.setText(mScaleModeItems[mScaleMode]);
		mBtnMgncut.setText(mMgnCutItems[mMgnCut]);
		mBtnMgncutColor.setText(mMgnCutColorItems[mMgnCutColor]);

		mBtnAlgoMode.setOnClickListener(this);
		mBtnDispMode.setOnClickListener(this);
		mBtnScaleMode.setOnClickListener(this);
		mBtnMgncut.setOnClickListener(this);
		mBtnMgncutColor.setOnClickListener(this);

		mBtnOK  = (Button) this.findViewById(R.id.btn_ok);
		mBtnApply   = (Button) this.findViewById(R.id.btn_apply);
		mBtnRevert = (Button) this.findViewById(R.id.btn_revert);

		mBtnOK.setOnClickListener(this);
		mBtnApply.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);
	}

	// ダイアログを表示してもIMMERSIVEが解除されない方法
	// http://stackoverflow.com/questions/22794049/how-to-maintain-the-immersive-mode-in-dialogs
	/**
	 * An hack used to show the dialogs in Immersive Mode (that is with the NavBar hidden). To
	 * obtain this, the method makes the dialog not focusable before showing it, change the UI
	 * visibility of the window like the owner activity of the dialog and then (after showing it)
	 * makes the dialog focusable again.
	 */
	@Override
	public void show() {
		// Set the dialog to not focusable.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		// 設定をコピー
		copySystemUiVisibility();

		// Show the dialog with NavBar hidden.
		super.show();

		// Set the dialog to focusable again.
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	/**
	 * Copy the visibility of the Activity that has started the dialog {@link mActivity}. If the
	 * activity is in Immersive mode the dialog will be in Immersive mode too and vice versa.
	 */
	@SuppressLint("NewApi")
	private void copySystemUiVisibility() {
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
	        getWindow().getDecorView().setSystemUiVisibility(
	                mContext.getWindow().getDecorView().getSystemUiVisibility());
	    }
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
		mListDialog = new ListDialog(mContext, title, items, selIndex, false, new ListSelectListener() {
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
//			int algomode = mSpnAlgoMode.getSelectedItemPosition();
//			int dispmode = mSpnDispMode.getSelectedItemPosition();
//			int scalemode = mSpnScaleMode.getSelectedItemPosition();
//			int mgncut = mSpnMgncut.getSelectedItemPosition();
//			int mgncutcolor = mSpnMgncutColor.getSelectedItemPosition();


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