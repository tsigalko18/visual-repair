package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import edu.illinois.reassert.AssertFixer;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Abstract base class for fix strategies that handle failing assertTrue and assertFalse 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public abstract class AssertBooleanFixer extends AssertFixer {

	public AssertBooleanFixer(Factory factory) {
		super(factory);
	}
	
	@Override
	public CodeFixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion, 
			Throwable failureException)
			throws UnfixableException {
		AssertFactory assertFactory = getAssertFactory(testMethod);
		if (assertFactory.isAssertTrue(assertion) 
				|| assertFactory.isAssertFalse(assertion)) {
			int truthArgIndex = 0;
			if (assertion.getArguments().size() == 2) {
				truthArgIndex = 1;
			}
			CtExpression<?> truthArg = assertion.getArguments().get(truthArgIndex);
			return fix(testMethod, assertion, truthArg, failureException);
		}
		return null;
	}

	protected abstract CodeFixResult fix(
			Method testMethod,
			CtInvocation<?> assertion, 
			CtExpression<?> truthArg,
			Throwable failureException) throws UnfixableException;

}
