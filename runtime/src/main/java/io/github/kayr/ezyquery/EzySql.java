package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.Conds;
import io.github.kayr.ezyquery.ast.EzyExpr;
import io.github.kayr.ezyquery.parser.ExprParser;
import io.github.kayr.ezyquery.parser.EszySqlTranspiler;
import io.github.kayr.ezyquery.parser.QueryAndParams;

import java.util.List;

public class EzySql {

  private EzySql() {}

  public static QueryAndParams transpile(List<Field<?>> fields, String sql) {
    return transpile(fields, ExprParser.parseExpr(sql));
  }

  public static QueryAndParams transpile(List<Field<?>> fields, EzyExpr ezyExpr) {
    return new EszySqlTranspiler(ezyExpr, fields).transpile();
  }

  public static void main(String[] args) {
    Conds conds = Cnd.orAll(Cnd.not("#reversed"), Cnd.orAll("#reversed", Cnd.isNotNull("#id")));

    System.out.println(conds.asExpr());
  }
}
