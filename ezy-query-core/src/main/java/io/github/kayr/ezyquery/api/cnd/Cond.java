package io.github.kayr.ezyquery.api.cnd;

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
    return ICond.expr(left);
  }

  private EzyExpr rightExpr() {
    return ICond.expr(right);
  }

  public BinaryExpr.Op getOperator() {
    return operator;
  }

  private List<EzyExpr> rightList() {
    Elf.assertTrue(right instanceof List, "right must be a list on expression: " + this);
    List<Object> rightList = (List<Object>) right;
    return rightList.stream().map(ICond::expr).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "Cond{" + "left=" + left + ", right=" + right + ", op=" + operator + '}';
  }
}
