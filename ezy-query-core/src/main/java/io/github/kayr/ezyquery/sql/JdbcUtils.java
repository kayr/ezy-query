package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Utility class for JDBC to make it easier to work with JDBC checked exceptions. */
public class JdbcUtils {
  private JdbcUtils() {}

  public static List<ColumnInfo> getColumns(ResultSet resultSet) {
    List<ColumnInfo> columns = new ArrayList<>();
    ResultSetMetaData metaData = getMetaData(resultSet);
    for (int i = 1; i <= getColumnCount(metaData); i++) {
      String columnName = getColumnName(metaData, i);
      String columnLabel = getColumnLabel(metaData, i);
      columns.add(new ColumnInfo(columnName, columnLabel));
    }
    return columns;
  }

  public static ResultSetMetaData getMetaData(ResultSet resultSet) {
    try {
      return resultSet.getMetaData();
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting metadata", e);
    }
  }

  public static int getColumnCount(ResultSetMetaData metaData) {
    try {
      return metaData.getColumnCount();
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting column count", e);
    }
  }

  public static String getColumnName(ResultSetMetaData metaData, int index) {
    try {
      return metaData.getColumnName(index);
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting column name", e);
    }
  }

  public static String getColumnLabel(ResultSetMetaData metaData, int index) {
    try {
      return metaData.getColumnLabel(index);
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting column label", e);
    }
  }

  public static boolean next(ResultSet resultSet) {
    try {
      return resultSet.next();
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting next row", e);
    }
  }

  public static void setObject(PreparedStatement preparedStatement, int index, Object value) {
    try {
      preparedStatement.setObject(index, value);
    } catch (SQLException e) {
      throw new UnCaughtException("Error setting object on statement", e);
    }
  }

  public static Object getObject(ResultSet resultSet, int index) {
    try {
      return resultSet.getObject(index);
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting object from result set", e);
    }
  }

  public static PreparedStatement preparedStatement(Connection connection, String sql) {
    try {
      return connection.prepareStatement(sql);
    } catch (SQLException e) {
      throw new UnCaughtException("Error creating prepared statement", e);
    }
  }

  public static PreparedStatement preparedStatementWithKeys(Connection connection, String sql) {
    try {
      return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    } catch (SQLException e) {
      throw new UnCaughtException("Error creating prepared statement with keys", e);
    }
  }

  public static Integer executeUpdate(PreparedStatement statement) {
    try {
      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new UnCaughtException("Error executing update", e);
    }
  }

  public static ResultSet executeQuery(PreparedStatement statement) {
    try {
      return statement.executeQuery();
    } catch (SQLException e) {
      throw new UnCaughtException("Error executing query", e);
    }
  }

  public static Object getSingleGeneratedKey(ResultSet generatedKeys) {
    try {
      if (generatedKeys.next()) {
        return generatedKeys.getObject(1);
      }
      return null;
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting single generated key", e);
    }
  }

  public static List<Object> getAllGeneratedKeys(ResultSet generatedKeys) {
    try {
      List<Object> keys = new ArrayList<>();
      while (generatedKeys.next()) {
        keys.add(generatedKeys.getObject(1));
      }
      return keys;
    } catch (SQLException e) {
      throw new UnCaughtException("Error getting all generated keys", e);
    }
  }
}
