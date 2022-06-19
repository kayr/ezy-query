package io.github.kayr.ezyquery.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Elf {

  private Elf() {}

  @SafeVarargs
  public static <T> List<T> combine(List<T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<T> list : lists) {
      result.addAll(list);
    }
    return result;
  }

  // asset true if all elements in the list are true
  public static void assertTrue(Boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

    public static boolean isEmpty(Collection<?> candidates) {
        return candidates == null || candidates.isEmpty();
    }
}
