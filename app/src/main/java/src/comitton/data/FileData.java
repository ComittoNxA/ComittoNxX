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
	private short type;
	private short exttype;
	private int state;
	private long size;
	private long date;
	private boolean marker;

	public FileData () {
		;
	}

	public FileData (String name) {
		setName(name);
	}

	public FileData (String name, long size, long date) {
		setName(name);
		setSize(size);
		setDate(date);
	}

	public FileData (String name, long size, long date, int state) {
		setName(name);
		setSize(size);
		setDate(date);
		setState(state);
	}

	public FileData (String name, int state) {
		setName(name);
		setState(state);
	}

	public String getName() {
		return name;
	}
	public static String getName(String filepath) {
		return filepath.substring(filepath.lastIndexOf("/") + 1);
	}

	public void setName(String name) {
		this.name = name;
		this.type = getType(name);
		this.exttype = getExtType(name);
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

	public static short getType(String filepath) {

		if (filepath.equals("..")) {
			return FILETYPE_PARENT;
		}
		if (filepath.endsWith("/")) {
			return FILETYPE_DIR;
		}

		int lastIndex = filepath.lastIndexOf(".");
		String ext = filepath;
		if (lastIndex == -1) {
			return FILETYPE_NONE;
		}
		else {
			ext = filepath.substring(lastIndex);
		}
		if (isArchive(ext)) {
			return FILETYPE_ARC;
		}
		if (isImage(ext)) {
			return FILETYPE_IMG;
		}
		if (isPdf(ext)) {
			return FILETYPE_PDF;
		}
		if (isText(ext)) {
			return FILETYPE_TXT;
		}
		if (isEpub(ext)) {
			return FILETYPE_EPUB;
		}
		if (isEpubSub(ext)) {
			return FILETYPE_EPUB_SUB;
		}
		return FILETYPE_NONE;
	}

	public static short getExtType(String filepath) {

		if (filepath.endsWith(".zip") || filepath.endsWith(".cbz") || filepath.endsWith(".epub")) {
			return EXTTYPE_ZIP;
		}
		if (filepath.endsWith(".rar") || filepath.endsWith(".cbr")) {
			return EXTTYPE_RAR;
		}
		if (filepath.endsWith(".pdf")) {
			return EXTTYPE_PDF;
		}
		if (filepath.endsWith(".txt") || filepath.endsWith(".xhtml") || filepath.endsWith(".html")) {
			return EXTTYPE_TXT;
		}
		if(DEF.WITH_JPEG && (filepath.endsWith(".jpg") || filepath.endsWith(".jpeg"))){
			return EXTTYPE_JPG;
		}
		if(DEF.WITH_PNG && filepath.endsWith(".png")){
			return EXTTYPE_PNG;
		}
		if(DEF.WITH_GIF && filepath.endsWith(".gif")){
			return EXTTYPE_GIF;
		}
		if(DEF.WITH_WEBP && filepath.endsWith(".webp")){
			return EXTTYPE_WEBP;
		}
		if(DEF.WITH_AVIF && filepath.endsWith(".avif")){
			return EXTTYPE_AVIF;
		}
		if(DEF.WITH_HEIF && (filepath.endsWith(".heif") || filepath.endsWith(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			return EXTTYPE_HEIF;
		}
		if(DEF.WITH_JXL && filepath.endsWith(".jxl")){
			return EXTTYPE_JXL;
		}
		return EXTTYPE_NONE;
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

	public static boolean isImage(String filepath) {

		//return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") /*|| ext.equals(".gif")*/ || ext.equals(".webp") || ext.equals(".avif") /*|| ext.equals(".heif") || ext.equals(".jxl")*/;

		if(DEF.WITH_JPEG && (filepath.endsWith(".jpg") || filepath.endsWith(".jpeg"))){
			return true;
		}
		if(DEF.WITH_PNG && filepath.endsWith(".png")){
			return true;
		}
		if(DEF.WITH_GIF && filepath.endsWith(".gif")){
			return true;
		}
		if(DEF.WITH_WEBP && filepath.endsWith(".webp")){
			return true;
		}
		if(DEF.WITH_AVIF && filepath.endsWith(".avif")){
			return true;
		}
		if(DEF.WITH_HEIF && (filepath.endsWith(".heif") || filepath.endsWith(".heic")) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			return true;
		}
		if(DEF.WITH_JXL && filepath.endsWith(".jxl")){
			return true;
		}
		return false;
	}
	public static boolean isArchive(String filepath) {
		return filepath.endsWith(".zip") || filepath.endsWith(".rar") || filepath.endsWith(".cbz") || filepath.endsWith(".cbr");
	}
	public static boolean isZip(String filepath) {
		return filepath.endsWith(".zip") || filepath.endsWith(".cbz") || filepath.endsWith(".epub");
	}
	public static boolean isRar(String filepath) {
		return filepath.endsWith(".rar") || filepath.endsWith(".cbr");
	}
	public static boolean isPdf(String filepath) {
		return filepath.endsWith(".pdf");
	}
	public static boolean isEpub(String filepath) {
		return filepath.endsWith(".epub");
	}
	public static boolean isEpubSub(String filepath) {
		return filepath.endsWith(".css") || filepath.endsWith(".xml") || filepath.endsWith(".opf") || filepath.endsWith(".ncx");
	}
	public static boolean isText(String filepath) {
		return filepath.endsWith(".txt") || filepath.endsWith(".xhtml") || filepath.endsWith(".html");
	}
}
