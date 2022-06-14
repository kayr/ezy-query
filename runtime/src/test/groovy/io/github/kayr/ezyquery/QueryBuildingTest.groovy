package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.api.Cnd
import io.github.kayr.ezyquery.api.Conds
import spock.lang.Specification

class QueryBuildingTest extends Specification {

    def 'test build a query'() {
        when:
        def expr = Conds.and(
                Cnd.eq("name", 'ronald'),
                Cnd.gt('age', 20),
                Conds.or(Cnd.gte("lastName", 'mah'),
                        Cnd.lte("lastName1", 'mah'),
                        Cnd.and("lastName2", Cnd.neq('sex', 'm'))))
                .asExpr()

        println(expr)

        then:
        expr.toString() == '(name = ronald AND age > 20 AND (lastName >= mah OR lastName1 <= mah OR lastName2 AND sex <> m))'

    }
}
