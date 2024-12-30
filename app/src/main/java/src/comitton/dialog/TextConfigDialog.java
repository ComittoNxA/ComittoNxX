package src.comitton.dialog;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.config.SetTextActivity;
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
public class TextConfigDialog extends TabDialogFragment implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private final int SELLIST_PICSIZE = 0;
	private final int SELLIST_ASCMODE = 1;

	private TextConfigListenerInterface mListener = null;
	private Activity mActivity;

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

	private int mX;
	private int mY;

	public TextConfigDialog(FragmentActivity activity, int cx, int cy, boolean isclose, MenuDialog.MenuSelectListener listener) {
		super(activity, cx, cy, isclose, listener);

		mActivity = activity;
		mX = cx;
		mY = cy;

		Resources res = mActivity.getResources();
		mDefaultStr = res.getString(R.string.auto);
		mSpUnitStr = res.getString(R.string.unitSumm1);
		mDotUnitStr = res.getString(R.string.rangeSumm1);

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

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		addSection(res.getString(R.string.txtConfFormat));
		addItem(inflater.inflate(R.layout.textconfig_format, null, false));

		addSection(res.getString(R.string.txtConfOther));
		addItem(inflater.inflate(R.layout.textconfig_other, null, false));
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		Resources res = mActivity.getResources();
		addHeader(res.getString(R.string.txtConfMenu));

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

		mChkIsSave.setChecked(mIsSave);

		for( int i = 0; i < mViewArray.size(); ++i) {
			mTxtBkLight = mTxtBkLight != null ? mTxtBkLight : (TextView) mViewArray.get(i).findViewById(R.id.label_bklight);
			mTxtFontTop = mTxtFontTop != null ? mTxtFontTop : (TextView) mViewArray.get(i).findViewById(R.id.label_fonttop);
			mTxtFontBody = mTxtFontBody != null ? mTxtFontBody : (TextView) mViewArray.get(i).findViewById(R.id.label_fontbody);
			mTxtFontRubi = mTxtFontRubi != null ? mTxtFontRubi : (TextView) mViewArray.get(i).findViewById(R.id.label_fontrubi);
			mTxtFontInfo = mTxtFontInfo != null ? mTxtFontInfo : (TextView) mViewArray.get(i).findViewById(R.id.label_fontinfo);
			mTxtSpaceW = mTxtSpaceW != null ? mTxtSpaceW : (TextView) mViewArray.get(i).findViewById(R.id.label_spacew);
			mTxtSpaceH = mTxtSpaceH != null ? mTxtSpaceH : (TextView) mViewArray.get(i).findViewById(R.id.label_spaceh);
			mTxtMarginW = mTxtMarginW != null ? mTxtMarginW : (TextView) mViewArray.get(i).findViewById(R.id.label_marginw);
			mTxtMarginH = mTxtMarginH != null ? mTxtMarginH : (TextView) mViewArray.get(i).findViewById(R.id.label_marginh);

			mSkbBkLight = mSkbBkLight != null ? mSkbBkLight : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_bklight);
			mSkbFontTop = mSkbFontTop != null ? mSkbFontTop : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fonttop);
			mSkbFontBody = mSkbFontBody != null ? mSkbFontBody : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fontbody);
			mSkbFontRubi = mSkbFontRubi != null ? mSkbFontRubi : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fontrubi);
			mSkbFontInfo = mSkbFontInfo != null ? mSkbFontInfo : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_fontinfo);
			mSkbSpaceW = mSkbSpaceW != null ? mSkbSpaceW : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_spacew);
			mSkbSpaceH = mSkbSpaceH != null ? mSkbSpaceH : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_spaceh);
			mSkbMarginW = mSkbMarginW != null ? mSkbMarginW : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_marginw);
			mSkbMarginH = mSkbMarginH != null ? mSkbMarginH : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_marginh);

			mBtnPicSize = mBtnPicSize != null ? mBtnPicSize : (Button) mViewArray.get(i).findViewById(R.id.btn_picsize);
			mBtnAscMode = mBtnAscMode != null ? mBtnAscMode : (Button) mViewArray.get(i).findViewById(R.id.btn_ascmode);
		}

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

		mBtnPicSize.setText(mPicSizeItems[mPicSize]);
		mBtnAscMode.setText(mAscModeItems[mAscMode]);

		mBtnPicSize.setOnClickListener(this);
		mBtnAscMode.setOnClickListener(this);

		mBtnOK = (Button) mView.findViewById(R.id.btn_ok);
		mBtnApply = (Button) mView.findViewById(R.id.btn_apply);
		mBtnRevert = (Button) mView.findViewById(R.id.btn_revert);

		mBtnOK.setOnClickListener(this);
		mBtnApply.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);

		return mView;
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
		mListDialog = new ListDialog(mActivity, mX, mY, title, items, selIndex, false, new ListSelectListener() {
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