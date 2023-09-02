package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtils {
  private JdbcUtils() {}

  public static <T> List<T> mapData(Mappers.ResultsMapper<T> mapper, ResultSet resultSet) {
    return mapData(mapper, resultSet, Integer.MAX_VALUE);
  }

  public static <T> List<T> mapData(
      Mappers.ResultsMapper<T> mapper, ResultSet resultSet, int numRecords) {

    List<Zql.Column> columns = getColumns(resultSet);
    List<T> data = new ArrayList<>();
    int count = 0;
    while (count < numRecords && next(resultSet)) {
      try {
        data.add(mapper.mapRow(count, columns, resultSet));
      } catch (Exception e) {
        throw new UnCaughtException("Error mapping row", e);
      }
    }
    return data;
  }

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
