package io.github.kayr.ezyquery.gen.walkers

import net.sf.jsqlparser.expression.JdbcNamedParameter
import net.sf.jsqlparser.expression.ExpressionVisitor
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.select.*
import net.sf.jsqlparser.statement.piped.FromQuery
import spock.lang.Specification
import spock.lang.Unroll

class QueryTraversalTest extends Specification {
    @Unroll
    def 'discovers predicate through #expression'() {
        given:
        def select = CCJSqlParserUtil.parse("SELECT t.id AS id FROM things t WHERE $expression")

        when:
        def found = DynamicQueriesFinder.lookup(select)

        then:
        found.keySet() == ['_ezy_filter'] as Set
        found._ezy_filter.select.is(select)

        where:
        expression << ['t.id = :ordinary AND :_ezy_filter',
                       'COALESCE(:_ezy_filter, TRUE)',
                       'CASE WHEN t.id > 0 THEN :_ezy_filter ELSE TRUE END',
                       'f(flag => :_ezy_filter)']
    }

    @Unroll
    def 'direct predicate marker in #clause belongs to its SELECT'() {
        given:
        def select = CCJSqlParserUtil.parse(sql)

        expect:
        DynamicQueriesFinder.lookup(select)._ezy_filter.select.is(select)

        where:
        clause    | sql
        'JOIN ON' | 'SELECT t.id FROM things t JOIN others o ON t.id = o.id AND :_ezy_filter'
        'HAVING'  | 'SELECT t.id FROM things t GROUP BY t.id HAVING COUNT(*) > 0 AND :_ezy_filter'
    }

    @Unroll
    def 'discovers inner query reached through #clause'() {
        given:
        def inner = '(SELECT i.id AS innerId FROM items i WHERE :_ezy_inner)'
        def sql = template.replace('@', inner)
        def outer = CCJSqlParserUtil.parse(sql)

        when:
        def found = DynamicQueriesFinder.lookup(outer)
        def owner = found._ezy_inner

        then:
        owner.select.plainSelect.selectItems*.alias*.name == ['innerId']
        owner.parent.select.is(outer)
        found.keySet() == ['_ezy_inner'] as Set

        where:
        clause       | template
        'projection' | 'SELECT @ AS value FROM things t'
        'function'   | 'SELECT COALESCE(@, 0) AS value FROM things t'
        'JOIN ON'    | 'SELECT t.id FROM things t JOIN others o ON o.id = @'
        'WHERE IN'   | 'SELECT t.id FROM things t WHERE t.id IN @'
        'EXISTS'     | 'SELECT t.id FROM things t WHERE EXISTS @'
        'GROUP BY'   | 'SELECT t.id FROM things t GROUP BY @'
        'HAVING'     | 'SELECT t.id FROM things t GROUP BY t.id HAVING COUNT(*) > @'
        'ORDER BY'   | 'SELECT t.id FROM things t ORDER BY @'
        'LIMIT'      | 'SELECT t.id FROM things t LIMIT @'
        'OFFSET'     | 'SELECT t.id FROM things t OFFSET @'
        'derived'    | 'SELECT t.innerId FROM @ t'
        'lateral'    | 'SELECT t.innerId FROM LATERAL @ t'
        'CTE'        | 'WITH c AS @ SELECT t.innerId FROM c t'
        'FROM group' | 'SELECT t.id FROM (things t JOIN @ i ON i.innerId = t.id)'
    }

    def 'nested queries restore parent before visiting the next outer marker'() {
        given:
        def outer = CCJSqlParserUtil.parse('''
            SELECT o.id FROM orders o
            WHERE EXISTS (SELECT i.id FROM items i WHERE :_ezy_inner)
              AND :_ezy_outer''')

        when:
        def found = DynamicQueriesFinder.lookup(outer)

        then:
        found._ezy_outer.select.is(outer)
        found._ezy_inner.parent.is(found._ezy_outer)
        found._ezy_outer.parent == null
    }

    @Unroll
    def 'compound #operator owns branch markers separately'() {
        given:
        def outer = CCJSqlParserUtil.parse("""
            SELECT q.id FROM (
                SELECT a.id FROM a WHERE :_ezy_left
                $operator SELECT b.id FROM b WHERE :_ezy_right
            ) q WHERE :_ezy_outer""")

        when:
        def found = DynamicQueriesFinder.lookup(outer)

        then:
        found._ezy_left.select instanceof PlainSelect
        found._ezy_right.select instanceof PlainSelect
        !found._ezy_left.select.is(found._ezy_right.select)
        found._ezy_left.select.fromItem.toString() == 'a'
        found._ezy_right.select.fromItem.toString() == 'b'
        found._ezy_left.parent.is(found._ezy_right.parent)
        found._ezy_outer.select.is(outer)

        where:
        operator << ['UNION', 'INTERSECT', 'EXCEPT']
    }

    @Unroll
    def 'compound-owned marker in #clause fails descriptively'() {
        when:
        DynamicQueriesFinder.lookup("SELECT q.id FROM (SELECT id FROM a UNION SELECT id FROM b $clause) q")

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('_ezy_compound')
        error.message.contains('SetOperationList')

        where:
        clause << ['ORDER BY :_ezy_compound']
    }

