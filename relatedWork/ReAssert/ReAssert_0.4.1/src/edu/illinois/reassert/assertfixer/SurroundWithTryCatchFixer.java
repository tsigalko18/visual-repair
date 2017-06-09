package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;
import java.util.List;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractFilter;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.FixResult;
import edu.illinois.reassert.FixStrategyBase;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Fix strategy in which a statement that throws an exception in enclosed
 * in a try/catch block.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class SurroundWithTryCatchFixer extends FixStrategyBase {

	public static final int MAX_TREE_DEPTH = 4; // TODO: make a command-line argument
	
	public SurroundWithTryCatchFixer(Factory factory) {
		super(factory);
	}

	@Override
	public FixResult fix(
			Method testMethod, 
			Throwable failureException) throws UnfixableException {
		AssertFactory assertFactory = getAssertFactory(testMethod);
		if (failureException instanceof Error) {
			return null;
		}
		StackTraceElement failingPosition = 
			findFailingPositionInMethod(testMethod, failureException.getStackTrace());
		if (failingPosition == null) {
			return null;
		}
		
		CtStatement failingStatement = findFailingStatement(testMethod, failingPosition);		
		if (failingStatement == null) {
			return null;
		}
		if (failingStatement instanceof CtVariable) {
			// TODO: Fail for now since Spoon calculates source position of
			// variables incorrectly.  It does not include the type, modifiers
			// (i.e. final), or default expression in the snippet size.  
			throw new UnfixableException("Cannot wrap a variable declaration in try-catch block");
			
//			TODO: INCOMPLETE!
//			CtVariable variable = (CtVariable<?>) failingStatement;
//			
//			CtStatement newVariable = getFactory().Code().createLocalVariable(
//					variable.getType(), 
//					variable.getSimpleName(),
//					createDefaultValue(variable.getType()));
//			replaceStatement(failingStatement, newVariable);
//			return new CodeFixResult(newVariable);						
//			
//			CtExpression oldDefault = variable.getDefaultExpression(); // the failing expression			
//			CtStatement toWrap = getFactory().Code().createVariableAssignment(
//					variable.getReference(), false, (CtExpression) getFactory().Code().createLiteral(1));
//			
//			CtBlock<?> containingBlock = variable.getParent(CtBlock.class);
//			ListIterator<CtStatement> stmtIter = containingBlock.getStatements().listIterator();
//			while (stmtIter.hasNext()) {
//				CtStatement cur = stmtIter.next();
//				if (cur == newVariable) {
//					stmtIter.add(toWrap);		
//					toWrap.setParent(containingBlock);
//					toWrap.setPosition(new SourcePositionImpl(
//							cur.getPosition().getCompilationUnit(),
//							cur.getPosition().getSourceEnd() + 3,
//							cur.getPosition().getSourceEnd() + 3,
//							new int[0]));
//					break;
//				}
//			}
//			new CodeFixResult(toWrap);
//			
//			failingStatement = toWrap;
		}
		else if (failingStatement instanceof CtAssignment) {
			ensureTargetHasDefaultValue((CtAssignment<?,?>) failingStatement);
		}
				
		CtTry tryCatch = buildTryCatch(
				failingStatement, 
				failureException, 
				assertFactory);
		SourceCodeFragment frag = getFactory().Fragment().replace(failingStatement, tryCatch);
		return new CodeFixResult(tryCatch, frag);
	}

	/**
	 * If the target of the assignment is an uninitialized local variable,
	 * then this method adds a default value to allow the assignment to be
	 * safely wrapped in a try-catch block. 
	 */
	private void ensureTargetHasDefaultValue(CtAssignment<?, ?> assignment) throws UnfixableException {
		CtExpression<?> assigned = assignment.getAssigned();
		if (assigned instanceof CtVariableAccess) {
			CtVariable<?> variable = ((CtVariableAccess<?>) assigned).getVariable().getDeclaration();
			if (variable != null && variable instanceof CtLocalVariable) {
				CtExpression<?> defaultInit = ((CtLocalVariable<?>) variable).getDefaultExpression();
				if (defaultInit == null) {
					updateDefaultValue(variable);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateDefaultValue(CtVariable<?> variable) throws UnfixableException {
		CtVariable newVariable = getFactory().Core().clone(variable);
		newVariable.setDefaultExpression(createDefaultValue(variable.getType()));
		newVariable.getModifiers().clear(); // remove final
		getFactory().Fragment().replace(variable, newVariable);
	}

	private CtExpression<?> createDefaultValue(CtTypeReference<?> type) {
		return createDefaultValue(type.getActualClass());
	}

	private CtExpression<?> createDefaultValue(Class<?> actualClass) {
		if (       Boolean.class.isAssignableFrom(actualClass)
				|| Boolean.TYPE.isAssignableFrom(actualClass)) {
			return getFactory().Code().createLiteral(false);
		}
		if (       Byte.class.isAssignableFrom(actualClass)
				|| Byte.TYPE.isAssignableFrom(actualClass)
				|| Short.class.isAssignableFrom(actualClass)
				|| Short.TYPE.isAssignableFrom(actualClass)
				|| Integer.class.isAssignableFrom(actualClass)
				|| Integer.TYPE.isAssignableFrom(actualClass)
				|| Long.class.isAssignableFrom(actualClass)
				|| Long.TYPE.isAssignableFrom(actualClass)
				|| Float.class.isAssignableFrom(actualClass)
				|| Float.TYPE.isAssignableFrom(actualClass)
				|| Double.class.isAssignableFrom(actualClass)
				|| Double.TYPE.isAssignableFrom(actualClass)) {
			return getFactory().Code().createLiteral(0);
		}
		if (       Character.class.isAssignableFrom(actualClass)
				|| Character.TYPE.isAssignableFrom(actualClass)) {
			return getFactory().Code().createLiteral('\0');
		}
		return getFactory().Code().createLiteral(null);
	}

	@SuppressWarnings("unchecked")
	private CtTry buildTryCatch(
			CtStatement failingStatement,
			Throwable failureException, 
			AssertFactory assertFactory) {
		
		CtBlock tryBody = getFactory().Core().createBlock();
		tryBody.getStatements().add(failingStatement);
		CtInvocation<?> failCall = assertFactory.createAssertFail();
		tryBody.getStatements().add(failCall);		
		
		CtLocalVariable<? extends Throwable> caught = 
			getFactory().Code().createLocalVariable(
					getFactory().Type().createReference(failureException.getClass()), 
					"e", null);
		
		AccessorTree tree = AccessorTree.build(null, failureException, MAX_TREE_DEPTH);
		CtStatement statement = tree.buildAssertionTree(
				getFactory(), 
				assertFactory, 
				null, 
				getFactory().Code().createVariableAccess(
						caught.getReference(), false));
		
		CtBlock catchBody = null;
		if (statement instanceof CtBlock) {
			catchBody = (CtBlock) statement;
		}
		else {
			catchBody = getFactory().Core().createBlock();
			catchBody.getStatements().add(statement);
		}
		
		CtCatch expectedCatch = getFactory().Core().createCatch();
		expectedCatch.setBody(catchBody);
		expectedCatch.setParameter(caught);
		
		CtTry tryCatch = getFactory().Core().createTry();
		tryCatch.setBody(tryBody);
		tryCatch.getCatchers().add(expectedCatch);
		
		return tryCatch;
	}

	private CtStatement findFailingStatement(
			Method testMethod,
			final StackTraceElement failingPosition) throws UnfixableException {
		CtExecutable<?> methodMirror = 
			getFactory().Method().createReference(testMethod).getDeclaration();
		List<CtInvocation<?>> failingInvocations = 
			Query.getElements(methodMirror, new AbstractFilter<CtInvocation<?>>(CtInvocation.class) {
				@Override
				public boolean matches(CtInvocation<?> element) {
					return elementContainsLocation(element, failingPosition);
				}
			});
		if (failingInvocations.size() == 0) {
			return null;
		}
		// the lowest invocation
		CtInvocation<?> failingInvocation = failingInvocations.get(failingInvocations.size() -1);
		// find the highest statement below a block
		CtStatement failingStatement = failingInvocation;
		while (failingStatement.getParent() != null 
				&& !(failingStatement.getParent() instanceof CtBlock)) {			
			failingStatement = failingStatement.getParent(CtStatement.class); 
		}
		return failingStatement;
	}

	private StackTraceElement findFailingPositionInMethod(
			Method testMethod, 
			StackTraceElement[] stackTrace) {
		String testClassName = testMethod.getDeclaringClass().getName();
		String testMethodName = testMethod.getName();
		for (StackTraceElement stackElem : stackTrace) {
			if (stackElem.getMethodName().equals(testMethodName) 
					&& stackElem.getClassName().equals(testClassName)) {
				return stackElem;
			}
		}
		return null;
	}

	protected boolean elementContainsLocation(
			CtElement element,
			StackTraceElement assertLocation) {
		SourcePosition position = element.getPosition();
		if (position == null) {
			return false;
		}
		int targetLine = assertLocation.getLineNumber();
		int startLine = position.getLine();
		int endLine = position.getEndLine();
		if (element instanceof CtVariable) {
			CtExpression<?> init = ((CtVariable<?>) element).getDefaultExpression();
			if (init != null) {
				// Bug in Spoon. Position does not include initial expression.
				endLine = init.getPosition().getEndLine();
			}
		}
		return startLine <= targetLine 
			&& targetLine <= endLine;
	}

	
}
