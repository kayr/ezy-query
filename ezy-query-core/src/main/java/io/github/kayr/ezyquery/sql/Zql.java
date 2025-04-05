package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import io.github.kayr.ezyquery.util.Elf;
import io.github.kayr.ezyquery.util.ThrowingFunction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;

public class Zql {

  public interface Query {
    String getSql();

    List<Object> getParams();
  }

  private final ConnectionProvider connectionProvider;

  public Zql(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
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
    try {
      PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql);
      setValues(statement, params);
      return JdbcUtils.executeUpdate(statement);
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

  public static void setValues(PreparedStatement preparedStatement, Object... values) {
    for (int i = 0; i < values.length; i++) {
      JdbcUtils.setObject(preparedStatement, i + 1, values[i]);
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
