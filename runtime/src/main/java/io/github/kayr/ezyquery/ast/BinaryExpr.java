package io.github.kayr.ezyquery.ast;

// e.g 1 + 8 , 1  and B
@lombok.AllArgsConstructor
@lombok.Getter
public class BinaryExpr implements EzyExpr {

  private EzyExpr left;
  private EzyExpr right;
  private Type operator;

  public String toString() {
    return String.format("%s %s %s", left, operator, right);
  }

  public enum Type {
    AND("AND"),
    OR("OR"),
    EQ("="),
    NEQ("<>"),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE");

    private final String symbol;

    Type(String symbol) {
      this.symbol = symbol;
    }

    public String symbol() {
      return symbol;
    }

    @Override
    public String toString() {
      return symbol;
    }
  }
}
