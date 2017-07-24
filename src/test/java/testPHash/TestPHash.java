package testPHash;

import java.io.IOException;

import org.junit.Test;

import utils.PHash;

public class TestPHash {
	
	@Test
	public void testSimilarity() {

		try {
			System.out
					.println(PHash.getPHashSimiliarity("src/test/resources/oracle.png", "src/test/resources/test.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
