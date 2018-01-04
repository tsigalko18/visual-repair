package utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class UtilsTemplateMatching {

	static {
		nu.pattern.OpenCV.loadShared();
	}

	/* Ad-hoc visual locator detector feature. */
	public static Point featureDetectorAndTemplateMatching(String imageFile, String templateFile) {

		Point result = null;

		/* run SIFT and FAST to check for the presence/absence of the template image. */
		boolean isPresent = runFeatureDetection(templateFile, imageFile);

		if (isPresent) {
			result = templateMatching(templateFile, imageFile);
		}

		return result;

	}

	/*
	 * Run the FAST and SIFT feature detector algorithms on the two input images and
	 * try to match the features found in @object image into the @scene image
	 * 
	 */
	public static boolean runFeatureDetection(String templ, String img) {
		boolean sift = siftDetector(templ, img);
		boolean fast = fastDetector(templ, img);
		return (sift || fast);
	}

	/*
	 * Run the FAST feature detector algorithms on the two input images and try to
	 * match the features found in @object image into the @scene image
	 * 
	 */
	private static boolean fastDetector(String object, String scene) {

		// System.out.println("FAST Detector");
		Mat objectImage = Highgui.imread(object, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat sceneImage = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);
		featureDetector.detect(objectImage, objectKeyPoints);
		// System.out.println("Detected key points by FAST: " +
		// objectKeyPoints.toList().size());

		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
		descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);

		/* Create output image. */
		Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar newKeypointColor = new Scalar(255, 0, 0);

		Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

		/* Match object image with the scene image. */
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
		// System.out.println("Detecting key points in background image...");
		featureDetector.detect(sceneImage, sceneKeyPoints);
		// System.out.println("Computing descriptors in background image...");
		descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

		Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar matchestColor = new Scalar(0, 255, 0);

		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		// System.out.println("Matching object and scene images...");
		descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

		// System.out.println("Calculating good match list...");
		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

		/* The threshold ratio used for the distance. */
		float nndrRatio = 0.7f;

		for (int i = 0; i < matches.size(); i++) {
			MatOfDMatch matofDMatch = matches.get(i);
			DMatch[] dmatcharray = matofDMatch.toArray();
			DMatch m1 = dmatcharray[0];
			DMatch m2 = dmatcharray[1];

			if (m1.distance <= m2.distance * nndrRatio) {
				goodMatchesList.addLast(m1);
			}
		}

		if (goodMatchesList.size() == 0) {
			return false;
		}

		System.out.println("Good matches (FAST): " + goodMatchesList.size());

		int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);

		System.out.println("Min matches (FAST): " + min_accepted_matches);

		if (goodMatchesList.size() > min_accepted_matches) {

			// System.out.println("Object Found!");

			List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
			List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

			LinkedList<Point> objectPoints = new LinkedList<Point>();
			LinkedList<Point> scenePoints = new LinkedList<Point>();

			for (int i = 0; i < goodMatchesList.size(); i++) {
				objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
				scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
			}

			MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
			objMatOfPoint2f.fromList(objectPoints);
			MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
			scnMatOfPoint2f.fromList(scenePoints);

			/* output filename. */
			String filename = object.toString();
			int i = filename.lastIndexOf("/");
			filename = filename.substring(i + 1, filename.length());
			filename = filename.replace(".png", "");

			/* visualize detected features. */
			Highgui.imwrite("output/templateMatching/FAST-" + filename + "-outputImage.jpg", outputImage);

			/* Get the rectangle the the potential match is. */
			try {
				Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.LMEDS, 3);
				Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
				Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

				obj_corners.put(0, 0, new double[] { 0, 0 });
				obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
				obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
				obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });

				// System.out.println("Transforming object corners to scene corners...");
				Core.perspectiveTransform(obj_corners, scene_corners, homography);

				Mat img = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_COLOR);

				Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)),
						new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)),
						new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)),
						new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)),
						new Scalar(255, 0, 0), 2);

				// System.out.println("Drawing matches image...");
				MatOfDMatch goodMatches = new MatOfDMatch();
				goodMatches.fromList(goodMatchesList);

				Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches,
						matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

				/* visualize feature detection. */
				Highgui.imwrite("output/templateMatching/FAST-" + filename + "-matchoutput.jpg", matchoutput);
				Highgui.imwrite("output/templateMatching/FAST-" + filename + "-img.jpg", img);
			} catch (Exception e) {
				System.out.println("Homography not found");
			}

			return true;

		} else {
			// System.out.println("Object Not Found");
			return false;
		}

	}

	/*
	 * Run the SIFT feature detector algorithms on the two input images and try to
	 * match the features found in @object image into the @scene image
	 * 
	 */
	private static boolean siftDetector(String object, String scene) {

		// System.out.println("SIFT Detector");
		Mat objectImage = Highgui.imread(object, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat sceneImage = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
		featureDetector.detect(objectImage, objectKeyPoints);
		// System.out.println("Detected key points by SIFT: " +
		// objectKeyPoints.toList().size());

		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
		descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);

		/* Create output image. */
		Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar newKeypointColor = new Scalar(255, 0, 0);

		Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

		/* Match object image with the scene image. */
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
		// System.out.println("Detecting key points in background image...");
		featureDetector.detect(sceneImage, sceneKeyPoints);
		// System.out.println("Computing descriptors in background image...");
		descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

		Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar matchestColor = new Scalar(0, 255, 0);

		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		// System.out.println("Matching object and scene images...");
		descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

		// System.out.println("Calculating good match list...");
		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

		/* The threshold ratio used for the distance. */
		float nndrRatio = 0.7f;

		for (int i = 0; i < matches.size(); i++) {
			MatOfDMatch matofDMatch = matches.get(i);
			DMatch[] dmatcharray = matofDMatch.toArray();
			DMatch m1 = dmatcharray[0];
			DMatch m2 = dmatcharray[1];

			if (m1.distance <= m2.distance * nndrRatio) {
				goodMatchesList.addLast(m1);
			}
		}

		if (goodMatchesList.size() == 0) {
			return false;
		}

		System.out.println("Good matches (SIFT): " + goodMatchesList.size());

		int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);

		System.out.println("Min matches (SIFT): " + min_accepted_matches);

		if (goodMatchesList.size() > min_accepted_matches) {

			// System.out.println("Object Found!");

			List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
			List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

			LinkedList<Point> objectPoints = new LinkedList<Point>();
			LinkedList<Point> scenePoints = new LinkedList<Point>();

			for (int i = 0; i < goodMatchesList.size(); i++) {
				objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
				scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
			}

			MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
			objMatOfPoint2f.fromList(objectPoints);
			MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
			scnMatOfPoint2f.fromList(scenePoints);

			/* output filename. */
			String filename = object.toString();
			int index = filename.lastIndexOf("/");
			filename = filename.substring(index + 1, filename.length());
			filename = filename.replace(".png", "");

			/* visualize detected features. */
			Highgui.imwrite("output/templateMatching/SIFT-" + filename + "-outputImage.jpg", outputImage);

			try {

				/* Get the rectangle the the potential match is. */
				Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);
				Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
				Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

				obj_corners.put(0, 0, new double[] { 0, 0 });
				obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
				obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
				obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });

				// System.out.println("Transforming object corners to scene corners...");
				Core.perspectiveTransform(obj_corners, scene_corners, homography);

				Mat img = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_COLOR);

				Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)),
						new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)),
						new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)),
						new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)),
						new Scalar(255, 0, 0), 2);

				// System.out.println("Drawing matches image...");
				MatOfDMatch goodMatches = new MatOfDMatch();
				goodMatches.fromList(goodMatchesList);

				Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches,
						matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

				filename = object.toString();
				index = filename.lastIndexOf("/");
				filename = filename.substring(index + 1, filename.length());
				filename = filename.replace(".png", "");

				Highgui.imwrite("output/templateMatching/SIFT-" + filename + "-matchoutput.jpg", matchoutput);
				Highgui.imwrite("output/templateMatching/SIFT-" + filename + "-img.jpg", img);

			} catch (Exception e) {
				System.out.println("Homography not found");
			}

			return true;

		} else {
			// System.out.println("Object Not Found");
			return false;
		}

	}

	/**
	 * Run the TM_CCOEFF_NORMED template matching algorithm. Returns the center of
	 * the rectangle where the best match has been found.
	 * 
	 * @param templateFile
	 * @param imageFile
	 * @return
	 */
	private static Point templateMatching(String templateFile, String imageFile) {

		/* load the images in grayscale. */
//		Mat img = Highgui.imread(imageFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
//		Mat templ = Highgui.imread(templateFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		
		/* load the images full color. */
		Mat img = Highgui.imread(imageFile);
		Mat templ = Highgui.imread(templateFile);
		
		File t = new File("output/templateMatching/TM-template.png");
		Highgui.imwrite(t.getPath(), templ);
		
		File o = new File("output/templateMatching/TM-imageoriginal.png");
		Highgui.imwrite(o.getPath(), img);

		/* Create the result matrix. */
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		/* Do the Matching and Normalize. */
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		
		File risultato = new File("output/templateMatching/TM-risultato.png");
		Highgui.imwrite(risultato.getPath(), result);
		
		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {
				
				if (result.get(i, j)[0] >= 0.99) {
					matches.add(new Point(i, j));
				}
			}
		}

		/* check the results. */
		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No matches found!");
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple matches: " + matches.size());
		}

		/* Localizing the best match with minMaxLoc. */
		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Point matchLoc = mmr.maxLoc;

		System.out.println("Max point found at: " + matchLoc.x + ", " + matchLoc.y);
		
		/* Draws a rectangle over the detected area. */
		Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
				new Scalar(0, 255, 0), 2);

