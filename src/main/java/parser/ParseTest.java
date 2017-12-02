package parser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Result;

import datatype.DriverGet;
import datatype.EnhancedAssertion;
import datatype.EnhancedSelect;
import datatype.EnhancedTestCase;
import datatype.EnhancedWebElement;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import utils.UtilsParser;
import utils.UtilsRunner;

public class ParseTest {

	static EnhancedTestCase tc;
	static String folder;

	public ParseTest(String f) {
		super();
		folder = f;
	}

	/**
	 * parse a test, get its static information and serialize a JSON file
	 * 
	 * @param clazz
	 * @return EnhancedTestCase
	 */
	public EnhancedTestCase parseAndSerialize(String clazz) {

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(new File(clazz));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		new MethodVisitor().visit(cu, clazz);

		UtilsParser.serializeTestCase(tc, clazz, folder);

		return tc;
	}

	public void setFolder(String f) {
		folder = f;
	}

	private static String getFolder() {
		return folder;
	}

	/**
	 * parse a test, get its static information and serialize a JSON file
	 * 
	 * @param clazz
	 * @return EnhancedTestCase
	 * @throws IOException
	 */
	public static EnhancedTestCase parseAndSaveToJava(EnhancedTestCase newTest, String clazz) throws IOException {

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
	 * parse a test, get its static information and serialize a JSON file
	 * 
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
		// String source = cu.toString();
		File fileMod = new File(clazz);
		// FileUtils.writeStringToFile(fileMod, source);

		Result r = UtilsRunner.runTestSuite(fileMod.getClass());

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
				String fullPath = arg.toString();

				tc = new EnhancedTestCase(m.getName(), fullPath);

				for (Statement st : m.getBody().getStmts()) {

					// driver get is managed separately
					if (st.toString().contains("driver.get(")) {

						DriverGet dg = new DriverGet();
						dg.setAction("get");
						dg.setLine(st.getBeginLine());
						dg.setValue(UtilsParser.getUrlFromDriverGet(st));

						tc.addStatementAtPosition(dg.getLine(), dg);

						// web element not assertion not select
					} else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")
							&& !st.toString().contains("new Select")) {

						EnhancedWebElement ewe = new EnhancedWebElement();
						int line = st.getBeginLine();
						ewe.setLine(line);
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
						} else if (st.toString().contains("clear")) {
							ewe.setAction("clear");
							ewe.setValue("");
						}

						try {
							/* get the screenshots. */
							ewe.setScreenshotBefore(UtilsParser.getScreenshot(className, line, "1before", getFolder()));
							ewe.setScreenshotAfter(UtilsParser.getScreenshot(className, line, "2after", getFolder()));
							ewe.setVisualLocator(
									UtilsParser.getScreenshot(className, line, "visualLocator", getFolder()));

							ewe.setAnnotatedScreenshot(
									UtilsParser.getScreenshot(className, line, "Annotated", getFolder()));

							/* get the DOMs. */
							ewe.setDomBefore(UtilsParser.getHTMLDOMfile(className, line, "1before", "", getFolder()));
							ewe.setDomAfter(UtilsParser.getHTMLDOMfile(className, line, "2after", "", getFolder()));
						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(line, ewe);

					}
					// select
					else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")
							&& st.toString().contains("new Select")) {

						EnhancedSelect esl = new EnhancedSelect();
						int line = st.getBeginLine();
						esl.setLine(line);
						esl.setDomLocator(UtilsParser.getDomLocator(st));

						if (st.toString().contains("selectByVisibleText")) {
							esl.setAction("selectByVisibleText");
							esl.setValue(UtilsParser.getValueFromSelect(st));
						} else if (st.toString().contains("selectByIndex")) {
							esl.setAction("selectByIndex");
							esl.setValue(UtilsParser.getValueFromSelect(st));
						} else if (st.toString().contains("selectByValue")) {
							esl.setAction("selectByValue");
							esl.setValue(UtilsParser.getValueFromSelect(st));
						}

						try {
							// get the screenshots
							esl.setScreenshotBefore(UtilsParser.getScreenshot(className, line, "1before", getFolder()));
							esl.setScreenshotAfter(UtilsParser.getScreenshot(className, line, "2after", getFolder()));
							esl.setVisualLocator(
									UtilsParser.getScreenshot(className, line, "visualLocator", getFolder()));

							esl.setAnnotatedScreenshot(
									UtilsParser.getScreenshot(className, line, "Annotated", getFolder()));

							// get the DOMs
							esl.setDomBefore(UtilsParser.getHTMLDOMfile(className, line, "1before", "", getFolder()));
							esl.setDomAfter(UtilsParser.getHTMLDOMfile(className, line, "2after", "", getFolder()));
						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(line, esl);
					}
					// assertion
					else if (st.toString().contains("driver.findElement(") && st.toString().contains("assert")) {

						EnhancedAssertion ea = new EnhancedAssertion();
						int line = st.getBeginLine();
						ea.setAssertion(UtilsParser.getAssertion(st));
						ea.setPredicate(UtilsParser.getPredicate(st));

						if (st.toString().contains("getText")) {
							ea.setAction("getText");
							ea.setValue(UtilsParser.getValueFromAssertion(st));
						} else {
							System.err.println("[LOG]\tAnalysing an assertion with no getText()");
						}

						ea.setLine(line);
						ea.setDomLocator(UtilsParser.getDomLocator(st));

						try {
							// get the screenshots
							ea.setScreenshotBefore(UtilsParser.getScreenshot(className, line, "1before", getFolder()));
							ea.setScreenshotAfter(UtilsParser.getScreenshot(className, line, "2after", getFolder()));
							ea.setVisualLocator(
									UtilsParser.getScreenshot(className, line, "visualLocator", getFolder()));

							ea.setAnnotatedScreenshot(
									UtilsParser.getScreenshot(className, line, "Annotated", getFolder()));

							// get the DOMs
							ea.setDomBefore(UtilsParser.getHTMLDOMfile(className, line, "1before", "", getFolder()));
							ea.setDomAfter(UtilsParser.getHTMLDOMfile(className, line, "2after", "", getFolder()));
						} catch (Exception e) {
							e.printStackTrace();
						}

						tc.addStatementAtPosition(line, ea);

					}
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
