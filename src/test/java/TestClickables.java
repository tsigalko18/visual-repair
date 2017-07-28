import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.WebDriverSingleton;
import utils.UtilsParser;

public class TestClickables {

	@Test
	public void testClickables() throws IOException, SAXException {

		String htmlFile = "Users/astocco/git/visual-repair/output/addressbook825/TestUserAdded/28-2after-TestUserAdded-28/edit.php.html";

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFile);
		WebDriver driver = instance.getDriver();

		System.out.println("If the page is correctly displayed, type anything to proceed further");
		Scanner scanner = new Scanner(System.in);
		scanner.next();
		scanner.close();

		File newpage = new File(htmlFile);
		FileUtils.write(newpage, driver.getPageSource());

		// build RTree for the HTML page
		HtmlDomTree dt = new HtmlDomTree(driver, htmlFile);
		dt.buildHtmlDomTree();

		/* get list of clickable elements */
		List<Node<HtmlElement>> clickables = new LinkedList<Node<HtmlElement>>();
		UtilsParser.extractClickablesFromHtmlPage(dt, clickables);

		System.out.println("Found " + clickables.size() + " clickables");
		for (Node<HtmlElement> node : clickables) {
			System.out.println(node.getData().getXPath());
		}
	}

}
