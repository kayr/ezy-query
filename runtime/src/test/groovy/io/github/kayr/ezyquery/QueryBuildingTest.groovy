package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.api.Cnd
import io.github.kayr.ezyquery.api.Conds
import io.github.kayr.ezyquery.api.Field
import io.github.kayr.ezyquery.transpile.EszySqlTranspiler
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
        def expr = Conds.and(
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
        expr.toString() == '(name = ronald AND age > 20 AND (lastName >= mah OR lastName1 <= mah2 OR lastName2 AND sex <> m OR name LIKE %kdj%))'
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
        expr.toString() == '(name = ronald AND age in [20, 30, 40])'
        transpiled.sql == '(? = ? AND t.age in (?, ?, ?))'
        transpiled.params == ['name', 'ronald', 20, 30, 40]
    }

    def 'test not'() {
        when:
        def expr = Cnd.not(Cnd.eq("name", 'ronald')).asExpr()
        def transpiled = EzySql.transpile(fields, expr)
        then:
        expr.toString() == 'not(name = ronald)'
        transpiled.sql == 'not(? = ?)'
        transpiled.params == ['name', 'ronald']
    }


}


