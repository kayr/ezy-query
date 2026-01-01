package io.github.kayr.ezyquery.itests

import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.sql.Zql
import spock.lang.Specification
import test.BinaryQuery

class TestBinaryDataTest extends Specification {

    Db db
    EzySql ez

    def setup() {
        db = new Db()
        db.init()
        ez = EzySql.withDataSource(db.ds)

        // Create table for binary data
        db.withDb { conn ->
            conn.createStatement().execute("CREATE TABLE binary_data (id INT PRIMARY KEY, data VARBINARY)")
        }
    }

    def cleanup() {
        db.close()
    }

    def 'test insertion and retrieval of binary data'() {
        given:
        def Q = BinaryQuery.selectBinary()
        byte[] originalData = [1, 2, 3, 4, 5] as byte[]
        int id = 1

        when: 'Inserting binary data'
        def insert = BinaryQuery.insert().id(id).data(Zql.raw(originalData)).query
        ez.zql.insertOne(insert)

        then: 'Retrieving and verifying'
        def result = ez.from(Q).where(Q.ID.eq(id)).one()
        result.data == originalData
    }

    def 'test deletion of binary data'() {
        given:
        def Q = BinaryQuery.selectBinary()
        byte[] originalData = [10, 20, 30] as byte[]
        int id = 2
        def insert = BinaryQuery.insert().id(id).data(Zql.raw(originalData)).query
        ez.zql.execute(insert)

        when: 'Deleting binary data'
        ez.zql.execute("DELETE FROM binary_data WHERE id = ?", id)

        then: 'Verifying deletion'
        ez.from(Q).where(Q.ID.eq(id)).list().isEmpty()
    }
}
