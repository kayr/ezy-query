package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzySelectVisitorAdapter;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

public class SelectWalker extends EzySelectVisitorAdapter {

  @Override
  public <S> Object visit(ParenthesedSelect parenthesedSelect, S context) {
    WalkContext wc = (WalkContext) context;
    wc.startedSelect(parenthesedSelect);
    parenthesedSelect.getSelect().accept(this, context);
    wc.endedSelect(parenthesedSelect);
    return context;
  }

  @Override
  public <S> Object visit(PlainSelect plainSelect, S context) {
    WalkContext wc = (WalkContext) context;
    wc.startedSelect(plainSelect);

    // visit the with items
    List<WithItem<?>> withItems = plainSelect.getWithItemsList();
    if (withItems != null) {
      for (WithItem<?> withItem : withItems) {
        withItem.getSelect().accept(this, context);
      }
    }

    // visit from clause
    FromItem fromItem = plainSelect.getFromItem();
    if (fromItem != null) {
      fromItem.accept(wc.getFromWalker(), context);
    }

    // visit joins
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      for (Join j : joins) {
        j.getRightItem().accept(wc.getFromWalker(), context);
      }
    }

    // visit where clause
    Expression where = plainSelect.getWhere();
    if (where != null) {
      where.accept(wc.getExpressionWalker(), context);
    }

    wc.endedSelect(plainSelect);
    return context;
  }

  @Override
  public <S> Object visit(SetOperationList setOpList, S context) {
    return context;
  }

  @Override
  public <S> Object visit(WithItem<?> withItem, S context) {
    withItem.getSelect().accept(this, context);
    return context;
  }

  @Override
  public <S> Object visit(Values aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(LateralSubSelect lateralSubSelect, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TableStatement tableStatement, S context) {
    return context;
  }

  @Override
  public <S> Object visit(FromQuery fromQuery, S context) {
    return context;
  }
}
