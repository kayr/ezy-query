package docs

import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.api.Sort
import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.itests.TestCanFetchDataTest
import io.github.kayr.ezyquery.sql.ColumnInfo
import io.github.kayr.ezyquery.sql.Mappers
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
        then:
        sql != null

    }

    def "filter with fluent API"() {
        when:
        //snippet:filter-with-fluent-api
        var Q = CustomerQueries.getAllCustomers()
        var sql = ezySql.from(Q)
                .where(Q.CUSTOMER_NAME.eq("John").and(Q.CUSTOMER_EMAIL.isNotNull()))
        //endsnippet
                .getQuery().getSql()
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
                .where(Q.CUSTOMER_NAME.eq("John").and(Q.CUSTOMER_EMAIL.isNotNull()))
                .orderBy(Q.CUSTOMER_NAME.asc(), Q.CUSTOMER_EMAIL.desc())
                .offset(0)
                .limit(10)
                .listAndCount()

        then://nosnippet
        assert result.getCount() > 0
        assert !result.getList().isEmpty()
        assert result.getList().get(0).getCustomerName().equals("John")
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
}
