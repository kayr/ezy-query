package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtils {
  private JdbcUtils() {}

  public static <T> List<T> mapData(Mappers.ResultsMapper<T> mapper, ResultSet resultSet) {
    return mapData(mapper, resultSet, Integer.MAX_VALUE);
  }

  public static <T> List<T> mapData(
      Mappers.ResultsMapper<T> mapper, ResultSet resultSet, int numRecords) {
    try {
      List<Zql.Column> columns = Zql.getColumns(resultSet);
      List<T> data = new ArrayList<>();
      int count = 0;
      while (count < numRecords && resultSet.next()) {
        T t = mapper.mapRow(count, columns, resultSet);
        data.add(t);
      }
      return data;
    } catch (Exception e) {
      throw new UnCaughtException("Error mapping data", e);
    }
  }
}
