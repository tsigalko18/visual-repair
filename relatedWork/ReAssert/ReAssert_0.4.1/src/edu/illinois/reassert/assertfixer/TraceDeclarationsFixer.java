package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractFilter;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.Factory;

public class TraceDeclarationsFixer extends AssertEqualsFixer {

	public TraceDeclarationsFixer(Factory factory) {
		super(factory);
	}

	@Override
	protected CodeFixResult fix(
			Method testMethod,
			final CtInvocation<?> assertion, 
			Throwable failureException, 
			Object expectedObject,
			Object actualObject) throws UnfixableException {
		if (!canBeLiteral(actualObject)) {
			return null;
		}
		
		CtExpression<?> expectedArg = getExpectedArg(assertion);		
		CtExpression<?> newActual = createLiteral(actualObject);	
			
		String assertingClassName = assertion.getParent(CtType.class).getQualifiedName();
		boolean foundAssertion = false;		
		Deque<CtInvocation<?>> stack = new LinkedList<CtInvocation<?>>();
		for (final StackTraceElement location : failureException.getStackTrace()) {			
			if (foundAssertion) {
				CtClass<?> assertingClass = getFactory().Class().get(location.getClassName());
				if (assertingClass == null) {
					break;
				}
				List<CtInvocation<?>> assertions = Query.getElements(
						assertingClass, 
						new AbstractFilter<CtInvocation<?>>(CtInvocation.class) {
							public boolean matches(CtInvocation<?> invocation) {
								return elementContainsLocation(invocation, location);
							}
						});
				if (assertions.size() == 0) {
					break;
				}
				// query is pre-order, so last element in list is the outermost
				// TODO: no guarantee that the assertion we want is the outermost; need to check
				// that its target matches the callee in the higher stack frame
				stack.add(assertions.remove(0));
			}
			else if (location.getClassName().equals(assertingClassName)
					&& elementContainsLocation(assertion, location)) {				
				foundAssertion = true;
			}
		}
		
		if (expectedArg instanceof CtVariableAccess) {
			CtVariableAccess<?> access = (CtVariableAccess<?>) expectedArg;
			return traceAndReplace(stack, access, newActual);
		}
		return null;
	}

	private CodeFixResult traceAndReplace(
			Deque<CtInvocation<?>> stack, 
			CtVariableAccess<?> access,
			CtExpression<?> newActual) {
		CtVariable<?> decl = access.getVariable().getDeclaration();
		if (decl == null) {
			// No source for variable
			return null;
		}
		
		CtExpression<?> oldExpected = null;		
		if (decl instanceof CtParameter) {
			if (stack.size() == 0) {
				return null;
			}
			// trace up call stack
			int paramIndex = 0;			
			CtExecutable<?> method = decl.getParent(CtExecutable.class);
			for (CtParameter<?> param : method.getParameters()) {
				if (param == decl) {
					break;
				}
				paramIndex++;
			}			
			CtInvocation<?> higherAssert = stack.pop();
			oldExpected = higherAssert.getArguments().get(paramIndex);
		}
		else {
			oldExpected = decl.getDefaultExpression();
		}
		
		if (oldExpected == null) {
			// Value is set elsewhere. Give up. 
			return null;
		}
		if (oldExpected instanceof CtVariableAccess) {
			// Trace back one assignment
			return traceAndReplace(stack, (CtVariableAccess<?>) oldExpected, newActual);
		}		
		
		// else blast away the old expression
		SourceCodeFragment frag = getFactory().Fragment().replace(oldExpected, newActual);
		return new CodeFixResult(newActual, frag);
	}

	/**
	 * @deprecated copied from {@link AccessorTree}. Refactor.
	 */
	private boolean canBeLiteral(Object value) {
		return value == null || canBeLiteral(value.getClass());
	}
	
	/**
	 * @deprecated copied from {@link AccessorTree}. Refactor.
	 */
	private boolean canBeLiteral(Class<?> actualClass) {		
		while(actualClass.isArray()) {
			actualClass = actualClass.getComponentType();
		}
		return 
			actualClass.isPrimitive()
			|| Boolean.class.isAssignableFrom(actualClass)
			|| Byte.class.isAssignableFrom(actualClass)
			|| Short.class.isAssignableFrom(actualClass)
			|| Integer.class.isAssignableFrom(actualClass)
			|| Long.class.isAssignableFrom(actualClass)
			|| Float.class.isAssignableFrom(actualClass)
			|| Double.class.isAssignableFrom(actualClass)
			|| Character.class.isAssignableFrom(actualClass)
			|| String.class.isAssignableFrom(actualClass)
			|| Class.class.isAssignableFrom(actualClass);
	}

	/**
	 * @deprecated copied from {@link AccessorTree}. Refactor.
	 */
	private CtExpression<?> createLiteral(Object value) {
		assert canBeLiteral(value);

		if (value == null) {
			return getFactory().Code().createLiteral(null);
		}
		if (value instanceof Class) {
			// literal .class
			return getFactory().Code().createClassAccess(
					getFactory().Class().createReference((Class<?>)value));
		}
		if (value.getClass().isArray()) {
			return createArrayLiteral(value);
		}
		return getFactory().Code().createLiteral(value);
	}

	/**
	 * @deprecated copied from {@link AccessorTree}. Refactor.
	 */
	@SuppressWarnings("unchecked")
	private CtNewArray<?> createArrayLiteral(Object value) {
		assert value.getClass().isArray();
		
		CtNewArray toReturn = 
			getFactory().Core().createNewArray();
		toReturn.setType(
				getFactory().Type().createReference(value.getClass()));
		toReturn.setElements(
				createArrayElements(value));
		return toReturn;
	}

	/**
	 * @deprecated copied from {@link AccessorTree}. Refactor.
	 */
	private List<CtExpression<?>> createArrayElements(Object actualValue) {
		List<CtExpression<?>> elements = new LinkedList<CtExpression<?>>();
		int length = Array.getLength(actualValue);
		for (int i = 0; i < length; i++) {
			// get array values by reflection because we can't 
			// cast primitive arrays to Object[]
			Object element = Array.get(actualValue, i);
			// recurse through multidimensional array
			elements.add(createLiteral(element));
		}
		return elements;
	}
}
