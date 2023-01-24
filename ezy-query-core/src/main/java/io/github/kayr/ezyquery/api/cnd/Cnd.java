package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.UnaryExpr;
import io.github.kayr.ezyquery.util.ArrayElf;
import io.github.kayr.ezyquery.util.Elf;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Cnd {

  private static final Cond TRUE = Cnd.eq(1, 1);
  public static final Cond FALSE = Cnd.eq(1, 0);

  private Cnd() {}

  public static Val val(Object value) {
    return new Val(value);
  }

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

  public static Cond in(Object left, Collection<?> right) {
    return Cond.create(left, right, BinaryExpr.Op.IN);
  }

  public static Cond notIn(Object left, Object right) {
    return Cond.create(left, right, BinaryExpr.Op.NOT_IN);
  }

  public static Conds and(Object left, Object right) {
    return combine(left, right, BinaryExpr.Op.AND);
  }

  public static Conds or(Object left, Object right) {
    return combine(left, right, BinaryExpr.Op.OR);
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
    return Conds.andAll(conds);
  }

  public static Conds orAll(Object... conds) {
    return Conds.orAll(conds);
  }

  public static BetweenCond between(Object left, Object start, Object end) {
    return BetweenCond.between(left, start, end);
  }

  public static BetweenCond notBetween(Object left, Object start, Object end) {
    return BetweenCond.notBetween(left, start, end);
  }

  public static Cond trueCnd() {
    return TRUE;
  }

  public static ExprCond expr(String expr) {
    return ExprCond.expr(expr);
  }

  public static ExprCond expr(String expr, Object arg0, Object... args) {
    expr = String.format(expr, ArrayElf.addFirst(args, arg0));
    return ExprCond.expr(expr);
  }

  public static SqlCond sql(String sql, Object... params) {
    return SqlCond.sql(sql, Arrays.asList(params));
  }

  private static Conds combine(Object left, Object right, BinaryExpr.Op operator) {
    if (left instanceof ICond && right instanceof ICond)
      return combine(((ICond) left), (ICond) right, operator);
    return Conds.createConds(operator, left, right);
  }

  static Conds combine(ICond left, ICond right, BinaryExpr.Op combineOperator) {
    Optional<Conds> cond1 = tryCombine(left, right, combineOperator);
    return cond1.orElse(Conds.createConds(combineOperator, left, right));
  }

  private static Optional<Conds> tryCombine(
      ICond left, ICond right, BinaryExpr.Op combineOperator) {

    BinaryExpr.Op leftOp = booleanOperator(left);
    BinaryExpr.Op otherOp = booleanOperator(right);

    if (left.isConds() && right.isConds() && leftOp == otherOp && combineOperator == leftOp) {

      List<Object> cnds1 = ((Conds) left).getCnds();
      List<Object> cnds2 = ((Conds) right).getCnds();

      return Optional.of(
          Conds.createConds(((Conds) right).getOperator(), Elf.combine(cnds1, cnds2)));
    }

    if (left.isConds() && !right.isConds() && leftOp == combineOperator) {

      List<Object> cnds1 = ((Conds) left).getCnds();

      return Optional.of(Conds.createConds(combineOperator, Elf.addAll(cnds1, right)));
    }

    if (!left.isConds() && right.isConds() && otherOp == combineOperator) {

      List<Object> cnds2 = ((Conds) right).getCnds();

      return Optional.of(Conds.createConds(combineOperator, Elf.addFirst(cnds2, left)));
    }

    return Optional.empty();
  }

  // todo create an interface
  public static BinaryExpr.Op booleanOperator(ICond cond) {
    if (cond.isConds()) return ((Conds) cond).getOperator();
    if (cond instanceof Cond) return ((Cond) cond).getOperator();
    return null;
  }
}
