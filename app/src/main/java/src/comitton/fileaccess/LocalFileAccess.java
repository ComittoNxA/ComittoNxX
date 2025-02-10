package src.comitton.fileaccess;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import src.comitton.fileview.data.FileData;

public class LocalFileAccess {
	private static final String TAG = "LocaFileAccess";

	public static String filename(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "parent: uri=" + uri);}

		return uri.replaceFirst("^.*?(([^/]+)?/?)$", "$1");
	}

	public static long length(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "length: uri=" + uri);}
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
		if (debug) {Log.d(TAG, "getParcelFileDescriptor: uri=" + uri);}

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
				Log.e(TAG, "openRandomAccessFile: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}
		if (debug) {Log.d(TAG, "openRandomAccessFile: 終了します.");}
		return result;
    }

	public static InputStream getInputStream(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getInputStream: uri=" + uri);}
		try {
			File orgfile = new File(uri);
			return new FileInputStream(orgfile);
		} catch (IOException e) {
			if(debug) {Log.d(TAG, "getInputStream: " + e.getLocalizedMessage());}
		}
		return null;
	}

	public static OutputStream getOutputStream(@NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getOutputStream: uri=" + uri);}
		try {
			File orgfile = new File(uri);
			if (!orgfile.exists()) {
				// ファイルがなければ作成する
				orgfile.createNewFile();
			}
			return new FileOutputStream(orgfile);
		} catch (IOException e) {
			if(debug) {Log.d(TAG, "getOutputStream: " + e.getLocalizedMessage());}
		}
		return null;
	}

	// ファイル存在チェック
	public static boolean exists(@NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "exists: uri=" + uri);}
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
		if(debug) {Log.d(TAG, "listFiles: uri=" + uri);}

		// ファイルリストを取得
		File lfiles[];
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

			FileData fileData = new FileData(activity, name, size, date, relativePath(uri, name));
			fileList.add(fileData);

			if(debug) {Log.d(TAG, "listFiles: index=" + (fileList.size() - 1) + ", name=" + fileData.getName() + ", type=" + fileData.getType() + ", extType=" + fileData.getExtType());}
		}

		if (!fileList.isEmpty()) {
			Collections.sort(fileList, new FileAccess.FileDataComparator());
		}

		return fileList;
	}

	public static boolean renameTo(@NonNull final String uri, @NonNull final String fromfile, @NonNull final String tofile) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "renameTo: uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile);}
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
		return orgfile.renameTo(dstfile);
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

			if (!file.canRead()) {
				Log.e(TAG, "delete: 読み込み権限がありません.");
			}
			if (!file.canWrite()) {
				Log.e(TAG, "delete: 書き込み権限がありません.");
			}

			try {
				// deleteメソッドを使用してファイルを削除する
				if (file.delete()) {
					if (debug) {Log.d(TAG, "delete: file.delete() 成功しました.");}
				} else {
					Log.e(TAG, "delete: file.delete() 失敗しました.");
				}
			} catch (SecurityException e) {
				Log.e(TAG, "delete: SecurityException: " + e.getLocalizedMessage());
				throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
			}

			// 消せたかどうかチェック
			if (!file.exists()) {
				return true;
			}
			else {
				Log.e(TAG, "delete: ファイルが存在します.");
			}

			// *************
			// ここ以降は通常実行されない(Android 10の時は実行される)
			// *************

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				Path path = Paths.get(uri);
				try {
					if (Files.deleteIfExists(path)) {
						if (debug) {Log.d(TAG, "delete: Files.deleteIfExists() 成功しました.");}
					} else {
						Log.e(TAG, "delete: Files.deleteIfExists() 失敗しました.");
					}
				}
				catch (DirectoryNotEmptyException e) {
					Log.e(TAG, "delete: SecurityException: " + e.getLocalizedMessage());
					throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
				}
				catch (SecurityException e) {
					// ソース読んだらSecurityException返さないじゃないか！！
					Log.e(TAG, "delete: SecurityException: " + e.getLocalizedMessage());
					throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
				}
				catch (IOException e) {
					Log.e(TAG, "delete: IOException: " + e.getLocalizedMessage());
				}

				// 消せたかどうかチェック
				if (!file.exists()) {
					return true;
				}
				else {
					Log.e(TAG, "delete: ファイルが存在します.");
				}
			}

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
				// Android10(Q) のとき

				// なにしても消せない…

				final String where = MediaStore.MediaColumns.DATA + "=?";
				final String[] selectionArgs = new String[] {
						file.getAbsolutePath()
				};
				ContentResolver contentResolver = activity.getContentResolver();
				Uri deleteUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
				activity.grantUriPermission(activity.getPackageName(), deleteUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				DocumentFile documentFile = DocumentFile.fromSingleUri(activity, deleteUri);
				if (debug) {Log.d(TAG, "delete: deleteUri=" + deleteUri);}

				// 永続的なアクセス権を要求する
				//final int takeFlags =
				//		( Intent.FLAG_GRANT_READ_URI_PERMISSION |
				//				Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
				//				Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
				//				Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
				//		);
				//activity.grantUriPermission(activity.getPackageName(), deleteUri, takeFlags);

				// ディレクトリにアクセス権を要求するUIを表示する
				//Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				//intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, deleteUri);
				//activity.startActivityForResult(intent, FileSelectActivity.WRITE_REQUEST_CODE);


				try {
					boolean isDeleted = DocumentFile.fromSingleUri(activity, deleteUri).delete();
					if (isDeleted) {
						if (debug) {Log.d(TAG, "delete: DocumentFile.fromSingleUri.delete() 成功しました.");}
					}
					else {
						Log.e(TAG, "delete: DocumentFile.fromSingleUri.delete() 失敗しました.");
					}
				} catch (NullPointerException e) {
					Log.e(TAG, "delete: NullPointerException: " + e.getLocalizedMessage());
					throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
				}

				// 消せたかどうかチェック
				if (!file.exists()) {
					return true;
				}
				else {
					Log.e(TAG, "delete: ファイルが存在します.");
				}

				try {
					boolean isDeleted = DocumentsContract.deleteDocument(contentResolver, documentFile.getUri());
					if (isDeleted) {
						if (debug) {Log.d(TAG, "delete: DocumentsContract.deleteDocument() 成功しました.");}
					}
					else {
						Log.e(TAG, "delete: DocumentsContract.deleteDocument() 失敗しました.");
					}
				} catch (FileNotFoundException e) {
					Log.e(TAG, "delete: FileNotFoundException: " + e.getLocalizedMessage());
					throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
				}

				// 消せたかどうかチェック
				if (!file.exists()) {
					return true;
				}
				else {
					Log.e(TAG, "delete: ファイルが存在します.");
				}

				if (documentFile.delete()) {
					if (debug) {Log.d(TAG, "delete: documentFile.delete() 成功しました.");}
				}
				else {
					Log.e(TAG, "delete: documentFile.delete() 失敗しました.");
				}

				// 消せたかどうかチェック
				if (!file.exists()) {
					return true;
				}
				else {
					Log.e(TAG, "delete: ファイルが存在します.");
				}

				try {
					// sdk 28 (Android 9) 未満
					int numDeleted = contentResolver.delete(deleteUri, where, selectionArgs);
					if (numDeleted != 0) {
						if (debug) {Log.d(TAG, "delete: contentResolver.delete() 成功しました.");}
					}
					else {
						Log.e(TAG, "delete: contentResolver.delete() 失敗しました.");
					}
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "delete: IllegalArgumentException: " + e.getLocalizedMessage());
					throw new FileAccessException(TAG + ": delete: " + e.getLocalizedMessage());
				} catch (SecurityException e) {
					// ソース読んだらSecurityException返さないじゃないか！！
					if (debug) {Log.d(TAG, "delete: SecurityException をキャッチしました.");}
					IntentSender intentSender = null;
					// sdk 30 (Android 11)
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
						if (debug) {Log.d(TAG, "delete: Android11(R)以降のバージョンです.");}
						intentSender = MediaStore.createDeleteRequest(contentResolver, Collections.singletonList(deleteUri)).getIntentSender();
					}
					// sdk 29 (Android 10)
					else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
						if (debug) {Log.d(TAG, "delete: Android10(Q)です.");}
						RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) e;
						if (recoverableSecurityException != null) {
							intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
						}
					}

					if (intentSender != null) {
						int REQUEST_CODE = 2025;
						try {
							activity.startIntentSenderForResult(intentSender, REQUEST_CODE, null, 0, 0, 0, null);
						} catch (IntentSender.SendIntentException ex) {
							throw new RuntimeException(ex);
						}
					}
				}

				// 消せたかどうかチェック
				if (!file.exists()) {
					return true;
				}
				else {
					Log.e(TAG, "delete: ファイルが存在します.");
				}

			}
		}

		return false;
	}

	// ディレクトリ作成
	public static boolean mkdir(@NonNull final String uri, @NonNull final String item) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "mkdir: uri=" + uri + ", item=" + item);}

		File orgfile = new File(uri + item);
		return orgfile.mkdir();
	}
}
