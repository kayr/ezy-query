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
    return new BetweenExpr(ICond.expr(left), ICond.expr(start), ICond.expr(end), not);
  }

  @Override
  public String toString() {
    return "BetweenCond{"
        + "left="
        + left
        + ", start="
        + start
        + ", end="
        + end
        + ", not="
        + not
        + '}';
  }
}
