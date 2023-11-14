package io.github.kayr.ezyquery.api;

import java.util.List;

@lombok.Getter
public class NamedCriteriaParam extends NamedParam {
  private final List<Field<?>> fields;

  protected NamedCriteriaParam(String name, List<Field<?>> fields) {
    super(name);
    this.fields = fields;
  }

  public static NamedCriteriaParam of(String name, List<Field<?>> fields) {
    return new NamedCriteriaParam(name, fields);
  }
}
