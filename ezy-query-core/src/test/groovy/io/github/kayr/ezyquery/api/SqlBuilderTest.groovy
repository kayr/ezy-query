package io.github.kayr.ezyquery.api

import io.github.kayr.ezyquery.EzyQueryWithResult
import io.github.kayr.ezyquery.api.*
import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.parser.QueryAndParams
import io.github.kayr.ezyquery.parser.SqlParts
import spock.lang.Shared
import spock.lang.Specification

class SqlBuilderTest extends Specification {

    def fields = [name, age, office, maxAge]

    def ezyQuery = new EzyQueryWithResult() {
        @Override
        QueryAndParams query(EzyCriteria params) {
            return null
        }

        @Override
        Class resultClass() {
            return null
        }

        @Override
        List<Field<?>> fields() {
            return fields
        }

        @Override
        SqlParts schema() {
            return SqlParts.of("my_table")
        }
    }
    @Shared
    private Field name = new Field('t.name', 'name')
    @Shared
    private Field age = new Field('t.age', 'age')
    @Shared
    private Field office = new Field('t.office', 'office')
    @Shared
    private Field maxAge = new Field('t.maxAge', 'maxAge')

    def "test build with a filter"() {


        def criteria = EzyCriteria.selectAll()
                .where(Cnd.expr("name = 'ronald'"))
                .where(Cnd.expr("age >  20"))
                .limit(10, 2)


        when:

        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age", \n' +
                '  t.office as "office", \n' +
                '  t.maxAge as "maxAge"\n' +
                'FROM my_table\n' +
                'WHERE ((t.name = ?) AND (t.age > ?))\n' +
                'LIMIT 10 OFFSET 2'


    }

    def "test build with filter API"() {


        def criteria = EzyCriteria.selectAll()
                .where(Cnd.orAll(Cnd.eq("#name", 'ronald'),
                        Cnd.gt('#age', 20)))
                .limit(10, 2)

        when:

        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:


        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age", \n' +
                '  t.office as "office", \n' +
                '  t.maxAge as "maxAge"\n' +
                'FROM my_table\n' +
                'WHERE (t.name = ? OR t.age > ?)\n' +
                'LIMIT 10 OFFSET 2'

    }

    def "test build with a filter and API"() {


        def criteria = EzyCriteria.selectAll()
                .where(Cnd.orAll(
                        Cnd.eq("#name", 'ronald'),
                        Cnd.gt('#age', 20),
                        Cnd.expr("office = 'NY'"),
                        Cnd.expr('maxAge > 30')))
                .limit(10, 2)

        when:
        def orQuery = SqlBuilder.buildSql(ezyQuery, criteria)


        then:


        orQuery.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age", \n' +
                '  t.office as "office", \n' +
                '  t.maxAge as "maxAge"\n' +
                'FROM my_table\n' +
                'WHERE (t.name = ? OR t.age > ? OR (t.office = ?) OR (t.maxAge > ?))\n' +
                'LIMIT 10 OFFSET 2'
        orQuery.params == ['ronald', 20, 'NY', 30]

    }

    def "test build with no filter"() {

        def criteria = EzyCriteria.selectAll()
                .limit(10, 2)

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age", \n' +
                '  t.office as "office", \n' +
                '  t.maxAge as "maxAge"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'LIMIT 10 OFFSET 2'
        query.params == []

    }


    def "test build with only offset uses default limit"() {

        def criteria = EzyCriteria.selectAll()
                .offset(2)

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age", \n' +
                '  t.office as "office", \n' +
                '  t.maxAge as "maxAge"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'LIMIT 50 OFFSET 2'
        query.params == []

    }

    def "test build uses default offset"() {
        def fields = [
                name,
                age,
                office,
                maxAge
        ]

        def criteria = EzyCriteria.selectAll()
                .limit(15)

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age", \n' +
                '  t.office as "office", \n' +
                '  t.maxAge as "maxAge"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'LIMIT 15 OFFSET 0'
        query.params == []

    }

    def "test builds count query"() {

        def criteria = EzyCriteria.selectCount()
                .limit(15)

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                ' COUNT(*) \n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)'
        query.params == []

    }

