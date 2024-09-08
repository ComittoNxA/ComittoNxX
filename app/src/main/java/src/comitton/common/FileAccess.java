package src.comitton.common;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

import src.comitton.data.FileData;
import src.comitton.exception.FileAccessException;

public class FileAccess {
//	public static final int TYPE_FILE = 0;
//	public static final int TYPE_DIR = 1;

//	public static final int KEY_NAME = 0;
//	public static final int KEY_IS_DIRECTORY = 1;
//	public static final int KEY_LENGTH = 2;
//	public static final int KEY_LAST_MODIFIED = 3;

//	private static final int REQUEST_CODE = 1;

	public static final int SMBLIB_JCIFS = 0;

	private static int SMBLIB = SMBLIB_JCIFS;

	public static int getSmbMode() {
		return SMBLIB;
	}

//	// ユーザ認証付きSambaアクセス
//	public static SmbFile jcifsFile(String url) throws MalformedURLException {
//		String user = null;
//		String pass = null;
//
//		// パラメタチェック
//		if (url.indexOf("smb://") == 0) {
//			int idx = url.indexOf("@");
//			if (idx >= 0) {
//				String userpass = url.substring(6, idx);
//				idx = userpass.indexOf(":");
//				if (idx >= 0) {
//					user = userpass.substring(0, idx);
//					user = URLDecoder.decode(user);
//					pass = userpass.substring(idx + 1);
//					pass = URLDecoder.decode(pass);
//				}
//				else {
//					user = userpass;
//					pass = "";
//				}
//			}
//		}
//		return jcifsFile(url, user, pass);
//	}

	// jcifs認証
	public static SmbFile jcifsFile(String url, String user, String pass) throws MalformedURLException {
		SmbFile sfile = null;
		NtlmPasswordAuthenticator smbAuth;
		CIFSContext context = null;
		String domain = "";
		String host = "";
		String share = "";
		String path = "";
		int idx;

		// SMBの基本設定
		Properties prop = new Properties();
		// JCIFSをAgNO3/jcifs-ngからcodelibs/jcifsに変更、SMB1を動作確認
		prop.setProperty("jcifs.smb.client.minVersion", "SMB1");
		// SMB311は動作確認
		prop.setProperty("jcifs.smb.client.maxVersion", "SMB311"); // SMB1, SMB202, SMB210, SMB300, SMB302, SMB311
		// https://github.com/AgNO3/jcifs-ng/issues/171
		prop.setProperty("jcifs.traceResources", "false");
//		prop.setProperty("jcifs.smb.lmCompatibility", "3");
//		prop.setProperty("jcifs.smb.client.useExtendedSecuruty", "true");
//		prop.setProperty("jcifs.smb.useRawNTLM", "true");
//		prop.setProperty("jcifs.smb.client.signingPreferred", "true");
//		prop.setProperty("jcifs.smb.client.useSMB2Negotiation", "true");
//		prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");
//
//		prop.setProperty("jcifs.smb.client.signingEnforced", "true");
//		prop.setProperty("jcifs.smb.client.disableSpnegoIntegrity", "true");
//
		try {
			// BaseContextではコネクションが足りなくなるため、SingletonContextを使用する
//			Configuration config = new PropertyConfiguration(prop);
//			context = new BaseContext(config);
			SingletonContext.init(prop);
		} catch (CIFSException e) {
			Log.d("FileAccess", "jcifsFile " + e.getMessage());
		}


		host = url.substring(6);
		idx = host.indexOf("/");
		if (idx >= 0){
			path = host.substring(idx + 1);
			host = host.substring(0, idx);
		}
		idx = path.indexOf("/", 1);
		if (idx >= 0){
			share = path.substring(0, idx);
			path = path.substring(idx);
		}

		if (user != null && user.length() != 0) {
			idx = user.indexOf(";");
			if (idx >= 0){
				domain = user.substring(0, idx);
				user = user.substring(idx + 1);
			}
		}

		Log.d("FileAccess", "jcifsFile domain=" + domain + ", user=" + user + ", pass=" + pass + ", host=" + host + ", share=" + share + ", path=" + path);

		if (domain != null && domain.length() != 0) {
			smbAuth = new NtlmPasswordAuthenticator(domain, user, pass);
			context = SingletonContext.getInstance().withCredentials(smbAuth);

		} else if (user != null && user.length() != 0 && !user.equalsIgnoreCase("guest")) {
			smbAuth = new NtlmPasswordAuthenticator(user, pass);
			context = SingletonContext.getInstance().withCredentials(smbAuth);

		} else if (user.equalsIgnoreCase("guest")) {
			// Guest認証を期待するWindows共有の接続向け
			context = SingletonContext.getInstance().withGuestCrendentials();
		} else {
			// Connect with anonymous mode
			context = SingletonContext.getInstance().withAnonymousCredentials();
		}

		sfile = new SmbFile(url, context);
		return sfile;
	}

