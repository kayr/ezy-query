package io.github.kayr.ezyquery.util;

public class CoercionUtil {

  private CoercionUtil() {}

  public static Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Long) {
      return (Long) value;
    }
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    if (value instanceof String) {
      return Long.valueOf((String) value);
    }
    throw new IllegalArgumentException("Cannot coerce " + value + " to Long");
  }
}
