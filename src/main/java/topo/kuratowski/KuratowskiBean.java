package topo.kuratowski;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.AjaxBehaviorEvent;

import lombok.Getter;

@Getter
@ManagedBean
@RequestScoped
public class KuratowskiBean {
	private static final String SEQUENCE = "ababab";
	private boolean[] selectionSet;
	private List<Interval> inputSet;
	private List<String[]> left;
	private List<String[]> right;

	@PostConstruct
	public void init() {
		selectionSet = new boolean[10];
		// inputSet = Arrays.asList(new Interval(null, false, 1, false), new Point(1), new Interval(1, false, 2, false),
		// 		new Point(2), new Interval(2, false, 4, false, 1), new Interval(2, false, 4, false, 2), new Point(4),
    // 		new Interval(4, false, 5, false), new Point(5), new Interval(5, false));
    inputSet = Arrays.asList(new Interval(0, false, 1, false), new Interval(1, false, 2, false), new Point(3), new Interval(4, false, 5, false, 1));
	}

	public void calculate(AjaxBehaviorEvent event) {
		left = new ArrayList<>();
		right = new ArrayList<>();

    List<Interval> A = IntStream.range(0, inputSet.size()).filter(i -> selectionSet[i]).mapToObj(i -> inputSet.get(i))
        .collect(Collectors.toList());
    if (!A.isEmpty()) {
      Map<String, Object> map = main(A);
      for (int i = -1; i < SEQUENCE.length(); i++) {
        String key = buildKey(i, true);
        Object value = map.get(key);
        left.add(new String[] { key, print(value) });

        key = buildKey(i, false);
        value = map.get(key);
        right.add(new String[] { key, print(value) });
      }
    }
	}

	private String print(Object value) {
		if (value == null) {
			return "";

		} else if (value instanceof List) {
			List<Interval> list = (List<Interval>) value;
			if (list.isEmpty()) {
				return Interval.EMPTY;
			} else {
				return list.stream().map(Interval::toString).collect(Collectors.joining(" U "));
			}
		} else {
			return (String) value;
		}
	}

	public Map<String, Object> main(List<Interval> A) {
		Map<String, Object> map = new HashMap<>();
		sortInterval(A);
		A = merge(A);
		A = merge(A);

		List<Interval> aA = complement(A);
		map.put("A", A);
		map.put("aA", aA);

		for (int i = 0; i < 6 && (A != null || aA != null); i++) {
			String key;
			Object item;
			boolean closureBuilder = i % 2 == 0;

			if (A != null) {
				key = buildSet(map, A, i, true, closureBuilder);
				item = map.get(key);
				if (item instanceof String) {
					A = null;
				} else {
					A = (List<Interval>) item;
				}
			}

			if (aA != null) {
				key = buildSet(map, aA, i, false, closureBuilder);
				item = map.get(key);
				if (item instanceof String) {
					aA = null;
				} else {
					aA = (List<Interval>) item;
				}
			}
		}
		return map;
	}

	private String buildSet(Map<String, Object> map, List<Interval> input, int i, boolean branch,
			boolean closureBuilder) {
		List<Interval> intervals = buildSet(input, closureBuilder);
		String isSame = checkExisted(intervals, map);
		String key = buildKey(i, branch);
		if (isSame != null) {
			map.put(key, isSame);
		} else {
			map.put(key, intervals);
		}
		return key;
	}

	private List<Interval> buildSet(List<Interval> input, boolean closureBuilder) {
		List<Interval> intervals;
		if (closureBuilder) {
			intervals = closure(input);
		} else {
			intervals = complement(input);
		}
		return intervals;
	}

	public String buildKey(int i, boolean branch) {
		String prefix = SEQUENCE.substring(5 - i);
		if (branch) {
			return prefix.concat("A");
		} else {
			return prefix.concat("aA");
		}
	}