    def "test builds select query with specified columns"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'LIMIT 15 OFFSET 0'
        query.params == []

    }

    def "test builds select query with order by clause"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)
                .orderBy(
                        Sort.by('name', Sort.DIR.ASC),
                        Sort.by('age', Sort.DIR.DESC))

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'ORDER BY t.name ASC, t.age DESC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == []

    }

    def "test builds select query with order by clause using strings"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)
                .orderBy('name', 'age')

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'ORDER BY t.name ASC, t.age ASC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == []

    }

    def "test builds select query with order by clause using one string"() {

        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)
                .orderBy('name')

        when:
        def query = SqlBuilder.buildSql(ezyQuery, criteria)

        then:
        query.sql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table\n' +
                'WHERE (1 = 1)\n' +
                'ORDER BY t.name ASC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == []

    }

    def "test builds select query with default where clause"() {

        EzyQueryWithResult ezyQueryWithWhere = queryWithWhereClause()


        def criteria = EzyCriteria.select('name', 'age')
                .limit(15)
                .orderBy('name')

        when:
        def query = SqlBuilder.buildSql(ezyQueryWithWhere, criteria)


        def generatedSql = query.sql
        then:
        generatedSql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table\n' +
                'WHERE (t.name = 123) AND (1 = 1)\n' +
                'ORDER BY t.name ASC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == []

    }

    def "test builds select query with default where clause does not add extra parenthesis"() {

        EzyQueryWithResult ezyQueryWithWhere = queryWithWhereClause()


        def criteria = EzyCriteria.select('name', 'age')
                .where(name.eq("RK").and(age.eq(1)))
                .limit(15)
                .orderBy('name');

        when:
        def query = SqlBuilder.buildSql(ezyQueryWithWhere, criteria)


        def generatedSql = query.sql
        then:
        generatedSql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table\n' +
                'WHERE (t.name = 123) AND (t.name = ? AND t.age = ?)\n' +
                'ORDER BY t.name ASC\n' +
                'LIMIT 15 OFFSET 0'
        query.params == ['RK', 1]

    }

    private EzyQueryWithResult queryWithWhereClause() {
        def ezyQueryWithWhere = new EzyQueryWithResult() {
            @Override
            QueryAndParams query(EzyCriteria params) {
                return null
            }


            @Override
            List<Field<?>> fields() {
                return fields
            }

            @Override
            SqlParts schema() {
                return SqlParts.of("my_table")
            }

            @Override
            Optional<SqlParts> whereClause() {
                return Optional.of(SqlParts.of('t.name = 123'))
            }
        }
        ezyQueryWithWhere
    }


    def 'test can build query with with name parameters'() {
        def query = new EzyQueryAdapter() {
            @Override
            List<Field<?>> fields() {
                return fields
            }

            @Override
            SqlParts schema() {
                // my_table inner join my_other_table on my_table.id = my_other_table.id and :name = my_other_table.name  or :gender = my_other_table.gender
                return SqlParts.of(
                        SqlParts.textPart("my_table inner join my_other_table on my_table.id = my_other_table.id and "),
                        SqlParts.paramPart("name"),
                        SqlParts.textPart(" = my_other_table.name  or "),
                        SqlParts.textPart(" my_other_table.gender in ("),
                        SqlParts.paramPart("gender"),
                        SqlParts.textPart(")")

                )
            }
        }

        def criteria = EzyCriteria.select('name', 'age')
                .where(name.eq("RK"))
                .setParam(NamedParam.of("name"), "NAME VALUE")
                .setParam(NamedParam.of("gender"), ['M', 'F']);
        when:
        def queryAndParams = SqlBuilder.buildSql(query, criteria)
        def generatedSql = queryAndParams.sql
        def params = queryAndParams.params


        then:
        generatedSql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table inner join my_other_table on my_table.id = my_other_table.id and ? = my_other_table.name  or  my_other_table.gender in (?,?)\n' +
                'WHERE t.name = ?\n' +
                'LIMIT 50 OFFSET 0'

        params == ['NAME VALUE', 'M', 'F', 'RK']
    }

    def 'with params params in default where clause'() {
        def query = new EzyQueryAdapter() {
            @Override
            List<Field<?>> fields() {
                return fields
            }

            @Override
            SqlParts schema() {
                return SqlParts.of("my_table t")
            }

            @Override
            Optional<SqlParts> whereClause() {
                return Optional.of(SqlParts.of('t.name = :name and t.age in (:age)'))
            }
        }

        def criteria = EzyCriteria.select('name', 'age')
                .where(name.eq("RK"))
                .setParam(NamedParam.of("name"), "NAME VALUE")
                .setParam(NamedParam.of("age"), [10, 15, 20]);
        when:
        def queryAndParams = SqlBuilder.buildSql(query, criteria)
        def generatedSql = queryAndParams.sql
        def params = queryAndParams.params

        println generatedSql


        then:
        generatedSql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table t\n' +
                'WHERE (t.name = ? and t.age in (?,?,?)) AND (t.name = ?)\n' +
                'LIMIT 50 OFFSET 0'

        params == ['NAME VALUE', 10, 15, 20, 'RK']
    }

    def 'with params in the order by clause'() {
        def query = new EzyQueryAdapter() {
            @Override
            List<Field<?>> fields() {
                return fields
            }

            @Override
            SqlParts schema() {
                return SqlParts.of("my_table t")
            }

            @Override
            Optional<SqlParts> orderByClause() {
                return Optional.of(SqlParts.of(' t.age + :age1 * :weight desc'))
            }
        }


        def criteria = EzyCriteria.select('name', 'age')
                .where(name.eq("RK"))
                .setParam(NamedParam.of("age1"), 10)
                .setParam(NamedParam.of("weight"), 15)


        when:
        def queryAndParams = SqlBuilder.buildSql(query, criteria)
        def generatedSql = queryAndParams.sql
        def params = queryAndParams.params


        then:
        generatedSql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table t\n' +
                'WHERE t.name = ?\n' +
                ' ORDER BY  t.age + ? * ? desc\n' +
                'LIMIT 50 OFFSET 0'

        params == ['RK', 10, 15]
    }

    def 'with params in all clauses'() {
        def query = new EzyQueryAdapter() {
            @Override
            List<Field<?>> fields() {
                return fields
            }

            @Override
            SqlParts schema() {
                return SqlParts.of("my_table t inner join my_other_table t2 on t.id = t2.id and t.name = :name and t.age in (:ages)")
            }

            @Override
            Optional<SqlParts> whereClause() {
                return Optional.of(SqlParts.of('t.name = :name and t.age in (:ages)'))
            }

            @Override
            Optional<SqlParts> orderByClause() {
                return Optional.of(SqlParts.of(' t.age + :age1 * :weight desc'))
            }
        }

        def criteria = EzyCriteria.select('name', 'age')
                .where(name.eq("RK"))
                .setParam(NamedParam.of("name"), "NAME VALUE")
                .setParam(NamedParam.of("ages"), [10, 15, 20])
//                .setParam(NamedParam.of("age1"), 10) //Don't set this param since it is overridden in the order by clause
//                .setParam(NamedParam.of("weight"), 15) //Don't set this param since it is overridden in the order by clause
                .orderBy(age.asc())

        when:
        def queryAndParams = SqlBuilder.buildSql(query, criteria)
        def generatedSql = queryAndParams.sql
        def params = queryAndParams.params

        then:
        generatedSql == 'SELECT \n' +
                '  t.name as "name", \n' +
                '  t.age as "age"\n' +
                'FROM my_table t inner join my_other_table t2 on t.id = t2.id and t.name = ? and t.age in (?,?,?)\n' +
                'WHERE (t.name = ? and t.age in (?,?,?)) AND (t.name = ?)\n' +
                'ORDER BY t.age ASC\n' + //default order by is overridden
                'LIMIT 50 OFFSET 0'

        params == ['NAME VALUE', 10, 15, 20, 'NAME VALUE', 10, 15, 20, 'RK']
    }


    static abstract class EzyQueryAdapter implements EzyQueryWithResult {
        @Override
        QueryAndParams query(EzyCriteria params) {
            return null
        }

        @Override
        List<Field<?>> fields() {
            return null
        }

        @Override
        SqlParts schema() {
            return null
        }
    }
}