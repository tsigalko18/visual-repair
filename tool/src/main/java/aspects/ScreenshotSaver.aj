package main.java.aspects;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import config.Settings;
import utils.UtilsAspect;
import utils.UtilsScreenshots;

public aspect ScreenshotSaver {

	static WebDriver d;
	static String testFolderName;
	static String mainPage;
	
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
			+ "call(* org.openqa.selenium.WebElement.getText()) || "
			+ "call(* org.openqa.selenium.WebElement.clear()) || "
			+ "call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..))")
	public void logSeleniumCommands(JoinPoint jp){}
	
	// advice definition
	@Before("logFindElementCalls(JoinPoint)")
	public void loggingAdvice(JoinPoint jp) {
			
		// CANT CAPTURE WEB ELEMENT IN THIS ASPECT, LEAD TO INFINITE RECURSIVE CALLS
			
		d = (WebDriver) jp.getTarget();
		
		// for each test, create a folder
		if(Settings.INRECORDING) {
			testFolderName = Settings.referenceTestSuiteVisualTraceExecutionFolder + jp.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
		} else {
			testFolderName = Settings.testingTestSuiteVisualTraceExecutionFolder   + jp.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
		}
		UtilsAspect.createTestFolder(testFolderName);
			
	}
	
	@Before("logSeleniumCommands(JoinPoint)")
	public void beforeEvent(JoinPoint joinPoint)  {
		
		WebElement we = null;
		Select sel = null;
		
		if(joinPoint.getTarget() instanceof WebElement){
			we = (WebElement) joinPoint.getTarget();
		}
		else if(joinPoint.getTarget() instanceof Select){
			sel =  (Select) joinPoint.getTarget();
			we = (WebElement) sel.getOptions().get(0);
		}
		
		// for each statement, get a unique name in the form 
		String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);
		
		// for each statement, get the line number
		int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint); 

		String screenshotBeforeEvent 			= testFolderName + Settings.separator + line + "-1before-" + statementName + Settings.imageExtension;
		String annotatedscreenshotBeforeEvent 	= testFolderName + Settings.separator + line + "-Annotated-" + statementName + Settings.imageExtension;
		String visualLocator 					= testFolderName + Settings.separator + line + "-visualLocator-" + statementName + Settings.imageExtension;
		String htmlPath 						= testFolderName + Settings.separator + line + "-1before-" + statementName;
		
		mainPage = d.getWindowHandle();
		
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
		
		
		if(Settings.VERBOSE) System.out.println("[LOG]\t@Before " + statementName);
	}

	@After("logSeleniumCommands(JoinPoint)")
	public void afterEvent(JoinPoint joinPoint) {	
		
		// for each statement, get a unique name and the line number
		String statementName = UtilsAspect.getStatementNameFromJoinPoint(joinPoint);
		int line = UtilsAspect.getStatementLineFromJoinPoint(joinPoint); 

		if(Settings.VERBOSE) System.out.println("[LOG]\t@After " + statementName);
		
		// save the screenshot before the execution of the event
		String screenshotBeforeEvent 	= testFolderName + Settings.separator + line + "-2after-" + statementName + Settings.imageExtension;
		
		// save the HTML page
		String htmlPath 				= testFolderName + Settings.separator + line + "-2after-" + statementName;
			
		if(UtilsScreenshots.isAlertPresent(d)){
			return;
		} else {
			
			try {
				UtilsAspect.saveHTMLPage(d.getCurrentUrl(), htmlPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
					
			UtilsScreenshots.saveScreenshot(d, screenshotBeforeEvent);
		}
			
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
			e.printStackTrace();
		}
		
		if(Settings.VERBOSE) System.out.println("[LOG]\t@AfterThrowing " + statementName);
	}
	

}


