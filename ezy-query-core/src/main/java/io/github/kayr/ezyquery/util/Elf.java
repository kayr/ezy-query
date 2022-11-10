package io.github.kayr.ezyquery.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
}
