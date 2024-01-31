package io.github.kayr.ezyquery.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringElf {

  private StringElf() {}

  public static List<String> splitByComma(String line) {
    if (line == null) {
      return Collections.emptyList();
    }
    List<String> fields = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder field = new StringBuilder();
    for (char c : line.toCharArray()) {
      if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == ',' && !inQuotes) {
        fields.add(field.toString());
        field = new StringBuilder();
      } else {
        field.append(c);
      }
    }
    fields.add(field.toString());
    return fields;
  }
}
