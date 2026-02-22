package io.github.kayr.ezyquery.sql

import io.github.kayr.ezyquery.it.Db
import io.github.kayr.ezyquery.sql.Zql.Query
import spock.lang.Shared
import spock.lang.Specification

/*
This test is mostly AI Generated. cause I am too lazy
 */
class ZqlTest extends Specification {
    @Shared
    Db db

    void setupSpec() {
        db = new Db().insertData()
    }

    void cleanupSpec() {
        db.close()
    }

    def 'test rows with mapper and parameters as list'() {
        given:
        def zql = db.ezySql().zql

        when:
        def offices = zql.rows(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [1])

        then:
        offices.size() == 1
        offices[0].officeCode == '1'
        offices[0].country == 'UG'
        offices[0].addressLine1 == 'Kampala'
    }

    def 'test rows with mapper and varargs parameters'() {
        given:
        def zql = db.ezySql().zql

        when:
        def offices = zql.rows(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", 2)

        then:
        offices.size() == 1
        offices[0].officeCode == '2'
        offices[0].country == 'KE'
        offices[0].addressLine1 == 'Nairobi'
    }

    def 'test oneRow with mapper and parameters'() {
        given:
        def zql = db.ezySql().zql

        when:
        def office = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [3])

        then:
        office != null
        office.officeCode == '3'
        office.country == 'TZ'
        office.addressLine1 == 'Dar es Salaam'
    }

    def 'test oneRow returns null when no records found'() {
        given:
        def zql = db.ezySql().zql

        when:
        def office = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [99])

