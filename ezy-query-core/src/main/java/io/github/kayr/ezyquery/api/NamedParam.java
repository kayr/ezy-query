package io.github.kayr.ezyquery.api;

@lombok.Getter
public class NamedParam {
  private final String name;

  protected NamedParam(String name) {
    this.name = name;
  }

  public static NamedParam of(String name) {
    return new NamedParam(name);
  }
}
