package utils;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import config.Settings;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

public class UtilsComputerVision {

	protected static WebDriver driver;
	protected static Properties configFile;
	protected static String screenshotFolder;

	static {
		nu.pattern.OpenCV.loadShared();
	}

	/**
	 * Save the GUI of the current driver instance in a file name identified by
	 * filename.
	 * 
	 * @param d
	 * @param filename
	 * @throws AWTException
	 * @throws HeadlessException
	 * @throws IOException
	 */
	public static void saveScreenshot(WebDriver d, String filename) {

		File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
		File destFile = new File(filename);

		try {
			FileUtils.copyFile(screenshot, destFile);
			screenshot = destFile;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save a unique visual locator for the web element.
	 * 
	 * @param d
	 * @param s
	 * @param we
	 * @param vl
	 */
	public static void saveVisualLocator(WebDriver d, String s, WebElement we, String vl) {

		try {
			UtilsComputerVision.getUniqueVisualLocator(d, s, we, vl);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Calculate a visual locator.
	 * 
	 * @param d
	 * @param filename
	 * @param element
	 * @param webElementImageName
	 * @throws IOException
	 */
	public static void getUniqueVisualLocator(WebDriver d, String filename, WebElement element,
			String webElementImageName) throws IOException {

		File destFile = new File(filename);
		BufferedImage img = ImageIO.read(destFile);

		File visualLocator = new File(webElementImageName);

		int scale = 4;
		getScaledSubImage(d, img, element, visualLocator, scale);

		while (!isUnique(destFile.getAbsolutePath(), visualLocator.getAbsolutePath())) {
			scale--;
			getScaledSubImage(d, img, element, visualLocator, scale);
		}

	}

	/**
	 * Save a precise crop for the web element (might not be unique).
	 * 
	 * @param d
	 * @param s
	 * @param we
	 * @param vl
	 */
	public static void saveVisualCrop(WebDriver d, String s, WebElement we, String vl) {

		try {
			UtilsComputerVision.getPreciseElementVisualCrop(d, s, we, vl);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Get the precise crop for the web element.
	 * 
	 * @param d
	 * @param filename
	 * @param element
	 * @param webElementImageName
	 * @throws IOException
	 */
	public static void getPreciseElementVisualCrop(WebDriver d, String filename, WebElement element,
			String webElementImageName) throws IOException {

		File destFile = new File(filename);
		BufferedImage img = ImageIO.read(destFile);

		File visualLocator = new File(webElementImageName);

		getPreciseSubImage(d, img, element, visualLocator);

	}

	/**
	 * Save the annotated screenshot locator.
	 * 
	 * @param d
	 * @param filename
	 * @param element
	 * @param webElementImageName
	 * @throws IOException
	 */
	public static void saveAnnotatedScreenshot(String inFile, String templateFile, String outFile) throws IOException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		// / Create the result matrix
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		// / Do the Matching and Normalize
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {
				if (result.get(i, j)[0] >= 0.99)
					matches.add(new Point(i, j));
			}
		}

		if (matches.size() == 0) {
			if (Settings.VERBOSE)
				System.err.println("[LOG]\tWARNING: No matches found!");
		} else if (matches.size() > 1) {
			if (Settings.VERBOSE)
				System.err.println("[LOG]\tWARNING: Multiple matches!");
		}

		/* Localizing the best match with minMaxLoc. */
		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Point matchLoc = mmr.maxLoc;

		/* Show me what you got. */
		Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
				new Scalar(0, 255, 0), 2);

		/* Save the visualized detection. */
		File annotated = new File(outFile);
		Highgui.imwrite(annotated.getPath(), img);
	}

	/**
	 * Get the visual locator.
	 * 
	 * @param d
	 * @param filename
	 * @param element
	 * @param webElementImageName
	 * @throws IOException
	 */
	public static void getScaledSubImage(WebDriver d, BufferedImage img, WebElement element, File visualLocator,
			int scale) throws IOException {

		org.openqa.selenium.Point elementCoordinates = null;
		driver = d;

		try {
			elementCoordinates = element.getLocation();
		} catch (StaleElementReferenceException e) {
			if (Settings.VERBOSE)
				System.out.println("[LOG]\ttest might have changed its state");
		}

		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();
		Rectangle rect = new Rectangle(width, height);
		BufferedImage subImage = null;

		int min_offset_x = Math.min(element.getLocation().x, img.getWidth() - rect.width - element.getLocation().x);
		int min_offset_y = Math.min(element.getLocation().y, img.getHeight() - rect.height - element.getLocation().y);
		int offset = Math.min(min_offset_x, min_offset_y);
		// int offset = Math.max(min_offset_x, min_offset_y);
		offset = offset / scale;

		try {
			if (element.getTagName().equals("option")) {

				WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
				new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

				elementCoordinates = thisShouldBeTheSelect.getLocation();
				subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset,
						2 * offset + rect.width, 2 * offset + rect.height);
			} else {
				subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset,
						2 * offset + rect.width, 2 * offset + rect.height);
			}
		} catch (RasterFormatException e) {
			System.err.println("[LOG]\tWARNING: " + e.getMessage());
		}

		ImageIO.write(subImage, "png", visualLocator);
		subImage.flush();

	}

	/**
	 * Get the visual locator.
	 * 
	 * @param d
	 * @param filename
	 * @param element
	 * @param webElementImageName
	 * @throws IOException
	 */
	public static void getPreciseSubImage(WebDriver d, BufferedImage img, WebElement element, File visualLocator)
			throws IOException {

		org.openqa.selenium.Point elementCoordinates = null;
		driver = d;

		try {
			elementCoordinates = element.getLocation();
		} catch (StaleElementReferenceException e) {
			if (Settings.VERBOSE)
				System.out.println("[LOG]\ttest might have changed its state");
		}

		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();
		Rectangle rect = new Rectangle(width, height);
		BufferedImage subImage = null;

		int offset = 0;

		try {
			if (element.getTagName().equals("option")) {

				WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
				new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

				if (Settings.VERBOSE) {
					System.err
							.println("\n\nthisShouldBeTheSelect.getLocation(): " + thisShouldBeTheSelect.getLocation());
					System.err.println("element.getLocation(): " + element.getLocation());
				}

				elementCoordinates = thisShouldBeTheSelect.getLocation();
				subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset,
						2 * offset + rect.width, 2 * offset + rect.height);
			} else {
				subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset,
						2 * offset + rect.width, 2 * offset + rect.height);
			}
		} catch (RasterFormatException e) {
			System.err.println("[LOG]\tWARNING: " + e.getMessage());
		}

