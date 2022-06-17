package io.github.kayr.ezyquery.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {

  private ReflectionUtils() {}

  private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

  private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>(256);

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
      Field[] fields = getDeclaredFields(targetClass);
      for (Field field : fields) {
        callback.call(field);
      }
      targetClass = targetClass.getSuperclass();
    } while (targetClass != null && targetClass != Object.class);
  }

  private static Field[] getDeclaredFields(Class<?> clazz) {
    Field[] result = declaredFieldsCache.get(clazz);
    if (result == null) {
      try {
        result = clazz.getDeclaredFields();
        declaredFieldsCache.put(clazz, (result.length == 0 ? EMPTY_FIELD_ARRAY : result));
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
    return result;
  }
}
