package addressbook825;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class Settings {

	static String version = "/addressbook/addressbookv8.2.5/addressbook/index.php";
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
