package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.Conds;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.EszySqlTranspiler;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.sample.TransactionQuery;
import io.github.kayr.ezyquery.sql.ConnectionProvider;
import io.github.kayr.ezyquery.sql.Zql;
import java.util.Collections;
import java.util.List;

public class EzySql {

  private ConnectionProvider provider;
  private Zql zql;

  private EzySql() {}

  public static EzySql withProvider(ConnectionProvider connectionProvider) {
    EzySql ezySql = new EzySql();
    ezySql.provider = connectionProvider;
    ezySql.zql = new Zql(connectionProvider);
    return ezySql;
  }

  public static QueryAndParams transpile(List<Field<?>> fields, String sql) {
    return transpile(fields, ExprParser.parseExpr(sql));
  }

  public static QueryAndParams transpile(List<Field<?>> fields, EzyExpr ezyExpr) {
    return new EszySqlTranspiler(ezyExpr, fields).transpile();
  }

  public <T> List<T> list(EzyQuery<T> query, FilterParams params) {
    QueryAndParams queryAndParams = query.query(params);
    return zql.rows(query.resultClass(), queryAndParams.getSql(), queryAndParams.getParams());
  }

  public <T> List<T> list(EzyQuery<T> query) {
    return Collections.emptyList();
  }

  public static void main(String[] args) {

    EzySql ez = new EzySql();

    ez.list(
        TransactionQuery.Q,
        FilterParams.selectCount()
            .where(
                Cnd.or(
                    Cnd.andAll(
                        Cnd.eq(TransactionQuery.NAME, "Ronald"),
                        Cnd.eq(TransactionQuery.NAME, "Ronald"),
                        Cnd.eq(TransactionQuery.NAME, "Ronald"),
                        Cnd.eq(TransactionQuery.NAME, "Ronald")),
                    Cnd.orAll(
                        Cnd.eq(TransactionQuery.NAME, "Ronald"),
                        Cnd.eq(TransactionQuery.NAME, "Ronald"),
                        Cnd.eq(TransactionQuery.NAME, "Ronald"),
                        Cnd.eq(TransactionQuery.NAME, "Ronald")))));

    Conds conds = Cnd.orAll(Cnd.not("#reversed"), Cnd.orAll("#reversed", Cnd.isNotNull("#id")));

    System.out.println(conds.asExpr());
  }
}
