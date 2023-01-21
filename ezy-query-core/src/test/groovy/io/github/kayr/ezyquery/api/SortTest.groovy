package io.github.kayr.ezyquery.api

import io.github.kayr.ezyquery.testqueries.Offices
import spock.lang.Specification

class SortTest extends Specification {
    def "Desc"() {
        when:
        def sort = Sort.by("name", Sort.DIR.ASC).desc()

        then:
        sort.toString() == "name DESC"
    }

    def "Asc"() {
        when:
        def sort = Sort.by("name", Sort.DIR.DESC).asc()

        then:
        sort.toString() == "name ASC"
    }

    def "Parse"() {
        expect:
        def parsed = Sort.parse(input)
        output == parsed

        where:
        input                        | output
        "name asc"                   | [Sort.by("name", Sort.DIR.ASC)]
        "name desc"                  | [Sort.by("name", Sort.DIR.DESC)]
        "name deSC"                  | [Sort.by("name", Sort.DIR.DESC)]
        "name asc, age desc"         | [Sort.by("name", Sort.DIR.ASC), Sort.by("age", Sort.DIR.DESC)]
        "name asc, age desc, office" | [Sort.by("name", Sort.DIR.ASC), Sort.by("age", Sort.DIR.DESC), Sort.by("office", Sort.DIR.ASC)]
        "name"                       | [Sort.by("name", Sort.DIR.ASC)]


    }

    def 'test parse throws exception when invalid sort'() {
        when:
        Sort.parse("name asc, age desc, office azc")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid sort direction: azc"
    }

    def 'test parse throws exception when blank sort is provided'() {
        when:
        Sort.parse("  ")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Sort expression cannot be blank"
    }

    def 'test parse throws exception when null sort is provided'() {
        when:
        Sort.parse(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Sort expression cannot be blank"
    }

    def 'test parse throws exception when invalid sort part is provided'() {
        when:
        Sort.parse("name asc, age desc, , office")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Sort cannot have blank parts"
    }

    def "By"() {
        when:
        def sort1 = Sort.by("name", Sort.DIR.ASC)
        def sort2 = Sort.by(Offices.CODE, Sort.DIR.DESC)

        then:
        sort1.toString() == "name ASC"
        sort2.toString() == "code DESC"
    }



}
