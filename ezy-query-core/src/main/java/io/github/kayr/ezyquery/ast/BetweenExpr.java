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
    return asString();
  }

  private String asString() {
    if (not) {
      return String.format("%s NOT BETWEEN %s AND %s", left, start, end);
    }
    return String.format("%s BETWEEN %s AND %s", left, start, end);
  }
}
