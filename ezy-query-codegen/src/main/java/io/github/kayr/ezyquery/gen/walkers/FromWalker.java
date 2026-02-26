package io.github.kayr.ezyquery.gen.walkers;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.*;

@lombok.AllArgsConstructor(staticName = "of")
public class FromWalker implements FromItemVisitor<Void> {

  private WalkContext context;

  @Override
  public <S> Void visit(Table tableName, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ParenthesedSelect selectBody, S context) {
    Select select = selectBody.getSelect();
    select.accept(this.context.getSelectWalker(), null);
    return null;
  }

  @Override
  public <S> Void visit(LateralSubSelect lateralSubSelect, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TableFunction tableFunction, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ParenthesedFromItem aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Values values, S context) {
    return null;
  }

  @Override
  public <S> Void visit(PlainSelect plainSelect, S context) {
    return null;
  }

  @Override
  public <S> Void visit(SetOperationList setOperationList, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TableStatement tableStatement, S context) {
    return null;
  }

  @Override
  public <S> Void visit(FromQuery fromQuery, S context) {
    return null;
  }
}
