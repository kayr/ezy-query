package io.github.kayr.ezyquery.gen.walkers;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

@lombok.AllArgsConstructor(staticName = "of")
public class FromWalker implements FromItemVisitor {

  private WalkContext context;

  @Override
  public void visit(Table tableName) {
    // continue
  }

  @Override
  public void visit(ParenthesedSelect selectBody) {
    Select select = selectBody.getSelect();
    select.accept(context.getSelectWalker());
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    // continue

  }

  @Override
  public void visit(TableFunction tableFunction) {
    // continue
  }

  @Override
  public void visit(ParenthesedFromItem aThis) {
    // continue
  }
}
