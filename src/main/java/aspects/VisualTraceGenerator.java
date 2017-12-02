package aspects;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import config.Settings;
import utils.UtilsAspect;
import utils.UtilsComputerVision;
import utils.UtilsParser;

@Aspect
public class VisualTraceGenerator {

	static WebDriver d;
	static String testFolderName;
	static String mainPage;

	static {
		nu.pattern.OpenCV.loadShared();
	}

	/* Pointcuts definition. */

	/* catch the findElement calls. */
	@Pointcut("call(* org.openqa.selenium.WebDriver.findElement(..))")
	public void logFindElementCalls(JoinPoint jp) {
	}

	/* catch the findElement execution. */
	@Pointcut("execution(* org.openqa.selenium.WebDriver.findElement(..))")
	public void catchFindElementExecutions(JoinPoint jp) {
	}

	/* catch the event calls. */
	@Pointcut("call(* org.openqa.selenium.WebElement.click()) || "
			+ "call(* org.openqa.selenium.WebElement.sendKeys(..)) || "
			+ "call(* org.openqa.selenium.WebElement.getText()) || "
			+ "call(* org.openqa.selenium.WebElement.clear()) || "
			+ "call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..))")
	public void logSeleniumCommands(JoinPoint jp) {
	}

	/* advice definition. */
	@Before("logFindElementCalls(JoinPoint)")
	public void loggingAdvice(JoinPoint jp) {

		if (Settings.aspectActive) {

			/*
			 * IMPORTANT: it is NOT possible to capture web element in this aspect, lead to
			 * infinite recursive calls.
			 */

			d = (WebDriver) jp.getTarget();

			String withinType = jp.getStaticPart().getSourceLocation().getWithinType().toString();
			String testSuiteName = UtilsParser.getTestSuiteNameFromWithinType(withinType);

			UtilsAspect.createTestFolder(Settings.outputFolder + testSuiteName);

			testFolderName = Settings.outputFolder + testSuiteName + Settings.separator
					+ jp.getStaticPart().getSourceLocation().getFileName().replace(Settings.JAVA_EXTENSION, "");

			UtilsAspect.createTestFolder(testFolderName);

		}

	}

	@Before("logSeleniumCommands(JoinPoint)")
	public void beforeEvent(JoinPoint joinPoint) {

		if (Settings.aspectActive) {

			WebElement we = null;
			Select sel = null;

			if (joinPoint.getTarget() instanceof WebElement) {
				we = (WebElement) joinPoint.getTarget();
			} else if (joinPoint.getTarget() instanceof Select) {
				sel = (Select) joinPoint.getTarget();
				we = (WebElement) sel.getOptions().get(0);
			}

			/* for each statement, get a unique name. */
			String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);

			/* for each statement, get the line number. */
			int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint);

			String screenshotBefore = testFolderName + Settings.separator + line + "-1before-" + statementName
					+ Settings.PNG_EXTENSION;
			String annotatedScreenshot = testFolderName + Settings.separator + line + "-Annotated-" + statementName
					+ Settings.PNG_EXTENSION;
			String visualLocator = testFolderName + Settings.separator + line + "-visualLocator-" + statementName
					+ Settings.PNG_EXTENSION;
			String htmlPath = testFolderName + Settings.separator + line + "-1before-" + statementName;

			mainPage = d.getWindowHandle();

			/* save the screenshot before the execution of the event. */
			UtilsComputerVision.saveScreenshot(d, screenshotBefore);

			try {

				/* save contextual based visual locator. */
				UtilsComputerVision.saveVisualLocator(d, screenshotBefore, we, visualLocator);

				/* save the annotated screenshot as well. */
				UtilsComputerVision.saveAnnotatedScreenshot(screenshotBefore, visualLocator, annotatedScreenshot);

				/* save the HTML page. */
				UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);

			} catch (IOException e) {
				e.printStackTrace();
			}

			if (Settings.VERBOSE)
				System.out.println("[LOG]\t@Before " + statementName);

		}
	}

	@After("logSeleniumCommands(JoinPoint)")
	public void afterEvent(JoinPoint joinPoint) {

		if (Settings.aspectActive) {

			/* for each statement, get a unique name and the line number. */
			String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);
			int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint);

			if (Settings.VERBOSE)
				System.out.println("[LOG]\t@After " + statementName);

			/* save the screenshot before the execution of the event. */
			String screenshotBefore = testFolderName + Settings.separator + line + "-2after-" + statementName
					+ Settings.PNG_EXTENSION;

			/* save the HTML page. */
			String htmlPath = testFolderName + Settings.separator + line + "-2after-" + statementName;

			if (UtilsComputerVision.isAlertPresent(d)) {
				return;
			} else {

				try {
					UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);
				} catch (IOException e) {
					e.printStackTrace();
				}

				UtilsComputerVision.saveScreenshot(d, screenshotBefore);
			}

		}

	}

	@AfterThrowing(pointcut = "logFindElementCalls(JoinPoint)", throwing = "exception")
	public void logAfterThrowing(Exception exception, JoinPoint joinPoint) {

		if (Settings.aspectActive) {

			/* for each statement, get a unique name. */
			String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);

			/* for each statement, get the line number. */
			int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint);

			/*
			 * get screenshot of the page before the action is executed, but after the
			 * exception has been raised.
			 */
			String screenshotBefore = testFolderName + Settings.separator + line + "-Annotated-" + statementName
					+ Settings.PNG_EXTENSION;
			UtilsComputerVision.saveScreenshot(d, screenshotBefore);

			/* save the HTML page. */
			String htmlPath = testFolderName + Settings.separator + line + "-2after-" + statementName;
			try {
				UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (Settings.VERBOSE)
				System.out.println("[LOG]\t@AfterThrowing " + statementName);

		}
	}

}
