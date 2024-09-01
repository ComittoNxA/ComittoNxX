package src.comitton.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import jcifs.smb.SmbFile;
import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.common.FileAccess;
import src.comitton.data.FileData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ImageManager extends InputStream implements Runnable {
	public static final int OPENMODE_VIEW = 0;
	public static final int OPENMODE_LIST = 1;
	public static final int OPENMODE_TEXTVIEW = 2;
	public static final int OPENMODE_THUMBNAIL = 3;
	public static final int OPENMODE_THUMBSORT = 4;

	public static final int OFFSET_LCL_SIGNA_LEN = 0;
	public static final int OFFSET_LCL_BFLAG_LEN = 6;
	public static final int OFFSET_LCL_FTIME_LEN = 10;
	public static final int OFFSET_LCL_FDATE_LEN = 12;
	public static final int OFFSET_LCL_CRC32_LEN = 14;
	public static final int OFFSET_LCL_CDATA_LEN = 18;
	public static final int OFFSET_LCL_OSIZE_LEN = 22;
	public static final int OFFSET_LCL_FNAME_LEN = 26;
	public static final int OFFSET_LCL_EXTRA_LEN = 28;

	public static final int OFFSET_CTL_SIGNA_LEN = 0;
	public static final int OFFSET_CTL_BFLAG_LEN = 8;
	public static final int OFFSET_CTL_FTIME_LEN = 12;
	public static final int OFFSET_CTL_FDATE_LEN = 14;
	public static final int OFFSET_CTL_CDATA_LEN = 20;
	public static final int OFFSET_CTL_OSIZE_LEN = 24;
	public static final int OFFSET_CTL_FNAME_LEN = 28;
	public static final int OFFSET_CTL_EXTRA_LEN = 30;
	public static final int OFFSET_CTL_CMENT_LEN = 32;
	public static final int OFFSET_CTL_LOCAL_LEN = 42;
	public static final int OFFSET_CTL_FNAME = 46;

	public static final int OFFSET_TRM_SIGNA_LEN = 0;
	public static final int OFFSET_TRM_CNTRL_LEN = 16;

	// RAR Format
	public static final int RAR_HTYPE_MARK = 0x72;
	public static final int RAR_HTYPE_MAIN = 0x73;
	public static final int RAR_HTYPE_FILE = 0x74;
	public static final int RAR_HTYPE_SUB = 0x7a;
	public static final int RAR_HTYPE_OLD = 0x7e;

	public static final byte RAR_METHOD_STORING = 0x30;

	public static final int OFFSET_RAR_HCRC = 0;
	public static final int OFFSET_RAR_HTYPE = 2;
	public static final int OFFSET_RAR_HFLAGS = 3;
	public static final int OFFSET_RAR_HSIZE = 5;
	public static final int OFFSET_RAR_ASIZE = 7;

	// Marker Block
	// Archive Header
	public static final int OFFSET_RAR_RESV1 = 7; // 2bytes
	public static final int OFFSET_RAR_RESV2 = 9; // 4bytes

	// FileHeader
	public static final int OFFSET_RAR_PKSIZE = 7; // 4bytes
	public static final int OFFSET_RAR_UNSIZE = 11; // 4bytes
	public static final int OFFSET_RAR_HOSTOS = 15; // 1byte
	public static final int OFFSET_RAR_FCRC = 16; // 4bytes
	public static final int OFFSET_RAR_FTIME = 20; // 2bytes
	public static final int OFFSET_RAR_FDATE = 22; // 2bytes
	public static final int OFFSET_RAR_UNPVER = 24; // 1byte
	public static final int OFFSET_RAR_METHOD = 25; // 1byte
	public static final int OFFSET_RAR_FNSIZE = 26; // 2bytes
	public static final int OFFSET_RAR_ATTRIB = 28; // 4bytes
	public static final int OFFSET_RAR_FNAME = 32; // OFFSET_RAR_FNSIZE
//	public static final int OFFSET_RAR_HPSIZE  = 32;	// 4bytes
//	public static final int OFFSET_RAR_HUSIZE  = 36;	// 4bytes
//	public static final int OFFSET_RAR_SALT    = xx;	// 8bytes
//	public static final int OFFSET_RAR_EXTTIME = xx;	// variable

	public static final int FILETYPE_DIR = 1;
	public static final int FILETYPE_ZIP = 2;
	public static final int FILETYPE_RAR = 3;
	public static final int FILETYPE_IMAGE = 4;
	public static final int FILETYPE_PDF = 5;

	public static final int FILETYPESUB_UNKNOWN = 0;
	public static final int FILETYPESUB_NORMAL = 1;
	public static final int FILETYPESUB_OLDVER = 2;

	public static final int IMAGETYPE_NONE = 0;
	public static final int IMAGETYPE_PDF = 3;
	public static final int IMAGETYPE_JPEG = 4;
	public static final int IMAGETYPE_PNG = 5;
	public static final int IMAGETYPE_GIF = 6;
	public static final int IMAGETYPE_TXT = 7;
	public static final int IMAGETYPE_WEBP = 51;
	public static final int IMAGETYPE_AVIF = 52;
	public static final int IMAGETYPE_HEIF = 53;
	public static final int IMAGETYPE_JXL = 54;

	public static final int HOSTTYPE_LOCAL = 0;
	public static final int HOSTTYPE_SAMBA = 1;
	public static final int HOSTTYPE_WEBDAV = 2;

	public static final int FILESORT_NONE = 0;
	public static final int FILESORT_NAME_UP = 1;
	public static final int FILESORT_NAME_DOWN = 2;

	public static final int SIZE_LOCALHEADER = 30;
	public static final int SIZE_CENTHEADER = 46;
	public static final int SIZE_TERMHEADER = 22;
	public static final int SIZE_EXTRAHEADER1 = 16;
	public static final int SIZE_EXTRAHEADER2 = 12;

	public static final int SIZE_BITFLAG = 12;

	public static final int BIS_BUFFSIZE = 100 * 1024;//Buffered Input Streamのバッファサイズ

	private static final int SIZE_BUFFER = 1024;
//	private static final int SIZE_RARHEADER = 7;
//	private static final int SIZE_RAR_HIGHSIZE = 8;

	private static final int CACHEMODE_NONE = 0;
	private static final int CACHEMODE_FILE = 1;
//	private static final int CACHEMODE_MEM = 2;

	private static final int FROMTYPE_CACHE = 2;
	private static final int FROMTYPE_LOCAL = 3;
	private static final int FROMTYPE_SERVER = 4;

	private static final int BLOCKSIZE = 128 * 1024;

	private static final int DISPMODE_DUAL = 1;
	private static final int DISPMODE_HALF = 2;
	private static final int DISPMODE_EXCHANGE = 3;

	private static final int HOKAN_DOTS = 4;

	private static final int ROTATE_NORMAL = 0;
//	private static final int ROTATE_90DEG = 1;
	private static final int ROTATE_180DEG = 2;
//	private static final int ROTATE_270DEG = 3;

	private final int THUMBNAIL_BUFFSIZE = 5;

	private Activity mActivity;

	private int mPageWay;
	private int mQuality;
	private boolean mPseLand;

	private int mHostType;
	private int mFileType;
//	private int mFileTypeSub;
	private int mFileSort;
	private int mOpenMode;

	private String mCharset;
	private boolean mHidden;

	private int mCacheMode;
	private int mCurrentPage;
	private int mLoadingPage;
	private boolean mCurrentSingle;
	private Handler mHandler;
	private int mFromType;

	private long mStartTime;
	private int mMsgCount;
	private int mReadSize;
	private int mDataSize;
	private boolean mCheWriteFlag;
	private boolean mThreadLoading = true;

	private Thread mThread;
	private boolean mRunningFlag = false;
	private boolean mTerminate = false;
	private boolean mCloseFlag = false;
	private boolean mCacheBreak;
	private boolean mCacheSleep;
	private Object mLock;

	private String mFilePath;
	private String mUser;
	private String mPass;
	public FileListItem mFileList[] = null;
	private int mMaxCmpLength;
	private int mMaxOrgLength;

	private PdfRenderer mPdfRenderer = null;
	private RarInputStream mRarStream = null;

	private int mMaxThreadNum;

	private String mRarCharset;

	@SuppressLint("SuspiciousIndentation")
    public ImageManager(Activity activity, String path, String cmpfile, String user, String pass, int sort, Handler handler, String charset, boolean hidden, int openmode, int maxthread) {
		mActivity = activity;
		mFileList = null;
		mFilePath = path + cmpfile;
		mUser = user;
		mPass = pass;
		mTerminate = false;
		mCacheBreak = false;
		mCacheSleep = false;
		mLock = this;
		mRunningFlag = true;
		mCloseFlag = false;
		mHandler = handler;
		mFileSort = sort;
		mCharset = charset;
		mHidden = hidden;
		mOpenMode = openmode;

 		// スレッド数
 		mMaxThreadNum = maxthread;

//		mMemSize = memsize;
//		mMemNextPages = memnext;
//		mMemPrevPages = memprev;

		mRarCharset = new String( "UTF-8" );
	}

	public void LoadImageList(int memsize, int memnext, int memprev) {
		Log.d("ImageManager", "LoadImageList: 開始します. memsize=" + memsize + ", memnext=" + memnext + ", memprev=" + memprev);
		try {
			if (mFilePath.length() >= 1 && mFilePath.substring(0, 1).equals("/")) {
				// ローカルパス
				mHostType = HOSTTYPE_LOCAL;
			}
			else if (mFilePath.length() >= 6 && mFilePath.substring(0, 6).equals("smb://")) {
				// サーバパス
				mHostType = HOSTTYPE_SAMBA;
			}
			else if (mFilePath.startsWith("http")) {
				// WebDAVサーバパス
				mHostType = HOSTTYPE_WEBDAV;
			}
			else {
				throw new IOException("Illegal Path.");
			}

			String ext = DEF.getFileExt(mFilePath);
			if (mFilePath.substring(mFilePath.length() - 1).equals("/")) {
				// ファイルリスト画面で画像ファイルを開いた時はこちら
				mFileType = FILETYPE_DIR;
			}
			else if (FileData.isImage(ext)) {
				// 画像ファイルのサムネイル画像を取得する時はこちら
				mFileType = FILETYPE_IMAGE;
			}
			else if (FileData.isPdf(ext)) {
				mFileType = FILETYPE_PDF;
			}
			else if (FileData.isZip(ext)) {
				mFileType = FILETYPE_ZIP;
			}
			else if (FileData.isRar(ext)) {
				mFileType = FILETYPE_RAR;
			}
			else {
				throw new IOException("Illegal Path.");
			}

			try {
				if (mFileType == FILETYPE_DIR) {
					DirFileList(mFilePath, mUser, mPass);
				}
				else if (mFileType == FILETYPE_IMAGE) {
					fileAccessInit(mFilePath);
					ImageFileList(mFilePath, mUser, mPass);
				}
				else if (mFileType == FILETYPE_PDF) {
					PdfFileList(mFilePath, mUser, mPass);
				}
				else {
					fileAccessInit(mFilePath);
					cmpFileList();
				}

				// ビューモードの時だけ初期化する
				//if (mOpenMode == OPENMODE_VIEW) {

					// ビューモードでは無いときも初期化する
					// メモリキャッシュの初期化(JNI)
					if (MemoryCacheInit(memsize, memnext, memprev, mFileList.length, mMaxOrgLength) == false) {
						Log.e("ImageManager", "LoadImageList: ImgLibrary Memory Alloc Error.");
						throw new IOException("ImgLibrary Memory Alloc Error.");
					}
					fileCacheInit(mFileList.length, (mHostType == HOSTTYPE_SAMBA));

				//}
			}
			catch (IOException e) {
				Log.e("ImageManager", "LoadImageList: エラーが発生しました.");
				if (e != null && e.getMessage() != null) {
					Log.e("ImageManager", "LoadImageList: エラーメッセージ. " + e.getMessage());
				}
				throw e;
			}
			startCacheRead();
		}
		catch (IOException ex) {
			mFileList = new FileListItem[0];
			Log.d("ImageManager", ex.getMessage());
			Message message = new Message();
			message.what = DEF.HMSG_ERROR;
			message.obj = ex.getMessage();
			mHandler.sendMessage(message);
		}
		Log.d("ImageManager", "LoadImageList: 終了します. ");
	}

	private void cmpFileList() throws IOException {
		Log.d("ImageManager", "cmpFileList: 開始します.");
		// ZIPファイル読み込み
		byte[] buf = new byte[SIZE_BUFFER];
		int readSize=0;
		long cmppos = 0;
		long orgpos = 0;
		long headpos = 0;
		int count = 0;
		int maxcmplen = 0;
		int maxorglen = 0;
		List<FileListItem> list = new ArrayList<FileListItem>();
		byte [] cdhBuf = null;
		long fileLength = cmpDirectLength();
		long cdhLength = 0;//central directory header length

		boolean rar5 = false;

		// 簡易的なRAR5判定
		if( mFileType == FILETYPE_RAR ){
			byte[] sigbuff = new byte[8];
			cmpDirectRead( sigbuff, 0, 8 );
			if( sigbuff[0] == 0x52 && sigbuff[1] == 0x61 && sigbuff[2] == 0x72 && sigbuff[3] == 0x21 &&
				sigbuff[4] == 0x1a && sigbuff[5] == 0x07 && sigbuff[6] == 0x01 && sigbuff[7] == 0x00 ){
				rar5 = true;
//				Log.d( "ComittoNxT", "RAR5 Hit." );
				// シグネチャ以降にRAR5データがある
			}
		}

//		boolean first = true;
		if (mFileType == FILETYPE_ZIP) {
			// 圧縮されたファイル情報取得
			headpos = zipSearchCentral();
		}
		if (headpos == 0) {
			cmpDirectSeek(0);
		}else{//central directory headerが見つかった場合、一括でバッファに読み込む
			cdhLength = fileLength - headpos;
			cdhBuf = new byte[(int)cdhLength];
			cmpDirectRead(cdhBuf, 0, (int)cdhLength);
			cmpDirectSeek(headpos);
		}

		FileListItem fl = null;
		while (true) {
			if(headpos != 0){
				if(headpos < fileLength) {
					readSize = (int) ((fileLength - headpos >= 1024) ? 1024 : fileLength - headpos);
					int pos = (int) (cdhLength - (fileLength - headpos));
					buf = Arrays.copyOfRange(cdhBuf, pos, readSize+pos);
				}else
					readSize = -1;
			}else {
				readSize = cmpDirectRead(buf, 0, SIZE_BUFFER);
			}
			if (readSize <= 0) {
				// ファイル終了
				break;
			}

			if (mFileType == FILETYPE_ZIP) {
//				if (mFileTypeSub != FILETYPESUB_OLDVER) {
//					// 通常バージョンで読み込み
				if (headpos == 0) {
					fl = zipFileListItem(buf, cmppos, orgpos, readSize, true);
					fl.sizefixed = true;
				}
				else {
//				}
//				// 最初はバージョンチェック
//				if (fl != null && fl.version == 0) {
//					mFileTypeSub = FILETYPESUB_NORMAL;
//				}
//				else {
//					mFileTypeSub = FILETYPESUB_OLDVER;
//					if (first) {
//						// 圧縮されたファイル情報取得
//						headpos = zipSearchCentral();
//						readSize = cmpDirectRead(buf, 0, SIZE_BUFFER);
//						first = false;
//					}
//				}

//				if (mFileTypeSub == FILETYPESUB_OLDVER) {
					fl = zipFileListOldItemLite(buf, orgpos, readSize);
//				}
					if(fl != null)
						fl.sizefixed = false;
				}
			}
			else if (mFileType == FILETYPE_RAR) {
				// RAR読み込み
				if( rar5 ){
					// RAR5
					fl = rar5FileListItem(buf, cmppos, orgpos, readSize);
				}else{
					// RAR4.x以前
					fl = rarFileListItem(buf, cmppos, orgpos, readSize);
				}
			}
			else {
				// 読み込み不可
				return;
			}
			if (fl == null) {
				break;

			}
			// 対象ファイル判定
			if (fl.name != null && fl.name.length() > 4 && fl.orglen > 0 && fl.cmplen > 0) {
				if (mHidden == false || !DEF.checkHiddenFile(fl.name)) {
					String ext = DEF.getFileExt(fl.name);
					fl.type = 0;
					if (FileData.isImage(ext)) {
						if (DEF.WITH_JPEG && (ext.equals(".jpg") || ext.equals(".jpeg"))) {
							fl.type = IMAGETYPE_JPEG;
						}
						else if (DEF.WITH_PNG && ext.equals(".png")) {
							fl.type = IMAGETYPE_PNG;
						}
						else if (DEF.WITH_GIF && ext.equals(".gif")) {
							fl.type = IMAGETYPE_GIF;
						}
						else if (DEF.WITH_WEBP && ext.equals(".webp")) {
							fl.type = IMAGETYPE_WEBP;
						}
						else if (DEF.WITH_AVIF && ext.equals(".avif")) {
							fl.type = IMAGETYPE_AVIF;
						}
						else if (DEF.WITH_HEIF && (ext.equals(".heif") || ext.equals(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							fl.type = IMAGETYPE_HEIF;
						}
						else if (DEF.WITH_JXL && ext.equals(".jxl")) {
							fl.type = IMAGETYPE_JXL;
						}
					}
					else if (FileData.isText(ext) && (mOpenMode == OPENMODE_LIST || mOpenMode == OPENMODE_TEXTVIEW)) {
						fl.type = 3;
					}

					if (fl.type != 0) {
						// リストへ登録
						list.add(fl);
						if (mFileType == FILETYPE_RAR) {
							if (maxcmplen < fl.cmplen - fl.header) {
								// 最大サイズを求める
								maxcmplen = fl.cmplen - fl.header;
							}
						}
						if (maxorglen < fl.orglen) {
							// 最大サイズを求める
							maxorglen = fl.orglen;
						}
						if (!(mFileType == FILETYPE_ZIP && headpos > 0) && mOpenMode == OPENMODE_THUMBNAIL && list.size() >= 5) {
							// ソートなしのサムネイル取得時は先頭5ファイルから選択
							// ZIPの場合は設定にかかわらずソート有り
							break;
						}
					}
				}
			}

			// 次のファイルへ
			cmppos += fl.cmplen;
			orgpos += fl.orglen;
			if (mFileType == FILETYPE_ZIP && headpos > 0) {
				// 旧タイプのZIPの場合はセントラルヘッダをアクセス
				headpos += fl.header;
				cmpDirectSeek(headpos);
			}
			else {
				cmpDirectSeek(cmppos);
			}

			count++;
			if (sendProgress(0, count) == false) {
				mFileList = new FileListItem[0];
				return;
			}
		}
		sort(list);
		mFileList = (FileListItem[]) list.toArray(new FileListItem[0]);
		// RARであればメモリ確保
		if (mFileType == FILETYPE_RAR) {
			int ret = CallJniLibrary.rarAlloc(maxcmplen, maxorglen);
			if (ret != 0) {
				throw new IOException("Memory Alloc Error.");
			}
		}
		mMaxCmpLength = maxcmplen;
		mMaxOrgLength = maxorglen;
		Log.d("ImageManager", "cmpFileList: 終了します.");
	}

	public boolean sendProgress(int type, int count) {
		// 10ファイル単位で通知
		if (count % 10 == 0) {
			if (mRunningFlag == false) {
				return false;
			}
			Message message = new Message();
			message.what = DEF.HMSG_PROGRESS;
			message.arg1 = count;
			message.arg2 = type;
			mHandler.sendMessage(message);
		}
		return true;
	}

	private long zipSearchCentral() throws IOException {
		long fileLength = cmpDirectLength();
		int pos = -1;
		int retsize;
		int buffSize = 1024;
		byte buff[] = new byte[buffSize];

		if (fileLength < SIZE_TERMHEADER) {
			throw new IOException("Broken Zip File.");
		}

		long filePos = fileLength - buffSize;
		for (int i = 0 ; i < 33 ; i ++) {
			if (filePos < 0) {
				// サイズが小さい場合はファイル先頭から
				buffSize = (int) (buff.length + filePos);
				filePos = 0;
			}

			// 終端コードへ移動
			cmpDirectSeek(filePos);
			retsize = cmpDirectRead(buff, 0, buffSize);
			if (retsize < buffSize) {
				throw new IOException("File Access Error.");
			}

			int sig;
			for (pos = buffSize - SIZE_TERMHEADER; pos >= 0; pos--) {
				if (buff[pos] == 0x50) {
					sig = getInt(buff, pos + OFFSET_TRM_SIGNA_LEN);
					if (sig == 0x06054b50) {
						break;
					}
				}
			}
			if (pos >= 0) {
				break;
			}
			if (filePos == 0) {
				// 先頭まできた
				break;
			}
			filePos -= (buffSize - (SIZE_TERMHEADER - 1));
		}
		if (pos < 0) {
			// ヘッダがおかしい
			throw new IOException("Central header is not found.");
		}
		int posCentral = getInt(buff, pos + OFFSET_TRM_CNTRL_LEN);
		if (posCentral >= fileLength || posCentral < 0) {
			// ヘッダがおかしい、けどとりあえず先頭から読み込ませてみる
			//throw new IOException("Broken Zip File.");
			posCentral = 0;
		}

		// セントラルヘッダに移動
		cmpDirectSeek(posCentral);
		return posCentral;
	}

	private int getExtraSize(byte [] buf){
		int sig = getInt(buf, OFFSET_LCL_SIGNA_LEN);
		if (sig != 0x04034b50) {
			// LocalFileHeaderじゃない
			return 0;
		}
		return getShort(buf, OFFSET_LCL_EXTRA_LEN);
	}

	private int getCompressedSize(byte [] buf){
		int sig = getInt(buf, OFFSET_LCL_SIGNA_LEN);
		if (sig != 0x04034b50) {
			// LocalFileHeaderじゃない
			return 0;
		}
		int bflag = getShort(buf, OFFSET_LCL_BFLAG_LEN);
		int lenCmp = getInt(buf, OFFSET_LCL_CDATA_LEN);
		int lenFName = getShort(buf, OFFSET_LCL_FNAME_LEN);
		int lenExtra = getShort(buf, OFFSET_LCL_EXTRA_LEN);
		int cmplen = SIZE_LOCALHEADER + lenFName + lenExtra + ((bflag & 0x0004) != 0 ? SIZE_BITFLAG : 0) + lenCmp;
		return cmplen;
	}

	public FileListItem zipFileListItem(byte buf[], long cmppos, long orgpos, int readsize, boolean isCmpSum) {
		int sig = getInt(buf, OFFSET_LCL_SIGNA_LEN);
		int bflag = getShort(buf, OFFSET_LCL_BFLAG_LEN);
		int ftime = getShort(buf, OFFSET_LCL_FTIME_LEN);
		int fdate = getShort(buf, OFFSET_LCL_FDATE_LEN);
		int crc32 = getInt(buf, OFFSET_LCL_CRC32_LEN);
		int lenCmp = getInt(buf, OFFSET_LCL_CDATA_LEN);
		int lenOrg = getInt(buf, OFFSET_LCL_OSIZE_LEN);
		int lenFName = getShort(buf, OFFSET_LCL_FNAME_LEN);
		int lenExtra = getShort(buf, OFFSET_LCL_EXTRA_LEN);

		if (readsize < SIZE_LOCALHEADER) {
			// データ不正
			return null;
		}
		if (sig != 0x04034b50) {
			// データの終わり
			return null;
		}

		String name = "";
		if (readsize >= lenFName + SIZE_LOCALHEADER) {
			// ファイル名までのデータがあり
			try {
//				name = new String(buf, SIZE_LOCALHEADER, lenFName, mCharset);
				// 日本語環境に限った話でShift-JISではなくUTF-8だった場合に変換しておく
				if( checkUFT8( buf, SIZE_LOCALHEADER, lenFName ) ){
					// RAR5がUTF-8なので定義を流用
					name = new String(buf, SIZE_LOCALHEADER, lenFName, mRarCharset);
//					Log.d("comittonx/UTF-8",name);
				}else {
					name = new String(buf, SIZE_LOCALHEADER, lenFName, mCharset);
//					Log.d("comittonx/Shift-JIS",name);
				}
			}
			catch (Exception e) {
				name = "Unknown";
			}
		}

		int yy = ((fdate >> 9) & 0x7F) + 80;
		int mm = ((fdate >> 5) & 0x0F) - 1;
		int dd = fdate & 0x1F;
		int hh = (ftime >> 11) & 0x1F;
		int nn = (ftime >> 5) & 0x3F;
		int ss = (ftime & 0x1F) * 2;
		Date d = new Date(yy, mm, dd, hh, nn, ss);

		// Image Fileのみ採用
		FileListItem imgfile = new FileListItem();
		imgfile.name = name;
		imgfile.cmppos = cmppos;
		imgfile.orgpos = orgpos;
		imgfile.cmplen = SIZE_LOCALHEADER + lenFName + lenExtra + ((bflag & 0x0004) != 0 ? SIZE_BITFLAG : 0) + (isCmpSum ? lenCmp : 0);
		imgfile.orglen = lenOrg;
		imgfile.version = (byte) (((bflag & 0x0008) != 0 && lenCmp == 0 && crc32 == 0) ? 1 : 0); // バージョン 0:通常、 1:古い
		imgfile.dtime = d.getTime();
//		imgfile.bmpsize = 0;
		return imgfile;
	}

	public FileListItem zipFileListOldItem(byte buf[], long orgpos, int readsize) throws IOException {
		int sig = getInt(buf, OFFSET_CTL_SIGNA_LEN);

		if (readsize < SIZE_CENTHEADER) {
			// データ不正
			return null;
		}
		if (sig != 0x02014b50) {
			// セントラルディレクトリヘッダでなければデータの終わり
			return null;
		}

		int lenCmp = getInt(buf, OFFSET_CTL_CDATA_LEN);
		int lenOrg = getInt(buf, OFFSET_CTL_OSIZE_LEN);
		int lenFName = getShort(buf, OFFSET_CTL_FNAME_LEN);
		int lenExtra = getShort(buf, OFFSET_CTL_EXTRA_LEN);
		int lenComent = getShort(buf, OFFSET_CTL_CMENT_LEN);
		int lclOffset = getInt(buf, OFFSET_CTL_LOCAL_LEN);

		cmpDirectSeek(lclOffset);
		readsize = cmpDirectRead(buf, 0, SIZE_BUFFER);
		FileListItem imgfile = zipFileListItem(buf, lclOffset, orgpos, readsize, false);
		if (imgfile != null) {
			imgfile.cmplen += lenCmp;
			imgfile.orglen = lenOrg;
			imgfile.version = 1; // バージョン 0:通常、 1:古い
			imgfile.header = SIZE_CENTHEADER + lenFName + lenExtra + lenComent;
		}
		return imgfile;
	}

	public FileListItem zipFileListOldItemLite(byte buf[], long orgpos, int readsize) throws IOException {
		int sig = getInt(buf, OFFSET_CTL_SIGNA_LEN);
		if (readsize < SIZE_CENTHEADER) {
			// データ不正
			return null;
		}
		if (sig != 0x02014b50) {
			// セントラルディレクトリヘッダでなければデータの終わり
			return null;
		}

		int bflag = getShort(buf, OFFSET_CTL_BFLAG_LEN);
		int lenCmp = getInt(buf, OFFSET_CTL_CDATA_LEN);
		int lenOrg = getInt(buf, OFFSET_CTL_OSIZE_LEN);
		int lenFName = getShort(buf, OFFSET_CTL_FNAME_LEN);
		int lenExtra = getShort(buf, OFFSET_CTL_EXTRA_LEN);
		int lenComent = getShort(buf, OFFSET_CTL_CMENT_LEN);
		int lclOffset = getInt(buf, OFFSET_CTL_LOCAL_LEN);
		int ftime = getShort(buf, OFFSET_CTL_FTIME_LEN);
		int fdate = getShort(buf, OFFSET_CTL_FDATE_LEN);

		int yy = ((fdate >> 9) & 0x7F) + 80;
		int mm = ((fdate >> 5) & 0x0F) - 1;
		int dd = fdate & 0x1F;
		int hh = (ftime >> 11) & 0x1F;
		int nn = (ftime >> 5) & 0x3F;
		int ss = (ftime & 0x1F) * 2;
		Date d = new Date(yy, mm, dd, hh, nn, ss);

		FileListItem imgfile = new FileListItem();
		//ローカルファイルヘッダの拡張フィールドのサイズはセントラルディレクトリヘッダからは取得出来ないので、実際の読み込み時に辻褄を合わせる
		imgfile.cmplen = SIZE_LOCALHEADER + lenFName + 0/*lenExtra*/ + ((bflag & 0x0004) != 0 ? SIZE_BITFLAG : 0) + lenCmp;
		imgfile.orglen = lenOrg;
		imgfile.cmppos = lclOffset;
		imgfile.orgpos = orgpos;
		imgfile.version = 1; // バージョン 0:通常、 1:古い
		imgfile.header = SIZE_CENTHEADER + lenFName + lenExtra + lenComent;
		imgfile.dtime = d.getTime();
		// ファイル名までのデータがあり
		try {
//			imgfile.name = new String(buf, OFFSET_CTL_FNAME, lenFName, mCharset);
			// 日本語環境に限った話でShift-JISではなくUTF-8だった場合に変換しておく
			if( checkUFT8( buf, OFFSET_CTL_FNAME, lenFName ) ){
				// RAR5がUTF-8なので定義を流用
				imgfile.name = new String(buf, OFFSET_CTL_FNAME, lenFName, mRarCharset);
//				Log.d("comittonx/UTF-8",imgfile.name);
			}else {
				imgfile.name = new String(buf, OFFSET_CTL_FNAME, lenFName, mCharset);
//				Log.d("comittonx/Shift-JIS",imgfile.name);
			}
		}
		catch (Exception e) {
			imgfile.name = "Unknown";
		}
		return imgfile;
	}

	public FileListItem rar5FileListItem(byte buf[], long cmppos, long orgpos, int readsize) throws IOException {
		// ヘッダを読み込み、ファイルヘッダだけをFileListItemとして返す
		// シグネチャ(マーカーブロック)やアーカイブヘッダーなどは読み飛ばす
		// VINTは2byteまでと決め打ちして簡略化 > vint取得関数を実装
		int pos = 0;
		VintData vint;
		int headpos = 0;

		// シグネチャ判定
		if( cmppos == 0 ){
			if( buf[0] == 0x52 && buf[1] == 0x61 && buf[2] == 0x72 && buf[3] == 0x21 &&
				buf[4] == 0x1a && buf[5] == 0x07 && buf[6] == 0x01 && buf[7] == 0x00 ){

//				Log.d( "ComittoNxT", "RAR5 Signature." );

				// ファイル情報ではない
				FileListItem imgfile = new FileListItem();
				imgfile.name = null;
				imgfile.cmppos = 0;
				imgfile.orgpos = 0;
				imgfile.cmplen = 8;
				imgfile.orglen = 0;
//				imgfile.bmpsize = 0;
				imgfile.width = 0;
				imgfile.height = 0;
				return imgfile;
			}
		}

		// Header CRC32
		long hcrc = getInt( buf, 0 );
		pos += 4;

		// Header size
		vint = readVint( buf, pos );
		int hsize = vint.vint;
		pos += vint.count;
		// Header size以前のサイズ
		headpos = pos;

		// Header type
		vint = readVint( buf, pos );
		int htype = vint.vint;
		pos += vint.count;

		// Header flags
		vint = readVint( buf, pos );
		int hflags = vint.vint;
		pos += vint.count;

		// Extra area size(flags is set 0x0001)
		int hextra = 0;
		if( (hflags & 0x0001) != 0 ){
			vint = readVint( buf, pos );
			hextra = vint.vint;
			pos += vint.count;
		}

		// Data size(flags is set 0x0002)
		int hdata = 0;
		if( (hflags & 0x0002) != 0 ){
			vint = readVint( buf, pos );
			hdata = vint.vint;
			pos += vint.count;
		}

		// ファイルヘッダー以外はスキップ
		if( htype != 2 ){
//			Log.d( "ComittoNxT", "RAR5 not FileHeader." );

			// ファイル情報ではない
			FileListItem imgfile = new FileListItem();
			imgfile.name = null;
			imgfile.cmppos = 0;
			imgfile.orgpos = 0;
			imgfile.cmplen = hdata + hsize + headpos;
			imgfile.orglen = 0;
//			imgfile.bmpsize = 0;
			imgfile.width = 0;
			imgfile.height = 0;
			return imgfile;
		}


		// File flags
		vint = readVint( buf, pos );
		int fflag = vint.vint;
		pos += vint.count;

		// Unpacked size
		vint = readVint( buf, pos );
		int lenOrg = vint.vint;
		pos += vint.count;

		// Attributes
		vint = readVint( buf, pos );
		pos += vint.count;

		// mtime
		int ftime = 0;
		if( (fflag & 0x0002) != 0 ){
			ftime = getInt( buf, pos );
			pos += 4;
		}
		Date d = new Date( ftime );

		// Data CRC32
		int dcrc = getInt( buf, pos );
		pos += 4;

		// Compression information
		vint = readVint( buf, pos );
		int cinfo = vint.vint;
		pos += vint.count;

		// Host OS
		vint = readVint( buf, pos );
		pos += vint.count;

		// Name length
		vint = readVint( buf, pos );
		int fnlen = vint.vint;
		pos += vint.count;

		// Name
		String name = "";
		if( readsize >= pos + fnlen ){
			// ファイル名までのデータがあり
			try {
				for( int i = 0; i < fnlen; ++i ){
					if( buf[ pos + i ] == 0 ){
						fnlen = i;
						break;
					}
				}
				name = new String( buf, pos, fnlen, mRarCharset );
			}
			catch( Exception e ){
				name = "Unknown";
			}
		}


		// Image Fileのみ採用
		FileListItem imgfile = new FileListItem();
		imgfile.name = name;
		imgfile.cmppos = cmppos;
		imgfile.orgpos = orgpos;
		imgfile.cmplen = hdata + hsize + headpos;//lenCmp + hsize;
		imgfile.orglen = lenOrg;
		imgfile.header = hsize + headpos;
		imgfile.version = 50;
		imgfile.nocomp = (cinfo & 0x0380) == 0;	// 無圧縮か
//		imgfile.bmpsize = 0;
		imgfile.dtime = d.getTime();
		return imgfile;
	}

	private class VintData {
		public int vint;
		public int count;

		public VintData() {
			init();
		}

		public void init() {
			vint = 0;
			count = 0;
		}
	};

	private VintData readVint( byte buf[], int pos ) {
		int dat;
		VintData data = new VintData();

		while( true ){
			dat = buf[ pos + data.count ];
			dat &= 0x0FF;
			data.vint += ((dat & ~0x80) << (data.count * 7));
			data.count++;
			if( (dat & 0x80) == 0 ){
				break;
			}
		}

		return data;
	}

	private boolean checkUFT8(byte buf[], long start, long len){
		// ZIP及びRAR4.xのファイル名がShift-JISではなくUTF-8で保存されている場合の判定関数
		// ある程度の推測で判断しています
		// 日本語以外では問題があるのかもしれません
		boolean isUTF8 = false;
		int namePos = (int)start;
		byte check_code;
		byte check_code2;
		while( true ){
			// ファイル名の最後まで判別出来なかった
			if( namePos >= len ) {
				break;
			}
			check_code = buf[ namePos ];
			check_code2 = buf[ namePos+1 ];
			// 先頭のバイトコードで簡易判定を行う
			if( check_code >= (byte)0x00 && check_code <= (byte)0x7e ){
				// Asciiなので次のバイトに判断を委ねる
				namePos++;
			}else {
				if (check_code2 >= (byte)0x80 && check_code2 <= (byte)0xbf) {
					if (check_code >= (byte)0xf0 && check_code <= (byte)0xfd) {
						// UTF-8確定
						isUTF8 = true;
					} else if (check_code >= (byte)0x80 && check_code <= (byte)0x9f) {
						// Shift-JIS確定
						isUTF8 = false;
					} else if (check_code >= (byte)0xa0 && check_code <= (byte)0xc1) {
						// Shift-JIS確定
						isUTF8 = false;
					} else if (check_code >= (byte)0xe0 && check_code <= (byte)0xef) {
						// UTF-8推定
						isUTF8 = true;
					} else if (check_code >= (byte)0xc2 && check_code <= (byte)0xdf) {
						// UTF-8推定
						isUTF8 = true;
					}
				}
				break;
			}
		}
		return isUTF8;
	}

	public FileListItem rarFileListItem(byte buf[], long cmppos, long orgpos, int readsize) throws IOException {
		int hcrc = getShort(buf, OFFSET_RAR_HCRC);
		int htype = buf[OFFSET_RAR_HTYPE];
		int hflags = getShort(buf, OFFSET_RAR_HFLAGS);
		int hsize = getShort(buf, OFFSET_RAR_HSIZE);
		int asize = (hflags & 0x8000) == 0 ? 0 : getInt(buf, OFFSET_RAR_ASIZE);

		boolean skip = false;
//		boolean oldFormat = false;

		if (htype != RAR_HTYPE_FILE) {
			if (htype == RAR_HTYPE_MARK) {
				if (hcrc != 0x6152 || hflags != 0x1a21 || hsize != 0x0007) {
					// ヘッダがおかしい
					throw new IOException("This RAR File Format is not supported.");
				}
			}
			else if (htype == RAR_HTYPE_OLD) {
				if (hcrc == 0x4552 && (hflags & 0x00FF) == 0x005e) {
//					oldFormat = true;
				}
			}
			else {
				// RAR_HTYPE_MAIN
				// 特にチェックなし

			}
			skip = true;
		}
		else {
			if ((hflags & 0x0100) != 0) {
				// 巨大ファイルの時は見ない
				skip = true;
			}
		}

		if (skip) {
			if (hsize + asize <= 5) {
				throw new IOException("File is broken.");
			}
			// ファイル情報ではない
			FileListItem imgfile = new FileListItem();
			imgfile.name = null;
			imgfile.cmppos = 0;
			imgfile.orgpos = 0;
			imgfile.cmplen = hsize + asize;
			imgfile.orglen = 0;
//			imgfile.bmpsize = 0;
			imgfile.width = 0;
			imgfile.height = 0;
			return imgfile;
		}

		// ファイル情報取得
		int lenFName = getShort(buf, OFFSET_RAR_FNSIZE);
		int posFName = OFFSET_RAR_FNAME;
		String name = "";

		if (readsize >= lenFName + posFName) {
			// ファイル名までのデータがあり
			try {
				for (int i = 0; i < lenFName; i++) {
					if (buf[posFName + i] == 0) {
						lenFName = i;
						break;
					}
				}
//				name = new String(buf, posFName, lenFName, mCharset);
				// 日本語環境に限った話でShift-JISではなくUTF-8だった場合に変換しておく
				if( checkUFT8( buf, posFName, lenFName ) ){
					// RAR5がUTF-8なので定義を流用
					name = new String(buf, posFName, lenFName, mRarCharset);
				}else{
					name = new String(buf, posFName, lenFName, mCharset);
				}
			}
			catch (Exception e) {
				name = "Unknown";
			}
		}

		int lenCmp = getInt(buf, OFFSET_RAR_PKSIZE);
		int lenOrg = getInt(buf, OFFSET_RAR_UNSIZE);
		byte rarVer = buf[OFFSET_RAR_UNPVER];
		byte method = buf[OFFSET_RAR_METHOD];

		int ftime = getShort(buf, OFFSET_RAR_FTIME);
		int fdate = getShort(buf, OFFSET_RAR_FDATE);

		int yy = ((fdate >> 9) & 0x7F) + 80;
		int mm = ((fdate >> 5) & 0x0F) - 1;
		int dd = fdate & 0x1F;
		int hh = (ftime >> 11) & 0x1F;
		int nn = (ftime >> 5) & 0x3F;
		int ss = (ftime & 0x1F) * 2;
		Date d = new Date(yy, mm, dd, hh, nn, ss);

		// Image Fileのみ採用
		FileListItem imgfile = new FileListItem();
		imgfile.name = name;
		imgfile.cmppos = cmppos;
		imgfile.orgpos = orgpos;
		imgfile.cmplen = lenCmp + hsize;
		imgfile.orglen = lenOrg;
		imgfile.header = hsize;
		imgfile.version = rarVer;
		imgfile.nocomp = method == RAR_METHOD_STORING;
//		imgfile.bmpsize = 0;
		imgfile.dtime = d.getTime();
		return imgfile;
	}

	private void DirFileList(String path, String user, String pass) throws IOException {
		Log.d("ImageManager", "DirFileList: 開始します. path=" + path + ", user=" + user + ", pass=" + pass);
		int maxorglen = 0;

		// ファイルリストを作成
		List<FileListItem> list = new ArrayList<FileListItem>();

		dirListFiles(path, user, pass);

		int count = 0;
		while (true) {
			FileListItem fl = dirGetFileListItem();
			if (fl == null) {
				break;
			}
			list.add(fl);

			if (maxorglen < fl.orglen) {
				maxorglen = fl.orglen;
			}
			// 読込通知
			count++;
			if (count % 10 == 0) {
				if (mRunningFlag == false) {
					mFileList = new FileListItem[0];
					return;
				}
				Message message = new Message();
				message.what = DEF.HMSG_PROGRESS;
				message.arg1 = count;
				message.arg2 = 0;
				mHandler.sendMessage(message);
			}
		}

		sort(list);
		mFileList = (FileListItem[]) list.toArray(new FileListItem[0]);
		mMaxOrgLength = maxorglen;
		Log.d("ImageManager", "DirFileList: 終了します. ");
	}

	private void ImageFileList(String path, String user, String pass) throws IOException {
		Log.d("ImageManager", "ImageFileList: 開始します. path=" + path + ", user=" + user + ", pass=" + pass);
		mFileList = new FileListItem[1];
		FileListItem filelist = new FileListItem();
		mFileList[0] = filelist;

		filelist.type = FileData.getExtType(mFilePath);
		filelist.name = FileData.getName(mFilePath);

		if (mHostType == HOSTTYPE_SAMBA) {
			Log.d("ImageManager", "ImageFileList: HOSTTYPE_SAMBA です.");
			SmbFile smbFile = FileAccess.jcifsFile(mFilePath, mUser, mPass);
			mFileList[0].orglen = (int)smbFile.length();
		}
		else if (mHostType == HOSTTYPE_LOCAL) {
			Log.d("ImageManager", "ImageFileList: HOSTTYPE_LOCAL です.");
			File localFile = new File(mFilePath);
			mFileList[0].orglen = (int)localFile.length();
		}
		mMaxOrgLength = mFileList[0].orglen;
	}

	private void PdfFileList(String path, String user, String pass) throws IOException {
		int maxPage = 0;
		mMaxOrgLength = 0;

		Log.d("ImageManager", "PdfFileList: 開始します. path=" + path + ", user=" + user + ", pass=" + pass);

		if (mPdfRenderer == null) {
			Log.d("ImageManager", "PdfFileList: PdfRendererを取得します.");
			ParcelFileDescriptor parcelFileDescriptor = null;
			FileDescriptor fd = null;
			File pdfFile = null;
			if (mHostType == HOSTTYPE_SAMBA) {
				Log.d("ImageManager", "PdfFileList: HOSTTYPE_SAMBA です.");

				Resources res = mActivity.getResources();
				WorkStream ws;
				BufferedInputStream bis;
				BufferedOutputStream bos;

				// ローカルディスクに一時ファイルを作成する
				String tmpPath = DEF.getBaseDirectory() + "share/pdftmp";
				File targetFile = new File(tmpPath);

				try {
					ws = new WorkStream(mFilePath, "", mUser, mPass, false);
					bis = new BufferedInputStream(ws, 500 * 1024);
					bos = new BufferedOutputStream(new FileOutputStream(targetFile), 500 * 1024);
				} catch (FileNotFoundException e) {
					Log.e("ImageManager", "PdfFileList: Stream取得中にエラーになりました. ");
					if (e != null && e.getMessage() != null) {
						Log.e("ImageManager", "PdfFileList: エラーメッセージ. " + e.getMessage());
					}
					return;
				}

				// SMBのファイルを一時ファイルにコピーする
				byte buff[] = new byte[BIS_BUFFSIZE * 10];
				long readtotal = 0;
				long filesize = ws.length();
				try {
					while (mRunningFlag == true) {
						if (mCloseFlag) {
							break;
						}
						int readsize = bis.read(buff, 0, buff.length);
						if (readsize <= 0) {
							break;
						}
						bos.write(buff, 0, readsize);
						readtotal += readsize;

						// 読込通知
						Message message = new Message();
						message.what = DEF.HMSG_PROGRESS;
						message.arg1 = 0;
						message.arg2 = 3; // ダウンロード中
						message.obj = String.format("%s\n%,d of %,d (%d%%)", res.getString(R.string.download), (readtotal/1024), (filesize/1024), (readtotal*100/filesize));
						mHandler.sendMessage(message);
					}
					bos.flush();
					bos.close();
				} catch (IOException e) {
					Log.e("ImageManager", "PdfFileList: ダウンロード中にエラーになりました. ");
					if (e != null && e.getMessage() != null) {
						Log.e("ImageManager", "PdfFileList: エラーメッセージ. " + e.getMessage());
					}
				}
				pdfFile = targetFile;
			}
			else if (mHostType == HOSTTYPE_LOCAL) {
				Log.d("ImageManager", "PdfFileList: HOSTTYPE_LOCAL です.");
				pdfFile = new File(mFilePath);
			}

			//表示するPDFファイルを指定し、ParcelFileDescriptorインスタンスを作成する。
			parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
			//ParcelFileDescriptorインスタンスを使用しPdfRendererをインスタンス化する。
			mPdfRenderer = new PdfRenderer(parcelFileDescriptor);
		}
		if (mPdfRenderer == null) {
			Log.e("ImageManager", "PdfFileList: mPdfRendererがnullです.");
			mFileList = new FileListItem[0];
			return;
		}

		// ファイルリストを作成
		maxPage = mPdfRenderer.getPageCount();
		mFileList = new FileListItem[maxPage];

		for (int page = 0; page < maxPage; page++) {

			FileListItem filelist = new FileListItem();
			filelist.type = IMAGETYPE_PDF;
			filelist.name = "Page" + (page+1);
			filelist.orglen = 0; // ファイルリスト読込中
			mFileList[page] = filelist;

			// 読込通知
			if ((page+1) % 10 == 0) {
				if (mRunningFlag == false) {
					mFileList = new FileListItem[0];
					return;
				}
				Message message = new Message();
				message.what = DEF.HMSG_PROGRESS;
				message.arg1 = (page+1);
				message.arg2 = 0;
				mHandler.sendMessage(message);
			}
		}
		Log.d("ImageManager", "PdfFileList: 終了します.");
	}

	// ソート実行
	public void sort(List<FileListItem> list) {
		if (mFileSort != FILESORT_NONE) {
			Collections.sort(list, new ZipComparator());
		}
	}

	// ソート用比較関数
	public class ZipComparator implements Comparator<FileListItem> {
		public int compare(FileListItem file1, FileListItem file2) {
			int result;
//			result = file1.name.toUpperCase().compareTo(file2.name.toUpperCase());
			result = DEF.compareFileName(file1.name.toLowerCase(), file2.name.toLowerCase());
			if (mFileSort == FILESORT_NAME_DOWN) {
				result *= -1;
			}
			return result;
		}
	}

	// ファイルタイプを返す
	public String getFilePath() {
		return mFilePath;
	}

	// ファイルタイプを返す
	public int getFileType() {
		return mFileType;
	}

	// 最大ファイルサイズ(圧縮時)を返す
	public int getMaxCmpLength() {
		return mMaxCmpLength;
	}

	// 最大ファイルサイズ(解凍時)を返す
	public int getMaxOrgLength() {
		return mMaxOrgLength;
	}

	// バックグラウンドのキャッシュ読み込みを止める
	public void setCacheSleep(boolean sleep) {
		mCacheSleep = sleep;
	}

	// 読み込み処理中断
	public void setBreakTrigger() {
		mRunningFlag = false;
	}

	// 読み込み処理中断
	public void unsetBreakTrigger() {
		mRunningFlag = true;
	}

	// ファイル数を返す
	public int length() {
		if (mFileList != null) {
			return mFileList.length;
		}
		return 0;
	}

	// ファイルを閉じる
	public void closeFiles() {
		try {
			close();
		}
		catch (IOException e) {
			Log.e("close", e.getMessage());
		}
	}

	// ロック用オブジェクト取得
	public Object getLockObject() {
		return mLock;
	}

	// ページ選択時に表示する文字列を作成
	public String createPageStr(int page) {
		// パラメタチェック
		if (mFileList == null || (page < 0 || mFileList.length <= page)) {
			return "";
		}

		String strPath = mFilePath;
		if (strPath.indexOf("smb://") == 0) {
			int idx = strPath.indexOf("@");
			if (idx >= 0) {
				strPath = "smb://" + strPath.substring(idx + 1);
			}
		}

		String pageStr;
		pageStr = (page + 1) + " / " + mFileList.length + "\n" + strPath + "\n" + mFileList[page].name;
		return pageStr;
	}

	public void startCacheRead() throws FileNotFoundException {
		if (mOpenMode != OPENMODE_VIEW) {
			// リスト取得モードの時は読み込み不要
			return;
		}
		// キャッシュ読込みスレッド開始
		mThread = new Thread(this);
		mThread.setPriority(Thread.MIN_PRIORITY);
		mThread.start();
	}

	// 画像の並びを逆にする
	public void reverseOrder() {
		mCacheBreak = true;
		CallImgLibrary.ImageCancel(1);
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(0);
				if (mFileList != null) {
					FileListItem newlist[] = new FileListItem[mFileList.length];

					int num = mFileList.length;
					CallImgLibrary.ImageScaleFree(-1, -1);
					for (int i = 0; i < num; i++) {
						if (mCloseFlag) {
							break;
						}
						CallImgLibrary.ImageFree(i);

						newlist[num - i - 1] = mFileList[i];

						// キャッシュ状態初期化
						mMemCacheFlag[i] = new MemCacheFlag();
						if (mCheCacheFlag != null) {
							mCheCacheFlag[i] = false;
						}
					}
					mFileList = newlist;
				}
			}
		}
	}

	// キャッシュをクリアする
	public void clearMemCache() {
		mCacheBreak = true;
		CallImgLibrary.ImageCancel(1);
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(0);
				CallImgLibrary.ImageScaleFree(-1, -1);

				if (mFileList != null) {
					for (int i = 0; i < mFileList.length; i++) {
						if (mCloseFlag) {
							break;
						}
						// キャッシュ状態初期化
						mMemCacheFlag[i] = new MemCacheFlag();
					}
				}
			}
		}
	}

	public void run() {
		Log.d("ImageManager", "run: 開始します.");
		// 読込用バッファ
		final int CACHE_FPAGE = 4;
		final int CACHE_BPAGE = 2;
		final int CACHE_RANGE = 50;
		byte buf[] = new byte[BIS_BUFFSIZE];
		boolean isError = false;
		boolean fMemCacheExec = false;
		int prevReadPage = -1;
		int sleepTimer;

		sleepTimer = 1000;

		// キャッシュ読込
		while (mRunningFlag && !isError) {
//			String LogStr = "";
//			for (int i = 0; i < mFileList.length; i++) {
//				if (memGetCacheState(i) == MEMCACHE_OK) {
//					LogStr += "*";
//				}
//				else if (memGetCacheState(i) == MEMCACHE_LOCK) {
//					LogStr += "+";
//				}
//				else {
//					LogStr += "-";
//				}
//			}
//			Log.d("mem", LogStr);

			// 処理中断フラグ
			boolean fContinue = false;

			if (sleepTimer > 0) {
				try {
					// 指定秒数スリープ
					Thread.sleep(sleepTimer);
				}
				catch (InterruptedException e) {

				}
			}
			sleepTimer = 50;

			boolean fMemCacheWrite = false;
			int page = -1;
			if (mCacheBreak || mCacheSleep) {
				mCacheBreak = false;
				if (mMemPriority.length > 0) {
					fMemCacheExec = true;
					prevReadPage = -1;
				}

				try {
					Thread.sleep(300);
				}
				catch (InterruptedException e) {

				}
				continue;
			}

			synchronized (mLock) {
				if (mCloseFlag) {
					break;
				}
				if (fMemCacheExec) {
//					mThreadLoading = true;
//					Log.d("run.check", "---- Start ----");
					int iPrio;
					for (iPrio = 1; iPrio < mMemPriority.length && page == -1; iPrio++) {
						if (mCloseFlag) {
							break;
						}
						if (mCacheBreak) {
							// キャッシュ処理中断
							break;
						}

						if (!fMemCacheExec) {
							// メモリキャッシュ不可ならそれ以上しない
							break;
						}

						// チェック対象ページ
						int chkPage = mMemPriority[iPrio] + mCurrentPage;
						int chkPage2 = mMemPriority[iPrio] + mCurrentPage + (mMemPriority[iPrio] >= 0 ? 1 : -1);

						if (0 <= chkPage2 && chkPage2 < mFileList.length && mFileList[chkPage2].width <= 0) {
							if (SizeCheckImage(chkPage2) < 0) {
								break;
							}
						}

						if (0 <= chkPage && chkPage < mFileList.length) {
							if (mFileList[chkPage].width <= 0) {
								if (SizeCheckImage(chkPage) < 0) {
									break;
								}
							}

							// 範囲内の時だけチェック
							if (mMemCacheFlag[chkPage].fSource == false) {
								if (prevReadPage != chkPage && memWriteLock(chkPage, 0, false)) {
									// メモリキャッシュ確保OK
									// Log.d("run", "current" + mCurrentPage + ", chkPage" + chkPage + ", prevPage=" + prevReadPage);
									page = chkPage;
									prevReadPage = chkPage;
									fMemCacheWrite = true;
									fContinue = false;
								}
								else {
									// メモリがなくなったら終了
									// Log.d("run", "chkPage" + chkPage + ", prevReadPage=" + prevReadPage);
									fMemCacheExec = false;
								}
								// ロックしてみたら結果によらずこれ以上探さない
								break;
							}
							else if (mCurrentPage >= 0 && mCurrentPage < mFileList.length) {
								// スケーリング処理を通知
								try {
									if (isDualView() == true) {
										// 並べて表示
										int p;
										int page1 = -1;
										int page2 = -1;	// ターゲット

										if (chkPage < mCurrentPage) {
											// 前方向
											for (p = mCurrentPage - 1 ; p >= chkPage ; p --) {	// 1ページ前からチェック
												if (mCloseFlag) {
													break;
												}
												if (!DEF.checkPortrait(mFileList[p].width, mFileList[p].height, mScrRotate)) {
													// 横
													page1 = p;
													page2 = -1;
												}
												else {
													// 左ページは縦
													if (p == 0 || !DEF.checkPortrait(mFileList[p - 1].width, mFileList[p - 1].height, mScrRotate)) {
														// 左ページが先頭ページ 又は 右ページが横長なら左ページ単体とする
														page1 = p;
														page2 = -1;
													}
													else {
														if (mTopSingle != 0 && p == 1) {
															// 先頭単独ON かつ 右ページが先頭ページなら左ページ単体とする
															page1 = p;
															page2 = -1;
														}
														else {
															// 右ページも縦長なら並べて見開き
															page1 = p - 1;
															page2 = p;
															p --;
														}
													}
												}
											}
										}
										else {
											// 後方向
											for (p = mCurrentPage ; p <= chkPage ; p ++) {	// 1ページ前からチェック
												if (mCloseFlag) {
													break;
												}
												if (!DEF.checkPortrait(mFileList[p].width, mFileList[p].height, mScrRotate) || (p == mCurrentPage && mCurrentSingle)) {
													// 横長 又は 先頭が単ページ指定
													page1 = p;
													page2 = -1;
												}
												else {
													// 左ページは縦
													if (p >= mFileList.length - 1 || !DEF.checkPortrait(mFileList[p + 1].width, mFileList[p + 1].height, mScrRotate)) {
														// 右ページが最終ページ 又は 左ページが横なら右ページ単体とする
														page1 = p;
														page2 = -1;
													}
													else {
														// 左ページも縦長なら並べて見開き
														page1 = p;
														page2 = p + 1;
														p ++;
													}
												}
											}
										}

										if (page2 == -1) {
											// 単ページ
											if (mMemCacheFlag[page1].fSource == true) {
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (ImageScaling(page1, -1, ImageData.HALF_NONE, 0, null, null) == false) {
													// スケール失敗
													fMemCacheExec = false;
												}
											}
										}
										else {
											if (mMemCacheFlag[page1].fSource == true && mMemCacheFlag[page2].fSource == true) {
												// 縦長なら左ページの可能性
												// 左表紙は左右反転
												if (mPageWay != DEF.PAGEWAY_RIGHT) {
													int page3 = page1;
													page1 = page2;
													page2 = page3;
												}
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (ImageScaling(page1, page2, ImageData.HALF_NONE, ImageData.HALF_NONE, null, null) == false) {
													// スケール失敗
													fMemCacheExec = false;
												}
											}
										}
									}
									else if (isHalfView() == true && !DEF.checkPortrait(mFileList[chkPage].width, mFileList[chkPage].height, mScrRotate)) {
										if (mMemCacheFlag[chkPage].fSource == true) {
											// 左側のみ単独表示
											if (mMemCacheFlag[chkPage].fScale[ImageData.HALF_LEFT] == false) {
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (ImageScaling(chkPage, -1, ImageData.HALF_LEFT, ImageData.HALF_NONE, null, null) == false) {
													// スケール失敗
													fMemCacheExec = false;
												}
												// スケールしたら結果によらずこれ以上探さない
												fContinue = true;
											}
											// 右側のみ単独表示
											if (!mCacheBreak && fMemCacheExec == true) {
												if (mMemCacheFlag[chkPage].fScale[ImageData.HALF_RIGHT] == false) {
													// 通知
													//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
													if (ImageScaling(chkPage, -1, ImageData.HALF_RIGHT, ImageData.HALF_NONE, null, null) == false) {
														// スケール失敗
														fMemCacheExec = false;
													}
													// スケールしたら結果によらずこれ以上探さない
													fContinue = true;
												}
											}
										}
									}
									else {
										// 単独表示
										if (mMemCacheFlag[chkPage].fSource == true) {
											if (mMemCacheFlag[chkPage].fScale[ImageData.HALF_NONE] == false) {
												// 通知
												//sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
												if (ImageScaling(chkPage, -1, ImageData.HALF_NONE, ImageData.HALF_NONE, null, null) == false) {
													// スケール失敗
													fMemCacheExec = false;
												}
												// スケールしたら結果によらずこれ以上探さない
												fContinue = true;
											}
										}
									}
								}
								finally {
									// 読み込み完了
									//sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
								}
							}
						}
						else {
							continue;
						}
					}
					if (iPrio >= mMemPriority.length) {
						fMemCacheExec = false;
					}
//					Log.d("run.check", "----  End  ----");
				}
				if (fContinue) {
					// スケール作成した
					continue;
				}

				if (mCacheBreak) {
					// キャッシュ処理中断
					continue;
				}

				// キャッシュ対象ページ
				if (page == -1 && mHostType == HOSTTYPE_SAMBA) {
					// 読込ページを探す
					int startPage = mCurrentPage;
					int range;

					// 対象ページ
					page = -1;

					for (range = 0; range < CACHE_RANGE; range++) {
						if (mCloseFlag) {
							break;
						}
						if (mCacheBreak) {
							// キャッシュ処理中断
							break;
						}

						int st; // 検索範囲
						int ed;

						// 順方向
						st = startPage + CACHE_FPAGE * range + 1;
						ed = startPage + CACHE_FPAGE * (range + 1);
						if (st < mFileList.length) {
							// 最終ページ以内
							if (ed >= mFileList.length) {
								// 範囲がはみ出していれば納める
								ed = mFileList.length - 1;
							}
							for (page = st; page <= ed; page++) {
								if (!cheGetCacheFlag(page)/* && memGetCacheState(page) == MEMCACHE_NONE */) {
									// 読込むページを見つけた
									break;
								}
							}
							if (page <= ed) {
								// キャッシュのないページを発見
								break;
							}
						}

						// 逆方向の割合は減らす
						st = startPage - CACHE_BPAGE * range - 1;
						ed = startPage - CACHE_BPAGE * (range + 1);
						if (st >= 0) {
							// 最終ページ以内
							if (ed < 0) {
								// 範囲がはみ出していれば納める
								ed = 0;
							}
							for (page = st; page >= ed; page--) {
								if (!cheGetCacheFlag(page)/* && memGetCacheState(page) != MEMCACHE_OK */) {
									// 読込むページを見つけた
									break;
								}
							}
							if (page >= ed) {
								// キャッシュのないページを発見
								break;
							}
						}
					}
					if (mCacheBreak) {
						// キャッシュ処理中断
						continue;
					}

					if (range >= CACHE_RANGE || page < 0 || mFileList.length <= page) {
						sleepTimer = 1000;
						continue;
					}
				}
				if (page != -1) {
					if (!mRunningFlag) {
						// closeされた場合
						break;
					}
					if (mCacheBreak) {
						// メインスレッドでビットマップの読込処理が入った
						continue;
					}

					// キャッシュ読み込みを通知
					// Log.d("comitton", "Load p=" + page);
					sendMessage(mHandler, DEF.HMSG_CACHE, 0, fMemCacheWrite ? 1 : 0, null);

					if (fMemCacheWrite) {
						if (cheGetCacheFlag(page)) {
							// ファイルキャッシュあり
							mCheWriteFlag = false;
						}
						else {
							// ファイルキャッシュなしならキャッシュする
							mCheWriteFlag = true;
							long pos;
							int len;
							mLoadingPage = page;
							if (mFileType != FILETYPE_DIR) {
								pos = mFileList[page].cmppos;
								len = mFileList[page].cmplen;
							}
							else {
								pos = mFileList[page].orgpos;
								len = mFileList[page].orglen;
							}
							try {
								// ファイル書き込み準備
								cheSeek(pos, len, page);
							}
							catch (IOException e) {
								Log.e("FileCache/start", e.getMessage());
								// ファイルキャッシュしない
								mCheWriteFlag = false;
							}
						}

						try {
							ImageData id = LoadImage(page, false);
							if (id == null) {
								// 読み込み失敗ならメモリキャッシュを継続しない
								fMemCacheExec = false;
							}
						}
						catch (IOException e) {
							Log.e("LoadImage", "thread: " + e.getMessage());
						}
//							mThreadLoading = true;
//							Log.d("run.Open", "---- Start ----");n

					}
					else {
						// このページの読込サイズ
						int lastsize;
						if (!fMemCacheWrite && mFileType != FILETYPE_DIR) {
							lastsize = mFileList[page].cmplen;// + SIZE_CENTHEADER + SIZE_TERMHEADER;
						}
						else {
							lastsize = mFileList[page].orglen;
						}

						// ファイルキャッシュする
						mCheWriteFlag = true;
						long pos;
						int len;
						if (mFileType != FILETYPE_DIR) {
							pos = mFileList[page].cmppos;
							len = mFileList[page].cmplen;
						}
						else {
							pos = mFileList[page].orgpos;
							len = mFileList[page].orglen;
						}

						try {
							// ファイル読み込み準備
							setLoadBitmapStart(page, false);
							cheSeek(pos, len, page);
						}
						catch (IOException e) {
							// エラーログ
							Log.e("FileCache/start", e.getMessage());
							break;
						}

						//
						while (mRunningFlag) {
							if (mCloseFlag) {
								break;
							}
							// Log.d("run.Load", "---- Start ----");
							if (!mRunningFlag) {
								// closeされた場合
								break;
							}
							if (mCacheBreak) {
								// メインスレッドでビットマップの読込処理が入った
								break;
							}
							try {
								int retsize;
								retsize = this.read(buf);
								if (retsize > 0) {
									lastsize -= retsize;
								}
								if (lastsize == 0 || retsize <= 0) {
									// ファイル終端？
									break;
								}
							}
							catch (Exception e) {
								String s = "";
								if (e != null && e.getMessage() != null) {
									s = e.getMessage();
								}
								Log.e("FileCache/read", s);
								// isError = true;
								break;
							}
						}
						try {
							setLoadBitmapEnd();
						}
						catch (IOException e) {
							String msg = "";
							if (e != null) {
								msg = e.getMessage();
							}
							Log.e("FileCache/end", msg);
						}
					}

					// キャッシュ読み込み完了を通知
					sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
				}
				else {
					sleepTimer = 1000;
				}
			}
		}
		mTerminate = true;
		Log.d("ImageManager", "run: 終了します.");
	}

	private void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj) {
//		Log.d("mark", "arg=" + arg1 + ", " + arg2);
		Message message = new Message();
		message.what = what;
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.obj = obj;
		handler.sendMessage(message);
	}

	// 見開きモードか？
	private boolean isDualView() {
		if (mScrDispMode == DISPMODE_DUAL) {
			return true;
		}
		else if (mScrDispMode == DISPMODE_EXCHANGE) {
			if (DEF.checkPortrait(mScrWidth, mScrHeight) == false) {
				return true;
			}
		}
		return false;
	}

	// 単ページモードか？
	private boolean isHalfView() {
		if (mScrDispMode == DISPMODE_HALF) {
			return true;
		}
		else if (mScrDispMode == DISPMODE_EXCHANGE) {
			if (DEF.checkPortrait(mScrWidth, mScrHeight) == true) {
				return true;
			}
		}
		return false;
	}

	public int getHostType() {
		return mHostType;
	}

	// ZIP内のファイル情報を返す
	public FileListItem[] getList() {
		return mFileList;
	}

	public int search(String name) {
		if (name != null && name.length() > 0 /* mFileType == FILETYPE_DIR */) {
			for (int i = 0; i < mFileList.length; i++) {
				if (mFileList[i].name.equals(name)) {
					return i;
				}
			}
		}
		return -1;
	}

	// 現在ページを設定
	public boolean setCurrentPage(int page, boolean single) {
		// キャッシュ範囲などで使用
		mCurrentPage = page;
		mCurrentSingle = single;
		return true;
	}

	// ビットマップ読み込み開始
	public ImageData getImageData(int page) {
		// パラメタチェック
		if (mFileList != null && page < 0 && mFileList.length <= page) {
			return null;
		}

		ImageData id = null;
		if (mMemCacheFlag[page].fSource == true) {
			// メモリキャッシュあり
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
			for (int i = 0; i < 3; i++) {
				if (mMemCacheFlag[page].fScale[i]) {
					id.HalfMode = i;
					id.SclWidth = mFileList[page].swidth[i];
					id.SclHeight = mFileList[page].sheight[i];
					id.FitWidth = mFileList[page].fwidth[i];
					id.FitHeight = mFileList[page].fheight[i];
				}
			}
		}
		return id;
	}

	// ビットマップ読み込み開始
	public ImageData loadBitmap(int page, boolean notice) throws IOException {
		// パラメタチェック
		if (mFileList != null && page < 0 && mFileList.length <= page) {
			return null;
		}

		ImageData id = null;
		mCacheBreak = true;
		CallImgLibrary.ImageCancel(1);
		synchronized (mLock) {
			if (!mCloseFlag) {
				mCacheBreak = false;
				CallImgLibrary.ImageCancel(0);
				mThreadLoading = false;
				if (mMemCacheFlag[page].fSource == true) {
					// メモリキャッシュあり
					id = new ImageData();
					id.Page = page;
					id.Width = mFileList[page].width;
					id.Height = mFileList[page].height;
				} else {
					// メモリキャッシュ無しなので読み込み
					mCheWriteFlag = false;
					if (mCacheMode != CACHEMODE_FILE && mHostType == HOSTTYPE_SAMBA) {
						// メモリキャッシュに保存できない場合はファイルキャッシュする
						mCheWriteFlag = true;
						long pos;
						int len;
						if (mFileType != FILETYPE_DIR) {
							pos = mFileList[page].cmppos;
							len = mFileList[page].cmplen;// + SIZE_CENTHEADER + SIZE_TERMHEADER;
						} else {
							pos = mFileList[page].orgpos;
							len = mFileList[page].orglen;
						}
						try {
							cheSeek(pos, len, page);
						} catch (IOException e) {
							Log.e("ImageManager", "loadBitmap: cheSeek Catch Exeption. " + e.getMessage());
							mCheWriteFlag = false;
						}
					}
					if (!mCloseFlag) {
						id = LoadImage(page, notice);
					}
				}
				mThreadLoading = true;
			}
		}
		return id;
	}

	// ビットマップ読み込み開始
	public void setLoadBitmapStart(int page, boolean notice) throws IOException {
		int from;

		// 読み込み位置を設定
		long pos;
		int len;
		if (mFileType != FILETYPE_DIR) {
			pos = mFileList[page].cmppos;
			len = mFileList[page].cmplen;// + SIZE_CENTHEADER + SIZE_TERMHEADER;
			mLoadingPage = page;
		}
		else {
			pos = mFileList[page].orgpos;
			len = mFileList[page].orglen;
		}
		mDataSize = len;

		if (cheGetCacheFlag(page)) {
			// ファイルキャッシュに存在
			mCacheMode = CACHEMODE_FILE;
			from = FROMTYPE_CACHE;
			// キャッシュファイルの読込設定
			cheSeek(pos, len, page);
		}
		else {
			// キャッシュに存在しない
			mCacheMode = CACHEMODE_NONE;
			if (mHostType == HOSTTYPE_SAMBA) {
				from = FROMTYPE_SERVER;
			}
			else {
				from = FROMTYPE_LOCAL;
			}
			// 元ファイルの読込設定
			if (mFileType == FILETYPE_DIR) {
				dirSetPage(mFilePath + mFileList[page].name);
				if (mHostType == HOSTTYPE_SAMBA)
					cmpSeek(0, len);
			}
			else {
				cmpSeek(pos, len);
			}
		}

//		if (mCacheMode != CACHEMODE_MEM && mFileList[page].len + BIS_BUFFSIZE < mMemCache.length) {
//			mMemCacheSave = true;
//		}
//		else {
//			mMemCacheSave = false;
////			mMemCachePage = -1;
//		}
		if (!mThreadLoading && notice) {
			// 0%
			sendHandler(DEF.HMSG_LOADING, from, 0, null);
		}

		mStartTime = System.currentTimeMillis();
		mReadSize = 0;
		mMsgCount = 0;
	}

	// ビットマップ読み込み終了
	public void setLoadBitmapEnd() throws IOException {
//		if (page != -1 && mMemWriteFlag) {
//			// 同時キャッシュ保存モードで正常終了した場合
//			cheSetCacheFlag(page);
//		}
		if (mFileType != FILETYPE_DIR) {
			// mFile.endPage();
		}
		else {
			dirEndPage();
		}
//		if (mMemCacheSave) {
//			// キャッシュをためた
//			mMemCacheLen = mMemCachePos;
//			mMemCachePage = page;
//		}
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int read(byte buf[], int off, int len) throws IOException {
		int ret = 0;
		try {
			if (mCacheMode == CACHEMODE_FILE) {
				// ファイルキャッシュに存在する
				ret = cheRead(buf, off, len);
//				Log.d("Mgr.read", "size:" + len + " from Cache");
			}
			else {
				// キャッシュに存在しない
				if (mFileType == FILETYPE_DIR || mFileType == FILETYPE_IMAGE) {
					ret = dirRead(buf, off, len);
				}
				else {
					ret = cmpRead(buf, off, len);
//					Log.d("Mgr.read", "size:" + len + " from File");
				}
//				Log.d("Mgr.read", "memcache.write size:" + len);
			}

//			if (mMemCacheSave) {
//				System.arraycopy(buf, off, mMemCache, mMemCachePos, len);
//				mMemCachePos += len;
//			}
		}
		catch (Exception e) {
			if (e != null) {
				String s = e.getMessage();
				if (s != null) {
					Log.e("Read", s);
				}
			}

			if (mThreadLoading == false) {
				// ユーザ操作による読み込みの場合
				Message message = new Message();
				message.what = DEF.HMSG_ERROR;
				message.obj = e.getMessage();
				mHandler.sendMessage(message);
			}
			// 終了時は壊れたデータを返してやろう
			Arrays.fill(buf, off, len - off, (byte)0);
			return len - off;
			//throw new IOException(e.getMessage());
		}

		if (ret > 0) {
			mReadSize += ret;
		}
		long nowTime = System.currentTimeMillis();
		// ちょっとだけ更新頻度を増やしてみる
		if (!mThreadLoading && nowTime - mStartTime > (mMsgCount + 1) * 500) {
			mMsgCount++;
			int prog = (int) ((long) mReadSize * 100 / mDataSize);
			int rate = (int) ((long) mReadSize * 10 / (nowTime - mStartTime));
			sendHandler(DEF.HMSG_LOADING, prog << 24 | 0x0100 | mFromType, rate, null);
		}
		return ret;
	}

	@Override
	public void close() throws IOException {
		mRunningFlag = false;
		mCloseFlag = true;
		if (mThread != null) {
			mThread.interrupt();
			// スレッドの終了待ち
			for (int i = 0; i < 10 && mTerminate == false; i++) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {

				}
			}
		}
		else {
			mTerminate = true;
		}

		// ■■■ ここで固まる ■■■
		synchronized (mLock) {
			cheClose();
			cmpClose();
			dirClose();
			if (mRarStream != null) {
				mRarStream.close();
				mRarStream = null;
			}
			if (mPdfRenderer != null) {
				mPdfRenderer.close();
				mPdfRenderer = null;
			}
			CallImgLibrary.ImageTerminate();
		}
	}

	// 4バイト数値取得
	public int getInt(byte b[], int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00) | (((int) b[pos + 2] << 16) & 0x00FF0000) | (((int) b[pos + 3] << 24) & 0xFF000000);

		return val;
	}

	// 2バイト数値取得
	public short getShort(byte b[], int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00);

		return (short) val;
	}

	// 通知
	private void sendHandler(int id, int arg1, int arg2, Object data) {
		Message message = new Message();
		message.what = id;
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.obj = data;
		mHandler.sendMessage(message);
	}

	/*************************** FileCache ***************************/
	private RandomAccessFile mCheRndFile;
	private int mChePage;
	private int mCheSize;
	private int mChePos;
	private boolean mCheCacheFlag[];
	private boolean mCheEnable;

	public void fileCacheInit(int total, boolean isEnable) throws IOException {
		Log.d("ImageManager", "fileCacheInit: 開始します. total=" + total + ", isEnable=" + isEnable);
		mCheEnable = isEnable;

		// サーバアクセス時はファイルキャッシュも行う
		if (isEnable) {
			// キャッシュ読込モードオン
			String file = DEF.getBaseDirectory() + "comittona.cache";
			String path = DEF.getBaseDirectory() + "thumb/";
			try {
				new File(path).mkdirs();
				new File(file).delete();
				mCheRndFile = new RandomAccessFile(file, "rw");
			}
			catch (Exception e) {
				mCheEnable = false;
				Log.d("Cache.open", e.getMessage());
				Message message = new Message();
				message.what = DEF.HMSG_ERROR;
				message.obj = "Open Error.(" + path + ")";
				mHandler.sendMessage(message);
			}
		}

		// 参照先
		if (mCheEnable) {
			mCheCacheFlag = new boolean[total];
			// キャッシュ済みフラグ初期化
			for (int i = 0; i < mCheCacheFlag.length; i++) {
				mCheCacheFlag[i] = false;
			}
		}
		Log.d("ImageManager", "fileCacheInit: 終了します.");
	}

	public void cheSetCacheFlag(int index) {
		if (mCheCacheFlag != null && index >= 0 && index < mCheCacheFlag.length) {
			mCheCacheFlag[index] = true;
		}
	}

	public boolean cheGetCacheFlag(int index) {
		if (mCheCacheFlag != null && index >= 0 && index < mCheCacheFlag.length) {
			return mCheCacheFlag[index];
		}
		return false;
	}

	public int cheRead(byte buf[], int off, int len) throws IOException {
		if (mRunningFlag == false) {
			throw new IOException("ImageManaget: cheRead: User Canceled.");
		}
		if (!mCheEnable) {
			return -1;
		}
		if (mCheSize == mChePos) {
			return -1;
		}
		int ret = 0;
		int size = len;
		if (size > mCheSize - mChePos) {
			size = mCheSize - mChePos;
		}
		if (size > 0) {
			ret = mCheRndFile.read(buf, off, size);
		}
		if (mChePos == 0 && mFileType == FILETYPE_ZIP) {
			// SHIFT-JISで読込み
			if (ret >= OFFSET_LCL_FNAME_LEN + 2) {
				int lenFName = getShort(buf, OFFSET_LCL_FNAME_LEN);

				if (ret >= SIZE_LOCALHEADER + lenFName) {
					for (int i = 0; i < lenFName - 4; i++) {
						buf[off + SIZE_LOCALHEADER + i] = '0';
					}
				}
			}
		}
		mChePos += ret;
		return ret;
	}

	public void cheWrite(byte buf[], int off, int len) throws IOException {
		if (!mCheEnable) {
			return;
		}

		if (len > mCheSize - mChePos) {
			len = mCheSize - mChePos;
		}
		if (len > 0) {
			mCheRndFile.write(buf, off, len);
			mChePos += len;
		}
		if (mCheSize == mChePos) {
			cheSetCacheFlag(mChePage);
//			Log.d("cheWrite", "Page:" + mChePage + " Cache OK (" + mCheSize + "/" + mChePos + ")" + (mThreadLoading ? "Sub" : "Main"));
		}
	}

	public void cheSeek(long pos, int size, int page) throws IOException {
		if (!mCheEnable) {
			return;
		}
		//Log.d("ImageManager", "cheSeek 開始します.");
		// エントリーサイズ
		mCheSize = size;
		mChePos = 0;
		mChePage = page;
		if (mFileType != FILETYPE_PDF) {
			mCheRndFile.seek(pos);
		}
		//Log.d("ImageManager", "cheSeek 終了します.");
	}

	public void cheClose() throws IOException {
		if (mCheRndFile != null) {
			mCheRndFile.close();
			mCheRndFile = null;
		}
	}

	/*************************** CompressAccess ***************************/
	private WorkStream mWorkStream;
	private int mCmpSize;
	private int mCmpPos;

	public void fileAccessInit(String path) throws IOException {
		Log.d("ImageManager", "fileAccessInit: 開始します. path=" + path);
		// 参照先
		if (mHostType == HOSTTYPE_SAMBA) {
//			mSambaRnd = FileAccess.jcifsAccessFile(path, mUser, mPass);
			mWorkStream = new WorkStream(path, "", mUser, mPass, false);
		}
		else if (mHostType == HOSTTYPE_LOCAL) {
//			mLocalRnd = new RandomAccessFile(path, "r");
			mWorkStream = new WorkStream("", path, mUser, mPass, false);
		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			mWebDAVRnd = new WebDAVRandomAccessFile(path, mUser, mPass);
//		}
		else {

		}
		Log.d("ImageManager", "fileAccessInit: 終了します. path=" + path);
	}

	public int cmpDirectRead(byte buf[], int off, int len) throws IOException {
		int ret = 0;
		ret = mWorkStream.read(buf, off, len);
//		if (mHostType == HOSTTYPE_SAMBA) {
//			ret = mSambaRnd.read(buf, off, len);
//		}
//		else if (mHostType == HOSTTYPE_LOCAL) {
//			ret = mLocalRnd.read(buf, off, len);
//		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			ret = mWebDAVRnd.read(buf, off, len);
//		}
		return ret;
	}

	public void cmpDirectSeek(long pos) throws IOException {
		//Log.d("ImageManager", "cmpDirectSeek 開始します.");
		// エントリーサイズ
		if (mFileType != FILETYPE_PDF) {
			mWorkStream.seek(pos);
		}
//		if (mHostType == HOSTTYPE_SAMBA) {
//			mSambaRnd.seek(pos);
//		}
//		else if (mHostType == HOSTTYPE_LOCAL) {
//			mLocalRnd.seek(pos);
//		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			mWebDAVRnd.seek(pos);
//		}
		//Log.d("ImageManager", "cmpDirectSeek 終了します.");
	}

	public long cmpDirectTell() throws IOException {
		// エントリーサイズ
		return mWorkStream.getFilePointer();
//		if (mHostType == HOSTTYPE_SAMBA) {
//			return mSambaRnd.getFilePointer();
//		}
//		else if (mHostType == HOSTTYPE_LOCAL) {
//			return mLocalRnd.getFilePointer();
//		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			return mWebDAVRnd.getFilePointer();
//		}
//		return 0;
	}

	public long cmpDirectLength() throws IOException {
		long fileLength = 0;

		// エントリーサイズ
		fileLength = mWorkStream.length();
//		if (mHostType == HOSTTYPE_SAMBA) {
//			fileLength = mSambaRnd.length();
//		}
//		else if (mHostType == HOSTTYPE_LOCAL) {
//			fileLength = mLocalRnd.length();
//		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			fileLength = mWebDAVRnd.length();
//		}
		if ((fileLength & 0xFFFFFFFF00000000L) == fileLength) {
			fileLength = (fileLength >> 32) & 0x00000000FFFFFFFFL;
		}
		Log.d("ImageManager", "cmpDirectLength filelength=" + fileLength);
		return fileLength;
	}

	public int cmpRead(byte buf[], int off, int len) throws IOException {
		if (mRunningFlag == false) {
			throw new IOException("ImageManager: cmpDirectLength: User Canceled.");
		}
		else if (mCmpSize <= mCmpPos) {
			// throw new IOException("This file format is not supported.");
			return -1;
		}

		int ret = 0;
		int size = len;
		if (size > mCmpSize - mCmpPos) {
			size = mCmpSize - mCmpPos;
		}
		if (size > 0) {
			ret = mWorkStream.read(buf, off, size);
//			if (mHostType == HOSTTYPE_SAMBA) {
//				ret = mSambaRnd.read(buf, off, size);
//			}
//			else if (mHostType == HOSTTYPE_LOCAL) {
//				ret = mLocalRnd.read(buf, off, size);
//			}
//			else if (mHostType == HOSTTYPE_WEBDAV) {
//				ret = mWebDAVRnd.read(buf, off, size);
//			}
		}
		if (ret <= 0) {
			return -1;
		}
		if (mCmpPos == 0 && mFileType == FILETYPE_ZIP) {
			// SHIFT-JISで読込み
			if (ret >= OFFSET_LCL_FNAME_LEN + 2) {
				int lenFName = getShort(buf, OFFSET_LCL_FNAME_LEN);

				if (ret >= SIZE_LOCALHEADER + lenFName) {
					String name = new String(buf, SIZE_LOCALHEADER, lenFName, mCharset);
					for (int i = 0; i < lenFName - 4; i++) {
						buf[off + SIZE_LOCALHEADER + i] = '0';
					}
					// セントラルディレクトリヘッダからはExtraSizeが取得出来ないので
					// ここでローカルヘッダの情報を取得して適宜更新する
					if(mFileList[mLoadingPage].sizefixed == false) {
						mCmpSize += getExtraSize(buf);
						mFileList[mLoadingPage].cmplen = mCmpSize;
						mFileList[mLoadingPage].sizefixed = true;
					}
					if(mCheWriteFlag)
						mCheSize = mCmpSize;
				}
			}
		}
		if (mCheWriteFlag) {
			// ファイルにキャッシュ
//			if (size < len || ret < size) {
//				Log.d("zipRead", "size:" + size + " len:" + len);
//			}
			cheWrite(buf, off, ret);
		}
		mCmpPos += ret;
//		if (mPos == mSize) {
//			size = SIZE_TRAILER - mHeaderPos;
//			System.arraycopy(TERM_HEADER, mHeaderPos, buf, off + ret, size);
//			ret += size;
//			mHeaderPos = size;
//		}
		return ret;
	}

	public void cmpSeek(long pos, int size) throws IOException {
		//Log.d("ImageManager", "cmpSeek 開始します.");
		// エントリーサイズ
		mCmpSize = size;
		mCmpPos = 0;
//		mHeaderPos = 0;
		if (mFileType != FILETYPE_PDF) {
			mWorkStream.seek(pos);
		}
//		if (mHostType == HOSTTYPE_SAMBA) {
//			mSambaRnd.seek(pos);
//		}
//		else if (mHostType == HOSTTYPE_LOCAL) {
//			mLocalRnd.seek(pos);
//		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			mWebDAVRnd.seek(pos);
//		}
		//Log.d("ImageManager", "cmpSeek 終了します.");
	}

	public void cmpClose() throws IOException {
		if (mWorkStream != null) {
			mWorkStream.close();
			mWorkStream = null;
		}
//		if (mSambaRnd != null) {
//// 閲覧終了時に固まるのでコメントアウト
////			mSambaRnd.close();
//			mSambaRnd = null;
//		}
//		if (mLocalRnd != null) {
//			mLocalRnd.close();
//			mLocalRnd = null;
//		}
//		if (mWebDAVRnd != null) {
//			mWebDAVRnd.close();
//			mWebDAVRnd = null;
//		}
		if (mFileType == FILETYPE_RAR) {
			// RARの領域解放
			CallJniLibrary.rarClose();
		}
	}

	/*************************** DirAccess ***************************/
	private BufferedInputStream mDirStream;
	private ArrayList<FileData> mFiles;

	private int mDirIndex;
	private int mDirOrgPos;


	public void dirListFiles(String path, String user, String pass) throws IOException {
		mDirIndex = 0;
		mDirOrgPos = 0;
		try {
			mFiles = FileAccess.listFiles(path, user, pass);
		}
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	public FileListItem dirGetFileListItem() throws IOException {
		while (true) {
			String name = "";
			boolean flag = false;
			long size = 0;

			if (mDirIndex < 0 || mFiles == null || mFiles.size() <= mDirIndex) {
				break;
			}
			name = mFiles.get(mDirIndex).getName();
			int len = name.length();
			if (name != null && len >= 1 && name.substring(len - 1).equals("/")) {
				flag = true;
			}
			else {
				flag = false;
			}
			size = mFiles.get(mDirIndex).getSize();

			mDirIndex++;
			if (!flag) {
				// 通常のファイル
				if (name.length() <= 4) {
					continue;
				}
				if (mHidden == true && DEF.checkHiddenFile(name)) {
					continue;
				}

				String ext = DEF.getFileExt(name);
				short type = 0;
				if (FileData.isImage(ext)) {
					if (DEF.WITH_JPEG && (ext.equals(".jpg") || ext.equals(".jpeg"))) {
						type = IMAGETYPE_JPEG;
					}
					else if (DEF.WITH_PNG && ext.equals(".png")) {
						type = IMAGETYPE_PNG;
					}
					else if (DEF.WITH_GIF && ext.equals(".gif")) {
						type = IMAGETYPE_GIF;
					}
					else if (DEF.WITH_WEBP && ext.equals(".webp")) {
						type = IMAGETYPE_WEBP;
					}
					else if (DEF.WITH_AVIF && ext.equals(".avif")) {
						type = IMAGETYPE_AVIF;
					}
					else if (DEF.WITH_HEIF && (ext.equals(".heif") || ext.equals(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						type = IMAGETYPE_HEIF;
					}
					else if (DEF.WITH_JXL && ext.equals(".jxl")) {
						type = IMAGETYPE_JXL;
					}
				}
				else if (FileData.isText(ext) && (mOpenMode == OPENMODE_LIST || mOpenMode == OPENMODE_TEXTVIEW)) {
					type = 3;
				}

				if (type != 0) {
					// Image Fileのみ採用
					FileListItem imgfile = new FileListItem();
					imgfile.name = name;
					imgfile.type = type;
					imgfile.cmppos = 0;
					imgfile.orgpos = mDirOrgPos;
					imgfile.cmplen = 0;
					imgfile.orglen = (int) size;
//					imgfile.bmpsize = 0;
					mDirOrgPos += size;
					return imgfile;
				}
			}
		}
		return null;
	}

	public void dirSetPage(String imagefile) throws IOException {
		if (mHostType == HOSTTYPE_SAMBA) {
//			mSambaRnd = FileAccess.jcifsAccessFile(path, mUser, mPass);
			mWorkStream = new WorkStream(imagefile, "", mUser, mPass, false);
		}
		else if (mHostType == HOSTTYPE_LOCAL) {
//			mLocalRnd = new RandomAccessFile(path, "r");
			mWorkStream = new WorkStream("", imagefile, mUser, mPass, false);
		}
//		if (mHostType == HOSTTYPE_SAMBA) {
//			try {
//				//SmbFileInputStreamは妙に遅いっぽいので、通常のファイルでもSmbRandomAccessFileを使う
//				mSambaRnd = FileAccess.jcifsAccessFile(imagefile, mUser, mPass);
//			}
//			catch (IOException e) {
//				throw new IOException(e.getMessage());
//			}
//		}
//		else if (mHostType == HOSTTYPE_LOCAL) {
//			try {
//				mDirStream = new BufferedInputStream(new FileInputStream(imagefile), BIS_BUFFSIZE);
//			}
//			catch (IOException e) {
//				throw new IOException(e.getMessage());
//			}
//		}
//		else if (mHostType == HOSTTYPE_WEBDAV) {
//			mDirStream = new BufferedInputStream(new WebDAVInputStream(imagefile, mUser, mPass), BIS_BUFFSIZE);
//		}
		else {
			throw new IOException("Illegal hosttype.");
		}
	}

	public void dirEndPage() throws IOException {
		if (mWorkStream != null) {
			mWorkStream.close();
			mWorkStream = null;
		}
//		if (mHostType == HOSTTYPE_SAMBA) {
//			if (mSambaRnd != null) {
//// 閲覧終了時に固まるのでコメントアウト
////				mSambaRnd.close();
//				mSambaRnd = null;
//			}
//		}else {
//			if (mDirStream != null) {
//				mDirStream.close();
//			}
//		}
	}

	public int dirRead(byte buf[], int off, int len) throws IOException {
		if (mRunningFlag == false) {
			throw new IOException("ImageManager: dirEndPage: User Canceled.");
		}

		int ret = 0;
		ret = mWorkStream.read(buf, off, len);
//		if (mHostType == HOSTTYPE_SAMBA) {
//			return cmpRead(buf, off, len);
//		}
//
//		int ret = 0;
//		ret = mDirStream.read(buf, off, len);
//		Log.d("dirRead", " ret:" + ret);

		if (mCheWriteFlag) {
			// ファイルにキャッシュ
			cheWrite(buf, off, ret);
		}
		return ret;
	}

	public void dirClose() throws IOException {
		if (mWorkStream != null) {
			mWorkStream.close();
			mWorkStream = null;
		}
//		if (mDirStream != null) {
//			mDirStream.close();
//			mDirStream = null;
//		}
//		if (mSambaDir != null) {
//			mSambaDir = null;
//		}
//		if (mLocalDir != null) {
//			mLocalDir = null;
//		}
//		if (mWebDAVDir != null) {
//			mWebDAVDir = null;
//		}
	}

	/*************************** MemoryCache ***************************/
	public static final byte MEMCACHE_NONE = 0;
//	public static final byte MEMCACHE_LOCK  = 1;
	public static final byte MEMCACHE_ORG = 2;
	public static final byte MEMCACHE_SCALE = 3;
//	public static final byte MEMCACHE_CHECK = 4;

	private int mMemPrevPages;
	private int mMemNextPages;
	private MemCacheFlag mMemCacheFlag[];
	private int mMemPriority[];

	private class MemCacheFlag {
		public boolean fSource = false;
		public boolean fScale[] = { false, false, false };
	}

	private boolean MemoryCacheInit(int memsize, int next, int prev, int total, long maxorglen) {
		Log.d("ImageManager", "MemoryCacheInit: 開始します. memsize=" + memsize + ", prev=" + prev + ", total=" + total + ", maxorglen=" + maxorglen);
		mMemNextPages = next;
		if (mMemNextPages == 0) {
			mMemNextPages = 1;
		}
		mMemPrevPages = prev;

		int ret = CallImgLibrary.ImageInitialize(maxorglen, memsize, mFileList.length, mMaxThreadNum);
		if (ret < 0) {
			return false;
		}

		// 配列初期化
		mMemCacheFlag = new MemCacheFlag[total];
		for (int i = 0; i < total; i++) {
			// キャッシュ状態初期化
			mMemCacheFlag[i] = new MemCacheFlag();
		}

		// 優先順位保持
		mMemPriority = new int[prev + next + 1];
		int prevIdx = 0;
		int nextIdx = 0;
		boolean fCacheNext;
		mMemPriority[0] = 0;
		for (int i = 1; i < prev + next + 1; i++) {
			if (mMemPrevPages == 0) {
				// 前頁方向にキャッシュしない
				fCacheNext = true;
			}
			else if (mMemNextPages == 0) {
				// 次頁方向にキャッシュしない
				fCacheNext = false;
			}
			else if (nextIdx < 2 && mMemNextPages >= 2) {
				// 次の2ページだけは優先的に読込
				fCacheNext = true;
			}
			else if (-prevIdx * 1000 / mMemPrevPages >= nextIdx * 1000 / mMemNextPages) {
				// 次頁の読込みが少ないので読込み
				fCacheNext = true;
			}
			else {
				// 前頁の読込みが少ないので読込み
				fCacheNext = false;
			}

			if (fCacheNext) {
				// 次ページ方向
				nextIdx++;
				mMemPriority[i] = nextIdx;
			}
			else {
				// 前ページ方向
				prevIdx--;
				mMemPriority[i] = prevIdx;
			}
		}
		Log.d("ImageManager", "MemoryCacheInit: 終了します.");
		return true;
	}

	private MemCacheFlag memGetCacheState(int page) {
		return mMemCacheFlag[page];
	}

//	public boolean memSaveCache(int page) {
//		// イメージをメモリキャッシュに保存
//		mFileList[page].width = bm.getWidth();
//		mFileList[page].height = bm.getHeight();
////		mFileList[page].bmpsize = mFileList[page].width * mFileList[page].height * 2;
//
//		int ret = CallImgLibrary.ImageSave(page, bm);
//		if (ret == 0) {
//			mMemCacheFlag[page] = MEMCACHE_OK;
//			return true;
//		}
//		return false;
//	}

	public boolean memFreeCache(int page) {
		// メモリキャッシュを解放
		int ret = CallImgLibrary.ImageFree(page);
		if (ret == 0) {
			mMemCacheFlag[page].fSource = false;
			mMemCacheFlag[page].fScale[0] = false;
			mMemCacheFlag[page].fScale[1] = false;
			mMemCacheFlag[page].fScale[2] = false;
			return true;
		}
		return false;
	}

	// キャッシュ書き込みするページを指定
	public boolean memWriteLock(int page, int half, boolean sclMode) {
		if (!sclMode && mMemCacheFlag[page].fSource == true) {
			// 元画像読み込みなのに未キャッシュじゃない場合
			return false;
		}
//		else if (sclMode && mMemCacheFlag[page] != MEMCACHE_ORG) {
//			// スケーリング用なのに元画像のみじゃない場合
//			return false;
//		}

//		// 自身が範囲外なら終了
//		if (page < mCurrentPage - mMemPrevPages || mCurrentPage + mMemNextPages < page) {
//			return false;
//		}

		boolean fClear = true;
		int clearIdx = mMemPriority.length - 1;

		int lineCount;
		int useCount;
		if (!sclMode) {
			// 元画像モード
			lineCount = (BLOCKSIZE / (mFileList[page].width + HOKAN_DOTS));
			useCount = (int) (Math.ceil((double) mFileList[page].height / (double) lineCount));
			CallImgLibrary.ImageFree(page);
		}
		else {
			// スケールモード
			lineCount = BLOCKSIZE / (mFileList[page].swidth[half] + HOKAN_DOTS);
			useCount = (int) (Math.ceil((double) mFileList[page].sheight[half] / (double) lineCount));
			CallImgLibrary.ImageScaleFree(page, half);
		}

		for (int loop = 0; fClear == true; loop++) {
			// 未使用領域で割り当て
			// ブロック数を求める
			int freeCount = CallImgLibrary.ImageGetFreeSize();
			if (freeCount >= useCount) {
				// 領域が足りた
			return true;
		}

			if (loop == 0) {
				// 初回は範囲外を全て消す
				for (int i = 0; i < mMemCacheFlag.length; i++) {
					if (i < mCurrentPage - mMemPrevPages || mCurrentPage + mMemNextPages < i) {
						if (mMemCacheFlag[i].fSource == true) {
							// メモリ使用中であれば解放
							mMemCacheFlag[i].fSource = false;
							if (memFreeCache(i)) {
								// 解放する物があった

							}
						}
					}
				}
			}
			else {
				// 自身が範囲外なら終了
				if (page < mCurrentPage - mMemPrevPages || mCurrentPage + mMemNextPages < page) {
					return false;
				}

				// 範囲外を消しましょう
				fClear = false;
				int clr = -1;
				while (clearIdx >= 0) {
					clr = mCurrentPage + mMemPriority[clearIdx];
					clearIdx--;
					if (clr == page) {
						// ロックしたい対象ページまできてしまったらループ終了
						break;
					}
					if (0 <= clr && clr < mMemCacheFlag.length) {
						if (mMemCacheFlag[clr].fSource == true) {
							mMemCacheFlag[clr].fSource = false;
							if (memFreeCache(clr)) {
								// 解放する物があった
								fClear = true;
								break;
							}
						}
					}
				}
			}
		}
		// 領域不足でロックできず
		return false;
	}

	public static Bitmap GetBitmapFromPath(String filepath) {
		boolean debug = false;

		int ret = 0;
		int width = 0;
		int height = 0;
		WorkStream ws = null;
		File fileObj = null;
		int extType = 0;
		long orglen = 0;
		Bitmap bm = null;
		try {
			if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージファイルを開きます. filepath=" + filepath);}

			String path = filepath.substring(0, filepath.lastIndexOf("/") + 1);
			String file = filepath.substring(filepath.lastIndexOf("/") + 1);

			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inJustDecodeBounds = true;

			if (debug) {Log.d("ImageManager", "GetBitmapFromPath: サイズ取得(BitmapFactory)を実行します. pathname=" + filepath);}
			BitmapFactory.decodeFile(filepath, option);
			width = option.outWidth;
			height = option.outHeight;

			if (width > 0 && height > 0) {
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: サイズ取得(BitmapFactory)に成功しました.");}
			} else {
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: サイズ取得(BitmapFactory)に失敗しました.");}
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: WorkStreamを作成します. uri=" + path + ", path=" + file);}
				ws = new WorkStream("", filepath, "", "", false);
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: Fileオブジェクトを作成します. pathname=" + filepath);}
				try {
					fileObj = new File(path + file);
				} catch (Exception e) {
					Log.e("ImageManager", "GetBitmapFromPath: Fileオブジェクトを作成中にエラーが発生しました.");
					if (e != null && e.getMessage() != null) {
						Log.e("ImageManager", "GetBitmapFromPath: エラーメッセージ. " + e.getMessage());
					}
					throw new RuntimeException(e);
				}
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: ファイルサイズを取得します.");}
				try {
					orglen = fileObj.length();
				} catch (Exception e) {
					Log.e("ImageManager", "GetBitmapFromPath: ファイルサイズの取得中にエラーが発生しました.");
					if (e != null && e.getMessage() != null) {
						Log.e("ImageManager", "GetBitmapFromPath: エラーメッセージ. " + e.getMessage());
					}
					throw new RuntimeException(e);
				}
				if (orglen == 0) {
					Log.e("ImageManager", "GetBitmapFromPath: ファイルサイズの取得に失敗しました.");
				}
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: ファイルタイプを取得します.");}
				if (file.lastIndexOf(".") != -1) {
					extType = FileData.getExtType(file.substring(file.lastIndexOf(".")));
				} else {
					Log.e("ImageManager", "GetBitmapFromPath: 拡張子がありません.");
				}
				int[] imagesize = new int[2];
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: サイズ取得(Native)を実行します. type=" + extType + ", orglen=" + orglen);}
				ret = ImageManager.SizeCheckImage(ws, -1, extType, orglen, imagesize);
				if (ret == 0 && imagesize[0] > 0 && imagesize[1] > 0) {
					if (debug) {Log.d("ImageManager", "GetBitmapFromPath: サイズ取得(Native)に成功しました.");}
					width = imagesize[0];
					height = imagesize[1];
				} else {
					Log.e("ImageManager", "GetBitmapFromPath: サイズ取得(Native)に失敗しました.");
					return null;
				}
			}
			if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージファイルのサイズ. width=" + width + ", height=" + height);}

			if (width > 0 && height > 0) {
				// 縮小してファイル読込
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージデータを取得します.");}
				option.inJustDecodeBounds = false;
				option.inPreferredConfig = Bitmap.Config.RGB_565;
				if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージデータ取得(BitmapFactory)を実行します. pathname=" + filepath);}
				try {
					bm = BitmapFactory.decodeFile(filepath, option);
				} catch (Exception e) {
					Log.e("ImageManager", "GetBitmapFromPath: イメージデータ取得(BitmapFactory)でエラーが発生しました.");
					if (e != null && e.getMessage() != null) {
						Log.e("ImageManager", "GetBitmapFromPath: エラーメッセージ. " + e.getMessage());
						return null;
					}
				}
				if (bm != null) {
					if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージデータ取得(BitmapFactory)に成功しました.");}
				} else {
					if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージデータ取得(BitmapFactory)に失敗しました.");}
					ws.seek(0);
					bm = ImageManager.GetBitmapNativeMain(ws, -1, extType, 1, orglen, width, height, bm);
					if (bm != null) {
						if (debug) {Log.d("ImageManager", "GetBitmapFromPath: イメージデータ取得(Native)に成功しました.");}
					} else {
						Log.e("ImageManager", "GetBitmapFromPath: イメージデータ取得(Native)に失敗しました.");
						return null;
					}
				}
				if (bm == null) {
					Log.e("ImageManager", "run: イメージファイルを取得できませんでした.");
					// NoImageであればステータス設定
					return null;
					//CallImgLibrary.ThumbnailSetNone(mID, index);
				} else {
					if (debug) {Log.d("ImageManager", "run: イメージファイルを取得できました.");}
				}
			}
		} catch (Exception e) {
			Log.e("ImageManager", "run: エラーが発生しました.");
			if (e != null && e.getMessage() != null) {
				Log.e("ImageManager", "run: エラーメッセージ. " + e.getMessage());
			}
			throw new RuntimeException(e);
		}
		return bm;
	}

	@SuppressLint("Range")
    private int SizeCheckImage(int page) {
		boolean debug = false;
		if (debug) {Log.d("ImageManager", "SizeCheckImage(1): 開始します. page=" + page + ", " + mFileList[page].name);}
		int returnCode = 0;
		//Log.e("FromStreamSizeCheck", "page=" + page + ", type=" + type);
		int width = -1;
		int height = -1;

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			return -1;
		}
		if (mFileList[page].width > 0) {
			return -2;
		}

//		Log.d("FromStreamSizeCheck", "start");
		if (mFileList[page].o_width == 0) {
			BitmapFactory.Options option = new BitmapFactory.Options();
			boolean fError = false;

			mCheWriteFlag = false;
			option.inJustDecodeBounds = true;
			InputStream inputStream = null;

			try {
				setLoadBitmapStart(page, false);
				if (mFileType == FILETYPE_PDF) {
					//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
					PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
					width = pdfPage.getWidth();
					height = pdfPage.getHeight();
					//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
					pdfPage.close();
				}
				else if (mFileType == FILETYPE_ZIP) {
					// メモリキャッシュ読込時のみZIP展開する
					// ファイルキャッシュを作成するときはZIP展開不要
					ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(this, BIS_BUFFSIZE));
					zipStream.getNextEntry();
					mLoadingPage = page;
					inputStream = new BufferedInputStream(zipStream);
				}
				else if (mFileType == FILETYPE_RAR) {
					// メモリキャッシュ読込時のみRAR展開する
					// ファイルキャッシュを作成するときはRAR展開不要
					inputStream = new RarInputStream(new BufferedInputStream(this, BIS_BUFFSIZE), page, mFileList[page]);
				}
				else {
					inputStream = new BufferedInputStream(this, BIS_BUFFSIZE);
				}

				if (width <= 0 || height <= 0) {
					inputStream.mark(mFileList[page].orglen + 1);
					if (debug) {Log.d("ImageManager", "SizeCheckImage(1): BitmapFactoryでデコードします. " + mFileList[page].name);}
					BitmapFactory.decodeStream(new BufferedInputStream(inputStream), null, option);
					width = option.outWidth;
					height = option.outHeight;
					if (width > 0 && height > 0) {
						if (debug) {Log.d("ImageManager", "SizeCheckImage(1): BitmapFactoryでデコードに成功しました. " + mFileList[page].name);}
					}
					else {
						if (debug) {Log.d("ImageManager", "SizeCheckImage(1): BitmapFactoryでデコードに失敗しました. " + mFileList[page].name);}
						try {
							inputStream.reset();
						} catch (IOException e) {
							if (debug) {Log.e("ImageManager", "SizeCheckImage(1): inputStreamのリセットでエラーになりました. " + mFileList[page].name);}
							inputStream.close();
							return -3;
						}
						int[] imagesize = new int[2];
						returnCode = SizeCheckImage(page, inputStream, imagesize);
						if (returnCode == 0 && imagesize[0] > 0 && imagesize[1] > 0) {
							if (debug) {Log.d("ImageManager", "SizeCheckImage(1): SizeCheckImage(3) に成功しました. " + mFileList[page].name);}
							width = imagesize[0];
							height = imagesize[1];
						} else {
							if (debug) {Log.e("ImageManager", "SizeCheckImage(1): SizeCheckImage(3) に失敗しました. " + mFileList[page].name);}
							return -4;
						}
					}
				}
			}
			catch (IOException e) {
				Log.e("ImageManager", "SizeCheckImage(1): エラーになりました.");
				if (e != null && e.getMessage() != null) {
					Log.e("FileThumbnailLoader", "SizeCheckImage(1): エラーメッセージ. " + e.getMessage());
				}
			}

			if (debug) {Log.d("ImageManager", "SizeCheckImage(1): サイズを取得しました. width=" + width + ", height=" + height);}
			try {
				setLoadBitmapEnd();
			}
			catch (Exception e) {
				Log.e("ImageManager", "SizeCheckImage(1): setLoadBitmapEndでエラーになりました.");
				if (e != null && e.getMessage() != null) {
					Log.e("FileThumbnailLoader", "SizeCheckImage(1): エラーメッセージ. " + e.getMessage());
				}
				fError = true;
			}

			if (fError) {
				return -5;
			}
			// mFileList[page].bmpsize = option.outWidth * option.outHeight * 2;
			mFileList[page].o_width = width;
			mFileList[page].o_height = height;
		}
		mFileList[page].scale = DEF.calcScale(mFileList[page].o_width, mFileList[page].o_height, mFileList[page].type, 3200, 3200);
//		mFileList[page].scale = 1;
		mFileList[page].width = DEF.divRoundUp(mFileList[page].o_width, mFileList[page].scale);
		mFileList[page].height = DEF.divRoundUp(mFileList[page].o_height, mFileList[page].scale);
		if (debug) {Log.d("ImageManager", "SizeCheckImage(1): 終了します. " + mFileList[page].name);}
		return returnCode;
	}

	private int SizeCheckImage(int page, InputStream inputStream, int[] imagesize) {
		boolean debug = false;
		if (debug) {Log.d("ImageManager", "SizeCheckImage(3): 開始します. page=" + page + ", " + mFileList[page].name);}
		int returnCode = 0;

		if (page < -1 || mFileList.length <= page) {
			// 範囲外
			return -6;
		}
		if (mFileList[page].width > 0) {
			return -7;
		}

		if (mFileList[page].o_width == 0) {
			returnCode = SizeCheckImage(inputStream, page, mFileList[page].type, mFileList[page].orglen, imagesize);
			if (returnCode < 0) {
				Log.e("ImageManager", "SizeCheckImage(3): SizeCheckImage(5) の実行に失敗しました.");
				return -8;
			}
			if (debug) {Log.d("ImageManager", "SizeCheckImage(3): SizeCheckImage(5) の実行に成功しました.");}
		}
		if (debug) {Log.d("ImageManager", "SizeCheckImage(3): 終了します. " + mFileList[page].name);}
		return returnCode;
	}

	public static int SizeCheckImage(InputStream inputStream, int page, int type, long orglen, int[] imagesize) {
		boolean debug = false;
		int returnCode = 0;
		if (debug) {Log.d("ImageManager", "SizeCheckImage(5): 開始します. page=" + page + ", type=" + type + ", orglen=" + orglen);}

		// 読み込み準備
		returnCode = CallImgLibrary.ImageSetFileSize(orglen);
		if (returnCode < 0) {
			if (debug) {Log.e("ImageManager", "SizeCheckImage(5): ImageSetFileSize に失敗しました. return=" + returnCode);}
			return -9;
		}
		byte data[] = new byte[100 * 1024];
		int total = 0;
		while (true) {
			int size = 0;
			try {
				size = inputStream.read(data, 0, data.length);
				if (size < 0) {
					break;
				}
			} catch (IOException e) {
				if (total != orglen) {
					Log.e("ImageManager", "SizeCheckImage(5): からのデータ取得でエラーになりました.");
					if (e != null && e.getMessage() != null) {
						Log.e("FileThumbnailLoader", "SizeCheckImage(5): エラーメッセージ. " + e.getMessage());
					}
					return -10;
				} else {
					//必要なデータを読み終えていた場合に抜ける
					break;
				}
			}
			CallImgLibrary.ImageSetData(data, size);
			total += size;
		}
		if (total < orglen) {
			Log.e("ImageManager", "SizeCheckImage(5): InputStream から取得したサイズが足りませんでした. total=" + total + ", orglen=" + orglen);
			return -12;
		} else {
			// 画像のサイズを取得する
			returnCode = CallImgLibrary.ImageGetSize(type, imagesize);
			if (returnCode < 0 && imagesize[0] < 0 && imagesize[1] < 0) {
				Log.e("ImageManager", "SizeCheckImage(5): CallImgLibrary.ImageGetSize() failed.");
				return -13;
			}
		}
		if (debug) {Log.d("ImageManager", "SizeCheckImage(5): End.");}
		return returnCode;
	}

	public Bitmap GetBitmapNative(InputStream inputStream, int page, int scale, Bitmap bm) {
		boolean debug = false;

		int ret = 0;
		int returnCode = 0;

		if (debug) {Log.d("ImageManager", "GetBitmapNative: Start. page=" + page + ", scale=" + scale + ", " + mFileList[page].name);}

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			if (debug) {Log.e("ImageManager", "GetBitmapNative: Page is out of range. " + mFileList[page].name);}
			return null;
		}
		if (mFileList[page].width <= 0 || mFileList[page].height <= 0) {
			Log.e("ImageManager", "GetBitmapNative: Image size is invalid. width=" + mFileList[page].width + "height=" + mFileList[page].height + ", " + mFileList[page].name);
			return null;
		}

		bm = GetBitmapNativeMain(inputStream, page, mFileList[page].type, scale, mFileList[page].orglen, mFileList[page].width, mFileList[page].height, bm);
		if (bm == null) {
			Log.e("ImageManager", "GetBitmapNative: GetBitmapNativeMain() failed. " + mFileList[page].name);
		}
		if (debug) {Log.d("ImageManager", "GetBitmapNative: End. Bitmap=" + bm + ", " + mFileList[page].name);}
		return bm;
	}

	public static Bitmap GetBitmapNativeMain(InputStream inputStream, int page, int type, int scale, long orglen, int width, int height, Bitmap bm) {
		boolean debug = false;

		int ret = 0;
		int returnCode = 0;

		if (debug) {Log.d("ImageManager", "GetBitmapNativeMain: Start. page=" + page + ", type=" + type + ", scale=" + scale);}

		// 読み込み準備
		returnCode = CallImgLibrary.ImageSetFileSize(orglen);
		if (returnCode < 0) {
			Log.e("ImageManager", "GetBitmapNativeMain: ImageSetFileSize failed. return=" + returnCode);
			return null;
		}
		byte data[] = new byte[100 * 1024];
		int total = 0;
		while (true) {
			int size = 0;
			try {
				size = inputStream.read(data, 0, data.length);
				if (size < 0) {
					break;
				}
			} catch (IOException e) {
				if (total != orglen) {
					String msg = null;
					if (e != null) {
						msg = e.getMessage();
					}
					if (msg == null) {
						msg = "";
					}
					Log.e("ImageManager", "GetBitmapNativeMain: Catch IOException: " + msg);
					return null;
				} else {
					//必要なデータを読み終えていた場合に抜ける
					break;
				}
			}
			returnCode = CallImgLibrary.ImageSetData(data, size);
			if (returnCode < 0) {
				Log.e("ImageManager", "GetBitmapNativeMain: ImageSetData failed. return=" + returnCode);
				return null;
			}
			total += size;
		}
		if (total < orglen) {
			Log.e("ImageManager", "GetBitmapNativeMain: bufferd size is short. total=" + total + ", orglen=" + orglen);
			return null;
		} else {
			bm = Bitmap.createBitmap(width, height, Config.RGB_565);
			try {
				if (debug) {Log.d("ImageManager", "GetBitmapNativeMain: CallImgLibrary.ImageGetBitmap start. type=" + type + ", scale=" + scale);}
				ret = CallImgLibrary.ImageGetBitmap(type, scale, bm);
			} catch (Exception e) {
				Log.e("ImageManager", "GetBitmapNativeMain: CallImgLibrary.ImageGetBitmap error.");
				if (e != null && e.getMessage() != null) {
					Log.e("ImageManager", "GetBitmapNativeMain: error message. " + e.getMessage());
					return null;
				}
			}

		//SaveFile(bm);
			if (ret < 0) {
				Log.e("ImageManager", "GetBitmapNativeMain: CallImgLibrary.ImageConvertBitmap() failed. return=" + ret);
				return null;
			}
			if (scale != 1) {
				int Outwidth = width / scale;
				int Outheight = height / scale;
				if (debug) {Log.d("ImageManager", "GetBitmapNativeMain: Bitmap.createScaledBitmap start. width=" + Outwidth + ", height=" + Outheight);}
				bm = Bitmap.createScaledBitmap(bm, Outwidth, Outheight, true);
				if (bm == null) {
					Log.e("ImageManager", "GetBitmapNativeMain: Bitmap.createScaledBitmap failed.");
					return null;
				}
			}
		}
		if (bm != null) {
			if (debug) {Log.d("ImageManager", "GetBitmapNativeMain: End. Bitmap is not null.");}
		}
		if (debug) {Log.d("ImageManager", "GetBitmapNativeMain: End. Bitmap=" + bm);}
		return bm;
	}

	private ImageData LoadImage(int page, boolean notice) throws IOException {
		ImageData id = null;

		if (mMemCacheFlag[page].fSource == true) {
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
			id.SclWidth = 0;// mFileList[page].swidth[half];
			id.SclHeight = 0;// mFileList[page].sheight[half];
			return id;
		}

//		Log.d("LoadImage", "start");

		if (mFileList[page].width <= 0) {
			SizeCheckImage(page);
		}

		if (!memWriteLock(page, 0, false)) {
			// throw new IOException("Memory Lock Error.");
			return null;
		}

		ZipInputStream zipStream = null;
		try {
			setLoadBitmapStart(page, notice);
			if (mFileType == FILETYPE_PDF) {
				id = LoadPdfImageData(page, mPdfRenderer);
			}
			else if (mFileType == FILETYPE_ZIP) {
				// メモリキャッシュ読込時のみZIP展開する
				// ファイルキャッシュを作成するときはZIP展開不要
				zipStream = new ZipInputStream(new BufferedInputStream(this, BIS_BUFFSIZE));
				zipStream.getNextEntry();
				id = LoadImageData(page, zipStream, mFileList[page].orglen);
				// ファイル破損時に無限ループするのでコメント化
				// zipStream.closeEntry();
			}
			else if (mFileType == FILETYPE_RAR) {
				// メモリキャッシュ読込時のみRAR展開する
				// ファイルキャッシュを作成するときはRAR展開不要
				if (mRarStream == null || mRarStream.getLoadPage() != page) {
					mRarStream = new RarInputStream(new BufferedInputStream(this, BIS_BUFFSIZE), page, mFileList[page]);
				}
				else {
					mRarStream.initSeek();
				}
				id = LoadImageData(page, mRarStream, mFileList[page].orglen);
			}
			else {
				id = LoadImageData(page, this, mFileList[page].orglen);
			}
		}
		catch (IOException e) {
			if (e != null) {
				String s = e.getMessage();
				if (s != null) {
					Log.e("LoadImage", "Load: " + s);
				}
			}
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			String str = "";
			if (e.getMessage() != null) {
				str = e.getMessage();
			}
			Log.e("LoadImage", "End: " + str);
		}

//		Log.d("LoadImage", "end");
		// ファイルクローズは不要
		return id;
	}

	public byte[] loadExpandData(String filename) {
		int page = -1;

		// データを探す
		for (int i = 0; i < mFileList.length; i++) {
			if (filename.equals(mFileList[i].name)) {
				page = i;
				break;
			}
		}

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			return null;
		}

		boolean fError = false;
		byte result[] = new byte[mFileList[page].orglen];

		try {
			setLoadBitmapStart(page, false);
			if (mFileType == FILETYPE_ZIP) {
				// メモリキャッシュ読込時のみZIP展開する
				// ファイルキャッシュを作成するときはZIP展開不要
				ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(this, BIS_BUFFSIZE));
				zipStream.getNextEntry();
				CacheInputStream cis = new CacheInputStream(zipStream);
				cis.read(result, 0, result.length);
//				zipStream.closeEntry();
			}
			else if (mFileType == FILETYPE_RAR) {
				// メモリキャッシュ読込時のみRAR展開する
				// ファイルキャッシュを作成するときはRAR展開不要
				mRarStream = new RarInputStream(new BufferedInputStream(this, BIS_BUFFSIZE), page, mFileList[page]);
				mRarStream.read(result, 0, result.length);
			}
			else {
				this.read(result, 0, result.length);
			}
		}
		catch (IOException e) {
			Log.e("Imagemanager", "loadExpandData: " + e.getMessage());
			fError = true;
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Log.e("Imagemanager", "loadExpandData: " + e.getMessage());
			fError = true;
		}

		if (fError) {
			return null;
		}
		return result;
	}

	public void getImageSize(String filename, Point pt) throws IOException {
		int page = -1;
		pt.x = 0;
		pt.y = 0;

		// データを探す
		for (int i = 0; i < mFileList.length; i++) {
			if (filename.equals(mFileList[i].name)) {
				page = i;
				break;
			}
		}

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			return;
		}

		//
		if (mFileList[page].width <= 0) {
			SizeCheckImage(page);
		}

		pt.x = mFileList[page].width;
		pt.y = mFileList[page].height;
		// ファイルクローズは不要
		return;
	}

	// テキスト用
	public Bitmap loadBitmapByName(String filename) throws IOException {
		int page = -1;

		// データを探す
		for (int i = 0; i < mFileList.length; i++) {
			if (filename.equals(mFileList[i].name)) {
				page = i;
				break;
			}
		}

		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			return null;
		}

		BitmapFactory.Options option = new BitmapFactory.Options();
		Bitmap bm = null;

		option.inJustDecodeBounds = false;
		option.inPreferredConfig = Config.RGB_565;
		InputStream inputStream;

		ZipInputStream zipStream = null;
		try {
			setLoadBitmapStart(page, false);
			if (mFileType == FILETYPE_ZIP) {
				// メモリキャッシュ読込時のみZIP展開する
				// ファイルキャッシュを作成するときはZIP展開不要
				zipStream = new ZipInputStream(new BufferedInputStream(this, BIS_BUFFSIZE));
				zipStream.getNextEntry();
				inputStream = new CacheInputStream(zipStream);
				// ファイル破損時に無限ループするのでコメント化
				// zipStream.closeEntry();
			}
			else if (mFileType == FILETYPE_RAR) {
				// メモリキャッシュ読込時のみRAR展開する
				// ファイルキャッシュを作成するときはRAR展開不要
				if (mRarStream == null || mRarStream.getLoadPage() != page) {
					mRarStream = new RarInputStream(new BufferedInputStream(this, BIS_BUFFSIZE), page, mFileList[page]);
				}
				else {
					mRarStream.initSeek();
				}
				inputStream = mRarStream;
			}
			else {
				inputStream = this;
			}
			bm = BitmapFactory.decodeStream(inputStream, null, option);
		}
		catch (IOException e) {
			Log.e("ImageManager","loadBitmapByName: " + e.getMessage());
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Log.e("ImageManager","loadBitmapByName: " + e.getMessage());
		}

		// ファイルクローズは不要
		return bm;
	}

	// サムネイルに近い縮尺を求める
	public Bitmap LoadThumbnail(int page, int width, int height) throws IOException {
		boolean debug = false;
		if (debug) {Log.d("ImageManager", "LoadThumbnail: 開始します. page=" + page + ", width=" + width + ", height=" + height);}
		if (mFileList[page].width <= 0) {
			SizeCheckImage(page);
		}

		int sampleSize = DEF.calcThumbnailScale(mFileList[page].width, mFileList[page].height, width, height);
		Bitmap bm = LoadThumbnailMain(page, sampleSize);

		if (debug) {Log.d("ImageManager", "LoadThumbnail: 終了します.");}
		return bm;
	}

	// サムネイル用に画像読込み
	public Bitmap LoadThumbnailMain(int page, int sampleSize) {
		boolean debug = false;
		BitmapFactory.Options option = new BitmapFactory.Options();
		Bitmap bm = null;

		option.inJustDecodeBounds = false;
		option.inPreferredConfig = Config.RGB_565;
		option.inSampleSize = sampleSize;
		InputStream inputStream = null;

		ZipInputStream zipStream = null;

		if (debug) {Log.d("ImageManager", "LoadThumbnailMain: start. page=" + page + ", sampleSize=" + sampleSize + ", " + mFileList[page].name);}

		try {
			setLoadBitmapStart(page, false);
			if (mFileType == FILETYPE_PDF) {
				if (debug) {Log.d("ImageManager", "LoadThumbnailMain: PDFファイルを開きます.");}
				//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
				PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
				int Outwidth = pdfPage.getWidth() / sampleSize;
				int Outheight = pdfPage.getHeight()/ sampleSize;

				//PdfRenderer.Pageの情報を使って空の描画用Bitmapインスタンスを作成する。
				bm = Bitmap.createBitmap(pdfPage.getWidth() , pdfPage.getHeight() , Bitmap.Config.ARGB_8888);
				// PDFをレンダリングする前にBitmapを白く塗る。
				Canvas canvas = new Canvas(bm);
				canvas.drawColor(Color.WHITE);
				canvas.drawBitmap(bm, 0, 0, null);
				//空のBitmapにPDFの内容を描画する。
				pdfPage.render(bm , null,null , PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
				//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
				pdfPage.close();
				if (bm == null) {
					if (debug) {Log.e("ImageManager", "LoadThumbnailMain: PDFファイルのレンダリングに失敗しました.");}
					return null;
				}
				else {
					if (debug) {Log.d("ImageManager", "LoadThumbnailMain: PDFファイルのレンダリングに成功しました.");}
					if (debug) {Log.d("ImageManager", "LoadThumbnailMain: ビットマップのサイズを変更します. width=" + Outwidth + ", height=" + Outheight);}
					bm = Bitmap.createScaledBitmap(bm, Outwidth, Outheight, true);
					if (bm == null) {
						if (debug) {Log.e("ImageManager", "LoadThumbnailMain: ビットマップのサイズの変更に失敗しました.");}
						return null;
					}
					else {
						if (debug) {Log.d("ImageManager", "LoadThumbnailMain: ビットマップのサイズの変更に成功しました.");}
						return bm;
					}
				}
			}
			else if (mFileType == FILETYPE_ZIP) {
				// メモリキャッシュ読込時のみZIP展開する
				// ファイルキャッシュを作成するときはZIP展開不要
				zipStream = new ZipInputStream(new BufferedInputStream(this, BIS_BUFFSIZE));
				zipStream.getNextEntry();
				inputStream = new BufferedInputStream(zipStream);
				// ファイル破損時に無限ループするのでコメント化
				// zipStream.closeEntry();
			}
			else if (mFileType == FILETYPE_RAR) {
				// メモリキャッシュ読込時のみRAR展開する
				// ファイルキャッシュを作成するときはRAR展開不要
				if (mRarStream == null || mRarStream.getLoadPage() != page) {
					mRarStream = new RarInputStream(new BufferedInputStream(this, BIS_BUFFSIZE), page, mFileList[page]);
				}
				else {
					mRarStream.initSeek();
				}
				inputStream = new BufferedInputStream(mRarStream);
			}
			else {
				inputStream = new BufferedInputStream(this);
			}
			inputStream.mark(mFileList[page].orglen + 1);
			bm = BitmapFactory.decodeStream(new BufferedInputStream(inputStream), null, option);
			if (bm != null) {
				// なにもしない
				if (debug) {Log.d("ImageManager", "LoadThumbnailMain: BitmapFactory decode succeed. " + mFileList[page].name);}
			}
			else {
				if (debug) {Log.d("ImageManager", "LoadThumbnailMain: BitmapFactory decode failed. " + mFileList[page].name);}
				try {
					inputStream.reset();
				} catch (IOException e) {
					Log.e("ImageManager", "LoadThumbnailMain: inputStream.reset() failed. " + mFileList[page].name);
					inputStream.close();
					return null;
				}
				bm = GetBitmapNative(inputStream, page, sampleSize, bm);
				if (bm == null) {
					Log.e("ImageManager", "LoadThumbnailMain: GetBitmapNative failed. " + mFileList[page].name);
					return null;
				}
				if (debug) {Log.d("ImageManager", "LoadThumbnailMain: GetBitmapNative succeed. " + mFileList[page].name);}
			}
		}
		catch (IOException e) {
			Log.e("ImageManager", "LoadThumbnailMain: Catch IOException: " + e.getMessage());
		}

		try {
			setLoadBitmapEnd();
		}
		catch (Exception e) {
			Log.e("ImageManager", "LoadThumbnailMain: Catch Exception: " + e.getMessage());
		}

		if (bm.getConfig() != Config.RGB_565) {
			bm = bm.copy(Config.RGB_565, true);
		}
		if (debug) {Log.d("ImageManager", "LoadThumbnailMain: end. " + mFileList[page].name);}
		// ファイルクローズは不要
		return bm;
	}

	private ImageData LoadPdfImageData(int page, PdfRenderer mPdfRenderer) {
		int ret = 0;
		ImageData id = null;
		//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
		PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
		//PdfRenderer.Pageの情報を使って空の描画用Bitmapインスタンスを作成する。
		Bitmap bm = Bitmap.createBitmap(pdfPage.getWidth() , pdfPage.getHeight() , Bitmap.Config.ARGB_8888);
		// PDFをレンダリングする前にBitmapを白く塗る。
		Canvas canvas = new Canvas(bm);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(bm, 0, 0, null);
		//空のBitmapにPDFの内容を描画する。
		pdfPage.render(bm , null,null , PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
		//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
		pdfPage.close();
		if (bm != null) {
			Log.d("ImageManager", "LoadPdfImageData: BitmapFactory decode succeed. " + mFileList[page].name);
			ret = CallImgLibrary.ImageSetPage(page, 0);
			if (ret < 0) {
				Log.e("ImageManager", "LoadPdfImageData: CallImgLibrary.ImageSetPage failed at BitmapFactory. return=" + ret + ", " + mFileList[page].name);
				return null;
			}
			if (mFileList[page].scale != 1) {
				int Outwidth = mFileList[page].width / mFileList[page].scale;
				int Outheight = mFileList[page].height / mFileList[page].scale;
				Log.d("ImageManager", "LoadPdfImageData: Bitmap.createScaledBitmap start. width=" + Outwidth + ", height=" + Outheight);
				bm = Bitmap.createScaledBitmap(bm, Outwidth, Outheight, true);
				if (bm == null) {
					Log.e("ImageManager", "LoadPdfImageData: Bitmap.createScaledBitmap failed.");
					return null;
				}
			}

			Log.d("ImageManager", "LoadPdfImageData: CallImgLibrary.ImageSetPage succeed. " + mFileList[page].name);
			ret = CallImgLibrary.ImageSetBitmap(bm);
			if (ret < 0) {
				Log.e("ImageManager", "LoadPdfImageData: ImageSetBitmap failed. return=" +ret + ", " + mFileList[page].name);
				return null;
			}
			Log.d("ImageManager", "LoadPdfImageData: CallImgLibrary.ImageSetBitmap succeed. " + mFileList[page].name);
			bm.recycle();
			// 読み込み成功
			mMemCacheFlag[page].fSource = true;
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
		}

		return id;
	}

	// ビュアー表示用の画像読込
	private ImageData LoadImageData(int page, InputStream is, int orglen) throws IOException {
		boolean debug = false;
		int ret = 0;
		InputStream inputStream = new BufferedInputStream(is);

		BitmapFactory.Options option = new BitmapFactory.Options();
		Bitmap bm = null;
		option.inJustDecodeBounds = false;
		option.inPreferredConfig = Config.RGB_565;
		option.inSampleSize = mFileList[page].scale;

		ImageData id = null;

		if (debug) {Log.d("ImageManager", "LoadImageData: Start. " + mFileList[page].name);}

		inputStream.mark(orglen + 1);
		bm = BitmapFactory.decodeStream(new BufferedInputStream(inputStream), null, option);
		if (bm != null) {
			if (debug) {Log.d("ImageManager", "LoadImageData: BitmapFactory decode succeed. " + mFileList[page].name);}
			ret = CallImgLibrary.ImageSetPage(page, 0);
			if (ret < 0) {
				if (debug) {Log.e("ImageManager", "LoadImageData: CallImgLibrary.ImageSetPage failed at BitmapFactory. return=" + ret + ", " + mFileList[page].name);}
				return null;
			}
			if (debug) {Log.d("ImageManager", "LoadImageData: CallImgLibrary.ImageSetPage succeed. " + mFileList[page].name);}
			ret = CallImgLibrary.ImageSetBitmap(bm);
			if (ret < 0) {
				if (debug) {Log.e("ImageManager", "LoadImageData: ImageSetBitmap failed. return=" +ret + ", " + mFileList[page].name);}
				return null;
			}
			if (debug) {Log.d("ImageManager", "LoadImageData: CallImgLibrary.ImageSetBitmap succeed. " + mFileList[page].name);}
			bm.recycle();
			// 読み込み成功
			mMemCacheFlag[page].fSource = true;
			id = new ImageData();
			id.Page = page;
			id.Width = mFileList[page].width;
			id.Height = mFileList[page].height;
		}
		else {
			if (debug) {Log.d("ImageManager", "LoadImageData: BitmapFactory decode failed. " + mFileList[page].name);}
			try {
				inputStream.reset();
			} catch (IOException e) {
				Log.e("ImageManager", "LoadImageData: inputStream.reset() failed. " + mFileList[page].name);
				inputStream.close();
				return null;
			}

			// 読み込み準備
			ret = CallImgLibrary.ImageSetPage(page, orglen);
			if (ret < 0) {
				Log.e("ImageManager", "LoadImageData: ImageSetPage failed at ImageConvert. return=" + ret + ", " + mFileList[page].name);
				return null;
			}

//			Log.d("LoadImageData", "start : page=" + page);
			byte data[] = new byte[100 * 1024];
			int total = 0;
			while (true) {
				int size = 0;
				try {
					size = inputStream.read(data, 0, data.length);
					if (size <= 0) {
						break;
					}
				}
				catch (IOException e) {
					if(total != orglen) {
						Log.e("ImageManager", "LoadImageData: catch IOException. " + mFileList[page].name);
						throw new IOException("Can't read file.");
					}
					else {
						//必要なデータを読み終えていた場合に抜ける
						break;
					}
				}
				// メモリセットも中断する
				if (mCacheBreak == true || mRunningFlag == false) {
					Log.e("ImageManager", "LoadImageData: User Canceled in LoadImageData. " + mFileList[page].name);
					// throw new IOException("User Canceled in LoadImageData.");
					return null;
				}
				CallImgLibrary.ImageSetData(data, size);
				total += size;
			}

			//		long sttime = SystemClock.uptimeMillis();
			if (total < orglen) {
				Log.e("ImageManager", "LoadImageData: bufferd size is short. total=" + total + ", orglen=" + orglen + ", " + mFileList[page].name);
				mFileList[page].error = true;
				throw new IOException("File is broken.");
			} else {
				int returnCode = 0;
				// 画像を取得してバッファに格納する
				returnCode = CallImgLibrary.ImageConvert(mFileList[page].type, mFileList[page].scale);
				if (returnCode >= 0 && mFileList[page].width > 0 && mFileList[page].height > 0) {
					if (debug) {Log.d("ImageManager", "LoadImageData: CallImgLibrary.ImageConvert succeed. " + mFileList[page].name);}
					// 読み込み成功
					mMemCacheFlag[page].fSource = true;
					id = new ImageData();
					id.Page = page;
					id.Width = mFileList[page].width;
					id.Height = mFileList[page].height;
				} else {
					Log.e("ImageManager", "LoadImageData: CallImgLibrary.ImageConvert failed. return=" + returnCode + ", " + mFileList[page].name);
					mFileList[page].error = true;
				}
			}
		}
//		Log.i("ImageManager", "LoadImageData time : " + (int)(SystemClock.uptimeMillis() - sttime));
		if (debug) {Log.d("ImageManager", "LoadImageData: End. " + mFileList[page].name);}
		return id;
	}

	private int mScrWidth;
	private int mScrHeight;
	private int mScrCenter;
	private int mScrScaleMode;
	private int mScrDispMode;
	private int mScrAlgoMode;
	private int mScrRotate;
	private int mScrScale; // 任意倍率
	private int mScrWAdjust; // 幅調整縮小
	private int mScrWidthScale; // 幅調整
	private int mScrImgScale; // 拡大
	private int mMarginCut; // 余白削除
	private int mMarginCutColor; // 余白削除の色
	private int mSharpen;	// シャープ化
	private int mInvert;	// カラー反転
	private int mGray;		// グレースケール
	private int mMoire;		// モアレ軽減
	private int mTopSingle;	// 先頭単ページ
	private int mGamma;		// ガンマ補正
	private int mBright;	// 明るさ

	private boolean mScrFitDual;
	private boolean mScrNoExpand;

	// 画面変更
	public void setViewSize(int width, int height) {
		mScrWidth = width;
		mScrHeight = height;

		CallImgLibrary.ImageScaleFree(-1, -1);
		if (mFileList != null && mMemCacheFlag != null) {
			for (int i = 0; i < mFileList.length; i++) {
				if (mMemCacheFlag[i] != null && (mMemCacheFlag[i].fScale[0] || mMemCacheFlag[i].fScale[1] || mMemCacheFlag[i].fScale[2])) {
					// 要チェックにする
					mMemCacheFlag[i].fScale[0] = false;
					mMemCacheFlag[i].fScale[1] = false;
					mMemCacheFlag[i].fScale[2] = false;
				}
			}
		}
	}

	// 設定変更
	public void setConfig(int mode, int center, boolean fFitDual, int dispMode, boolean noExpand, int algoMode, int rotate, int wadjust, int wscale, int scale, int pageway, int mgncut, int mgncutcolor, int quality, int bright, int gamma, int sharpen, boolean invert, boolean gray, boolean pseland, boolean moire, boolean topsingle, boolean scaleinit) {
		Log.d("ImageManager", "setConfig wscale=" + wscale + ", scale=" + scale);
		mScrScaleMode = mode;
		if (scaleinit) {
			mScrScale = scale;	// 初期化
		}
		mScrCenter = center;
		mScrFitDual = fFitDual;
		mScrDispMode = dispMode;
		mScrNoExpand = noExpand;
		mScrAlgoMode = algoMode;
		mScrRotate = rotate;
		mScrWAdjust = wadjust;
		mScrWidthScale = wscale;
		mScrImgScale = scale;
		mPageWay = pageway;
		mMarginCut = mgncut;
		mMarginCutColor = mgncutcolor;
		mBright = bright;
		mGamma = gamma;
		mSharpen = sharpen;
		mInvert = invert ? 1 : 0;
		mGray = gray ? 1 : 0;
		mQuality = quality;
		mPseLand = pseland;
		mMoire = moire ? 1 : 0;
		mTopSingle = topsingle ? 1 : 0;

		freeScaleCache();
	}

	public void setImageScale(int scale) {
		mScrScale = scale;
//		mScrScaleMode = DEF.SCALE_PINCH;

		freeScaleCache();
		return;
	}

	private void freeScaleCache() {
		mCacheBreak = true;
		CallImgLibrary.ImageCancel(1);
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(0);
				CallImgLibrary.ImageScaleFree(-1, -1);
				if (mFileList != null && mMemCacheFlag != null) {
					for (int i = 0; i < mFileList.length; i++) {
						if (mCloseFlag) {
							break;
						}
						if (mMemCacheFlag[i].fScale[0] || mMemCacheFlag[i].fScale[1] || mMemCacheFlag[i].fScale[2]) {
							// 要チェックにする
							mMemCacheFlag[i].fScale[0] = false;
							mMemCacheFlag[i].fScale[1] = false;
							mMemCacheFlag[i].fScale[2] = false;
						}
					}
				}
			}
		}
	}

	public boolean ImageScalingSync(int page1, int page2, int half1, int half2, ImageData img1, ImageData img2) {
		boolean ret = false;
		mCacheBreak = true;
		CallImgLibrary.ImageCancel(1);
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(0);
				ret = ImageScaling(page1, page2, half1, half2, img1, img2);
			}
		}
		return ret;
	}

	/**
	 * イメージを並べて作成
	 */
	public boolean ImageScaling(int page1, int page2, int half1, int half2, ImageData img1, ImageData img2) {
		boolean debug = false;
		// boolean fDual = false;
//		if (mScrDual && page < mFileList.length - 1) {
//			// 並べるモード && 最終ページではない
//			if ((mFileList[page].width < mFileList[page].height) &&
//				(mFileList[page + 1].width < mFileList[page + 1].height)) {
//				 // 2画像とも縦長
//				fDual = true;
//			}
//		}

//		Log.d("ImageScaling", "start : p1=" + page1 + ", p2=" + page2 + ", h1=" + half1 + ", h2=" + half2);

		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", ■■■■■ ■■■■■ 開始 ■■■■■ ■■■■■ ");}

		int src_x[] = { 0, 0 }; // 映像オリジナルサイズ
		int src_y[] = { 0, 0 };
		int adj_x[] = { 0, 0 }; // 映像拡大縮小後サイズ
		int adj_y[] = { 0, 0 };
		int view_x; // 1～2画像のまとめたサイズ
		int view_y;
		int disp_x = mScrWidth; // 画面の横サイズ
		int disp_x2 = disp_x - mScrCenter; // 2ページ目の横サイズ
		int disp_y = mScrHeight; // 画面の縦サイズ
		boolean fWidth;
		int pseland = mPseLand ? 1 : 0;

		int size[] = {0, 0}; // 画像の完成サイズの戻り値
		int margin[] = { 0, 0, 0, 0 }; // 余白サイズの戻り値, 左, 右, 上, 下
		int left[] = {0, 0};
		int right[] = {0, 0};
		int top[] = {0, 0};
		int bottom[] = {0, 0};

		// 画面サイズ
		disp_x = mScrWidth;
		disp_y = mScrHeight;
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 画面サイズ disp_x=" + disp_x + ", disp_y=" + disp_y);}

		// 画像1の情報
		if (mScrRotate == ROTATE_NORMAL || mScrRotate == ROTATE_180DEG) {
			src_x[0] = mFileList[page1].width;
			src_y[0] = mFileList[page1].height;
		}
		else {
			src_x[0] = mFileList[page1].height;
			src_y[0] = mFileList[page1].width;
		}
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 元画像:P1 src_x1=" + src_x[0] + ", src_y1=" + src_y[0]);}

		if (mMarginCut != 0) {
			// 余白カットありの場合
			// 余白のサイズを計測
			if (CallImgLibrary.GetMarginSize(page1, half1, 0, disp_x, disp_y, mMarginCut, mMarginCutColor, margin) > 0) {
				left[0] = margin[0];
				right[0] = margin[1];
				top[0] = margin[2];
				bottom[0] = margin[3];
			}
		}
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", マージン:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);}

		if (page2 != -1) {
			// 画像2の情報
			if (mScrRotate == ROTATE_NORMAL || mScrRotate == ROTATE_180DEG) {
				src_x[1] = mFileList[page2].width;
				src_y[1] = mFileList[page2].height;
			} else {
				src_x[1] = mFileList[page2].height;
				src_y[1] = mFileList[page2].width;
			}
			if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half2 + ", 元画像:P2 src_x2=" + src_x[1] + ", src_y2=" + src_y[1]);}

			if (mMarginCut != 0) {
				// 余白カットありの場合
				// 余白のサイズを計測
				if (CallImgLibrary.GetMarginSize(page2, half2, 0, disp_x2, disp_y, mMarginCut, mMarginCutColor, margin) > 0) {
					left[1] = margin[0];
					right[1] = margin[1];
					top[1] = margin[2];
					bottom[1] = margin[3];
				}
			}
			CallImgLibrary.GetMarginSize(page2, half2, 0, disp_x2, disp_y, mMarginCut, mMarginCutColor, margin);
			if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half1 + ", マージン:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);}
		}

		// カットしてサイズがマイナスになったらプラスに戻す
		if (src_y[0] - top[0] - bottom[0] <= 20) {
			top[0] = (src_y[0] / 2) - 10;
			bottom[0] = (src_y[0] / 2) - 10;
		}
		if (src_x[0] - left[0] - right[0] <= 20) {
			left[0] = (src_x[0] / 2) - 10;
			right[0] = (src_x[0] / 2) - 10;
		}
		if (page2 != -1) {
			if (src_y[1] - top[1] - bottom[1] <= 20) {
				top[1] = (src_y[1] / 2) - 10;
				bottom[1] = (src_y[1] / 2) - 10;
			}
			if (src_x[1] - left[1] - right[1] <= 20) {
				left[1] = (src_x[1] / 2) - 10;
				right[1] = (src_x[1] / 2) - 10;
			}
		}

		if (mMarginCut != 0 && mMarginCut != 6) {
			// 余白カットありで縦横比を維持の場合

			if (page2 != -1) {
				if (mScrFitDual) {
					// 高さを揃える場合
					// 上下のカット率を少ないほうに合わせる
					if (top[0] * 1000 / src_y[0] > top[1] * 1000 / src_y[1]) {
						top[0] = top[1] * src_y[0] / src_y[1];
					} else {
						top[1] = top[0] * src_y[1] / src_y[1];
					}
					if (bottom[0] * 1000 / src_y[0] > bottom[1] * 1000 / src_y[1]) {
						bottom[0] = bottom[1] * src_y[0] / src_y[1];
					} else {
						bottom[1] = bottom[0] * src_y[1] / src_y[0];
					}

					if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 上下を揃える:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);}
					if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half1 + ", 上下を揃える:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);}
				}
			}
				
			// 横幅が画面の縦横比より細い場合、横のカットを戻す
			int work_x = (disp_x > disp_y) ? disp_x / 2 : disp_x;
			if (left[0] + right[0] > 0) {
				int x = (src_x[0] - left[0] - right[0]);
				int y = (src_y[0] - top[0] - bottom[0]);
				if (x * 1000 / work_x < y * 1000 / disp_y) {
					int margin_x = (int) ((float) src_x[0] - ((float) y * ((float) work_x / (float) disp_y)));
					margin_x = Math.max(0, margin_x);
					left[0] = margin_x * left[0] / (left[0] + right[0]);
					right[0] = margin_x - left[0];
				}
			}
			if (page2 != -1) {
				if (left[1] + right[1] > 0) {
					int x = (src_x[1] - left[1] - right[1]);
					int y = (src_y[1] - top[1] - bottom[1]);
					if (x * 1000 / work_x < y * 1000 / disp_y) {
						int margin_x = (int) ((float) src_x[1] - ((float) y * ((float) work_x / (float) disp_y)));
						margin_x = Math.max(0, margin_x);
						left[1] = margin_x * left[1] / (left[1] + right[1]);
						right[1] = margin_x - left[1];
					}
				}
			}

			// 横幅が画面の縦横比より太い場合、縦のカットを戻す
			if (top[0] + bottom[0] > 0) {
				int x = (src_x[0] - left[0] - right[0]);
				int y = (src_y[0] - top[0] - bottom[0]);
				if (x * 1000 / work_x > y * 1000 / disp_y) {
					int margin_y = (int) ((float) src_y[0] - ((float) x * ((float) disp_y / (float) work_x)));
					margin_y = Math.max(0, margin_y);
					top[0] = margin_y * top[0] / (top[0] + bottom[0]);
					bottom[0] = margin_y - top[0];
				}
			}
			if (page2 != -1) {
				if (top[1] + bottom[1] > 0) {
					int x = (src_x[1] - left[1] - right[1]);
					int y = (src_y[1] - top[1] - bottom[1]);
					if (x * 1000 / work_x > y * 1000 / disp_y) {
						int margin_y = (int) ((float) src_y[1] - ((float) x * ((float) disp_y / (float) work_x)));
						margin_y = Math.max(0, margin_y);
						top[1] = margin_y * top[1] / (top[1] + bottom[1]);
						bottom[1] = margin_y - top[1];
					}
				}
			}

			if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 画面の比率に近づける:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);}
			if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half1 + ", 画面に比率に近づける:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);}

			if (page2 != -1) {
				// 左右の画像の縦横比を揃える
				int x0 = src_x[0] - left[0] - right[0];
				int x1 = src_x[1] - left[1] - right[1];
				int y0 = src_y[0] - top[0] - bottom[0];
				int y1 = src_y[1] - top[1] - bottom[1];
				if (x0 * 1000 / y0 > x1 * 1000 / y1) {
					int width = x0 * y1 / y0;
					if (src_x[1] > width) {
						if (left[1] + right[1] != 0) {
							left[1] = (src_x[1] - width) * left[1] / (left[1] + right[1]);
							right[1] = (src_x[1] - width) - left[1];
						}
					} else {
						left[1] = 0;
						right[1] = 0;
					}
					if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 左右を揃える:P1 width=" + width + ", src_x=" + src_x[1] + ", 左=" + left[1] + ", 右=" + right[1]);}
				} else {
					int width = x1 * y0 / y1;
					if (src_x[0] > width) {
						if (left[0] + right[0] != 0) {
							left[0] = (src_x[0] - width) * left[0] / (left[0] + right[0]);
							right[0] = (src_x[0] - width) - left[0];
						}
					} else {
						left[0] = 0;
						right[0] = 0;
					}
					if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 左右を揃える:P1 width=" + width + ", src_x=" + src_x[0] + ", 左=" + left[0] + ", 右=" + right[0]);}
				}

				if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 左右を揃える:P1 左=" + left[0] + ", 右=" + right[0] + ", 上=" + top[0] + ", 下=" + bottom[0]);}
				if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half1 + ", 左右を揃える:P2 左=" + left[1] + ", 右=" + right[1] + ", 上=" + top[1] + ", 下=" + bottom[1]);}
			}
			
			// 画像が横長なら左右のカットを同じにする
			if (src_x[0] > src_y[0]) {
				left[0] = Math.min(left[0], right[0]);
				right[0] = left[0];
			}
			

			// 元画像の縦横比をカット後の値にする
			src_x[0] = (src_x[0] - left[0] - right[0]);
			src_y[0] = (src_y[0] - top[0] - bottom[0]);
			src_x[1] = (src_x[1] - left[1] - right[1]);
			src_y[1] = (src_y[1] - top[1] - bottom[1]);

		}

		if (img1 != null) {
			img1.CutLeft = left[0];
			img1.CutRight = right[0];
			img1.CutTop = top[0];
			img1.CutBottom = bottom[0];
		}
		if (img2 != null) {
			img2.CutLeft = left[1];
			img2.CutRight = right[1];
			img2.CutTop = top[1];
			img2.CutBottom = bottom[1];
		}


		if(mMarginCut == 6) {
			// 余白削除モードが縦横比無視の場合
			// 元画像の縦横比を画面サイズにする
			src_x[0] = disp_x;
			src_y[0] = disp_y;
			src_x[1] = disp_x2;
			src_y[1] = disp_y;

		}

		// 画面縦横比調整
		if (mScrWAdjust != 100) {
			if (disp_x > disp_y) {
				// 横持ちの時
				src_x[0] = src_x[0] * 100 / mScrWAdjust;
			}
			else {
				// 縦持ちの時
				src_x[0] = src_x[0] * mScrWAdjust / 100;
			}
		}
		// 画像幅調整
		if (mScrWidthScale != 100) {
			src_x[0] = src_x[0] * mScrWidthScale / 100;
		}

		if (half1 != 0) {
			// 半分にする
			src_x[0] = (src_x[0] + 1) / 2;
		}
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", アスペクト比調整:P1 src_x1=" + src_x[0] + ", src_y1=" + src_y[0]);}
		adj_x[0] = src_x[0];
		adj_y[0] = src_y[0];


		if (page2 != -1) {
			// 画面縦横比調整
			if (mScrWAdjust != 100) {
				if (disp_x2 > disp_y) {
					// 横持ちの時
					src_x[1] = src_x[1] * 100 / mScrWAdjust;
				} else {
					// 縦持ちの時
					src_x[1] = src_x[1] * mScrWAdjust / 100;
				}
			}
			// 画像幅調整
			if (mScrWidthScale != 100) {
				src_x[1] = src_x[1] * mScrWidthScale / 100;
			}

			if (half2 != 0) {
				// 半分にする
				src_x[1] = (src_x[1] + 1) / 2;
			}
			if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", アスペクト比調整:P1 src_x2=" + src_x[1] + ", src_y2=" + src_y[1]);}
			adj_x[1] = src_x[1];
			adj_y[1] = src_y[1];
		}

		// 高さを揃える必要がある
		if (mScrFitDual) {
			// 拡大あり
			if (src_y[0] > src_y[1] && src_y[1] != 0) {
				adj_x[1] = src_x[1] * src_y[0] / src_y[1];
				adj_y[1] = src_y[0];
			}
			else if (src_y[0] < src_y[1] && src_y[0] != 0) {
				adj_x[0] = src_x[0] * src_y[1] / src_y[0];
				adj_y[0] = src_y[1];
			}
		}


		// 1～2映像を足したサイズ
		int src_cx = adj_x[0] + adj_x[1];
		int src_cy = adj_y[0] > adj_y[1] ? adj_y[0] : adj_y[1];
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 左右サイズ揃えP1 adj_x=" + adj_x[0] + ", adj_y=" + adj_y[0]);}
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half2 + ", 左右サイズ揃えP2 adj_x=" + adj_x[1] + ", adj_y=" + adj_y[1]);}
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ",左右サイズ合計 src_cx=" + src_cx + ", src_cy=" + src_cy);}

		// サイズ0だと0除算なので終了
		if (src_cx == 0 || src_cy == 0) {
			return false;
		}

		if (mScrScaleMode == DEF.SCALE_ORIGINAL) {
			// 元サイズのまま
			view_x = src_cx;
			view_y = src_cy;
		}
		else if (mScrScaleMode == DEF.SCALE_FIT_ALLMAX) {
			// 縦横比無視で拡大
			view_x = disp_x;
			view_y = disp_y;
		}
		else if (mScrScaleMode == DEF.SCALE_FIT_SPRMAX) {
			// 縦横比無視で拡大（見開き対応）
			if (DEF.checkPortrait(disp_x, disp_y) == true) {
				// 縦画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x : disp_x * 2;
			}
			else {
				// 横画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x / 2 : disp_x;
			}
			view_y = disp_y;
		}
		else if (mScrScaleMode == DEF.SCALE_FIT_WIDTH2) {
			// 幅基準（見開き対応）
			if (DEF.checkPortrait(disp_x, disp_y) == true) {
				// 縦画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x : disp_x * 2;
			}
			else {
				// 横画面
				view_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x / 2 : disp_x;
			}
			view_y = src_cy * view_x / src_cx;
		}
		else if (mScrScaleMode == DEF.SCALE_FIT_ALL2) {
			// 全体表示（見開き対応）
			int dispwk_x;
			if (DEF.checkPortrait(disp_x, disp_y) == true) {
				// 縦画面
				dispwk_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x : disp_x * 2;
			}
			else {
				// 横画面
				dispwk_x = DEF.checkPortrait(src_cx, src_cy) ? disp_x / 2 : disp_x;
			}

			if (dispwk_x * 1000 / src_cx < disp_y * 1000 / src_cy) {
				// Y方向よりもX方向の方が拡大率が小さく画面いっぱいになる
				// 幅基準
				view_x = dispwk_x;
				view_y = src_cy * dispwk_x / src_cx;
			}
			else {
				// 高さ基準
				view_x = src_cx * disp_y / src_cy;
				view_y = disp_y;
			}
		}
		else {
			if (mScrScaleMode == DEF.SCALE_FIT_ALL) {
				if (disp_x * 1000 / src_cx < disp_y * 1000 / src_cy) {
					// Y方向よりもX方向の方が拡大率が小さく画面いっぱいになる
					fWidth = true;
				}
				else {
					// その逆
					fWidth = false;
				}
			}
			else if (mScrScaleMode == DEF.SCALE_FIT_WIDTH) {
				// 幅にあわせる
				fWidth = true;
			}
			else {
				// 高さにあわせる
				fWidth = false;
			}

			if (fWidth) {
				// 幅基準
				view_x = disp_x;
				view_y = src_cy * disp_x / src_cx;
			}
			else {
				// 高さ基準
				view_x = src_cx * disp_y / src_cy;
				view_y = disp_y;
			}
		}

		if (view_x > src_cx && mScrNoExpand) {
			// 拡大かつ拡大しないモードのときは元サイズのまま
			view_x = src_cx;
			view_y = src_cy;
		}

		int width[] = new int[2];
		int height[] = new int[2];
		int fitwidth[] = new int[2];
		int fitheight[] = new int[2];

		// サイズ算出 & リサイズ
		width[0] = view_x * adj_x[0] / (adj_x[0] + adj_x[1]);
		height[0] = view_y * adj_y[0] / src_cy;

		if (page2 >= 0) {
			width[1] = view_x - width[0];
			height[1] = view_y * adj_y[1] / src_cy;
		}

		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 表示方法を反映 view_x=" + view_x + ", view_y=" + view_y);}
		// 拡大しすぎの時は抑える
		int limit = Math.max(mScrWidth, mScrHeight) * 3;
		for (int i = 0 ; i < 2 ; i ++) {
    		if (mScrScaleMode == DEF.SCALE_FIT_HEIGHT) {
    			if (width[i] > limit) {
    				// 高さに合わせる場合の幅は画面の長辺の2倍まで
    				height[i] = height[i] * limit / width[i] ;
    				width[i] = limit;
    			}
    		}
    		if (mScrScaleMode == DEF.SCALE_FIT_WIDTH) {
    			if (height[i] > limit) {
    				// 幅さに合わせる場合の高さ画面の長辺の2倍まで
    				width[i] = width[i] * limit / height[i];
    				height[i] = limit;
    			}
    		}
    		if (page2 < 0) {
    			// 2ページ目はない
    			break;
    		}
		}

		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 指定サイズP1 width=" + width[0] + ", height=" + height[0]);}
		if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half2 + ", 指定サイズP2 width=" + width[1] + ", height=" + height[1]);}

		// 任意スケールの設定前に100%状態のサイズを保持
		fitwidth[0] = width[0];
		fitheight[0] = height[0];
		fitwidth[1] = width[1];
		fitheight[1] = height[1];

		mFileList[page1].fwidth[half1] = fitwidth[0];
		mFileList[page1].fheight[half1] = fitheight[0];
		if (page2 != -1) {
			mFileList[page2].fwidth[half1] = fitwidth[1];
			mFileList[page2].fheight[half1] = fitheight[1];
		}
		// 任意スケールは結果に対して設定
		width[0] = width[0] * mScrScale / 100;
		height[0] = height[0] * mScrScale / 100;

		if (page2 >= 0) {
			width[1] = width[1] * mScrScale / 100;
			height[1] = height[1] * mScrScale / 100;
		}

		if (page1 >= 0 && mMemCacheFlag[page1].fSource && width[0] > 0 && height[0] > 0) {
			if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", ソース読み込み済み");}
			if (mFileList[page1].swidth[half1] == width[0] && mFileList[page1].sheight[half1] == height[0] && mMemCacheFlag[page1].fScale[half1]) {
				if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 画像作成済み");}
				if (img1 != null) {
					img1.SclWidth = width[0];
					img1.SclHeight = height[0];
					img1.FitWidth = fitwidth[0];
					img1.FitHeight = fitheight[0];
				}
			}
			else {
				if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 画像作成開始");}
				mFileList[page1].swidth[half1] = width[0];
				mFileList[page1].sheight[half1] = height[0];
				if (memWriteLock(page1, half1, true)) {
					// スケール作成
					sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
//					long sttime = SystemClock.uptimeMillis();
					int param = CallImgLibrary.ImageScaleParam(mInvert, mGray, 0, mMoire, pseland);
					if (CallImgLibrary.ImageScale(page1, half1, width[0], height[0], left[0], right[0], top[0], bottom[0], mScrAlgoMode, mScrRotate, mMarginCut, mMarginCutColor, mSharpen, mBright, mGamma, param, size) >= 0) {
						if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1 + ", 完成サイズP1 size_w=" + size[0] + ", size_h=" + size[1]);}
						mMemCacheFlag[page1].fScale[half1] = true;
						if (img1 != null) {
							img1.SclWidth = width[0];
							img1.SclHeight = height[0];
							img1.FitWidth = fitwidth[0];
							img1.FitHeight = fitheight[0];
						}
					}
					else {
						mFileList[page1].swidth[half1] = 0;
						mFileList[page1].sheight[half1] = 0;
					}
