package src.comitton.filelist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.data.RecordItem;
import src.comitton.data.ServerData;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;


public class RecordList {
	public static final int TYPE_DIRECTORY = 0;
	public static final int TYPE_SERVER = 1;
	public static final int TYPE_BOOKMARK = 2;
	public static final int TYPE_HISTORY = 3;
	public static final int TYPE_MENU = 4;
	public static final int TYPE_FILELIST = 5;
	public static final int TYPE_MAXNUM = TYPE_FILELIST;
	private static final String FILENAME[] = {"directory.dat", "server.dat", "bookmark.dat", "history.dat", "optmenu.dat"};
	private static final String SEPARATOR = "\t";
	private static final int INDEX_TYPE = 0;
	private static final int INDEX_SERVER = 1;
	private static final int INDEX_PATH = 2;
	private static final int INDEX_FILE = 3;
	private static final int INDEX_DATE = 4;
	private static final int INDEX_IMAGE = 5;
	private static final int INDEX_PAGE = 6;
	private static final int INDEX_DISPNAME = 7;
	private static final int INDEX_CHAPTER = 8;
	private static final int INDEX_PAGERATE = 9;

	private static final int MENU_TITLE_ID[] = {R.string.shortCut, R.string.delMenu, R.string.thumbMenu,
			R.string.rotateMenu, R.string.onlineMenu, R.string.noticeMenu, R.string.aboutMenu, R.string.setMenu};

	private static final int MENU_ITEM[] = {DEF.MENU_SHORTCUT, DEF.MENU_SIORI, DEF.MENU_THUMBDEL,
			DEF.MENU_ROTATE, DEF.MENU_ONLINE, DEF.MENU_NOTICE, DEF.MENU_ABOUT, DEF.MENU_SETTING};

	private static final int MENU_IMAGE[] = {R.drawable.add_smartphone, R.drawable.delete_favorite,
			R.drawable.delete_history, R.drawable.rotate, R.drawable.help,
			R.drawable.note, R.drawable.info,
			R.drawable.config};

