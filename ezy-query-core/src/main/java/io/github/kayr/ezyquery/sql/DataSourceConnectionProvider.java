package io.github.kayr.ezyquery.sql;

import java.sql.Connection;

public class DataSourceConnectionProvider implements ConnectionProvider {

  private final javax.sql.DataSource dataSource;

  public DataSourceConnectionProvider(javax.sql.DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Connection getConnection() throws Exception {
    return dataSource.getConnection();
  }

  @Override
  public void closeConnection(Connection connection) throws Exception {
    connection.close();
  }
}
