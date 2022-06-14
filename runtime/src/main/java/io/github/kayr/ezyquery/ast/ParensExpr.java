package io.github.kayr.ezyquery.ast;

@lombok.Getter
@lombok.AllArgsConstructor
public class ParensExpr implements EzyExpr {

  private EzyExpr expr;

  @Override
  public String toString() {
    return String.format("(%s)", expr);
  }
}
