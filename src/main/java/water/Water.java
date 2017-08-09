package water;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.SeleniumLocator;
import datatype.Statement;
import utils.UtilsRepair;
import utils.UtilsWater;

public class Water {

	static List<EnhancedTestCase> repairs;
	static EnhancedTestCase broken;
	static EnhancedTestCase correct;
	static EnhancedException ex;
	static HtmlDomTree oldDom;
	static HtmlDomTree newDom;

	public Water(EnhancedTestCase b, EnhancedTestCase c, EnhancedException e)
			throws NumberFormatException, SAXException, IOException {
		broken = b;
		correct = c;
		ex = e;
		oldDom = UtilsWater.getDom(correct, Integer.parseInt(e.getInvolvedLine()));
		newDom = UtilsWater.getDom(broken, Integer.parseInt(e.getInvolvedLine()));
	}

	public List<EnhancedTestCase> suggestRepair() {

		repairs = new LinkedList<EnhancedTestCase>();

		// locator error
		if (ex.getMessage().contains("Unable to locate element")) {

			long startTime = System.currentTimeMillis();

			repairs = repairLocators(broken, correct, ex, oldDom, newDom);

			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println("Repairs found in: " + elapsedTime / 1000);

			// assertion error
		} else if (ex.getMessage().contains("Assertion error")) {

			repairs = repairLocators(broken, correct, ex, oldDom, newDom);

			if (!repairs.isEmpty())
				return repairs;

			if (ex.getMessage().contains("Assertion value")) {
				// get new value
			} else {
				// negate assertion
			}

			// TODO: to manage
			// repairs.add(checkRepair());
		} else if (ex.getMessage().contains("Cannot locate element with text")) {

		}

		if (repairs.isEmpty()) {
			// remove statement
			// repairs.add(checkRepair());
		}

		return repairs;
	}

	public static List<EnhancedTestCase> repairLocators(EnhancedTestCase broken, EnhancedTestCase correct,
			EnhancedException ex, HtmlDomTree oldTree, HtmlDomTree newTree) {

		List<HtmlElement> matches = new LinkedList<HtmlElement>();
		SeleniumLocator l = broken.getStatements().get(Integer.parseInt(ex.getInvolvedLine())).getDomLocator();

		HtmlElement oldNode = UtilsWater.getNodeByLocator(oldTree, l);
		// HtmlElement oldNode = UtilsWater.getNodeByLocator(oldTree, l.getValue());

		HtmlElement el = newTree.searchHtmlDomTreeByAttribute("id", oldNode.getId());
		if (el != null)
			matches.add(el);

		el = newTree.searchHtmlDomTreeByXPath(oldNode.getXPath());
		if (el != null)
			matches.add(el);

		el = newTree.searchHtmlDomTreeByAttribute("class", oldNode.getHtmlAttributes().get("class"));
		if (el != null)
			matches.add(el);

		el = newTree.searchHtmlDomTreeByAttribute("text", oldNode.getHtmlAttributes().get("text"));
		if (el != null)
			matches.add(el);

		el = newTree.searchHtmlDomTreeByAttribute("name", oldNode.getHtmlAttributes().get("name"));
		if (el != null)
			matches.add(el);

		if (!matches.isEmpty()) {
			for (HtmlElement candidateElement : matches) {

				Statement st = broken.getStatements().get(Integer.parseInt(ex.getInvolvedLine()));
				st.setDomLocator(new SeleniumLocator("xpath", candidateElement.getXPath()));
				
				EnhancedTestCase temp = UtilsRepair.copyTest(broken);
				temp.addAndReplaceStatement(Integer.parseInt(ex.getInvolvedLine()), st);
				repairs.add(temp);

			}
		}

		if (repairs.isEmpty()) {
			List<HtmlElement> similarNodes = UtilsWater.getSimilarNodes(oldNode, newTree, Settings.similarityThreshold);
			for (HtmlElement similarElement : similarNodes) {

				Statement st = broken.getStatements().get(Integer.parseInt(ex.getInvolvedLine()));
				st.setDomLocator(new SeleniumLocator("xpath", similarElement.getXPath()));
				
				EnhancedTestCase temp = UtilsRepair.copyTest(broken);
				temp.addAndReplaceStatement(Integer.parseInt(ex.getInvolvedLine()), st);
				repairs.add(temp);
			}
		}

		if (Settings.VERBOSE) {
			System.out.println(repairs.size() + " candidate(s) element found");
		}

		return repairs;
	}

	public static List<EnhancedTestCase> getRepairs() {
		return repairs;
	}

}
