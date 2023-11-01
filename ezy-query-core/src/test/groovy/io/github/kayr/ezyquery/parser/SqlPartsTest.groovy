package io.github.kayr.ezyquery.parser


import spock.lang.Specification

class SqlPartsTest extends Specification {

    def s = SqlParts.of(
            SqlParts.textPart("from table inner join ( select * from table2 where table2.id in ("),
            SqlParts.paramPart("param1"),
            SqlParts.textPart(")) as t2 on t2.id = table.id "),
            SqlParts.paramPart("param2")
    )

    def query = "from table inner join ( select * from table2 where table2.id in ( :param1 )) as t2 on t2.id = table.id :param2"

    def 'should fail if some params are not set'() {
        when:
        s.setParam("param1", 1)
                .getQuery()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Param [param2] is not set"
    }

    def 'should fail if you set a param that does not exist'() {
        when:
        s.setParam("param3", 1)
                .getQuery()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Param [param3] does not exist"
    }

    def 'should construct sql with postional params'() {
        when:
        def query = s.setParam("param1", "XX1")
                .setParam("param2", "FF2")
                .getQuery()

        then:
        query.getSql() == "from table inner join ( select * from table2 where table2.id in (?)) as t2 on t2.id = table.id ?"
        query.params == ["XX1", "FF2"]
    }

    def 'should construct sql with correct list values'() {
        when:
        def query = s.setParam("param1", ["XX1", "XX2"])
                .setParam("param2", "FF2")
                .getQuery()

        then:
        query.getSql() == "from table inner join ( select * from table2 where table2.id in (?,?)) as t2 on t2.id = table.id ?"
        query.params == ["XX1", "XX2", "FF2"]
    }


    def 'convert to param value should handle both collection and objects'() {

        expect:
        SqlParts.convertToValueParam(a) == b

        where:
        a                             | b
        "a"                           | ["a"]
        1                             | [1]
        [1, 2]                        | [1, 2]
        ["a", "b"]                    | ["a", "b"]
        null                          | [null]
        new Vector([1, 2])            | [1, 2]
        new Vector([1, 2]).iterator() | [1, 2]
        new Vector([1, 2]).elements() | [1, 2]
        new HashSet([1, 2])           | [1, 2]
        createIterable([1, 2])        | [1, 2]
        [1, 2].stream()               | [1, 2]
        [1, 2].toArray()              | [1, 2]
    }

    def 'should be parse raw sql query with params'() {
        when:
        def query = SqlParts.of(query)

        then:
        query.rawSql == "from table inner join ( select * from table2 where table2.id in ( :param1 )) as t2 on t2.id = table.id :param2"
        query.parts.findAll { it instanceof SqlParts.IPart.Param }.collect { it.name } == ["param1", "param2"]
    }


    def 'should parse query with named param at beginning and end'(){
        when:
        def query = SqlParts.of(":param1 from table inner join ( select * from table2 where table2.id in ( :param2 )) as t2 on t2.id = table.id :p")

        then:
        query.rawSql == ":param1 from table inner join ( select * from table2 where table2.id in ( :param2 )) as t2 on t2.id = table.id :p"
        query.parts.findAll { it instanceof SqlParts.IPart.Param }.collect { it.name } == ["param1", "param2", "p"]
    }

    def 'should parse query with params in the middle'(){
        when:
        def query = SqlParts.of("from table inner join ( select * from table2 where table2.id in ( :param1 )) as t2 on t2.id = table.id :param2 and table.xxx = t2.yyy")

        then:
        query.rawSql == "from table inner join ( select * from table2 where table2.id in ( :param1 )) as t2 on t2.id = table.id :param2 and table.xxx = t2.yyy"
        query.parts.findAll { it instanceof SqlParts.IPart.Param }.collect { it.name } == ["param1", "param2"]

    }



    def 'should parse with no named params'(){
        when:
        def query = SqlParts.of("from table inner join ( select * from table2 where table2.id in ( 1 )) as t2 on t2.id = table.id ")

        then:
        query.rawSql == "from table inner join ( select * from table2 where table2.id in ( 1 )) as t2 on t2.id = table.id "
        query.parts.findAll { it instanceof SqlParts.IPart.Param }.collect { it.name } == []
    }

    def "empty should return empty query"(){
        when:
        def query = SqlParts.empty()

        then:
        query.rawSql == ""
        query.getQuery().getSql() == ""
        query.getQuery().params.isEmpty()
        query.parts.isEmpty()
    }


    Iterable createIterable(List itesm) {
        return new Iterable() {
            @Override
            Iterator iterator() {
                return itesm.iterator()
            }
        }
    }


}
