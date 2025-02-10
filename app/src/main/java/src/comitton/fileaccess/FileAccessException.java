package src.comitton.fileaccess;

import java.io.IOException;

public class FileAccessException extends IOException {
	private static final long serialVersionUID = 1L;

	public FileAccessException(String str) {
		super(str);
	}

	public FileAccessException(Exception e) {
		super(e);
	}
}
