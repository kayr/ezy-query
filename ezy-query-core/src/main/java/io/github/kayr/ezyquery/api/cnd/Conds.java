package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.ParensExpr;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@lombok.RequiredArgsConstructor
public class Conds implements ICond {

  private final List<Object> cnds;
  private final BinaryExpr.Op operator;

  @Override
  public EzyExpr asExpr() {
    Optional<EzyExpr> reduce =
        cnds.stream()
            .map(ICond::expr)
            .reduce((left, right) -> new BinaryExpr(left, right, operator));
    EzyExpr condition = reduce.orElseThrow(() -> new IllegalStateException("Conditions is empty"));
    return new ParensExpr(condition);
  }

  public static Conds andAll(Object... cond) {
    return createConds(BinaryExpr.Op.AND, cond);
  }

  public static Conds orAll(Object... cond) {
    return createConds(BinaryExpr.Op.OR, cond);
  }

  static Conds createConds(BinaryExpr.Op op, List<Object> cond) {
    return new Conds(cond, op);
  }

  static Conds createConds(BinaryExpr.Op op, Object... cond) {
    return new Conds(Arrays.asList(cond), op);
  }

  public BinaryExpr.Op getOperator() {
    return operator;
  }

  public List<Object> getCnds() {
    return cnds;
  }

  @Override
  public String toString() {
    return "Conds{" + "cnds=" + cnds + ", op=" + operator + '}';
  }
}
