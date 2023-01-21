package io.github.kayr.ezyquery.testqueries;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * -- [officeCode: '1', country: 'UG', addressLine1: 'Kampala'], select officeCode as "code",
 * country as "country", addressLine1 as "addressLine" from offices
 */
//@Generated(value = "io.github.kayr.ezyquery.gen.QueryGen", date = "2023-01-21T05:44:24.716395")
public class Offices implements EzyQuery<Offices.Result> {
  public static Field<Object> CODE = Field.of("officeCode", "code", Object.class);

  public static Field<Object> COUNTRY = Field.of("country", "country", Object.class);

  public static Field<Object> ADDRESS_LINE = Field.of("addressLine1", "addressLine", Object.class);

  public static final Offices QUERY = new Offices();

  private final String schema = "offices";

  private final List<Field<?>> fields = new ArrayList<Field<?>>();

  public Offices() {
    init();
  }

  private void init() {
    fields.add(CODE);
    fields.add(COUNTRY);
    fields.add(ADDRESS_LINE);
  }

  public QueryAndParams query(EzyCriteria criteria) {
    return SqlBuilder.buildSql(this, criteria);
  }

  @Override
  public String schema() {
    return this.schema;
  }

  @Override
  public Optional<String> whereClause() {
    return Optional.empty();
  }

  @Override
  public Optional<String> orderByClause() {
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

    private Object country;

    private Object addressLine;

    public Object getCode() {
      return code;
    }

    public Object getCountry() {
      return country;
    }

    public Object getAddressLine() {
      return addressLine;
    }

    @Override
    public String toString() {
      return "OfficesQuery.Result{"
          + "code = "
          + code
          + ", country = "
          + country
          + ", addressLine = "
          + addressLine
          + "}";
    }
  }
}
