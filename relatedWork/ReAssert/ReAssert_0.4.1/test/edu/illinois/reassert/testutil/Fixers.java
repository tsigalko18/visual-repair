package edu.illinois.reassert.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.illinois.reassert.FixStrategy;


/**
 * Annotation used to define the {@link FixStrategy}s that {@link FixChecker} 
 * should use to fix tests in a unit test.
 * 
 * @see FixChecker
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Fixers {

	Class<? extends FixStrategy>[] value();

}
