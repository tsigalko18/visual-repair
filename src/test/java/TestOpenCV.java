

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class TestOpenCV {

	@Before
	public void setUp() throws Exception {
		nu.pattern.OpenCV.loadShared();
	}

	@Test
	public void test() {
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		System.out.println("mat = " + mat.dump());
	}

}
