package utils;

import java.io.IOException;

import com.pragone.jphash.jpHash;
import com.pragone.jphash.image.radial.RadialHash;

public class PHash {

	public static double getPHashSimiliarity(String imagepath1, String imagepath2) throws IOException {

		if (imagepath1.endsWith("png"))
			imagepath1 = UtilsScreenshots.convertPngToJpg(imagepath1);

		if (imagepath2.endsWith("png"))
			imagepath2 = UtilsScreenshots.convertPngToJpg(imagepath2);

		RadialHash hash1 = jpHash.getImageRadialHash(imagepath1);

		RadialHash hash2 = jpHash.getImageRadialHash(imagepath2);

		return jpHash.getSimilarity(hash1, hash2);

	}
}
