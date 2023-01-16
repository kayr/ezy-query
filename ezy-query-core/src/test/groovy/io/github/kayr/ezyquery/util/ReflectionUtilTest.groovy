package io.github.kayr.ezyquery.util

import groovy.transform.PackageScope
import spock.lang.Specification

class ReflectionUtilTest extends Specification {

    static class UnderTest {
        private def privateField
        public def publicField
        protected def protectedField
        @PackageScope
        def packageField
        private final def privateFinalField
    }

    def "SetField"() {
        given:


        expect:
        def obj = new UnderTest()
        ReflectionUtil.setField(fieldName, obj, value)
        obj."$fieldName" == value

        where:
        fieldName           | value
        "privateField"      | "value"
        "publicField"       | "value"
        "protectedField"    | "value"
        "packageField"      | "value"
        "privateFinalField" | "value"


    }

    def "DoWithFields"() {
        given:
        def obj = new UnderTest()
        def fields = []

        when:
        ReflectionUtil.doWithFields(UnderTest) { if (!it.isSynthetic()) fields << it }

        then:
        fields.size() == 5
        fields*.name == ["privateField", "publicField", "protectedField", "packageField", "privateFinalField"]
    }

    def "GetField"() {

        expect:
        ReflectionUtil.getField(UnderTest, "privateField")?.name != null
        ReflectionUtil.getField(UnderTest, "publicField")?.name != null
        ReflectionUtil.getField(UnderTest, "protectedField")?.name != null
        ReflectionUtil.getField(UnderTest, "packageField")?.name != null
        ReflectionUtil.getField(UnderTest, "privateFinalField")?.name != null


    }
}