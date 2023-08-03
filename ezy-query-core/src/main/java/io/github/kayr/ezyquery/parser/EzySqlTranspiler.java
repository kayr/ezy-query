package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.*;
import io.github.kayr.ezyquery.util.Elf;
import java.util.*;
import java.util.function.Function;

public class EzySqlTranspiler {

  private final EzyExpr expr;
  private final List<Field<?>> fields;

  public EzySqlTranspiler(EzyExpr expr, List<Field<?>> fields) {
    this.expr = expr;
    this.fields = fields;
    initHandlers();
  }

  public static QueryAndParams transpile(List<Field<?>> fields, EzyExpr ezyExpr) {
    return new EzySqlTranspiler(ezyExpr, fields).transpile();
  }

  public static QueryAndParams transpile(List<Field<?>> fields, String sql) {
    return transpile(fields, ExprParser.parseExpr(sql));
  }

  public QueryAndParams transpile() {
    return transpile(expr);
  }

  private QueryAndParams transpile(EzyExpr expr) {

    Function<EzyExpr, QueryAndParams> handler = findHandler(expr);

    return handler.apply(expr);
  }

  private Function<EzyExpr, QueryAndParams> findHandler(EzyExpr expr) {
    Function<? extends EzyExpr, QueryAndParams> handler = handlers.get(expr.getClass());
    if (handler == null) {
      throw new IllegalArgumentException("No handler for " + expr.getClass());
    }
    //noinspection unchecked
    return (Function<EzyExpr, QueryAndParams>) handler;
  }

  public void initHandlers() {

    register(
        ConstExpr.class,
        constExpr -> QueryAndParams.of("?", Collections.singletonList(constExpr.getValue())));

    register(
        BetweenExpr.class,
        betweenExpr -> {
          QueryAndParams left = transpile(betweenExpr.getLeft());
          QueryAndParams start = transpile(betweenExpr.getStart());
          QueryAndParams end = transpile(betweenExpr.getEnd());
          boolean isNot = betweenExpr.isNot();

          if (isNot) {
            return left.append(" NOT BETWEEN ").append(start).append(" AND ").append(end);
          }
          return left.append(" BETWEEN ").append(start).append(" AND ").append(end);
        });

    register(
        BinaryExpr.class,
        binaryExpr -> {
          QueryAndParams left = transpile(binaryExpr.getLeft());
          QueryAndParams right = transpile(binaryExpr.getRight());
          return left.append(" ")
              .append(binaryExpr.getOperator().symbol())
              .append(" ")
              .append(right);
        });

    register(
        InExpr.class,
        inExpr -> {
          if (Elf.isEmpty(inExpr.getCandidates())) {
            if (inExpr.isNot()) return QueryAndParams.of("1 = 1");
            else return QueryAndParams.of("1 = 0");
          }

          QueryAndParams left = transpile(inExpr.getLeft());

          if (inExpr.isNot()) {
            left = left.append(" NOT IN (");
          } else {
            left = left.append(" IN (");
          }

          boolean first = true;
          for (EzyExpr candidate : inExpr.getCandidates()) {
            QueryAndParams sqlPart = transpile(candidate);
            left = left.append(!first, ", ").append(sqlPart);
            first = false;
          }

          return left.append(")");
        });

    register(
        UnaryExpr.class,
        unaryExpr -> {
          QueryAndParams sqlExpr = transpile(unaryExpr.getLeft());
          switch (unaryExpr.getType()) {
            case MINUS:
              return QueryAndParams.of("-").append(sqlExpr);
            case PLUS:
              return QueryAndParams.of("+").append(sqlExpr);
            case IS_NOT_NULL:
              return sqlExpr.append(" IS NOT NULL");
            case IS_NULL:
              return sqlExpr.append(" IS NULL");
            case NOT:
              return QueryAndParams.of("NOT(").append(sqlExpr).append(")");
            default:
              throw new EzyTranspileException("Unknown unary operator " + unaryExpr.getType());
          }
        });

    register(
        VariableExpr.class,
        variableExpr -> {
          String fieldName = variableExpr.getVariable();
          Optional<Field<?>> fieldResult = findField(fieldName);

          if (!fieldResult.isPresent()) {
            throw new EzyTranspileException("Unknown field " + fieldName);
          }

          Field<?> field = fieldResult.get();

          String sqlField = field.getSqlField();
          if (field.getExpressionType() == Field.ExpressionType.BINARY) {
            sqlField = "(" + sqlField + ")";
          }
          return QueryAndParams.of(sqlField, Collections.emptyList());
        });

    register(
        ParensExpr.class,
        parensExpr -> {
          QueryAndParams sqlExpr = transpile(parensExpr.getExpr());
          return QueryAndParams.of("(").append(sqlExpr).append(")");
        });

    register(SqlExpr.class, sqlExpr -> QueryAndParams.of(sqlExpr.getSql(), sqlExpr.getParams()));
  }

  private Optional<Field<?>> findField(String alias) {
    return fields.stream().filter(f -> f.getAlias().equals(alias)).findFirst();
  }

  private final Map<Class<? extends EzyExpr>, Function<? extends EzyExpr, QueryAndParams>>
      handlers = new HashMap<>();

  <T extends EzyExpr> void register(Class<T> clazz, Function<T, QueryAndParams> function) {

    handlers.put(clazz, function);
  }
}
