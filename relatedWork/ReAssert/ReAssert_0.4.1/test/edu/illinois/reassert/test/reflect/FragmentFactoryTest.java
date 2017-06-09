package edu.illinois.reassert.test.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.FragmentDrivenJavaPrettyPrinter;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.ReAssertPrettyPrinter;
import edu.illinois.reassert.reflect.VirtualFile;

public class FragmentFactoryTest {

	private Factory factory;

	@Before
	public void init() {
		this.factory = new Factory();
	}
	
	@Test
	public void testReplaceInvocation() {
		testReplaceStatement("assertEquals(1, 2);", "m();");		
	}
	
	@Test
	public void testReplaceWithInvocation() {
		testReplaceStatement("m();", "assertEquals(1, 2);");
	}
	
	@Test
	public void testReplaceLocalVariable_Uninitialized() {
		testReplaceStatement("int i;", "m();");
	}
	@Test
	public void testReplaceLocalVariable_Primitive() {
		testReplaceStatement("int i = 0;", "m();");
	}
	@Test
	public void testReplaceLocalVariable_Array() {
		testReplaceStatement("int[] i = new int[] {};", "m();");
	}
	@Test
	public void testReplaceLocalVariable_ArrayArray() {
		testReplaceStatement("int[][] i = null;", "m();");
	}
	@Test
	public void testReplaceLocalVariable_RefType() {
		testReplaceStatement("Integer i = 0;", "m();");
	}
	@Test
	public void testReplaceLocalVariable_Generic() {
		testReplaceStatement("Iterable<String> i = null;", "m();");
	}
	@Test
	public void testReplaceLocalVariable_Generics() {
		testReplaceStatement("Map<String, String> i = null;", "m();");
	}
	@Test
	public void testReplaceWithLocalVariable() {
		testReplaceStatement("m();", "int i = 0;");
	}
	@Test
	public void testReplaceFinalLocalVariable() {
		testReplaceStatement("final int i = 0;", "m();");
	}
	@Test
	public void testReplaceWithFinalLocalVariable() {
		testReplaceStatement("m();", "final int i = 0;");
	}

	@Test
	public void testReplaceAssignment() {
		testReplaceStatement("i = 0;", "m();");
	}
	@Test
	public void testReplaceWithAssignment() {
		testReplaceStatement("m();", "i = 0;");
	}

	@Test
	public void testReplaceBlock() {
		testReplaceStatement(
				"{ m(); }", 
				"{\n" +
				"			m();\n" +
				"			m();\n" +
				"		}");
	}
	
	@Test
	@Ignore("Not currently needed (Aug 4 2009)")
	public void testReplaceBlockWithStatement() {
		testReplaceStatement("{ m(); }", "m();");
	}
	
	@Test
	public void testReplaceStatementWithBlock() {
		testReplaceStatement("m();", 
				"{\n" +
				"			m();\n" +
				"		}");
	}

	@Test
	public void testReplaceBlockWithBlock() {
		testReplaceStatement("{ { m(); } }",
				"{\n" +
				"			m();\n" +
				"		}");
	}

	@Test
	public void testReplaceTry() {
		testReplaceStatement(
				"try {\n" +
				"			m();\n" +
				"		}\n" +
				"		catch (Exception e) {\n" +
				"			fail();\n" +
				"		}",
				"try {\n" +
				"			m();\n" +
				"		}\n" +
				"		catch (Exception e) {\n" +
				"			fail();\n" +
				"		}");
	}

	@Test
	@Ignore("Not currently needed (Aug 4 2009)")
	public void testReplaceTryWithStatement() {
		testReplaceStatement(
				"try {\n" +
				"			m();\n" +
				"		}\n" +
				"		catch (Exception e) {\n" +
				"			fail();\n" +
				"		}",
				"m();");
	}
	
	@Test
	public void testReplaceStatementWithTry() {
		testReplaceStatement(
				"m();", 
				"try {\n" +
				"			m();\n" +
				"		}\n" +
				"		catch (Exception e) {\n" +
				"			fail();\n" +
				"		}");
	}
	
	// TODO: more testReplace* as needed

	@Test
	public void testLineNumbers_Unchanged() {
		CompilationUnit cu = testReplaceStatement(
				"m();", "m();");
		CtMethod<?> m = ((CtClass<?>) cu.getMainType()).getMethod("m");
		assertEquals(
				"void m() 5-9:{\n" +
				"	6-6:m();\n" +
				"	7-7:m();\n" +
				"	8-8:m();\n" +
				"}", new LinePrinter().print(m));
	}

