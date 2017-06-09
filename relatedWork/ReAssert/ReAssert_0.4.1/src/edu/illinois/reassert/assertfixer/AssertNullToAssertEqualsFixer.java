package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.cu.SourceCodeFragment;
import edu.illinois.reassert.AssertFixer;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.FixResult;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Intermediate strategy that simply changes assertNull(x) to assertEquals(null, x)
 * so other strategies can fix it appropriately.  
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class AssertNullToAssertEqualsFixer extends AssertFixer {

	public AssertNullToAssertEqualsFixer(Factory factory) {
		super(factory);
	}

	@Override
	public FixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion,
			Throwable failureException) throws UnfixableException {
		AssertFactory assertFactory = getAssertFactory(testMethod);
		if (assertFactory.isAssertNull(assertion)) {
			// create assertEquals(null, actual) and fix on next pass
			CtInvocation<?> newAssert = assertFactory.createAssertEquals(
					getFactory().Code().createLiteral(null),
					assertion.getArguments().get(0));
			SourceCodeFragment frag = getFactory().Fragment().replace(assertion, newAssert);
			return new CodeFixResult(newAssert, frag);			
		}
		return null;
	}

}
