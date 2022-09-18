package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.EzySql;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.util.Elf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@lombok.AllArgsConstructor
public class SqlBuilder {

  private final List<Field<?>> fields;
  private final FilterParams filterParams;

  private Map<String, Field<?>> fieldMap = new HashMap<>();

  public SqlBuilder(List<Field<?>> fields, FilterParams filterParams) {
    this.fields = fields;
    this.filterParams = filterParams;
    for (Field<?> f : fields) {
      fieldMap.put(f.getAlias(), f);
    }
  }

  public static SqlBuilder with(List<Field<?>> fields, FilterParams filterParams) {
    return new SqlBuilder(fields, filterParams);
  }

  public String selectStmt() {

    if (filterParams.isCount()) {
      return " COUNT(*) \n";
    }

    List<String> columns =
        Elf.isEmpty(filterParams.getColumns())
            ? fields.stream().map(Field::getAlias).collect(Collectors.toList())
            : filterParams.getColumns();

    StringBuilder selectPart = new StringBuilder();

    int size = columns.size();
    for (int i = 0; i < size; i++) {

      String columnName = columns.get(i);
      Field<?> theField = getFields(columnName);

      selectPart.append(theField.getSqlField()).append(" as ").append(theField.getAlias());

      if (i < size - 1) {
        selectPart.append(", ");
      }

      selectPart.append("\n");
    }

    return selectPart.toString();
  }

  public QueryAndParams whereStmt() {

    if (Elf.isEmpty(filterParams.getConditions())
        && Elf.isEmpty(filterParams.getConditionExpressions())) {
      return EzySql.transpile(fields, Cnd.trueCnd().asExpr());
    }

    EzyExpr stringExpr = combineExpressions(filterParams.getConditionExpressions());

    BinaryExpr.Op operator = combineOperator();

    // process condition objects
    EzyExpr apiExpr =
        filterParams.getConditions().stream()
            .map(ICond::expr)
            .reduce((l, r) -> new BinaryExpr(l, r, operator))
            .orElse(Cnd.trueCnd().asExpr());

    EzyExpr combined = new BinaryExpr(stringExpr, apiExpr, BinaryExpr.Op.AND);

    // avoid statements like 1 = 1 and 7 = x
    if (Elf.isEmpty(filterParams.getConditions())) {
      combined = stringExpr;
    } else if (Elf.isEmpty(filterParams.getConditionExpressions())) {
      combined = apiExpr;
    }

    return EzySql.transpile(fields, combined);
  }

  private EzyExpr combineExpressions(List<String> expressions) {

    if (Elf.isEmpty(expressions)) {
      return Cnd.trueCnd().asExpr();
    }
    BinaryExpr.Op operator = combineOperator();

    Optional<EzyExpr> reduced =
        expressions.stream()
            .map(ExprParser::parseExpr)
            .reduce((l, r) -> new BinaryExpr(l, r, operator));

    return reduced.orElseThrow(() -> new IllegalStateException("Conditions is empty"));
  }

  private BinaryExpr.Op combineOperator() {
    return filterParams.isUseOr() ? BinaryExpr.Op.OR : BinaryExpr.Op.AND;
  }

  private Field<?> getFields(String alias) {
    Field<?> field = fieldMap.get(alias);
    if (field == null) {
      throw new IllegalArgumentException("Field with alias [" + alias + "] not found");
    }
    return field;
  }
}
