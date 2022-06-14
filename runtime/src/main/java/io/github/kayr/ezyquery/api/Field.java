package io.github.kayr.ezyquery.api;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.Builder(toBuilder = true)
public class Field {
  private String sqlField;
  private String alias;
}
