package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.ConstExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.VariableExpr;
import io.github.kayr.ezyquery.util.Elf;
import java.util.List;
import java.util.Optional;

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

  EzyExpr asExpr();

  default Conds and(ICond cond) {
    Optional<Conds> cond1 = combine(cond, BinaryExpr.Op.AND);
    return cond1.orElse(Conds.and(this, cond));
  }

  default Conds or(ICond cond) {
    Optional<Conds> combine = combine(cond, BinaryExpr.Op.OR);
    return combine.orElse(Conds.or(this, cond));
  }

  default Optional<Conds> combine(ICond cond, BinaryExpr.Op combineOperator) {

    BinaryExpr.Op thisOp = operator(this);
    BinaryExpr.Op otherOp = operator(cond);

    if (isConds() && cond.isConds() && thisOp == otherOp && combineOperator == thisOp) {

      List<Object> cnds1 = ((Conds) this).getCnds();
      List<Object> cnds2 = ((Conds) cond).getCnds();

      return Optional.of(
          Conds.createConds(((Conds) cond).getOperator(), Elf.combine(cnds1, cnds2)));
    }

    if (isConds() && !cond.isConds() && thisOp == combineOperator) {

      List<Object> cnds1 = ((Conds) this).getCnds();

      return Optional.of(Conds.createConds(combineOperator, Elf.addAll(cnds1, cond)));
    }
    return Optional.empty();
  }

  default boolean isConds() {
    return this instanceof Conds;
  }

  // todo create an interface
  static BinaryExpr.Op operator(ICond cond) {
    if (cond.isConds()) return ((Conds) cond).getOperator();
    if (cond instanceof Cond) return ((Cond) cond).getOperator();
    return null;
  }
}
