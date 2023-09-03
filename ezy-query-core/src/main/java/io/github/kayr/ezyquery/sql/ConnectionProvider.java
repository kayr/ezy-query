package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import java.sql.Connection;
import javax.sql.DataSource;

public interface ConnectionProvider {

  Connection getConnection() throws Exception;

  default Connection getConnectionUnChecked() {
    try {
      return getConnection();
    } catch (Exception e) {
      throw new UnCaughtException(e);
    }
  }

  void closeConnection(Connection connection) throws Exception;

  static ConnectionProvider of(Connection connection) {
    return new SimpleConnectionProvider(connection);
  }

  static ConnectionProvider of(DataSource dataSource) {
    return new DataSourceConnectionProvider(dataSource);
  }
}
