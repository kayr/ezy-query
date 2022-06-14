package io.github.kayr.ezyquery.ast;

@lombok.Setter
@lombok.AllArgsConstructor
public class VariableExpr implements EzyExpr {

  private String variable;

  public String toString() {
    return String.format("%s", variable);
  }

  public String getVariable() {
    return variable;
  }
}
