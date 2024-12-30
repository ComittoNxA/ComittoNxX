package src.comitton.common;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

import jp.dip.muracoro.comittonx.BuildConfig;
import src.comitton.data.FileData;

import org.mozilla.universalchardet.UniversalDetector;

public class DEF {

	public static final boolean DEBUG = BuildConfig.DEBUG;
	public static final String BUILD_DATE = (new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())).format(BuildConfig.BUILD_DATE);
	//public static final String BUILD_DATE = BuildConfig.BUILD_DATE;

	// WITH_XXX は app/build.gradle 内で宣言している
	public static final boolean WITH_JPEG = BuildConfig.WITH_JPEG.equals("ON");
	public static final boolean WITH_PNG = BuildConfig.WITH_PNG.equals("ON");
	public static final boolean WITH_GIF = BuildConfig.WITH_GIF.equals("ON");
	public static final boolean WITH_WEBP = BuildConfig.WITH_WEBP.equals("ON");
	public static final boolean WITH_AVIF = BuildConfig.WITH_AVIF.equals("ON");
	public static final boolean WITH_HEIF = BuildConfig.WITH_HEIF.equals("ON");
	public static final boolean WITH_JXL = BuildConfig.WITH_JXL.equals("ON");

	public static final String DOWNLOAD_URL = "https://github.com/ComittoNxA/ComittoNxX";
	public static final int MESSAGE_FILE_DELETE = 1000;
	public static final int MESSAGE_RECORD_DELETE = 1001;
	public static final int MESSAGE_LASTPAGE = 1002;
	public static final int MESSAGE_SHORTCUT = 1003;
	public static final int MESSAGE_EDITSERVER = 1004;
	public static final int MESSAGE_DOWNLOAD = 1005;
	public static final int MESSAGE_MARKER = 1006;
	public static final int MESSAGE_CLOSE = 1007;
	public static final int MESSAGE_RESUME = 1008;
	public static final int MESSAGE_RENAME = 1010;
	public static final int MESSAGE_LISTMODE = 1011;
	public static final int MESSAGE_SORT = 1012;
	public static final int MESSAGE_FILE_LONGCLICK = 1013;
	public static final int MESSAGE_RECORD_LONGCLICK = 1014;
	public static final int MESSAGE_FILE_RENAME = 1015;
	public static final int MESSAGE_MOVE_PATH_EROOR = 1016;
	public static final int MESSAGE_RESETLOCAL = 1017;

	public static final int HMSG_LOAD_END = 1;
	public static final int HMSG_READ_END = 2;
	public static final int HMSG_PROGRESS = 3;
	public static final int HMSG_ERROR = 4;
	public static final int HMSG_CACHE = 5;
	public static final int HMSG_LOADING = 6;
	public static final int HMSG_NOISE = 7;
	public static final int HMSG_NOISESTATE = 8;
	public static final int HMSG_THUMBNAIL = 9;
	public static final int HMSG_LOADFILELIST = 10;
	public static final int HMSG_UPDATEFILELIST = 11;
	public static final int HMSG_DRAWENABLE = 13;
	public static final int HMSG_TX_PARSE = 14;
	public static final int HMSG_TX_LAYOUT = 15;

	public static final int MENU_HELP = Menu.FIRST + 0;
	public static final int MENU_SETTING = Menu.FIRST + 1;
	public static final int MENU_SIORI = Menu.FIRST + 2;
	public static final int MENU_ABOUT = Menu.FIRST + 3;
	public static final int MENU_DISPDUAL = Menu.FIRST + 4;
	public static final int MENU_DISPHALF = Menu.FIRST + 5;
	public static final int MENU_DISPNORM = Menu.FIRST + 6;
	public static final int MENU_SHORTCUT = Menu.FIRST + 7;
	public static final int MENU_SERVER = Menu.FIRST + 8;
	public static final int MENU_REFRESH = Menu.FIRST + 9;
	public static final int MENU_NOISE = Menu.FIRST + 10;
	public static final int MENU_QUIT = Menu.FIRST + 11;
	public static final int MENU_THUMBDEL = Menu.FIRST + 12;
	public static final int MENU_ONLINE = Menu.FIRST + 13;
	public static final int MENU_MARKER = Menu.FIRST + 14;
	public static final int MENU_SHARE = Menu.FIRST + 15;
	public static final int MENU_SHARER = Menu.FIRST + 16;
	public static final int MENU_SHAREL = Menu.FIRST + 17;
	public static final int MENU_DELSHARE = Menu.FIRST + 18;
	public static final int MENU_REVERSE = Menu.FIRST + 19;
	public static final int MENU_ROTATE = Menu.FIRST + 20;
	public static final int MENU_CHG_OPE = Menu.FIRST + 21;
	public static final int MENU_PAGEWAY = Menu.FIRST + 22;
	public static final int MENU_SCRLWAY = Menu.FIRST + 23;
	public static final int MENU_THUMBSWT = Menu.FIRST + 24;
	public static final int MENU_IMGCONF = Menu.FIRST + 25;
	public static final int MENU_IMGROTA = Menu.FIRST + 26;
	public static final int MENU_IMGVIEW = Menu.FIRST + 27;
	public static final int MENU_IMGSIZE = Menu.FIRST + 28;
	public static final int MENU_PAGESEL = Menu.FIRST + 29;
	public static final int MENU_SHARPEN = Menu.FIRST + 30;
	public static final int MENU_INVERT = Menu.FIRST + 31;
	public static final int MENU_MGNCUT = Menu.FIRST + 32;
	public static final int MENU_MGNCUTCOLOR = Menu.FIRST + 33;
	public static final int MENU_GRAY = Menu.FIRST + 34;
	public static final int MENU_NOTICE = Menu.FIRST + 35;
	public static final int MENU_ADDBOOKMARK = Menu.FIRST + 36;
	public static final int MENU_SELBOOKMARK = Menu.FIRST + 37;
	public static final int MENU_SORT = Menu.FIRST + 38;
	public static final int MENU_ADDDIR = Menu.FIRST + 39;
	public static final int MENU_LISTMODE = Menu.FIRST + 40;
	public static final int MENU_AUTOPLAY = Menu.FIRST + 41;
	public static final int MENU_TOP_SETTING = Menu.FIRST + 42;
	public static final int MENU_SELCHAPTER = Menu.FIRST + 43;
	public static final int MENU_SEARCHTEXT = Menu.FIRST + 44;
	public static final int MENU_SEARCHJUMP = Menu.FIRST + 45;
	public static final int MENU_IMGALGO = Menu.FIRST + 46;
	public static final int MENU_TXTCONF = Menu.FIRST + 47;
	public static final int MENU_BRIGHT = Menu.FIRST + 48;
	public static final int MENU_GAMMA = Menu.FIRST + 49;
	public static final int MENU_BKLIGHT = Menu.FIRST + 50;
	public static final int MENU_SEL_DIR_TREE = Menu.FIRST + 51;
	public static final int MENU_EDIT_TOOLBAR = Menu.FIRST + 52;
	public static final int MENU_BOOKMARK = Menu.FIRST + 1000;
	public static final int MENU_CHAPTER = Menu.FIRST + 2000;
	public static final int MENU_DIR_TREE = MENU_CHAPTER;

	public static final int MENU_CMARGIN = Menu.FIRST + 101;
	public static final int MENU_CSHADOW = Menu.FIRST + 102;
	public static final int MENU_SETTHUMB = Menu.FIRST + 103;
	public static final int MENU_SETTHUMBCROPPED = Menu.FIRST + 104;
	public static final int MENU_LICENSE = Menu.FIRST + 105;

	public static final String KEY_PAGE_SELECT_TOOLBAR = "PageSelectToolbar";
	public static final int TOOLBAR_LEFTMOST = 1001;
	public static final int TOOLBAR_LEFT100 = 1002;
	public static final int TOOLBAR_LEFT10 = 1003;
	public static final int TOOLBAR_LEFT1 = 1004;
	public static final int TOOLBAR_RIGHT1 = 1005;
	public static final int TOOLBAR_RIGHT10 = 1006;
	public static final int TOOLBAR_RIGHT100 = 1007;
	public static final int TOOLBAR_RIGHTMOST = 1008;
	public static final int TOOLBAR_PAGE_RESET = 1009;

	public static final int TOOLBAR_BOOK_LEFT = 1010;
	public static final int TOOLBAR_BOOK_RIGHT = 1011;
	public static final int TOOLBAR_BOOKMARK_LEFT = 1012;
	public static final int TOOLBAR_BOOKMARK_RIGHT = 1013;
	public static final int TOOLBAR_THUMB_SLIDER = 1014;
	public static final int TOOLBAR_DIR_TREE = 1015;
	public static final int TOOLBAR_TOC = 1016;
	public static final int TOOLBAR_FAVORITE = 1017;
	public static final int TOOLBAR_ADD_FAVORITE = 1018;
	public static final int TOOLBAR_SEARCH = 1019;
	public static final int TOOLBAR_SHARE = 1020;
	public static final int TOOLBAR_SHARE_LEFT_PAGE = 1021;
	public static final int TOOLBAR_SHARE_RIGHT_PAGE = 1022;
	public static final int TOOLBAR_ROTATE = 1023;
	public static final int TOOLBAR_ROTATE_IMAGE = 1024;
	public static final int TOOLBAR_SELECT_THUMB = 1025;
	public static final int TOOLBAR_TRIM_THUMB = 1026;
	public static final int TOOLBAR_CONTROL = 1027;
	public static final int TOOLBAR_MENU = 1028;
	public static final int TOOLBAR_CONFIG = 1029;
	public static final int TOOLBAR_EDIT_TOOLBAR = 1030;

	public static final int SHARE_SINGLE = 2001;
	public static final int SHARE_LR = 2002;


	public static final int REQUEST_SETTING = 101;
	public static final int REQUEST_FILE = 102;
	public static final int REQUEST_HELP = 103;
	public static final int REQUEST_SERVER = 104;
	public static final int REQUEST_IMAGE = 105;
	public static final int REQUEST_TEXT = 106;
	public static final int REQUEST_EXPAND = 107;
	public static final int REQUEST_RECORD = 108;
	public static final int REQUEST_LICENSE = 109;
	public static final int REQUEST_EPUB = 110;
	public static final int REQUEST_CROP = 1000;

	public static final int VIEWPT_RIGHTTOP = 0;
	public static final int VIEWPT_LEFTTOP = 1;
	public static final int VIEWPT_RIGHTBTM = 2;
	public static final int VIEWPT_LEFTBTM = 3;
	public static final int VIEWPT_CENTER = 4;

	public static final int ZOOMTYPE_ORIG10 = 0;
	public static final int ZOOMTYPE_ORIG15 = 1;
	public static final int ZOOMTYPE_ORIG20 = 2;
	public static final int ZOOMTYPE_DISP15 = 3;
	public static final int ZOOMTYPE_DISP20 = 4;
	public static final int ZOOMTYPE_DISP25 = 5;
	public static final int ZOOMTYPE_DISP30 = 6;

	public static final int TOOLBAR_NONE = -1;
	public static final int TOOLBAR_PARENT = 0;
	public static final int TOOLBAR_REFRESH = 1;
	public static final int TOOLBAR_THUMBNAIL = 2;
	public static final int TOOLBAR_MARKER = 3;
	//	public static final int TOOLBAR_SERVER = 4;
	public static final int TOOLBAR_ADDDIR = 4;
	public static final int TOOLBAR_EXIT = 5;

	// ページめくり表示方向
	public static final int PAGEWAY_RIGHT = 0; // 右から左
	public static final int PAGEWAY_LEFT = 1; // 左から右

	// スクロール方向
	public static final int SCRLWAY_H = 0; // 横→縦
	public static final int SCRLWAY_V = 1; // 縦→横

	// 拡大方法
	public static final int SCALE_ORIGINAL = 0;
	public static final int SCALE_FIT_WIDTH = 1;
	public static final int SCALE_FIT_HEIGHT = 2;
	public static final int SCALE_FIT_ALL = 3;
	public static final int SCALE_FIT_ALLMAX = 4;
	public static final int SCALE_FIT_SPRMAX = 5;
	public static final int SCALE_FIT_WIDTH2 = 6;
	public static final int SCALE_FIT_ALL2 = 7;
	public static final int SCALE_PINCH = 8;

	// 最終ページの動作
	public static final int LASTMSG_CLOSE = 0;
	public static final int LASTMSG_DIALOG = 1;
	public static final int LASTMSG_NEXT = 2;

	// ソート方法
	public static final int ZIPSORT_NONE = 0; // ソートなし
	public static final int ZIPSORT_FILEMGR = 1; // ファイル名順(ディレクトリ混在)
	public static final int ZIPSORT_FILESEP = 2; // ファイル名順(ディレクトリ分離)
	public static final int ZIPSORT_NEWMGR = 3; // 新しい順(ディレクトリ混在)
	public static final int ZIPSORT_NEWSEP = 4; // 新しい順(ディレクトリ分離)
	public static final int ZIPSORT_OLDMGR = 5; // 古い順(ディレクトリ混在)
	public static final int ZIPSORT_OLDSEP = 6; // 古い順(ディレクトリ分離)

	// ファイル拡張子
	public static final String EXTENSION_SETTING = ".set";

	public static final String KEY_EXPORTSETTING = "ExportSetting";
	public static final String KEY_IMPORTSETTING = "ImportSetting";

	public static final String KEY_CONFHELP = "ConfHelp";
	public static final String KEY_FILEHELP = "FileHelp";
	public static final String KEY_FCLRHELP = "FClrHelp";
	public static final String KEY_IMAGEHELP = "ImageHelp";
	public static final String KEY_IDTLHELP = "IDtlHelp";
	public static final String KEY_COMMHELP = "CommHelp";
	public static final String KEY_NOISEHELP = "NoiseHelp";
	public static final String KEY_CACHEHELP = "CacheHelp";
	public static final String KEY_TEXTHELP = "TextHelp";
	public static final String KEY_IMTXHELP = "ImgTxtHelp";
	public static final String KEY_ITDTLHELP = "ImgTxtDtlHelp";
	public static final String KEY_ITCLRHELP = "ImgTxtClrHelp";
	public static final String KEY_RECHELP = "RecordHelp";

	public static final String KEY_CSTUPDATE = "CustomUpdate";

	public static final String KEY_LISTROTA = "ListRota";
	public static final String KEY_LISTSORT = "ListSort";
	public static final String KEY_FONTTITLE = "FontTitleSp";
	public static final String KEY_FONTMAIN = "FontMainSp";
	public static final String KEY_FONTSUB = "FontSubSp";
	public static final String KEY_FONTTILE = "FontTileSp";
	public static final String KEY_BKPARENT = "BackParent";
	public static final String KEY_ITEMMRGN = "ItemMarginSp";

	public static final String KEY_RDIRVIEW = "RDView";
	public static final String KEY_RBMVIEW = "RBView";
	public static final String KEY_RHISTVIEW = "RHView";
	public static final String KEY_SELECTOR = "Selector";
	public static final String KEY_RECLOCAL = "RHLocalRec";
	public static final String KEY_RECSAMBA = "RHSambaRec";

	public static final String KEY_PRESET = "Preset";

	public static final String KEY_TXTCOLOR = "TxtColor";
	public static final String KEY_DIRCOLOR = "DirColor";
	public static final String KEY_BEFCOLOR = "BefColor";
	public static final String KEY_NOWCOLOR = "NowColor";
	public static final String KEY_AFTCOLOR = "AftColor";
	public static final String KEY_IMGCOLOR = "ImgColor";
	public static final String KEY_INFCOLOR = "InfColor";
	public static final String KEY_BAKCOLOR = "BakColor";

	public static final String KEY_TXTRGB = "TxtRGB";
	public static final String KEY_DIRRGB = "DirRGB";
	public static final String KEY_BEFRGB = "BefRGB";
	public static final String KEY_NOWRGB = "NowRGB";
	public static final String KEY_AFTRGB = "AftRGB";
	public static final String KEY_IMGRGB = "ImgRGB";
	public static final String KEY_INFRGB = "InfRGB";
	public static final String KEY_MRKRGB = "MrkRGB";
	public static final String KEY_BAKRGB = "BakRGB";
	public static final String KEY_CURRGB = "CurRGB";
	public static final String KEY_TITRGB = "TitRGB";
	public static final String KEY_TIBRGB = "TibRGB";
	public static final String KEY_TLDRGB = "TldRGB";
	public static final String KEY_TLBRGB = "TlbRGB";

	// public static final String KEY_TITLECLR = "TitleColor";
	// public static final String KEY_TOOLBCLR = "ToolbarColor";

	public static final String KEY_MGNCOLOR = "MgnColor";
	public static final String KEY_CNTCOLOR = "CntColor";
	public static final String KEY_GUICOLOR = "GuiColor";

	public static final String KEY_MGNRGB = "MgnRGB";
	public static final String KEY_CNTRGB = "CntRGB";
	public static final String KEY_GUIRGB = "GuiRGB";

	public static final String KEY_ORGWIDTH = "OrgWidth";
	public static final String KEY_ORGHEIGHT = "OrgHeight";

	public static final String KEY_VIEWROTA = "ViewRota";
	public static final String KEY_FILESORT = "FileSort";
	public static final String KEY_VIEWPT = "ViewPt";
	public static final String KEY_INISCALE = "IniScale";

	public static final String KEY_CLICKAREA = "ClickAreaSp";
	public static final String KEY_PAGERANGE = "PageRangeSp";
	public static final String KEY_TAPRANGE = "TapRangeSp";
	public static final String KEY_VOLSCRL = "VolScrlSp";
	public static final String KEY_SCROLL = "Scroll";
	public static final String KEY_MARGIN = "Mergin";
	public static final String KEY_LONGTAP = "LongTap";
	public static final String KEY_WADJUST = "WAdjust";
	public static final String KEY_WSCALING = "WScaling";
	public static final String KEY_SCALING = "Scaling";
	public static final String KEY_CENTER = "Center";
	public static final String KEY_GRADATION = "Gradation";
	public static final String KEY_ZOOMTYPE = "ZoomType";
	public static final String KEY_SCRLRNGW = "ScrlRngW";
	public static final String KEY_SCRLRNGH = "ScrlRngH";

	public static final String KEY_CHAR_DETECT = "CharDetect";
	public static final String KEY_CHARSET = "Charset";
	public static final String KEY_SORT_BY_IGNORE_WIDTH = "SortByIgnoreWidth";
	public static final String KEY_SORT_BY_IGNORE_CASE = "SortByIgnoreCase";
	public static final String KEY_SORT_BY_SYMBOL = "SortBySymbol";
	public static final String KEY_SORT_BY_NATURAL_NUMBERS = "SortByNaturalNumbers";
	public static final String KEY_SORT_BY_KANJI_NUMERALS = "SortByKanjiNumerals";
	public static final String KEY_SORT_BY_JAPANESE_VOLUME_NAME = "SortByJapaneseVolumeName";
	public static final String KEY_SORT_PRIORITY_WORD_01 = "SortPriorityWord01";
	public static final String KEY_SORT_PRIORITY_WORD_02 = "SortPriorityWord02";
	public static final String KEY_SORT_PRIORITY_WORD_03 = "SortPriorityWord03";
	public static final String KEY_SORT_PRIORITY_WORD_04 = "SortPriorityWord04";
	public static final String KEY_SORT_PRIORITY_WORD_05 = "SortPriorityWord05";
	public static final String KEY_SORT_PRIORITY_WORD_06 = "SortPriorityWord06";
	public static final String KEY_SORT_PRIORITY_WORD_07 = "SortPriorityWord07";
	public static final String KEY_SORT_PRIORITY_WORD_08 = "SortPriorityWord08";
	public static final String KEY_SORT_PRIORITY_WORD_09 = "SortPriorityWord09";
	public static final String KEY_SORT_PRIORITY_WORD_10 = "SortPriorityWord10";
	public static final String KEY_EFFECTTIME = "EffectTime";
	public static final String KEY_MOMENTMODE = "MomentMode";
	public static final String KEY_PAGESELECT = "PageSelect";
	public static final String KEY_AUTOPLAY = "AutoPlayInt";
	public static final String KEY_IMMENABLE = "ImmEnable";
	public static final String KEY_BOTTOMFILE = "BottomFile";
	public static final String KEY_PINCHENABLE = "PinchEnable";
	public static final String KEY_OLDMENU = "OldMenu";

	public static final String KEY_HIDDENFILE = "HiddenFile";

	public static final String KEY_NOISESCRL = "NoiseScrlSp";
	public static final String KEY_NOISEUNDER = "NoiseUnder";
	public static final String KEY_NOISEOVER = "NoiseOver";
	public static final String KEY_NOISELEVEL = "NoiseLevel";
	public static final String KEY_NOISEDEC = "NoiseDec";

	// public static final String KEY_TITLE = "Title";
	public static final String KEY_SHARPEN = "Sharpen";
	public static final String KEY_NOTICE = "Notice";
	public static final String KEY_NOSLEEP = "NoSleep";
	public static final String KEY_CHGPAGE = "ChgPage";
	public static final String KEY_CHGFLICK = "ChgFlick";
	public static final String KEY_ORGWIDTHTWICE = "OrgWidthTwice";
	public static final String KEY_REDUCE = "Reduce";
	public static final String KEY_MEMFREE = "MemFree";
	public static final String KEY_HALFHEIGHT = "HalfHeight";
	public static final String KEY_SCALEBMP = "ScaleBmp";
	public static final String KEY_BACKMODE = "BackMode";
	public static final String KEY_VOLKEY = "VolKey";
	public static final String KEY_SCRLWAY = "ScrlWay";
	public static final String KEY_THUMBNAIL = "Thumbnail";
	public static final String KEY_THUMBCACHE = "ThumbCache";
	public static final String KEY_THUMBCROP = "ThumbCrop";
	public static final String KEY_THUMBMARGIN = "ThumbMargin";
	public static final String KEY_ROTATEBTN = "RotateBtn";
	public static final String KEY_ACCESSLAMP = "AccessLamp";
	public static final String KEY_MARGINCUT = "MarginCut";
	public static final String KEY_MARGINCUTCOLOR = "MarginCutColor";
	public static final String KEY_TAPEXPAND = "TapExpand";
	public static final String KEY_TAPPATTERN = "TapPattern";
	public static final String KEY_PAGENUMBER = "PageNumber";
	public static final String KEY_TIMEANDBATTERY = "TimeAndBattery";
	public static final String KEY_TAPRATE = "TapRate";
	public static final String KEY_OLDPAGESEL = "OldPageSel";
	public static final String KEY_RESUMEOPEN = "ResumeOpen";
	public static final String KEY_CONFIRMBACK = "ConfirmBack";
	public static final String KEY_CLEARTOP = "ClearTop";
	public static final String KEY_HISTNUM = "RLSave3";
	public static final String KEY_EXTENSION = "Extension";
	public static final String KEY_SPLIT_FILENAME = "SplitFilename";
	public static final String KEY_MAX_LINES = "MaxLinesFilename";
	public static final String KEY_THUMBSORT = "ThumbSort";
	public static final String KEY_PARENTMOVE = "ParentMove";
	public static final String KEY_FILEDELMENU = "FileDelMenu";
	public static final String KEY_FILERENMENU = "FileRenMenu";
	public static final String KEY_BRIGHT = "Bright";
	public static final String KEY_GAMMA = "Gamma";
	public static final String KEY_BKLIGHT = "BkLight";
	public static final String KEY_MOIRE = "Moire";
	public static final String KEY_GRAY = "Gray";
	public static final String KEY_INVERT = "Invert";
	public static final String KEY_TOPSINGLE = "TopSingle";
	public static final String KEY_MAXTHREAD = "MaxThread";
	public static final String KEY_THUMBNAILTAP = "ThumbnailTap";
	public static final String KEY_MENULONGTAP = "MenuLongTap";
	public static final String KEY_LOUPESIZE = "LoupeSize";
	public static final String KEY_SCRLNEXT = "ScrollNext";
	public static final String KEY_VIEWNEXT = "ViewNextPage";
	public static final String KEY_NEXTFILTER = "NextPageFilter";
	public static final String KEY_CHGPAGEKEY = "ChgPageKey";

	public static final String KEY_SHOWTOOLBAR = "ShowToolbar";
	public static final String KEY_SHOWSELECTOR = "ShowSelector";
	public static final String KEY_TOOLBARNAME = "ToolbarName";
	public static final String KEY_TOOLBARSEEK = "ToolbarSeek";
	public static final String KEY_LISTTHUMBSEEK = "ListThumbSeek";
	public static final String KEY_THUMBSEEK = "ThumbSeek";
	public static final String KEY_THUMBSIZEW = "ThumbSizeW";
	public static final String KEY_THUMBSIZEH = "ThumbSizeH";

	public static final String KEY_PNUMDISP = "PnumDisp";
	public static final String KEY_PNUMFORMAT = "PnumFormat";
	public static final String KEY_PNUMPOS = "PnumPos";
	public static final String KEY_PNUMSIZE = "PnumSizeSp";
	public static final String KEY_PNUMCOLOR = "PnumColor";

	public static final String KEY_TIMEDISP = "TimeDisp";
	public static final String KEY_TIMEFORMAT = "TimeFormat";
	public static final String KEY_TIMEPOS = "TimePos";
	public static final String KEY_TIMESIZE = "TimeSizeSp";
	public static final String KEY_TIMECOLOR = "TimeColor";

	public static final String KEY_LASTPAGE = "LastPage";
	public static final String KEY_SAVEPAGE = "SavePage";
	public static final String KEY_PREVREV = "PrevRev";
	public static final String KEY_INITVIEW = "InitView";
	public static final String KEY_QUALITY = "Quality";
	public static final String KEY_PAGEWAY = "PageWay";
	public static final String KEY_FITDUAL = "FitDual";
	public static final String KEY_CMARGIN = "CMargin";
	public static final String KEY_CSHADOW = "CShadow";
	public static final String KEY_NOEXPAND = "NoExpand";
	public static final String KEY_VIBFLAG = "VibFlag";
	public static final String KEY_EFFECTLIST = "EffectList";
	public static final String KEY_DELSHARE = "DelShare";
	public static final String KEY_TAPSCRL = "TapScrl";
	public static final String KEY_FLICKPAGE = "FlickPage";
	public static final String KEY_FLICKEDGE = "FlickEdge";
	public static final String KEY_TOPMENU = "TopMenu";

	// スケーリング設定
	public static final String KEY_ALGOMODE = "AlgoMode";

	public static final String KEY_MEMSIZE = "MemSize";
	public static final String KEY_MEMNEXT = "MemNext";
	public static final String KEY_MEMPREV = "MemPrev";

	public static final int DEFAULT_INISCHALE = 5; //全体を表示(見開き対応)
	public static final int DEFAULT_INITVIEW = 1; //見開き表示
	public static final int DEFAULT_QUALITY = 1; //画質を優先する
	public static final int DEFAULT_CLICKAREA = 30; //上下の操作エリアサイズ:60sp
	public static final int DEFAULT_PAGERANGE = 5; //ページ選択の感度:1ページ/5sp
	public static final int DEFAULT_SCROLL = 2;
	public static final int DEFAULT_ORGWIDTH = 0;
	public static final int DEFAULT_ORGHEIGHT = 0;
	public static final int DEFAULT_TAPRANGE = 6; // スクロール開始の感度
	public static final int DEFAULT_MARGIN = 0;
	public static final int DEFAULT_LONGTAP = 4;
	public static final int DEFAULT_MENULONGTAP = 8; // 0.8秒
	public static final int DEFAULT_WADJUST = 25; // -25～+25(1%単位)
	public static final int DEFAULT_WSCALING = 25; // -25～+25(1%単位)
	public static final int DEFAULT_SCALING = 75; // -75～+125(1%単位)
	public static final int DEFAULT_CENTER = 2; // ドット
	public static final int DEFAULT_GRADATION = 5; // 0～30%(1%単位)
	public static final int DEFAULT_FONTTITLE = 10; // 28sp
	public static final int DEFAULT_FONTMAIN = 14; // 28sp
	public static final int DEFAULT_FONTSUB = 8; // 22sp
	public static final int DEFAULT_FONTTILE = 6; // 16sp
	public static final int DEFAULT_MEMSIZE = 18; // キャッシュサイズ:200MByte
	public static final int DEFAULT_MEMNEXT = 16; // キャッシュ前方:32ページ
	public static final int DEFAULT_MEMPREV = 8; // キャッシュ後方:16ページ
	public static final int DEFAULT_NOISESCRL = 1; // 20ドット
	public static final int DEFAULT_NOISEUNDER = 8; // 800
	public static final int DEFAULT_NOISEOVER = 15; // 1500
	public static final int DEFAULT_VOLSCRL = 6; // 32ドット
	public static final int DEFAULT_SCRLRNGW = 6; // 35% ((6+1)*5)
	public static final int DEFAULT_SCRLRNGH = 6; // 35% ((6+1)*5)
	public static final int DEFAULT_ITEMMARGIN = 10; // 項目の余白:10sp
	public static final int DEFAULT_EFFECTTIME = 3; // スクロール時間:250msec
	public static final int DEFAULT_MOMENTMODE = 8; // スクロール減速:フレーム1/8ずつ減速
	public static final int DEFAULT_AUTOPLAY = 2; // 1.5sec(0.5 * (5 + 1))
	public static final boolean DEF_SAVEPAGE = true; // ページ移動時にしおりを保存
	public static final int DEFAULT_TAPPATTERN = 0; // タッチ位置のパターン:左右分割
	public static final int DEFAULT_TAPRATE = 4; // 50% : 50%
	public static final boolean DEFAULT_CHGPAGE = true; // タップ操作の入替え:YES(縦書き、漫画)
	public static final boolean DEFAULT_PREVREV = true; // 前ページに戻った時に逆から表示
	public static final boolean DEFAULT_CHGPAGEKEY = false; // 左右キー操作の入替え

	public static final boolean DEFAULT_PNUMDISP = false; // ページ番号表示しない
	public static final int DEFAULT_PNUMFORMAT = 0; // page / total
	public static final int DEFAULT_PNUMPOS = 4; // 中央下
	public static final int DEFAULT_PNUMSIZE = 10; // 16px (8 + 6)
	public static final int DEFAULT_PNUMCOLOR = 0; // 0 = 白, 1 = 黒

	public static final boolean DEFAULT_TIMEDISP = false; // 時刻と充電表示しない
	public static final int DEFAULT_TIMEFORMAT = 0; // 24:00
	public static final int DEFAULT_TIMEPOS = 5; // 中央下
	public static final int DEFAULT_TIMESIZE = 10; // 16px (8 + 6)
	public static final int DEFAULT_TIMECOLOR = 0; // 0 = 白, 1 = 黒

	public static final int DEFAULT_TOOLBARSEEK = 14; // 38 (8+24)
	public static final int DEFAULT_THUMBSIZEW = 23; // 270 (23 * 10 + 40)
	public static final int DEFAULT_THUMBSIZEH = 28; // 320 (28 * 10 + 40)
	public static final int DEFAULT_LISTTHUMBSIZEH = 20; // 240 (20 * 10 + 40)

	public static final int MAX_SCROLL = 9;
	public static final int MAX_CLICKAREA = 100;
	public static final int MAX_PAGERANGE = 25;
	public static final int MAX_ORGWIDTH = 32;
	public static final int MAX_ORGHEIGHT = 32;
	public static final int MAX_TAPRANGE = 50; // スクロール開始の感度
	public static final int MAX_MARGIN = 20;
	public static final int MAX_LONGTAP = 16;
	public static final int MIN_MENULONGTAP = 0; // 0.5秒
	public static final int MAX_MENULONGTAP = 40; // 4秒
	public static final int MAX_WADJUST = DEFAULT_WADJUST * 2;
	public static final int MAX_WSCALING = DEFAULT_WSCALING * 2;
	public static final int MAX_SCALING = 225;
	public static final int MAX_CENTER = 30; // 30ドット
	public static final int MAX_GRADATION = 20; // 20%
	public static final int MAX_FONTTITLE = 44; // 50ドット
	public static final int MAX_FONTMAIN = 44; // 50ドット
	public static final int MAX_FONTSUB = 44; // 50ドット
	public static final int MAX_FONTTILE = 44; // 50ドット
	public static final int MAX_MEMSIZE = 198; // 2000MByte
	public static final int MAX_MEMNEXT = 100; // 200ページ
	public static final int MAX_MEMPREV = 100; // 200ページ
	public static final int MAX_NOISESCRL = 39; // 200ドット
	public static final int MAX_NOISEUNDER = 50; // 5000
	public static final int MAX_NOISEOVER = 50; // 5000
	public static final int MAX_VOLSCRL = 39; // 200ドット
	public static final int MAX_SCRLRNGW = 19; // 100% ((19+1)*5)
	public static final int MAX_SCRLRNGH = 19; // 100% ((19+1)*5)
	public static final int MAX_ITEMMARGIN = 30; // 50ドット
	public static final int MAX_EFFECTTIME = 20; // 1000msec
	public static final int MAX_MOMENTMODE = 16; // 1000msec
	public static final int MAX_AUTOPLAY = 59; // 30sec (0.5*(59+1))

	public static final int MAX_TOOLBARSEEK = 36; // 60 (36+24)
	public static final int MAX_THUMBSIZE = 60; // 640 (60 * 10 + 40)
	public static final int MAX_LISTTHUMBSIZE = 60; // 640 (60 * 10 + 40)

	public static final int MAX_PNUMSIZE = 54; // 6 + 24 = 60px
	// テキストビュアー設定
	public static final String KEY_TX_INISCALE = "txIniScale";
	public static final String KEY_TX_INITVIEW = "txInitView";
	public static final String KEY_TX_VIEWROTA = "txViewRota";
	public static final String KEY_TX_PAPER = "txPaperSize";
	public static final String KEY_TX_PICSIZE = "txPicSize";
	public static final String KEY_TX_NOTICE = "txNotice";
	public static final String KEY_TX_NOSLEEP = "txNoSleep";
	public static final String KEY_TX_CMARGIN = "txCMargin";
	public static final String KEY_TX_CSHADOW = "txCShadow";
	public static final String KEY_TX_EFFECT = "txEffect";
	public static final String KEY_TX_FONTDL = "txFontDL";
	public static final String KEY_TX_FONTNAME = "txFontName";
	public static final String KEY_TX_PAGESELECT = "txPageSelect";
	public static final String KEY_TX_ASCMODE = "txAscMode";
	public static final String KEY_EP_VIEWER = "epubViewer";

	public static final String KEY_TX_MGNRGB = "txMgnRGB";
	public static final String KEY_TX_CNTRGB = "txCntRGB";
	public static final String KEY_TX_GUIRGB = "txGuiRGB";

	public static final String KEY_TX_TVTRGB = "txTvtRGB";
	public static final String KEY_TX_TVBRGB = "txTvbRGB";
	public static final String KEY_TX_TVGRGB = "txTvgRGB";
	public static final String KEY_TX_GRADATION = "txGradation";
	public static final String KEY_TX_HITRGB = "txHitRGB";

	public static final String KEY_TX_FONTTOP = "txFontTopSp";
	public static final String KEY_TX_FONTBODY = "txFontBodySp";
	public static final String KEY_TX_FONTRUBI = "txFontRubiSp";
	public static final String KEY_TX_FONTINFO = "txFontInfoSp";
	public static final String KEY_TX_SPACEW = "txSpaceW";
	public static final String KEY_TX_SPACEH = "txSpaceH";
	public static final String KEY_TX_MARGINW = "txMarginW";
	public static final String KEY_TX_MARGINH = "txMarginH";

	public static final String KEY_TX_SCRLRNGW = "txScrlRngW";
	public static final String KEY_TX_SCRLRNGH = "txScrlRngH";

	public static final String KEY_TX_BKLIGHT = "txBright";

	public static final int DEFAULT_TX_FONTTOP = 18; // 32(8～64)
	public static final int DEFAULT_TX_FONTBODY = 14; // 24(8～64)
	public static final int DEFAULT_TX_FONTRUBI = 6; // 16(8～64)
	public static final int DEFAULT_TX_FONTINFO = 10; // 18(8～64)
	public static final int DEFAULT_TX_SPACEW = 8; // 8(0～50)
	public static final int DEFAULT_TX_SPACEH = 2; // 8(0～50)
	public static final int DEFAULT_TX_MARGINW = 16; // 32(0～100,x2)
	public static final int DEFAULT_TX_MARGINH = 16; // 32(0～100,x2)
	public static final int DEFAULT_TX_SCRLRNGW = 6; // 35% ((6+1)*5)
	public static final int DEFAULT_TX_SCRLRNGH = 6; // 35% ((6+1)*5)

	public static final int MAX_TX_FONTTOP = 56; // 64ドット
	public static final int MAX_TX_FONTBODY = 56; // 64ドット
	public static final int MAX_TX_FONTRUBI = 56; // 64ドット
	public static final int MAX_TX_FONTINFO = 56; // 64ドット
	public static final int MAX_TX_SPACEW = 50; // 50ドット
	public static final int MAX_TX_SPACEH = 50; // 50ドット
	public static final int MAX_TX_MARGINW = 50; // 100ドット
	public static final int MAX_TX_MARGINH = 50; // 100ドット
	public static final int MAX_TX_SCRLRNGW = 19; // 100% ((19+1)*5)
	public static final int MAX_TX_SCRLRNGH = 19; // 100% ((19+1)*5)

	public static final int COLOR_TX_TVTRGB = 0xFF000000;
	public static final int COLOR_TX_TVBRGB = 0xFFFFFFC8;
	public static final int COLOR_TX_TVGRGB = 0xFFFFFFFF;
	public static final int COLOR_TX_HITRGB = 0xFFEBC5E2;

	public static final int VOLKEY_NONE = 0;
	public static final int VOLKEY_DOWNTONEXT = 1;
	public static final int VOLKEY_UPTONEXT = 2;

	// サムネイルサイズ
	public static final int THUMBID_NONE = -1;
	public static final int THUMBSIZE_NONE = -1;

	public static final int THUMBSTATE_NONE = -1;
	public static final int THUMBSTATE_NOLOAD = -2;
	public static final int THUMBSTATE_ERROR = -3;

	public static final int CACHE_MAXSTORE = 500;

	// 3秒以内にbackを押せば終了
	public static final long MILLIS_EXITTIME = 2000;
	public static final long MILLIS_DELETECHE = (10 * 60 * 1000); // 10分

	public static final int PAPERSEL_SCREEN = 0;
	public static final int PAPERSIZE[][] = {{0, 0}, {800, 1280}, {720, 1280}, {540, 960}, {480, 800}};

	public static final String URL_IMAGEDETIAL = "";
	public static final String URL_FILESELECT = "https://docs.google.com/document/d/197jmNnXY3BP4F9HHmJCuroWslbTFH8nWTBVWbn8T4dE/edit";
	public static final String URL_COMMON = "";
	public static final String URL_IMAGEVIEW = "";
	public static final String URL_SERVER = "";

	public static final int DISPMODE_TX_DUAL = 0;
	public static final int DISPMODE_TX_HALF = 1;
	public static final int DISPMODE_TX_SERIAL = 2;

	public static final int LASTOPEN_NONE = 0;
	public static final int LASTOPEN_TEXT = 1;
	public static final int LASTOPEN_IMAGE = 2;
	public static final int LASTOPEN_EPUB = 3;

	public static final int SHOWMENU_NONE = 0;
	public static final int SHOWMENU_ALWAYS = 1;
	public static final int SHOWMENU_LOCAL = 2;
	public static final int SHOWMENU_SERVER = 3;

	public static final int ColorList[] = {Color.rgb(0, 0, 0) // 0
			, Color.rgb(255, 255, 255) // 1
			, Color.rgb(0, 0, 255) // 2
			, Color.rgb(255, 0, 0) // 3
			, Color.rgb(255, 0, 255) // 4
			, Color.rgb(0, 255, 0) // 5
			, Color.rgb(255, 255, 0) // 6
			, Color.rgb(0, 255, 255) // 7
			, Color.rgb(129, 129, 129) // 8
			, Color.rgb(128, 128, 255) // 9
			, Color.rgb(255, 128, 128) // 10
			, Color.rgb(255, 128, 255) // 11
			, Color.rgb(128, 255, 128) // 12
			, Color.rgb(255, 255, 128) // 13
			, Color.rgb(128, 255, 255) // 14
			, Color.rgb(193, 193, 193) // 15
			, Color.rgb(0, 0, 97) // 16
			, Color.rgb(97, 0, 0) // 17
			, Color.rgb(97, 0, 97) // 18
			, Color.rgb(0, 97, 0) // 19
			, Color.rgb(97, 97, 0) // 20
			, Color.rgb(0, 97, 97) // 21
			, Color.rgb(97, 97, 97)}; // 22
	public static final int GuideList[] = {0x80000000 // 0 : 黒
			, 0x80000070 // 1 : 青
			, 0x80700000 // 2 : 赤
			, 0x80700070 // 3 : マゼンタ
			, 0x80007000 // 4 : 緑
			, 0x80707000 // 5 : 黄
			, 0x80007070}; // 6 : シアン

	public static final int RotateBtnList[] = {0, KeyEvent.KEYCODE_FOCUS // フォーカスキー
			, KeyEvent.KEYCODE_CAMERA // シャッターキー
	};
	public static final String CharsetList[] = {"UTF-8", "Shift_JIS", "EUC-JP", "EUC-KR", "Big5", "CB2312", "GB18030", "Big5-HKSCS"};

	public static final int ROTATE_AUTO = 0;
	public static final int ROTATE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

	public static final int ROTATE_LANDSCAPE = 2;
	public static final int ROTATE_PSELAND = 3;

	public static final int TEXTSIZE_MESSAGE = 18;
	public static final int TEXTSIZE_SUMMARY = 14;
	public static final int TEXTSIZE_EDIT = 20;

	public static final int THUMBNAIL_PAGESIZE = 2 * 1024 * 1024;    // サムネイルバッファサイズ(4MB)
	public static final int THUMBNAIL_BLOCK = 8 * 1024;                // サムネイルブロックサイズ(8KB)
	public static final int THUMBNAIL_MAXPAGE = 4;                    // サムネイルバッファ数

	public static final boolean TEXT_VIEWER = false;
	public static final boolean IMAGE_VIEWER = true;

	public static final int PAGENUMBER_UNREAD = -1;    // 未読
	public static final int PAGENUMBER_READ = -2;        // 既読

	// 縦長チェック
	static public boolean checkPortrait(int cx, int cy) {
		return cx <= cy ? true : false;
	}

	static public boolean checkPortrait(int cx, int cy, int rotate) {
		if (rotate == 0 || rotate == 2) {
			return cx <= cy ? true : false;
		} else {
			return cy <= cx ? true : false;
		}
	}

	static public boolean checkHiddenFile(String path) {
		String folder[] = path.split("/");
		if (folder.length == 0) {
			return true;
		}
		// ファイル名が .で始まる
		String top = folder[folder.length - 1].substring(0, 1);
		if (top.equals(".")) {
			return true;
		}
		return false;
	}

	// 色を取得
	public static int getColorValue(SharedPreferences sp, String keyOld, String keyRGB, int defColor) {
		String str = "";
		int index = -1;
		int val;

		// 旧設定から読み込み
		if (keyOld != null) {
			str = sp.getString(keyOld, "");
		}
		if (!str.equals("")) {
			index = Integer.parseInt(str);
		}
		if (0 <= index && index < DEF.ColorList.length) {
			// 旧設定がある場合は色情報を取得
			val = DEF.ColorList[index];
		} else {
			// 色情報
			val = sp.getInt(keyRGB, DEF.ColorList[defColor]);
		}
		return val;
	}

	// 色を取得
	public static int getGuideValue(SharedPreferences sp, String keyOld, String keyRGB, int defColor) {
		String str = "";
		int index = -1;
		int val;

		// 旧設定から読み込み
		if (keyOld != null) {
			str = sp.getString(keyOld, "");
		}
		if (!str.equals("")) {
			index = Integer.parseInt(str);
		}
		if (0 <= index && index < DEF.GuideList.length) {
			// 旧設定がある場合は色情報を取得
			val = DEF.GuideList[index];
		} else {
			// 色情報
			val = sp.getInt(keyRGB, DEF.GuideList[defColor]);
		}
		return val;
	}

	// 色の補正
	public static int calcColor(int color, int gap) {
		int r = (color >> 16) & 0x000000FF;
		int g = (color >> 8) & 0x000000FF;
		int b = (color >> 0) & 0x000000FF;
		r += gap;
		g += gap;
		b += gap;
		r = r > 255 ? 255 : r;
		g = g > 255 ? 255 : g;
		b = b > 255 ? 255 : b;
		r = r < 0 ? 0 : r;
		g = g < 0 ? 0 : g;
		b = b < 0 ? 0 : b;
		return Color.rgb(r, g, b);
	}

	// 色の補正
	public static int calcGradation(int color, int to, int rate) {
		int r = (color >> 16) & 0x000000FF;
		int g = (color >> 8) & 0x000000FF;
		int b = (color >> 0) & 0x000000FF;
		r = (to - r) * rate / 100 + r;
		g = (to - g) * rate / 100 + g;
		b = (to - b) * rate / 100 + b;
		return Color.rgb(r, g, b);
	}

	// 色のマージ
	public static int margeColor(int from, int to, int rate, int total) {
		int r1 = (from >> 16) & 0x000000FF;
		int g1 = (from >> 8) & 0x000000FF;
		int b1 = (from >> 0) & 0x000000FF;
		int r2 = (to >> 16) & 0x000000FF;
		int g2 = (to >> 8) & 0x000000FF;
		int b2 = (to >> 0) & 0x000000FF;
		r1 += (r2 - r1) * rate / total;
		g1 += (g2 - g1) * rate / total;
		b1 += (b2 - b1) * rate / total;
		return Color.rgb(r1, g1, b1);
	}

	static public String makeCode(String str, int thum_cx, int thum_cy) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			String path = str + ":" + thum_cx + "x" + thum_cy;
			digest.update(path.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(messageDigest[i] & 0x00FF));
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			Log.e("makeCode", "NoSuchAlgorithmException");
		}
		return "";
	}

	static public boolean setRotation(Activity act, int rotate) {
		int way = act.getRequestedOrientation();
		// 回転制御
		if (rotate == DEF.ROTATE_PORTRAIT || rotate == ROTATE_PSELAND) {
			// 縦固定 又は 疑似横画面
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if (way == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				return true;
			}
		} else if (rotate == DEF.ROTATE_LANDSCAPE) {
			// 横固定
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (way == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				return true;
			}
		} else {
			// 回転あり
			act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
		return false;
	}

	static public int calcThumbnailScale(int width, int height, int tcx, int tcy) {
		if (height >= tcy) {
			return height / tcy;
		} else {
			return 1;
		}
	}

	static public String getSizeStr(int val, String summ1, String summ2) {
		String str;

		if (val == 0) {
			str = summ1;
		} else {
			str = val * 100 + " " + summ2;
		}
		return str;
	}

	// スクロール倍率の計算
	static public int calcScroll(int val) {
		return val + 1;
	}

	// ドット数の計算(等倍)
	static public int calcRange1x(int val) {
		return val;
	}

	// 上下操作エリア
	static public int calcClickArea(int val) {
		return val * 2;
	}

	// 上下操作エリア (sp計算)
	static public int calcClickAreaPix(int val, float density) {
		return (int) (calcClickArea(val) * density);
	}

	// ページ選択感度
	static public int calcPageRange(int val) {
		return val;
	}

	// ページ選択感度 (sp計算)
	static public int calcPageRangePix(int val, float density) {
		return (int) (calcPageRange(val) * density);
	}

	// スクロール開始感度
	static public int calcTapRange(int val) {
		return val * 2;
	}

	// スクロール開始感度 (sp計算)
	static public int calcTapRangePix(int val, float density) {
		return (int) (calcTapRange(val) * density);
	}

	// イメージ、テキストのマージン
	static public int calcDispMargin(int val) {
		return val * 2;
	}

	// ミリ秒の計算
	static public int calcMSec100(int val) {
		return val * 100;
	}

	// ミリ秒の計算
	static public int calcMSec200(int val) {
		return val * 200;
	}

	// エフェクト時間の計算(msec)
	static public int calcEffectTime(int val) {
		return val * 50;
	}

	// 自動再生間隔の計算(100msec)
	static public int calcAutoPlay(int val) {
		return (val + 1) * 500;
	}

	// 縦横比(%)の計算
	static public int calcWAdjust(int val) {
		return (val - DEF.DEFAULT_WADJUST) + 100;
	}

	// 補正(%)の計算
	static public int calcWScaling(int val) {
		return (val - DEF.DEFAULT_WSCALING) + 100;
	}

	// 拡大縮小(%)の計算
	static public int calcScaling(int val) {
		return (val - DEF.DEFAULT_SCALING) + 100;
	}

	// フォントサイズの計算
	static public int calcFont(int val) {
		return (val + 6); // 最小値:2sp
	}

	// フォントサイズの計算(px)
	static public int calcFontPix(int val, float density) {
		return calcSpToPix(calcFont(val), density); // 最小値:8ドット
	}

	// ポイントをpxに変換
	static public int calcSpToPix(int val, float density) {
		return (int) (val * density);
	}

	// ツールバーサイズの計算
	static public int calcToolbarSize(int val) {
		return (val + 24); // 最小値:12sp
	}

	// ツールバーサイズの計算
	static public int calcToolbarPix(int val, float density) {
		return (int) (calcToolbarSize(val) * density); // 最小値:12sp
	}

	// サムネイルサイズの計算
	static public int calcThumbnailSize(int val) {
		return (val * 10 + 40); // 最小値:40px
	}

	// メモリサイズ
	static public int calcMemSize(int val) {
		return val * 10 + 20;
	}

	// キャッシュページ数
	static public int calcMemPage(int val) {
		return val * 2;
	}

	// スクロールドット数
	static public int calcScrlSpeed(int val) {
		return (val + 1);
	}

	// スクロールドット数 (sp単位)
	static public int calcScrlSpeedPix(int val, float density) {
		return (int) (calcScrlSpeed(val) * density);
	}

	// スクロールドット数
	static public int calcScrlRange(int val) {
		return (val + 1) * 5;
	}

	// ノイズレベル
	static public int calcNoiseLevel(int val) {
		return val * 100;
	}

	// ページ番号表示
	static public int calcPnumSize(int val) {
		return val + 6;
	}

	// ページ番号表示
	static public int calcPnumSizePix(int val, float density) {
		return (int) (calcPnumSize(val) * density);
	}

	// 保持件数
	static public int calcSaveNum(int val) {
		return val * 20;
	}

	// サマリ文字列作成
	static public String getScrollStr(int val, String summ1, String summ2) {
		return summ1 + " " + calcScroll(val) + " " + summ2;
	}

	// 操作エリアの文字列
	static public String getClickAreaStr(int val, String summ1) {
		return calcClickArea(val) + " " + summ1;
	}

	// ページ選択感度文字列
	static public String getPageRangeStr(int val, String summ1) {
		return calcPageRange(val) + " " + summ1;
	}

	// スクロール開始感度文字列
	static public String getTapRangeStr(int val, String summ1) {
		return calcTapRange(val) + " " + summ1;
	}

	// イメージ、テキストのマージン文字列
	static public String getDispMarginStr(int val, String summ1) {
		return calcDispMargin(val) + " " + summ1;
	}

	// サマリ文字列作成
	static public String getMarginStr(int val, String summ1) {
		return val + " " + summ1;
	}

	// テキストの字間文字列
	static public String getTextSpaceStr(int val, String summ1) {
		return val + " " + summ1;
	}

	// サマリ文字列作成(ミリ秒)
	static public String getMSecStr100(int val, String summ1) {
		int msec = calcMSec100(val);
		return (msec / 1000) + "." + (msec / 100 % 10) + " " + summ1;
	}

	// サマリ文字列作成(ミリ秒)
	static public String getMSecStr200(int val, String summ1) {
		int msec = calcMSec200(val);
		return (msec / 1000) + "." + (msec / 100 % 10) + " " + summ1;
	}

	// サマリ文字列作成(エフェクト時間)
	static public String getEffectTimeStr(int val, String summ1) {
		int msec = calcEffectTime(val);
		String m1 = "" + (msec / 1000);
		String m2 = "0" + (msec / 10 % 100);
		if (m2.length() >= 3) {
			m2 = m2.substring(1);
		}
		return m1 + "." + m2 + " " + summ1;
	}

	// サマリ文字列作成(自動再生間隔)
	static public String getAutoPlayStr(int val, String summ1) {
		int itv = calcAutoPlay(val);
		String m1 = "" + (itv / 1000);
		String m2 = "" + (itv / 100 % 10);
		return m1 + "." + m2 + " " + summ1;
	}

	// サマリ文字列作成(減速率)
	static public String getMomentModeStr(int val, String summ1, String summ2) {
		String str;

		if (val == 0) {
			str = summ1;
		} else if (val == MAX_MOMENTMODE) {
			str = summ2;
		} else {
			str = "" + val;
		}
		return str;
	}

	// サマリ文字列作成(100 : ?)
	static public String getAdjustStr(int val, String summ1, String summ2) {
		// 75～125
		int percent = calcWAdjust(val);
		String str;

		// if (percent != 100) {
		str = percent + " : 100" + " " + summ1;
		// }
		// else{
		// str = summ2;
		// }
		return str;
	}

	// サマリ文字列作成(-25%～+25%)
	static public String getWScalingStr(int val, String summ1, String summ2) {
		// -25 ～ +25
		int percent = calcWScaling(val) - 100;
		String str;

		if (percent < 0) {
			str = percent + " " + summ1;
		} else if (percent > 0) {
			str = "+" + percent + " " + summ1;
		} else {
			str = summ2;
		}
		return str;
	}

	// サマリ文字列作成(25%～+250%)
	static public String getScalingStr(int val, String summ1, String summ2) {
		int percent = calcScaling(val);
		String str;

		if (percent == 100) {
			str = summ2;
		} else {
			str = percent + " " + summ1;
		}
		return str;
	}

	// サマリ文字列作成(1 dot)
	static public String getCenterStr(int val, String summ1) {
		// 0.1%単位
		return val + " " + summ1;
	}

	// サマリ文字列作成(0%～)
	static public String getGradationStr(int val) {
		// 1%単位
		return val + " %";
	}

	// フォント用サマリ文字列作成(xx sp)
	static public String getFontSpStr(int val, String summ1) {
		// ドット単位
		return calcFont(val) + " " + summ1;
	}

	// 余白サマリ文字列作成(xx sp)
	static public String getMarginSpStr(int val, String summ1) {
		// ドット単位
		return val + " " + summ1;
	}

	// ツールバーサイズ文字列作成(xx sp)
	static public String getToolbarSeekStr(int val, String summ1) {
		// ドット単位
		return calcToolbarSize(val) + " " + summ1;
	}

	// リストサムネイルサイズ文字列作成(xx sp)
	static public String getListThumbSeekStr(int val, String summ1) {
		// ドット単位
		return "H: " + calcThumbnailSize(val) + " " + summ1;
	}

	// サムネイルサイズサマリ文字列作成(??sp x ??sp)
	static public String getThumbnailStr(int w, int h, String summ1) {
		// ドット単位
		return "W: " + calcThumbnailSize(w) + " " + summ1 + " x " + "H: " + calcThumbnailSize(h) + " " + summ1;
	}

	// キャッシュメモリサイズ
	static public String getMemSizeStr(int val, String summ1, String summ2) {
		// if (val == 0) {
		// return summ2;
		// }
		return calcMemSize(val) + " " + summ1;
	}

	// キャッシュページ数
	static public String getMemPageStr(int val, String summ1) {
		return calcMemPage(val) + " " + summ1;
	}

	// スクロールドット数
	static public String getScrlSpeedStr(int val, String summ1) {
		return calcScrlSpeed(val) + " " + summ1;
	}

	// スクロール量
	static public String getScrlRangeStr(int val, String summ1, String summ2) {
		return summ1 + ' ' + calcScrlRange(val) + " " + summ2;
	}

	// ページ番号表示サイズ
	static public String getPnumSizeStr(int val, String summ) {
		return calcPnumSize(val) + " " + summ;
	}

	// 音量レベル
	static public String getNoiseLevelStr(int val) {
		return "" + calcNoiseLevel(val);
	}

	// 2バイト数値取得
	static public short getShort(byte b[], int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00);

		return (short) val;
	}

	// 4バイト数値取得
	static public int getInt(byte b[], int pos) {
		int val;
		val = ((int) b[pos] & 0x000000FF) | (((int) b[pos + 1] << 8) & 0x0000FF00) | (((int) b[pos + 2] << 16) & 0x00FF0000) | (((int) b[pos + 3] << 24) & 0xFF000000);

		return val;
	}

	static public String getFontDirectory() {
		return DEF.getBaseDirectory() + "font/";
	}

	static public String getConfigDirectory() {
		return DEF.getBaseDirectory() + "conf/";
	}

	// パス部分のみ取得
	static public String getDir(String filepath) {
		int prev = 0;

		if (filepath == null) {
			return "";
		}
		while (true) {
			int pos = filepath.indexOf('/', prev);
			if (pos == -1) {
				break;
			}
			prev = pos + 1;
		}
		return filepath.substring(0, prev);
	}

	// ファイル拡張子の取得
	static public String getFileExt(String filepath) {
		String ext = "";
		if (filepath != null) {
			int pos = filepath.lastIndexOf('.');

			if (pos >= 0) {
				// 拡張子切り出し
				ext = filepath.substring(pos).toLowerCase();
			}
		}
		return ext;
	}

	// 拡張子の取得
	public static String getExtension(String filename) {
		if (filename != null) {
			int extpos = filename.lastIndexOf('.');
			int slashpos = filename.lastIndexOf('/');
			if (slashpos < extpos && extpos >= 1) {
				return filename.substring(extpos).toLowerCase();
			}
		}
		return "";
	}

	// 番号とか考慮しながら文字列比較
	private static final int CHTYPE_NUM = 1;
	private static final int CHTYPE_CHAR = 2;
	private static final int CHTYPE_KANJI_NUMERALS = 3;
	private static final int CHTYPE_JAPANESE_VOLUME_NAME = 4;
	private static final int CHTYPE_SYMBOL_BEFORE = 5;
	private static final int CHTYPE_SYMBOL_AFTER = 6;

	public static boolean SORT_BY_IGNORE_WIDTH = true;
	public static boolean SORT_BY_IGNORE_CASE = true;
	public static boolean SORT_BY_SYMBOL = true;
	public static boolean SORT_BY_NATURAL_NUMBERS = true;
	public static boolean SORT_BY_KANJI_NUMERALS = true;
	public static boolean SORT_BY_JAPANESE_VOLUME_NAME = true;
	public static String[] PRIORITY_WORDS = new String[0];

	static public int compareFileName(final String str1, final String str2) {
		boolean debug = false;

		String name1 = str1;
		String name2 = str2;

		if (name1 == null && name2 == null) {
			return 0;
		}
		if (name1 == null) {
			return 1;
		}
		if (name2 == null) {
			return -1;
		}

		if (debug) {Log.d("DEF","compareFileName 開始します. name1=" + name1 + ", name2=" + name2);}

		int i1, i2;
		char ch1, ch2;
		int ct1, ct2;
		String ext1 = "", ext2 = "";

		if (SORT_BY_IGNORE_WIDTH) {
			// 全角を半角に変換
			name1 = Normalizer.normalize(name1, Normalizer.Form.NFKC);
			name2 = Normalizer.normalize(name2, Normalizer.Form.NFKC);
		}

		if (SORT_BY_IGNORE_CASE) {
			// 小文字を大文字に変換
			name1 = name1.toUpperCase();
			name2 = name2.toUpperCase();
		}

		if (name1.lastIndexOf('.') > name1.lastIndexOf('/')) {
			name1 = name1.substring(0, name1.lastIndexOf('.'));
			ext1 = name1.substring(name1.lastIndexOf('.') + 1);
		}
		if (name2.lastIndexOf('.') > name2.lastIndexOf('/')) {
			name2 = name2.substring(0, name2.lastIndexOf('.'));
			ext2 = name2.substring(name2.lastIndexOf('.') + 1);
		}

		int len1 = name1.length();
		int len2 = name2.length();

		for (i1 = i2 = 0; i1 < len1 && i2 < len2; i1++, i2++) {
			if (debug) {Log.d("DEF","compareFileName ループを実行します. i1=" + i1 + ", i2=" + i2 + ", name1=" + name1 + ", name2=" + name2);}
			ch1 = name1.charAt(i1);
			ch2 = name2.charAt(i2);
			ct1 = getCharType(ch1);
			ct2 = getCharType(ch2);

			if (PRIORITY_WORDS != null && PRIORITY_WORDS.length > 0) {
				// 優先単語があれば優先単語を使用している方が先
				int wlen1 = 0;
				int wlen2 = 0;

				for (int i = 0; i < PRIORITY_WORDS.length; ++i) {
					String word;
					if (PRIORITY_WORDS[i] != null && !PRIORITY_WORDS[i].isEmpty()) {
						if (SORT_BY_IGNORE_CASE) {
							word = PRIORITY_WORDS[i].toUpperCase();
						} else {
							word = PRIORITY_WORDS[i];
						}

						if (name1.startsWith(word, i1)) {
							wlen1 = word.length();
						}
						if (name2.startsWith(word, i2)) {
							wlen2 = word.length();
						}
					}
				}
				if (wlen1 > 0 && wlen2 <= 0) {
					return -1;
				}
				if (wlen1 <= 0 && wlen2 > 0) {
					return 1;
				}
				if (wlen1 > 0 && wlen2 > 0) {
					i1 += wlen1 - 1;
					i2 += wlen2 - 1;
				}
			}

			if (SORT_BY_SYMBOL) {
				if (ct1 == CHTYPE_SYMBOL_BEFORE && ct2 != CHTYPE_SYMBOL_BEFORE) {
					// 1が記号で2が記号以外なら、1が先
					return -1;
				} else if (ct1 == CHTYPE_SYMBOL_BEFORE && ct2 == CHTYPE_SYMBOL_BEFORE) {
					// 1も2も記号なら、記号を比べる
					int s1 = getSymbolBefore(ch1);
					int s2 = getSymbolBefore(ch2);
					if (s1 != s2) {
						return s1 - s2;
					}
					else {
						continue;
					}
				} else if (ct1 != CHTYPE_SYMBOL_BEFORE && ct2 == CHTYPE_SYMBOL_BEFORE) {
					// 2が記号で1が記号以外なら、2が先
					return 1;
				}

				else if (ct1 == CHTYPE_SYMBOL_AFTER && ct2 != CHTYPE_SYMBOL_AFTER) {
					// 1が記号で2が記号以外なら、2が先
					return 1;
				}
				else if (ct1 == CHTYPE_SYMBOL_AFTER && ct2 == CHTYPE_SYMBOL_AFTER) {
					// 1も2も記号なら、記号を比べる
					int s1 = getSymbolAfter(ch1);
					int s2 = getSymbolAfter(ch2);
					if (s1 != s2) {
						return s1 - s2;
					}
					else {
						continue;
					}
				}
				else if (ct1 != CHTYPE_SYMBOL_AFTER && ct2 == CHTYPE_SYMBOL_AFTER) {
					// 2が記号で1が記号以外なら、1が先
					return -1;
				}
			}

			if (ct1 != ct2) {
				if (debug) {Log.d("DEF","compareFileName 文字種が違います. ch1=" + ch1 + ", ch2=" + ch2);}
				// 文字種が違う場合
				char tmp1, tmp2;
				if (ct1 == CHTYPE_KANJI_NUMERALS) {
					tmp1 = '一';
				}
				else if (ct1 == CHTYPE_JAPANESE_VOLUME_NAME) {
					tmp1 = '上';
				}
				else {
					tmp1 = ch1;
				}

				if (ct2 == CHTYPE_KANJI_NUMERALS) {
					tmp2 = '一';
				}
				else if (ct2 == CHTYPE_JAPANESE_VOLUME_NAME) {
					tmp2 = '上';
				}
				else {
					tmp2 = ch2;
				}

				if (tmp1 != tmp2) {
					// 元の値を比較
					return tmp1 - tmp2;
				}
				else {
					continue;
				}
			}

			if (SORT_BY_NATURAL_NUMBERS) {
				if (ct1 == CHTYPE_NUM) {
					//Log.d("DEF","compareFileName 文字1=" + ch1 + ", 文字2=" + ch2);
					String num1 = getNumbers(name1, i1);
					String num2 = getNumbers(name2, i2);
					int nlen1 = num1.length();
					int nlen2 = num2.length();

					// カンマを取り除く
					num1 = num1.replace(",", "");
					num2 = num2.replace(",", "");

					// マイナス判定を残しているが、マイナス記号は数字に含めるのをやめた
					// ファイル名ハイフン001がマイナス001になるとおかしくなるから
					boolean minus1 = num1.startsWith("-");
					boolean minus2 = num2.startsWith("-");

					if (minus1 && !minus2) {
						// num2が大きい
						return -1;
					} else if (!minus1 && minus2) {
						// num1が大きい
						return 1;
					} else {
						//Log.d("DEF","compareFileName 数字1=" + num1 + ", 数字2=" + num2);
						//小数点の位置
						int index_dot1 = num1.indexOf(".");
						int index_dot2 = num2.indexOf(".");

						//小数以下の桁数
						int col_dec1;
						int col_dec2;
						if (index_dot1 == -1) {
							col_dec1 = 0;
						} else {
							col_dec1 = nlen1 - index_dot1 - 1;
						}
						if (index_dot2 == -1) {
							col_dec2 = 0;
						} else {
							col_dec2 = nlen2 - index_dot2 - 1;
						}

						// 小数点以下の桁数を合わせる
						int col_diff = col_dec1 - col_dec2;
						for (int i = 1; i <= col_diff; i++) {
							num2 = num2 + "0";
						}
						for (int i = -1; i >= col_diff; i--) {
							num1 = num1 + "0";
						}
						//Log.d("DEF","compareFileName 数字1=" + num1 + ", 数字2=" + num2 + ", 小数点位置1=" + index_dot1 + ", 小数点位置2=" + index_dot2 + ", 小数桁1=" + col_dec1 + ", 小数桁2=" + col_dec2);
						num1 = num1.replace(".", "");
						num2 = num2.replace(".", "");
						//Log.d("DEF","compareFileName 数字1=" + num1 + ", 数字2=" + num2 + ", 小数点位置1=" + index_dot1 + ", 小数点位置2=" + index_dot2 + ", 小数桁1=" + col_dec1 + ", 小数桁2=" + col_dec2);

						int num_len1 = num1.length();
						int num_len2 = num2.length();

						if (!minus1 && !minus2) {
							// どちらも正の数

							if (num_len1 < num_len2) {
								int difflen = num_len2 - num_len1;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber('0') - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければプラス
										return diff;
									}
								}
								// 残り部分で比較
								num2 = num2.substring(difflen);
							} else if (num_len1 > num_len2) {
								int difflen = num_len1 - num_len2;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber('0');
									if (diff != 0) {
										// num1が大きければプラス
										return diff;
									}
								}
								// 残り部分で比較
								num1 = num1.substring(difflen);
							}
							// 数字が異なる場合は比較
							if (num1.length() == num2.length()) {
								for (int i = 0; i < num1.length(); i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければプラス
										return diff;
									}
								}
							} else {
								if (debug) {Log.d("DEF", "compareFileName 長さが違います。 num1=" + num1 + ", num2=" + num2);}
							}
						} else {
							// どちらも負の数

							// マイナスを取り除く
							num1 = num1.replace("-", "");
							num2 = num2.replace("-", "");

							if (num_len1 < num_len2) {
								int difflen = num_len2 - num_len1;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber('0') - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければマイナス
										return -diff;
									}
								}
								// 残り部分で比較
								num2 = num2.substring(difflen);
							} else if (num_len1 > num_len2) {
								int difflen = num_len1 - num_len2;
								for (int i = 0; i < difflen; i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber('0');
									if (diff != 0) {
										// num1が大きければマイナス
										return -diff;
									}
								}
								// 残り部分で比較
								num1 = num1.substring(difflen);
							}
							// 数字が異なる場合は比較
							if (num1.length() == num2.length()) {
								for (int i = 0; i < num1.length(); i++) {
									int diff = getNumber(num1.charAt(i)) - getNumber(num2.charAt(i));
									if (diff != 0) {
										// num1が大きければマイナス
										return -diff;
									}
								}
							} else {
								if (debug) {Log.d("DEF", "compareFileName 長さが違います。 num1=" + num1 + ", num2=" + num2);}
							}
						}
						i1 += nlen1 - 1;
						i2 += nlen2 - 1;
					}
					continue;
				}
			}

			if (SORT_BY_KANJI_NUMERALS) {
				if (ct1 == CHTYPE_KANJI_NUMERALS) {
					if (debug) {Log.d("DEF", "compareFileName 漢数字を比較します. ch1=" + ch1 + ", ch2=" + ch2);}
					String num1 = getKanjiNumerals(name1, i1);
					String num2 = getKanjiNumerals(name2, i2);
					int nlen1 = num1.length();
					int nlen2 = num2.length();
					if (debug) {Log.d("DEF", "compareFileName 漢数字を比較します. num1=" + num1 + ", num2=" + num2);}
					if (nlen1 < nlen2) {
						int difflen = nlen2 - nlen1;
						for (int i = 0; i < difflen; i++) {
							if (getKanjiNumeral(num2.charAt(i)) != 0) {
								// num2の方が大きい
								if (debug) {Log.d("DEF", "compareFileName 漢数字を比較します. num1が小さいです.");}
								return -1;
							}
						}
						// 残り部分で比較
						num2 = num2.substring(difflen);
					} else if (nlen1 > nlen2) {
						int difflen = nlen1 - nlen2;
						for (int i = 0; i < difflen; i++) {
							if (getKanjiNumeral(num1.charAt(i)) > 0) {
								// num1の方が大きい
								if (debug) {Log.d("DEF", "compareFileName 漢数字を比較します. num2が小さいです.");}
								return 1;
							}
						}
						// 残り部分で比較
						num1 = num1.substring(difflen);
					}
					// 数字が異なる場合は比較
					for (int i = 0; i < num1.length(); i++) {
						int diff = getKanjiNumeral(num1.charAt(i)) - getKanjiNumeral(num2.charAt(i));
						if (diff != 0) {
							// num1の方が大きい
							if (debug) {
								if (diff>0) {Log.d("DEF", "compareFileName 漢数字を比較します. num2が小さいです.");}
								else {Log.d("DEF", "compareFileName 漢数字を比較します. num1が小さいです.");}
							}
							return diff;
						}
					}
					i1 += nlen1 - 1;
					i2 += nlen2 - 1;
					continue;
				}
			}

			if (SORT_BY_JAPANESE_VOLUME_NAME) {
				if (ct1 == CHTYPE_JAPANESE_VOLUME_NAME) {
					int s1 = getJapaneseVolumeName(ch1);
					int s2 = getJapaneseVolumeName(ch2);
					if (s1 != s2) {
						return s1 - s2;
					}
					continue;
				}
			}

			if (ch1 != ch2) {
				// 両方とも特殊な文字ではない
				// 元の値を比較
				return ch1 - ch2;
			}
		}

		// 最後まで結果が決まらなかった
		if (name1.compareToIgnoreCase(name2) != 0) {
			// 単純に大小比較してみる
			return name1.compareToIgnoreCase(name2);
		}
		else {
			// 完全一致なら拡張子を比較
			return ext1.compareToIgnoreCase(ext2);
		}
	}

	static private String getNumbers(String str, int idx) {
		int i;
		for (i = idx; i < str.length(); i++) {
			int ch = str.charAt(i);
//			if ((ch < '0' || '9' < ch) && '-' != ch && '.' != ch && ',' != ch) {
			if ((ch < '0' || '9' < ch) && '.' != ch && ',' != ch) {
				break;
			}
		}
		return str.substring(idx, i);
	}

	static private int getNumber(char ch) {
		switch (ch) {
//			case '-':
//				return -1;
			case '0':
				return 0;
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
			case '.':
				return 10;
			case ',':
				return 11;
		}
		return -2;
	}

	static private int getSymbolBefore(char ch) {
		switch (ch) {
			case ' ':		// 半角スペース
				return 0;
			case '　':		// 全角スペース
				return 1;
			case '!':
				return 2;
			case '！':
				return 3;
			case '#':
				return 4;
			case '＃':
				return 5;
			case '$':
				return 6;
			case '＄':
				return 7;
			case '%':
				return 8;
			case '％':
				return 9;
			case '&':
				return 10;
			case '＆':
				return 11;
			case '(':
				return 12;
			case '（':
				return 13;
			case ')':
				return 14;
			case '）':
				return 15;
			case '*':
				return 16;
			case ',':
				return 17;
			case '，':
				return 18;
			case '.':
				return 19;
			case '．':
				return 20;
			case '＊':
				return 21;
			case '\'':
				return 22;
			case '-':
				return 23;
			case ':':
				return 24;
			case '：':
				return 25;
			case ';':
				return 26;
			case '；':
				return 27;
			case '?':
				return 28;
			case '？':
				return 29;
			case '@':
				return 30;
			case '＠':
				return 31;
			case '[':
				return 32;
			case '［':
				return 33;
			case ']':
				return 34;
			case '］':
				return 35;
			case '^':
				return 36;
			case '＾':
				return 37;
			case '_':
				return 38;
			case '＿':
				return 39;
			case '{':
				return 40;
			case '｛':
				return 41;
			case '|':
				return 42;
			case '｜':
				return 43;
			case '}':
				return 44;
			case '｝':
				return 45;
			case '‘':
				return 46;
			case '’':
				return 47;
			case '"':
				return 48;
			case '“':
				return 49;
			case '”':
				return 50;
			case '′':
				return 51;
			case '\\':
				return 52;
			case '￥':
				return 53;
			case '「':
				return 54;
			case '」':
				return 55;
			case '『':
				return 56;
			case '』':
				return 57;
			case '【':
				return 58;
			case '】':
				return 59;
			case '+':
				return 60;
			case '＋':
				return 61;
			case '<':
				return 62;
			case '＜':
				return 63;
			case '=':
				return 64;
			case '＝':
				return 65;
			case '>':
				return 66;
			case '＞':
				return 67;
			case '×':
				return 68;
			case '∩':
				return 69;
			case '∪':
				return 70;
			case '≡':
				return 71;
			case '⊂':
				return 72;
			case '⊃':
				return 73;
			case '■':
				return 74;
			case '□':
				return 75;
			case '▲':
				return 76;
			case '△':
				return 77;
			case '▼':
				return 78;
			case '▽':
				return 79;
			case '◆':
				return 80;
			case '◇':
				return 81;
			case '○':
				return 82;
			case '◎':
				return 83;
			case '●':
				return 84;
			case '↑':
				return 85;
			case '→':
				return 86;
			case '←':
				return 87;
			case '↓':
				return 88;
			case '・':
				return 89;
			case '☆':
				return 90;
			case '★':
				return 91;
			case '※':
				return 92;
		}
		return -2;
	}

	static private int getSymbolAfter(char ch) {
		switch (ch) {
			case 'ー':		// 長音
				return 0;
			case '―':		// ダッシュ
				return 1;
		}
		return -2;
	}

	static private String getKanjiNumerals(String str, int idx) {
		int i;
		for (i = idx; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (getCharType(ch) != CHTYPE_KANJI_NUMERALS) {
				break;
			}
		}
		return str.substring(idx, i);
	}

	static private int getJapaneseVolumeName(char ch) {
		switch (ch) {
			case '上':
				return 0;
			case '前':
				return 1;
			case '中':
				return 2;
			case '後':
				return 3;
			case '下':
				return 4;
			case '完':
				return 5;
			case '短':
				return 6;
			case '外':
				return 7;
		}
		return -1;
	}

	static private int getKanjiNumeral(char ch) {
		switch (ch) {
			case '〇':
				return 0;
			case '零':
				return 1;
			case '一':
				return 2;
			case '壱':
				return 3;
			case '二':
				return 4;
			case '弐':
				return 5;
			case '三':
				return 6;
			case '参':
				return 7;
			case '四':
				return 8;
			case '肆':
				return 9;
			case '五':
				return 10;
			case '伍':
				return 11;
			case '六':
				return 12;
			case '陸':
				return 13;
			case '七':
				return 14;
			case '漆':
				return 15;
			case '八':
				return 16;
			case '捌':
				return 17;
			case '九':
				return 18;
			case '玖':
				return 19;
			case '十':
				return 20;
			case '拾':
				return 21;
			case '什':
				return 22;
			case '廿':
				return 23;
			case '卅':
				return 24;
			case '丗':
				return 25;
			case '百':
				return 26;
			case '佰':
				return 27;
			case '千':
				return 28;
			case '仟':
				return 29;
			case '阡':
				return 30;
			case '万':
				return 31;
			case '萬':
				return 32;
			case '億':
				return 33;
			case '兆':
				return 34;
		}
		return -1;
	}

	static private int getCharType(char ch) {
		boolean debug = false;
		if (SORT_BY_SYMBOL && getSymbolBefore(ch) >= 0) {
			if (debug) {Log.d("DEF", "getCharType: TYPE=CHTYPE_SYMBOL_BEFORE");}
			return CHTYPE_SYMBOL_BEFORE;
		}

		if (SORT_BY_SYMBOL && getSymbolAfter(ch) >= 0) {
			if (debug) {Log.d("DEF", "getCharType: TYPE=CHTYPE_SYMBOL_AFTER");}
			return CHTYPE_SYMBOL_AFTER;
		}

		if (SORT_BY_NATURAL_NUMBERS && (('0' <= ch && ch <= '9') || '.' == ch || ',' == ch)) {
			if (debug) {Log.d("DEF", "getCharType: TYPE=CHTYPE_NUM");}
			return CHTYPE_NUM;
		}

		if (SORT_BY_JAPANESE_VOLUME_NAME && getJapaneseVolumeName(ch) >= 0) {
			if (debug) {Log.d("DEF", "getCharType: TYPE=CHTYPE_JAPANESE_VOLUME_NAME");}
			return CHTYPE_JAPANESE_VOLUME_NAME;
		}

		if (SORT_BY_KANJI_NUMERALS && getKanjiNumeral(ch) >= 0) {
			if (debug) {Log.d("DEF", "getCharType: TYPE=CHTYPE_KANJI_NUMERALS");}
			return CHTYPE_KANJI_NUMERALS;
		}

		if (debug) {Log.d("DEF", "getCharType: TYPE=CHTYPE_CHAR");}
		return CHTYPE_CHAR;
	}

	// 縮小取り込みの倍率計算
	static public int calcScale(int w, int h, int type, int lw, int lh) {
		int lw2 = DEF.checkPortrait(w, h) ? lw : lw * 2;
		if (type == FileData.EXTTYPE_JPG) {
			// JPEGではライブラリで1/2^xに縮小できる
			if (w >= lw2 * 8 && h >= lh * 8) {
				return 8;
			} else if (w >= lw2 * 4 && h >= lh * 4) {
				return 4;
			} else if (w >= lw2 * 2 && h >= lh * 2) {
				return 2;
			}
		} else if (type != FileData.EXTTYPE_PNG && type != FileData.EXTTYPE_GIF) {
			// pngとgif他以外
			if (w >= lw2 * 2 && h >= lh * 2) {
				int w_scale = w / lw2;
				int h_scale = h / lh;
				return w_scale < h_scale ? w_scale : h_scale;
			}
		}
		return 1;
	}

	static public int divRoundUp(int val, int div) {
		return val / div + (val % div != 0 ? 1 : 0);
	}

	// Comittoの諸々保存先のパスを返す
	static public String getBaseDirectory() {
		return Environment.getExternalStorageDirectory() + "/comittona/";
	}

	// 設定画面のメッセージのフォントサイズ
	static public int getMessageTextSize(float density) {
		return (int) (TEXTSIZE_MESSAGE * density);
	}

	// 設定画面のメッセージのフォントサイズ
	static public int getSummaryTextSize(float density) {
		return (int) (TEXTSIZE_SUMMARY * density);
	}

	static public boolean checkExportKey(String key) {
		// サーバー情報としおり情報をExport/Import対象に含める
		/*
		if (key.indexOf('/') >= 0) {
			return false;
		}
		else if (key.startsWith("smb-")) {
			return false;
		}
		*/
		if (key.indexOf('/') >= 0) {
			// 安全のため、しおり削除の条件に一致しないKeyは対象外としておく
			int len = key.length();
			if (len >= 1 && key.substring(0, 1).equals("/")) {
			} else if (len >= 6 && key.substring(0, 6).equals("smb://")) {
			} else {
				return false;
			}
		} else if (key.equals("path")) {
			return false;
		} else if (key.equals("ImportSetting")) {
			return false;
		}
		return true;
	}

	// NxT専用キーの判別
	static public boolean checkTonlyExportKey(String key) {
		if (key.indexOf('/') >= 0) {
			// 安全のため、しおり削除の条件に一致しないKeyは対象外としておく
			int len = key.length();
			if (len >= 1 && key.substring(0, 1).equals("/")) {
				return true;
			} else if (len >= 6 && key.substring(0, 6).equals("smb://")) {
				return true;
			}
		} else if (key.startsWith("smb-")) {
			return true;
		}
		return false;
	}

	// 定義からフラグ読み込み
	static public boolean getBoolean(SharedPreferences sp, String key, boolean defval) {
		boolean intval = defval;
		try {
			// 読み込み
			intval = sp.getBoolean(key, defval);
		} catch (Exception e) {
			Log.e("DEF.getBoolean", "error(key=" + key + ")");
		}
		return intval;
	}

	// 定義から数値読み込み
	static public int getInt(SharedPreferences sp, String key, int defval) {
		int intval = defval;
		try {
			// 読み込み
			intval = sp.getInt(key, defval);
		} catch (Exception e) {
			Log.e("DEF.getInt", "error(key=" + key + ")");
		}
		return intval;
	}

	// 定義から文字列読み込み
	static public int getInt(SharedPreferences sp, String key, String defval) {
		String strval = defval;
		int intval = 0;
		try {
			strval = sp.getString(key, defval);
		} catch (Exception e) {
			Log.e("DEF.getInt", "getInt error(key=" + key + ")");
		}

		try {
			// 読み込み
			intval = Integer.parseInt(strval);
		} catch (Exception e) {
			Log.e("DEF.getInt", "parseInt error(key=" + key + ", str=" + strval + ")");
		}
		return intval;
	}

	public static void StackTrace(String tag, String msg) {
		StackTraceElement[] ste = new Throwable().getStackTrace();
		for (int i = 1; i < ste.length; i++) {
			Log.d(tag, msg + ste[i]);
		}
	}

	public static boolean isUiThread() {
		return Thread.currentThread().equals(Looper.getMainLooper().getThread());
	}

	// Url文字列作成
	public static String createUrl(String url, String user, String pass) {
		if (url == null) {
			return "";
		}
		if (url.length() <= 6) {
			return url;
		}
		if (!url.startsWith("smb://") || user == null || user.length() == 0) {
			return url;
		}
		// サーバ名
		String ret = "smb://" + URLEncoder.encode(user);
		if (pass != null && pass.length() > 0) {
			ret += ":" + URLEncoder.encode(pass);
		}
		ret += "@" + url.substring(6);
		return ret;
	}


	public static boolean CHAR_DETECT = true;
	public static String CHARSET = "Shift_JIS";


	/**
	 * ファイルにBOMが含まれていた場合、
	 * BOMなしに変換する。
	 *
	 * @param s ファイル文字列
	 * @return BOMなしのファイル文字列
	 *
	 */
	public static String removeUTF8BOM(String s) {
		// BOMをUnicodeコード表示したもの
		final String BOM = "\uFEFF";
		if (s.startsWith(BOM)) {
			// ファイルの先頭より後ろの文字列を読み込む
			s = s.substring(1);
		}
		return s;
	}

	public static String toUTF8(byte[] bytes, int offset, int length) {
		boolean debug = false;
		String encoding = "UTF-8";
		String dst;

		int tmp_offset = offset;
		int tmp_length = length;

		// UTF-8のBOMがあったら削除する
		if( bytes[offset] == 0xFE && bytes[offset+1] == 0xFF ){
			tmp_offset = offset + 1;
			tmp_length = length - 1;
		}

		if (debug) {Log.d("DEF", "toUTF8: 文字コードを自動判定します.");}
		encoding = CharDetecter(bytes, tmp_offset, tmp_length);

		dst = new String(bytes, tmp_offset, tmp_length, Charset.forName(encoding));
		if(debug) {Log.d("DEF", "toUTF8: Stringを出力します. dst=" + dst);}
		return dst;
	}

	public static String CharDetecter(byte[] bytes, int offset, int length) {
		boolean debug = false;
		if (debug) {Log.d("DEF", "CharDetecter: 文字コードを判定します.");}

		String encoding = null;
		int tmp_offset = offset;
		int tmp_length = length;

		// UTF-8のBOMがあったら削除する
		if( bytes[offset] == 0xFE && bytes[offset+1] == 0xFF ){
			tmp_offset = offset + 1;
			tmp_length = length - 1;
		}

		if (CHAR_DETECT) {
			// 文字コード判定ライブラリの実装
			UniversalDetector detector = new UniversalDetector(null);
			// 判定開始
			detector.handleData(bytes, tmp_offset, tmp_length);
			// 判定終了
			detector.dataEnd();
			// 文字コード判定
			encoding = detector.getDetectedCharset();
			// 判定の初期化
			detector.reset();

			if (encoding != null) {
				if("WINDOWS-1252".equals(encoding)) {
					// 判定された文字コードがWindows-1252の場合は誤判定ではないか確認する
					if (debug) {Log.d("DEF", "CharDetecter: 判定結果が WINDOWS-1252 なので誤判定かどうか確認します.");}
					byte[] src = Arrays.copyOfRange(bytes, tmp_offset, tmp_offset + tmp_length);
					String charset = (CHARSET == "Shift_JIS" ? "MS932" : CHARSET);
					if (Arrays.equals(src, new String(src, Charset.forName(charset)).getBytes(Charset.forName(charset)))) {
						if (debug) {Log.d("DEF", "CharDetecter: 文字コードは " + charset + " です.");}
						encoding = charset;
						return encoding;
					} else {
						if (debug) {Log.d("DEF", "CharDetecter: 文字コードは " + charset + " ではありません.");}
					}
					if (Arrays.equals(src, new String(src, Charset.forName("MS932")).getBytes(Charset.forName("MS932")))) {
						if (debug) {Log.d("DEF", "CharDetecter: 文字コードは MS932 です.");}
						encoding = "MS932";
					} else {
						if (debug) {Log.d("DEF", "CharDetecter: 文字コードは MS932 ではありません.");}
					}
				}
				// 判定された文字コードがShift_JISの場合は、MS932としてデータを読み込む
				if("Shift_JIS".equals(encoding)) {
					encoding = "MS932";
				}
				if (debug) {Log.d("DEF", "CharDetecter: 文字コードを判定しました. encoding=" + encoding);}
			} else {
				if (debug) {Log.d("DEF", "CharDetecter: 文字コードを判定できませんでした.");}
				byte[] src = Arrays.copyOfRange(bytes, tmp_offset, tmp_offset + tmp_length);

				if (debug) {
					Log.d("DEF", "CharDetecter: 文字コードを自動判定できませんでした.");
				}
				// 文字コード判定に失敗したので中国国家標準規格かどうか再度確認する
				if (debug) {Log.d("DEF", "CharDetecter: 文字コードが GB18030 かどうか判定します.");}
				if (Arrays.equals(src, new String(src, Charset.forName("GB18030")).getBytes(Charset.forName("GB18030")))) {
					if (debug) {Log.d("DEF", "CharDetecter: 文字コードは GB18030 です.");}
					encoding = "GB18030";
				} else {
					if (debug) {Log.d("DEF", "CharDetecter: 文字コードは GB18030 ではありません.");}
				}
			}
		}

		if (encoding == null) {
			// 判定できなかったら共通の操作設定設定で設定した文字コードに設定する
			String charset = (CHARSET == "Shift_JIS" ? "MS932" : CHARSET);
			if (debug) {Log.d("DEF", "CharDetecter: 文字コードを設定画面で設定した " + charset + " に設定します.");}
			encoding = charset;
		}
		return encoding;
	}

}