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
import src.comitton.fileview.data.FileData;

public class SafFileAccess {
	private static final String TAG = "SafFileAccess";

	public static String filename(@NonNull final Context context, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "filename: 開始します. uri=" + uri);}

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
			Log.e(TAG, "filename: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "filename: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "filename: 終了します. name=" + name);}
		return name;
	}

	public static String parent(@NonNull final Context context, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "parent: 開始します. uri=" + uri);}
		if (debug) {Log.d(TAG, "parent: サポート外です.");}
		return "";
	}

	public static String relativePath(@NonNull final Context context, @NonNull final String base, @NonNull final String target) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "relativePath: 開始します. base=" + base + ", target=" + target);}
		if (debug) {DEF.StackTrace(TAG, "relativePath: ");}

		// baseから末尾のスラッシュを除いた文字列を結果の初期値に代入する
		String result = base.replaceFirst("/*$", "");
		if (debug) {Log.d(TAG, "relativePath: 初期化します. result=" + result);}

		if (target.startsWith("content://")) {
			// targetがcontent://で始まるならtargeを返す
			result = target;
			if (debug) {Log.d(TAG, "relativePath: target が content:// で始まっています. result=" + result);}
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
					Log.e(TAG, "relativePath: " + context.getString(R.string.noResponseProvider) + ", base=" + base + ", target=" + target);
					return result;
				}

				Uri uri;
				for (int i = 0; i < targetArray.length; ++i) {
					if (debug) {Log.d(TAG, "relativePath: ループを実行します. i=" + i + ", targetArray.length=" + targetArray.length);}

					if (targetArray[i].isEmpty()) {
						// 文字列が空文字列なら次を実行する
						continue;
                    }
					else if (targetArray[i].equals("..")) {
						// 文字列が..なら親を返す
						result = parent(context, result);
					} else {
						// 文字列が..以外なら子を探す
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

						if (debug) {Log.d(TAG, "relativePath: 子要素を検索します. index=" + i + ", targetArray[i]=" + targetArray[i]);}
						while (cursor.moveToNext()) {
							docId = cursor.getString(0);
							name = cursor.getString(1);
							mime = cursor.getString(2);
							if (debug) {Log.d(TAG, "relativePath: 子要素の名前. name=" + name);}

							if (targetArray[i].equals(name)) {
								// ファイル名が一致するなら
								result = DocumentsContractCompat.buildDocumentUriUsingTree(Uri.parse(result), docId).toString();
								if (i == targetArray.length - 1 && DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
									// 最後の回でディレクトリの場合
									result += "/";
								}
								if (debug) {Log.d(TAG, "relativePath: 子要素を取得しました. i=" + i + ", targetArray[index]=" + targetArray[i] + ", result=" + result);}
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				result = "";
				Log.e(TAG, "relativePath: エラーが発生しました. base=" + base + ", target=" + target);
				if (e.getLocalizedMessage() != null) {
					Log.e(TAG, "relativePath: エラーメッセージ. " + e.getLocalizedMessage());
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

		if (debug) {Log.d(TAG, "relativePath: 終了します. result=" + result);}
		return result;
	}

	public static ParcelFileDescriptor openParcelFileDescriptor(@NonNull final Context context, @NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getParcelFileDescriptor: uri=" + uri);}

		String rootUri = uri.replaceFirst("/*$", "");

		ParcelFileDescriptor result = null;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = contentResolver.openFileDescriptor(Uri.parse(rootUri),"r");
			if (result == null) {
				Log.e(TAG, "SafRandomAccessFile: エラーが発生しました. ParcelFileDescriptor == null");
				new FileAccessException(TAG + "SafRandomAccessFile: エラーが発生しました. ParcelFileDescriptor == null");
			}
		}
		catch (Exception e) {
			result = null;
			Log.e(TAG, "getParcelFileDescriptor: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "getParcelFileDescriptor: エラーメッセージ. " + e.getLocalizedMessage());
			}
			new FileAccessException(TAG + "SafRandomAccessFile: エラーが発生しました. " + e.getLocalizedMessage());
		}

		if (debug) {Log.d(TAG, "getParcelFileDescriptor: 終了します.");}
		return result;
	}

	// RandomAccessFile
	public static SafRandomAccessFile openRandomAccessFile(@NonNull final Context context, @NonNull final String uri, @NonNull final String mode) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "openRandomAccessFile:");}

		String rootUri = uri.replaceFirst("/*$", "");

		SafRandomAccessFile result = null;
		try {
			result = new SafRandomAccessFile(context, rootUri, "r");
		}
		catch (Exception e) {
			result = null;
			Log.e(TAG, "getParcelFileDescriptor: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "getParcelFileDescriptor: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "getParcelFileDescriptor: 終了します.");}
		return result;
	}

	public static InputStream getInputStream(@NonNull final Context context, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getInputStream: uri=" + uri);}

		String rootUri = uri.replaceFirst("/*$", "");

		InputStream result = null;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = contentResolver.openInputStream(Uri.parse(rootUri));
		}
		catch (Exception e) {
			result = null;
			Log.e(TAG, "getInputStream: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "getInputStream: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "getInputStream: 終了します. result=" + result);}
		return result;
	}

	public static OutputStream getOutputStream(@NonNull final Context context, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getOutputStream: uri=" + uri);}
		String rootUri = uri.replaceFirst("/*$", "");

		OutputStream result = null;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = contentResolver.openOutputStream(Uri.parse(rootUri));
		}
		catch (Exception e) {
			result = null;
			Log.e(TAG, "getInputStream: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "getInputStream: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "getInputStream: 終了します. result=" + result);}
		return result;
	}

	// ファイル存在チェック
	public static boolean exists(@NonNull final Context context, @NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "exists: uri=" + uri);}

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
			Log.e(TAG, "exists: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "exists: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "exists: 終了します. result=" + result);}
		return result;
	}

	public static boolean isDirectory(@NonNull final Context context, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "isDirectory: 開始します. uri=" + uri);}

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			result = DocumentsContract.Document.MIME_TYPE_DIR.equals(getMimeType(context, rootUri));
			if (debug) {Log.d(TAG, "isDirectory: MIME_TYPE_DIR: result=" + result);}
		}
		catch (Exception e) {
			result = false;
			Log.e(TAG, "isDirectory: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "isDirectory: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "isDirectory: 終了します. result=" + result);}
		return result;
	}

	public static ArrayList<FileData> listFiles(@NonNull final Activity activity, @NonNull final String uri, @Nullable Handler handler) {
		boolean debug = false;
		if(debug) {Log.d(TAG, "listFiles: 開始します. uri=" + uri);}
		//if (debug) {DEF.StackTrace(TAG, "listFiles: ");}

		ArrayList<FileData> fileList = new ArrayList<FileData>();

		Uri rootUri = Uri.parse(uri.replaceFirst("/*$", ""));
		if (debug) {Log.d(TAG, "listFiles: rootUri=" + rootUri);}

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
			Log.e(TAG, "listFiles: " + activity.getString(R.string.noResponseProvider) + ", uri=" + uri);
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
				if(debug) {Log.d(TAG, MessageFormat.format("listFiles: name={0}, size={1}, date={2}, mime={3}, dicId={4}", new Object[]{name, size, date, mime, docId}));}

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
			Log.e(TAG, "listFiles: エラーが発生しました. " + activity.getString(R.string.permissionDenied) + ", uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "listFiles: エラーメッセージ. SecurityException: " + e.getLocalizedMessage());
			}
			DEF.sendMessage(activity, R.string.permissionDenied, Toast.LENGTH_LONG, handler);
		}
		catch (Exception e) {
			Log.e(TAG, "listFiles: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "listFiles: エラーメッセージ. " + e.getLocalizedMessage());
			}
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

		if (debug) {Log.d(TAG, "listFiles: 終了します. fileList.size()=" + fileList.size());}
		return fileList;
	}

	public static boolean renameTo(@NonNull final Context context, @NonNull final String uri, @NonNull final String fromfile, @NonNull final String tofile) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "renameTo: uri=" + uri + ", fromfile=" + fromfile + ", tofile=" + tofile);}

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			final ContentResolver contentResolver = context.getContentResolver();
			result = (DocumentsContractCompat.renameDocument(contentResolver, Uri.parse(relativePath(context, rootUri, fromfile)), tofile) != null);
		}
		catch (Exception e) {
			result = false;
			Log.e(TAG, "renameTo: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "renameTo: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "renameTo: 終了します. result=" + result);}
		return result;
	}

	// ファイル削除
	public static boolean delete(@NonNull final Context context, @NonNull final String uri) throws FileAccessException {
		boolean debug = false;
		if (debug) {Log.d(TAG, "delete: 開始します. uri=" + uri);}

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			result = DocumentsContract.deleteDocument(contentResolver, Uri.parse(rootUri));
		}
		catch (Exception e) {
			result = false;
			Log.e(TAG, "delete: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "delete: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "delete: 終了します. result=" + result);}
		return result;
	}

	// ディレクトリ作成
	public static boolean mkdir(@NonNull final Context context, @NonNull final String uri, @NonNull final String item) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "mkdir: 開始します. uri=" + uri + ", item=" + item);}

		String rootUri = uri.replaceFirst("/*$", "");

		boolean result = false;
		try {
			DocumentFile documentFile = DocumentFile.fromSingleUri(context, Uri.parse(rootUri));
			result = (documentFile.createDirectory(item) != null);
		}
		catch (Exception e) {
			result = false;
			Log.e(TAG, "mkdir: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "mkdir: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}

		if (debug) {Log.d(TAG, "mkdir: 終了します. result=" + result);}
		return result;
	}

	public static String getPathName(@NonNull final Context context, @NonNull final String uri) {
		boolean debug = false;
		if (debug) {Log.d(TAG, "getPathName: 開始します. uri=" + uri);}

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
			Log.e(TAG, "getPathName: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "getPathName: エラーメッセージ. " + e.getLocalizedMessage());
			}
            try {
                result = URLDecoder.decode(rootUri,"UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ;
            }
        }

		if (!result.startsWith("content://")) {
			result = header + result;
		}
		if (debug) {Log.d(TAG, "getPathName: 終了します. result=" + result);}
		return result;
	}

	public static long length(@NonNull final Context context, @NonNull final String uri) {
		try {
			return getLongValue(context, uri, DocumentsContract.Document.COLUMN_SIZE);
		} catch (Exception e) {
			Log.e(TAG, "length: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "length: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}
		return 0L;
	}

	public static String getName(@NonNull final Context context, @NonNull final String uri) {
		try {
			return getStringValue(context, uri, DocumentsContract.Document.COLUMN_DISPLAY_NAME);
		} catch (Exception e) {
			Log.e(TAG, "GetMimeType: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "GetMimeType: エラーメッセージ. " + e.getLocalizedMessage());
			}
		}
		return "";
	}

	public static String getMimeType(@NonNull final Context context, @NonNull final String uri) {
		try {
			return getStringValue(context, uri, DocumentsContract.Document.COLUMN_MIME_TYPE);
		} catch (Exception e) {
			Log.e(TAG, "GetMimeType: エラーが発生しました. uri=" + uri);
			if (e.getLocalizedMessage() != null) {
				Log.e(TAG, "GetMimeType: エラーメッセージ. " + e.getLocalizedMessage());
			}
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
		boolean debug = false;
		String documentId = "";
		if (DocumentsContractCompat.isDocumentUri(context, uri)) {
			documentId = DocumentsContractCompat.getDocumentId(uri);
			if (debug) {Log.d(TAG, "getDocumentId: DOCUMENT: documentId=" + documentId);}
		}
		else if (DocumentsContractCompat.isTreeUri(uri)) {
			documentId = DocumentsContractCompat.getTreeDocumentId(uri);
			if (debug) {Log.d(TAG, "getDocumentId: TREE: documentId=" + documentId);}
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			if (DocumentsContract.isRootsUri(context, uri)) {
				documentId = DocumentsContract.getRootId(uri);
				if (debug) {Log.d(TAG, "getDocumentId: ROOT: documentId=" + documentId);}
			}
		}
		else {
			documentId = DocumentsContract.getRootId(uri);
			if (debug) {Log.d(TAG, "getDocumentId: 多分ROOT: documentId=" + documentId);}
		}
		return documentId;
	}

}