		ImageIO.write(subImage, "png", visualLocator);
		subImage.flush();

	}

	public static boolean isUnique(String inFile, String templateFile) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		if (Settings.VERBOSE) {
			System.out.println("[LOG]\tLoading library " + Core.NATIVE_LIBRARY_NAME
					+ " using image recognition algorithm TM_CCOEFF_NORMED");
			System.out.println("[LOG]\tSearching matches of " + templateFile + " in " + inFile);
		}

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		// / Create the result matrix
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		// Do the Matching and Normalize
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {
				if (result.get(i, j)[0] >= 0.99)
					matches.add(new Point(i, j));
			}
		}

		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No matches found!");
			return false;
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple matches!");
			return false;
		} else
			return true;

	}

	public static void resizeScreenshot(String path, double scale) throws IOException {
		Thumbnails.of(path).scale(scale).outputFormat("png").toFiles(Rename.NO_CHANGE);
	}

	public static Point findBestMatch(String inFile, String templateFile) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		if (Settings.VERBOSE) {
			System.out.println("[LOG]\tLoading library " + Core.NATIVE_LIBRARY_NAME
					+ " using image recognition algorithm TM_CCOEFF_NORMED");

			System.out.println("[LOG]\tSearching matches of " + templateFile + " in " + inFile);
		}

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		/* Create the result matrix. */
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		/* Do the Matching and Normalize. */
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {

				if (result.get(i, j)[0] >= 0.99) {
					matches.add(new Point(i, j));
				}

			}
		}

		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No matches found!");
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple matches!");
		}

		/* Localizing the best match with minMaxLoc. */
		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Point matchLoc = mmr.maxLoc;

		/* Show me what you got. */
		Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
				new Scalar(0, 255, 0), 2);

		/* Save the visualized detection. */
		File annotated = new File("annotated.png");
		Highgui.imwrite(annotated.getPath(), img);

		return matchLoc;
	}

	public static Point findBestMatchCenter(String inFile, String templateFile) {

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		/* Create the result matrix. */
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		/* Do the Matching and Normalize. */
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {

				if (result.get(i, j)[0] >= 0.99) {
					matches.add(new Point(i, j));
				}

			}
		}

		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No visual matches found!");
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple visual matches!");
		}

		/* Localizing the best match with minMaxLoc. */
		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Point matchLoc = mmr.maxLoc;
		
		if(mmr.maxVal > 0.95) {
			
			/* Show me what you got. */
			Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
					new Scalar(0, 255, 0), 2);

			/* Save the visualized detection. */
			File annotated = new File("annotated.png");
			Highgui.imwrite(annotated.getPath(), img);

			/*
			 * TODO: a way to check over or filter the results could be to get the subset
			 * image and compare it with the template.
			 */

			return new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
			
		} else
			return null;
		
	}

	public static Point findAllCenters(String inFile, String templateFile) {

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		/* Create the result matrix. */
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		// / Do the Matching and Normalize
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {

				if (result.get(i, j)[0] >= 0.99) {
					matches.add(new Point(i, j));
				}

			}
		}

		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No visual matches found!");
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple visual matches!");
		}

		/* Localizing the best match with minMaxLoc. */
		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Point matchLoc = mmr.maxLoc;

		/* Show me what you got. */
		Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
				new Scalar(0, 255, 0), 2);

		/* Save the visualized detection. */
		File annotated = new File("annotated.png");
		Highgui.imwrite(annotated.getPath(), img);

		return new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
	}

	public static List<Point> returnAllMatches(String inFile, String templateFile) {

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		File visuallocator = new File("visuallocator.png");
		Highgui.imwrite(visuallocator.getPath(), templ);
		templ = Highgui.imread(visuallocator.getPath());

		// / Create the result matrix
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		List<Point> matches = new LinkedList<Point>();
		List<Rectangle2D> boxes = new LinkedList<Rectangle2D>();

		if (Settings.VERBOSE) {
			System.out.println("[LOG]\tSearching matches of " + templateFile + " in " + inFile);
		}

		/* Do the Matching and Thresholding. */
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Imgproc.threshold(result, result, 0.1, 1, Imgproc.THRESH_TOZERO);
		double maxval;
		while (true) {
			Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
			Point maxp = maxr.maxLoc;
			maxval = maxr.maxVal;

			// Point maxop = new Point(maxp.x + templ.width(), maxp.y + templ.height());

			if (maxval >= 0.95) {

				Core.rectangle(img, maxp, new Point(maxp.x + templ.cols(), maxp.y + templ.rows()),
						new Scalar(0, 0, 255), 2);
				Core.rectangle(result, maxp, new Point(maxp.x + templ.cols(), maxp.y + templ.rows()),
						new Scalar(0, 255, 0), -1);

				matches.add(maxp);
				boxes.add(new Rectangle((int) maxp.x, (int) maxp.y, templ.cols(), templ.rows()));
			} else {
				break;
			}
		}

		System.out.println("Found " + matches.size() + " matches with input image");

		/*
		 * non-maxima suppression step to filter the results. Needs to be tested!
		 */
		// Rectangle2D picked = nonMaxSuppression(boxes);

		/* Save the visualized detection. */
		File annotated = new File("annotated.png");
		Highgui.imwrite(annotated.getPath(), img);

		return matches;
	}

	public static Rectangle2D nonMaxSuppression(List<Rectangle2D> boxes) {

		ArrayList<Rectangle2D> picked = new ArrayList<Rectangle2D>();

		if (boxes.size() == 0)
			return picked.get(0);

		// ArrayList<Integer> x1 = getAllX1(boxes);
		// ArrayList<Integer> y1 = getAllY1(boxes);
		// ArrayList<Integer> x2 = getAllX2(boxes);
		// ArrayList<Integer> y2 = getAllY2(boxes);
		// ArrayList<Double> area = getAllAreas(boxes);

		ArrayList<Rectangle2D> idxs = new ArrayList<Rectangle2D>();
		idxs.addAll(boxes);

		Comparator<Rectangle2D> comp = new RectangleComparator();
		Collections.sort(idxs, comp);

		ArrayList<Integer> pick = new ArrayList<Integer>();
		ArrayList<Integer> suppress = new ArrayList<Integer>();

		while (idxs.size() > 0) {

			int last = idxs.size() - 1;
			Rectangle2D i = idxs.get(last);
			pick.add(last);

			ArrayList<Rectangle2D> idxs_temp = new ArrayList<Rectangle2D>();
			idxs_temp.addAll(idxs);
			idxs_temp.remove(last);

			for (int pos = 0; pos < idxs_temp.size(); pos++) {

				Rectangle2D j = idxs_temp.get(pos);

				/*
				 * find the largest (x, y) coordinates for the start of the bounding box and the
				 * smallest (x, y) coordinates for the end of the bounding box.
				 */
				double xx1 = Math.max(i.getX(), j.getX());
				double yy1 = Math.max(i.getY(), j.getY());
				double xx2 = Math.min(i.getX() + i.getWidth(), j.getX() + j.getWidth());
				double yy2 = Math.min(i.getY() + i.getHeight(), j.getY() + j.getHeight());

				/* compute the width and height of the bounding box. */
				double w = Math.max(0, xx2 - xx1 + 1);
				double h = Math.max(0, yy2 - yy1 + 1);

				/*
				 * compute the ratio of overlap between the computed bounding box and the
				 * bounding box in the area list.
				 */
				double overlap = (w * h) / (j.getWidth() * j.getHeight());

				/*
				 * if there is sufficient overlap, suppress the current bounding box.
				 */
				if (overlap > 0.3)
					suppress.add(pos);
			}

			idxs.remove(i);
		}

		pick.removeAll(suppress);
		System.out.println("pick size: " + pick.size());
		System.out.println("pick: " + pick.get(0));
		return boxes.get(pick.get(0));
	}

	public static ArrayList<Integer> getAllX1(List<Rectangle2D> boxes) {
		ArrayList<Integer> x1 = new ArrayList<Integer>();
		for (Rectangle2D rect : boxes) {
			x1.add((int) rect.getX());
		}
		return x1;
	}

	public static ArrayList<Integer> getAllY1(List<Rectangle2D> boxes) {
		ArrayList<Integer> y1 = new ArrayList<Integer>();
		for (Rectangle2D rect : boxes) {
			y1.add((int) rect.getY());
		}
		return y1;
	}

	public static ArrayList<Integer> getAllX2(List<Rectangle2D> boxes) {
		ArrayList<Integer> x2 = new ArrayList<Integer>();
		for (Rectangle2D rect : boxes) {
			x2.add((int) (rect.getX() + rect.getWidth()));
		}
		return x2;
	}

	public static ArrayList<Integer> getAllY2(List<Rectangle2D> boxes) {
		ArrayList<Integer> y2 = new ArrayList<Integer>();
		for (Rectangle2D rect : boxes) {
			y2.add((int) (rect.getY() + rect.getHeight()));
		}
		return y2;
	}

	public static ArrayList<Double> getAllAreas(List<Rectangle2D> boxes) {
		ArrayList<Double> area = new ArrayList<Double>();

		for (int i = 0; i < boxes.size(); i++) {
			area.add(boxes.get(i).getWidth() * boxes.get(i).getHeight());
		}

		return area;
	}

	public static List<Point> matchUsingCanny(String inFile, String templateFile) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		if (Settings.VERBOSE) {
			System.out.println("[LOG]\tLoading library " + Core.NATIVE_LIBRARY_NAME
					+ " using image recognition algorithm TM_CCOEFF_NORMED");

			System.out.println("[LOG]\tSearching matches of " + templateFile + " in " + inFile);
		}

		Mat img = Highgui.imread(inFile);
		Mat grayImage = new Mat();

		Imgproc.cvtColor(img, grayImage, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur(grayImage, img, new Size(3, 3));
		Imgproc.Canny(img, img, 300, 600, 5, true);

		Mat dest = new Mat();
		Core.add(dest, Scalar.all(0), dest);

		File annotated = new File("canny.png");
		Highgui.imwrite(annotated.getPath(), img);
		//
		img = Highgui.imread(annotated.getPath());
		//
		Mat templ = Highgui.imread(templateFile);
		Mat grayImageTempl = new Mat();

		Imgproc.cvtColor(templ, grayImageTempl, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur(grayImageTempl, templ, new Size(3, 3));
		Imgproc.Canny(templ, templ, 300, 600, 5, true);

		dest = new Mat();
		Core.add(dest, Scalar.all(0), dest);

		File visuallocator = new File("visuallocator.png");
		Highgui.imwrite(visuallocator.getPath(), templ);

		templ = Highgui.imread(visuallocator.getPath());

		// / Create the result matrix
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		int methods[] = { Imgproc.TM_SQDIFF_NORMED, Imgproc.TM_SQDIFF, Imgproc.TM_CCOEFF_NORMED, Imgproc.TM_CCOEFF,
				Imgproc.TM_CCORR, Imgproc.TM_CCORR_NORMED };

		List<Point> matches = new LinkedList<Point>();
		List<Point> bestMatches = new LinkedList<Point>();

		for (int meth : methods) {

			/* Do the Matching and Normalize. */
			Imgproc.matchTemplate(img, templ, result, meth);
			Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

			for (int i = 0; i < result_rows; i++) {
				for (int j = 0; j < result_cols; j++) {
					if (result.get(i, j)[0] >= 0.99) {
						matches.add(new Point(i, j));
					}
				}
			}

			/* TODO: maxLoc does not work for all methods. */
			MinMaxLocResult mmr = Core.minMaxLoc(result);
			Point matchLoc = mmr.maxLoc;
			bestMatches.add(matchLoc);

		}

		for (Point m : bestMatches) {
			/* Show me what you got. */
			Core.rectangle(img, m, new Point(m.x + templ.cols(), m.y + templ.rows()), new Scalar(0, 255, 0), 1);
		}

		/* Save the visualized detection. */
		annotated = new File("annotated.png");
		Highgui.imwrite(annotated.getPath(), img);

		return bestMatches;
	}

	public static boolean isAlertPresent(WebDriver d) {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException Ex) {
			return false;
		}
	}

	/**
	 * Converts a PNG image to JPG
	 * 
	 * @param path
	 *            to PNG image
	 * @return path to new JPG image
	 */
	public static String convertPngToJpg(String imgPath) {

		BufferedImage bufferedImage;
		String newPath = imgPath.replace("png", "jpg");

		try {

			/* read image file. */
			bufferedImage = ImageIO.read(new File(imgPath));

			/* create a blank, RGB, same width and height, and a white background.˙ */
			BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

			/* write to jpeg file. */
			ImageIO.write(newBufferedImage, "jpg", new File(newPath));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return newPath;

	}
}
