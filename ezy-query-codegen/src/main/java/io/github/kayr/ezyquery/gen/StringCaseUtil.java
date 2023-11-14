package io.github.kayr.ezyquery.gen;

public class StringCaseUtil {

  public static final String SPLIT_CHARS_REGEX = "[\\s_-]";

  private StringCaseUtil() {}

  public static String toPascalCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }

    // add a delimiter before every capital letter in-cases the input is in camel case
    String modifiedInput = splitCamelCase(input);

    String[] parts = modifiedInput.split(SPLIT_CHARS_REGEX);
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part.isEmpty()) {
        continue;
      }
      sb.append(part.substring(0, 1).toUpperCase());
      if (part.length() > 1) {
        sb.append(part.substring(1).toLowerCase());
      }
    }
    return sb.toString();
  }

  public static String toCamelCase(String input) {
    String pascalCase = toPascalCase(input);
    if (pascalCase == null || pascalCase.isEmpty()) {
      return pascalCase;
    }
    return pascalCase.substring(0, 1).toLowerCase() + pascalCase.substring(1);
  }

  public static String toScreamingSnakeCase(String input) {
    return toSnakeCase(input).toUpperCase();
  }

  public static String toSnakeCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    // add a delimiter before every capital letter in-cases the input is in camel case
    String modifiedInput = splitCamelCase(input);

    String[] parts = modifiedInput.split(SPLIT_CHARS_REGEX);
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String part : parts) {
      if (part.isEmpty()) {
        continue;
      }
      if (!first) {
        sb.append("_");
      }
      sb.append(part.toLowerCase());

      first = false;
    }
    return sb.toString();
  }

  private static String splitCamelCase(String input) {
    return input
        .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
        .replaceAll("([a-z\\d])([A-Z])", "$1_$2");
  }
}
