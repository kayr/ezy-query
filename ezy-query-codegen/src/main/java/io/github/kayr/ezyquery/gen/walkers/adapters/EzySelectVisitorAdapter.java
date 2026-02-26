package io.github.kayr.ezyquery.gen.walkers.adapters;

import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

public class EzySelectVisitorAdapter implements SelectVisitor<Void> {

  @Override
  public <S> Void visit(ParenthesedSelect select, S context) {
    throw new UnsupportedOperationException("visit(ParenthesedSelect)");
  }

  @Override
  public <S> Void visit(PlainSelect plainSelect, S context) {
    throw new UnsupportedOperationException("visit(PlainSelect)");
  }

  @Override
  public <S> Void visit(SetOperationList setOperationList, S context) {
    throw new UnsupportedOperationException("visit(SetOperationList)");
  }

  @Override
  public <S> Void visit(WithItem<?> withItem, S context) {
    throw new UnsupportedOperationException("visit(WithItem)");
  }

  @Override
  public <S> Void visit(Values values, S context) {
    throw new UnsupportedOperationException("visit(Values)");
  }

  @Override
  public <S> Void visit(LateralSubSelect lateralSubSelect, S context) {
    throw new UnsupportedOperationException("visit(LateralSubSelect)");
  }

  @Override
  public <S> Void visit(TableStatement tableStatement, S context) {
    throw new UnsupportedOperationException("visit(TableStatement)");
  }

  @Override
  public <S> Void visit(FromQuery fromQuery, S context) {
    throw new UnsupportedOperationException("visit(FromQuery)");
  }
}
