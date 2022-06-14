package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.UnaryExpr;

@lombok.AllArgsConstructor
public class UnaryCond implements ICond {

  private Object left;
  private UnaryExpr.Type operator;

  @Override
  public EzyExpr asExpr() {
    return new UnaryExpr(operator, Cond.expr(left));
  }
}