	@Test
	public void testLineNumbers_Increase() {
		CompilationUnit cu = testReplaceStatement(
				"m();", 
				"{\n" +
				"			m();\n" +
				"		}");
		CtMethod<?> m = ((CtClass<?>) cu.getMainType()).getMethod("m");
		assertEquals(
				"void m() 5-13:{\n" +
				"	6-6:m();\n" +
				"	7-9:{\n" +
				"		m();\n" +
				"	}\n" +
				"	10-12:{\n" +
				"		11-11:m();\n" +
				"	}\n" +
				"}", new LinePrinter().print(m));
	}

	@Test
	public void testLineNumbers_Decrease() {
		CompilationUnit cu = testReplaceStatement(
				"{\n" +
				"			m();\n" +
				"			m();\n" +
				"		}",
				"{\n" +
				"			m();\n" +
				"		}");
		CtMethod<?> m = ((CtClass<?>) cu.getMainType()).getMethod("m");
		assertEquals(
				"void m() 5-13:{\n" +
				"	6-6:m();\n" +
				"	7-9:{\n" +
				"		m();\n" +
				"	}\n" +
				"	10-12:{\n" +
				"		11-11:m();\n" +
				"	}\n" +
				"}", new LinePrinter().print(m));
	}

	public class LinePrinter extends ReAssertPrettyPrinter {
		public LinePrinter() {
			super(factory.getEnvironment());
		}
		@Override
		protected void enterCtStatement(CtStatement s) {
			SourcePosition p = s.getPosition();
			if (p != null) {
				write(String.valueOf(p.getLine()));
				write("-");
				write(String.valueOf(p.getEndLine()));
				write(":");
			}
			super.enterCtStatement(s);
		}
	}
	
	/**
	 * Test helper that replaces the given statement with another
	 * and verifies that the compilation unit is printed correctly.
	 */
	@SuppressWarnings("unchecked")
	private CompilationUnit testReplaceStatement(String original, String replacement) {
		factory.addSource(new VirtualFile("C.java",
				"import static org.junit.Assert.*;\n" + 
				"import java.util.*;\n" + 
				"class C {\n" +
				"	int i = 0;\n" +
				"	void m() {\n" +
				"		m();\n" +
				"		" + original + "\n" +
				"		" + replacement + "\n" +
				"	} \n" +
				"}"));
		CompilationUnit cu = factory.CompilationUnit().create("C.java");
		CtMethod m = factory.Class().get("C").getMethod("m");
		CtBlock<?> body = m.getBody();
		
		CtStatement oldStatement = body.getStatements().get(1);
		CtStatement newStatement = factory.Core().clone(body.getStatements().get(2));
		newStatement.setPositions(null);
		SourceCodeFragment frag = factory.Fragment().replace(oldStatement, newStatement);

		assertNotNull(frag);	
		assertEquals(3, body.getStatements().size());
		assertEquals(newStatement, body.getStatements().get(1));
		
		String source = printWithFragments(cu);
		
		assertEquals(
				"import static org.junit.Assert.*;\n" + 
				"import java.util.*;\n" + 
				"class C {\n" +
				"	int i = 0;\n" +
				"	void m() {\n" +
				"		m();\n" +
				"		" + replacement + "\n" +
				"		" + replacement + "\n" +
				"	} \n" +
				"}", 
				source);
		
		return cu;
	}
	
