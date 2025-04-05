package io.github.kayr.ezyquery.gen

import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StaticQueryGenTest extends Specification {

    def setup() {
      TimeElf.setClock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
    }


    def "test static query generation"() {
        given:
        def resource = TestUtil.load("static")

        when:
        def generated = StaticQueryGen.of("package", "Query", resource.v1)
                .javaFile();

        println generated.toString()

        then:
        generated.toString().trim() == resource.v2.trim()
    }
}
