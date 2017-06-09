package edu.illinois.reassert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import edu.illinois.reassert.assertfixer.AssertCollectionSizeFixer;
import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer;
import edu.illinois.reassert.assertfixer.AssertNullToAssertEqualsFixer;
import edu.illinois.reassert.assertfixer.InvertBooleanAssertFixer;
import edu.illinois.reassert.assertfixer.InvertRelationalFixer;
import edu.illinois.reassert.assertfixer.RemoveNondeterministicFixer;
import edu.illinois.reassert.assertfixer.SurroundWithTryCatchFixer;
import edu.illinois.reassert.assertfixer.TraceDeclarationsFixer;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.SimpleSpoonLoader;

/**
 * Fixes a single failing test using the given fix strategies 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class TestFixer {

	public static final char METHOD_NAME_SEPARATOR = '#';
	
	/**
	 * The default list of fixers to attempt when repairing a failing test.
	 * Order is important.
	 */
	@SuppressWarnings("unchecked")
	public static final Class<FixStrategy>[] DEFAULT_FIXERS = new Class[] {
		AssertCollectionSizeFixer.class,
		AssertNullToAssertEqualsFixer.class,
		TraceDeclarationsFixer.class,
		AssertEqualsReplaceLiteralFixer.class,
		AssertEqualsExpandAccessorsFixer.class,
		InvertRelationalFixer.class,
		InvertBooleanAssertFixer.class,
		SurroundWithTryCatchFixer.class,
		RemoveNondeterministicFixer.class,
	};

	private Factory factory = new Factory();
	private SimpleSpoonLoader loader = new SimpleSpoonLoader(factory);
	private JUnitCore junitCore = new JUnitCore();
	private List<FixStrategy> fixStrategies = new LinkedList<FixStrategy>();

	private static int enableInstrumentationCount = 0;
	
	public TestFixer() {
		for (Class<FixStrategy> fixerClass : DEFAULT_FIXERS) {
			try {
				addFixStrategy(fixerClass);
			} 
			catch (InstantiationException e) {
				// Programmer error. A default fixer is written incorrectly
				throw new RuntimeException(e);
			}
		}
	}	

	public Factory getFactory() {
		return factory;
	}
	
	public SimpleSpoonLoader getLoader() {
		return loader;
	}
	
	/**
	 * Add a directory from which to load classes
	 */
	public void addSourcePath(String sourcePath) {
		factory.addSourcePath(sourcePath);
	}

	/**
	 * @param fixerClass a class of {@link FixStrategy} to use to repair broken assertions
	 * @throws InstantiationException if fixerClass cannot be instantiated
	 */
	public void addFixStrategy(Class<? extends FixStrategy> fixerClass) throws InstantiationException {
		FixStrategy instance = instantiateFixStrategy(fixerClass);
		addFixStrategy(instance);
	}

	/**
	 * @param fixerClass a class of {@link FixStrategy} to use to repair broken assertions
	 * @throws InstantiationException if fixerClass cannot be instantiated
	 */
	public void prependFixStrategy(Class<? extends FixStrategy> fixerClass) throws InstantiationException {
		FixStrategy instance = instantiateFixStrategy(fixerClass);
		prependFixStrategy(instance);
	}

	
	/**
	 * @return an instance of the given {@link FixStrategy}
	 * @throws InstantiationException if the strategy cannot be instantiated
	 */
	private FixStrategy instantiateFixStrategy(
			Class<? extends FixStrategy> fixerClass) throws InstantiationException {
		Constructor<? extends FixStrategy> constructor;
		try {
			constructor = fixerClass.getConstructor(getFactory().getClass()); // single-argument constructor
			return constructor.newInstance(getFactory());
		}
		catch (NoSuchMethodException e) {
			try {
				constructor = fixerClass.getConstructor();
				return constructor.newInstance();
			} 
			catch (Exception e1) {				
				throw new InstantiationException("Unable to instantiate " + fixerClass.getName());
			}
		} 
		catch (Exception e) {
			throw new InstantiationException("Unable to instantiate " + fixerClass.getName());
		}
		
	}

	/**
	 * Adds a fix strategy to the end of the current list 
	 */
	public void addFixStrategy(FixStrategy fixer) {
		getFixStrategies().add(fixer);
	}
	
	/**
	 * Adds a fix strategy to the start of the current list 
	 */
	public void prependFixStrategy(FixStrategy fixer) {
		getFixStrategies().add(0, fixer);
	}
	
	public List<FixStrategy> getFixStrategies() {	
		return fixStrategies;
	}

	public Map<String, Set<String>> getMethodsToInstrument() {
		Map<String, Set<String>> toInstrument = new HashMap<String, Set<String>>();
		for (FixStrategy strategy : getFixStrategies()) {
			Collection<String> assertMethods = strategy.getMethodsToInstrument();
			if (assertMethods != null) {
				for (String assertMethod : assertMethods) {
					// TODO: repeated in several places and no error checking. Refactor.
					int splitPoint = assertMethod.indexOf(METHOD_NAME_SEPARATOR);
					String className = assertMethod.substring(0, splitPoint);
					String methodName = assertMethod.substring(splitPoint + 1);
					if (!toInstrument.containsKey(className)) {
						toInstrument.put(className, new HashSet<String>());
					}
					toInstrument.get(className).add(methodName);
				}
			}
		}
		return toInstrument;
	}
	
	/**
	 * Runs the given test and attempts to fix it if it fails.
	 * Only fixes the first failing assertion it encounters. 
	 * Call multiple times for multiple assertions.
	 * 
	 * @param testName the fully-qualified name of the test to execute 
	 * (e.g. some.package.SomeClass#testMethod)
	 * @return a {@link FixResult} or <code>null</code> if the test passed
	 */
	public FixResult fix(String testName) throws UnfixableException {
		int splitPoint = testName.indexOf(METHOD_NAME_SEPARATOR);
		String className = testName.substring(0, splitPoint);
		String methodName = testName.substring(splitPoint + 1);
		Class<?> testClass;
		try {
			testClass = loader.load(className);
		} 
		catch (ClassNotFoundException e) {
			throw new UnfixableException(String.format(
					"No class %s.", className), e);
		}
		return fix(testClass, methodName);
	}
	
	/**
	 * Runs the given test and attempts to fix it if it fails.
	 * Only fixes the first failing assertion it encounters. 
	 * Call multiple times for multiple assertions.
	 * 
	 * @param testClass the class containing the test to execute
	 * @param methodName the test method's name
	 * @throws UnfixableException if the test failed but could not be fixed
	 * @return a {@link FixResult} or <code>null</code> if the test passed
	 */
	public FixResult fix(Class<?> testClass, String methodName) throws UnfixableException {
		String methodIdentifier = 
			testClass.getName() + METHOD_NAME_SEPARATOR + methodName;
		
		testClass = instrumentAssertions(testClass);

		Method testMethod;
		try {
			testMethod = testClass.getMethod(methodName);
		} 
		catch (NoSuchMethodException e) {
			throw new UnfixableException(String.format(
					"No method %s.", methodIdentifier), e);
		}
				
		enableManualInstrumentation(true);
		Result result = junitCore.run(Request.method(testClass, methodName));
		enableManualInstrumentation(false);
		
		if (result.getRunCount() == 0) {
			throw new UnfixableException(String.format(
					"Unable to run %s.", methodIdentifier));
		}

		Failure failure = findFailureForMethod(result, testClass, methodName);
		if (failure == null) {
			// Test succeeded.
			// Note that JUnit3 runs all tests in a class, so the failure
			// count may not be 0
			return null;  
		}
		
		Throwable testException = failure.getException();
		for (FixStrategy fixer : getFixStrategies()) {
			FixResult fixResult = fixer.fix(testMethod,	testException);
			if (fixResult != null) {
				fixResult.setAppliedFixer(fixer);
				if (fixResult instanceof CodeFixResult) {
					((CodeFixResult)fixResult).setLoader(loader); // HACK!
				}
				// return first fixer that succeeds
				return fixResult;
			}
		}		
		// no fix was applied
		throw new UnfixableException("No applicable fix strategies.");
	}

	private void enableManualInstrumentation(boolean enable) {
		if (enable) {
			enableInstrumentationCount++;
		}
		else {
			enableInstrumentationCount--;
		}
		// act like a semaphore so ReAssert can fix itself
		enable = (enableInstrumentationCount  > 0);
		try {
			// Set manual instrumentation in assert classes.			
			junit.framework.Assert.ENABLE_INSTRUMENTATION = enable;
			org.junit.Assert.ENABLE_INSTRUMENTATION = enable;
		}
		catch (NoSuchFieldError e) {
			// Accessing the fields has the side-effect of ensuring that the 
			// user has the CLASSPATH set correctly.
			throw new RuntimeException(
					"ReAssert must come before JUnit on the CLASSPATH");
		}
	}

	private Class<?> instrumentAssertions(Class<?> testClass) {
		if (getMethodsToInstrument().size() == 0) {
			return testClass;
		}
		AssertInstrumenter instrumenter = new AssertInstrumenter(testClass.getClassLoader());
		for (String instrumentedClass : getMethodsToInstrument().keySet()) {
			for (String instrumentedMethod : getMethodsToInstrument().get(instrumentedClass)) {
				instrumenter.instrument(instrumentedClass, instrumentedMethod);
			}
		}
		try {
			return instrumenter.loadClass(testClass.getName());			
		} 
		catch (ClassNotFoundException e) {
			// ignore. Just use given testClass
			return testClass;
		}
	}

	private Failure findFailureForMethod(
			Result result, 
			Class<?> testClass,
			String methodName) throws UnfixableException {
		String expectedDescription = String.format(
				"%s(%s)",
				methodName,
				testClass.getName());
		for (Failure failure : result.getFailures()) {
			String failureString = failure.getTestHeader();
			if (failureString.contains("initializationError")) {
				throw new UnfixableException(failure.getException().getMessage() + '.');
			}
			if (expectedDescription.equals(failureString)) {
				return failure;
			}
		}
		return null;
	}

}
