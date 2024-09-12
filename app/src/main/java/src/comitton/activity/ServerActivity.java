package src.comitton.activity;

import java.lang.reflect.Method;
import java.util.ArrayList;

import jp.dip.muracoro.comittonx.R;

import src.comitton.common.DEF;
import src.comitton.config.SetConfigActivity;
import src.comitton.config.SetFileColorActivity;
import src.comitton.config.SetFileListActivity;
import src.comitton.data.FileData;
import src.comitton.data.ServerData;
import src.comitton.filelist.ServerSelect;
import src.comitton.view.ListItemView;
import src.comitton.view.TitleView;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.os.Environment;

public class ServerActivity extends ListActivity {
	private TitleView mTitleView;
	private ListView mListView;

	private ServerListAdapter mServerListAdapter = null;
	private ArrayList<ServerData> mServerList = null;
	private ServerSelect mServer;
	private int mEditIndex;
	private ServerData mServerData;
	private View mEditDlg = null;
	private int mFontTitle;
	private int mFontMain;
	private int mFontSub;
	private int mItemMargin;

	private int mTxtColor;
	private int mInfColor;
	private int mBakColor;
	private int mCurColor;
	private int mTitColor;
	private int mTibColor;
	private int mListRota;

	private float mDensity;
	private SharedPreferences mSharedPreferences = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDensity = getResources().getDisplayMetrics().scaledDensity;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mTxtColor = SetFileColorActivity.getTxtColor(mSharedPreferences);
		mInfColor = SetFileColorActivity.getInfColor(mSharedPreferences);
		mBakColor = SetFileColorActivity.getBakColor(mSharedPreferences);
		mCurColor = SetFileColorActivity.getCurColor(mSharedPreferences);

		mTitColor = SetFileColorActivity.getTitColor(mSharedPreferences);
		mTibColor = SetFileColorActivity.getTibColor(mSharedPreferences);

		mFontTitle = DEF.calcFontPix(SetFileListActivity.getFontTitle(mSharedPreferences), mDensity);
		mFontMain = DEF.calcFontPix(SetFileListActivity.getFontMain(mSharedPreferences), mDensity);
		mFontSub = DEF.calcFontPix(SetFileListActivity.getFontSub(mSharedPreferences), mDensity);
		mItemMargin = DEF.calcSpToPix(SetFileListActivity.getItemMargin(mSharedPreferences), mDensity);
		mListRota = SetFileListActivity.getListRota(mSharedPreferences);
		DEF.setRotation(this, mListRota);

		setContentView(R.layout.serverview);

		mTitleView = (TitleView) this.findViewById(R.id.title);
		Resources res = getResources();
		mTitleView.setTextSize(mFontTitle, mTitColor, mTibColor);
		mTitleView.setTitle("[" + res.getString(R.string.saTitle1) + "]", res.getString(R.string.saTitle2));

		mListView = this.getListView();
		mListView.setBackgroundColor(mBakColor);

		LinearLayout linear = (LinearLayout) this.findViewById(R.id.listlayout);
		linear.setBackgroundColor(mBakColor);

		// サーバ情報の読み込み
		mServer = new ServerSelect(mSharedPreferences, this);

		mServerList = new ArrayList<ServerData>();
		for (int i = -1; i < ServerSelect.MAX_SERVER; i++) {
			ServerData serverData = new ServerData();
			String name = mServer.getName(i);
			String host = mServer.getHost(i);
			String user = mServer.getUser(i);
			String pass = mServer.getPass(i);
			String path = mServer.getPath(i);
			serverData.setName(name);
			serverData.setHost(host);
			serverData.setUser(user);
			serverData.setPass(pass);
			serverData.setPath(path);
			mServerList.add(serverData);
		}

		mServerListAdapter = new ServerListAdapter(this, R.layout.listitem, mServerList);
		setListAdapter(mServerListAdapter);

