package edu.illinois.reassert;

import java.lang.reflect.Method;
import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractFilter;
import edu.illinois.reassert.reflect.Factory;

/**
 * Base class for objects that can repair a single failed assertion invocation 
 * when given the exception that the invocation produced.
 * <br /><br />
 * Implementers are expected to have a constructor that takes either zero 
 * parameters or a single {@link Factory}.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public abstract class AssertFixer extends FixStrategyBase {

	public AssertFixer(Factory factory) {
		super(factory);
	}

	/**
	 * Fixes (or replaces) the given assertion such that the calling test passes
	 * @param testMethod the test method that was executed
	 * @param assertion the invocation of the Assert method to fix 
	 * (Note that it may not be contained in the test method)
	 * @param failureException the exception thrown by the assert method 
	 * (usually contains the value that caused the failure) 
	 * @throws UnfixableException if the assertion cannot be fixed for some reason
	 */
	public abstract FixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion, 
			Throwable failureException) throws UnfixableException;
	
	@Override
	public FixResult fix(
			Method testMethod, 
			Throwable failureException)
			throws UnfixableException {
		if (!(failureException instanceof RecordedAssertFailure)) {
			return null;
		}
		RecordedAssertFailure record = (RecordedAssertFailure) failureException;
		StackTraceElement instrumented = record.getStackTrace()[0];		
		StackTraceElement location = record.getStackTrace()[1]; // one above instrumented
		
		CtTypeReference<?> assertClassRef = 
			getFactory().Class().createReference(instrumented.getClassName());
		
		CompilationUnit cu = findFailingCompilationUnit(location);
		if (cu == null) {
			throw new UnfixableException("Cannot find failing compilation unit");
		}
		
		CtInvocation<?> assertion = findFailingInvocation(
				cu, 
				assertClassRef,
				instrumented.getMethodName(),
				location.getLineNumber());
		if (assertion == null) {
			return null;
		}
		return fix(testMethod, assertion, record);
	}
	
	private CtInvocation<?> findFailingInvocation(
			CompilationUnit cu,
			final CtTypeReference<?> assertClassRef, 
			final String expectedAssertMethodName, 
			final int lineNumber) {
		for (CtSimpleType<?> type : cu.getDeclaredTypes()) {
			List<CtInvocation<?>> elements = 
				Query.getElements(type, new AbstractFilter<CtInvocation<?>>(CtInvocation.class) {
					@Override
					public boolean matches(CtInvocation<?> element) {
						CtTypeReference<?> actualAssertClassRef = 
							element.getExecutable().getDeclaringType();
						String actualAssertMethodName = 
							element.getExecutable().getSimpleName();
						return elementContainsLine(element, lineNumber)
							&& actualAssertClassRef.isAssignableFrom(assertClassRef)
							&& actualAssertMethodName.equals(expectedAssertMethodName);							
					}
				});
			if (elements.size() > 0) {
				// return "innermost" invocation
				return elements.get(elements.size() - 1);
			}
		}
		return null;
	}
	
	/**
	 * Utility method that determines if the given invocation has a 
	 * message argument
	 */
	protected boolean hasMessage(CtInvocation<?> assertion) {		
		List<CtExpression<?>> args = assertion.getArguments();
		CtTypeReference<String> stringType = 
			assertion.getFactory().Type().createReference(String.class);
		return args.size() >= 3
			&& args.get(0).getType().isAssignableFrom(stringType);
	}
	
}