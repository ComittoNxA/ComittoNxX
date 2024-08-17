package src.comitton.data;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;

import src.comitton.filelist.ServerSelect;


public class RecordItem {
	public static final int TYPE_NONE		= -1;
	public static final int TYPE_IMAGE		=  0;	// イメージ
	public static final int TYPE_TEXT		=  1;	// テキスト
	public static final int TYPE_COMPTEXT	=  2;	// 圧縮ファイル中テキスト
	public static final int TYPE_FOLDER		=  3;	// ディレクトリオープン
	public static final int TYPE_SERVER		=  4;	// サーバ
	public static final int TYPE_MENU		=  5;	// オプションメニュー

	private int server;
	private String servername;
	private String path;
	private String file;
	private int type;
	private long date;
	private String image;
	private int page;
	private String dispname;
	private String host;
	private String user;
	private String pass;
	private int item;
	private int icon;

	public RecordItem() {
		this.server = ServerSelect.INDEX_LOCAL;
		this.servername = null;
		this.path = null;
		this.file = null;
		this.date = 0;
		this.image = null;
		this.dispname = null;
		this.host = null;
		this.user = null;
		this.pass = null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getServer() {
		return server;
	}

	public void setServer(int server) {
		this.server = server;
	}

	public String getServerName() {
		return servername;
	}

	public void setServerName(String servername) {
		this.servername = servername == null ? "" : servername;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path == null ? "" : path;
	}

	public String getFile() {
		return this.file;
	}

	public void setFile(String file) {
		this.file = file == null ? "" : file;
	}

	// 日付取得
	public long getDate() {
		return this.date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	// ページ情報
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image == null ? "" : image;
	}

	// ページ情報
	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getDispName() {
		return dispname;
	}

	public void setDispName(String dispname) {
		this.dispname = dispname == null ? "" : dispname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host == null ? "" : host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user == null ? "" : user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass == null ? "" : pass;
	}

	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	@SuppressLint("SimpleDateFormat")
	public String getDateStr() {
		String dateStr;
		if (date != 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");
			dateStr = sdf.format(date);
		} else {
			dateStr = "[----/--/-- --:--:--]";
		}
		return dateStr;
	}

	// ArrayListのindexOfから呼ばれる比較処理
	public boolean equals(Object obj) {
		RecordItem data = (RecordItem) obj;
		if (this.server != data.getServer()) {
			return false;
		} else if (compare(this.path, data.getPath()) == false) {
			return false;
		} else if (compare(this.file, data.getFile()) == false) {
			return false;
//		} else if (compare(this.image, data.getImage()) == false) {
//			return false;
//		} else if (this.page != data.getPage()) {
//			return false;
		}

		// 一致
		return true;
	}

	// 文字列比較
	private boolean compare(String str1, String str2) {
		if (str1 == null) {
			if (str2 != null) {
				return false;
			}
		} else if (!str1.equals(str2)) {
			return false;
		}
		return true;
	}
}
