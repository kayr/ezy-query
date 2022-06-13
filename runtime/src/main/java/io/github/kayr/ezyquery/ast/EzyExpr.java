package io.github.kayr.ezyquery.ast;

public interface EzyExpr {

  default boolean isMultiExpr() {
    return !(this instanceof ConstExpr) && !(this instanceof VariableExpr);
  }
}
