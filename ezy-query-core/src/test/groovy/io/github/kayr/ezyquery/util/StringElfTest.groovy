package io.github.kayr.ezyquery.util


import spock.lang.Specification

class StringElfTest extends Specification {
    def "split by comma"() {

        expect:
        StringElf.splitByComma(input) == output
        where:
        input     | output
        "a"       | ["a"]
        "a,b"     | ["a", "b"]
        "a,b,c"   | ["a", "b", "c"]
        '"a,b",c' | ['a,b', "c"]
        'a,"b,c"' | ['a', 'b,c']
        ""        | ['']
        null      | []
    }
}
