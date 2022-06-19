package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.BetweenExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;

@lombok.AllArgsConstructor
public class BetweenCond implements ICond {

  private Object left;
  private Object start;
  private Object end;
  private boolean not;

  public static BetweenCond between(Object left, Object start, Object end) {
    return new BetweenCond(left, start, end, false);
  }

  public static BetweenCond notBetween(Object left, Object start, Object end) {
    return new BetweenCond(left, start, end, true);
  }

  @Override
  public EzyExpr asExpr() {
    return new BetweenExpr(Cond.expr(left), Cond.expr(start), Cond.expr(end), not);
  }
}
