package io.github.kayr.ezyquery.sql;

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
  public <T> List<T> rows(Class<T> clazz, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return mapData(clazz, resultSet.resultSet);
    }
  }

  @lombok.SneakyThrows
  public <T> List<T> rows(Class<T> clazz, String sql, Object... params) {
    try (DbReSources resultSet = rows(sql, params)) {
      return mapData(clazz, resultSet.resultSet);
    }
  }

  @lombok.SneakyThrows
  public <T> T firstRow(Class<T> clazz, String sql, List<Object> params) {
    try (DbReSources resultSet = rows(sql, params)) {
      List<T> results = mapData(clazz, resultSet.resultSet, 1);

      if (resultSet.resultSet.next())
        throw new IllegalArgumentException("More than one row returned");

      return results.isEmpty() ? null : results.get(0);
    }
  }

  public DbReSources rows(String sql, List<Object> params) {
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

  private <T> List<T> mapData(Class<T> clazz, ResultSet resultSet) throws SQLException {
    return mapData(clazz, resultSet, Integer.MAX_VALUE);
  }

  private <T> List<T> mapData(Class<T> clazz, ResultSet resultSet, int numRecords)
      throws SQLException {
    List<ResultSetMapper.Column> columns = getColumns(resultSet);
    ResultSetMapper<T> mapper = ResultSetMapper.forClass(clazz);
    List<T> data = new ArrayList<>();
    int count = 0;
    while (count < numRecords && resultSet.next()) {
      T t = mapper.mapRow(resultSet, columns);
      data.add(t);
      count++;
    }
    return data;
  }

  @lombok.SneakyThrows
  public List<ResultSetMapper.Column> getColumns(ResultSet resultSet) {
    List<ResultSetMapper.Column> columns = new ArrayList<>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      String columnName = metaData.getColumnName(i);
      String columnLabel = metaData.getColumnLabel(i);
      columns.add(new ResultSetMapper.Column(columnName, columnLabel));
    }
    return columns;
  }

  @lombok.SneakyThrows
  public DbReSources rows(String sql, Object... params) {

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
