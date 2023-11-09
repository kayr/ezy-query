package io.github.kayr.ezyquery.parser;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;

/*
To parse the SQL we have 2 options...
Use the JSqlParser library or write our own parser. Using JSQl would be the cleanest way but there is no direct way to walk
the entire AST and get the query parts. We can use the SelectVisitorAdapter to walk the AST, but it is not very clean.

Using our own parser would be a slightly more portable version to avoid writing a tree walker. As of this moment there is no actual benefit in writing a tree walker
since we only really need the select fields and the aliases.
 */

public class SubQueryParser {
  private enum State {
    START,
    IN_QUERY,
    IN_NESTED_QUERY,
  }

  private static final String START_QUERY_MARKER = "-- START_QUERY";
  private static final String END_QUERY_MARKER = "-- END_QUERY";
  private final String sql;

  private final List<SubQuery> queries = new ArrayList<>();
  private State parseState = State.START;
  private String currentQueryName;
  private StringBuilder currentQuery = new StringBuilder();
  private int nestingLevel = 0;

  public SubQueryParser(String sql) {
    this.sql = sql;
  }

  public List<SubQuery> parse() throws JSQLParserException {

    String[] lines = sql.split("\n");

    for (int i = 0, linesLength = lines.length; i < linesLength; i++) {
      String line = lines[i];
      String trimmed = line.trim();
      switch (parseState) {
        case START:
          handleStart(trimmed, i);
          break;
        case IN_QUERY:
          handleInQuery(trimmed, line);
          break;
        case IN_NESTED_QUERY:
          handleInNested(trimmed, line);
          break;
      }
    }

    checkIsParseComplete();

    return queries;
  }

  private void checkIsParseComplete() {
    if (parseState != State.START) {
      throw new IllegalStateException(
          "File ended while still in a query. Current state: " + parseState);
    }
  }

  private void handleInNested(String trimmed, String line) {
    if (isStartQuery(trimmed)) {
      nestingLevel++;
    } else if (isEndQuery(trimmed)) {
      nestingLevel--;
      if (nestingLevel == 0) parseState = State.IN_QUERY;
    }
    appendToCurrent(line);
  }

  private void handleInQuery(String trimmed, String line) {
    if (isStartQuery(trimmed)) {
      parseState = State.IN_NESTED_QUERY;
      nestingLevel++;
      appendToCurrent(line);
    } else if (isEndQuery(trimmed)) {
      queries.add(SubQuery.of(currentQueryName, currentQuery.toString(), null));
      parseState = State.START;
    } else {
      appendToCurrent(line);
    }
  }

  private void appendToCurrent(String line) {
    currentQuery.append(line).append("\n");
  }

  private void handleStart(String trimmed, int i) {
    if (isStartQuery(trimmed)) {
      parseState = State.IN_QUERY;
      currentQuery = new StringBuilder();
      currentQueryName = trimmed.substring(START_QUERY_MARKER.length()).trim();
    } else if (isEndQuery(trimmed)) {
      throw new IllegalStateException("Unexpected end query marker on line " + i);
    }
  }

  private static boolean isEndQuery(String trimmed) {
    return trimmed.startsWith(END_QUERY_MARKER);
  }

  private static boolean isStartQuery(String trimmed) {
    return trimmed.startsWith(START_QUERY_MARKER);
  }

  @lombok.AllArgsConstructor(staticName = "of")
  @lombok.Getter
  public static class SubQuery {
    String name;
    String sqlString;
    List<SubQuery> subQueries;
  }
}
