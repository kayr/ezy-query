package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.*;
import io.github.kayr.ezyquery.util.Elf;

import java.util.List;
import java.util.stream.Collectors;

public class Cond implements ICond {

  private Object left;
  private Object right;
  private BinaryExpr.Op operator;

  private Cond() {}

  public static Cond create(Object left, Object right, BinaryExpr.Op operator) {
    Cond cond = new Cond();
    cond.left = left;
    cond.right = right;
    cond.operator = operator;
    return cond;
  }

  @Override
  public EzyExpr asExpr() {
    switch (operator) {
      case IN:
        return new InExpr(leftExpr(), rightList());
      case NOT_IN:
        return new InExpr(leftExpr(), rightList()).notExpr();
      default:
        return new BinaryExpr(leftExpr(), rightExpr(), operator);
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
    Elf.assertTrue(right instanceof List, "right must be a list on expression: " + this);
    List<Object> rightList = (List<Object>) right;
    return rightList.stream().map(Cond::expr).collect(Collectors.toList());
  }

  static EzyExpr expr(Object value) {
    if (value instanceof Field) { // todo a hash can also be a field
      return new VariableExpr(((Field) value).getAlias());
    }

    if (value instanceof String && ((String) value).startsWith("#")) {
      return new VariableExpr(((String) value).substring(1));
    }

    if (value instanceof ICond) {
      return ((Cond) value).asExpr();
    }

    return new ConstExpr(value, ConstExpr.Type.ANY);
  }

  @Override
  public String toString() {
    return "Cond{" + "left=" + left + ", right=" + right + ", op=" + operator + '}';
  }
}
