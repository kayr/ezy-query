package io.github.kayr.ezyquery.ast;

import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
public class ParensExpr implements EzyExpr {

  private EzyExpr expr;

  @Override
  public String toString() {
    return String.format("(%s)", expr);
  }
}
