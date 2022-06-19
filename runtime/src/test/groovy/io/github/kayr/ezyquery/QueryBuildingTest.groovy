package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.api.cnd.Conds
import io.github.kayr.ezyquery.api.Field
import spock.lang.Specification

class QueryBuildingTest extends Specification {
    def fields = [
            new Field('t.name', 'name'),
            new Field('t.age', 'age'),
            new Field('t.office', 'office'),
            new Field('t.maxAge', 'maxAge')
    ]

    def 'test build a query'() {


        when:
        def expr = Cnd.andAll(
                Cnd.eq("name", 'ronald'),
                Cnd.gt('#age', 20),
                Conds.or(Cnd.gte("lastName", 'mah'),
                        Cnd.lte("lastName1", 'mah2'),
                        Cnd.and("lastName2",
                                Cnd.neq('sex', 'm')),
                        Cnd.like('#name', '%kdj%')))
                .asExpr()


        def transpiled = EzySql.transpile(fields, expr)


        then:
        expr.toString() == '(name = ronald AND #age > 20 AND (lastName >= mah OR lastName1 <= mah2 OR lastName2 AND sex <> m OR #name LIKE %kdj%))'
        transpiled.sql == '(? = ? AND t.age > ? AND (? >= ? OR ? <= ? OR ? AND ? <> ? OR t.name LIKE ?))'
        transpiled.params == ['name', 'ronald', 20, 'lastName', 'mah', 'lastName1', 'mah2', 'lastName2', 'sex', 'm', '%kdj%']
    }

    def 'test in operator must use a list'() {
        when:
        Cnd.in('x', 20).asExpr()
        then:
        IllegalStateException e = thrown()
        e.message.startsWith('right must be a list on expression: Cond')
    }

    def 'test build a query with in'() {
        given:
        def fields = [
                new Field('t.name', 'name'),
                new Field('t.age', 'age'),
                new Field('t.office', 'office'),
                new Field('t.maxAge', 'maxAge')
        ]
        when:
        def expr = Conds.and(
                Cnd.eq("name", 'ronald'),
                Cnd.in('#age', [20, 30, 40]))
                .asExpr()


        def transpiled = EzySql.transpile(fields, expr)


        then:
        expr.toString() == '(name = ronald AND #age in [20, 30, 40])'
        transpiled.sql == '(? = ? AND t.age IN (?, ?, ?))'
        transpiled.params == ['name', 'ronald', 20, 30, 40]
    }

    def "test build a query with in with empty list"() {
        given:
        def fields = [
                new Field('t.name', 'name'),
                new Field('t.age', 'age'),
                new Field('t.office', 'office'),
                new Field('t.maxAge', 'maxAge')
        ]
        when:
        def expr = Cnd.andAll(
                Cnd.or("name", 'ronald'),
                Cnd.in('#age', []))
                .asExpr()

        then:
        expr.toString() == '(name OR ronald AND #age in [])'
        EzySql.transpile(fields, expr).sql == '(? OR ? AND 1 = 0)'
    }

    def "test build a query with in with empty list and empty list"() {
        given:
        def fields = [
                new Field('t.name', 'name'),
                new Field('t.age', 'age'),
                new Field('t.office', 'office'),
                new Field('t.maxAge', 'maxAge')
        ]
        when:
        def expr = Cnd.orAll(
                Cnd.negate(10),
                Cnd.positive("5/5"),
                Cnd.isNull("NV"),
                Cnd.isNotNull("NNV"),
                Cnd.notLike("#name", '%kdj%'),
                Cnd.notIn('#age', [1, 3, 4]))
                .asExpr()

        def strExpr = expr.toString()


        def result = EzySql.transpile(fields, expr)
        def transpiled = result.sql

        then:
        strExpr == '(-10 OR +5/5 OR NV is null OR NNV is not null OR #name NOT LIKE %kdj% OR #age not in [1, 3, 4])'
        transpiled == '(-? OR +? OR ? IS NULL OR ? IS NOT NULL OR t.name NOT LIKE ? OR t.age NOT IN (?, ?, ?))'
        result.params == [10, '5/5', 'NV', 'NNV', '%kdj%', 1, 3, 4]

    }


    def "test between expression"() {
        given:
        def fields = [
                new Field('t.name', 'name'),
                new Field('t.age', 'age'),
                new Field('t.office', 'office'),
                new Field('t.maxAge', 'maxAge')
        ]
        when:
        def expr = Cnd.orAll(
                Cnd.trueCnd(),
                Cnd.between(
                        Cnd.negate(10),
                        Cnd.positive("5/5"),
                        Cnd.isNull("NV")))
                .asExpr()

        def strExpr = expr.toString()

        println(strExpr)


        def transpiled = EzySql.transpile(fields, expr)
        def sql = transpiled.sql
        def params = transpiled.params

        then:
        strExpr == '(1 = 1 OR -10 BETWEEN +5/5 AND NV is null)'
        sql == '(? = ? OR -? between +? and ? IS NULL)'
        params == [1, 1, 10, '5/5', 'NV']


    }

    def 'test not'() {
        when:
        def expr = Cnd.not(Cnd.eq("name", 'ronald')).asExpr()
        def transpiled = EzySql.transpile(fields, expr)
        then:
        expr.toString() == 'not(name = ronald)'
        transpiled.sql == 'NOT(? = ?)'
        transpiled.params == ['name', 'ronald']
    }


}


