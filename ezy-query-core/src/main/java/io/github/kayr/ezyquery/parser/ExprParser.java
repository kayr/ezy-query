/* (C)2022 */
package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.ast.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;

/** In the future, we could implement a custom lightweight sql parser */
public class ExprParser {

  final String expression;

  public ExprParser(String expression) {
    this.expression = expression;
    initHandlers();
  }

  @lombok.SneakyThrows
  public EzyExpr parse() {
    try {
      Expression cond = CCJSqlParserUtil.parseCondExpression(expression, false);
      return toEzyExpr(cond);
    } catch (JSQLParserException e) {
      throw new EzyParseException("Failed to parse statement: " + e.getMessage(), e);
    }
  }

  public static EzyExpr parseExpr(String expression) {
    return new ExprParser(expression).parse();
  }

  final Map<Class<? extends Expression>, Function<? extends Expression, EzyExpr>> handlers =
      new HashMap<>();

  EzyExpr toEzyExpr(Expression expr) {

    Function<Expression, EzyExpr> handler = findHandler(expr);

    return handler.apply(expr);
  }

  private Function<Expression, EzyExpr> findHandler(Expression expr) {
    Class<? extends Expression> clazz = expr.getClass();
    if (handlers.containsKey(clazz)) {
      //noinspection unchecked
      return (Function<Expression, EzyExpr>) handlers.get(clazz);
    }
    throw new EzyParseException(
        "UnSupported Expression: [" + clazz.getSimpleName() + "]: " + expression);
  }

  public void initHandlers() {

    // to handle Signed Expression
    register(StringValue.class, sv -> new ConstExpr(sv.getValue(), ConstExpr.Type.STRING));

    register(LongValue.class, lv -> new ConstExpr(lv.getValue(), ConstExpr.Type.NUMBER));

    register(DoubleValue.class, dv -> new ConstExpr(dv.getValue(), ConstExpr.Type.NUMBER));

    register(
        SignedExpression.class,
        se -> {
          if (isNegative(se)) {
            return new UnaryExpr(UnaryExpr.Type.MINUS, toEzyExpr(se.getExpression()));
          } else {
            return new UnaryExpr(UnaryExpr.Type.PLUS, toEzyExpr(se.getExpression()));
          }
        });

    register(
        AndExpression.class,
        expr -> binaryExpr(expr.getLeftExpression(), expr.getRightExpression(), BinaryExpr.Op.AND));

    register(
        OrExpression.class,
        orExpr ->
            binaryExpr(orExpr.getLeftExpression(), orExpr.getRightExpression(), BinaryExpr.Op.OR));

    register(
        Addition.class,
        addExpr ->
            binaryExpr(
                addExpr.getLeftExpression(), addExpr.getRightExpression(), BinaryExpr.Op.PLUS));

    register(
        Subtraction.class,
        subExpr ->
            binaryExpr(
                subExpr.getLeftExpression(), subExpr.getRightExpression(), BinaryExpr.Op.MINUS));

    register(
        Division.class,
        divExpr ->
            binaryExpr(
                divExpr.getLeftExpression(), divExpr.getRightExpression(), BinaryExpr.Op.DIV));

    register(
        Multiplication.class,
        mulExpr ->
            binaryExpr(
                mulExpr.getLeftExpression(), mulExpr.getRightExpression(), BinaryExpr.Op.MUL));

    register(
        Modulo.class,
        modExpr ->
            binaryExpr(
                modExpr.getLeftExpression(), modExpr.getRightExpression(), BinaryExpr.Op.MOD));

    register(
        GreaterThan.class,
        gtExpr ->
            binaryExpr(gtExpr.getLeftExpression(), gtExpr.getRightExpression(), BinaryExpr.Op.GT));

    register(
        GreaterThanEquals.class,
        gteExpr ->
            binaryExpr(
                gteExpr.getLeftExpression(), gteExpr.getRightExpression(), BinaryExpr.Op.GTE));

    register(
        MinorThanEquals.class,
        ltExpr ->
            binaryExpr(ltExpr.getLeftExpression(), ltExpr.getRightExpression(), BinaryExpr.Op.LTE));

    register(
        MinorThan.class,
        lteExpr ->
            binaryExpr(
                lteExpr.getLeftExpression(), lteExpr.getRightExpression(), BinaryExpr.Op.LT));

    register(
        EqualsTo.class,
        eqExpr ->
            binaryExpr(eqExpr.getLeftExpression(), eqExpr.getRightExpression(), BinaryExpr.Op.EQ));

    register(
        NotEqualsTo.class,
        neqExpr ->
            binaryExpr(
                neqExpr.getLeftExpression(), neqExpr.getRightExpression(), BinaryExpr.Op.NEQ));

    register(
        LikeExpression.class,
        likeExpr ->
            likeExpr.isNot()
                ? binaryExpr(
                    likeExpr.getLeftExpression(),
                    likeExpr.getRightExpression(),
                    BinaryExpr.Op.NOT_LIKE)
                : binaryExpr(
                    likeExpr.getLeftExpression(),
                    likeExpr.getRightExpression(),
                    BinaryExpr.Op.LIKE));

    register(
        IsNullExpression.class,
        isNullExpr ->
            new UnaryExpr(
                isNullExpr.isNot() ? UnaryExpr.Type.IS_NOT_NULL : UnaryExpr.Type.IS_NULL,
                toEzyExpr(isNullExpr.getLeftExpression())));

    register(
        Between.class,
        betweenExpr ->
            new BetweenExpr(
                toEzyExpr(betweenExpr.getLeftExpression()),
                toEzyExpr(betweenExpr.getBetweenExpressionStart()),
                toEzyExpr(betweenExpr.getBetweenExpressionEnd()),
                betweenExpr.isNot()));

    register(
        Column.class,
        sv -> {
          String columnName = sv.getFullyQualifiedName();
          if (Objects.equals(columnName, "true") || Objects.equals(columnName, "false")) {
            return new ConstExpr(Boolean.valueOf(columnName), ConstExpr.Type.BOOLEAN);
          } else {
            return new VariableExpr(columnName);
          }
        });

    register(Parenthesis.class, parenExpr -> new ParensExpr(toEzyExpr(parenExpr.getExpression())));

    register(
        InExpression.class,
        exp -> {
          EzyExpr left = toEzyExpr(exp.getLeftExpression());
          List<EzyExpr> right = toExprList(exp.getRightExpression());

          if (right == null) {
            throw new EzyParseException("Invalid IN expression");
          }

          InExpr inExpr = new InExpr(left, right);
          return exp.isNot() ? inExpr.notExpr() : inExpr;
        });
  }

  private boolean isNegative(SignedExpression se) {
    String sign = UnaryExpr.Type.MINUS.getSign();
    return se.getSign() == sign.charAt(0);
  }

  private BinaryExpr binaryExpr(
      Expression leftSqlExpr, Expression rightSqlExpr, BinaryExpr.Op plus) {
    EzyExpr left = toEzyExpr(leftSqlExpr);
    EzyExpr right = toEzyExpr(rightSqlExpr);
    return new BinaryExpr(left, right, plus);
  }

  public List<EzyExpr> toExprList(Expression expression) {

    if (expression instanceof ExpressionList) {
      ExpressionList<?> expressions = (ExpressionList<?>) expression;

      return expressions.stream().map(this::toEzyExpr).collect(Collectors.toList());
    }

    throw new EzyParseException("unsupported in expression detected: " + expression);
  }

  <T extends Expression> void register(Class<T> clazz, Function<T, EzyExpr> function) {

    handlers.put(clazz, function);
  }
}
