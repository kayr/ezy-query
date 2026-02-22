package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.RawValue;
import io.github.kayr.ezyquery.api.UnCaughtException;
import io.github.kayr.ezyquery.util.Elf;
import io.github.kayr.ezyquery.util.ThrowingFunction;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Zql {

  public interface Query {
    String getSql();

    List<Object> getParams();
  }

  private final ConnectionProvider connectionProvider;
  private final BinderRegistry binderRegistry;

  public Zql(ConnectionProvider connectionProvider) {
    this(connectionProvider, new BinderRegistry());
  }

  public Zql(ConnectionProvider connectionProvider, BinderRegistry binderRegistry) {
    this.connectionProvider = connectionProvider;
    this.binderRegistry = binderRegistry;
  }

  public Zql withBinder(Class<?> type, ParameterBinder binder) {
    return new Zql(connectionProvider, binderRegistry.withBinder(type, binder));
  }

  public <T> List<T> rows(Mappers.RowMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return Mappers.resultSetToList(resultSet.resultSet, Integer.MAX_VALUE, mapper);
    }
  }

  public <T> List<T> rows(Mappers.RowMapper<T> mapper, String sql, Object... params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return Mappers.resultSetToList(resultSet.resultSet, Integer.MAX_VALUE, mapper);
    }
  }

  public <T> List<T> rows(Mappers.RowMapper<T> mapper, Query query) {
    return rows(mapper, query.getSql(), query.getParams());
  }

  public <T> T oneRow(Mappers.RowMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources dbReSources = rows(sql, params)) {
      List<T> results = Mappers.resultSetToList(dbReSources.resultSet, 1, mapper);
      assertNoMoreRecords(dbReSources.resultSet);
      return results.isEmpty() ? null : results.get(0);
    }
  }

  public <T> T oneRow(Mappers.RowMapper<T> mapper, Query query) {
    return oneRow(mapper, query.getSql(), query.getParams());
  }

  private static void assertNoMoreRecords(ResultSet dbReSources) {
    if (JdbcUtils.next(dbReSources)) {
      throw new IllegalArgumentException("More than one row returned");
    }
  }

  public <R> R query(String sql, List<Object> params, ThrowingFunction<ResultSet, R> rsConsumer) {
    try (DbReSources dbReSources = rows(sql, params)) {
      return rsConsumer.apply(dbReSources.resultSet);
    } catch (Exception e) {
      throw new UnCaughtException("Error executing query", e);
    }
  }

  public <R> R query(Query query, ThrowingFunction<ResultSet, R> rsConsumer) {
    return query(query.getSql(), query.getParams(), rsConsumer);
  }

  private DbReSources rows(String sql, List<Object> params) {

    return rows(sql, params.toArray());
  }

  public <T> T one(Class<T> clazz, String sql, List<Object> params) {
    try (DbReSources r = rows(sql, params)) {

      ResultSet resultSet = r.resultSet;

      T result = null;
      if (JdbcUtils.next(resultSet)) {
        //noinspection unchecked
        result = (T) JdbcUtils.getObject(resultSet, 1);
      }

      assertNoMoreRecords(resultSet);

      return result;
    }
  }

  public <T> T one(Class<T> clazz, Query query) {
    return one(clazz, query.getSql(), query.getParams());
  }

  private DbReSources rows(String sql, Object... params) {
    Connection connection = connectionProvider.getConnectionUnChecked();
    PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql);
    setValues(statement, params);
    ResultSet resultSet = JdbcUtils.executeQuery(statement);
    return new DbReSources(connection, statement, resultSet, connectionProvider);
  }

  public Integer update(Query query) {
    return update(query.getSql(), query.getParams());
  }

  public Integer update(String sql, List<Object> params) {
    return update(sql, params.toArray());
  }

  public Integer update(String sql, Object... params) {
    Connection connection = connectionProvider.getConnectionUnChecked();
    try (PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql)) {
      setValues(statement, params);
      return JdbcUtils.executeUpdate(statement);
    } catch (SQLException e) {
      throw new UnCaughtException(e);
    } finally {
      closeConnection(connectionProvider, connection);
    }
  }

  private static void closeConnection(ConnectionProvider provider, Connection connection) {
    try {
      provider.closeConnection(connection);
    } catch (Exception e) {
      throw new UnCaughtException("Error closing connection", e);
    }
  }

  public void setValues(PreparedStatement preparedStatement, Object... values) {
    try {
      for (int i = 0; i < values.length; i++) {
        binderRegistry.bind(preparedStatement, i + 1, values[i]);
      }
    } catch (SQLException e) {
      throw new UnCaughtException("Error setting values on statement", e);
    }
  }

  public <T> T insertOne(String sql, List<Object> params) {
    return insertOne(sql, params.toArray());
  }

  @SuppressWarnings("unchecked")
  public <T> T insertOne(String sql, Object... params) {
    return (T) executeInsert(sql, params, JdbcUtils::getSingleGeneratedKey);
  }

  public <T> T insertOne(Query query) {
    return insertOne(query.getSql(), query.getParams());
  }

  public List<Object> insertMulti(String sql, List<Object> params) {
    return insertMulti(sql, params.toArray());
  }

  public List<Object> insertMulti(String sql, Object... params) {
    return executeInsert(sql, params, JdbcUtils::getAllGeneratedKeys);
  }

  public List<Object> insertMulti(Query query) {
    return insertMulti(query.getSql(), query.getParams());
  }

  private <T> T executeInsert(String sql, Object[] params, Function<ResultSet, T> keyProcessor) {
    Connection connection = connectionProvider.getConnectionUnChecked();
    try (PreparedStatement statement = JdbcUtils.preparedStatementWithKeys(connection, sql)) {
      setValues(statement, params);

      statement.executeUpdate();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        return keyProcessor.apply(generatedKeys);
      }
    } catch (Exception e) {
      throw new UnCaughtException("Error executing insert", e);
    } finally {
      closeConnection(connectionProvider, connection);
    }
  }

  public boolean execute(String sql, List<Object> params) {
    return execute(sql, params.toArray());
  }

  public boolean execute(String sql, Object... params) {
    Connection connection = connectionProvider.getConnectionUnChecked();
    try (PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql)) {
      setValues(statement, params);
      return statement.execute();
    } catch (Exception e) {
      throw new UnCaughtException("Error executing statement", e);
    } finally {
      closeConnection(connectionProvider, connection);
    }
  }

  public boolean execute(Query query) {
    return execute(query.getSql(), query.getParams());
  }

  public int[] batch(String sql, List<List<Object>> paramSets) {
    Connection connection = connectionProvider.getConnectionUnChecked();
    try (PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql)) {
      addBatches(statement, paramSets);
      return JdbcUtils.executeBatch(statement);
    } catch (SQLException e) {
      throw new UnCaughtException("Error executing batch", e);
    } finally {
      closeConnection(connectionProvider, connection);
    }
  }

  public int[] batch(List<Query> queries) {
    if (queries.isEmpty()) {
      return new int[0];
    }
    return batch(queries.get(0).getSql(), toParamSets(queries));
  }

  public List<Object> batchInsert(String sql, List<List<Object>> paramSets) {
    Connection connection = connectionProvider.getConnectionUnChecked();
    try (PreparedStatement statement = JdbcUtils.preparedStatementWithKeys(connection, sql)) {
      addBatches(statement, paramSets);
      JdbcUtils.executeBatch(statement);
      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        return JdbcUtils.getAllGeneratedKeys(generatedKeys);
      }
    } catch (Exception e) {
      throw new UnCaughtException("Error executing batch insert", e);
    } finally {
      closeConnection(connectionProvider, connection);
    }
  }

  public List<Object> batchInsert(List<Query> queries) {
    if (queries.isEmpty()) {
      return Collections.emptyList();
    }
    return batchInsert(queries.get(0).getSql(), toParamSets(queries));
  }

  private void addBatches(PreparedStatement statement, List<List<Object>> paramSets)
      throws SQLException {
    for (List<Object> params : paramSets) {
      setValues(statement, params.toArray());
      statement.addBatch();
    }
  }

  private static List<List<Object>> toParamSets(List<Query> queries) {
    List<List<Object>> paramSets = new ArrayList<>();
    for (Query query : queries) {
      paramSets.add(query.getParams());
    }
    return paramSets;
  }

  public static RawValue raw(Object value) {
    return RawValue.of(value);
  }

  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  private static class DbReSources implements AutoCloseable {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private ConnectionProvider connectionProvider;

    @Override
    public void close() {
      Elf.closeQuietly(resultSet);
      Elf.closeQuietly(statement);
      closeConnection(connectionProvider, connection);
    }
  }
}
