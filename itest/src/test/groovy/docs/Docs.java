package docs;

import static docs.GetCustomers.CUSTOMER_EMAIL;
import static docs.GetCustomers.CUSTOMER_NAME;

import io.github.kayr.ezyquery.EzySql;
import io.github.kayr.ezyquery.api.Sort;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import javax.sql.DataSource;

public class Docs {

  public static void main(String[] args) {
    DataSource dataSource = null;

    EzySql ezySql = EzySql.withDataSource(dataSource);

    var query = ezySql.from(GetCustomers.QUERY);

    // to get a count of the results -> this generates a select count(*) query
    // assert query.count() > 0;

    // to get a list of the results
    // assert query.list().size() > 0;

    // filter by name
    ezySql.from(GetCustomers.QUERY).where(CUSTOMER_NAME.eq("John").and(CUSTOMER_EMAIL.isNotNull()));

    // filter with condition api
    ezySql
        .from(GetCustomers.QUERY)
        .where(Cnd.and(CUSTOMER_NAME.eq("John"), CUSTOMER_EMAIL.isNotNull()));

    // filter with ezy-query expression
    ezySql.from(GetCustomers.QUERY).where(Cnd.sql("c.name = ? and c.created_at > now()", "John"));

    // sorting
    ezySql.from(GetCustomers.QUERY).orderBy(CUSTOMER_NAME.asc(), CUSTOMER_EMAIL.desc());

    // sort with string
    ezySql
        .from(GetCustomers.QUERY)
        .orderBy("customerName asc, customerEmail desc")
        .getQuery()
        .print();

    // sort with string
    ezySql
        .from(GetCustomers.QUERY)
        .orderBy(Sort.by("customerName", Sort.DIR.ASC))
        .limit(10)
        .offset(20)
        .getQuery()
        .print();

    // full query
    ezySql
        .from(GetCustomers.QUERY)
        .select(CUSTOMER_NAME, CUSTOMER_EMAIL)
        .where(CUSTOMER_NAME.eq("John").and(CUSTOMER_EMAIL.isNotNull()))
        .orderBy(CUSTOMER_NAME.asc(), CUSTOMER_EMAIL.desc())
        .limit(10)
        .offset(20);

    // full query with ezy-query expression
    ezySql
        .from(GetCustomers.QUERY)
        .select(CUSTOMER_NAME, CUSTOMER_EMAIL)
        .where(Cnd.expr("customerName = 'John' and customerEmail is not null"))
        .orderBy("customerName asc, customerEmail desc")
        .limit(10)
        .offset(20);
  }
}
