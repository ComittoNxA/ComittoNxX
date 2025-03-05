package src.comitton.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.EventListener;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.fileaccess.SafFileAccess;
import src.comitton.fileview.filelist.ServerSelect;

@SuppressLint("NewApi")
public class EditServerDialog extends ImmersiveDialog implements OnClickListener, OnDismissListener {
	private static final String TAG = "EditServerDialog";

	private final int SELLIST_SERVER_TYPE = 0;

	public static final int Servertype[] = { R.string.serverTypeSMB, R.string.serverTypeSAF, R.string.serverTypePicker};

	private ListDialog mListDialog;
	private int mSelectMode;

	private String mServerTypeTitle;
	private String[] mServerTypeItems;

	private Button mBtnAccessType;
	private TextView mTextName;
	private TextView mTextHost;
	private TextView mTextUser;
	private TextView mTextPass;
	private TextView mTextProvider;
	private EditText mEditName;
	private EditText mEditHost;
	private EditText mEditUser;
	private ViewGroup mLayoutPass;
	private EditText mEditPass;
	private Button mBtnProvider;
	private Button mBtnCancel;
	private Button mBtnClear;
	private Button mBtnOK;

	private int mIndex;
	ServerSelect mServer;
	private int mAccessType;
	private String mName;
	private String mHost;
	private String mUser;
	private String mPass;
	private String mProvider;

	SearchListener mListener;

