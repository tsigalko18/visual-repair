package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;
import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import edu.illinois.reassert.AssertFixer;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.RecordedAssertFailure;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Base class for fixers that can repair assertEquals or assertArrayEquals invocations
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public abstract class AssertEqualsFixer extends AssertFixer {
	
	public AssertEqualsFixer(Factory factory) {
		super(factory);
	}

	@Override
	public CodeFixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion, 
			Throwable failureException) throws UnfixableException {
		AssertFactory assertFactory = getAssertFactory(testMethod);
		if (!assertFactory.isAssertEquals(assertion) 
				&& !assertFactory.isAssertArrayEquals(assertion)) {
			return null;
		}
		
		Object expectedObject = null;
		Object actualObject = null;
		if (failureException instanceof RecordedAssertFailure) {
			RecordedAssertFailure record = (RecordedAssertFailure) failureException;
			int argIndex = 0;
			if (hasMessage(assertion)) {
				argIndex = 1;
			}
			expectedObject = record.getArgs()[argIndex];
			actualObject = record.getArgs()[argIndex + 1];
		}
		else {
			throw new UnfixableException("Unknown failure exception", failureException);
		}
		
		return fix(testMethod, assertion, failureException, expectedObject,	actualObject);
	}
	
	/**
	 * Fixes (or replaces) the given assertEquals call such that the calling test passes.
	 * Called from #{@link #fix(Method, CtInvocation, Throwable)}
	 * 
	 * @param assertion the invocation of assertEquals or assertArrayEquals to fix
	 * @param expectedObject the object passed to the expected side of assertEquals
	 * @param actualObject the object passed to the actual side of assertEquals 
	 */
	protected abstract CodeFixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion,
			Throwable failureException,
			Object expectedObject,
			Object actualObject) throws UnfixableException;

	/**
	 * @return the expression on the expected side of the given assertion
	 */
	protected CtExpression<?> getExpectedArg(CtInvocation<?> assertion) {
		List<CtExpression<?>> args = assertion.getArguments();
		if (hasMessage(assertion)) {
			return args.get(1);
		}
		return args.get(0);
	}
	
	/**
	 * @return the expression on the actual side of the given assertion
	 */
	protected CtExpression<?> getActualArg(CtInvocation<?> assertion) {
		List<CtExpression<?>> args = assertion.getArguments();
		if (hasMessage(assertion)) {
			return args.get(2);
		}
		return args.get(1);
	}

	/**
	 * @return the expression for the tolerance argument or null
	 */
	protected CtExpression<?> getToleranceArg(CtInvocation<?> assertion) {
		List<CtExpression<?>> args = assertion.getArguments();
		boolean hasMessage = hasMessage(assertion);
		if (hasMessage && args.size() == 4) {
			return args.get(3);
		}
		if (!hasMessage && args.size() == 3) {
			return args.get(2);
		}
		return null;
	}

	
}