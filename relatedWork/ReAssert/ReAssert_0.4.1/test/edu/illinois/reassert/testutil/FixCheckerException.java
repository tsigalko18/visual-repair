package edu.illinois.reassert.testutil;

public class FixCheckerException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FixCheckerException(String string) {
		super(string);
	}

	public FixCheckerException(Throwable cause) {
		super(cause);
	}

	public FixCheckerException(String string, Throwable cause) {
		super(string, cause);
	}

}
