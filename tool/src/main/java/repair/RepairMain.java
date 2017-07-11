package main.java.repair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import main.java.datatype.EnhancedException;
import main.java.datatype.EnhancedTestCase;
import main.java.datatype.HtmlDomTree;
import main.java.datatype.HtmlElement;

public class RepairMain {

	static List<HtmlElement> repairs;
	static EnhancedTestCase broken;
	static EnhancedTestCase correct;
	static EnhancedException ex;
	static HtmlDomTree oldDom;
	static String locator;
	static HtmlDomTree newDom;

	public static List<HtmlElement> suggestRepair(EnhancedException e, EnhancedTestCase b, EnhancedTestCase c) throws SAXException, IOException {

		repairs = new LinkedList<HtmlElement>();

		// locator error
		if (e.getMessage().contains("Unable to locate element")) {

			// apply strategy 1
			repairs = ElementRelocatedSameState.searchLocatorWithinTheSameState(e, b, c);

			// apply strategy 2
			if (repairs.isEmpty())
				repairs = searchLocatorWithinNeighbouringhStates();

		
		} else if (e.getMessage().contains("Assertion error")) {

			// assertion error
			if (ex.getMessage().contains("Assertion value")) {

				// repair assertion with the actual value
				repairs = getNewActualValue();

			} else {
				// negate assertion?
			}
			
			if (!repairs.isEmpty())
				return repairs;

			// TODO: to manage
			// repairs.add(checkRepair());
		} else if (e.getMessage().contains("Cannot locate element with text")) {
			
			// repair dropdownlist
			repairs = getNewDropdownlistAttributes();
		}

		return repairs;
	}

	private static List<HtmlElement> searchLocatorWithinNeighbouringhStates() {
		// TODO Auto-generated method stub
		return null;
	}

	private static List<HtmlElement> getNewActualValue() {
		// TODO Auto-generated method stub
		return null;
	}

	private static List<HtmlElement> getNewDropdownlistAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
