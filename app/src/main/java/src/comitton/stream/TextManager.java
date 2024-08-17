package src.comitton.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import src.comitton.common.DEF;
import src.comitton.data.MarkerDrawData;
import src.comitton.data.TextDrawData;
import src.comitton.data.PictureData;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

public class TextManager {
	public static final int SIZE_BITFLAG = 12;
	public static final int MSG_ERROR = 4;

	private final int COMMENT_NONE = 0;
	private final int COMMENT_JISAGE = 1;
	private final int COMMENT_JISAGE_RANGE = 2;
	private final int COMMENT_PAGECONTROL = 3;
	private final int COMMENT_STRCODE = 4;
	private final int COMMENT_STRCODE_EXT = 5;
	private final int COMMENT_PICTURE = 6;
	private final int COMMENT_PICTURE_CHAR = 7;
	private final int COMMENT_MIDASHI = 8;
	private final int COMMENT_MIDASHI_ST = 9;
	private final int COMMENT_MIDASHI_ED = 10;
	private final int COMMENT_TEXTSTYLE = 11;
	private final int COMMENT_TEXTSTYLE_ST = 12;
	private final int COMMENT_TEXTSTYLE_ED = 13;
	private final int COMMENT_JITUKI = 14;

	private final int PARAM_PAGEBREAK = 0;
	private final int PARAM_PAGECENTER = 1;

	private final int TB_PICTURE = -1;
	private final int TB_PAGECONTROL = -2;

	private final int CHARTYPE_INIT = -1;
	private final int CHARTYPE_KANJI = 0;
	private final int CHARTYPE_ASCII = 1;
	private final int CHARTYPE_KANA = 2;
	private final int CHARTYPE_CTRL = 3;
	private final int CHARTYPE_SYMBOL = 4;
	private final int CHARTYPE_MARK = 5;
	private final int CHARTYPE_PCHAR = 6;
	private final int CHARTYPE_ASCII_NUM = 7;
	private final int CHARTYPE_ASCII_MARK = 8;

	private final int BMPSCALE_ORIGINAL = 0;
	private final int BMPSCALE_ZOOM2X = 1;
	private final int BMPSCALE_ZOOM3X = 2;
	private final int BMPSCALE_ZOOM4X = 3;
	private final int BMPSCALE_SCREEN = 4;

	public static final char CHARDRAW_YOKO = 0x2a00;
	public static final char CHARDRAW_DAKUTEN = 0x2a01;
	public static final char CHARDRAW_HANDAKU = 0x2a02;
	public static final char CHARDRAW_KOGAKI = 0x2a03;
	public static final char CHARDRAW_KOGAKIDAKUTEN = 0x2a04;
	public static final char CHARDRAW_KOGAKIHANDAKU = 0x2a05;

	public static final int ASC_NORMAL  = 0;
	public static final int ASC_ROTATE  = 1;
	public static final int ASC_TWINCHK = 2;

	public static final float TITLE_SIZE = 1.5f;

	private boolean mRunningFlag;

	private Handler mHandler;

	private TextDrawData mTextPages[][] = null;
	private byte mInputBuff[];
	private char mTextBuff[];
	private PictureData mPictures[];
	private ArrayList<PictureData> mPicArray;
	private MidashiManager mMidashi;
//	private StyleManager mTextStyle;
	private SparseArray<ArrayList<MarkerDrawData>> mMarker;
	private MidashiData[] mSearchList;
	private float mFontWidth[][];

	private static final int CODETBL_SIZE = 13288;
	private static final int CODETBL_BLOCK = 4;
	private byte mCodeTbl[];

	private String mTitle;

	// private int mPosX; // 次の文字X座標(文字の右上基準)
	// private int mTxPosY; // 次の文字Y座標(文字の右上基準)
	// private int mRbPosY; // 次の文字Y座標(文字の右上基準)
	private float mSpaceW; // 行間
	private float mSpaceH; // 文字間
	private int mMarginW; // 左右マージン
	private int mMarginH; // 上下マージン
	private int mTextRight; // 文字右端
	private int mTextTop; // 文字上端
	private int mWidth; // 用紙幅
	private int mHeight; // 用紙高さ
	private int mTextWidth; // テキストエリア幅
	private int mTextHeight; // テキストエリア高さ

	public int mAscMode;	// 半角の表示方法

	private int mPicScale;

	// 設定値
	private float mHeadSize; // 見出し文字サイズ
	private float mTextSize; // 本文文字サイズ
	private float mRubiSize; // ルビ文字サイズ

	private float mFontSize; // 現在の文字サイズ

	ImageManager mImageMgr;
	private String mTextFile;
	private String mTextPath;
	private String mUser;
	private String mPass;

	private Paint mTextPaint;
	
	public static final int STYLE_POINT_BASE = 0x80000000;
	public static final int STYLE_POINT1 = STYLE_POINT_BASE + 1;
	public static final int STYLE_POINT2 = STYLE_POINT_BASE + 2;
	public static final int STYLE_POINT3 = STYLE_POINT_BASE + 3;
	public static final int STYLE_POINT4 = STYLE_POINT_BASE + 4;
	public static final int STYLE_POINT5 = STYLE_POINT_BASE + 5;
	public static final int STYLE_POINT6 = STYLE_POINT_BASE + 6;
	public static final int STYLE_POINT7 = STYLE_POINT_BASE + 7;
	public static final int STYLE_POINT8 = STYLE_POINT_BASE + 8;
	public static final int STYLE_LINE_BASE = 0x40000000;
	public static final int STYLE_LINE1 = STYLE_LINE_BASE + 1;
	public static final int STYLE_LINE2 = STYLE_LINE_BASE + 2;
	public static final int STYLE_LINE3 = STYLE_LINE_BASE + 3;
	public static final int STYLE_LINE4 = STYLE_LINE_BASE + 4;
	public static final int STYLE_LINE5 = STYLE_LINE_BASE + 5;

	private class LineData {
		public short indent; // 字下げ数
		public TextBlock[] textblock;
		public char[][] extdata;
		public StyleData[] stylelist;
	}

	private char[][] copyExtData(char[][] extdata, int extidx, char[] textbuff, int idx, int len) { 							
		int extcnt = 0;
		for (int i = 0 ; i < len ; i ++) {
			if (0x2a00 <= textbuff[idx + i] && textbuff[idx + i] <= 0x2aff) {
				extcnt ++;
			}
		}
		char[][] result = null;
		if (extcnt > 0 && extdata != null) {
			// 傍点・傍線のスタイルがあれば配列化して設定
			result = new char[extcnt][];
			for (int i = 0 ; i < extcnt && extidx < extdata.length ; i ++) {
				result[i] = extdata[extidx ++];
			}
		}
		return result;
	}
	
	/** 
	 * 傍点・傍線の保持
	 */
	public class StyleData {
		public int index; // 開始位置
		public int length; // 長さ
		public int type; // 種別
	}

	private class TextBlock {
		public int rb_index; // ルビ文字列開始位置
		public int rb_length; // ルビ文字列長
		public int tx_index; // 本文文字列開始位置
		public int tx_length; // 本文文字列長

		public TextBlock(int tx_idx, int tx_len, int rb_idx, int rb_len) {
			tx_index = tx_idx;
			tx_length = tx_len;
			rb_index = rb_idx;
			rb_length = rb_len;
		}

		public TextBlock(int tx_idx, int tx_len) {
			tx_index = tx_idx;
			tx_length = tx_len;
			rb_index = -1;
			rb_length = 0;
		}
	}

	// テキスト解析管理
	public class TextBlockManager {
		private ArrayList<TextBlock> textblocks;
		private ArrayList<char[]> extdatas;
		private ArrayList<StyleData> stylelist;
		StringBuffer textbuff;
		private short baseindent;
		private short currentindent;
		private int tx_st;
		private int tx_ed;
		private int rb_st;
		private int linecolumn;
		private boolean isRubi; 	// ルビ設定中
		private boolean isRubiSet; // ルビ開始位置の明示的な指定
		private int textcolumn;
		private int chartype;
		private int rubipos;

		public TextBlockManager(StringBuffer tb) {
			// 文字列領域
			textbuff = tb;
			currentindent = 0;
			baseindent = 0;
			isRubi = false;
			tx_st = 0;
			tx_ed = 0;
			rb_st = 0;
			linecolumn = 0;
			textcolumn = 0;
			isRubi = false; // ルビ設定中
			isRubiSet = false; // ルビ開始位置の明示的な指定
			chartype = CHARTYPE_INIT;
			rubipos = 0;
			textblocks = new ArrayList<TextBlock>();
		}

		// 改行又は左右中央設定
		public void addPageControl(int param) {
			// これまでの設定を確定
			addTextBlock();

			// 改行設定
			if (isRubi == false) {
				TextBlock tb = new TextBlock(TB_PAGECONTROL, param);
				textblocks.add(tb);
			}
		}
		
		// 挿絵設定
		public void addPicture(int param) {
			// これまでの設定を確定
			addTextBlock();

			// 挿絵設定
			if (isRubi == false) {
				TextBlock tb = new TextBlock(TB_PICTURE, param);
				textblocks.add(tb);
			}
		}
		
		// その行のみ字下げ
		public void setCurrentIndent(int indent) {
			if (isRubi == false) {
				currentindent = (short)indent;
			}
		}
		
		// 以降の行を字下げ
		public void setBaseIndent(int indent) {
			if (isRubi == false) {
				baseindent = (short)indent;
			}
		}

		public short getNextIndent() {
			return baseindent;
		}
		
		public int getTextColumn() {
			return textcolumn;
		}
		
		// テキストバッファに文字設定
		public void appendText(char code) {
			textbuff.append(code);
			linecolumn++;
			if (isRubi == false) { 
    			// 本文の位置を設定
    			textcolumn ++;
			}
		}

		public void replaceText(char code, int len) {
			if (code != 0) {
				int bufflen = textbuff.length();
				if (linecolumn >= len && bufflen >= len) {
					// 行の先頭でなければ
					textbuff.setLength(bufflen - len);
					//textbuff.setCharAt(bufflen - param2, code);
				}
				textbuff.append(code);
			}
		}

		public void setRubiStart() {
			if (isRubi == false && rubipos < textbuff.length()) {
        		if (tx_st < rubipos) {
        			// ルビ対象よりも前の部分を登録
        			TextBlock tb = new TextBlock(tx_st, rubipos - tx_st);
        			textblocks.add(tb);
        		}
        		// ルビ対象情報
        		tx_st = rubipos;
        		tx_ed = textbuff.length();
        		rb_st = textbuff.length();
        		isRubi = true;
			}
    	}

		public void setRubiTarget() {
			if (isRubi == false) {
				// ｜はルビ開始位置の指定なのでコピーしない
				rubipos = textbuff.length();
				isRubiSet = true;
			}
		}

		/**
		 * 文字種別の切り替わりをルビの対象として設定する
		 * @param newchartype 文字種
		 */
		public void checkRubiTarget(int newchartype) {
			if (isRubi == false && isRubiSet == false) {
				// テキストモードかつルビ位置指定なし
				if (chartype != newchartype) {
					// 漢字/非漢字の切り替わりで位置記憶
					chartype = newchartype;
					rubipos = textbuff.length();
				}
			}
		}
		
		/**
		 * 蓄積したデータをブロックとして登録
		 */
		public void addTextBlock() {
			int ed = textbuff.length();
			TextBlock tb = null;

			if (isRubi == true) {
				// ルビ
				if (rb_st < ed) {
					// ルビの文字列がある
					tb = new TextBlock(tx_st, tx_ed - tx_st, rb_st, ed - rb_st);
				}
				else {
					// ルビの文字列がない場合は本文のみ
					tb = new TextBlock(tx_st, tx_ed - tx_st);
				}
			}
			else {
				// 本文
				if (tx_st < ed) {
					tb = new TextBlock(tx_st, ed - tx_st);
				}
			}
			// テキスト＋ルビの情報を登録
			if (tb != null) {
				textblocks.add(tb);
			}

			chartype = CHARTYPE_INIT;
			tx_st = rubipos = textbuff.length();

			isRubi = false;
			isRubiSet = false; // ルビ開始位置の指定は終わり
		}

		/**
		 * 1行分のテキストデータとスタイルデータを配列化して返す
		 * @return 1行分のデータ
		 */
		public LineData getLineData() {
			isRubi = false;
			isRubiSet = false; // ルビ開始位置の指定は終わり

    		LineData ld = new LineData();
    		ld.indent = (short) currentindent;
    		ld.textblock = (TextBlock[]) textblocks.toArray(new TextBlock[0]);
    		if (extdatas != null) {
    			// 傍点・傍線のスタイルがあれば配列化して設定
        		ld.extdata = (char[][])extdatas.toArray(new char[0][]);
    		}
    		if (stylelist != null) {
    			// 傍点・傍線のスタイルがあれば配列化して設定
        		ld.stylelist = (StyleData[])stylelist.toArray(new StyleData[0]);
    		}
    		return ld;
		}

		/**
		 * 行の切り替え
		 */
		public void setNewLine() {
			// 行変わりで状態を初期化
			textblocks = new ArrayList<TextBlock>();
			extdatas = null;
			stylelist = null;
			chartype = CHARTYPE_INIT;
    		currentindent = baseindent;	// 次の行のインデント設定
			tx_st = rubipos = textbuff.length();
			isRubiSet = false; // ルビ開始位置の明示的な指定
			isRubi = false;
			linecolumn = 0;
			textcolumn = 0;
		}

		/**
		 * 行中の合成文字データを設定
		 * @param extdata データ
		 */
		public void addExtData(char[] extdata) {
			if (extdatas == null) {
				// なければ新規追加
				extdatas = new ArrayList<char[]>();
			}
			extdatas.add(extdata);
		}
		
		public void setStyle(int type, int idx, int len) {
			StyleData sd = new StyleData();
			sd.type = type;
			sd.index = idx;
			sd.length = len;
			if (stylelist == null) {
				// なければ新規追加
				stylelist = new ArrayList<StyleData>();
			}
			else {
				// あれば既存重複チェック
				int i = 0;
				int st_idx = sd.index;
				int ed_idx = sd.index + sd.length - 1;
				while (i < stylelist.size()) {
					StyleData sdw = stylelist.get(i);
					if (sdw != null) {
						int tgt_st_idx = sdw.index;
						int tgt_ed_idx = sdw.index + sdw.length - 1;

						// 重なっている場合はずらす
						if (st_idx <= tgt_st_idx && tgt_st_idx <= ed_idx) {
							sdw.length -= ed_idx - tgt_st_idx + 1;
							sdw.index = ed_idx + 1;
						}
						if (st_idx <= tgt_ed_idx && tgt_ed_idx <= ed_idx) {
							sdw.length -= tgt_ed_idx - st_idx + 1;
							sdw.index = st_idx - 1;
						}
						if (sdw.length <= 0) {
							// 長さがなくなった場合は削除
							stylelist.remove(i);
						}
						else {
							i++;
						}
					}
				}
			}
			stylelist.add(sd);
			return;
		}
	}
	
	public class MidashiData {
		public String text; // 見出しテキスト
		public int line; // 行位置
		public int page; // ページ位置

		public void setText(String t) {
			if (text == null) {
				text = t;
			}
			else {
				text = text + t;
			}
		}

		public String getText() {
			return text;
		}

		public int getPage() {
			return page;
		}

		public boolean equals(Object obj) {
			MidashiData md = (MidashiData) obj;
			if (this.text.equals(md.text) && this.page == md.page && this.line == md.line) {
				return true;
			}
			return false;
		}
	}

	public class MidashiManager {
		private ArrayList<MidashiData> midashilist;
		private int current;

		public MidashiManager() {
			// 初期化
			midashilist = new ArrayList<MidashiData>();
			current = 0;
		}

		public MidashiData addMidashiData(String t, int l) {
			MidashiData md = new MidashiData();
			md.text = t;
			md.line = l;
			md.page = -1;
			if (midashilist.indexOf(md) < 0) {
				// 同じものがない場合のみ登録
				midashilist.add(md);
			}
			return md;
		}

		public void initCurrent() {
			current = 0;
		}

		// レイアウトの行に変換
		public void setLine(int linecnt, int newline) {
			while (current < midashilist.size()) {
				MidashiData md = midashilist.get(current);
				if (md.line > linecnt) {
					break;
				}
				if (md.line == linecnt) {
					md.line = newline;
				}
				current++;
			}
		}

		// ページ確定時にページ番号を設定
		public void setPage(int linecnt, int page) {
			while (current < midashilist.size()) {
				MidashiData md = midashilist.get(current);
				if (md.line > linecnt) {
					break;
				}
				if (md.line <= linecnt) {
					md.page = page;
				}
				current++;
			}
		}
	}

	public static char getStylePointChar(int type) {
		switch (type) {
			case STYLE_POINT2:
				return '﹆';
			case STYLE_POINT3:
				return '●';
			case STYLE_POINT4:
				return '○';
			case STYLE_POINT5:
				return '▲';
			case STYLE_POINT6:
				return '△';
			case STYLE_POINT7:
				return '◎';
			case STYLE_POINT8:
				return '◉';
		}
		return '﹅';
	}

	public static int getStyleOffset(int type) {
		switch (type) {
			case STYLE_POINT1:
			case STYLE_POINT2:
				return 50;
			case STYLE_POINT3:
			case STYLE_POINT4:
			case STYLE_POINT5:
			case STYLE_POINT6:
			case STYLE_POINT7:
			case STYLE_POINT8:
				return 40;
			case STYLE_LINE1:
			case STYLE_LINE3:
			case STYLE_LINE4:
			case STYLE_LINE2:
			case STYLE_LINE5:
				return 0;
		}
		return 100;
	}

	public static int getStyleWidth(int type) {
		switch (type) {
			case STYLE_POINT1:
			case STYLE_POINT2:
			case STYLE_POINT3:
			case STYLE_POINT4:
			case STYLE_POINT5:
			case STYLE_POINT6:
			case STYLE_POINT7:
			case STYLE_POINT8:
				return 45;
			case STYLE_LINE1:
			case STYLE_LINE3:
			case STYLE_LINE4:
				return 20;
			case STYLE_LINE2:
			case STYLE_LINE5:
				return 35;
		}
		return 100;
	}

	public int getMidashiSize() {
		if (mMidashi != null && mMidashi.midashilist != null) {
			return mMidashi.midashilist.size();
		}
		return 0;
	}

	public MidashiData getMidashi(int index) {
		if (mMidashi != null && mMidashi.midashilist != null && index < mMidashi.midashilist.size()) {
			return mMidashi.midashilist.get(index);
		}
		return null;
	}

	// 行情報
	public static final short TYPE_TEXT = 0;
	public static final short TYPE_RUBI = 1;
	public static final short TYPE_STYLE = 2;

	private class LineManager {
		public class TextState {
			public boolean kinsoku; // 有無？
			public boolean lineend; // 行末？
			public boolean jituki;	// 地付き？
			public short top;
			public short height;

			public TextState() {
				kinsoku = false;
				lineend = true;
				top = 0;
				height = 0;
			}
		}

		SparseArray<ArrayList<TextDrawData>> tx_linelist;
		SparseArray<ArrayList<TextDrawData>> rb_linelist;
		SparseArray<ArrayList<TextDrawData>> st_linelist;
		SparseArray<TextState> statelist;

		private int linemax;

		public LineManager() {
			tx_linelist = new SparseArray<ArrayList<TextDrawData>>();
			rb_linelist = new SparseArray<ArrayList<TextDrawData>>();
			st_linelist = new SparseArray<ArrayList<TextDrawData>>();
			statelist = new SparseArray<TextState>();
			linemax = 0;
		}

		public void clear() {
			if (tx_linelist != null) {
				tx_linelist.clear();
				tx_linelist = null;
			}
			if (rb_linelist != null) {
				rb_linelist.clear();
				rb_linelist = null;
			}
			if (st_linelist != null) {
				st_linelist.clear();
				st_linelist = null;
			}
			if (statelist != null) {
				statelist.clear();
				statelist = null;
			}
		}