	// ユーザ認証付きSambaストリーム
	public static SmbRandomAccessFile jcifsAccessFile(String url, String user, String pass) throws IOException {
		Log.d("FileAccess", "smbRandomAccessFile url=" + url + ", user=" + user + ", pass=" + pass);
		SmbRandomAccessFile stream;
		try {
			if (!exists(url, user, pass)) {
				throw new IOException("File not found.");
			}
		} catch (FileAccessException | IOException e) {
			throw new IOException("File not found.");
		}
		SmbFile sfile = jcifsFile(url, user, pass);
		stream = new SmbRandomAccessFile(sfile, "r");
		return stream;
	}

	// ローカルファイルのOutputStream
	public static OutputStream localOutputStream(String url) throws FileAccessException {
		Log.d("FileAccess", "localOutputStream url=" + url);
		boolean result;
		if (url.startsWith("/")) {
			// ローカルの場合
			try {
				File orgfile = new File(url);
				if (!orgfile.exists()) {
					// ファイルがなければ作成する
					orgfile.createNewFile();
				}
				return new FileOutputStream(orgfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// ファイル存在チェック
	public static boolean exists(String url) throws FileAccessException {
		String user = null;
		String pass = null;

		// パラメタチェック
		if (url.startsWith("/")) {
			return exists(url, "", "");
		}
		else if (url.indexOf("smb://") == 0) {
			int idx = url.indexOf("@");
			if (idx >= 0) {
				String userpass = url.substring(6, idx);
				idx = userpass.indexOf(":");
				if (idx >= 0) {
					user = userpass.substring(0, idx);
					user = URLDecoder.decode(user);
					pass = userpass.substring(idx + 1);
					pass = URLDecoder.decode(pass);
				}
				else {
					user = userpass;
					pass = "";
				}
			}
		}
		return exists(url, user, pass);
	}

	// ファイル存在チェック
	public static boolean exists(String url, String user, String pass) throws FileAccessException {
		Log.d("FileAccess", "exists url=" + url + ", user=" + user + ", pass=" + pass);
		boolean result = false;
		if (url.startsWith("/")) {
			// ローカルの場合/
			File orgfile = new File(url);
			result = orgfile.exists();
		}
		else if (SMBLIB == SMBLIB_JCIFS) {
			// jcifsの場合
			SmbFile orgfile;
			try {
				orgfile = FileAccess.jcifsFile(url, user, pass);
			} catch (MalformedURLException e) {
				throw new FileAccessException(e);
			}
			try {
				result = orgfile.exists();
			} catch (SmbException e) {
				throw new FileAccessException(e);
			}
		}
		return result;
	}

	public static boolean isDirectory(String url, String user, String pass) throws MalformedURLException, SmbException {
		Log.d("FileAccess", "isDirectory url=" + url + ", user=" + user + ", pass=" + pass);
		boolean result = false;
		if (url.startsWith("/")) {
			// ローカルの場合/
			File orgfile = new File(url);
			result = orgfile.isDirectory();
		}
		else if (SMBLIB == SMBLIB_JCIFS) {
			// jcifsの場合
			SmbFile orgfile;
			orgfile = FileAccess.jcifsFile(url, user, pass);
			try {
				result = orgfile.isDirectory();
			} catch (SmbException e) {
				result = false;
			}
		}
		return result;
	}

	public static ArrayList<FileData> listFiles(String url, String user, String pass) throws SmbException {
		Log.d("FileAccess", "listFiles url=" + url + ", user=" + user + ", pass=" + pass);
		boolean isLocal;

		String host = "";
		String share = "";
		String path = "";
		int idx = 0;

		if (url.startsWith("/")) {
			isLocal = true;
		}
		else {
			isLocal = false;

			// URLをホスト、共有フォルダ、パスに分解する
			host = url.substring(6);
			idx = host.indexOf("/");
			if (idx >= 0){
				path = host.substring(idx + 1);
				host = host.substring(0, idx);
			}
			idx = path.indexOf("/", 1);
			if (idx >= 0){
				share = path.substring(0, idx);
				path = path.substring(idx + 1);
			}
		}

		Log.d("FileAccess", "listFiles isLocal=" + isLocal);

		// ファイルリストを取得
		File lfiles[] = null;
		SmbFile jcifsFile = null;
		SmbFile[] jcifsFiles = null;
		String[] fnames = null;
		ArrayList<FileData> fileList = new ArrayList<FileData>();
		int length = 0;

		if (isLocal) {
			// ローカルの場合のファイル一覧取得
			lfiles = new File(url).listFiles();
			if (lfiles == null || lfiles.length == 0) {
				return fileList;
			}
			length = lfiles.length;
		}

		else if (SMBLIB == SMBLIB_JCIFS) {
			// jcifsの場合のファイル一覧取得
			try {
				jcifsFile = FileAccess.jcifsFile(url, user, pass);
			} catch (MalformedURLException e) {
				return fileList;
			}
			try {
				if ((url).indexOf("/", 6) == (url).length() - 1) {
					// ホスト名までしか指定されていない場合
					fnames = jcifsFile.list();
					if (fnames == null || fnames.length == 0) {
						return fileList;
					}
					length = fnames.length;
				}
				else {
					// 共有ポイントまで指定済みの場合
					jcifsFiles = jcifsFile.listFiles();
					if (jcifsFiles == null || jcifsFiles.length == 0) {
						return fileList;
					}
					length = jcifsFiles.length;
				}
			} catch (SmbException e) {
				return fileList;
			}
		}
		
		Log.d("FileAccess", "listFiles length=" + length);

		// FileData型のリストを作成
		boolean flag = false;
		String name = "";
		long size = 0;
		long date = 0;
		short type = 0;
		short exttype = 0;

		for (int i = 0; i < length; i++) {
			if (isLocal) {
				name = lfiles[i].getName();
				flag = lfiles[i].isDirectory();
				size = lfiles[i].length();
				date = lfiles[i].lastModified();
			}
			else if (SMBLIB == SMBLIB_JCIFS) {
				// jcifsの場合
				if ((url).indexOf("/", 6) == (url).length() - 1) {
					// ホスト名までしか指定されていない場合
					name = fnames[i];
					// 全部フォルダ扱い
					flag = true;
				}
				else {
					// 共有ポイントまで指定済みの場合
					name = jcifsFiles[i].getName();
					int len = name.length();
					if (name != null && len >= 1 && name.substring(len - 1).equals("/")) {
						flag = true;
					} else {
						flag = false;
					}
					size = jcifsFiles[i].length();
					date = jcifsFiles[i].lastModified();
				}
			}
			
			if (flag) {
				// ディレクトリの場合
				int len = name.length();
				if (len >= 1 && !name.substring(len - 1).equals("/")) {
					name += "/";
				}
				type = FileData.FILETYPE_DIR;
				exttype = FileData.EXTTYPE_NONE;
			} else {
				// 通常のファイル
				int len = name.length();
				if (len < 5) {
					continue;
				}
				String ext = DEF.getFileExt(name);
				if (FileData.isImage(ext)) {
					type = FileData.FILETYPE_IMG;
					if (DEF.WITH_JPEG && (ext.equals(".jpg") || ext.equals(".jpeg"))) {
						exttype = FileData.EXTTYPE_JPG;
					}
					else if (DEF.WITH_PNG && ext.equals(".png")) {
						exttype = FileData.EXTTYPE_PNG;
					}
					else if (DEF.WITH_GIF && ext.equals(".gif")) {
						exttype = FileData.EXTTYPE_GIF;
					}
					else if (DEF.WITH_WEBP && ext.equals(".webp")) {
						exttype = FileData.EXTTYPE_WEBP;
					}
					else if (DEF.WITH_AVIF && ext.equals(".avif")) {
						exttype = FileData.EXTTYPE_AVIF;
					}
					else if (DEF.WITH_HEIF && (ext.equals(".heif") || ext.equals(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						exttype = FileData.EXTTYPE_HEIF;
					}
					else if (DEF.WITH_JXL && ext.equals(".jxl")) {
						exttype = FileData.EXTTYPE_GIF;
					}
					else {
						exttype = FileData.EXTTYPE_GIF;
					}
				}
				else if (FileData.isArchive(ext)) {
					type = FileData.FILETYPE_ARC;
					if (ext.equals(".zip") || ext.equals(".cbz") || ext.equals(".epub")) {
						exttype = FileData.EXTTYPE_ZIP;
					}
					else if (ext.equals(".rar") || ext.equals(".cbr")) {
						exttype = FileData.EXTTYPE_RAR;
					}
				}
				else if (FileData.isPdf(ext)) {
					type = FileData.FILETYPE_PDF;
					exttype = FileData.EXTTYPE_PDF;
				}
				else if (FileData.isText(ext)) {
					type = FileData.FILETYPE_TXT;
					exttype = FileData.EXTTYPE_TXT;
				}
				else {
				type = FileData.FILETYPE_NONE;
				exttype = FileData.EXTTYPE_NONE;
				}
			}

			FileData fileData = new FileData();
			fileData.setType(type);
			fileData.setExtType(exttype);
			fileData.setName(name);
			fileData.setSize(size);
			fileData.setDate(date);

			fileList.add(fileData);
		}

		if (fileList.size() > 0) {
			Collections.sort(fileList, new FileDataComparator());
		}
		return fileList;
	}

	public static class FileDataComparator implements Comparator<FileData> {

		@Override
		public int compare(FileData f1, FileData f2) {
			if (f1.getType() != FileData.FILETYPE_DIR && f2.getType() == FileData.FILETYPE_DIR) {
				return -1;
			}
			else if (f1.getType() == FileData.FILETYPE_DIR && f2.getType() != FileData.FILETYPE_DIR) {
				return 1;
			}
			else {
				return f1.getName().compareTo(f2.getName());
			}
		}
	}
	
	public static boolean renameTo(String uri, String path, String fromfile, String tofile, String user, String pass) throws FileAccessException {
		Log.d("FileAccess", "renameTo url=" + uri + ", path=" + path + ", fromfile=" + fromfile + ", tofile=" + tofile + ", user=" + user + ", pass=" + pass);
		if (tofile.indexOf('/') > 0) {
			throw new FileAccessException("Invalid file name.");
		}

		if (uri == null || uri.length() == 0) {
			// ローカルの場合のファイル一覧取得
			File orgfile = new File(path + fromfile);
			if (orgfile.exists() == false) {
				// 変更前ファイルが存在しなければエラー
				throw new FileAccessException("File not found.");
			}
			File dstfile = new File(path + tofile);
			if (dstfile.exists() == true) {
				// 変更後ファイルが存在すればエラー
				throw new FileAccessException("File access error.");
			}
			orgfile.renameTo(dstfile);
			return dstfile.exists();
		}
		else if (SMBLIB == SMBLIB_JCIFS) {
			// jcifsの場合
			SmbFile orgfile;
			try {
				orgfile = FileAccess.jcifsFile(uri + path + fromfile, user, pass);
				if (orgfile.exists() == false) {
					// 変更前ファイルが存在しなければエラー
					throw new FileAccessException("File not found.");
				}
			} catch (MalformedURLException e) {
				throw new FileAccessException(e);
			} catch (SmbException e) {
				throw new FileAccessException(e);
			}

			SmbFile dstfile;
			try {
				dstfile = FileAccess.jcifsFile(uri + path + tofile, user, pass);
				if (dstfile.exists() == true) {
					// 変更後ファイルが存在すればエラー
					throw new FileAccessException("File access error.");
				}
			} catch (MalformedURLException e) {
				throw new FileAccessException(e);
			} catch (SmbException e) {
				throw new FileAccessException(e);
			}

			// ファイル名変更
			try {
				orgfile.renameTo(dstfile);
				return dstfile.exists();
			} catch (SmbException e) {
				throw new FileAccessException(e);
			}
		}
		return false;
	}

	// ファイル削除
	public static boolean delete(String url, String user, String pass) throws FileAccessException {
		Log.d("FileAccess", "delete url=" + url + ", user=" + user + ", pass=" + pass );
		boolean result;
		if (url.startsWith("/")) {
			// ローカルの場合
			File orgfile = new File(url);
			orgfile.delete();
			return orgfile.exists();
		}
		else if (SMBLIB == SMBLIB_JCIFS) {
			// jcifsの場合
			SmbFile orgfile;
			try {
				orgfile = FileAccess.jcifsFile(url, user, pass);
			} catch (MalformedURLException e) {
				throw new FileAccessException(e);
			}
			try {
				orgfile.delete();
				return orgfile.exists();
			} catch (SmbException e) {
				throw new FileAccessException(e);
			}
		}
		return false;
	}

	// ディレクトリ作成
	public static boolean mkdir(String url, String item, String user, String pass) throws FileAccessException {
		Log.d("FileAccess", "mkdir url=" + url + ", item=" + item + ", user=" + user + ", pass=" + pass );
		boolean result;
		if (url.startsWith("/")) {
			// ローカルの場合
			File orgfile = new File(url + item);
			return orgfile.mkdir();

		}
//		else {
//			// サーバの場合
//			SmbFile orgfile;
//			try {
//				orgfile = FileAccess.jcifsFile(url + item, user, pass);
//			} catch (MalformedURLException e) {
//				throw new FileAccessException(e);
//			}
//			try {
//				orgfile.mkdir();
//				result = orgfile.exists();
//			} catch (SmbException e) {
//				throw new FileAccessException(e);
//			}
//		}
		return false;
	}

	/**
	 * Get a list of external SD card paths. (KitKat or higher.)
	 *
	 * @return A list of external SD card paths.
	 */
	public static String[] getExtSdCardPaths(Context context) {
		List<String> paths = new ArrayList<>();
		for (File file : context.getExternalFilesDirs("external")) {
//			if (file != null && !file.equals(mActivity.getExternalFilesDir("external"))) {
			if (file != null) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) {
					Log.w("FileAccess", "Unexpected external file dir: " + file.getAbsolutePath());
				}
				else {
					String path = file.getAbsolutePath().substring(0, index);
					try {
						path = new File(path).getCanonicalPath();
					}
					catch (IOException e) {
						// Keep non-canonical path.
					}
					paths.add(path);
				}

			}
		}
		return paths.toArray(new String[paths.size()]);
	}
}
