package io.github.kayr.ezyquery.gen.walkers;

import java.util.Map;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

@lombok.AllArgsConstructor(staticName = "of")
public class DynamicQueriesFinder {

  private Statement statement;

  public Map<String, WalkContext.SelectExpr> lookup() {
    WalkContext context = WalkContext.create();
    statement.accept(context.getStatementWalker());
    return context.getDynamicSelects();
  }

  public static Map<String, WalkContext.SelectExpr> lookup(Statement statement) {
    return DynamicQueriesFinder.of(statement).lookup();
  }

  public static Map<String, WalkContext.SelectExpr> lookup(String statement)
      throws JSQLParserException {
    return DynamicQueriesFinder.lookup(CCJSqlParserUtil.parse(statement));
  }
}
