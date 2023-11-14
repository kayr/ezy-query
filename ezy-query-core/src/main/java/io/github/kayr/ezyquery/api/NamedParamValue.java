package io.github.kayr.ezyquery.api;

@lombok.AllArgsConstructor
@lombok.Getter
public class NamedParamValue {
  private NamedParam param;
  private Object value;

  public static NamedParamValue of(String param, Object value) {
    return new NamedParamValue(NamedParam.of(param), value);
  }
}
