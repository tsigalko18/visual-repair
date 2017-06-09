package edu.illinois.reassert.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation attached to a test method that indicates a particular failing test cannot be fixed
 * @see Fix
 * @see FixChecker
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Unfixable {

}
