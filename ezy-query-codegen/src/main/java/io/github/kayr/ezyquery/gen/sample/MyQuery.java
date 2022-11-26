package io.github.kayr.ezyquery.gen.sample;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Generated;

/**
 * SELECT td.id AS 'id_bigint', td.long_ AS 'longval_long', td.int_ AS 'intval_int', tr.float_ AS
 * 'floatval_float', maw.double_ AS 'doubleval_double', mtw.string_ AS 'stringval_string',
 * mtw.boolean_ AS 'booleanval_boolean', tr.date_ AS 'dateval_date', tr.time AS 'timeval_time',
 * tr.bigint_ AS 'bigintval_bigint', tr.bigdecimal_ AS 'bigdecimalval_decimal', tr.blob_ AS
 * 'blobval_byte', tr.object_ AS 'objectval_object' FROM m_awamo_wallet maw INNER JOIN
 * m_tenant_wallet mtw ON mtw.id = maw.wallet_id INNER JOIN w_wallet_transaction_request tr ON
 * tr.wallet_id = mtw.id INNER JOIN m_wallet_transaction_detail td ON td.wallet_tx_request_id =
 * tr.id where 1 <> 8
 */
@Generated(value = "io.github.kayr.ezyquery.gen.QueryGen", date = "2022-11-05T17:10:56.804")
public class MyQuery implements EzyQuery<MyQuery.Result> {
  public static Field<BigInteger> FIELD_ID = Field.of("td.id", "id", BigInteger.class);

  public static Field<Long> FIELD_LONGVAL = Field.of("td.long_", "longval", Long.class);

  public static Field<Integer> FIELD_INTVAL = Field.of("td.int_", "intval", Integer.class);

  public static Field<Float> FIELD_FLOATVAL = Field.of("tr.float_", "floatval", Float.class);

  public static Field<Double> FIELD_DOUBLEVAL = Field.of("maw.double_", "doubleval", Double.class);

  public static Field<String> FIELD_STRINGVAL = Field.of("mtw.string_", "stringval", String.class);

  public static Field<Boolean> FIELD_BOOLEANVAL =
      Field.of("mtw.boolean_", "booleanval", Boolean.class);

  public static Field<Date> FIELD_DATEVAL = Field.of("tr.date_", "dateval", Date.class);

  public static Field<Timestamp> FIELD_TIMEVAL = Field.of("tr.time", "timeval", Timestamp.class);

  public static Field<BigInteger> FIELD_BIGINTVAL =
      Field.of("tr.bigint_", "bigintval", BigInteger.class);

  public static Field<BigDecimal> FIELD_BIGDECIMALVAL =
      Field.of("tr.bigdecimal_", "bigdecimalval", BigDecimal.class);

  public static Field<Byte> FIELD_BLOBVAL = Field.of("tr.blob_", "blobval", Byte.class);

  public static Field<Object> FIELD_OBJECTVAL = Field.of("tr.object_", "objectval", Object.class);

  public static final MyQuery Q = new MyQuery();

  private final String schema =
      "m_awamo_wallet maw\n"
          + "INNER JOIN m_tenant_wallet mtw ON mtw.id = maw.wallet_id\n"
          + "INNER JOIN w_wallet_transaction_request tr ON tr.wallet_id = mtw.id\n"
          + "INNER JOIN m_wallet_transaction_detail td ON td.wallet_tx_request_id = tr.id\n";

  private final List<Field<?>> fields = new ArrayList<Field<?>>();

  public MyQuery() {
    init();
  }

  private void init() {
    fields.add(FIELD_ID);
    fields.add(FIELD_LONGVAL);
    fields.add(FIELD_INTVAL);
    fields.add(FIELD_FLOATVAL);
    fields.add(FIELD_DOUBLEVAL);
    fields.add(FIELD_STRINGVAL);
    fields.add(FIELD_BOOLEANVAL);
    fields.add(FIELD_DATEVAL);
    fields.add(FIELD_TIMEVAL);
    fields.add(FIELD_BIGINTVAL);
    fields.add(FIELD_BIGDECIMALVAL);
    fields.add(FIELD_BLOBVAL);
    fields.add(FIELD_OBJECTVAL);
  }

  public QueryAndParams query(FilterParams criteria) {
    return EzyQuery.buildQueryAndParams(criteria, fields, schema);
  }

  @Override
  public List<Field<?>> fields() {
    return Collections.emptyList();
  }

  @Override
  public Class<Result> resultClass() {
    return Result.class;
  }

  public static class Result {
    public BigInteger id;

    public Long longval;

    public Integer intval;

    public Float floatval;

    public Double doubleval;

    public String stringval;

    public Boolean booleanval;

    public Date dateval;

    public Timestamp timeval;

    public BigInteger bigintval;

    public BigDecimal bigdecimalval;

    public Byte blobval;

    public Object objectval;

    @Override
    public String toString() {
      return "MyQuery{"
          + "id = "
          + id
          + ", longval = "
          + longval
          + ", intval = "
          + intval
          + ", floatval = "
          + floatval
          + ", doubleval = "
          + doubleval
          + ", stringval = "
          + stringval
          + ", booleanval = "
          + booleanval
          + ", dateval = "
          + dateval
          + ", timeval = "
          + timeval
          + ", bigintval = "
          + bigintval
          + ", bigdecimalval = "
          + bigdecimalval
          + ", blobval = "
          + blobval
          + ", objectval = "
          + objectval
          + "}";
    }
  }
}
