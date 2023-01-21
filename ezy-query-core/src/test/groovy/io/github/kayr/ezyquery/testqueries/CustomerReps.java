package io.github.kayr.ezyquery.testqueries;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

/**
 * SELECT c.customerName AS customerName, e.employeeNumber AS employeeRep, o.addressLine1 AS
 * employeeOffice, o.officeCode AS employeeOfficeCode, o.country AS employeeCounty FROM offices o
 * LEFT JOIN employees e ON o.officeCode * = e.officeCode LEFT JOIN customers c ON e.employeeNumber
 * = c.salesRepEmployeeNumber
 */
@Generated(value = "io.github.kayr.ezyquery.gen.QueryGen", date = "2022-11-05T17:10:56.978")
public class CustomerReps implements EzyQuery<CustomerReps.Result> {
  public static Field<Object> FIELD_CUSTOMER_NAME =
      Field.of("c.customerName", "customerName", Object.class);

  public static Field<Object> FIELD_EMPLOYEE_REP =
      Field.of("e.employeeNumber", "employeeRep", Object.class);

  public static Field<Object> FIELD_EMPLOYEE_OFFICE =
      Field.of("o.addressLine1", "employeeOffice", Object.class);

  public static Field<Object> FIELD_EMPLOYEE_COUNTY =
      Field.of("o.country", "employeeCounty", Object.class);
  public static Field<Object> FIELD_EMPLOYEE_OFFICE_CODE =
      Field.of(" o.officeCode", "employeeOfficeCode", Object.class);

  public static final CustomerReps Q = new CustomerReps();

  private final String schema =
      "offices o\n"
          + "LEFT JOIN employees e ON o.officeCode = e.officeCode\n"
          + "LEFT JOIN customers c ON e.employeeNumber = c.salesRepEmployeeNumber\n";

  private final List<Field<?>> fields = new ArrayList<Field<?>>();

  public CustomerReps() {
    init();
  }

  private void init() {
    fields.add(FIELD_CUSTOMER_NAME);
    fields.add(FIELD_EMPLOYEE_REP);
    fields.add(FIELD_EMPLOYEE_OFFICE);
    fields.add(FIELD_EMPLOYEE_COUNTY);
    fields.add(FIELD_EMPLOYEE_OFFICE_CODE);
  }

  public QueryAndParams query(EzyCriteria criteria) {
    return SqlBuilder.buildSql(this, criteria);
  }

  @Override
  public List<Field<?>> fields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public String schema() {
    return this.schema;
  }

  @Override
  public Class<Result> resultClass() {
    return Result.class;
  }

  public static class Result {
    public Object customerName;

    public Object employeeRep;

    public Object employeeOffice;

    public Object employeeCounty;
    public Object employeeOfficeCode;

    @Override
    public String toString() {
      return "CustomerReps{"
          + "customerName = "
          + customerName
          + ", employeeRep = "
          + employeeRep
          + ", employeeOffice = "
          + employeeOffice
          + ", employeeCounty = "
          + employeeCounty
          + ", employeeOfficeCode = "
          + employeeOfficeCode
          + '}';
    }
  }
}
