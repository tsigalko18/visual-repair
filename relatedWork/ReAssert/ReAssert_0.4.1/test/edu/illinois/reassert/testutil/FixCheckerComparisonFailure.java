package edu.illinois.reassert.testutil;

import org.junit.ComparisonFailure;

import spoon.reflect.declaration.CtExecutable;

public class FixCheckerComparisonFailure extends ComparisonFailure {
	private static final long serialVersionUID = 1L;
	private CtExecutable<?> expectedMethod;
	private CtExecutable<?> actualMethod;

	public FixCheckerComparisonFailure(
			String message, 
			CtExecutable<?> expected,
			String expectedSource, 
			CtExecutable<?> actual, 
			String actualSource) {
		super(message, expectedSource, actualSource);
		this.expectedMethod = expected;
		this.actualMethod = actual; 
	}

	public CtExecutable<?> getExpectedMethod() {
		return expectedMethod;
	}
	
	public CtExecutable<?> getActualMethod() {
		return actualMethod;
	}
}
