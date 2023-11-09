package io.github.kayr.ezyquery.gen.walkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;

@lombok.NoArgsConstructor(staticName = "create")
public class WalkContext {

  public static final String EZY_MARKER = "_ezy_";
  // region The Visitor Walkers
  @lombok.Getter private final SelectWalker selectWalker = SelectWalker.of(this);
  @lombok.Getter private final StatementWalker statementWalker = StatementWalker.of(this);
  @lombok.Getter private final FromWalker fromWalker = FromWalker.of(this);
  @lombok.Getter private final ExpressionWalker expressionWalker = ExpressionWalker.of(this);
  // endregion

  // region Fields
  @lombok.Getter private final Map<String, SelectExpr> dynamicSelects = new HashMap<>();
  private SelectExpr currentSelect;
  private int duplicateCount = 0;
  // endregion

  void startedSelect(Select select) {

    if (currentSelect != null && currentSelect.select == select) {
      duplicateCount++;
      return;
    }

    if (isChildOfCurrentParenSelect(select)) {
      return;
    }

    SelectExpr s = new SelectExpr(select, currentSelect);

    if (currentSelect != null) {
      currentSelect.addChild(s);
    }
    currentSelect = s;
  }

  void endedSelect(Select select) {

    if (currentSelect.getSelect() == select && duplicateCount > 0) {
      duplicateCount--;
      return;
    }

    if (isChildOfCurrentParenSelect(select)) {
      return;
    }

    if (currentSelect.select != select) {
      throw new IllegalStateException("Selects do not match");
    }
    currentSelect = currentSelect.parent;
  }

  /**
   * We do not need to validate the current state of the context as this will only be called from
   * within the select walker. In future if we need to use this outside of the select walker, we
   * will need to validate the context.
   */
  void namedParamVisited(JdbcNamedParameter jdbcNamedParameter) {
    if (currentSelect == null) {
      throw new IllegalStateException("No Select Found for named param: " + jdbcNamedParameter);
    }

    String name = jdbcNamedParameter.getName();
    if (!name.startsWith(EZY_MARKER)) {
      return;
    }

    String queryName = name.substring(EZY_MARKER.length());
    dynamicSelects.put(queryName, currentSelect);
  }

  private boolean isChildOfCurrentParenSelect(Select select) {
    return currentSelect != null
        && currentSelect.getSelect() instanceof ParenthesedSelect
        && ((ParenthesedSelect) currentSelect.getSelect()).getSelect() == select;
  }

  @lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @lombok.Getter
  public static class SelectExpr {
    private final Select select;
    private final SelectExpr parent;
    private final List<SelectExpr> subSelects = new ArrayList<>();

    public void addChild(SelectExpr expr) {
      subSelects.add(expr);
    }

    public String toString() {
      return getExpr();
    }

    String getExpr() {
      return select.toString();
    }
  }
}
