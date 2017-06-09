/**
 * 
 */
package edu.illinois.reassert.reflect;

import spoon.reflect.Factory;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Abstract base class for classes that can produce assertions
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public abstract class AssertFactory {
	
	private Factory factory;

	public AssertFactory(Factory factory) {
		this.factory = factory;
	}

	public Factory getFactory() {
		return factory;
	}
	
	public abstract Class<?> getAssertClass();
	public abstract CtExecutableReference<?> getAssertNullReference();
	public abstract CtExecutableReference<?> getAssertNotNullReference();
	public abstract CtExecutableReference<?> getAssertEqualsReference();
	public abstract CtExecutableReference<?> getAssertSameReference();
	public abstract CtExecutableReference<?> getAssertTrueReference();
	public abstract CtExecutableReference<?> getAssertFalseReference();
	public abstract CtExecutableReference<?> getAssertFailReference();
	
	protected  CtExecutableReference<?> createAssertReference(
			String refString) {
		CtExecutableReference<?> ref = 
			getFactory().Method().createReference(refString);
		ref.setStatic(true);
		return ref;
	}
	
	public boolean isAssertClass(CtTypeReference<?> assertClass) {			
		return getAssertClass().isAssignableFrom(assertClass.getActualClass());
	}
	
	public CtInvocation<?> createAssertFalse(CtExpression<?> truthArg) {
		return createBooleanAssert(getAssertFalseReference(), truthArg);
	}
	
	public CtInvocation<?> createAssertTrue(CtExpression<?> truthArg) {
		return createBooleanAssert(getAssertTrueReference(), truthArg);
	}
	
	protected CtInvocation<?> createBooleanAssert(
			CtExecutableReference<?> assertMethodRef,
			CtExpression<?> arg) {
		CtTypeReference<Boolean> booleanTypeRef = 
			getFactory().Type().createReference(Boolean.TYPE);
		CtTypeReference<Boolean> booleanBoxTypeRef = 
			getFactory().Type().createReference(Boolean.class);
		CtTypeReference<?> argTypeRef = null;			
		if (arg instanceof CtInvocation<?>) {
			// HACK: CtInvocation doesn't return a type for some reason
			CtInvocation<?> call = (CtInvocation<?>) arg;
			argTypeRef = call.getExecutable().getType();
		}
		else {
			argTypeRef = arg.getType();
		}
		if (!booleanTypeRef.isAssignableFrom(argTypeRef) 
				&& !booleanBoxTypeRef.isAssignableFrom(argTypeRef)) {
			arg.getTypeCasts().add(booleanBoxTypeRef);
		}			
		return createInvocation(assertMethodRef, arg);
	}
	
	public CtInvocation<?> createAssertEquals(
			CtExpression<?> expected,
			CtExpression<?> actual) {
		if (actual instanceof CtLiteral && 
				!(expected instanceof CtLiteral)) {
			// Literal values should almost always go on the expected side
			CtExpression<?> tmp = actual;
			actual = expected;
			expected = tmp;
		}
		return createInvocation(getAssertEqualsReference(), expected, actual);
	}
	
	public CtInvocation<?> createAssertNull(CtExpression<?> arg) {
		return createInvocation(getAssertNullReference(), arg);
	}

	public CtInvocation<?> createAssertNotNull(CtExpression<?> arg) {
		return createInvocation(getAssertNotNullReference(), arg);
	}
	
	public CtInvocation<?> createAssertFail() {
		return createInvocation(getAssertFailReference());
	}
	
	protected CtInvocation<?> createInvocation(
			CtExecutableReference<?> assertMethodRef,
			CtExpression<?>... args) {
		return getFactory().Code().createInvocation(
				null, // static call, no target
				assertMethodRef, 
				args);
	}
	
	public boolean isAssertNull(CtInvocation<?> assertion) {
		return isAssertMethod(assertion, "assertNull");
	}
	
	public boolean isAssertEquals(CtInvocation<?> assertion) {
		return isAssertMethod(assertion, "assertEquals");
	}

	public boolean isAssertArrayEquals(CtInvocation<?> assertion) {
		return isAssertMethod(assertion, "assertArrayEquals");
	}

	public boolean isAssertSame(CtInvocation<?> assertion) {
		return isAssertMethod(assertion, "assertSame");
	}
	
	public boolean isAssertTrue(CtInvocation<?> assertion) {
		return isAssertMethod(assertion, "assertTrue");
	}

	public boolean isAssertFalse(CtInvocation<?> assertion) {
		return isAssertMethod(assertion, "assertFalse");
	}

	protected boolean isAssertMethod(CtInvocation<?> assertion, String methodName) {
		if (assertion == null) {
			return false;
		}
		CtExecutableReference<?> ref = assertion.getExecutable();
		CtTypeReference<?> assertClass = ref.getDeclaringType();
		return isAssertClass(assertClass)
				&& methodName.equals(ref.getSimpleName());
	}

	public CtTypeReference<?> getAssertTypeReference() {
		return getFactory().Type().createReference(getAssertClass());
	}

}