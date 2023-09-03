package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import io.github.kayr.ezyquery.util.Elf;
import io.github.kayr.ezyquery.util.ThrowingConsumer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class Zql {

    private final ConnectionProvider connectionProvider;

    public Zql(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public <T> List<T> rows(Mappers.Row<T> mapper, String sql, List<Object> params) {
        try (DbReSources resultSet = rows(sql, params)) {
            return Mappers.mapResultSet(resultSet.resultSet, Integer.MAX_VALUE, mapper);
        }
    }

    public <T> List<T> rows(Mappers.Row<T> mapper, String sql, Object... params) {
        try (DbReSources resultSet = rows(sql, params)) {
            return Mappers.mapResultSet(resultSet.resultSet, Integer.MAX_VALUE, mapper);
        }
    }

    public <T> T firstRow(Mappers.Row<T> mapper, String sql, List<Object> params) {
        try (DbReSources dbReSources = rows(sql, params)) {
            List<T> results = Mappers.mapResultSet(dbReSources.resultSet, 1, mapper);
            assertNoMoreRecords(dbReSources.resultSet);
            return results.isEmpty() ? null : results.get(0);
        }
    }

    private static void assertNoMoreRecords(ResultSet dbReSources) {
        if (JdbcUtils.next(dbReSources)) {
            throw new IllegalArgumentException("More than one row returned");
        }
    }

    void query(String sql, List<Object> params, ThrowingConsumer<DbReSources> resultSetConsumer) {
        try (DbReSources resultSet = rows(sql, params)) {
            resultSetConsumer.accept(resultSet);
        } catch (Exception e) {
            throw new UnCaughtException("Error executing query", e);
        }
    }

    private DbReSources rows(String sql, List<Object> params) {

        return rows(sql, params.toArray());
    }

    public <T> T one(Class<T> clazz, String sql, List<Object> params) {
        try (DbReSources r = rows(sql, params)) {

            ResultSet resultSet = r.resultSet;

            T result = null;
            if (JdbcUtils.next(resultSet)) {
                //noinspection unchecked
                result = (T) JdbcUtils.getObject(resultSet, 1);
            }

            assertNoMoreRecords(resultSet);

            return result;
        }
    }

    private DbReSources rows(String sql, Object... params) {
        Connection connection = connectionProvider.getConnectionUnChecked();
        PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql);
        setValues(statement, params);
        ResultSet resultSet = JdbcUtils.executeQuery(statement);
        return new DbReSources(connection, statement, resultSet, connectionProvider);
    }

    public Integer executeUpdate(String sql, Object... params) {
        Connection connection = connectionProvider.getConnectionUnChecked();
        try {
            PreparedStatement statement = JdbcUtils.preparedStatement(connection, sql);
            setValues(statement, params);
            return JdbcUtils.executeUpdate(statement);
        } finally {
            closeConnection(connectionProvider, connection);
        }
    }

    private static void closeConnection(ConnectionProvider provider, Connection connection) {
        try {
            provider.closeConnection(connection);
        } catch (Exception e) {
            throw new UnCaughtException("Error closing connection", e);
        }
    }

    public static void setValues(PreparedStatement preparedStatement, Object... values) {
        for (int i = 0; i < values.length; i++) {
            JdbcUtils.setObject(preparedStatement, i + 1, values[i]);
        }
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    public static class Column {
        private String name;
        private String label;
    }

    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DbReSources implements AutoCloseable {
        private Connection connection;
        private Statement statement;
        private ResultSet resultSet;
        private ConnectionProvider connectionProvider;

        @Override
        public void close() {
            Elf.closeQuietly(resultSet);
            Elf.closeQuietly(statement);
            closeConnection(connectionProvider, connection);
        }
    }
}
