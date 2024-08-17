package src.comitton.exception;

public class FileAccessException extends Exception {
	private static final long serialVersionUID = 1L;

	public FileAccessException(String str) {
		super(str);
	}

	public FileAccessException(Exception e) {
		super(e);
	}
}
