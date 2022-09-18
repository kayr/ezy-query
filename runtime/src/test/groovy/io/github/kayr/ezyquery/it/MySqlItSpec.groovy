package io.github.kayr.ezyquery.it

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.kayr.ezyquery.EzySql
import io.github.kayr.ezyquery.api.FilterParams
import io.github.kayr.ezyquery.api.cnd.Cnd
import io.github.kayr.ezyquery.gen.QueryGen
import io.github.kayr.ezyquery.sql.ConnectionProvider
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection

//import org.testcontainers.db.AbstractContainerDatabaseTest;

class MySqlItSpec extends Specification {

    def setup() {

    }

    def xxxxx() {
        def sql = '''SELECT
    c.customerName   AS customerName,
    e.employeeNumber AS employeeRep,
    o.addressLine1   AS employeeOffice,
    o.country        AS employeeCounty
FROM offices o
    LEFT JOIN employees e ON o.officeCode = e.officeCode
    LEFT JOIN customers c ON e.employeeNumber = c.salesRepEmployeeNumber'''

        when:
        new QueryGen("io.github.kayr.ezyquery.it", "CustomerReps", sql)
                .writeTo("/home/kayr/var/code/prsnl/ezy-query/runtime/src/test/groovy")

        then:
        1 == 1
    }

    def "test i can do this"() {
        given:
        EzySql ezql = ezySql()

        when:
        def list = ezql.list(CustomerReps.Q,
                FilterParams.neww()
                        .where(
                                Cnd.isNotNull(CustomerReps.FIELD_CUSTOMER_NAME)))
        println list

        then:

        1 == 1
    }

    private EzySql ezySql() {
        def source = getDataSource()

        ConnectionProvider p = new ConnectionProvider() {
            @Override
            Connection getConnection() {
                return source.getConnection()
            }

            @Override
            void closeConnection(Connection connection) {
                connection.close()
            }
        }

        def ezql = EzySql.withProvider(p)
        ezql
    }


    protected DataSource getDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/classicmodels");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("pass");
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        return new HikariDataSource(hikariConfig);
    }

}
