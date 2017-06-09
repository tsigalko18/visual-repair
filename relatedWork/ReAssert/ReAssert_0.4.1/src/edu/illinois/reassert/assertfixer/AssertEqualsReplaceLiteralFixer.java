package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourceCodeFragment;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.Factory;

/**
 * Special case of {@link AssertEqualsExpandAccessorsFixer} that simply replaces the
 * expected side of the assertion with a literal value.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class AssertEqualsReplaceLiteralFixer extends AssertEqualsExpandAccessorsFixer {
	
	public AssertEqualsReplaceLiteralFixer(Factory factory) {
		super(factory);
	}

	@Override
	public CodeFixResult fix(
			Method testMethod,
			CtInvocation<?> assertion, 
			Throwable failureException,
			Object expectedValue,
			Object actualValue) throws UnfixableException {
		CtStatement statement = buildStatement(testMethod, assertion, expectedValue, actualValue, 0);
		if (statement == null || statement instanceof CtBlock) {
			return null;
		}
		SourceCodeFragment frag = getFactory().Fragment().replace(assertion, statement);
		return new CodeFixResult(statement, frag);
	}

}
