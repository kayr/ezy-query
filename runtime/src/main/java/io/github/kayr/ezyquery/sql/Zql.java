package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.Function;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Zql {

  private DataSource dataSource;
  private Connection connection;

  public Zql(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  private Zql(Connection connection) {
    this.connection = connection;
  }

  @lombok.SneakyThrows
  public <T> List<T> rows(Class<T> clazz, String sql, Object... params) {
    ResultSet resultSet = rows(sql, params);
    return mapData(clazz, resultSet);
  }

  public ResultSet rows(String sql, List<Object> params) {
    return rows(sql, params.toArray());
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
    Connection con = createConnection();
    try {
      return fx.apply(con);
    } finally {
      if (this.connection == null) closeQuietly(con);
    }
  }

  private void closeQuietly(AutoCloseable con) {
    try {
      con.close();
    } catch (Exception e) {
      // ignore
    }
  }

  @lombok.SneakyThrows
  protected Statement prepareStatement(Connection connection, String sql) {

    return connection.prepareStatement(sql);
  }

  @lombok.SneakyThrows
  protected Connection createConnection() {
    if (dataSource == null) {
      return connection;
    }
    return dataSource.getConnection();
  }
}
