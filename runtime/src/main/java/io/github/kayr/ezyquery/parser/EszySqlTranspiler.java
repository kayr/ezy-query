package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.*;
import io.github.kayr.ezyquery.parser.EzyTranspileException;
import io.github.kayr.ezyquery.util.Elf;

import java.util.*;
import java.util.function.Function;

public class EszySqlTranspiler {

  private final EzyExpr expr;
  private final List<Field> fields;

  public EszySqlTranspiler(EzyExpr expr, List<Field> fields) {
    this.expr = expr;
    this.fields = fields;
    initHandlers();
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

          return left.append(" between ").append(start).append(" and ").append(end);
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
            return QueryAndParams.of("1 = 0");
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
          Optional<Field> fieldResult =
              fields.stream().filter(f -> f.getAlias().equals(fieldName)).findFirst();

          if (!fieldResult.isPresent()) {
            throw new EzyTranspileException("Unknown field " + fieldName);
          }

          Field field = fieldResult.get();

          return QueryAndParams.of(field.getSqlField(), Collections.emptyList());
        });

    register(
        ParensExpr.class,
        parensExpr -> {
          QueryAndParams sqlExpr = transpile(parensExpr.getExpr());
          return QueryAndParams.of("(").append(sqlExpr).append(")");
        });
  }

  private final Map<Class<? extends EzyExpr>, Function<? extends EzyExpr, QueryAndParams>> handlers =
      new HashMap<>();

  <T extends EzyExpr> void register(Class<T> clazz, Function<T, QueryAndParams> function) {

    handlers.put(clazz, function);
  }

  @lombok.Getter
  @lombok.AllArgsConstructor
  public static class QueryAndParams {

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
}
