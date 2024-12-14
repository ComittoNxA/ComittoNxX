package src.comitton.dialog;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.config.SetTextActivity;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class TextConfigDialog extends Dialog implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private final int SELLIST_PICSIZE = 0;
	private final int SELLIST_ASCMODE = 1;

	private TextConfigListenerInterface mListener = null;
	private Activity mContext;

	private ListDialog mListDialog;

	private int mPicSize;
	private int mBkLight;
	private int mFontTop;
	private int mFontBody;
	private int mFontRubi;
	private int mFontInfo;
	private int mSpaceW;
	private int mSpaceH;
	private int mMarginW;
	private int mMarginH;
	private int mAscMode;
	private boolean mIsSave;

	// ボタンは現在値を覚える必要がある
	private int mPicSizeTemp;
	private int mAscModeTemp;

	private Button mBtnRevert;
	private Button mBtnApply;
	private Button mBtnOK;

	private TextView mTxtBkLight;
	private TextView mTxtFontTop;
	private TextView mTxtFontBody;
	private TextView mTxtFontRubi;
	private TextView mTxtFontInfo;
	private TextView mTxtSpaceW;
	private TextView mTxtSpaceH;
	private TextView mTxtMarginW;
	private TextView mTxtMarginH;
	private Button mBtnPicSize;
	private Button mBtnAscMode;
	private SeekBar mSkbBkLight;
	private SeekBar mSkbFontTop;
	private SeekBar mSkbFontBody;
	private SeekBar mSkbFontRubi;
	private SeekBar mSkbFontInfo;
	private SeekBar mSkbSpaceW;
	private SeekBar mSkbSpaceH;
	private SeekBar mSkbMarginW;
	private SeekBar mSkbMarginH;
	private CheckBox mChkIsSave;

	private String mPicSizeTitle;
	private String mAscModeTitle;

	private String mBkLightStr;
	private String mFontTopStr;
	private String mFontBodyStr;
	private String mFontRubiStr;
	private String mFontInfoStr;
	private String mSpaceWStr;
	private String mSpaceHStr;
	private String mMarginWStr;
	private String mMarginHStr;

	private String mDefaultStr;
	private String mSpUnitStr;
	private String mDotUnitStr;

	private String[] mPicSizeItems;
	private String[] mAscModeItems;

	private int mSelectMode;

	String mTitle;
	int mLayoutId;

	public TextConfigDialog(Activity context) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Resources res = context.getResources();
		mDefaultStr = res.getString(R.string.auto);
		mSpUnitStr = res.getString(R.string.unitSumm1);
		mDotUnitStr = res.getString(R.string.rangeSumm1);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		//PaintDrawable paintDrawable = new PaintDrawable(0x80000000);
		//dlgWindow.setBackgroundDrawable(paintDrawable);
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe);

		// 外をタッチすると閉じる
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mContext = context;

		int nItem;

		// 挿絵サイズの選択肢設定
		mPicSizeTitle = res.getString(R.string.picSize);
		nItem = SetTextActivity.PicSizeName.length;
		mPicSizeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mPicSizeItems[i] = res.getString(SetTextActivity.PicSizeName[i]);
		}

		mAscModeTitle = res.getString(R.string.ascMode);
		nItem = SetTextActivity.AscModeName.length;
		mAscModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mAscModeItems[i] = res.getString(SetTextActivity.AscModeName[i]);
		}
	}

	public void setConfig(int picsize, int bklight, int top, int body, int rubi, int info, int spacew, int spaceh, int marginw, int marginh, int ascmode, boolean issave) {
		mPicSizeTemp = mPicSize = picsize;
		mAscModeTemp = mAscMode = ascmode;
		mBkLight = bklight;
		mFontTop = top;
		mFontBody = body;
		mFontRubi = rubi;
		mFontInfo = info;
		mSpaceW = spacew;
		mSpaceH = spaceh;
		mMarginW = marginw;
		mMarginH = marginh;

		mIsSave = issave;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.textconfigdialog);

		TextView textView = (TextView)this.findViewById(R.id.text_message);
		ScrollView scrollView = (ScrollView)this.findViewById(R.id.ScrollView);

		int width = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.80);
		int scale = (int)(mContext.getResources().getDisplayMetrics().scaledDensity * 320);
		width = Math.max(width, scale);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(lp);
		scrollView.setLayoutParams(lp);

		mChkIsSave = (CheckBox) this.findViewById(R.id.chk_save);

		mChkIsSave.setChecked(mIsSave);

		mTxtBkLight = (TextView)this.findViewById(R.id.label_bklight);
		mTxtFontTop = (TextView)this.findViewById(R.id.label_fonttop);
		mTxtFontBody = (TextView)this.findViewById(R.id.label_fontbody);
		mTxtFontRubi = (TextView)this.findViewById(R.id.label_fontrubi);
		mTxtFontInfo = (TextView)this.findViewById(R.id.label_fontinfo);
		mTxtSpaceW = (TextView)this.findViewById(R.id.label_spacew);
		mTxtSpaceH = (TextView)this.findViewById(R.id.label_spaceh);
		mTxtMarginW = (TextView)this.findViewById(R.id.label_marginw);
		mTxtMarginH = (TextView)this.findViewById(R.id.label_marginh);

		mBkLightStr  = mTxtBkLight.getText().toString();
		mFontTopStr  = mTxtFontTop.getText().toString();
		mFontBodyStr = mTxtFontBody.getText().toString();
		mFontRubiStr = mTxtFontRubi.getText().toString();
		mFontInfoStr = mTxtFontInfo.getText().toString();
		mSpaceWStr   = mTxtSpaceW.getText().toString();
		mSpaceHStr   = mTxtSpaceH.getText().toString();
		mMarginWStr  = mTxtMarginW.getText().toString();
		mMarginHStr  = mTxtMarginH.getText().toString();

		mTxtBkLight.setText(mBkLightStr.replaceAll("%", getBkLight(mBkLight)));
		mTxtFontTop.setText(mFontTopStr.replaceAll("%", getBkLight(mFontTop)));
		mTxtFontBody.setText(mFontBodyStr.replaceAll("%", getBkLight(mFontBody)));
		mTxtFontRubi.setText(mFontRubiStr.replaceAll("%", getBkLight(mFontRubi)));
		mTxtFontInfo.setText(mFontInfoStr.replaceAll("%", getBkLight(mFontInfo)));
		mTxtSpaceW.setText(mSpaceWStr.replaceAll("%", getBkLight(mSpaceW)));
		mTxtSpaceH.setText(mSpaceHStr.replaceAll("%", getBkLight(mSpaceH)));
		mTxtMarginW.setText(mMarginWStr.replaceAll("%", getBkLight(mMarginW)));
		mTxtMarginH.setText(mMarginHStr.replaceAll("%", getBkLight(mMarginH)));


		mSkbBkLight = (SeekBar)this.findViewById(R.id.seek_bklight);
		mSkbFontTop = (SeekBar)this.findViewById(R.id.seek_fonttop);
		mSkbFontBody = (SeekBar)this.findViewById(R.id.seek_fontbody);
		mSkbFontRubi = (SeekBar)this.findViewById(R.id.seek_fontrubi);
		mSkbFontInfo = (SeekBar)this.findViewById(R.id.seek_fontinfo);
		mSkbSpaceW = (SeekBar)this.findViewById(R.id.seek_spacew);
		mSkbSpaceH = (SeekBar)this.findViewById(R.id.seek_spaceh);
		mSkbMarginW = (SeekBar)this.findViewById(R.id.seek_marginw);
		mSkbMarginH = (SeekBar)this.findViewById(R.id.seek_marginh);

		mSkbBkLight.setMax(11);
		mSkbBkLight.setOnSeekBarChangeListener(this);
		mSkbFontTop.setMax(56);
		mSkbFontTop.setOnSeekBarChangeListener(this);
		mSkbFontBody.setMax(56);
		mSkbFontBody.setOnSeekBarChangeListener(this);
		mSkbFontRubi.setMax(56);
		mSkbFontRubi.setOnSeekBarChangeListener(this);
		mSkbFontInfo.setMax(56);
		mSkbFontInfo.setOnSeekBarChangeListener(this);
		mSkbSpaceW.setMax(50);
		mSkbSpaceW.setOnSeekBarChangeListener(this);
		mSkbSpaceH.setMax(50);
		mSkbSpaceH.setOnSeekBarChangeListener(this);
		mSkbMarginW.setMax(50);
		mSkbMarginW.setOnSeekBarChangeListener(this);
		mSkbMarginH.setMax(50);
		mSkbMarginH.setOnSeekBarChangeListener(this);

		mSkbBkLight.setProgress(mBkLight);
		mSkbFontTop.setProgress(mFontTop);
		mSkbFontBody.setProgress(mFontBody);
		mSkbFontRubi.setProgress(mFontRubi);
		mSkbFontInfo.setProgress(mFontInfo);
		mSkbSpaceW.setProgress(mSpaceW);
		mSkbSpaceH.setProgress(mSpaceH);
		mSkbMarginW.setProgress(mMarginW);
		mSkbMarginH.setProgress(mMarginH);

