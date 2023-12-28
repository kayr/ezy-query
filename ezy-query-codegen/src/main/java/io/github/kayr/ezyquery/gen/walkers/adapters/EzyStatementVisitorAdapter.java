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
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class EzyStatementVisitorAdapter implements StatementVisitor {

  @Override
  public void visit(Analyze analyze) {
    throw new UnsupportedOperationException("visit(Analyze analyze)");
  }

  @Override
  public void visit(SavepointStatement savepointStatement) {
    throw new UnsupportedOperationException("visit(SavepointStatement savepointStatement)");
  }

  @Override
  public void visit(RollbackStatement rollbackStatement) {
    throw new UnsupportedOperationException("visit(RollbackStatement rollbackStatement)");
  }

  @Override
  public void visit(Comment comment) {
    throw new UnsupportedOperationException("visit(Comment comment)");
  }

  @Override
  public void visit(Commit commit) {
    throw new UnsupportedOperationException("visit(Commit commit)");
  }

  @Override
  public void visit(Delete delete) {
    throw new UnsupportedOperationException("visit(Delete delete)");
  }

  @Override
  public void visit(Update update) {
    throw new UnsupportedOperationException("visit(Update update)");
  }

  @Override
  public void visit(Insert insert) {
    throw new UnsupportedOperationException("visit(Insert insert)");
  }

  @Override
  public void visit(Drop drop) {
    throw new UnsupportedOperationException("visit(Drop drop)");
  }

  @Override
  public void visit(Truncate truncate) {
    throw new UnsupportedOperationException("visit(Truncate truncate)");
  }

  @Override
  public void visit(CreateIndex createIndex) {
    throw new UnsupportedOperationException("visit(CreateIndex createIndex)");
  }

  @Override
  public void visit(CreateSchema aThis) {
    throw new UnsupportedOperationException("visit(CreateSchema aThis)");
  }

  @Override
  public void visit(CreateTable createTable) {
    throw new UnsupportedOperationException("visit(CreateTable createTable)");
  }

  @Override
  public void visit(CreateView createView) {
    throw new UnsupportedOperationException("visit(CreateView createView)");
  }

  @Override
  public void visit(AlterView alterView) {
    throw new UnsupportedOperationException("visit(AlterView alterView)");
  }

  @Override
  public void visit(RefreshMaterializedViewStatement materializedView) {
    throw new UnsupportedOperationException(
        "visit(RefreshMaterializedViewStatement materializedView)");
  }

  @Override
  public void visit(Alter alter) {
    throw new UnsupportedOperationException("visit(Alter alter)");
  }

  @Override
  public void visit(Statements stmts) {
    throw new UnsupportedOperationException("visit(Statements stmts)");
  }

  @Override
  public void visit(Execute execute) {
    throw new UnsupportedOperationException("visit(Execute execute)");
  }

  @Override
  public void visit(SetStatement set) {
    throw new UnsupportedOperationException("visit(SetStatement set)");
  }

  @Override
  public void visit(ResetStatement reset) {
    throw new UnsupportedOperationException("visit(ResetStatement reset)");
  }

  @Override
  public void visit(ShowColumnsStatement set) {
    throw new UnsupportedOperationException("visit(ShowColumnsStatement set)");
  }

  @Override
  public void visit(ShowIndexStatement showIndex) {
    throw new UnsupportedOperationException("visit(ShowIndexStatement showIndex)");
  }

  @Override
  public void visit(ShowTablesStatement showTables) {
    throw new UnsupportedOperationException("visit(ShowTablesStatement showTables)");
  }

  @Override
  public void visit(Merge merge) {
    throw new UnsupportedOperationException("visit(Merge merge)");
  }

  @Override
  public void visit(Select select) {
    throw new UnsupportedOperationException("visit(Select select)");
  }

  @Override
  public void visit(Upsert upsert) {
    throw new UnsupportedOperationException("visit(Upsert upsert)");
  }

  @Override
  public void visit(UseStatement use) {
    throw new UnsupportedOperationException("visit(UseStatement use)");
  }

  @Override
  public void visit(Block block) {
    throw new UnsupportedOperationException("visit(Block block)");
  }

  @Override
  public void visit(DescribeStatement describe) {
    throw new UnsupportedOperationException("visit(DescribeStatement describe)");
  }

  @Override
  public void visit(ExplainStatement aThis) {
    throw new UnsupportedOperationException("visit(ExplainStatement aThis)");
  }

  @Override
  public void visit(ShowStatement aThis) {
    throw new UnsupportedOperationException("visit(ShowStatement aThis)");
  }

  @Override
  public void visit(DeclareStatement aThis) {
    throw new UnsupportedOperationException("visit(DeclareStatement aThis)");
  }

  @Override
  public void visit(Grant grant) {
    throw new UnsupportedOperationException("visit(Grant grant)");
  }

  @Override
  public void visit(CreateSequence createSequence) {
    throw new UnsupportedOperationException("visit(CreateSequence createSequence)");
  }

  @Override
  public void visit(AlterSequence alterSequence) {
    throw new UnsupportedOperationException("visit(AlterSequence alterSequence)");
  }

  @Override
  public void visit(CreateFunctionalStatement createFunctionalStatement) {
    throw new UnsupportedOperationException(
        "visit(CreateFunctionalStatement createFunctionalStatement)");
  }

  @Override
  public void visit(CreateSynonym createSynonym) {
    throw new UnsupportedOperationException("visit(CreateSynonym createSynonym)");
  }

  @Override
  public void visit(AlterSession alterSession) {
    throw new UnsupportedOperationException("visit(AlterSession alterSession)");
  }

  @Override
  public void visit(IfElseStatement aThis) {
    throw new UnsupportedOperationException("visit(IfElseStatement aThis)");
  }

  @Override
  public void visit(RenameTableStatement renameTableStatement) {
    throw new UnsupportedOperationException("visit(RenameTableStatement renameTableStatement)");
  }

  @Override
  public void visit(PurgeStatement purgeStatement) {
    throw new UnsupportedOperationException("visit(PurgeStatement purgeStatement)");
  }

  @Override
  public void visit(AlterSystemStatement alterSystemStatement) {
    throw new UnsupportedOperationException("visit(AlterSystemStatement alterSystemStatement)");
  }

  @Override
  public void visit(UnsupportedStatement unsupportedStatement) {
    throw new UnsupportedOperationException("visit(UnsupportedStatement unsupportedStatement)");
  }
}
