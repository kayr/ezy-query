package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.UnaryExpr;

public class Cnd {
  public static Cond eq(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.EQ);
  }

  public static Cond neq(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.NEQ);
  }

  public static Cond lt(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.LT);
  }

  public static Cond gt(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.GT);
  }

  public static Cond lte(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.LTE);
  }

  public static Cond gte(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.GTE);
  }

  public static Cond like(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.LIKE);
  }

  public static Cond notLike(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.NOT_LIKE);
  }

  public static Cond in(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.IN);
  }

  public static Cond notIn(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.NOT_IN);
  }

  public static Cond and(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.AND);
  }

  public static Cond or(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.OR);
  }

  public static UnaryCond negate(Object left) {
    return new UnaryCond(left, UnaryExpr.Type.NOT);
  }

  public static UnaryCond postive(Object left) {
    return new UnaryCond(left, UnaryExpr.Type.NOT);
  }

  public static UnaryCond not(Object left) {
    return new UnaryCond(left, UnaryExpr.Type.NOT);
  }

  public static UnaryCond isNull(Object left) {
    return new UnaryCond(left, UnaryExpr.Type.IS_NULL);
  }

  public static UnaryCond isNotNull(Object left) {
    return new UnaryCond(left, UnaryExpr.Type.IS_NOT_NULL);
  }
}
