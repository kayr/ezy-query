package io.github.kayr.ezyquery.itests

import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.api.cnd.Cnd
import spock.lang.Specification
import test.DynTableQuery

import static test.DynTableQuery.*

class TestDynTableTest extends Specification {

    Db db
    EzySql ez

    def setup() {
        db = new Db()

        def offices = [
                [officeCode: '1', country: 'UG', addressLine1: 'Kampala'],
                [officeCode: '2', country: 'KE', addressLine1: 'Nairobi'],
                [officeCode: '3', country: 'TZ', addressLine1: 'Dar es Salaam'],
        ]

        def employees = [
                [employeeNumber: '1', officeCode: '1', firstName: 'Kay'],
                [employeeNumber: '2', officeCode: '2', firstName: 'John'],
                [employeeNumber: '3', officeCode: '2', firstName: 'Jane'],
                [employeeNumber: '4', officeCode: '3', firstName: 'Doe'],
                [employeeNumber: '5', officeCode: '1', firstName: 'Alice'],
        ]

        db.intoDb(offices, "offices")
        db.intoDb(employees, "employees")

        ez = EzySql.withDataSource(db.ds)
    }

    def cleanup() {
        db.close()
    }

    def 'test dynamic table names with Cnd.raw'() {
        given:
        //snippet:dyn-table-usage
        var Q = selectOfficesDynamic()
        var P = SelectOfficesDynamic.PARAMS
        when://nosnippet
        def results = ez.from(Q)
                .setParam(P.DYN_O, Cnd.raw("offices"))
                .setParam(P.DYN_E, Cnd.raw("employees"))
                .setParam(P.COUNTRY_FILTER, "UG")
                .list()
        //endsnippet

        then:
        results.size() == 2
        results*.employeeName.sort() == ['Alice', 'Kay']
        results.every { it.country == 'UG' }
        results.every { it.officeCode == '1' }
    }

    def 'test dynamic table names with different country filter'() {
        given:
        def Q = selectOfficesDynamic()
        def P = SelectOfficesDynamic.PARAMS

        when:
        def results = ez.from(Q)
                .setParam(P.DYN_O, Cnd.raw("offices"))
                .setParam(P.DYN_E, Cnd.raw("employees"))
                .setParam(P.COUNTRY_FILTER, "KE")
                .list()

        then:
        results.size() == 2
        results*.employeeName.sort() == ['Jane', 'John']
        results.every { it.country == 'KE' }
        results.every { it.officeCode == '2' }
    }

    def 'test dynamic table names with no matching results'() {
        given:
        def Q = selectOfficesDynamic()
        def P = SelectOfficesDynamic.PARAMS

        when:
        def results = ez.from(Q)
                .setParam(P.DYN_O, Cnd.raw("offices"))
                .setParam(P.DYN_E, Cnd.raw("employees"))
                .setParam(P.COUNTRY_FILTER, "XX")
                .list()

        then:
        results.isEmpty()
    }
}

