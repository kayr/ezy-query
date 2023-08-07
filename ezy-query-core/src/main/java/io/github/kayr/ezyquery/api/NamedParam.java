package io.github.kayr.ezyquery.api;

@lombok.Getter
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class NamedParam {
  private String name;

  public static NamedParam of(String name) {
    return new NamedParam(name);
  }
}
