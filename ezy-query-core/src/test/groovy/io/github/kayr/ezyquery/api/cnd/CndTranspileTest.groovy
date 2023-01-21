package io.github.kayr.ezyquery.api.cnd

import io.github.kayr.ezyquery.api.Field
import io.github.kayr.ezyquery.parser.EzySqlTranspiler
import spock.lang.Specification
import spock.lang.Unroll

class CndTranspileTest extends Specification {

    private Field name = new Field('t.name', 'name')
    private Field age = new Field('t.age', 'age')
    private Field office = new Field('t.office', 'office')
    private Field maxAge = new Field('t.maxAge', 'maxAge')

    def fields = [name, age, office, maxAge]

    @Unroll
    def "test transpiling"() {
        expect:
        def expr = cnd.asExpr()
        def query = EzySqlTranspiler.transpile(fields, expr)
        query.sql == sql
        query.params == params
        expr.toString() == exprString

        where:
        cnd                                                          || params                   | exprString                 | sql
        Cnd.eq("name", 'john')                                       || ['name', 'john']         | 'name = john'              | '? = ?'
        Cnd.neq("name", 'john')                                      || ['name', 'john']         | 'name <> john'             | '? <> ?'
        Cnd.gt("name", 'john')                                       || ['name', 'john']         | 'name > john'              | '? > ?'
        Cnd.gte("name", 'john')                                      || ['name', 'john']         | 'name >= john'             | '? >= ?'
        Cnd.lt("name", 'john')                                       || ['name', 'john']         | 'name < john'              | '? < ?'
        Cnd.lte("name", 'john')                                      || ['name', 'john']         | 'name <= john'             | '? <= ?'
        Cnd.like("name", 'john')                                     || ['name', 'john']         | 'name LIKE john'           | '? LIKE ?'
        Cnd.notLike("name", 'john')                                  || ['name', 'john']         | 'name NOT LIKE john'       | '? NOT LIKE ?'
        Cnd.in("name", ['john'])                                     || ['name', 'john']         | 'name in [john]'           | '? IN (?)'
        Cnd.notIn("name", ['john'])                                  || ['name', 'john']         | 'name not in [john]'       | '? NOT IN (?)'
        Cnd.notIn("name", ['john', 'doe'])                           || ['name', 'john', 'doe']  | 'name not in [john, doe]'  | '? NOT IN (?, ?)'
        Cnd.in("name", ['john', 'doe'])                              || ['name', 'john', 'doe']  | 'name in [john, doe]'      | '? IN (?, ?)'
        Cnd.isNull("name")                                           || ['name']                 | 'name is null'             | '? IS NULL'
        Cnd.isNotNull("name")                                        || ['name']                 | 'name is not null'         | '? IS NOT NULL'
        Cnd.val("name")                                              || ['name']                 | 'name'                     | '?'
        Cnd.val("name").and(Cnd.eq("name", 'john'))                  || ['name', 'name', 'john'] | '(name AND name = john)'   | '(? AND ? = ?)'
        Cnd.val("name").or(Cnd.eq("name", 'john'))                   || ['name', 'name', 'john'] | '(name OR name = john)'    | '(? OR ? = ?)'
        Cnd.not(Cnd.eq("name", 'john'))                              || ['name', 'john']         | 'not(name = john)'         | 'NOT(? = ?)'
        Cnd.every(Cnd.val("true"), Cnd.val("false"), Cnd.val("foo")) || ['true', 'false', 'foo'] | '(true AND false AND foo)' | '(? AND ? AND ?)'
        Cnd.any(Cnd.val("true"), Cnd.val("false"), Cnd.val("foo"))   || ['true', 'false', 'foo'] | '(true OR false OR foo)'   | '(? OR ? OR ?)'
        Cnd.trueCnd()                                                || [1, 1]                   | '1 = 1'                    | '? = ?'
        Cnd.positive("10")                                           || ['10']                   | '+10'                      | '+?'
        Cnd.negate("10")                                             || ['10']                   | '-10'                      | '-?'

    }

    public void 'test combining multiple expressions'() {
        expect:
        def expr = cnd.asExpr()
        def query = EzySqlTranspiler.transpile(fields, expr)
        def actualSql = query.sql
        def actualParams = query.params
        def actualEzyExpr = expr.toString()
        actualSql == sql
        actualParams == params
        actualEzyExpr == exprString

        where:
        cnd                                                                                              || params                                        | exprString                                            | sql
        Cnd.eq("name", 'john').and(Cnd.eq("age", 10))                                                    || ['name', 'john', 'age', 10]                   | '(name = john AND age = 10)'                          | '(? = ? AND ? = ?)'
        Cnd.eq("name", 'john').or(Cnd.eq("age", 10))                                                     || ['name', 'john', 'age', 10]                   | '(name = john OR age = 10)'                           | '(? = ? OR ? = ?)'
        Cnd.eq("name", 'john').and(Cnd.eq("age", 10)).or(Cnd.eq("office", 'hcm'))                        || ['name', 'john', 'age', 10, 'office', 'hcm']  | '((name = john AND age = 10) OR office = hcm)'        | '((? = ? AND ? = ?) OR ? = ?)'
        Cnd.every(Cnd.val("true"), Cnd.val("false"), Cnd.val("foo")).and(Cnd.val("bar"))                 || ['true', 'false', 'foo', 'bar']               | '(true AND false AND foo AND bar)'                    | '(? AND ? AND ? AND ?)'
        Cnd.every(Cnd.val("true"), Cnd.val("false"), Cnd.val("foo")).or(Cnd.val("bar"))                  || ['true', 'false', 'foo', 'bar']               | '((true AND false AND foo) OR bar)'                   | '((? AND ? AND ?) OR ?)'
        Cnd.every(Cnd.val("true"), Cnd.val("false"), Cnd.val("foo")).or(Cnd.every("bar", "foo", "car"))  || ['true', 'false', 'foo', 'bar', 'foo', 'car'] | '((true AND false AND foo) OR (bar AND foo AND car))' | '((? AND ? AND ?) OR (? AND ? AND ?))'
        Cnd.every(Cnd.val("true"), Cnd.val("false"), Cnd.val("foo")).and(Cnd.every("bar", "foo", "car")) || ['true', 'false', 'foo', 'bar', 'foo', 'car'] | '(true AND false AND foo AND bar AND foo AND car)'  | '(? AND ? AND ? AND ? AND ? AND ?)'

    }


}
