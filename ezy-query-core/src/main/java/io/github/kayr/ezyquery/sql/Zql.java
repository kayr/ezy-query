package io.github.kayr.ezyquery.sql;

import static io.github.kayr.ezyquery.api.UnCaughtException.doGet;

import io.github.kayr.ezyquery.api.UnCaughtException;
import io.github.kayr.ezyquery.util.Elf;
import java.sql.*;
import java.util.List;

public class Zql {

  private final ConnectionProvider connectionProvider;

  public Zql(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public <T> List<T> rows(Mappers.ResultsMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return JdbcUtils.mapData(mapper, resultSet.resultSet);
    }
  }

  public <T> List<T> rows(Mappers.ResultsMapper<T> mapper, String sql, Object... params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return JdbcUtils.mapData(mapper, resultSet.resultSet);
    }
  }

  public <T> T firstRow(Mappers.ResultsMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      List<T> results = JdbcUtils.mapData(mapper, resultSet.resultSet, 1);

      if (resultSet.resultSet.next())
        throw new IllegalArgumentException("More than one row returned");

      return results.isEmpty() ? null : results.get(0);
    } catch (Exception e) {
      throw new UnCaughtException("Error executing query", e);
    }
  }

  private DbReSources rows(String sql, List<Object> params) {

    return rows(sql, params.toArray());
  }

  public <T> T one(Class<T> clazz, String sql, List<Object> params) {
    try (DbReSources r = rows(sql, params)) {

      ResultSet resultSet = r.resultSet;

      T result = null;
      if (resultSet.next()) {
        //noinspection unchecked
        result = (T) resultSet.getObject(1);
      }

      if (resultSet.next()) {
        throw new IllegalArgumentException("More than one row returned");
      }

      return result;
    } catch (Exception e) {
      throw new UnCaughtException("Error executing query", e);
    }
  }

  private DbReSources rows(String sql, Object... params) {
    try {
      Connection connection = connectionProvider.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql);
      setValues(statement, params);
      ResultSet resultSet = statement.executeQuery();
      return new DbReSources(connection, statement, resultSet);
    } catch (Exception e) {
      throw new UnCaughtException("Error executing query", e);
    }
  }

  public Integer executeUpdate(String sql, Object... params) {
    Connection connection = doGet(connectionProvider::getConnection);
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      setValues(statement, params);
      return statement.executeUpdate();
    } catch (Exception e) {
      throw new UnCaughtException("Error executing update", e);
    } finally {
      closeConnection(connection);
    }
  }

  private void closeConnection(Connection connection) {
    try {
      connectionProvider.closeConnection(connection);
    } catch (Exception e) {
      throw new UnCaughtException("Error closing connection", e);
    }
  }

  public static void setValues(PreparedStatement preparedStatement, Object... values)
      throws SQLException {
    for (int i = 0; i < values.length; i++) {
      preparedStatement.setObject(i + 1, values[i]);
    }
  }

  @lombok.AllArgsConstructor
  @lombok.Getter
  public static class Column {
    private String name;
    private String label;
  }

  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  private class DbReSources implements AutoCloseable {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    @Override
    public void close() {
      Elf.closeQuietly(resultSet);
      Elf.closeQuietly(statement);
      closeConnection(connection);
    }
  }
}
