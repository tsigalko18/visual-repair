package claroline;

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
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		driver.get("http://localhost:8888/claroline/claroline-1.8.6/");
	}

	@Test
	public void testLoginAdmin() {
		driver.findElement(By.id("login")).sendKeys("admin"); // username
		driver.findElement(By.id("password")).sendKeys("admin"); // password
		driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr/td[2]/form/fieldset/input")).click(); // confirmLogin
		assertTrue(driver.findElement(By.id("userName")).getText().contains("John Doe"));
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
	}

	public WebDriver getDriver() {
		return driver;
	}
}
