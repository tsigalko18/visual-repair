package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.cu.SourceCodeFragment;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Fix strategy that changes a failing assertTrue to assertFalse or assertFalse to assertTrue.
 * Also handles assertTrue(x.equals(y)) and assertFalse(x.equals(y)) by changing to assertEquals.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class InvertBooleanAssertFixer extends AssertBooleanFixer {

	public InvertBooleanAssertFixer(Factory factory) {
		super(factory);
	}

	@Override
	protected CodeFixResult fix(
			Method testMethod,
			CtInvocation<?> assertion, 
			CtExpression<?> truthArg,
			Throwable failureException)
				throws UnfixableException {
		AssertFactory assertFactory = getAssertFactory(testMethod);
		CtInvocation<?> newAssert;
		
		if (truthArg instanceof CtBinaryOperator
				&& isIdentityOperation((CtBinaryOperator<?>) truthArg)) {
			CtBinaryOperator<?> binaryArg = (CtBinaryOperator<?>) truthArg;
			CtExpression<?> expected = binaryArg.getLeftHandOperand();
			CtExpression<?> actual = binaryArg.getRightHandOperand();
			newAssert = assertFactory.createAssertEquals(expected, actual);
		}
		else if (truthArg instanceof CtInvocation
				&& isEqualsInvocation((CtInvocation<?>) truthArg)) {
			CtInvocation<?> equalsCall = (CtInvocation<?>) truthArg;
			CtExpression<?> expected = equalsCall.getTarget();
			CtExpression<?> actual = equalsCall.getArguments().get(0);
			newAssert = assertFactory.createAssertEquals(expected, actual);
		}
		else if (assertFactory.isAssertTrue(assertion)) {
			newAssert = assertFactory.createAssertFalse(truthArg);
		}
		else if (assertFactory.isAssertFalse(assertion)) {
			newAssert = assertFactory.createAssertTrue(truthArg);
		}
		else { 
			return null;
		}
		SourceCodeFragment frag = getFactory().Fragment().replace(assertion, newAssert);
		return new CodeFixResult(newAssert, frag);
	}

	private boolean isIdentityOperation(CtBinaryOperator<?> binaryArg) {
		return 
			binaryArg.getKind() == BinaryOperatorKind.EQ
			|| binaryArg.getKind() == BinaryOperatorKind.NE;
	}

	private boolean isEqualsInvocation(CtInvocation<?> truthArg) {
		return 
			"equals".equals(truthArg.getExecutable().getSimpleName())
			&& truthArg.getArguments().size() == 1;
	}
}
