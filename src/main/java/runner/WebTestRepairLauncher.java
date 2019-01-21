package runner;

import java.io.IOException;

import config.Settings.RepairMode;

public class WebTestRepairLauncher {

	static long startTime;
	static long stopTime;
	static long elapsedTime;

	/* This demo class first runs the visual repair, and after executes the DOM-based repair. */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		/* set up the test class to validate/repair. */
		String packageName = "clarolineNew" + ".";
		String testClassName = "TestLoginAdmin";

		/* execute test repair through visual validation. */
		runWebTestRepair(packageName, testClassName, RepairMode.VISUAL);

		/* re-execute test repair using WATER strategy (DOM-based). */
		//runWebTestRepair(packageName, testClassName, RepairMode.DOM);
	}

	/**
	 * Convenience procedure to launch the visual web test repair function and
	 * measure the elapsed running time.
	 * 
	 * @param packageName
	 * @param testClassName
	 * @param repairMode
	 */
	public static void runWebTestRepair(String packageName, String testClassName, RepairMode repairMode) {

		startTime = System.currentTimeMillis();

		VisualTestRepair vtp = new VisualTestRepair(repairMode);

		try {
			vtp.runTestWithVisualValidation(packageName, testClassName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.format("\nelapsedTime (s): %.3f\n\n", elapsedTime / 1000.0f);

	}
}