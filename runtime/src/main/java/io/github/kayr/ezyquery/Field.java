package io.github.kayr.ezyquery;

@lombok.Builder(toBuilder = true)
@lombok.Getter
public class Field {
  private String sqlField;
  private String alias;
}