	private String checkExisted(List<Interval> e, Map<String, Object> map) {
		Set<Entry<String, Object>> set = map.entrySet();
		for (Entry<String, Object> entry : set) {
			Object item = entry.getValue();

			if (item instanceof List) {
				List<Interval> list = (List<Interval>) item;

				boolean isSame = compare(e, list);
				if (isSame) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	public List<Interval> complement(List<Interval> sets) {
		List<Interval> complements = new ArrayList<>();

		if (sets.isEmpty()) {
			complements.add(new Interval());

		} else {
			Integer temp = null;
			boolean leftInclude = false;

			for (Interval interval : sets) {
				if (interval.getLeft() != null) {
					Interval complementSet = buildComplementSet(temp, leftInclude, interval);
					if (complementSet != null) {
						complements.add(complementSet);
					}
				}

				if (interval.getType() > 0) {
					complements.add(buildComplementSet(interval));
				}

				temp = interval.getRight();
				leftInclude = !interval.isRightInclude();
			}
			if (temp != null) {
				complements.add(new Interval(temp, leftInclude));
			}
		}
		return complements;
	}

	private Interval buildComplementSet(Interval interval) {
		return new Interval(interval.getLeft(), interval.isLeftInclude(), interval.getRight(),
				interval.isRightInclude(), 3 - interval.getType());
	}

	private Interval buildComplementSet(Integer temp, boolean leftInclude, Interval interval) {
		if (temp == interval.getLeft() && leftInclude) {
			if (!interval.isLeftInclude()) {
				return new Point(temp);
			} else {
				return null;
			}
		} else {
			return new Interval(temp, leftInclude, interval.getLeft(), !interval.isLeftInclude());
		}
	}

	public List<Interval> closure(List<Interval> sets) {
		List<Interval> closures = new ArrayList<>();

		if (sets.isEmpty()) {
			return Collections.emptyList();

		} else {
			for (Interval interval : sets) {
				closures.add(closure(interval));
			}

			return merge(closures);
		}
	}

	private Interval closure(Interval interval) {
		if (interval instanceof Point) {
			return interval;
		} else {
			boolean leftInclude = true;
			if (interval.getLeft() == null) {
				leftInclude = false;
			}

			boolean rightInclude = true;
			if (interval.getRight() == null) {
				rightInclude = false;
			}
			return new Interval(interval.getLeft(), leftInclude, interval.getRight(), rightInclude);
		}
	}

	public void sortInterval(List<Interval> input) {
		Comparator<Interval> comparator = getComparator();
		Collections.sort(input, comparator);
	}

	public List<Interval> merge(List<Interval> closures) {
		List<Interval> merges = new ArrayList<>();
		Interval temp = closures.get(0);
		for (int i = 1; i < closures.size(); i++) {
			Interval merge = merge(temp, closures.get(i));
			if (merge != null) {
				temp = merge;
			} else {
				merges.add(temp);
				temp = closures.get(i);
			}
		}
		merges.add(temp);
		return merges;
	}

	private boolean compare(List<Interval> first, List<Interval> second) {
		int i = 0;
		boolean isEqual = first.size() == second.size();
		while (isEqual && i < first.size()) {
			isEqual = compare(first.get(i), second.get(i));
			i++;
		}
		return isEqual;
	}

	private Interval merge(Interval first, Interval second) {
		if (first.getRight() == second.getLeft() && first.getType() == 0 && second.getType() == 0
				&& (first.isRightInclude() || second.isLeftInclude())) {
			return new Interval(first.getLeft(), first.isLeftInclude(), second.getRight(), second.isRightInclude());
		} else if (first.getLeft() == second.getLeft() && first.getRight() == second.getRight()
				&& (first.getType() + second.getType()) == 3 && !first.isLeftInclude() && !second.isLeftInclude()
				&& !first.isRightInclude() && !second.isRightInclude()) {
			return new Interval(first.getLeft(), false, first.getRight(), false);
		}
		return null;
	}

	private boolean compare(Interval first, Interval second) {
		return first.getLeft() == second.getLeft() && first.getRight() == second.getRight()
				&& first.isLeftInclude() == second.isLeftInclude() && first.isRightInclude() == second.isRightInclude()
				&& first.getType() == second.getType();
	}

	private Comparator<Interval> getComparator() {
		return Comparator.comparing(Interval::getLeft, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(Interval::getRight, Comparator.nullsLast(Comparator.naturalOrder()));
	}
}

class Interval {
	private Integer left;
	private Integer right;
	private boolean leftInclude;
	private boolean rightInclude;
  private int type;// 0:real, 1:rational, 2:irrational
  public static final String INFINITY = "\u221E";
  public static final String EMPTY = "\u2205";

	public Interval() {
		this(null, false);
	}

	public Interval(Integer left, boolean leftInclude) {
		this(left, leftInclude, null, false);
	}

	public Interval(Integer left, boolean leftInclude, Integer right, boolean rightInclude) {
		this.left = left;
		this.right = right;
		this.leftInclude = leftInclude;
		this.rightInclude = rightInclude;
	}

	public Interval(Integer left, boolean leftInclude, Integer right, boolean rightInclude, int type) {
		this.left = left;
		this.right = right;
		this.leftInclude = leftInclude;
		this.rightInclude = rightInclude;
		this.type = type;
	}

	public Integer getLeft() {
		return left;
	}

	public Integer getRight() {
		return right;
	}

	public boolean isLeftInclude() {
		return leftInclude;
	}

	public boolean isRightInclude() {
		return rightInclude;
	}

	public int getType() {
		return type;
  }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (type == 1) {
			builder.append('Q');
		} else if (type == 2) {
			builder.append('I');
		}

		if (leftInclude) {
			builder.append('[');
		} else {
			builder.append('(');
    }
    if (left == null) {
      builder.append('-' + INFINITY);
    } else {
      builder.append(left);
    }
    builder.append(',');
    if (right == null) {
      builder.append('+' + INFINITY);
    } else {
      builder.append(right);
    }
		if (rightInclude) {
			builder.append(']');
		} else {
			builder.append(')');
		}
		return builder.toString();
	}
}

class Point extends Interval {
	private int number;

	public Point(int number) {
		this.number = number;
	}

	@Override
	public Integer getLeft() {
		return number;
	}

	@Override
	public Integer getRight() {
		return number;
	}

	@Override
	public boolean isLeftInclude() {
		return true;
	}

	@Override
	public boolean isRightInclude() {
		return true;
	}

	@Override
	public String toString() {
		return "{" + number + "}";
	}
}