	private String printWithFragments(CompilationUnit cu) {
		FragmentDrivenJavaPrettyPrinter pp;
		try {
			pp = new FragmentDrivenJavaPrettyPrinter(
					factory.getEnvironment(), cu);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return pp.getResult().toString();
	}
	
	@Test
	public void testReplaceInt() {
		testReplaceExpression("1", "2");
	}

	@Test
	public void testReplaceIntString() {
		testReplaceExpression("1", "\"bar\"");
	}

	@Test
	public void testReplaceStringInt() {
		testReplaceExpression("\"bar\"", "1");
	}
	
	@Test
	public void testReplaceIntExpr() {
		testReplaceExpression("1 + 1", "2 + 2");
	}

	@Test
	public void testReplaceString() {
		testReplaceExpression("\"foo\"", "\"barbaz\"");
	}

	@Test
	public void testReplaceStringExpr() {
		testReplaceExpression(
				"\"multi\" + \"part\"", 
				"\"multi2\" + \"part2\"");
	}
		
	@Test 
	public void testReplaceAssignmentExpr() {
		testReplaceExpression(
				"o = 1",
				"o = f = 2");
	}
	
	@Test 
	public void testReplaceConditional() {
		testReplaceExpression(
				"true ? 1 : 2",
				"false ? \"foo\" : \"bar\"");
	}
		
	@Test
	public void testReplaceNewArray() {
		testReplaceExpression(
				"new int[]{1, 2, 3}",
				"new Object[]{ null, null }");
	}

	@Test
	public void testArrayAccess() {
		testReplaceExpression(
				"new int[]{1, 2, 3}[0]",
				"new Object[]{ null, null }[0]");
	}

	@Test
	public void testFieldAccess() {
		testReplaceExpression(
				"f",
				"null"); // only the old matters
	}
	
	@Test
	public void testReplaceInvocationExpr() {
		testReplaceExpression(
				"equals(this)",
				"hashCode()");
	}

	@Test
	public void testReplaceNewClass() {
		testReplaceExpression(
				"new Object()",
				"new C()");
	}
	
	@Test
	public void testReplacePostIncrement() {
		testReplaceExpression(
				"f++",
				"null"); // only the old matters
	}

	@Test
	public void testReplacePreIncrement() {
		testReplaceExpression(
				"++f",
				"null"); // only the old matters
	}
	
	@Test
	public void testReplaceNegate() {
		testReplaceExpression(
				"-f",
				"null"); // only the old matters
	}

	@Test
	public void testReplaceExpressionStatmentWithPureExpression() {
		testReplaceExpression(
				"equals(this)",
				"1");
	}
	
	/**
	 * Test helper that replaces the given expression with another
	 * and verifies that the compilation unit is printed correctly.
	 */
	private void testReplaceExpression(String original, String replace) {
		factory.addSource(new VirtualFile("C.java", 
				"class C {\n" +
				"   int f = 0;\n" +
				"	void m() {\n" +
				"		Object o = " + original + ";\n" +
				"		Object r = " + replace + ";\n" +
				"	}\n" +
				"}"));
		CompilationUnit cu = factory.CompilationUnit().create("C.java");
		CtMethod<?> m = factory.Class().get("C").getMethod("m");
		List<CtStatement> body = m.getBody().getStatements();
		CtExpression<?> oldExpr = ((CtVariable<?>) body.get(0)).getDefaultExpression();
		CtExpression<?> newExpr = factory.Core().clone(
				((CtVariable<?>) body.get(1)).getDefaultExpression());
		
		factory.Fragment().replace(oldExpr, newExpr);
		
		assertEquals(
				"class C {\n" +
				"   int f = 0;\n" +
				"	void m() {\n" +
				"		Object o = " + replace + ";\n" +
				"		Object r = " + replace + ";\n" +
				"	}\n" +
				"}", printWithFragments(cu));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveInvocation() {
		factory.addSource(new VirtualFile("C.java", 
				"import static org.junit.Assert.*;\n" +
				"class C {\n" +
				"	void m() {\n" +
				"		assertEquals(1,2);\n" +
				"		assertEquals(3, 4);\n" +
				"		assertEquals (5 , 6  );\n" +
				"	}\n" +
				"}"));
		CtMethod m = factory.Class().get("C").getMethod("m");
		CtBlock<?> body = m.getBody();
		CompilationUnit cu = factory.CompilationUnit().create("C.java");
		
		// remove middle
		factory.Fragment().remove(body.getStatements().get(1));
		assertEquals("import static org.junit.Assert.*;\n" +
				"class C {\n" +
				"	void m() {\n" +
				"		assertEquals(1,2);\n" +
				"		\n" +
				"		assertEquals (5 , 6  );\n" +
				"	}\n" +
				"}", printWithFragments(cu));
		// remove end
		factory.Fragment().remove(body.getStatements().get(1));
		assertEquals("import static org.junit.Assert.*;\n" +
				"class C {\n" +
				"	void m() {\n" +
				"		assertEquals(1,2);\n" +
				"		\n" +
				"		\n" +
				"	}\n" +
				"}", printWithFragments(cu));
		// remove start
		factory.Fragment().remove(body.getStatements().get(0));
		assertEquals("import static org.junit.Assert.*;\n" +
				"class C {\n" +
				"	void m() {\n" +
				"		\n" +
				"		\n" +
				"		\n" +
				"	}\n" +
				"}", printWithFragments(cu));
	}
	
}
