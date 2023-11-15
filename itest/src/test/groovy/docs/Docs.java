package docs;

import static docs.GetCustomers.*;
import static docs.GetOrders.GET_ORDERS;
import static prod.QueryWithParams.QUERY_WITH_PARAMS;
import static test.DerivedTableQuery.DERIVED_TABLE_QUERY;

import io.github.kayr.ezyquery.EzySql;
import io.github.kayr.ezyquery.api.Sort;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.sql.ColumnInfo;
import io.github.kayr.ezyquery.sql.Mappers;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class Docs {

  public static void main(String[] args) {
    DataSource dataSource = null;

    EzySql ezySql = EzySql.withDataSource(dataSource);

    // to get a count of the results -> this generates a select count(*) query
    // assert query.count() > 0;

    // to get a list of the results
    // assert query.list().size() > 0;

    // filter by name
    ezySql
        .from(DERIVED_TABLE_QUERY)
        .where(
            GET_CUSTOMERS.CUSTOMER_NAME.eq("John").and(GET_CUSTOMERS.CUSTOMER_EMAIL.isNotNull()));

    // filter with condition api
    ezySql
        .from(GET_CUSTOMERS)
        .where(
            Cnd.and(
                GET_CUSTOMERS.CUSTOMER_NAME.eq("John"), GET_CUSTOMERS.CUSTOMER_EMAIL.isNotNull()));

    // filter with ezy-query expression
    ezySql.from(GET_CUSTOMERS).where(Cnd.sql("c.name = ? and c.created_at > now()", "John"));

    // sorting
    ezySql
        .from(GET_CUSTOMERS)
        .orderBy(GET_CUSTOMERS.CUSTOMER_NAME.asc(), GET_CUSTOMERS.CUSTOMER_EMAIL.desc());

    // sort with string
    ezySql.from(GET_CUSTOMERS).orderBy("customerName asc, customerEmail desc").getQuery().print();

    // sort with string
    ezySql
        .from(GET_CUSTOMERS)
        .orderBy(Sort.by("customerName", Sort.DIR.ASC))
        .limit(10)
        .offset(20)
        .getQuery()
        .print();

    // full query
    ezySql
        .from(GET_CUSTOMERS)
        .select(GET_CUSTOMERS.CUSTOMER_NAME, GET_CUSTOMERS.CUSTOMER_EMAIL)
        .where(GET_CUSTOMERS.CUSTOMER_NAME.eq("John").and(GET_CUSTOMERS.CUSTOMER_EMAIL.isNotNull()))
        .orderBy(GET_CUSTOMERS.CUSTOMER_NAME.asc(), GET_CUSTOMERS.CUSTOMER_EMAIL.desc())
        .limit(10)
        .offset(20);

    // full query with ezy-query expression
    ezySql
        .from(GET_CUSTOMERS)
        .select(GET_CUSTOMERS.CUSTOMER_NAME, GET_CUSTOMERS.CUSTOMER_EMAIL)
        .where(Cnd.expr("customerName = 'John' and customerEmail is not null"))
        .orderBy("customerName asc, customerEmail desc")
        .limit(10)
        .offset(20);

    // named params
    ezySql
        .from(GET_ORDERS)
        .where(GET_ORDERS.PRICE.gt(100).and(GET_ORDERS.QUANTITY.lt(10)))
        .setParam(GetOrders.PARAMS.MEMBERSHIP, "GOLD")
        .getQuery()
        .print();

    // custom mappers
    ezySql
        .from(GET_ORDERS)
        .setParam(GetOrders.PARAMS.MEMBERSHIP, "GOLD")
        .mapTo(
            Mappers.toObject(HashMap::new, (column, result, o) -> result.put(column.getLabel(), o)))
        .list();

    ezySql
        .from(QUERY_WITH_PARAMS)
        .mapTo(
            (rowIndex, columns, rs) -> {
              Map<String, Object> map = new HashMap<>();
              for (ColumnInfo column : columns) {
                map.put(column.getLabel(), rs.getObject(column.getLabel()));
              }
              return map;
            })
        .list();
  }
}
