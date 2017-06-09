package edu.illinois.reassert.reflect;

import spoon.reflect.Factory;
import spoon.reflect.reference.CtExecutableReference;

public class JUnit3Factory extends AssertFactory {

	public JUnit3Factory(Factory factory) {
		super(factory);
	}

	@Override
	public Class<?> getAssertClass() {
		return junit.framework.Assert.class;
	}

	@Override
	public  CtExecutableReference<?> getAssertNullReference() {
		return createAssertReference(
				"void junit.framework.Assert#assertNull(java.lang.Object)");
	}

	@Override
	public  CtExecutableReference<?> getAssertNotNullReference() {
		return createAssertReference(
				"void junit.framework.Assert#assertNotNull(java.lang.Object)");
	}
	
	@Override
	public  CtExecutableReference<?> getAssertEqualsReference() {
		return createAssertReference(
				"void junit.framework.Assert#assertEquals(java.lang.Object, java.lang.Object)");
	}

	@Override
	public  CtExecutableReference<?> getAssertSameReference() {
		return createAssertReference(
				"void junit.framework.Assert#assertSame(java.lang.Object, java.lang.Object)");
	}

	@Override
	public  CtExecutableReference<?> getAssertTrueReference() {
		return createAssertReference(
				"void junit.framework.Assert#assertTrue(boolean)");
	}	

	@Override
	public  CtExecutableReference<?> getAssertFalseReference() {
		return createAssertReference(
				"void junit.framework.Assert#assertFalse(boolean)");
	}
	
	@Override
	public CtExecutableReference<?> getAssertFailReference() {
		return createAssertReference(
		"void junit.framework.Assert#fail()");
	}

}