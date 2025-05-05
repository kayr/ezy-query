package io.github.kayr.ezyquery.itests

import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.api.Sort
import io.github.kayr.ezyquery.sql.Mappers
import prod.Queries
import prod.QueryWithParams
import spock.lang.Specification
import test.DerivedTableQuery
import test.DerivedTableQueryCte

import javax.swing.tree.RowMapper

import static prod.ProdQuery1.PROD_QUERY1
import static prod.QueryWithParams.QUERY_WITH_PARAMS
import static test.QueryWithDaultOrderBy.QUERY_WITH_DAULT_ORDER_BY
import static test.TestQuery1.TEST_QUERY1

class TestCanFetchDataTest extends Specification {

    Db db
    EzySql ez;

    def setup() {

        db = new Db()

        def offices = [
                [officeCode: '1', country: 'UG', addressLine1: 'Kampala'],
                [officeCode: '2', country: 'KE', addressLine1: 'Nairobi'],
                [officeCode: '3', country: 'TZ', addressLine1: 'Dar es Salaam'],
                [officeCode: '4', country: 'KE', addressLine1: 'Nairobi'],
        ]

        def employees = [
                [employeeNumber: '1', officeCode: '1', firstName: 'Kay'],
                [employeeNumber: '2', officeCode: '2', firstName: 'John'],
                [employeeNumber: '3', officeCode: '2', firstName: 'Jane'],
                [employeeNumber: '4', officeCode: '3', firstName: 'Doe']
        ]

        def customers = [
                [customerNumber: '1', customerName: 'Kay', salesRepEmployeeNumber: '1'],
                [customerNumber: '2', customerName: 'John', salesRepEmployeeNumber: '1'],
                [customerNumber: '3', customerName: 'Jane', salesRepEmployeeNumber: '1'],
                [customerNumber: '4', customerName: 'Doe', salesRepEmployeeNumber: '2'],
                [customerNumber: '5', customerName: 'Daniel', salesRepEmployeeNumber: '2']

        ]

        def orders = [
                [orderNumber: '1', customerNumber: '1', item: 'item1', quantity: 1, price: 100],
                [orderNumber: '2', customerNumber: '1', item: 'item2', quantity: 2, price: 200],
                [orderNumber: '3', customerNumber: '1', item: 'item3', quantity: 3, price: 300],
                [orderNumber: '4', customerNumber: '2', item: 'item4', quantity: 4, price: 400],
                [orderNumber: '5', customerNumber: '2', item: 'item5', quantity: 5, price: 500],
                [orderNumber: '6', customerNumber: '3', item: 'item6', quantity: 6, price: 600],
                [orderNumber: '7', customerNumber: '3', item: 'item7', quantity: 7, price: 700],
                [orderNumber: '8', customerNumber: '4', item: 'item8', quantity: 8, price: 800],
                [orderNumber: '9', customerNumber: '4', item: 'item9', quantity: 9, price: 900],
                [orderNumber: '10', customerNumber: '5', item: 'item10', quantity: 10, price: 1000],
        ]

        db.intoDb(offices, "offices")
        db.intoDb(employees, "employees")
        db.intoDb(customers, "customers")
        db.intoDb(orders, "orders")

        ez = EzySql.withDataSource(db.ds)
    }

    def cleanup() {
        db.close()
    }

    def 'test that can fetch data with generated queries'() {
        given:


        def criteria = ez.from(TEST_QUERY1)
                .orderBy(Sort.by(TEST_QUERY1.OFFICE_CODE, Sort.DIR.ASC))
                .limit(3)


        when:
        def r = criteria.listAndCount()
        def count = criteria.count()
        def list = criteria.list()

        then:
        r.list.size() == 3
        r.list.every { it.officeCode != null }
        r.list.every { it.country != null }
        r.list.every { it.addressLine != null }
        r.count == 4

        count == 4
        list.size() == 3
        list.every { it.officeCode != null }
        list.every { it.country != null }
        list.every { it.addressLine != null }

    }

    def 'test that can fetch data with fields correctly set'() {
        given:
        def criteria = ez.from(TEST_QUERY1)
                .where(TEST_QUERY1.OFFICE_CODE.eq("1"))

        when:
        def r = criteria.one()
        def count = criteria.count()

        then:
        r.officeCode == "1"
        r.country == "UG"
        r.addressLine == "Kampala"
        count == 1
    }

