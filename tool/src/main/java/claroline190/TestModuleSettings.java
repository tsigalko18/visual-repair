package main.java.claroline190;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TestModuleSettings {

	private WebDriver driver;
	private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();// Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline";
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
	}

	@Test
	public void testModuleSettings() throws Exception {
		driver.get(baseUrl + Settings.getVersion());
		driver.findElement(By.id("login")).clear();
		driver.findElement(By.id("login")).sendKeys("admin");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("admin");
		driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click();
		driver.findElement(By.linkText("Platform Administration")).click();
		driver.findElement(By.linkText("Modules")).click();
		driver.findElement(By.xpath("html/body/div[3]/table[2]/tbody/tr[1]/td[5]/a/img")).click();
		driver.findElement(By.linkText("Make visible")).click();
		assertTrue(driver.switchTo().alert().getText()
				.matches("^ Are you sure you want to make this module visible in all courses [\\s\\S]$"));
		driver.switchTo().alert().accept();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table/tbody/tr/td")).getText()
				.contains("Module visibility updated"));
		driver.findElement(By.linkText("Global settings")).click();
		driver.findElement(By.linkText("About")).click();
		assertTrue(driver.findElement(By.id("claroBody")).getText().matches("^[\\s\\S]*Course description[\\s\\S]*$"));
		driver.findElement(By.linkText("Global settings")).click();
		driver.findElement(By.xpath("html/body/div[3]/form/table/tbody/tr[1]/td[2]/a/img")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table/tbody/tr/td")).getText()
				.contains("Module deactivation succeeded"));
		driver.findElement(By.xpath("html/body/div[3]/form/table/tbody/tr[1]/td[2]/a/img")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table/tbody/tr/td")).getText()
				.contains("Module activation succeeded"));
		driver.findElement(By.linkText("About")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table[1]/tbody/tr[3]/td[2]")).getText()
				.contains("Course description"));
		// driver.findElement(By.linkText("Automatic")).click();
		// driver.findElement(By.linkText("Manual")).click();
		// // Warning: verifyTextPresent may require manual changes
		// try {
		// //assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Module
		// activation at course creation set to AUTOMATIC[\\s\\S]*$"));
		// assertTrue(driver.findElement(By.cssSelector("div.claroDialogBox.boxSuccess")).getText().matches("^[\\s\\S]*Module
		// activation at course creation set to AUTOMATIC[\\s\\S]*$"));
		// } catch (Error e) {
		// verificationErrors.append(e.toString());
		// }
		driver.findElement(By.linkText("Logout")).click();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
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

}
