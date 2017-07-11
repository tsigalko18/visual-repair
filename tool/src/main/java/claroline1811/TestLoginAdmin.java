package claroline1811;

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
	private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();//Settings.getDriver();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		baseUrl = "http://localhost:8888/claroline";
	}

	@Test
	public void testHomePage() {
		driver.get(baseUrl + Settings.getVersion());
        driver.findElement(By.id("login")).sendKeys("admin"); // username
        driver.findElement(By.id("password")).sendKeys("admin"); // password
        driver.findElement(By.xpath("html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input")).click(); // confirmLogin
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
	
}
