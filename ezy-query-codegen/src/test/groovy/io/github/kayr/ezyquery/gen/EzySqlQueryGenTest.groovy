package io.github.kayr.ezyquery.gen

import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset


class EzySqlQueryGenTest extends Specification {

    def setup() {
        TimeElf.setClock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
    }


    def "test ez sql files are parsed"() {

        def resource = TestUtil.load("ez_static")

        when:
        def generated = EzySqlQueryGen.of("package", "Query", resource.v1, new Properties())
                .generate()

        then:
        generated.toString().trim() == resource.v2.trim()
    }

}
