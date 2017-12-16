package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlTaskConsumer;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;
import utils.UtilsComputerVision;
import utils.UtilsTemplateMatching;
import utils.UtilsXPath;

/*
 *  In order to run test steps to get to initial state. 
 *  		https://github.com/aminmf/crawljax/blob/master/core/src/main/java/com/crawljax/core/plugin/ExecuteInitialPathsPlugin.java
		extends plugin with ExecuteInitialPathsPlugin which can CrawljaxConfiguration config, CrawlTaskConsumer firstConsumer
 */

public class Plugin implements OnNewStatePlugin, OnUrlLoadPlugin, PostCrawlingPlugin, PreCrawlingPlugin {

	CrawlerContext arg = null;

	private static final Logger LOG = LoggerFactory.getLogger(Plugin.class);

	private HostInterface hostInterface;

	private String testCaseFile;

	private int brokenStep;

	private String templateToMatch = null;

	private HashMap<Integer, String> appliedRepairs;

	private EnhancedTestCase testBroken;

	private EnhancedTestCase testCorrect;

	public Plugin(HostInterfaceImpl hostInterfaceImpl, EnhancedTestCase testBroken, EnhancedTestCase testCorrect,
			int brokenStep, HashMap<Integer, String> appliedRepairs) {
		this.hostInterface = hostInterfaceImpl;
		this.testBroken = testBroken;
		this.testCorrect = testCorrect;
		this.brokenStep = brokenStep;
		this.appliedRepairs = appliedRepairs;
		Settings.aspectActive = false;
	}

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		CrawlPath path = context.getCrawlPath();
		CrawlPathExport crawlPathExport = new CrawlPathExport(path);

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.disableHtmlEscaping();
		Gson gson = gsonBuilder.create();
		Type type = new TypeToken<CrawlPathExport>() {
		}.getType();
		String jsonToWrite = gson.toJson(crawlPathExport, type);
		System.out.println(jsonToWrite);
		System.out.println(path.size());
		FileWriter outputFile = null;

