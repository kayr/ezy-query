/* (C)2022 */
package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.util.List;

public interface EzyQuery<T> {

  static QueryAndParams buildQueryAndParams(
      EzyCriteria criteria, List<Field<?>> allFields, String baseSchema) {
    SqlBuilder builder = SqlBuilder.with(allFields, criteria);

    String s = builder.selectStmt();

    QueryAndParams w = builder.whereStmt();

    String orderBy = builder.orderByStmt();

    StringBuilder sb = new StringBuilder();

    StringBuilder queryBuilder =
        sb.append("SELECT \n")
            .append(s)
            .append("FROM ")
            .append(baseSchema)
            .append("\n")
            .append("WHERE ")
            .append(w.getSql());

    if (!criteria.isCount()) {

      queryBuilder.append("\n");

      if (!orderBy.isEmpty()) {
        queryBuilder.append(orderBy).append("\n");
      }
      queryBuilder
          .append("LIMIT ")
          .append(criteria.getLimit())
          .append(" OFFSET ")
          .append(criteria.getOffset());
    }

    return new QueryAndParams(queryBuilder.toString(), w.getParams());
  }

  QueryAndParams query(EzyCriteria params);

  Class<T> resultClass();

  List<Field<?>> fields();
}
