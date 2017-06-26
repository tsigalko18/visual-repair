package main.java.clarolineDirectBreakage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class DirectBreakage {
	
	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
	}

	@Test
	public void testHomePage() throws Exception {
		driver.get("http://localhost:8888/claroline/claroline-1.11.10/");
        driver.findElement(By.id("login")).sendKeys("admin"); // username
        driver.findElement(By.id("password")).sendKeys("admin"); // password
        driver.findElement(By.xpath(".//*[@id='loginBox']/form/fieldset/input[4]")).click(); // test breaks HERE, fault is here
        					    // "html/body/div[1]/div[2]/div[1]/div/form/fieldset/button"
        assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John Doe"));
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	/**
	 * This test contains two locator breakages: XPath at line 31 and id at line 32
	 */
}
