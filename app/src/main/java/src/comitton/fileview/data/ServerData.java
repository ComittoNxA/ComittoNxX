package src.comitton.fileview.data;

import android.net.Uri;
import android.util.Log;

import src.comitton.common.DEF;

public class ServerData {
	private static final String TAG = "ServerData";

	private int mAccessType = 0;
	private String mName = "";
	private String mHost = "";
	private String mUser = "";
	private String mPass = "";
	private String mPath = "";
	private String mProvider = "";
	private String mDispName = "";

	public int getAccessType() {
		return this.mAccessType;
	}

	public void setAccessType(int accessType) {
		this.mAccessType = accessType;
	}

	public String getName() {
		return this.mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getHost() {
		return this.mHost;
	}

	public void setHost(String host) {
		this.mHost = host;
	}

	public String getUser() {
		return this.mUser;
	}

	public void setUser(String user) {
		this.mUser = user;
	}

	public String getPass() {
		return this.mPass;
	}

	public void setPass(String pass) {
		this.mPass = pass;
	}

	public String getPath() {
		return this.mPath;
	}

	public void setPath(String path) {
		if (!path.isEmpty()) {
			this.mPath = path;
		}
	}

	public String getProvider() {
		return this.mProvider;
	}

	public void setProvider(String provider) {
		this.mProvider = provider;
	}

	public String getDispName() {
		return this.mDispName;
	}

	public void setDispName(String dispName) {
		this.mDispName = dispName;
	}

	public String getURI() {
		if (getAccessType() == DEF.ACCESS_TYPE_SMB) {
			return "smb://" + getHost();
		}
		else {
			return getProvider();
		}
	}

}
