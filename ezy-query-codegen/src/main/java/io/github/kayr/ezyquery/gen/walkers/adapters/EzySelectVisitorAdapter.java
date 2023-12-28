package io.github.kayr.ezyquery.gen.walkers.adapters;

import net.sf.jsqlparser.statement.select.*;

public class EzySelectVisitorAdapter implements SelectVisitor {

  @Override
  public void visit(ParenthesedSelect select) {
    throw new UnsupportedOperationException("visit(ParenthesedSelect)");
  }

  @Override
  public void visit(PlainSelect plainSelect) {
    throw new UnsupportedOperationException("visit(PlainSelect)");
  }

  @Override
  public void visit(SetOperationList setOperationList) {
    throw new UnsupportedOperationException("visit(SetOperationList)");
  }

  @Override
  public void visit(WithItem withItem) {
    throw new UnsupportedOperationException("visit(WithItem)");
  }

  @Override
  public void visit(Values values) {
    throw new UnsupportedOperationException("visit(Values)");
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    throw new UnsupportedOperationException("visit(LateralSubSelect)");
  }

  @Override
  public void visit(TableStatement tableStatement) {
    throw new UnsupportedOperationException("visit(TableStatement)");
  }
}
