import static org.junit.Assert.assertTrue;

import org.junit.Test;

import datatype.SeleniumLocator;
import utils.UtilsParser;

public class TestUtilsParser {

	@Test
	public void testGetUrlFromDriverGet() throws Exception {

		String st = "driver.get(\"http://localhost:8888/claroline/issta2018/claroline-1.11.0/index.php\");";

		assertTrue(UtilsParser.getUrlFromDriverGet(st)
				.equals("http://localhost:8888/claroline/issta2018/claroline-1.11.0/index.php"));

		st = "stringwithoutquotes";

		try {
			UtilsParser.getUrlFromDriverGet(st);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetValueFromSendKeys() throws Exception {

		String st = "driver.findElement(By.id(\"login\")).sendKeys(\"admin\");";

		assertTrue(UtilsParser.getValueFromSendKeys(st).equals("admin"));

		st = "stringwithoutquotes";

		try {
			UtilsParser.getValueFromSendKeys(st);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetDomLocatorStatement() throws Exception {

		String st = "driver.findElement(By.id(\"login\")).sendKeys(\"admin\");";

		SeleniumLocator loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("id"));
		assertTrue(loc.getValue().equals("login"));

		st = "driver.findElement(By.xpath(\"//*[@id='loginBox']/form/fieldset/button\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("xpath"));
		assertTrue(loc.getValue().equals("//*[@id='loginBox']/form/fieldset/button"));

		st = "driver.findElement(By.linkText(\"001 - Course001\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("linkText"));
		assertTrue(loc.getValue().equals("001 - Course001"));

		st = "driver.findElement(By.name(\"submitEvent\")).click();";

		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("name"));
		assertTrue(loc.getValue().equals("submitEvent"));

		st = "assertTrue(driver.findElement(By.xpath(\"//*[@id='claroBody']/div[2]/div\")).getText().contains(\"You have just created the course website : 003\"));";
		
		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("xpath"));
		assertTrue(loc.getValue().equals("//*[@id='claroBody']/div[2]/div"));
		
		st = "driver.findElement(By.cssSelector(\"img[alt='Edit']\")).click();";
		
		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("cssSelector"));
		assertTrue(loc.getValue().equals("img[alt='Edit']"));
		
		st = "driver.findElement(By.cssSelector(\"a.msremove > img\")).click();";
		
		loc = UtilsParser.getDomLocator(st);

		assertTrue(loc.getStrategy().equals("cssSelector"));
		assertTrue(loc.getValue().equals("a.msremove > img"));
		
		st = "";

		try {
			UtilsParser.getDomLocator(st);
		} catch (Exception e) {
			assertTrue(true);
		}

		st = "notalocator";

		try {
			UtilsParser.getDomLocator(st);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	// @Test
	// public void testGetDomLocatorWebElement() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetSeleniumLocatorFromWebElement() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetValueFromSelect() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetAssertion() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetPredicate() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetValueFromAssertion() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSerializeTestCase() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSerializeException() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSerializeHtmlDomTree() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testToJsonPath() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testReadException() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetElementXPath() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetElementFromXPathJava() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetValueFromRegex() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testIsPointInRectangle() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetFailedTestFromFailure() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetExceptionFromFailure() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMessageFromFailure() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetLineFromFailure() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testConvertToHashMap() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testPrintResults() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testExtractClickablesFromHtmlPage() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetTestSuiteNameFromWithinType() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetPackageName() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSaveDOMInformation() {
	// fail("Not yet implemented");
	// }

}
