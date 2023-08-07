/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.kayr.ezyquery.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Adapted from Spring Framework's {@code NamedParameterUtils} */
class NamedParamParser {

  /** Set of characters that qualify as comment or quotes starting characters. */
  private static final String[] START_SKIP = new String[] {"'", "\"", "--", "/*"};

  /** Set of characters that at are the corresponding comment or quotes ending characters. */
  private static final String[] STOP_SKIP = new String[] {"'", "\"", "\n", "*/"};

  /**
   * Set of characters that qualify as parameter separators, indicating that a parameter name in an
   * SQL String has ended.
   */
  private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^]";

  /**
   * An index with separator flags per character code. Technically only needed between 34 and 124 at
   * this point.
   */
  private static final boolean[] separatorIndex = new boolean[128];

  static {
    for (char c : PARAMETER_SEPARATORS.toCharArray()) {
      separatorIndex[c] = true;
    }
  }

  public static SqlParts buildParts(String sql) {
    return buildParts(parse(sql));
  }

  public static SqlParts buildParts(ParsedSql parsedSql) {
    List<String> names = parsedSql.getParameterNames();
    int paramsCount = names.size();

    if (names.isEmpty()) {
      return SqlParts.of(SqlParts.textPart(parsedSql.getOriginalSql()));
    }

    List<SqlParts.IPart> parts = new ArrayList<>(names.size());

    int sqlStartPos = 0;
    for (int i = 0; i < paramsCount; i++) {
      String name = names.get(i);
      int[] indexes = parsedSql.getParameterIndexes(i);
      int sqlEndPos = indexes[0];
      if (sqlStartPos != sqlEndPos) {
        parts.add(SqlParts.textPart(parsedSql.getOriginalSql().substring(sqlStartPos, sqlEndPos)));
      }

      parts.add(SqlParts.paramPart(name));

      sqlStartPos = indexes[1];
    }

    if (sqlStartPos < parsedSql.getOriginalSql().length()) {
      parts.add(SqlParts.textPart(parsedSql.getOriginalSql().substring(sqlStartPos)));
    }

    return SqlParts.of(parts);
  }

  /**
   * Parse the SQL statement and locate any placeholders or named parameters. Named parameters are
   * substituted for a JDBC placeholder.
   *
   * @param sql the SQL statement
   * @return the parsed statement, represented as {@link ParsedSql} instance
   */
  public static ParsedSql parse(String sql) {

    Set<String> namedParameters = new HashSet<>();
    StringBuilder sqlToUse = new StringBuilder(sql);

    List<ParameterHolder> parameterList = new ArrayList<>();

    char[] statement = sql.toCharArray();
    int namedParameterCount = 0;
    int unnamedParameterCount = 0;
    int totalParameterCount = 0;

    int escapes = 0;
    int i = 0;
    while (i < statement.length) {
      int skipToPosition = i;
      while (i < statement.length) {
        skipToPosition = skipCommentsAndQuotes(statement, i);
        if (i == skipToPosition) {
          break;
        } else {
          i = skipToPosition;
        }
      }

      if (i >= statement.length) {
        break;
      }
      char c = statement[i];
      if (c == ':' || c == '&') {
        int j = i + 1;
        if (c == ':' && j < statement.length && statement[j] == ':') {
          // Postgres-style "::" casting operator should be skipped
          i = i + 2;
          continue;
        }
        String parameter = null;
        if (c == ':' && j < statement.length && statement[j] == '{') {
          // :{x} style parameter
          while (statement[j] != '}') {
            j++;
            if (j >= statement.length) {
              throw new IllegalArgumentException(
                  "Non-terminated named parameter declaration at position "
                      + i
                      + " in statement: "
                      + sql);
            }
            if (statement[j] == ':' || statement[j] == '{') {
              throw new IllegalArgumentException(
                  "Parameter name contains invalid character '"
                      + statement[j]
                      + "' at position "
                      + i
                      + " in statement: "
                      + sql);
            }
          }
          if (j - i > 2) {
            parameter = sql.substring(i + 2, j);

            namedParameterCount =
                addNewNamedParameter(namedParameters, namedParameterCount, parameter);
            totalParameterCount =
                addNamedParameter(parameterList, totalParameterCount, escapes, i, j + 1, parameter);
          }
          j++;
        } else {
          while (j < statement.length && !isParameterSeparator(statement[j])) {
            j++;
          }
          if (j - i > 1) {
            parameter = sql.substring(i + 1, j);
            if (j < statement.length && statement[j] == ']' && parameter.contains("[")) {
              // preserve end bracket for index/key
              j++;
              parameter = sql.substring(i + 1, j);
            }
            namedParameterCount =
                addNewNamedParameter(namedParameters, namedParameterCount, parameter);
            totalParameterCount =
                addNamedParameter(parameterList, totalParameterCount, escapes, i, j, parameter);
          }
        }
        i = j - 1;
      } else {
        if (c == '\\') {
          int j = i + 1;
          if (j < statement.length && statement[j] == ':') {
            // escaped ":" should be skipped
            sqlToUse.deleteCharAt(i - escapes);
            escapes++;
            i = i + 2;
            continue;
          }
        }
        if (c == '?') {
          int j = i + 1;
          if (j < statement.length
              && (statement[j] == '?' || statement[j] == '|' || statement[j] == '&')) {
            // Postgres-style "??", "?|", "?&" operator should be skipped
            i = i + 2;
            continue;
          }
          unnamedParameterCount++;
          totalParameterCount++;
        }
      }
      i++;
    }
    ParsedSql parsedSql = new ParsedSql(sqlToUse.toString());
    for (ParameterHolder ph : parameterList) {
      parsedSql.addNamedParameter(ph.getParameterName(), ph.getStartIndex(), ph.getEndIndex());
    }
    parsedSql.setNamedParameterCount(namedParameterCount);
    parsedSql.setUnnamedParameterCount(unnamedParameterCount);
    parsedSql.setTotalParameterCount(totalParameterCount);
    return parsedSql;
  }

  private static int addNamedParameter(
      List<ParameterHolder> parameterList,
      int totalParameterCount,
      int escapes,
      int i,
      int j,
      String parameter) {

    parameterList.add(new ParameterHolder(parameter, i - escapes, j - escapes));
    totalParameterCount++;
    return totalParameterCount;
  }

  private static int addNewNamedParameter(
      Set<String> namedParameters, int namedParameterCount, String parameter) {
    if (!namedParameters.contains(parameter)) {
      namedParameters.add(parameter);
      namedParameterCount++;
    }
    return namedParameterCount;
  }

  /**
   * Skip over comments and quoted names present in an SQL statement.
   *
   * @param statement character array containing SQL statement
   * @param position current position of statement
   * @return next position to process after any comments or quotes are skipped
   */
  private static int skipCommentsAndQuotes(char[] statement, int position) {
    for (int i = 0; i < START_SKIP.length; i++) {
      if (statement[position] == START_SKIP[i].charAt(0)) {
        boolean match = true;
        for (int j = 1; j < START_SKIP[i].length(); j++) {
          if (statement[position + j] != START_SKIP[i].charAt(j)) {
            match = false;
            break;
          }
        }
        if (match) {
          int offset = START_SKIP[i].length();
          for (int m = position + offset; m < statement.length; m++) {
            if (statement[m] == STOP_SKIP[i].charAt(0)) {
              boolean endMatch = true;
              int endPos = m;
              for (int n = 1; n < STOP_SKIP[i].length(); n++) {
                if (m + n >= statement.length) {
                  // last comment not closed properly
                  return statement.length;
                }
                if (statement[m + n] != STOP_SKIP[i].charAt(n)) {
                  endMatch = false;
                  break;
                }
                endPos = m + n;
              }
              if (endMatch) {
                // found character sequence ending comment or quote
                return endPos + 1;
              }
            }
          }
          // character sequence ending comment or quote not found
          return statement.length;
        }
      }
    }
    return position;
  }

  /**
   * Determine whether a parameter name ends at the current position, that is, whether the given
   * character qualifies as a separator.
   */
  private static boolean isParameterSeparator(char c) {
    return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
  }

  @lombok.Getter
  @lombok.AllArgsConstructor
  private static class ParameterHolder {

    private final String parameterName;

    private final int startIndex;

    private final int endIndex;
  }

  public static void main(String[] args) {
    String sql =
        "select * from user where name = :name and age = :age and id = :id and name = :name";

    SqlParts parsedSql = buildParts(sql);

    System.out.println(parsedSql.getQuery());
  }
}