    def 'test that can fetch data using new ez.sql'() {
        given:
        def criteria = Queries.selectOffices()
        .officeCode("1").offset(0).max(50).query

        when:
        def r = ez.zql.oneRow(Mappers.toMap(),criteria)


        then:
        r.officeCode == "1"
        r.country == "UG"
        r.addressLine == "Kampala"
    }

    def 'test that can fetch data using new ez.sql but with defaults'() {
        given:
        def criteria = Queries.selectOffices2()
        .officeCode("1").offset(0).max(50).query

        when:
        def r = ez.zql.oneRow(Mappers.toMap(),criteria)


        then:
        r.officeCode == "1"
        r.country == "UG"
        r.addressLine == "Kampala"
    }


    def 'test that can fetch data using new ez.sql for dynamic embedded'() {
        def q = Queries.selectOfficesDynamic()

        given:
        def criteria = ez.from(q).where(q.OFFICE_CODE.eq(1))

        when:
        def r = criteria.list()


        then:
        r.size() == 1
        r.first().officeCode == "1"
        r.first().country == "UG"
        r.first().addressLine == "Kampala"
    }

    def 'test  retrieve data with named param'() {
        given:
        def c = ez.from(QUERY_WITH_PARAMS)
                .setParam(QueryWithParams.PARAMS.FIRST_NAME, ["Kay"])

        c.query.print()


        when:
        def r = c.one()
        def count = c.count()

        then:
        r.addressLine == "Kampala"
        r.country == "UG"
        count == 1
    }

    def 'test that prod query is compiled and usable'() {
        given:
        def criteria = ez.from(PROD_QUERY1)
                .where(PROD_QUERY1.ADDRESS_LINE.eq("Kampala"))


        when:
        def list = criteria.list()
        def count = criteria.count()

        then:
        list.size() == 1
        list.every { it.addressLine == "Kampala" }
        count == 1
    }

    def 'test query with default order by'() {
        given:
        def criteria = ez.from(QUERY_WITH_DAULT_ORDER_BY)
                .select(QUERY_WITH_DAULT_ORDER_BY.OFFICE_CODE)

        when:
        def list = criteria.list()
        def count = criteria.count()


        then:
        list*.officeCode == ['1', '2', '3', '4'].reverse()
        count == 4

    }

    def "test derived table"() {

        def p = DerivedTableQuery.PARAMS
        def c = DerivedTableQuery.CRITERIA
        def t = DerivedTableQuery.DERIVED_TABLE_QUERY

        when:
        def list = ez.from(t)
                .setCriteria(c.CUSTOMERS, c.CUSTOMERS.CUSTOMER_NAME.in("John", "Daniel"))
                .setParam(p.CUSTOMER_IDS, ["1", "2", "3", "4", "5"])
                .list()
        then:
        list.size() == 3
        list*.customerName == ["John", "John", "Daniel"]
        list*.item == ["item4", "item5", "item10"]
        list*.price == [400, 500, 1000]
        list*.quantity == [4, 5, 10]


    }

    def "test derived table with cte"() {

        def p = DerivedTableQueryCte.PARAMS
        def c = DerivedTableQueryCte.CRITERIA
        def t = DerivedTableQueryCte.DERIVED_TABLE_QUERY_CTE

        when:
        def list = ez.from(t)
                .setCriteria(c.CUSTOMERS, c.CUSTOMERS.CUSTOMER_NAME.in("John", "Daniel"))
                .setParam(p.CUSTOMER_IDS, ["1", "2", "3", "4", "5"])
                .list()
        then:
        list.size() == 3
        list*.customerName == ["John", "John", "Daniel"]
        list*.item == ["item4", "item5", "item10"]
        list*.price == [400, 500, 1000]
        list*.quantity == [4, 5, 10]


    }

    def "xxxxx"() {

        ez.from(Queries.selectOrders())
        .setCriteria(Queries.selectOrders().CUSTOMER_ID, Queries.selectOrders().CUSTOMER_ID.in("1", "2", "3", "4", "5"))
        .setParam(Queries.selectOrders().CUSTOMER_ID, ["1", "2", "3", "4", "5"])


    }
}
