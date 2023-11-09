package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzySelectVisitor;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

@lombok.AllArgsConstructor(staticName = "of")
public class SelectWalker extends EzySelectVisitor {

  WalkContext context;

  @Override
  public void visit(ParenthesedSelect parenthesedSelect) {
    context.startedSelect(parenthesedSelect);
    parenthesedSelect.getSelect().accept(this);
    context.endedSelect(parenthesedSelect);
  }

  @Override
  public void visit(PlainSelect plainSelect) {
    context.startedSelect(plainSelect);

    // visit where clause
    Expression where = plainSelect.getWhere();
    if (where != null) {
      where.accept(context.getExpressionWalker());
    }

    // visit from clause
    FromItem fromItem = plainSelect.getFromItem();
    if (fromItem != null) {
      fromItem.accept(context.getFromWalker());
    }

    // visit joins
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      for (Join j : joins) {
        j.getRightItem().accept(context.getFromWalker());
      }
    }

    context.endedSelect(plainSelect);
  }

  @Override
  public void visit(SetOperationList setOpList) {
    // continue
  }

  @Override
  public void visit(WithItem withItem) {
    // continue
  }

  @Override
  public void visit(Values aThis) {
    // continue
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    // continue
  }
}
