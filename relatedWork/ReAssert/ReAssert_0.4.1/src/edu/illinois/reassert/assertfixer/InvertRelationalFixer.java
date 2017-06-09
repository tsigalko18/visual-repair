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
 * Fix strategy that fixes assertTrue or assertFalse by inverting the top-level 
 * relational operator in the argument.  
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class InvertRelationalFixer extends AssertBooleanFixer {

	public InvertRelationalFixer(Factory factory) {
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
		assert assertFactory.isAssertTrue(assertion) 
				|| assertFactory.isAssertFalse(assertion);
		
		if (truthArg instanceof CtBinaryOperator
				&& isRelational((CtBinaryOperator<?>) truthArg)) {
			invertRelationalOperator((CtBinaryOperator<?>)truthArg);
			SourceCodeFragment frag = getFactory().Fragment().modify(assertion);
			return new CodeFixResult(truthArg, frag);
		}
		// TODO: add cases for java.lang.Comparable and java.util.Comparator.
		// Not needed as of 2009-04-26 for any of the evaluation projects.
		return null;
	}

	private void invertRelationalOperator(CtBinaryOperator<?> truthArg) {
		BinaryOperatorKind inverted = getInvertedOperator(truthArg.getKind());
		truthArg.setKind(inverted);
	}

	private BinaryOperatorKind getInvertedOperator(BinaryOperatorKind kind) {
		assert isRelational(kind);
		
		if (kind == BinaryOperatorKind.LT) {
			return BinaryOperatorKind.GE;
		}
		if (kind == BinaryOperatorKind.LE) {
			return BinaryOperatorKind.GT;
		}
		if (kind == BinaryOperatorKind.GT) {
			return BinaryOperatorKind.LE;
		}
		if (kind == BinaryOperatorKind.GE) {
			return BinaryOperatorKind.LT;
		}
		throw new IllegalArgumentException();
	}

	private boolean isRelational(CtBinaryOperator<?> truthArg) {
		return isRelational(truthArg.getKind());
	}

	private boolean isRelational(BinaryOperatorKind op) {
		return op == BinaryOperatorKind.LT
		|| op == BinaryOperatorKind.LE
		|| op == BinaryOperatorKind.GT
		|| op == BinaryOperatorKind.GE;
	}

}
