package io.github.kayr.ezyquery.sql;

import java.sql.Connection;

public class SimpleConnectionProvider implements ConnectionProvider {

  private final Connection connectionFactory;

  public SimpleConnectionProvider(Connection connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public Connection getConnection() throws Exception {
    return connectionFactory;
  }

  @Override
  public void closeConnection(Connection connection) throws Exception {
    // do nothing
  }
}
