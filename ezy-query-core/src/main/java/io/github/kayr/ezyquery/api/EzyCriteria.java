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

  public EzyCriteria addSelect(String... columns) {
    this.columns.addAll(Arrays.asList(columns));
    return this;
  }

  public EzyCriteria where(ICond... conds) {
    this.conditions.addAll(Arrays.asList(conds));
    return this;
  }

  public EzyCriteria where(String expr) {
    this.conditionExpressions.add(expr);
    return this;
  }

  public EzyCriteria offset(@lombok.NonNull Integer offset) {
    this.offset = offset;
    return this;
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit) {
    this.limit = limit;
    return this;
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit, @lombok.NonNull Integer offset) {
    limit(limit);
    offset(offset);
    return this;
  }

  public EzyCriteria useOr() {
    this.useOr = true;
    return this;
  }

  public EzyCriteria useAnd() {
    this.useOr = false;
    return this;
  }

  public boolean isUseOr() {
    return useOr;
  }

  public boolean isCount() {
    return count;
  }
}
