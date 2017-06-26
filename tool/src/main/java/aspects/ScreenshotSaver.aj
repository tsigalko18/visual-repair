package main.java.aspects;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import main.java.config.Settings;
import main.java.utils.UtilsAspect;
import main.java.utils.UtilsScreenshots;

public aspect ScreenshotSaver {

	static WebDriver d;
	static String testFolderName;
	
	// pointcuts definition
	
	// catch the findElement calls
	@Pointcut("call(* org.openqa.selenium.WebDriver.findElement(..))")
	public void logFindElementCalls(JoinPoint jp){}
		
	// catch the findElement execution
	@Pointcut("execution(* org.openqa.selenium.WebDriver.findElement(..))")
	public void catchFindElementExecutions(JoinPoint jp){}
		
	// catch the event calls
	@Pointcut("call(* org.openqa.selenium.WebElement.click()) || "
			+ "call(* org.openqa.selenium.WebElement.sendKeys(..)) || "
			+ "call(* org.openqa.selenium.WebElement.getText())")
	public void logSeleniumCommands(JoinPoint jp){}
	
	// advice definition
	@Before("logFindElementCalls(JoinPoint)")
	public void loggingAdvice(JoinPoint jp) {
			
		// CANT CAPTURE WEB ELEMENT IN THIS ASPECT, LEAD TO INFINITE RECURSIVE CALLS
			
		d = (WebDriver) jp.getTarget();
		
		// for each test, create a folder
		testFolderName = Settings.testSuiteFolder + jp.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
		UtilsAspect.createTestFolder(testFolderName);
			
	}
	
	@Before("logSeleniumCommands(JoinPoint)")
	public void beforeEvent(JoinPoint joinPoint)  {
		
		// retrieve the Selenium WebElement
		WebElement we = (WebElement) joinPoint.getTarget();
		
		// for each statement, get a unique name in the form 
		String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);
		
		// for each statement, get the line number
		int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint); 

		String screenshotBeforeEvent = testFolderName + Settings.separator + line + "-1before-" + statementName + Settings.imageExtension;
		String annotatedscreenshotBeforeEvent = testFolderName + Settings.separator + line + "-Annotated-" + statementName + Settings.imageExtension;
		String visualLocator = testFolderName + Settings.separator + line + "-visualLocator-" + statementName + Settings.imageExtension;
		String htmlPath = testFolderName + Settings.separator + line + "-1before-" + statementName;
		
		// save the screenshot before the execution of the event
		UtilsScreenshots.saveScreenshot(d, screenshotBeforeEvent);
		
		// save the visual locator
		UtilsScreenshots.saveVisualLocator(d, screenshotBeforeEvent, we, visualLocator);
//		UtilsScreenshots.saveVisualCrop(d, screenshotBeforeEvent, we, visualLocator);
		
		
		try {
			// get the annotated screenshot as well
			UtilsScreenshots.saveAnnotatedScreenshot(screenshotBeforeEvent, visualLocator, annotatedscreenshotBeforeEvent);
			
			// save the HTML page
			UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(Settings.verbose) System.out.println("[LOG]\t@Before " + statementName);
	}

	@After("logSeleniumCommands(JoinPoint)")
	public void afterEvent(JoinPoint joinPoint) {	
		
		// for each statement, get a unique name and the line number
		String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);
		int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint); 

		// save the screenshot before the execution of the event
		String screenshotBeforeEvent = testFolderName + Settings.separator + line + "-2after-" + statementName + Settings.imageExtension;
		UtilsScreenshots.saveScreenshot(d, screenshotBeforeEvent);
		
		// save the HTML page
		String htmlPath = testFolderName + Settings.separator + line + "-2after-" + statementName;
		try {
			UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Settings.verbose) System.out.println("[LOG]\t@After " + statementName);
		
	}
	
	@AfterThrowing(pointcut = "logFindElementCalls(JoinPoint)", throwing="exception")
	public void logAfterThrowing(Exception exception, JoinPoint joinPoint) {
		
		// for each statement, get a unique name in the form 
		String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint); 
		
		// for each statement, get the line number
		int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint); 

		// get screenshot of the page before the action is executed, but after the exception has been raised
		String screenshotBeforeEvent = testFolderName + Settings.separator + line + "-Annotated-" + statementName + Settings.imageExtension;
		UtilsScreenshots.saveScreenshot(d, screenshotBeforeEvent);
		
		// save the HTML page
		String htmlPath = testFolderName + Settings.separator + line + "-2after-" + statementName;
		try {
			UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Settings.verbose) System.out.println("[LOG]\t@AfterThrowing " + statementName);
	}
	

}


