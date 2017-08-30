import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import rx.Observable;

public class TestRTree {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRTree() {
		RTree<String, Rectangle> tree = RTree.maxChildren(5).create();
		tree = tree.add("DAVE", Geometries.rectangle(0, 0, 2, 2));
		tree = tree.add("JOHN", Geometries.rectangle(0, 0, 3, 3));
		tree = tree.add("WAYNE", Geometries.rectangle(0, 0, 4, 4));

		Point point = Geometries.point(1, 1);		
		Observable<Entry<String, Rectangle>> results = tree.nearest(point, 10, 1);
		
		List<Entry<String, Rectangle>> asList = results.toList().toBlocking().single();
		
		for (Entry<String, Rectangle> entry : asList) {
			System.out.println(entry.value());
		}
		
		tree.visualize(600, 600).save("target/mytree.png");
	}

}
