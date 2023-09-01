package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

public interface ResultsMapper<T> {
  T mapRow(int rowIndex, List<Zql.Column> columns, ResultSet rs) throws Exception;

  static <T> ResultsMapper<T> usingReflection(Class<T> targetClass) {
    return (rowIndex, columns, rs) -> {
      T obj = ReflectionUtil.construct(targetClass);
      for (Zql.Column column : columns) {
        Field field = ReflectionUtil.getField(targetClass, column.getLabel());
        if (field != null) {
          ReflectionUtil.setFieldValue(rs, obj, field);
        }
      }
      return obj;
    };
  }
}
