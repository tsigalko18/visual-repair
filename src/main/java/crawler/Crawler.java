package crawler;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.plugins.crawloverview.CrawlOverview;

import config.Settings;
import datatype.EnhancedTestCase;

/**
 * Use the sample plugin in combination with Crawljax.
 */
public class Crawler {

	private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);
	private String URL;
	private String testCaseFile;
	private int brokenStep;
	private HashMap<Integer, String> appliedRepairs;
	private EnhancedTestCase testBroken;
	private EnhancedTestCase testCorrect;
	private static final int MAX_DEPTH = 1;

	public Crawler(String url, EnhancedTestCase testBroken, EnhancedTestCase testCorrect, int brokenStep,
			HashMap<Integer, String> appliedRepairs) {
		this.URL = url;
		this.testBroken = testBroken;
		this.testCorrect = testCorrect;
		this.brokenStep = brokenStep;
		this.appliedRepairs = appliedRepairs;
		Settings.aspectActive = false;
	}

	public void runLocalCrawling() {

		/* run the crawler. */

		CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);

		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().dontClick("a").withText("Logout");

		/* limit the crawling scope. */
		builder.setUnlimitedStates();
		builder.setMaximumDepth(MAX_DEPTH);
		builder.setMaximumRunTime(5, TimeUnit.MINUTES);
//		builder.setMaximumRunTime(300, TimeUnit.SECONDS);
		builder.addPlugin(new Plugin(new HostInterfaceImpl(new File("out"), null), this.testBroken, this.testCorrect,
				this.brokenStep, this.appliedRepairs));
		builder.addPlugin(new CrawlOverview());

		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();

	}

	public static void main(String[] args) {
		new Crawler("http://localhost:8888/addressbook/addressbookv8.2.5/addressbook/index.php", null, null, -1, null)
				.runLocalCrawling();
	}

	// private static InputSpecification getInputSpecification() {
	// InputSpecification input = new InputSpecification();
	// input.field("gbqfq").setValue("Crawljax");
	// return input;
	// }

}
