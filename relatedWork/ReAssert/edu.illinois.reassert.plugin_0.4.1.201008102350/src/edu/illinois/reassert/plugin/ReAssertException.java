package edu.illinois.reassert.plugin;


public class ReAssertException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ReAssertException(String string) {
		super(string);
	}

	public ReAssertException(String string, Throwable cause) {
		super(string, cause);
	}

	public ReAssertException(Throwable cause) {
		super(cause);
	}


}
