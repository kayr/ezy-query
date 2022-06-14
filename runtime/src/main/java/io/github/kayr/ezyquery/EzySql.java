package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.transpile.EszySqlTranspiler;

import java.util.List;

public class EzySql {

  private EzySql() {}

  public static EszySqlTranspiler.Result transpile(List<Field> fields, String sql) {
    EzyExpr ezyExpr = ExprParser.parseExpr(sql);
    return transpile(fields, ezyExpr);
  }

  public static EszySqlTranspiler.Result transpile(List<Field> fields, EzyExpr ezyExpr) {
    return new EszySqlTranspiler(ezyExpr, fields).transpile();
  }
}
