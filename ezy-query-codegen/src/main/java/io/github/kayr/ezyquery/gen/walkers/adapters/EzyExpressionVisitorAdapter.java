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

public class EzyExpressionVisitorAdapter implements ExpressionVisitor<Object> {
  // region Not Implemented or Not Needed

  @Override
  public <S> Object visit(BitwiseRightShift aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(BitwiseLeftShift aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(NullValue nullValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Function function, S context) {
    return context;
  }

  @Override
  public <S> Object visit(SignedExpression signedExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(JdbcParameter jdbcParameter, S context) {
    return context;
  }

  @Override
  public <S> Object visit(JdbcNamedParameter jdbcNamedParameter, S context) {
    return context;
  }

  @Override
  public <S> Object visit(DoubleValue doubleValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(LongValue longValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(HexValue hexValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(DateValue dateValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TimeValue timeValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TimestampValue timestampValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(StringValue stringValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(BooleanValue booleanValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Addition addition, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Division division, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IntegerDivision division, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Multiplication multiplication, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Subtraction subtraction, S context) {
    return context;
  }

  @Override
  public <S> Object visit(AndExpression andExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(OrExpression orExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(XorExpression orExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Between between, S context) {
    return context;
  }

  @Override
  public <S> Object visit(OverlapsCondition overlapsCondition, S context) {
    return context;
  }

  @Override
  public <S> Object visit(EqualsTo equalsTo, S context) {
    return context;
  }

  @Override
  public <S> Object visit(GreaterThan greaterThan, S context) {
    return context;
  }

  @Override
  public <S> Object visit(GreaterThanEquals greaterThanEquals, S context) {
    return context;
  }

  @Override
  public <S> Object visit(InExpression inExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IncludesExpression includesExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ExcludesExpression excludesExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(FullTextSearch fullTextSearch, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IsNullExpression isNullExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IsBooleanExpression isBooleanExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IsUnknownExpression isUnknownExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(LikeExpression likeExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(MinorThan minorThan, S context) {
    return context;
  }

  @Override
  public <S> Object visit(MinorThanEquals minorThanEquals, S context) {
    return context;
  }

  @Override
  public <S> Object visit(NotEqualsTo notEqualsTo, S context) {
    return context;
  }

  @Override
  public <S> Object visit(DoubleAnd doubleAnd, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Contains contains, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ContainedBy containedBy, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ParenthesedSelect selectBody, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Column tableColumn, S context) {
    return context;
  }

  @Override
  public <S> Object visit(CaseExpression caseExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(WhenClause whenClause, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ExistsExpression existsExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(MemberOfExpression memberOfExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(AnyComparisonExpression anyComparisonExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Concat concat, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Matches matches, S context) {
    return context;
  }

  @Override
  public <S> Object visit(BitwiseAnd bitwiseAnd, S context) {
    return context;
  }

  @Override
  public <S> Object visit(BitwiseOr bitwiseOr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(BitwiseXor bitwiseXor, S context) {
    return context;
  }

  @Override
  public <S> Object visit(CastExpression cast, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Modulo modulo, S context) {
    return context;
  }

  @Override
  public <S> Object visit(AnalyticExpression aexpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ExtractExpression eexpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IntervalExpression iexpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(OracleHierarchicalExpression oexpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(RegExpMatchOperator rexpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(JsonExpression jsonExpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(JsonOperator jsonExpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(UserVariable var, S context) {
    return context;
  }

  @Override
  public <S> Object visit(NumericBind bind, S context) {
    return context;
  }

  @Override
  public <S> Object visit(KeepExpression aexpr, S context) {
    return context;
  }

  @Override
  public <S> Object visit(MySQLGroupConcat groupConcat, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ExpressionList<? extends Expression> expressionList, S context) {
    return context;
  }

  @Override
  public <S> Object visit(RowConstructor<? extends Expression> rowConstructor, S context) {
    return context;
  }

  @Override
  public <S> Object visit(RowGetExpression rowGetExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(OracleHint hint, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TimeKeyExpression timeKeyExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(DateTimeLiteralExpression literal, S context) {
    return context;
  }

  @Override
  public <S> Object visit(NotExpression aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(NextValExpression aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(CollateExpression aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(SimilarToExpression aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ArrayExpression aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ArrayConstructor aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(VariableAssignment aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(XMLSerializeExpr aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TimezoneExpression aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(JsonAggregateFunction aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(JsonFunction aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ConnectByRootOperator aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(ConnectByPriorOperator aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(OracleNamedFunctionParameter aThis, S context) {
    return context;
  }

  @Override
  public <S> Object visit(AllColumns allColumns, S context) {
    return context;
  }

  @Override
  public <S> Object visit(FunctionAllColumns functionColumns, S context) {
    return context;
  }

  @Override
  public <S> Object visit(AllTableColumns allTableColumns, S context) {
    return context;
  }

  @Override
  public <S> Object visit(AllValue allValue, S context) {
    return context;
  }

  @Override
  public <S> Object visit(IsDistinctExpression isDistinctExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(GeometryDistance geometryDistance, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Select selectBody, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TranscodingFunction transcodingFunction, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TrimFunction trimFunction, S context) {
    return context;
  }

  @Override
  public <S> Object visit(RangeExpression rangeExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TSQLLeftJoin tsqlLeftJoin, S context) {
    return context;
  }

  @Override
  public <S> Object visit(TSQLRightJoin tsqlRightJoin, S context) {
    return context;
  }

  @Override
  public <S> Object visit(StructType structType, S context) {
    return context;
  }

  @Override
  public <S> Object visit(LambdaExpression lambdaExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(HighExpression highExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(LowExpression lowExpression, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Plus plus, S context) {
    return context;
  }

  @Override
  public <S> Object visit(PriorTo priorTo, S context) {
    return context;
  }

  @Override
  public <S> Object visit(Inverse inverse, S context) {
    return context;
  }

  @Override
  public <S> Object visit(CosineSimilarity cosineSimilarity, S context) {
    return context;
  }

  @Override
  public <S> Object visit(FromQuery fromQuery, S context) {
    return context;
  }

  // endregion

  @Override
  public <S> Object visit(net.sf.jsqlparser.expression.DateUnitExpression value, S context) {
    return context;
  }

  @Override
  public <S> Object visit(net.sf.jsqlparser.expression.KeyExpression value, S context) {
    return context;
  }

  @Override
  public <S> Object visit(
      net.sf.jsqlparser.expression.PostgresNamedFunctionParameter value, S context) {
    return context;
  }

  @Override
  public <S> Object visit(
      net.sf.jsqlparser.expression.operators.relational.Intersects value, S context) {
    return context;
  }

  @Override
  public <S> Object visit(net.sf.jsqlparser.expression.TernaryExpression value, S context) {
    return context;
  }
}
