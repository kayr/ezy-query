package io.github.kayr.ezyquery.gen;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

public class SectionsParser {

  public static final String SECTION_MARKER = "-- ##";
  public static final String SECTION_MARKER_NO_SPACE = "--##";

  public static List<Section> splitUp(String sql) {
    List<String> lines = lines(sql);
    List<Section> sections = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (isMarker(line)) {
        String name = getName(line);
        StringBuilder collector = new StringBuilder();
        i = collectSection(i, lines, collector);
        sections.add(Section.of(name, collector.toString()));
      }
    }
    return sections;
  }

  private static String getName(String line) {
    String trimmed = line.trim();
    int markerIndex =
        trimmed.startsWith(SECTION_MARKER)
            ? SECTION_MARKER.length()
            : SECTION_MARKER_NO_SPACE.length();
    return trimmed.substring(markerIndex).trim();
  }

  private static int collectSection(int i, List<String> lines, StringBuilder sb) {
    for (int j = i + 1; j < lines.size(); j++) {
      String nextLine = lines.get(j);
      if (isMarker(nextLine)) {
        i = j - 1;
        break;
      }
      sb.append(nextLine).append("\n");
    }
    return i;
  }

  private static boolean isMarker(String nextLine) {
    String trim = nextLine.trim();
    return trim.startsWith(SECTION_MARKER) || trim.startsWith(SECTION_MARKER_NO_SPACE);
  }

  @SneakyThrows
  public static List<String> lines(String sql) {
    BufferedReader br = new BufferedReader(new StringReader(sql));
    List<String> lines = new ArrayList<>();
    String line = br.readLine();
    while (line != null) {
      lines.add(line);
      line = br.readLine();
    }
    return lines;
  }

  @lombok.RequiredArgsConstructor(staticName = "of")
  @lombok.Getter
  @Accessors(fluent = true)
  public static class Section {
    private final String name;
    private final String sql;
  }
}