		// 行データを設定(x座標は未定)
		public TextDrawData setLineData(int line, float size, float x, float y, int pos, int len, short type, float gap, float height, char[][] ext, short t, short h) {
			boolean istext = false;
			boolean isascii = false;
			if ((pos & (STYLE_POINT_BASE | STYLE_LINE_BASE)) == 0) {
				// テキストデータ
				if (type == TYPE_TEXT) {
					istext = true;
				}
				if (mAscMode != ASC_NORMAL && mTextBuff[pos] < 0x80) {
					// 半角文字
					isascii = true;
				}
			}
			else {
				// 傍点・傍線
				istext = false;
			}
			TextDrawData tdd = new TextDrawData(istext, isascii, size, x, y, pos, len, gap, height, ext);
			if (type == TYPE_TEXT) {
				// 本文
				ArrayList<TextDrawData> linedata = tx_linelist.get(line);
				if (linedata == null) {
					// まだ行データがないので追加
					linedata = new ArrayList<TextDrawData>();
					tx_linelist.put(line, linedata);
				}
				linedata.add(tdd);
			}
			else if (type == TYPE_RUBI) {
				// ルビ
				ArrayList<TextDrawData> linedata = rb_linelist.get(line);
				if (linedata == null) {
					// まだ行データがないので追加
					linedata = new ArrayList<TextDrawData>();
					rb_linelist.put(line, linedata);
				}
				linedata.add(tdd);
			}
			else if (type == TYPE_STYLE) {
				// スタイル
				ArrayList<TextDrawData> linedata = st_linelist.get(line);
				if (linedata == null) {
					// まだ行データがないので追加
					linedata = new ArrayList<TextDrawData>();
					st_linelist.put(line, linedata);
				}
				linedata.add(tdd);
			}
			if (linemax < line) {
				linemax = line;
			}

			// 描画範囲設定
			TextState state = getLineState(line);
			state.top = t;
			state.height = h;
			return tdd;
		}

		// 行データが禁則されたかを設定
		public void setLineKinsoku(int line, boolean kinsoku) {
			getLineState(line).kinsoku = kinsoku;
		}

		// 行データが禁則されたかを設定
		public void setLineEnd(int line, boolean end) {
			getLineState(line).lineend = end;
		}

		// 行が地付き指定かを設定
		public void setLineJituki(int line, boolean jituki) {
			getLineState(line).jituki = jituki;
		}

		// 行の禁則有無を返す
		public boolean getLineKinsoku(int line) {
			return getLineState(line).kinsoku;
		}

		// 行末かどうかを返す
		public boolean getLineEnd(int line) {
			return getLineState(line).lineend;
		}

		// 行の属性データを返す
		public TextState getLineState(int line) {
			TextState state = statelist.get(line);
			if (state == null) {
				// まだページデータがないので追加
				state = new TextState();
				statelist.put(line, state);
			}
			return statelist.get(line);
		}

		public ArrayList<TextDrawData> getTextLineData(int line) {
			return tx_linelist.get(line);
		}

		public ArrayList<TextDrawData> getRubiLineData(int line) {
			return rb_linelist.get(line);
		}

		public ArrayList<TextDrawData> getStyleLineData(int line) {
			return st_linelist.get(line);
		}

		public int getMaxLine() {
			return linemax + 1;
		}
	}

	private class RemainingResult {
		public int count;
		public boolean newline;
		public boolean ascii;
		public float height;

		private void clear() {
			count = 0;
			newline = false;
			ascii = false;
			height = 0.0f;
		}
	}

	/**
	 * TextBuff+ExtDataを文字列化する
	 * @param textbuff
	 * @param index
	 * @param length
	 * @param extdata
	 * @return
	 */
	public String getTextString(char[] textbuff, int index, int length, char[][] extdata) {
		StringBuffer sb = new StringBuffer(length);
		if (index >= 0 && length > 0) {
			int extidx = 0;
			for (int i = 0 ; i < length ; i ++) {
				char code = mTextBuff[index + i];
				if (0x2a00 <= code && code <= 0x2aff) {
					// 特殊文字対応
					if (extdata != null && extidx < extdata.length) { 
						sb.append(extdata[extidx++]);
					}
				}
				else {
					sb.append(code);
				}
			}
		}
		return sb.toString(); 
	}

	/**
	 * 指定文字数が表示上では何文字になるかを返す(ExtDataを考慮する)
	 * @param td 描画情報
	 * @param textbuff 文字列バッファ
	 * @param startpos カウントの開始位置
	 * @param search_length 文字数
	 * @param result_length(対応する検索文字としての文字数)
	 */
	private boolean getTextLengthPos(TextDrawData td, char[] textbuff, int startpos, int search_length, int[] result_length) {
		char[][] extdata = td.mExData;
		int res_pos = 0;
		int res_len = 0;

		int text_pos = 0;
		int text_num = 0;

		if (td.mTextPos >= 0 && td.mTextLen > 0) {
			int extidx = 0;
			for (int i = 0 ; i < td.mTextLen && text_pos < startpos + search_length ; i ++) {
				char code = mTextBuff[td.mTextPos + i];
				
				text_num = 0;
				if (0x2a00 <= code && code <= 0x2aff) {
					// 特殊文字対応
					if (extdata != null && extidx < extdata.length) { 
						// 横並びの文字数など
						text_num = extdata[extidx].length;
					}
				}
				else {
					text_num = 1;
				}
				// 
				if (text_num > 0) {
					// 文字有り
					if (text_pos + text_num > startpos) {
        				res_len ++;
    				}
    				else {
    					res_pos ++;
    				}
				}
				
				// 進める
				text_pos += text_num;
			}
		}
		result_length[0] = res_pos;
		result_length[1] = res_len;
		result_length[2] = text_pos - startpos; 
		return true; 
	}

	/**
	 * 指定文字数が表示上では何ドットになるかを返す
	 * @param td 描画情報
	 * @param textbuff 文字列バッファ
	 * @param startpos カウントの開始位置
	 * @param search_length 文字数
	 * @param result_length(対応する検索文字としての文字数)
	 */
	private boolean getTextLengthPosAscii(TextDrawData td, char[] textbuff, Paint textpaint, int startpos, int search_length, int[] result_length) {
		int res_pos = 0;
		int res_len = 0;
		int text_len = 0;

		if (td.mTextPos >= 0 && td.mTextLen > 0) {
    		// 開始位置を求める
    		if (startpos > 0) {
    			res_pos = (int)textpaint.measureText(textbuff, td.mTextPos, startpos);
    		}
    		
    		// 長さを求める
    		text_len = search_length;
    		if (text_len > td.mTextLen - startpos) {
    			text_len = td.mTextLen - startpos;
    		}
    				
    		// 文字列の長さ分の幅を求める
    		if (text_len > 0) {
    			res_len = (int)textpaint.measureText(textbuff, td.mTextPos + startpos, text_len);
    		}
		}
		result_length[0] = res_pos;
		result_length[1] = res_len;
		result_length[2] = text_len; 
		return true; 
	}

	/**
	 * 文字列を描画したときの高さを取得
	 * @param textbuff 文字バッファ
	 * @param i 開始位置
	 * @param l 長さ
	 * @param font_cy 全角人文字のサイズ
	 * @return 高さを返す
	 */
	private float calcCharsHeight(char[] textbuff, int idx, int len, float font_size, float space_h, boolean isText) {
		if (idx < 0 || len < 0) {
			return 0.0f;
		}
//		mTextPaint.setTextSize(font_size);
		float sum = 0.0f;
		int cnt;

		float widths[] = mFontWidth[isText ? 0 : 1];
		// int i = 0;
		for (int i = 0 ; i < len ; i += cnt) {
			if (mAscMode != ASC_NORMAL && textbuff[idx + i] < 0x80) {
				int code = textbuff[idx + i];
//				// 半角が続く文字数をカウント
//				for (cnt = 1 ; i + cnt < len ; cnt ++) {
//					if (textbuff[idx + i + cnt] >= 0x80) {
//						// 全角がある
//						break;
//					}
//				}
//				// 文字列の長さを加算
//				sum += mTextPaint.measureText(textbuff, idx + i, cnt);
//				if (i + cnt < len) {
//					// 最後の文字以外は字間設定
//					sum += space_h;
//				}
				// 半角
				sum += widths[code];
				if (i < len - 1) {
					// 最後の文字以外は字間設定
					sum += space_h;
				}
				cnt = 1;
			}
			else {
				// 全角
				sum += font_size;
				if (i < len - 1) {
					// 最後の文字以外は字間設定
					sum += space_h;
				}
				cnt = 1;
			}
		}
		return sum;
	}

	private void calcRemainingCount(char[] textbuff, int ti, int tl, float font_size, float space_h, float rest_cy, RemainingResult rr) {
		rr.clear();

		if (ti < 0 || tl < 0) {
			return;
		}
		mTextPaint.setTextSize(font_size);

		int tl_adj;	// 半角・全角が続く文字数

		if (mAscMode != ASC_NORMAL && textbuff[ti] < 0x80) {
			// 半角
			rr.ascii = true;
			// 半角が続く文字数をカウント
			for (tl_adj = 1 ; tl_adj < tl ; tl_adj ++) {
				if (textbuff[ti + tl_adj] >= 0x80) {
					// 全角がある
					break;
				}
			}

			float result[] = new float[tl_adj];
			mTextPaint.getTextWidths(textbuff, ti, tl_adj, result);
			// 先頭から詰める
			float sum = 0.0f;
			int cnt;
			int kin_idx = -1;
			for (cnt = 0; cnt < result.length; cnt ++) {
				if (sum + result[cnt] > rest_cy) {
					// はみ出した
					rr.newline = true;
					if (kin_idx >= cnt - 10) {
						cnt = kin_idx + 1;
					}
					break;
				}
				sum += result[cnt];
				switch (textbuff[ti + cnt]) {
					case ' ':
					case '.':
					case ',':
					case '!':
					case '?':
					case '>':
					case ')':
					case ']':
					case '}':
					case '|':
					case '~':
					case '&':
					case '%':
					case '$':
					case ':':
					case ';':
					case '@':
    					// 半角で英数以外の文字を改行の区切りとする
    					kin_idx = cnt;
				}
//				if ((textbuff[ti + cnt] < 'a' || 'z' < textbuff[ti + cnt])
//					&& (textbuff[ti + cnt] < 'A' || 'Z' < textbuff[ti + cnt])
//						&& (textbuff[ti + cnt] < '0' || '9' < textbuff[ti + cnt])
//							&& textbuff[ti + cnt] != '(') {
//					// 半角で英数以外の文字を改行の区切りとする
//					kin_idx = cnt;
//				}
			}
			rr.count = cnt;
			rr.height = sum;
		}
		else {
			// 全角
			rr.ascii = false;
			if (mAscMode == ASC_NORMAL) {
				// 全角扱い
				tl_adj = tl;
			}
			else {
				// 全角が続く文字数をカウント
				for (tl_adj = 1 ; tl_adj < tl ; tl_adj ++) {
					if (textbuff[ti + tl_adj] < 0x80) {
						// 半角がある
						break;
					}
				}
			}

			float th = (font_size + space_h) * tl_adj - space_h; // 本文の高さ
			// 行に入る本文の文字数を算出
			if (rest_cy >= th) {
				rr.count = tl_adj;
			}
			else {
				// 分断位置
				rr.count = (int) ((rest_cy + space_h) / (font_size + space_h));	// 最後の文字の後は余白不要
				rr.newline = true;
			}
			if (rr.count < 0) {
				rr.count = 0;
			}
			else if (rr.count > tl_adj) {
				rr.count = tl_adj;
			}
			rr.height = (font_size + space_h) * rr.count - space_h;
		}
	}

	public TextManager(ImageManager imagemgr, String textfile, String user, String pass, Handler handler, Context context) {
		mTextPages = null;
		mHandler = handler;
		mRunningFlag = true;
		mCodeTbl = new byte[CODETBL_SIZE];
		mTextFile = textfile;
		mTextPath = DEF.getDir(textfile);
		mUser = user;
		mPass = pass;

		mImageMgr = imagemgr;

		mFontWidth = new float[2][];
		mFontWidth[0] = new float[0x80];
		mFontWidth[1] = new float[0x80];

		AssetManager am = context.getResources().getAssets();
		InputStream is;
		try {
			is = am.open("codetbl.bin", AssetManager.ACCESS_STREAMING);
			is.read(mCodeTbl);
		} catch (IOException e) {
			// 文字テーブル読み込み失敗
			Log.e("TextManager", e.getMessage());
		}
	}

	public void LoadTextFile() {
		// try {
		mInputBuff = mImageMgr.loadExpandData(mTextFile);
		if (mInputBuff == null) {
			// }
			// catch (IOException ex) {
			Log.d("TextManager", "Text file load error");
			Message message = new Message();
			message.what = MSG_ERROR;
			message.obj = "Text file load error";
			mHandler.sendMessage(message);
		}
	}

	public void formatTextFile(int width, int height, float headfont, float textfont, float rubifont, float space_w, float space_h, int margin_w, int margin_h, int pic_scale, String fontfile, int ascmode) {
		// リスト確保
		StringBuffer textbuff = new StringBuffer();
		mPicArray = new ArrayList<PictureData>();

		if (mInputBuff == null) {
			return;
		}

		// 基準値保持
		mWidth = width;
		mHeight = height;
		mSpaceW = space_w;
		mSpaceH = space_h;

		mMarginW = margin_w;
		mMarginH = margin_h;

		mTextWidth = mWidth - mMarginW * 2;
		mTextHeight = mHeight - mMarginH * 2;

		mTextRight = mMarginW;
		mTextTop = mMarginH;

		mPicScale = pic_scale;

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		Typeface face = null;
		if (fontfile != null && fontfile.length() > 0) {
			try {
				face = Typeface.createFromFile(fontfile);
			}
			catch (RuntimeException e) {
				;
			}
			if (face != null) {
				mTextPaint.setTypeface(face);
			}
		}
		if (face == null) {
			mTextPaint.setTypeface(Typeface.DEFAULT);
		}

		mAscMode = ascmode;

		String inputStr;
		StringBuffer inputSB = null;
		String title = "";
		String strCharSet = checkShiftJIS(mInputBuff);
		try {
			inputStr = new String(mInputBuff, strCharSet);
			inputStr = inputStr.replaceAll("\r\n", "\n");
		} catch (UnsupportedEncodingException e) {
			// 自動判定による変換失敗
			return;
		}

		// XML読み込み
		if (inputStr.startsWith("<?xml")) {
			XmlPullParser xmlPullParser = Xml.newPullParser();

			try {
				xmlPullParser.setInput(new StringReader(inputStr));
			} catch (XmlPullParserException e) {
				xmlPullParser = null;
				Log.e("formatText", "XMLの読み込みに失敗");
			}

			if (xmlPullParser != null) {
				inputSB = new StringBuffer(inputStr.length());
				int eventType = 0;
				int tag_level = 0;
				int tag_level_p = 0;
				int tag_level_title = 0;
				int tag_level_body = 0;
				int tag_level_ruby = 0;
				int tag_level_rp = 0;
				int tag_level_rt = 0;
				int tag_level_h = 0;
				int text_cnt = 0;
				boolean isDiv = false;
				String line_text = null;

				try {
					eventType = xmlPullParser.getEventType();
				}
                catch (Exception e) {
                	inputSB = null;
                }
				String tag_name;
				while (eventType != XmlPullParser.END_DOCUMENT) {
					try {
						tag_name = xmlPullParser.getName();
						if (eventType == XmlPullParser.START_DOCUMENT) {
							// Log.d("XmlPullParserSample", "Start document");
						} else if (eventType == XmlPullParser.END_DOCUMENT) {
							// Log.d("XmlPullParserSample", "End document");
						} else if (eventType == XmlPullParser.START_TAG) {
							// Log.d("XmlPullParserSample", "Start tag " + tag_name);
							int atr_count = xmlPullParser.getAttributeCount();

							// タグの階層
							tag_level++;
							if (tag_name.equals("br")) {
								// brタグであれば改行追加
								inputSB.append("\n");
								text_cnt = 0;
							}
							else if (tag_name.equals("ruby")) {
								tag_level_ruby++;
								if (tag_level_ruby == 1) {
									inputSB.append("｜");
								}
							}
							else if (tag_name.equals("rt")) {
								tag_level_rt++;
								if (tag_level_rt == 1) {
									inputSB.append("《");
								}
							}
							else if (tag_name.equals("rp")) {
								tag_level_rp++;
							}
							else if (tag_name.equals("p")) {
								tag_level_p++;
								text_cnt = 0;
								isDiv = false;
							}
							else if (tag_name.equals("img")) {
								for (int i = 0; i < atr_count; i++) {
									String atr_name = xmlPullParser.getAttributeName(i);
									if (atr_name.equals("src")) {
										String atr_value = xmlPullParser.getAttributeValue(i);
										inputSB.append("［＃挿絵（");
										inputSB.append(atr_value);
										inputSB.append("）入る］\n");
									}
								}
							}
							else if (tag_name.equals("title")) {
								tag_level_title++;
							}
							else if (tag_name.equals("body")) {
								tag_level_body++;
							}
							else if (tag_name.equals("div")) {
								if (isDiv == false && inputSB.length() > 0) {
									inputSB.append("［＃改ページ］\n");
									isDiv = true;
								}
							}
							else if (tag_name.equals("h1") || tag_name.equals("h2") || tag_name.equals("h3")) {
								// 見出しとする
								tag_level_h++;
								line_text = "";
								text_cnt = 0;
							}
							else if (tag_name.equals("ol")) {
								inputSB.append("\n");
							}
						} else if (eventType == XmlPullParser.END_TAG) {
							//Log.d("XmlPullParserSample", "End tag " + tag_name);
							if (tag_level > 0) {
								tag_level--;
							}
							if (tag_name.equals("ruby")) {
								if (tag_level_ruby > 0) {
									tag_level_ruby--;
								}
							}
							else if (tag_name.equals("rt")) {
								if (tag_level_rt == 1) {
									inputSB.append("》");
								}
								if (tag_level_rt > 0) {
									tag_level_rt--;
								}
							}
							else if (tag_name.equals("rp")) {
								if (tag_level_rp > 0) {
									tag_level_rp--;
								}
							}
							else if (tag_name.equals("p")) {
								if (tag_level_p > 0) {
									tag_level_p--;

									if (tag_level_p == 0 && text_cnt > 0) {
										inputSB.append("\n");
									}
								}
							}
							else if (tag_name.equals("title")) {
								if (tag_level_title > 0) {
									tag_level_title--;
								}
							}
							else if (tag_name.equals("body")) {
								if (tag_level_body > 0) {
									tag_level_body--;
								}
							}
							else if (tag_name.equals("div")) {
								if (isDiv == false && inputSB.length() > 0) {
									inputSB.append("［＃改ページ］\n");
									isDiv = true;
								}
							}
							else if (tag_name.equals("h1") || tag_name.equals("h2") || tag_name.equals("h3")) {
								if (line_text.length() > 0) {
    								inputSB.append(line_text);
    								inputSB.append("［＃「" + line_text + "」は大見出し］\n");
    							}
   								text_cnt = 0;
								if (tag_level_h > 0) {
									tag_level_h--;
								}
							}
							else if (tag_name.equals("li") || tag_name.equals("dt") || tag_name.equals("dd")) {
								inputSB.append("\n");
							}
						} else if (eventType == XmlPullParser.TEXT) {
							//Log.d("XmlPullParserSample", "Text " + xmlPullParser.getText());
							String text = xmlPullParser.getText().replaceAll("[\n\t]", "");
							if (text.equals("!!")) {
								text = "‼";
							}
							else if (text.equals("!?")) {
								text = "⁉";
							}
							else if (text.equals("?!")) {
								text = "⁈";
							}
							else if (text.equals("??")) {
								text = "⁇";
							}
							text = text.replaceAll(" ", "");

							if (tag_level_title > 0) {
								if (text.length() > 0) {
									// タイトル文字列設定
									title = title + text;
								}
							}
							else if (tag_level_h > 0) {
								if (text.length() > 0) {
									line_text = line_text + text;
									text_cnt++;
								}
							}
							else if (tag_level_body > 0 && tag_level_rp == 0) {
								if (text.length() > 0) {
									inputSB.append(text);
									text_cnt++;
								}
							}
						}
						eventType = xmlPullParser.next();
					} catch (Exception e) {
						Log.e("formatText", "xml解析失敗");
//						inputSB = null;
						break;
					}
				}
			}
		}

		char[] intext;
		if (inputSB != null) {
			intext = inputSB.toString().toCharArray();
			inputSB = null;
		}
		else {
			intext = inputStr.toCharArray();
		}
		inputStr = null;

		ArrayList<LineData> linedata = new ArrayList<LineData>();
		int inpos = 0;

		mHeadSize = headfont;
		mTextSize = textfont;
		mRubiSize = rubifont;

		// テキスト全体を中間形式に配置
		parseTextData(intext, inpos, textbuff, linedata);
		if (mRunningFlag == false) {
			return;
		}

		// Char配列化
		mTextBuff = textbuff.toString().toCharArray();
		// StringBufferは解放
		textbuff = null;
		System.gc();

		// 先頭行の本文を文字列化
		if (linedata.size() <= 0) {
			return;
		}

		// タイトルの設定
		LineData ld = linedata.get(0);
		StringBuffer linestr = new StringBuffer();
		for (TextBlock tb : ld.textblock) {
			// TextBlockを文字列化
			linestr.append(getTextString(mTextBuff, tb.tx_index, tb.tx_length, ld.extdata));
		}
		// 1行目の本文をタイトルとして設定
		mTitle = linestr.toString();
		if (mTitle == null || mTitle.length() == 0) {
			mTitle = title;
		}

		LineManager dm = layoutLines(linedata);
		mTextPages = layoutPages(dm);
		return;
	}