//		mSpnAlgoMode = (Spinner) this.findViewById(R.id.spin_algomode);
//		mSpnDispMode = (Spinner) this.findViewById(R.id.spin_spread);
//		mSpnScaleMode = (Spinner) this.findViewById(R.id.spin_scale);
//		mSpnMgncut = (Spinner) this.findViewById(R.id.spin_mgncut);

//		mSpnAlgoMode.setSelection(mAlgoMode);
//		mSpnDispMode.setSelection(mDispMode);
//		mSpnScaleMode.setSelection(mScaleMode);
//		mSpnMgncut.setSelection(mMgnCut);

		mBtnPicSize = (Button) this.findViewById(R.id.btn_picsize);
		mBtnAscMode = (Button) this.findViewById(R.id.btn_ascmode);

		mBtnPicSize.setText(mPicSizeItems[mPicSize]);
		mBtnAscMode.setText(mAscModeItems[mAscMode]);

		mBtnPicSize.setOnClickListener(this);
		mBtnAscMode.setOnClickListener(this);

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

	public void setTextConfigListner(TextConfigListenerInterface listener) {
		mListener = listener;
	}

	public interface TextConfigListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onButtonSelect(int select, int picsize, int bklight, int top, int body, int rubi, int info, int spacew, int spaceh, int marginw, int margin, int ascmode, boolean issave);
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
			case SELLIST_PICSIZE:
				// 画像補間法の選択肢設定
				title = mPicSizeTitle;
				items = mPicSizeItems;
				selIndex = mPicSizeTemp;
				break;
			case SELLIST_ASCMODE:
				// 画像補間法の選択肢設定
				title = mAscModeTitle;
				items = mAscModeItems;
				selIndex = mAscModeTemp;
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(mContext, title, items, selIndex, false, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_PICSIZE:
						// 画像補間法
						mPicSizeTemp = index;
						mBtnPicSize.setText(mPicSizeItems[index]);
						break;
					case SELLIST_ASCMODE:
						// 画像補間法
						mAscModeTemp = index;
						mBtnAscMode.setText(mAscModeItems[index]);
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
		if (mBtnPicSize == v) {
			// 画像補間法
			showSelectList(SELLIST_PICSIZE);
			return;
		}
		else if (mBtnAscMode == v) {
			// 半角文字表示形式
			showSelectList(SELLIST_ASCMODE);
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
			mListener.onButtonSelect(select, mPicSize, mBkLight, mFontTop, mFontBody, mFontRubi, mFontInfo, mSpaceW, mSpaceH, mMarginW, mMarginH, mAscMode, mIsSave);
		}
		else {
			// OK/適用は設定された値を通知
			boolean issave = mChkIsSave.isChecked();
			int bklight = mSkbBkLight.getProgress();
			int top = mSkbFontTop.getProgress();
			int body = mSkbFontBody.getProgress();
			int rubi = mSkbFontRubi.getProgress();
			int info = mSkbFontInfo.getProgress();
			int spacew = mSkbSpaceW.getProgress();
			int spaceh = mSkbSpaceH.getProgress();
			int marginw = mSkbMarginW.getProgress();
			int marginh = mSkbMarginH.getProgress();

			mListener.onButtonSelect(select, mPicSizeTemp, bklight, top, body, rubi, info, spacew, spaceh, marginw, marginh, mAscModeTemp, issave);
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
		if (seekBar == mSkbBkLight) {
			String str = getBkLight(progress);
			mTxtBkLight.setText(mBkLightStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbFontTop || seekBar == mSkbFontBody || seekBar == mSkbFontRubi || seekBar == mSkbFontInfo) {
			String str = DEF.getFontSpStr(progress, mSpUnitStr);
			if (seekBar == mSkbFontTop) {
				// 見出しフォント
				mTxtFontTop.setText(mFontTopStr.replaceAll("%", str));
			}
			else if (seekBar == mSkbFontBody) {
				// 本文フォント
				mTxtFontBody.setText(mFontBodyStr.replaceAll("%", str));
			}
			else if (seekBar == mSkbFontRubi) {
				// ルビフォント
				mTxtFontRubi.setText(mFontRubiStr.replaceAll("%", str));
			}
			else {
				// ヘッダフッタフォント
				mTxtFontInfo.setText(mFontInfoStr.replaceAll("%", str));
			}
		}
		else if (seekBar == mSkbSpaceW || seekBar == mSkbSpaceH) {
			String str = DEF.getTextSpaceStr(progress, mDotUnitStr);
			if (seekBar == mSkbSpaceW) {
				// 行間
				mTxtSpaceW.setText(mSpaceWStr.replaceAll("%", str));
			}
			else {
				// 字間
				mTxtSpaceH.setText(mSpaceHStr.replaceAll("%", str));
			}
		}
		else if (seekBar == mSkbMarginW || seekBar == mSkbMarginH) {
			String str = DEF.getDispMarginStr(progress, mDotUnitStr);
			if (seekBar == mSkbMarginW) {
				// 左右余白
				mTxtMarginW.setText(mMarginWStr.replaceAll("%", str));
			}
			else {
				// 上下余白
				mTxtMarginH.setText(mMarginHStr.replaceAll("%", str));
			}
		}
		return;
	}

	private String getBkLight(int progress) {
		String str;
		if (progress >= 11) {
			str = mDefaultStr;
		}
		else {
			str = String.valueOf(progress * 10) + "%";
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