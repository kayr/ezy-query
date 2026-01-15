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


        def properties = new Properties()
        properties.setProperty("resultDto.addWither","true")
        when:
        def generated = EzySqlQueryGen.of("package", "Query", new BatchQueryGen.SourceCode(resource.v1, Path.of('ez_static',"in.sql.txt")), properties)
                .generate()


        then:
        generated.toString().trim() == resource.v2.trim()
    }

    def "test ez sql files are parsed without withers"() {

        def resource = TestUtil.load("ez_static_no_wither")


        def properties = new Properties()
        properties.setProperty("resultDto.addWither","false")
        when:
        def generated = EzySqlQueryGen.of("package", "Query", new BatchQueryGen.SourceCode(resource.v1, Path.of('ez_static',"in.sql.txt")), properties)
                .generate()


        then:
        generated.toString().trim() == resource.v2.trim()
    }

    def "test ez sql files are parsed with mutable setters"() {

        def resource = TestUtil.load("ez_static_mutable")


        def properties = new Properties()
        properties.setProperty("staticQuery.mutable","true")
        when:
        def generated = EzySqlQueryGen.of("package", "Query", new BatchQueryGen.SourceCode(resource.v1, Path.of('ez_static',"in.sql.txt")), properties)
                .generate()

        then:
        generated.toString().trim() == resource.v2.trim()
    }

}
