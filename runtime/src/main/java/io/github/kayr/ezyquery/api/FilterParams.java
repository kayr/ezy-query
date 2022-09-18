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

  public static FilterParams neww() {
    return new FilterParams();
  }

  public FilterParams select(String... columns) {
    this.columns.addAll(Arrays.asList(columns));
    return this;
  }

  public FilterParams selectCount() {
    this.count = true;
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
