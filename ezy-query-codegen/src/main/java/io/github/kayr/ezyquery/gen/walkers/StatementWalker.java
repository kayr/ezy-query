package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzyStatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.Select;

public class StatementWalker extends EzyStatementVisitorAdapter {

  @Override
  public <S> Object visit(Select select, S context) {
    WalkContext wc = (WalkContext) context;
    select.accept(wc.getSelectWalker(), context);
    return context;
  }
}
