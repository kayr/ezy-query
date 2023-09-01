package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.EzyQueryUnCheckedException;
import io.github.kayr.ezyquery.util.Elf;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Zql {

  private final ConnectionProvider connectionProvider;

  public Zql(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  @lombok.SneakyThrows
  public <T> List<T> rows(ResultsMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return mapData(mapper, resultSet.resultSet);
    }
  }

  @lombok.SneakyThrows
  public <T> List<T> rows(ResultsMapper<T> mapper, String sql, Object... params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return mapData(mapper, resultSet.resultSet);
    }
  }

  @lombok.SneakyThrows
  public <T> T firstRow(ResultsMapper<T> mapper, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      List<T> results = mapData(mapper, resultSet.resultSet, 1);

      if (resultSet.resultSet.next())
        throw new IllegalArgumentException("More than one row returned");

      return results.isEmpty() ? null : results.get(0);
    }
  }

  private DbReSources rows(String sql, List<Object> params) {
    return rows(sql, params.toArray());
  }

  @lombok.SneakyThrows
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
    }
  }

  private <T> List<T> mapData(ResultsMapper<T> mapper, ResultSet resultSet) throws SQLException {
    return mapData(mapper, resultSet, Integer.MAX_VALUE);
  }

  private <T> List<T> mapData(ResultsMapper<T> mapper, ResultSet resultSet, int numRecords)
      throws SQLException {
    List<Column> columns = getColumns(resultSet);
    List<T> data = new ArrayList<>();
    int count = 0;
    while (count < numRecords && resultSet.next()) {
      try {
        T t = mapper.mapRow(count, columns, resultSet);
        data.add(t);
      } catch (Exception e) {
        EzyQueryUnCheckedException.throwException("Error mapping row", e);
      }
    }
    return data;
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

  @lombok.SneakyThrows
  private DbReSources rows(String sql, Object... params) {

    Connection connection = connectionProvider.getConnection();
    PreparedStatement statement = connection.prepareStatement(sql);
    setValues(statement, params);
    ResultSet resultSet = statement.executeQuery();
    return new DbReSources(connection, statement, resultSet);
  }

  @lombok.SneakyThrows
  public Integer executeUpdate(String sql, Object... params) {
    Connection connection = connectionProvider.getConnection();
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      setValues(statement, params);
      return statement.executeUpdate();
    } finally {
      connectionProvider.closeConnection(connection);
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
    public void close() throws Exception {
      Elf.closeQuietly(resultSet);
      Elf.closeQuietly(statement);
      connectionProvider.closeConnection(connection);
    }
  }
}
