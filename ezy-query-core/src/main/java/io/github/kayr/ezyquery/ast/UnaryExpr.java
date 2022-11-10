package io.github.kayr.ezyquery.ast;

@lombok.AllArgsConstructor
@lombok.Getter
public class UnaryExpr implements EzyExpr {

  private Type type;
  private EzyExpr left;

  public enum Type {
    NOT("NOT"),
    MINUS("-"),
    PLUS("+"),
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");
    private final String sign;

    Type(String sign) {
      this.sign = sign;
    }

    public String getSign() {
      return sign;
    }
  }

  public String toString() {
    return asString();
  }

  private String asString() {
    switch (type) {
      case NOT:
        return String.format("not(%s)", left);
      case MINUS:
        return String.format("-%s", left);
      case PLUS:
        return String.format("+%s", left);
      case IS_NULL:
        return String.format("%s is null", left);
      case IS_NOT_NULL:
        return String.format("%s is not null", left);
      default:
        return String.format("%s %s", left, type);
    }
  }
}
