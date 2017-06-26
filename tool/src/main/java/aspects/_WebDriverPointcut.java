package main.java.aspects;
//package aspects;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Date;
//
//import org.apache.commons.io.FileUtils;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.openqa.selenium.By;
//import org.openqa.selenium.NoSuchElementException;
//import org.openqa.selenium.OutputType;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.TakesScreenshot;
//
//@Aspect
//public class WebDriverPointcut {
//	
//	WebDriver d;
//	String path = "/Users/astocco/Desktop";
//	
//	// pointcuts definition
//	
//	// catch the findElement calls
//	@Pointcut("call(* org.openqa.selenium.WebDriver.findElement(..))")
//	public void logElementLocalization(JoinPoint jp){}
//	
//	// catch the findElement execution
//	@Pointcut("execution(* org.openqa.selenium.WebDriver.findElement(..))")
//	public void logExecutionElementLocalization(JoinPoint jp){}
//	
//	// catch the event calls
//	@Pointcut("call(* org.openqa.selenium.WebElement.click()) || "
//			+ "call(* org.openqa.selenium.WebElement.sendKeys(..)) || "
//			+ "call(* org.openqa.selenium.WebElement.getText())")
//	public void logSeleniumCommands(JoinPoint jp){}
//	
//	
//	// advice definition
//	
//	@Before("logElementLocalization(JoinPoint)")
//	public void loggingAdvice(JoinPoint jp) throws IOException{
//		
//		// CANT CAPTURE WEB ELEMENT IN THIS ASPECT, LEAD TO INFINITE RECURSIVE CALLS
//		
//		d = (WebDriver) jp.getTarget();
//		
//	}
//	
//	/**
//	 * capture the GUI state after that the webElement has been
//	 * successfully retrieved
//	 * @param we
//	 * @throws IOException
//	 */
//	@AfterReturning(pointcut = "logElementLocalization(JoinPoint)", returning="we")
//	public void loggingAdvice(WebElement we) throws IOException{
//		
//		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
//		File destFile = new File(path + File.separator + "beforeEvent" + Settings.extension);
//		
//		try {
//			FileUtils.copyFile(screenshot, destFile);
//			screenshot = destFile;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		UtilsOpenCv.saveVisualLocator(d, we, Settings.annotate);
//		
//		System.out.println("[LOG]\t@findElement " + we + " executed correctly");
//	}
//	
//	/**
//	 * capture the GUI state even when Selenium fails at retrieving
//	 * the desired webElement
//	 * @param we
//	 * @throws IOException
//	 */
//	@AfterThrowing(pointcut = "logElementLocalization(JoinPoint)", throwing="exception")
//	public void logAfterThrowing(Exception exception){
//		
//		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
//		File destFile = new File(path + File.separator + "beforeEvent" + Settings.extension);
//		
//		try {
//			FileUtils.copyFile(screenshot, destFile);
//			screenshot = destFile;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println("[LOG]\t@Exception raised in findElement");
//	}
//	
//
//	
//
//	
//	@After("logSeleniumCommands(JoinPoint)")
//	public void loggingClickHandler2() throws InterruptedException{
//		
//		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
//		File destFile = new File(path + File.separator + "afterEvent" + Settings.extension);
//		
//		try {
//			Thread.sleep(500);
//			FileUtils.copyFile(screenshot, destFile);
//			screenshot = destFile;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("[LOG]\t@After command executed correctly");
//	}
//	
//	@AfterThrowing(pointcut = "logSeleniumCommands(JoinPoint)", throwing="exception")
//	public void logAfterThrowing2(Exception exception) throws InterruptedException{
//		
//		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
//		File destFile = new File(path + File.separator + "afterEvent" + Settings.extension);
//		
//		try {
//			Thread.sleep(500);
//			FileUtils.copyFile(screenshot, destFile);
//			screenshot = destFile;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("[LOG]\t@Exception raised in Selenium command");
//	}
//	
//	
//	
//	
//	
//	
////	@Before("logSeleniumCommands(JoinPoint)")
////	public void loggingClickHandler(){
////		
////		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
////		File destFile = new File(path + File.separator + "beforeClick" + Settings.extension);
////		
////		try {
////			FileUtils.copyFile(screenshot, destFile);
////			screenshot = destFile;
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
////		System.out.println("[LOG]\t@Before command executed correctly");
////	}
//	
////	@After("logElementLocalization(JoinPoint)")
////	public void loggingAdvice2(JoinPoint jp){
////		
////		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
////		File destFile = new File(path + File.separator + "afterFindElement" + Settings.extension);
////		
////		try {
////			FileUtils.copyFile(screenshot, destFile);
////			screenshot = destFile;
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
////		System.out.println("[LOG]\t@After findElement " + jp.getArgs()[0] + " executed correctly");
////	}
//		
//}
