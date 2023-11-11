package io.github.kayr.ezyquery.gen.walkers;

import io.github.kayr.ezyquery.gen.walkers.adapters.EzyExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcNamedParameter;

@lombok.AllArgsConstructor(staticName = "of")
public class ExpressionWalker extends EzyExpressionVisitorAdapter {

  private final WalkContext context;

  @Override
  public void visit(JdbcNamedParameter jdbcNamedParameter) {
    context.namedParamVisited(jdbcNamedParameter);
  }
}