        then:
        office == null
    }

    def 'test oneRow throws exception when multiple records found'() {
        given:
        def zql = db.ezySql().zql

        when:
        def rows = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE country = ?", ['KE'])

        then:
        thrown(IllegalArgumentException)
    }

    def 'test one method with parameters'() {
        given:
        def zql = db.ezySql().zql

        when:
        def count = zql.one(Integer.class, "SELECT COUNT(*) FROM offices", [])

        then:
        count == 4
    }

    def 'test one method returns null when no records found'() {
        given:
        def zql = db.ezySql().zql

        when:
        def result = zql.one(String.class, "SELECT officeCode FROM offices WHERE officeCode = ?", [99])

        then:
        result == null
    }

    def 'test query method with result set consumer'() {
        given:
        def zql = db.ezySql().zql

        when:
        def result = zql.query("SELECT * FROM offices ORDER BY officeCode", [], { rs ->
            def offices = []
            while (rs.next()) {
                offices << [code: rs.getString("officeCode"), country: rs.getString("country")]
            }
            return offices
        })

        then:
        result.size() == 4
        result[0].code == '1'
        result[0].country == 'UG'
        result[3].code == '4'
        result[3].country == 'KE'
    }

    def 'test executeUpdate with direct parameters'() {
        given:
        def zql = db.ezySql().zql

        when:
        def rowsAffected = zql.update("UPDATE offices SET country = ? WHERE officeCode = ?", ['US', '1'])

        then:
        rowsAffected == 1

        when:
        def office = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [1])

        then:
        office.country == 'US'
    }

    def 'test executeUpdate with Query object'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "UPDATE offices SET country = ? WHERE officeCode = ?"
            }

            @Override
            List<Object> getParams() {
                return ['CA', '2']
            }
        }

        when:
        def rowsAffected = zql.update(query)

        then:
        rowsAffected == 1

        when:
        def office = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [2])

        then:
        office.country == 'CA'
    }

    def 'test executeUpdate with list parameters'() {
        given:
        def zql = db.ezySql().zql

        when:
        def rowsAffected = zql.update("UPDATE offices SET country = ? WHERE officeCode = ?", ['UK', '3'])

        then:
        rowsAffected == 1

        when:
        def office = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [3])

        then:
        office.country == 'UK'
    }

    def 'test insert with executeUpdate'() {
        given:
        def zql = db.ezySql().zql

        when:
        def rowsAffected = zql.update(
                "INSERT INTO offices (officeCode, country, addressLine1) VALUES (?, ?, ?)",
                ['5', 'FR', 'Paris']
        )

        then:
        rowsAffected == 1

        when:
        def office = zql.oneRow(Mappers.toMap(), "SELECT * FROM offices WHERE officeCode = ?", [5])

        then:
        office != null
        office.officeCode == '5'
        office.country == 'FR'
        office.addressLine1 == 'Paris'
    }

    def 'test delete with executeUpdate'() {
        given:
        def zql = db.ezySql().zql

        // Create a dedicated test table
        zql.execute("""
        CREATE TABLE IF NOT EXISTS test_delete_table (
            id VARCHAR(10) PRIMARY KEY,
            name VARCHAR(100),
            location VARCHAR(100)
        )
    """, [])

        // Insert a record to be deleted
        zql.update(
                "INSERT INTO test_delete_table (id, name, location) VALUES (?, ?, ?)",
                ['TEST1', 'Test Office', 'Berlin']
        )

        when:
        def rowsAffected = zql.update("DELETE FROM test_delete_table WHERE id = ?", ['TEST1'])

        then:
        rowsAffected == 1

        when:
        def record = zql.oneRow(Mappers.toMap(), "SELECT * FROM test_delete_table WHERE id = ?", ['TEST1'])

        then:
        record == null

        cleanup:
        // Drop the test table to clean up
        zql.execute("DROP TABLE IF EXISTS test_delete_table", [])
    }

    def 'test insertAndGetKey returns generated primary key'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"
        def params = ['Monitor', 349.99, 'Electronics']

        when:
        def generatedKey = zql.insertOne(sql, params)

        then:
        generatedKey != null
        generatedKey instanceof Integer
        (Integer) generatedKey > 0

        // Verify the record was inserted
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM items WHERE id = ?", [generatedKey])
        item.name == 'Monitor'
        item.price == 349.99
        item.category == 'Electronics'
    }

    def 'test insertAndGetKey with varargs parameters'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"

        when:
        def generatedKey = zql.insertOne(sql, 'Keyboard', 79.99, 'Accessories')

        then:
        generatedKey != null
        generatedKey instanceof Integer
        (Integer) generatedKey > 0

        // Verify the record was inserted
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM items WHERE id = ?", [generatedKey])
        item.name == 'Keyboard'
        item.price == 79.99
        item.category == 'Accessories'
    }

    def 'test insertAndGetKey with Query object'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"
            }

            @Override
            List<Object> getParams() {
                return ['Mouse', 29.99, 'Accessories']
            }
        }

        when:
        def generatedKey = zql.insertOne(query)

        then:
        generatedKey != null
        generatedKey instanceof Integer
        (Integer) generatedKey > 0

        // Verify the record was inserted
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM items WHERE id = ?", [generatedKey])
        item.name == 'Mouse'
        item.price == 29.99
        item.category == 'Accessories'
    }

    def 'test insertAndGetKeys returns all generated keys from batch insert'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)"
        def params = [
                'Printer', 199.99, 'Electronics',
                'Scanner', 149.99, 'Electronics',
                'External Drive', 89.99, 'Storage'
        ]

        when:
        def generatedKeys = zql.insertMulti(sql, params)

        then:
        generatedKeys != null
        generatedKeys.size() == 3
        generatedKeys.every { it instanceof Integer }
        generatedKeys.every { (Integer) it > 0 }

        // Verify the records were inserted
        def items = zql.rows(Mappers.toMap(), "SELECT * FROM items WHERE id IN (?, ?, ?)", generatedKeys)
        items.size() == 3
        items.find { it.name == 'Printer' }.price == 199.99
        items.find { it.name == 'Scanner' }.price == 149.99
        items.find { it.name == 'External Drive' }.category == 'Storage'
    }

    def 'test insertAndGetKeys with varargs parameters'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"

        when:
        def generatedKeys = zql.insertMulti(sql, 'Scanner', 149.99, 'Electronics')

        then:
        generatedKeys != null
        generatedKeys.size() == 1
        generatedKeys[0] instanceof Integer
        (Integer) generatedKeys[0] > 0

        // Verify the record was inserted
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM items WHERE id = ?", [generatedKeys[0]])
        item.name == 'Scanner'
        item.price == 149.99
        item.category == 'Electronics'
    }

    def 'test insertAndGetKeys with Query object for multiple records'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return """INSERT INTO items (name, price, category) 
                          VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)"""
            }

            @Override
            List<Object> getParams() {
                return [
                        'Headphones', 89.99, 'Audio',
                        'Speakers', 129.99, 'Audio',
                        'Microphone', 59.99, 'Audio'
                ]
            }
        }

        when:
        def generatedKeys = zql.insertMulti(query)

        then:
        generatedKeys != null
        generatedKeys.size() == 3
        generatedKeys.every { it instanceof Integer }
        generatedKeys.every { (Integer) it > 0 }

        // Verify the records were inserted
        def items = zql.rows(Mappers.toMap(), "SELECT * FROM items WHERE id IN (?, ?, ?)", generatedKeys)
        items.size() == 3
        items.find { it.name == 'Headphones' }.price == 89.99
        items.find { it.name == 'Speakers' }.price == 129.99
        items.find { it.name == 'Microphone' }.category == 'Audio'
    }

    def 'test execute for DDL statement'() {
        given:
        def zql = db.ezySql().zql
        def createTableSql = """
            CREATE TABLE IF NOT EXISTS test_execute (
                id INTEGER PRIMARY KEY,
                name VARCHAR(100),
                created_at TIMESTAMP
            )
        """

        when:
        def result = zql.execute(createTableSql, [])

        and: 'insert some data to verify table was created'
        zql.update("INSERT INTO test_execute (id, name, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", [1, 'Test Item'])

        then:
        !result // DDL statements return false for execute()

        and: 'we can query the new table'
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM test_execute WHERE id = ?", [1])
        item.id == 1
        item.name == 'Test Item'
    }

    def 'test execute for insert statement'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"
        def params = ['Tablet', 299.99, 'Electronics']

        when:
        def result = zql.execute(sql, params)

        then:
        !result // INSERT statements typically return false

        and: 'the item was inserted'
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM items WHERE name = ?", ['Tablet'])
        item.name == 'Tablet'
        item.price == 299.99
        item.category == 'Electronics'
    }

    def 'test execute for select statement'() {
        given:
        def zql = db.ezySql().zql
        def sql = "SELECT * FROM offices WHERE officeCode = ?"
        def params = [1]

        when:
        def result = zql.execute(sql, params)

        then:
        result // SELECT statements return true
    }

    def 'test execute with Query object'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"
            }

            @Override
            List<Object> getParams() {
                return ['Smart Watch', 199.99, 'Wearables']
            }
        }

        when:
        def result = zql.execute(query)

        then:
        !result // INSERT statements typically return false

        and: 'the item was inserted'
        def item = zql.oneRow(Mappers.toMap(), "SELECT * FROM items WHERE name = ?", ['Smart Watch'])
        item.name == 'Smart Watch'
        item.price == 199.99
        item.category == 'Wearables'
    }

    def 'test execute for multiple statements in batch'() {
        given:
        def zql = db.ezySql().zql
        def sql = """
            CREATE TABLE IF NOT EXISTS batch_test (
                id INTEGER PRIMARY KEY,
                description TEXT
            );
            INSERT INTO batch_test (id, description) VALUES (1, 'First item');
            INSERT INTO batch_test (id, description) VALUES (2, 'Second item');
        """

        when:
        def result = zql.execute(sql, [])

        then:
        !result // Multiple statements typically return false

        and: 'all statements executed successfully'
        def items = zql.rows(Mappers.toMap(), "SELECT * FROM batch_test ORDER BY id")
        items.size() == 2
        items[0].id == 1
        items[0].description == 'First item'
        items[1].id == 2
        items[1].description == 'Second item'
    }

    def 'test rows with mapper and Query object'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "SELECT * FROM offices WHERE officeCode = ?"
            }

            @Override
            List<Object> getParams() {
                return [1]
            }
        }

        when:
        def offices = zql.rows(Mappers.toMap(), query)

        then:
        offices.size() == 1
        offices[0].officeCode == '1'
        offices[0].country == 'US' // After update in previous tests
        offices[0].addressLine1 == 'Kampala'
    }

    def 'test oneRow with mapper and Query object'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "SELECT * FROM offices WHERE officeCode = ?"
            }

            @Override
            List<Object> getParams() {
                return [3]
            }
        }

        when:
        def office = zql.oneRow(Mappers.toMap(), query)

        then:
        office != null
        office.officeCode == '3'
        office.country == 'UK' // After update in previous tests
        office.addressLine1 == 'Dar es Salaam'
    }

    def 'test oneRow with Query object returns null when no records found'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "SELECT * FROM offices WHERE officeCode = ?"
            }

            @Override
            List<Object> getParams() {
                return [99]
            }
        }

        when:
        def office = zql.oneRow(Mappers.toMap(), query)

        then:
        office == null
    }



    def 'test one method with Query object'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "SELECT COUNT(*) FROM offices"
            }

            @Override
            List<Object> getParams() {
                return []
            }
        }

        when:
        def count = zql.one(Integer.class, query)

        then:
        count == 5 // After insertions in previous tests
    }

    def 'test batch insert uses binder registry'() {
        given:
        def zql = db.ezySql().zql.withBinder(String, { ps, i, v ->
            ps.setString(i, v.toUpperCase())
        } as ParameterBinder)
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"
        def paramSets = [
                ['batch item 1', 10.00, 'bindercat'],
                ['batch item 2', 20.00, 'bindercat'],
                ['batch item 3', 30.00, 'bindercat'],
        ]

        when:
        def results = zql.batch(sql, paramSets)

        then:
        results.length == 3
        results.every { it == 1 }

        and:
        def items = db.ezySql().zql.rows(Mappers.toMap(), "SELECT * FROM items WHERE category = ? ORDER BY name", ['BINDERCAT'])
        items.size() == 3
        items[0].name == 'BATCH ITEM 1'
        items[1].name == 'BATCH ITEM 2'
        items[2].name == 'BATCH ITEM 3'
    }

    def 'test batch update'() {
        given:
        def zql = db.ezySql().zql
        zql.execute("CREATE TABLE IF NOT EXISTS batch_update_test (id INT PRIMARY KEY, name VARCHAR(100))", [])
        zql.update("INSERT INTO batch_update_test (id, name) VALUES (?, ?)", [1, 'A'])
        zql.update("INSERT INTO batch_update_test (id, name) VALUES (?, ?)", [2, 'B'])

        when:
        def results = zql.batch("UPDATE batch_update_test SET name = ? WHERE id = ?", [
                ['A-Updated', 1],
                ['B-Updated', 2],
        ])

        then:
        results.length == 2
        results.every { it == 1 }

        and:
        def rows = zql.rows(Mappers.toMap(), "SELECT * FROM batch_update_test ORDER BY id")
        rows[0].name == 'A-Updated'
        rows[1].name == 'B-Updated'

        cleanup:
        zql.execute("DROP TABLE IF EXISTS batch_update_test", [])
    }

    def 'test batch with empty param sets'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"

        when:
        def results = zql.batch(sql, [])

        then:
        results.length == 0
    }

    def 'test batch with list of Query objects'() {
        given:
        def zql = db.ezySql().zql
        def queries = (1..3).collect { i ->
            new Query() {
                String getSql() { "INSERT INTO items (name, price, category) VALUES (?, ?, ?)" }
                List<Object> getParams() { ["Query Item $i" as String, i * 10.0, 'QueryBatch'] }
            }
        }

        when:
        def results = zql.batch(queries)

        then:
        results.length == 3
        results.every { it == 1 }

        and:
        def items = zql.rows(Mappers.toMap(), "SELECT * FROM items WHERE category = ? ORDER BY name", ['QueryBatch'])
        items.size() == 3
        items[0].name == 'Query Item 1'
        items[1].name == 'Query Item 2'
        items[2].name == 'Query Item 3'
    }

    def 'test batch with empty list of Query objects'() {
        given:
        def zql = db.ezySql().zql

        when:
        def results = zql.batch([] as List<Query>)

        then:
        results.length == 0
    }

    def 'test batchInsert returns generated keys'() {
        given:
        def zql = db.ezySql().zql
        def sql = "INSERT INTO items (name, price, category) VALUES (?, ?, ?)"
        def paramSets = [
                ['BatchIns 1', 10.00, 'BatchIns'],
                ['BatchIns 2', 20.00, 'BatchIns'],
                ['BatchIns 3', 30.00, 'BatchIns'],
        ]

        when:
        def keys = zql.batchInsert(sql, paramSets)

        then:
        keys.size() == 3
        keys.every { it instanceof Integer && (Integer) it > 0 }

        and:
        def items = zql.rows(Mappers.toMap(), "SELECT * FROM items WHERE category = ? ORDER BY name", ['BatchIns'])
        items.size() == 3
        items[0].name == 'BatchIns 1'
        items[1].name == 'BatchIns 2'
        items[2].name == 'BatchIns 3'
    }

    def 'test batchInsert with list of Query objects'() {
        given:
        def zql = db.ezySql().zql
        def queries = (1..3).collect { i ->
            new Query() {
                String getSql() { "INSERT INTO items (name, price, category) VALUES (?, ?, ?)" }
                List<Object> getParams() { ["QBatchIns $i" as String, i * 10.0, 'QBatchIns'] }
            }
        }

        when:
        def keys = zql.batchInsert(queries)

        then:
        keys.size() == 3
        keys.every { it instanceof Integer && (Integer) it > 0 }

        and:
        def items = zql.rows(Mappers.toMap(), "SELECT * FROM items WHERE category = ? ORDER BY name", ['QBatchIns'])
        items.size() == 3
    }

    def 'test batchInsert with empty params returns empty list'() {
        given:
        def zql = db.ezySql().zql

        expect:
        zql.batchInsert("INSERT INTO items (name, price, category) VALUES (?, ?, ?)", []).isEmpty()
        zql.batchInsert([] as List<Query>).isEmpty()
    }

    def 'test query method with Query object and result set consumer'() {
        given:
        def zql = db.ezySql().zql
        def query = new Query() {
            @Override
            String getSql() {
                return "SELECT * FROM offices ORDER BY officeCode"
            }

            @Override
            List<Object> getParams() {
                return []
            }
        }

        when:
        def result = zql.query(query, { rs ->
            def offices = []
            while (rs.next()) {
                offices << [code: rs.getString("officeCode"), country: rs.getString("country")]
            }
            return offices
        })

        then:
        result.size() >= 4
        result[0].code == '1'
        result[0].country == 'US'
    }
}
