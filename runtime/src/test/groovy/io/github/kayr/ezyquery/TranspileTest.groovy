package io.github.kayr.ezyquery

import spock.lang.Specification

class TranspileTest extends Specification {

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

        def result = EzySql.transpile(fields, expr)
        println(result)
        then:
        result.toString() == 'Result{sql=\'? = t.name AND t.age in (, ?, ?, ?, t.maxAge) OR (t.office = ? AND t.age > ?)\', params=[Julius Ceaser, 10, 20, 30, London, 20]}'
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
