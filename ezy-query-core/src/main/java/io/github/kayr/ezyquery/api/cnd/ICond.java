package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.ConstExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.VariableExpr;

public interface ICond {

  static EzyExpr expr(Object value) {
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

  default Conds and(Object cond) {
    return Cnd.and(this, cond);
  }

  default Conds or(Object cond) {
    return Cnd.or(this, cond);
  }

  default boolean isConds() {
    return this instanceof Conds;
  }

  EzyExpr asExpr();
}
