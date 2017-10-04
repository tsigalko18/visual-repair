package crawler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.plugin.descriptor.Parameter;
import com.crawljax.core.plugin.descriptor.PluginDescriptor;

import datatype.WebDriverSingleton;

/**
 * Use the sample plugin in combination with Crawljax.
 */
public class Crawler {

	private static final Logger LOG = LoggerFactory.getLogger(Plugin.class);
	private static Scanner scanner = new Scanner(System.in);
	private String URL;
	private static final int MAX_DEPTH = 1;
	private boolean check = true;

	public Crawler(String url) {
		this.URL = url;
	}

	public void runLocalCrawling() {

		/* run the crawler. */
//		if (check) {

//			WebDriverSingleton instance = WebDriverSingleton.getInstance();
//			instance.loadPage(URL);
//			WebDriver driver = instance.getDriver();
			
			/* extra check for the cases when the authentication is needed. */
//			System.out.println("Is the web page correctly displayed? [type Y and Enter key to proceed]");
//			while (!scanner.next().equals("Y")) {
//			}

			CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
			builder.crawlRules().insertRandomDataInInputForms(true);
			builder.crawlRules().clickDefaultElements();
			

			/* limit the crawling scope. */
			builder.setUnlimitedStates();
			builder.setMaximumDepth(MAX_DEPTH);
			builder.setMaximumRunTime(10, TimeUnit.SECONDS);
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX));

			builder.addPlugin(new Plugin(new HostInterfaceImpl(new File("out"), null)));

			// builder.crawlRules().setInputSpec(getInputSpecification());

			CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
			crawljax.call();

//		}

	}

	public static void main(String[] args) {
		new Crawler("http://github.com/").runLocalCrawling();
	}

	// private static InputSpecification getInputSpecification() {
	// InputSpecification input = new InputSpecification();
	// input.field("gbqfq").setValue("Crawljax");
	// return input;
	// }

}
