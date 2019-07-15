package topo.kuratowski;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestNumber {
	private static final String SEQUENCE = "ckckck";
	private KuratowskiBean controller;

	@Before
	public void init() {
		controller = new KuratowskiBean();
		controller.init();
	}

	@Test
	public void test_sort_interval() {
		Interval interval1 = new Interval(2, false, 4, true);
		Point point = new Point(2);

		List<Interval> sets = Arrays.asList(interval1, point);
		controller.sortInterval(sets);
	}
	
	@Test
	public void test_substring() {
		for (int i = SEQUENCE.length(); i >= 0; i--) {
			System.out.println(SEQUENCE.substring(i));
		}
		System.out.println(controller.buildKey(-1, true));
		System.out.println(controller.buildKey(-1, false));
	}

	@Test
	public void test_merge_interval() {
		Interval interval1 = new Interval(2, false, 4, true);
		Point point = new Point(2);

		List<Interval> sets = Arrays.asList(point, interval1);
		System.out.println(sets);
		List<Interval> merges = controller.merge(sets);
		System.out.println(merges);
	}
	
	@Test
	public void test_main() {
		boolean[] selectionSet = controller.getSelectionSet();
		selectionSet[9] = true;
		selectionSet[2] = true;
		selectionSet[8] = true;
		selectionSet[7] = true;
		selectionSet[6] = true;
		selectionSet[4] = true;
		selectionSet[5] = true;
		selectionSet[1] = true;

		controller.calculate(null);
		List<String[]> left = controller.getLeft();
		List<String[]> right = controller.getRight();
		System.out.println(left.size() + " " + right.size());
	}
}