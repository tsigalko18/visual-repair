package vision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class Util {

	/**
	 * Get filename name and extension separated. Suffix can be provided to be
	 * added to the name part.
	 * 
	 * @param fileName
	 *            filename to be split (e.g. abc.html)
	 * @param additionalSuffix
	 *            any suffix that should be added to the name part only of the
	 *            file (e.g. _test, 1)
	 * @return String array of size 2. Index 0 contains filename only and index
	 *         1 contains extension (e.g. arr[0] = abc_test1, arr[1] = .html)
	 */
	public static String[] getFileNameAndExtension(String fileName, String... additionalSuffix) {
		String[] fileNameArray = fileName.split(Constants.FILE_EXTENSION_REGEX);
		String[] returnFileNameArray = new String[2];

		returnFileNameArray[0] = fileNameArray[0];
		for (int i = 0; i < additionalSuffix.length; i++) {
			returnFileNameArray[0] = returnFileNameArray[0] + additionalSuffix[i];
		}

		String temp = "";
		for (int i = 1; i < fileNameArray.length; i++) {
			temp = temp + "." + fileNameArray[i];
		}
		returnFileNameArray[1] = temp;

		return returnFileNameArray;
	}

	private static int getSiblingIndex(String xPathElement) {
		String value = getValueFromRegex(Constants.REGEX_FOR_GETTING_INDEX, xPathElement);
		if (value == null)
			return -1;
		return Integer.parseInt(value);
	}

	private static String getElementId(String xPathElement) {
		return getValueFromRegex(Constants.REGEX_FOR_GETTING_ID, xPathElement);
	}

	public static String getValueFromRegex(String regex, String str) {
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public static WebElement getElementFromCoordinates(JavascriptExecutor js, double x, double y) {
		// convert absolute co-ordinate values to relative with respect to the
		// viewport
		// handle elements like embed which are javascript functions instead of
		// DOM elements. In this case, return parent

		String javscriptGetElementFromCoordinates = "var getElementFromCoordinates = function (x, y) " + "{"
				+ "var scrollx = window.scrollX;" + "var scrolly = window.scrollY;" + "var newx = x + scrollx;"
				+ "var newy = y + scrolly;" + "window.scrollTo(newx, newy);" + "scrollx = window.scrollX;"
				+ "scrolly = window.scrollY;"
				+ "var element = document.elementFromPoint((newx-scrollx), (newy-scrolly));"
				+ "while(typeof element === 'function')" + "{" + "element = element.parentElement;" + "}"
				+ "window.scrollTo(0, 0);" + "return element;" + "};"
				+ "return getElementFromCoordinates(arguments[0], arguments[1]);";
		return (WebElement) js.executeScript(javscriptGetElementFromCoordinates, x, y);
	}

	public static String getHtmlPageCharset(Document document) {
		String charsetName = Constants.DEFAULT_CHARSET;
		Element meta = document.select("meta[http-equiv=content-type], meta[charset]").first();
		if (meta != null) {
			String foundCharset = meta.hasAttr("http-equiv")
					? getValueFromRegex(Constants.CHARSET_REGEX, meta.attr("content")) : meta.attr("charset");
			if (foundCharset != null && foundCharset.length() != 0) {
				charsetName = foundCharset;
			}
		}
		return charsetName;
	}

	public static String getNextAvailableFileName(String dir, String baseFileName) {
		File file = null;
		int count = -1;
		String[] fileNameArray = getFileNameAndExtension(baseFileName);
		String newFileName = "";
		do {
			count++;
			newFileName = fileNameArray[0];
			if (count > 0) {
				newFileName = newFileName + "_" + count;
			}
			file = new File(dir + File.separatorChar + newFileName + fileNameArray[1]);

		} while (file != null && file.exists());

		return newFileName + fileNameArray[1];
	}

	public static List<Integer> getNumbersFromString(String string) {
		List<Integer> numbers = new ArrayList<Integer>();
		Pattern p = Pattern.compile("-?\\d+");
		Matcher m = p.matcher(string);
		while (m.find()) {
			numbers.add(Integer.valueOf(m.group()));
		}
		return numbers;
	}

	public static Double getDecimalNumberFromString(String string) {
		Pattern p = Pattern.compile("-?\\d+\\.?\\d*");
		Matcher m = p.matcher(string);
		if (m.find()) {
			return Double.valueOf(m.group());
		}
		return null;
	}

	public static String[] getPathAndFileNameFromFullPath(String fullPath) {
		String[] result = new String[2];

		File file = new File(fullPath);
		if (file.exists() && file.isFile()) {
			result[0] = file.getParent();
			result[1] = file.getName();
		}
		return result;
	}

	public static double getWeightedMean(List<Double> weights, List<Double> values) {
		double numerator = 0.0;
		double denominator = 0.0;

		for (int i = 0; i < weights.size(); i++) {
			numerator = numerator + (weights.get(i) * values.get(i));
			denominator = denominator + weights.get(i);
		}

		return numerator / denominator;
	}

	public static int getDistance(String expected, String actual) {
		String expectedArray[] = expected.split("/");
		String actualArray[] = actual.split("/");
		int expectedLength = expectedArray.length - 1;
		int actualLength = actualArray.length - 1;
		int distance;

		int matchingCount = 0;
		for (int i = 1; i < expectedArray.length && i < actualArray.length; i++) {
			if (expectedArray[i].equals(actualArray[i])) {
				matchingCount++;
			} else {
				break;
			}
		}

		distance = (actualLength - matchingCount) + (expectedLength - matchingCount);

		return distance;
	}

	public static boolean isPointInRectangle(int x, int y, int left, int top, int width, int height,
			boolean isBorderIncluded) {
		if (isBorderIncluded) {
			if (x >= left && y >= top && x <= (left + width) && y <= (top + height))
				return true;
		} else {
			if (x > left && y > top && x < (left + width) && y < (top + height))
				return true;
		}
		return false;
	}

	public static double convertNanosecondsToSeconds(long time) {
		return (double) time / 1000000000.0;
	}

	public static int getDecimalFromHex(String hex) {
		try {
			return Integer.parseInt(hex.replace("#", ""), 16);
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getHexFromDecimal(int dec) {
		// return "#" + Integer.toHexString(dec);
		return String.format("#%06X", (0xFFFFFF & dec));
	}

	public static String getHexFromRGB(int red, int green, int blue) {
		return String.format("#%02x%02x%02x", red, green, blue);
	}

	public static void drawRectangleOnImage(String imageFileName, String path, int x, int y, int w, int h)
			throws IOException {
		File imageFile = new File(path + File.separatorChar + imageFileName);
		BufferedImage img = ImageIO.read(imageFile);

		Graphics2D graph = img.createGraphics();
		graph.setColor(Color.BLACK);
		graph.drawRect(x, y, w, h);
		graph.dispose();

		ImageIO.write(img, Constants.IMAGE_EXTENSION, new File(path + File.separatorChar + imageFileName));
	}

}