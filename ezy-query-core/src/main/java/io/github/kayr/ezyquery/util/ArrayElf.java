package io.github.kayr.ezyquery.util;

import java.util.Arrays;

public class ArrayElf {

  private ArrayElf() {}

  public static <T> T[] array(T... items) {
    return items;
  }

  public static <T> T[] addAll(T[] array, T... items) {
    if (array == null) return items;
    if (items == null) return array;
    T[] result = Arrays.copyOf(array, array.length + items.length);
    System.arraycopy(items, 0, result, array.length, items.length);
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] addFirst(T[] items, T item) {
    return addAll(array(item), items);
  }
}
