package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.*;
import io.github.kayr.ezyquery.util.Elf;

import java.util.List;
import java.util.stream.Collectors;

public class Cond implements ICond {

  private Object left;
  private Object right;
  private BinaryExpr.Type type;

  private Cond() {}

  public static Cond create(Object left, Object right, BinaryExpr.Type type) {
    Cond cond = new Cond();
    cond.left = left;
    cond.right = right;
    cond.type = type;
    return cond;
  }

  @Override
  public EzyExpr asExpr() {
    switch (type) {
      case IN:
        return new InExpr(leftExpr(), rightList());
      case NOT_IN:
        return new InExpr(leftExpr(), rightList()).notExpr();
      default:
        return new BinaryExpr(leftExpr(), rightExpr(), type);
    }
  }

  private EzyExpr leftExpr() {
    return expr(left);
  }

  private EzyExpr rightExpr() {
    return expr(right);
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private List<EzyExpr> rightList() {
    Elf.assertTrue(right instanceof List, "right must be a list");
    List<Object> rightList = (List<Object>) right;
    return rightList.stream().map(Cond::expr).collect(Collectors.toList());
  }

  private static EzyExpr expr(Object value) {
    if (value instanceof Field) {
      return new VariableExpr(((Field) value).getAlias());
    }
    if (value instanceof ICond) {
      return ((Cond) value).asExpr();
    }

    return new ConstExpr(value, ConstExpr.Type.ANY);
  }
}
