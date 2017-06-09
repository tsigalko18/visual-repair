/**
 * 
 */
package edu.illinois.reassert.reflect;

import spoon.reflect.Factory;
import spoon.reflect.reference.CtExecutableReference;

public class JUnit4Factory extends AssertFactory {

	public JUnit4Factory(Factory factory) {
		super(factory);
	}

	@Override
	public Class<?> getAssertClass() {
		return org.junit.Assert.class;
	}
	
	@Override
	public CtExecutableReference<?> getAssertNullReference() {
		return createAssertReference(
				"void org.junit.Assert#assertNull(java.lang.Object)");
	}

	@Override
	public CtExecutableReference<?> getAssertNotNullReference() {
		return createAssertReference(
				"void org.junit.Assert#assertNotNull(java.lang.Object)");
	}
	
	@Override
	public CtExecutableReference<?> getAssertEqualsReference() {
		return createAssertReference(
				"void org.junit.Assert#assertEquals(java.lang.Object, java.lang.Object)");
	}

	@Override
	public CtExecutableReference<?> getAssertSameReference() {
		return createAssertReference(
				"void org.junit.Assert#assertSame(java.lang.Object, java.lang.Object)");
	}

	@Override
	public CtExecutableReference<?> getAssertTrueReference() {
		return createAssertReference(
				"void org.junit.Assert#assertTrue(boolean)");
	}	

	@Override
	public CtExecutableReference<?> getAssertFalseReference() {
		return createAssertReference(
				"void org.junit.Assert#assertFalse(boolean)");
	}

	@Override
	public CtExecutableReference<?> getAssertFailReference() {
		return createAssertReference(
			"void org.junit.Assert#fail()");
	}
	
}