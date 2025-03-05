package src.comitton.fileaccess;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import jp.dip.muracoro.comittonx.R;
import org.apache.commons.io.FileUtils;
import src.comitton.common.DEF;
import src.comitton.fileview.data.FileData;

public class LocalFileAccess {
	private static final String TAG = "LocaFileAccess";

	public static String filename(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "filename: 開始します. uri=" + uri);}

		return uri.replaceFirst("^.*?(([^/]+)?/?)$", "$1");
	}

	public static long length(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "length: 開始します. uri=" + uri);}
		new File(uri);
		return new File(uri).length();
	}

	public static String parent(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "parent: 開始します. uri=" + uri);}
		String result = uri.replaceFirst("([^/]+?)?/*$", "");
		if (debug) {Log.d(TAG, "parent: 終了します. uri=" + uri + ", result=" + result);}
		return result;
	}

	public static String relativePath(@NonNull final String base, @NonNull final String target) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "relativePath: 開始します. base=" + base + ", target=" + target);}

		String result;
		String tmp;

		if (target.startsWith("/")) {
			// targetがスラッシュで始まるならそのまま返す
			result = target;
		} else {
			// ファイルの場合は親を取得、ディレクトリの場合はそのまま
			String filePath = base.replaceFirst("([^/]+?)?$", "");
			result = filePath + target;
		}

		// 連続するスラッシュは1つにまとめる
		result = result.replaceAll("/+", "/");
		// ../があれば親ディレクトリを削除
		while (true) {
			tmp = result;
			result = result.replaceFirst("[^/]+/\\.\\./", "");
			if (result.equals(tmp)) {
				break;
			}
		}
		// 末尾が..なら親ディレクトリを削除
		result = result.replaceFirst("[^/]+/\\.\\.$", "");

		if (debug) {Log.d(TAG, "relativePath: 終了します. result=" + result);}
		return result;
	}

	public static ParcelFileDescriptor openParcelFileDescriptor(@NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getParcelFileDescriptor: 開始します. uri=" + uri);}

		ParcelFileDescriptor parcelFileDescriptor = null;

		File file = new File(uri);
		try {
			parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
		}
		catch (FileNotFoundException e) {
			throw new FileAccessException(TAG + ": getParcelFileDescriptor: Local File not found.");
		}

		return parcelFileDescriptor;
	}

	// RandomAccessFile
	public static RandomAccessFile openRandomAccessFile(@NonNull final String uri, @NonNull final String mode) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "openRandomAccessFile: 開始します. uri=" + uri + ", mode=" + mode);}

		RandomAccessFile result = null;
        try {
			result = new RandomAccessFile(uri, mode);
		}
		catch (Exception e) {
			result = null;
			Log.e(TAG, "openRandomAccessFile: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "openRandomAccessFile: エラーメッセージ. " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
			}
		}
		if (debug) {Log.d(TAG, "openRandomAccessFile: 終了します.");}
		return result;
    }

	public static InputStream getInputStream(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getInputStream: 開始します. uri=" + uri);}
		try {
			File orgfile = new File(uri);
			return new FileInputStream(orgfile);
		} catch (IOException e) {
			if(debug) {Log.d(TAG, "getInputStream: " + e.getLocalizedMessage());}
		}
		return null;
	}

	public static OutputStream getOutputStream(@NonNull final Activity activity, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getOutputStream: 開始します. uri=" + uri);}

		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
			// Android10(Q) ではない場合

			try {
				File orgfile = new File(uri);
				return new FileOutputStream(orgfile);
			} catch (IOException e) {
				Log.e(TAG, "getOutputStream: " + e.getLocalizedMessage());
			}

		}
		else {
			// Android10(Q) の場合

			String documentUri = documentUri(activity, uri);
			Log.d(TAG, "getOutputStream: documentUri=" + documentUri);
			return SafFileAccess.getOutputStream(activity, documentUri);
		}

		return null;
	}

	// ファイル存在チェック
	public static boolean exists(@NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "exists: 開始します. uri=" + uri);}
		return new File(uri).exists();
	}

	public static boolean isDirectory(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "isDirectory: 開始します. uri=" + uri);}
		boolean result = new File(uri).isDirectory();
		if (debug) {Log.d(TAG, "isDirectory: 終了します. uri=" + uri + ", result=" + result);}
		return result;
	}

	public static ArrayList<FileData> listFiles(@NonNull final Activity activity, @NonNull final String uri) {
		boolean debug = false;
		if(debug) {Log.d(TAG, "listFiles: 開始します. uri=" + uri);}

		// ファイルリストを取得
		File[] lfiles;
		ArrayList<FileData> fileList = new ArrayList<FileData>();
		int length;

		// ローカルの場合のファイル一覧取得
		lfiles = new File(uri).listFiles();
		if (lfiles == null || lfiles.length == 0) {
			return fileList;
		}
		length = lfiles.length;

		if(debug) {Log.d(TAG, "listFiles: length=" + length);}

		// FileData型のリストを作成
		boolean isDir;
		String name;
		long size;
		long date;

		for (int i = 0; i < length; i++) {
			isDir = lfiles[i].isDirectory();
			name = lfiles[i].getName();
			size = lfiles[i].length();
			date = lfiles[i].lastModified();

			if (isDir) {
				// ディレクトリの場合
				if (!name.endsWith("/")) {
					name += "/";
				}
			}

			FileData fileData = new FileData(activity, name, size, date);
			fileList.add(fileData);

			if(debug) {Log.d(TAG, "listFiles: index=" + (fileList.size() - 1) + ", name=" + fileData.getName() + ", type=" + fileData.getType() + ", extType=" + fileData.getExtType());}
		}

		if (!fileList.isEmpty()) {
			Collections.sort(fileList, new FileAccess.FileDataComparator());
		}

		return fileList;
	}

	public static boolean renameTo(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String fromfile, @NonNull final String tofile) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "renameTo: 開始します. uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile);}
		if (tofile.indexOf('/') > 0) {
			throw new FileAccessException(TAG + ": renameTo: Invalid file name.");
		}

		File orgfile = new File(uri + fromfile);
		if (!orgfile.exists()) {
			// 変更前ファイルが存在しなければエラー
			throw new FileAccessException(TAG + ": renameTo: File not found.");
		}
		File dstfile = new File(uri + tofile);
		if (dstfile.exists()) {
			// 変更後ファイルが存在すればエラー
			throw new FileAccessException(TAG + ": renameTo: File access error.");
		}

		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
			// Android10(Q) ではない場合

			// deleteメソッドを使用してファイル名を変更する
			if (orgfile.renameTo(dstfile)) {
				if (debug) {Log.d(TAG, "renameTo: file.renameTo() 成功しました.");}
			} else {
				Log.e(TAG, "renameTo: file.renameTo() 失敗しました.");
			}
		}
		else {
			// Android10(Q) の場合

			// SAFでファイル名を変更する
			String documentUri = documentUri(activity, uri);
			Log.d(TAG, "renameTo: documentUri=" + documentUri);
			if(!documentUri.isEmpty() && SafFileAccess.renameTo(activity, documentUri, fromfile, tofile)) {
				if (debug) {Log.d(TAG, "renameTo: SafFileAccess.renameTo() 成功しました.");}
			} else {
				Log.e(TAG, "renameTo: SafFileAccess.renameTo() 失敗しました.");
			}
		}

		// 変更後ファイルが存在するかチェック
		if (dstfile.exists()) {
			if (debug) {Log.d(TAG, "renameTo: ファイルが存在します.");}
			return true;
		}
		else {
			Log.e(TAG, "renameTo: ファイルが存在しません.");
		}

		return false;
	}

	// タイムスタンプ
	public static long date(@NonNull final Activity activity, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {
			Log.d(TAG, "date: 開始します. uri=" + uri);
		}

		File file = new File(uri);
		return file.lastModified();
	}

	// ファイル削除
	public static boolean delete(@NonNull final Activity activity, @NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "delete: 開始します. uri=" + uri);}

		File file = new File(uri);

		// existsメソッドを使用してファイルの存在を確認する
		if (!file.exists()) {
			// 最初からファイルが存在しない場合
			Log.e(TAG, "delete: ファイルが存在しません.");
			throw new FileAccessException(TAG + ": delete: ファイルが存在しません.");
		}
		else {
			// ファイルが存在する場合

			if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
				// Android10(Q) ではない場合

				// deleteメソッドを使用してファイルを削除する
				if (file.isDirectory()) {
					try {
						FileUtils.deleteDirectory(file);
						if (debug) {Log.d(TAG, "delete: FileUtils.deleteDirectory() 成功しました.");}
					} catch (IOException e) {
						Log.e(TAG, "delete: FileUtils.deleteDirectory() 失敗しました.");
					}

				}
				else {
					if (file.delete()) {
						if (debug) {Log.d(TAG, "delete: file.delete() 成功しました.");}
					} else {
						Log.e(TAG, "delete: file.delete() 失敗しました.");
					}
				}
			}
			else {
				// Android10(Q) の場合

				// SAFでファイルを削除する
				String documentUri = documentUri(activity, uri);
				Log.d(TAG, "delete: documentUri=" + documentUri);
				if(!documentUri.isEmpty() && SafFileAccess.delete(activity, documentUri)) {
					if (debug) {Log.d(TAG, "delete: SafFileAccess.delete() 成功しました.");}
				} else {
					Log.e(TAG, "delete: SafFileAccess.delete() 失敗しました.");
				}
			}
		}

		// 消せたかどうかチェック
		if (!file.exists()) {
			if (debug) {Log.d(TAG, "delete: ファイルが存在しません.");}
			return true;
		}
		else {
			Log.e(TAG, "delete: ファイルが存在します.");
		}

		return false;
	}

	// ディレクトリ作成
	public static boolean mkdir(@NonNull final String uri, @NonNull final String item) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "mkdir: 開始します. uri=" + uri + ", item=" + item);}

		File orgfile = new File(uri + item);
		return orgfile.mkdir();
	}

	// ファイル作成
	public static boolean createFile(@NonNull final Activity activity, @NonNull final String uri, @NonNull final String item) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "createFile: 開始します. uri=" + uri + ", item=" + item);}

		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
			// Android10(Q) ではない場合

			try {
				File orgfile = new File(uri + item);
				if (!orgfile.exists()) {
					// ファイルがなければ作成する
					return orgfile.createNewFile();
				}
			} catch (IOException e) {
				Log.e(TAG, "createFile: " + e.getLocalizedMessage());
			}

		}
		else {
				// Android10(Q) の場合

				// SAFでファイルを作成する
				String documentUri = documentUri(activity, uri);
				Log.d(TAG, "renameTo: documentUri=" + documentUri);
				if(!documentUri.isEmpty() && SafFileAccess.createFile(activity, documentUri, item)) {
					if (debug) {Log.d(TAG, "renameTo: SafFileAccess.createFile() 成功しました.");}
				} else {
					Log.e(TAG, "renameTo: SafFileAccess.createFile() 失敗しました.");
				}
			}

		return false;
	}

	/**
	 * ローカルパスをSAFで使えるパスに変換する
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static String documentUri(@NonNull final Activity activity, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "documentUri: 開始します. uri=" + uri);}

		String result = "";
		String id = "";
		String base = "";
		String target = "";

		if (uri.startsWith("/storage/emulated/0/")) {
			id = "primary";
			target = uri.substring("/storage/emulated/0/".length());
		}
		else if (uri.startsWith("/storage/")) {
			String[] pathArray = uri.split("/");
			id = uri.split("/")[2];
			target = String.join("/", Arrays.copyOfRange(pathArray, 3, pathArray.length));
		}
		base = "content://com.android.externalstorage.documents/tree/" + id + "%3A";

		if (debug) {Log.d(TAG, "documentUri: base=" + base + ", subUri=" + target);}

		/*
		result = SafFileAccess.relativePath(activity, base, target);
		if (result.isEmpty()) {
			requestPermission(activity, base);
		}
		if (debug) {Log.d(TAG, "documentUri: result=" + result);}
		*/

		result = base + "/document/" + id + "%3A" + URLEncoder.encode(target);
		if (debug) {Log.d(TAG, "documentUri: result=" + result);}

		try {
			SafFileAccess.exists(activity, result);
		}
		catch (Exception e) {
			// エラーが出たらアクセス権を要求する
			requestPermission(activity, base);
		}

		if (debug) {Log.d(TAG, "documentUri: 終了します. result=" + result);}
		return result;
	}

	public static void requestPermission(@NonNull final Activity activity, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "requestPermission: 開始します. uri=" + uri);}

		// ストレージアクセスフレームワーク
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		intent.addFlags(
				Intent.FLAG_GRANT_READ_URI_PERMISSION |
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
						Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
						Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
		);
		intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
		activity.startActivityForResult(Intent.createChooser(intent, activity.getText(R.string.SafChooseTitle)), DEF.REQUEST_CODE_ACTION_OPEN_DOCUMENT_TREE);

		if (debug) {Log.d(TAG, "requestPermission: 終了します.");}
	}

}
