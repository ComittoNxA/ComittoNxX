package src.comitton.fileaccess;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import androidx.core.provider.DocumentsContractCompat;

import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.Logcat;
import src.comitton.fileview.data.FileData;

public class SafFileAccess {
	private static final String TAG = "SafFileAccess";

	public static String filename(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		String name;
		try {
			name = getName(context, rootUri);
			if (isDirectory(context, rootUri)) {
				name += "/";
			}
		}
		catch (Exception e) {
			name = "";
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します. name=" + name);
		return name;
	}

	public static String parent(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.w(logLevel, "サポート外です.");
		return "";
	}

	public static String relativePath(@NonNull final Context context, @NonNull final String base, @NonNull final String target) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. base=" + base + ", target=" + target);

		// baseから末尾のスラッシュを除いた文字列を結果の初期値に代入する
		String result = base.replaceFirst("/*$", "");
		Logcat.d(logLevel, "初期化します. result=" + result);

		if (target.startsWith("content://")) {
			// targetがcontent://で始まるならtargeを返す
			result = target;
			Logcat.d(logLevel, "target が content:// で始まっています. result=" + result);
		}
		else {
			Cursor cursor = null;
			try {
				String[] targetArray = target.split("/");
				String docId;
				String name;
				String mime;

				final ContentResolver contentResolver = context.getContentResolver();

				ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient(Uri.parse(result));
				if (contentProviderClient == null) {
					Logcat.e(logLevel, context.getString(R.string.noResponseProvider) + ", base=" + base + ", target=" + target);
					return result;
				}

				Uri uri;
				for (int i = 0; i < targetArray.length; ++i) {
					Logcat.d(logLevel, "ループを実行します. i=" + i + ", targetArray.length=" + targetArray.length);

					if (targetArray[i].isEmpty()) {
						// 文字列が空文字列なら次を実行する
						Logcat.d(logLevel, "文字列が空なのでスキップします. i=" + i + ", targetArray.length=" + targetArray.length);
						continue;
                    }
					else if (targetArray[i].equals("..")) {
						// 文字列が『..』ならエラー
						Logcat.w(logLevel, "親ディレクトリ指定には対応していません. i=" + i + ", targetArray.length=" + targetArray.length);
						return "";
					} else {
						// 文字列が『..』以外なら子を探す
						uri = Uri.parse(result);
						String documentId = getDocumentId(context, uri);
						Uri childTree = DocumentsContractCompat.buildChildDocumentsUriUsingTree(uri, documentId);

						cursor = contentResolver.query(childTree,
								new String[]{
										DocumentsContract.Document.COLUMN_DOCUMENT_ID,
										DocumentsContract.Document.COLUMN_DISPLAY_NAME,
										DocumentsContract.Document.COLUMN_MIME_TYPE
								},
								// フィルタを設定しても全件返される
								DocumentsContract.Document.COLUMN_DISPLAY_NAME + "=?",
								new String[]{targetArray[i]},
								null
						);

						Logcat.d(logLevel, "子要素を検索します. targetArray[" + i + "]=" + targetArray[i]);
						boolean find = false;
						while (cursor.moveToNext()) {
							docId = cursor.getString(0);
							name = cursor.getString(1);
							mime = cursor.getString(2);
							Logcat.d(logLevel, "子要素の名前. name=" + name);

							if (targetArray[i].equals(name)) {
								// ファイル名が一致するなら
								find = true;
								result = DocumentsContractCompat.buildDocumentUriUsingTree(Uri.parse(result), docId).toString();
								if (i == targetArray.length - 1 && DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
									// 最後の回でディレクトリの場合
									result += "/";
								}
								Logcat.d(logLevel, "子要素を取得しました. i=" + i + ", targetArray[index]=" + targetArray[i] + ", result=" + result);
								break;
							}
						}
						if (!find) {
							// 名前が一致しなければエラー
							Logcat.w(logLevel, "ファイルが存在しません. i=" + i + ", targetArray.length=" + targetArray.length + ", name=" + targetArray[i]);
							return "";
						}
					}
				}
			} catch (Exception e) {
				result = "";
				Logcat.e(logLevel, "エラーが発生しました. base=" + base + ", target=" + target, e);
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (RuntimeException re) {
						throw re;
					} catch (Exception ignore) {
						// ignore exception
					}
				}
			}
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static ParcelFileDescriptor openParcelFileDescriptor(@NonNull final Context context, @NonNull final String uri) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		ParcelFileDescriptor result = null;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = contentResolver.openFileDescriptor(Uri.parse(rootUri),"r");
			if (result == null) {
				Logcat.e(logLevel, "エラーが発生しました. ParcelFileDescriptor == null");
				new FileAccessException(TAG + "SafRandomAccessFile: エラーが発生しました. ParcelFileDescriptor == null");
			}
		}
		catch (Exception e) {
			result = null;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
			new FileAccessException(TAG + "SafRandomAccessFile: エラーが発生しました. " + e.getLocalizedMessage());
		}

