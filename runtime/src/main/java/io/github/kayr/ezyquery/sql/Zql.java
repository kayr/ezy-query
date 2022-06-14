package io.github.kayr.ezyquery.sql;

import javax.sql.DataSource;
import java.sql.Connection;

public class Zql {

  private DataSource dataSource;
  private Connection connection;

  public Zql(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  private Zql(Connection connection) {
    this.connection = connection;
  }




}
