package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.BinaryExpr;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.ParensExpr;
import io.github.kayr.ezyquery.parser.ExprParser;

@lombok.Getter
public class ExprCond implements ICond {
  private String expr;

  private ExprCond(String expr) {
    this.expr = expr;
  }

  public static ExprCond expr(String expr) {
    return new ExprCond(expr);
  }

  @Override
  public EzyExpr asExpr() {
    EzyExpr ezyExpr = ExprParser.parseExpr(expr);
    if (ezyExpr instanceof BinaryExpr) {
      return new ParensExpr(ezyExpr);
    }
    return ezyExpr;
  }
}
