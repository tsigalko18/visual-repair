package edu.illinois.reassert;

/**
 * Exception used to record the arguments passed to an instrumented method.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class RecordedAssertFailure extends Error {
	private static final long serialVersionUID = 1L;
	
	private Object[] args;
	
	public RecordedAssertFailure(Throwable e, Object...args) {
		super(e);
		this.args = args;
	}

	/**
	 * @return the arguments passed to an instrumented method
	 */
	public Object[] getArgs() {
		return args;
	}
}
