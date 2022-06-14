package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.ParensExpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Conds implements ICond {

  private List<ICond> conds = new ArrayList<>();
  private BinaryExpr.Type type;

  @Override
  public EzyExpr asExpr() {
    Optional<EzyExpr> reduce =
        conds.stream().map(ICond::asExpr).reduce((left, right) -> new BinaryExpr(left, right, type));
    EzyExpr condition = reduce.orElseThrow(() -> new IllegalStateException("Conditions is empty"));
    return new ParensExpr(condition);

  }

  public static Conds and(ICond... cond) {
    return createConds(BinaryExpr.Type.AND, cond);
  }

  public static Conds or(ICond... cond) {
    return createConds(BinaryExpr.Type.OR, cond);
  }

  private static Conds createConds(BinaryExpr.Type or, ICond[] cond) {
    Conds conds = new Conds();
    conds.type = or;
    Collections.addAll(conds.conds, cond);
    return conds;
  }
}
