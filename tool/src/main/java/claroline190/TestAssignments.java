package main.java.claroline190;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.openqa.selenium.support.ui.Select;

public class TestAssignments {
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();// Settings.getDriver();
		baseUrl = "http://localhost:8888/claroline";
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
	}

	@Test
	public void testAssignments() throws Exception {
		driver.get(baseUrl + Settings.getVersion());
		driver.findElement(By.id("login")).clear();
		driver.findElement(By.id("login")).sendKeys("admin");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("admin");
		driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click();
		driver.findElement(By.linkText("Platform Administration")).click();
		driver.findElement(By.linkText("Configuration")).click();
		driver.findElement(By.linkText("Assignments")).click();
		driver.findElement(By.id("label_confval_def_sub_vis_change_only_new_FALSE")).click();
		driver.findElement(By.id("label_confval_def_sub_vis_change_only_new_TRUE")).click();
		assertTrue(driver.findElement(By.name("editConfClass")).getText().matches(
				"^[\\s\\S]*ets how the assignment property \"default works visibility\" acts\\. It will change the visibility of all the new submissions or it will change the visibility of all submissions already done in the assignment and the new one\\.[\\s\\S]*$"));
		assertTrue(driver.findElement(By.name("editConfClass")).getText()
				.matches("^[\\s\\S]*For assignments list[\\s\\S]*$"));
		// assertTrue(driver.findElement(By.name("editConfClass")).getText().matches("^[\\s\\S]*/<COURSEID>/work/[\\s\\S]*$"));
		driver.findElement(By.linkText("Quota")).click();
		assertTrue(driver.findElement(By.name("editConfClass")).getText()
				.matches("^[\\s\\S]*Maximum size of a document that a user can upload[\\s\\S]*$"));
		driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
		assertTrue(driver.findElement(By.xpath("html/body/div[3]/table/tbody/tr/td")).getText()
				.matches("^[\\s\\S]*Properties for Assignments, \\(CLWRK\\) are now effective on server\\.[\\s\\S]*$"));
		// driver.findElement(By.linkText("Submissions")).click();
		// driver.findElement(By.id("label_clwrk_endDateDelay")).clear();
		// driver.findElement(By.id("label_clwrk_endDateDelay")).sendKeys("364");
		// driver.findElement(By.id("label_clwrk_endDateDelay")).clear();
		// driver.findElement(By.id("label_clwrk_endDateDelay")).sendKeys("365");
		// driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
		// driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
		driver.findElement(By.linkText("View all")).click();
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
