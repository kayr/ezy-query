package io.github.kayr.ezyquery.gen

import spock.lang.Specification

class StringCaseUtilTest extends Specification {

    def "test toPascalCase"() {
        expect:
        StringCaseUtil.toPascalCase(input) == expected
        where:
        input                      | expected
        null                       | null
        ""                         | ""
        "hello"                    | "Hello"
        "hello world"              | "HelloWorld"
        "hello-world"              | "HelloWorld"
        "hello_world"              | "HelloWorld"
        "helloWorld"               | "HelloWorld"
        "hey-helloWorld"           | "HeyHelloWorld"
        "Hey-helloWorld"           | "HeyHelloWorld"
        "Hey-helloWorld  "         | "HeyHelloWorld"
        "__Hey-helloWorld  "       | "HeyHelloWorld"
        "__Hey-helloWorld_WWW-W  " | "HeyHelloWorldWwwW"
    }

    def "test toCamelCase"() {
        expect:
        StringCaseUtil.toCamelCase(input) == expected
        where:
        input                | expected
        null                 | null
        ""                   | ""
        "hello"              | "hello"
        "hello world"        | "helloWorld"
        "hello-world"        | "helloWorld"
        "hello_world"        | "helloWorld"
        "helloWorld"         | "helloWorld"
        "hey-helloWorld"     | "heyHelloWorld"
        "Hey-helloWorld"     | "heyHelloWorld"
        "Hey-helloWorld  "   | "heyHelloWorld"
        "__Hey-helloWorld  " | "heyHelloWorld"
        "Hey-helloWorld_WWW-W  " | "heyHelloWorldWwwW"

    }

    def "test toSnakeCase"() {
        expect:
        StringCaseUtil.toSnakeCase(input) == expected
        where:
        input                | expected
        null                 | null
        ""                   | ""
        "hello"              | "hello"
        "hello world"        | "hello_world"
        "hello-world"        | "hello_world"
        "hello_world"        | "hello_world"
        "helloWorld"         | "hello_world"
        "hey-helloWorld"     | "hey_hello_world"
        "Hey-helloWorld"     | "hey_hello_world"
        "Hey-helloWorld  "   | "hey_hello_world"
        "__Hey-helloWorld  " | "hey_hello_world"
        "Hey-helloWorld_WWW-W  " | "hey_hello_world_www_w"
    }

}