//		/* Draws a cross mark at the center of the detected area. */
//		Core.line(img, new Point(matchLoc.x + templ.cols() / 2, (matchLoc.y + templ.rows() / 2) - 5),
//				new Point(matchLoc.x + templ.cols() / 2, (matchLoc.y + templ.rows() / 2) + 5), new Scalar(0, 0, 255),
//				2);
//		Core.line(img, new Point(((matchLoc.x + templ.cols() / 2) - 5), matchLoc.y + templ.rows() / 2),
//				new Point(((matchLoc.x + templ.cols() / 2) + 5), matchLoc.y + templ.rows() / 2), new Scalar(0, 0, 255),
//				2);
//
//		/* Draws the others detected matches. */
//		for (Point point : matches) {
//			/* Draws a rectangle over the detected area. */
//			Core.rectangle(img, point, new Point(point.x + templ.cols(), point.y + templ.rows()), new Scalar(0, 0, 255),
//					1);
//		}

		/* Save the visualized detection. */
		String filename = imageFile.toString();
		int i = filename.lastIndexOf("/");
		filename = filename.substring(i + 1, filename.length());
		filename = filename.replace(".png", "");
		File annotated = new File("output/templateMatching/TM-" + filename + ".png");
		Highgui.imwrite(annotated.getPath(), img);

		/* Return the center. */
		return new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
	}

	// private static int round(double value, int places) {
	// if (places < 0)
	// throw new IllegalArgumentException();
	//
	// BigDecimal bd = new BigDecimal(value);
	// bd = bd.setScale(places, RoundingMode.FLOOR);
	// return bd.toBigInteger().intValue();
	// }

	// /**
	// * converts a Mat dump to an array of Point
	// *
	// * @param dump
	// * @return
	// */
	// private static Point[] getPointsFromMatDump(String dump) {
	// Point[] result = new Point[4];
	// String[] split = dump.split(";");
	// for (int i = 0; i < split.length; i++) {
	// split[i] = split[i].replaceAll("\\[", "").trim();
	// split[i] = split[i].replaceAll("\\]", "").trim();
	// String[] coords = split[i].split(",");
	// double x = Double.parseDouble(coords[0].trim());
	// double y = Double.parseDouble(coords[1].trim());
	// if (x < 0 || y < 0) {
	// return null;
	// } else {
	// result[i] = new Point(x, y);
	// }
	// }
	// return result;
	// }

	// /* Ad-hoc visual locator detector feature. */
	// public static Point siftAndMultipleTemplateMatching(String imageFile, String
	// templateFile, double threshold) {
	//
	// List<Point> matches = new LinkedList<Point>();
	// Point best_result = null;
	//
	// /* run SIFT to check for the presence/absence of the template image. */
	// boolean isPresent = runFeatureDetection(templateFile, imageFile);
	//
	// System.out.println("isPresent: " + isPresent);
	// // if (isPresent) {
	//
	// /* Get the image. */
	// Mat img = Highgui.imread(imageFile);
	//
	// /* Get the template. */
	// Mat templ = Highgui.imread(templateFile);
	//
	// /* Create the result matrix. */
	// int result_cols = img.cols() - templ.cols() + 1;
	// int result_rows = img.rows() - templ.rows() + 1;
	// Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
	//
	// List<Rectangle2D> boxes = new LinkedList<Rectangle2D>();
	//
	// // System.out.println("[LOG]\tSearching matches of " + templateFile + " in "
	// +
	// // imageFile);
	//
	// /* Do the Matching and Thresholding. */
	// Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
	// Imgproc.threshold(result, result, 0.1, 1, Imgproc.THRESH_TOZERO);
	//
	// double maxval;
	// while (true) {
	// Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
	// Point maxp = maxr.maxLoc;
	// maxval = maxr.maxVal;
	// if (maxval >= threshold) {
	//
	// Core.rectangle(img, maxp, new Point(maxp.x + templ.cols(), maxp.y +
	// templ.rows()),
	// new Scalar(0, 0, 255), 2);
	// Core.rectangle(result, maxp, new Point(maxp.x + templ.cols(), maxp.y +
	// templ.rows()),
	// new Scalar(0, 255, 0), -1);
	//
	// matches.add(maxp);
	// boxes.add(new Rectangle((int) maxp.x, (int) maxp.y, templ.cols(),
	// templ.rows()));
	// } else {
	// break;
	// }
	// }
	//
	// int x_sift = -1;
	// int y_sift = -1;
	//
	// if (matches.size() == 0) {
	//
	// System.out.println("[LOG]\tTemplate Matching found no matches");
	//
	// } else if (matches.size() == 1) {
	//
	// System.out.println("Template Matching found " + matches.size() + " match");
	// best_result = matches.get(0);
	//
	// } else {
	//
	// System.out.println("Template Matching found multiple matches: " +
	// matches.size());
	// System.out.println("Filtering results based on SIFT detection");
	//
	// if (points != null && isPresent) {
	// System.out.println("Best Match with SIFT");
	// x_sift = round(points[0].x, 0);
	// y_sift = round(points[0].y, 0);
	// System.out.println("[" + 0 + "]\tx=" + x_sift + "\ty=" + y_sift);
	// }
	//
	// for (int i = 0; i < matches.size(); i++) {
	//
	// int x = round(matches.get(i).x, 0);
	// int y = round(matches.get(i).y, 0);
	//
	// System.out.println("[" + i + "]\tx=" + x + "\ty=" + y);
	//
	// /* filter the results. */
	// if (isPresent) {
	// if (x == x_sift && y == y_sift) {
	// best_result = matches.get(i);
	// }
	// }
	// }
	//
	// System.out.println("Best Template Matching result");
	// System.out.println("[" + 0 + "]\tx=" + best_result.x + "\ty=" +
	// best_result.y);
	//
	// /*
	// * non-maxima suppression step to filter the results. Needs to be tested!
	// */
	// // Rectangle2D picked = nonMaxSuppression(boxes);
	//
	// }
	//
	// /* Save the visualized detection. */
	// File template = new File("5template.png");
	// Highgui.imwrite(template.getPath(), templ);
	//
	// File annotated = new File("6annotatedTemplateMatching.png");
	// Highgui.imwrite(annotated.getPath(), img);
	//
	// return best_result;
	//
	// }

}
