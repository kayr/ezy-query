package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.api.Field
import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.api.cnd.Conds
import io.github.kayr.ezyquery.ast.EzyExpr
import io.github.kayr.ezyquery.parser.EzySqlTranspiler
import io.github.kayr.ezyquery.testqueries.Booleans
import spock.lang.Specification


class EzySqlTranspilerTest extends Specification {
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
                Conds.orAll(Cnd.gte("lastName", 'mah'),
                        Cnd.lte("lastName1", 'mah2'),
                        Cnd.and("lastName2",
                                Cnd.neq('sex', 'm')),
                        Cnd.like('#name', '%kdj%')))
                .asExpr()


        def transpiled = EzySqlTranspiler.transpile(fields, expr)


        then:
        expr.toString() == '(name = ronald AND #age > 20 AND (lastName >= mah OR lastName1 <= mah2 OR (lastName2 AND sex <> m) OR #name LIKE %kdj%))'
        transpiled.sql == '(? = ? AND t.age > ? AND (? >= ? OR ? <= ? OR (? AND ? <> ?) OR t.name LIKE ?))'
        transpiled.params == ['name', 'ronald', 20, 'lastName', 'mah', 'lastName1', 'mah2', 'lastName2', 'sex', 'm', '%kdj%']
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
        def expr = Conds.andAll(
                Cnd.eq("name", 'ronald'),
                Cnd.in('#age', [20, 30, 40]))
                .asExpr()


        def transpiled = EzySqlTranspiler.transpile(fields, expr)


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
        expr.toString() == '((name OR ronald) AND #age in [])'
        EzySqlTranspiler.transpile(fields, expr).sql == '((? OR ?) AND 1 = 0)'
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


        def result = EzySqlTranspiler.transpile(fields, expr)
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


        def transpiled = EzySqlTranspiler.transpile(fields, expr)
        def sql = transpiled.sql
        def params = transpiled.params

        then:
        strExpr == '(1 = 1 OR -10 between +5/5 and NV is null)'
        sql == '(? = ? OR -? BETWEEN +? AND ? IS NULL)'
        params == [1, 1, 10, '5/5', 'NV']


    }

    def 'test not'() {
        when:
        def expr = Cnd.not(Cnd.eq("name", 'ronald')).asExpr()
        def transpiled = EzySqlTranspiler.transpile(fields, expr)
        then:
        expr.toString() == 'not(name = ronald)'
        transpiled.sql == 'NOT(? = ?)'
        transpiled.params == ['name', 'ronald']
    }

    def 'test rendering of multiple conds'() {
        when:
        def expr = Cnd.andAll(
                Cnd.trueCnd(),
                Cnd.eq("name", 'ronald'),
                Cnd.gt('#age', 20),
                Cnd.orAll(
                        Cnd.or("lastName", 'mah'),
                        Cnd.lte("lastName1", 'mah2'),
                        Cnd.like('#name', '%kdj%')),
                Cnd.gt('#age', 30),
        ).asExpr()

        then:
        expr.toString() == '(1 = 1 AND name = ronald AND #age > 20 AND ((lastName OR mah) OR lastName1 <= mah2 OR #name LIKE %kdj%) AND #age > 30)'

    }

    def 'test not between'() {
        when:
        def expr = Cnd.notBetween(
                Cnd.negate(1),
                Cnd.negate("5/5"),
                Cnd.val(10)).asExpr()

        def transpiled = EzySqlTranspiler.transpile(fields, expr)


        then:
        expr.toString() == '-1 not between -5/5 and 10'
        transpiled.sql == '-? NOT BETWEEN -? AND ?'
        transpiled.params == [1, '5/5', 10]
    }

    def 'test binary expressions are wrapped in parens'() {
        // (name  or age ) and office
        EzyExpr expr = Booleans.A.and(Booleans.B).asExpr()
        when:
        def transpiled = EzySqlTranspiler.transpile(Booleans.QUERY.fields(), expr)

        def (sql, params) = [transpiled.sql, transpiled.params]

        then:
        sql == '((true OR true) AND false)'
        params == []

    }

    def 'test transpiling expression'() {

        when:
        def expr = '''
'Julius Ceaser' = name and age in (10, 20, 30,maxAge)
or (office = 'London' and age > 20)
'''

        def fields = [
                new Field('t.name', 'name'),
                new Field('t.age', 'age'),
                new Field('t.office', 'office'),
                new Field('t.maxAge', 'maxAge')
        ]

        def result = EzySqlTranspiler.transpile(fields, expr)
        println(result)
        then:
        result.toString() == 'Result{sql=\'? = t.name AND t.age IN (?, ?, ?, t.maxAge) OR (t.office = ? AND t.age > ?)\', params=[Julius Ceaser, 10, 20, 30, London, 20]}'
        assert result.params == [
                'Julius Ceaser',
                10,
                20,
                30,
                'London',
                20
        ]
    }


}


