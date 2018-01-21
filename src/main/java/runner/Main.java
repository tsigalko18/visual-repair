package runner;

import java.io.IOException;

public class Main {

	String prefix;
	String className;
	static long startTime;
	static long stopTime;
	static long elapsedTime;

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		/* package name. */
		String prefix = "addressbook411" + ".";

		/* class name. */
		String className = "AddressBookSearchAddressBookNameTest";

		visualRunner(prefix, className);
	}

	public static void visualRunner(String prefix, String className) {

		startTime = System.currentTimeMillis();

		VisualAssertionTestRunnerWithCrawler var = new VisualAssertionTestRunnerWithCrawler();

		try {
			var.runTestWithVisualAssertion(prefix, className);
		} catch (IOException e) {
			e.printStackTrace();
		}

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.format("\nelapsedTime (s): %.3f", elapsedTime / 1000.0f);
		
	}
}