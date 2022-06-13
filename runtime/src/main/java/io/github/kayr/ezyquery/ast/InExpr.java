package io.github.kayr.ezyquery.ast;

import java.util.List;

@lombok.Getter
public class InExpr implements EzyExpr {

  private boolean not;
  private EzyExpr left;
  private List<EzyExpr> candidates;

  public InExpr(EzyExpr left, List<EzyExpr> candidates) {
    this(left, candidates, false);
  }

  public InExpr(EzyExpr left, List<EzyExpr> candidates, boolean not) {
    this.not = not;
    this.left = left;
    this.candidates = candidates;
  }

  public String toString() {
    String leftStr = left.isMultiExpr() ? "(" + left + ")" : left.toString();
    if (not) {
      return String.format("(%s not in %s)", leftStr, candidates);
    }
    return String.format("(%s in %s)", leftStr, candidates);
  }

  public InExpr notExpr() {
    return new InExpr(left, candidates, true);
  }
}
