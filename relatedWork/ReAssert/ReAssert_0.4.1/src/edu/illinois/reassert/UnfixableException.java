package edu.illinois.reassert;

/**
 * Exception thrown when a test cannot be fixed by any strategy.  
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class UnfixableException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnfixableException() {
		super();
	}

	public UnfixableException(String string) {
		super(string);
	}

	public UnfixableException(String string, Throwable cause) {
		super(string, cause);
	}

}