		Logcat.d(logLevel, "終了します.");
		return result;
	}

	// RandomAccessFile
	public static SafRandomAccessFile openRandomAccessFile(@NonNull final Context context, @NonNull final String uri, @NonNull final String mode) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. ");

		String rootUri = uri.replaceFirst("/*$", "");

		SafRandomAccessFile result = null;
		try {
			result = new SafRandomAccessFile(context, rootUri, "r");
		}
		catch (Exception e) {
			result = null;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します.");
		return result;
	}

	public static InputStream getInputStream(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		InputStream result = null;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = contentResolver.openInputStream(Uri.parse(rootUri));
		}
		catch (Exception e) {
			result = null;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static OutputStream getOutputStream(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);
		String rootUri = uri.replaceFirst("/*$", "");

		OutputStream result = null;
		try {
			DocumentFile documentFile = DocumentFile.fromSingleUri(context, Uri.parse(rootUri));
			ContentResolver contentResolver = context.getContentResolver();
			result = contentResolver.openOutputStream(Uri.parse(rootUri));
		}
		catch (Exception e) {
			result = null;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// ファイル存在チェック
	public static boolean exists(@NonNull final Context context, @NonNull final String uri) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			Cursor cursor = context.getContentResolver().query(Uri.parse(rootUri),
					null, null, null, null);
			try {
				if (cursor != null) {
					result = true;
				}
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (RuntimeException re) {
						throw re;
					} catch (Exception ignore) {
						// ignore exception
					}
				}
			}
		}
		catch (Exception e) {
			result = false;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
			throw new FileAccessException(TAG + ": exists: " + e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static boolean isDirectory(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			result = DocumentsContract.Document.MIME_TYPE_DIR.equals(getMimeType(context, rootUri));
			Logcat.d(logLevel, "MIME_TYPE_DIR: result=" + result);
		}
		catch (Exception e) {
			result = false;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static ArrayList<FileData> listFiles(@NonNull final Activity activity, @NonNull final String uri, @Nullable Handler handler) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		ArrayList<FileData> fileList = new ArrayList<FileData>();

		Uri rootUri = Uri.parse(uri.replaceFirst("/*$", ""));
		Logcat.d(logLevel, "rootUri=" + rootUri);

		String docId;
		String childUri;
		Uri parseUri;
		String name;
		long size;
		long date;
		String mime;

		ContentResolver contentResolver = activity.getContentResolver();

		ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient(rootUri);
		if (contentProviderClient == null) {
			Logcat.e(logLevel, activity.getString(R.string.noResponseProvider) + ", uri=" + uri);
			DEF.sendMessage(activity, R.string.noResponseProvider, Toast.LENGTH_LONG, handler);
			return fileList;
		}

		Cursor cursor = null;
		try {
			String documentId = getDocumentId(activity, rootUri);
            Uri childTree = DocumentsContractCompat.buildChildDocumentsUriUsingTree(rootUri, documentId);

			cursor = contentResolver.query(childTree, new String[]{
					DocumentsContract.Document.COLUMN_DOCUMENT_ID,
					DocumentsContract.Document.COLUMN_DISPLAY_NAME,
					DocumentsContract.Document.COLUMN_SIZE,
					DocumentsContract.Document.COLUMN_LAST_MODIFIED,
					DocumentsContract.Document.COLUMN_MIME_TYPE
			}, null, null, null);

			while (cursor.moveToNext()) {
				docId = cursor.getString(0);
				//childUri = DocumentsContractCompat.buildDocumentUriUsingTree(rootUri, docId).toString();
				//parseUri = Uri.parse(childUri);
				name = cursor.getString(1);
				size = cursor.getLong(2);
				date = cursor.getLong(3);
				mime = cursor.getString(4);
				Logcat.d(logLevel, MessageFormat.format("name={0}, size={1}, date={2}, mime={3}, dicId={4}", new Object[]{name, size, date, mime, docId}));

				FileData fileData;
				if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
					// ディレクトリの場合
					fileData = new FileData(activity, name + "/", size, date);
				}
				else {
					fileData = new FileData(activity, name, size, date);
				}

				fileList.add(fileData);
			}
		}
		catch (SecurityException e) {
			Logcat.e(logLevel, "エラーが発生しました. " + activity.getString(R.string.permissionDenied) + ", uri=" + uri, e);
			DEF.sendMessage(activity, R.string.permissionDenied, Toast.LENGTH_LONG, handler);
		}
		catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}
		finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (RuntimeException re) {
					throw re;
				} catch (Exception ignore) {
					// ignore exception
				}
			}
		}

		if (!fileList.isEmpty()) {
			Collections.sort(fileList, new FileAccess.FileDataComparator());
		}

		Logcat.d(logLevel, "終了します. fileList.size()=" + fileList.size());
		return fileList;
	}

	public static boolean renameTo(@NonNull final Context context, @NonNull final String uri, @NonNull final String fromfile, @NonNull final String tofile) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile);

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			final ContentResolver contentResolver = context.getContentResolver();
			String path = relativePath(context, rootUri, fromfile);
			Logcat.d(logLevel, "path=" + path + ", tofile=" + tofile);
			if (!path.isEmpty()) {
				result = (DocumentsContractCompat.renameDocument(contentResolver, Uri.parse(path), tofile) != null);
			}
			else {
				Logcat.e(logLevel, "ファイルが存在しません.uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile);
			}
		}
		catch (Exception e) {
			result = false;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile, e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// タイムスタンプ
	public static long date(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		long result;
		try {
			result = getDate(context, rootUri);
		}
		catch (Exception e) {
			result = 0L;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// ファイル削除
	public static boolean delete(@NonNull final Context context, @NonNull final String uri) throws FileAccessException {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = DocumentsContract.deleteDocument(contentResolver, Uri.parse(rootUri));
		}
		catch (Exception e) {
			result = false;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// ディレクトリ作成
	public static boolean mkdir(@NonNull final Context context, @NonNull final String uri, @NonNull final String item) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", item=" + item);

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			DocumentFile documentFile = DocumentFile.fromSingleUri(context, Uri.parse(rootUri));
			result = (documentFile.createDirectory(item) != null);
		}
		catch (Exception e) {
			result = false;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	// ディレクトリ作成
	public static boolean createFile(@NonNull final Context context, @NonNull final String uri, @NonNull final String item) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri + ", item=" + item);

		String rootUri = uri.replaceFirst("/*$", "");

		DocumentFile documentFile = null;
		boolean result = false;
		try {
			if (!relativePath(context, uri, item).isEmpty()) {
				Logcat.e(logLevel, "ファイルが存在します.");
				return false;
			}

			String item2 = item;
			if (item.endsWith("._dl")) {
				item2 = item.substring(0, item.lastIndexOf('.'));
			}
			String mimeType = FileData.getMimeType(context, item2);
			Logcat.d(logLevel, "mimeType=" + mimeType);

			DocumentFile documentParent = DocumentFile.fromSingleUri(context, Uri.parse(rootUri));
			result = (documentParent.createFile(mimeType, item) != null);
		}
		catch (Exception e) {
			result = false;
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri + ", item=" + item, e);
		}


		if (!relativePath(context, uri, item).isEmpty()) {
			Logcat.d(logLevel, "ファイルが存在します.");
			result = true;
		}
		else {
			Logcat.e(logLevel, "ファイルが存在しません.");
			result = true;
		}

		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static String getPathName(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		Logcat.d(logLevel, "開始します. uri=" + uri);

		// 先頭からパッケージ名までをheaderに格納
		String header = uri.replaceFirst("^(smb://[^/]+).*/", "$1");
		String rootUri = uri.replaceFirst("/*$", "");

		String result = "";
		try {
			ContentResolver contentResolver = context.getContentResolver();
            DocumentsContract.Path path = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                path = DocumentsContract.findDocumentPath(contentResolver, Uri.parse(rootUri));
				List<String> pathList = path.getPath();
				result = pathList.get(pathList.size() - 1);
            }
			else {
				Uri.decode(rootUri);
			}
		}
		catch (Exception e) {
			Logcat.d(logLevel, "エラーが発生しました. uri=" + uri, e);
            try {
                result = URLDecoder.decode(rootUri,"UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ;
            }
        }

		if (!result.startsWith("content://")) {
			result = header + result;
		}
		Logcat.d(logLevel, "終了します. result=" + result);
		return result;
	}

	public static long length(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			return getLongValue(context, uri, DocumentsContract.Document.COLUMN_SIZE);
		} catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}
		return 0L;
	}

	public static long getDate(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			return getLongValue(context, uri, DocumentsContract.Document.COLUMN_LAST_MODIFIED);
		} catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}
		return 0L;
	}

	public static String getName(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			return getStringValue(context, uri, DocumentsContract.Document.COLUMN_DISPLAY_NAME);
		} catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}
		return "";
	}

	public static String getMimeType(@NonNull final Context context, @NonNull final String uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		try {
			return getStringValue(context, uri, DocumentsContract.Document.COLUMN_MIME_TYPE);
		} catch (Exception e) {
			Logcat.e(logLevel, "エラーが発生しました. uri=" + uri, e);
		}
		return "";
	}

	private static long getLongValue(@NonNull final Context context, @NonNull final String documentUri, @NonNull final String projection) {
		Cursor cursor = context.getContentResolver().query(Uri.parse(documentUri),
				new String[]{projection},
				null, null, null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				return cursor.getLong(0);
			}
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (RuntimeException re) {
					throw re;
				} catch (Exception ignore) {
					// ignore exception
				}
			}
		}
		return 0L;
	}

	private static String getStringValue(@NonNull final Context context, @NonNull final String documentUri, @NonNull final String projection) {
		Cursor cursor = context.getContentResolver().query(Uri.parse(documentUri),
				new String[]{projection},
				null, null, null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				return cursor.getString(0);
			}
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (RuntimeException re) {
					throw re;
				} catch (Exception ignore) {
					// ignore exception
				}
			}
		}
		return "";
	}

	private static String getDocumentId(@NonNull final Context context, @NonNull final Uri uri) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String documentId = "";
		if (DocumentsContractCompat.isDocumentUri(context, uri)) {
			documentId = DocumentsContractCompat.getDocumentId(uri);
			Logcat.d(logLevel, "DOCUMENT: documentId=" + documentId);
		}
		else if (DocumentsContractCompat.isTreeUri(uri)) {
			documentId = DocumentsContractCompat.getTreeDocumentId(uri);
			Logcat.d(logLevel, "TREE: documentId=" + documentId);
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			if (DocumentsContract.isRootsUri(context, uri)) {
				documentId = DocumentsContract.getRootId(uri);
				Logcat.d(logLevel, "ROOT: documentId=" + documentId);
			}
		}
		else {
			documentId = DocumentsContract.getRootId(uri);
			Logcat.d(logLevel, "多分ROOT: documentId=" + documentId);
		}
		return documentId;
	}

}
