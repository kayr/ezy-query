package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.UnaryExpr;

@lombok.AllArgsConstructor
public class UnaryCond implements ICond {

  private Object left;
  private UnaryExpr.Type operator;

  @Override
  public EzyExpr asExpr() {
    return new UnaryExpr(operator, ICond.expr(left));
  }

  @Override
  public String toString() {
    return "UnaryCond{" + "left=" + left + ", op=" + operator + '}';
  }
}
