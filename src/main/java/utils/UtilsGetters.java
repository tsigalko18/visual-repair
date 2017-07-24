package utils;

import java.io.File;

public class UtilsGetters {

	public static File getExceptionFile(File file) {
		File[] exception = file.listFiles(FileFilters.exceptionFilter);
		if (exception.length == 1 && exception[0].getName().equals("exception.json"))
			return exception[0];
		return null;
	}

	public static boolean isTestBroken(File file) {
		File[] exception = file.listFiles(FileFilters.exceptionFilter);
		if (exception.length == 1 && exception[0].getName().equals("exception.json"))
			return true;
		return false;
	}

	public static File[] getVisualLocators(String directory) {
	
		File[] files = new File(directory).listFiles(FileFilters.visualLocatorFilter);
		return files;
	
	}

	public static File[] getAnnotatedScreenshots(String directory) {
	
		File[] files = new File(directory).listFiles(FileFilters.annotatedScreenshotsFilter);
		return files;
	
	}

	public static String getTestFile(String name, String pathToTestSuiteUnderTest) {
	
		File[] files = new File(pathToTestSuiteUnderTest).listFiles(FileFilters.javaFilesFilter);
		for (File file : files) {
			if (file.getName().contains(name)) {
				return file.getAbsolutePath();
			}
		}
	
		return null;
	}

}
