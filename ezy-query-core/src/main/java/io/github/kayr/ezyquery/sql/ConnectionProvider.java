package io.github.kayr.ezyquery.sql;

import java.sql.Connection;
import javax.sql.DataSource;

public interface ConnectionProvider {

  Connection getConnection() throws Exception;

  void closeConnection(Connection connection) throws Exception;

  static ConnectionProvider of(Connection connection) {
    return new SimpleConnectionProvider(connection);
  }

  static ConnectionProvider of(DataSource dataSource) {
    return new DataSourceConnectionProvider(dataSource);
  }
}
