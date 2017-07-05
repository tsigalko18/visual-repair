package main.java.repair;

import java.util.LinkedList;
import java.util.List;

import main.java.datatype.*;

public class RepairMain {

	static List<HtmlElement> repairs;
	static EnhancedTestCase broken;
	static EnhancedTestCase correct;
	static EnhancedException ex;
	static HtmlDomTree oldDom;
	static String locator;
	static HtmlDomTree newDom;

	public List<HtmlElement> suggestRepair(HtmlDomTree oldDom, String locator, HtmlDomTree newDom) {

		repairs = new LinkedList<HtmlElement>();

		// locator error
		if (ex.getMessage().contains("Unable to locate element")) {

			// apply strategy 1
			repairs = searchLocatorWithinTheSameState();

			// apply strategy 2
			if (repairs.isEmpty())
				repairs = searchLocatorWithinNeighbouringhStates();

		
		} else if (ex.getMessage().contains("Assertion error")) {

			

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
		} else if (ex.getMessage().contains("Cannot locate element with text")) {
			
			// repair dropdownlist
			repairs = getNewDropdownlistAttributes();
		}

		return repairs;
	}

	private List<HtmlElement> searchLocatorWithinNeighbouringhStates() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<HtmlElement> searchLocatorWithinTheSameState() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<HtmlElement> getNewActualValue() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<HtmlElement> getNewDropdownlistAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
