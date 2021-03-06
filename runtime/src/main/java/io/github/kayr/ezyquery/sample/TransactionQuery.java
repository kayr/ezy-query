package io.github.kayr.ezyquery.sample;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.sql.ConnectionProvider;
import io.github.kayr.ezyquery.sql.Zql;

import javax.annotation.Generated;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Generated(value = "EzyQuery", date = "2020-01-01T00:00:00.000+0000")
public class TransactionQuery implements EzyQuery<TransactionQuery.Result> {

  public static final Field<Object> NAME = Field.of("c.name", "name");
  public static final Field<String> SEX = Field.of("c.sex", "sex", String.class);
  public static final Field<BigDecimal> AGE = Field.of("c.age", "age", BigDecimal.class);

  private String schema =
      "m_share_account msa\n"
          + "LEFT OUTER JOIN m_client cl ON cl.id = msa.client_id\n"
          + "LEFT OUTER JOIN m_group grp ON grp.id = msa.group_id\n"
          + "LEFT OUTER JOIN i_institution inst ON inst.client_id = cl.id\n"
          + "LEFT OUTER JOIN m_share_product msp ON msp.id = msa.product_id\n"
          + "LEFT OUTER JOIN m_share_product_market_price prod_price ON prod_price.product_id = msp.id\n"
          + "LEFT OUTER JOIN m_office mo_cl ON cl.office_id = mo_cl.id\n"
          + "LEFT OUTER JOIN m_office mo_grp ON grp.office_id = mo_grp.id\n";

  private final List<Field<?>> fields = Arrays.asList(NAME, SEX, AGE);
  ;
  private ConnectionProvider connectionProvider;

  public static TransactionQuery from(ConnectionProvider provider) {
    TransactionQuery transactionQuery = new TransactionQuery();
    transactionQuery.connectionProvider = provider;
    return transactionQuery;
  }

  public TransactionQuery() {
    init();
  }

  private void init() {
    System.out.println("Hello");
  }

  @Override
  public QueryAndParams query(FilterParams criteria) {

    SqlBuilder builder = SqlBuilder.with(fields, criteria);

    String s = builder.selectStmt();

    QueryAndParams w = builder.whereStmt();

    StringBuilder sb = new StringBuilder();

    StringBuilder queryBuilder =
        sb.append("SELECT \n")
            .append(s)
            .append(" FROM \n")
            .append(schema)
            .append(" WHERE ")
            .append(w.getSql());

    if (!criteria.isCount()) {
      queryBuilder =
          queryBuilder
              .append(" LIMIT ")
              .append(criteria.getLimit())
              .append(" OFFSET ")
              .append(criteria.getOffset());
    }

    return new QueryAndParams(queryBuilder.toString(), w.getParams());
  }

  @Override
  public Class<Result> resultClass() {
    return Result.class;
  }

  public static class Result {
    public Object name;
    public String sex;
    public BigDecimal age;
  }

  public List<Result> list(FilterParams params) {
    Zql sql = new Zql(connectionProvider);
    QueryAndParams query = query(params);
    return sql.rows(Result.class, query.getSql(), query.getParams());
  }

  public Long count(FilterParams params) {
    Zql sql = new Zql(connectionProvider);
    FilterParams filterParams = params.selectCount();
    QueryAndParams query = query(params);
    return sql.one(Long.class, query.getSql(), query.getParams());
  }

  public static List<Result> list(ConnectionProvider provider, FilterParams params) {
    TransactionQuery query = from(provider);
    return query.list(params);
  }

  public static Long count(ConnectionProvider provider, FilterParams params) {
    TransactionQuery query = from(provider);
    return query.count(params);
  }
}
