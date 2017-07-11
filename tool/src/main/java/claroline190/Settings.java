package claroline190;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class Settings {

	static String version = "/claroline-1.9.0/index.php";
//	static WebDriver driver = new FirefoxDriver();
//	static WebDriver driver = new FirefoxDriver();
	static WebDriver driver;

	public static String getVersion() {
		return version;
	}
	
	public static WebDriver getDriver() {
		if(driver == null) {
			driver = new FirefoxDriver();
		}
		return driver;
	}
	
}
