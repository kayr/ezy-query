package io.github.kayr.ezyquery.gen

import spock.lang.Specification

import java.nio.file.Path
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
        def generated = EzySqlQueryGen.of("package", "Query", new BatchQueryGen.SourceCode(resource.v1, Path.of('ez_static',"in.sql.txt")), new Properties())
                .generate()


        then:
        generated.toString().trim() == resource.v2.trim()
    }

}
