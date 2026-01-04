package io.github.kayr.ezyquery.sql

import io.github.kayr.ezyquery.it.Db
import io.github.kayr.ezyquery.testqueries.Offices
import io.github.kayr.ezyquery.util.ReflectionUtil
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

    def 'test #Mappers.toMap'() {

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

    def "toObject with field mappers should apply converters for simple fields"() {
        given:
        def fieldMappers = [(Integer.class): { it * 2 } as ThrowingFunction]
        def mapper = Mappers.toObject(SamplePerson, fieldMappers)
        def columns = [new ColumnInfo("name", "name"), new ColumnInfo("age", "age")]
        def rs = Mock(ResultSet)
        rs.getObject("name") >> "John"
        rs.getObject("age") >> 25

        when:
        def result = mapper.mapRow(0, columns, rs)

        then:
        result.name == "John"
        result.age == 50
    }

    def "toObject with field mappers should support generic types"() {
        given:
        def type = new TypeRef<List<String>>() {}.getType()
        def fieldMappers = [(type): { it.split(",") as List } as ThrowingFunction]
        def mapper = Mappers.toObject(SamplePerson, fieldMappers)
        def columns = [new ColumnInfo("tags", "tags")]
        def rs = Mock(ResultSet)
        rs.getObject("tags") >> "tag1,tag2,tag3"

        when:
        def result = mapper.mapRow(0, columns, rs)

        then:
        result.tags == ["tag1", "tag2", "tag3"]
    }

    def "toObject with field mappers should handle DynamicFieldSetter for unknown fields"() {
        given:
        def mapper = Mappers.toObject(SamplePerson, [:])
        def columns = [new ColumnInfo("extra", "extra")]
        def rs = Mock(ResultSet)
        rs.getObject("extra") >> "SomeValue"

        when:
        def result = mapper.mapRow(0, columns, rs)

        then:
        result.extraFields["extra"] == "SomeValue"
    }

    def "toObject with field mappers should apply converters to dynamic fields if type matches"() {
        given:
        def fieldMappers = [(Integer.class): { it * 10 } as ThrowingFunction]
        def mapper = Mappers.toObject(SamplePerson, fieldMappers)
        def columns = [new ColumnInfo("age", "age"), new ColumnInfo("extra", "extra")]
        def rs = Mock(ResultSet)
        rs.getObject("age") >> 25
        rs.getObject("extra") >> "dynamic"

        when:
        def result = mapper.mapRow(0, columns, rs)

        then:
        result.age == 250
        result.extraFields["extra"] == "dynamic"
    }

    def "toObject with field mappers should handle complex nested generics"() {
        given:
        def type = new TypeRef<Map<Integer, List<CustomType>>>() {}.getType()
        def fieldMappers = [
                (type)            : { [1: [new CustomType("a"), new CustomType("b")]] } as ThrowingFunction,
                (CustomType.class): { new CustomType(it as String) } as ThrowingFunction
        ]
        def mapper = Mappers.toObject(MyClass, fieldMappers)
        def columns = [new ColumnInfo("values", "values"), new ColumnInfo("value", "value"), new ColumnInfo("otherValue", "otherValue")]
        def rs = Mock(ResultSet)
        rs.getObject("values") >> 'json'
        rs.getObject("value") >> 'plain'
        rs.getObject("otherValue") >> 'custom'

        when:
        def result = mapper.mapRow(0, columns, rs)

        then:
        result.value == "plain"
        result.otherValue.name == "custom"
        result.values[1][0].name == "a"
    }

    static class CustomType {
        String name

        CustomType(String name) { this.name = name }
    }

    static class MyClass {
        Map<Integer, List<CustomType>> values
        String value
        CustomType otherValue
    }

    static class SamplePerson implements DynamicFieldSetter {
        String name
        Integer age
        List<String> tags
        Map<String, Object> extraFields = [:]

        @Override
        void setField(String fieldName, Object value) {
            def field = ReflectionUtil.getField(getClass(), fieldName)
            if (field != null) {
                ReflectionUtil.setNonSyntheticField(this, field, value)
            } else
                extraFields.put(fieldName, value)
        }
    }

}
