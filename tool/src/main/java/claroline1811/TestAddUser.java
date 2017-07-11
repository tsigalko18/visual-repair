package main.java.claroline1811;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.openqa.selenium.support.ui.Select;

public class TestAddUser {
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();//Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline/";
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
	}

	@Test
	public void testAddUser() throws Exception {
		driver.get(baseUrl + Settings.getVersion());
		driver.findElement(By.id("login")).clear();
		driver.findElement(By.id("login")).sendKeys("admin");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("admin");
		driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click();
		driver.findElement(By.linkText("Platform Administration")).click();
		driver.findElement(By.linkText("Create user")).click();
		driver.findElement(By.id("lastname")).clear();
		driver.findElement(By.id("lastname")).sendKeys("Naman");
		driver.findElement(By.id("firstname")).clear();
		driver.findElement(By.id("firstname")).sendKeys("Agr");
		driver.findElement(By.id("officialCode")).clear();
		driver.findElement(By.id("officialCode")).sendKeys("a7");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("nam");
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys("naman");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("123456");
		driver.findElement(By.id("password_conf")).clear();
		driver.findElement(By.id("password_conf")).sendKeys("123456");
		driver.findElement(By.xpath("html/body/div[3]/form/table/tbody/tr[12]/td[2]/input")).click();
		assertEquals("Create a new user - Claroline", driver.getTitle());
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
