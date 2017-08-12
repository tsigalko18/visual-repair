package visualrepair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import utils.UtilsRepair;

public class ElementDeleted {

	static List<EnhancedTestCase> deleteElementFromState(EnhancedException e, EnhancedTestCase b, EnhancedTestCase c,
			boolean check) throws SAXException, IOException, CloneNotSupportedException {

		System.out.println("[LOG]\tApplying visual repair strategy <deleteElementFromState>");

		/* get the line responsible for the breakage. */
		int brokenStatementLine = Integer.parseInt(e.getInvolvedLine());

		List<EnhancedTestCase> candidateRepairs = new LinkedList<EnhancedTestCase>();

		EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(b);
		temp.removeStatementAtPosition(brokenStatementLine);

		candidateRepairs.add(temp);

		return candidateRepairs;

	}

}
