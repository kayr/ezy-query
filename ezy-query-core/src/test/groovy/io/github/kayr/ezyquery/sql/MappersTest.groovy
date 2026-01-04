package io.github.kayr.ezyquery.sql


import io.github.kayr.ezyquery.it.Db
import io.github.kayr.ezyquery.testqueries.Offices
import io.github.kayr.ezyquery.util.ThrowingFunction
import spock.lang.Shared
import spock.lang.Specification

import java.sql.ResultSet


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

    def "toObject with field mappers should apply converters"() {
        given:

        def fieldMappers = [(Integer.class): { Object v -> (v as Integer) * 2 } as ThrowingFunction]
        def mapper = Mappers.toObject(Person, fieldMappers)

        def columns = [new ColumnInfo("name", "name"), new ColumnInfo("age", "age")]
        def rs = Mock(ResultSet)
        rs.getObject("name") >> "John"
        rs.getObject("age") >> 25

        when:
        def person = mapper.mapRow(0, columns, rs)

        then:
        person.name == "John"
        person.age == 50
    }

    def "toObject with field mappers should support DynamicFieldSetter"() {
        given:

        def fieldMappers = [(String.class): { Object v -> "Mr. $v" } as ThrowingFunction]
        def mapper = Mappers.toObject(PersonWithDynamic, fieldMappers)

        def columns = [new ColumnInfo("extra", "extra")]
        def rs = Mock(ResultSet)
        rs.getObject("extra") >> "SomeValue"

        when:
        def person = mapper.mapRow(0, columns, rs)

        then:
        person.extraFields["extra"] == "SomeValue"
    }

    def "toObject with field mappers should fallback to normal set if no converter and not dynamic"() {
        given:

        def mapper = Mappers.toObject(Person, Collections.emptyMap())

        def columns = [new ColumnInfo("name", "name"), new ColumnInfo("age", "age")]
        def rs = Mock(ResultSet)
        rs.getObject("name") >> "John"
        rs.getObject("age") >> 25

        when:
        def person = mapper.mapRow(0, columns, rs)

        then:
        person.name == "John"
        person.age == 25
    }

    static class Person {
        public String name;
        public Integer age;
    }

    static class PersonWithDynamic implements DynamicFieldSetter {
        public String name;
        public Map<String, Object> extraFields = new HashMap<>();

        @Override
        void setField(String fieldName, Object value) {
            extraFields.put(fieldName, value);
        }
    }

}
