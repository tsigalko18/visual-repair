package edu.illinois.reassert;

import java.lang.reflect.Method;
import java.util.Collection;

import edu.illinois.reassert.reflect.Factory;

/**
 * Interface for objects that can repair a single failed test 
 * when given the exception that the invocation produced.
 * <br /><br />
 * Implementers are expected to have a constructor that takes either zero 
 * parameters or a single {@link Factory}.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public interface FixStrategy {

	/**
	 * Attempt to fix the given method such that it passes.
	 * @param testMethod the test method that was executed
	 * @param failureException the exception thrown by the assert method 
	 * (usually contains the value that caused the failure) 
	 * @throws UnfixableException if the assertion cannot be fixed for some reason
	 * @return a {@link FixResult} or null if no fix was applied
	 */
	public FixResult fix(
			Method testMethod, 
			Throwable failureException) throws UnfixableException;
	
	/**
	 * @return The collection of method signatures to instrument or <code>null</code>.  
	 * Instrumented methods throw a {@link RecordedAssertFailure} if the body of 
	 * the method throws an exception.  
	 * Expected string format is <code>some.package.SomeClass#someMethod</code> 
	 */
	public Collection<String> getMethodsToInstrument();
	
}
