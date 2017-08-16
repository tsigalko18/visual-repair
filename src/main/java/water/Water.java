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

	public Water(EnhancedTestCase b, EnhancedTestCase c, EnhancedException e, boolean checkOnBrowser)
			throws NumberFormatException, SAXException, IOException {
		broken = b;
		correct = c;
		ex = e;

		System.out.println("[LOG]\tLoading the old DOM");
		oldDom = UtilsWater.getDom(correct, Integer.parseInt(e.getInvolvedLine()), checkOnBrowser);

		System.out.println("[LOG]\tLoading the new DOM");
		newDom = UtilsWater.getDom(broken, Integer.parseInt(e.getInvolvedLine()), checkOnBrowser);
	}

	public List<EnhancedTestCase> suggestRepair() {

		repairs = new LinkedList<EnhancedTestCase>();

		// locator error
		if (ex.getMessage().contains("Unable to locate element")) {

			repairs = repairLocators(broken, correct, ex, oldDom, newDom);

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
			
			/* get the line responsible for the breakage. */
			int brokenStatementLine = Integer.parseInt(ex.getInvolvedLine());

			EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(broken);
			temp.removeStatementAtPosition(brokenStatementLine);

			repairs.add(temp);
			
		}

		return repairs;
	}

	public static List<EnhancedTestCase> repairLocators(EnhancedTestCase broken, EnhancedTestCase correct,
			EnhancedException ex, HtmlDomTree oldTree, HtmlDomTree newTree) {

		List<HtmlElement> matches = new LinkedList<HtmlElement>();
		SeleniumLocator l = correct.getStatements().get(Integer.parseInt(ex.getInvolvedLine())).getDomLocator();

		HtmlElement oldNode = UtilsWater.getNodeByLocator(oldTree, l);

		if (oldNode == null) {
			System.err.println("[LOG]\tElement not found in old DOM by its own locator " + l.toString());
			System.exit(1);
		}

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

			if (Settings.VERBOSE) {
				System.out.println(matches.size() + " candidates(s) element found");
			}

			for (HtmlElement candidateElement : matches) {

				List<SeleniumLocator> locators = new LinkedList<SeleniumLocator>();
				locators = UtilsRepair.generateAllLocators(candidateElement);

				for (SeleniumLocator seleniumLocator : locators) {

					EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(broken);
					Statement newStatementWithNewLocator = temp.getStatements()
							.get(Integer.parseInt(ex.getInvolvedLine()));
					newStatementWithNewLocator.setDomLocator(seleniumLocator);
					temp.addAndReplaceStatement(Integer.parseInt(ex.getInvolvedLine()), newStatementWithNewLocator);

					repairs.add(temp);

				}

			}

		}

		if (repairs.isEmpty()) {

			List<HtmlElement> similarNodes = UtilsWater.getSimilarNodes(oldNode, newTree, Settings.SIMILARITY_THRESHOLD);

			if (Settings.VERBOSE) {
				System.out.println(similarNodes.size() + " similar(s) element found");
			}

			for (HtmlElement similarElement : similarNodes) {

				List<SeleniumLocator> locators = new LinkedList<SeleniumLocator>();
				locators = UtilsRepair.generateAllLocators(similarElement);

				for (SeleniumLocator seleniumLocator : locators) {

					EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(broken);
					Statement newStatementWithNewLocator = temp.getStatements()
							.get(Integer.parseInt(ex.getInvolvedLine()));
					newStatementWithNewLocator.setDomLocator(seleniumLocator);
					temp.addAndReplaceStatement(Integer.parseInt(ex.getInvolvedLine()), newStatementWithNewLocator);

					repairs.add(temp);

				}

			}

			for (int i = 0; i < similarNodes.size(); i++) {
				System.out.println(i + ":\t" + similarNodes.get(i).getXPath());
			}

		}

		return repairs;
	}

	public static List<EnhancedTestCase> getRepairs() {
		return repairs;
	}

}
