package io.github.kayr.ezyquery.testqueries;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.parser.SqlParts;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * -- (True or True) and False select officeCode as "code", true or true as a , false as b from
 * offices
 */
public class Booleans implements EzyQuery<Booleans.Result> {
  public static Field<Object> CODE = Field.of("officeCode", "code", Object.class);

  public static Field<Object> A =
      Field.of("true OR true", "a", Object.class, Field.ExpressionType.BINARY);

  public static Field<Object> B = Field.of("false", "b", Object.class);

  public static final Booleans QUERY = new Booleans();

  private final SqlParts schema = SqlParts.of("offices");

  private final List<Field<?>> fields = new ArrayList<Field<?>>();

  public Booleans() {
    init();
  }

  private void init() {
    fields.add(CODE);
    fields.add(A);
    fields.add(B);
  }

  public QueryAndParams query(EzyCriteria criteria) {
    return SqlBuilder.buildSql(this, criteria);
  }

  @Override
  public SqlParts schema() {
    return this.schema;
  }

  @Override
  public Optional<SqlParts> whereClause() {
    return Optional.empty();
  }

  @Override
  public Optional<SqlParts> orderByClause() {
    return Optional.empty();
  }

  @Override
  public List<Field<?>> fields() {
    return this.fields;
  }

  @Override
  public Class<Result> resultClass() {
    return Result.class;
  }

  public static class Result {
    private Object code;

    private Object a;

    private Object b;

    public Object getCode() {
      return code;
    }

    public Object getA() {
      return a;
    }

    public Object getB() {
      return b;
    }

    @Override
    public String toString() {
      return "Booleans.Result{" + "code = " + code + ", a = " + a + ", b = " + b + "}";
    }
  }
}
