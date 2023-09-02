package io.github.kayr.ezyquery.sql;

import static io.github.kayr.ezyquery.api.EzyQueryUnCaughtException.doGet;

import io.github.kayr.ezyquery.api.EzyQueryUnCaughtException;
import io.github.kayr.ezyquery.util.Elf;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Zql {

  private final ConnectionProvider connectionProvider;

  public Zql(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public <T> List<T> rows(ResultsMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return mapData(mapper, resultSet.resultSet);
    }
  }

  public <T> List<T> rows(ResultsMapper<T> mapper, String sql, Object... params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return mapData(mapper, resultSet.resultSet);
    }
  }

  public <T> T firstRow(ResultsMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      List<T> results = mapData(mapper, resultSet.resultSet, 1);

      if (resultSet.resultSet.next())
        throw new IllegalArgumentException("More than one row returned");

      return results.isEmpty() ? null : results.get(0);
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException("Error executing query", e);
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
        result = (T) resultSet.getObject(1);
      }

      if (resultSet.next()) {
        throw new IllegalArgumentException("More than one row returned");
      }

      return result;
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException("Error executing query", e);
    }
  }

  private <T> List<T> mapData(ResultsMapper<T> mapper, ResultSet resultSet) {
    return mapData(mapper, resultSet, Integer.MAX_VALUE);
  }

  private <T> List<T> mapData(ResultsMapper<T> mapper, ResultSet resultSet, int numRecords) {
    try {
      List<Column> columns = getColumns(resultSet);
      List<T> data = new ArrayList<>();
      int count = 0;
      while (count < numRecords && resultSet.next()) {
        T t = mapper.mapRow(count, columns, resultSet);
        data.add(t);
      }
      return data;
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException("Error mapping data", e);
    }
  }

  public static List<Column> getColumns(ResultSet resultSet) throws SQLException {
    List<Column> columns = new ArrayList<>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      String columnName = metaData.getColumnName(i);
      String columnLabel = metaData.getColumnLabel(i);
      columns.add(new Column(columnName, columnLabel));
    }
    return columns;
  }

  private DbReSources rows(String sql, Object... params) {
    try {
      Connection connection = connectionProvider.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql);
      setValues(statement, params);
      ResultSet resultSet = statement.executeQuery();
      return new DbReSources(connection, statement, resultSet);
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException("Error executing query", e);
    }
  }

  public Integer executeUpdate(String sql, Object... params) {
    Connection connection = doGet(connectionProvider::getConnection);
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      setValues(statement, params);
      return statement.executeUpdate();
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException("Error executing update", e);
    } finally {
      closeConnection(connection);
    }
  }

  private void closeConnection(Connection connection) {
    try {
      connectionProvider.closeConnection(connection);
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException("Error closing connection", e);
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
