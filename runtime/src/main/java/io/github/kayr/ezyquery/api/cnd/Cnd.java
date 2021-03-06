package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.UnaryExpr;

public class Cnd {

  private Cnd() {}

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
    return new UnaryCond(left, UnaryExpr.Type.MINUS);
  }

  public static UnaryCond positive(Object left) {
    return new UnaryCond(left, UnaryExpr.Type.PLUS);
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

  public static Conds andAll(Object... conds) {
    return Conds.and(conds);
  }

  public static Conds orAll(Object... conds) {
    return Conds.or(conds);
  }

  public static BetweenCond between(Object left, Object start, Object end) {
    return BetweenCond.between(left, start, end);
  }

  public static BetweenCond notBetween(Object left, Object start, Object end) {
    return BetweenCond.notBetween(left, start, end);
  }

  public static Cond trueCnd() {
    return Cnd.eq(1, 1);
  }
}
