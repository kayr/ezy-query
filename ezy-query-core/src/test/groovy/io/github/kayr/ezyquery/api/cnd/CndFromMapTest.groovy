package io.github.kayr.ezyquery.api.cnd

import io.github.kayr.ezyquery.api.Field
import io.github.kayr.ezyquery.parser.EzySqlTranspiler
import spock.lang.Specification

class CndFromMapTest extends Specification {
    def fields = [
            new Field('t.name', 'name'),
            new Field('t.age', 'age'),
            new Field('t.office', 'office'),
            new Field('t.maxAge', 'maxAge')
    ]

    def "from map should return correct condition"() {


        expect:
        def cond = CndFromMap.create().from(map).asExpr()
        def transpiled = EzySqlTranspiler.transpile(fields, cond)

        def transpiledSql = transpiled.sql
        def transpiledParams = transpiled.params
        def exprString = cond.toString()


        exprString == criteria
        transpiledSql == sql
        transpiledParams == params


        where:
        map                                | criteria                                            | sql                                            | params
        [name: ["John"]]                   | "#name = John"                                      | "t.name = ?"                                   | ["John"]
        ['name.eq': ["John"]]              | "#name = John"                                      | "t.name = ?"                                   | ["John"]
        [name: ["John", "Jane"]]           | "(#name = John AND #name = Jane)"                   | "(t.name = ? AND t.name = ?)"                  | ["John", "Jane"]
        ['name.neq': ["John", "Jane"]]     | "(#name <> John AND #name <> Jane)"                 | "(t.name <> ? AND t.name <> ?)"                | ["John", "Jane"]
        ['name.like': ["John", "Jane"]]    | "(#name LIKE John AND #name LIKE Jane)"             | "(t.name LIKE ? AND t.name LIKE ?)"            | ["John", "Jane"]
        ['name.notlike': ["John", "Jane"]] | "(#name NOT LIKE John AND #name NOT LIKE Jane)"     | "(t.name NOT LIKE ? AND t.name NOT LIKE ?)"    | ["John", "Jane"]
        ['name.gt': ['20', '30']]          | "(#name > 20 AND #name > 30)"                       | "(t.name > ? AND t.name > ?)"                  | ['20', '30']
        ['name.gte': ['20', 30]]           | "(#name >= 20 AND #name >= 30)"                     | "(t.name >= ? AND t.name >= ?)"                | ['20', 30]
        ['name.lt': [20, 30]]              | "(#name < 20 AND #name < 30)"                       | "(t.name < ? AND t.name < ?)"                  | [20, 30]
        ['name.lte': [20, 30]]             | "(#name <= 20 AND #name <= 30)"                     | "(t.name <= ? AND t.name <= ?)"                | [20, 30]
        ['name.in': [20, 30]]              | "(#name in [20] AND #name in [30])"                 | "(t.name IN (?) AND t.name IN (?))"            | [20, 30]
        ['name.in': ["John,Jane", 30]]     | "(#name in [John, Jane] AND #name in [30])"         | "(t.name IN (?, ?) AND t.name IN (?))"         | ["John", "Jane", 30]
        ['name.notin': ["John,Jane", 30]]  | "(#name not in [John, Jane] AND #name not in [30])" | "(t.name NOT IN (?, ?) AND t.name NOT IN (?))" | ["John", "Jane", 30]
        ['name.between': [20, 30]]         | "#name between 20 and 30"                           | "t.name BETWEEN ? AND ?"                       | [20, 30]
        ['name.notbetween': [20, 30]]      | "#name not between 20 and 30"                       | "t.name NOT BETWEEN ? AND ?"                   | [20, 30]
        ['name.isnull': []]                | "#name is null"                                     | "t.name IS NULL"                               | []
        ['name.isnull': null]              | "#name is null"                                     | "t.name IS NULL"                               | []
        ['name.isnotnull': []]             | "#name is not null"                                 | "t.name IS NOT NULL"                           | []
        ['name.isnotnull': null]           | "#name is not null"                                 | "t.name IS NOT NULL"                           | []
        [name: ['John'], age: ['10']]      | "(#name = John AND #age = 10)"                      | "(t.name = ? AND t.age = ?)"                   | ['John', '10']
    }
}
