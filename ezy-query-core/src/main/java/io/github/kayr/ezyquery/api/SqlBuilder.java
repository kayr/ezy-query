package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.EzySqlTranspiler;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.parser.SqlParts;
import io.github.kayr.ezyquery.util.Elf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SqlBuilder {

  private final List<Field<?>> fields;
  private final EzyCriteria ezyCriteria;

  private final Map<String, Field<?>> fieldMap = new HashMap<>();

  public SqlBuilder(List<Field<?>> fields, EzyCriteria ezyCriteria) {
    this.fields = fields;
    this.ezyCriteria = ezyCriteria;
    for (Field<?> f : fields) {
      fieldMap.put(f.getAlias(), f);
    }
  }

  public static SqlBuilder with(EzyQuery ezyQuery, EzyCriteria filterParams) {
    return new SqlBuilder(ezyQuery.fields(), filterParams);
  }

  public String selectStmt() {

    if (ezyCriteria.isCount()) {
      return " COUNT(*) \n";
    }

    List<String> columns =
        Elf.isEmpty(ezyCriteria.getColumns())
            ? fields.stream().map(Field::getAlias).collect(Collectors.toList())
            : ezyCriteria.getColumns();

    StringBuilder selectPart = new StringBuilder();

    int size = columns.size();
    for (int i = 0; i < size; i++) {

      String columnName = columns.get(i);
      Field<?> theField = getField(columnName);

      selectPart
          .append("  ")
          .append(theField.getSqlField())
          .append(" as \"")
          .append(theField.getAlias())
          .append("\"");

      if (i < size - 1) {
        selectPart.append(", ");
      }

      selectPart.append("\n");
    }

    return selectPart.toString();
  }

  public QueryAndParams whereStmt() {

    if (Elf.isEmpty(ezyCriteria.getConditions())) {
      return EzySqlTranspiler.transpile(fields, Cnd.sql("1 = 1").asExpr());
    }

    // process condition objects
    EzyExpr expr =
        ezyCriteria.getConditions().stream()
            .reduce(Cnd::and)
            .map(ICond::asExpr)
            .orElse(Cnd.trueCnd().asExpr());

    return EzySqlTranspiler.transpile(fields, expr);
  }

  public QueryAndParams orderByStmt(SqlParts defaultOrderBy) {

    if (Elf.isEmpty(ezyCriteria.getSorts())) {
      return Optional.ofNullable(defaultOrderBy)
          .map(
              sqlParts ->
                  QueryAndParams.of(" ORDER BY ")
                      .append(sqlParts.getQuery(ezyCriteria.getParamValues())))
          .orElse(null);
    }

    QueryAndParams orderByPart = QueryAndParams.of("ORDER BY ");

    int size = ezyCriteria.getSorts().size();
    for (int i = 0; i < size; i++) {

      Sort sort = ezyCriteria.getSorts().get(i);

      Field<?> theField = getField(sort.getField());

      orderByPart =
          orderByPart.append(theField.getSqlField()).append(" ").append(sort.getDir().toString());

      if (i < size - 1) {
        orderByPart = orderByPart.append(", ");
      }
    }

    return orderByPart;
  }

  private Field<?> getField(String alias) {
    Field<?> field = fieldMap.get(alias);
    if (field == null) {
      throw new IllegalArgumentException("Field with alias [" + alias + "] not found");
    }
    return field;
  }

  QueryAndParams build(EzyQuery query) {

    QueryAndParams queryBuilder = new QueryAndParams("");

    Optional<SqlParts> preQuery = query.preQuery();
    if (preQuery.isPresent()) {
      queryBuilder = queryBuilder.append(preQuery.get().getQuery(ezyCriteria.getParamValues()));
    }

    queryBuilder = queryBuilder.newLine().append("SELECT \n");

    String s = selectStmt();

    QueryAndParams dynamicWhereClause = whereStmt();

    QueryAndParams orderBy = orderByStmt(query.orderByClause().orElse(null));

    queryBuilder =
        queryBuilder
            .append(s)
            .append("FROM ")
            .append(query.schema().getQuery(ezyCriteria.getParamValues()))
            .append("\n")
            .append("WHERE ");

    Optional<SqlParts> defaultWhereClause = query.whereClause();
    if (defaultWhereClause.isPresent()) {
      String dynWhereStr = Elf.mayBeAddParens(dynamicWhereClause.getSql());
      QueryAndParams defaultWhere = defaultWhereClause.get().getQuery(ezyCriteria.getParamValues());
      queryBuilder =
          queryBuilder
              .append("(")
              .append(defaultWhere)
              .append(") AND ")
              .append(dynWhereStr, dynamicWhereClause.getParams());
    } else {
      queryBuilder = queryBuilder.append(dynamicWhereClause);
    }

    if (!ezyCriteria.isCount()) {

      queryBuilder = queryBuilder.append("\n");

      if (orderBy != null) {
        queryBuilder = queryBuilder.append(orderBy).append("\n");
      }
      queryBuilder =
          queryBuilder
              .append("LIMIT ")
              .append(ezyCriteria.getLimit() + "")
              .append(" OFFSET ")
              .append(ezyCriteria.getOffset() + "");
    }

    return queryBuilder;
  }

  public static QueryAndParams buildSql(EzyQuery query, EzyCriteria criteria) {
    return SqlBuilder.with(query, criteria).build(query);
  }
}
