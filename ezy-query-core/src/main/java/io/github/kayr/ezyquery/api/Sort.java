package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.util.Elf;
import java.util.ArrayList;
import java.util.List;

@lombok.EqualsAndHashCode
public class Sort {

  public enum DIR {
    ASC,
    DESC;

    private static DIR fromString(String dir) {
      if (dir.equalsIgnoreCase("asc")) {
        return ASC;
      }
      if (dir.equalsIgnoreCase("desc")) {
        return DESC;
      }
      throw new IllegalArgumentException("Invalid sort direction: " + dir);
    }
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

  public static List<Sort> parse(String sortExpr) {

    if (Elf.isBlank(sortExpr)) {
      throw new IllegalArgumentException("Sort expression cannot be blank");
    }

    String[] parts = sortExpr.trim().split(",");
    List<Sort> sorts = new ArrayList<>(parts.length);
    for (String part : parts) {
      Sort sortObj = parsePart(part);
      sorts.add(sortObj);
    }
    return sorts;
  }

  private static Sort parsePart(String sort) {
    if (Elf.isBlank(sort)) {
      throw new IllegalArgumentException("Sort cannot have blank parts");
    }

    // parse the string in form of "field dir" take care of multiple spaces
    String[] parts = sort.trim().split("\\s+");

    String field = parts[0];
    if (parts.length > 1) {
      return Sort.by(field, DIR.fromString(parts[1]));
    }
    return Sort.by(field, DIR.ASC);
  }

  public static Sort by(Field<?> field, DIR dir) {
    return new Sort(field.getAlias(), dir);
  }

  public static Sort by(String field, DIR dir) {
    return new Sort(field, dir);
  }

  @Override
  public String toString() {
    return field + " " + dir;
  }
}
