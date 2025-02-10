package src.comitton.fileview.data;

public class FileListItem {
	public String name;
	public String uri;
	public short type;
	public short exttype;
	public long cmppos;
	public long orgpos;
	public int cmplen;
	public int orglen;
	public int header;
	public long dtime;
	public int scale;		// 取り込み時の縮小倍率
	public int o_width;		// 実画像幅
	public int o_height;	// 実画像高さ
	public int width;		// 画像幅
	public int height;		// 画像高さ
	public int fwidth[] = new int[3];		// スケール幅
	public int fheight[] = new int[3];		// スケール高さ
	public int swidth[] = new int[3];		// スケール幅
	public int sheight[] = new int[3];		// スケール高さ
	public boolean error;
	public byte version;	// バージョン(rarのみ使用)
	public boolean nocomp;	// 無圧縮フラグ(rarのみ使用)
	public int params[];	// (PDF-CCITT)
	public boolean sizefixed; // ZIPのcmplenを修正済みかどうか
};
