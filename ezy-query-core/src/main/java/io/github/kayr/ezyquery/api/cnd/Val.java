package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.ConstExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;

public class Val implements ICond {

  Object value;

  public Val(Object value) {
    this.value = value;
  }

  public static Val create(Object value) {
    return new Val(value);
  }

  @Override
  public EzyExpr asExpr() {
    return new ConstExpr(value, ConstExpr.Type.ANY);
  }
}
