package main.java.claroline190;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class TestViewProfile {
	
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();//Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline";
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
	}

	@Test
	public void testViewProfile() throws Exception {
		driver.get(baseUrl + Settings.getVersion());
		driver.findElement(By.id("login")).clear();
		driver.findElement(By.id("login")).sendKeys("admin");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("admin");
		driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click();
		driver.findElement(By.linkText("Platform Administration")).click();
		driver.findElement(By.linkText("Right profile list")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table[2]/tbody/tr[3]/td[2]")).getText().contains("Course member (the user is actually enrolled in the course"));
		driver.findElement(By.xpath("(//img[@alt='Edit'])[6]")).click();
		assertEquals("Claroline", driver.getTitle());
//		driver.findElement(By.cssSelector("a > span")).click();
//		driver.findElement(By.cssSelector("a > span")).click();
//		driver.findElement(By.cssSelector("a > span")).click();
//		assertTrue(driver.findElement(By.cssSelector("table.claroTable.emphaseLine")).getText().matches("^[\\s\\S]*Access allowed[\\s\\S]*$"));
		driver.findElement(By.linkText("View all right profile")).click();
//		assertTrue(driver.findElement(By.cssSelector("table.claroTable.emphaseLine")).getText().matches("^[\\s\\S]*Edition allowed[\\s\\S]*$"));
		driver.findElement(By.linkText("Logout")).click();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();

	}

	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	private boolean isAlertPresent() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	private String closeAlertAndGetItsText() {
		try {
			Alert alert = driver.switchTo().alert();
			String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			} else {
				alert.dismiss();
			}
			return alertText;
		} finally {
			acceptNextAlert = true;
		}
	}
}
