package io.github.kayr.ezyquery.gen

import org.junit.Ignore
import spock.lang.Specification

class QueryGenTest extends Specification {
    def "JavaCode"() {

        def sql = '''        
                    SELECT
                        td.id AS 'id_bigint',
                        td.long_ AS 'longval_long',
                        td.int_ AS 'intval_int',
                        tr.float_ AS 'floatval_float',
                        maw.double_ AS 'doubleval_double',
                        mtw.string_ AS 'stringval_string',
                        mtw.boolean_ AS 'booleanval_boolean',
                        tr.date_ AS 'dateval_date',
                        tr.time AS 'timeval_time',
                        tr.bigint_ AS 'bigintval_bigint',
                        tr.bigdecimal_ AS 'bigdecimalval_decimal',
                        tr.blob_ AS 'blobval_byte',
                        tr.object_ AS 'objectval_object'
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
        def code = new QueryGen(sql, "MyQuery","mypackage.queries").javaCode()
        println(code)
        then:
        1 == 1
    }

    @Ignore("not implemented nested classes")
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
        def code = new QueryGen(sql, "MyQuery","mypackage.sql").javaCode()
        println(code)
        then:
        1 == 1
    }


}
