package src.comitton.stream;

import java.io.IOException;
import java.io.InputStream;

import src.comitton.common.DEF;

import android.os.Handler;
import android.os.Message;
import static src.comitton.stream.ImageManager.MSG_LOADING;

public class RarInputStream extends InputStream {
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
	public static final int OFFSET_RAR_FTIME = 20; // 4bytes
	public static final int OFFSET_RAR_UNPVER = 24; // 1byte
	public static final int OFFSET_RAR_METHOD = 25; // 1byte
	public static final int OFFSET_RAR_FNSIZE = 26; // 2bytes
	public static final int OFFSET_RAR_ATTRIB = 28; // 4bytes
	public static final int OFFSET_RAR_FNAME = 32; // OFFSET_RAR_FNSIZE
//
	public static final int RAR_HTYPE_MARK = 0x72;
	public static final int RAR_HTYPE_MAIN = 0x73;
	public static final int RAR_HTYPE_FILE = 0x74;

	public static final byte RAR_METHOD_STORING = 0x30;

	Handler mHandler;
	InputStream	mInputStream;
	int mPage;

	public RarInputStream(InputStream is, String charset) throws IOException {
		// TODO: RAR5対応されていない
		mInputStream = is;

		byte buff[] = new byte[1024 * 8];
		int retsize;
		int readsize;
		int ret;
		int datasize, orglen;
		byte rarver;
		byte method;

		int header = OFFSET_RAR_HSIZE + 2;
		int hcrc;
		int htype;
		int hflags;
		int hsize;
		int asize;

		int maxcmplen = 0;
		int maxorglen = 0;

		int count = 0;

		while (true) {
			// ヘッダ読込
			retsize = is.read(buff, 0, header);

			hcrc = getShort(buff, OFFSET_RAR_HCRC);
			htype = buff[OFFSET_RAR_HTYPE];
			hflags = getShort(buff, OFFSET_RAR_HFLAGS);
			hsize = getShort(buff, OFFSET_RAR_HSIZE);
			asize = 0;

			if (hsize > header) {
				if (hsize <= buff.length) {
					retsize = is.read(buff, header, hsize - header);
					if (retsize < hsize - header) {
						// ヘッダがおかしい
						throw new IOException("This RAR File Format is not supported.");
					}
				}
				else {
					// ヘッダがおかしい
					throw new IOException("This RAR File is broken.");
				}
			}

			boolean skip = false;

			if (htype != RAR_HTYPE_FILE) {
				if (htype == RAR_HTYPE_MARK) {
					if (hcrc != 0x6152 || hflags != 0x1a21 || hsize != 0x0007) {
						// ヘッダがおかしい
						throw new IOException("This RAR File Format is not supported.");
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
				// ヘッダの残りを読込
				asize = (hflags & 0x8000) == 0 ? 0 : getInt(buff, OFFSET_RAR_ASIZE);
				if (asize > 0) {
					if (hsize + asize <= buff.length) {
						// 追加ヘッダが有りかつバッファに入りきる
						retsize = is.read(buff, hsize, asize);
						if (retsize < asize) {
							// ヘッダがおかしい
							throw new IOException("This RAR File Format is not supported.");
						}
					}
					else {
						// ヘッダがおかしい
						throw new IOException("This RAR File is broken.");
					}
				}
			}
			else {
				// ファイル情報取得
				datasize = getInt(buff, OFFSET_RAR_PKSIZE);
				orglen = getInt(buff, OFFSET_RAR_UNSIZE);
				rarver = buff[OFFSET_RAR_UNPVER];
				method = buff[OFFSET_RAR_METHOD];

				// ファイル名取得
				int lenFName = getShort(buff, OFFSET_RAR_FNSIZE);
				int posFName = OFFSET_RAR_FNAME;
				String name = "";

				if (hsize + asize >= lenFName + posFName) {
					// ファイル名までのデータがあり
					try {
						for (int i = 0; i < lenFName; i++) {
							if (buff[posFName + i] == 0) {
								lenFName = i;
								break;
							}
						}
						name = new String(buff, posFName, lenFName, charset);
					}
					catch (Exception e) {
						name = "";
					}
				}
				int len = name.length();
				skip = true;
				if (len >= 5) {
					String ext = DEF.getFileExt(name);
					if (DEF.WITH_JPEG && (ext.equals(".jpg") || ext.equals(".jpeg"))) {
						skip = false;
					}
					else if (DEF.WITH_PNG && ext.equals(".png")) {
						skip = false;
					}
					else if (DEF.WITH_GIF && ext.equals(".gif")) {
						skip = false;
					}
					else if (DEF.WITH_WEBP && ext.equals(".webp")) {
						skip = false;
					}
					else if (DEF.WITH_AVIF && ext.equals(".avif")) {
						skip = false;
					}
					else if (DEF.WITH_HEIF && (ext.equals(".heif") || ext.equals(".heic"))) {
						skip = false;
					}
					else if (DEF.WITH_JXL && ext.equals(".jxl")) {
						skip = false;
					}
				}

				if (skip) {
					while (datasize > 0) {
						readsize = buff.length;
						if (readsize > datasize) {
							readsize = datasize;
						}
						retsize = is.read(buff, 0, readsize);
						if (retsize <= 0) {
							break;
						}
						datasize -= retsize;
					}
					count ++;
					if (count > 10) {
						break;
					}
					continue;
				}


				if (maxcmplen < datasize || maxorglen < orglen) {
					maxcmplen = datasize;
					maxorglen = orglen;
					ret = CallJniLibrary.rarAlloc(maxcmplen, maxorglen);
					if (ret != 0) {
						throw new IOException("RAR malloc error.");
					}
				}
				ret = CallJniLibrary.rarInit(datasize, orglen, rarver, method == RAR_METHOD_STORING);
				if (ret != 0) {
					throw new IOException("Illegal function call.");
				}

				while (datasize > 0) {
					readsize = buff.length;
					if (readsize > datasize) {
						readsize = datasize;
					}
					retsize = is.read(buff, 0, readsize);
					if (retsize <= 0) {
						break;
					}
					datasize -= retsize;
					CallJniLibrary.rarWrite(buff, 0, retsize);
				}
				// 変換
				CallJniLibrary.rarDecomp();
				break;
			}
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

	public RarInputStream(InputStream is, int page, FileListItem fi) throws IOException {
		this(is, page, fi, null);
	}

	public RarInputStream(InputStream is, int page, FileListItem fi, Handler handler) throws IOException {
		mInputStream = is;
		mHandler = handler;
		mPage = page;
		int cmplen = fi.cmplen;
		int orglen = fi.orglen;
		int header = fi.header;
		byte rarver = fi.version;
		boolean nocomp = fi.nocomp;

		byte buff[] = new byte[1024 * 100];
		int	retsize;
		int readsize;
		int datasize = cmplen - header;
		int ret;

		ret = CallJniLibrary.rarInit(cmplen - header, orglen, rarver, nocomp);
		if (ret != 0) {
			throw new IOException("Illegal function call.");
		}

		retsize = is.read(buff, 0, header);
		int remain = datasize;

		int msgCount = 0;
		long startTime = System.currentTimeMillis();
		while (remain > 0) {
			readsize = buff.length;
			if (readsize > remain) {
				readsize = remain;
			}
			retsize = is.read(buff, 0, readsize);
			if (retsize <= 0) {
				break;
			}
			remain -= retsize;
			CallJniLibrary.rarWrite(buff, 0, retsize);
			if(mHandler != null) {
				long nowTime = System.currentTimeMillis();
				if (nowTime - startTime > (msgCount + 1) * 200) {
					msgCount++;
					int prog = (int) ((long) (datasize - remain) * 100 / datasize);
					int rate = (int) ((long) (datasize - remain) * 10 / (nowTime - startTime));
					Message message = new Message();
					message.what = MSG_LOADING;
					message.arg1 = prog << 24;
					message.arg2 = rate;
					message.obj = null;
					mHandler.sendMessage(message);
				}
			}
		}
		// 変換
		CallJniLibrary.rarDecomp();
	}

	// 読み込み済みのページを返す
	public int getLoadPage() {
		return mPage;
	}

	// 現在の展開ファイルをもう一度最初から返す
	public void initSeek() {
		CallJniLibrary.rarInitSeek();
	}

	@Override
	public int read() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	@Override
	public int read(byte buf[], int off, int len) throws IOException {
		int ret = CallJniLibrary.rarRead(buf, off, len);
		if (ret == 0) {
			ret = -1;
		}
		return ret;
	}

	public void close() {
		CallJniLibrary.rarClose();
	}
}
