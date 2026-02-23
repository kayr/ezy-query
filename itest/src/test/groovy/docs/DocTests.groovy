package docs

import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.api.Sort
import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.itests.TestCanFetchDataTest
import io.github.kayr.ezyquery.parser.SqlParts
import io.github.kayr.ezyquery.sql.ColumnInfo
import io.github.kayr.ezyquery.sql.Mappers
import io.github.kayr.ezyquery.sql.Zql
import org.h2.result.Row
import spock.lang.Specification

class DocSpec extends Specification {

    private EzySql ezySql
    //cause I am lazy :)
    TestCanFetchDataTest test

    def setup() {
        test = new TestCanFetchDataTest()
        test.setup()
        ezySql = test.ez
    }

    def cleanup() {
        test.cleanup()
    }

    def "filter with condition API"() {
        when:
        //snippet:filter-with-condition-api
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .where(Cnd.and(Cnd.eq(Q.CUSTOMER_NAME, "John"), Cnd.isNotNull(Q.CUSTOMER_EMAIL)))
        //endsnippet
                .getQuery().getSql()
                .with {
                    genSqlDoc(it,"filter-with-condition-api")
                }
        then:
        sql != null
        println sql

    }


    def genSqlDoc(String doc, snippetName) {

        def baseFolder = new File(".", "build/generated/sql-snippets/")
        baseFolder.mkdirs()
        def finalFile = new File(baseFolder, snippetName + ".sql")
        finalFile.text = """
-- snippet:$snippetName-out
$doc
-- endsnippet
"""

    }

    def "filter with fluent API"() {
        when:
        //snippet:filter-with-fluent-api
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .where(Q.CUSTOMER_NAME.eq("John").and(Q.CUSTOMER_EMAIL.isNotNull()))
        //endsnippet
                .getQuery().getSql()
                .with {
                    genSqlDoc(it,'filter-with-fluent-api')
                }
        then:
        sql != null

    }

    def "full query with EzyQuery expression"() {
        when:
        //snippet:filter-with-ezy-query-expressions
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .where(Cnd.expr("customerName = 'John' and customerEmail is not null"))
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }

    def "filter with raw sql expression"() {
        when:
        //snippet:filter-with-raw-sql
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .where(Cnd.sql("c.customerName = ? AND c.email IS NOT NULL", "John"))
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }

    def "sorting"() {
        when:
        //snippet:sort-fluent
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .orderBy(Q.CUSTOMER_NAME.asc(), Q.CUSTOMER_EMAIL.desc())
                .limit(10)
                .offset(20)
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }

    def "sort with string"() {
        when:
        //snippet:sort-with-string
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .orderBy("customerName asc, customerEmail desc")
                .limit(10, 20) //another alternative for pagination
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }

    def "sort and pagination"() {
        when:
        //snippet:sort-with-sort-object
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .orderBy(Sort.by("customerName", Sort.DIR.ASC))
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }

    def "full query"() {
        when:
        //snippet:how-to-use
        var Q = CustomerQueries.getAllCustomers()
        var result = ezySql.from(Q)
                .where(Q.CUSTOMER_NAME.eq("john").and(Q.CUSTOMER_EMAIL.isNotNull()))
                .orderBy(Q.CUSTOMER_NAME.asc(), Q.CUSTOMER_EMAIL.desc())
                .offset(0)
                .limit(10)
                .listAndCount()

        then://nosnippet
        assert result.getCount() > 0
        assert !result.getList().isEmpty()
        assert result.getList().get(0).getCustomerName().equals("john")
        //endsnippet
    }

    def "optional select field"() {
        when:
        //snippet:optional-select-field
        var Q = CustomerQueries.getAllCustomers()
        var result = ezySql.from(Q)
                .select(Q.CUSTOMER_NAME, Q.CUSTOMER_EMAIL)
                .list()

        then://nosnippet
        assert result.size() > 0
        assert result.get(0).customerName != null
        assert result.get(0).customerId == null//we did not select this.. so it will be null
        //endsnippet
    }

    def "query from map"() {
        when:
        //snippet:filter-with-map
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .where(Cnd.fromMap(
                        Map.of("customerName.eq", "John",
                                "customerEmail.isnotnull", Optional.empty())))//use optional empty or null to show that we have no value here
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }

