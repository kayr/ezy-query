package io.github.kayr.ezyquery.gen;

import java.util.Properties;

public class NoTimeQueryGen extends QueryGen {

  public NoTimeQueryGen(String packageName, String className, String sql, Properties config) {
    super(packageName, className, sql, config);
  }

  @Override
  protected String timeStamp() {
    return "0000-00-00 00:00:00";
  }
}
