package io.github.kayr.ezyquery.ast;


@lombok.Getter
@lombok.AllArgsConstructor
public class ConstExpr implements EzyExpr {

  private Object value;
  private Type type;

    public String toString() {
        return String.format("%s", value);
    }

  public enum Type {
    STRING,
    NUMBER,
    BOOLEAN
  }
}
