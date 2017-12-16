package crawler;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification.How;

public class CrawlPathExport {

	public List<EventableExport> eventableExportList = new ArrayList<EventableExport>();

	public CrawlPathExport(CrawlPath path) {
		for (Eventable eventable : path) {
			EventableExport eventableExport = new EventableExport(eventable);
			this.eventableExportList.add(eventableExport);
		}
	}

}

class EventableExport {
	public How getHow;
	public String getValue;
	public String getSource;
	public String getTarget;

	public EventableExport(Eventable eventable) {
		this.getHow = eventable.getIdentification().getHow();
		this.getValue = eventable.getIdentification().getValue();
		this.getSource = eventable.getSourceStateVertex().getName();
		this.getTarget = eventable.getTargetStateVertex().getName();
	}
}