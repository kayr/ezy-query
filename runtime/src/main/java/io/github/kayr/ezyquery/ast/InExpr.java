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
    if (not) {
      return String.format("%s not in %s", left, candidates);
    }
    return String.format("%s in %s", left, candidates);
  }

  public InExpr notExpr() {
    return new InExpr(left, candidates, true);
  }
}
