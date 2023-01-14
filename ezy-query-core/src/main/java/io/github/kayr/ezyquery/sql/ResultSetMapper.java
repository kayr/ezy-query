package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

public class ResultSetMapper<T> {

  Class<T> targetClass;

  public ResultSetMapper(Class<T> targetClass) {
    this.targetClass = targetClass;
  }

  public static <T> ResultSetMapper<T> forClass(Class<T> targetClass) {
    return new ResultSetMapper<>(targetClass);
  }

  public T mapRow(ResultSet rs, List<Column> columns) {
    T obj = construct();
    // map object to result set
    for (Column column : columns) {
      Field field = ReflectionUtil.getField(targetClass, column.getLabel());
      if (field != null) {
        setFieldValue(rs, obj, field);
      }
    }

    return obj;
  }

  private void setFieldValue(ResultSet rs, T obj, Field field) {
    ReflectionUtil.makeAccessible(field);
    try {
      if (field.isSynthetic()) return;
      ReflectionUtil.setField(field, obj, rs.getObject(field.getName()));
    } catch (Exception e) {
      throw new UnsupportedOperationException("Unable to map " + targetClass.getName(), e);
    }
  }

  private T construct() {
    T t = null;
    try {
      t = targetClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new UnsupportedOperationException("Unable to instantiate " + targetClass.getName(), e);
    }
    return t;
  }

  @lombok.AllArgsConstructor
  @lombok.Getter
  public static class Column {
    private String name;
    private String label;
  }
}
