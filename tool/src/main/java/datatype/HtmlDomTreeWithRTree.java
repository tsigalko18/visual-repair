package main.java.datatype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import main.java.config.Settings;
import main.java.utils.HtmlAttributesParser;
import main.java.utils.UtilsParser;

public class HtmlDomTreeWithRTree {

	Logger rootLogger = Logger.getRootLogger();

	private Node<HtmlElement> root;
	private SpatialIndex spatialIndex;
	private Map<Integer, Rectangle> rects;
	private int rectId;
	private Map<Integer, Node<HtmlElement>> rectIdHtmlDomTreeNodeMap;
	private HtmlAttributesParser htmlAttributesParser;

	public HtmlDomTreeWithRTree(WebDriver driver, String htmlFileFullPath) throws SAXException, IOException {

		rootLogger.setLevel(Level.OFF);
		rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));

		// get the root element
		List<WebElement> elements = driver.findElements(By.xpath("//*"));
		WebElement rootElementFromSelenium = elements.get(0);
		HtmlElement htmlRootElement = new HtmlElement();
		int x = rootElementFromSelenium.getLocation().x;
		int y = rootElementFromSelenium.getLocation().y;
		int w = rootElementFromSelenium.getSize().width;
		int h = rootElementFromSelenium.getSize().height;

		// parse HTML attributes
		htmlAttributesParser = new HtmlAttributesParser(htmlFileFullPath);

		htmlRootElement.setSeleniumWebElement(rootElementFromSelenium);
		htmlRootElement.setTagName(rootElementFromSelenium.getTagName());
		htmlRootElement.setX(x);
		htmlRootElement.setY(y);
		htmlRootElement.setWidth(w);
		htmlRootElement.setHeight(h);
		this.root = new Node<HtmlElement>(null, htmlRootElement);
		htmlRootElement.setXPath(computeXPath(this.root));
		htmlRootElement.setHtmlAttributes(htmlAttributesParser.getHTMLAttributesForElement(htmlRootElement.getXPath()));

		htmlRootElement.setRectId(rectId);

		// Create and initialize an rtree
		spatialIndex = new RTree();
		spatialIndex.init(null);
		rects = new HashMap<Integer, Rectangle>();
		rectIdHtmlDomTreeNodeMap = new HashMap<Integer, Node<HtmlElement>>();

		Rectangle r = new Rectangle(x, y, x + w, y + h);
		rects.put(rectId, r);
		rectIdHtmlDomTreeNodeMap.put(rectId, root);
		spatialIndex.add(r, rectId++);
	}

	public void buildHtmlDomTree() {
		buildHtmlDomTreeFromNode(this.root);
	}

	private void buildHtmlDomTreeFromNode(Node<HtmlElement> node) {
		try {
			List<WebElement> children = node.getData().getSeleniumWebElement().findElements(By.xpath("*"));
			for (WebElement child : children) {
				int x = child.getLocation().x;
				int y = child.getLocation().y;
				int w = child.getSize().width;
				int h = child.getSize().height;

				// adjust size of option to that of the parent (select)
				if (child.getTagName().equals("option")) {
					if (node.getData().getTagName().equals("select")) {
						x = node.getData().getX();
						y = node.getData().getY();
					}
				}

				// don't process elements with no visual impact
				// if(x >= 0 && y >= 0 && w > 0 && h > 0)
				if (!Arrays.asList(Settings.NON_VISUAL_TAGS).contains(child.getTagName())) {
					HtmlElement newChild = new HtmlElement();

					// set tag name
					newChild.setTagName(child.getTagName());

					// set id
					newChild.setId(child.getAttribute("id"));

					// set web element
					newChild.setSeleniumWebElement(child);

					// set rectangle information
					newChild.setX(x);
					newChild.setY(y);
					newChild.setWidth(w);
					newChild.setHeight(h);

					Node<HtmlElement> newNode = new Node<HtmlElement>(node, newChild);
					// set xpath by traversing the built html dom tree
					newChild.setXPath(computeXPath(newNode));

					// set html attributes
					newChild.setHtmlAttributes(htmlAttributesParser.getHTMLAttributesForElement(newChild.getXPath()));

					newChild.setRectId(rectId);
					rectIdHtmlDomTreeNodeMap.put(rectId, newNode);

					Rectangle r = new Rectangle(x, y, x + w, y + h);
					rects.put(rectId, r);
					spatialIndex.add(r, rectId++);

					// if(newChild.getHtmlAttributes().get("value") != null &&
					// newChild.getHtmlAttributes().get("value").equals("Enter")){
					// System.out.println("===============");
					// System.out.println(rects.get(newChild.getRectId()));
					// System.out.println("===============");
					// }

					buildHtmlDomTreeFromNode(newNode);
				}
			}
		} catch (NoSuchElementException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * compute XPath of the invoking element from the root
	 */
	private String computeXPath(Node<HtmlElement> node) {
		return getElementTreeXPath(node);
	}

	private static String getElementTreeXPath(Node<HtmlElement> node) {
		ArrayList<String> paths = new ArrayList<String>();
		for (; node != null; node = node.getParent()) {
			HtmlElement element = node.getData();
			int index = 0;

			int siblingIndex = node.getCurrentNodeSiblingIndex();
			for (Node<HtmlElement> sibling = node.getSiblingNodeAtIndex(--siblingIndex); sibling != null; sibling = node
					.getSiblingNodeAtIndex(--siblingIndex)) {
				if (sibling.getData().getTagName().equals(element.getTagName())) {
					++index;
				}
			}
			String tagName = element.getTagName().toLowerCase();
			String pathIndex = "[" + (index + 1) + "]";
			paths.add(tagName + pathIndex);
		}

		String result = null;
		if (paths.size() > 0) {
			result = "/";
			for (int i = paths.size() - 1; i > 0; i--) {
				result = result + paths.get(i) + "/";
			}
			result = result + paths.get(0);
		}

		return result;
	}

	public List<Node<HtmlElement>> searchRTreeByPoint(int x, int y) {
		final List<Node<HtmlElement>> resultSet = new ArrayList<Node<HtmlElement>>();
		final List<Integer> resultRectIds = new ArrayList<Integer>();

		final Point p = new Point(x, y);
		spatialIndex.nearest(p, new gnu.trove.TIntProcedure() {
			public boolean execute(int i) {
				resultRectIds.add(i);
				return true;
			}
		}, Float.MAX_VALUE);

		// filter result set based on containment relationship
		for (Integer id : resultRectIds) {
			// System.out.println(resultRectIds);
			List<Integer> containedElementsRectIds = getContainedElements(id);

			for (Integer cid : containedElementsRectIds) {
				HtmlElement containingElement = rectIdHtmlDomTreeNodeMap.get(id).getData();
				HtmlElement containedElement = rectIdHtmlDomTreeNodeMap.get(cid).getData();

				// check if the containing and contained element don't have the
				// same size
				if (resultRectIds.contains(cid) && containingElement.getX() <= containedElement.getX()
						&& containingElement.getY() <= containedElement.getY()
						&& containingElement.getWidth() > containedElement.getWidth()
						&& containingElement.getHeight() > containedElement.getHeight() && cid > id) {
					// System.out.println("rect " + id + " contains rect " +
					// cid);
					// keep contained element, remove containing element
					int index = resultRectIds.indexOf(id);
					resultRectIds.set(index, -1);
					break;
				}
			}
		}

		// clean results
		for (Integer id : resultRectIds) {
			if (id != -1) {
				resultSet.add(rectIdHtmlDomTreeNodeMap.get(id));
			}
		}

		// further filter the results based on XPath containment
		// this is necessary because there can be some children which are
		// outside parent
		// causing both the children and parent to be reported in error
		Map<Integer, String> xpaths = new HashMap<Integer, String>();
		for (Node<HtmlElement> node : resultSet) {
			xpaths.put(node.getData().getRectId(), node.getData().getXPath());
		}

		for (Integer key : xpaths.keySet()) {
			for (Integer key2 : xpaths.keySet()) {
				// check that it not the same element itself
				if (key != key2 && xpaths.get(key2) != null && xpaths.get(key) != null
						&& xpaths.get(key2).contains(xpaths.get(key))) {
					HtmlElement ele1 = rectIdHtmlDomTreeNodeMap.get(key).getData();
					HtmlElement ele2 = rectIdHtmlDomTreeNodeMap.get(key2).getData();

					if (ele1.getX() != ele2.getX() && ele1.getY() != ele2.getY() && ele1.getWidth() != ele2.getWidth()
							&& ele1.getHeight() != ele2.getHeight()) {
						xpaths.put(key, null);
						break;
					}
				}
			}
		}

		List<Node<HtmlElement>> finalResultSet = new ArrayList<Node<HtmlElement>>();
		for (Integer key : xpaths.keySet()) {
			if (xpaths.get(key) != null) {
				finalResultSet.add(rectIdHtmlDomTreeNodeMap.get(key));
			}
		}

		return finalResultSet;
	}

	private List<Integer> getContainedElements(final int rectId) {
		final List<Integer> resultRectIds = new ArrayList<Integer>();
		spatialIndex.contains(rects.get(rectId), new gnu.trove.TIntProcedure() {
			public boolean execute(int i) {
				if (i != rectId) {
					resultRectIds.add(i);
				}
				return true;
			}
		});
		return resultRectIds;
	}

	public Node<HtmlElement> searchHtmlDomTreeByRectId(int rectId) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (node.getData().getRectId() == rectId) {
				return node;
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public Node<HtmlElement> searchHtmlDomTreeByNode(Node<HtmlElement> searchNode) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (node.equals(searchNode)) {
				return node;
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public HtmlElement searchHtmlDomTreeByXpath(String xpath) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (node.getData().getXPath().equalsIgnoreCase(xpath)) {
				return node.getData();
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return null;
	}

	public Node<HtmlElement> searchHtmlDomTreeByPoint(int x, int y) {
		return searchHtmlDomTreeByPoint(this.root, x, y);
	}

	public Node<HtmlElement> searchHtmlDomTreeByPoint(Node<HtmlElement> node, int x, int y) {
		// traverse in pre-order
		// for visit, check if the node contains this point
		// if yes, go to children
		// if node is leaf and contains the point return node
		// else return parent

		HtmlElement element = node.getData();
		if (node.getChildren() == null && UtilsParser.isPointInRectangle(x, y, element.getX(), element.getY(),
				element.getWidth(), element.getHeight(), true)) {
			return node;
		} else {
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					if (UtilsParser.isPointInRectangle(x, y, child.getData().getX(), child.getData().getY(),
							child.getData().getWidth(), child.getData().getHeight(), true)) {
						node = searchHtmlDomTreeByPoint(child, x, y);
						return node;
					}
				}
			}
			return node;
		}
	}

	public void preOrderTraversalRTree() {
		preOrderTraversalRTree(this.root);
	}

	private void preOrderTraversalRTree(Node<HtmlElement> node) {
		if (node == null) {
			return;
		}
		// System.out.println(node.getData().getTagName() + ": " +
		// node.getData());
		if (node.getChildren() != null) {
			for (Node<HtmlElement> child : node.getChildren()) {
				preOrderTraversalRTree(child);
			}
		}
	}

	public Logger getRootLogger() {
		return rootLogger;
	}

	public void setRootLogger(Logger rootLogger) {
		this.rootLogger = rootLogger;
	}

	public SpatialIndex getSpatialIndex() {
		return spatialIndex;
	}

	public void setSpatialIndex(SpatialIndex spatialIndex) {
		this.spatialIndex = spatialIndex;
	}

	public Map<Integer, Rectangle> getRects() {
		return rects;
	}

	public void setRects(Map<Integer, Rectangle> rects) {
		this.rects = rects;
	}

	public int getRectId() {
		return rectId;
	}

	public void setRectId(int rectId) {
		this.rectId = rectId;
	}

	public Map<Integer, Node<HtmlElement>> getRectIdHtmlDomTreeNodeMap() {
		return rectIdHtmlDomTreeNodeMap;
	}

	public void setRectIdHtmlDomTreeNodeMap(Map<Integer, Node<HtmlElement>> rectIdHtmlDomTreeNodeMap) {
		this.rectIdHtmlDomTreeNodeMap = rectIdHtmlDomTreeNodeMap;
	}

	public HtmlAttributesParser getHtmlAttributesParser() {
		return htmlAttributesParser;
	}

	public void setHtmlAttributesParser(HtmlAttributesParser htmlAttributesParser) {
		this.htmlAttributesParser = htmlAttributesParser;
	}

	public Node<HtmlElement> getRoot() {
		return root;
	}

	public void setRoot(Node<HtmlElement> root) {
		this.root = root;
	}

}
