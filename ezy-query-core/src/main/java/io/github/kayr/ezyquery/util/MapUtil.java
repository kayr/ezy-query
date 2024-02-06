package io.github.kayr.ezyquery.util;

import java.util.List;
import java.util.Map;

public class MapUtil {

  private MapUtil() {}

  public static <K, V> V firstValue(Map<K, List<?>> map, K key) {
    List<?> vs = map.get(key);
    if (vs == null || vs.isEmpty()) return null;
    //noinspection unchecked
    return (V) vs.get(0);
  }
}
