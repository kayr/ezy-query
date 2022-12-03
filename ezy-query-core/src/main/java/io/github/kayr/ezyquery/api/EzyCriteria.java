package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.api.cnd.ICond;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@lombok.Getter
public class EzyCriteria {

  private final List<String> columns = new ArrayList<>();
  private final List<ICond> conditions = new ArrayList<>();
  private final List<String> conditionExpressions = new ArrayList<>();

  private Integer offset = 0;
  private Integer limit = 50;

  private boolean useOr = false;
  private boolean count = false;

  // region Static methods

  /** Convenience method just to better communicate the intention */
  public static EzyCriteria selectAll() {
    return create();
  }

  public static EzyCriteria select(String... columns) {
    EzyCriteria p = create();
    p.addSelect(columns);
    return p;
  }

  private static EzyCriteria create() {
    return new EzyCriteria();
  }

  public static EzyCriteria select(Field<?>... columns) {
    EzyCriteria p = create();
    for (Field<?> f : columns) {
      p.addSelect(f.getAlias());
    }
    return p;
  }

  public static EzyCriteria selectCount() {
    EzyCriteria p = new EzyCriteria();
    p.count = true;
    return p;
  }
  // endregion

  // region Side effects
  public EzyCriteria addSelect(String... columns) {
    EzyCriteria copy = copy();
    copy.columns.addAll(Arrays.asList(columns));
    return copy;
  }

  public EzyCriteria where(ICond... conds) {
    EzyCriteria copy = copy();
    copy.conditions.addAll(Arrays.asList(conds));
    return copy;
  }

  public EzyCriteria where(String expr) {
    EzyCriteria copy = copy();
    copy.conditionExpressions.add(expr);
    return copy;
  }

  public EzyCriteria offset(@lombok.NonNull Integer offset) {
    EzyCriteria copy = copy();
    copy.offset = offset;
    return copy;
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit) {
    EzyCriteria copy = copy();
    copy.limit = limit;
    return copy;
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit, @lombok.NonNull Integer offset) {
    EzyCriteria copy = copy();
    copy.limit(limit);
    copy.offset(offset);
    return copy;
  }

  public EzyCriteria useOr() {
    EzyCriteria copy = copy();
    copy.useOr = true;
    return copy;
  }

  public EzyCriteria useAnd() {
    EzyCriteria copy = copy();
    copy.useOr = false;
    return copy;
  }

  public EzyCriteria count() {
    EzyCriteria copy = copy();
    copy.count = true;
    return copy;
  }

  // endregion

  // region Read only
  public boolean isUseOr() {
    return useOr;
  }

  public boolean isCount() {
    return count;
  }
  // endregion

  public EzyCriteria copy() {
    EzyCriteria copy = new EzyCriteria();
    copy.columns.addAll(this.columns);
    copy.conditions.addAll(this.conditions);
    copy.conditionExpressions.addAll(this.conditionExpressions);
    copy.offset = this.offset;
    copy.limit = this.limit;
    copy.useOr = this.useOr;
    copy.count = this.count;
    return copy;
  }
}
