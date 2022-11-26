package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.api.cnd.ICond;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@lombok.Getter
public class FilterParams {

  private final List<String> columns = new ArrayList<>();
  private final List<ICond> conditions = new ArrayList<>();
  private final List<String> conditionExpressions = new ArrayList<>();

  private Integer offset = 0;
  private Integer limit = 50;

  private boolean useOr = false;
  private boolean count = false;

  /** Convenience method just to better communicate the intention */
  public static FilterParams selectAll() {
    return create();
  }

  public static FilterParams select(String... columns) {
    FilterParams p = create();
    p.addSelect(columns);
    return p;
  }

  private static FilterParams create() {
    FilterParams p = create();
    return p;
  }

  public static FilterParams select(Field<?>... columns) {
    FilterParams p = create();
    for (Field<?> f : columns) {
      p.addSelect(f.getAlias());
    }
    return p;
  }

  public static FilterParams selectCount() {
    FilterParams p = new FilterParams();
    p.count = true;
    return p;
  }

  public FilterParams addSelect(String... columns) {
    this.columns.addAll(Arrays.asList(columns));
    return this;
  }

  public FilterParams where(ICond... conds) {
    this.conditions.addAll(Arrays.asList(conds));
    return this;
  }

  public FilterParams where(String expr) {
    this.conditionExpressions.add(expr);
    return this;
  }

  public FilterParams offset(@lombok.NonNull Integer offset) {
    this.offset = offset;
    return this;
  }

  public FilterParams limit(@lombok.NonNull Integer limit) {
    this.limit = limit;
    return this;
  }

  public FilterParams limit(@lombok.NonNull Integer limit, @lombok.NonNull Integer offset) {
    limit(limit);
    offset(offset);
    return this;
  }

  public FilterParams useOr() {
    this.useOr = true;
    return this;
  }

  public FilterParams useAnd() {
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
