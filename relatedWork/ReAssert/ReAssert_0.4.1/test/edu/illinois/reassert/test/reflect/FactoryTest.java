package edu.illinois.reassert.test.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtSimpleType;
import spoon.support.builder.support.CtFileFile;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.VirtualFile;

public class FactoryTest {

	private static final String INPUT_DIR = "resources/factory-tests";
	
	private Factory factory;

	@Before
	public void init() {
		factory = new Factory();
		factory.addSourcePath(INPUT_DIR);
	}
	
	@Test
	public void testCompilationUnit() {
		CompilationUnit cu = factory.CompilationUnit().create(
				INPUT_DIR + "/some/pack/TestClass.java");
		assertEquals(
				"some.pack.TestClass", 
				cu.getMainType().getQualifiedName());
	}
	
	@Test
	public void testFindCompilationUnit() {
		CompilationUnit cu = factory.CompilationUnit().find(
				"/some/pack/TestClass.java");
		assertEquals(
				"some.pack.TestClass", 
				cu.getMainType().getQualifiedName());
	}
	
	@Test
	public void testFindCompilationUnit_Nonexistent() {
		assertNull(factory.CompilationUnit().find("FAKE_FILE"));
	}
	
	@Test
	public void testType_MainType() {
		String name = "some.pack.TestClass";
		CtSimpleType<?> reflection = factory.Type().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}

	@Test
	public void testType_NonMain() { 
		String name = "some.pack.NonMainClass"; // in the TestClass compilation unit
		CtSimpleType<?> reflection = factory.Type().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}
	
	@Test
	public void testType_InnerClass() {
		String name = "some.pack.TestClass$InnerClass$InnerInnerClass";
		CtSimpleType<?> reflection = factory.Type().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}

	@Test
	public void testType_DefaultPackage_MainType() {
		String name = "DefaultPackageClass";
		CtSimpleType<?> reflection = factory.Type().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}

	@Test
	public void testType_DefaultPackage_NonMain() { 
		String name = "DefaultPackageNonMainType"; // in the DefaultPackageClass compilation unit
		CtSimpleType<?> reflection = factory.Type().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}
	
	@Test
	public void testType_DefaultPackage_InnerClass() {
		String name = "DefaultPackageClass$InnerClass$InnerInnerClass";
		CtSimpleType<?> reflection = factory.Type().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}
	
	@Test
	public void testType_Nonexistent() {
		assertNull(factory.Type().get("FAKECLASS"));
	}
	
	@Test
	public void testClass() {
		String name = "some.pack.TestClass";
		CtClass<?> reflection = factory.Class().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}

	@Test
	public void testClass_Nonexistent() {
		assertNull(factory.Class().get("FAKECLASS"));
	}
	
	@Test
	public void testClass_NotClass() {
		assertNull(factory.Class().get("some.pack.Interface"));
	}

	@Test
	@Ignore("Add functionality if needed")
	public void testInterface() {
		String name = "some.pack.Interface";
		CtInterface<?> reflection = factory.Interface().get(name);
		assertEquals(name, reflection.getQualifiedName());
	}

	@Test
	public void testInterface_Nonexistent() {
		assertNull(factory.Interface().get("FAKECLASS"));
	}
	
	@Test
	public void testInterface_NotInterface() {
		assertNull(factory.Interface().get("some.pack.TestClass"));
	}
	
	@Test
	public void testIncrementalBuilding() {
		assertEquals(0, factory.Class().getAll().size());
		factory.Class().get("some.pack.TestClass");
		assertEquals(2, factory.Class().getAll().size()); // loads both in compilation unit
		factory.Class().get("some.pack.OtherClass");
		assertEquals(3, factory.Class().getAll().size());
		
		// reloading shouldn't increase number
		factory.Class().get("some.pack.TestClass");
		assertEquals(3, factory.Class().getAll().size());
	}
	
	@Test
	public void testCaching() {
		// build TestClass on demand
		CtClass<?> class1 = factory.Class().get("some.pack.TestClass");
		// FAKECLASS does not exist, so factory will build the package
		assertNull(factory.Class().get("some.pack.FAKECLASS"));
		// make sure rebuilding did not clobber TestClass
		CtClass<?> class2 = factory.Class().get("some.pack.TestClass");
		assertSame(class1, class2);
	}
	
	@Test
	public void testAddSource() {
		Factory f = new Factory(); // use a different factory ensure correct file
		CtFileFile inFile = new CtFileFile(
				new File(INPUT_DIR, "DefaultPackageClass.java"));
		f.addSource(inFile);
		CompilationUnit cu = f.CompilationUnit().create(inFile.getPath());
		assertNotNull(cu.getOriginalSourceCode());
		assertEquals("DefaultPackageClass", cu.getMainType().getQualifiedName());
	}
	
	@Test
	public void testVirtualFile() {
		String source = 
			"class C {\n" +
			"	void m() {\n" +
			"	}\n" +
			"	\n" +
			"}";
		VirtualFile inFile = new VirtualFile("C.java", source);
		factory.addSource(inFile);
		CompilationUnit cu = factory.CompilationUnit().create(inFile.getPath());
		assertEquals(source, cu.getOriginalSourceCode());
		CtClass<?> reflection = factory.Class().get("C");
		assertEquals(source, reflection.toString());
	}

	@Test
	public void testGetAllCompilationUnits() {
		factory.CompilationUnit().create(
				INPUT_DIR + "/some/pack/TestClass.java");
		assertEquals(1, factory.CompilationUnit().getAll().size());
		factory.CompilationUnit().create(
				INPUT_DIR + "DefaultPackageClass.java");
		assertEquals(2, factory.CompilationUnit().getAll().size());
	}
}