		// // イベント組み込み
		mListView.setOnItemLongClickListener(new MyClickAdapter());
		try {
			Method setLayerTypeMethod = mListView.getClass().getMethod("setLayerType", new Class[] {int.class, Paint.class});
			setLayerTypeMethod.invoke(mListView, new Object[] {View.LAYER_TYPE_SOFTWARE, null});
		} catch (Exception e) {
			;
		}
	}

	@Override
	// 選択イベント
	protected void onListItemClick(ListView listView, View v, int position, long id) {
		super.onListItemClick(listView, v, position, id);
		if (position < mServerList.size()) {
			ServerData server = (ServerData) mServerList.get(position);
			String name = server.getName();

			if (!name.equals("")) {
				Intent intent = new Intent();
				intent.putExtra("svrindex", position - 1);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	}

	// ActivityクラスのonCreateDialogをオーバーライド
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		switch (id) {
			case DEF.MESSAGE_EDITSERVER:
				// レイアウトの呼び出し
				LayoutInflater factory = LayoutInflater.from(this);
				mEditDlg = factory.inflate(R.layout.editsvr, null);

				// ダイアログの作成(AlertDialog.Builder)
				dialogBuilder.setTitle(R.string.svTitle);
				dialogBuilder.setView(mEditDlg);
				dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// 入力値を保存
						EditText name = (EditText) mEditDlg.findViewById(R.id.edit_name);
						EditText host = (EditText) mEditDlg.findViewById(R.id.edit_host);
						EditText user = (EditText) mEditDlg.findViewById(R.id.edit_user);
						EditText pass = (EditText) mEditDlg.findViewById(R.id.edit_pass);
						mServerData.setName(name.getText().toString());
						mServerData.setHost(host.getText().toString());
						mServerData.setUser(user.getText().toString());
						mServerData.setPass(pass.getText().toString());
						mServerData.setPath("/");

						// リスト更新
						mServerListAdapter.notifyDataSetChanged();
						mServer.setData(mEditIndex, mServerData);
					}
				});
				dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* キャンセル処理 */
					}
				});
				dialog = dialogBuilder.create();
				break;
		}
		return dialog;
	}

	// ActivityクラスのonCreateDialogをオーバーライド
	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog) {
		switch (id) {
			case DEF.MESSAGE_EDITSERVER:
				if (mEditDlg == null) {
					break;
				}
				// 一度ダイアログを表示すると画面回転時に呼び出される
				EditText name = (EditText) mEditDlg.findViewById(R.id.edit_name);
				EditText host = (EditText) mEditDlg.findViewById(R.id.edit_host);
				EditText user = (EditText) mEditDlg.findViewById(R.id.edit_user);
				EditText pass = (EditText) mEditDlg.findViewById(R.id.edit_pass);
				// 属性
				name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
				host.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
				user.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
				pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				// 文字列
				if (mServerData != null) {
					name.setText(mServerData.getName());
					host.setText(mServerData.getHost());
					user.setText(mServerData.getUser());
					pass.setText(mServerData.getPass());
				}
				break;
		}
		return;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		Resources res = getResources();

		// メニューをクリア
		menu.clear();

		// ヘルプ
		menu.add(0, DEF.MENU_ONLINE, Menu.NONE, res.getString(R.string.onlineMenu)).setIcon(android.R.drawable.ic_menu_set_as);
		return ret;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == DEF.MENU_ONLINE) {
			// 操作方法画面に遷移
			Resources res = getResources();
			String url = res.getString(R.string.url_server);	// 設定画面
			Intent intent;
			intent = new Intent(ServerActivity.this, HelpActivity.class);
			intent.putExtra("Url", url);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	// 長押しイベント処理用クラス
	class MyClickAdapter implements OnItemLongClickListener {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (position == 0) {
				// ローカルはストレージルートにリセット
				String path = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
				mServer.select(ServerSelect.INDEX_LOCAL);
				mServer.setPath(path);
				mServerListAdapter.notifyDataSetChanged();
				return true;
			}
			mEditIndex = position - 1;
			mServerData = mServerList.get(position);
			if (mServerData == null) {
				// データがない
				return true;
			}

			showDialog(DEF.MESSAGE_EDITSERVER);
			return true;
		}
	}

	public class ServerListAdapter extends ArrayAdapter<ServerData> {
		private ArrayList<ServerData> items;
		private LayoutInflater inflater;

		public ServerListAdapter(Context context, int textViewResourceId, ArrayList<ServerData> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ビューを受け取る
			View view = convertView;
			if (view == null) {
				// 受け取ったビューがnullなら新しくビューを生成
				view = inflater.inflate(R.layout.listitem, null);
				// // 背景画像をセットする
				// view.setBackgroundResource(R.drawable.back);
			}

			// 表示すべきデータの取得
			ServerData item = (ServerData) items.get(position);
			if (item != null) {
				ListItemView itemView = (ListItemView) view.findViewById(R.id.listitem);

				String name = item.getName();
				String uri = "";
				int size = 0;
				if (name != null && !name.equals("")) {
					if (position != 0) {
						String user = item.getUser();
						String pass = item.getPass();
						String host = item.getHost();

						uri = "smb://";
						if (!user.equals("")) {
							uri += user;
							if (!pass.equals("")) {
								uri += ":******";
							}
							uri += "@";
						}
						uri += host + "/";
						size = mFontSub;
					}else{//ローカルの場合
						uri = mServer.getPath(ServerSelect.INDEX_LOCAL);
						size = mFontSub;
					}
				}
				else {
					Resources res = getResources();
					name = res.getString(R.string.undefine);
					uri = "";
				}
				// マージンが小さすぎると窮屈なので最低8に
				itemView.setDrawInfo(mTxtColor, Color.WHITE, mInfColor, mFontMain, size, false, 0, 0, mItemMargin < 8 ? 8 : mItemMargin);
				itemView.setFileInfo(DEF.THUMBID_NONE, DEF.THUMBSTATE_NONE, false, name, uri, true);
				itemView.setMarker(mBakColor, mCurColor);
			}
			return view;
		}
	}
}
