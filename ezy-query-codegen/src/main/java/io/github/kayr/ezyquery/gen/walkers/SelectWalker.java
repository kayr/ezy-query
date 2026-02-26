package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzySelectVisitorAdapter;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

@lombok.AllArgsConstructor(staticName = "of")
public class SelectWalker extends EzySelectVisitorAdapter {

  WalkContext context;

  @Override
  public <S> Void visit(ParenthesedSelect parenthesedSelect, S context) {
    this.context.startedSelect(parenthesedSelect);
    parenthesedSelect.getSelect().accept(this, null);
    this.context.endedSelect(parenthesedSelect);
    return null;
  }

  @Override
  public <S> Void visit(PlainSelect plainSelect, S context) {
    this.context.startedSelect(plainSelect);

    // visit the with items
    List<WithItem<?>> withItems = plainSelect.getWithItemsList();
    if (withItems != null) {
      for (WithItem<?> withItem : withItems) {
        withItem.getSelect().accept(this, null);
      }
    }

    // visit from clause
    FromItem fromItem = plainSelect.getFromItem();
    if (fromItem != null) {
      fromItem.accept(this.context.getFromWalker());
    }

    // visit joins
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      for (Join j : joins) {
        j.getRightItem().accept(this.context.getFromWalker());
      }
    }

    // visit where clause
    Expression where = plainSelect.getWhere();
    if (where != null) {
      where.accept(this.context.getExpressionWalker());
    }

    this.context.endedSelect(plainSelect);
    return null;
  }

  @Override
  public <S> Void visit(SetOperationList setOpList, S context) {
    return null;
  }

  @Override
  public <S> Void visit(WithItem<?> withItem, S context) {
    withItem.getSelect().accept(this, null);
    return null;
  }

  @Override
  public <S> Void visit(Values aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(LateralSubSelect lateralSubSelect, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TableStatement tableStatement, S context) {
    return null;
  }

  @Override
  public <S> Void visit(FromQuery fromQuery, S context) {
    return null;
  }
}
