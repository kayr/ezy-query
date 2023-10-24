/* (C)2022 */
package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.util.Elf;
import java.util.Collections;
import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
public class QueryAndParams {

  private String sql;
  private List<Object> params = Collections.emptyList();

  public QueryAndParams(String sql) {
    this.sql = sql;
  }

  public static QueryAndParams of(String sql) {
    return new QueryAndParams(sql);
  }

  public static QueryAndParams of(String s, List<Object> params) {
    return new QueryAndParams(s, params);
  }

  public QueryAndParams append(boolean conditional, String sql) {
    if (conditional) {
      return append(sql);
    }
    return this;
  }

  public QueryAndParams newLine() {
    if (!sql.endsWith("\n")) {
      sql += "\n";
    }
    return this;
  }

  public QueryAndParams append(String sql) {
    return of(this.sql + sql, params);
  }

  public QueryAndParams append(QueryAndParams sql) {
    return of(this.sql + sql.getSql(), Elf.combine(this.params, sql.params));
  }

  public QueryAndParams append(String sql, List<Object> params) {
    return of(this.sql + sql, Elf.combine(this.params, params));
  }

  @Override
  public String toString() {
    return "Result{" + "sql='" + sql + '\'' + ", params=" + params + '}';
  }

  public QueryAndParams print() {
    printSql();
    printParams();
    return this;
  }

  public QueryAndParams printSql() {
    System.out.println("SQL:\n" + sql);
    return this;
  }

  public QueryAndParams printParams() {
    System.out.println("PARAMS:" + params);
    return this;
  }
}
