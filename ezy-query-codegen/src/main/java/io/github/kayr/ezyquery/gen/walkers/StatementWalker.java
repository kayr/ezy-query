package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzyStatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.Select;

@lombok.AllArgsConstructor(staticName = "of")
public class StatementWalker extends EzyStatementVisitorAdapter {

  private WalkContext context;

  @Override
  public <S> Void visit(Select select, S context) {
    select.accept(this.context.getSelectWalker(), null);
    return null;
  }
}