//					Log.i("jpeg-scaling", "time : " + (int)(SystemClock.uptimeMillis() - sttime));
					sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
				}
			}
			if (img1 != null) {
				if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page1 + ", Half=" + half1
						+ ", Width=" + img1.Width + ", Height=" + img1.Height
						+ ", FitWidth=" + img1.FitWidth + ", FitHeight=" + img1.FitHeight
						+ ", SclWidth=" + img1.SclWidth + ", SclHeight=" + img1.SclHeight);}
			}
		}

		if (page2 >= 0 && mMemCacheFlag[page2].fSource && width[1] > 0 && height[1] > 0) {
			// 見開き時
			if (mFileList[page2].swidth[half2] == width[1] && mFileList[page2].sheight[half2] == height[1] && mMemCacheFlag[page2].fScale[half2]) {
				if (img2 != null) {
					img2.SclWidth = width[1];
					img2.SclHeight = height[1];
					img2.FitWidth = fitwidth[1];
					img2.FitHeight = fitheight[1];
				}
			}
			else {
				mFileList[page2].swidth[half2] = width[1];
				mFileList[page2].sheight[half2] = height[1];
				if (memWriteLock(page2, half2, true)) {
					// スケール作成
					sendMessage(mHandler, DEF.HMSG_CACHE, 0, 2, null);
//					long sttime = SystemClock.uptimeMillis();
					int param = CallImgLibrary.ImageScaleParam(mInvert, mGray, 0, mMoire, pseland);
					if (CallImgLibrary.ImageScale(page2, half2, width[1], height[1], left[1], right[1], top[1], bottom[1], mScrAlgoMode, mScrRotate, mMarginCut, mMarginCutColor, mSharpen, mBright, mGamma, param, size) >= 0) {
						if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half2 + ", 完成サイズP2 size_w=" + size[0] + ", size_h=" + size[1]);}
						mMemCacheFlag[page2].fScale[half2] = true;
						if (img2 != null) {
							img2.SclWidth = width[1];
							img2.SclHeight = height[1];
							img2.FitWidth = fitwidth[1];
							img2.FitHeight = fitheight[1];
						}
					}
					else {
						mFileList[page2].swidth[half2] = 0;
						mFileList[page2].sheight[half2] = 0;
					}
//					Log.i("jpeg-scaling", "time : " + (int)(SystemClock.uptimeMillis() - sttime));
					sendMessage(mHandler, DEF.HMSG_CACHE, -1, 0, null);
				}
			}
			if (img2 != null) {
				if (debug) {Log.d("ImageManager", "ImageScaling Page=" + page2 + ", Half=" + half2
						+ ", Width=" + img2.Width + ", Height=" + img2.Height
						+ ", FitWidth=" + img2.FitWidth + ", FitHeight=" + img2.FitHeight
						+ ", SclWidth=" + img2.SclWidth + ", SclHeight=" + img2.SclHeight);}
			}
		}

		//		Log.d("ImageScaling", "end : p1=" + page1 + ", p2=" + page2 + ", h1=" + half1 + ", h2=" + half2);
		return true;
	}

	// 指定した保存ファイル名で指定ページを書き出し
	// nameがnullなら元のファイル名で
	public String decompFile(int page, String name) {
		if (page < 0 || mFileList.length <= page) {
			// 範囲外
			return null;
		}

		String resultPath = null;
		mCacheBreak = true;
		CallImgLibrary.ImageCancel(1);
		synchronized (mLock) {
			if (!mCloseFlag) {
				CallImgLibrary.ImageCancel(0);

				// キャッシュ読込モードオン
				new File(DEF.getBaseDirectory()).mkdirs();
				new File(DEF.getBaseDirectory() + "share/").mkdirs();

				if (name == null) {
					name = new String();
					name = mFileList[page].name;
				}
				name = name.replace("\\", "_");
				name = name.replace("/", "_");
				String file = DEF.getBaseDirectory() + "share/" + name;
				new File(file).delete();

				BufferedOutputStream os;
				try {
					os = new BufferedOutputStream(new FileOutputStream(file), 500 * 1024);
				} catch (FileNotFoundException e) {
					Log.e("decodeFile/open", e.getMessage());
					return null;
				}
				byte buff[] = new byte[BIS_BUFFSIZE];

				try {
					mCheWriteFlag = false;
					setLoadBitmapStart(page, false);

					if (mFileType == FILETYPE_PDF) {
						Log.d("ImageManager", "decompFile: PDFファイルを開きます.");
						//ページ番号を指定してPdfRenderer.Pageインスタンスを取得する。
						PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
						//PdfRenderer.Pageの情報を使って空の描画用Bitmapインスタンスを作成する。
						Bitmap bm = Bitmap.createBitmap(pdfPage.getWidth() , pdfPage.getHeight() , Bitmap.Config.ARGB_8888);
						// PDFをレンダリングする前にBitmapを白く塗る。
						Canvas canvas = new Canvas(bm);
						canvas.drawColor(Color.WHITE);
						canvas.drawBitmap(bm, 0, 0, null);
						//空のBitmapにPDFの内容を描画する。
						pdfPage.render(bm , null,null , PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
						//PdfRenderer.Pageを閉じる、この処理を忘れると次回読み込む時に例外が発生する。
						pdfPage.close();
						if (bm == null) {
							Log.e("ImageManager", "decompFile: PDFファイルのレンダリングに失敗しました.");
							return null;
						}
						else {
							Log.d("ImageManager", "decompFile: PDFファイルのレンダリングに成功しました.");
							Log.d("ImageManager", "decompFile: JPG形式で保存します.");
							// jpegで保存
							bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
						}
					}
					else if (mFileType == FILETYPE_ZIP) {
						// メモリキャッシュ読込時のみZIP展開する
						// ファイルキャッシュを作成するときはZIP展開不要
						ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(this, BIS_BUFFSIZE));
						zipStream.getNextEntry();
						int readsum = 0;
						while (mRunningFlag == true) {
							if (mCloseFlag) {
								break;
							}
							int readsize = zipStream.read(buff, 0, buff.length);
							if (readsize <= 0) {
								break;
							} else
								readsum += readsize;
							os.write(buff, 0, readsize);
							// ロード経過をcallback
							long nowTime = System.currentTimeMillis();
							if (nowTime - mStartTime > (mMsgCount + 1) * 200) {
								mMsgCount++;
								int prog = (int) ((long) readsum * 100 / mDataSize);
								int rate = (int) ((long) readsum * 10 / (nowTime - mStartTime));
								sendHandler(DEF.HMSG_LOADING, prog << 24, rate, null);
							}
						}
					} else if (mFileType == FILETYPE_RAR) {
						// メモリキャッシュ読込時のみRAR展開する
						// ファイルキャッシュを作成するときはRAR展開不要
						mRarStream = new RarInputStream(new BufferedInputStream(this, BIS_BUFFSIZE), page, mFileList[page], mHandler);
						while (mRunningFlag == true) {
							if (mCloseFlag) {
								break;
							}
							int readsize = mRarStream.read(buff, 0, buff.length);
							if (readsize <= 0) {
								break;
							}
							os.write(buff, 0, readsize);
						}
					} else {
						while (mRunningFlag == true) {
							if (mCloseFlag) {
								break;
							}
							int readsize = this.read(buff, 0, buff.length);
							if (readsize <= 0) {
								break;
							}
							os.write(buff, 0, readsize);
						}
					}
					os.flush();
					os.close();
					resultPath = file;
				} catch (IOException e) {
					Log.e("decodeFile/write", e.getMessage());
					resultPath = null;
				}

				try {
					setLoadBitmapEnd();
				} catch (Exception e) {
					Log.e("decodeFile/end", e.getMessage());
					resultPath = null;
				}
			}
		}
		return resultPath;
	}

	// 指定ページをファイルに書き出し
	public String decompFile(int page) {
		return decompFile(page, null);
	}

	// 共有ファイルを削除する
	public void deleteShareCache() {
		// キャッシュ保存先
		String path = DEF.getBaseDirectory() + "share/";

		// ファイルのリスト取得
		File files[] = new File(path).listFiles();
		if (files == null) {
			// ファイルなし
			return;
		}

		// ファイルのリストを全て削除
		for (File file : files) {
			file.delete();
		}
	}

	public class CacheInputStream extends InputStream {
		InputStream	mInputStream;

		public CacheInputStream(InputStream is) throws IOException {
			mInputStream = is;
		}

		@Override
		public int read() throws IOException {
			// TODO 自動生成されたメソッド・スタブ
			return 0;
		}

		@Override
		public int read(byte buf[], int off, int len) throws IOException {
			int size = len;
			int total = 0;
			int ret = 0;
			while (size > 0) {
				ret = mInputStream.read(buf, off + total, size);
				if (mRunningFlag == false) {
					return -1;
				}
				if (ret <= 0) {
					break;
				}
				total += ret;
				size -= ret;
			}
			if (total == 0 && ret < 0) {
				return -1;
			}
			return total;
		}
	}
}
