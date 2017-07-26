package testPHash;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import utils.PHash;

public class TestPHash {

	@Test
	public void testSimilarity() {

		String image1 = "src/test/resources/oracle.png";
		String image2 = "src/test/resources/test.png";

		try {
			assertEquals(Double.toString(PHash.getPHashSimiliarity(image1, image2)), "0.9990959769465696");
			System.out.println(PHash.getPHashSimiliarity(image1, image2));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
