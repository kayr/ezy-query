package io.github.kayr.ezyquery.api;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

  private final List<String> columns = new ArrayList<>();

  public QueryBuilder select(String... columns) {
    return this;
  }
}
