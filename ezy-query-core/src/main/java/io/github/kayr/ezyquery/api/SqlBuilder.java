package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.EzySql;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.ParensExpr;
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
  private final EzyCriteria ezyCriteria;

  private Map<String, Field<?>> fieldMap = new HashMap<>();

  public SqlBuilder(List<Field<?>> fields, EzyCriteria ezyCriteria) {
    this.fields = fields;
    this.ezyCriteria = ezyCriteria;
    for (Field<?> f : fields) {
      fieldMap.put(f.getAlias(), f);
    }
  }

  public static SqlBuilder with(List<Field<?>> fields, EzyCriteria filterParams) {
    return new SqlBuilder(fields, filterParams);
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
      Field<?> theField = getFields(columnName);

      selectPart
          .append("  ")
          .append(theField.getSqlField())
          .append(" as ")
          .append(theField.getAlias());

      if (i < size - 1) {
        selectPart.append(", ");
      }

      selectPart.append("\n");
    }

    return selectPart.toString();
  }

  public QueryAndParams whereStmt() {

    if (Elf.isEmpty(ezyCriteria.getConditions())
        && Elf.isEmpty(ezyCriteria.getConditionExpressions())) {
      return EzySql.transpile(fields, Cnd.trueCnd().asExpr());
    }

    EzyExpr stringExpr = combineExpressions(ezyCriteria.getConditionExpressions());

    BinaryExpr.Op operator = combineOperator();

    // process condition objects
    EzyExpr apiExpr =
        ezyCriteria.getConditions().stream()
            .map(ICond::expr)
            .reduce((l, r) -> new BinaryExpr(l, r, operator))
            .orElse(Cnd.trueCnd().asExpr());

    EzyExpr combined =
        new BinaryExpr(new ParensExpr(stringExpr), new ParensExpr(apiExpr), operator);

    // avoid statements like 1 = 1 and 7 = x
    if (Elf.isEmpty(ezyCriteria.getConditions())) {
      combined = stringExpr;
    } else if (Elf.isEmpty(ezyCriteria.getConditionExpressions())) {
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

  public String orderByStmt(String defaultOrderBy) {

    if (Elf.isEmpty(ezyCriteria.getSorts())) {
      return Optional.ofNullable(defaultOrderBy)
               .map(s -> " ORDER BY " + s)
               .orElse("");
    }

    StringBuilder orderByPart = new StringBuilder();

    orderByPart.append("ORDER BY ");

    int size = ezyCriteria.getSorts().size();
    for (int i = 0; i < size; i++) {

      Sort sort = ezyCriteria.getSorts().get(i);

      Field<?> theField = getFields(sort.getField());

      orderByPart.append(theField.getSqlField()).append(" ").append(sort.getDir());

      if (i < size - 1) {
        orderByPart.append(", ");
      }
    }

    return orderByPart.toString();
  }

  private BinaryExpr.Op combineOperator() {
    return ezyCriteria.isUseOr() ? BinaryExpr.Op.OR : BinaryExpr.Op.AND;
  }

  private Field<?> getFields(String alias) {
    Field<?> field = fieldMap.get(alias);
    if (field == null) {
      throw new IllegalArgumentException("Field with alias [" + alias + "] not found");
    }
    return field;
  }
}
