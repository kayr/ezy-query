package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.ast.EzyExpr;
import net.sf.jsqlparser.expression.Expression;

public interface ExpressionHandler<T extends Expression> {


  EzyExpr handle(T expr);
}
