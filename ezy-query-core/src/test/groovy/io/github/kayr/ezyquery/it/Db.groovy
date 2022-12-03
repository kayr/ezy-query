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
import io.github.kayr.ezyquery.sql.ConnectionProvider

import java.sql.Connection


class Db {


    HikariDataSource ds

    def init() {
        if (ds == null) {
            //initialise h2 inmemory database
            def config = new HikariConfig()
            config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            config.setUsername("sa")
            config.setPassword("")
            config.setMaximumPoolSize(1)
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
                of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT).
                withSqlRenderer(Db.h2_RENDERER)


        def csvData = FuzzyCSVTable.fromMapList(data).name(name)

        withDb { csvData.dbExport(it, exportParams) }

    }


    def close() {
        if (ds != null) {
            ezySql().zql.executeUpdate( "DROP ALL OBJECTS")
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
}
