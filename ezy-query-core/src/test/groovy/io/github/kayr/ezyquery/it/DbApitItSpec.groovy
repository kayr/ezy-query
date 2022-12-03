package io.github.kayr.ezyquery.it


import io.github.kayr.ezyquery.api.EzyCriteria
import io.github.kayr.ezyquery.api.cnd.Cnd
import spock.lang.Specification

class DbApitItSpec extends Specification {

    Db db

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
    }

    def cleanup() {
        db.close()
    }

    def "should be able to filter by office code"() {
        when:
        def list = db.ezySql().list(CustomerReps.Q,
                EzyCriteria.selectAll()
                        .where(
                                Cnd.all(
                                        Cnd.eq("#employeeOfficeCode", "2"),
                                        Cnd.isNotNull("#customerName")
                                )
                        )
                        .limit(10).offset(0))

        then:
        list.size() == 2

    }

    def "should be able to filter by office code objects"() {
        when:
        def list = db.ezySql().list(CustomerReps.Q,
                EzyCriteria.selectAll()
                        .where(
                                Cnd.all(
                                        Cnd.eq(CustomerReps.FIELD_EMPLOYEE_OFFICE_CODE, "2"),
                                        Cnd.isNotNull(CustomerReps.FIELD_CUSTOMER_NAME)
                                )
                        )
                        .limit(10).offset(0))

        then:
        list.size() == 2

    }

    def "should be able to filter by office code objects using fluent api"() {
        def criteria = db.ezySql()
                .from(CustomerReps.Q)
                .where(
                        Cnd.all(
                                Cnd.eq(CustomerReps.FIELD_EMPLOYEE_OFFICE_CODE, "2"),
                                Cnd.isNotNull(CustomerReps.FIELD_CUSTOMER_NAME)
                        )
                )
                .limit(10).offset(0)
        when:
        def list = criteria.list()
        def count = criteria.count()

        then:
        list.size() == 2
        count == 2

    }


    def "test i can do this"() {

        when:
        def list = db.ezySql().list(CustomerReps.Q,
                EzyCriteria.selectAll()
                        .where(
                                Cnd.or(
                                        Cnd.andAll(
                                                Cnd.isNotNull(CustomerReps.FIELD_CUSTOMER_NAME),
                                                Cnd.eq(CustomerReps.FIELD_EMPLOYEE_COUNTY, "XXX")),
                                        Cnd.gt(CustomerReps.FIELD_EMPLOYEE_OFFICE, "Kampala")))
                        .where(
                                String.format("(%s is not null and %s = 'XXX') or %s not like '%%Kampala%%'",
                                        CustomerReps.FIELD_CUSTOMER_NAME,
                                        CustomerReps.FIELD_EMPLOYEE_COUNTY,
                                        CustomerReps.FIELD_EMPLOYEE_OFFICE


                                )
                        )
                        .limit(10).offset(20)
        )
        println list

        then:

        1 == 1
    }


}
