package io.github.kayr.ezyquery.gen

import spock.lang.Specification

class SectionsParserTest extends Specification {


    def "should split up sql string by sections"() {

        given:
        def sql = """
            -- Select Users
            select * from users
            where name = 'john'
            and age > 30
            order by name
            
            -- Select Orders
            select * from orders
            where user_id = 1
        """.stripIndent().trim()

        when:
        def sections = SectionsParser.splitUp(sql)

        then:
        sections.size() == 2
        sections[0].sql() == "select * from users\n" +
                "where name = 'john'\n" +
                "and age > 30\n" +
                "order by name\n\n"

        sections[0].name() == "Select Users"

        sections[1].sql() == "select * from orders\n" +
                "where user_id = 1\n"
        sections[1].name() == "Select Orders"

    }

    def "should return empty list when no sections"() {

        given:
        def sql = """
            select * from users
            where name = 'john'
            and age > 30
            order by name
        """.stripIndent().trim()

        when:
        def sections = SectionsParser.splitUp(sql)

        then:
        sections.size() == 0

    }

}
