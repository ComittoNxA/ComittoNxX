package src.comitton.common;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class Aes {
	private static final String AESKEY = "invisible_passwd";

	// 複合化
	public static String decode(String text) {
		String result = null;
		result = cryptAes128(Cipher.DECRYPT_MODE, text);
		return result;
	}

	// 複合化
	public static String encode(String text) {
		String result = null;
		result = cryptAes128(Cipher.ENCRYPT_MODE, text);
		return result;
	}

	// 暗号化/複合化実行部
	public static String cryptAes128(int mode, String text) {
		byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0, 0};
		String result = null;

		SecretKeySpec key = new SecretKeySpec(AESKEY.getBytes(), "AES");
		AlgorithmParameterSpec param = new IvParameterSpec(iv);

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(mode, key, param);

			// 暗号化or複合化
			result = toHexString(cipher.doFinal(toByteArray(text)));
		} catch (Exception ex) {
			String msg = "";
			if (ex != null && ex.getMessage() != null) {
				msg = ex.getMessage();
			}
			Log.e("", msg);
		}
		return result;
	}

	//16進数の文字列をバイト配列に変換
	public static byte[] toByteArray(String hex) {
		byte[] bytes = new byte[hex.length() / 2];

		for (int index = 0; index < bytes.length; index++) {
			bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2), 16);
		}
		return bytes;
	}

	//バイト配列を16進数の文字列に変換
	public static String toHexString(byte bytes[]) {
		StringBuffer strbuf = new StringBuffer(bytes.length * 2);
		for (int index = 0; index < bytes.length; index++) {
			int bt = bytes[index] & 0xff;
			if (bt < 0x10) {
				strbuf.append("0");
			}
			strbuf.append(Integer.toHexString(bt));
		}
		return strbuf.toString();
	}
}
