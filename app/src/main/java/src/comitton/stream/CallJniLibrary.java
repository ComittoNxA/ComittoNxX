package src.comitton.stream;

public class CallJniLibrary {
	static {
		// UNRARライブラリ
		System.loadLibrary ("comitton");
	}

    public static native int rarAlloc(int cmplen, int orglen);
    public static native int rarInit(int cmplen, int orglen, int rarver, boolean nocomp);
    public static native int rarWrite(byte data[], int off, int len);
    public static native int rarDecomp();
    public static native int rarRead(byte data[], int off, int len);
    public static native int rarInitSeek();
    public static native void rarClose();
    
};

