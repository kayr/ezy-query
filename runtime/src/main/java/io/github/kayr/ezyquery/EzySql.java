package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.transpile.Transpiler;

import java.util.List;

public class EzySql {

  public static Transpiler.Result transpile(List<Field> fields, String sql) {
    EzyExpr ezyExpr = ExprParser.parseExpr(sql);
    return new Transpiler(ezyExpr, fields).transpile();
  }
}