	private LineManager layoutLines(ArrayList<LineData> linedata) {
		LineManager dm = new LineManager();

		// ファイル先頭コメントチェック
		int linemax = linedata.size();

		boolean isComment = false;
		int commst = 0;
		int commed = 0;

		LineData ld = null;

		for (int linecnt = 2; linecnt < linemax && linecnt < 500 && commed == 0; linecnt++) {
			// 行データを取得
			ld = linedata.get(linecnt);

			if (ld.textblock.length >= 1 && ld.textblock[0].tx_length >= 40) {
				// 40桁以上のときだけ
				boolean isAllMinus = true;
				for (int i = 0; i < ld.textblock[0].tx_length; i++) {
					// コメント行チェック
					if (mTextBuff[ld.textblock[0].tx_index + i] != '-') {
						// マイナスだけかのチェック
						isAllMinus = false;
						break;
					}
				}
				if (isAllMinus == true) {
					// コメント
					if (commst == 0) {
						commst = linecnt;
					}
					else {
						commed = linecnt;
					}
				}
			}
		}
		// コメント検索結果判定
		if (commst != 0 && commed != 0) {
			// コメントが見つかった
			isComment = true;
		}

		// 現在のフォントサイズ
		mFontSize = mHeadSize;

		char ascii[] = new char[0x80];
		for (int i = 0 ; i < 0x80 ; i ++) {
			// 半角文字を配列化
			ascii[i] = (char)i;
		}
		mTextPaint.setTextSize(mHeadSize);
		mTextPaint.getTextWidths(ascii, 0, ascii.length, mFontWidth[0]);
		mTextPaint.setTextSize(mRubiSize);
		mTextPaint.getTextWidths(ascii, 0, ascii.length, mFontWidth[1]);

		int progress = 0;
		sendMessage(DEF.HMSG_TX_LAYOUT, 0);

		int tx_row = 0; // 本文の行
		int rb_row = 0; // ルビの行

		mMidashi.initCurrent();

		for (int linecnt = 0; linecnt < linemax; linecnt++) {
			if (mRunningFlag == false) {
				// 処理中断のため全てクリア
				linedata.clear();
				break;
			}
			if (linecnt == 1) {
				// 二行目からは本文サイズ
				mFontSize = mTextSize;

				// 本文のサイズ
				mTextPaint.setTextSize(mTextSize);
				mTextPaint.getTextWidths(ascii, 0, ascii.length, mFontWidth[0]);
			}
			if (isComment && commst <= linecnt && linecnt <= commed) {
				// コメントの範囲は無視
				continue;
			}

			mMidashi.setLine(linecnt, tx_row);

			// 1行分のデータを取得
			ld = linedata.get(linecnt);

			float font_cy = mFontSize + mSpaceH; // 1文字の高さ
			float rubi_cy = mRubiSize;
			float tx_indent_y = ld.indent >= 0 ? (int) (ld.indent * font_cy) : 0;
			float rb_top_y = -(int) (rubi_cy / 2); // ルビ先頭位置初期化
			float tx_pos_y = 0;
			float tx_pos_y_first = 0;
			float rb_pos_y = rb_top_y;
			float area_cy = mTextHeight - (int) (tx_indent_y); // 本文の領域サイズ
			float style_st_y = 0.0f;
			float style_ed_y = 0.0f;
			int tx_cnt = 0;

			boolean isRubiAri = false; // ルビ有無
			boolean isRubiAriPrev;

			int style_idx = 0;
			StyleData style_data = null;
			if (ld.stylelist != null) {
				style_data = ld.stylelist[style_idx++];
			}
			TextDrawData tdd = null;
			int extidx = 0;

			// 地付き設定
			if (ld.indent < 0) {
				dm.setLineJituki(tx_row, true);
			}

			// テキストブロック分ループ
			for (int tbidx = 0; tbidx < ld.textblock.length; tbidx++) {
				// ブロックのサイズ／座標を求める
				TextBlock tb = ld.textblock[tbidx];

				isRubiAriPrev = isRubiAri; // 前ブロックのルビ有無
				isRubiAri = tb.rb_index > 0; // ルビ有無

				// テキストを割り当て
				int ti = tb.tx_index;
				int tl = tb.tx_length;
				int ri = tb.rb_index;
				int rl = tb.rb_length;
				float th = calcCharsHeight(mTextBuff, ti, tl, mFontSize, mSpaceH, true); // 本文の高さ //(int) (font_cy * tl);
				float rh = calcCharsHeight(mTextBuff, ri, rl, mRubiSize, 0.0f, false);//(int) (rubi_cy * rl); // ルビの高さ

				// まずは割り当て行に収まるかを確認
				if (ti == TB_PICTURE || ti == TB_PAGECONTROL) {
					// 画像入る or 改ページ
					if (tx_row < rb_row) {
						// ルビがすすんでいればその後に表示
						rb_row++;
						tx_row = rb_row;
					}
					else if (tx_pos_y > 0) {
						// 文字があれば改行
						tx_row++;
						rb_row = tx_row;
					}

					if (ti == -1) {
						// 画像入る or 改ページ
						if (tl < mPicArray.size()) {
							// 収まりきる部分だけ描画情報に追加
							dm.setLineData(tx_row, mFontSize, 0, 0, ti, tl, TYPE_TEXT, font_cy, 0, null, (short) 0, (short) mTextHeight);
						}
					}
					else {
						// 改ページor左右中央
						dm.setLineData(tx_row, mFontSize, 0, 0, ti, tl, TYPE_TEXT, font_cy, 0, null, (short) 0, (short) mTextHeight);
					}

					// 改行
					tx_pos_y = 0;
					rb_row = tx_row;
					break;
				}
				else if (rl > 0) {
					// ルビあり
					float rb_offset = (th - rh) / 2;
					// テキストからはみ出すか
					if (rb_offset < rubi_cy / 2 * -1) {
						// 上へのはみ出しはルビの半分まで
						rb_offset = (int) (rubi_cy / 2 * -1);
					}

					// 本文とルビの位置補正
					if (tx_row < rb_row) {
						// ルビだけ行が次へ行っている場合は本文もあわせる
						tx_row = rb_row;
						tx_pos_y = 0;
					}
					else if (tx_row > rb_row) {
						// ルビの行を本文の行にあわせる
						rb_row = tx_row;
						rb_pos_y = rb_top_y;
					}
					if (isRubiAriPrev && tx_pos_y < rb_pos_y) {
						// ルビがテキストにかかっていた場合はテキストをその後までずらす
						tx_pos_y = rb_pos_y;
					}
					if (tx_pos_y + rb_offset < rb_pos_y) {
						// ルビが前のルビにかかってしまう場合はその分テキストをずらす
						if (rb_offset < 0) {
							tx_pos_y = rb_pos_y - rb_offset;
						}
						else {
							tx_pos_y = rb_pos_y;
						}
					}
					rb_pos_y = tx_pos_y + rb_offset;
					tx_pos_y_first = tx_pos_y; // 本文の先頭位置を覚えておく
				}

				boolean isfirst = true;
				RemainingResult rr = new RemainingResult();

				while (true) {
					// 本文を配置
					while (tl > 0) {
						float tx_rest_cy = area_cy - tx_pos_y; // 残りの高さ
						int tl_adj; // 禁則補正後の設定文字数
						float th_adj;

						// 半角・全角単位で残りの高さに入る文字数を取得
						calcRemainingCount(mTextBuff, ti, tl, mFontSize, mSpaceH, tx_rest_cy, rr);
						tl_adj = rr.count;

						// ルビ先行時の開始位置調整
						if (isfirst == true) {
							if (isRubiAri && tl_adj == 0) {
								// 最初に文字が入らない
								rb_row++;
								rb_pos_y = (rb_pos_y - tx_pos_y);
							}
							isfirst = false;
						}

						// 禁則チッェク
						boolean kin_head = false;
						boolean kin_tail = false;
						if (0 < tl_adj) {
							// 文字あり
							if (rr.ascii == false && tx_rest_cy - font_cy * tl_adj < font_cy) {
								// 全角文字の時は禁則チェック下に1文字分の空きがない
								char code1 = mTextBuff[ti + tl_adj - 1]; // 最後の文字
								kin_tail = isKinsokuTail(code1); // 末尾禁則

								if (tl_adj < tl) {
									// ブロック途中の分断のとき
									char code2 = mTextBuff[ti + tl_adj]; // 次行の先頭文字
									kin_head = isKinsokuHead(code2); // 先頭禁則
								}
								else {
									// ブロックの区切りのとき
									if (tbidx + 1 < ld.textblock.length) {
										// 次のブロックがあれば
										TextBlock tb_nx = ld.textblock[tbidx + 1];
										int ti_nx = tb_nx.tx_index;
										int tl_nx = tb_nx.tx_length;

										if (ti_nx >= 0 && tl_nx > 0) {
											// 画像ではない
											char code2 = mTextBuff[ti_nx]; // 次ブロックの先頭文字
											kin_head = isKinsokuHead(code2); // 先頭禁則
										}
									}
								}
								if (kin_head == true && tl_adj >= 2) {
									// 先頭禁則文字であってもその前の文字が末尾禁則なら無視
									char code3 = mTextBuff[ti + tl_adj - 2];
									if (isKinsokuTail(code3) == true) {
										kin_head = false;
									}
								}
							}
							if (kin_head || kin_tail) {
								// 末尾禁止又は先頭禁止の場合1文字減らす
								tl_adj--;
								// この行は禁則あり
								dm.setLineKinsoku(tx_row, true); // 禁則状態
								rr.newline = true;
							}
						}

						if (rr.ascii == true) {
							th_adj = rr.height;
						}
						else {
							th_adj = font_cy * tl_adj - mSpaceH; // 本文の高さ
						}

						if (tl_adj > 0) {
							// 収まりきる部分だけ描画情報に追加
							float gap; 
							if (rr.ascii == true) {
								// 半角の場合文字列幅/文字数
								gap = rr.height / rr.count;
							}
							else {
								// 全角の場合はフォントサイズ+文字間
								gap = font_cy;
							}
							
							// 拡張データを必要なだけコピー
							char[][] extdata = copyExtData(ld.extdata, extidx, mTextBuff, ti, tl_adj);
							if (extdata != null) {
								extidx += extdata.length;
							}
							dm.setLineData(tx_row, mFontSize, 0, tx_pos_y, ti, tl_adj, TYPE_TEXT, gap, th_adj, extdata, (short) tx_indent_y, (short) area_cy);

							while (style_data != null) {
								// この行にスタイル設定あり
								int st = style_data.index;
								int ed = style_data.index + style_data.length;

								if (tx_cnt <= st && st < tx_cnt + tl_adj) {
									// 開始位置記録
									style_st_y = tx_pos_y + font_cy * (st - tx_cnt);
									// 終了位置記録
									if (ed > tx_cnt + tl_adj) {
										ed = tx_cnt + tl_adj;
									}
									style_ed_y = tx_pos_y + font_cy * (ed - tx_cnt) - mSpaceH;

									//
									int style_len = tx_cnt + tl_adj - st;
									if (style_len > style_data.length) {
										style_len = style_data.length;
									}

									// スタイルを設定
									if (tdd == null) {
										float y = 0;
										float cy = 0;
										float tensize;
										if ((style_data.type & STYLE_POINT_BASE) != 0) {
											// 傍点の場合はフォントのサイズ
											if (style_data.type == STYLE_POINT1 || style_data.type == STYLE_POINT2) {
												tensize = mFontSize / 1.5f;
											}
											else {
												tensize = mFontSize / 3;
											}
											y = (font_cy - tensize) / 2;
											cy = font_cy;
										}
										else {
											// 傍線の場合は範囲指定
											cy = style_ed_y - style_st_y;
											tensize = mFontSize;
										}

										// 傍点/傍線を表示
										tdd = dm.setLineData(tx_row, tensize, 0, style_st_y + y, style_data.type, style_len, TYPE_STYLE, cy, cy, null, (short) tx_indent_y, (short) area_cy);
										if ((style_data.type & STYLE_POINT_BASE) != 0) {
											// 傍点の場合は拡張しない
											tdd = null;
										}
									}
									else {
										tdd.mGap = style_ed_y - tdd.mTextY;
									}

									style_data.index += style_len;
									style_data.length -= style_len;
									if (style_data.length <= 0) {
										if (style_idx < ld.stylelist.length) {
											style_data = ld.stylelist[style_idx++];
											// スタイルの切り替え時にクリア
											tdd = null;
										}
										else {
											style_data = null;
										}
									}
									else {
										break;
									}
								}
								else {
									break;
								}
							}
						}

						tx_pos_y += th_adj + mSpaceH; // 描画位置ずらし

						// テキストの位置を進める
						ti += tl_adj;
						tl -= tl_adj;
						tx_cnt += tl_adj;

						if (rr.newline == true && tl > 0) {
							dm.setLineEnd(tx_row, false); // 行の途中を記録
							tx_pos_y = 0;
							tx_row++;
							tdd = null;
						}
					}
					if (tbidx >= ld.textblock.length - 1 || ld.textblock[tbidx + 1].rb_length > 0 || ld.textblock[tbidx + 1].tx_index == -1) {
						// ブロックがなくなった or 次がルビ有りブロック or 次が画像ブロック
						break;
					}
					// 次がルビなしブロックなら続けて設定
					tbidx++;
					// ブロックのサイズ／座標を求める
					tb = ld.textblock[tbidx];

					isRubiAriPrev = isRubiAri; // 前ブロックのルビ有無
					isRubiAri = tb.rb_index > 0; // ルビ有無

					// テキストを割り当て
					tl = tb.tx_length;
					ti = tb.tx_index;
					th = (int) (font_cy * tl); // 本文の高さ
				}

				// ルビを配置
				isfirst = true;
				while (rl > 0) {
					boolean kinsoku = dm.getLineKinsoku(rb_row); // 禁則有無
					float kin_h = kinsoku ? font_cy : 0;
					float rb_rest_cy = area_cy - kin_h - rb_pos_y; // 残りの高さ
					int rl_adj; // 禁則補正後の設定文字数
					float rh_adj; // 設定する高さ

					if (isfirst == true) {
						// 初回ループ
						if (rb_rest_cy < rh && tx_pos_y_first < rb_pos_y) {
							// ルビに改行がはいる場合は最初の行のルビ位置をテキストに合わせる
							rb_pos_y = tx_pos_y_first;
							rb_rest_cy = area_cy - kin_h - rb_pos_y;
						}
						isfirst = false;
					}

					// 半角・全角単位で残りの高さに入る文字数を取得
					calcRemainingCount(mTextBuff, ri, rl, mRubiSize, 0, rb_rest_cy, rr);
					rl_adj = rr.count;
					rh = rubi_cy * rl_adj; // ルビの高さ

					// 禁則チッェク
					boolean kin_head = false;
					if (rr.ascii == false && rl_adj < rl) {
						// 分割の場合
						char code1 = mTextBuff[ri + rl_adj]; // 最後の文字
						kin_head = isKinsokuHead(code1); // 末尾禁則

						if (kin_head || (rl - rl_adj == 1 && rl >= 4)) {
							// 末尾禁止の場合1文字減らす
							rl_adj--;
						}
					}

					if (rr.ascii == true) {
						// 半角
						rh_adj = rr.height;
					}
					else {
						// 全角
						rh_adj = rubi_cy * rl_adj; // ルビの高さ
					}

					if (rl_adj > 0) {
						// 収まりきる部分だけ描画情報に追加
						float gap; 
						if (rr.ascii == true) {
							// 半角の場合文字列幅/文字数
							gap = rr.height / rr.count;
						}
						else {
							// 全角の場合はフォントサイズ
							gap = rubi_cy;
						}
						
						// 拡張データを必要なだけコピー
						char[][] extdata = copyExtData(ld.extdata, extidx, mTextBuff, ri, rl_adj);
						if (extdata != null) {
							extidx += extdata.length;
						}

						// ルビ登録
						dm.setLineData(rb_row, mRubiSize, 0, rb_pos_y, ri, rl_adj, TYPE_RUBI, gap, rr.height, extdata, (short) tx_indent_y, (short) area_cy);
					}
					rb_pos_y += rh_adj; // 描画位置ずらし

					// テキストの位置を進める
					ri += rl_adj; // ルビ
					rl -= rl_adj;
					if (rr.newline == true && rl > 0) {
						// 残りの文字がある場合は改行
						rb_pos_y = 0; // ルビの途中改行はテキストの先頭にあわせる
						rb_row++;
					}
				}
			}

			// 行データを全て処理したので改行
			tx_row++;
			rb_row++;
			// 後ろの方が有効
			tx_row = Math.max(tx_row, rb_row);
			rb_row = Math.max(tx_row, rb_row);
			tx_pos_y = 0;
			rb_pos_y = rb_top_y;

			int pg = 50;
			if (linemax > 0) {
				pg = linecnt * 50 / linemax;
			}
			if (progress != pg) {
				progress = pg;
				sendMessage(DEF.HMSG_TX_LAYOUT, progress);
			}

		}
		mPictures = (PictureData[]) mPicArray.toArray(new PictureData[0]);
		return dm;
	}

