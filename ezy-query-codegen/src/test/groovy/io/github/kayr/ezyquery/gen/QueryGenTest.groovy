package io.github.kayr.ezyquery.gen

import groovy.transform.NamedVariant
import spock.lang.Specification


class QueryGenTest extends Specification {

    def "test ex1"() {

        def data = TestUtil.load('ex1')

        when:
        def generated = generateCode(data.v1)

        def expected = data.v2.trim()


        then:
        generated == expected
    }


    @NamedVariant
    private String generateCode(String sql, Properties config = new Properties(), String className = "MyQuery") {
        if (config.get("resultDto.addWither") == null) {
            config.setProperty("resultDto.addWither", "true")
        }

        new NoTimeQueryGen("mypackage.sql", className, sql, config).javaCode().toString().trim()
    }

    def "no joins test"() {

        def data = TestUtil.load('nojoin')


        when:
        def generated = generateCode(data.v1)

        def expected = data.v2.trim()

        then:
        generated == expected
    }

    def 'test multi statement'() {
        def data = TestUtil.load('multi-statement')
        when:
        def generated = generateCode(data.v1)
        def expected = data.v2.trim()

        then:
        generated == expected
    }


    @spock.lang.Ignore("not implemented nested classes")
    def "JavaCode3"() {

        def sql = '''        SELECT
                        td.id AS 'id_bigint',
                        td.id AS 'idByConfig',
                        tr.id AS 'request.id_bigint',
                        maw.id AS 'wallet.awamoWalletId_bigint',
                        mtw.id AS 'wallet.walletId_bigint',
                        mtw.account_no AS accountNumber_string,
                        td.amount AS amount_decimal,
                        tr.amount AS 'request.amount_decimal',
                        tr.tx_type  AS 'request.type_string',
                        td.tx_type  AS 'detail.type_string',
                        tr.tx_status  AS 'status',
                        '' as 'copied'
                    FROM
                        m_awamo_wallet maw
                    INNER JOIN m_tenant_wallet mtw ON
                        mtw.id = maw.wallet_id
                    INNER JOIN w_wallet_transaction_request tr ON
                        tr.wallet_id = mtw.id
                    INNER JOIN m_wallet_transaction_detail td ON
                        td.wallet_tx_request_id = tr.id
                        where 1 <> 8'''


        when:
        def code = new QueryGen("mypackage.sql", "MyQuery", sql, new Properties()).javaCode()
        then:
        1 == 1
    }

    def 'test with named params'() {
        def data = TestUtil.load('named-params')
        when:
        def generated = generateCode(data.v1)
        def expected = data.v2.trim()

        then:
        generated == expected
    }

    def 'test can read custom java types'() {
        def data = TestUtil.load('custom-java-types')
        when:
        def generated = generateCode(data.v1, data.v3)
        def expected = data.v2.trim()

        then:
        generated == expected
    }


    def 'test can read nested queries'() {
        def data = TestUtil.load('nested-select')
        when:
        def generated = generateCode(data.v1, data.v3)
        def expected = data.v2.trim()

        then:
        generated == expected
    }

    def 'test employee-customer-summary'() {
        def data = TestUtil.load('employee-customer-summary')
        when:
        def generated = generateCode(data.v1, data.v3)
        def expected = data.v2.trim()

        then:
        generated == expected
    }

    def 'test simple cte'() {
        def data = TestUtil.load('basic-cte')
        when:
        def generated = generateCode(data.v1, data.v3)
        def expected = data.v2.trim()

        then:
        generated == expected
    }

    def 'test multiple cte'() {
        def data = TestUtil.load('multi-cte')
        when:
        def generated = generateCode(data.v1, data.v3)
        def expected = data.v2.trim()


        then:
        generated == expected
    }

    def 'test with dynamic table name'() {
        def data = TestUtil.load('dyn-table')
        when:
        def generated = generateCode(data.v1)
        def expected = data.v2.trim()

        then:
        generated == expected
    }


}
