package src.comitton.fileview.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import src.comitton.common.Logcat;
import src.comitton.fileaccess.FileAccess;


public class FileData {
	private static final String TAG = "FileData";

	public static final short FILETYPE_PARENT = 0;
	public static final short FILETYPE_DIR = 1;
	public static final short FILETYPE_ARC = 2;
	public static final short FILETYPE_IMG = 3;
	public static final short FILETYPE_PDF = 4;
	public static final short FILETYPE_TXT = 5;
	public static final short FILETYPE_EPUB = 6;
	public static final short FILETYPE_EPUB_SUB = 7;
	public static final short FILETYPE_NONE = 8;

	public static final short EXTTYPE_NONE = 0;
	public static final short EXTTYPE_ZIP = 1;
	public static final short EXTTYPE_RAR = 2;
	public static final short EXTTYPE_PDF = 3;
	public static final short EXTTYPE_JPG = 4;
	public static final short EXTTYPE_PNG = 5;
	public static final short EXTTYPE_GIF = 6;
	public static final short EXTTYPE_TXT = 7;
	public static final short EXTTYPE_WEBP = 51;
	public static final short EXTTYPE_AVIF = 52;
	public static final short EXTTYPE_HEIF = 53;
	public static final short EXTTYPE_JXL = 54;
	public static final short EXTTYPE_EPUB = 55;

	private String name;
	private short type;		// ファイルタイプ
	private short exttype;	// 拡張子タイプ
	private int state;		// 読書状態
	private long size;
	private long date;
	private int maxpage;
	private boolean marker;
	private int index;

	public FileData () {
		;
	}

	public FileData (Context context, String name) {
		setName(context, name);
	}

	public FileData (Context context, String name, long size, long date) {
		this(context, name);
		setSize(size);
		setDate(date);
	}

	public FileData (Context context, String name, int state) {
		this(context, name);
		setState(state);
	}

	public FileData (Context context, String name, long size, long date, int state) {
		this(context, name, size, date);
		setState(state);
	}

	public String getName() {
		return name;
	}
	public static String getName(String filepath) {
		return filepath.substring(filepath.lastIndexOf("/") + 1);
	}

