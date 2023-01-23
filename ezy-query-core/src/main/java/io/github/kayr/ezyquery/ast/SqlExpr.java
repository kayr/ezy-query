package io.github.kayr.ezyquery.ast;

import java.util.List;

@lombok.Getter
@lombok.RequiredArgsConstructor
public class SqlExpr implements EzyExpr {

  final String sql;
  final List<Object> params;

  @Override
  public String toString() {
    return sql;
  }
}
