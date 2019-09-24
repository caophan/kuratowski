package topo.kuratowski;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class ListSubSetBean {
  private Set<Integer> set;

  @PostConstruct
	public void init() {
    set = new HashSet<Integer>(); // Create an ArrayList
    set.add(1);
    set.add(2);
    set.add(3);
    set.add(4);
    set.add(5);
    set.add(6);
    set.add(7);
  }

  public Set<Set<Integer>> getSubsets() {
    return getSubsets(set);
  }

  private Set<Set<Integer>> getSubsets(Set<Integer> set) {
    if (set.isEmpty()) {
      return Collections.singleton(Collections.emptySet());
    }

    Set<Set<Integer>> subSets = set.stream().map(item -> {
      Set<Integer> clone = new HashSet<>(set);
      clone.remove(item);
      return clone;
    }).map(group -> getSubsets(group)).reduce(new HashSet<>(), (x, y) -> {
      x.addAll(y);
      return x;
    });

    subSets.add(set);
    return subSets;
  }
}
