package io.github.kayr.ezyquery.itests


import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.api.Sort
import prod.ProdQuery1
import spock.lang.Specification
import test.QueryWithDaultOrderBy
import test.TestQuery1

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

        db.intoDb(offices, "offices")
        db.intoDb(employees, "employees")
        db.intoDb(customers, "customers")

        ez = EzySql.withDataSource(db.ds)
    }

    def cleanup() {
        db.close()
    }

    def 'test that can fetch data with generated queries'() {
        given:


        def criteria = ez.from(TestQuery1.QUERY)
                .orderBy(Sort.by(TestQuery1.OFFICE_CODE, Sort.DIR.ASC))
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
        def criteria = ez.from(TestQuery1.QUERY)
                .where(TestQuery1.OFFICE_CODE.eq("1"))

        when:
        def r = criteria.one()
        def count = criteria.count()

        then:
        r.officeCode == "1"
        r.country == "UG"
        r.addressLine == "Kampala"
        count == 1
    }

    def 'test that prod query is compiled and usable'() {
        given:
        def criteria = ez.from(ProdQuery1.QUERY)
                .where(ProdQuery1.ADDRESS_LINE.eq("Kampala"))

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
        def criteria = ez.from(QueryWithDaultOrderBy.QUERY)
        .select(QueryWithDaultOrderBy.OFFICE_CODE)

        when:
        def list = criteria.list()
        def count = criteria.count()


        then:
        list*.officeCode == ['1', '2', '3', '4'].reverse()

    }
}
