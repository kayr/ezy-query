package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.EzySqlTranspiler;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.sql.ConnectionProvider;
import io.github.kayr.ezyquery.sql.Zql;
import java.util.Collections;
import java.util.List;

/**
 * This class works with the EzySQL query objects as opposed to raw Sql Strings. All the db handling
 * is delegated to Zql with handles all the database problems.
 */
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
    return new EzySqlTranspiler(ezyExpr, fields).transpile();
  }

  public <T> List<T> list(EzyQuery<T> query, EzyCriteria params) {
    QueryAndParams queryAndParams = query.query(params);
    return zql.rows(query.resultClass(), queryAndParams.getSql(), queryAndParams.getParams());
  }

  public <T> List<T> list(EzyQuery<T> query) {
    return Collections.emptyList();
  }

  public Zql getZql() {
    return zql;
  }
}
