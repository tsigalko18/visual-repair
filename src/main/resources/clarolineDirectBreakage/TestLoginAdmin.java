package clarolineDirectBreakage;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TestLoginAdmin {

	private WebDriver driver;

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();// Settings.getDriver();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		driver.get("http://localhost:8888/claroline/claroline-1.11.1.0/");
	}

	@Test
	public void testHomePage() {
		driver.findElement(By.id("login")).sendKeys("admin"); // username
		driver.findElement(By.id("password")).sendKeys("admin"); // password
		driver.findElement(By.xpath("html/body/div[1]/div[2]/div[1]/p[1]/a")).click(); // breaks HERE
		assertTrue(driver.findElement(By.id("username")).getText().contains("John Doe"));
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
	}

	public WebDriver getDriver() {
		return driver;
	}

	// driver.findElement(By.xpath(".//*[@id='loginBox']/form/fieldset/input[4]")).click();
	// // breaks HERE
	// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
	// Doe"));

	/**
	 * This test contains two locator breakages: XPath at line 31 and id at line 32
	 */

	// System.setProperty("webdriver.firefox.bin",
	// "/Applications/_Firefox.app/Contents/MacOS/firefox-bin");

}