	private TextDrawData[][] layoutPages(LineManager dm) {
		ArrayList<TextDrawData[]> pagelist = new ArrayList<TextDrawData[]>();
		ArrayList<TextDrawData> pagedata;
		ArrayList<TextDrawData> textlist;
		ArrayList<TextDrawData> rubilist;
		ArrayList<TextDrawData> stylelist;

		float pos_x = mTextRight;
		float font_size = mHeadSize;
		int linecnt = 0;

		int linemax = dm.getMaxLine();
		int progress = 0;
		sendMessage(DEF.HMSG_TX_LAYOUT, 50);

		mMidashi.initCurrent();

		pagedata = new ArrayList<TextDrawData>();
		while (true) {
			if (linecnt >= linemax) {
				// 全てのデータを処理済み
				if (pagedata != null && pagedata.size() > 0) {
					// 最終ページ登録
					pagelist.add((TextDrawData[]) pagedata.toArray(new TextDrawData[0]));
				}
				break;
			}

			textlist = dm.getTextLineData(linecnt);
			rubilist = dm.getRubiLineData(linecnt);
			stylelist = dm.getStyleLineData(linecnt);
			boolean lineend = dm.getLineEnd(linecnt); // 行末かどうか
			LineManager.TextState ts = dm.getLineState(linecnt);

			if (textlist != null && textlist.size() == 1 && textlist.get(0).mTextPos == TB_PICTURE) {
				// mTextPos==-1なら画像表示
				TextDrawData td = textlist.get(0);
				PictureData pd = mPicArray.get(td.mTextLen); // mTextLenに画像番号を設定
				boolean prenewpage = false;
				if (pd != null) {
					// TODO 画像
					if (mPicScale == BMPSCALE_SCREEN) {
						// まず改ページ
						prenewpage = true;
					}
					else if (pos_x + pd.mWidth > mTextWidth + mTextRight) {
						// 画像がページの残りの幅に入りきらない
						prenewpage = true;
					}

					if (prenewpage == true && pagedata.size() > 0) {
						// これまでのデータは改ページでページ登録
						pagelist.add((TextDrawData[]) pagedata.toArray(new TextDrawData[0]));

						// x座標初期化
						pos_x = mTextRight;
						pagedata = new ArrayList<TextDrawData>();
					}

					if (mPicScale == BMPSCALE_SCREEN) {
						// ページに全体表示
						pagedata = new ArrayList<TextDrawData>();
						td.mTextX = (mWidth - pd.mWidth) / 2;
						td.mTextY = (mHeight - pd.mHeight) / 2;
					}
					else {
						td.mTextX = pos_x;
						td.mTextY = (mHeight - pd.mHeight) / 2; // 高さは画面中央へ
					}

					// 画像情報登録
					pagedata.add(td);

					if (mPicScale == BMPSCALE_SCREEN) {
						// 次ページの先頭へ
						pagelist.add((TextDrawData[]) pagedata.toArray(new TextDrawData[0]));

						pos_x = mTextRight;
						pagedata = new ArrayList<TextDrawData>();
					}
					else {
						// 次の位置へ
						pos_x += pd.mWidth + mSpaceW;
					}
				}
			}
			else if (textlist != null && textlist.size() == 1 && textlist.get(0).mTextPos == TB_PAGECONTROL) {
				// mTextPos==-2なら制御情報
				TextDrawData td = textlist.get(0);
				if (td.mTextLen == PARAM_PAGECENTER) {
					// 左右中央
					if (pos_x > (mWidth - mFontSize) / 2 - mRubiSize) {
    					// これまでのデータがページ中央を越えている場合は改ページでページ登録
    					pagelist.add((TextDrawData[]) pagedata.toArray(new TextDrawData[0]));
    					pagedata = new ArrayList<TextDrawData>();
					}
					// ページ中央
				    pos_x = (mWidth - mFontSize) / 2 - mRubiSize;
				}
				else {
					// これまでのデータは改ページでページ登録
					pagelist.add((TextDrawData[]) pagedata.toArray(new TextDrawData[0]));
					pagedata = new ArrayList<TextDrawData>();
					// ページ先頭
				    pos_x = mTextRight;
				}
			}
			else {
				float linewidth = font_size + mRubiSize; // 行あたりの幅
				if (pos_x + linewidth > mTextWidth + mTextRight) {
					// 改ページでページ登録
					pagelist.add((TextDrawData[]) pagedata.toArray(new TextDrawData[0]));

					// x座標初期化
					pos_x = mTextRight;
					pagedata = new ArrayList<TextDrawData>();
				}

				// ページ設定
				mMidashi.setPage(linecnt, pagelist.size());

				float max_h = 0.0f;	// 下端の座標
				float max_t = 0.0f;	// 下端文字の頭座標
				float scale = 1.0f;
				float offset = 0.0f;

				// 本文で一番下を求める
				if (textlist != null) {
					int tn = textlist.size();
					if (tn > 0) {
						TextDrawData td = textlist.get(tn - 1);
						float h = td.mTextY + td.mHeight;
						float t = td.mTextY + td.mGap * (td.mTextLen - 1);
						if (max_h < h) {
							max_h = h;
						}
						if (max_t < t) {
							max_t = t;
						}
					}
				}
				// ルビで一番下を求める
				if (rubilist != null) {
					int rn = rubilist.size();
					if (rn > 0) {
						TextDrawData rd = rubilist.get(rn - 1);
						float h = rd.mTextY + rd.mHeight;
						float t = rd.mTextY + rd.mGap * (rd.mTextLen - 1);
						if (max_h < h) {
							max_h = h;
						}
						if (max_t < t) {
							max_t = t;
						}
					}
				}
				if (ts.jituki == false) {
					// 地付きではない場合は均等
					float scalewk = 1.0f; 

					if (max_h > 0.0f && ts.height > max_h) {
						float dif_h = ts.height - max_h;
						// 均等割り当ての最大は抑制
						scalewk = (max_t + dif_h) / max_t; 
					}

					if (lineend == true) {
						float scalewk2 = 1.0f; 
						// 行末
						if (textlist != null) {
							for (int i = 0 ; i < textlist.size() ; i ++) {
								TextDrawData td = textlist.get(i);
								if (td.mTextPos >= 0) {
									int chcnt = (int) ((ts.height + mSpaceH) / td.mGap);	// 1行の最大文字数 
									float t = (chcnt - 1) * td.mGap;
									float h = chcnt * td.mGap - mSpaceH;
									scalewk2 = (t + (ts.height - h)) / t;
									break;
								}
							}
						}
						// 小さい方を設定
						scale = Math.min(scalewk,  scalewk2);
					}
					else {
						// 行末ではない
						scale = scalewk; 
					}
				}
				else {
					// 地付きの場合は下詰め
					offset = ts.height - max_h;
				}

				// scale = 1.0f; // TODO test
				int tx_pd_idx1 = pagedata.size();
				if (textlist != null) {
					for (TextDrawData td : textlist) {
						td.mTextX = pos_x + mRubiSize;
						if (td.mIsAscii) {
							// 半角
							float th_half = td.mHeight / 2;
							float y = (td.mTextY + th_half) * scale;
							td.mTextY = (y - th_half) + ts.top + mTextTop + offset;
						}
						else {
							// 全角
							td.mTextY = td.mTextY * scale + ts.top + mTextTop + offset;
							td.mGap *= scale;
						}
						pagedata.add(td);
					}
					textlist.clear();
				}
				int tx_pd_idx2 = pagedata.size();
				int rb_pd_idx1 = pagedata.size();
				if (rubilist != null) {
					for (TextDrawData rd : rubilist) {
						float rh_half = (rd.mGap * rd.mTextLen) / 2;
						float y = (rd.mTextY + rh_half) * scale;
						rd.mTextY = y - rh_half + ts.top + mTextTop + offset;
						rd.mTextX = pos_x;
						pagedata.add(rd);
					}
					rubilist.clear();
				}
				int rb_pd_idx2 = pagedata.size();
				int st_pd_idx1 = pagedata.size();
				if (stylelist != null) {
					for (TextDrawData sd : stylelist) {
						sd.mTextX = pos_x + mRubiSize - (mTextSize * getStyleOffset(sd.mTextPos) / 100);	// 本文基準にする
						sd.mTextY = sd.mTextY * scale + ts.top + mTextTop + offset;
						sd.mGap *= scale;
						pagedata.add(sd);
					}
					stylelist.clear();
				}
				int st_pd_idx2 = pagedata.size();
				// ルビと傍点傍線が重なるらなら位置ずらし
				// まずはチェック
				int overlap_shift = 0;
				if (stylelist != null && rubilist != null) {
					for (int ri = rb_pd_idx1; ri < rb_pd_idx2; ri++) {
						TextDrawData rd = pagedata.get(ri);
						float ry1 = rd.mTextY;
						float ry2 = ry1 + rd.mTextLen * rd.mGap;
						for (int si = st_pd_idx1; si < st_pd_idx2; si++) {
							TextDrawData sd = pagedata.get(si);
							float sy1 = sd.mTextY;
							float sy2 = sy1 + sd.mTextLen * sd.mGap;
							if (ry1 <= sy2 && sy1 <= ry2) {
								// 重なりあり
								int os_wk = (int) (mTextSize * getStyleWidth(sd.mTextPos) / 100);
								if (overlap_shift < os_wk) {
									overlap_shift = os_wk;
								}
							}
						}
					}
				}
				if (overlap_shift > 0) {
					for (int ri = rb_pd_idx1; ri < rb_pd_idx2; ri++) {
						TextDrawData rd = pagedata.get(ri);
						float ry1 = rd.mTextY;
						float ry2 = ry1 + rd.mTextLen * rd.mGap;
						float overlap_rb = 0;
						for (int si = st_pd_idx1; si < st_pd_idx2; si++) {
							TextDrawData sd = pagedata.get(si);
							float sy1 = sd.mTextY;
							float sy2 = sy1 + sd.mTextLen * sd.mGap;
							if (ry1 <= sy2 && sy1 <= ry2) {
								// 重なりあり
								int os_wk = (int) (mTextSize * getStyleWidth(sd.mTextPos) / 100);
								if (overlap_rb < os_wk) {
									overlap_rb = os_wk;
								}
							}
						}
						rd.mTextX += overlap_shift - overlap_rb;
					}
					for (int si = st_pd_idx1; si < st_pd_idx2; si++) {
						TextDrawData sd = pagedata.get(si);
						sd.mTextX += overlap_shift;
					}
					for (int ti = tx_pd_idx1; ti < tx_pd_idx2; ti++) {
						TextDrawData td = pagedata.get(ti);
						td.mTextX += overlap_shift;
					}
					pos_x += overlap_shift;
				}
				pos_x += font_size + mRubiSize + mSpaceW;
				font_size = mTextSize;
			}
			linecnt++;

			int pg = 100;
			if (linemax > 0) {
				pg = linecnt * 50 / linemax + 50;
			}
			if (progress != pg) {
				progress = pg;
				sendMessage(DEF.HMSG_TX_LAYOUT, progress);
			}
		}
		dm.clear();
		return (TextDrawData[][]) pagelist.toArray(new TextDrawData[0][]);
	}

	// 句点指定の漢字コードをUNICODEに変換
	private char searchCodeTable(int src_kuten) {
		int size = mCodeTbl.length / CODETBL_BLOCK;
		int rng_st = 0;
		int rng_ed = size - 1;

		while (rng_st <= rng_ed) {
			int idx = rng_st + (rng_ed - rng_st) / 2;
			int bidx = idx * CODETBL_BLOCK;
			int dst_kuten = (((int) mCodeTbl[bidx] << 8) & 0xFF00) | ((int) mCodeTbl[bidx + 1] & 0xFF);
			if (dst_kuten == src_kuten) {
				char result = (char) ((((int) mCodeTbl[bidx + 2] << 8) & 0xFF00) | ((int) mCodeTbl[bidx + 3] & 0xFF));
				return result;
			}
			else if (src_kuten < dst_kuten) {
				// 小さいときは
				rng_ed = idx - 1;
			}
			else {
				rng_st = idx + 1;
			}
		}
		return 0;
	}

	private class CommentResult {
		public int code;
		public int param1;
		public int param2;
		public char[] extdata;

		CommentResult() {
			code = COMMENT_NONE;
			param1 = 0;
			param2 = 0;
			extdata = null;
		}
	}
	
