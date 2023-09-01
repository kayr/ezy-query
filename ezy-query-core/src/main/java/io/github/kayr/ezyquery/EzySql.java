package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.NamedParam;
import io.github.kayr.ezyquery.api.Sort;
import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.sql.ConnectionProvider;
import io.github.kayr.ezyquery.sql.ResultsMapper;
import io.github.kayr.ezyquery.sql.Zql;
import io.github.kayr.ezyquery.util.CoercionUtil;
import java.sql.Connection;
import java.util.*;
import javax.sql.DataSource;
import lombok.NonNull;

/**
 * This class works with the EzySQL query objects as opposed to raw Sql Strings. All the db handling
 * is delegated to Zql with handles all the database problems.
 */
public class EzySql {

  private Zql zql;

  private EzySql(Zql zql) {
    this.zql = zql;
  }

  public static EzySql withProvider(ConnectionProvider connectionProvider) {
    return withZql(new Zql(connectionProvider));
  }

  public static EzySql withZql(Zql zql) {
    return new EzySql(zql);
  }

  public static EzySql withDataSource(DataSource dataSource) {
    return withProvider(ConnectionProvider.of(dataSource));
  }

  public static EzySql withConnection(Connection connection) {
    return withProvider(ConnectionProvider.of(connection));
  }

  private <T> List<T> list(EzyQuery<T> query, EzyCriteria params) {
    QueryAndParams queryAndParams = query.query(params);
    return zql.rows(
        ResultsMapper.usingReflection(query.resultClass()),
        queryAndParams.getSql(),
        queryAndParams.getParams());
  }

  private <T> Optional<T> mayBeOne(EzyQuery<T> query, EzyCriteria params) {
    QueryAndParams queryAndParams = query.query(params);
    T one =
        zql.firstRow(
            ResultsMapper.usingReflection(query.resultClass()),
            queryAndParams.getSql(),
            queryAndParams.getParams());
    return Optional.ofNullable(one);
  }

  private <T> T one(EzyQuery<T> query, EzyCriteria params) {
    return mayBeOne(query, params).orElseThrow(() -> new NoSuchElementException("No result found"));
  }

  private <T> Long count(EzyQuery<T> query, EzyCriteria criteria) {
    Object one =
        zql.one(Object.class, query.query(criteria).getSql(), query.query(criteria).getParams());
    if (one == null) {
      return 0L;
    }
    return CoercionUtil.toLong(one);
  }

  public Zql getZql() {
    return zql;
  }

  public <T> CriteriaBuilder<T> from(EzyQuery<T> q) {
    return new CriteriaBuilder<>(q, this);
  }

  public static class CriteriaBuilder<T> {
    private final EzyQuery<T> query;
    private final EzySql ezySql;
    private final EzyCriteria criteria;

    public CriteriaBuilder(EzyQuery<T> query, EzySql ezySql) {
      this(query, ezySql, EzyCriteria.selectAll());
    }

    public CriteriaBuilder(EzyQuery<T> query, EzySql ezySql, EzyCriteria criteria) {
      this.query = query;
      this.ezySql = ezySql;
      this.criteria = criteria;
    }

    public CriteriaBuilder<T> select(Field<?> field, Field<?>... otherFields) {

      List<String> aliases = new ArrayList<>();
      aliases.add(field.getAlias());
      for (Field<?> otherField : otherFields) {
        aliases.add(otherField.getAlias());
      }
      return withCriteria(criteria.addSelect(aliases.toArray(new String[0])));
    }

    public CriteriaBuilder<T> where(ICond... conds) {
      return withCriteria(criteria.where(conds));
    }

    public CriteriaBuilder<T> setParam(NamedParam param, Object value) {
      return withCriteria(criteria.setParam(param, value));
    }

    public CriteriaBuilder<T> offset(@NonNull Integer offset) {
      return withCriteria(criteria.offset(offset));
    }

    public CriteriaBuilder<T> limit(@NonNull Integer limit) {
      return withCriteria(criteria.limit(limit));
    }

    public CriteriaBuilder<T> limit(@NonNull Integer limit, @NonNull Integer offset) {
      return withCriteria(criteria.limit(limit, offset));
    }

    public CriteriaBuilder<T> orderBy(String... orderBy) {
      return withCriteria(criteria.orderBy(orderBy));
    }

    public CriteriaBuilder<T> orderBy(Sort... sort) {
      return withCriteria(criteria.orderBy(sort));
    }

    public List<T> list() {
      return ezySql.list(query, criteria);
    }

    public Long count() {
      return ezySql.count(query, criteria.count());
    }

    public EzySql.Result<T> listAndCount() {
      List<T> list = list();
      Long count = count();
      return new EzySql.Result<>(count, list);
    }

    public Optional<T> mayBeOne() {
      return ezySql.mayBeOne(query, criteria);
    }

    public T one() {
      return ezySql.one(query, criteria);
    }

    public CriteriaBuilder<T> withCriteria(EzyCriteria criteria) {
      return new CriteriaBuilder<>(query, ezySql, criteria);
    }

    public QueryAndParams getQuery() {
      return query.query(criteria);
    }
  }

  @lombok.Getter
  @lombok.AllArgsConstructor
  public static class Result<T> {
    private final Long count;
    private final List<T> list;
  }
}