    def 'an explicitly compound-owned LIMIT marker fails descriptively'() {
        given:
        def outer = CCJSqlParserUtil.parse('SELECT q.id FROM (SELECT id FROM a UNION SELECT id FROM b) q')
        // 5.4 parses a trailing textual LIMIT onto the last branch; exercise the compound AST here.
        outer.fromItem.select.limit = new net.sf.jsqlparser.statement.select.Limit()
                .withRowCount(new JdbcNamedParameter('_ezy_compound'))

        when:
        DynamicQueriesFinder.lookup(outer)

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('_ezy_compound')
        error.message.contains('SetOperationList')
    }

    def 'derived wrapper visits its CTE and ORDER BY children once while retaining the underlying owner'() {
        given:
        def outer = CCJSqlParserUtil.parse('SELECT q.id FROM (SELECT id FROM items WHERE :_ezy_owner) q WHERE :_ezy_outer')
        def wrapper = outer.fromItem
        def underlying = wrapper.select
        def visits = []
        underlying.where = countingMarker('_ezy_owner', visits)
        wrapper.withItemsList = CCJSqlParserUtil.parse(
                'WITH c AS (SELECT id FROM cte_items WHERE :_ezy_cte) SELECT id FROM c').withItemsList
        wrapper.withItemsList[0].select.plainSelect.where = countingMarker('_ezy_cte', visits)
        def sorting = CCJSqlParserUtil.parseExpression('(SELECT id FROM sorting WHERE :_ezy_sort)')
        sorting.plainSelect.where = countingMarker('_ezy_sort', visits)
        wrapper.orderByElements = [new OrderByElement().withExpression(sorting)]

        when:
        def found = DynamicQueriesFinder.lookup(outer)

        then:
        visits == ['_ezy_cte', '_ezy_owner', '_ezy_sort']
        found._ezy_owner.select.is(underlying)
        found._ezy_owner.parent.select.is(outer)
        found._ezy_cte.parent.is(found._ezy_owner)
        found._ezy_sort.parent.is(found._ezy_owner)
        found._ezy_outer.select.is(outer)
    }

    def 'compound derived wrapper rejects a marker in its own ORDER BY'() {
        given:
        def outer = CCJSqlParserUtil.parse('SELECT q.id FROM (SELECT id FROM a UNION SELECT id FROM b) q')
        outer.fromItem.orderByElements = [new OrderByElement().withExpression(new JdbcNamedParameter('_ezy_wrapper'))]

        when:
        DynamicQueriesFinder.lookup(outer)

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('_ezy_wrapper')
        error.message.contains('SetOperationList')
    }

    @Unroll
    def 'unsupported #kind preserves outer ownership through #surface dispatch'() {
        given:
        def outer = CCJSqlParserUtil.parse('SELECT id FROM items WHERE :_ezy_outer')
        def hidden = CCJSqlParserUtil.parse('SELECT id FROM hidden WHERE :_ezy_hidden')
        def unsupported = [
                values: new Values().addExpressions(new JdbcNamedParameter('_ezy_hidden')),
                pivot: new PivotQuery().withFromItem(hidden),
                piped: new FromQuery(hidden)
        ][kind]
        def context = WalkContext.create()
        context.startedSelect(outer)

        when:
        if (surface == 'SELECT') {
            unsupported.accept(context.selectWalker, context)
        } else {
            unsupported.accept(context.fromWalker, context)
        }
        context.namedParamVisited(new JdbcNamedParameter('_ezy_outer'))
        context.endedSelect(outer)

        then:
        context.dynamicSelects.keySet() == ['_ezy_outer'] as Set
        context.dynamicSelects._ezy_outer.select.is(outer)

        where:
        kind     | surface
        'values' | 'FROM'
        'pivot'  | 'SELECT'
        'pivot'  | 'FROM'
        'piped'  | 'FROM'
    }

    private static JdbcNamedParameter countingMarker(String name, List<String> visits) {
        new JdbcNamedParameter(name) {
            @Override
            def <T, S> T accept(ExpressionVisitor<T> visitor, S context) {
                visits << name
                super.accept(visitor, context)
            }
        }
    }

    def 'existing duplicate names keep the last visited owner'() {
        given:
        def outer = CCJSqlParserUtil.parse('''
            SELECT q.id FROM (SELECT id FROM items WHERE :_ezy_same) q
            WHERE :_ezy_same''')

        expect:
        DynamicQueriesFinder.lookup(outer)._ezy_same.select.is(outer)
    }

    def 'visits nested CTE marker exactly once without mutating SQL'() {
        given:
        def statement = CCJSqlParserUtil.parse('''
            SELECT o.id FROM orders o WHERE EXISTS (
              WITH c AS (SELECT id FROM items WHERE :_ezy_items)
              SELECT id FROM c)''')
        def visits = []
        def marker = countingMarker('_ezy_items', visits)
        def nested = statement.plainSelect.where.rightExpression
        nested.select.withItemsList[0].select.plainSelect.where = marker
        def original = statement.toString()

        when:
        DynamicQueriesFinder.lookup(statement)

        then:
        visits == ['_ezy_items']
        statement.toString() == original
    }
}