	public EditServerDialog(AppCompatActivity activity, @StyleRes int themeResId, @NonNull int index, SearchListener listener) {
		super(activity, themeResId, true);
		boolean debug = false;

		setCanceledOnTouchOutside(true);
		setOnDismissListener(this);

		Resources res = mActivity.getResources();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
		mServer = new ServerSelect(sp, mActivity);

		mIndex = index;
		mAccessType = mServer.getAccessType(index);
		mName = mServer.getName(index);
		mHost = mServer.getHost(index);
		mUser = mServer.getUser(index);
		mPass = mServer.getPass(index);
		mProvider = mServer.getProvider(index);

		if (debug) {Log.d(TAG, "EditServerDialog: index=" + mIndex + ", accessType=" + mAccessType + ", name=" + mName + ", host=" + mHost + ", user=" + mUser + ", pass=" + mPass + ", provider=" + mProvider);}

		mListener = listener;

		int nItem;
		// サーバタイプの選択肢設定
		mServerTypeTitle = res.getString(R.string.serverType);
		nItem = Servertype.length;
		mServerTypeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mServerTypeItems[i] = res.getString(Servertype[i]);
		}
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editserverdialog);
		mBtnAccessType = (Button) this.findViewById(R.id.button_type);
		mTextName = (TextView) this.findViewById(R.id.text_name);
		mTextHost = (TextView) this.findViewById(R.id.text_host);
		mTextUser = (TextView) this.findViewById(R.id.text_user);
		mTextPass = (TextView) this.findViewById(R.id.text_pass);
		mTextProvider = (TextView) this.findViewById(R.id.text_provider);
		mEditName = (EditText) this.findViewById(R.id.edit_name);
		mEditHost = (EditText) this.findViewById(R.id.edit_host);
		mEditUser = (EditText) this.findViewById(R.id.edit_user);
		mLayoutPass = (ViewGroup) this.findViewById(R.id.layout_pass);
		mEditPass = (EditText) this.findViewById(R.id.edit_pass);
		mBtnProvider  = (Button) this.findViewById(R.id.button_provider);
		mBtnCancel  = (Button) this.findViewById(R.id.btn_cancel);
		mBtnClear  = (Button) this.findViewById(R.id.btn_clear);
		mBtnOK  = (Button) this.findViewById(R.id.btn_search);

		mBtnAccessType.setText(mServerTypeItems[mAccessType]);
		mEditName.setText(mName);
		mEditHost.setText(mHost);
		mEditUser.setText(mUser);
		mEditPass.setText(mPass);
		if (mProvider.isEmpty() || mAccessType == DEF.ACCESS_TYPE_PICKER) {
			mBtnProvider.setText(R.string.svProviderUnselected);
		}
		else {
			mBtnProvider.setText(R.string.svProviderSelected);
		}
		showItem(mAccessType);

		mBtnAccessType.setOnClickListener(this);
		mBtnProvider.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mBtnClear.setOnClickListener(this);
		mBtnOK.setOnClickListener(this);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MENU:
					dismiss();
					break;
				case KeyEvent.KEYCODE_ENTER:
					onClick(mBtnOK);
					break;
			}
		}
		// 自動生成されたメソッド・スタブ
		return super.dispatchKeyEvent(event);
	}

	public interface SearchListener extends EventListener {
	    // 入力
	    public void onSearch(int accessType, String name, String host, String user, String pass, String path, String provider, String dispName);
	    public void onCancel();
	    public void onClose();
	}

	@Override
	public void onClick(View v) {
		boolean debug = false;
		if (v == mBtnOK) {
			if (debug) {Log.d(TAG, "onClick: v == mBtnOK");}
			String name = "";
			String host = "";
			String user = "";
			String pass = "";
			String path = "";
			String provider = "";
			String dispName = "";
			if (mEditName.getText() != null) {
				name = mEditName.getText().toString().trim();
			}
			if (mAccessType == DEF.ACCESS_TYPE_SMB) {
				if (mEditHost.getText() != null) {
					host = mEditHost.getText().toString().trim();
				}
				if (mEditUser.getText() != null) {
					user = mEditUser.getText().toString().trim();
				}
				if (mEditPass.getText() != null) {
					pass = mEditPass.getText().toString().trim();
				}
				path = "/";

				dispName = ServerSelect.dispName(host, user, pass);
			}
			else if (mAccessType == DEF.ACCESS_TYPE_SAF || mAccessType == DEF.ACCESS_TYPE_PICKER) {
				provider = mProvider;
				//path = mProvider;
				path = "/";
				dispName = SafFileAccess.getPathName(mActivity, mProvider);
			}

			if (debug) {Log.d(TAG, "onClick: index=" + mIndex + ", accessType=" + mAccessType + ", name=" + name + ", host=" + host + ", user=" + user + ", pass=" + pass + ", provider=" + provider + ", dispName=" + dispName);}
			mListener.onSearch(mAccessType, name, host, user, pass, path, provider, dispName);
			dismiss();
		}
		else if (v == mBtnClear) {
			if (debug) {Log.d(TAG, "onClick: v == mBtnClear");}
			// クリアクリック
			mEditName.setText("");
			mEditHost.setText("");
			mEditUser.setText("");
			mEditPass.setText("");
			mProvider = "";
			mBtnProvider.setText(R.string.svProviderUnselected);
		}
		else if (v == mBtnCancel) {
			if (debug) {Log.d(TAG, "onClick: v == mBtnCancel");}
			// キャンセルクリック
			mListener.onCancel();
			dismiss();
		}
		else if (v == mBtnAccessType) {
			if (debug) {Log.d(TAG, "onClick: v == mBtnAccessType");}
			// サーバタイプ
			showSelectList(SELLIST_SERVER_TYPE);
		}
		else if (v == mBtnProvider) {
			if (debug) {Log.d(TAG, "ServerSelect: v == mBtnProvider");}
			// パスクリック
			if (mAccessType == DEF.ACCESS_TYPE_SAF) {
				// ストレージアクセスフレームワーク
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				intent.addFlags(
						Intent.FLAG_GRANT_READ_URI_PERMISSION |
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
						Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
						Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
				);
				intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, mProvider);
				mActivity.startActivityForResult(Intent.createChooser(intent, mActivity.getText(R.string.SafChooseTitle)), DEF.REQUEST_CODE_ACTION_OPEN_DOCUMENT_TREE);
			}

			else if (mAccessType == DEF.ACCESS_TYPE_PICKER) {
				// ファイルピッカー
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				if (!mProvider.isEmpty()) {
					intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, mProvider);
				}
				mActivity.startActivityForResult(intent, DEF.REQUEST_CODE_ACTION_OPEN_DOCUMENT);
			}
		}
	}

	public void setProvider(@NonNull final String provider) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "setProvider: provider=" + provider);}
		mProvider = provider;
		if (mProvider.isEmpty()) {
			mBtnProvider.setText(R.string.svProviderUnselected);
		}
		else {
			mBtnProvider.setText(R.string.svProviderSelected);
		}

		if (mAccessType == DEF.ACCESS_TYPE_PICKER) {
			String name = "";
			String host = "";
			String user = "";
			String pass = "";
			String path = "";
			String dispName = SafFileAccess.getPathName(mActivity, mProvider);
			if (mEditName.getText() != null) {
				name = mEditName.getText().toString().trim();
			}

			if (debug) {Log.d(TAG, "onClick: index=" + mIndex + ", accessType=" + mAccessType + ", name=" + name + ", host=" + host + ", user=" + user + ", pass=" + pass + ", provider=" + provider + ", dispName=" + dispName);}
			mListener.onSearch(mAccessType, name, host, user, pass, path, provider, dispName);
			dismiss();
		}

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
			case SELLIST_SERVER_TYPE:
				// サーバタイプの設定
				title = mServerTypeTitle;
				items = mServerTypeItems;
				selIndex = mAccessType;
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(mActivity, R.style.MyDialog, title, items, selIndex, new ListDialog.ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_SERVER_TYPE:
						// サーバタイプの設定
						mAccessType = index;
						mBtnAccessType.setText(mServerTypeItems[mAccessType]);
						showItem(mAccessType);
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

	private void showItem(int serverType) {
		if (serverType == DEF.ACCESS_TYPE_SMB) {
			mTextName.setVisibility(View.VISIBLE);
			mTextHost.setVisibility(View.VISIBLE);
			mTextUser.setVisibility(View.VISIBLE);
			mTextPass.setVisibility(View.VISIBLE);
			mTextProvider.setVisibility(View.GONE);
			mEditName.setVisibility(View.VISIBLE);
			mEditHost.setVisibility(View.VISIBLE);
			mEditUser.setVisibility(View.VISIBLE);
			mLayoutPass.setVisibility(View.VISIBLE);
			mEditPass.setVisibility(View.VISIBLE);
			mBtnProvider.setVisibility(View.GONE);
		}
		else {
			mTextName.setVisibility(View.VISIBLE);
			mTextHost.setVisibility(View.GONE);
			mTextUser.setVisibility(View.GONE);
			mTextPass.setVisibility(View.GONE);
			mTextProvider.setVisibility(View.VISIBLE);
			mEditName.setVisibility(View.VISIBLE);
			mEditHost.setVisibility(View.GONE);
			mEditUser.setVisibility(View.GONE);
			mLayoutPass.setVisibility(View.GONE);
			mEditPass.setVisibility(View.GONE);
			mBtnProvider.setVisibility(View.VISIBLE);
		}
		if (serverType == DEF.ACCESS_TYPE_PICKER){
			mBtnClear.setVisibility(View.GONE);
			mBtnCancel.setVisibility(View.GONE);
			mBtnOK.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
	}
}