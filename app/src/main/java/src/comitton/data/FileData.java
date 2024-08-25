package src.comitton.data;

import android.annotation.SuppressLint;
import android.os.Build;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import jp.dip.muracoro.comittonx.BuildConfig;
import src.comitton.common.DEF;


public class FileData {
	public static final short FILETYPE_PARENT = 0;
	public static final short FILETYPE_DIR = 1;
	public static final short FILETYPE_ARC = 2;
	public static final short FILETYPE_IMG = 3;
	public static final short FILETYPE_TXT = 4;
	public static final short FILETYPE_NONE = 5;

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

	private String name;
	private short type;
	private short exttype;
	private int state;
	private long size;
	private long date;
	private boolean marker;

	public FileData () {
		;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getExtType() {
		return exttype;
	}

	public void setExtType(short exttype) {
		this.exttype = exttype;
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
    		if (len > 11) {
    			sizeStr = "999,999,999";
    			len = 11;
    		}
    		sizeStr = ("           " + sizeStr).substring(len);
			return dateStr + " " + sizeStr + "Bytes";  
		}
	}

//	public String getFileSize() {
//		NumberFormat format = NumberFormat.getNumberInstance();
//		String sizeStr = format.format(size);
//		int len = sizeStr.length();
//		if (len > 11) {
//			sizeStr = "999,999,999";
//			len = 11;
//		}
//		sizeStr = ("           " + sizeStr).substring(len);
//		return sizeStr + "Bytes";  
//	}

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

	public static boolean isImage(String ext) {

		//return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") /*|| ext.equals(".gif")*/ || ext.equals(".webp") || ext.equals(".avif") /*|| ext.equals(".heif") || ext.equals(".jxl")*/;

		if(DEF.WITH_JPEG && (ext.equals(".jpg") || ext.equals(".jpeg"))){
			return true;
		}
		if(DEF.WITH_PNG && ext.equals(".png")){
			return true;
		}
		if(DEF.WITH_GIF && ext.equals(".gif")){
			return true;
		}
		if(DEF.WITH_WEBP && ext.equals(".webp")){
			return true;
		}
		if(DEF.WITH_AVIF && ext.equals(".avif")){
			return true;
		}
		if(DEF.WITH_HEIF && (ext.equals(".heif") || ext.equals(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			return true;
		}
		if(DEF.WITH_JXL && ext.equals(".jxl")){
			return true;
		}
		return false;
	}
	public static boolean isArchive(String ext) {
		return ext.equals(".zip") || ext.equals(".rar") || ext.equals(".cbz") || ext.equals(".cbr") || ext.equals(".epub");
	}
	public static boolean isZip(String ext) {
		return ext.equals(".zip") || ext.equals(".cbz") || ext.equals(".epub");
	}
	public static boolean isRar(String ext) {
		return ext.equals(".rar") || ext.equals(".cbr");
	}
	public static boolean isText(String ext) {
		return ext.equals(".txt") || ext.equals(".xhtml") || ext.equals(".html");
	}
}
