package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.Function;

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
    try (ResultSet resultSet = rows(sql, params)) {
      return mapData(clazz, resultSet);
    }
  }

  @lombok.SneakyThrows
  public <T> List<T> rows(Class<T> clazz, String sql, Object... params) {
    try (ResultSet resultSet = rows(sql, params)) {
      return mapData(clazz, resultSet);
    }
  }

  public ResultSet rows(String sql, List<Object> params) {
    return rows(sql, params.toArray());
  }

  @lombok.SneakyThrows
  public <T> T one(Class<T> clazz, String sql, List<Object> params) {
    try (ResultSet resultSet = rows(sql, params)) {

      T result = null;
      if (resultSet.next()) {
        result = resultSet.getObject(1, clazz);
      }

      if (resultSet.next()) {
        throw new IllegalArgumentException("More than one row returned");
      }

      return result;
    }
  }

  private <T> List<T> mapData(Class<T> clazz, ResultSet resultSet) throws SQLException {
    ResultSetMapper<T> mapper = ResultSetMapper.forClass(clazz);
    List<T> data = new ArrayList<>();
    int index = 0;
    while (resultSet.next()) {
      T t = mapper.mapRow(resultSet, index++);
      data.add(t);
    }
    return data;
  }

  public ResultSet rows(String sql, Object... params) {
    return withStatement(
        statement -> {
          setValues(statement, params);
          return statement.executeQuery();
        },
        sql);
  }

  public static void setValues(PreparedStatement preparedStatement, Object... values)
      throws SQLException {
    for (int i = 0; i < values.length; i++) {
      preparedStatement.setObject(i + 1, values[i]);
    }
  }

  public <T> T withStatement(Function<PreparedStatement, T> function, String sql) {

    return withConnection(
        con -> {
          PreparedStatement statement = con.prepareStatement(sql);

          try {
            return function.apply(statement);
          } finally {
            closeQuietly(statement);
          }
        });
  }

  @lombok.SneakyThrows
  public <T> T withConnection(Function<Connection, T> fx) {
    Connection con = connectionProvider.getConnection();
    try {
      return fx.apply(con);
    } finally {
      connectionProvider.closeConnection(con);
    }
  }

  private void closeQuietly(AutoCloseable con) {
    try {
      con.close();
    } catch (Exception e) {
      // ignore
    }
  }
}
