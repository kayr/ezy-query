package io.github.kayr.ezyquery.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@lombok.Getter
public class BetweenExpr implements EzyExpr {
  private EzyExpr left;
  private EzyExpr start;
  private EzyExpr end;
  private boolean not;

  @Override
  public String toString() {
    if (not) {
      return String.format("(%s not between %s AND %s)", left, start, end);
    }
    return String.format("(%s between %s and %s)", left, start, end);
  }
}
