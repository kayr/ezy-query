package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetMapper<T> {

  Class<T> targetClass;

  public ResultSetMapper(Class<T> targetClass) {
    this.targetClass = targetClass;
  }

  public static <T> ResultSetMapper<T> forClass(Class<T> targetClass) {
    return new ResultSetMapper<>(targetClass);
  }


  public T mapRow(ResultSet rs, int rowNum) {
    T obj = construct();
    // map object to result set
    ReflectionUtils.doWithFields(targetClass, field -> setFieldValue(rs, obj, field));

    return obj;
  }

  private void setFieldValue(ResultSet rs, T obj, Field field) {
    ReflectionUtils.makeAccessible(field);
    try {
      ReflectionUtils.setField(field, obj, rs.getObject(field.getName()));
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
}
