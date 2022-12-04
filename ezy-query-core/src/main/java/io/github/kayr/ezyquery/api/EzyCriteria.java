package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.api.cnd.ICond;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@lombok.Getter
public class EzyCriteria {

  private final List<String> columns = new ArrayList<>();
  private final List<ICond> conditions = new ArrayList<>();
  private final List<String> conditionExpressions = new ArrayList<>();

  private final List<Sort> sorts = new ArrayList<>();

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
    return p.addSelect(columns);
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

  // region Side effects but these actually create copies of the object for immutability
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
    return copy().limit(limit).offset(offset);
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

  public EzyCriteria orderBy(Sort... sort) {
    EzyCriteria copy = copy();
    copy.sorts.addAll(Arrays.asList(sort));
    return copy;
  }

  public EzyCriteria orderBy(String... sort) {
    EzyCriteria copy = copy();
    List<Sort> collect =
        Arrays.stream(sort).map(s -> Sort.by(s, Sort.DIR.ASC)).collect(Collectors.toList());
    copy.sorts.addAll(collect);
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

  public List<String> getColumns() {
    return Collections.unmodifiableList(columns);
  }

  public List<ICond> getConditions() {
    return Collections.unmodifiableList(conditions);
  }

  public List<String> getConditionExpressions() {
    return Collections.unmodifiableList(conditionExpressions);
  }

  public List<Sort> getSorts() {
    return Collections.unmodifiableList(sorts);
  }

  // endregion

  public EzyCriteria copy() {
    EzyCriteria copy = new EzyCriteria();
    copy.columns.addAll(this.columns);
    copy.conditions.addAll(this.conditions);
    copy.conditionExpressions.addAll(this.conditionExpressions);
    copy.sorts.addAll(this.sorts);
    copy.offset = this.offset;
    copy.limit = this.limit;
    copy.useOr = this.useOr;
    copy.count = this.count;
    return copy;
  }
}
