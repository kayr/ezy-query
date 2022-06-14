package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.BinaryExpr;

public class Cnd {
    public static Cond eq(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.EQ);
    }

    public static Cond neq(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.NEQ);
    }

    public static Cond lt(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.LT);
    }

    public static Cond gt(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.GT);
    }

    public static Cond lte(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.LTE);
    }

    public static Cond gte(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.GTE);
    }

    public static Cond like(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.LIKE);
    }

    public static Cond notLike(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.NOT_LIKE);
    }

    public static Cond in(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.IN);
    }

    public static Cond notIn(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.NOT_IN);
    }

    public static Cond and(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.AND);
    }

    public static Cond or(Object left, Object right) {
      return Cond.create(left, right, BinaryExpr.Type.OR);
    }
}
