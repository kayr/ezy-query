package io.github.kayr.ezyquery.gen.walkers.adapters;

import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class EzyStatementVisitorAdapter implements StatementVisitor<Object> {

  @Override
  public <S> Object visit(Analyze analyze, S context) {
    throw new UnsupportedOperationException("visit(Analyze analyze)");
  }

  @Override
  public <S> Object visit(SavepointStatement savepointStatement, S context) {
    throw new UnsupportedOperationException("visit(SavepointStatement savepointStatement)");
  }

  @Override
  public <S> Object visit(RollbackStatement rollbackStatement, S context) {
    throw new UnsupportedOperationException("visit(RollbackStatement rollbackStatement)");
  }

  @Override
  public <S> Object visit(Comment comment, S context) {
    throw new UnsupportedOperationException("visit(Comment comment)");
  }

  @Override
  public <S> Object visit(Commit commit, S context) {
    throw new UnsupportedOperationException("visit(Commit commit)");
  }

  @Override
  public <S> Object visit(Delete delete, S context) {
    throw new UnsupportedOperationException("visit(Delete delete)");
  }

  @Override
  public <S> Object visit(Update update, S context) {
    throw new UnsupportedOperationException("visit(Update update)");
  }

  @Override
  public <S> Object visit(Insert insert, S context) {
    throw new UnsupportedOperationException("visit(Insert insert)");
  }

  @Override
  public <S> Object visit(Drop drop, S context) {
    throw new UnsupportedOperationException("visit(Drop drop)");
  }

  @Override
  public <S> Object visit(Truncate truncate, S context) {
    throw new UnsupportedOperationException("visit(Truncate truncate)");
  }

  @Override
  public <S> Object visit(CreateIndex createIndex, S context) {
    throw new UnsupportedOperationException("visit(CreateIndex createIndex)");
  }

  @Override
  public <S> Object visit(CreateSchema aThis, S context) {
    throw new UnsupportedOperationException("visit(CreateSchema aThis)");
  }

  @Override
  public <S> Object visit(CreateTable createTable, S context) {
    throw new UnsupportedOperationException("visit(CreateTable createTable)");
  }

  @Override
  public <S> Object visit(CreateView createView, S context) {
    throw new UnsupportedOperationException("visit(CreateView createView)");
  }

  @Override
  public <S> Object visit(AlterView alterView, S context) {
    throw new UnsupportedOperationException("visit(AlterView alterView)");
  }

  @Override
  public <S> Object visit(RefreshMaterializedViewStatement materializedView, S context) {
    throw new UnsupportedOperationException(
        "visit(RefreshMaterializedViewStatement materializedView)");
  }

  @Override
  public <S> Object visit(Alter alter, S context) {
    throw new UnsupportedOperationException("visit(Alter alter)");
  }

  @Override
  public <S> Object visit(Statements stmts, S context) {
    throw new UnsupportedOperationException("visit(Statements stmts)");
  }

  @Override
  public <S> Object visit(Execute execute, S context) {
    throw new UnsupportedOperationException("visit(Execute execute)");
  }

  @Override
  public <S> Object visit(SetStatement set, S context) {
    throw new UnsupportedOperationException("visit(SetStatement set)");
  }

  @Override
  public <S> Object visit(ResetStatement reset, S context) {
    throw new UnsupportedOperationException("visit(ResetStatement reset)");
  }

  @Override
  public <S> Object visit(ShowColumnsStatement set, S context) {
    throw new UnsupportedOperationException("visit(ShowColumnsStatement set)");
  }

  @Override
  public <S> Object visit(ShowIndexStatement showIndex, S context) {
    throw new UnsupportedOperationException("visit(ShowIndexStatement showIndex)");
  }

  @Override
  public <S> Object visit(ShowTablesStatement showTables, S context) {
    throw new UnsupportedOperationException("visit(ShowTablesStatement showTables)");
  }

  @Override
  public <S> Object visit(Merge merge, S context) {
    throw new UnsupportedOperationException("visit(Merge merge)");
  }

  @Override
  public <S> Object visit(Select select, S context) {
    throw new UnsupportedOperationException("visit(Select select)");
  }

  @Override
  public <S> Object visit(Upsert upsert, S context) {
    throw new UnsupportedOperationException("visit(Upsert upsert)");
  }

  @Override
  public <S> Object visit(UseStatement use, S context) {
    throw new UnsupportedOperationException("visit(UseStatement use)");
  }

  @Override
  public <S> Object visit(Block block, S context) {
    throw new UnsupportedOperationException("visit(Block block)");
  }

  @Override
  public <S> Object visit(DescribeStatement describe, S context) {
    throw new UnsupportedOperationException("visit(DescribeStatement describe)");
  }

  @Override
  public <S> Object visit(ExplainStatement aThis, S context) {
    throw new UnsupportedOperationException("visit(ExplainStatement aThis)");
  }

  @Override
  public <S> Object visit(ShowStatement aThis, S context) {
    throw new UnsupportedOperationException("visit(ShowStatement aThis)");
  }

  @Override
  public <S> Object visit(DeclareStatement aThis, S context) {
    throw new UnsupportedOperationException("visit(DeclareStatement aThis)");
  }

  @Override
  public <S> Object visit(Grant grant, S context) {
    throw new UnsupportedOperationException("visit(Grant grant)");
  }

  @Override
  public <S> Object visit(CreateSequence createSequence, S context) {
    throw new UnsupportedOperationException("visit(CreateSequence createSequence)");
  }

  @Override
  public <S> Object visit(AlterSequence alterSequence, S context) {
    throw new UnsupportedOperationException("visit(AlterSequence alterSequence)");
  }

  @Override
  public <S> Object visit(CreateFunctionalStatement createFunctionalStatement, S context) {
    throw new UnsupportedOperationException(
        "visit(CreateFunctionalStatement createFunctionalStatement)");
  }

  @Override
  public <S> Object visit(CreateSynonym createSynonym, S context) {
    throw new UnsupportedOperationException("visit(CreateSynonym createSynonym)");
  }

  @Override
  public <S> Object visit(AlterSession alterSession, S context) {
    throw new UnsupportedOperationException("visit(AlterSession alterSession)");
  }

  @Override
  public <S> Object visit(IfElseStatement aThis, S context) {
    throw new UnsupportedOperationException("visit(IfElseStatement aThis)");
  }

  @Override
  public <S> Object visit(RenameTableStatement renameTableStatement, S context) {
    throw new UnsupportedOperationException("visit(RenameTableStatement renameTableStatement)");
  }

  @Override
  public <S> Object visit(PurgeStatement purgeStatement, S context) {
    throw new UnsupportedOperationException("visit(PurgeStatement purgeStatement)");
  }

  @Override
  public <S> Object visit(AlterSystemStatement alterSystemStatement, S context) {
    throw new UnsupportedOperationException("visit(AlterSystemStatement alterSystemStatement)");
  }

  @Override
  public <S> Object visit(UnsupportedStatement unsupportedStatement, S context) {
    throw new UnsupportedOperationException("visit(UnsupportedStatement unsupportedStatement)");
  }

  @Override
  public <S> Object visit(ParenthesedInsert parenthesedInsert, S context) {
    throw new UnsupportedOperationException("visit(ParenthesedInsert parenthesedInsert)");
  }

  @Override
  public <S> Object visit(ParenthesedUpdate parenthesedUpdate, S context) {
    throw new UnsupportedOperationException("visit(ParenthesedUpdate parenthesedUpdate)");
  }

  @Override
  public <S> Object visit(ParenthesedDelete parenthesedDelete, S context) {
    throw new UnsupportedOperationException("visit(ParenthesedDelete parenthesedDelete)");
  }
}
