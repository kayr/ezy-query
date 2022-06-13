package io.github.kayr.ezyquery.ast;

@lombok.AllArgsConstructor
@lombok.Getter
public class UnaryExpr implements EzyExpr {

  private Type type;
  private EzyExpr left;

  public enum Type {
    NOT("not"),
    MINUS("-"),
    PLUS("+"),
    IS_NULL("is null"),
    IS_NOT_NULL("is not null");
    private final String sign;

    Type(String sign) {
      this.sign = sign;
    }

    public String getSign() {
      return sign;
    }
  }

  public String toString() {
    String leftStr = left.isMultiExpr() ? "(" + left + ")" : left.toString();
    switch (type) {
      case NOT:
        return String.format("not%s", leftStr);
      case MINUS:
        return String.format("-%s", leftStr);
      case PLUS:
        return String.format("+%s", leftStr);
      case IS_NULL:
        return String.format("%s is null", leftStr);
      case IS_NOT_NULL:
        return String.format("%s is not null", leftStr);
      default:
        return String.format("%s %s", leftStr, type);
    }
  }
}
