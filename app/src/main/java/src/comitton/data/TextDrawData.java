package src.comitton.data;

public class TextDrawData {
	public boolean mIsText;
	public boolean mIsAscii;
	public float mTextSize;
	public float mTextX;
	public float mTextY;
	public int mTextPos;
	public int mTextLen;
//	public boolean mIsBody;
	public float mGap;
	public float mHeight;
	public char mExData[][];
	
	public TextDrawData (boolean istext, boolean isascii, float size, float x, float y, int pos, int len, float gap, float height, char[][] ext) {
		mIsText = istext;
		mIsAscii = isascii;
		mTextSize = size;
		mTextX = x;
		mTextY = y;
		mTextPos = pos;
		mTextLen = len;
//		mIsBody = body;
		mGap = gap;
		mHeight = height;
		mExData = ext;
	}
}