    def "query from multi value map"() {
        when:
        //snippet:filter-with-mv-map
        var Q = CustomerQueries.getAllCustomers()

        def filterMap = new HashMap<String, List<?>>()
        filterMap.put("customerName.eq", List.of("John"))
        filterMap.put("customerEmail.isnotnull", Collections.emptyList())//empty list to show we have no values here

        var sql = ezySql.from(Q)
                .where(Cnd.fromMvMap(filterMap))
        //endsnippet
                .getQuery().getSql()

        then:
        sql != null
        println sql
    }


    def "named params"() {
        when:
        //snippet:named-params
        var Q = CustomerQueries.getOrders()
        var P = CustomerQueries.GetOrders.PARAMS
        var sql = ezySql.from(Q)
                .where(Q.PRICE.gt(100).and(Q.QUANTITY.lt(10)))
                .setParam(P.MEMBERSHIP, "GOLD")
        //endsnippet
                .getQuery().print()

        then:
        sql != null
        println sql
    }

    def 'specifying a custom mappet'() {
        when:
        //snippet:mapper-to-map
        var Q = CustomerQueries.getAllCustomers()
        List<Map> result = ezySql.from(Q)
                .mapTo(Mappers.toMap())
                .list()
        //endsnippet
        then:
        result.size() > 0
    }

    def 'specifying a custom mapper'() {
        when:
        //snippet:mapper-to-map-custom
        var Q = CustomerQueries.getAllCustomers()
        List<Map> result = ezySql.from(Q)
                .mapTo((rowIndex, columns, resultSet) -> {
                    Map<String, Object> map = new HashMap<>();
                    for (ColumnInfo column : columns) {
                        map.put(column.getLabel(), resultSet.getObject(column.getLabel()));
                    }
                    return map;
                })
                .list();
        //endsnippet
        then:
        result.size() > 0
    }

    def 'running other types of query'() {
        //snippet:running-other-queries
        /*//nosnippet
        //set up the datasource
        DataSource ds = new HikariDataSource(config)
        //create the Zql instance which is a convenient api around jdbc
        var zql = new Zql(ConnectionProvider.of(ds)
        //var zql = ezySql.getZql() // or you can get it from EzySql instance
         *///nosnippet
        var zql = ezySql.getZql() //nosnippet

        when://nosnippet
        var Q = CustomerQueries.updateCustomer()

        var updateCount = zql.update(
                Q.email("john@example.com") //these functions are generated from the query param in the sql query
                        .score(10)
                        .getQuery())
        then://nosnippet
        assert updateCount > 0

        //endsnippet
    }

    def 'batch with raw sql'() {
        given:
        var zql = ezySql.getZql() //nosnippet

        when://nosnippet
        //snippet:batch-raw-sql
        var results = zql.batch("UPDATE customers SET score = ? WHERE email = ?", List.of(
                List.of(100, "john@example.com"),
                List.of(200, "jane@example.com")
        ))
        //endsnippet

        then:
        results.every { it == 1 }
    }

    def 'batch with query objects'() {
        given:
        var zql = ezySql.getZql() //nosnippet

        when://nosnippet
        //snippet:batch-query-objects
        var Q = CustomerQueries.updateCustomer()
        var results = zql.batch(List.of(
                Q.email("john@example.com").score(100).getQuery(),
                Q.email("jane@example.com").score(200).getQuery()
        ))
        //endsnippet

        then:
        results.every { it == 1 }
    }

    def 'batch insert with generated keys'() {
        given:
        var zql = ezySql.getZql() //nosnippet
        zql.execute("CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100))", []) //nosnippet

        when://nosnippet
        //snippet:batch-insert-keys
        List<Object> keys = zql.batchInsert(
                "INSERT INTO products (name) VALUES (?)", List.of(
                        List.of("Widget"),
                        List.of("Gadget")
                ))
        //endsnippet

        then:
        keys.size() == 2
    }

    def 'batch insert with query objects'() {
        given:
        var zql = ezySql.getZql() //nosnippet
        zql.execute("CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100))", []) //nosnippet

        when://nosnippet
        //snippet:batch-insert-query-objects
        var Q = CustomerQueries.insertProduct()
        List<Object> keys = zql.batchInsert(List.of(
                Q.name("Widget").getQuery(),
                Q.name("Gadget").getQuery()
        ))
        //endsnippet

        then:
        keys.size() == 2
    }
}
