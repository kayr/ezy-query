package io.github.kayr.ezyquery.api;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.Builder(toBuilder = true)
public class Field<T> {
  private String sqlField;
  private String alias;
  private Class<T> dataType;

  public static <T> Field<T> of(String sqlField, String alias, Class<T> dataType) {
    return new Field<>(sqlField, alias, dataType);
  }

  public static Field<Object> of(String sqlField, String alias) {
    return of(sqlField, alias, Object.class);
  }


}
