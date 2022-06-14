package io.github.kayr.ezyquery.transpile;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.*;
import io.github.kayr.ezyquery.parser.EzyTranspileException;
import io.github.kayr.ezyquery.util.Elf;

import java.util.*;
import java.util.function.Function;

public class Transpiler {

  private final EzyExpr expr;
  private final List<Field> fields;

  public Transpiler(EzyExpr expr, List<Field> fields) {
    this.expr = expr;
    this.fields = fields;
    initHandlers();
  }

  public Result transpile() {
    return transpile(expr);
  }

  private Result transpile(EzyExpr expr) {

    Function<EzyExpr, Result> handler = findHandler(expr);

    return handler.apply(expr);
  }

  private Function<EzyExpr, Result> findHandler(EzyExpr expr) {
    Function<? extends EzyExpr, Result> handler = handlers.get(expr.getClass());
    if (handler == null) {
      throw new IllegalArgumentException("No handler for " + expr.getClass());
    }
    //noinspection unchecked
    return (Function<EzyExpr, Result>) handler;
  }

  public void initHandlers() {

    register(
        ConstExpr.class,
        constExpr -> new Result("?", Collections.singletonList(constExpr.getValue())));

    register(
        BetweenExpr.class,
        betweenExpr -> {
          Result left = transpile(betweenExpr.getLeft());
          Result start = transpile(betweenExpr.getStart());
          Result end = transpile(betweenExpr.getEnd());

          return left.append(" between ").append(start).append(" and ").append(end);
        });

    register(
        BinaryExpr.class,
        binaryExpr -> {
          Result left = transpile(binaryExpr.getLeft());
          Result right = transpile(binaryExpr.getRight());
          return left.append(" ")
              .append(binaryExpr.getOperator().symbol())
              .append(" ")
              .append(right);
        });

    register(
        InExpr.class,
        inExpr -> {
          Result left = transpile(inExpr.getLeft());

          if (inExpr.isNot()) {
            left = left.append(" not in (");
          } else {
            left = left.append(" in (");
          }

          for (EzyExpr candidate : inExpr.getCandidates()) {
            Result sqlPart = transpile(candidate);
            left = left.append(", ").append(sqlPart);
          }

          return left.append(")");
        });

    register(
        UnaryExpr.class,
        unaryExpr -> {
          Result sqlExpr = transpile(unaryExpr.getLeft());
          switch (unaryExpr.getType()) {
            case MINUS:
              return sqlExpr.append("-").append(sqlExpr);
            case PLUS:
              return sqlExpr.append("+").append(sqlExpr);
            case IS_NOT_NULL:
              return sqlExpr.append(" is not null");
            case IS_NULL:
              return sqlExpr.append(" is null");
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

          return new Result(field.getSqlField(), Collections.emptyList());
        });

    register(
        ParensExpr.class,
        parensExpr -> {
          Result sqlExpr = transpile(parensExpr.getExpr());
          return new Result("(").append(sqlExpr).append(")");
        });
  }

  private final Map<Class<? extends EzyExpr>, Function<? extends EzyExpr, Result>> handlers =
      new HashMap<>();

  <T extends EzyExpr> void register(Class<T> clazz, Function<T, Result> function) {

    handlers.put(clazz, function);
  }

  @lombok.Getter
  @lombok.AllArgsConstructor
  public static class Result {
    private String sql;
    private List<Object> params = Collections.emptyList();

    public Result(String sql) {
      this.sql = sql;
    }

    Result append(String sql) {
      return new Result(this.sql + sql, params);
    }

    Result append(Result sql) {
      return new Result(this.sql + sql.getSql(), Elf.combine(this.params, sql.params));
    }

    @Override
    public String toString() {
      return "Result{" + "sql='" + sql + '\'' + ", params=" + params + '}';
    }
  }
}
