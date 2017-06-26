package main.java.aspects;
//package aspects;
//
//import java.io.File;
//import java.io.IOException;
//import java.awt.image.BufferedImage;
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//
//import javax.imageio.ImageIO;
//
//import org.opencv.imgproc.*;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Before;
//import org.openqa.selenium.OutputType;
//import org.openqa.selenium.StaleElementReferenceException;
//import org.openqa.selenium.TakesScreenshot;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebDriverException;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.support.ui.Select;
//
//import datatype.DriverGet;
//import datatype.EnhancedSelect;
//import datatype.EnhancedTestCase;
//import datatype.EnhancedTestSuite;
//import datatype.EnhancedWebElement;
//import datatype.SeleniumLocator;
//import datatype.Statement;
//
//public aspect TemporalSnapshotSaver {
//
//	static WebDriver d;
//	static EnhancedTestSuite testsuite;
//	static EnhancedTestCase testcase;
//	static Statement statement;
//	
//	/**
//	 * This advice starts at any Selenium WebDriver get invocation. It creates
//	 * the directory containing all the files and get the initial screenshot of
//	 * the SUT
//	 * 
//	 * @param joinPoint
//	 */
//	@After("call(* org.openqa.selenium.WebDriver.get(..))")
//	//@Before("call(* org.openqa.selenium.WebDriver.get(..))")
//	public void logWebDriverGet(JoinPoint joinPoint) {
//		
//		// creates a separate folder for each test
//		File theDir = new File(Settings.testSuiteFolder);
//		if (!theDir.exists()) {
//			System.out.print("creating directory " + Settings.testSuiteFolder + "...");
//			boolean result = theDir.mkdir();
//			if (result) { System.out.println("done"); }
//		}
//		
//		// create the test suite object (singleton)
//		if(testsuite == null) { testsuite = new EnhancedTestSuite(); }
//		
//		// for each test, create a folder
//		String testFolderName = Settings.testSuiteFolder + File.separator + joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", "");	
//		theDir = new File(testFolderName);
//		if (!theDir.exists()) {
//			System.out.print("creating directory " + testFolderName + "...");
//			boolean result = theDir.mkdir();
//			if (result) { System.out.println("done"); }
//		}
//				
//		// for each statement, get a unique name in the form <testName>-<lineNumber>
//		String statementName = joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
//		statementName = statementName.concat("-");
//		statementName = statementName.concat(Integer.toString(joinPoint.getStaticPart().getSourceLocation().getLine()));
//				
//		// capturing the opening of the browser (i.e., the first state(ment) of the test)
//		if (joinPoint.getTarget() instanceof WebDriver) {
//					
//			d = (WebDriver) joinPoint.getTarget();
//			String testcasename = joinPoint.getStaticPart().getSourceLocation().getFileName();
//					
//			// create the test case object
//			testcase = new EnhancedTestCase(testcasename);
//				
//			if(joinPoint.getStaticPart().getSignature().getName().equals("get")){
//						
//				statement = new DriverGet();
//				statement.setDom(d.getPageSource());
//				statement.setAction("get");
//				statement.setValue((String) joinPoint.getArgs()[0]);
//						
//				// take screenshot
//				File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
//				BufferedImage img = null;
//				try {
//					img = ImageIO.read(screenshot);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				File realScreen = new File(testFolderName + File.separator + statementName + Settings.extension);
//				try {
//					ImageIO.write(img, "png", realScreen);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				screenshot = realScreen;
//						
//				statement.setScreenshot(screenshot);
//				statement.setLine(joinPoint.getStaticPart().getSourceLocation().getLine());
//				statement.setName(statementName);
//			}
//								
//		}
//				
//	}
//	
//	/**
//	 * manages calls to web elements and test statements
//	 * 
//	 * @param joinPoint
//	 * @throws IOException 
//	 * @throws InterruptedException 
//	 */
//	@After(  "call(* org.openqa.selenium.WebElement.click()) || "
//			+ "call(* org.openqa.selenium.WebElement.sendKeys(..)) || "
//			+ "call(* org.openqa.selenium.WebElement.getText()) ||"
//			+ "call(* org.openqa.selenium.WebElement.clear(..)) ||"
//			+ "call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..)) ||"
//			+ "call(* org.openqa.selenium.support.ui.Select.selectByIndex(..))")
////	@Before(	"call(* org.openqa.selenium.WebDriver.findElement(..))")
//	public void logWebDriverCalls(JoinPoint joinPoint) {
//		
//		String statementName = "";
//				
//		// for each test, create a folder
//		String testFolderName = Settings.testSuiteFolder + File.separator + joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
//		
//		File theDir = new File(testFolderName);
//		if (!theDir.exists())
//		{
//			System.out.print("creating directory " + testFolderName + "...");
//			boolean result = theDir.mkdir();
//			if (result)
//			{
//				System.out.println("done");
//			}
//		}
//		
//		// for each statement, get a unique name in the form 
//		statementName = statementName.concat(joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", ""));
//		statementName = statementName.concat("-");
//		statementName = statementName.concat(Integer.toString(joinPoint.getStaticPart().getSourceLocation().getLine()));
//		
//		
//		// I've captured a Select. TODO: to manage
//		if (joinPoint.getTarget() instanceof Select) {
//			
//			statement = new EnhancedSelect();
//			statement.setSelect((Select) joinPoint.getTarget());
//			statement.setWebElement((WebElement) statement.getSelect().getOptions().get(0));
//			// TODO: test!
//			// DOM LOCATOR !!!
//			statement.setDomLocator(extractDomLocator(statement.getWebElement()));
//			
//		// I've captured a WebElement.
//		} else if (joinPoint.getTarget() instanceof WebElement) {
//			
//			statement = new EnhancedWebElement();
//			statement.setWebElement((WebElement) joinPoint.getTarget());
//			statement.setDom(d.getPageSource());
//			String action = joinPoint.getStaticPart().getSignature().getName();
//			statement.setAction(action);
//			
//			// if action is sendKeys, save the value (i.e., parameter of sendKeys)
//			if(action.equals("sendKeys")){
//				CharSequence[] cs = (CharSequence[]) joinPoint.getArgs()[0];
//				String value = cs[0].toString();
//				statement.setValue(value);
//			} 
//			// if action is getText, save the assertion type
//			else if(action.equals("getText")){
//				// TODO: probably cannot do it here 
//			}
//			
//			// DOM LOCATOR !!!
//			statement.setDomLocator(extractDomLocator(statement.getWebElement()));
//			
//			// get screenshot of the page
//			File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
//			BufferedImage img = null;
//			try {
//				img = ImageIO.read(screenshot);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			File realScreen = new File(testFolderName + File.separator + statementName + Settings.extension);
//			try {
//				ImageIO.write(img, "png", realScreen);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			screenshot = realScreen;
//			
//			statement.setScreenshot(screenshot);
//			statement.setLine(joinPoint.getStaticPart().getSourceLocation().getLine());
//			statement.setName(statementName);
//			statement.setCoordinates(statement.getWebElement().getLocation());
//			statement.setDimension(statement.getWebElement().getSize());
//			statement.setLine(joinPoint.getStaticPart().getSourceLocation().getLine());
//			
//			// annotate screenshot and crop visual locator
//			String webElementImageName = joinPoint.getSourceLocation().getFileName().replace(".java", "") + 
//											"/" + joinPoint.getSourceLocation().getLine();
//
//			String pathScreenShot = "";
//			String pathVisualLocator = "";
//
//			List<String> images = new LinkedList<String>();
//			int scale = 10;
//
//			boolean matchUnivocal = false;
//			try {
//				do {
////					images = UtilsOpenCv.shoot(d, we, webElementImageName, scale);
//					images = UtilsOpenCv.cropVisualLocator(d, statement.getWebElement(), webElementImageName, scale);
//					pathScreenShot = images.get(0);
//					pathVisualLocator = images.get(1);
//					matchUnivocal = UtilsOpenCv.isUniqueMatch(pathScreenShot, pathVisualLocator, null, Imgproc.TM_CCOEFF_NORMED);
//					scale--;
//				} while (matchUnivocal == false);
//			} catch (IOException | InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			
//		}
//		
//		// add the statement to the test case
//		testcase.addStatement(statement);
//		System.out.println(statement.toString());
//			
//	}
//
//	/**
//	 * This advice starts at any Selenium WebDriver quit invocation. It saves all
//	 * the locators mapping on the filesystem on a dedicated file
//	 * 
//	 * @param joinPoint
//	 */
//	@Before("call(* org.openqa.selenium.WebDriver.quit())")
//	public void logBeforeWebDriverQuit(JoinPoint joinPoint) {
//		// at the end of the test case, I have all the elements to create
//		// the visual locators and the annotated screenshots
//		testsuite.addTestCase(testcase);
//		
//		testsuite.print();
//	}
//	
//	/**
//	 * auxiliary method to extract the DOM locator 
//	 * used by the web element
//	 * 
//	 * @param webElement
//	 * @return
//	 */
//	private SeleniumLocator extractDomLocator(WebElement webElement) {
//		
//		String domLocator = webElement.toString(); // [[FirefoxDriver: firefox on MAC (3ba14698-bb30-aa41-b4f1-3c44e1769bb4)] -> id: login]
//		domLocator = domLocator.substring(3 + domLocator.indexOf("->")); // id : login]
//		String strategy = domLocator.split(":")[0].trim();
//		String value = domLocator.split(":")[1];
//		value = value.substring(0, value.length()-1).trim();
//		
//		return new SeleniumLocator(strategy, value);
//	}
//
//}
//
//
