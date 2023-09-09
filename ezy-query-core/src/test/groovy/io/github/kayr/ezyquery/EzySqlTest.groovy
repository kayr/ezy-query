package io.github.kayr.ezyquery

import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.it.Db
import io.github.kayr.ezyquery.sql.Mappers
import io.github.kayr.ezyquery.sql.Zql
import io.github.kayr.ezyquery.testqueries.Offices
import spock.lang.Shared
import spock.lang.Specification

class EzySqlTest extends Specification {
    @Shared
    Db db

    void setupSpec() {
        db = new Db().insertData()
    }

    void cleanupSpec() {
        db.close()
    }

    def 'test that with connection works'() {
        given:
        def sql = EzySql.withConnection(db.ds.getConnection())

        when: 'when i make the first query every thing is fine'
        def count = sql.from(Offices.QUERY).count()
        then:
        count == 4

    }

    def 'test that count never returns null'() {
        given:
        def zql = Mock(Zql)
        def ezySql = EzySql.withZql(zql)


        when:
        def count = ezySql.from(Offices.QUERY).count()

        then:
        1 * zql.one(_, _, _) >> null
        count == 0
    }

    def 'test that may be one return an empty optional'() {
        when:
        def optional = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.eq(99999)).mayBeOne()
        then:
        !optional.isPresent()

    }

    def 'test that may be one return a present optional'() {
        when:
        def optional = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.eq(4)).mayBeOne()
        then:
        optional.isPresent()
        optional.get().code == '4'
        optional.get().country == 'KE'
        optional.get().addressLine == 'Nairobi'
    }


    def 'test that one throws an exception if no result is found'() {
        when:
        db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.eq(99999)).one()
        then:
        thrown(NoSuchElementException)
    }

    def 'test that one returns a result if one is found'() {
        db.ezySql()
        when:
        def office = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.eq(4)).one()
        then:
        office.code == '4'
        office.country == 'KE'
        office.addressLine == 'Nairobi'
    }

    def 'test that you can select specific fields'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .select(Offices.CODE, Offices.COUNTRY)
                .where(Offices.CODE.eq('4'))
                .list()
        then:
        offices.size() == 1
        offices[0].code == '4'
        offices[0].country == 'KE'
        offices[0].addressLine == null
    }

    def 'test that you can filter with string expression'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .where(Cnd.expr("code = '4'"))
                .list()
        then:
        offices.size() == 1
        offices[0].code == '4'

    }

    def 'test that you can use an offset'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .offset(1)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices.size() == 3
        offices[0].code == '2'
        offices[1].code == '3'
        offices[2].code == '4'

        when:
        def offices2 = db.ezySql().from(Offices.QUERY)
                .offset(3)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices2.size() == 1
        offices2[0].code == '4'
    }

    def 'test that you can use a limit'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .limit(2)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices.size() == 2
        offices[0].code == '1'
        offices[1].code == '2'

        when:
        def offices2 = db.ezySql().from(Offices.QUERY)
                .limit(1)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices2.size() == 1
        offices2[0].code == '1'
    }

    def 'test that you can use a limit and offset'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .limit(2)
                .offset(1)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices.size() == 2
        offices[0].code == '2'
        offices[1].code == '3'

        when:
        def offices2 = db.ezySql().from(Offices.QUERY)
                .limit(1)
                .offset(3)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices2.size() == 1
        offices2[0].code == '4'
    }

    def 'test that you can use a limit and offset single method'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.gt(1))
                .limit(2, 1)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices.size() == 2
        offices[0].code == '3'
        offices[1].code == '4'

        when:
        def offices2 = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.gt(1))
                .limit(1, 3)
                .orderBy(Offices.CODE.asc())
                .list()
        then:
        offices2.size() == 0
    }

    def 'test that you can sort ascending using field object'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .orderBy(Offices.CODE.desc())
                .list()
        then:
        offices.size() == 4
        offices[0].code == '4'
        offices[1].code == '3'
        offices[2].code == '2'
        offices[3].code == '1'

    }


    def 'test that you can sort using string expression'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .orderBy("code desc")
                .list()
        then:
        offices.size() == 4
        offices[0].code == '4'
        offices[1].code == '3'
        offices[2].code == '2'
        offices[3].code == '1'
    }

    def 'test that you can convert results to a map'() {
        when:
        def offices = db.ezySql().from(Offices.QUERY)
                .mapTo(Mappers.toMap())
                .orderBy(Offices.CODE.desc())
                .list()
        then:
        offices.first() instanceof Map
        offices.size() == 4
        offices[0].get('code') == '4'
        offices[1].code == '3'
        offices[2].code == '2'
        offices[3].code == '1'
    }

    def 'test get query returns a query'() {
        when:
        def query = db.ezySql().from(Offices.QUERY)
                .where(Offices.CODE.eq(4))
                .getQuery()
        then:
        query != null
        query.getSql() == "SELECT \n" +
                "  officeCode as \"code\", \n" +
                "  country as \"country\", \n" +
                "  addressLine1 as \"addressLine\"\n" +
                "FROM offices\n" +
                "WHERE officeCode = ?\n" +
                "LIMIT 50 OFFSET 0"
        query.params.size() == 1
        query.params[0] == 4
    }


}
