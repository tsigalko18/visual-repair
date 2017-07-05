package main.java.utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.runner.Result;

import main.java.datatype.*;
import main.java.parser.ParseTest;

public class UtilsWater {

	/**
	 * Return the list of nodes in @param newTree
	 * that are found similar to @param oldNode 
	 * according to the @param similarityThreshold
	 * @param oldNode
	 * @param newTree
	 * @param similarityThreshold
	 * @return
	 */
	public static List<HtmlElement> getSimilarNodes(HtmlElement oldNode, HtmlDomTree newTree, double similarityThreshold) {
		
		List <HtmlElement> results = new LinkedList<HtmlElement>();
		return searchHtmlDomTreeByNode(oldNode, newTree.getRoot(), similarityThreshold, results);
		
	}

	public static HtmlElement getNodeByLocator(HtmlDomTree tree, String xpath) {
		return tree.searchHtmlDomTreeByXPath(xpath);
	}
	
	public static HtmlElement getNodeByLocator(HtmlDomTree tree, SeleniumLocator l) {
		
		if(l.getStrategy().equals("id")) { 
			return tree.searchHtmlDomTreeByAttribute("id", l.getValue());
		
		} else if(l.getStrategy().equals("className")) { 
			return tree.searchHtmlDomTreeByAttribute("class", l.getValue());
		
		} else if(l.getStrategy().equals("linkText")) { 
			return tree.searchHtmlDomTreeByAttribute("text", l.getValue());
		
		} else if(l.getStrategy().equals("name")) { 
			return tree.searchHtmlDomTreeByAttribute("name", l.getValue());
		
		} else if(l.getStrategy().equals("tagName")) { 
			return tree.searchHtmlDomTreeByTagName(l.getValue());
		
		} else if(l.getStrategy().equals("xpath")) { 
			// differentiate further!!!
			// supports only absolute xpaths
			return tree.searchHtmlDomTreeByXPath(l.getValue());
		}
		return null;
	}

	public static HtmlElement getNodesByProperty(HtmlDomTree tree, String attribute, String value) {
		return tree.searchHtmlDomTreeByAttribute(attribute, value);
	}

	public static boolean checkRepair(EnhancedTestCase t) throws IOException {
		Result r = ParseTest.runTest(t, t.getPath());
		return r.wasSuccessful();
	}
	
	public static List<HtmlElement> searchHtmlDomTreeByNode(HtmlElement searchNode, Node<HtmlElement> newTree, double similarityThreshold, List<HtmlElement> similarNodes) {
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(newTree);

		while (!q.isEmpty()) {
			Node<HtmlElement> node = q.remove();
			if (getSimilarityScore(node.getData(), searchNode) > similarityThreshold) {
				similarNodes.add(node.getData());
			}
			if (node.getChildren() != null) {
				for (Node<HtmlElement> child : node.getChildren()) {
					q.add(child);
				}
			}
		}
		return similarNodes;
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1,
						distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

		return distance[str1.length()][str2.length()];
	}

	private static double getSimilarityScore(HtmlElement a, HtmlElement b) {
		double alpha = 0.9;
		double rho, rho1, rho2 = 0;
		
		if (a.getTagName().equals(b.getTagName())) {
			double levDist = computeLevenshteinDistance(a.getXPath(), b.getXPath());
			rho1 = 1 - levDist / Math.max(a.getXPath().length(), b.getXPath().length());

			if (Math.abs(a.getX() - b.getX()) <= 5 && Math.abs((a.getX() + a.getWidth()) - (b.getY() - b.getHeight())) <= 5
			 && Math.abs(a.getY() - b.getY()) <= 5 && Math.abs((a.getY() + a.getWidth()) - (b.getY() - b.getHeight())) <= 5) {
				rho2 = rho2 + 1;
			}
			rho2 = rho2 / 2;
			rho = (rho1 * alpha + rho2 * (1 - alpha));

			return rho;
		}
		return 0;
	}
}
