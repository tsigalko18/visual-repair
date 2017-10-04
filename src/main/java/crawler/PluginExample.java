package crawler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

/**
 * This example shows how to add your own plugin. The plugin just prints the DOM
 * when a new state is detected.
 */
public class PluginExample {

	public static void main(String[] args) {

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration
				.builderFor("http://www.disi.unige.it/person/StoccoA/testWebsite/index.php");

		builder.crawlRules().clickDefaultElements();

		// limit the crawling scope
		builder.setUnlimitedStates();
		builder.setMaximumDepth(1);
		builder.setMaximumRunTime(1, TimeUnit.MINUTES);
		
		builder.addPlugin(new Plugin());

		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();
	}
}
