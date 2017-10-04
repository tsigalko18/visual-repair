package utils;

import java.util.List;

import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class UtilsXPath {

	public static String generateXPathForWebElement(WebElement childElement, String current) {
		String childTag = childElement.getTagName();
		if (childTag.equals("html")) {
			return "html[1]" + current;
		}
		WebElement parentElement = childElement.findElement(By.xpath(".."));
		List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
		int count = 0;
		for (int i = 0; i < childrenElements.size(); i++) {
			WebElement childrenElement = childrenElements.get(i);
			String childrenElementTag = childrenElement.getTagName();
			if (childTag.equals(childrenElementTag)) {
				count++;
			}
			if (childElement.equals(childrenElement)) {
				return generateXPathForWebElement(parentElement, "/" + childTag + "[" + count + "]" + current);
			}
		}
		return null;
	}

	public static String getXPathFromLocation(Point matches, WebDriver driver) {
		String getXpathScript = "window.getPathTo = function(element) {"
				// + " if (element.id!=='') "
				// + " return 'id(\"'+element.id+'\")'; "
				+ "	if (element===document.body) " + "		return element.tagName; " + "	var ix= 0; "
				+ "	var siblings= element.parentNode.childNodes; " + "	for (var i= 0; i<siblings.length; i++) {"
				+ "		var sibling= siblings[i];" + "		if (sibling===element)"
				+ "			return getPathTo(element.parentNode)+'/'+element.tagName+'['+(ix+1)+']';"
				+ "		if (sibling.nodeType===1 && sibling.tagName===element.tagName)" + " 			ix++;" + "	}"
				+ "};";
		String elemFromPoint = "elemForPoint = document.elementFromPoint(" + matches.x + "," + matches.y + ");";
		String getXpathFromPoint = "window.elemFromPoint = window.getPathTo(elemForPoint);";
		((JavascriptExecutor) driver).executeScript(getXpathScript + elemFromPoint + getXpathFromPoint);
		String xpath = (String) ((JavascriptExecutor) driver).executeScript("return window.elemFromPoint");
		String result = "HTML/" + xpath;
		return result.toLowerCase();
	}

}