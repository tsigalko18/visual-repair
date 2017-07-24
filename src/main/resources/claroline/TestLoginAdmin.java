package claroline;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TestLoginAdmin {

	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
	}

	@Test
	public void testLoginAdmin() {
		driver.get("http://localhost:8888/claroline/claroline-1.10.7/");
		driver.findElement(By.id("login")).sendKeys("admin"); // username
		driver.findElement(By.id("password")).sendKeys("admin"); // password
		driver.findElement(By.xpath(".//*[@id='loginBox']/form/fieldset/input[3]")).click(); // confirmLogin
		assertTrue(driver.findElement(By.id("userName")).getText().contains("John Doe"));
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}
	
	// System.setProperty("webdriver.firefox.bin", "/Applications/_Firefox.app/Contents/MacOS/firefox-bin");

}
