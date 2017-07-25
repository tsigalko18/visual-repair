package vision;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

	private String fileEnclosingCharacter;

	static {
		nu.pattern.OpenCV.loadShared();
	}

	public void compareImages(String referenceImagePath, String referenceImageName, String comparisonImagePath,
			String comparisonImageName, String differenceText, String function, boolean doNotOverwrite)
			throws IOException {
		String img1 = referenceImagePath + File.separatorChar + referenceImageName;
		String img2 = comparisonImagePath + File.separatorChar + comparisonImageName;
		String diff = comparisonImagePath + File.separatorChar + differenceText;

		if (doNotOverwrite && new File(diff).exists() && new File(diff).length() > 0) {
			return;
		}

		// for imagemagick composite function
		BufferedImage bimg = ImageIO.read(new File(img1));
		int img1Width = bimg.getWidth();
		int img1Height = bimg.getHeight();
		bimg = ImageIO.read(new File(img2));
		int img2Width = bimg.getWidth();
		int img2Height = bimg.getHeight();

		// swap if img2 size is less than img1 size
		if (img1Width * img1Height > img2Width * img2Height) {
			String temp = img2;
			img2 = img1;
			img1 = temp;
		}

		Runtime runtime = Runtime.getRuntime();
		Process p = runtime.exec("composite -compose " + function + " " + fileEnclosingCharacter + img1
				+ fileEnclosingCharacter + " " + fileEnclosingCharacter + img2 + fileEnclosingCharacter + " "
				+ fileEnclosingCharacter + diff + fileEnclosingCharacter);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		p.destroy();
		p = runtime.exec("composite -compose " + function + " " + fileEnclosingCharacter + img1 + fileEnclosingCharacter
				+ " " + fileEnclosingCharacter + img2 + fileEnclosingCharacter + " " + fileEnclosingCharacter
				+ comparisonImagePath + File.separatorChar + Util.getFileNameAndExtension(differenceText)[0]
				+ Constants.SCREENSHOT_FILE_EXTENSION + fileEnclosingCharacter);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		p.destroy();
	}

	public List<org.openqa.selenium.Point> compareImages(String referenceImagePath, String referenceImageName,
			String comparisonImagePath, String comparisonImageName) {
		List<org.openqa.selenium.Point> differencePixels = new ArrayList<org.openqa.selenium.Point>();

		String img1 = referenceImagePath + File.separatorChar + referenceImageName;
		String img2 = comparisonImagePath + File.separatorChar + comparisonImageName;

		Mat img1Mat = Imgcodecs.imread(img1);
		Mat img2Mat = Imgcodecs.imread(img2);

		Mat outDiff = new Mat(Math.max(img1Mat.rows(), img2Mat.rows()), Math.max(img1Mat.cols(), img2Mat.cols()),
				img1Mat.type());

		for (int col = 0; col < img1Mat.cols()
				|| col < img2Mat.cols(); col += Constants.IMAGE_COMPARISON_PIXEL_SUB_SAMPLING_RATE) {
			for (int row = 0; row < img1Mat.rows()
					|| row < img2Mat.rows(); row += Constants.IMAGE_COMPARISON_PIXEL_SUB_SAMPLING_RATE) {
				double[] img1RGB = img1Mat.get(row, col);
				double[] img2RGB = img2Mat.get(row, col);

				// point is present in one of the images but not both (different
				// sized images)
				if (img1RGB == null && img2RGB != null || img2RGB == null && img1RGB != null) {
					differencePixels.add(new org.openqa.selenium.Point(col, row));
					if (img1RGB != null)
						outDiff.put(row, col, img1RGB);
					else
						outDiff.put(row, col, img2RGB);
				} else if (img1RGB != null & img2RGB != null) {
					boolean isSame = true;
					/*
					 * String img1Hex = Util.getHexFromRGB((int)img1RGB[0], (int)img1RGB[1],
					 * (int)img1RGB[2]); String img2Hex = Util.getHexFromRGB((int)img2RGB[0],
					 * (int)img2RGB[1], (int)img2RGB[2]); if(!img1Hex.equalsIgnoreCase(img2Hex)) {
					 * differencePixels.add(new org.openqa.selenium.Point(col, row)); }
					 */
					for (int i = 0; i < img1RGB.length; i++) {
						if (img1RGB[i] != img2RGB[i]) {
							isSame = false;
							break;
						}
					}
					if (!isSame) {
						differencePixels.add(new org.openqa.selenium.Point(col, row));
						outDiff.put(row, col, img1RGB);
					}
				}
			}
		}
		Imgcodecs.imwrite(comparisonImagePath + File.separatorChar + "diff"
				+ (Util.getNumbersFromString(comparisonImageName).size() > 0
						? Util.getNumbersFromString(comparisonImageName).get(0)
						: "")
				+ ".png", outDiff);
		return differencePixels;
	}

	public static double compareImagesByHistogram(String img1FullPath, String img2FullPath) {

		Mat image1 = Imgcodecs.imread(img1FullPath);
		Mat image2 = Imgcodecs.imread(img2FullPath);
		
//		Imgproc.resize(image1, image1, new Size(), 0.5, 0.5,Imgproc.INTER_AREA);
//		Imgproc.resize(image2, image2, new Size(), 0.5, 0.5,Imgproc.INTER_AREA);
//		Imgproc.resize(image1, image1, new Size(200,200)); //, 0.5, 0.5,Imgproc.INTER_AREA);
//		Imgproc.resize(image2, image2, new Size(200,200)); //, 0.5, 0.5,Imgproc.INTER_AREA);
//		
//		Imgcodecs.imwrite("/Users/astocco/Desktop/image1.jpg", image1);
//		Imgcodecs.imwrite("/Users/astocco/Desktop/image2.jpg", image2);
//		
//		Mat grayImage1 = new Mat(), grayImage2 = new Mat();
//
//		Imgproc.cvtColor(image1, image1, Imgproc.COLOR_RGB2GRAY);
//		Imgproc.cvtColor(image2, image2, Imgproc.COLOR_RGB2GRAY);
//
//		Imgcodecs.imwrite("/Users/astocco/Desktop/grayImage1.jpg", grayImage1);
//		Imgcodecs.imwrite("/Users/astocco/Desktop/grayImage2.jpg", grayImage2);
//
//		Mat h1 = new Mat();
//		Mat h2 = new Mat();
//		
//		int hist_bins = 30;           //number of histogram bins
//		int hist_range[]= {0,180};	//histogram range
//		MatOfFloat ranges = new MatOfFloat(0f, 256f);
//		MatOfInt histSize = new MatOfInt(25);
//
//		Imgproc.calcHist(Arrays.asList(grayImage1), new MatOfInt(0), new Mat(), h1, histSize, ranges);
//		Imgproc.calcHist(Arrays.asList(grayImage2), new MatOfInt(0), new Mat(), h2, histSize, ranges);

		List<Mat> images = new ArrayList<Mat>();
		images.add(image1);
		Mat h1 = new Mat();

		Imgproc.calcHist(images, new MatOfInt(0, 1), new Mat(), h1, new MatOfInt(256, 256),
				new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f));
		images = new ArrayList<Mat>();
		images.add(image2);
		Mat h2 = new Mat();
		Imgproc.calcHist(images, new MatOfInt(0, 1), new Mat(), h2, new MatOfInt(256, 256),
				new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f));

		double result = Imgproc.compareHist(h1, h2, Imgproc.CV_COMP_CORREL);

		return result;
	}

	public Rectangle getImageSize(String imagePath, String imageName) throws IOException {
		BufferedImage bimg = ImageIO.read(new File(imagePath + File.separatorChar + imageName));
		return new Rectangle(0, 0, bimg.getWidth(), bimg.getHeight());
	}

	public static void main(String[] args) {

		System.out.println(ImageProcessing.compareImagesByHistogram("src/test/resources/oracle.jpg",
				"src/test/resources/test.jpg"));

	}
}