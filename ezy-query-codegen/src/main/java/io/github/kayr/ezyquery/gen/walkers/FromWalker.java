package io.github.kayr.ezyquery.gen.walkers;

import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

public class FromWalker extends FromItemVisitorAdapter<Object> {

  private final ExpressionWalker expressions;

  public FromWalker(ExpressionWalker expressions) {
    super(null, expressions);
    this.expressions = expressions;
  }

  @Override
  public <S> Object visit(ParenthesedSelect select, S context) {
    return ((WalkContext) context).getSelectWalker().visitDerivedTable(select, context);
  }

  @Override
  public <S> Object visit(LateralSubSelect select, S context) {
    return visit((ParenthesedSelect) select, context);
  }

  @Override
  public <S> Object visit(PlainSelect select, S context) {
    return select.accept(expressions.getSelectVisitor(), context);
  }

  @Override
  public <S> Object visit(SetOperationList select, S context) {
    return select.accept(expressions.getSelectVisitor(), context);
  }

  @Override
  public <S> Object visit(ParenthesedFromItem from, S context) {
    visitFromItem(from.getFromItem(), context);
    visitJoins(from.getJoins(), context);
    return context;
  }
  // These query forms remain outside dynamic criteria discovery.
  @Override
  public <S> Object visit(Values select, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TableStatement select, S context) {
    return context;
  }

  @Override
  public <S> Object visit(FromQuery select, S context) {
    return context;
  }

  @Override
  public <S> Object visit(PivotQuery select, S context) {
    return context;
  }
}
