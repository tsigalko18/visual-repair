package edu.illinois.reassert.testutil;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import edu.illinois.reassert.TestFixer;

/**
 * JUnit runner that ignores an inner class when not run by ReAssert (in particular,
 * {@link TestFixer}).
 * Used to prevent inner classes used for testing from appearing in Eclipse's JUnit
 * runner. @{@link Ignore} will not work because ReAssert needs to be able to run
 * the tests in the class.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class IgnoredInner extends Runner {

	private Class<?> testClass;
	private Runner decorated;

	public IgnoredInner(Class<?> testClass) throws InitializationError {
		this.testClass = testClass;
		if (TestCase.class.isAssignableFrom(testClass)) {
			decorated = new JUnit38ClassRunner(testClass);
		}
		else {
			decorated = new BlockJUnit4ClassRunner(testClass);
		}
	}

	@Override
	public Description getDescription() {
		return Description.createSuiteDescription(testClass);
	}

	@Override
	public void run(RunNotifier notifier) {
		if (isRunByReAssert()) {
			decorated.run(notifier);
		}
		else {
			notifier.fireTestIgnored(getDescription());
		}
	}

	private boolean isRunByReAssert() {
		StackTraceElement[] stack = new Throwable().getStackTrace();
		for (StackTraceElement frame : stack) {
			if (TestFixer.class.getName().equals(frame.getClassName())) {
				return true;
			}
		}
		return false;
	}

}

