package io.github.kayr.ezyquery.it


import io.github.kayr.ezyquery.api.EzyCriteria
import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.testqueries.CustomerReps
import spock.lang.Shared
import spock.lang.Specification

class DbApitItSpec extends Specification {

    @Shared
    Db db

    def setupSpec() {

        db = new Db().insertData()


    }

    def cleanupSpec() {
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
        def result = criteria.listAndCount();

        then:
        list.size() == 2
        count == 2
        result.count == 2
        result.list.size() == 2
        result.list*.toString() == list*.toString()

    }

    def "test listing data without filter criteria"() {
        def criteria = db.ezySql()
                .from(CustomerReps.Q)
                .limit(5).offset(0)
        when:
        def list = criteria.list()
        def count = criteria.count()
        def result = criteria.listAndCount();

        then:
        list.size() == 5
        count == 8
        result.count == 8
        result.list.size() == 5
        result.list*.toString() == list*.toString()

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
