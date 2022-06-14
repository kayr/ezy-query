package io.github.kayr.ezyquery.api;

@lombok.Builder(toBuilder = true)
@lombok.Getter
public class Field {
  private String sqlField;
  private String alias;
}
