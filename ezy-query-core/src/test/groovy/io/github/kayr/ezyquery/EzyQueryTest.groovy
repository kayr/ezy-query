package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.api.EzyCriteria
import io.github.kayr.ezyquery.api.Field
import io.github.kayr.ezyquery.api.Sort
import io.github.kayr.ezyquery.api.cnd.Cnd
import spock.lang.Specification

class EzyQueryTest extends Specification {

    def fields = [
            new Field('t.name', 'name'),
            new Field('t.age', 'age'),
            new Field('t.office', 'office'),
            new Field('t.maxAge', 'maxAge')
    ]

    def "test build with a filter"() {


        def criteria = EzyCriteria.selectAll()
                .where("name = 'ronald'")
                .where("age >  20")
                .limit(10, 2)

        when:
        def orQuery = EzyQuery.buildQueryAndParams(criteria.useOr(), fields, "my_table")

        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE t.name = ? AND t.age > ?\n' +
                'LIMIT 10 OFFSET 2'

        orQuery.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE t.name = ? OR t.age > ?\n' +
                'LIMIT 10 OFFSET 2'

    }

    def "test build with filter API"() {


        def criteria = EzyCriteria.selectAll()
                .where(Cnd.eq("#name", 'ronald'))
                .where(Cnd.gt('#age', 20))
                .limit(10, 2)

        when:
        def orQuery = EzyQuery.buildQueryAndParams(criteria.useOr(), fields, "my_table")

        def query = EzyQuery.buildQueryAndParams(criteria.useOr().useAnd(), fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE t.name = ? AND t.age > ?\n' +
                'LIMIT 10 OFFSET 2'

        orQuery.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE t.name = ? OR t.age > ?\n' +
                'LIMIT 10 OFFSET 2'

    }

    def "test build with a filter and API"() {


        def criteria = EzyCriteria.selectAll()
                .where(Cnd.eq("#name", 'ronald'))
                .where(Cnd.gt('#age', 20))
                .where("office = 'NY'")
                .where('maxAge > 30')
                .limit(10, 2)

        when:
        def orQuery = EzyQuery.buildQueryAndParams(criteria.useOr(), fields, "my_table")

        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE (t.office = ? AND t.maxAge > ?) AND (t.name = ? AND t.age > ?)\n' +
                'LIMIT 10 OFFSET 2'
        query.params == ['NY', 30, 'ronald', 20]

        orQuery.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE (t.office = ? OR t.maxAge > ?) OR (t.name = ? OR t.age > ?)\n' +
                'LIMIT 10 OFFSET 2'

    }

    def "test build with no filter"() {

        def criteria = EzyCriteria.selectAll()
                .limit(10, 2)

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'LIMIT 10 OFFSET 2'
        query.params == [1, 1]

    }


    def "test build with only offset uses default limit"() {

        def criteria = EzyCriteria.selectAll()
                .offset(2)

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'LIMIT 50 OFFSET 2'
        query.params == [1, 1]

    }

    def "test build uses default offset"() {
        def fields = [
                new Field('t.name', 'name'),
                new Field('t.age', 'age'),
                new Field('t.office', 'office'),
                new Field('t.maxAge', 'maxAge')
        ]

        def criteria = EzyCriteria.selectAll()
                .limit(15)

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age, \n' +
                '  t.office as office, \n' +
                '  t.maxAge as maxAge\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'LIMIT 15 OFFSET 0'
        query.params == [1, 1]

    }

    def "test builds count query"() {

        def criteria = EzyCriteria.selectCount()
                .limit(15)

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                ' COUNT(*) \n' +
                'FROM my_table\n' +
                'WHERE ? = ?'
        query.params == [1, 1]

    }

    def "test builds select query with specified columns"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'LIMIT 15 OFFSET 0'
        query.params == [1, 1]

    }

    def "test builds select query with order by clause"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)
                .orderBy(
                        Sort.by('name', Sort.DIR.ASC),
                        Sort.by('age', Sort.DIR.DESC))

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'ORDER BY t.name ASC, t.age DESC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == [1, 1]

    }

    def "test builds select query with order by clause using strings"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)
                .orderBy('name', 'age')

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'ORDER BY t.name ASC, t.age ASC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == [1, 1]

    }

    def "test builds select query with order by clause using one string"() {

        def criteria = EzyCriteria.select('name','age')
                .limit(15)
                .orderBy('name')

        when:
        def query = EzyQuery.buildQueryAndParams(criteria, fields, "my_table")

        then:
        query.sql == 'SELECT \n' +
                '  t.name as name, \n' +
                '  t.age as age\n' +
                'FROM my_table\n' +
                'WHERE ? = ?\n' +
                'ORDER BY t.name ASC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == [1, 1]

    }
}