	public void setName(Context context, String name) {
		this.name = name;
		setType(context, name);
		setExtType(context, name);
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getMaxpage() {
		return maxpage;
	}

	public void setMaxpage(int maxpage) {
		this.maxpage = maxpage;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public short getType() {
		return type;
	}

	private void setType(short type) {
		this.type = type;
	}

	private void setType(Context context, String filepath) {
		setType(getType(context, filepath));
	}

	public short getExtType() {
		return exttype;
	}

	public static short getType(Context context, String filepath) {
		int logLevel = Logcat.LOG_LEVEL_WARN;
		String filename = FileAccess.filename(context, filepath);
		Logcat.d(logLevel, "開始します. filepath=" + filepath + ", filename=" + filename);

		if (filename.equals("..")) {
			return FILETYPE_PARENT;
		}
		if (filename.endsWith("/")) {
			return FILETYPE_DIR;
		}
		if (isArchive(filename)) {
			return FILETYPE_ARC;
		}
		if (isImage(context, filename)) {
			return FILETYPE_IMG;
		}
		if (isPdf(filename)) {
			return FILETYPE_PDF;
		}
		if (isText(filename)) {
			return FILETYPE_TXT;
		}
		if (isEpub(filename)) {
			return FILETYPE_EPUB;
		}
		if (isEpubSub(filename)) {
			return FILETYPE_EPUB_SUB;
		}
		return FILETYPE_NONE;
	}

	public static short getExtType(Context context, String filepath) {
		String filename = FileAccess.filename(context, filepath).toLowerCase();

		if (filename.endsWith(".zip") || filename.endsWith(".cbz")) {
			return EXTTYPE_ZIP;
		}
		if (filename.endsWith(".epub")) {
			return EXTTYPE_EPUB;
		}
		if (filename.endsWith(".rar") || filename.endsWith(".cbr")) {
			return EXTTYPE_RAR;
		}
		if (filename.endsWith(".pdf")) {
			return EXTTYPE_PDF;
		}
		if (filename.endsWith(".txt") || filename.endsWith(".xhtml") || filename.endsWith(".html")) {
			return EXTTYPE_TXT;
		}
		if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
			return EXTTYPE_JPG;
		}
		if (filename.endsWith(".png")) {
			return EXTTYPE_PNG;
		}
		if (filename.endsWith(".gif")) {
			return EXTTYPE_GIF;
		}
		if (filename.endsWith(".webp")) {
			return EXTTYPE_WEBP;
		}
		if (filename.endsWith(".avif")) {
			return EXTTYPE_AVIF;
		}
		if ((filename.endsWith(".heif") || filename.endsWith(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return EXTTYPE_HEIF;
		}
		if (filename.endsWith(".jxl")) {
			return EXTTYPE_JXL;
		}
		return EXTTYPE_NONE;
	}

	private void setExtType(short exttype) {
		this.exttype = exttype;
	}

	private void setExtType(Context context, String filepath) {
		setExtType(getExtType(context, filepath));
	}

	public boolean getMarker() {
		return marker;
	}

	public void setMarker(boolean marker) {
		this.marker = marker;
	}

	@SuppressLint("SuspiciousIndentation")
    public String getFileInfo() {
		String dateStr;
		if (date != 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");  
			dateStr = sdf.format(date);
		}
		else {
			dateStr = "[----/--/-- --:--:--]";
		}

		if (type == FILETYPE_PARENT) { 
			return "";  
		}
		else if (type == FILETYPE_DIR) {
			return dateStr;  
		}
		else {
    		// サイズ
    		NumberFormat format = NumberFormat.getNumberInstance();
    		String sizeStr = format.format(size);
    		int len = sizeStr.length();
    		if (len > 15) {
    			sizeStr = "999,999,999,999";
    			len = 15;
    		}
    		sizeStr = ("                " + sizeStr).substring(len);
			return dateStr + " " + sizeStr + "Bytes";  
		}
	}

	public void setDate(long date) {
		this.date = date;
	}

	// 日付取得
	public long getDate() {
		return this.date;
	}

	public void setSize(long size) {
		this.size = size;
	}
	public long getSize() {
		return this.size;
	}

	// ArrayListのindexOfから呼ばれる
	public boolean equals(Object obj){
		FileData fd = (FileData)obj;
		if (name.equals(fd.getName())) {
			return true;
		}
		return false;
	}

	public static boolean isImage(Context context, String filepath) {

		switch (getExtType(context, filepath)) {
			case EXTTYPE_JPG:
			case EXTTYPE_PNG:
			case EXTTYPE_GIF:
			case EXTTYPE_WEBP:
			case EXTTYPE_AVIF:
			case EXTTYPE_HEIF:
			case EXTTYPE_JXL:
				return true;
			default:
				return false;
		}
	}

	public static boolean isArchive(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".zip") || filename.endsWith(".rar") || filename.endsWith(".cbz") || filename.endsWith(".cbr");
	}
	public static boolean isZip(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".zip") || filename.endsWith(".cbz") || filename.endsWith(".epub");
	}
	public static boolean isRar(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".rar") || filename.endsWith(".cbr");
	}
	public static boolean isPdf(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".pdf");
	}
	public static boolean isEpub(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".epub");
	}
	public static boolean isEpubSub(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".css") || filename.endsWith(".xml") || filename.endsWith(".opf") || filename.endsWith(".ncx");
	}
	public static boolean isText(String filepath) {
		String filename = filepath.toLowerCase();
		return filename.endsWith(".txt") || filename.endsWith(".xhtml") || filename.endsWith(".html");
	}

	public static String getMimeType(Context context, String filepath) {
		int extType = getExtType(context, filepath);
		if (extType == FileData.EXTTYPE_ZIP) {
			if (filepath.toLowerCase().endsWith(".epub")) {
				return "application/epub+zip";
			}
			else {
				return "application/zip";
			}
		}
		if (extType == FileData.EXTTYPE_RAR) {
			return "application/x-rar-compressed";
		}
		if (extType == FileData.EXTTYPE_PDF) {
			return "application/pdf";
		}
		if (extType == FileData.EXTTYPE_TXT) {
			if (filepath.toLowerCase().endsWith(".txt")) {
				return "text/plain";
			} else if (filepath.toLowerCase().endsWith(".xhtml")) {
				return "application/xhtml+xml";
			} else if (filepath.toLowerCase().endsWith(".html")) {
				return "application/html";
			}
		}
		if (extType == FileData.EXTTYPE_JPG) {
			return "image/jpeg";
		}
		else if (extType == FileData.EXTTYPE_PNG) {
			return "image/png";
		}
		else if (extType == FileData.EXTTYPE_GIF) {
			return "image/gif";
		}
		else if (extType == FileData.EXTTYPE_WEBP) {
			return "image/webp";
		}
		else if (extType == FileData.EXTTYPE_AVIF) {
			return "image/avif";
		}
		else if (extType == FileData.EXTTYPE_HEIF) {
			return "image/heif";
		}
		else if (extType == FileData.EXTTYPE_JXL) {
			return "image/jxl";
		}
		else {
			return "application/octet-stream";
		}
	}
}
