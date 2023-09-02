package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.ReflectionUtil;
import io.github.kayr.ezyquery.util.ThrowingSupplier;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

public interface Mappers<T> {

  interface ResultsMapper<T> {
    T mapRow(int rowIndex, List<Zql.Column> columns, ResultSet rs) throws Exception;
  }

  interface FieldSetter<T> {
    void set(Zql.Column column, T obj, Object value) throws Exception;
  }

  static <T> ResultsMapper<T> toObject(ThrowingSupplier<T> factory, FieldSetter<T> setter) {
    return (rowIndex, columns, rs) -> {
      T obj = factory.get();
      for (Zql.Column column : columns) {
        setter.set(column, obj, rs.getObject(column.getLabel()));
      }
      return obj;
    };
  }

  static <T> ResultsMapper<T> toClass(Class<T> target) {
    return toObject(
        () -> ReflectionUtil.construct(target),
        (col, obj, value) -> {
          Field field = ReflectionUtil.getField(target, col.getLabel());
          if (field != null) ReflectionUtil.setNonSyntheticField(obj, field, value);
        });
  }
}
