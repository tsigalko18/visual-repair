package claroline1811;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class TestCourseCategoryEdit {
	
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();//Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline";
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}

	@Test
	public void testCourseCategoryEdit() throws Exception {
		driver.get(baseUrl + Settings.getVersion());
		driver.findElement(By.id("login")).clear();
		driver.findElement(By.id("login")).sendKeys("admin");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("admin");
		driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click();
		driver.findElement(By.linkText("Platform Administration")).click();
		driver.findElement(By.linkText("Manage course categories")).click();
		driver.findElement(By.xpath("html/body/div[3]/table/tbody/tr[1]/td[3]/a/img")).click();
		driver.findElement(By.id("codeCat")).clear();
		driver.findElement(By.id("codeCat")).sendKeys("SCI");
		driver.findElement(By.xpath("html/body/div[3]/table[2]/tbody/tr[1]/td[7]/a/img")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table[1]/tbody/tr/td/div/div")).getText().matches("^[\\s\\S]*Category moved[\\s\\S]*$"));
		driver.findElement(By.xpath("html/body/div[3]/table[2]/tbody/tr[2]/td[6]/a/img")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table[1]/tbody/tr/td/div/div")).getText().matches("^[\\s\\S]*Category moved[\\s\\S]*$"));
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
