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

  public static QueryAndParams of(String s, List<Object> singletonList) {
    return new QueryAndParams(s, singletonList);
  }

  QueryAndParams append(boolean conditional, String sql) {
    if (conditional) {
      return append(sql);
    }
    return this;
  }

  QueryAndParams append(String sql) {
    return of(this.sql + sql, params);
  }

  QueryAndParams append(QueryAndParams sql) {
    return of(this.sql + sql.getSql(), Elf.combine(this.params, sql.params));
  }

  @Override
  public String toString() {
    return "Result{" + "sql='" + sql + '\'' + ", params=" + params + '}';
  }
}
