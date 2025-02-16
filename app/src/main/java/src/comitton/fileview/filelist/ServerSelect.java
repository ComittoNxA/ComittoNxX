package src.comitton.fileview.filelist;

import java.io.File;
import java.net.URLEncoder;

import jp.dip.muracoro.comittonx.R;

import src.comitton.common.DEF;
import src.comitton.fileview.data.ServerData;
import src.comitton.fileview.data.RecordItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ServerSelect {
	private static final String TAG = "ServerSelect";

	public static final String SERVER_NAME_UNDEFINE = "Undefined";

	private int mSelect = DEF.INDEX_LOCAL;
	private SharedPreferences mSharedPrefer = null;

	private ServerData mServer[];
	private String mLocalPath;
	private String mLocalName;

	public ServerSelect(SharedPreferences sharedPreferences, Context context) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "ServerSelect: 開始します.");}

		mSharedPrefer = sharedPreferences;
		mServer = new ServerData[DEF.MAX_SERVER];
		mSelect = DEF.INDEX_LOCAL;
		Resources res = context.getResources();
		mLocalName = res.getString(R.string.localStrage);

		// パス設定
		mLocalPath = sharedPreferences.getString("path", "/");
		if (mLocalPath == null || mLocalPath.length() < 1 || !mLocalPath.substring(0, 1).equals("/")) {
			mLocalPath = "/";
		}
		if (mLocalPath.equals("/")) {
			// ローカルのルートフォルダ
			File dir = new File(mLocalPath);
			if (!dir.canRead()) {
				// 読み取り権限がない
				// ストレージルートにリセット
				mLocalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
			}
		}

		for (int i = 0 ; i < DEF.MAX_SERVER ; i ++) {
			mServer[i] = new ServerData();
			int accessType = sharedPreferences.getInt("smb-access-type" + i, 0);
			String name = sharedPreferences.getString("smb-name" + i, "");
			String host = sharedPreferences.getString("smb-host" + i, "");
			String path = sharedPreferences.getString("smb-path" + i, "");
			String user = sharedPreferences.getString("smb-user" + i, "");
			String pass = sharedPreferences.getString("smb-pass" + i, "");
			String provider = sharedPreferences.getString("smb-provider" + i, "");
			String dispName = sharedPreferences.getString("smb-dispname" + i, "");
			if (dispName.isEmpty()) {
				dispName = dispName(host, user, pass);
			}
			if (accessType < DEF.ACCESS_TYPE_SMB || accessType > DEF.ACCESS_TYPE_PICKER) {
				accessType = DEF.ACCESS_TYPE_SMB;
			}
			if (debug) {Log.d(TAG, "ServerSelect: i=" + i + ", accessType=" + accessType + ", name=" + name + ", host=" + host + ", path=" + path + ", user=" + user + ", pass=" + pass + ", provider=" + provider + ", dispName=" + dispName);}
			mServer[i].setAccessType(accessType);
			mServer[i].setName(name);
			mServer[i].setPath(path);
			if (!host.isEmpty()) {
				mServer[i].setHost(host);
				mServer[i].setUser(user);
				mServer[i].setPass(pass);
			}
			if (!provider.isEmpty()) {
				mServer[i].setProvider(provider);
			}
			mServer[i].setDispName(dispName);
		}
	}

	// サーバ選択
	public boolean select(int index) {
		if (index == DEF.INDEX_LOCAL || 0 <= index && index < DEF.MAX_SERVER) {
			mSelect = index;
			return true;
		}
		return false;
	}

	// サーバ選択
	public boolean select(String code) {
		if (code == null || code.isEmpty()) {
			mSelect = DEF.INDEX_LOCAL;
			return true;
		}
		else {
			for (int i = 0 ; i < DEF.MAX_SERVER ; i ++) {
				String str = getCode(i);
				if (str.equals(code)) {
					mSelect = i;
					return true;
				}
			}
		}
		return false;
	}

	// 選択中のサーバ
	public int getSelect() {
		return mSelect;
	}

	public String getCode(){
		return getCode(mSelect);
	}

	public String getCode(int index){
		String uri = getUserPassURI(index);

		long val = 0;
		byte[] buf = uri.getBytes();

		if (uri.isEmpty()) {
			return "";
		}

		for (int i = 0 ; i < buf.length ; i ++) {
			val ^= ((long)buf[i] & 0xFF) << (i % 56);
		}
		return Long.valueOf(val).toString();
	}

	// アクセスタイプ取得
	public int getAccesType() {
		return getAccessType(mSelect);
	}

	// アクセスタイプ取得
	public int getAccessType(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// ローカル
			return DEF.ACCESS_TYPE_LOCAL;
		}
		else{
			// アクセスタイプ
			return mServer[index].getAccessType();
		}
	}

	// サーバ名前取得
	public String getName() {
		return getName(mSelect);
	}

	// サーバ名前取得
	public String getName(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// ローカル名
			return mLocalName;
		}
		else{
			// サーバ名
			return mServer[index].getName();
		}
	}

	// ホスト名取得
	public String getRoot() {
		return "smb://" + getHost(mSelect);
	}

	// ホスト名取得
	public String getHost() {
		return getHost(mSelect);
	}

	// ホスト名取得
	public String getHost(int index) {
		if (index == DEF.INDEX_LOCAL) {
			return "";
		}
		else{
			return mServer[index].getHost();
		}
	}

	// ユーザ名取得
	public String getUser() {
		return(getUser(mSelect));
	}

	public String getUser(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// ユーザ
			return "";
		}
		else{
			// ユーザ
			return mServer[index].getUser();
		}
	}

	// パスワード取得
	public String getPass() {
		return(getPass(mSelect));
	}

	public String getPass(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// パスワード
			return "";
		}
		else{
			// パスワード
			return mServer[index].getPass();
		}
	}

	// プロバイダ取得
	public String getProvider() {
		return(getProvider(mSelect));
	}

	public String getProvider(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// プロバイダ
			return "";
		}
		else{
			// プロバイダ
			return mServer[index].getProvider();
		}
	}

	// プロバイダ取得
	public String getDispName() {
		return(getDispName(mSelect));
	}

	public String getDispName(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// プロバイダ
			return "";
		}
		else{
			// プロバイダ
			return mServer[index].getDispName();
		}
	}

	// サーバパスの取得
	public String getPath() {
		return getPath(mSelect);
	}

	// サーバパスの取得
	public String getPath(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// ローカル名
			return mLocalPath;
		}
		else{
			// サーバ名
			return mServer[index].getPath();
		}
	}

	// サーバパスの設定
	public void setPath(String path) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "setPath: 開始します. path=" + path);}
		if (debug) {DEF.StackTrace(TAG, "setPath:");}
		if (mSelect == DEF.INDEX_LOCAL) {
			// ローカル名
			mLocalPath = path;
		}
		else{
			// サーバ名
			mServer[mSelect].setPath(path);
		}
		savePath();
	}

	/**
	 * URIの取得
	 */
	public String getURI() {
		return getURI(mSelect);
	}

	/**
	 * URIの取得
	 */
	public String getURI(int index) {
		if (index == DEF.INDEX_LOCAL) {
			return "";
		}
		else {
			return mServer[index].getURI();
		}
	}


	/**
	  * URIの取得
	  */
	public String getUserPassURI(int index) {
		if (index == DEF.INDEX_LOCAL) {
			// ローカル名
			return "";
		}
		else{
			// サーバ名
			String ret = "smb://";
			String user = URLEncoder.encode(mServer[index].getUser());
			String pass = URLEncoder.encode(mServer[index].getPass());
			if (!user.equals("")) {
				ret += user;
				if (!pass.equals("")) {
					ret += ":" + pass;
				}
		 		ret += "@";
			}
	 		ret += mServer[index].getHost();
			return ret;
		}
	}

	/**
	 * サーバの情報の保存
	 */
	public void setData(int index, ServerData data) {
		if (0 <= index && index < DEF.MAX_SERVER) {
			setData(index, data.getAccessType(), data.getName(), data.getHost(), data.getUser(), data.getPass(), data.getPath(), data.getProvider(), data.getDispName());
			save(index);
		}
	}

	/**
	 * サーバの情報の保存
 	 */
	public void setData(int index, RecordItem record) {
		boolean debug = false;
		if (0 <= index && index < DEF.MAX_SERVER) {
			if (debug) {Log.d(TAG, "setData: index=" + index + ", accessType=" + record.getAccessType() + ", name=" + record.getServerName() + ", host=" + record.getHost() + ", path=" + record.getPath() + ", user=" + record.getUser() + ", pass=" + record.getPass() + ", provider=" + record.getProvider() + ", dispName=" + record.getDispName());}
			setData(index, record.getAccessType(), record.getServerName(), record.getHost(), record.getUser(), record.getPass(), record.getPath(), record.getProvider(), record.getDispName());
			save(index);
		}
	}

	public void setData(int index, int accessType, String name, String host, String user, String pass, String path, String provider, String dispName) {
		if (0 <= index && index < DEF.MAX_SERVER) {
			mServer[index].setAccessType(accessType);
			mServer[index].setName(name);
			mServer[index].setHost(host);
			mServer[index].setUser(user);
			mServer[index].setPass(pass);
			mServer[index].setPath(path);
			mServer[index].setProvider(provider);
			mServer[index].setDispName(dispName);
			save(index);
		}
	}

	/**
	 * サーバの情報の保存
	 */
	public void save(int index) {
		boolean debug = false;
		Editor ed = mSharedPrefer.edit();
		// サーバ情報
		if (debug) {Log.d(TAG, "save: index=" + index + ", accessType=" + mServer[index].getAccessType() + ", name=" + mServer[index].getName() + ", host=" + mServer[index].getHost() + ", path=" + mServer[index].getPath() + ", user=" + mServer[index].getUser() + ", pass=" + mServer[index].getPass() + ", provider=" + mServer[index].getProvider());}
		if (mServer[index].getProvider().equals("user")) {DEF.StackTrace(TAG , "save:");}
		ed.putInt("smb-access-type" + index, mServer[index].getAccessType());
		ed.putString("smb-name" + index, mServer[index].getName());
		ed.putString("smb-host" + index, mServer[index].getHost());
		ed.putString("smb-user" + index, mServer[index].getUser());
		ed.putString("smb-pass" + index, mServer[index].getPass());
		ed.putString("smb-path" + index, mServer[index].getPath());
		ed.putString("smb-provider" + index, mServer[index].getProvider());
		ed.putString("smb-dispname" + index, mServer[index].getDispName());
		ed.apply();
	}

	/**
	 * ローカルのフォルダパスの保存
	 */
	public void savePath() {
		Editor ed = mSharedPrefer.edit();
		if (mSelect == DEF.INDEX_LOCAL) {
			// ローカル情報
			if (mLocalPath != null) {
				ed.putString("path", mLocalPath);
			}
		}
		else{
			String path = getPath();
			// サーバ情報
			if (path != null) {
				ed.putString("smb-path" + mSelect, path);
			}
		}
		ed.apply();
	}

	public static String dispName(String host, String user, String pass) {
		if (host.isEmpty()) {
			return "";
		}
		String dispName = "smb://";
		if (!user.isEmpty()) {
			dispName += user;
			if (!pass.isEmpty()) {
				dispName += ":******";
			}
			dispName += "@";
		}
		dispName += host + "/";
		return dispName;
	}

}
