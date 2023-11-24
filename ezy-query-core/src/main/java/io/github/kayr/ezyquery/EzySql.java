package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.*;
import io.github.kayr.ezyquery.api.cnd.ICond;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.sql.ConnectionProvider;
import io.github.kayr.ezyquery.sql.Mappers;
import io.github.kayr.ezyquery.sql.Zql;
import io.github.kayr.ezyquery.util.CoercionUtil;
import io.github.kayr.ezyquery.util.ThrowingFunction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.NonNull;

/**
 * This class works with the EzySQL query objects as opposed to raw Sql Strings. All the db handling
 * is delegated to Zql with handles all the database problems.
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class EzySql {

  @lombok.Getter private final Zql zql;

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

  private <T> List<T> list(EzyQuery query, EzyCriteria params, Mappers.RowMapper<T> resultMapper) {
    QueryAndParams queryAndParams = query.query(params);
    return zql.rows(resultMapper, queryAndParams.getSql(), queryAndParams.getParams());
  }

  private <T> Optional<T> mayBeOne(
      EzyQuery query, EzyCriteria params, Mappers.RowMapper<T> resultMapper) {
    QueryAndParams queryAndParams = query.query(params);
    T one = zql.firstRow(resultMapper, queryAndParams.getSql(), queryAndParams.getParams());
    return Optional.ofNullable(one);
  }

  private <T> T one(EzyQuery query, EzyCriteria params, Mappers.RowMapper<T> resultMapper) {
    return mayBeOne(query, params, resultMapper)
        .orElseThrow(() -> new NoSuchElementException("No result found"));
  }

  private <T> Long count(EzyQuery query, EzyCriteria criteria) {
    Object one =
        zql.one(Object.class, query.query(criteria).getSql(), query.query(criteria).getParams());
    if (one == null) {
      return 0L;
    }
    return CoercionUtil.toLong(one);
  }

  private <R> R query(
      EzyQuery sql, EzyCriteria criteria, ThrowingFunction<ResultSet, R> rsConsumer) {
    QueryAndParams queryAndParams = sql.query(criteria);
    return zql.query(queryAndParams.getSql(), queryAndParams.getParams(), rsConsumer);
  }

  public <T> CriteriaBuilder<T> from(EzyQueryWithResult<T> q) {
    return new CriteriaBuilder<>(q, this);
  }

  public static class CriteriaBuilder<T> {
    private final EzyQuery query;
    private final EzySql ezySql;
    private final EzyCriteria criteria;
    private final Mappers.RowMapper<T> resultsMapper;

    public CriteriaBuilder(EzyQueryWithResult<T> query, EzySql ezySql) {
      this(query, ezySql, EzyCriteria.selectAll(), Mappers.toClass(query.resultClass()));
    }

    public CriteriaBuilder(
        EzyQuery query, EzySql ezySql, EzyCriteria criteria, Mappers.RowMapper<T> mapper) {
      this.query = query;
      this.ezySql = ezySql;
      this.criteria = criteria;
      this.resultsMapper = mapper;
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

    public CriteriaBuilder<T> setCriteria(CriteriaHolder param, ICond cond) {
      return withCriteria(criteria.setCriteria(param, cond));
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

    public <T2> CriteriaBuilder<T2> mapTo(Mappers.RowMapper<T2> mapper) {
      return new CriteriaBuilder<>(query, ezySql, criteria, mapper);
    }

    public List<T> list() {
      return ezySql.list(query, criteria, resultsMapper);
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
      return ezySql.mayBeOne(query, criteria, resultsMapper);
    }

    public T one() {
      return ezySql.one(query, criteria, resultsMapper);
    }

    public <R> R query(ThrowingFunction<ResultSet, R> rsConsumer) {
      return ezySql.query(query, criteria, rsConsumer);
    }

    public CriteriaBuilder<T> withCriteria(EzyCriteria criteria) {
      return new CriteriaBuilder<>(query, ezySql, criteria, resultsMapper);
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