	private CommentResult analyzeComment(String comment) {
		CommentResult result = new CommentResult();
		int size = comment.length();
		if (size <= 3 || !comment.substring(0, 2).equals("［＃")) {
			// コメントになってない
			return result;
		}
		if (size >= 7) {
			// ×字下げチェック
			String work1 = comment.substring(size - 4);
			String work2 = comment.substring(2, size - 4);
			if (work1.equals("字下げ］")) {
				//
				try {
					int val = Integer.parseInt(work2);
					result.code = COMMENT_JISAGE;
					result.param1 = val;
					return result;
				} catch (NumberFormatException e) {
					;
				}
			}
		}
		if (size >= 11) {
			// ここから×字下げチェック
			String work1 = comment.substring(0, 6);
			String work2 = comment.substring(size - 4);
			String work3 = comment.substring(6, size - 4);
			if (work1.equals("［＃ここから") && work2.equals("字下げ］")) {
				//
				try {
					int val = Integer.parseInt(work3);
					result.code = COMMENT_JISAGE_RANGE;
					result.param1 = val;
					return result;
				} catch (NumberFormatException e) {
					;
				}
			}
		}
		// 地付きチェック
		if (comment.equals("［＃地付き］")) {
			result.code = COMMENT_JITUKI;
			result.param1 = 0;
			return result;
		}
//		if (size >= 8) {
//			// 文字の置き換えチェック
//			int idx = comment.indexOf("字（fig");
//			if (idx > 0) {
//				String work = comment.substring(idx + 1);
//				String filename = null;
//				// ファイル名切り出し
//				for (int i = 0; i < work.length(); i++) {
//					char ch = work.charAt(i);
//					if (ch >= 0x80) {
//						filename = work.substring(0, i);
//						break;
//					}
//				}
//				int fileindex = loadBitmap(filename, true);
//				if (fileindex >= 0 && fileindex < 768) {
//					// ファイル名あり
//					result.code = COMMENT_PICTURE_CHAR;
//					result.param1 = 0x1a00 + fileindex;
//					result.param2 = 1;
//					return result;
//				}
//			}
//		}
		if (size >= 8) {
			// 挿絵チェック
			if (comment.endsWith("）入る］") || comment.endsWith("）］")) {
				int wk = 0;
				int idx = 0;
				while (wk >= 0) {
					wk = comment.indexOf('（', wk + 1);
					if (wk >= 0) {
						idx = wk + 1;
					}
				}
				if (idx > 0) {
					// String work2 = comment.substring(idx);
					// String filename = null;
					int lastidx = comment.lastIndexOf('）');
					String filename = comment.substring(idx, lastidx);
					// ファイル名切り出し
					for (int i = 0; i < filename.length(); i++) {
						char ch = filename.charAt(i);
						if (ch == '、' || ch == '。' || ch == '．' || ch == '，') {
							filename = filename.substring(0, i);
							break;
						}
					}
					// ファイル名中のカレントディレクトリ指定は削除
					if (filename.startsWith("./")) {
						filename = filename.substring(2);
					}
					int fileindex = loadBitmap(filename, false);
					if (fileindex >= 0) {
						// ファイル名あり
						result.code = COMMENT_PICTURE;
						result.param1 = fileindex;
						return result;
					}
				}
			}
		}
		if (comment.equals("［＃ここで字下げ終わり］")) {
			result.code = COMMENT_JISAGE_RANGE;
			result.param1 = 0;
			return result;
		}
		if (comment.equals("［＃改ページ］")) {
			result.code = COMMENT_PAGECONTROL;
			result.param1 = PARAM_PAGEBREAK;
			return result;
		}
		if (comment.equals("［＃ページの左右中央］")) {
			result.code = COMMENT_PAGECONTROL;
			result.param1 = PARAM_PAGECENTER;
			return result;
		}
		{
			// 見出し（範囲指定）
			int cmd = -1;
			int type = 0;
			int pos = 0;
			if (size == 7 && comment.endsWith("見出し］")) {
				cmd = COMMENT_MIDASHI_ST;
				pos = 2;
			}
			if (size == 11 && comment.startsWith("［＃ここから") && comment.endsWith("見出し］")) {
				cmd = COMMENT_MIDASHI_ST;
				pos = 6;
			}
			else if (size == 10 && comment.endsWith("見出し終わり］")) {
				cmd = COMMENT_MIDASHI_ED;
				pos = 2;
			}
			else if (size == 13 && comment.startsWith("［＃ここで") && comment.endsWith("見出し終わり］")) {
				cmd = COMMENT_MIDASHI_ED;
				pos = 5;
			}
			if (cmd != -1) {
				char code = comment.charAt(pos);
				//
				if (code == '小') {
					type = 1;
				}
				else if (code == '中') {
					type = 2;
				}
				else if (code == '大') {
					type = 3;
				}

				if (type != 0) {
					result.code = cmd;
					result.param1 = type;
					return result;
				}
			}
		}
		if (comment.startsWith("［＃「")) {
			int cmd = -1;
			int type = 0;
			int len = 0;
			if (comment.endsWith("」は小見出し］")) {
				cmd = COMMENT_MIDASHI;
				type = 1;
				len = size - 10;
			}
			else if (comment.endsWith("」は中見出し］")) {
				cmd = COMMENT_MIDASHI;
				type = 2;
				len = size - 10;
			}
			else if (comment.endsWith("」は大見出し］")) {
				cmd = COMMENT_MIDASHI;
				type = 3;
				len = size - 10;
			}
			else if (comment.endsWith("」に傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT1;
				len = size - 8;
			}
			else if (comment.endsWith("」に白ゴマ傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT2;
				len = size - 11;
			}
			else if (comment.endsWith("」に丸傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT3;
				len = size - 9;
			}
			else if (comment.endsWith("」に白丸傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT4;
				len = size - 10;
			}
			else if (comment.endsWith("」に黒三角傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT5;
				len = size - 11;
			}
			else if (comment.endsWith("」に白三角傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT6;
				len = size - 11;
			}
			else if (comment.endsWith("」に二重丸傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT7;
				len = size - 11;
			}
			else if (comment.endsWith("」に蛇の目傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT8;
				len = size - 11;
			}
			else if (comment.endsWith("」の左に傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT1;
				len = size - 10;
			}
			else if (comment.endsWith("」の左に白ゴマ傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT2;
				len = size - 13;
			}
			else if (comment.endsWith("」の左に丸傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT3;
				len = size - 11;
			}
			else if (comment.endsWith("」の左に白丸傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT4;
				len = size - 12;
			}
			else if (comment.endsWith("」の左に黒三角傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT5;
				len = size - 13;
			}
			else if (comment.endsWith("」の左に白三角傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT6;
				len = size - 13;
			}
			else if (comment.endsWith("」の左に二重丸傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT7;
				len = size - 13;
			}
			else if (comment.endsWith("」の左に蛇の目傍点］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_POINT8;
				len = size - 13;
			}
			else if (comment.endsWith("」に傍線］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_LINE1;
				len = size - 8;
			}
			else if (comment.endsWith("」に二重傍線］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_LINE2;
				len = size - 10;
			}
			else if (comment.endsWith("」に鎖線］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_LINE3;
				len = size - 8;
			}
			else if (comment.endsWith("」に破線］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_LINE4;
				len = size - 8;
			}
			else if (comment.endsWith("」に波線］")) {
				cmd = COMMENT_TEXTSTYLE;
				type = STYLE_LINE5;
				len = size - 8;
			}

			if (cmd != -1) {
				boolean rubi = false;
				int cnt = 0;
				for (int i = 0; i < len; i++) {
					char code = comment.charAt(i + 3);
					if (code == '《') {
						rubi = true;
					}
					else if (code == '》') {
						rubi = false;
					}
					else if (rubi == false) {
						cnt++;
					}
				}

				result.code = cmd;
				result.param1 = type;
				result.param2 = cnt;
				return result;
			}
		}
		{
			int cmd = -1;
			int type = 0;
			if (comment.startsWith("［＃傍点") || comment.startsWith("［＃左に傍点")) {
				type = STYLE_POINT1;
			}
			else if (comment.startsWith("［＃白ゴマ傍点") || comment.startsWith("［＃左に白ゴマ傍点")) {
				type = STYLE_POINT2;
			}
			else if (comment.startsWith("［＃丸傍点") || comment.startsWith("［＃左に丸傍点")) {
				type = STYLE_POINT3;
			}
			else if (comment.startsWith("［＃白丸傍点") || comment.startsWith("［＃左に白丸傍点")) {
				type = STYLE_POINT4;
			}
			else if (comment.startsWith("［＃黒三角傍点") || comment.startsWith("［＃左に黒三角傍点")) {
				type = STYLE_POINT5;
			}
			else if (comment.startsWith("［＃白三角傍点") || comment.startsWith("［＃左に白三角傍点")) {
				type = STYLE_POINT6;
			}
			else if (comment.startsWith("［＃二重丸傍点") || comment.startsWith("［＃左に二重丸傍点")) {
				type = STYLE_POINT7;
			}
			else if (comment.startsWith("［＃蛇の目傍点") || comment.startsWith("［＃左に蛇の目傍点")) {
				type = STYLE_POINT8;
			}
			else if (comment.startsWith("［＃傍線") || comment.startsWith("［＃左に傍線")) {
				type = STYLE_LINE1;
			}
			else if (comment.startsWith("［＃二重傍線") || comment.startsWith("［＃左に二重傍線")) {
				type = STYLE_LINE2;
			}
			else if (comment.startsWith("［＃鎖線") || comment.startsWith("［＃左に鎖線")) {
				type = STYLE_LINE3;
			}
			else if (comment.startsWith("［＃破線") || comment.startsWith("［＃左に破線")) {
				type = STYLE_LINE4;
			}
			else if (comment.startsWith("［＃波線") || comment.startsWith("［＃左に波線")) {
				type = STYLE_LINE5;
			}

			if (type != 0) {
				// 指定あり
				if (comment.endsWith("終わり］"))  {
					cmd = COMMENT_TEXTSTYLE_ED;
				}
				else {
					cmd = COMMENT_TEXTSTYLE_ST;
				}
				result.code = cmd;
				result.param1 = type;
				return result;
			}
		}
		if (size >= 11) {
			// ここから×字下げチェック
			String work1 = comment.substring(0, 6);
			String work2 = comment.substring(size - 4);
			String work3 = comment.substring(6, size - 4);
			if (work1.equals("［＃ここから") && work2.equals("字下げ］")) {
				//
				try {
					int val = Integer.parseInt(work3);
					result.code = COMMENT_JISAGE_RANGE;
					result.param1 = val;
					return result;
				} catch (NumberFormatException e) {
					;
				}
			}
		}

		int code = 0;
		String extdata = null; 
		int szpos = comment.indexOf("水準");
		if (szpos >= 2) {
			// 面区点コードを数値化
			int seppos;
			String work[] = new String[3];

			szpos += 2; // 水準の次へ
			// 面番号
			seppos = comment.indexOf("-", szpos);
			if (seppos > 0) {
				work[0] = comment.substring(szpos, seppos);
				szpos = seppos + 1;
			}
			// 区番号
			seppos = comment.indexOf("-", szpos);
			if (seppos > 0) {
				work[1] = comment.substring(szpos, seppos);
				szpos = seppos + 1;
			}
			// 点番号
			seppos = comment.indexOf("］", szpos);
			if (seppos > 0) {
				work[2] = comment.substring(szpos, seppos);
			}
			if (work[0] != null && work[1] != null && work[2] != null) {
				try {
					code = Integer.parseInt(work[0]) * 10000 + Integer.parseInt(work[1]) * 100 + Integer.parseInt(work[2]);
					result.code = COMMENT_STRCODE;
					result.param1 = searchCodeTable(code);
					result.param2 = 1;
				} catch (NumberFormatException e) {
					;
				}
			}
			return result;
		}
		int len = 1;
		if (comment.startsWith("［＃感嘆符二つ")) {
			code = '‼';
		}
		else if (comment.startsWith("［＃感嘆符三つ")) {
			code = CHARDRAW_YOKO;
			extdata = "!!!";
		}
		else if (comment.startsWith("［＃疑問符二つ")) {
			code = '⁇';
		}
		else if (comment.startsWith("［＃疑問符感嘆符")) {
			code = '⁈';
		}
		else if (comment.startsWith("［＃感嘆符疑問符")) {
			code = '⁉';
		}
		else if (comment.startsWith("［＃疑問符一つ感嘆符二つ")) {
			code = CHARDRAW_YOKO;
			extdata = "?!!";
		}
		else if (comment.startsWith("［＃逆感嘆符")) {
			code = '¡';
		}
		else if (comment.startsWith("［＃逆疑問符")) {
			code = '¿';
		}
		else if (comment.startsWith("［＃始め二重括弧")) {
			code = '｟';
		}
		else if (comment.startsWith("［＃終わり二重括弧")) {
			code = '｠';
		}
		else if (comment.startsWith("［＃始め二重きっこう（亀甲）括弧")) {
			code = '〘';
		}
		else if (comment.startsWith("［＃終わり二重きっこう（亀甲）括弧")) {
			code = '〙';
		}
		else if (comment.startsWith("［＃始めすみ付き括弧（白）")) {
			code = '〖';
		}
		else if (comment.startsWith("［＃終わりすみ付き括弧（白）")) {
			code = '〗';
		}
		else if (comment.startsWith("［＃始め二重山括弧引用記号，始めギュメ")) {
			code = '«';
		}
		else if (comment.startsWith("［＃終わり二重山括弧引用記号，終わりギュメ")) {
			code = '»';
		}
		else if (comment.startsWith("［＃二の字点")) {
			code = '〻';
		}
		else if (comment.startsWith("［＃ます記号")) {
			code = '〼';
		}
		else if (comment.startsWith("［＃コト")) {
			code = 'ヿ';
		}
		else if (comment.startsWith("［＃より")) {
			code = 'ゟ';
		}
		else if (comment.startsWith("［＃濁点付き")) {
			String target = comment.substring(6).replaceAll("[、］].*$", ""); 
			if (target.equals("片仮名ワ")) {
				code = 'ヷ';
			}
			else if (target.equals("片仮名ヰ")) {
				code = 'ヸ';
			}
			else if (target.equals("片仮名ヱ")) {
				code = 'ヹ';
			}
			else if (target.equals("片仮名ヲ")) {
				code = 'ヺ';
			}
			else if (target.equals("平仮名う")) {
				code = 'ゔ';
			}
			else if (target.equals("小書き平仮名つ")) {
				code = CHARDRAW_KOGAKIDAKUTEN;
				code = 'っ';
			}
			else {
    			int targetlen = target.length();
    			if (targetlen > 0) {
    				code = CHARDRAW_DAKUTEN;
    				// 拡張データに文字を保持
    				extdata = target.substring(targetlen - 1);
    			}
			}
		}
		else if (comment.startsWith("［＃半濁点付き")) {
			String target = comment.substring(7).replaceAll("[、］].*$", "");
			if (target.equals("小書き片仮名フ")) {
				code = CHARDRAW_KOGAKIHANDAKU;	// 半濁点
				extdata = "フ";
			}
			else {
    			int targetlen = target.length();
    			if (targetlen > 0) {
    				code = CHARDRAW_HANDAKU;	// 半濁点
    				// 拡張データに文字を保持
    				extdata = target.substring(targetlen - 1);
    			}
			}
		}
		else if (comment.startsWith("［＃小書き")) {
			String target = comment.substring(5).replaceAll("[、］].*$", "");
			int targetlen = target.length();
			if (targetlen > 0) {
				code = CHARDRAW_KOGAKI;
				// 拡張データに文字を保持
				extdata = target.substring(targetlen - 1);
			}
		}
		else if (comment.startsWith("［＃丸")) {
			String target = comment.substring(3).replaceAll("[、］].*$", ""); 
			if (target.equals("ア")) {
				code = '㋐';
			}
			else if (target.equals("イ")) {
				code = '㋑';
			}
			else if (target.equals("ウ")) {
				code = '㋒';
			}
			else if (target.equals("エ")) {
				code = '㋓';
			}
			else if (target.equals("オ")) {
				code = '㋔';
			}
			else if (target.equals("カ")) {
				code = '㋕';
			}
			else if (target.equals("キ")) {
				code = '㋖';
			}
			else if (target.equals("ク")) {
				code = '㋗';
			}
			else if (target.equals("ケ")) {
				code = '㋘';
			}
			else if (target.equals("コ")) {
				code = '㋙';
			}
			else if (target.equals("サ")) {
				code = '㋚';
			}
			else if (target.equals("シ")) {
				code = '㋛';
			}
			else if (target.equals("ス")) {
				code = '㋜';
			}
			else if (target.equals("セ")) {
				code = '㋝';
			}
			else if (target.equals("ソ")) {
				code = '㋞';
			}
			else if (target.equals("タ")) {
				code = '㋟';
			}
			else if (target.equals("チ")) {
				code = '㋠';
			}
			else if (target.equals("ツ")) {
				code = '㋡';
			}
			else if (target.equals("テ")) {
				code = '㋢';
			}
			else if (target.equals("ト")) {
				code = '㋣';
			}
			else if (target.equals("ナ")) {
				code = '㋤';
			}
			else if (target.equals("ニ")) {
				code = '㋥';
			}
			else if (target.equals("ヌ")) {
				code = '㋦';
			}
			else if (target.equals("ネ")) {
				code = '㋧';
			}
			else if (target.equals("ノ")) {
				code = '㋨';
			}
			else if (target.equals("ハ")) {
				code = '㋩';
			}
			else if (target.equals("ヒ")) {
				code = '㋪';
			}
			else if (target.equals("フ")) {
				code = '㋫';
			}
			else if (target.equals("ヘ")) {
				code = '㋬';
			}
			else if (target.equals("ホ")) {
				code = '㋭';
			}
			else if (target.equals("マ")) {
				code = '㋮';
			}
			else if (target.equals("ミ")) {
				code = '㋯';
			}
			else if (target.equals("ム")) {
				code = '㋰';
			}
			else if (target.equals("メ")) {
				code = '㋱';
			}
			else if (target.equals("モ")) {
				code = '㋲';
			}
			else if (target.equals("ヤ")) {
				code = '㋳';
			}
			else if (target.equals("ユ")) {
				code = '㋴';
			}
			else if (target.equals("ヨ")) {
				code = '㋵';
			}
			else if (target.equals("ラ")) {
				code = '㋶';
			}
			else if (target.equals("リ")) {
				code = '㋷';
			}
			else if (target.equals("ル")) {
				code = '㋸';
			}
			else if (target.equals("レ")) {
				code = '㋹';
			}
			else if (target.equals("ロ")) {
				code = '㋺';
			}
			else if (target.equals("ワ")) {
				code = '㋻';
			}
			else if (target.equals("ヰ")) {
				code = '㋼';
			}
			else if (target.equals("ヱ")) {
				code = '㋽';
			}
			else if (target.equals("ヲ")) {
				code = '㋾';
			}
			else if (target.equals("月")) {
				code = '㊊';
			}
			else if (target.equals("火")) {
				code = '㊋';
			}
			else if (target.equals("水")) {
				code = '㊌';
			}
			else if (target.equals("木")) {
				code = '㊍';
			}
			else if (target.equals("金")) {
				code = '㊎';
			}
			else if (target.equals("土")) {
				code = '㊏';
			}
			else if (target.equals("日")) {
				code = '㊐';
			}
			else if (target.equals("株")) {
				code = '㊑';
			}
			else if (target.equals("有")) {
				code = '㊒';
			}
			else if (target.equals("社")) {
				code = '㊓';
			}
			else if (target.equals("名")) {
				code = '㊔';
			}
			else if (target.equals("特")) {
				code = '㊕';
			}
			else if (target.equals("財")) {
				code = '㊖';
			}
			else if (target.equals("祝")) {
				code = '㊗';
			}
			else if (target.equals("労")) {
				code = '㊘';
			}
			else if (target.equals("秘")) {
				code = '㊙';
			}
			else if (target.equals("男")) {
				code = '㊚';
			}
			else if (target.equals("女")) {
				code = '㊛';
			}
			else if (target.equals("適")) {
				code = '㊜';
			}
			else if (target.equals("優")) {
				code = '㊝';
			}
			else if (target.equals("印")) {
				code = '㊞';
			}
			else if (target.equals("注")) {
				code = '㊟';
			}
			else if (target.equals("項")) {
				code = '㊠';
			}
			else if (target.equals("休")) {
				code = '㊡';
			}
			else if (target.equals("写")) {
				code = '㊢';
			}
			else if (target.equals("正")) {
				code = '㊣';
			}
			else if (target.equals("付き上")) {
				code = '㊤';
			}
			else if (target.equals("付き中")) {
				code = '㊥';
			}
			else if (target.equals("付き下")) {
				code = '㊦';
			}
			else if (target.equals("付き左")) {
				code = '㊧';
			}
			else if (target.equals("付き右")) {
				code = '㊨';
			}
			else if (target.equals("医")) {
				code = '㊩';
			}
			else if (target.equals("宗")) {
				code = '㊪';
			}
			else if (target.equals("学")) {
				code = '㊫';
			}
			else if (target.equals("監")) {
				code = '㊬';
			}
			else if (target.equals("企")) {
				code = '㊭';
			}
			else if (target.equals("資")) {
				code = '㊮';
			}
			else if (target.equals("協")) {
				code = '㊯';
			}
			else if (target.equals("夜")) {
				code = '㊰';
			}
			else if (target.equals("A大文字")) {
				code = 'Ⓐ';
			}
			else if (target.equals("B大文字")) {
				code = 'Ⓑ';
			}
			else if (target.equals("C大文字")) {
				code = 'Ⓒ';
			}
			else if (target.equals("D大文字")) {
				code = 'Ⓓ';
			}
			else if (target.equals("E大文字")) {
				code = 'Ⓔ';
			}
			else if (target.equals("F大文字")) {
				code = 'Ⓕ';
			}
			else if (target.equals("G大文字")) {
				code = 'Ⓖ';
			}
			else if (target.equals("H大文字")) {
				code = 'Ⓗ';
			}
			else if (target.equals("I大文字")) {
				code = 'Ⓘ';
			}
			else if (target.equals("J大文字")) {
				code = 'Ⓙ';
			}
			else if (target.equals("K大文字")) {
				code = 'Ⓚ';
			}
			else if (target.equals("L大文字")) {
				code = 'Ⓛ';
			}
			else if (target.equals("M大文字")) {
				code = 'Ⓜ';
			}
			else if (target.equals("N大文字")) {
				code = 'Ⓝ';
			}
			else if (target.equals("O大文字")) {
				code = 'Ⓞ';
			}
			else if (target.equals("P大文字")) {
				code = 'Ⓟ';
			}
			else if (target.equals("Q大文字")) {
				code = 'Ⓠ';
			}
			else if (target.equals("R大文字")) {
				code = 'Ⓡ';
			}
			else if (target.equals("S大文字")) {
				code = 'Ⓢ';
			}
			else if (target.equals("T大文字")) {
				code = 'Ⓣ';
			}
			else if (target.equals("U大文字")) {
				code = 'Ⓤ';
			}
			else if (target.equals("V大文字")) {
				code = 'Ⓥ';
			}
			else if (target.equals("W大文字")) {
				code = 'Ⓦ';
			}
			else if (target.equals("X大文字")) {
				code = 'Ⓧ';
			}
			else if (target.equals("Y大文字")) {
				code = 'Ⓨ';
			}
			else if (target.equals("Z大文字")) {
				code = 'Ⓩ';
			}
			else if (target.equals("A小文字")) {
				code = 'ⓐ';
			}
			else if (target.equals("B小文字")) {
				code = 'ⓑ';
			}
			else if (target.equals("C小文字")) {
				code = 'ⓒ';
			}
			else if (target.equals("D小文字")) {
				code = 'ⓓ';
			}
			else if (target.equals("E小文字")) {
				code = 'ⓔ';
			}
			else if (target.equals("F小文字")) {
				code = 'ⓕ';
			}
			else if (target.equals("G小文字")) {
				code = 'ⓖ';
			}
			else if (target.equals("H小文字")) {
				code = 'ⓗ';
			}
			else if (target.equals("I小文字")) {
				code = 'ⓘ';
			}
			else if (target.equals("J小文字")) {
				code = 'ⓙ';
			}
			else if (target.equals("K小文字")) {
				code = 'ⓚ';
			}
			else if (target.equals("L小文字")) {
				code = 'ⓛ';
			}
			else if (target.equals("M小文字")) {
				code = 'ⓜ';
			}
			else if (target.equals("N小文字")) {
				code = 'ⓝ';
			}
			else if (target.equals("O小文字")) {
				code = 'ⓞ';
			}
			else if (target.equals("P小文字")) {
				code = 'ⓟ';
			}
			else if (target.equals("Q小文字")) {
				code = 'ⓠ';
			}
			else if (target.equals("R小文字")) {
				code = 'ⓡ';
			}
			else if (target.equals("S小文字")) {
				code = 'ⓢ';
			}
			else if (target.equals("T小文字")) {
				code = 'ⓣ';
			}
			else if (target.equals("U小文字")) {
				code = 'ⓤ';
			}
			else if (target.equals("V小文字")) {
				code = 'ⓥ';
			}
			else if (target.equals("W小文字")) {
				code = 'ⓦ';
			}
			else if (target.equals("X小文字")) {
				code = 'ⓧ';
			}
			else if (target.equals("Y小文字")) {
				code = 'ⓨ';
			}
			else if (target.equals("Z小文字")) {
				code = 'ⓩ';
			}
			else if (target.equals("付きマイナス")) {
				code = '⊖';
			}
			else if (target.equals("中黒")) {
				code = '⦿';
			}
			else if (target.equals("一")) {
				code = '㊀';
			}
			else if (target.equals("二")) {
				code = '㊁';
			}
			else if (target.equals("三")) {
				code = '㊂';
			}
			else if (target.equals("四")) {
				code = '㊃';
			}
			else if (target.equals("五")) {
				code = '㊄';
			}
			else if (target.equals("八")) {
				code = '㊇';
			}
			else if (target.equals("十")) {
				code = '㊉';
			}
			else if (target.equals("10")) {
				code = '⑩';
			}
			else if (target.equals("11")) {
				code = '⑪';
			}
			else if (target.equals("12")) {
				code = '⑫';
			}
			else if (target.equals("13")) {
				code = '⑬';
			}
			else if (target.equals("14")) {
				code = '⑭';
			}
			else if (target.equals("15")) {
				code = '⑮';
			}
			else if (target.equals("16")) {
				code = '⑯';
			}
			else if (target.equals("17")) {
				code = '⑰';
			}
			else if (target.equals("18")) {
				code = '⑱';
			}
			else if (target.equals("19")) {
				code = '⑲';
			}
			else if (target.equals("20")) {
				code = '⑳';
			}
			else if (target.equals("21")) {
				code = '㉑';
			}
			else if (target.equals("22")) {
				code = '㉒';
			}
			else if (target.equals("23")) {
				code = '㉓';
			}
			else if (target.equals("24")) {
				code = '㉔';
			}
			else if (target.equals("25")) {
				code = '㉕';
			}
			else if (target.equals("26")) {
				code = '㉖';
			}
			else if (target.equals("27")) {
				code = '㉗';
			}
			else if (target.equals("28")) {
				code = '㉘';
			}
			else if (target.equals("29")) {
				code = '㉙';
			}
			else if (target.equals("30")) {
				code = '㉚';
			}
			else if (target.equals("31")) {
				code = '㉛';
			}
			else if (target.equals("32")) {
				code = '㉜';
			}
			else if (target.equals("33")) {
				code = '㉝';
			}
			else if (target.equals("34")) {
				code = '㉞';
			}
			else if (target.equals("35")) {
				code = '㉟';
			}
			else if (target.equals("36")) {
				code = '㊱';
			}
			else if (target.equals("37")) {
				code = '㊲';
			}
			else if (target.equals("38")) {
				code = '㊳';
			}
			else if (target.equals("39")) {
				code = '㊴';
			}
			else if (target.equals("40")) {
				code = '㊵';
			}
			else if (target.equals("41")) {
				code = '㊶';
			}
			else if (target.equals("42")) {
				code = '㊷';
			}
			else if (target.equals("43")) {
				code = '㊸';
			}
			else if (target.equals("44")) {
				code = '㊹';
			}
			else if (target.equals("45")) {
				code = '㊺';
			}
			else if (target.equals("46")) {
				code = '㊻';
			}
			else if (target.equals("47")) {
				code = '㊼';
			}
			else if (target.equals("48")) {
				code = '㊽';
			}
			else if (target.equals("49")) {
				code = '㊾';
			}
			else if (target.equals("50")) {
				code = '㊿';
			}
			else if (target.equals("0")) {
				code = '⓪';
			}
			else if (target.equals("1")) {
				code = '①';
			}
			else if (target.equals("2")) {
				code = '②';
			}
			else if (target.equals("3")) {
				code = '③';
			}
			else if (target.equals("4")) {
				code = '④';
			}
			else if (target.equals("5")) {
				code = '⑤';
			}
			else if (target.equals("6")) {
				code = '⑥';
			}
			else if (target.equals("7")) {
				code = '⑦';
			}
			else if (target.equals("8")) {
				code = '⑧';
			}
			else if (target.equals("9")) {
				code = '⑨';
			}
		}
		else if (comment.startsWith("［＃ローマ数字")) {
			String target = comment.substring(7).replaceAll("[、］].*$", ""); 
			if (target.equals("1小文字")) {
				code = 'ⅰ';
			}
			else if (target.equals("2小文字")) {
				code = 'ⅱ';
			}
			else if (target.equals("3小文字")) {
				code = 'ⅲ';
			}
			else if (target.equals("4小文字")) {
				code = 'ⅳ';
			}
			else if (target.equals("5小文字")) {
				code = 'ⅴ';
			}
			else if (target.equals("6小文字")) {
				code = 'ⅵ';
			}
			else if (target.equals("7小文字")) {
				code = 'ⅶ';
			}
			else if (target.equals("8小文字")) {
				code = 'ⅷ';
			}
			else if (target.equals("9小文字")) {
				code = 'ⅸ';
			}
			else if (target.equals("10小文字")) {
				code = 'ⅹ';
			}
			else if (target.equals("11小文字")) {
				code = 'ⅺ';
			}
			else if (target.equals("12小文字")) {
				code = 'ⅻ';
			}
			else if (target.equals("10")) {
				code = 'Ⅹ';
			}
			else if (target.equals("11")) {
				code = 'Ⅺ';
			}
			else if (target.equals("12")) {
				code = 'Ⅻ';
			}
			else if (target.equals("1")) {
				code = 'Ⅰ';
			}
			else if (target.equals("2")) {
				code = 'Ⅱ';
			}
			else if (target.equals("3")) {
				code = 'Ⅲ';
			}
			else if (target.equals("4")) {
				code = 'Ⅳ';
			}
			else if (target.equals("5")) {
				code = 'Ⅴ';
			}
			else if (target.equals("6")) {
				code = 'Ⅵ';
			}
			else if (target.equals("7")) {
				code = 'Ⅶ';
			}
			else if (target.equals("8")) {
				code = 'Ⅷ';
			}
			else if (target.equals("9")) {
				code = 'Ⅸ';
			}
		}
		else if (comment.startsWith("［＃黒丸")) {
			String target = comment.substring(4).replaceAll("[、］].*$", ""); 
			if (target.equals("10")) {
				code = '❿';
			}
			else if (target.equals("11")) {
				code = '⓫';
			}
			else if (target.equals("12")) {
				code = '⓬';
			}
			else if (target.equals("13")) {
				code = '⓭';
			}
			else if (target.equals("14")) {
				code = '⓮';
			}
			else if (target.equals("15")) {
				code = '⓯';
			}
			else if (target.equals("16")) {
				code = '⓰';
			}
			else if (target.equals("17")) {
				code = '⓱';
			}
			else if (target.equals("18")) {
				code = '⓲';
			}
			else if (target.equals("19")) {
				code = '⓳';
			}
			else if (target.equals("20")) {
				code = '⓴';
			}
			else if (target.equals("1")) {
				code = '❶';
			}
			else if (target.equals("2")) {
				code = '❷';
			}
			else if (target.equals("3")) {
				code = '❸';
			}
			else if (target.equals("4")) {
				code = '❹';
			}
			else if (target.equals("5")) {
				code = '❺';
			}
			else if (target.equals("6")) {
				code = '❻';
			}
			else if (target.equals("7")) {
				code = '❼';
			}
			else if (target.equals("8")) {
				code = '❽';
			}
			else if (target.equals("9")) {
				code = '❾';
			}
		}
		else if (comment.startsWith("［＃二重丸")) {
			String target = comment.substring(5).replaceAll("[、］].*$", ""); 
			if (target.equals("［10")) {
				code = '⓾';
			}
			else if (target.equals("1")) {
				code = '⓵';
			}
			else if (target.equals("2")) {
				code = '⓶';
			}
			else if (target.equals("3")) {
				code = '⓷';
			}
			else if (target.equals("4")) {
				code = '⓸';
			}
			else if (target.equals("5")) {
				code = '⓹';
			}
			else if (target.equals("6")) {
				code = '⓺';
			}
			else if (target.equals("7")) {
				code = '⓻';
			}
			else if (target.equals("8")) {
				code = '⓼';
			}
			else if (target.equals("9")) {
				code = '⓽';
			}
		}
		else if (comment.startsWith("［＃キャロン付き")) {
			String target = comment.substring(8).replaceAll("[、］].*$", ""); 
			if (target.equals("A小文字")) {
				code = 'ǎ';
			}
			else if (target.equals("I小文字")) {
				code = 'ǐ';
			}
			else if (target.equals("O小文字")) {
				code = 'ǒ';
			}
			else if (target.equals("u小文字")) {
				code = 'ǔ';
			}
			else if (target.equals("L小文字")) {
				code = 'ľ';
			}
			else if (target.equals("S小文字")) {
				code = 'š';
			}
			else if (target.equals("T小文字")) {
				code = 'ť';
			}
			else if (target.equals("Z小文字")) {
				code = 'ž';
			}
			else if (target.equals("C小文字")) {
				code = 'č';
			}
			else if (target.equals("E小文字")) {
				code = 'ě';
			}
			else if (target.equals("D小文字")) {
				code = 'ď';
			}
			else if (target.equals("N小文字")) {
				code = 'ň';
			}
			else if (target.equals("R小文字")) {
				code = 'ř';
			}
			else if (target.equals("A")) {
				code = 'Ǎ';
			}
			else if (target.equals("O")) {
				code = 'Ǒ';
			}
			else if (target.equals("L")) {
				code = 'Ľ';
			}
			else if (target.equals("S")) {
				code = 'Š';
			}
			else if (target.equals("T")) {
				code = 'Ť';
			}
			else if (target.equals("Z")) {
				code = 'Ž';
			}
			else if (target.equals("C")) {
				code = 'Č';
			}
			else if (target.equals("E")) {
				code = 'Ě';
			}
			else if (target.equals("D")) {
				code = 'Ď';
			}
			else if (target.equals("N")) {
				code = 'Ň';
			}
			else if (target.equals("R")) {
				code = 'Ř';
			}
		}
		else if (comment.startsWith("［＃マクロンとダイエレシス付きU小文字")) {
			code = 'ǖ';
		}
		else if (comment.startsWith("［＃アキュートアクセントとダイエレシス付きU小文字")) {
			code = 'ǘ';
		}
		else if (comment.startsWith("［＃キャロンとダイエレシス付きU小文字")) {
			code = 'ǚ';
		}
		else if (comment.startsWith("［＃グレーブアクセントとダイエレシス付きU小文字")) {
			code = 'ǜ';
		}
		else if (comment.startsWith("［＃アイスランド語ETH")) {
			code = 'Ð';
		}
		else if (comment.startsWith("［＃アイスランド語THORN")) {
			code = 'Þ';
		}
		else if (comment.startsWith("［＃アイスランド語ETH小文字")) {
			code = 'ð';
		}
		else if (comment.startsWith("［＃アイスランド語THORN小文字")) {
			code = 'þ';
		}
		else if (comment.startsWith("［＃オゴネク付きA")) {
			code = 'Ą';
		}
		else if (comment.startsWith("［＃上ドット付きZ")) {
			code = 'Ż';
		}
		else if (comment.startsWith("［＃オゴネク付きA小文字")) {
			code = 'ą';
		}
		else if (comment.startsWith("［＃上ドット付きZ小文字")) {
			code = 'ż';
		}
		else if (comment.startsWith("［＃ブリーブ付きA")) {
			code = 'Ă';
		}
		else if (comment.startsWith("［＃オゴネク付きE")) {
			code = 'Ę';
		}
		else if (comment.startsWith("［＃ダブルアキュートアクセント付きO")) {
			code = 'Ő';
		}
		else if (comment.startsWith("［＃ダブルアキュートアクセント付きU")) {
			code = 'Ű';
		}
		else if (comment.startsWith("［＃ブリーブ付きA小文字")) {
			code = 'ă';
		}
		else if (comment.startsWith("［＃オゴネク付きE小文字")) {
			code = 'ę';
		}
		else if (comment.startsWith("［＃ダブルアキュートアクセント付きO小文字")) {
			code = 'ő';
		}
		else if (comment.startsWith("［＃ダブルアキュートアクセント付きU小文字")) {
			code = 'ű';
		}
		else if (comment.startsWith("［＃ブリーブ付きU")) {
			code = 'Ŭ';
		}
		else if (comment.startsWith("［＃ブリーブ付きU小文字")) {
			code = 'ŭ';
		}
		else if (comment.startsWith("［＃フック付きM小文字，有声唇歯鼻音")) {
			code = 'ɱ';
		}
		else if (comment.startsWith("［＃フック付きV小文字，有声唇歯接近音")) {
			code = 'ʋ';
		}
		else if (comment.startsWith("［＃フィッシュフック付きR小文字，有声歯茎弾き音")) {
			code = 'ɾ';
		}
		else if (comment.startsWith("［＃発音記号")) {
			code = 'ʃ';
		}
		else if (comment.startsWith("［＃発音記号")) {
			code = 'ʒ';
		}
		else if (comment.startsWith("［＃ベルト付きL小文字，無声歯茎側面摩擦音")) {
			code = 'ɬ';
		}
		else if (comment.startsWith("［＃LEZH小文字，有声歯茎側面摩擦音")) {
			code = 'ɮ';
		}
		else if (comment.startsWith("［＃ターンドR小文字，無声歯茎接近音")) {
			code = 'ɹ';
		}
		else if (comment.startsWith("［＃レトロフレックスフック付きT小文字，無声そり舌破裂音")) {
			code = 'ʈ';
		}
		else if (comment.startsWith("［＃テール付きD小文字，有声そり舌破裂音")) {
			code = 'ɖ';
		}
		else if (comment.startsWith("［＃レトロフレックスフック付きN小文字，有声そり舌鼻音")) {
			code = 'ɳ';
		}
		else if (comment.startsWith("［＃テール付きR小文字，有声そり舌弾き音")) {
			code = 'ɽ';
		}
		else if (comment.startsWith("［＃フック付きS小文字，無声そり舌摩擦音")) {
			code = 'ʂ';
		}
		else if (comment.startsWith("［＃レトロフレックスフック付きZ小文字，有声そり舌摩擦音")) {
			code = 'ʐ';
		}
		else if (comment.startsWith("［＃フック付きターンドR小文字，有声そり舌接近音")) {
			code = 'ɻ';
		}
		else if (comment.startsWith("［＃レトロフレックスフック付きL小文字，有声そり舌側面接近音")) {
			code = 'ɭ';
		}
		else if (comment.startsWith("［＃ストローク付きドットなしJ小文字，有声硬口蓋破裂音")) {
			code = 'ɟ';
		}
		else if (comment.startsWith("［＃左フック付きN小文字，有声硬口蓋鼻音")) {
			code = 'ɲ';
		}
		else if (comment.startsWith("［＃クロスドテール付きJ小文字，有声硬口蓋摩擦音")) {
			code = 'ʝ';
		}
		else if (comment.startsWith("［＃ターンドY小文字，有声硬口")) {
			code = 'ʎ';
		}
		else if (comment.startsWith("［＃スクリプトG小文字，有声軟口蓋破裂音")) {
			code = 'ɡ';
		}
		else if (comment.startsWith("［＃発音記号")) {
			code = 'ŋ';
		}
		else if (comment.startsWith("［＃ロングレッグ付きターンドM小文字，有声軟口蓋接近音")) {
			code = 'ɰ';
		}
		else if (comment.startsWith("［＃インバーテッドRスモールキャピタル，有声口蓋垂摩擦音")) {
			code = 'ʁ';
		}
		else if (comment.startsWith("［＃リバースドグロッタルストップ，有声咽頭摩擦音")) {
			code = 'ʕ';
		}
		else if (comment.startsWith("［＃グロッタルストップ，無声声門破裂音")) {
			code = 'ʔ';
		}
		else if (comment.startsWith("［＃フック付きH小文字，有声声門摩擦音")) {
			code = 'ɦ';
		}
		else if (comment.startsWith("［＃両唇吸着音")) {
			code = 'ʘ';
		}
		else if (comment.startsWith("［＃硬口蓋歯茎吸着音")) {
			code = 'ǂ';
		}
		else if (comment.startsWith("［＃フック付きB小文字，有声両唇内破音")) {
			code = 'ɓ';
		}
		else if (comment.startsWith("［＃フック付きD小文字，有声歯茎内破音")) {
			code = 'ɗ';
		}
		else if (comment.startsWith("［＃フックとストローク付きドットなしJ小文字，有声硬口蓋内破音")) {
			code = 'ʄ';
		}
		else if (comment.startsWith("［＃フック付きG小文字，有声軟口蓋内破音")) {
			code = 'ɠ';
		}
		else if (comment.startsWith("［＃フック付きG大文字，有声口蓋垂内破音")) {
			code = 'Ɠ';
		}
		else if (comment.startsWith("［＃バー付きU小文字，円唇中舌狭母音")) {
			code = 'ʉ';
		}
		else if (comment.startsWith("［＃リバースドE小文字，非円唇中舌狭・中段母音")) {
			code = 'ɘ';
		}
		else if (comment.startsWith("［＃バー付きO小文字，円唇中舌狭・中段母音")) {
			code = 'ɵ';
		}
		else if (comment.startsWith("［＃発音記号")) {
			code = 'ə';
		}
		else if (comment.startsWith("［＃リバースドオープンE小文字，非円唇中舌広・中段母音")) {
			code = 'ɜ';
		}
		else if (comment.startsWith("［＃クローズドリバースドオープンE小文字，円唇中舌広・中段母音")) {
			code = 'ɞ';
		}
		else if (comment.startsWith("［＃ターンドA小文字，非円唇中舌狭めの広母音")) {
			code = 'ɐ';
		}
		else if (comment.startsWith("［＃ターンドM小文字，非円唇後舌狭母音")) {
			code = 'ɯ';
		}
		else if (comment.startsWith("［＃UPSILON小文字，円唇後舌広めの狭母音")) {
			code = 'ʊ';
		}
		else if (comment.startsWith("［＃ベビーガンマ，非円唇後舌狭中段母音")) {
			code = 'ɤ';
		}
		else if (comment.startsWith("［＃ターンドV小文字，非円唇後舌広・中段母音")) {
			code = 'ʌ';
		}
		else if (comment.startsWith("［＃発音記号")) {
			code = 'ɔ';
		}
		else if (comment.startsWith("［＃スクリプトA小文字，非円唇後舌広母音")) {
			code = 'ɑ';
		}
		else if (comment.startsWith("［＃ターンドALPHA小文字，円唇後舌広母音")) {
			code = 'ɒ';
		}
		else if (comment.startsWith("［＃ターンドW小文字，無声両唇・軟口蓋摩擦音")) {
			code = 'ʍ';
		}
		else if (comment.startsWith("［＃ターンドH小文字，有声両唇・硬口蓋接近音")) {
			code = 'ɥ';
		}
		else if (comment.startsWith("［＃ストローク付きリバースドグロッタルストップ，有声咽頭蓋摩擦音")) {
			code = 'ʢ';
		}
		else if (comment.startsWith("［＃ストローク付きグロッタルストップ，咽頭蓋破裂音")) {
			code = 'ʡ';
		}
		else if (comment.startsWith("［＃カール付きC小文字，歯茎・硬口蓋摩擦音")) {
			code = 'ɕ';
		}
		else if (comment.startsWith("［＃カール付きZ小文字，歯茎・硬口蓋摩擦音")) {
			code = 'ʑ';
		}
		else if (comment.startsWith("［＃ロングレッグ付きターンドR小文字，歯茎側面弾き音")) {
			code = 'ɺ';
		}
		else if (comment.startsWith("［＃フック付きHENG小文字，無声後部歯茎軟口蓋摩擦音")) {
			code = 'ɧ';
		}
		else if (comment.startsWith("［＃フック付きSCHWA")) {
			code = 'ɚ';
		}
		else if (comment.startsWith("［＃グレーブアクセント付きAE小文字")) {
			code = 'æ';
		}
		else if (comment.startsWith("［＃アキュートアクセント付きAE小文字")) {
			code = 'ǽ';
		}
		else if (comment.startsWith("［＃グレーブアクセント付きスクリプトA小文字")) {
			code = 'ὰ';
		}
		else if (comment.startsWith("［＃グレーブアクセント付きオープンO小文字")) {
			code = 'ɔ';
		}
		else if (comment.startsWith("［＃アキュートアクセント付きオープンO小文字")) {
			code = 'ɔ';
		}
		else if (comment.startsWith("［＃グレーブアクセント付きターンドV小文字")) {
			code = 'ʌ';
		}
		else if (comment.startsWith("［＃アキュートアクセント付きターンドV小文字")) {
			code = 'ʌ';
		}
		else if (comment.startsWith("［＃グレーブアクセント付きSCHWA小文字")) {
			code = 'ə';
		}
		else if (comment.startsWith("［＃発音記号")) {
			code = 'ə';
		}
		else if (comment.startsWith("［＃グレーブアクセントとフック付きSCHWA小文字")) {
			code = 'ɚ';
		}
		else if (comment.startsWith("［＃アキュートアクセントとフック付きSCHWA小文字")) {
			code = 'ɚ';
		}
		else if (comment.startsWith("［＃「v」は下線（_）付き")) {
			code = 'v';
		}
		else if (comment.startsWith("［＃ギリシア小文字ファイナルSIGMA")) {
			code = 'ς';
		}
		else if (comment.startsWith("［＃グレーブアクセント付きEPSILON小文字")) {
			code = 'ὲ';
		}
		else if (comment.startsWith("［＃アキュートアクセント付きEPSILON小文字")) {
			code = 'έ';
		}
		else if (comment.startsWith("［＃部分集合の否定")) {
			code = '⊄';
		}
		else if (comment.startsWith("［＃部分集合の否定（逆方向）")) {
			code = '⊅';
		}
		else if (comment.startsWith("［＃真部分集合2")) {
			code = '⊊';
		}
		else if (comment.startsWith("［＃真部分集合2（逆方向）")) {
			code = '⊋';
		}
		else if (comment.startsWith("［＃要素の否定，元の否定")) {
			code = '∉';
		}
		else if (comment.startsWith("［＃空集合")) {
			code = '∅';
		}
		else if (comment.startsWith("［＃射影的関係")) {
			code = '⌅';
		}
		else if (comment.startsWith("［＃背景的関係")) {
			code = '⌆';
		}
		else if (comment.startsWith("［＃直和")) {
			code = '⊕';
		}
		else if (comment.startsWith("［＃テンソル積")) {
			code = '⊗';
		}
		else if (comment.startsWith("［＃平行")) {
			code = '∥';
		}
		else if (comment.startsWith("［＃平行の否定")) {
			code = '∦';
		}
		else if (comment.startsWith("［＃合同否定")) {
			code = '≢';
		}
		else if (comment.startsWith("［＃漸進的に等しい，ホモトープ")) {
			code = '≃';
		}
		else if (comment.startsWith("［＃同形")) {
			code = '≅';
		}
		else if (comment.startsWith("［＃同相")) {
			code = '≈';
		}
		else if (comment.startsWith("［＃小さいか大きい")) {
			code = '≶';
		}
		else if (comment.startsWith("［＃大きいか小さい")) {
			code = '≷';
		}
		else if (comment.startsWith("［＃同等")) {
			code = '↔';
		}
		else if (comment.startsWith("［＃小さいか等しいか大きい")) {
			code = '⋚';
		}
		else if (comment.startsWith("［＃大きいか等しいか小さい")) {
			code = '⋛';
		}
		else if (comment.startsWith("［＃負又は正符号")) {
			code = '∓';
		}
		else if (comment.startsWith("［＃アレフ")) {
			code = 'ℵ';
		}
		else if (comment.startsWith("［＃エイチバー")) {
			code = 'ℏ';
		}
		else if (comment.startsWith("［＃2プラス")) {
			code = '⧺';
		}
		else if (comment.startsWith("［＃3プラス")) {
			code = '⧻';
		}
		else if (comment.startsWith("［＃直角")) {
			code = '∟';
		}
		else if (comment.startsWith("［＃経路積分記号")) {
			code = '∮';
		}
		else if (comment.startsWith("［＃２分の１")) {
			code = '½';
		}
		else if (comment.startsWith("［＃３分の１")) {
			code = '⅓';
		}
		else if (comment.startsWith("［＃３分の２")) {
			code = '⅔';
		}
		else if (comment.startsWith("［＃４分の１")) {
			code = '¼';
		}
		else if (comment.startsWith("［＃４分の３")) {
			code = '¾';
		}
		else if (comment.startsWith("［＃５分の１")) {
			code = '⅕';
		}
		else if (comment.startsWith("［＃ＨＰ，ホースパワー（馬力）")) {
			code = '㏋';
		}
		else if (comment.startsWith("［＃リットル")) {
			code = 'ℓ';
		}
		else if (comment.startsWith("［＃モー")) {
			code = '℧';
		}
		else if (comment.startsWith("［＃不特定通貨記号")) {
			code = '¤';
		}
		else if (comment.startsWith("［＃ユーロ記号")) {
			code = '€';
		}
		else if (comment.startsWith("［＃「オンス」の単位記号")) {
			code = '℥';
		}
		else if (comment.startsWith("［＃破断線")) {
			code = '¦';
		}
		else if (comment.startsWith("［＃著作権表示記号")) {
			code = '©';
		}
		else if (comment.startsWith("［＃登録商標記号")) {
			code = '®';
		}
		else if (comment.startsWith("［＃女性序数標識")) {
			code = 'ª';
		}
		else if (comment.startsWith("［＃男性序数標識")) {
			code = 'º';
		}
		else if (comment.startsWith("［＃ダブルアステ")) {
			code = '⁑';
		}
		else if (comment.startsWith("［＃アステリズム")) {
			code = '⁂';
		}
		else if (comment.startsWith("［＃左半黒丸")) {
			code = '◐';
		}
		else if (comment.startsWith("［＃右半黒丸")) {
			code = '◑';
		}
		else if (comment.startsWith("［＃下半黒丸")) {
			code = '◒';
		}
		else if (comment.startsWith("［＃上半黒丸")) {
			code = '◓';
		}
		else if (comment.startsWith("［＃右向三角")) {
			code = '▷';
		}
		else if (comment.startsWith("［＃右向黒三角")) {
			code = '▶';
		}
		else if (comment.startsWith("［＃左向三角")) {
			code = '◁';
		}
		else if (comment.startsWith("［＃左向黒三角")) {
			code = '◀';
		}
		else if (comment.startsWith("［＃スペード（白）")) {
			code = '♤';
		}
		else if (comment.startsWith("［＃スペード")) {
			code = '♠';
		}
		else if (comment.startsWith("［＃ダイヤ（白）")) {
			code = '♢';
		}
		else if (comment.startsWith("［＃ダイヤ")) {
			code = '♦';
		}
		else if (comment.startsWith("［＃ハート（白）")) {
			code = '♡';
		}
		else if (comment.startsWith("［＃ハート")) {
			code = '♥';
		}
		else if (comment.startsWith("［＃クラブ（白）")) {
			code = '♧';
		}
		else if (comment.startsWith("［＃クラブ")) {
			code = '♣';
		}
		else if (comment.startsWith("［＃白将棋駒")) {
			code = '☖';
		}
		else if (comment.startsWith("［＃黒将棋駒")) {
			code = '☗';
		}
		else if (comment.startsWith("［＃郵便マーク")) {
			code = '〠';
		}
		else if (comment.startsWith("［＃電話マーク")) {
			code = '☎';
		}
		else if (comment.startsWith("［＃晴マーク")) {
			code = '☀';
		}
		else if (comment.startsWith("［＃曇マーク")) {
			code = '☁';
		}
		else if (comment.startsWith("［＃雨マーク")) {
			code = '☂';
		}
		else if (comment.startsWith("［＃雪マーク")) {
			code = '☃';
		}
		else if (comment.startsWith("［＃温泉マーク")) {
			code = '♨';
		}
		else if (comment.startsWith("［＃指示マーク")) {
			code = '☞';
		}
		else if (comment.startsWith("［＃チェックマーク")) {
			code = '✓';
		}
		else if (comment.startsWith("［＃コマンド記号")) {
			code = '⌘';
		}
		else if (comment.startsWith("［＃空白記号")) {
			code = '␣';
		}
		else if (comment.startsWith("［＃リターン記号（リターン）")) {
			code = '⏎';
		}
		else if (comment.startsWith("［＃四つ菱")) {
			code = '❖';
		}
		else if (comment.startsWith("［＃平行四辺形")) {
			code = '▱';
		}
		else if (comment.startsWith("［＃直角三角")) {
			code = '⊿';
		}
		else if (comment.startsWith("［＃白ビュレット")) {
			code = '◦';
		}
		else if (comment.startsWith("［＃ビュレット")) {
			code = '•';
		}
		else if (comment.startsWith("［＃白ゴマ")) {
			code = '﹆';
		}
		else if (comment.startsWith("［＃ゴマ")) {
			code = '﹅';
		}
		else if (comment.startsWith("［＃蛇の目")) {
			code = '◉';
		}
		else if (comment.startsWith("［＃歌記号")) {
			code = '〽';
		}
		else if (comment.startsWith("［＃左上向矢印")) {
			code = '↖';
		}
		else if (comment.startsWith("［＃右上向矢印")) {
			code = '↗';
		}
		else if (comment.startsWith("［＃右下向矢印")) {
			code = '↘';
		}
		else if (comment.startsWith("［＃左下向矢印")) {
			code = '↙';
		}
		else if (comment.startsWith("［＃右矢印左矢印")) {
			code = '⇄';
		}
		else if (comment.startsWith("［＃左向白矢印")) {
			code = '⇦';
		}
		else if (comment.startsWith("［＃上向白矢印")) {
			code = '⇧';
		}
		else if (comment.startsWith("［＃右向白矢印")) {
			code = '⇨';
		}
		else if (comment.startsWith("［＃下向白矢印")) {
			code = '⇩';
		}
		else if (comment.startsWith("［＃曲がり矢印上がる")) {
			code = '⤴';
		}
		else if (comment.startsWith("［＃曲がり矢印下がる")) {
			code = '⤵';
		}
		else if (comment.startsWith("［＃四分音符")) {
			code = '♩';
		}
		else if (comment.startsWith("［＃連こう（桁）付き八分音符")) {
			code = '♫';
		}
		else if (comment.startsWith("［＃連こう（桁）付き十六分音符")) {
			code = '♬';
		}
		else if (comment.startsWith("［＃ナチュラル")) {
			code = '♮';
		}
		else if (comment.startsWith("［＃全角")) {
			String target = comment.substring(4).replaceAll("[、］].*$", ""); 
			if (target.equals("括弧付き株")) {
				code = '㈱';
			}
			else if (target.equals("括弧付き有")) {
				code = '㈲';
			}
			else if (target.equals("括弧付き代")) {
				code = '㈹';
			}
			else if (target.equals("株式会社")) {
				code = '㍿';
			}
			else if (target.equals("ミリ")) {
				code = '㍉';
			}
			else if (target.equals("キロ")) {
				code = '㌔';
			}
			else if (target.equals("センチ")) {
				code = '㌢';
			}
			else if (target.equals("メートル")) {
				code = '㍍';
			}
			else if (target.equals("グラム")) {
				code = '㌘';
			}
			else if (target.equals("トン")) {
				code = '㌧';
			}
			else if (target.equals("アール")) {
				code = '㌃';
			}
			else if (target.equals("ヘクタール")) {
				code = '㌶';
			}
			else if (target.equals("リットル")) {
				code = '㍑';
			}
			else if (target.equals("ワット")) {
				code = '㍗';
			}
			else if (target.equals("カロリー")) {
				code = '㌍';
			}
			else if (target.equals("ドル")) {
				code = '㌦';
			}
			else if (target.equals("セント")) {
				code = '㌣';
			}
			else if (target.equals("パーセント")) {
				code = '㌫';
			}
			else if (target.equals("ミリバール")) {
				code = '㍊';
			}
			else if (target.equals("ページ")) {
				code = '㌻';
			}
			else if (target.equals("MM")) {
				code = '㎜';
			}
			else if (target.equals("CM")) {
				code = '㎝';
			}
			else if (target.equals("KM")) {
				code = '㎞';
			}
			else if (target.equals("MG")) {
				code = '㎎';
			}
			else if (target.equals("KG")) {
				code = '㎏';
			}
			else if (target.equals("CC")) {
				code = '㏄';
			}
			else if (target.equals("M2")) {
				code = '㎡';
			}
			else if (target.equals("NO")) {
				code = '№';
			}
			else if (target.equals("KK")) {
				code = '㏍';
			}
			else if (target.equals("TEL")) {
				code = '℡';
			}
			else if (target.equals("元号明治")) {
				code = '㍾';
			}
			else if (target.equals("元号大正")) {
				code = '㍽';
			}
			else if (target.equals("元号昭和")) {
				code = '㍼';
			}
			else if (target.equals("元号平成")) {
				code = '㍻';
			}
		}
		else if (comment.startsWith("［＃「卍」を左右反転したもの")) {
			code = '卐';
		}
		else if (comment.startsWith("［＃AE小文字")) {
			code = 'æ';
		}
		else if (comment.endsWith("は縦中横］")) { 
			String target = comment.substring(3).replaceAll("」.*$", ""); 
    		if (target.equals("（1）")) {
    			code = '⑴';
    			len = 3;
    		}
    		else if (target.equals("（2）")) {
    			code = '⑵';
    			len = 3;
    		}
    		else if (target.equals("（3）")) {
    			code = '⑶';
    			len = 3;
    		}
    		else if (target.equals("（4）")) {
    			code = '⑷';
    			len = 3;
    		}
    		else if (target.equals("（5）")) {
    			code = '⑸';
    			len = 3;
    		}
    		else if (target.equals("（6）")) {
    			code = '⑹';
    			len = 3;
    		}
    		else if (target.equals("（7）")) {
    			code = '⑺';
    			len = 3;
    		}
    		else if (target.equals("（8）")) {
    			code = '⑻';
    			len = 3;
    		}
    		else if (target.equals("（9）")) {
    			code = '⑼';
    			len = 3;
    		}
    		else if (target.equals("（10）")) {
    			code = '⑽';
    			len = 4;
    		}
    		else if (target.equals("（11）")) {
    			code = '⑾';
    			len = 4;
    		}
    		else if (target.equals("（12）")) {
    			code = '⑿';
    			len = 4;
    		}
    		else if (target.equals("（13）")) {
    			code = '⒀';
    			len = 4;
    		}
    		else if (target.equals("（14）")) {
    			code = '⒁';
    			len = 4;
    		}
    		else if (target.equals("（15）")) {
    			code = '⒂';
    			len = 4;
    		}
    		else if (target.equals("（16）")) {
    			code = '⒃';
    			len = 4;
    		}
    		else if (target.equals("（17）")) {
    			code = '⒄';
    			len = 4;
    		}
    		else if (target.equals("（18）")) {
    			code = '⒅';
    			len = 4;
    		}
    		else if (target.equals("（19）")) {
    			code = '⒆';
    			len = 4;
    		}
    		else if (target.equals("（20）")) {
    			code = '⒇';
    			len = 4;
    		}
		}
		if (extdata != null) {
			result.code = COMMENT_STRCODE_EXT;
			result.extdata = extdata.toCharArray();
		}
		else if (code != 0) {
			result.code = COMMENT_STRCODE;
		}
		result.param1 = code;
		result.param2 = len;
		return result;
	}

	private int[] analyzeTag(String comment) {
		int result[] = { COMMENT_NONE, 0, 0 };
		int size = comment.length();
		if (size <= 5 || !comment.substring(0, 4).equals("<img")) {
			// コメントになってない
			return result;
		}
		if (size >= 8) {
			// 挿絵チェック <img src="xxx.jpg">
			int st = comment.indexOf("src=\"");
			if (st > 0) {
				int ed = comment.indexOf('\"', st + 5);
				if (ed > 0) {
					String filename = comment.substring(st + 5, ed);
					int fileindex = loadBitmap(filename, false);
					if (fileindex >= 0) {
						// ファイル名あり
						result[0] = COMMENT_PICTURE;
						result[1] = fileindex;
						return result;
					}
				}
			}
		}
		return result;
	}

	// コメントを飛ばす
	private int skipComment(char buff[], int offset, int count) {
		int i = offset;
		int size = buff.length;
		int cnt = 0;
		while (i < size && i - offset < count) {
			if (i < size - 1 && buff[i] == '［' && buff[i + 1] == '＃') {
				// 更にコメントがあった
				cnt++;
				i += 2;
			}
			else {
				if (i < size && buff[i] == '］') {
					cnt--;
				}

				// 文字の種別判定
				i++;

				// コメント終了チェック
				if (cnt <= 0) {
					// コメント終了
					break;
				}
			}
		}
		return i - offset;
	}

	// タグを飛ばす
	private int skipTag(char buff[], int offset, int count) {
		int i = offset;
		int size = buff.length;
		int cnt = 0;
		while (i < size && i - offset < count) {
			if (i < size && buff[i] == '>') {
				// コメント閉じる
				cnt--;
			}
			// 文字の種別判定
			i++;

			// コメント終了チェック
			if (cnt < 0) {
				// コメント終了
				break;
			}
		}
		return i - offset;
	}

	// 読込通知
	private void sendMessage(int msg, int progress) {
		Message message = new Message();
		message.what = msg;
		message.arg1 = progress;
		message.arg2 = 0;
		mHandler.sendMessage(message);
	}

	/**
	 * コメント以降の本文全体を解析する
	 * @param inbuff	テキストファイルの内容(in)
	 * @param inoff		テキスト解析開始位置(in)
	 * @param textbuff	表示用テキストデータの格納先(out)
	 * @param linedata	行ごとの表示情報(out)
	 */ 
	private void parseTextData(char inbuff[], int inoff, StringBuffer textbuff, ArrayList<LineData> linedata) {
		int inpos = inoff;
		int size = inbuff.length;

		MidashiManager midashi1 = new MidashiManager(); // 見出しタグ用
		MidashiManager midashi2 = new MidashiManager(); // 1行単独用
		StringBuffer midashiLabel = new StringBuffer();
		int midashiMode = 0;
		int midashiLine = 0; 
		int textStyleMode = 0;
		int textStyleLen = 0; 
		int textStyleLevel = 0; 

		int progress = 0;
		sendMessage(DEF.HMSG_TX_PARSE, 0);

		// テキストブロック管理
		TextBlockManager tb = new TextBlockManager(textbuff); 
		char last_char = 0;
		char last_char2 = 0;
		int textcolumn2 = 0;
		int textcolumn3 = 0;
		int prevchartype = CHARTYPE_INIT;
		
		while (mRunningFlag == true) {
			if (inpos < size - 1 && inbuff[inpos] == '［' && inbuff[inpos + 1] == '＃') {
				// 注釈は外字のみ処理する
				int skipcnt = skipComment(inbuff, inpos, inbuff.length - inpos);
				if (skipcnt > 0) {
					// 本文のときのみ
					String comment = new String(inbuff, inpos, skipcnt);
					CommentResult cr = analyzeComment(comment);
					switch (cr.code) {
						case COMMENT_STRCODE: {
							// 外字 or 記号置換
							tb.replaceText((char)cr.param1, cr.param2);
							break;
						}
						case COMMENT_STRCODE_EXT: {
							// 合成文字
							tb.replaceText((char)cr.param1, cr.param2);
							tb.addExtData(cr.extdata);
							break;
						}
						case COMMENT_PICTURE_CHAR: {
							// 文字画像を追加
							tb.checkRubiTarget(CHARTYPE_PCHAR);
							tb.appendText((char)cr.param1);
							break;
						}
						case COMMENT_JISAGE:
							// その行だけ字下げ
							tb.setCurrentIndent(cr.param1);
							break;
						case COMMENT_JITUKI:
							// その行だけ地付き
							tb.setCurrentIndent(-1);
							break;
						case COMMENT_JISAGE_RANGE:
							// 範囲指定の字下げ (0なら解除)
							tb.setBaseIndent(cr.param1);
							break;
						case COMMENT_PAGECONTROL:
							// 改ページ
							tb.addPageControl(cr.param1);
							break;
						case COMMENT_PICTURE: {
							int nextpos = inpos + skipcnt;
							if (nextpos >= size || inbuff[nextpos] == '\n') {
								// 画像の番号を設定
								tb.addPicture(cr.param1);
							}
							else {
								// 文字画像を追加
								tb.checkRubiTarget(CHARTYPE_PCHAR);
								tb.appendText((char)(cr.param1 + 0x1a00));
							}
							break;
						}
						case COMMENT_MIDASHI: {
							int linecnt = linedata.size();
							String label = comment.substring(3, comment.length() - 7);
							midashi1.addMidashiData(label.replaceAll("[　 \t]+", "　"), linecnt);
							break;
						}
						case COMMENT_MIDASHI_ST:{
							midashiMode ++;
							if (midashiMode == 1) {
								// 見出し開始行
    							midashiLine = linedata.size();
							}
							break;
						}
						case COMMENT_MIDASHI_ED:
							if (midashiMode == 1 && midashiLabel.length() > 0) {
    							String label = midashiLabel.toString().replaceAll("[　 \t]+", "　"); 
    							midashi1.addMidashiData(label, midashiLine);
    							midashiLabel.setLength(0);
							}
							// 見出し終了
							if (midashiMode > 0) {
								midashiMode --;
							}
							break;
						case COMMENT_TEXTSTYLE: {
							int textclm = tb.getTextColumn();
							if (textclm >= cr.param2) {
								// その長さの本文ががあるなら
								tb.setStyle(cr.param1, textclm - cr.param2, cr.param2);
							}
							break;
						}
						case COMMENT_TEXTSTYLE_ST: {
							if (textStyleLevel == 0) {
								// 指定が入れ子のときは最初のもののみを有効にする
								textStyleLen = 0;
								textStyleMode = cr.param1;
							}
							textStyleLevel ++;
							break;
						}
						case COMMENT_TEXTSTYLE_ED: {
							if (textStyleLevel == 1 && textStyleMode != 0) {
    							// 開始指定あり
								int textclm = tb.getTextColumn();
								if (textclm >= textStyleLen) {
    								// その長さの本文ががあるなら
    								tb.setStyle(textStyleMode, textclm - textStyleLen, textStyleLen);
    							}
								// 終了
								textStyleLen = 0;
								textStyleMode = 0;
							}
							textStyleLevel --;
							break;
						}
					}
					inpos += skipcnt;
				}
			}
			else if (inpos < size - 3 && inbuff[inpos] == '<' && inbuff[inpos + 1] == 'i' && inbuff[inpos + 2] == 'm' && inbuff[inpos + 3] == 'g') {
				// imgタグによる挿絵
				int skipcnt = skipTag(inbuff, inpos, inbuff.length - inpos);
				if (skipcnt > 0) {
					// 本文のときのみ
					String comment = new String(inbuff, inpos, skipcnt);
					int commentResult[] = analyzeTag(comment);
					int cmd = commentResult[0];
					int param = commentResult[1];
					if (cmd == COMMENT_PICTURE) {
						// 画像の番号を設定
						tb.addPicture(param);
					}
					inpos += skipcnt;
				}
			}
			else if (inpos < size - 1 && inbuff[inpos] == '／' && inbuff[inpos + 1] == '＼') {
				tb.appendText('〳');
				tb.appendText('〵');
				inpos += 2;
			}
			else if (inpos < size - 2 && inbuff[inpos] == '／' && inbuff[inpos + 1] == '″' && inbuff[inpos + 2] == '＼') {
				tb.appendText('〴');
				tb.appendText('〵');
				inpos += 3;
			}
			else {
				char code;
				if (inpos < size) {
					code = inbuff[inpos];
					if (0x2a00 <= code && code <= 0x2aff) {
						code = '□';
					}
				}
				else {
					code = '\n';
				}
				if (code == '《') {
					// 《 がきたらルビ開始
					tb.setRubiStart();
				}
				else if (code == '》') {
					// 》 がきたらルビ終了
					tb.addTextBlock();
				}
				else if (code == '｜') {
					tb.setRubiTarget();
				}
				else {
					// 文字種別を取得
					int chartype = getCharType(code);
					char[] extdata = null;

					if (tb.isRubi == false) {
						if (prevchartype != chartype) {
							if (mAscMode == ASC_TWINCHK && chartype == CHARTYPE_ASCII) {
								// 2文字だけが半角かをチェック
								char code1 = 0;
								char code2 = 0;
								int ctype1 = CHARTYPE_INIT;
								int ctype2 = CHARTYPE_INIT;
								if (inpos < size - 1) {
									// 次の文字がバッファ内
									code1 = inbuff[inpos + 1];
									ctype1 = getCharType(code1);
									if (ctype1 != chartype) {
										// 次の文字は同じ文字種ではない
										if (code1 != '《' && code1 != '［') {
											// 次の文字がルビ開始やコメントではない
											extdata = new char[] {code};
											code = CHARDRAW_YOKO;
										}
									}
								}
								if (inpos < size - 2 && extdata == null && ctype1 == chartype) {
									// さらに次の文字もバッファ内
									code2 = inbuff[inpos + 2];
									ctype2 = getCharType(code2);
									if (ctype2 != chartype) {
										// 2文字だけが同じ文字種
										if (code2 != '《' && code2 != '［') {
											// 次の文字がルビ開始やコメントではない
											extdata = new char[] {code, code1};
											code = CHARDRAW_YOKO;
											inpos ++;
										}
									}
								}
							}
							else if (mAscMode == ASC_NORMAL && (chartype == CHARTYPE_ASCII_MARK || chartype == CHARTYPE_ASCII_NUM)) {
								// 2文字だけが半角かをチェック
								char code1 = 0;
								char code2 = 0;
								int ctype1 = CHARTYPE_INIT;
								int ctype2 = CHARTYPE_INIT;
								if (inpos < size - 2) {
									// 次の文字がバッファ内
									code1 = inbuff[inpos + 1];
									ctype1 = getCharType(code1);
									code2 = inbuff[inpos + 2];
									ctype2 = getCharType(code2);
									if (ctype1 == chartype && ctype2 != chartype) {
										// 2文字だけが同じ文字種
										if (code2 != '《' && code2 != '［') {
											// 次の文字がルビ開始やコメントではない
											extdata = new char[] {code, code1};
											code = CHARDRAW_YOKO;
											inpos ++;
										}
									}
								}
							}
						}
					}
					prevchartype = chartype; 

					// ルビの範囲設定
					tb.checkRubiTarget(chartype);

					// TODO 縦中横対応はまた今度
//					if (chartype == CHARTYPE_ASCII) {
//						// 半角文字の場合はブロック切り替え
//						;
//					}
					
					// テキストをコピー
					if (code != '\n') {
						// 改行コード以外は設定
						tb.appendText(code);
						if (extdata != null) {
							// 縦中横
							tb.addExtData(extdata);
						}
						
						if (tb.isRubi == false) {
							last_char = code;
							if (midashiMode > 0) {
								// 見出し範囲
								if (extdata == null) {
									midashiLabel.append(code);
								}
								else {
									midashiLabel.append(extdata);
								}
							}
							if (textStyleMode != 0) {
								// 傍線・傍点の範囲指定
								textStyleLen ++;
							}
						}
					}
					else {
						// 傍線・傍点の範囲指定
						if (textStyleMode != 0) {
							// 開始指定あり
							int textclm = tb.getTextColumn();
							if (textclm >= textStyleLen) {
								// その長さの本文ががあるなら
								tb.setStyle(textStyleMode, textclm - textStyleLen, textStyleLen);
							}
							textStyleLen = 0;
						}
						
						// 改行 がきたらテキストを登録
						tb.addTextBlock();

						// 行データを保存
						linedata.add(tb.getLineData());

						int linecnt = linedata.size() - 1;
						int textclm = tb.getTextColumn();
						if (linecnt >= 2 && textclm == 0 && textcolumn2 > 0 && textcolumn2 < 40 && last_char2 != '。' && last_char2 != '）' && textcolumn3 == 0) {
							LineData ld = linedata.get(linecnt - 1);
							if (ld != null) {
								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < ld.textblock.length; i++) {
									TextBlock tbwk = ld.textblock[i];
									if (tbwk.tx_index >= 0) {
										sb.append(textbuff.subSequence(tbwk.tx_index, tbwk.tx_index + tbwk.tx_length));
									}
								}
								midashi2.addMidashiData(sb.toString().replaceAll("^[　 \t]+", "").replaceAll("[　 \t]+", "　"), linecnt - 1);
							}
						}
						textcolumn3 = textcolumn2;
						textcolumn2 = textclm;
						last_char2 = last_char;
						
						// 改行
						tb.setNewLine();
						prevchartype = CHARTYPE_INIT;

						// 進捗の表示
						int pg = 100;
						if (size > 0) {
							pg = inpos * 100 / size;
						}
						if (progress != pg) {
							progress = pg;
							sendMessage(DEF.HMSG_TX_PARSE, progress);
						}
					}
				}
				inpos++; // 次の文字へ
				if (inpos > size) {
					// ファイル末尾まで処理したら終了
					break;
				}
			}
		}
		if (midashi1.midashilist.size() > 0) {
			// 見出しタグがあればそちらを採用
			mMidashi = midashi1;
		}
		else {
			// 見出しタグがなければ単独行を見出しにする
			mMidashi = midashi2;
		}
		return;
	}

	// 文字が漢字かどうか返す(ルビの対象にすべきかどうか)
	private int getCharType(char code) {
		if (code < 0x20) {
			// 制御文字 or 空白
			return CHARTYPE_CTRL;
		}
		else if (code < 0x80) {
			if (mAscMode == ASC_NORMAL) {
				if (code >= '0' && code <= '9') {
					// 半角数字
					return CHARTYPE_ASCII_NUM;
				}
				else if (code == '!' || code == '?') {
					// 半角末尾記号
					return CHARTYPE_ASCII_MARK;
				}
				else {
					// 半角文字(半角を90度回転表示のときだけ)
					return CHARTYPE_ASCII;
				}
			}
			else if (mAscMode == ASC_TWINCHK) {
				if (isSymbolHalf(code)) {
					// 半角記号
					return CHARTYPE_SYMBOL;
				}
				else {
					// 半角文字(半角を90度回転表示のときだけ)
					return CHARTYPE_ASCII;
				}
			}
			else {
				// 半角文字(半角を90度回転表示のときだけ)
				return CHARTYPE_ASCII;
			}
		}
		else if ((0x2a00 <= code && code <= 0x2a1F) || (0x3041 <= code && code <= 0x3096) || (0x30A1 <= code && code <= 0x30FA) || code == 0x30FC || code == '、' || code == '。' || code == '～' || code == '／' || code == '＼') {
			// 仮名文字
			return CHARTYPE_KANA;
		}
		else if (0x3008 <= code && code <= 0x301B) {
			return CHARTYPE_MARK;
		}
		else if (isSymbol(code)) {
			return CHARTYPE_SYMBOL; // 記号
		}
		// ※ 々なんかもルビ対象
		return CHARTYPE_KANJI;
	}

	// 記号チェック
	private boolean isSymbol(char code) {
		switch (code) {
			case '　':
			case '（':
			case '〔':
			case '［':
			case '｛':
			case '〈':
			case '《':
			case '「':
			case '『':
			case '【':
			case '≪':
			case '）':
			case '〕':
			case '］':
			case '｝':
			case '〉':
			case '》':
			case '」':
			case '』':
			case '】':
			case '≫':
			case '―':
			case '…':
			case '・':
				return true;
		}
		// 半角を縦表示のときは半角も同じ扱い
		if (isSymbolHalf(code)) {
			return true;
		}
		if (0x2a20 <= code && code <= 0x2a2f) {
			// 合成の文字(!!!,?!!)
			return true;
		}
		return false;
	}

	// 記号チェック
	private boolean isSymbolHalf(char code) {
		switch (code) {
			case '<':
			case '(':
			case '[':
			case '{':
			case '｢':
			case ')':
			case ']':
			case '}':
			case '>':
			case '\'':
			case '\"':
			case '/':
			case '｣':
				return true;
		}
		return false;
	}

	// 末尾禁止
	private boolean isKinsokuTail(char code) {
		switch (code) {
			case '（':
			case '〔':
			case '［':
			case '｛':
			case '〈':
			case '《':
			case '「':
			case '『':
			case '【':
			case '≪':
			case '〳':
			case '〴':
			case '―':
			case '…':
			case 'ー':
			case '<':
			case '(':
			case '{':
				return true;
		}
		return false;
	}

	// 先頭禁止
	private boolean isKinsokuHead(char code) {
		switch (code) {
			case '）':
			case '〕':
			case '］':
			case '｝':
			case '〉':
			case '》':
			case '」':
			case '』':
			case '】':
			case '≫':
			case '、':
			case '。':
			case 'ぁ':
			case 'ぃ':
			case 'ぅ':
			case 'ぇ':
			case 'ぉ':
			case 'っ':
			case 'ゃ':
			case 'ゅ':
			case 'ょ':
			case 'ゎ':
			case 'ヵ':
			case 'ァ':
			case 'ィ':
			case 'ゥ':
			case 'ェ':
			case 'ォ':
			case 'ッ':
			case 'ャ':
			case 'ュ':
			case 'ョ':
			case 'ヶ':
			case 'ヮ':
			case '〵':
			case '！':
			case '？':
			case '‼':
			case '⁇':
			case '⁈':
			case '⁉':
			case '¡':
			case '¿':
			case '>':
			case ')':
			case '}':
				return true;
		}
		return false;
	}

	// テキストバッファを返す
	public char[] getTextBuffer() {
		return mTextBuff;
	}

	// ビットマップ配列を返す
	public PictureData[] getPictures() {
		return mPictures;
	}

	// 小説タイトルを返す
	public String getTitle() {
		return mTitle;
	}

	// 読み込み処理中断
	public void setBreakTrigger() {
		mRunningFlag = false;
		return;
	}

	// ページ数を返す
	public int length() {
		if (mTextPages != null) {
			return mTextPages.length;
		}
		return 0;
	}

	// ページ選択時に表示する文字列を作成
	public String createPageStr(int page, String filename) {
		//
		if (mTextPages == null || (page < 0 || mTextPages.length <= page)) {
			return "";
		}

		String strPath = filename;
		if (strPath.indexOf("smb://") == 0) {
			int idx = strPath.indexOf("@");
			if (idx >= 0) {
				strPath = "smb://" + strPath.substring(idx + 1);
			}
		}

		String pageStr;
		pageStr = (page + 1) + " / " + mTextPages.length + "\n" + strPath;
		return pageStr;
	}

	// テキストの場合はデータを取得
	public TextDrawData[] getTextData(int page) {
		if (page < 0 || mTextPages.length <= page) {
			return null;
		}
		// テキストデータを返す
		return mTextPages[page];
	}

	public TextDrawData[][] getTextData() {
		// 全てのテキストデータを返す
		return mTextPages;
	}

	// テキストの場合はデータを取得
	public void release() {
		mTextBuff = null;
		mTextPages = null;
	}

	// ビットマップの情報読み込み
	private int loadBitmap(String filename, boolean isCharacter) {
		if (filename == null || filename.length() <= 0) {
			return -1;
		}

		// BitmapFactory.Options option = new BitmapFactory.Options();
		// option.inJustDecodeBounds = true;

		Point pt = new Point(0, 0);
		String textpath;
		try {
			textpath = mImageMgr.getFilePath() + mTextPath;
			// ビットマップの読み込み
			if (mImageMgr.getFileType() == ImageManager.FILETYPE_DIR) {
				BitmapFactory.Options option = new BitmapFactory.Options();
				option.inJustDecodeBounds = true;
				TextInputStream tis = new TextInputStream();
				tis.fileAccessInit(textpath + filename, mUser, mPass);
				BitmapFactory.decodeStream(tis, null, option);
				tis.fileClose();
				pt.x = option.outWidth;
				pt.y = option.outHeight;
			}
			else {
				textpath = mTextPath;
				while (true) {
					if (filename.startsWith("../")) {
						// 親ディレクトリ指定
						filename = filename.substring(3);
						if (textpath.length() > 2) {
							int pridx = textpath.lastIndexOf('/', textpath.length() - 2);
							if (pridx >= 0) {
								textpath = textpath.substring(0, pridx + 1);
							}
						}
					}
					else {
						break;
					}
				}
				mImageMgr.getImageSize(textpath + filename, pt);
			}
		} catch (IOException e) {
			// 読み込みエラー
			Log.e("Text/BitmapLoad", e.getMessage());
			return -1;
		}

		PictureData picdata = new PictureData();
		picdata.mFileName = textpath + filename;

		int src_cx = pt.x;
		int src_cy = pt.y;
		int dst_cx;
		int dst_cy;

		if (isCharacter) {
			// 文字の置き換え
			picdata.mWidth = 0;
			picdata.mHeight = 0;
		}
		else {
			switch (mPicScale) {
				case BMPSCALE_SCREEN:
					dst_cx = mWidth;
					dst_cy = mTextHeight;
					break;
				case BMPSCALE_ZOOM2X:
					dst_cx = src_cx * 2;
					dst_cy = src_cy * 2;
					break;
				case BMPSCALE_ZOOM3X:
					dst_cx = src_cx * 3;
					dst_cy = src_cy * 3;
					break;
				case BMPSCALE_ZOOM4X:
					dst_cx = src_cx * 4;
					dst_cy = src_cy * 4;
					break;
				case BMPSCALE_ORIGINAL:
				default:
					dst_cx = src_cx;
					dst_cy = src_cy;
					break;
			}

			float scale_x;
			float scale_y;
			float scale;
			if (dst_cx > mTextWidth || dst_cy > mTextHeight) {
				scale_x = (float) mTextWidth / (float) src_cx;
				scale_y = (float) mTextHeight / (float) src_cy;
			}
			else {
				scale_x = (float) dst_cx / (float) src_cx;
				scale_y = (float) dst_cy / (float) src_cy;
			}

			if (scale_x < scale_y) {
				scale = scale_x;
			}
			else {
				scale = scale_y;
			}
			picdata.mWidth = (int) (src_cx * scale);
			picdata.mHeight = (int) (src_cy * scale);
		}

		mPicArray.add(picdata);
		return mPicArray.size() - 1;
	}

	private String checkShiftJIS(byte[] buff) {
		if (buff == null || buff.length < 3) {
			return "MS932";
		}

		int size = 2048;
		if (buff.length < 2048) {
			size = buff.length;
		}
		String strSJIS = null;
		String strUTF8 = null;
		String strEUCKR = null;
		try {
			strSJIS = new String(buff, 0, size, "MS932");
		} catch (UnsupportedEncodingException e) {
			// UTF-8
		}
		try {
			strUTF8 = new String(buff, 0, size, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Shift-JIS
		}
		try {
			strEUCKR = new String(buff, 0, size, "EUC-KR");
		} catch (UnsupportedEncodingException e) {
			// EUC-KR
		}
		if (strSJIS != null && strUTF8 == null && strEUCKR == null) {
			return "MS932";
		}
		else if (strSJIS == null && strUTF8 != null && strEUCKR == null) {
			return "UTF-8";
		}
		else if (strSJIS == null && strUTF8 == null && strEUCKR != null) {
			return "EUC-KR";
		}

		byte[] buffSJIS = null;
		byte[] buffUTF8 = null;
		byte[] buffEUCKR = null;
		try {
			buffSJIS = strSJIS.getBytes("MS932");
		} catch (UnsupportedEncodingException e) {
			// Shift-JIS
		}
		try {
			buffUTF8 = strUTF8.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8
		}
		try {
			buffEUCKR = strEUCKR.getBytes("EUC-KR");
		} catch (UnsupportedEncodingException e) {
			// EUCKR
		}
		if (buffSJIS != null && buffUTF8 == null && buffEUCKR == null) {
			return "MS932";
		}
		else if (buffSJIS == null && buffUTF8 != null && buffEUCKR == null) {
			return "UTF-8";
		}
		else if (buffSJIS == null && buffUTF8 == null && buffEUCKR != null) {
			return "EUC-KR";
		}

		int cntSJIS = 0;
		if (buffSJIS != null) {
			for (int i = 0; i < buffSJIS.length && i < size; i++) {
				if (buffSJIS[i] != buff[i]) {
					cntSJIS++;
				}
			}
		}
		else {
			cntSJIS = 99999;
		}
		int cntUTF8 = 0;
		if (buffUTF8 != null) {
			for (int i = 0; i < buffUTF8.length && i < size; i++) {
				if (buffUTF8[i] != buff[i]) {
					cntUTF8++;
				}
			}
		}
		else {
			cntUTF8 = 99999;
		}

		int cntEUCKR = 0;
		if (buffEUCKR != null) { 
			for (int i = 0; i < buffEUCKR.length && i < size; i++) {
				if (buffEUCKR[i] != buff[i]) {
					cntEUCKR++;
				}
			}
		}
		else {
			cntEUCKR = 99999;
		}

		if (cntEUCKR < cntSJIS && cntEUCKR < cntUTF8) {
			return "EUC-KR";
		}
		else if (cntUTF8 < cntSJIS) {
			return "UTF-8";
		}
		return "MS932";
	}

	public void searchClear() {
		mMarker = null;
	}

	public void searchText(String searchtext) {
		// 検索文字列を小文字にする
		searchtext = searchtext.toLowerCase(Locale.JAPANESE);
		
		ArrayList<MidashiData> mdlist = new ArrayList<MidashiData>();
		// マーカー表示
		if (mMarker == null) {
			mMarker = new SparseArray<ArrayList<MarkerDrawData>>();
		}
		else {
			mMarker.clear();
		}
		int searchlen = searchtext.length();
		int lastpage = -1;
		StringBuffer sb = new StringBuffer(searchtext.length() + 100);

		for (int i = mTextPages.length - 1; i >= 0; i--) {
			TextDrawData[] tdlist = mTextPages[i];
			for (int j = tdlist.length - 1; j >= 0; j--) {
				TextDrawData td = tdlist[j];
				if (td.mIsText == false) {
					// 画像又はスタイル又はルビ
					continue;
				}
				sb.insert(0, getTextString(mTextBuff, td.mTextPos, td.mTextLen, td.mExData).toLowerCase(Locale.JAPANESE));

				int pos = sb.length() - searchtext.length();
				for (int k = pos; k >= 0; k--) {
					if (sb.substring(k).equals(searchtext)) {
						// ヒット
						if (lastpage != i) {
							MidashiData md = new MidashiData();
							md.text = "";
							md.page = i;
							md.line = 0;
							mdlist.add(0, md);
							lastpage = i;
						}

						// ハイライト設定
						setHighlight(i, j, k, searchlen);
					}
					sb.setLength(k + searchlen - 1);
				}
			}
		}
		mSearchList = mdlist.toArray(new MidashiData[0]);
		return;
	}

	/**
	 * ハイライト設定
	 * @param page ハイライトの対象ページ
	 * @param block ハイライトの対象ブロック
	 * @param expos ハイライトの対象位置
	 * @param exlen ハイライトの対象長さ
	 */
	void setHighlight(int page, int block, int expos, int exlen) {
		MarkerDrawData md = null;
		int lastpage = -1;
		int pos = expos;
		int len = exlen;

		int[] result = new int[3]; 

		// 
		for (int i = page; i < mTextPages.length; i++) {
			// ページの構成要素
			TextDrawData[] tdlist = mTextPages[i];
			for (int j = block; j < tdlist.length; j++) {
				// テキストブロックごとに
				TextDrawData td = tdlist[j];
				if (td.mIsText == false) {
					// 画像又はスタイル又はルビ
					continue;
				}
	
				// hi_lenには表示上の文字数、retlenには実質の文字数を返す 
				int hi_pos;
				int hi_len;
				if (td.mIsAscii == false) {
    				getTextLengthPos(td, mTextBuff, pos, len, result);
				}
				else {
					// プロポーショナルフォントの文字列幅取得
					mTextPaint.setTextSize(td.mTextSize);
    				getTextLengthPosAscii(td, mTextBuff, mTextPaint, pos, len, result);
				}
				hi_pos = result[0];
				hi_len = result[1];
				len -= result[2];	// 処理済み分を減算
    				
				// MarkerDrawDataにマーカー情報を設定
				if (md == null || md.mX1 != td.mTextX || i != lastpage) {
					// 前回ループとマーカー追加
					ArrayList<MarkerDrawData> mklist = mMarker.get(i);
					if (mklist == null) {
						mklist = new ArrayList<MarkerDrawData>(1);
						mMarker.put(i, mklist);
					}
					md = new MarkerDrawData();
					mklist.add(0, md);
					md.mX1 = td.mTextX;
					md.mX2 = td.mTextX + td.mTextSize;
					if (td.mIsAscii == false) {
						// 全角の場合は単純に等倍
    					md.mY1 = td.mTextY + td.mGap * hi_pos;
    					md.mY2 = md.mY1 + td.mGap * hi_len;
					}
					else {
						// 半角の場合は実際の文字幅
    					md.mY1 = td.mTextY + hi_pos;
    					md.mY2 = md.mY1 + hi_len;
					}
    				lastpage = i;
				}
				else {
					// 縦方向に延ばす
					if (td.mIsAscii == false) {
						// 全角の場合は単純に等倍
    					md.mY2 = td.mTextY + td.mGap * hi_len;
					}
					else {
						// 半角の場合は実際の文字幅
    					md.mY2 = td.mTextY + hi_len;
					}
				}
				// 次のテキストブロックは先頭から
				pos = 0;
				if (len == 0) {
					return;
				}
			}
			// ページが変わったら最終ブロックから
			block = 0;
		}
	}

	public MidashiData[] getSearchList() {
		return mSearchList;
	}

	public SparseArray<ArrayList<MarkerDrawData>> getMarker() {
		return mMarker;
	}
	
	// public MidashiData[] searchText(String searchtext) {
	// ArrayList<MidashiData> mdlist = new ArrayList<MidashiData>();
	//
	// StringBuffer sb = new StringBuffer();
	// int len = searchtext.length();
	//
	// for (int i = 0; i < mTextPages.length; i ++) {
	// TextDrawData[] tdlist = mTextPages[i];
	// for (int j = 0; j < tdlist.length; j ++) {
	// TextDrawData td = tdlist[j];
	// if (td.mIsText) {
	// // 文字列化
	// sb.append(mTextBuff, td.mTextPos, td.mTextLen);
	// }
	// }
	// // 検索
	// int index = sb.indexOf(searchtext);
	// if (index >= 0) {
	// MidashiData md = new MidashiData();
	// int last = index + 20;
	// if (last > sb.length()) {
	// last = sb.length();
	// }
	// md.text = sb.substring(index, last);
	// md.page = i;
	// md.line = 0;
	// mdlist.add(md);
	// }
	// sb.setLength(0);
	// }
	// return mdlist.toArray(new MidashiData[0]);
	// }
}
