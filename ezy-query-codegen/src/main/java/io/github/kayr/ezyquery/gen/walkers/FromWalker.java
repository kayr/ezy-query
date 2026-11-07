package io.github.kayr.ezyquery.gen.walkers;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

public class FromWalker implements FromItemVisitor<Object> {

  @Override
  public <S> Object visit(Table tableName, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ParenthesedSelect selectBody, S context) {
    WalkContext wc = (WalkContext) context;
    Select select = selectBody.getSelect();
    select.accept(wc.getSelectWalker(), context);
    return context;
  }

  @Override
  public <S> Object visit(LateralSubSelect lateralSubSelect, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TableFunction tableFunction, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ParenthesedFromItem aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Values values, S context) {
    return context;
  }

  @Override
  public <S> Object visit(PlainSelect plainSelect, S context) {
    return context;
  }

  @Override
  public <S> Object visit(SetOperationList setOperationList, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TableStatement tableStatement, S context) {
    return context;
  }

  @Override
  public <S> Object visit(FromQuery fromQuery, S context) {
    return context;
  }

  @Override
  public <S> Object visit(net.sf.jsqlparser.statement.imprt.Import value, S context) {
    return context;
  }
}
