package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.ast.EzyExpr;

public interface ICondition {

  EzyExpr asExpr();
}
