package io.github.kayr.ezyquery.api;

import static java.util.Collections.singletonList;

import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.util.Elf;
import io.github.kayr.ezyquery.util.MapUtil;
import java.util.*;
import lombok.AccessLevel;
import lombok.Builder;

@lombok.Getter
@lombok.Builder(toBuilder = true, access = AccessLevel.PRIVATE)
public class EzyCriteria {

  public static final String SORT_BY_MAP_PARAM = "_sortby";
  public static final String OFFSET_PARAM = "_offset";
  public static final String LIMIT_PARAM = "_limit";

  @Builder.Default private List<String> columns = new ArrayList<>();
  @Builder.Default private List<ICond> conditions = new ArrayList<>();
  @Builder.Default private List<NamedParamValue> paramValues = new ArrayList<>();

  @Builder.Default private List<Sort> sorts = new ArrayList<>();

  @Builder.Default private Long offset = 0L;
  @Builder.Default private Integer limit = 50;

  @Builder.Default private boolean count = false;

  // region Static methods

  /** Convenience method just to better communicate the intention */
  public static EzyCriteria selectAll() {
    return builder().build();
  }

  public static EzyCriteria select(String... columns) {
    return builder().columns(Arrays.asList(columns)).build();
  }

  public static EzyCriteria selectCount() {
    return builder().count(true).build();
  }

  public static EzyCriteria fromMap(Map<String, ?> criteria) {
    HashMap<String, List<?>> map = new HashMap<>();
    for (Map.Entry<String, ?> entry : criteria.entrySet()) {
      map.put(entry.getKey(), singletonList(entry.getValue()));
    }
    return fromMvMap(map);
  }

  public static EzyCriteria fromMvMap(Map<String, List<?>> criteria) {

    List<Sort> sortByStr = extractSort(criteria);
    Integer limit = extractLimit(criteria);
    Long offset = extractOffset(criteria);
    List<ICond> conditions = toConds(criteria);

    return EzyCriteria.builder()
        .conditions(conditions)
        .limit(limit)
        .offset(offset)
        .sorts(sortByStr)
        .build();
  }

  private static List<ICond> toConds(Map<String, List<?>> criteria) {
    Map<String, List<?>> condOnlyMap =
        Elf.remove(criteria, SORT_BY_MAP_PARAM, OFFSET_PARAM, LIMIT_PARAM);
    return singletonList(Cnd.fromMvMap(condOnlyMap));
  }

  private static Integer extractLimit(Map<String, List<?>> criteria) {
    return Optional.ofNullable(MapUtil.firstValue(criteria, LIMIT_PARAM))
        .map(Object::toString)
        .map(Integer::parseInt)
        .orElse(50);
  }

  private static Long extractOffset(Map<String, List<?>> criteria) {
    return Optional.ofNullable(MapUtil.firstValue(criteria, OFFSET_PARAM))
        .map(Object::toString)
        .map(Long::parseLong)
        .orElse(0L);
  }

  private static List<Sort> extractSort(Map<String, List<?>> criteria) {
    Object firstValue = MapUtil.firstValue(criteria, SORT_BY_MAP_PARAM);
    return Elf.safeMap(Elf.toString(firstValue), Sort::parse);
  }

  // endregion

  // region Builder methods
  public EzyCriteria addSelect(String... columns) {
    return toBuilder().columns(Elf.addAll(this.columns, columns)).build();
  }

  public EzyCriteria where(ICond... conds) {
    return toBuilder().conditions(Elf.addAll(this.conditions, conds)).build();
  }

  public EzyCriteria offset(@lombok.NonNull Long offset) {
    return toBuilder().offset(offset).build();
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit) {
    return toBuilder().limit(limit).build();
  }

  public EzyCriteria limit(@lombok.NonNull Integer limit, @lombok.NonNull Long offset) {
    return toBuilder().limit(limit).offset(offset).build();
  }

  public EzyCriteria count() {
    return toBuilder().count(true).build();
  }

  public EzyCriteria orderBy(Sort... sort) {
    return toBuilder().sorts(Elf.addAll(this.sorts, sort)).build();
  }

  public EzyCriteria orderBy(String... sort) {
    List<Sort> sortList = new ArrayList<>();
    for (String s : sort) {
      List<Sort> parse = Sort.parse(s);
      sortList.addAll(parse);
    }
    return toBuilder().sorts(Elf.combine(this.sorts, sortList)).build();
  }

  public EzyCriteria setParam(NamedParam namedParam, Object value) {
    return toBuilder()
        .paramValues(Elf.addAll(this.paramValues, new NamedParamValue(namedParam, value)))
        .build();
  }

  public EzyCriteria setCriteria(CriteriaHolder criteria, ICond cond) {
    return setParam(criteria.getName(), cond);
  }

  // endregion

  // region Read only

  public List<String> getColumns() {
    return Collections.unmodifiableList(columns);
  }

  public List<ICond> getConditions() {
    return Collections.unmodifiableList(conditions);
  }

  public List<Sort> getSorts() {
    return Collections.unmodifiableList(sorts);
  }

  // endregion

}
