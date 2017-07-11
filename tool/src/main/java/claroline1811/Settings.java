package main.java.claroline1811;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class Settings {

	static String version = "/claroline-1.8.11/index.php";
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
