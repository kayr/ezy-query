package io.github.kayr.ezyquery.util;

import java.util.ArrayList;
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
}
