package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Utility class for JDBC to make it easier to work with JDBC checked exceptions. */
public class JdbcUtils {
  private JdbcUtils() {}

  public static List<Zql.Column> getColumns(ResultSet resultSet) {
    List<Zql.Column> columns = new ArrayList<>();
    ResultSetMetaData metaData = getMetaData(resultSet);
    for (int i = 1; i <= getColumnCount(resultSet); i++) {
      String columnName = getColumnName(metaData, i);
      String columnLabel = getColumnLabel(metaData, i);
      columns.add(new Zql.Column(columnName, columnLabel));
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

  public static int getColumnCount(ResultSet resultSet) {
    try {
      return resultSet.getMetaData().getColumnCount();
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
}
