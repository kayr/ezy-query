package io.github.kayr.ezyquery.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BinderRegistry {
  private final Map<Class<?>, ParameterBinder<?>> binders;

  public BinderRegistry() {
    this(Collections.emptyMap());
  }

  private BinderRegistry(Map<Class<?>, ParameterBinder<?>> binders) {
    this.binders = Collections.unmodifiableMap(new HashMap<>(binders));
  }

  public <T> BinderRegistry withBinder(Class<T> type, ParameterBinder<?> binder) {
    Map<Class<?>, ParameterBinder<?>> newBinders = new HashMap<>(this.binders);
    newBinders.put(type, binder);

    return new BinderRegistry(newBinders);
  }

  public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
    if (value == null) {
      ps.setObject(index, null);
      return;
    }

    if (binders.isEmpty()) {
      JdbcUtils.setObject(ps, index, value);
      return;
    }

    @SuppressWarnings("unchecked")
    ParameterBinder<Object> binder = (ParameterBinder<Object>) binders.get(value.getClass());
    if (binder != null) {
      binder.bind(ps, index, value);
    } else {
      JdbcUtils.setObject(ps, index, value);
    }
  }
}
