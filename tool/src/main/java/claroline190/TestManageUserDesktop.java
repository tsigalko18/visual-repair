package main.java.claroline190;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.openqa.selenium.support.ui.Select;

public class TestManageUserDesktop {

	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();// Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testManageUserDesktop() throws Exception {
		System.out.println("[TestManageUserDesktop]\t\tnot applicable in this version");
		// driver.get(baseUrl + Version.getVersion());
		// driver.findElement(By.id("login")).clear();
		// driver.findElement(By.id("login")).sendKeys("admin");
		// driver.findElement(By.id("password")).clear();
		// driver.findElement(By.id("password")).sendKeys("admin");
		// driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click();
		// driver.findElement(By.linkText("Platform Administration")).click();
		// driver.findElement(By.linkText("Manage user desktop")).click();
		// assertTrue(driver.findElement(By.cssSelector("table.claroTable.emphaseLine")).getText().matches("^[\\s\\S]*My
		// course list[\\s\\S]*$"));
		// driver.findElement(By.linkText("Logout")).click();
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
