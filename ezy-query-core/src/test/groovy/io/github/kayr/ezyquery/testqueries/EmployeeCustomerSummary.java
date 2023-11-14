package io.github.kayr.ezyquery.testqueries;

import io.github.kayr.ezyquery.EzyQueryWithResult;
import io.github.kayr.ezyquery.api.*;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.parser.SqlParts;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SELECT e.employeeNumber as "employeeNumber", e.firstName as "firstName", o.officeCode as
 * "officeCode", o.country as "country", o.addressLine1 as "addressLine1", c.totalCustomers as
 * "totalCustomers" FROM ( SELECT salesRepEmployeeNumber, COUNT(*) AS totalCustomers FROM customers
 * WHERE :_ezy_customerSummary GROUP BY salesRepEmployeeNumber ) AS c JOIN employees e ON
 * c.salesRepEmployeeNumber = e.employeeNumber JOIN offices o ON e.officeCode = o.officeCode;
 */
// @Generated(value = "io.github.kayr.ezyquery.gen.QueryGen", date = "0000-00-00 00:00:00")
public class EmployeeCustomerSummary implements EzyQueryWithResult<EmployeeCustomerSummary.Result> {
  public final Field<Object> EMPLOYEE_NUMBER =
      Field.of("e.employeeNumber", "employeeNumber", Object.class, Field.ExpressionType.COLUMN);

  public final Field<Object> FIRST_NAME =
      Field.of("e.firstName", "firstName", Object.class, Field.ExpressionType.COLUMN);

  public final Field<Object> OFFICE_CODE =
      Field.of("o.officeCode", "officeCode", Object.class, Field.ExpressionType.COLUMN);

  public final Field<Object> COUNTRY =
      Field.of("o.country", "country", Object.class, Field.ExpressionType.COLUMN);

  public final Field<Object> ADDRESS_LINE1 =
      Field.of("o.addressLine1", "addressLine1", Object.class, Field.ExpressionType.COLUMN);

  public final Field<Object> TOTAL_CUSTOMERS =
      Field.of("c.totalCustomers", "totalCustomers", Object.class, Field.ExpressionType.COLUMN);

  public static final EmployeeCustomerSummary QUERY = new EmployeeCustomerSummary();

  public static final CustomerSummary CUSTOMER_SUMMARY = new CustomerSummary();

  private final SqlParts schema =
      SqlParts.of(
          SqlParts.textPart(
              "(SELECT salesRepEmployeeNumber, COUNT(*) AS totalCustomers FROM customers WHERE "),
          SqlParts.paramPart("_ezy_customerSummary"),
          SqlParts.textPart(
              " GROUP BY salesRepEmployeeNumber) AS c\n"
                  + "JOIN employees e ON c.salesRepEmployeeNumber = e.employeeNumber\n"
                  + "JOIN offices o ON e.officeCode = o.officeCode"));

  // beginregion Experiemental
  public Field<Object> getEmployeeNumber() {
    return EMPLOYEE_NUMBER;
  }

  public Field<Object> firstName() {
    return FIRST_NAME;
  }

  public Field<Object> officeCode() {
    return OFFICE_CODE;
  }

  public Field<Object> country() {
    return COUNTRY;
  }

  public Field<Object> addressLine1() {
    return ADDRESS_LINE1;
  }

  // endregion

  private final List<Field<?>> fields = new ArrayList<Field<?>>();

  public EmployeeCustomerSummary() {
    init();
  }

  private void init() {
    fields.add(EMPLOYEE_NUMBER);
    fields.add(FIRST_NAME);
    fields.add(OFFICE_CODE);
    fields.add(COUNTRY);
    fields.add(ADDRESS_LINE1);
    fields.add(TOTAL_CUSTOMERS);
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
    private Object employeeNumber;

    private Object firstName;

    private Object officeCode;

    private Object country;

    private Object addressLine1;

    private Object totalCustomers;

    public Object getEmployeeNumber() {
      return employeeNumber;
    }

    public Object getFirstName() {
      return firstName;
    }

    public Object getOfficeCode() {
      return officeCode;
    }

    public Object getCountry() {
      return country;
    }

    public Object getAddressLine1() {
      return addressLine1;
    }

    public Object getTotalCustomers() {
      return totalCustomers;
    }

    @Override
    public String toString() {
      return "MyQuery.Result{"
          + "employeeNumber = "
          + employeeNumber
          + ", firstName = "
          + firstName
          + ", officeCode = "
          + officeCode
          + ", country = "
          + country
          + ", addressLine1 = "
          + addressLine1
          + ", totalCustomers = "
          + totalCustomers
          + "}";
    }
  }

  public static class Params {
    public static final NamedParam _EZY_CUSTOMER_SUMMARY = NamedParam.of("_ezy_customerSummary");
  }

  public static class CustomerSummary implements CriteriaName {

    public final Field<Object> SALES_REP_EMPLOYEE_NUMBER =
        Field.of(
            "salesRepEmployeeNumber",
            "salesRepEmployeeNumber",
            Object.class,
            Field.ExpressionType.COLUMN);

    public final Field<Object> TOTAL_CUSTOMERS =
        Field.of("some_function()", "totalCustomers", Object.class, Field.ExpressionType.COLUMN);

    private final List<Field<?>> fields = new ArrayList<>();

    public CustomerSummary() {
      init();
    }

    private void init() {
      fields.add(SALES_REP_EMPLOYEE_NUMBER);
      fields.add(TOTAL_CUSTOMERS);
    }

    @Override
    public NamedCriteriaParam getName() {
      return NamedCriteriaParam.of("_ezy_customerSummary", fields);
    }
  }
}
