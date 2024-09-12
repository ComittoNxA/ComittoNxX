package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.config.SetTextActivity;
import src.comitton.dialog.ListDialog.ListSelectListener;

@SuppressLint("NewApi")
public class EpubConfigDialog extends Dialog implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private final int SELLIST_PICSIZE = 0;
	private final int SELLIST_ASCMODE = 1;

	private EpubConfigListenerInterface mListener = null;
	private Activity mContext;

	private ListDialog mListDialog;

	private int mBkLight;
	private int mFontBody;
	private int mFontInfo;
	private int mMarginW;
	private int mMarginH;
	private boolean mIsSave;

	private Button mBtnRevert;
	private Button mBtnApply;
	private Button mBtnOK;

	private TextView mTxtBkLight;
	private TextView mTxtFontBody;
	private TextView mTxtFontInfo;
	private TextView mTxtMarginW;
	private TextView mTxtMarginH;
	private SeekBar mSkbBkLight;
	private SeekBar mSkbFontBody;
	private SeekBar mSkbFontInfo;
	private SeekBar mSkbMarginW;
	private SeekBar mSkbMarginH;
	private CheckBox mChkIsSave;

	private String mBkLightStr;
	private String mFontBodyStr;
	private String mFontInfoStr;
	private String mMarginWStr;
	private String mMarginHStr;

	private String mDefaultStr;
	private String mSpUnitStr;
	private String mDotUnitStr;

	String mTitle;
	int mLayoutId;

	public EpubConfigDialog(Activity context) {
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
		PaintDrawable paintDrawable = new PaintDrawable(0x80000000);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		// 外をタッチすると閉じる
		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		mContext = context;

		int nItem;
	}

	public void setConfig(int bklight, int body, int info, int marginw, int marginh, boolean issave) {
		mBkLight = bklight;
		mFontBody = body;
		mFontInfo = info;
		mMarginW = marginw;
		mMarginH = marginh;

		mIsSave = issave;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.epubconfigdialog);

		mChkIsSave = (CheckBox) this.findViewById(R.id.chk_save);

		mChkIsSave.setChecked(mIsSave);

		mTxtBkLight = (TextView)this.findViewById(R.id.label_bklight);
		mTxtFontBody = (TextView)this.findViewById(R.id.label_fontbody);
		mTxtFontInfo = (TextView)this.findViewById(R.id.label_fontinfo);
		mTxtMarginW = (TextView)this.findViewById(R.id.label_marginw);
		mTxtMarginH = (TextView)this.findViewById(R.id.label_marginh);

		mBkLightStr  = mTxtBkLight.getText().toString();
		mFontBodyStr = mTxtFontBody.getText().toString();
		mFontInfoStr = mTxtFontInfo.getText().toString();
		mMarginWStr  = mTxtMarginW.getText().toString();
		mMarginHStr  = mTxtMarginH.getText().toString();

		mTxtBkLight.setText(mBkLightStr.replaceAll("%", getBkLight(mBkLight)));
		mTxtFontBody.setText(mFontBodyStr.replaceAll("%", getBkLight(mFontBody)));
		mTxtFontInfo.setText(mFontInfoStr.replaceAll("%", getBkLight(mFontInfo)));
		mTxtMarginW.setText(mMarginWStr.replaceAll("%", getBkLight(mMarginW)));
		mTxtMarginH.setText(mMarginHStr.replaceAll("%", getBkLight(mMarginH)));


		mSkbBkLight = (SeekBar)this.findViewById(R.id.seek_bklight);
		mSkbFontBody = (SeekBar)this.findViewById(R.id.seek_fontbody);
		mSkbFontInfo = (SeekBar)this.findViewById(R.id.seek_fontinfo);
		mSkbMarginW = (SeekBar)this.findViewById(R.id.seek_marginw);
		mSkbMarginH = (SeekBar)this.findViewById(R.id.seek_marginh);

		mSkbBkLight.setMax(11);
		mSkbBkLight.setOnSeekBarChangeListener(this);
		mSkbFontBody.setMax(56);
		mSkbFontBody.setOnSeekBarChangeListener(this);
		mSkbFontInfo.setMax(56);
		mSkbFontInfo.setOnSeekBarChangeListener(this);
		mSkbMarginW.setMax(50);
		mSkbMarginW.setOnSeekBarChangeListener(this);
		mSkbMarginH.setMax(50);
		mSkbMarginH.setOnSeekBarChangeListener(this);

		mSkbBkLight.setProgress(mBkLight);
		mSkbFontBody.setProgress(mFontBody);
		mSkbFontInfo.setProgress(mFontInfo);
		mSkbMarginW.setProgress(mMarginW);
		mSkbMarginH.setProgress(mMarginH);

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

	public void setEpubConfigListner(EpubConfigListenerInterface listener) {
		mListener = listener;
	}

	public interface EpubConfigListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onButtonSelect(int select, int bklight, int body, int info, int marginw, int margin, boolean issave);
	    public void onClose();
	}

	@Override
	public void onClick(View v) {
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
			mListener.onButtonSelect(select, mBkLight, mFontBody, mFontInfo, mMarginW, mMarginH, mIsSave);
		}
		else {
			// OK/適用は設定された値を通知
			boolean issave = mChkIsSave.isChecked();
			int bklight = mSkbBkLight.getProgress();
			int body = mSkbFontBody.getProgress();
			int info = mSkbFontInfo.getProgress();
			int marginw = mSkbMarginW.getProgress();
			int marginh = mSkbMarginH.getProgress();

			mListener.onButtonSelect(select, bklight, body, info, marginw, marginh, issave);
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
		else if (seekBar == mSkbFontBody || seekBar == mSkbFontInfo) {
			String str = DEF.getFontSpStr(progress, mSpUnitStr);
			if (seekBar == mSkbFontBody) {
				// 本文フォント
				mTxtFontBody.setText(mFontBodyStr.replaceAll("%", str));
			}
			else {
				// ヘッダフッタフォント
				mTxtFontInfo.setText(mFontInfoStr.replaceAll("%", str));
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