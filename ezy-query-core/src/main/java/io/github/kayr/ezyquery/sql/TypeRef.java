package io.github.kayr.ezyquery.sql;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.Getter;

@Getter
public abstract class TypeRef<T> {
  private final Type type;

  protected TypeRef() {
    Type superclass = getClass().getGenericSuperclass();
    if (!(superclass instanceof ParameterizedType)) {
      throw new IllegalArgumentException("TypeReference must be instantiated with a generic type");
    }
    this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
  }
}
