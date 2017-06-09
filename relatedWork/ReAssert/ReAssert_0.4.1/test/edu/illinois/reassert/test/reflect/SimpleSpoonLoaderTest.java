package edu.illinois.reassert.test.reflect;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import spoon.reflect.declaration.CtField;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.SimpleSpoonLoader;
import edu.illinois.reassert.testutil.PackageAccessTest;


public class SimpleSpoonLoaderTest {
	private static final String TEST_DIR = "test";
	private SimpleSpoonLoader loader;
	private Factory factory;
	
	@Before
	public void init() throws IOException {
		factory = new Factory();
		factory.addSourcePath(TEST_DIR);
		loader = new SimpleSpoonLoader(factory);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadModifyReload() throws Exception {
		Field targetField = ClassToModify.class.getField("field");
		
		// modify
		CtField<?> toModify = factory.Field().createReference(targetField).getDeclaration();
		factory.Fragment().replace(
				toModify.getDefaultExpression(),
				factory.Code().createLiteral("MODIFIED"));
		
		// recompile
		Class modified = loader.load(ClassToModify.class.getName());
		// check recompilation by getting field value
		assertEquals("MODIFIED", modified.getFields()[0].get(null));

		// modify again
		toModify = factory.Field().createReference(targetField).getDeclaration();
		factory.Fragment().replace(
				toModify.getDefaultExpression(),
				factory.Code().createLiteral("MODIFIED2"));

		// recompile again
		modified = loader.load(ClassToModify.class.getName());
		// check recompilation
		assertEquals("MODIFIED2", modified.getFields()[0].get(null));
	}

	public static class ClassToModify {
		public static String field = "ORIGINAL";
	}
	
	/**
	 * Test for Darko's bug.  
	 * Requires that the test class access a non-final, package-level field
	 * declared in a different compilation unit.
	 */
	@Test
	public void testPackageAccess() throws Throwable {
		Class<?> loaded = loader.load(PackageAccessTest.class.getName());
		// invocation should not throw anything
		loaded.getMethod("testPass").invoke(loaded.newInstance());
	}
}
