package io.github.kayr.ezyquery.api;

@lombok.AllArgsConstructor
@lombok.Getter
public class NamedParamValue {
  private NamedParam param;
  private Object value;
}
