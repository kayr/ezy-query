/* (C)2022 */
package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.util.List;
import java.util.Optional;

public interface EzyQuery<T> {

  static QueryAndParams buildQueryAndParams(EzyCriteria criteria, EzyQuery<?> query) {
    SqlBuilder builder = SqlBuilder.with(query.fields(), criteria);

    String s = builder.selectStmt();

    QueryAndParams w = builder.whereStmt();

    String orderBy = builder.orderByStmt();

    StringBuilder sb = new StringBuilder();

    StringBuilder queryBuilder =
        sb.append("SELECT \n")
            .append(s)
            .append("FROM ")
            .append(query.schema())
            .append("\n")
            .append("WHERE ");

    Optional<String> whereClause = query.whereClause();
    if (whereClause.isPresent()) {
      queryBuilder
          .append("(")
          .append(whereClause.get())
          .append(") AND (")
          .append(w.getSql())
          .append(")");
    } else {
      queryBuilder.append(w.getSql());
    }

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

  String schema();

  default Optional<String> whereClause() {
    return Optional.empty();
  }
}
