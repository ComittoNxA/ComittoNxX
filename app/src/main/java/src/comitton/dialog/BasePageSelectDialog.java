package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.appcompat.widget.AppCompatImageButton;

import java.util.Arrays;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.listener.PageSelectListener;

@SuppressLint("NewApi")
public class BasePageSelectDialog extends Dialog implements
		OnClickListener, OnSeekBarChangeListener, DialogInterface.OnDismissListener {

	protected PageSelectListener mListener = null;
	protected Activity mContext;

	private final int HMSG_PAGESELECT		 = 5001;
	private final int TERM_PAGESEELCT		 = 100;

	// パラメータ
	protected boolean mViewer;
	protected boolean mDirTree;
	protected int mShareType = DEF.SHARE_SINGLE;
	protected int mPage;
	protected int mMaxPage;
	protected boolean mReverse;
	protected boolean mAutoApply;

	protected boolean mShare;
	protected SeekBar mSeekPage;

	private AppCompatImageButton mBtnLeftMost;
	private AppCompatImageButton mBtnLeft100;
	private AppCompatImageButton mBtnLeft10;
	private AppCompatImageButton mBtnLeft1;
	private AppCompatImageButton mBtnRight1;
	private AppCompatImageButton mBtnRight10;
	private AppCompatImageButton mBtnRight100;
	private AppCompatImageButton mBtnRightMost;
	private AppCompatImageButton mBtnPageReset;

	private AppCompatImageButton mBtnBookLeft;
	private AppCompatImageButton mBtnBookRight;
	private AppCompatImageButton mBtnBookmarkLeft;
	private AppCompatImageButton mBtnBookmarkRight;
	private AppCompatImageButton mBtnThumbSlider;
	private AppCompatImageButton mBtnDirTree;
	private AppCompatImageButton mBtnTOC;
	private AppCompatImageButton mBtnFavorite;
	private AppCompatImageButton mBtnAddFavorite;
	private AppCompatImageButton mBtnSearch;
	private AppCompatImageButton mBtnShare;
	private AppCompatImageButton mBtnShareLeftPage;
	private AppCompatImageButton mBtnShareRightPage;
	private AppCompatImageButton mBtnRotate;
	private AppCompatImageButton mBtnRotateImage;
	private AppCompatImageButton mBtnSelectThum;
	private AppCompatImageButton mBtnTrimThumb;
	private AppCompatImageButton mBtnControl;
	private AppCompatImageButton mBtnMenu;
	private AppCompatImageButton mBtnConfig;
	private AppCompatImageButton mBtnEditButton;

	public BasePageSelectDialog(Activity context) {
		super(context);
		Window dlgWindow = getWindow();

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		//PaintDrawable paintDrawable = new PaintDrawable(0x80000000);
		//dlgWindow.setBackgroundDrawable(paintDrawable);
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialognoframe);

		// 画面下に表示
		WindowManager.LayoutParams wmlp = dlgWindow.getAttributes();
		wmlp.gravity = Gravity.BOTTOM;
		dlgWindow.setAttributes(wmlp);
		setCanceledOnTouchOutside(true);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mContext = context;

		// ダイアログ終了通知設定
		setOnDismissListener(this);
	}

	public void setParams(boolean viewer, int page, int maxpage, boolean reverse,boolean dirtree) {
		mViewer = viewer;
		mPage = page;
		mMaxPage = maxpage;
		mReverse = reverse;
		mDirTree = dirtree;
	}

	public void setShareType(int shareType) {
		mShareType = shareType;
		if (mShare) {
			if (mShareType == DEF.SHARE_SINGLE) {
				mBtnShare.setVisibility(View.VISIBLE);
				mBtnShareLeftPage.setVisibility(View.GONE);
				mBtnShareRightPage.setVisibility(View.GONE);
			} else {
				mBtnShare.setVisibility(View.GONE);
				mBtnShareLeftPage.setVisibility(View.VISIBLE);
				mBtnShareRightPage.setVisibility(View.VISIBLE);
			}
		}
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		boolean debug = false;

		// 一度ダイアログを表示すると画面回転時に呼び出される
        Window win = getWindow();
        WindowManager.LayoutParams lpCur = win.getAttributes();
        WindowManager.LayoutParams lpNew = new WindowManager.LayoutParams();
        lpNew.copyFrom(lpCur);
        lpNew.width = WindowManager.LayoutParams.MATCH_PARENT;
        lpNew.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lpNew);

		mSeekPage = (SeekBar) findViewById(R.id.seek_page);
		mSeekPage.setMax(mMaxPage - 1);
		setProgress(mPage, false);
		mSeekPage.setOnSeekBarChangeListener(this);

		mBtnLeftMost = (AppCompatImageButton) this.findViewById(R.id.leftmost);
		mBtnLeft100 = (AppCompatImageButton) this.findViewById(R.id.left100);
		mBtnLeft10  = (AppCompatImageButton) this.findViewById(R.id.left10);
		mBtnLeft1   = (AppCompatImageButton) this.findViewById(R.id.left1);
		mBtnRightMost = (AppCompatImageButton) this.findViewById(R.id.rightmost);
		mBtnRight100 = (AppCompatImageButton) this.findViewById(R.id.right100);
		mBtnRight10  = (AppCompatImageButton) this.findViewById(R.id.right10);
		mBtnRight1   = (AppCompatImageButton) this.findViewById(R.id.right1);
		mBtnPageReset = (AppCompatImageButton) this.findViewById(R.id.page_reset);

		mBtnLeftMost.setOnClickListener(this);
		mBtnLeft100.setOnClickListener(this);
		mBtnLeft10.setOnClickListener(this);
		mBtnLeft1.setOnClickListener(this);
		mBtnRightMost.setOnClickListener(this);
		mBtnRight100.setOnClickListener(this);
		mBtnRight10.setOnClickListener(this);
		mBtnRight1.setOnClickListener(this);
		mBtnPageReset.setOnClickListener(this);

		mBtnBookLeft = (AppCompatImageButton) this.findViewById(R.id.book_left);
		mBtnBookRight = (AppCompatImageButton) this.findViewById(R.id.book_right);
		mBtnBookmarkLeft = (AppCompatImageButton) this.findViewById(R.id.bookmark_left);
		mBtnBookmarkRight = (AppCompatImageButton) this.findViewById(R.id.bookmark_right);
		mBtnThumbSlider = (AppCompatImageButton) this.findViewById(R.id.thumb_slider);
		mBtnDirTree = (AppCompatImageButton) this.findViewById(R.id.directory_tree);
		mBtnTOC = (AppCompatImageButton) this.findViewById(R.id.table_of_contents);
		mBtnFavorite = (AppCompatImageButton) this.findViewById(R.id.favorite);
		mBtnAddFavorite = (AppCompatImageButton) this.findViewById(R.id.add_favorite);
		mBtnSearch = (AppCompatImageButton) this.findViewById(R.id.search);
		mBtnShare = (AppCompatImageButton) this.findViewById(R.id.share);
		mBtnShareLeftPage = (AppCompatImageButton) this.findViewById(R.id.share_left_page);
		mBtnShareRightPage = (AppCompatImageButton) this.findViewById(R.id.share_right_page);
		mBtnRotate = (AppCompatImageButton) this.findViewById(R.id.rotate);
		mBtnRotateImage = (AppCompatImageButton) this.findViewById(R.id.rotate_image);
		mBtnSelectThum = (AppCompatImageButton) this.findViewById(R.id.select_thumb);
		mBtnTrimThumb = (AppCompatImageButton) this.findViewById(R.id.trimming_thumb);
		mBtnControl = (AppCompatImageButton) this.findViewById(R.id.control);
		mBtnMenu = (AppCompatImageButton) this.findViewById(R.id.menu);
		mBtnConfig = (AppCompatImageButton) this.findViewById(R.id.config);
		mBtnEditButton = (AppCompatImageButton) this.findViewById(R.id.edit_button);

		mBtnBookLeft.setOnClickListener(this);
		mBtnBookRight.setOnClickListener(this);
		mBtnBookmarkLeft.setOnClickListener(this);
		mBtnBookmarkRight.setOnClickListener(this);
		mBtnThumbSlider.setOnClickListener(this);
		mBtnDirTree.setOnClickListener(this);
		mBtnTOC.setOnClickListener(this);
		mBtnFavorite.setOnClickListener(this);
		mBtnAddFavorite.setOnClickListener(this);
		mBtnSearch.setOnClickListener(this);
		mBtnShare.setOnClickListener(this);
		mBtnShareLeftPage.setOnClickListener(this);
		mBtnShareRightPage.setOnClickListener(this);
		mBtnRotate.setOnClickListener(this);
		mBtnRotateImage.setOnClickListener(this);
		mBtnSelectThum.setOnClickListener(this);
		mBtnTrimThumb.setOnClickListener(this);
		mBtnControl.setOnClickListener(this);
		mBtnMenu.setOnClickListener(this);
		mBtnConfig.setOnClickListener(this);
		mBtnEditButton.setOnClickListener(this);

		boolean[] states = PageSelectCheckDialog.loadToolbarState(mContext);
		if (debug) {Log.d("BasePageSelectDialog", "onCreate: states[]=" + Arrays.toString(states));}

		for (int i = 0; i < states.length; ++i) {
			switch (PageSelectCheckDialog.COMMAND_ID[i]) {
				case DEF.TOOLBAR_LEFTMOST: {
					if (!states[i]) {
						mBtnLeftMost.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_LEFT100: {
					if (!states[i]) {
						mBtnLeft100.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_LEFT10: {
					if (!states[i]) {
						mBtnLeft10.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_LEFT1: {
					if (!states[i]) {
						mBtnLeft1.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHT1: {
					if (!states[i]) {
						mBtnRight1.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHT10: {
					if (!states[i]) {
						mBtnRight10.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHT100: {
					if (!states[i]) {
						mBtnRight100.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_RIGHTMOST: {
					if (!states[i]) {
						mBtnRightMost.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_PAGE_RESET: {
					if (!states[i]) {
						mBtnPageReset.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOK_LEFT: {
					if (!states[i]) {
						mBtnBookLeft.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOK_RIGHT: {
					if (!states[i]) {
						mBtnBookRight.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOKMARK_LEFT: {
					if (!states[i]) {
						mBtnBookmarkLeft.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_BOOKMARK_RIGHT: {
					if (!states[i]) {
						mBtnBookmarkRight.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_THUMB_SLIDER: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnThumbSlider.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_DIR_TREE: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER || !mDirTree) {
						mBtnDirTree.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_TOC: {
					if (!states[i] || mViewer == DEF.IMAGE_VIEWER) {
						mBtnTOC.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_FAVORITE: {
					if (!states[i]) {
						mBtnFavorite.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_ADD_FAVORITE: {
					if (!states[i]) {
						mBtnAddFavorite.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_SEARCH: {
					if (!states[i] || mViewer == DEF.IMAGE_VIEWER) {
						mBtnSearch.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_SHARE: {
					mShare = states[i];
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnShare.setVisibility(View.GONE);
						mBtnShareLeftPage.setVisibility(View.GONE);
						mBtnShareRightPage.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_ROTATE: {
					if (!states[i]) {
						mBtnRotate.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_ROTATE_IMAGE: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnRotateImage.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_SELECT_THUMB: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnSelectThum.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_TRIM_THUMB: {
					if (!states[i] || mViewer == DEF.TEXT_VIEWER) {
						mBtnTrimThumb.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_CONTROL: {
					if (!states[i]) {
						mBtnControl.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_MENU: {
					if (!states[i]) {
						mBtnMenu.setVisibility(View.GONE);
					}
					break;
				}
				case DEF.TOOLBAR_CONFIG: {
					if (!states[i]) {
						mBtnConfig.setVisibility(View.GONE);
					}
					break;
				}
			}
		}
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

	public void setPageSelectListear(PageSelectListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		boolean debug  = true;
		// ボタンクリック

		if (mBtnBookLeft == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_LEFT);
		}
		else if (mBtnBookRight == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOK_RIGHT);
		}
		else if (mBtnBookmarkLeft == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_LEFT);
		}
		else if (mBtnBookmarkRight == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_BOOKMARK_RIGHT);
		}
		else if (mBtnThumbSlider == v) {
			dismiss();
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_THUMB_SLIDER);
		}
		else if (mBtnDirTree == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_DIR_TREE);
		}
		else if (mBtnTOC == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_TOC);
		}
		else if (mBtnFavorite == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_FAVORITE);
		}
		else if (mBtnAddFavorite == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ADD_FAVORITE);
		}
		else if (mBtnSearch == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SEARCH);
		}
		else if (mBtnShare == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE);
		}
		else if (mBtnShareLeftPage == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE_LEFT_PAGE);
		}
		else if (mBtnShareRightPage == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SHARE_RIGHT_PAGE);
		}
		else if (mBtnRotate == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ROTATE);
		}
		else if (mBtnRotateImage == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_ROTATE_IMAGE);
		}
		else if (mBtnSelectThum == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_SELECT_THUMB);
		}
		else if (mBtnTrimThumb == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_TRIM_THUMB);
		}
		else if (mBtnControl == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_CONTROL);
		}
		else if (mBtnMenu == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_MENU);
		}
		else if (mBtnConfig == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_CONFIG);
		}
		else if (mBtnEditButton == v) {
			mListener.onSelectPageSelectDialog(DEF.TOOLBAR_EDIT_TOOLBAR);
		}

		else {
			// ページ選択の場合
			int page = calcProgress(mSeekPage.getProgress());
			if(debug) {Log.d("BasePageSelectDialog", "onClick: 現在ページ=" + page + ", mReverse=" + mReverse);}

			if (mBtnLeftMost == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnLeftMost");}
				if (mReverse) {
					page = mMaxPage - 1;
				} else {
					page = 0;
				}
			} else if (mBtnLeft100 == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnLeft100");}
				if (mReverse) {
					page += 100;
				} else {
					page -= 100;
				}
			} else if (mBtnLeft10 == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnLeft10");}
				if (mReverse) {
					page += 10;
				} else {
					page -= 10;
				}
			} else if (mBtnLeft1 == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnLeft1");}
				if (mReverse) {
					page += 1;
				} else {
					page -= 1;
				}
			} else if (mBtnRight1 == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnRight1");}
				if (mReverse) {
					page -= 1;
				} else {
					page += 1;
				}
			} else if (mBtnRight10 == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnRight10");}
				if (mReverse) {
					page -= 10;
				} else {
					page += 10;
				}
			} else if (mBtnRight100 == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnRight100");}
				if (mReverse) {
					page -= 100;
				} else {
					page += 100;
				}
			} else if (mBtnRightMost == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnRightMost");}
				if (mReverse) {
					page = 0;
				} else {
					page = mMaxPage - 1;
				}
			} else if (mBtnPageReset == v) {
				if(debug) {Log.d("BasePageSelectDialog", "onClick: v=mBtnPageReset");}
				// ページ選択をリセット
				page = mPage;
			}

			if(debug) {Log.d("BasePageSelectDialog", "onClick: 変更後ページ=" + page);}

			if (page < 0) {
				page = 0;
			} else if (page >= mMaxPage) {
				page = mMaxPage - 1;
			}

			// 設定と通知
			if (mAutoApply) {
				mListener.onSelectPage(page);
			}
			setProgress(page, false);
		}
	}

	protected void setProgress(int pos, boolean fromThumb) {
		boolean debug  = false;
		if(debug) {Log.d("BasePageSelectDialog", "setProgress: pos=" + pos);}
		//if (debug) {DEF.StackTrace("BasePageSelectDialog", "setProgress:");}
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		if(debug) {Log.d("BasePageSelectDialog", "setProgress: convpos=" + convpos);}
		mSeekPage.setProgress(convpos);
	}

	protected int calcProgress(int pos) {
		int convpos;

		if (mReverse == false) {
			convpos = pos;
		}
		else {
			convpos = mSeekPage.getMax() - pos;
		}
		return convpos;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int page, boolean fromUser) {
		boolean debug  = true;
		if(debug) {Log.d("BasePageSelectDialog", "onProgressChanged: page=" + page + ", fromUser=" + fromUser);}
		// 変更
		if (fromUser) {
			int cnvpage = calcProgress(page);
			mListener.onSelectPage(cnvpage);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// 開始

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 終了
		if (mAutoApply) {
			int cnvpage = calcProgress(seekBar.getProgress());
			mListener.onSelectPage(cnvpage);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
	}

}