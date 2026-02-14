package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.ast.SqlExpr;
import io.github.kayr.ezyquery.util.Elf;
import java.util.List;

@lombok.Getter
@lombok.RequiredArgsConstructor
public class SqlCond implements ICond {
  private final String sql;
  private final List<Object> params;

  public static SqlCond sql(String sql, List<Object> params) {
    return new SqlCond(Elf.mayBeAddParens(sql), Elf.copyList(params));
  }

  public static SqlCond raw(String sql, List<Object> params) {
    return new SqlCond(sql, Elf.copyList(params));
  }

  @Override
  public EzyExpr asExpr() {
    return new SqlExpr(sql, params);
  }

  @Override
  public String toString() {
    return sql;
  }
}
