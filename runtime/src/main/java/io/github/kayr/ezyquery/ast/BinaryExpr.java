package io.github.kayr.ezyquery.ast;

// e.g 1 + 8 , 1  and B
@lombok.AllArgsConstructor
@lombok.Getter
public class BinaryExpr implements EzyExpr {

  private EzyExpr left;
  private EzyExpr right;
  private Op operator;

  public String toString() {
    return asString();
  }

  private String asString() {
    return String.format("%s %s %s", left, operator, right);
  }

  public enum Op {
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
    NOT_LIKE("NOT LIKE"),
    IN("IN"),
    NOT_IN("NOT IN");

    private final String symbol;

    Op(String symbol) {
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
