package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.api.cnd.IOperand;

@lombok.Getter
@lombok.AllArgsConstructor
public class Field<T> implements IOperand {
  private String sqlField;
  private String alias;
  private Class<T> dataType;

  public Field(String sqlField, String alias) {
    this.sqlField = sqlField;
    this.alias = alias;
    //noinspection unchecked
    this.dataType = (Class<T>) Object.class;
  }

  @Override
  public String toString() {
    return alias;
  }

  public static <T> Field<T> of(String sqlField, String alias, Class<T> dataType) {
    return new Field<>(sqlField, alias, dataType);
  }

  public Sort asc() {
    return Sort.by(this, Sort.DIR.ASC);
  }

  public Sort desc() {
    return Sort.by(this, Sort.DIR.DESC);
  }
}
