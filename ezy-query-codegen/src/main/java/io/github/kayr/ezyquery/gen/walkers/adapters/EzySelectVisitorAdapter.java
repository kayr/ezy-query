package io.github.kayr.ezyquery.gen.walkers.adapters;

import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

public class EzySelectVisitorAdapter implements SelectVisitor<Object> {

  @Override
  public <S> Object visit(ParenthesedSelect select, S context) {
    throw new UnsupportedOperationException("visit(ParenthesedSelect)");
  }

  @Override
  public <S> Object visit(PlainSelect plainSelect, S context) {
    throw new UnsupportedOperationException("visit(PlainSelect)");
  }

  @Override
  public <S> Object visit(SetOperationList setOperationList, S context) {
    throw new UnsupportedOperationException("visit(SetOperationList)");
  }

  @Override
  public <S> Object visit(WithItem<?> withItem, S context) {
    throw new UnsupportedOperationException("visit(WithItem)");
  }

  @Override
  public <S> Object visit(Values values, S context) {
    throw new UnsupportedOperationException("visit(Values)");
  }

  @Override
  public <S> Object visit(LateralSubSelect lateralSubSelect, S context) {
    throw new UnsupportedOperationException("visit(LateralSubSelect)");
  }

  @Override
  public <S> Object visit(TableStatement tableStatement, S context) {
    throw new UnsupportedOperationException("visit(TableStatement)");
  }

  @Override
  public <S> Object visit(FromQuery fromQuery, S context) {
    throw new UnsupportedOperationException("visit(FromQuery)");
  }
}
