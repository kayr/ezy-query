package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.ConstExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.VariableExpr;

public interface ICond {

  public static EzyExpr expr(Object value) {
    if (value instanceof Field) {
      return new VariableExpr(((Field) value).getAlias());
    }

    if (value instanceof String && ((String) value).startsWith("#")) {
      return new VariableExpr(((String) value).substring(1));
    }

    if (value instanceof ICond) {
      return ((ICond) value).asExpr();
    }

    return new ConstExpr(value, ConstExpr.Type.ANY);
  }

  EzyExpr asExpr();


}