		try {
			outputFile = new FileWriter(System.getProperty("user.dir") + Settings.separator + "crawloutput.txt", true);
			outputFile.write(String.valueOf(path.size()) + " : ");
			outputFile.write(path.toString() + '\n');
			outputFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				outputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Found a new dom! Here it is: " + context.getBrowser().getCurrentUrl());
		// Get old template and current screenshot for visual assertion
		Statement oldst = testCorrect.getStatements().get(brokenStep);
		WebDriver driver = context.getBrowser().getWebDriver();

		/* get the visual locator of the statement in the correct test case. */
		String template = oldst.getVisualLocator().toString();
		System.out.println(template);

		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsComputerVision.saveScreenshot(driver, currentScreenshot);

		// List<Point>
		Point matches = UtilsTemplateMatching.featureDetectorAndTemplateMatching(currentScreenshot, template); 
				//UtilsComputerVision.findBestMatchCenter(currentScreenshot, template);
		// returnAllMatches(currentScreenshot, template);

		System.out.println(matches);
		// ((JavascriptExecutor)driver).executeScript("alert('hello world')");
		String xpathForMatches = UtilsXPath.getXPathFromLocation(matches, driver);
		if (xpathForMatches.startsWith("BODY") || xpathForMatches.startsWith("body")) {
			xpathForMatches = "/HTML[1]/" + xpathForMatches;
		}

		System.out.println("Xpath for match : " + xpathForMatches);
		WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));
		if (!UtilsXPath.isLeaf(fromVisual)) {
			fromVisual = null;

		} else {
			System.out.println("Found a matching element for old template in this state");

			FileWriter resultFile = null;
			try {
				resultFile = new FileWriter(System.getProperty("user.dir") + Settings.separator + "matchingStates.txt",
						true);
				// resultFile.write(String.valueOf(path.size())+ " : ");
				resultFile.write(jsonToWrite + '\n');
				resultFile.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					resultFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void postCrawling(CrawlSession arg0, ExitStatus arg1) {

		System.out.println("The number of states is: " + arg0.getStateFlowGraph().getNumberOfStates());

		StateFlowGraph sfg = arg0.getStateFlowGraph();
		ImmutableSet<StateVertex> sfg_states = sfg.getAllStates();
		for (StateVertex sv : sfg_states) {
			ImmutableList<CandidateElement> candidateElements = sv.getCandidateElements();
			if (candidateElements != null) {
				for (CandidateElement ce : candidateElements)
					System.out.println(ce.getGeneralString());
			}
		}
		System.out.println("The number of edges is: " + arg0.getStateFlowGraph().getAllEdges().size());

	}

	@Override
	public void onUrlLoad(CrawlerContext arg0) {
		System.out.println("onURL");
		System.out.println("Initial URL: " + arg0.getBrowser().getCurrentUrl());

		WebDriver driver = arg0.getBrowser().getWebDriver();

		if (testBroken == null)
			return;
		if (brokenStep == -1)
			return;
		
		Map<Integer, Statement> statementMap = testBroken.getStatements();

		for (Integer I : statementMap.keySet()) {
			if (I == this.brokenStep) {
				break;
			}

			String repairedXpath = null;

			WebElement element = null;

			Statement statement = statementMap.get(I);
			// System.out.println(statement);
			SeleniumLocator domSelector = statement.getDomLocator();
			String strategy = domSelector.getStrategy();
			String action = statement.getAction();
			String locator = domSelector.getValue();
			String value = statement.getValue();
			if (value.startsWith("\""))
				value = value.substring(1);
			if (value.endsWith("\""))
				value = value.substring(0, value.length() - 1);

			System.out.println("locator: " + locator + ": strategy :" + strategy);
			System.out.println("action : " + action + " : value : " + value);

			if (this.appliedRepairs != null && this.appliedRepairs.containsKey(I)) {

				repairedXpath = this.appliedRepairs.get(I);
				element = driver.findElement(By.xpath(repairedXpath));

			} else {

				// For getting webelement
				try {
					if (strategy.equalsIgnoreCase("xpath")) {
						element = driver.findElement(By.xpath(locator));
					} else if (strategy.equalsIgnoreCase("name")) {
						element = driver.findElement(By.name(locator));
					} else if (strategy.equalsIgnoreCase("id")) {
						element = driver.findElement(By.id(locator));
					} else if (strategy.equalsIgnoreCase("linkText")) {
						element = driver.findElement(By.linkText(locator));
					} else if (strategy.equalsIgnoreCase("cssSelector")) {
						element = driver.findElement(By.cssSelector(locator));
					}
				} catch (Exception Ex) {
					Ex.printStackTrace();

				}
			}
			if (element != null) {
				String xpathForElement = UtilsXPath.generateXPathForWebElement(element, "");

				System.out.println("Found a web element");
				// Insert visual assertion here.
				// do visual search with previous screenshot on new page.

				try {
					// after ascertaining the right element, perform the action
					if (action.equalsIgnoreCase("click")) {
						element.click();
					}
					if (action.equalsIgnoreCase("sendkeys")) {
						System.out.println(value);
						element.sendKeys(value);
					}
					if (action.equalsIgnoreCase("selectByVisibleText")) {
						new Select(element).selectByVisibleText(value);
					}

				} catch (Exception Ex) {
					Ex.printStackTrace();
					// Apply repair strategies
					break;
				}
			}

		}
		/*
		 * arg = arg0; //driver = firstConsumer.getContext().getBrowser().; try {
		 * driver.findElement(By.name("user")).sendKeys("admin"); // username
		 * driver.findElement(By.name("pass")).sendKeys("admin"); // password
		 * driver.findElement(By.cssSelector("input[type='submit']")).click(); } //
		 * confirmLogin catch(Exception Ex) { System.out.println(" NO need of login x");
		 * } //WebElement webElement =
		 * driver.findElement(By.cssSelector("input[type='submit']"));
		 * driver.findElement(By.xpath("html/body/div[1]/div[3]/ul/li[2]/a")).click();
		 */
		System.out.println("Changed initial path URL : " + arg0.getBrowser().getCurrentUrl());
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {

		System.out.println("PreCrawl");

	}

	private Eventable getCorrespondingEventable(WebElement webElement, Identification identification,
			EventType eventType, EmbeddedBrowser browser) {
		CandidateElement candidateElement = getCorrespondingCandidateElement(webElement, identification, browser);
		Eventable event = new Eventable(candidateElement, eventType);
		System.out.println(event);
		return event;
	}

	public org.w3c.dom.Element getElementFromXpath(String xpathToRetrieve, EmbeddedBrowser browser)
			throws XPathExpressionException {
		Document dom;
		org.w3c.dom.Element element = null;
		try {
			xpathToRetrieve = xpathToRetrieve.toUpperCase();
			String source = browser.getStrippedDomWithoutIframeContent();
			dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			// System.out.println(source);
			XPath xPath = XPathFactory.newInstance().newXPath();
			// System.out.println("value is " + xPath.evaluate(xpathToRetrieve, dom));
			// NodeList nodes = (NodeList)xPath.evaluate(xpathToRetrieve,
			// dom.getDocumentElement(), XPathConstants.NODESET);
			// System.out.println(nodes.getLength());
			// element = (Element) nodes.item(0);
			element = (org.w3c.dom.Element) xPath.evaluate(xpathToRetrieve, dom.getDocumentElement(),
					XPathConstants.NODE);
			// System.out.println("element.getNodeName(): " + element.getNodeName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return element;
	}

	private CandidateElement getCorrespondingCandidateElement(WebElement webElement, Identification identification,
			EmbeddedBrowser browser) {
		
		Document dom;
		try {
			dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());

			// Get the corresponding org.w3c.dom.Element of a WebElement
			// String xpath = getXPath(webElement);
			String xpath = identification.getValue();
			org.w3c.dom.Element sourceElement = getElementFromXpath(xpath, browser);
			// CandidateElement candidateElement = new CandidateElement(sourceElement, new
			// Identification(Identification.How.xpath, xpath), "");
			CandidateElement candidateElement = new CandidateElement(sourceElement, identification, "");
			LOG.debug("Found new candidate element: {} with eventableCondition {}", candidateElement.getUniqueString(),
					null);
			candidateElement.setEventableCondition(null);
			return candidateElement;

			/*
			 * Previous inefficient way for (CrawlElement crawlTag :
			 * config.getCrawlRules().getAllCrawlElements()) { // checking all tags defined
			 * in the crawlRules NodeList nodeList =
			 * dom.getElementsByTagName(crawlTag.getTagName());
			 * 
			 * //String xpath1 = getXPath(webElement); String xpath2 = null;
			 * org.w3c.dom.Element sourceElement = null;
			 * 
			 * for (int k = 0; k < nodeList.getLength(); k++){ sourceElement =
			 * (org.w3c.dom.Element) nodeList.item(k); // check if sourceElement is
			 * webElement if (checkEqulity(webElement, sourceElement)){ xpath2 =
			 * XPathHelper.getXPathExpression(sourceElement); //
			 * System.out.println("xpath : " + xpath2); CandidateElement candidateElement =
			 * new CandidateElement(sourceElement, new
			 * Identification(Identification.How.xpath, xpath2), "");
			 * LOG.debug("Found new candidate element: {} with eventableCondition {}",
			 * candidateElement.getUniqueString(), null);
			 * candidateElement.setEventableCondition(null); return candidateElement; } } }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("could not find the corresponding CandidateElement");
		return null;
	}

}
