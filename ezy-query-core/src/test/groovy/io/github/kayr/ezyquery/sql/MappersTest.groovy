package io.github.kayr.ezyquery.sql

import io.github.kayr.ezyquery.it.Db
import io.github.kayr.ezyquery.testqueries.Offices
import spock.lang.Shared
import spock.lang.Specification

class MappersTest extends Specification {


    @Shared
    Db db

    void setupSpec() {
        db = new Db().insertData()
    }

    void cleanupSpec() {
        db.close()
    }

    def 'test #Mappers.toClass'() {
        when:
        def result = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.in('1', '2', '3'))
                .orderBy(Offices.CODE.asc())
                .query { Mappers.resultSetToList(it, Mappers.toClass(Offices.Result.class)) }

        then:
        result.size() == 3
        result[0].code == '1'
        result[0].addressLine == 'Kampala'
        result[0].country == 'UG'

        result[1].code == '2'
        result[1].addressLine == 'Nairobi'
        result[1].country == 'KE'

        result[2].code == '3'
        result[2].addressLine == 'Dar es Salaam'
        result[2].country == 'TZ'


    }

    def 'test #Mappers.toMap'(){

        when:
        def result = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.in('1', '2', '3'))
                .orderBy(Offices.CODE.asc())
                .query { Mappers.resultSetToList(it, Mappers.toMap()) }

        then:
        result.size() == 3
        result[0].get('code') == '1'
        result[0].get('addressLine') == 'Kampala'
        result[0].get('country') == 'UG'

        result[1].get('code') == '2'
        result[1].get('addressLine') == 'Nairobi'
        result[1].get('country') == 'KE'

        result[2].get('code') == '3'
        result[2].get('addressLine') == 'Dar es Salaam'
        result[2].get('country') == 'TZ'
    }

}
