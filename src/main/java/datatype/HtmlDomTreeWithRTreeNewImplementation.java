package datatype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;

import config.Settings;
import rx.Observable;
import utils.HtmlAttributesParser;
import utils.UtilsParser;

public class HtmlDomTreeWithRTreeNewImplementation {

	private Node<HtmlElement> root;
	private Map<Integer, Rectangle> rects;
	private int rectId;
	private Map<Integer, Node<HtmlElement>> rectIdHtmlDomTreeNodeMap;
	private HtmlAttributesParser htmlAttributesParser;

	private RTree<Integer, Rectangle> tree;

	public HtmlDomTreeWithRTreeNewImplementation(WebDriver driver, String htmlFileFullPath)
			throws SAXException, IOException {

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

		// Create and initialize an RTree
		tree = RTree.create();
		rects = new HashMap<Integer, Rectangle>();
		rectIdHtmlDomTreeNodeMap = new HashMap<Integer, Node<HtmlElement>>();
		Rectangle r = Geometries.rectangle(x, y, x + w, y + h);
		rects.put(rectId, r);
		rectIdHtmlDomTreeNodeMap.put(rectId, root);
		tree.add(rectId++, r);

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

					Rectangle r = Geometries.rectangle(x, y, x + w, y + h);
					rects.put(rectId, r);
					tree.add(rectId++, r);

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

	public Node<HtmlElement> searchRTreeByPoint(int x, int y) {

		Point point = Geometries.point(x, y);

		Observable<Entry<Integer, Rectangle>> results = tree.nearest(point, 100, 1);
		List<Entry<Integer, Rectangle>> asList = results.toList().toBlocking().single();
		Entry<Integer, Rectangle> entry = asList.get(0);

		return rectIdHtmlDomTreeNodeMap.get(entry.value());

	}

	public Set<Node<HtmlElement>> searchRTreeByPoint(int x, int y, int distance, int numEntries) {

		Set<Node<HtmlElement>> finalResults = new HashSet<Node<HtmlElement>>();
		Point point = Geometries.point(x, y);

		Observable<Entry<Integer, Rectangle>> results = tree.nearest(point, distance, numEntries);
		List<Entry<Integer, Rectangle>> asList = results.toList().toBlocking().single();

		for (Entry<Integer, Rectangle> entry : asList) {
			finalResults.add(rectIdHtmlDomTreeNodeMap.get(entry.value()));
		}

		return finalResults;

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
		if (node.getChildren() != null) {
			for (Node<HtmlElement> child : node.getChildren()) {
				preOrderTraversalRTree(child);
			}
		}
	}

	public RTree<Integer, Rectangle> getTree() {
		return tree;
	}

	public void setTree(RTree<Integer, Rectangle> tree) {
		this.tree = tree;
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
