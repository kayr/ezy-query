package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.parser.EszySqlTranspiler;

import java.util.List;

public class EzySql {

  private EzySql() {}

  public static EszySqlTranspiler.QueryAndParams transpile(List<Field> fields, String sql) {
    return transpile(fields, ExprParser.parseExpr(sql));
  }

  public static EszySqlTranspiler.QueryAndParams transpile(List<Field> fields, EzyExpr ezyExpr) {
    return new EszySqlTranspiler(ezyExpr, fields).transpile();
  }
}
