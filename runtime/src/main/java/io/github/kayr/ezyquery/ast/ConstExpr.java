package io.github.kayr.ezyquery.ast;

@lombok.Getter
@lombok.AllArgsConstructor
public class ConstExpr implements EzyExpr {

  private Object value;
  private Type type;

  public String toString() {
    return asString();
  }

  private String asString() {
    if (type == Type.STRING) {
      return String.format("'%s'", value);
    }
    return String.format("%s", value);
  }

  public enum Type {
    STRING,
    NUMBER,
    BOOLEAN,
    ANY
  }
}
