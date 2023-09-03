package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import io.github.kayr.ezyquery.util.ReflectionUtil;
import io.github.kayr.ezyquery.util.ThrowingSupplier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Simple mapper to convert a ResultSet to a List of Objects */
public interface Mappers<T> {

  interface Row<T> {
    T mapRow(int rowIndex, List<Zql.Column> columns, ResultSet rs) throws Exception;

    default T mapRowUnChecked(int rowIndex, List<Zql.Column> columns, ResultSet rs) {
      try {
        return mapRow(rowIndex, columns, rs);
      } catch (Exception e) {
        throw new UnCaughtException("Error mapping row", e);
      }
    }
  }

  interface Cell<T> {
    void set(Zql.Column column, T obj, Object cellValue) throws Exception;
  }

  static <T> List<T> resultSetToList(ResultSet resultSet, int limit, Row<T> mapper) {
    List<Zql.Column> columns = JdbcUtils.getColumns(resultSet);
    List<T> data = new ArrayList<>();
    int count = 0;
    while (count < limit && JdbcUtils.next(resultSet)) {
      data.add(mapper.mapRowUnChecked(count, columns, resultSet));
    }
    return data;
  }

  static <T> Row<T> toObject(ThrowingSupplier<T> factory, Cell<T> setter) {
    return (rowIndex, columns, rs) -> {
      T obj = factory.get();
      for (Zql.Column column : columns) {
        setter.set(column, obj, rs.getObject(column.getLabel()));
      }
      return obj;
    };
  }

  static <T> Row<T> toClass(Class<T> target) {
    return toObject(
        () -> ReflectionUtil.construct(target),
        (col, obj, cellValue) -> {
          java.lang.reflect.Field field = ReflectionUtil.getField(target, col.getLabel());
          if (field != null) ReflectionUtil.setNonSyntheticField(obj, field, cellValue);
        });
  }
}