	private static Context mContext;
	/**
	 * 更新の有無をチェック
	 * @param listtype 表示対象リストの種類 このクラスのTYPE_***
	 * @return 更新の有無
	 * 		- 画面更新の要・不要
	 */
	public static boolean checkModified(int listtype, long modified) {
		String filepath = getFilePath(listtype);

		if (listtype == TYPE_SERVER  || listtype == TYPE_MENU){
			return true;
		}
		else {
			File file = new File(filepath);
			if (!file.exists()) {
				// ファイルが存在しない
				return false;
			}

			if (modified > 0) {
				// ファイル更新時刻取得
				long lastmodified = file.lastModified();
				if (lastmodified <= modified) {
					// 更新されていなければ
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * リストを読み込み
	 */
	public static ArrayList<RecordItem> load(ArrayList<RecordItem> list, int listtype) {
		if(listtype == TYPE_SERVER) {
			if (list == null) {
				// なければ新規
				list = new ArrayList<RecordItem>();
			}
			else {
				list.clear();
			}

			for (int i = ServerSelect.INDEX_LOCAL; i < ServerSelect.MAX_SERVER; i++) {
				// データ設定
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
				ServerSelect server = new ServerSelect(sharedPreferences, mContext);
				RecordItem data = new RecordItem();
				data.setType(RecordItem.TYPE_SERVER);
				data.setServer(i);
				data.setServerName(server.getName(i));
				data.setHost(server.getHost(i));
				data.setUser(server.getUser(i));
				data.setPass(server.getPass(i));
				data.setPath(server.getPath(i));
				list.add(data);
			}
			return list;
		}
		else if(listtype == TYPE_MENU) {
			if (list == null) {
				// なければ新規
				list = new ArrayList<RecordItem>();
			} else {
				list.clear();
			}

			// データ設定
			for (int index = 0; index < MENU_TITLE_ID.length; index++){
				Resources res = mContext.getResources();
				RecordItem data = new RecordItem();
				data.setType(RecordItem.TYPE_MENU);
				data.setItem(MENU_ITEM[index]);
				data.setIcon(MENU_IMAGE[index]);
				data.setDispName(res.getString(MENU_TITLE_ID[index]));
				list.add(data);
			}
			return list;
		}
		else {
			return load(list, listtype, -2, null, null);
		}
	}

	// ブックマークを読み込み(パス指定あり)
	public static ArrayList<RecordItem> load(ArrayList<RecordItem> list, int listtype, int server, String path, String name) {
		if (list == null) {
			// なければ新規
			list = new ArrayList<RecordItem>();
		}
		else {
			list.clear();
		}

		String filepath = getFilePath(listtype);
		FileInputStream is;
		InputStreamReader sr;
		BufferedReader br;

		try {
			File file = new File(filepath);
			if (!file.exists()) {
				// ファイルが存在しない
				return list;
			}

			is = new FileInputStream(filepath);
			sr = new InputStreamReader(is, "UTF-8");
			br = new BufferedReader(sr, 8192);

			String line;
			// パス,日付(long値),ページ
			String[] params = new String[10];
			while ((line = br.readLine()) != null) {
				int pos = 0;
				int next = 0;
				int index = 0;
				for (index = 0 ; index < 9 ; index ++) {
					next = line.indexOf(SEPARATOR, pos);
					if (next >= 0) {
						params[index] = line.substring(pos, next);
						pos = next + 1;
					}
					else {
						// 項目不足
						break;
					}
				}
				if (index < 7) {
					// 項目不足
					continue;
				}
				// 名称
				params[index] = line.substring(pos);
				
				// データ設定
				RecordItem data = new RecordItem();
				try {
					int type = Integer.parseInt(params[INDEX_TYPE]);
					data.setType(type & 0x00FF);
					data.setServer(Integer.parseInt(params[INDEX_SERVER]));
					data.setPath(params[INDEX_PATH]);
					data.setFile(params[INDEX_FILE]);
					data.setDate(Long.parseLong(params[INDEX_DATE]));
					data.setImage(params[INDEX_IMAGE]);
					data.setPage(Integer.parseInt(params[INDEX_PAGE]));
					data.setDispName(params[INDEX_DISPNAME]);
					if (index >= INDEX_CHAPTER) {
						data.setChapter(Integer.parseInt(params[INDEX_CHAPTER]));
					}
					if (index >= INDEX_PAGERATE) {
						data.setPageRate(Float.parseFloat(params[INDEX_PAGERATE]));
					}
				}
				catch (Exception ex) {
					Log.e("Bookmark/load", ex.getMessage());
					continue;
				}
				if (server < -1 ||
						(data.getServer() == server && data.getPath().equals(path) && data.getFile().equals(name))) {
					list.add(data);
				}
			}

			br.close();
			sr.close();
			is.close();
		}
		catch (Exception ex) {
			// 例外発生
			String msg = "";
			if (ex != null && ex.getMessage() != null) {
				msg = ex.getMessage();
			}
			Log.e("Bookmark/Load", msg);
		}
		return list;
	}

	public static void add(int listtype, int type, int server, String path, String file, long date, String image, int page, String name) {
		ArrayList<RecordItem>list = load(null, listtype);

		if (name == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			name = sdf.format(new Date(date));
		}
		RecordItem data = new RecordItem();
		data.setType(type);
		data.setServer(server);
		data.setPath(path);
		data.setFile(file);
		data.setDate(date);
		data.setImage(image);
		data.setPage(page);
		data.setDispName(name);
		data.setChapter(-1);
		data.setPageRate(-1f);
		if (listtype == TYPE_HISTORY) {
			// 履歴の場合、既存は削除する
			int index = list.indexOf(data);
			if (index >= 0) {
				// 登録済なら既存を削除して追加
				list.remove(index);
				list.add(data);
				update(list, listtype);
				return;
			}
		}
		String filepath = getFilePath(listtype);
		FileOutputStream os;
		OutputStreamWriter sw;
		BufferedWriter bw;

		try {
			// ファイルオープン
			os = new FileOutputStream(filepath, true);
			sw = new OutputStreamWriter(os, "UTF-8");
			bw = new BufferedWriter(sw, 8192);
		}
		catch (Exception e) {
			// ファイル作成失敗
			Log.e("saveBookmark/Open", e.getMessage());
			return;
		}

		try {
//			int size = list.size();
//			for (int i = 0 ; i < size ; i++) {
//				bw.newLine();
//			}
//			String encpass = Aes.encode(pass);
//			if (encpass == null) {
//				encpass = "";
//			}
			bw.write(type + SEPARATOR + server + SEPARATOR + data.getPath() + SEPARATOR + data.getFile() + SEPARATOR + date + SEPARATOR + data.getImage() + SEPARATOR + page + SEPARATOR + data.getDispName() + SEPARATOR + data.getChapter() + SEPARATOR + data.getPageRate());
			bw.newLine();
		}
		catch (Exception e) {
			// 書き込みエラー
			Log.e("saveBookmark/Write", e.getMessage());
		}

		try {
			bw.flush();
			bw.close();
		}
		catch (Exception e) {
			// クローズエラー
			Log.e("saveBookmark/Close", e.getMessage());
		}
		return;
	}

	public static void add(int listtype, int type, int server, String path, String file, long date, String image, int chapter, float pagerate, int page, String name) {
		ArrayList<RecordItem>list = load(null, listtype);

		if (name == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			name = sdf.format(new Date(date));
		}
		RecordItem data = new RecordItem();
		data.setType(type);
		data.setServer(server);
		data.setPath(path);
		data.setFile(file);
		data.setDate(date);
		data.setImage(image);
		data.setPage(page);
		data.setDispName(name);
		data.setChapter(chapter);
		data.setPageRate(pagerate);
		if (listtype == TYPE_HISTORY) {
			// 履歴の場合、既存は削除する
			int index = list.indexOf(data);
			if (index >= 0) {
				// 登録済なら既存を削除して追加
				list.remove(index);
				list.add(data);
				update(list, listtype);
				return;
			}
		}
		String filepath = getFilePath(listtype);
		FileOutputStream os;
		OutputStreamWriter sw;
		BufferedWriter bw;

		try {
			// ファイルオープン
			os = new FileOutputStream(filepath, true);
			sw = new OutputStreamWriter(os, "UTF-8");
			bw = new BufferedWriter(sw, 8192);
		}
		catch (Exception e) {
			// ファイル作成失敗
			Log.e("saveBookmark/Open", e.getMessage());
			return;
		}

		try {
//			int size = list.size();
//			for (int i = 0 ; i < size ; i++) {
//				bw.newLine();
//			}
//			String encpass = Aes.encode(pass);
//			if (encpass == null) {
//				encpass = "";
//			}
			bw.write(type + SEPARATOR + server + SEPARATOR + data.getPath() + SEPARATOR + data.getFile() + SEPARATOR + date + SEPARATOR + data.getImage() + SEPARATOR + page + SEPARATOR + data.getDispName() + SEPARATOR + chapter + SEPARATOR + pagerate);
			bw.newLine();
		}
		catch (Exception e) {
			// 書き込みエラー
			Log.e("saveBookmark/Write", e.getMessage());
		}

		try {
			bw.flush();
			bw.close();
		}
		catch (Exception e) {
			// クローズエラー
			Log.e("saveBookmark/Close", e.getMessage());
		}
		return;
	}

	public static void update(ArrayList<RecordItem> list, int listtype) {
		String filepath = getFilePath(listtype);
		FileOutputStream os;
		OutputStreamWriter sw;
		BufferedWriter bw;

		if(listtype == TYPE_SERVER){
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			ServerSelect server = new ServerSelect(sharedPreferences, mContext);
			for (int i = 1; i < list.size(); i++) {
				int index = list.get(i).getServer();
				server.setData(index, list.get(i));
			}
			return;
		}
		if(listtype == TYPE_MENU){
			return;
		}
		try {
			// ファイルオープン
			os = new FileOutputStream(filepath, false);
			sw = new OutputStreamWriter(os, "UTF-8");
			bw = new BufferedWriter(sw, 8192);
		}
		catch (Exception e) {
			// ファイル作成失敗
			Log.e("saveBookmark/Open", e.getMessage());
			return;
		}
		for (int i = 0 ; i < list.size() ; i ++) {
			RecordItem data = list.get(i);
			int type = data.getType();
			int server = data.getServer();
			String path = data.getPath();
			String file = data.getFile();
			long date = data.getDate();
			String image = data.getImage();
			int page = data.getPage();
			String name = data.getDispName();
			int chapter = data.getChapter();
			float pagerate = data.getPageRate();
			try {
				bw.write(type + SEPARATOR + server + SEPARATOR + path + SEPARATOR + file + SEPARATOR + date + SEPARATOR + image + SEPARATOR + page + SEPARATOR + name + SEPARATOR + chapter + SEPARATOR + pagerate);
				bw.newLine();
			}
			catch (Exception e) {
				// 書き込みエラー
				Log.e("saveBookmark/Write", e.getMessage());
			}
		}

		try {
			bw.flush();
			bw.close();
		}
		catch (Exception e) {
			// クローズエラー
			Log.e("saveBookmark/Close", e.getMessage());
		}
		return;
	}

	private static String getFilePath(int type) {
		String path = DEF.getBaseDirectory() + "conf/";
		try {
			// ディレクトリがなければ作成
			new File(path).mkdirs();
		}
		catch (Exception e) {
			Log.e("Bookmark/mkdirs", e.getMessage());
		}

		return path + FILENAME[type];
	}

	public static void setContext(Context context){ mContext = context; }

}