package io.github.kayr.ezyquery.api;

public class Sort {

  public enum DIR {
    ASC,
    DESC
  }

  private final String field;
  private final DIR dir;

  public Sort(String field, DIR dir) {
    this.field = field;
    this.dir = dir;
  }

  public String getField() {
    return field;
  }

  public DIR getDir() {
    return dir;
  }

  public Sort desc() {
    return by(field, DIR.DESC);
  }

  public Sort asc() {
    return by(field, DIR.ASC);
  }

  public static Sort by(Field<?> field, DIR dir) {
    return new Sort(field.getAlias(), dir);
  }

  public static Sort by(String field, DIR dir) {
    return new Sort(field, dir);
  }
}
