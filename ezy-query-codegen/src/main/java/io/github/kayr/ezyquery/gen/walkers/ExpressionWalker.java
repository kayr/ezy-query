package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzyExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcNamedParameter;

public class ExpressionWalker extends EzyExpressionVisitorAdapter {

  @Override
  public <S> Object visit(JdbcNamedParameter jdbcNamedParameter, S context) {
    WalkContext wc = (WalkContext) context;
    wc.namedParamVisited(jdbcNamedParameter);
    return context;
  }
}
