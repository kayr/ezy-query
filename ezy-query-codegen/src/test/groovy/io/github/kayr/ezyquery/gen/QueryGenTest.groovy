package io.github.kayr.ezyquery.gen

import spock.lang.Specification

class QueryGenTest extends Specification {

    def "test ex1"() {

        def data = load('ex1')

        when:
        def generated = generateCode(data.v1)


        def expected = data.v2.trim()
        then:
        generated == expected
    }

    private String generateCode(String sql) {
        new NoTimeQueryGen("mypackage.sql", "MyQuery", sql).javaCode().toString().trim()
    }

    def "no joins test"() {

        def data = load('nojoin')


        when:
        def generated = generateCode(data.v1)
        def expected = data.v2.trim()

        then:
        generated == expected
    }

    def 'test multi statement'() {
        def data = load('multi-statement')
        when:
        def generated = generateCode(data.v1)
        def expected = data.v2.trim()

        then:
        generated == expected
    }


    Tuple2<String, String> load(String path) {
        def sql = QueryGenTest.class.getResource("/generated/$path/in.sql.txt").text
        def java = QueryGenTest.class.getResource("/generated/$path/out.java.txt").text
        return new Tuple2(sql, java)
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
        def code = new QueryGen("mypackage.sql", "MyQuery", sql).javaCode()
        then:
        1 == 1
    }

    def 'test with named params'() {
        def data = load('named-params')
        when:
        def generated = generateCode(data.v1)
        println generated
        def expected = data.v2.trim()

        then:
        generated == expected
    }


}
