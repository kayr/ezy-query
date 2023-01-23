package io.github.kayr.ezyquery.api.cnd

import io.github.kayr.ezyquery.api.Field
import io.github.kayr.ezyquery.parser.EzySqlTranspiler
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CndTranspileTest extends Specification {

    @Shared
    private Field name = new Field('t.name', 'name')
    @Shared
    private Field age = new Field('t.age', 'age')
    private Field office = new Field('t.office', 'office')
    private Field maxAge = new Field('t.maxAge', 'maxAge')

    def fields = [name, age, office, maxAge]

    @Unroll
    def "test transpiling"() {
        expect:
        def expr = cnd.asExpr()
        def query = EzySqlTranspiler.transpile(fields, expr)
        def actualSql = query.sql
        def actualParams = query.params
        def actualEzyExpr = expr.toString()

        actualParams == params
        actualEzyExpr == exprString
        actualSql == sql

        where:
        cnd                                                  || params                   | exprString                 | sql
        Cnd.eq("name", 'john')                               || ['name', 'john']         | 'name = john'              | '? = ?'
        Cnd.neq("name", 'john')                              || ['name', 'john']         | 'name <> john'             | '? <> ?'
        Cnd.gt("name", 'john')                               || ['name', 'john']         | 'name > john'              | '? > ?'
        Cnd.gte("name", 'john')                              || ['name', 'john']         | 'name >= john'             | '? >= ?'
        Cnd.lt("name", 'john')                               || ['name', 'john']         | 'name < john'              | '? < ?'
        Cnd.lte("name", 'john')                              || ['name', 'john']         | 'name <= john'             | '? <= ?'
        Cnd.like("name", 'john')                             || ['name', 'john']         | 'name LIKE john'           | '? LIKE ?'
        Cnd.notLike("name", 'john')                          || ['name', 'john']         | 'name NOT LIKE john'       | '? NOT LIKE ?'
        Cnd.in("name", ['john'])                             || ['name', 'john']         | 'name in [john]'           | '? IN (?)'
        Cnd.notIn("name", ['john'])                          || ['name', 'john']         | 'name not in [john]'       | '? NOT IN (?)'
        Cnd.notIn("name", ['john', 'doe'])                   || ['name', 'john', 'doe']  | 'name not in [john, doe]'  | '? NOT IN (?, ?)'
        Cnd.in("name", ['john', 'doe'])                      || ['name', 'john', 'doe']  | 'name in [john, doe]'      | '? IN (?, ?)'
        Cnd.isNull("name")                                   || ['name']                 | 'name is null'             | '? IS NULL'
        Cnd.isNotNull("name")                                || ['name']                 | 'name is not null'         | '? IS NOT NULL'
        Cnd.val("name")                                      || ['name']                 | 'name'                     | '?'
        Cnd.not(Cnd.eq("name", 'john'))                      || ['name', 'john']         | 'not(name = john)'         | 'NOT(? = ?)'
        Cnd.andAll("true", Cnd.val("false"), Cnd.val("foo")) || ['true', 'false', 'foo'] | '(true AND false AND foo)' | '(? AND ? AND ?)'
        Cnd.orAll(Cnd.val("true"), "false", "foo")           || ['true', 'false', 'foo'] | '(true OR false OR foo)'   | '(? OR ? OR ?)'
        Cnd.trueCnd()                                        || [1, 1]                   | '1 = 1'                    | '? = ?'
        Cnd.positive("10")                                   || ['10']                   | '+10'                      | '+?'
        Cnd.negate("10")                                     || ['10']                   | '-10'                      | '-?'
        Cnd.expr("'name' = 'john'")                          || ['name', 'john']         | "('name' = 'john')"        | "(? = ?)"
        Cnd.expr("1 in (0)")                                 || [1, 0]                   | "1 in [0]"                 | "? IN (?)"
        Cnd.expr("1 in (0) and 2 > 1")                       || [1, 0, 2, 1]             | "(1 in [0] AND 2 > 1)"     | "(? IN (?) AND ? > ?)"
        Cnd.expr("name = 'john'")                            || ['john']                 | "(#name = 'john')"         | "(t.name = ?)"
        Cnd.sql("some sql expression", "param1", "param2")   || ['param1', 'param2']     | "(some sql expression)"    | "(some sql expression)"
        Cnd.sql("(some sql expression)", "param1", "param2") || ['param1', 'param2']     | "(some sql expression)"    | "(some sql expression)"
        Cnd.sql("(some sql expression)")                     || []                       | "(some sql expression)"    | "(some sql expression)"
    }

    def 'test combining multiple expressions'() {
        expect:
        def expr = cnd.asExpr()
        def query = EzySqlTranspiler.transpile(fields, expr)
        def actualSql = query.sql
        def actualParams = query.params
        def actualEzyExpr = expr.toString()

        actualParams == params
        actualEzyExpr == exprString
        actualSql == sql

        where:
        cnd                                                                               || params                                                | exprString                                            | sql
        Cnd.eq("name", 'john').and(Cnd.eq("age", 10))                                     || ['name', 'john', 'age', 10]                           | '(name = john AND age = 10)'                          | '(? = ? AND ? = ?)'
        Cnd.eq("name", 'john').or(Cnd.eq("age", 10))                                      || ['name', 'john', 'age', 10]                           | '(name = john OR age = 10)'                           | '(? = ? OR ? = ?)'
        Cnd.eq("name", 'john').and(Cnd.eq("age", 10)).or(Cnd.eq("office", 'hcm'))         || ['name', 'john', 'age', 10, 'office', 'hcm']          | '((name = john AND age = 10) OR office = hcm)'        | '((? = ? AND ? = ?) OR ? = ?)'
        Cnd.andAll("true", "false", "foo").and(Cnd.val("bar"))                            || ['true', 'false', 'foo', 'bar']                       | '(true AND false AND foo AND bar)'                    | '(? AND ? AND ? AND ?)'
        Cnd.andAll("true", "false", "foo").or(Cnd.val("bar"))                             || ['true', 'false', 'foo', 'bar']                       | '((true AND false AND foo) OR bar)'                   | '((? AND ? AND ?) OR ?)'
        Cnd.andAll("true", "false", "foo").or(Cnd.andAll("bar", "foo", "car"))            || ['true', 'false', 'foo', 'bar', 'foo', 'car']         | '((true AND false AND foo) OR (bar AND foo AND car))' | '((? AND ? AND ?) OR (? AND ? AND ?))'
        Cnd.andAll("true", "false", "foo").and(Cnd.andAll("bar", "foo", "car"))           || ['true', 'false', 'foo', 'bar', 'foo', 'car']         | '(true AND false AND foo AND bar AND foo AND car)'    | '(? AND ? AND ? AND ? AND ? AND ?)'
        Cnd.val("name").and(Cnd.eq("name", 'john'))                                       || ['name', 'name', 'john']                              | '(name AND name = john)'                              | '(? AND ? = ?)'
        Cnd.val("name").or(Cnd.eq("name", 'john'))                                        || ['name', 'name', 'john']                              | '(name OR name = john)'                               | '(? OR ? = ?)'
        Cnd.val("name").and(Cnd.val("age")).or(Cnd.val("office")).and(Cnd.val("address")) || ['name', 'age', 'office', 'address']                  | '(((name AND age) OR office) AND address)'            | '(((? AND ?) OR ?) AND ?)'
        Cnd.expr("true and true or false").and(Cnd.eq("a", 'b'))                          || [Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, 'a', 'b'] | "((true AND true OR false) AND a = b)"                | "((? AND ? OR ?) AND ? = ?)"
        Cnd.expr("'name' = 'john'").and(Cnd.eq("a", 'b'))                                 || ['name', 'john', 'a', 'b']                            | "(('name' = 'john') AND a = b)"                       | "((? = ?) AND ? = ?)"
        Cnd.sql("(some sql expression)", "param1", "param2").and(Cnd.eq('a', 'b'))        || ['param1', 'param2', 'a', 'b']                        | "((some sql expression) AND a = b)"                   | "((some sql expression) AND ? = ?)"


    }

    def 'test transpiling with field object'() {
        expect:
        def expr = cnd.asExpr()
        def query = EzySqlTranspiler.transpile(fields, expr)

        def actualSql = query.sql
        def actualParams = query.params
        def actualExpr = expr.toString()

        actualParams == params
        actualExpr == exprString
        actualSql == sql

        where:
        cnd                                 || params          | exprString                       | sql
        Cnd.eq(name, 'john')                || ['john']        | '#name = john'                   | 't.name = ?'
        Cnd.neq(name, 'john')               || ['john']        | '#name <> john'                  | 't.name <> ?'
        Cnd.gt(name, 'john')                || ['john']        | '#name > john'                   | 't.name > ?'
        Cnd.gte(name, 'john')               || ['john']        | '#name >= john'                  | 't.name >= ?'
        Cnd.lt(name, 'john')                || ['john']        | '#name < john'                   | 't.name < ?'
        Cnd.lte(name, 'john')               || ['john']        | '#name <= john'                  | 't.name <= ?'
        Cnd.like(name, 'john')              || ['john']        | '#name LIKE john'                | 't.name LIKE ?'
        Cnd.notLike(name, 'john')           || ['john']        | '#name NOT LIKE john'            | 't.name NOT LIKE ?'
        Cnd.in(name, ['john', 'doe'])       || ['john', 'doe'] | '#name in [john, doe]'           | 't.name IN (?, ?)'
        Cnd.notIn(name, ['john', 'doe'])    || ['john', 'doe'] | '#name not in [john, doe]'       | 't.name NOT IN (?, ?)'
        Cnd.isNull(name)                    || []              | '#name is null'                  | 't.name IS NULL'
        Cnd.isNotNull(name)                 || []              | '#name is not null'              | 't.name IS NOT NULL'
        Cnd.between(name, 'john', 'doe')    || ['john', 'doe'] | '#name between john and doe'     | 't.name BETWEEN ? AND ?'
        Cnd.notBetween(name, 'john', 'doe') || ['john', 'doe'] | '#name not between john and doe' | 't.name NOT BETWEEN ? AND ?'
        Cnd.positive(name)                  || []              | '+#name'                         | '+t.name'
        Cnd.negate(name)                    || []              | '-#name'                         | '-t.name'
        Cnd.val(name)                       || [name]          | name.toString()                  | '?'  //val passes any object to the query even if it is a field object

    }

    def 'test transpiling with fluent builder'() {
        expect:
        def expr = cnd.asExpr()
        def query = EzySqlTranspiler.transpile(fields, expr)

        def actualSql = query.sql
        def actualParams = query.params
        def actualExpr = expr.toString()

        actualParams == params
        actualExpr == exprString
        actualSql == sql

        where:
        cnd                               || params          | exprString                       | sql
        name.eq('john')                   || ['john']        | '#name = john'                   | 't.name = ?'
        name.neq('john')                  || ['john']        | '#name <> john'                  | 't.name <> ?'
        name.gt('john')                   || ['john']        | '#name > john'                   | 't.name > ?'
        name.gte('john')                  || ['john']        | '#name >= john'                  | 't.name >= ?'
        name.lt('john')                   || ['john']        | '#name < john'                   | 't.name < ?'
        name.lte('john')                  || ['john']        | '#name <= john'                  | 't.name <= ?'
        name.like('john')                 || ['john']        | '#name LIKE john'                | 't.name LIKE ?'
        name.notLike('john')              || ['john']        | '#name NOT LIKE john'            | 't.name NOT LIKE ?'
        name.in(['john', 'doe'])          || ['john', 'doe'] | '#name in [john, doe]'           | 't.name IN (?, ?)'
        name.notIn(['john', 'doe'])       || ['john', 'doe'] | '#name not in [john, doe]'       | 't.name NOT IN (?, ?)'
        name.isNull()                     || []              | '#name is null'                  | 't.name IS NULL'
        name.isNotNull()                  || []              | '#name is not null'              | 't.name IS NOT NULL'
        name.between('john', 'doe')       || ['john', 'doe'] | '#name between john and doe'     | 't.name BETWEEN ? AND ?'
        name.notBetween('john', 'doe')    || ['john', 'doe'] | '#name not between john and doe' | 't.name NOT BETWEEN ? AND ?'
        name.positive()                   || []              | '+#name'                         | '+t.name'
        name.negate()                     || []              | '-#name'                         | '-t.name'
        name.and(age)                     || []              | '#name AND #age'                 | 't.name AND t.age'
        name.and("xxx")                   || ["xxx"]         | '#name AND xxx'                  | 't.name AND ?'
        name.or(age)                      || []              | '#name OR #age'                  | 't.name OR t.age'
        Conds.andAll(age, name).and(name) || []              | '(#age AND #name) AND #name'     | '(t.age AND t.name) AND t.name'
        Conds.orAll(age, age).and(name)   || []              | '(#age OR #age) AND #name'       | '(t.age OR t.age) AND t.name'


    }

}
