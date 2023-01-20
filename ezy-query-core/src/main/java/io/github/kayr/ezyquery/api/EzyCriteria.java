package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.util.Elf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@lombok.Getter
@lombok.Builder(toBuilder = true, access = AccessLevel.PRIVATE)
public class EzyCriteria {

  @Builder.Default private List<String> columns = new ArrayList<>();
  @Builder.Default private List<ICond> conditions = new ArrayList<>();
  @Builder.Default private List<String> conditionExpressions = new ArrayList<>();

  @Builder.Default private List<Sort> sorts = new ArrayList<>();

  @Builder.Default private Integer offset = 0;
  @Builder.Default private Integer limit = 50;

  @Builder.Default private boolean useOr = false;
  @Builder.Default private boolean count = false;

  // region Static methods

  /** Convenience method just to better communicate the intention */
  public static EzyCriteria selectAll() {
    return builder().build();
  }

  public static EzyCriteria select(String... columns) {
    return builder().columns(Arrays.asList(columns)).build();
  }

  public static EzyCriteria select(Field<?>... columns) {
    List<String> list = new ArrayList<>();
    for (Field<?> column : columns) {
      String alias = column.getAlias();
      list.add(alias);
    }
    return builder().columns(list).build();
  }

  public static EzyCriteria selectCount() {
    return builder().count(true).build();
  }
  // endregion

  // region Side effects but these actually create copies of the object for immutability
  public EzyCriteria addSelect(String... columns) {
    return toBuilder().columns(Elf.addAll(this.columns, columns)).build();
  }

  public EzyCriteria where(ICond... conds) {
    return toBuilder().conditions(Elf.addAll(this.conditions, conds)).build();
  }

  public EzyCriteria where(String expr) {
    return toBuilder().conditionExpressions(Elf.addAll(this.conditionExpressions, expr)).build();
  }

  public EzyCriteria offset(@lombok.NonNull Integer offset) {
    return toBuilder().offset(offset).build();
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit) {
    return toBuilder().limit(limit).build();
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit, @lombok.NonNull Integer offset) {
    return toBuilder().limit(limit).offset(offset).build();
  }

  public EzyCriteria useOr() {
    return toBuilder().useOr(true).build();
  }

  public EzyCriteria useAnd() {
    return toBuilder().useOr(false).build();
  }

  public EzyCriteria count() {
    return toBuilder().count(true).build();
  }

  public EzyCriteria orderBy(Sort... sort) {
    return toBuilder().sorts(Elf.addAll(this.sorts, sort)).build();
  }

  public EzyCriteria orderBy(String... sort) {
    List<Sort> sorts = new ArrayList<>();
    for (String s : sort) {
      sorts.add(Sort.by(s, Sort.DIR.ASC));
    }
    return toBuilder().sorts(Elf.combine(this.sorts, sorts)).build();
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

}
