package water;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import config.Settings;
import datatype.*;
import utils.UtilsWater;

public class Water {

	static List<HtmlElement> repairs;
	static EnhancedTestCase  broken;
	static EnhancedTestCase  correct;
	static EnhancedException ex;
	static HtmlDomTree oldDom;
	static String locator;
	static HtmlDomTree newDom;
	
	public Water(EnhancedTestCase b, EnhancedTestCase c, EnhancedException e) {
		Water.broken = b;
		Water.correct = c;
		Water.ex = e;
	}
	
	public List<HtmlElement> suggestRepair(HtmlDomTree oldDom, String locator, HtmlDomTree newDom) {

		repairs = new LinkedList<HtmlElement>();
		Water.oldDom = oldDom;
		Water.locator = locator;
		Water.newDom = newDom;

		// locator error
		if (ex.getMessage().contains("Unable to locate element")) {
			repairs = repairLocators(broken, correct, ex, oldDom, locator, newDom);
		// assertion error
		} else if (ex.getMessage().contains("Assertion error")) {

			repairs = repairLocators(broken, correct, ex, oldDom, locator, newDom);

			if (!repairs.isEmpty())
				return repairs;

			if (ex.getMessage().contains("Assertion value")) {
				// get new value
			} else {
				// negate assertion
			}

			// TODO: to manage
			// repairs.add(checkRepair());
		} else if (ex.getMessage().contains("Cannot locate element with text")){
			
		}

		if (repairs.isEmpty()) {
			// remove statement
			// repairs.add(checkRepair());
		}

		return repairs;
	}
	
	public static List<HtmlElement> repairLocators(EnhancedTestCase broken, EnhancedTestCase correct, EnhancedException ex,
			HtmlDomTree oldTree, String locator, HtmlDomTree newTree) {
		
		List<HtmlElement> matches = new LinkedList<HtmlElement>();
		SeleniumLocator l = broken.getStatements().get(Integer.parseInt(ex.getInvolvedLine())).getDomLocator();
		
		HtmlElement oldNode = UtilsWater.getNodeByLocator(oldTree, locator);
//		HtmlElement oldNode = UtilsWater.getNodeByLocator(oldTree, l.getValue());
		
		HtmlElement el = newTree.searchHtmlDomTreeByAttribute("id", oldNode.getId());
		if(el != null) matches.add(el);
		
		el = newTree.searchHtmlDomTreeByXPath(oldNode.getXPath());
		if(el != null) matches.add(el);
		
		el = newTree.searchHtmlDomTreeByAttribute("class", oldNode.getHtmlAttributes().get("class"));
		if(el != null) matches.add(el);
		
		el = newTree.searchHtmlDomTreeByAttribute("text", oldNode.getHtmlAttributes().get("text"));
		if(el != null) matches.add(el);
		
		el = newTree.searchHtmlDomTreeByAttribute("name", oldNode.getHtmlAttributes().get("name"));
		if(el != null) matches.add(el);
		
		if(!matches.isEmpty()) {
			for (HtmlElement candidateElement : matches) {
				
				repairs.add(candidateElement);
				
//				EnhancedTestCase testRepair = broken;
//				Statement st = testRepair.getStatements().get(Integer.parseInt(ex.getInvolvedLine()));
//				st.setDomLocator(new SeleniumLocator("xpath", candidateElement.getXPath()));
//				
//				// run test case and if it passes, add it to the repair list
//				try {
//					if(UtilsWater.checkRepair(testRepair)){
//						repairs.add(candidateElement);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				
			}
		}
		
		if(repairs.isEmpty()) {
			List<HtmlElement> similarNodes = UtilsWater.getSimilarNodes(oldNode, newTree, Settings.similarityThreshold);
			for (HtmlElement similarElement : similarNodes) {
				
				EnhancedTestCase testRepair = broken;
				Statement st = testRepair.getStatements().get(Integer.parseInt(ex.getInvolvedLine()));
				st.setDomLocator(new SeleniumLocator("xpath", similarElement.getXPath()));
				
				// run test case and if it passes, add the element to the the repair list
				try {
					if(UtilsWater.checkRepair(testRepair)){
						repairs.add(similarElement);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return repairs;
	}
	
	public static List<HtmlElement> getRepairs() {
		return repairs;
	}
	

}
