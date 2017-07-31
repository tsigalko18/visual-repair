package visualrepair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTree;
import datatype.HtmlElement;

public class RepairStrategies {

	static List<EnhancedTestCase> repairs;
	static EnhancedTestCase broken;
	static EnhancedTestCase correct;
	static EnhancedException ex;
	static HtmlDomTree oldDom;
	static String locator;
	static HtmlDomTree newDom;

	public static List<EnhancedTestCase> suggestRepair(EnhancedException e, EnhancedTestCase b, EnhancedTestCase c)
			throws SAXException, IOException {

		repairs = new LinkedList<EnhancedTestCase>();

		// locator error
		if (e.getMessage().contains("Unable to locate element")) {

			// apply strategy 1
//			repairs.addAll(ElementRelocatedSameState.searchLocatorWithinTheSameState(e, b, c));

			// apply strategy 2
//			if (repairs.isEmpty())
//				repairs.addAll(MisSelection.searchForMisSelection(e, b, c));
			
			// apply strategy 3
			if (repairs.isEmpty())
				 repairs.addAll(ElementMovedNewState.searchElementNewState(e, b, c));
			

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

	private static List<EnhancedTestCase> getNewActualValue() {
		// TODO Auto-generated method stub
		return null;
	}

	private static List<EnhancedTestCase> getNewDropdownlistAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
