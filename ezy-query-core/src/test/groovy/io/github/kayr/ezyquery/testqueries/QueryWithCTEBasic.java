package io.github.kayr.ezyquery.testqueries;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.NamedParam;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.parser.SqlParts;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/*
WITH "SalesRepInfo" AS (
    SELECT
        "e"."employeeNumber",
        "e"."firstName" AS "salesRepName",
        "o"."country" AS "salesRepCountry"
    FROM
        "Employees" "e"
    JOIN "Offices" "o" ON "e"."officeCode" = "o"."officeCode"
    WHERE "e"."jobTitle" = :jobTitle
)
SELECT
    "c"."customerNumber" as "customerNumber",
    "c"."customerName" as "customerName",
    "s"."salesRepName" as "salesRepName",
    "s"."salesRepCountry" as "salesRepCountry"
FROM
    "Customers" "c"
JOIN "SalesRepInfo" "s" ON "c"."salesRepEmployeeNumber" = "s"."employeeNumber";


 */
public class QueryWithCTEBasic implements EzyQuery {

  public static Field<Object> CUSTOMER_NUMBER =
      Field.of("\"c\".\"customerNumber\"", "customerNumber", Object.class);

  public static Field<Object> CUSTOMER_NAME =
      Field.of("\"c\".\"customerName\"", "customerName", Object.class);

  public static Field<Object> SALES_REP_NAME =
      Field.of("\"s\".\"salesRepName\"", "salesRepName", Object.class);

  public static Field<Object> SALES_REP_COUNTRY =
      Field.of("\"s\".\"salesRepCountry\"", "salesRepCountry", Object.class);

  @Override
  public QueryAndParams query(EzyCriteria params) {
    return SqlBuilder.buildSql(this, params);
  }

  @Override
  public List<Field<?>> fields() {
    return Arrays.asList(CUSTOMER_NUMBER, CUSTOMER_NAME, SALES_REP_NAME, SALES_REP_COUNTRY);
  }

  @Override
  public Optional<SqlParts> preQuery() {
    return Optional.of(
        SqlParts.of(
            SqlParts.textPart(
                "WITH \"SalesRepInfo\" AS (\n"
                    + "    SELECT\n"
                    + "        \"e\".\"employeeNumber\",\n"
                    + "        \"e\".\"firstName\" AS \"salesRepName\",\n"
                    + "        \"o\".\"country\" AS \"salesRepCountry\"\n"
                    + "    FROM\n"
                    + "        \"Employees\" \"e\"\n"
                    + "    JOIN \"Offices\" \"o\" ON \"e\".\"officeCode\" = \"o\".\"officeCode\"\n"
                    + "    WHERE \"e\".\"jobTitle\" = "),
            SqlParts.paramPart("jobTitle"),
            SqlParts.textPart(")")));
  }

  @Override
  public SqlParts schema() {
    return SqlParts.of(
        "Customers c\n"
            + "JOIN \"SalesRepInfo\" \"s\" ON \"c\".\"salesRepEmployeeNumber\" = \"s\".\"employeeNumber\"");
  }

  public interface Params {
    static NamedParam JOB_TITLE = NamedParam.of("jobTitle");
  }
}
