package io.github.kayr.ezyquery.sql;

import io.github.kayr.ezyquery.api.UnCaughtException;
import io.github.kayr.ezyquery.util.ReflectionUtil;
import io.github.kayr.ezyquery.util.ThrowingFunction;
import io.github.kayr.ezyquery.util.ThrowingSupplier;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Simple mapper to convert a ResultSet to a List of Objects */
public interface Mappers {

  interface RowMapper<T> {
    T mapRow(int rowIndex, List<ColumnInfo> columns, ResultSet rs) throws Exception;

    default T mapRowUnChecked(int rowIndex, List<ColumnInfo> columns, ResultSet rs) {
      try {
        return mapRow(rowIndex, columns, rs);
      } catch (RuntimeException e) {
        throw e;
      } catch (Throwable e) {
        throw new UnCaughtException("Error mapping row", e);
      }
    }
  }

  interface CellMapper<T> {
    void set(ColumnInfo column, T obj, Object cellValue) throws Exception;
  }

  static <T> List<T> resultSetToList(ResultSet resultSet, RowMapper<T> mapper) {
    return resultSetToList(resultSet, Integer.MAX_VALUE, mapper);
  }

  static <T> List<T> resultSetToList(ResultSet resultSet, int limit, RowMapper<T> mapper) {
    List<ColumnInfo> columns = JdbcUtils.getColumns(resultSet);
    List<T> data = new ArrayList<>();
    int count = 0;
    while (count < limit && JdbcUtils.next(resultSet)) {
      data.add(mapper.mapRowUnChecked(count++, columns, resultSet));
    }
    return data;
  }

  static <T> RowMapper<T> toObject(ThrowingSupplier<T> factory, CellMapper<T> setter) {
    return (rowIndex, columns, rs) -> {
      T obj = factory.get();
      for (ColumnInfo column : columns) {
        setter.set(column, obj, rs.getObject(column.getLabel()));
      }
      return obj;
    };
  }

  static <T> RowMapper<T> toClass(Class<T> target) {
    if (DynamicFieldSetter.class.isAssignableFrom(target)) {
      return toObject(
          () -> ReflectionUtil.construct(target),
          (col, obj, cellValue) -> ((DynamicFieldSetter) obj).setField(col.getLabel(), cellValue));
    }
    return toObject(
        () -> ReflectionUtil.construct(target),
        (col, obj, cellValue) -> {
          java.lang.reflect.Field field = ReflectionUtil.getField(target, col.getLabel());
          if (field != null) ReflectionUtil.setNonSyntheticField(obj, field, cellValue);
        });
  }

  static RowMapper<Map<String, Object>> toMap() {
    return toObject(
        LinkedHashMap::new, (column, obj, cellValue) -> obj.put(column.getLabel(), cellValue));
  }

  /**
   * Create a RowMapper for a specific class with custom field mappers.
   *
   * @param target the target class
   * @param fieldMappers a map of type-to-converter function. Use {@link TypeRef} to capture generic
   *     types.
   * @param <T> the target type
   * @return a RowMapper
   */
  static <T> RowMapper<T> toObject(
      Class<T> target, Map<? extends Type, ThrowingFunction<Object, Object>> fieldMappers) {

    boolean isDynamic = DynamicFieldSetter.class.isAssignableFrom(target);

    return (rowIndex, columns, rs) -> {
      T obj = ReflectionUtil.construct(target);
      for (ColumnInfo column : columns) {
        String label = column.getLabel();
        Object cellValue = rs.getObject(label);

        Field targetField = ReflectionUtil.getField(target, label);

        if (targetField == null) {
          if (isDynamic) ((DynamicFieldSetter) obj).setField(label, cellValue);
          continue;
        }

        ThrowingFunction<Object, Object> converter = fieldMappers.get(targetField.getGenericType());
        if (converter == null) {
          converter = fieldMappers.get(targetField.getType());
        }

        Object convertedValue = converter != null ? converter.apply(cellValue) : cellValue;

        if (isDynamic) ((DynamicFieldSetter) obj).setField(label, convertedValue);
        else ReflectionUtil.setNonSyntheticField(obj, targetField, convertedValue);
      }
      return obj;
    };
  }
}
