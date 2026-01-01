package io.github.kayr.ezyquery.util;

import io.github.kayr.ezyquery.api.UnCaughtException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  public static <T, V> V safeMap(T value, ThrowingFunction<T, V> mapper) {
    if (value == null) return null;
    try {
      return mapper.apply(value);
    } catch (Exception e) {
      throw new UnCaughtException(e);
    }
  }

  public static String toString(Object value) {
    return value == null ? null : value.toString();
  }

  public static <T> List<T> flatten(List<List<T>> lists) {
    if (lists == null) return null;
    return lists.stream().flatMap(Collection::stream).collect(Collectors.toList());
  }

  public static <T> List<T> addFirst(List<T> items, T left) {
    List<T> result = new ArrayList<>(items);
    result.add(0, left);
    return result;
  }

  public static <T> List<T> copyList(Collection<T> list) {
    return new ArrayList<>(list);
  }

  public static <T> List<T> arrayToList(Object array) {
    if (array == null) return null;
    List<T> result = new ArrayList<>(Array.getLength(array));
    for (int i = 0; i < Array.getLength(array); i++) {
      result.add((T) Array.get(array, i));
    }
    return result;
  }

  @SafeVarargs
  public static <T> List<T> addAll(List<T> list, T... items) {
    List<T> result = new ArrayList<>(list);
    result.addAll(Arrays.asList(items));
    return result;
  }

  // asset true if all elements in the list are true
  public static void assertTrue(Boolean condition, String message, Object... args) {
    if (!Boolean.TRUE.equals(condition)) {
      throw new IllegalStateException(String.format(message, args));
    }
  }

  public static boolean isEmpty(Collection<?> candidates) {
    return candidates == null || candidates.isEmpty();
  }

  public static void closeQuietly(AutoCloseable con) {
    try {
      con.close();
    } catch (Exception e) {
      // ignore
    }
  }

  public static boolean classExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @lombok.SneakyThrows
  public static List<Path> listAllSqlFiles(Path path) {
    BiPredicate<Path, BasicFileAttributes> filter =
        (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().endsWith(".sql");

    try (Stream<Path> pathStream = Files.find(path, Integer.MAX_VALUE, filter)) {
      return pathStream.collect(Collectors.toList());
    }
  }

  @lombok.SneakyThrows
  public static String readText(Path path) {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  public static String resolveOutputPath(
      String parentInputPath, String filePath, String parentOutputPath) {
    return resolveOutputPath(
            Paths.get(parentInputPath), Paths.get(filePath), Paths.get(parentOutputPath))
        .normalize()
        .toString();
  }

  public static Path resolveOutputPath(Path parentInputPath, Path filePath, Path parentOutputPath) {

    // validate filePath is a child of parentInputPath
    assertTrue(
        filePath.startsWith(parentInputPath),
        "File[%s] is not a child of Parent[%s]",
        filePath,
        parentInputPath);

    Path relativePath = parentInputPath.relativize(filePath);
    return parentOutputPath.resolve(relativePath);
  }

  public static Path changeExtension(Path path, String newExtension) {
    String fileName = path.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1) {
      return path.resolveSibling(fileName + "." + newExtension);
    }
    return path.resolveSibling(fileName.substring(0, dotIndex) + "." + newExtension);
  }

  public static String fromKebabToCamelCase(String kebab) {
    String[] parts = kebab.split("-");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      sb.append(part.substring(0, 1).toUpperCase());
      sb.append(part.substring(1));
    }
    return sb.toString();
  }

  public static boolean isBlank(String str) {
    return str == null || str.trim().isEmpty();
  }

  public static String mayBeAddParens(String expression) {
    if (expression.trim().startsWith("(") && expression.trim().endsWith(")")) return expression;
    return "(" + expression + ")";
  }

  public static <K, V> Map<K, V> put(Map<K, V> paramValues, K paramName, V value) {
    HashMap<K, V> map = new HashMap<>(paramValues);
    map.put(paramName, value);
    return map;
  }

  public static <K, V> Map<K, V> remove(Map<K, V> paramValues, K... paramName) {
    HashMap<K, V> map = new HashMap<>(paramValues);
    for (K k : paramName) {
      map.remove(k);
    }
    return map;
  }

  public static List<Object> toList(Enumeration<?> value) {
    //noinspection unchecked
    return (List<Object>) Collections.list(value);
  }

  public static List<Object> toList(Iterator<?> value) {
    List<Object> list = new ArrayList<>();
    while (value.hasNext()) {
      list.add(value.next());
    }
    return list;
  }

  public static Properties readProperties(Path path) {
    Properties properties = new Properties();

    if (!Files.exists(path)) throw new IllegalArgumentException("File does not exist: " + path);

    try (InputStream is = Files.newInputStream(path)) {
      properties.load(is);
    } catch (IOException e) {
      throw new UnCaughtException("Error reading properties file", e);
    }

    return properties;
  }
}
