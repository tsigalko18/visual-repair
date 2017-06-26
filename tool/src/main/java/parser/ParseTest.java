package main.java.parser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Result;

import main.java.config.Settings;
import main.java.datatype.DriverGet;
import main.java.datatype.EnhancedAssertion;
import main.java.datatype.EnhancedTestCase;
import main.java.datatype.EnhancedWebElement;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import main.java.utils.UtilsParser;
import main.java.utils.UtilsRepair;

public class ParseTest {

	static EnhancedTestCase tc;

	/**
	 * parse a test, get its static information and
	 * serialize a JSON file
	 * @param clazz
	 * @return EnhancedTestCase
	 */
	public static EnhancedTestCase parse(String clazz) {

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(clazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		new MethodVisitor().visit(cu, clazz);

		UtilsParser.serializeTestCase(tc, clazz);
			
		return tc;
	}
	
	/**
	 * parse a test, get its static information and
	 * serialize a JSON file
	 * @param clazz
	 * @return EnhancedTestCase
	 * @throws IOException 
	 */
	public static EnhancedTestCase saveToJava(EnhancedTestCase newTest, String clazz) throws IOException {

		tc = newTest;
		
		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(clazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		// replace body method with that present in tc
		new ChangeMethodMethodVisitor().visit(cu, clazz);

		// save back to java
		String source = cu.toString();
		File fileMod = new File(clazz);
		FileUtils.writeStringToFile(fileMod, source);
		
		return tc;
	}
	
	/**
	 * parse a test, get its static information and
	 * serialize a JSON file
	 * @param clazz
	 * @return EnhancedTestCase
	 * @throws IOException 
	 */
	public static Result runTest(EnhancedTestCase newTest, String clazz) throws IOException {

		tc = newTest;
		
		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(clazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		// replace body method with that present in tc
		new ChangeMethodMethodVisitor().visit(cu, clazz);

		// save back to java
//		String source = cu.toString();
		File fileMod = new File(clazz);
//		FileUtils.writeStringToFile(fileMod, source);
		
		Result r = UtilsRepair.runTestSuite(fileMod.getClass());
	
		return r;
	}


	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class MethodVisitor extends VoidVisitorAdapter<Object> {
		@Override
		public void visit(MethodDeclaration m, Object arg) {

			if (m.getAnnotations() != null && m.getAnnotations().get(0).getName().getName().equals("Test")) {

				String className = UtilsParser.getClassNameFromPath((String) arg);
				String fullPath = System.getProperty("user.dir") + Settings.separator + arg;
				
				tc = new EnhancedTestCase(m.getName(), fullPath);
				
				for (Statement st : m.getBody().getStmts()) {

					// driver get is managed separately
					if (st.toString().contains("driver.get(")) {

						DriverGet dg = new DriverGet();
						dg.setAction("get");
						dg.setLine(st.getBeginLine());
						dg.setValue(UtilsParser.getUrlFromDriverGet(st));
						
						tc.addStatement(dg.getLine(), dg);

					// web element not assertion
					} else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")) {

						EnhancedWebElement ewe = new EnhancedWebElement();
						ewe.setLine(st.getBeginLine());
						ewe.setDomLocator(UtilsParser.getDomLocator(st));
						
						if (st.toString().contains("click()")) {
							ewe.setAction("click");
							ewe.setValue("");
						} else if (st.toString().contains("sendKeys")) {
							ewe.setAction("sendKeys");
							ewe.setValue(UtilsParser.getValueFromSendKeys(st));
						} else if (st.toString().contains("getText")) {
							ewe.setAction("getText");
							ewe.setValue("");
						}
						
						try {
							// get the screenshots
							ewe.setScreenshotBefore(UtilsParser.getScreenshot(className, st.getBeginLine(), "1before"));
							ewe.setScreenshotAfter(UtilsParser.getScreenshot(className, st.getBeginLine(), "2after"));
							ewe.setVisualLocator(UtilsParser.getScreenshot(className, st.getBeginLine(), "visualLocator"));
							ewe.setAnnotatedScreenshot(UtilsParser.getScreenshot(className, st.getBeginLine(), "Annotated"));
							
							// get the DOMs
							ewe.setDomBefore(UtilsParser.getHTMLDOMfile(className, st.getBeginLine(), "1before", ""));
							ewe.setDomAfter(UtilsParser.getHTMLDOMfile(className, st.getBeginLine(), "2after", ""));
						} catch (Exception e) {
							e.printStackTrace();
						} 
						
						tc.addStatement(ewe.getLine(), ewe);
						
					}
					// assertion 
					else if (st.toString().contains("driver.findElement(") && st.toString().contains("assert")) {
						
						EnhancedAssertion ea = new EnhancedAssertion();
					
						ea.setAssertion(UtilsParser.getAssertion(st));
						ea.setPredicate(UtilsParser.getPredicate(st));
						
						if(st.toString().contains("getText")){
							ea.setAction("getText");
							ea.setValue("");
						} else {
							System.err.println("[LOG]\tAnalysing an assertion with no getText()");
						}
						
						ea.setLine(st.getBeginLine());
						ea.setDomLocator(UtilsParser.getDomLocator(st));
						
						try {
							// get the screenshots
							ea.setScreenshotBefore(UtilsParser.getScreenshot(className, st.getBeginLine(), "1before"));
							ea.setScreenshotAfter(UtilsParser.getScreenshot(className, st.getBeginLine(), "2after"));
							ea.setVisualLocator(UtilsParser.getScreenshot(className, st.getBeginLine(), "visualLocator"));
							ea.setAnnotatedScreenshot(UtilsParser.getScreenshot(className, st.getBeginLine(), "Annotated"));
							
							// get the DOMs
							ea.setDomBefore(UtilsParser.getHTMLDOMfile(className, st.getBeginLine(), "1before", ""));
							ea.setDomAfter(UtilsParser.getHTMLDOMfile(className, st.getBeginLine(), "2after", ""));
						} catch (Exception e) {
							e.printStackTrace();
						} 
						
						tc.addStatement(ea.getLine(), ea);
						
					// select
					} else if (st.toString().contains("Select")) {
						// TODO: to manage
					}

//					System.out.println(st.getBeginLine() + ": " + st);
				}
			}
		}
	}
	
	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class ChangeMethodMethodVisitor extends VoidVisitorAdapter<Object> {
		@Override
		public void visit(MethodDeclaration m, Object arg) {

			if (m.getAnnotations() != null && m.getAnnotations().get(0).getName().getName().equals("Test") 
					&& m.getName().equals(tc.getName())) {

				BlockStmt newBlockStmt = new BlockStmt();
				
				for (Integer i : tc.getStatements().keySet()) {
					ASTHelper.addStmt(newBlockStmt, new NameExpr(tc.getStatements().get(i).toString()));
				}
				
				m.setBody(newBlockStmt);
				
			}
		}
	}
	
}
