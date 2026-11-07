package io.github.kayr.ezyquery.gen.walkers;

import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

public class SelectWalker extends SelectVisitorAdapter<Object> {

  public SelectWalker() {
    this(new ExpressionWalker());
  }

  private SelectWalker(ExpressionWalker expressions) {
    super(expressions, new FromWalker(expressions));
    expressions.setSelectVisitor(this);
  }

  @Override
  public <S> Object visit(PlainSelect select, S context) {
    WalkContext wc = (WalkContext) context;
    wc.startedSelect(select);
    try {
      super.visit(select, context);
      // JSqlParser 5.4's plain-select adapter omits LIMIT.
      getExpressionVisitor().visitLimit(select.getLimit(), context);
      return context;
    } finally {
      wc.endedSelect(select);
    }
  }

  @Override
  public <S> Object visit(ParenthesedSelect select, S context) {
    return visitParenthesized(select, select, context);
  }

  <S> Object visitDerivedTable(ParenthesedSelect select, S context) {
    // Derived-table criteria have historically exposed the underlying SELECT as their owner.
    return visitParenthesized(select, select.getSelect(), context);
  }

  private <S> Object visitParenthesized(ParenthesedSelect select, Select owner, S context) {
    WalkContext wc = (WalkContext) context;
    wc.startedSelect(owner);
    try {
      visitWithItems(select.getWithItemsList(), context);
      select.getSelect().accept(this, context);
      visitTail(select, context);
      return context;
    } finally {
      wc.endedSelect(owner);
    }
  }

  @Override
  public <S> Object visit(LateralSubSelect select, S context) {
    return visit((ParenthesedSelect) select, context);
  }

  @Override
  public <S> Object visit(SetOperationList select, S context) {
    WalkContext wc = (WalkContext) context;
    wc.startedSelect(select);
    try {
      visitWithItems(select.getWithItemsList(), context);
      super.visit(select, context);
      visitTail(select, context);
      return context;
    } finally {
      wc.endedSelect(select);
    }
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

  private <S> void visitTail(Select select, S context) {
    getExpressionVisitor().visitOrderBy(select.getOrderByElements(), context);
    getExpressionVisitor().visitLimit(select.getLimit(), context);
    if (select.getOffset() != null) {
      getExpressionVisitor().visitExpression(select.getOffset().getOffset(), context);
    }
    if (select.getFetch() != null) {
      getExpressionVisitor().visitExpression(select.getFetch().getExpression(), context);
    }
  }
}
