package claroline1811;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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


public class TestSendMessage {
	
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();//Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testSendMessage() throws Exception {
		System.out.println("[TestSendMessage]\t\tnot applicable in this version");
//		driver.get(baseUrl + Version.getVersion());
//		driver.findElement(By.id("login")).clear();
//		driver.findElement(By.id("login")).sendKeys("admin");
//		driver.findElement(By.id("password")).clear();
//		driver.findElement(By.id("password")).sendKeys("admin");
//		driver.findElement(By.xpath("//button")).click();
//		driver.findElement(By.linkText("Platform administration")).click();
//		driver.findElement(By.linkText("Send a message to all users")).click();
//		driver.findElement(By.id("message_subject")).clear();
//		driver.findElement(By.id("message_subject")).sendKeys("Hello");
//		driver.findElement(By.id("message_fontselect_open")).click();
//		driver.findElement(By.cssSelector("#mce_18_aria > span.mceText")).click();
//		driver.findElement(By.cssSelector("span.mceIcon.mce_bold")).click();
//		driver.findElement(By.cssSelector("span.mceIcon.mce_italic")).click();
//		driver.findElement(By.name("send")).click();
//		assertTrue(driver.findElement(By.cssSelector("div.claroDialogBox.boxInfo")).getText().matches("^[\\s\\S]*Message sent[\\s\\S]*$"));
//		driver.findElement(By.linkText("Logout")).click();
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
