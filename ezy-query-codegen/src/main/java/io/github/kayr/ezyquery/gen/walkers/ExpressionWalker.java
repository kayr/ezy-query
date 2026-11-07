package io.github.kayr.ezyquery.gen.walkers;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;

public class ExpressionWalker extends ExpressionVisitorAdapter<Object> {

  @Override
  public <S> Object visit(JdbcNamedParameter parameter, S context) {
    ((WalkContext) context).namedParamVisited(parameter);
    return context;
  }

  @Override
  public <S> Object visit(Select select, S context) {
    // The select walker owns CTE traversal; the upstream expression adapter also visits them.
    return select.accept(getSelectVisitor(), context);
  }

  @Override
  public <S> Object visit(ParenthesedSelect select, S context) {
    // Keep wrapper clauses in the select walk rather than visiting pivots a second time.
    return visit((Select) select, context);
  }
}
