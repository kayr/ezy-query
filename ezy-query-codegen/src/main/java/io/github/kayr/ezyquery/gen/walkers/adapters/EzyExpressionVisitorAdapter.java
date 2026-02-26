package io.github.kayr.ezyquery.gen.walkers.adapters;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FunctionAllColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;

public class EzyExpressionVisitorAdapter implements ExpressionVisitor<Void> {
  // region Not Implemented or Not Needed

  @Override
  public <S> Void visit(BitwiseRightShift aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(BitwiseLeftShift aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(NullValue nullValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Function function, S context) {
    return null;
  }

  @Override
  public <S> Void visit(SignedExpression signedExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(JdbcParameter jdbcParameter, S context) {
    return null;
  }

  @Override
  public <S> Void visit(JdbcNamedParameter jdbcNamedParameter, S context) {
    return null;
  }

  @Override
  public <S> Void visit(DoubleValue doubleValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(LongValue longValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(HexValue hexValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(DateValue dateValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TimeValue timeValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TimestampValue timestampValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(StringValue stringValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(BooleanValue booleanValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Addition addition, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Division division, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IntegerDivision division, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Multiplication multiplication, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Subtraction subtraction, S context) {
    return null;
  }

  @Override
  public <S> Void visit(AndExpression andExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(OrExpression orExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(XorExpression orExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Between between, S context) {
    return null;
  }

  @Override
  public <S> Void visit(OverlapsCondition overlapsCondition, S context) {
    return null;
  }

  @Override
  public <S> Void visit(EqualsTo equalsTo, S context) {
    return null;
  }

  @Override
  public <S> Void visit(GreaterThan greaterThan, S context) {
    return null;
  }

  @Override
  public <S> Void visit(GreaterThanEquals greaterThanEquals, S context) {
    return null;
  }

  @Override
  public <S> Void visit(InExpression inExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IncludesExpression includesExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ExcludesExpression excludesExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(FullTextSearch fullTextSearch, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IsNullExpression isNullExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IsBooleanExpression isBooleanExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IsUnknownExpression isUnknownExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(LikeExpression likeExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(MinorThan minorThan, S context) {
    return null;
  }

  @Override
  public <S> Void visit(MinorThanEquals minorThanEquals, S context) {
    return null;
  }

  @Override
  public <S> Void visit(NotEqualsTo notEqualsTo, S context) {
    return null;
  }

  @Override
  public <S> Void visit(DoubleAnd doubleAnd, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Contains contains, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ContainedBy containedBy, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ParenthesedSelect selectBody, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Column tableColumn, S context) {
    return null;
  }

  @Override
  public <S> Void visit(CaseExpression caseExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(WhenClause whenClause, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ExistsExpression existsExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(MemberOfExpression memberOfExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(AnyComparisonExpression anyComparisonExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Concat concat, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Matches matches, S context) {
    return null;
  }

  @Override
  public <S> Void visit(BitwiseAnd bitwiseAnd, S context) {
    return null;
  }

  @Override
  public <S> Void visit(BitwiseOr bitwiseOr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(BitwiseXor bitwiseXor, S context) {
    return null;
  }

  @Override
  public <S> Void visit(CastExpression cast, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Modulo modulo, S context) {
    return null;
  }

  @Override
  public <S> Void visit(AnalyticExpression aexpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ExtractExpression eexpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IntervalExpression iexpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(OracleHierarchicalExpression oexpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(RegExpMatchOperator rexpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(JsonExpression jsonExpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(JsonOperator jsonExpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(UserVariable var, S context) {
    return null;
  }

  @Override
  public <S> Void visit(NumericBind bind, S context) {
    return null;
  }

  @Override
  public <S> Void visit(KeepExpression aexpr, S context) {
    return null;
  }

  @Override
  public <S> Void visit(MySQLGroupConcat groupConcat, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ExpressionList<? extends Expression> expressionList, S context) {
    return null;
  }

  @Override
  public <S> Void visit(RowConstructor<? extends Expression> rowConstructor, S context) {
    return null;
  }

  @Override
  public <S> Void visit(RowGetExpression rowGetExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(OracleHint hint, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TimeKeyExpression timeKeyExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(DateTimeLiteralExpression literal, S context) {
    return null;
  }

  @Override
  public <S> Void visit(NotExpression aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(NextValExpression aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(CollateExpression aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(SimilarToExpression aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ArrayExpression aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ArrayConstructor aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(VariableAssignment aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(XMLSerializeExpr aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TimezoneExpression aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(JsonAggregateFunction aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(JsonFunction aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ConnectByRootOperator aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(ConnectByPriorOperator aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(OracleNamedFunctionParameter aThis, S context) {
    return null;
  }

  @Override
  public <S> Void visit(AllColumns allColumns, S context) {
    return null;
  }

  @Override
  public <S> Void visit(FunctionAllColumns functionColumns, S context) {
    return null;
  }

  @Override
  public <S> Void visit(AllTableColumns allTableColumns, S context) {
    return null;
  }

  @Override
  public <S> Void visit(AllValue allValue, S context) {
    return null;
  }

  @Override
  public <S> Void visit(IsDistinctExpression isDistinctExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(GeometryDistance geometryDistance, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Select selectBody, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TranscodingFunction transcodingFunction, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TrimFunction trimFunction, S context) {
    return null;
  }

  @Override
  public <S> Void visit(RangeExpression rangeExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TSQLLeftJoin tsqlLeftJoin, S context) {
    return null;
  }

  @Override
  public <S> Void visit(TSQLRightJoin tsqlRightJoin, S context) {
    return null;
  }

  @Override
  public <S> Void visit(StructType structType, S context) {
    return null;
  }

  @Override
  public <S> Void visit(LambdaExpression lambdaExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(HighExpression highExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(LowExpression lowExpression, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Plus plus, S context) {
    return null;
  }

  @Override
  public <S> Void visit(PriorTo priorTo, S context) {
    return null;
  }

  @Override
  public <S> Void visit(Inverse inverse, S context) {
    return null;
  }

  @Override
  public <S> Void visit(CosineSimilarity cosineSimilarity, S context) {
    return null;
  }

  @Override
  public <S> Void visit(FromQuery fromQuery, S context) {
    return null;
  }

  // endregion

}
