package edu.illinois.reassert.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method whose body contains the fix for a failed method.
 * Used for testing.
 * @see Unfixable
 * @see FixChecker
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Fix {

	/**
	 * @return the name of the test that this is a fix for
	 */
	String value();

}
