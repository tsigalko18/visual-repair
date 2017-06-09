package edu.illinois.reassert.testutil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import spoon.reflect.Factory;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.FixResult;
import edu.illinois.reassert.FixStrategy;
import edu.illinois.reassert.TestFixer;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.ReAssertPrettyPrinter;

/**
 * JUnit runner used for testing ReAssert fixers.
 * Executes a JUnit test, fixing each failed test method using the {@link FixStrategy} 
 * classes specified in the @{@link Fixers} annotation.  Fixes are stored in memory, 
 * not the filesystem.  It then checks that the fixed result matches the corresponding 
 * method annotated with {@link Fix}.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class FixChecker extends BlockJUnit4ClassRunner {

	public static String SOURCE_DIR = "test/";
	
	/**
	 * test method names to fix methods
	 */
	private HashMap<String, Method> testFixes = null;

	private Class<? extends FixStrategy>[] fixerClasses;
	/**
	 * When reexecuting the method for a fix, indicates whether this runner's special
	 * functionality should be used. Prevents multiple FixCheckers from running at once.
	 */
	public static boolean shouldUseFixingRunner = true;

	public FixChecker(Class<?> klass) throws InitializationError {		
		super(klass);
	}

	/**
	 * @return methods annotated with @{@link Fix} in the current test class
	 */
	protected Map<String, Method> getTestFixes() {
		if (testFixes == null) {
			testFixes = new HashMap<String, Method>();
			Method[] methods = getTestClass().getJavaClass().getMethods();
			for (Method method : methods) {
				Fix fix = method.getAnnotation(Fix.class);
				if (fix != null) {
					testFixes.put(fix.value(), method);
				}
			}
		}
		return testFixes;
	}
	
	/**
	 * @return the @{@link Fix} annotation that corresponds to the given @{@link Test} method
	 */
	protected Method getFix(String testMethod) {
		return getTestFixes().get(testMethod);
	}
	
	/**
	 * @return the classes in the class' @{@link Fixers} annotation or an empty array
	 */
	@SuppressWarnings("unchecked")
	protected Class<? extends FixStrategy>[] getFixerClasses() {
		if (fixerClasses == null) {
			Fixers annotation = getTestClass().getJavaClass().getAnnotation(Fixers.class);
			if (annotation == null) {
				return new Class[0];
			}
			fixerClasses = annotation.value();
		}
		return fixerClasses;
	}
	
	@Override
	public void run(RunNotifier notifier) {
		if (shouldUseFixingRunner) {
			super.run(new FixingNotifier(notifier));
		}
		else {
			super.run(notifier);
		}
	}
	
	private class FixingNotifier extends RunNotifier {

		private RunNotifier decorated;
		private Set<String> failedMethods = new HashSet<String>();
		
		public FixingNotifier(RunNotifier decorated) {
			this.decorated = decorated;			
		}
		
		@Override
		public void fireTestFailure(Failure failure) {
			String testMethod = extractMethodName(failure.getDescription());
			Class<?> testClass = getTestClass().getJavaClass();
			failedMethods.add(testMethod);

			try {
				TestFixer fixer = new TestFixer();
				fixer.addSourcePath(SOURCE_DIR);
				
				Class<? extends FixStrategy>[] fixers = getFixerClasses();
				if (fixers.length > 0) {
					fixer.getFixStrategies().clear();
					for (Class<? extends FixStrategy> fixerClass : fixers) {
						fixer.addFixStrategy(fixerClass);
					}
				}

				shouldUseFixingRunner = false;
				FixResult result = fixer.fix(testClass, testMethod);
				if (result == null) {
					// shouldn't ever hit this since we already know the test fails.
					throw new FixCheckerException(
							"Test succeeded. Does it depend on external state?");
				}
				else if (isMarkedUnfixable(testClass, testMethod)) {
					throw new FixCheckerException(
							"Fix was applied but test is marked @Unfixable.");
				}
				if (!(result instanceof CodeFixResult)) {
					throw new FixCheckerException(
							"Fixer should return a code fix.");
				}
				CodeFixResult fixResult = (CodeFixResult) result;
				
				CtExecutable<?> expectedResult = 
					findExpectedResult(testMethod, fixer.getFactory());
				CtExecutable<?> reassertResult = 
					fixResult.getFixedElement().getParent(CtExecutable.class);						
				if (expectedResult == null) {
					String message = String.format(
							"No method annotated with @Fix(\"%s\") was found.",
							testMethod);
					throw new FixCheckerException(message, failure.getException());
				}

				ReAssertPrettyPrinter pp = new ReAssertPrettyPrinter(
						fixer.getFactory().getEnvironment());
				String expectedBody = pp.print(expectedResult.getBody());
				String actualBody = pp.print(reassertResult.getBody());
				if (!expectedBody.equals(actualBody)) {
					String message = String.format(
							"Fixed method %s does not match @Fix(\"%s\") method",
							testMethod, 
							testMethod);
					throw new FixCheckerComparisonFailure(
							message, 
							expectedResult, 
							expectedBody, 
							reassertResult, 
							actualBody);
				}
	
				checkRecompilation(fixer);
			}
			catch (UnfixableException e) {
				if (!isMarkedUnfixable(testClass, testMethod)) {
					decorated.fireTestFailure(new Failure(
							failure.getDescription(),
							new FixCheckerException(
									String.format(
											"Unable to fix %s. %s\n" +
											"Mark with @Unfixable if this was expected", 
											testMethod,
											e.getMessage()), e)));
				}
				// else the test was marked @Unfixable and thus it passes
			}
			catch (FixCheckerComparisonFailure e) {
				decorated.fireTestFailure(new Failure(
						failure.getDescription(), e));
			}
			catch (FixCheckerException e) {
				decorated.fireTestFailure(new Failure(
						failure.getDescription(), e));

			}
			catch (Exception e) {
				decorated.fireTestFailure(new Failure(
						failure.getDescription(),
						new FixCheckerException("ReAssert threw an exception", e)));
			}
			finally {
				shouldUseFixingRunner = true;
			}
		}

		private void checkRecompilation(TestFixer fixer) {
			PrintStream oldErr = System.err;
			ByteArrayOutputStream recordingErr = new ByteArrayOutputStream();
			System.setErr(new PrintStream(recordingErr));
			fixer.getLoader().compile();
			System.setErr(oldErr);
			String errors = recordingErr.toString();
			if (errors != null && errors.length() > 0) {
				System.err.println(errors);
				throw new FixCheckerException("Could not recompile. Were snippets applied correctly?");
			}
		}

		private CtExecutable<?> findExpectedResult(String testMethod,
				Factory factory) {
			Method fixMethod = getFix(testMethod);
			if (fixMethod == null) {
				return null;
			}
			CtExecutableReference<?> fixMethodRef = 
				factory.Method().createReference(fixMethod);
			return fixMethodRef.getDeclaration();
		}
		
		private boolean isMarkedUnfixable(Class<?> testClass, String testMethod) {
			Method method;
			try {
				method = testClass.getMethod(testMethod);
			} catch (Exception e) {
				// programmer error
				throw new RuntimeException(e);
			}
			Unfixable annotation = method.getAnnotation(Unfixable.class);
			return annotation != null;
		}

		private String extractMethodName(Description description) {
			String displayName = description.getDisplayName();
			return displayName.substring(0, displayName.indexOf('('));
		}

		@Override
		public void fireTestFinished(Description description) {
			String testMethod = extractMethodName(description);
			if (!failedMethods.contains(testMethod)
					&& getFix(testMethod) != null) {
				// error if it does
				decorated.fireTestFailure(new Failure(
						description,
						new FixCheckerException(String.format(
								"%s succeeded but has @Fix method", 
								testMethod))));
			}

			// should always fire test finished regardless of success or failure
			decorated.fireTestFinished(description);
		}

		@Override
		public void fireTestRunFinished(Result result) {
			decorated.fireTestRunFinished(result);
		}

		@Override
		public void addFirstListener(RunListener listener) {
			decorated.addFirstListener(listener);
		}
		
		@Override
		public void addListener(RunListener listener) {
			decorated.addListener(listener);
		}
		
		@Override
		public void removeListener(RunListener listener) {
			decorated.removeListener(listener);
		}
		
		@Override
		public void fireTestIgnored(Description description) {
			decorated.fireTestIgnored(description);
		}
		
		@Override
		public void fireTestRunStarted(Description description) {
			decorated.fireTestRunStarted(description);
		}
		
		@Override
		public void fireTestStarted(Description description)
				throws StoppedByUserException {
			decorated.fireTestStarted(description);
		}
		
		@Override
		public void pleaseStop() {
			decorated.pleaseStop();
		}
		
	}
}	
