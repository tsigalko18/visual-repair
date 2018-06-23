package runner;

import java.io.IOException;

import config.Settings.RepairMode;

public class Main {

	String prefix;
	String className;
	static long startTime;
	static long stopTime;
	static long elapsedTime;

	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		/* package name. */
		String prefix = "clarolineNew" + ".";

		/* class name. */
		String className = "TestLoginAdmin";

		/* repair mode. */
		RepairMode rm = RepairMode.VISUAL;

		/* execute visual-augmented test repair. */
		runWebTestRepair(prefix, className, rm);
	}

	/**
	 * Convenience procedure to launch the visual web test repair function and
	 * measure the running time.
	 * 
	 * @param prefix
	 * @param className
	 * @param rm
	 */
	public static void runWebTestRepair(String prefix, String className, RepairMode rm) {

		startTime = System.currentTimeMillis();

		VisualTestRepair var = new VisualTestRepair(rm);

		try {
			var.runTestWithVisualValidation(prefix, className);
		} catch (IOException e) {
			e.printStackTrace();
		}

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.format("\nelapsedTime (s): %.3f", elapsedTime / 1000.0f);

	}
}