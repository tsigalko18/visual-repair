package addressbook825;

import java.util.concurrent.TimeUnit;
import org.junit.*;

import static org.junit.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class TestUserAdded {

	private WebDriver driver;

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		driver.get("http://localhost:8888/addressbook/addressbookv8.2.5/addressbook/index.php");
	}

	@Test
	public void testUserAdded() throws Exception {
		driver.findElement(By.name("user")).sendKeys("admin"); // username
		driver.findElement(By.name("pass")).sendKeys("admin"); // password
		driver.findElement(By.cssSelector("input[type='submit']")).click(); // confirmLogin
		driver.findElement(By.xpath("html/body/div[1]/div[3]/ul/li[2]/a")).click();
		driver.findElement(By.xpath("html/body/div[1]/div[4]/form/input[3]")).sendKeys("John"); // firstname
		driver.findElement(By.xpath("html/body/div[1]/div[4]/form/input[5]")).sendKeys("Doe"); // lastname
		driver.findElement(By.xpath("html/body/div[1]/div[4]/form/textarea[1]")).sendKeys("Times Square"); // address
		driver.findElement(By.xpath("html/body/div[1]/div[4]/form/input[10]")).sendKeys(".com"); // email
		driver.findElement(By.xpath("html/body/div[1]/div[4]/form/input[11]")).sendKeys("123456789"); // mobile
		new Select(driver.findElement(By.xpath("//*[@id='content']/form/select[1]"))).selectByVisibleText("31");
		new Select(driver.findElement(By.xpath("//*[@id='content']/form/select[2]"))).selectByVisibleText("January");
		driver.findElement(By.name("byear")).sendKeys("1969");
		driver.findElement(By.xpath("html/body/div[1]/div[4]/form/input[15]")).click(); // insertButton
		assertTrue(driver.findElement(By.xpath("html/body/div[1]/div[4]/label/strong/span")).getText().contains("1"));
	}
	
	public WebDriver getDriver() {
		return driver;
	}

}
