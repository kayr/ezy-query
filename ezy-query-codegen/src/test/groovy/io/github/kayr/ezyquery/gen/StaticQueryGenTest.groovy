package io.github.kayr.ezyquery.gen

import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class StaticQueryGenTest extends Specification {

    def setup() {
      TimeElf.setClock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
    }


    def "test static query generation"() {
        given:
        def sql = """
            --- Select Users
            select * from users
            where name = :name
            and address = :address
            
            
            -- Select Orders
            select * from orders
            where user_id = userId
            
            -- Select Products
            select * from products
            where product_id = :productId
            
        """.stripIndent().trim()

        when:
        def generated = StaticQueryGen.of("package", "Query", sql)
                .javaFile();

        println generated.toString()

        then:
        1 == 1
    }
}
