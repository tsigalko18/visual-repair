package crawler;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.StateVertex;

public class Plugin implements OnNewStatePlugin, OnUrlLoadPlugin, PostCrawlingPlugin, PreCrawlingPlugin {

	CrawlerContext arg = null;
	
	private HostInterface hostInterface;

	public Plugin(HostInterface hostInterface) {
		this.hostInterface = hostInterface;
	}
	
	public Plugin() {
		this.hostInterface = null;
	}

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {
		System.out.println("Found a new dom! Here it is: " + context.getBrowser().getCurrentUrl());
	}

	@Override
	public void postCrawling(CrawlSession arg0, ExitStatus arg1) {

		System.out.println("The number of states is: " + arg0.getStateFlowGraph().getNumberOfStates());
		System.out.println("The number of edges is: " + arg0.getStateFlowGraph().getAllEdges().size());

	}

	@Override
	public void onUrlLoad(CrawlerContext arg0) {
		arg = arg0;
		System.out.println("onURL");
		System.out.println("Initial URL: " + arg0.getBrowser().getCurrentUrl());
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
		
		System.out.println("PreCrawl");
		
	}

}
