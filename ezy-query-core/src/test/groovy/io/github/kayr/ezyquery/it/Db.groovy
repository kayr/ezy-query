package io.github.kayr.ezyquery.it

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import fuzzycsv.FuzzyCSVTable
import fuzzycsv.rdbms.DbExportFlags
import fuzzycsv.rdbms.ExportParams
import fuzzycsv.rdbms.FuzzyCSVDbExporter
import fuzzycsv.rdbms.stmt.DefaultSqlRenderer
import fuzzycsv.rdbms.stmt.SqlRenderer
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import io.github.kayr.ezyquery.EzySql

class Db {


    HikariDataSource ds

    def init() {
        if (ds == null) {
            //initialise h2 inmemory database
            def config = new HikariConfig()
            config.setJdbcUrl("jdbc:h2:mem:test;DATABASE_TO_LOWER=FALSE;DATABASE_TO_UPPER=FALSE;CASE_INSENSITIVE_IDENTIFIERS=FALSE")
            config.setUsername("sa")
            config.setPassword("")
            config.setMaximumPoolSize(2)
            config.setMinimumIdle(1)
            config.setConnectionTestQuery("SELECT 1")
            config.setDriverClassName("org.h2.Driver")
            ds = new HikariDataSource(config)
        }
    }


    def <T> T withDb(@ClosureParams(value = SimpleType, options = "java.sql.Connection") Closure<T> closure) {
        init()
        def connection = ds.connection
        try {
            closure(connection)
        } finally {
            connection.close()
        }
    }

    EzySql ezySql() {
        EzySql.withDataSource(ds)
    }

    def intoDb(List<Map> data, String name) {
        def exportParams = ExportParams.
                of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT)
                .withSqlRenderer(getH2_RENDERER())


        def csvData = FuzzyCSVTable.fromMapList(data).name(name)

        withDb {
            csvData.export().toDb().withDatasource(ds)
                    .withExportParams(exportParams)
                    .export()

        }
    }


    def close() {
        if (ds != null) {
            ezySql().zql.update("DROP ALL OBJECTS")
            ds.close()
        }
    }

    //poor mans H2 renderer.. :)
    static SqlRenderer H2_RENDERER = new DefaultSqlRenderer() {
        @Override
        String quoteName(String name) {
            return name;
        }

        @Override
        String toDataString(FuzzyCSVDbExporter.Column column) {
            if (column.type == 'bigint')
                return super.toDataString(new FuzzyCSVDbExporter.Column(name: column.name, type: column.type))

            return super.toDataString(column)

        }

        @Override
        String createTable(String tableName, List<FuzzyCSVDbExporter.Column> columns) {
            def table = super.createTable(tableName, columns)
            println("Creating table: [$tableName] ................")
            println(table)
            return table
        }
    }

    def insertData() {
        def offices = [
                [officeCode: '1', country: 'UG', addressLine1: 'Kampala'],
                [officeCode: '2', country: 'KE', addressLine1: 'Nairobi'],
                [officeCode: '3', country: 'TZ', addressLine1: 'Dar es Salaam'],
                [officeCode: '4', country: 'KE', addressLine1: 'Nairobi'],
        ]

        def employees = [
                [employeeNumber: '1', officeCode: '1', firstName: 'Kay'],
                [employeeNumber: '2', officeCode: '2', firstName: 'John'],
                [employeeNumber: '3', officeCode: '2', firstName: 'Jane'],
                [employeeNumber: '4', officeCode: '3', firstName: 'Doe']
        ]

        def customers = [
                [customerNumber: '1', customerName: 'Kay', salesRepEmployeeNumber: '1'],
                [customerNumber: '2', customerName: 'John', salesRepEmployeeNumber: '1'],
                [customerNumber: '3', customerName: 'Jane', salesRepEmployeeNumber: '1'],
                [customerNumber: '4', customerName: 'Doe', salesRepEmployeeNumber: '2'],
                [customerNumber: '5', customerName: 'Daniel', salesRepEmployeeNumber: '2']

        ]

        intoDb(offices, "offices")
        intoDb(employees, "employees")
        intoDb(customers, "customers")
        createItemsTableWithAutoId()
        return this
    }
    
    def createItemsTableWithAutoId() {
        withDb { connection ->
            def statement = connection.createStatement()
            // Create a table with auto-incrementing primary key
            statement.execute("""
                CREATE TABLE items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    category VARCHAR(50)
                )
            """)
            
            // Insert some initial data
            statement.execute("""
                INSERT INTO items (name, price, category) VALUES 
                ('Laptop', 999.99, 'Electronics'),
                ('Chair', 149.50, 'Furniture'),
                ('Book', 24.99, 'Office')
            """)
            
            statement.close()
        }
    }
}

