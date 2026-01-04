package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.it.Db
import io.github.kayr.ezyquery.sql.Mappers
import io.github.kayr.ezyquery.testqueries.CustomerReps
import spock.lang.Shared
import spock.lang.Specification

import java.sql.ResultSet

class EzySqlMapperTest extends Specification {

    @Shared
    Db db

    def setupSpec() {
        db = new Db().insertData()
    }

    def cleanupSpec() {
        db.close()
    }

    def "should use Map mapper by default for plain EzyQuery"() {
        given:
        EzySql ezySql = db.ezySql()

        when:
        def list = ezySql.from((EzyQuery) CustomerReps.Q).list()

        then:
        assert list.size() > 0
        assert list[0] instanceof Map
        assert list[0].containsKey("customerName")
    }

    def "should be able to use a custom default mapper factory for transformations"() {
        given:
        // A mapper factory that wraps the default mapper and transforms the result
        def customMapperFactory = { Class<?> clazz ->
            def originalMapper = Mappers.toClass(clazz)
            return { int rowIndex, List columns, ResultSet rs ->
                def result = originalMapper.mapRow(rowIndex, columns, rs)
                if (result instanceof CustomerReps.Result) {
                    result.customerName = "Mr/Ms. $result.customerName"
                }
                return result
            } as Mappers.RowMapper

        }

        EzySql ezySql = db.ezySql().withMapperFactory(customMapperFactory)

        when:
        def list = ezySql.from(CustomerReps.Q).list()
        def firstResult = list.find { it.customerName != null }

        then:
        list.size() > 0
        firstResult.customerName.startsWith("Mr/Ms. ")
    }

    def "should still allow overriding mapper per query using mapTo"() {
        given:
        EzySql ezySql = db.ezySql()

        when:
        def list = ezySql.from(CustomerReps.Q)
                .mapTo(Mappers.toMap())
                .list()

        then:
        list.size() > 0
        list[0] instanceof Map
    }

}
