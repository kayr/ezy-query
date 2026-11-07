package io.github.kayr.ezyquery.gen.walkers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

@lombok.NoArgsConstructor(staticName = "create")
public class WalkContext {

  public static final String EZY_MARKER = "_ezy_";
  // region The Visitor Walkers
  @lombok.Getter private final SelectWalker selectWalker = new SelectWalker();
  @lombok.Getter private final StatementWalker statementWalker = new StatementWalker();

  @lombok.Getter
  private final FromWalker fromWalker = (FromWalker) selectWalker.getFromItemVisitor();

  @lombok.Getter
  private final ExpressionWalker expressionWalker =
      (ExpressionWalker) selectWalker.getExpressionVisitor();
  // endregion

  // region Fields
  @lombok.Getter private final Map<String, SelectExpr> dynamicSelects = new HashMap<>();
  private final Deque<SelectExpr> selectStack = new ArrayDeque<>();
  // endregion

  void startedSelect(Select select) {
    SelectExpr currentSelect = selectStack.peek();
    if (currentSelect != null
        && (currentSelect.select == select || isChildOfCurrentParenSelect(select))) {
      // Wrapper/body delegation shares a logical owner but still has balanced visitor calls.
      selectStack.push(currentSelect);
      return;
    }

    SelectExpr owner = new SelectExpr(select, currentSelect);
    if (currentSelect != null) {
      currentSelect.addChild(owner);
    }
    selectStack.push(owner);
  }

  void endedSelect(Select select) {
    SelectExpr currentSelect = selectStack.peek();
    if (currentSelect == null
        || (currentSelect.select != select && !isChildOfCurrentParenSelect(select))) {
      throw new IllegalStateException("Selects do not match");
    }
    selectStack.pop();
  }

  /**
   * We do not need to validate the current state of the context as this will only be called from
   * within the select walker. In future if we need to use this outside of the select walker, we
   * will need to validate the context.
   */
  void namedParamVisited(JdbcNamedParameter jdbcNamedParameter) {
    SelectExpr currentSelect = selectStack.peek();
    if (currentSelect == null) {
      throw new IllegalStateException("No Select Found for named param: " + jdbcNamedParameter);
    }

    String name = jdbcNamedParameter.getName();
    if (!name.startsWith(EZY_MARKER)) {
      return;
    }

    Select owner = currentSelect.select;
    while (owner instanceof ParenthesedSelect) {
      owner = ((ParenthesedSelect) owner).getSelect();
    }
    if (!(owner instanceof PlainSelect)) {
      throw new IllegalArgumentException(
          "Cannot determine criteria fields for marker "
              + name
              + " in "
              + owner.getClass().getSimpleName());
    }
    dynamicSelects.put(name, currentSelect);
  }

  private boolean isChildOfCurrentParenSelect(Select select) {
    SelectExpr currentSelect = selectStack.peek();
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
