package io.github.kayr.ezyquery.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil {

  private ReflectionUtil() {}

  private static final Map<Class<?>, Map<String, Field>> declaredFieldsCache =
      new ConcurrentHashMap<>(256);

  public static void setField(String field, Object target, Object value) {
    setField(getField(target.getClass(), field), target, value);
  }

  @lombok.SneakyThrows
  public static void setField(Field field, Object target, Object value) {
    makeAccessible(field);
    field.set(target, value); // NOSONAR
  }

  public static void makeAccessible(Field field) {
    if ((!Modifier.isPublic(field.getModifiers())
            || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
            || Modifier.isFinal(field.getModifiers()))
        && !field.isAccessible()) {
      field.setAccessible(true); // NOSONAR
    }
  }

  @lombok.SneakyThrows
  public static void doWithFields(Class<?> clazz, Callback<Field> callback) {
    Class<?> targetClass = clazz;
    do {
      Collection<Field> fields = getDeclaredFields(targetClass);
      for (Field field : fields) {
        callback.call(field);
      }
      targetClass = targetClass.getSuperclass();
    } while (targetClass != null && targetClass != Object.class);
  }

  private static Collection<Field> getDeclaredFields(Class<?> clazz) {
    Map<String, Field> fieldCache1 = getFieldCache(clazz);
    return fieldCache1.values();
  }

  private static Map<String, Field> getFieldCache(Class<?> clazz) {
    Map<String, Field> result = declaredFieldsCache.get(clazz);

    if (result != null) return result;

    Field[] declaredFields = getDeclareFields(clazz);
    Map<String, Field> fieldMap =
        declaredFields.length > 0 ? new HashMap<>(declaredFields.length) : Collections.emptyMap();
    declaredFieldsCache.put(clazz, fieldMap);

    for (Field field : declaredFields) {
      fieldMap.put(field.getName(), field);
    }

    return fieldMap;
  }

  private static Field[] getDeclareFields(Class<?> clazz) {
    try {
      return clazz.getDeclaredFields();
    } catch (Throwable ex) { // NOSONAR
      throw new IllegalStateException(
          "Failed to introspect Class ["
              + clazz.getName()
              + "] from ClassLoader ["
              + clazz.getClassLoader()
              + "]",
          ex);
    }
  }

  public static <T> Field getField(Class<T> targetClass, String fieldName) {
    Map<String, Field> fieldCache = getFieldCache(targetClass); // ensure introspection is done
    return fieldCache.get(fieldName);
  }

  public static <T> T construct(Class<T> targetClass1) {
    T t = null;
    try {
      t = targetClass1.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new UnsupportedOperationException("Unable to instantiate " + targetClass1.getName(), e);
    }
    return t;
  }

  public static void setFieldValue(ResultSet rs, Object obj, Field field) {
    makeAccessible(field);
    try {
      if (field.isSynthetic()) return;
      setField(field, obj, rs.getObject(field.getName()));
    } catch (Exception e) {
      throw new UnsupportedOperationException(
          "Unable to set field on :" + field.getName() + " for class: " + obj.getClass().getName(),
          e);
    }
  }
}
