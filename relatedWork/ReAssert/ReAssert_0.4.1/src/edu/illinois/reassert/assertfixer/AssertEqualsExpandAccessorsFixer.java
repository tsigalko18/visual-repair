package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourceCodeFragment;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.Factory;

/**
 * Fix strategy that repairs <code>assertEquals</code> and <code>assertArrayEquals</code>. 
 * For literals, simply replaces the expected side.
 * For reference types, builds a tree of assertions that test the values
 * returned from accessor methods.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class AssertEqualsExpandAccessorsFixer extends AssertEqualsFixer {

	public static final int DEFAULT_MAX_TREE_DEPTH = 4; // TODO: make a command-line argument
	
	public AssertEqualsExpandAccessorsFixer(Factory factory) {
		super(factory);
	}
	
	@Override
	public CodeFixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion, 
			Throwable failureException, 
			Object expectedValue,
			Object actualValue) throws UnfixableException {
		CtStatement statement = buildStatement(testMethod, assertion, 
				expectedValue, actualValue, DEFAULT_MAX_TREE_DEPTH);
		SourceCodeFragment frag = getFactory().Fragment().replace(assertion, statement);
		return new CodeFixResult(statement, frag);
	}

	protected CtStatement buildStatement(
			Method testMethod,
			CtInvocation<?> assertion, 
			Object expectedValue, 
			Object actualValue, 
			int treeDepth) {
		CtExpression<?> expectedArg = getExpectedArg(assertion);
		CtExpression<?> actualArg = getActualArg(assertion);
		CtExpression<?> toleranceArg = getToleranceArg(assertion);
		
		if (actualArg instanceof CtLiteral && !(expectedArg instanceof CtLiteral)) {
			// expected should almost always be the literal value
			CtExpression<?> tmp = actualArg;
			actualArg = expectedArg;
			expectedArg = tmp;
		}
		
		AccessorTree accessorTree = AccessorTree.build(
				expectedValue, actualValue, treeDepth);
		CtStatement statement = accessorTree.buildAssertionTree(
				getFactory(), 
				getAssertFactory(testMethod), 
				expectedArg, 
				actualArg);		
		
		if (toleranceArg != null && statement instanceof CtInvocation) {
			// append tolerance to root-level assertion if necessary
			((CtInvocation<?>) statement).getArguments().add(toleranceArg);
		}
		return statement;
	}

}
