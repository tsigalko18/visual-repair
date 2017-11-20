package utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.pragone.jphash.jpHash;
import com.pragone.jphash.image.radial.RadialHash;

public class PHash {

	public static double getPHashSimiliarity(String imagepath1, String imagepath2) throws IOException {

		if (imagepath1.endsWith("png"))
			imagepath1 = UtilsComputerVision.convertPngToJpg(imagepath1);

		if (imagepath2.endsWith("png"))
			imagepath2 = UtilsComputerVision.convertPngToJpg(imagepath2);

		RadialHash hash1 = jpHash.getImageRadialHash(imagepath1);

		RadialHash hash2 = jpHash.getImageRadialHash(imagepath2);

		Double sim = jpHash.getSimilarity(hash1, hash2);

		FileUtils.deleteQuietly(new File(imagepath1));
		FileUtils.deleteQuietly(new File(imagepath2));

		return sim;

	}
}
