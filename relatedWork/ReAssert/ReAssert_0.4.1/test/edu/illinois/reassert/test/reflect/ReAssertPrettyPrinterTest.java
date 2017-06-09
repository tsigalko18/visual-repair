package edu.illinois.reassert.test.reflect;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.ImportScanner;
import edu.illinois.reassert.reflect.ReAssertPrettyPrinter;
import edu.illinois.reassert.reflect.StaticImportScanner;

public class ReAssertPrettyPrinterTest {

	private static final String TEST_DIR = "test";
	private static final String SAMPLE_DIR = "resources/sample-tests";
	
	private Factory factory;
	private ReAssertPrettyPrinter printer;
	
	@Before
	public void init() throws IOException {
		factory = new Factory();
		factory.addSourcePath(TEST_DIR);
		factory.addSourcePath(SAMPLE_DIR);
		printer = new ReAssertPrettyPrinter(
				factory.getEnvironment());
	}
	
	/**
	 * Pretty-prints the compilation unit and verifies that it is
	 * unchanged from the input file
	 */
	@Test
	public void testPrintCompilationUnit() throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException {
		CtElement testClass = factory.Class().get("PrettyPrinterTest");
		CompilationUnit cu = testClass.getPosition().getCompilationUnit();
		
		ImportScanner scanner = new ImportScanner();
		Collection<CtTypeReference<?>> imports = scanner.makeImports(cu);
		// print fully-qualified calendar
		imports.remove(factory.Type().createReference(Calendar.class));
		
		StaticImportScanner siScanner = new StaticImportScanner();
		siScanner.importStaticFrom(factory.Type().createReference(org.junit.Assert.class));
		Collection<CtExecutableReference<?>> staticImports = siScanner.makeStaticImports(cu);
		
		printer.addImports(imports);
		printer.addStaticImports(staticImports);
		printer.calculate(cu, cu.getDeclaredTypes());
		assertEquals(
				FileUtils.readFileToString(cu.getFile()),
				printer.toString());
	}

	@Test
	public void testPrintMultilineStrings() throws SecurityException, NoSuchMethodException {
		CtExecutable<?> elem = factory.Method().createReference(
				getClass().getMethod("multiLine", String.class))
				.getDeclaration();
		
		String printed = printer.print(elem);
		assertEquals(
				"public void multiLine(String s) {\n" +
				"	multiLine(\"\");\n" +
				"	multiLine(\"\\n\");\n" +
				"	multiLine(\"\\n\" +\n" +
				"		\"\\n\");\n" +
				"	multiLine(\"multiline\\n\" +\n" +
				"		\"string\");\n" +
				"	multiLine(\"multiline\\n\" +\n" +
				"		\"string\\n\");\n" +
				"}", 
				printed);
	}
	public void multiLine(String s) {
		multiLine("");
		multiLine("\n");
		multiLine("\n\n");
		multiLine("multiline\nstring");
		multiLine("multiline\nstring\n");
	}

	@Test
	public void testFieldAccesses() throws SecurityException, NoSuchMethodException {
		CtElement testClass = factory.Class().get(getClass());
		CompilationUnit cu = testClass.getPosition().getCompilationUnit();
		
		ImportScanner scanner = new ImportScanner();
		printer.addImports(scanner.makeImports(cu));
		
		CtExecutable<?> elem = factory.Method().createReference(
				getClass().getMethod("fieldAccesses"))
				.getDeclaration();
		
		String printed = printer.print(elem);
		assertEquals(
				"public void fieldAccesses() {\n" +
				"	i = 0;\n" +
				"	this.i = 0;\n" +
				"	this.i = 0;\n" +
				"	new ReAssertPrettyPrinterTest().i = 0;\n" +
				"	j = 0;\n" +
				"	this.j = 0;\n" +
				"	this.j = 0;\n" +
				"	new ReAssertPrettyPrinterTest().j = 0;\n" +
				"	j = 0;\n" +
				"	new Other().i = 0;\n" +
				"	new Other().j = 0;\n" +
				"	Other.j = 0;\n" +
				"}", 
				printed);
	}
	int i = 0;
	static int j = 0;
	static class Other {
		int i = 0;
		static int j = 0;
	}
	public void fieldAccesses() {
		i = 0;
		this.i = 0;
		ReAssertPrettyPrinterTest.this.i = 0;
		new ReAssertPrettyPrinterTest().i = 0;
		j = 0;
		this.j = 0;
		ReAssertPrettyPrinterTest.this.j = 0;
		new ReAssertPrettyPrinterTest().j = 0;
		ReAssertPrettyPrinterTest.j = 0;
		new Other().i = 0;
		new Other().j = 0;
		Other.j = 0;
	}

	
}

