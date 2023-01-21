package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.Elf;
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
  public void closeConnection(Connection connection) {
    Elf.closeQuietly(connection);
  }
}
